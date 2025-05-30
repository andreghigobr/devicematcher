package com.experian.devicematcher.domain;

import java.util.function.Supplier;

public record DeviceProfile(
    String deviceId,
    Long hitCount,
    UserAgent userAgent
) {
    public DeviceProfile {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("Device ID cannot be null or blank");
        }
        if (hitCount == null) {
            throw new IllegalArgumentException("Hit count cannot be null");
        }
        if (hitCount < 0) {
            throw new IllegalArgumentException("Hit count cannot be negative");
        }
        if (userAgent == null) {
            throw new IllegalArgumentException("User agent cannot be null");
        }
    }


    public static DeviceProfile from(Supplier<String> idSupplier, UserAgent userAgent) {
        return new DeviceProfile(
            idSupplier.get(),
            0L,
            userAgent
        );
    }

    public DeviceProfile withHitCount(long updatedHitCount) {
        return new DeviceProfile(
            deviceId,
            updatedHitCount,
            userAgent
        );
    }

    public boolean match(UserAgent userAgent) {
        return this.userAgent.osName().equalsIgnoreCase(userAgent.osName()) &&
            this.userAgent.osVersion().equals(userAgent.osVersion()) &&
            this.userAgent.browserName().equalsIgnoreCase(userAgent.browserName()) &&
            this.userAgent.browserVersion().equals(userAgent.browserVersion());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DeviceProfile that = (DeviceProfile) obj;
        return deviceId.equals(that.deviceId);
    }

    @Override
    public int hashCode() {
        return deviceId.hashCode();
    }
}
