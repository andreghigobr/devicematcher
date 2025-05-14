package com.experian.devicematcher.domain;

/**
 * Interface for generating device IDs.
 * This interface defines a method to generate a unique device ID.
 * Implementations of this interface can provide different strategies for generating device IDs.
 * For example, a UUID generator or a hash-based generator.
 */
public interface DeviceIdGenerator {
    /**
     * Generates a unique device ID based on the provided user agent.
     * @param userAgent the user agent to base the ID on
     * @return a unique device ID as a string
     */
    String newId(UserAgent userAgent);
}
