package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.AerospikeClient;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.repository.DeviceProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DeviceProfileAerospikeRepository implements DeviceProfileRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileAerospikeRepository.class);

    private final AerospikeClient client;

    @Autowired
    public DeviceProfileAerospikeRepository(AerospikeClient client) {
        this.client = client;
    }

    @Override
    public Optional<DeviceProfile> getDeviceById(String deviceId) {
        logger.info("Retrieving device by ID from Aerospike | deviceId={}", deviceId);
        return Optional.empty();
    }

    @Override
    public List<DeviceProfile> getDevicesByOS(String osName) {
        logger.info("Retrieving devices by OS from Aerospike | osName={}", osName);
        return List.of();
    }

    @Override
    public List<DeviceProfile> getDevices(String osName, String osVersion, String browserName, String browserVersion) {
        logger.info("Retrieving devices from Aerospike | osName={} osVersion={} browserName={} browserVersion={}", osName, osVersion, browserName, browserVersion);
        return List.of();
    }

    @Override
    public void deleteDeviceById(String deviceId) {
        logger.info("Deleting device by ID from Aerospike | deviceId={}", deviceId);
    }
}
