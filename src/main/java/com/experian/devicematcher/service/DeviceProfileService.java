package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.exceptions.DeviceProfileException;

import java.util.List;
import java.util.Optional;


public interface DeviceProfileService {
    /**
     * Get a list of all device profiles
     *
     * @param deviceId The ID of the device to be found
     * @return A list of device profiles
     * @throws DeviceProfileException if an error occurs while retrieving the device profiles
     */
    Optional<DeviceProfile> getDeviceById(String deviceId) throws DeviceProfileException;

    /**
     * Match a device profile based on the user agent string
     * Example: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
     *
     * @param userAgentString The user agent string to match
     * @return A device profile that matches the user agent string
     * @throws DeviceProfileException if an error occurs while matching the device profile
     */
    DeviceProfile matchDevice(String userAgentString) throws DeviceProfileException;

    /**
     * Get a list of device profiles that match the given OS name
     *
     * @param osName The OS name to match
     *               Example: "Windows 10" "Linux"
     * @return A list of device profiles that match the OS name
     * @throws DeviceProfileException if an error occurs while retrieving the device profiles by OS name
     */
    List<DeviceProfile> getDevicesByOS(String osName) throws DeviceProfileException;

    /**
     * Delete a device profile by ID
     *
     * @param deviceId The ID of the device to be deleted
     * @throws DeviceProfileException if an error occurs while deleting the device profile
     */
    void deleteDeviceById(String deviceId) throws DeviceProfileException;
}
