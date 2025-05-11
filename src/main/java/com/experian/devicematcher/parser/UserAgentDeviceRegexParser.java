package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.UserAgentParsingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentDeviceRegexParser implements UserAgentDeviceParser {
    private static final Logger logger = LoggerFactory.getLogger(UserAgentDeviceRegexParser.class);

    @Override
    public UserAgent parse(String userAgent) throws UserAgentParsingException {
        try {
            if (StringUtils.isBlank(userAgent)) {
                throw new IllegalArgumentException("User-Agent string is blank");
            }

            Parser uaParser = new Parser();
            Client client = uaParser.parse(userAgent);

            var osName = client.os.family == null ? "Unknown" : (client.os.family.equalsIgnoreCase("Other") ? "Unknown" : client.os.family);

            var osMajorVersion = client.os.major == null ? "" : client.os.major;
            var osMinorVersion = client.os.minor == null ? "0" : client.os.minor;
            var osPatchVersion = client.os.patch == null ? "0" : client.os.patch;
            var osVersion = osMajorVersion.isEmpty() ? "" : (osMajorVersion + "." + osMinorVersion + "." + osPatchVersion);

            var browserName = client.userAgent.family == null ? "Unknown" : (client.userAgent.family.equalsIgnoreCase("Other") ? "Unknown" : client.userAgent.family);

            var browserMajorVersion = client.userAgent.major == null ? "" : client.userAgent.major;
            var browserMinorVersion = client.userAgent.minor == null ? "0" : client.userAgent.minor;
            var browserPatchVersion = client.userAgent.patch == null ? "0" : client.userAgent.patch;
            var browserVersion = browserMajorVersion.isBlank() ? "" : (browserMajorVersion + "." + browserMinorVersion + "." + browserPatchVersion);

            return UserAgent.create(
                    osName.toLowerCase(),
                    osVersion,
                    browserName.toLowerCase(),
                    browserVersion
            );
        } catch (Exception ex) {
            logger.error("Error parsing User-Agent string: {}", ex.getMessage(), ex);
            throw new UserAgentParsingException(ex);
        }
    }
}
