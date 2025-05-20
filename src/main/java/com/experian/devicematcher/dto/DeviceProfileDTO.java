package com.experian.devicematcher.dto;

import com.experian.devicematcher.domain.DeviceProfile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceProfileDTO(
    @JsonProperty("deviceId")
    String deviceId,

    @JsonProperty("hitCount")
    Long hitCount,

    @JsonProperty("osName")
    String osName,

    @JsonProperty("osVersion")
    String osVersion,

    @JsonProperty("browserName")
    String browserName,

    @JsonProperty("browserVersion")
    String browserVersion
) {
    public static DeviceProfileDTO from(DeviceProfile deviceProfile) {
        return new DeviceProfileDTO(
            deviceProfile.deviceId(),
            deviceProfile.hitCount(),
            deviceProfile.userAgent().osName(),
            deviceProfile.userAgent().osVersion().toString(),
            deviceProfile.userAgent().browserName(),
            deviceProfile.userAgent().browserVersion().toString()
        );
    }
}
