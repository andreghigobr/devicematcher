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
        return new UserAgent(osName, osVersion, browserName, browserVersion);
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

    @Override
    public String toString() {
        return "UserAgent{" +
            "osName='" + osName + '\'' +
            ", osVersion='" + osVersion + '\'' +
            ", browserName='" + browserName + '\'' +
            ", browserVersion='" + browserVersion + '\'' +
            '}';
    }
}
