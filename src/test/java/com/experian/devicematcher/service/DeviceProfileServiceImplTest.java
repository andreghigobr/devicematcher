package com.experian.devicematcher.service;

import com.experian.devicematcher.domain.DeviceProfileIdGenerator;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.DeviceProfileException;
import com.experian.devicematcher.exceptions.DeviceProfileMatchException;
import com.experian.devicematcher.parser.UserAgentParser;
import com.experian.devicematcher.repository.DeviceProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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

@Tag("unit")
class DeviceProfileServiceImplTest {
    @Mock
    private UserAgentParser userAgentParser;

    @Mock
    private DeviceProfileRepository repository;

    @Mock
    private DeviceProfileIdGenerator deviceProfileIdGenerator;

    @InjectMocks
    private DeviceProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDeviceById_WhenIdIsNull() {
        // Arrange
        String deviceId = null;
        when(repository.findDeviceProfileById(deviceId)).thenReturn(Optional.empty());

        // Assert
        assertThrows(DeviceProfileException.class, () -> { service.getDeviceById(deviceId); });
        verify(repository, times(0)).findDeviceProfileById(deviceId);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }

    @Test
    void getDeviceById_Id_Invalid() throws Exception {
        // Arrange
        String deviceId = UUID.randomUUID().toString();
        when(repository.findDeviceProfileById(deviceId)).thenReturn(Optional.empty());

        // Act
        var device = service.getDeviceById(deviceId);

        // Assert
        Assert.isTrue(device.isEmpty(), "Device should be empty for invalid ID");
        verify(repository, times(1)).findDeviceProfileById(deviceId);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }

    @Test
    void getDeviceById_WhenExistingId_ThenReturnDevice() throws Exception {
        // Arrange
        String deviceId = UUID.randomUUID().toString();
        UserAgent userAgent = new UserAgent("Windows", "10", "Chrome", "90");
        DeviceProfile device = new DeviceProfile(deviceId, 0L, userAgent);
        when(repository.findDeviceProfileById(deviceId)).thenReturn(Optional.of(device));

        // Act
        var result = service.getDeviceById(deviceId);

        // Assert
        Assert.isTrue(result.isPresent(), "Device should be present for valid ID");
        Assert.isTrue(result.get().deviceId().equals(deviceId), "Device ID should match");
        Assert.isTrue(result.get().userAgent().osName().equals("Windows"), "OS Name should match");
        Assert.isTrue(result.get().userAgent().osVersion().toString().equals("10.0.0"), "OS Version should match");
        Assert.isTrue(result.get().userAgent().browserName().equals("Chrome"), "Browser Name should match");
        Assert.isTrue(result.get().userAgent().browserVersion().toString().equals("90.0.0"), "Browser Version should match");
        verify(repository, times(1)).findDeviceProfileById(deviceId);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
    }

    @Test
    void matchDevice_WhenInvalidUserAgent_ThenThrowsException() throws Exception {
        // Arrange
        String userAgentString = "MozilxxxxxxxxxINVALIDxxxxxxxxxx5.110 Mobilexxxxxari/537.36";
        when(userAgentParser.parse(userAgentString)).thenThrow(new IllegalArgumentException("Invalid User Agent"));

        // Act & Assert
        assertThrows(DeviceProfileMatchException.class, () -> { service.matchDevice(userAgentString); });
        verify(userAgentParser, times(1)).parse(userAgentString);
        verify(repository, times(0)).persistDeviceProfile(any());
        verify(repository, times(0)).persistDeviceProfile(any());
        verify(deviceProfileIdGenerator, times(0)).newId(any(UserAgent.class));
    }

    @Test
    void matchDevice_WhenValidUserAgent_NewDevice_ThenReturnDeviceWithCount1() throws Exception {
        // Arrange
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        var osName = "iPhone";
        var osVersion = "16.0.0";
        var browserName = "Safari";
        var browserVersion = "16.0.0";
        var initialHitCount = 0L;

        var deviceId = osName + "-" + UUID.randomUUID();
        var userAgent = new UserAgent(osName, osVersion, browserName, browserVersion);
        when(userAgentParser.parse(ua)).thenReturn(userAgent);
        when(repository.findDeviceProfiles(userAgent)).thenReturn(List.of());
        when(repository.incrementHitCount(deviceId)).thenReturn(initialHitCount + 1L);
        when(deviceProfileIdGenerator.newId(userAgent)).thenReturn(deviceId);

        // Act
        var device = service.matchDevice(ua);

        // Assert
        assertEquals(deviceId, device.deviceId(), "Device ID should match");
        assertEquals(initialHitCount + 1, device.hitCount(), "Device hit count should be 1 for new devices");
        assertEquals(osName.toLowerCase(), device.userAgent().osName().toLowerCase(), "OS Name should match");
        assertEquals(osVersion, device.userAgent().osVersion().toString(), "OS Version should match");
        assertEquals(browserName.toLowerCase(), device.userAgent().browserName().toLowerCase(), "Browser Name should match");
        assertEquals(browserVersion, device.userAgent().browserVersion().toString(), "Browser Version should match");

        verify(userAgentParser, times(1)).parse(ua);
        verify(repository, times(1)).findDeviceProfiles(userAgent);
        verify(repository, times(1)).persistDeviceProfile(any(DeviceProfile.class));
        verify(repository, times(1)).incrementHitCount(deviceId);
        verify(deviceProfileIdGenerator, times(1)).newId(userAgent);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
        verifyNoMoreInteractions(deviceProfileIdGenerator);
    }

