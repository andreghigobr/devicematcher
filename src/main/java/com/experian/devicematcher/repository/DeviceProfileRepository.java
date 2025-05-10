package com.experian.devicematcher.repository;

import com.experian.devicematcher.domain.DeviceProfile;

import java.util.List;
import java.util.Optional;

public interface DeviceProfileRepository {
    Optional<DeviceProfile> findDeviceById(String deviceId);
    List<DeviceProfile> findDevicesByOSName(String osName);
    void deleteDeviceById(String deviceId);
    void persistDevice(DeviceProfile device);
}
