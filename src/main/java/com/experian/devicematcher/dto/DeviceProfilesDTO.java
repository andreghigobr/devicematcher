package com.experian.devicematcher.dto;

import com.experian.devicematcher.domain.DeviceProfile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeviceProfilesDTO {
    @JsonProperty("devices")
    private final List<DeviceProfileDTO> devices;

    public DeviceProfilesDTO(List<DeviceProfileDTO> devices) {
        this.devices = devices;
    }

    public static DeviceProfilesDTO from(List<DeviceProfile> devices) {
        if (devices == null || devices.isEmpty()) {
            return new DeviceProfilesDTO(List.of());
        }

        return new DeviceProfilesDTO(
                devices.stream()
                        .map(DeviceProfileDTO::from)
                        .toList()
        );
    }

    public List<DeviceProfileDTO> getDevices() {
        return devices;
    }
}
