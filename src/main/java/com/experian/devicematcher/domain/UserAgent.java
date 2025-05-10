package com.experian.devicematcher.domain;

public class UserAgent {
    private final String osName;
    private final String osVersion;
    private final String browserName;
    private final String browserVersion;
    private final String rawUserAgent;

    public UserAgent(String osName, String osVersion, String browserName, String browserVersion, String rawUserAgent) {
        this.osName = osName;
        this.osVersion = osVersion;
        this.browserName = browserName;
        this.browserVersion = browserVersion;
        this.rawUserAgent = rawUserAgent;
    }

    public static UserAgent create(String osName, String osVersion, String browserName, String browserVersion, String rawUserAgent) {
        return new UserAgent(osName, osVersion, browserName, browserVersion, rawUserAgent);
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

    public String getRawUserAgent() {
        return rawUserAgent;
    }

    @Override
    public String toString() {
        return "UserAgent{" +
                "osName='" + osName + '\'' +
                ", osVersion='" + osVersion + '\'' +
                ", browserName='" + browserName + '\'' +
                ", browserVersion='" + browserVersion + '\'' +
                ", rawUserAgent='" + rawUserAgent + '\'' +
                '}';
    }
}
