package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceProfileIdGenerator;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.exceptions.DeviceProfileDeleteException;
import com.experian.devicematcher.exceptions.DeviceProfileException;
import com.experian.devicematcher.exceptions.DeviceProfileMatchException;
import com.experian.devicematcher.parser.UserAgentParser;
import com.experian.devicematcher.repository.DeviceProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
public class DeviceProfileServiceImpl implements DeviceProfileService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileServiceImpl.class);

    private final DeviceProfileIdGenerator deviceProfileIdGenerator;
    private final UserAgentParser userAgentParser;
    private final DeviceProfileRepository repository;

    @Autowired
    public DeviceProfileServiceImpl(DeviceProfileIdGenerator deviceProfileIdGenerator, UserAgentParser userAgentParser, DeviceProfileRepository repository) {
        this.deviceProfileIdGenerator = deviceProfileIdGenerator;
        this.userAgentParser = userAgentParser;
        this.repository = repository;
    }

    @Override
    public Optional<DeviceProfile> getDeviceById(String deviceId) throws DeviceProfileException {
        try {
            logger.info("Getting Device By ID | deviceId={}", deviceId);

            requireNonNull(deviceId, "Device ID cannot be null");
            if (deviceId.isBlank()) throw new IllegalArgumentException("Device ID cannot be blank");

            return repository.findDeviceProfileById(deviceId);
        } catch (Exception ex) {
            logger.error("Error getting device by ID: {}", ex.getMessage(), ex);
            throw new DeviceProfileException(ex);
        }
    }

    @Override
    public DeviceProfile matchDevice(String userAgentString) throws DeviceProfileException {
        try {
            logger.info("Matching Device by User-Agent | userAgent={}", userAgentString);

            requireNonNull(userAgentString, "User-Agent cannot be null");
            if (userAgentString.isBlank()) throw new IllegalArgumentException("User-Agent cannot be blank");

            var userAgent = userAgentParser.parse(userAgentString);

            var device = repository.findDeviceProfiles(userAgent).stream()
                .findFirst().orElseGet(() -> {
                    var d = DeviceProfile.from(() -> deviceProfileIdGenerator.newId(userAgent), userAgent);
                    repository.persistDeviceProfile(d);
                    return d;
                });

            long hitCount = repository.incrementHitCount(device.deviceId());
            return device.withHitCount(hitCount);
        } catch (Exception ex) {
            logger.error("Error matching device by User-Agent: {}", ex.getMessage(), ex);
            throw new DeviceProfileMatchException(ex);
        }
    }

    @Override
    public List<DeviceProfile> getDevicesByOS(String osName) throws DeviceProfileException {
        try {
            logger.info("Getting Device By OS name | osName={}", osName.toLowerCase());
            requireNonNull(osName, "OS Name cannot be null");
            if (osName.isBlank()) throw new IllegalArgumentException("OS Name cannot be blank");

            return repository.findDeviceProfilesByOSName(osName.toLowerCase());
        } catch (Exception ex) {
            logger.error("Error getting devices by OS name: {}", ex.getMessage(), ex);
            throw new DeviceProfileException(ex);
        }
    }

    @Override
    public void deleteDeviceById(String deviceId) throws DeviceProfileException {
        try {
            logger.info("Deleting Device By ID | deviceId={}", deviceId);
            requireNonNull(deviceId, "Device ID cannot be null");
            if (deviceId.isBlank()) throw new IllegalArgumentException("Device ID cannot be blank");

            repository.deleteDeviceProfileById(deviceId);
        } catch (Exception ex) {
            logger.error("Error deleting device by ID: {}", ex.getMessage(),ex);
            throw new DeviceProfileDeleteException(ex);
        }
    }
}
