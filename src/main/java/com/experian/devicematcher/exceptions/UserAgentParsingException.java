package com.experian.devicematcher.exceptions;

public class UserAgentParsingException extends Exception {
    private static final long serialVersionUID = 1L;

    public UserAgentParsingException(String message) {
        super(message);
    }

    public UserAgentParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAgentParsingException(Throwable cause) {
        super(cause);
    }
}
