package com.experian.devicematcher.domain;

public class UserAgent {
    private final String osName;
    private final String osVersion;
    private final String browserName;
    private final String browserVersion;

    public UserAgent(String osName, String osVersion, String browserName, String browserVersion) {
        this.osName = osName;
        this.osVersion = osVersion;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
    }

    public static UserAgent create(String osName, String osVersion, String browserName, String browserVersion) {
        return new UserAgent(osName.toLowerCase(), osVersion.toLowerCase(), browserName.toLowerCase(), browserVersion.toLowerCase());
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getBrowserName() {
        return browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }
}
