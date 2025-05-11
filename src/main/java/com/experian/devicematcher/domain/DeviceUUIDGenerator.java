package com.experian.devicematcher.domain;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeviceUUIDGenerator implements DeviceIdGenerator {
    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
