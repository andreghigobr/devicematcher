package com.experian.devicematcher.repository;

import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.domain.UserAgent;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing device profiles.
 * This interface provides methods to find, persist, and delete device profiles.
 * It also provides a method to increment the hit count for a device.
 */
public interface DeviceProfileRepository {
    /**
     * Finds a device profile by its ID.
     * This method returns an Optional containing the device profile if found, or an empty Optional if not found.
     *
     * @param deviceId The ID of the device to be found.
     * @return An Optional containing the device profile if found, or an empty Optional if not found.
     */
    Optional<DeviceProfile> findDeviceProfileById(String deviceId);

    /**
     * Finds all device profiles that match the given user agent.
     * This method returns a list of device profiles that match the user agent.
     *
     * @param userAgent The user agent to match.
     * @return A list of device profiles that match the user agent.
     */
    List<DeviceProfile> findDeviceProfiles(UserAgent userAgent);

    /**
     * Finds all device profiles that match the given OS name.
     * This method returns a list of device profiles that match the OS name.
     *
     * @param osName The OS name to match.
     * @return A list of device profiles that match the OS name.
     */
    List<DeviceProfile> findDeviceProfilesByOSName(String osName);

    /**
     * Finds a device profile by its ID.
     *
     * @param deviceId The ID of the device to be found.
     */
    void deleteDeviceProfileById(String deviceId);

    /**
     * Persists a device profile.
     * This method is used to save a new device profile or update an existing one.
     *
     * @param device The device profile to be persisted.
     */
    void persistDeviceProfile(DeviceProfile device);

    /**
     * Increments the hit count for a device profile.
     *
     * @param deviceId The ID of the device to increment the hit count for.
     * @return The new hit count for the device.
     */
    long incrementHitCount(String deviceId);
}
