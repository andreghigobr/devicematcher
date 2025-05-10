package com.experian.devicematcher.service;

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

@Service
public class DeviceProfileService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileService.class);

    private UserAgentDeviceParser userAgentParser;
    private DeviceProfileRepository repository;

    // constructor
    @Autowired
    public DeviceProfileService(UserAgentDeviceParser userAgentParser, DeviceProfileRepository repository) {
        this.userAgentParser = userAgentParser;
        this.repository = repository;
    }

    public Optional<DeviceProfile> getDeviceById(String deviceId) throws DeviceProfileNotFoundException {
        try {
            logger.info("Getting Device By ID | deviceId={}", deviceId);
            return repository.findDeviceById(deviceId);
        } catch (Exception ex) {
            throw new DeviceProfileNotFoundException(ex);
        }
    }

    public DeviceProfile matchDevice(String userAgent) throws DeviceProfileMatchException {
        try {
            logger.info("Matching Device | userAgent={}", userAgent);

            var ua = userAgentParser.parse(userAgent);

            var device = repository.findDevicesByOSName(ua.getOsName()).stream()
                    .filter(d -> d.getOsVersion().equals(ua.getOsVersion()))
                    .filter(d -> d.getBrowserName().equals(ua.getBrowserName()))
                    .filter(d -> d.getBrowserVersion().equals(ua.getBrowserVersion()))
                    .findFirst()
                    .orElse(DeviceProfile.from(ua));

            device.incrementHitCount();
            repository.persistDevice(device);

            return device;
        } catch (Exception ex) {
            throw new DeviceProfileMatchException(userAgent, ex);
        }
    }

    public List<DeviceProfile> getDevicesByOS(String osName) throws DeviceProfileNotFoundException {
        try {
            logger.info("Getting Device By OS | osName={}", osName);
            return repository.findDevicesByOSName(osName.toLowerCase());
        } catch (Exception ex) {
            throw new DeviceProfileNotFoundException(ex);
        }
    }

    public void deleteDeviceById(String deviceId) throws DeviceProfileException {
        try {
            logger.info("Getting Device | deviceId={}", deviceId);
            repository.deleteDeviceById(deviceId);
        } catch (Exception ex) {
            throw new DeviceProfileException(deviceId, ex);
        }
    }
}
