package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceProfile;
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

    public Optional<DeviceProfile> getDeviceById(String deviceId) {
        throw new IllegalArgumentException("TODO - Implement getDeviceById");
    }

    public DeviceProfile matchDevice(String userAgent) {
        throw new IllegalArgumentException("TODO - Implement matchDevice");
    }

    public List<DeviceProfile> getDevicesByOS(String osName) {
        throw new IllegalArgumentException("TODO - Implement getDeviceByOS");
    }

    public List<DeviceProfile> getDevices(String osName, String osVersion, String browserName, String browserVersion) {
        throw new IllegalArgumentException("TODO - Implement getDevices");
    }

    public void deleteDeviceById(String deviceId) {
        throw new IllegalArgumentException("TODO - Implement deleteDeviceById");
    }
}
