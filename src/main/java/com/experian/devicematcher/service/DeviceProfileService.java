package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceIdGenerator;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.exceptions.DeviceProfileException;
import com.experian.devicematcher.exceptions.DeviceProfileMatchException;
import com.experian.devicematcher.exceptions.DeviceProfileNotFoundException;
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
public class DeviceProfileService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileService.class);

    private final DeviceIdGenerator deviceIdGenerator;
    private final UserAgentParser userAgentParser;
    private final DeviceProfileRepository repository;

    @Autowired
    public DeviceProfileService(DeviceIdGenerator deviceIdGenerator, UserAgentParser userAgentParser, DeviceProfileRepository repository) {
        this.deviceIdGenerator = deviceIdGenerator;
        this.userAgentParser = userAgentParser;
        this.repository = repository;
    }

    public Optional<DeviceProfile> getDeviceById(String deviceId) throws DeviceProfileNotFoundException {
        try {
            logger.info("Getting Device By ID | deviceId={}", deviceId);

            requireNonNull(deviceId, "Device ID cannot be null");
            if (deviceId.isBlank()) throw new IllegalArgumentException("Device ID cannot be blank");

            return repository.findDeviceById(deviceId);
        } catch (Exception ex) {
            logger.error("Error getting device by ID: {}", ex.getMessage(), ex);
            throw new DeviceProfileNotFoundException(ex);
        }
    }

    public DeviceProfile matchDevice(String userAgentString) throws DeviceProfileMatchException {
        try {
            logger.info("Matching Device by User-Agent | userAgent={}", userAgentString);

            requireNonNull(userAgentString, "User-Agent cannot be null");
            if (userAgentString.isBlank()) throw new IllegalArgumentException("User-Agent cannot be blank");

            var userAgent = userAgentParser.parse(userAgentString);
            var device = repository.findDevicesByOSName(userAgent.getOsName().toLowerCase()).stream()
                .filter(d -> d.match(userAgent))
                .findFirst().orElseGet(() -> {
                    var d = DeviceProfile.from(deviceIdGenerator::generateId, userAgent);
                    repository.persistDevice(d);
                    return d;
                });

            long hitCount = repository.incrementHitCount(device.getDeviceId());
            return device.withHitCount(hitCount);
        } catch (Exception ex) {
            logger.error("Error matching device by User-Agent: {}", ex.getMessage(), ex);
            throw new DeviceProfileMatchException(ex);
        }
    }

    public List<DeviceProfile> getDevicesByOS(String osName) throws DeviceProfileNotFoundException {
        try {
            logger.info("Getting Device By OS name | osName={}", osName.toLowerCase());
            requireNonNull(osName, "OS Name cannot be null");
            if (osName.isBlank()) throw new IllegalArgumentException("OS Name cannot be blank");

            return repository.findDevicesByOSName(osName.toLowerCase());
        } catch (Exception ex) {
            logger.error("Error getting devices by OS name: {}", ex.getMessage(), ex);
            throw new DeviceProfileNotFoundException(ex);
        }
    }

    public void deleteDeviceById(String deviceId) throws DeviceProfileException {
        try {
            logger.info("Deleting Device By ID | deviceId={}", deviceId);
            requireNonNull(deviceId, "Device ID cannot be null");
            if (deviceId.isBlank()) throw new IllegalArgumentException("Device ID cannot be blank");

            repository.deleteDeviceById(deviceId);
        } catch (Exception ex) {
            logger.error("Error deleting device by ID: {}", ex.getMessage(),ex);
            throw new DeviceProfileException(ex);
        }
    }
}
