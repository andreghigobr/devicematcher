package com.experian.devicematcher.exceptions;

public class DeviceProfileNotFoundException extends DeviceProfileException {
    private static final long serialVersionUID = 1L;

    public DeviceProfileNotFoundException(String message) {
        super(message);
    }

    public DeviceProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceProfileNotFoundException(Throwable cause) {
        super(cause);
    }
}
