package com.experian.devicematcher.domain;

import java.time.LocalDate;
import java.util.UUID;

public class DeviceProfile {
    private final UUID deviceId;
    private Long hitCount;
    private final String osName;
    private final String osVersion;
    private final String browserName;
    private final String browserVersion;
    private final String userAgent;
    private final LocalDate createdAt;
    private LocalDate lastUpdatedAt;

    // --------------------------------------------
    // Constructors
    // --------------------------------------------
    public DeviceProfile(UUID deviceId, Long hitCount, String osName, String osVersion, String browserName, String browserVersion, String userAgent) {
        this.deviceId = deviceId;
        this.hitCount = hitCount;
        this.osName = osName;
        this.osVersion = osVersion;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.userAgent = userAgent;
        this.createdAt = LocalDate.now();
        this.lastUpdatedAt = null;
    }

    public DeviceProfile(String osName, String osVersion, String browserName, String browserVersion, String userAgent) {
        this.deviceId = UUID.randomUUID();
        this.hitCount = 0L;
        this.osName = osName;
        this.osVersion = osVersion;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.userAgent = userAgent;
        this.createdAt = LocalDate.now();
        this.lastUpdatedAt = null;
    }

    // --------------------------------------------
    // Factory Method
    // --------------------------------------------
    public static DeviceProfile create(String osName, String osVersion, String browserName, String browserVersion, String userAgent) {
        return new DeviceProfile(osName, osVersion, browserName, browserVersion, userAgent);
    }

    // --------------------------------------------
    // Domain Logic
    // --------------------------------------------
    public void incrementHitCount() {
        this.hitCount++;
        this.lastUpdatedAt = LocalDate.now();
    }

    // --------------------------------------------
    // Getters
    // --------------------------------------------
    public UUID getDeviceId() {
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

    public String getUserAgent() {
        return userAgent;
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

    // toString
    @Override
    public String toString() {
        return "DeviceProfile{" +
                "deviceId='" + deviceId + '\'' +
                ", hitCount=" + hitCount +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", browserName='" + browserName + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}
