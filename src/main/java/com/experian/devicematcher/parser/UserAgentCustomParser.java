package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.SemVersion;
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

    private static final String UNKNOWN = "unknown";
    private static final String OTHER = "Other";
    private final Parser uaParser;

    @Autowired
    public UserAgentCustomParser(Parser uaParser) {
        this.uaParser = uaParser;
    }

    @Override
    public UserAgent parse(String userAgentString) throws UserAgentParsingException {
        try {
            logger.info("Parsing User-Agent string: {}", userAgentString);
            if (StringUtils.isBlank(userAgentString)) throw new IllegalArgumentException("User-Agent string is blank");

            Client client = uaParser.parse(userAgentString);

            return new UserAgent(
                parseOSName(client).toLowerCase(),
                parseOSVersion(client),
                parseBrowserName(client).toLowerCase(),
                parseBrowserVersion(client)
            );
        } catch (Exception ex) {
            throw new UserAgentParsingException(ex);
        }
    }

    private String parseOSName(Client client) {
        if (client.os.family == null) return UNKNOWN;
        return client.os.family.equalsIgnoreCase(OTHER) ? UNKNOWN : client.os.family;
    }

    private SemVersion parseOSVersion(Client client) {
        var major = client.os.major == null ? 0L : Long.parseLong(client.os.major);
        var minor = client.os.minor == null ? 0L : Long.parseLong(client.os.minor);
        var patch = client.os.patch == null ? 0L : Long.parseLong(client.os.patch);
        return new SemVersion(major, minor, patch);
    }

    private String parseBrowserName(Client client) {
        if (client.userAgent.family == null) return UNKNOWN;
        return client.userAgent.family.equalsIgnoreCase(OTHER) ? UNKNOWN : client.userAgent.family;
    }

    private SemVersion parseBrowserVersion(Client client) {
        var major = client.userAgent.major == null ? 0L : Long.parseLong(client.userAgent.major);
        var minor = client.userAgent.minor == null ? 0L : Long.parseLong(client.userAgent.minor);
        var patch = client.userAgent.patch == null ? 0L : Long.parseLong(client.userAgent.patch);
        return new SemVersion(major, minor, patch);
    }
}
