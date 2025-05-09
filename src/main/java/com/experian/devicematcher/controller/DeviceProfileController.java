package com.experian.devicematcher.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/devices")
public class DeviceProfileController {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileController.class);

    @PostMapping("/")
    public String matchDeviceProfile(
            @RequestHeader(value = "User-Agent", required = true) String userAgent
    ) {
        logger.info("Receiving Match Device Request | userAgent={}", userAgent);

        // Logic to create a device profile
        return "Device profile created";
    }

    @GetMapping("/{deviceId}")
    public String getDeviceProfileById(
            @PathVariable(value = "deviceId", required = true) String deviceId
    ) {
        logger.info("Receiving Get Device Profile By Id Request | deviceId={}", deviceId);

        // Logic to get a device profile by ID
        return "Device profile retrieved";
    }

    @GetMapping("/")
    public String getDeviceProfiles(
            @RequestHeader(value = "os-name", required = true) String osName
    ) {
        logger.info("Receiving Get Device Profiles Request | osName={}", osName);

        // Logic to get all device profiles
        return "All device profiles retrieved";
    }

    @DeleteMapping("/{deviceId}")
    public String deleteDeviceProfile(
            @PathVariable(value = "deviceId", required = true) String deviceId
    ) {
        logger.info("Receiving Delete Device Profile Request | deviceId={}", deviceId);

        // Logic to delete a device profile

        return "Device profile deleted";
    }
}