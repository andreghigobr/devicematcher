package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceIdGenerator;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.DeviceProfileException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DeviceProfileServiceTest {
    @Mock
    private UserAgentDeviceParser userAgentParser;

    @Mock
    private DeviceProfileRepository repository;

    @Mock
    private DeviceIdGenerator deviceIdGenerator;

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
        verify(repository, times(0)).persistDevice(any());
        verify(deviceIdGenerator, times(0)).generateId();
    }

    @Test
    public void matchDevice_WhenValidUserAgent_NewDevice_ThenReturnDeviceWithCount1() throws Exception {
        // Arrange
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        var osName = "iPhone";
        var osVersion = "16.0.0";
        var browserName = "Safari";
        var browserVersion = "16.0.0";
        var deviceId = UUID.randomUUID().toString();
        var initialHitCount = 0L;

        var userAgent = new UserAgent(osName, osVersion, browserName, browserVersion);
        var deviceProfile = new DeviceProfile(deviceId, initialHitCount, osName, osVersion, browserName, browserVersion);
        when(userAgentParser.parse(ua)).thenReturn(userAgent);
        when(repository.findDevicesByOSName(osName.toLowerCase())).thenReturn(List.of());
        when(repository.incrementHitCount(deviceId)).thenReturn(initialHitCount + 1L
        );
        when(deviceIdGenerator.generateId()).thenReturn(deviceId);

        // Act
        var device = service.matchDevice(ua);

        // Assert
        assertEquals(deviceId, device.getDeviceId(), "Device ID should match");
        assertEquals(initialHitCount + 1, device.getHitCount(), "Device hit count should be 1 for new devices");
        assertEquals(osName.toLowerCase(), device.getOsName(), "OS Name should match");
        assertEquals(osVersion, device.getOsVersion(), "OS Version should match");
        assertEquals(browserName.toLowerCase(), device.getBrowserName(), "Browser Name should match");
        assertEquals(browserVersion, device.getBrowserVersion(), "Browser Version should match");

        verify(userAgentParser, times(1)).parse(ua);
        verify(repository, times(1)).persistDevice(any(DeviceProfile.class));
        verify(repository, times(1)).findDevicesByOSName(osName.toLowerCase());
        verify(repository, times(1)).incrementHitCount(deviceId);
        verify(repository, times(1)).persistDevice(deviceProfile);
        verify(deviceIdGenerator, times(1)).generateId();
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
        verifyNoMoreInteractions(deviceIdGenerator);
    }

    @Test
    public void matchDevice_WhenValidUserAgent_ExistingDevice_ThenReturnDeviceWithCountGreaterThan1() throws Exception {
        // Arrange
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        var osName = "iPhone";
        var osVersion = "16.0.0";
        var browserName = "Safari";
        var browserVersion = "16.0.0";
        var deviceId = UUID.randomUUID().toString();
        var initialHitCount = 1L;

        var userAgent = new UserAgent(osName, osVersion, browserName, browserVersion);
        var deviceProfile = new DeviceProfile(deviceId, initialHitCount, osName, osVersion, browserName, browserVersion);
        when(userAgentParser.parse(ua)).thenReturn(userAgent);
        when(repository.findDevicesByOSName(osName.toLowerCase())).thenReturn(List.of(deviceProfile));
        when(repository.incrementHitCount(deviceId)).thenReturn(initialHitCount + 1L);
        when(deviceIdGenerator.generateId()).thenReturn(deviceId);

        // Act
        var device = service.matchDevice(ua);

        // Assert
        assertEquals(deviceId, device.getDeviceId(), "Device ID should match");
        assertEquals(initialHitCount + 1, device.getHitCount(), "Device hit count should be 1 for new devices");
        assertEquals(osName, device.getOsName(), "OS Name should match");
        assertEquals(osVersion, device.getOsVersion(), "OS Version should match");
        assertEquals(browserName, device.getBrowserName(), "Browser Name should match");
        assertEquals(browserVersion, device.getBrowserVersion(), "Browser Version should match");

        verify(userAgentParser, times(1)).parse(ua);
        verify(repository, times(0)).persistDevice(any(DeviceProfile.class));
        verify(repository, times(1)).findDevicesByOSName(osName.toLowerCase());
        verify(repository, times(1)).incrementHitCount(deviceId);
        verify(repository, times(0)).persistDevice(deviceProfile);
        verify(deviceIdGenerator, times(0)).generateId();
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
        verifyNoMoreInteractions(deviceIdGenerator);
    }

    @Test
    public void getDevicesByOS_WhenNullOSName_ShouldThrowException() throws DeviceProfileNotFoundException {
        String osName = null;
        assertThrows(DeviceProfileNotFoundException.class, () -> { service.getDevicesByOS(osName);});
        verify(repository, times(0)).findDevicesByOSName(osName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void getDevicesByOS_WhenEmptyOSName_ShouldThrowException() {
        String osName = "";
        assertThrows(DeviceProfileNotFoundException.class, () -> { service.getDevicesByOS(osName);});
        verify(repository, times(0)).findDevicesByOSName(osName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void getDevicesByOS_WhenValidOSName_NotExists_ShouldReturnEmptyList() throws DeviceProfileNotFoundException {
        String osName = "alternative OS";
        when(repository.findDevicesByOSName(osName)).thenReturn(List.of());

        var devices = service.getDevicesByOS(osName);

        assertEquals(0, devices.size(), "Device list should be empty");
        verify(repository, times(1)).findDevicesByOSName(osName.toLowerCase());
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void getDevicesByOS_WhenValidOSName_Exists_ShouldReturnList() throws DeviceProfileNotFoundException {
        String osName = "windows";
        var device = new DeviceProfile("deviceId", 0L, osName, "10", "Chrome", "90");
        when(repository.findDevicesByOSName(osName)).thenReturn(List.of(device));

        var devices = service.getDevicesByOS(osName);

        assertEquals(1, devices.size(), "Device list should contain 1 device");
        assertEquals(device.getDeviceId(), devices.getFirst().getDeviceId(), "Device ID should match");
        assertEquals(device.getOsName(), devices.getFirst().getOsName(), "OS Name should match");
        assertEquals(device.getOsVersion(), devices.getFirst().getOsVersion(), "OS Version should match");
        assertEquals(device.getBrowserName(), devices.getFirst().getBrowserName(), "Browser Name should match");
        assertEquals(device.getBrowserVersion(), devices.getFirst().getBrowserVersion(), "Browser Version should match");
        assertEquals(device.getHitCount(), devices.getFirst().getHitCount(), "Hit Count should match");

        verify(repository, times(1)).findDevicesByOSName(osName.toLowerCase());
        verifyNoMoreInteractions(repository);
    }

    // deleteDeviceById
    @Test
    public void deleteDeviceById_WhenNullId_ShouldThrowException() throws DeviceProfileException {
        String deviceId = null;

        assertThrows(DeviceProfileException.class, () -> { service.deleteDeviceById(deviceId); });
        verify(repository, times(0)).deleteDeviceById(deviceId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void deleteDeviceById_WhenEmptyId_ShouldThrowException() {
        String deviceId = "";

        assertThrows(DeviceProfileException.class, () -> { service.deleteDeviceById(deviceId); });
        verify(repository, times(0)).deleteDeviceById(deviceId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void deleteDeviceById_WhenValidId_ThenDelete() throws DeviceProfileException {
        String deviceId = UUID.randomUUID().toString();

        service.deleteDeviceById(deviceId);

        verify(repository, times(1)).deleteDeviceById(deviceId);
        verifyNoMoreInteractions(repository);
    }
}
