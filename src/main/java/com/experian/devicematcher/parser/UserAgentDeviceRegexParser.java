package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.exceptions.UserAgentParsingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserAgentDeviceRegexParser implements UserAgentDeviceParser {
    private static final Logger logger = LoggerFactory.getLogger(UserAgentDeviceRegexParser.class);

    private final String REGEX = "Mozilla/\\d\\.\\d \\(([^;\\)]+)(?:; ([^;\\)]+))?(?:; ([^;\\)]+))?\\) .*? ([^/]+)/([\\d\\.]+)";

    @Override
    public DeviceProfile parse(String userAgent) throws UserAgentParsingException {
        try {
            logger.info("Parsing User Agent | userAgent={}", userAgent);
            if (StringUtils.isBlank(userAgent)) return DeviceProfile.UNKNOWN;

            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(userAgent);

            if (!matcher.find()) throw new IllegalArgumentException("Invalid User-Agent: " + userAgent);

            String osName = matcher.group(1);
            String osVersion = matcher.group(2) != null ? matcher.group(2) : DeviceProfile.UNKNOWN_VERSION;
            String browserName = matcher.group(4);
            String browserVersion = matcher.group(5);

            return DeviceProfile.create(
                    osName,
                    osVersion,
                    browserName,
                    browserVersion,
                    userAgent
            );
        } catch (Exception ex) {
            logger.error("Error parsing user agent: {}", ex.getMessage(), ex);
            throw new UserAgentParsingException("Error parsing user agent", userAgent, ex);
        }
    }
}
