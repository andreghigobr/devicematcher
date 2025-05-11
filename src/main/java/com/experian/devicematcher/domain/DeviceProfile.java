package com.experian.devicematcher.domain;

import java.util.function.Supplier;

public final class DeviceProfile {
    private final String deviceId;
    private final Long hitCount;
    private final String osName;
    private final String osVersion;
    private final String browserName;
    private final String browserVersion;

    // --------------------------------------------
    // Constructors
    // --------------------------------------------
    public DeviceProfile(String deviceId, Long hitCount, String osName, String osVersion, String browserName, String browserVersion) {
        this.deviceId = deviceId;
        this.hitCount = hitCount;
        this.osName = osName;
        this.osVersion = osVersion;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
    }

    // --------------------------------------------
    // Factory Method
    // --------------------------------------------
    public static DeviceProfile from(Supplier<String> idSupplier, UserAgent userAgent) {
        return new DeviceProfile(
                idSupplier.get(),
                0L,
                userAgent.getOsName().toLowerCase(),
                userAgent.getOsVersion().toLowerCase(),
                userAgent.getBrowserName().toLowerCase(),
                userAgent.getBrowserVersion().toLowerCase()
        );
    }

    // --------------------------------------------
    // Update Methods
    // --------------------------------------------
    public DeviceProfile withHitCount(long hitCount) {
        return new DeviceProfile(
                deviceId,
                hitCount,
                osName,
                osVersion,
                browserName,
                browserVersion
        );
    }

    // --------------------------------------------
    // Getters
    // --------------------------------------------
    public String getDeviceId() {
        return deviceId;
    }

    public Long getHitCount() {
        return hitCount;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getBrowserName() {
        return browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    // --------------------------------------------
    // Interfaces
    // --------------------------------------------
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

    @Override
    public String toString() {
        return "DeviceProfile{" +
                "deviceId='" + deviceId + '\'' +
                ", hitCount=" + hitCount +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", browserName='" + browserName + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                '}';
    }
}
