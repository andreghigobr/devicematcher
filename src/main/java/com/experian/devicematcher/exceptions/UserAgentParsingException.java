package com.experian.devicematcher.exceptions;

public class UserAgentParsingException extends Exception {
    private static final long serialVersionUID = 1L;

    public final String userAgent;

    public UserAgentParsingException(String message, String userAgent, Throwable cause) {
        super(message, cause);
        this.userAgent = userAgent;
    }
}
