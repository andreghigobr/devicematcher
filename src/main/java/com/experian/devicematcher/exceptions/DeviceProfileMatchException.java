package com.experian.devicematcher.exceptions;

public class DeviceProfileMatchException extends DeviceProfileException {
    private static final long serialVersionUID = 1L;

    private String userAgent;

    public DeviceProfileMatchException(String message) {
        super(message);
    }

    public DeviceProfileMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceProfileMatchException(Throwable cause) {
        super(cause);
    }

    public DeviceProfileMatchException(String message, String userAgent) {
        super(message);
        this.userAgent = userAgent;
    }

    public DeviceProfileMatchException(String message, String userAgent, Throwable cause) {
        super(message, cause);
        this.userAgent = userAgent;
    }
}
