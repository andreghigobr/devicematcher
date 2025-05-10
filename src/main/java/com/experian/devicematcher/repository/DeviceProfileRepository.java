package com.experian.devicematcher.repository;

import com.experian.devicematcher.domain.DeviceProfile;

import java.util.List;
import java.util.Optional;

public interface DeviceProfileRepository {
    public Optional<DeviceProfile> findDeviceById(String deviceId);
    public List<DeviceProfile> findDevicesByOSName(String osName);
    public void deleteDeviceById(String deviceId);
    public void persistDevice(DeviceProfile device);
}
