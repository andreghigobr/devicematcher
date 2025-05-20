package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.*;
import com.aerospike.client.Record;
import com.aerospike.client.exp.Exp;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.repository.DeviceProfileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.experian.devicematcher.db.vendor.aerospike.DeviceProfileBins.*;

@Component
public class DeviceProfileAerospikeRepository implements DeviceProfileRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileAerospikeRepository.class);

    @Value("${aerospike.namespace}")
    private String namespace;

    @Value("${aerospike.set}")
    private String setName;

    private final IAerospikeClient client;

    private final AerospikePolicies policies;

    @Autowired
    public DeviceProfileAerospikeRepository(
        IAerospikeClient client,
        AerospikePolicies policies
    ) {
        this.client = client;
        this.policies = policies;
    }

    @Override
    public Optional<DeviceProfile> findDeviceProfileById(String deviceId) {
        logger.info("Retrieving device by ID from Aerospike | deviceId={}", deviceId);

        Key key = new Key(namespace, setName, deviceId);
        Record rec = client.get(policies.newDefaultPolicy(), key);

        if (rec == null) {
            logger.debug("Device not found | deviceId={}", deviceId);
            return Optional.empty();
        }

        var device = DeviceProfileBins.toEntity(rec);
        logger.debug("Device by id {} found | device={}", deviceId, device);
        return Optional.of(device);
    }

    @Override
    public List<DeviceProfile> findDeviceProfiles(UserAgent userAgent) {
        logger.info("Retrieving devices by User-Agent from Aerospike | userAgent={}", userAgent);

        var stmt = new Statement();
        stmt.setNamespace(namespace);
        stmt.setSetName(setName);
        var policy = new QueryPolicy(policies.newDefaultPolicy());
        policy.setMaxRecords(1L);

        policy.filterExp = Exp.build(
            Exp.and(
                Exp.eq(Exp.stringBin(OS_NAME), Exp.val(userAgent.osName().toLowerCase())),
                Exp.eq(Exp.stringBin(OS_VERSION), Exp.val(userAgent.osVersion())),
                Exp.eq(Exp.stringBin(BROWSER_NAME), Exp.val(userAgent.browserName().toLowerCase())),
                Exp.eq(Exp.stringBin(BROWSER_VERSION), Exp.val(userAgent.browserVersion()))
            )
        );

        var devices = new ArrayList<DeviceProfile>();
        try (RecordSet rs = client.query(policy, stmt)) {
            if (rs.next()) {
                Record rec = rs.getRecord();
                var device = DeviceProfileBins.toEntity(rec);
                devices.add(device);
            }
        }

        logger.debug("Devices found | total={} userAgent={}", devices.size(), userAgent);
        return Collections.unmodifiableList(devices);
    }

    @Override
    public List<DeviceProfile> findDeviceProfilesByOSName(String osName) {
        logger.info("Retrieving devices by OS from Aerospike | osName={}", osName);

        var stmt = new Statement();
        stmt.setNamespace(namespace);
        stmt.setSetName(setName);
        stmt.setFilter(Filter.equal(OS_NAME, osName.toLowerCase()));

        var policy = policies.newQueryPolicy();
        List<DeviceProfile> devices = new ArrayList<>();
        try (RecordSet recordSet = client.query(policy, stmt)) {
            while (recordSet.next()) {
                Record rec = recordSet.getRecord();
                var device = DeviceProfileBins.toEntity(rec);
                devices.add(device);
            }
        }

        logger.debug("Devices by OS {} found | devices={}", osName, devices.size());
        return Collections.unmodifiableList(devices);
    }

    @Override
    public void deleteDeviceProfileById(String deviceId) {
        logger.info("Deleting device by ID from Aerospike | deviceId={}", deviceId);
        Key key = new Key(namespace, setName, deviceId);
        var policy = policies.newWritePolicy();
        boolean isDeleted = client.delete(policy, key);

        if (isDeleted) {
            logger.debug("Device deleted successfully on Aerospike | deviceId={}", deviceId);
        } else {
            logger.warn("Device not found for deletion on Aerospike | deviceId={}", deviceId);
        }
    }

    @Override
    public void persistDeviceProfile(DeviceProfile device) {
        logger.info("Persisting device profile into Aerospike | device={}", device);
        Key key = new Key(namespace, setName, device.deviceId());
        var policy = policies.newWritePolicy();
        client.put(policy, key, DeviceProfileBins.toBins(device));
        logger.debug("Device device profile persisted into Aerospike | device={}", device);
    }

    @Override
    public long incrementHitCount(String deviceId) {
        logger.info("Incrementing device hit count on Aerospike | deviceId={}", deviceId);

        var policy = policies.newWritePolicy();
        Key key = new Key(namespace, setName, deviceId);
        var rec = client.operate(
            policy, key,
            Operation.add(new Bin(HIT_COUNT, 1L)),
            Operation.get(HIT_COUNT)
        );

        var updatedHitCount = rec.getLong(HIT_COUNT);
        logger.debug("Device HitCount updated | deviceId={} | updatedHitCount={}", deviceId, updatedHitCount);
        return updatedHitCount;
    }
}
