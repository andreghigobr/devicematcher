package com.experian.devicematcher.parser;

import com.experian.devicematcher.exceptions.UserAgentParsingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserAgentDeviceRegexParserTest {

    private UserAgentDeviceParser parser = new UserAgentDeviceRegexParser();

    @Test
    public void parseUserAgent_WhenBlankUserAgent_ShouldThrowException() throws UserAgentParsingException {
        // Arrange
        String userAgent = "";

        // Act & Assert
        assertThrows(UserAgentParsingException.class, () -> {parser.parse(userAgent);});
    }

    @Test
    public void parseUserAgent_WhenInvalidUserAgent_ShouldThrowException() throws UserAgentParsingException {
        // Arrange
        String userAgent = "Mozillaxxxxxxxxxxxxxxxxfari/537.36";

        // Act & Assert
        assertThrows(UserAgentParsingException.class, () -> {parser.parse(userAgent);});
    }

    @ParameterizedTest
    @CsvSource({
        "'Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0', 'x11', 'linux x86_64', 'firefox', '102.0'",
        "'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36', 'windows', '10.0', 'chrome', '116.0'",
        "'Mozilla/5.0 (X11; Linux 86_64) AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36', 'linux', '10.0', 'safari', '537.36'",
        "'Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Mobile Safari/537.36', 'android', '12.0', 'chrome', '116.0'",
        "'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1', 'iphone', '16.0', 'safari', '16.0'",
        "'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36 Edg/116.0.1938.69', 'mac', '12.6', 'edge', '116.0'",
        "'Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko', 'windows', '10.0', 'internet explorer', '11.0'",
        "'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36 Edg/116.0.1938.69', 'linux', '10.0', 'edge', '116.0'",
        "'Mozilla/5.0 (Android 12; Mobile; rv:102.0) Gecko/102.0 Firefox/102.0', 'android', '12.0', 'firefox', '102.0'",
        "'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) CriOS/116.0.5845.110 Mobile/15E148 Safari/604.1', 'iphone', '16.0', 'chrome', '116.0'",
    })
    public void parseUserAgent_WhenValidUserAgent_NewDevice_ShouldReturnDevice(
            String userAgent,
            String expectedOsName,
            String expectedOsVersion,
            String expectedBrowserName,
            String expectedBrowserVersion
    ) throws UserAgentParsingException {
        // Act
        var ua = parser.parse(userAgent);

        System.out.println("Parsed User-Agent: " + ua);

        // Assert
        assertEquals(expectedOsName, ua.getOsName(), "OS Name should be " + expectedOsName);
        assertEquals(expectedOsVersion, ua.getOsVersion(), "OS Version should be " + expectedOsVersion);
        assertEquals(expectedBrowserName, ua.getBrowserName(), "Browser Name should be " + expectedBrowserName);
        assertEquals(expectedOsVersion, ua.getBrowserVersion(), "Browser Version should be " + expectedBrowserVersion);
    }
}
