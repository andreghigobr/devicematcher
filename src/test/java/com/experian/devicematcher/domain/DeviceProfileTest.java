package com.experian.devicematcher.domain;

import com.experian.devicematcher.parser.UserAgentDeviceParser;
import com.experian.devicematcher.parser.UserAgentDeviceRegexParser;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceProfileTest {
    private UserAgentDeviceParser userAgentParser = new UserAgentDeviceRegexParser();

    @Test
    public void createNewDevice_fromIdSupplier_and_UserAgent() throws Exception {
        var userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) CriOS/116.0.5845.110 Mobile/15E148 Safari/604.1";
        var userAgent = userAgentParser.parse(userAgentString);
        var deviceId = "1234";
        Supplier<String> idSupplier = () -> deviceId;

        var device = DeviceProfile.from(idSupplier, userAgent);

        assertEquals(idSupplier.get(), device.getDeviceId());
        assertEquals(userAgent.getOsName(), device.getOsName());
        assertEquals(userAgent.getOsVersion(), device.getOsVersion());
        assertEquals(userAgent.getBrowserName(), device.getBrowserName());
        assertEquals(userAgent.getBrowserVersion(), device.getBrowserVersion());
    }

    @Test
    public void match_whenUserAgentMatchDevice() throws Exception {
        var userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) CriOS/116.0.5845.110 Mobile/15E148 Safari/604.1";
        var userAgent = userAgentParser.parse(userAgentString);
        var deviceId = "1234";
        Supplier<String> idSupplier = () -> deviceId;

        var device = DeviceProfile.from(idSupplier, userAgent);

        assertTrue(device.match(userAgent));
    }

    @Test
    public void match_whenUserAgentDoesNotMatchDevice() throws Exception {
        var userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) CriOS/116.0.5845.110 Mobile/15E148 Safari/604.1";
        var userAgent = userAgentParser.parse(userAgentString);
        var deviceId = "1234";
        Supplier<String> idSupplier = () -> deviceId;

        var device = new DeviceProfile("1", 0L, "android", "12.0.0", "chrome mobile", "116.0.5845");

        assertFalse(device.match(userAgent));
    }
}
