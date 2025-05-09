package com.experian.devicematcher.repository;

import com.experian.devicematcher.domain.DeviceProfile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface DeviceProfileRepository {
    public Optional<DeviceProfile> getDeviceById(String deviceId);
    public List<DeviceProfile> getDevicesByOS(String osName);
    public List<DeviceProfile> getDevices(String osName, String osVersion, String browserName, String browserVersion);
    public void deleteDeviceById(String deviceId);
}
