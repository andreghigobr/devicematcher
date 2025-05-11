package com.experian.devicematcher.controller;

import com.experian.devicematcher.dto.DeviceProfileDTO;
import com.experian.devicematcher.parser.UserAgentDeviceRegexParser;
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
    private UserAgentDeviceRegexParser userAgentParser;

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
        // Arrange
        String userAgentString = "";
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgentString);

        // Act
        ResponseEntity<DeviceProfileDTO> response = restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void matchDevice_WithInvalidUserAgent_ShouldReturnBadRequest() throws Exception {
        // Arrange
        var userAgentString = "Invalid User-Agent String";
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgentString);

        // Act
        ResponseEntity<DeviceProfileDTO> response = restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void matchDevice_WithValidUserAgent_NewDevice_FirstHit_ShouldReturnDeviceWithHitCount_1() throws Exception {
        // Arrange
        var userAgentString = "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36";
        var userAgent = userAgentParser.parse(userAgentString);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgentString);

        // Act
        ResponseEntity<DeviceProfileDTO> response = restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

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

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgentString);

        // first hit
        restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

        // second hit
        ResponseEntity<DeviceProfileDTO> response = restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

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
        HttpHeaders headers = new HttpHeaders();
        String deviceId = "";

        ResponseEntity<DeviceProfileDTO> response = restTemplate.exchange(
                baseUrl + "/v1/devices/" + deviceId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void getDevicesById_WithValidId_NotExists_ShouldReturnNotFound() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        String deviceId = "INVALID USER ID";

        ResponseEntity<DeviceProfileDTO> response = restTemplate.exchange(
                baseUrl + "/v1/devices/" + deviceId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getDevicesById_WithValidId_Exists_ShouldReturnOK() {
        // Arrange
        var userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36";
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", userAgentString);

        // first hit
        ResponseEntity<DeviceProfileDTO> matchResponse = restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

        assertEquals(HttpStatus.OK, matchResponse.getStatusCode());
        assertNotNull(matchResponse.getBody());

        ResponseEntity<DeviceProfileDTO> response = restTemplate.exchange(
                baseUrl + "/v1/devices/" + matchResponse.getBody().getDeviceId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(matchResponse.getBody().getDeviceId(), response.getBody().getDeviceId());
        assertEquals(matchResponse.getBody().getHitCount(), response.getBody().getHitCount());
        assertEquals(matchResponse.getBody().getOsName(), response.getBody().getOsName());
        assertEquals(matchResponse.getBody().getOsVersion(), response.getBody().getOsVersion());
        assertEquals(matchResponse.getBody().getBrowserName(), response.getBody().getBrowserName());
        assertEquals(matchResponse.getBody().getBrowserVersion(), response.getBody().getBrowserVersion());
    }

    /*@Test
    public void getDevicesByOS_WithValidOS_ShouldReturnDeviceList() {
        // Act
        ResponseEntity<List<DeviceProfile>> response = restTemplate.exchange(
                baseUrl + "/os/android",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<DeviceProfile>>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertTrue(response.getBody().stream()
                .anyMatch(device -> "Android".equalsIgnoreCase(device.getOsName())));
    }

    @Test
    public void deleteDeviceById_WithValidId_ShouldReturnNoContent() {
        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + testDevice.getDeviceId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }*/

    // match device - new device - hit count 1
    // match device - existing device - hit count > 1
    // get device by id - not found
    // get device by id - existing device
    // get device by os - not found
    // get device by os - found
    // delete device - existing device - no content and next get by id call return not found
}
