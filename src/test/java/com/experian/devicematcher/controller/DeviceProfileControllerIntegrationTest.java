package com.experian.devicematcher.controller;

import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.dto.DeviceProfileDTO;
import com.experian.devicematcher.parser.UserAgentDeviceRegexParser;
import com.experian.devicematcher.repository.DeviceProfileRepository;
import org.junit.jupiter.api.*;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class DeviceProfileControllerIntegrationTest {
    private static GenericContainer<?> aerospikeContainer;

    private static final int AEROSPIKE_PORT = 3000;
    private static final String AEROSPIKE_NAMESPACE = "devicematcher";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DeviceProfileRepository repository;

    @Autowired
    private UserAgentDeviceRegexParser userAgentParser;

    private String baseUrl;
    private DeviceProfile testDevice;


    static {
        aerospikeContainer = new GenericContainer<>(
                DockerImageName.parse("aerospike/aerospike-server:latest"))
                .withExposedPorts(AEROSPIKE_PORT)
                .withClasspathResourceMapping("aerospike-test.conf", "/opt/aerospike/etc/aerospike.conf", BindMode.READ_ONLY)
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
        registry.add("aerospike.set", () -> "devices");
    }

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    public void tearDownTest() {
        // Clean up test data
        try {
            repository.deleteDeviceById(testDevice.getDeviceId());
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @AfterAll
    public static void tearDownContainer() {
        try {
            if (aerospikeContainer != null && aerospikeContainer.isRunning()) {
                aerospikeContainer.stop();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
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

        restTemplate.exchange(
                baseUrl + "/v1/devices",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                DeviceProfileDTO.class
        );

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
