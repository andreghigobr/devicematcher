package com.experian.devicematcher.domain;

import java.util.function.Supplier;

public record DeviceProfile(
    String deviceId,
    Long hitCount,
    String osName,
    String osVersion,
    String browserName,
    String browserVersion
) {
    public static DeviceProfile from(Supplier<String> idSupplier, UserAgent userAgent) {
        return new DeviceProfile(
            idSupplier.get(),
            0L,
            userAgent.getOsName().toLowerCase(),
            userAgent.getOsVersion(),
            userAgent.getBrowserName().toLowerCase(),
            userAgent.getBrowserVersion()
        );
    }

    public DeviceProfile withHitCount(long updatedHitCount) {
        return new DeviceProfile(
            deviceId,
            updatedHitCount,
            osName,
            osVersion,
            browserName,
            browserVersion
        );
    }

    public boolean match(UserAgent userAgent) {
        return this.osName.equalsIgnoreCase(userAgent.getOsName()) &&
            this.osVersion.equalsIgnoreCase(userAgent.getOsVersion()) &&
            this.browserName.equalsIgnoreCase(userAgent.getBrowserName()) &&
            this.browserVersion.equalsIgnoreCase(userAgent.getBrowserVersion());
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
