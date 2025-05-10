package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.UserAgent;
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
    public UserAgent parse(String userAgent) throws UserAgentParsingException {
        try {
            logger.info("Parsing User-Agent String | userAgent={}", userAgent);

            if (StringUtils.isBlank(userAgent)) throw new IllegalArgumentException("Blank User-Agent string");

            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(userAgent);

            if (!matcher.find()) throw new IllegalArgumentException("Invalid User-Agent string: " + userAgent);

            String osName = matcher.group(1) == null ? "" : matcher.group(1).toLowerCase();
            String osVersion = matcher.group(2) == null ? "" : matcher.group(2).toLowerCase();
            String browserName = matcher.group(4) == null ? "" : matcher.group(4).toLowerCase();
            String browserVersion = matcher.group(5) == null ? "" : matcher.group(5).toLowerCase();

            return UserAgent.create(
                    osName,
                    osVersion,
                    browserName,
                    browserVersion
            );
        } catch (Exception ex) {
            logger.error("Error parsing User-Agent string: {}", ex.getMessage(), ex);
            throw new UserAgentParsingException("Error parsing User-Agent string", userAgent, ex);
        }
    }
}
