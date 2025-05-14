package com.experian.devicematcher.domain;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
public class DeviceProfileUserAgentIdGeneratorTest {
    private final DeviceProfileIdGenerator idGenerator = new DeviceProfileUserAgentProfileIdGenerator();

    @Test
    public void deviceProfileId_withIphoneUserAgent() {
        var userAgent = UserAgent.create("iPhone", "15.0", "Safari", "15.0");

        var deviceId = idGenerator.newId(userAgent);

        assertEquals("iphone", deviceId.split("-")[0]);
    }
}
