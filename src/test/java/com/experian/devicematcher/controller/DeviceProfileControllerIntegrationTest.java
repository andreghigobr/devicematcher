package com.experian.devicematcher.controller;

import com.experian.devicematcher.dto.DeviceProfileDTO;
import com.experian.devicematcher.dto.DeviceProfilesDTO;
import com.experian.devicematcher.parser.UserAgentCustomParser;
import com.experian.devicematcher.repository.DeviceProfileRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class DeviceProfileControllerIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(DeviceProfileControllerIntegrationTest.class);

    private static GenericContainer<?> aerospikeContainer;

    private static final int AEROSPIKE_PORT = 3000;
    private static final String AEROSPIKE_NAMESPACE = "devicematcher";
    // conf
    private static final String AEROSPIKE_CONF = "aerospike-test.conf";
    private static final String AEROSPIKE_SET = "devices";
    private static final String AEROSPIKE_HOST = "localhost";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DeviceProfileRepository repository;

    @Autowired
    private UserAgentCustomParser userAgentParser;

    private String baseUrl;

    static {
        logger.info("Setting up Aerospike container at {}:{} namespace={} set={} conf={}", AEROSPIKE_HOST, AEROSPIKE_PORT, AEROSPIKE_NAMESPACE, AEROSPIKE_SET, AEROSPIKE_CONF);

        aerospikeContainer = new GenericContainer<>(
                DockerImageName.parse("aerospike/aerospike-server:latest"))
                .withExposedPorts(AEROSPIKE_PORT)
                .withClasspathResourceMapping(AEROSPIKE_CONF, "/opt/aerospike/etc/aerospike.conf", BindMode.READ_ONLY)
                .withEnv("NAMESPACE", AEROSPIKE_NAMESPACE)
                .withLogConsumer(outputFrame -> System.out.println("Aerospike: " + outputFrame.getUtf8String()));

        aerospikeContainer.start();
    }

    // Dynamically update Spring properties with container values
    @DynamicPropertySource
    static void registerAerospikeProperties(DynamicPropertyRegistry registry) {
        registry.add("aerospike.host", aerospikeContainer::getHost);
        registry.add("aerospike.port", () -> aerospikeContainer.getMappedPort(AEROSPIKE_PORT));
        registry.add("aerospike.query-policy.max-records", () -> 1000L);
        registry.add("aerospike.policy.timeout", () -> 1000);
        registry.add("aerospike.namespace", () -> AEROSPIKE_NAMESPACE);
        registry.add("aerospike.set", () -> AEROSPIKE_SET);
    }

    @BeforeEach
    public void setUpTest() {
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void tearDownContainer() {
        try {
            if (aerospikeContainer != null && aerospikeContainer.isRunning()) {
                aerospikeContainer.stop();
            }
        } catch (Exception e) {
            logger.error("Error tearing down aerospike test container: {}", e.getMessage(), e);
            // Ignore cleanup errors
        }
    }

    @Test
    public void matchDevice_WithBlankUserAgent_ShouldReturnBadRequest() throws Exception {
        // Act
        ResponseEntity<DeviceProfileDTO> response = matchDevice("");
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void matchDevice_WithInvalidUserAgent_ShouldReturnOK_UnknownDevice() throws Exception {
        // Act
        ResponseEntity<DeviceProfileDTO> response = matchDevice("Invalid User-Agent String");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        var device = getDeviceById(response.getBody().getDeviceId()).getBody();
        assertEquals("unknown", device.getOsName());
        assertEquals("", device.getOsVersion());
        assertEquals("unknown", device.getBrowserName());
        assertEquals("", device.getBrowserVersion());
    }

    @Test
    public void matchDevice_WithValidUserAgent_NewDevice_FirstHit_ShouldReturnDeviceWithHitCount_1() throws Exception {
        // Arrange
        var userAgentString = "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36";
        var userAgent = userAgentParser.parse(userAgentString);

        // Act
        ResponseEntity<DeviceProfileDTO> response = matchDevice(userAgentString);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getDeviceId());
        assertEquals(1L, response.getBody().getHitCount()); // new device first hit count
        assertEquals(userAgent.getOsName(), response.getBody().getOsName());
        assertEquals(userAgent.getOsVersion(), response.getBody().getOsVersion());
        assertEquals(userAgent.getBrowserName(), response.getBody().getBrowserName());
        assertEquals(userAgent.getBrowserVersion(), response.getBody().getBrowserVersion());
    }

    @Test
    public void matchDevice_WithValidUserAgent_ExistingDevice_SecondHit_ShouldReturnDeviceWithHitCount_2() throws Exception {
        // Arrange
        var userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
        var userAgent = userAgentParser.parse(userAgentString);

        // first hit
        matchDevice(userAgentString);

        // second hit
        ResponseEntity<DeviceProfileDTO> response = matchDevice(userAgentString);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getDeviceId());
        assertEquals(2L, response.getBody().getHitCount()); // not new device, second hit
        assertEquals(userAgent.getOsName(), response.getBody().getOsName());
        assertEquals(userAgent.getOsVersion(), response.getBody().getOsVersion());
        assertEquals(userAgent.getBrowserName(), response.getBody().getBrowserName());
        assertEquals(userAgent.getBrowserVersion(), response.getBody().getBrowserVersion());
    }

    @Test
    public void getDevicesById_WithBlankId_ShouldReturnNotFoundResource() {
        String deviceId = "";
        ResponseEntity<DeviceProfileDTO> response = getDeviceById(deviceId);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void getDevicesById_WithValidId_NotExists_ShouldReturnNotFound() throws Exception {
        String deviceId = "INVALID USER ID";
        ResponseEntity<DeviceProfileDTO> response = getDeviceById(deviceId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getDevicesById_WithValidId_Exists_ShouldReturnOK() {
        // Arrange
        var userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36";

        // first hit
        ResponseEntity<DeviceProfileDTO> matchResponse = matchDevice(userAgentString);

        assertEquals(HttpStatus.OK, matchResponse.getStatusCode());
        assertNotNull(matchResponse.getBody());

        ResponseEntity<DeviceProfileDTO> response = getDeviceById(matchResponse.getBody().getDeviceId());

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(matchResponse.getBody().getDeviceId(), response.getBody().getDeviceId());
        assertEquals(matchResponse.getBody().getHitCount(), response.getBody().getHitCount());
        assertEquals(matchResponse.getBody().getOsName(), response.getBody().getOsName());
        assertEquals(matchResponse.getBody().getOsVersion(), response.getBody().getOsVersion());
        assertEquals(matchResponse.getBody().getBrowserName(), response.getBody().getBrowserName());
        assertEquals(matchResponse.getBody().getBrowserVersion(), response.getBody().getBrowserVersion());
    }

    @Test
    public void getDevicesByOS_WithBlankOS_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<DeviceProfilesDTO> response = getDevicesByOS("");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getDevicesByOS_WithInvalidOS_ShouldReturnNotFound() {
        // Act
        ResponseEntity<DeviceProfilesDTO> response = getDevicesByOS("INVALID OS");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getDevicesByOS_WithValidOS_ShouldReturnOK() {
        // Act
        var osName = "Linux";
        var device1 = matchDevice("Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0").getBody();
        var device2 = matchDevice("Mozilla/5.0 (X11; Linux 86_64) AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36").getBody();
        var device3 = matchDevice("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36 Edg/116.0.1938.69").getBody();
        var expectedDevices = List.of(device1, device2, device3);

        // Act
        var response = getDevicesByOS(osName);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        var devicesByOS = response.getBody();

        assertEquals(expectedDevices.size(), devicesByOS.getDevices().size());

        assertTrue(expectedDevices.stream().anyMatch(d -> d.getDeviceId().equals(device1.getDeviceId())));
        assertTrue(expectedDevices.stream().anyMatch(d -> d.getDeviceId().equals(device2.getDeviceId())));
        assertTrue(expectedDevices.stream().anyMatch(d -> d.getDeviceId().equals(device3.getDeviceId())));

        assertTrue(devicesByOS.getDevices().stream().allMatch(d -> d.getOsName().equalsIgnoreCase(osName)));
    }

    @Test
    public void deleteDeviceById_WithBlankId_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<DeviceProfileDTO> response = deleteDeviceById("");
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteDeviceById_WithNonExistedId_ShouldReturnOK() {
        String deviceId = "test";
        var deleteResponse = deleteDeviceById(deviceId);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
    }

    @Test
    public void deleteDeviceById_WithDeviceCreated_ShouldDeleteFromDatabase_And_ShouldReturnOK() {
        // Act
        String userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36 Edg/116.0.1938.69";

        // 1 - Match Device and Check Device Exists After Creation
        var matchedDevice = matchDevice(userAgentString).getBody();
        var currentDevice = getDeviceById(matchedDevice.getDeviceId()).getBody();
        assertEquals(matchedDevice.getDeviceId(), currentDevice.getDeviceId());

        // Delete created device profile
        var deleteResponse = deleteDeviceById(currentDevice.getDeviceId());
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Check device profile was removed from database
        var response = getDeviceById(currentDevice.getDeviceId());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    //----------------------------------------
    // Helpers
    //----------------------------------------
    private ResponseEntity<DeviceProfileDTO>  matchDevice(String userAgentString) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgentString);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );
    }

    private ResponseEntity<DeviceProfilesDTO> getDevicesByOS(String osName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("os-name", osName);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DeviceProfilesDTO.class
        );
    }

    private ResponseEntity<DeviceProfileDTO> getDeviceById(String deviceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return restTemplate.exchange(
                baseUrl + "/v1/devices/" + deviceId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );
    }

    private ResponseEntity<DeviceProfileDTO> deleteDeviceById(String deviceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return restTemplate.exchange(
                baseUrl + "/v1/devices/" + deviceId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );
    }
}
