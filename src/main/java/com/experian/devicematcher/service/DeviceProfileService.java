package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceIdGenerator;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.exceptions.DeviceProfileException;
import com.experian.devicematcher.exceptions.DeviceProfileMatchException;
import com.experian.devicematcher.exceptions.DeviceProfileNotFoundException;
import com.experian.devicematcher.parser.UserAgentDeviceParser;
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

    private DeviceIdGenerator deviceIdGenerator;
    private UserAgentDeviceParser userAgentParser;
    private DeviceProfileRepository repository;

    @Autowired
    public DeviceProfileService(DeviceIdGenerator deviceIdGenerator, UserAgentDeviceParser userAgentParser, DeviceProfileRepository repository) {
        this.deviceIdGenerator = deviceIdGenerator;
        this.userAgentParser = userAgentParser;
        this.repository = repository;
    }

    public Optional<DeviceProfile> getDeviceById(String deviceId) throws DeviceProfileNotFoundException {
        try {
            requireNonNull(deviceId, "Device ID cannot be null");
            if (deviceId.isBlank()) throw new IllegalArgumentException("Device ID cannot be blank");

            logger.info("Getting Device By ID | deviceId={}", deviceId);
            return repository.findDeviceById(deviceId);
        } catch (Exception ex) {
            throw new DeviceProfileNotFoundException(ex);
        }
    }

    public DeviceProfile matchDevice(String userAgentString) throws DeviceProfileMatchException {
        try {
            requireNonNull(userAgentString, "User-Agent cannot be null");
            if (userAgentString.isBlank()) throw new IllegalArgumentException("User-Agent cannot be blank");

            logger.info("Matching Device by User-Agent | userAgent={}", userAgentString);

            var userAgent = userAgentParser.parse(userAgentString);

            var device = repository.findDevicesByOSName(userAgent.getOsName().toLowerCase()).stream()
                .filter(d -> d.match(userAgent))
                .findFirst().orElseGet(() -> {
                    var d = DeviceProfile.from(() -> deviceIdGenerator.generateId(), userAgent);
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
            requireNonNull(osName, "OS Name cannot be null");
            if (osName.isBlank()) throw new IllegalArgumentException("OS Name cannot be blank");

            logger.info("Getting Device By OS name | osName={}", osName.toLowerCase());
            return repository.findDevicesByOSName(osName.toLowerCase());
        } catch (Exception ex) {
            throw new DeviceProfileNotFoundException(ex);
        }
    }

    public void deleteDeviceById(String deviceId) throws DeviceProfileException {
        try {
            requireNonNull(deviceId, "Device ID cannot be null");
            if (deviceId.isBlank()) throw new IllegalArgumentException("Device ID cannot be blank");

            logger.info("Deleting Device By ID | deviceId={}", deviceId);
            repository.deleteDeviceById(deviceId);
        } catch (Exception ex) {
            throw new DeviceProfileException(ex);
        }
    }
}
