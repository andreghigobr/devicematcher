package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.DeviceProfileMatchException;
import com.experian.devicematcher.exceptions.DeviceProfileNotFoundException;
import com.experian.devicematcher.parser.UserAgentDeviceParser;
import com.experian.devicematcher.repository.DeviceProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DeviceProfileServiceTest {
    @Mock
    private UserAgentDeviceParser userAgentParser;

    @Mock
    private DeviceProfileRepository repository;

    @InjectMocks
    private DeviceProfileService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getDeviceById_WhenIdIsNull() throws DeviceProfileNotFoundException {
        // Arrange
        String deviceId = null;
        when(repository.findDeviceById(deviceId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(DeviceProfileNotFoundException.class, () -> { service.getDeviceById(deviceId); });
        verify(repository, times(0)).findDeviceById(deviceId);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }

    @Test
    public void getDeviceById_Id_Invalid() throws Exception {
        // Arrange
        String deviceId = UUID.randomUUID().toString();
        when(repository.findDeviceById(deviceId)).thenReturn(Optional.empty());

        // Act
        var device = service.getDeviceById(deviceId);

        // Assert
        Assert.isTrue(device.isEmpty(), "Device should be empty for invalid ID");
        verify(repository, times(1)).findDeviceById(deviceId);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }

    @Test
    public void getDeviceById_WhenExistingId_ThenReturnDevice() throws DeviceProfileNotFoundException {
        // Arrange
        String deviceId = UUID.randomUUID().toString();
        DeviceProfile device = new DeviceProfile(deviceId, 0L, "Windows", "10", "Chrome", "90");
        when(repository.findDeviceById(deviceId)).thenReturn(Optional.of(device));

        // Act
        var result = service.getDeviceById(deviceId);

        // Assert
        Assert.isTrue(result.isPresent(), "Device should be present for valid ID");
        Assert.isTrue(result.get().getDeviceId().equals(deviceId), "Device ID should match");
        Assert.isTrue(result.get().getOsName().equals("Windows"), "OS Name should match");
        Assert.isTrue(result.get().getOsVersion().equals("10"), "OS Version should match");
        Assert.isTrue(result.get().getBrowserName().equals("Chrome"), "Browser Name should match");
        Assert.isTrue(result.get().getBrowserVersion().equals("90"), "Browser Version should match");
        verify(repository, times(1)).findDeviceById(deviceId);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }

    @Test
    public void matchDevice_WhenInvalidUserAgent_ThenThrowsException() throws Exception {
        // Arrange
        String userAgent = "MozilxxxxxxxxxINVALIDxxxxxxxxxx5.110 Mobilexxxxxari/537.36";
        when(userAgentParser.parse(userAgent)).thenThrow(new IllegalArgumentException("Invalid User Agent"));

        // Act & Assert
        assertThrows(DeviceProfileMatchException.class, () -> { service.matchDevice(userAgent); });
        verify(userAgentParser, times(1)).parse(userAgent);
        verify(repository, times(0)).persistDevice(any());
    }

    // SCENARIO: MATCH DEVICE BY USER AGENT: WHEN VALID USER AGENT AND NEW DEVICE, THEN RETURNS DEVICE WITH HIT COUNT 1
    @Test
    public void matchDevice_WhenValidUserAgent_NewDevice_ThenReturnDeviceWithCount1() throws Exception {
        // Arrange
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        var userAgent = new UserAgent("iPhone", "16_0", "Safari", "16.0");
        var deviceProfile = new DeviceProfile("1", 0L, "iPhone", "16_0", "Safari", "16.0");
        when(userAgentParser.parse(ua)).thenReturn(userAgent);
        when(repository.findDevicesByOSName("iPhone")).thenReturn(List.of(deviceProfile));

        // Act
        var device = service.matchDevice(ua);

        // Assert
        Assert.isTrue(device.getHitCount() == 1, "Device hit count should be 1 for new device");
        Assert.isTrue(device.getOsName().equals("iPhone"), "OS Name should match");
        Assert.isTrue(device.getOsVersion().equals("16_0"), "OS Version should match");
        Assert.isTrue(device.getBrowserName().equals("Safari"), "Browser Name should match");
        Assert.isTrue(device.getBrowserVersion().equals("16.0"), "Browser Version should match");
        verify(userAgentParser, times(1)).parse(ua);
        verify(repository, times(1)).persistDevice(any(DeviceProfile.class));
        verify(repository, times(1)).findDevicesByOSName("iPhone");
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }

    @Test
    public void matchDevice_WhenValidUserAgent_NewDevice_ThenReturnDeviceWithCountGreaterThan1() throws Exception {
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        var userAgent = new UserAgent("iPhone", "16_0", "Safari", "16.0");
        var deviceProfile = new DeviceProfile("1", 1L, "iPhone", "16_0", "Safari", "16.0");
        when(userAgentParser.parse(ua)).thenReturn(userAgent);
        when(repository.findDevicesByOSName("iPhone")).thenReturn(List.of(deviceProfile));

        // Act
        var device = service.matchDevice(ua);

        // Assert
        Assert.isTrue(device.getHitCount() == 2, "Device hit count should be greater than 1 for existing device");
        Assert.isTrue(device.getOsName().equals("iPhone"), "OS Name should match");
        Assert.isTrue(device.getOsVersion().equals("16_0"), "OS Version should match");
        Assert.isTrue(device.getBrowserName().equals("Safari"), "Browser Name should match");
        Assert.isTrue(device.getBrowserVersion().equals("16.0"), "Browser Version should match");
        verify(userAgentParser, times(1)).parse(ua);
        verify(repository, times(1)).persistDevice(any(DeviceProfile.class));
        verify(repository, times(1)).findDevicesByOSName("iPhone");
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }
}
