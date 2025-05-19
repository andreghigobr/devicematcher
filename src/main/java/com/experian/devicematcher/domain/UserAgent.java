package com.experian.devicematcher.domain;

public record UserAgent(
    String osName,
    String osVersion,
    String browserName,
    String browserVersion
) {
}
