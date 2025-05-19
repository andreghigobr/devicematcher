package com.experian.devicematcher.parser;

import com.experian.devicematcher.exceptions.UserAgentParsingException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua_parser.Parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
public class UserAgentDeviceRegexParserTest {
    private final Parser uaParser = new Parser();
    private final UserAgentParser parser = new UserAgentCustomParser(uaParser);

    @Test
    public void parseUserAgent_WhenUserAgentIsNull_ShouldThrowException() {
        // Arrange
        String userAgent = null;

        // Act & Assert
        assertThrows(UserAgentParsingException.class, () -> parser.parse(userAgent));
    }

    @ParameterizedTest
    @CsvSource({
        "'INVALID_INPUT_Mozillaxxxxxxxxxxxxxxxxfari/53df', 'unknown', '', 'unknown', ''",
        "'Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0', 'linux', '', 'firefox', '102.0.0'",
        "'Mozilla/5.0 (X11; Linux 86_64) AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36', 'linux', '', 'safari', ''",
        "'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36', 'windows', '10.0.0', 'chrome', '116.0.5845'",
        "'Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Mobile Safari/537.36', 'android', '12.0.0', 'chrome mobile', '116.0.5845'",
        "'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1', 'ios', '16.0.0', 'mobile safari', '16.0.0'",
        "'Mozilla/5.0 (Macintosh; Intel Mac OS X 12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36 Edg/116.0.1938.69', 'mac os x', '12.6.0', 'edge', '116.0.1938'",
        "'Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko', 'windows', '10.0.0', 'ie', '11.0.0'",
        "'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.110 Safari/537.36 Edg/116.0.1938.69', 'linux', '', 'edge', '116.0.1938'",
        "'Mozilla/5.0 (Android 12; Mobile; rv:102.0) Gecko/102.0 Firefox/102.0', 'android', '12.0.0', 'firefox mobile', '102.0.0'",
        "'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) CriOS/116.0.5845.110 Mobile/15E148 Safari/604.1', 'ios', '16.0.0', 'chrome mobile ios', '116.0.5845'",
    })
    public void parseUserAgent_WhenValidUserAgent_ShouldReturnDevice(
            String userAgent,
            String expectedOsName,
            String expectedOsVersion,
            String expectedBrowserName,
            String expectedBrowserVersion
    ) throws UserAgentParsingException {
        // Act
        var ua = parser.parse(userAgent);

        var actualOsName = ua.osName().toLowerCase();
        var actualOsVersion = ua.osVersion();
        var actualBrowserName = ua.browserName();
        var actualBrowserVersion = ua.browserVersion();

        // Assert
        assertEquals(expectedOsName, actualOsName, "OS Name should be " + expectedOsName);
        assertEquals(expectedOsVersion, actualOsVersion, "OS Version should be " + expectedOsVersion);
        assertEquals(expectedBrowserName, actualBrowserName, "Browser Name should be " + expectedBrowserName);
        assertEquals(expectedBrowserVersion, actualBrowserVersion, "Browser Version should be " + expectedBrowserVersion);
    }
}
