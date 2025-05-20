package com.experian.devicematcher.domain;

public record UserAgent(
    String osName,
    SemVersion osVersion,
    String browserName,
    SemVersion browserVersion
) {
    public UserAgent {
        if (osName == null || osName.isBlank()) {
            throw new IllegalArgumentException("osName cannot be null or blank");
        }
        if (browserName == null || browserName.isBlank()) {
            throw new IllegalArgumentException("browserName cannot be null or blank");
        }
    }

    public UserAgent(String osName, String osVersion, String browserName, String browserVersion) {
        this(osName, SemVersion.parse(osVersion), browserName, SemVersion.parse(browserVersion));
    }
}
