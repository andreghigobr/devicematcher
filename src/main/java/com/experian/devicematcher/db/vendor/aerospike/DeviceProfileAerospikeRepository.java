package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.*;
import com.aerospike.client.Record;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.repository.DeviceProfileRepository;
import com.aerospike.client.query.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.experian.devicematcher.db.vendor.aerospike.DeviceProfileBins.HIT_COUNT;
import static com.experian.devicematcher.db.vendor.aerospike.DeviceProfileBins.OS_NAME;

@Component
public class DeviceProfileAerospikeRepository implements DeviceProfileRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileAerospikeRepository.class);

    @Value("${aerospike.namespace}")
    private String namespace;

    @Value("${aerospike.set}")
    private String setName;

    private final Policy defaultPolicy;

    private final WritePolicy writePolicy;

    private final QueryPolicy queryPolicy;

    private final IAerospikeClient client;

    @Autowired
    public DeviceProfileAerospikeRepository(
            IAerospikeClient client,
            @Qualifier("aerospikeDefaultPolicy") Policy policy,
            @Qualifier("aerospikeWritePolicy") WritePolicy writePolicy,
            @Qualifier("aerospikeQueryPolicy") QueryPolicy queryPolicy
    ) {
        this.client = client;
        this.defaultPolicy = policy;
        this.writePolicy = writePolicy;
        this.queryPolicy = queryPolicy;
    }

    @Override
    public Optional<DeviceProfile> findDeviceById(String deviceId) {
        logger.info("Retrieving device by ID from Aerospike | deviceId={}", deviceId);

        Key key = new Key(namespace, setName, deviceId);
        Record record = client.get(defaultPolicy, key);

        if (record == null) {
            logger.debug("Device not found | deviceId={}", deviceId);
            return Optional.empty();
        }

        logger.debug("Device found | deviceId={}", deviceId);
        var device = DeviceProfileBins.from(record);
        return Optional.of(device);
    }

    @Override
    public List<DeviceProfile> findDevicesByOSName(String osName) {
        logger.info("Retrieving devices by OS from Aerospike | osName={}", osName);

        Statement statement = new Statement();
        statement.setNamespace(namespace);
        statement.setSetName(setName);
        statement.setFilter(Filter.equal(OS_NAME, osName));

        List<DeviceProfile> devices = new ArrayList<>();
        try (RecordSet recordSet = client.query(queryPolicy, statement)) {
            while (recordSet.next()) {
                Record record = recordSet.getRecord();
                var device = DeviceProfileBins.from(record);
                devices.add(device);
            }
        } catch (Exception ex) {
            logger.error("Error retrieving devices by OS from Aerospike | osName={} error={}", osName, ex.getMessage());
            throw ex;
        }

        return Collections.unmodifiableList(devices);
    }

    @Override
    public void deleteDeviceById(String deviceId) {
        logger.info("Deleting device by ID from Aerospike | deviceId={}", deviceId);
        Key key = new Key(namespace, setName, deviceId);
        boolean isDeleted = client.delete(writePolicy, key);

        if (isDeleted) {
            logger.debug("Device deleted successfully on Aerospike | deviceId={}", deviceId);
        } else {
            logger.warn("Device not found for deletion on Aerospike | deviceId={}", deviceId);
        }
    }

    @Override
    public void persistDevice(DeviceProfile device) {
        logger.info("Persisting device profile into Aerospike | device={}", device);
        Key key = new Key(namespace, setName, device.getDeviceId());
        client.put(writePolicy, key, DeviceProfileBins.toBins(device));
        logger.debug("Device device profile persisted into Aerospike | device={}", device);
    }

    @Override
    public long incrementHitCount(String deviceId) {
        logger.info("Incrementing device hit count on Aerospike | deviceId={}", deviceId);

        Key key = new Key(namespace, setName, deviceId);
        var record = client.operate(
                writePolicy, key,
                Operation.add(new Bin(HIT_COUNT, 1L)),
                Operation.get(HIT_COUNT)
        );

        var updatedHitCount = record.getLong(HIT_COUNT);
        logger.debug("Device HitCount updated | deviceId={} | updatedHitCount={}", deviceId, updatedHitCount);
        return updatedHitCount;
    }
}
