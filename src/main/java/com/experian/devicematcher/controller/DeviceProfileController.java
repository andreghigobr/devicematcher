package com.experian.devicematcher.controller;

import com.experian.devicematcher.dto.DeviceProfileDTO;
import com.experian.devicematcher.dto.DeviceProfilesDTO;
import com.experian.devicematcher.service.DeviceProfileService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/devices")
public class DeviceProfileController {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileController.class);

    private DeviceProfileService service;

    @Autowired
    public DeviceProfileController(DeviceProfileService service) {
        this.service = service;
    }

    @PostMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceProfileDTO> matchDeviceProfile(
            @RequestHeader(value = "User-Agent", required = true) @NotBlank String userAgent
    ) throws Exception {
        logger.info("Receiving Match Device Request | userAgent={}", userAgent);

        var device = service.matchDevice(userAgent);

        var response = DeviceProfileDTO.from(device);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(
        value = "/{deviceId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceProfileDTO> getDeviceProfileById(
            @PathVariable(value = "deviceId", required = true) @NotBlank String deviceId
    ) throws Exception {
        logger.info("Receiving Get Device Profile By Id Request | deviceId={}", deviceId);

        var device = service.getDeviceById(deviceId);

        if (device.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var response = DeviceProfileDTO.from(device.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DeviceProfilesDTO> getDeviceProfiles(
            @RequestHeader(value = "os-name", required = true) @NotBlank String osName
    ) throws Exception {
        logger.info("Receiving Get Device Profiles Request | osName={}", osName);

        var devices = service.getDevicesByOS(osName);

        if (devices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var response = DeviceProfilesDTO.from(devices);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(
        value = "/{deviceId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> deleteDeviceProfile(
            @PathVariable(value = "deviceId", required = true) @NotBlank String deviceId
    ) throws Exception {
        logger.info("Receiving Delete Device Profile Request | deviceId={}", deviceId);

        service.deleteDeviceById(deviceId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}