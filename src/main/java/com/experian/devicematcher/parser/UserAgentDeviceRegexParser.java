package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.UserAgentParsingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ua_parser.Parser;

import java.util.regex.Pattern;

@Component
public class UserAgentDeviceRegexParser implements UserAgentDeviceParser {
    private static final Logger logger = LoggerFactory.getLogger(UserAgentDeviceRegexParser.class);

    @Override
    public UserAgent parse(String userAgent) throws UserAgentParsingException {
        try {
            logger.info("Parsing User-Agent String | userAgent={}", userAgent);

            if (StringUtils.isBlank(userAgent)) {
                throw new IllegalArgumentException("Blank User-Agent string");
            }

            String regex = "Mozilla/\\d\\.\\d \\(([^;\\)]+)(?:; ([^;\\)]+))?(?:; ([^;\\)]+))?\\).*? ([^/]+)/([\\d\\.]+)";
            Pattern pattern = Pattern.compile(regex);
            var matcher = pattern.matcher(userAgent);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Invalid User-Agent string format: " + userAgent);
            }

            var osName = matcher.group(1);
            var osVersion = matcher.group(2);
            var browserName = matcher.group(3);
            var browserVersion = matcher.group(4);

            return UserAgent.create(
                osName == null ? "" : osName.toLowerCase(),
                osVersion == null ? "" : osVersion.toLowerCase(),
                browserName == null ? "" : browserName.toLowerCase(),
                browserVersion == null ? "" : browserVersion.toLowerCase(),
                userAgent
            );
        } catch (Exception ex) {
            logger.error("Error parsing User-Agent string: {}", ex.getMessage(), ex);
            throw new UserAgentParsingException("Error parsing User-Agent string", userAgent, ex);
        }
    }
}
