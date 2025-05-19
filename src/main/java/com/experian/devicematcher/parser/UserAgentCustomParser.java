package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.UserAgentParsingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentCustomParser implements UserAgentParser {
    private static final Logger logger = LoggerFactory.getLogger(UserAgentCustomParser.class);

    private final Parser uaParser;

    @Autowired
    public UserAgentCustomParser(Parser uaParser) {
        this.uaParser = uaParser;
    }

    @Override
    public UserAgent parse(String userAgentString) throws UserAgentParsingException {
        try {
            if (StringUtils.isBlank(userAgentString)) throw new IllegalArgumentException("User-Agent string is blank");

            Client client = uaParser.parse(userAgentString);

            return new UserAgent(
                parseOSName(client).toLowerCase(),
                parseOSVersion(client),
                parseBrowserName(client).toLowerCase(),
                parseBrowserVersion(client)
            );
        } catch (Exception ex) {
            logger.error("Error parsing User-Agent string: {}", ex.getMessage(), ex);
            throw new UserAgentParsingException(ex);
        }
    }

    private String parseOSName(Client client) {
        return client.os.family == null ? "Unknown" : (client.os.family.equalsIgnoreCase("Other") ? "Unknown" : client.os.family);
    }

    private String parseOSVersion(Client client) {
        var osMajorVersion = client.os.major == null ? "" : client.os.major;
        var osMinorVersion = client.os.minor == null ? "0" : client.os.minor;
        var osPatchVersion = client.os.patch == null ? "0" : client.os.patch;
        return osMajorVersion.isEmpty() ? "" : (osMajorVersion + "." + osMinorVersion + "." + osPatchVersion);
    }

    private String parseBrowserName(Client client) {
        return client.userAgent.family == null ? "Unknown" : (client.userAgent.family.equalsIgnoreCase("Other") ? "Unknown" : client.userAgent.family);
    }

    private String parseBrowserVersion(Client client) {
        var browserMajorVersion = client.userAgent.major == null ? "" : client.userAgent.major;
        var browserMinorVersion = client.userAgent.minor == null ? "0" : client.userAgent.minor;
        var browserPatchVersion = client.userAgent.patch == null ? "0" : client.userAgent.patch;
        return browserMajorVersion.isBlank() ? "" : (browserMajorVersion + "." + browserMinorVersion + "." + browserPatchVersion);
    }
}
