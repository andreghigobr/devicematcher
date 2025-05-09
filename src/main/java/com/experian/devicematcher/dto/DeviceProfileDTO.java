package com.experian.devicematcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeviceProfileDTO {
    @JsonProperty("deviceId")
    private final UUID deviceId;

    @JsonProperty("hitCount")
    private final Long hitCount;

    @JsonProperty("osName")
    private final String osName;

    @JsonProperty("osVersion")
    private final String osVersion;

    @JsonProperty("browserName")
    private final String browserName;

    @JsonProperty("browserVersion")
    private final String browserVersion;

    public DeviceProfileDTO(UUID deviceId, Long hitCount, String osName, String osVersion, String browserName, String browserVersion) {
        this.deviceId = deviceId;
        this.hitCount = hitCount;
        this.osName = osName;
        this.osVersion = osVersion;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
    }

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

    @Override
    public String toString() {
        return "DeviceProfileDTO{" +
                "deviceId=" + deviceId +
                ", hitCount=" + hitCount +
                ", osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", browserName='" + browserName + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                '}';
    }
}