    @Test
    void matchDevice_WhenValidUserAgent_ExistingDevice_ThenReturnDeviceWithCountGreaterThan1() throws Exception {
        // Arrange
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        var osName = "iPhone";
        var osVersion = "16.0.0";
        var browserName = "Safari";
        var browserVersion = "16.0.0";
        var deviceId = UUID.randomUUID().toString();
        var initialHitCount = 1L;

        var userAgent = new UserAgent(osName.toLowerCase(), osVersion, browserName.toLowerCase(), browserVersion);
        var deviceProfile = new DeviceProfile(deviceId, initialHitCount, userAgent);
        when(userAgentParser.parse(ua)).thenReturn(userAgent);
        when(repository.findDeviceProfiles(userAgent)).thenReturn(List.of(deviceProfile));
        when(repository.incrementHitCount(deviceId)).thenReturn(initialHitCount + 1L);
        when(deviceProfileIdGenerator.newId(userAgent)).thenReturn(deviceId);

        // Act
        var device = service.matchDevice(ua);

        // Assert
        assertEquals(deviceId, device.deviceId(), "Device ID should match");
        assertEquals(initialHitCount + 1, device.hitCount(), "Device hit count should be 1 for new devices");
        assertEquals(osName.toLowerCase(), device.userAgent().osName().toLowerCase(), "OS Name should match");
        assertEquals(osVersion, device.userAgent().osVersion().toString(), "OS Version should match");
        assertEquals(browserName.toLowerCase(), device.userAgent().browserName().toLowerCase(), "Browser Name should match");
        assertEquals(browserVersion, device.userAgent().browserVersion().toString(), "Browser Version should match");

        verify(userAgentParser, times(1)).parse(ua);
        verify(repository, times(0)).persistDeviceProfile(any(DeviceProfile.class));
        verify(repository, times(1)).incrementHitCount(deviceId);
        verify(repository, times(1)).findDeviceProfiles(any(UserAgent.class));
        verify(deviceProfileIdGenerator, times(0)).newId(any(UserAgent.class));
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(userAgentParser);
        verifyNoMoreInteractions(deviceProfileIdGenerator);
    }

    @Test
    void getDevicesByOS_WhenNullOSName_ShouldThrowException() {
        String osName = null;
        assertThrows(DeviceProfileException.class, () -> { service.getDevicesByOS(osName);});
        verify(repository, times(0)).findDeviceProfilesByOSName(osName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getDevicesByOS_WhenEmptyOSName_ShouldThrowException() {
        String osName = "";
        assertThrows(DeviceProfileException.class, () -> { service.getDevicesByOS(osName);});
        verify(repository, times(0)).findDeviceProfilesByOSName(osName);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getDevicesByOS_WhenValidOSName_NotExists_ShouldReturnEmptyList() throws Exception {
        String osName = "alternative OS";
        when(repository.findDeviceProfilesByOSName(osName)).thenReturn(List.of());

        var devices = service.getDevicesByOS(osName);

        assertEquals(0, devices.size(), "Device list should be empty");
        verify(repository, times(1)).findDeviceProfilesByOSName(osName.toLowerCase());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getDevicesByOS_WhenValidOSName_Exists_ShouldReturnList() throws Exception {
        String osName = "windows";
        UserAgent userAgent = new UserAgent(osName, "10", "Chrome", "90");
        var device = new DeviceProfile("deviceId", 0L, userAgent);
        when(repository.findDeviceProfilesByOSName(osName)).thenReturn(List.of(device));

        var devices = service.getDevicesByOS(osName);

        assertEquals(1, devices.size(), "Device list should contain 1 device");
        assertEquals(device.deviceId(), devices.getFirst().deviceId(), "Device ID should match");
        assertEquals(device.userAgent().osName(), devices.getFirst().userAgent().osName(), "OS Name should match");
        assertEquals(device.userAgent().osVersion(), devices.getFirst().userAgent().osVersion(), "OS Version should match");
        assertEquals(device.userAgent().browserName(), devices.getFirst().userAgent().browserName(), "Browser Name should match");
        assertEquals(device.userAgent().browserVersion(), devices.getFirst().userAgent().browserVersion(), "Browser Version should match");
        assertEquals(device.hitCount(), devices.getFirst().hitCount(), "Hit Count should match");

        verify(repository, times(1)).findDeviceProfilesByOSName(osName.toLowerCase());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteDeviceById_WhenNullId_ShouldThrowException() {
        String deviceId = null;

        assertThrows(DeviceProfileException.class, () -> { service.deleteDeviceById(deviceId); });
        verify(repository, times(0)).deleteDeviceProfileById(deviceId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteDeviceById_WhenEmptyId_ShouldThrowException() {
        String deviceId = "";

        assertThrows(DeviceProfileException.class, () -> { service.deleteDeviceById(deviceId); });
        verify(repository, times(0)).deleteDeviceProfileById(deviceId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteDeviceById_WhenValidId_ThenDelete() throws DeviceProfileException {
        String deviceId = UUID.randomUUID().toString();

        service.deleteDeviceById(deviceId);

        verify(repository, times(1)).deleteDeviceProfileById(deviceId);
        verifyNoMoreInteractions(repository);
    }
}
