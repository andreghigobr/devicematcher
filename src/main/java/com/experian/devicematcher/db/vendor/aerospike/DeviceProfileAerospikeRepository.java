package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.Bin;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.Key;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DeviceProfileAerospikeRepository implements DeviceProfileRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileAerospikeRepository.class);

    @Value("${aerospike.namespace}")
    private String namespace;

    @Value("${aerospike.set}")
    private String setName;

    private final IAerospikeClient client;

    @Autowired
    public DeviceProfileAerospikeRepository(IAerospikeClient client) {
        this.client = client;
    }

    @Override
    public Optional<DeviceProfile> findDeviceById(String deviceId) {
        logger.info("Retrieving device by ID from Aerospike | deviceId={}", deviceId);

        Key key = new Key(namespace, setName, deviceId);
        Policy policy = new Policy();
        Record record = client.get(policy, key);

        if (record != null) {
            logger.info("Device found | deviceId={}", deviceId);

            var device = new DeviceProfile(
                record.getString("deviceId"),
                record.getLong("hitCount"),
                record.getString("osName"),
                record.getString("osVersion"),
                record.getString("browserName"),
                record.getString("browserVersion")
            );

            return Optional.of(device);
        }

        logger.info("Device not found | deviceId={}", deviceId);
        return Optional.empty();
    }

    @Override
    public List<DeviceProfile> findDevicesByOSName(String osName) {
        logger.info("Retrieving devices by OS from Aerospike | osName={}", osName);

        Statement statement = new Statement();
        statement.setNamespace(namespace);
        statement.setSetName(setName);
        statement.setFilter(Filter.equal("osName", osName));

        QueryPolicy policy = new QueryPolicy();
        policy.setMaxRecords(10_000);

        try (RecordSet recordSet = client.query(policy, statement)) {
            List<DeviceProfile> devices = new ArrayList<>();
            while (recordSet.next()) {
                Record record = recordSet.getRecord();
                var device = new DeviceProfile(
                    record.getString("deviceId"),
                    record.getLong("hitCount"),
                    record.getString("osName"),
                    record.getString("osVersion"),
                    record.getString("browserName"),
                    record.getString("browserVersion")
                );
                devices.add(device);
            }
            return devices;
        } catch (Exception e) {
            logger.error("Error retrieving devices by OS | osName={} error={}", osName, e.getMessage());
        }

        return List.of();
    }

    @Override
    public void deleteDeviceById(String deviceId) {
        logger.info("Deleting device by ID from Aerospike | deviceId={}", deviceId);
        Key key = new Key(namespace, setName, deviceId);
        WritePolicy policy = new WritePolicy();

        boolean isDeleted = client.delete(policy, key);

        if (isDeleted) {
            logger.info("Device deleted successfully | deviceId={}", deviceId);
        } else {
            logger.warn("Device not found for deletion | deviceId={}", deviceId);
        }
    }

    @Override
    public void persistDevice(DeviceProfile device) {
        logger.info("Persisting device profile into Aerospike | device={}", device);
        Key key = new Key(namespace, setName, device.getDeviceId());
        WritePolicy policy = new WritePolicy();

        Bin[] bins = new Bin[]{
            new Bin("deviceId", device.getDeviceId()),
            new Bin("hitCount", device.getHitCount()),
            new Bin("osName", device.getOsName()),
            new Bin("osVersion", device.getOsVersion()),
            new Bin("browserName", device.getBrowserName()),
            new Bin("browserVersion", device.getBrowserVersion()),
        };

        client.put(policy, key, bins);
    }
}
