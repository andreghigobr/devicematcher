package com.experian.devicematcher.domain;

/**
 * Interface for generating device IDs.
 * This interface defines a method to generate a unique device ID.
 * Implementations of this interface can provide different strategies for generating device IDs.
 * For example, a UUID generator or a hash-based generator.
 */
public interface DeviceIdGenerator {
    /**
     * Generates a unique device ID.
     * This method should return a string representation of the device ID.
     * The generated ID should be unique and suitable for identifying a device.
     * @return a unique device ID as a string
     */
    public String generateId();
}
