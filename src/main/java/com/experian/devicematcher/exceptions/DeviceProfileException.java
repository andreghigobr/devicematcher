package com.experian.devicematcher.exceptions;

public class DeviceProfileException extends Exception {
    private static final long serialVersionUID = 1L;

    public DeviceProfileException(String message) {
        super(message);
    }

    public DeviceProfileException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceProfileException(Throwable cause) {
        super(cause);
    }
}