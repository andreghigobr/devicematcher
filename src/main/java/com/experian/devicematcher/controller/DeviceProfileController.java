package com.experian.devicematcher.controller;

import com.experian.devicematcher.dto.DeviceProfileDTO;
import com.experian.devicematcher.dto.DeviceProfilesDTO;
import com.experian.devicematcher.service.DeviceProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Device Profile", description = "Device profile management operations")
public class DeviceProfileController {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileController.class);

    private DeviceProfileService service;

    @Autowired
    public DeviceProfileController(DeviceProfileService service) {
        this.service = service;
    }

    @PostMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Match a device based on User-Agent",
            description = "Creates or updates a device profile based on the User-Agent header",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Device matched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid User-Agent header")
            }
    )
    public ResponseEntity<DeviceProfileDTO> matchDeviceProfile(
            @Parameter(description = "User-Agent header containing device information", required = true)
            @RequestHeader(value = "User-Agent", required = true) @NotBlank String userAgent
    ) throws Exception {
        logger.info("Receiving Match Device Request | userAgent={}", userAgent);

        var device = service.matchDevice(userAgent);

        var response = DeviceProfileDTO.from(device);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(
        value = "/{deviceId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get device profile by ID",
            description = "Retrieves a device profile using its unique identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Device found successfully"),
                    @ApiResponse(responseCode = "404", description = "Device not found")
            }
    )
    public ResponseEntity<DeviceProfileDTO> getDeviceProfileById(
            @Parameter(description = "ID of the device to retrieve", required = true)
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
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get devices by operating system",
            description = "Retrieves all device profiles for a specific operating system",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Devices retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "No devices found")
            }
    )
    public ResponseEntity<DeviceProfilesDTO> getDeviceProfiles(
            @Parameter(description = "Operating system name", required = true)
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
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Delete a device profile",
            description = "Deletes a device profile by ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            }
    )
    public ResponseEntity<Object> deleteDeviceProfile(
            @Parameter(description = "ID of the device to delete", required = true)
            @PathVariable(value = "deviceId", required = true) @NotBlank String deviceId
    ) throws Exception {
        logger.info("Receiving Delete Device Profile Request | deviceId={}", deviceId);

        service.deleteDeviceById(deviceId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}