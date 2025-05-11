package com.experian.devicematcher.domain;

import com.experian.devicematcher.parser.UserAgentDeviceParser;
import com.experian.devicematcher.parser.UserAgentDeviceRegexParser;

public class DeviceProfileTest {
    private DeviceIdGenerator idGenerator = new DeviceUUIDGenerator();
    private UserAgentDeviceParser userAgentParser = new UserAgentDeviceRegexParser()

    @Test
    public void createNewDevice_fromIdGenerator_and_UserAgent() {
        var userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) CriOS/116.0.5845.110 Mobile/15E148 Safari/604.1";
        var userAgent = userAgentParser.parse(userAgentString);

        var device = DeviceProfile.from(idGenerator::generateId, userAgent);

        assertEquals(userAgent.getOsName(), device.getOsName());
        assertEquals(userAgent.getOsVersion(), device.getOsVersion());
        assertEquals(userAgent.getBrowserName(), device.getBrowserName());
        assertEquals(userAgent.getBrowserVersion(), device.getBrowserVersion());
    }
}
