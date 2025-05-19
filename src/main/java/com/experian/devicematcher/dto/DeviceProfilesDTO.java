package com.experian.devicematcher.dto;

import com.experian.devicematcher.domain.DeviceProfile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceProfilesDTO(
    @JsonProperty("devices")
    List<DeviceProfileDTO> devices
) {
    // Compact constructor to ensure devices is never null
    public DeviceProfilesDTO {
        devices = devices == null ? List.of() : devices;
    }

    public static DeviceProfilesDTO from(List<DeviceProfile> devices) {
        return new DeviceProfilesDTO(devices.stream().map(DeviceProfileDTO::from).toList());
    }
}
