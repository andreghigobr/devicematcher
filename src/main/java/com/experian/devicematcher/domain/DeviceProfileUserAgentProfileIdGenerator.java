package com.experian.devicematcher.domain;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeviceProfileUserAgentProfileIdGenerator implements DeviceProfileIdGenerator {
    @Override
    public String newId(UserAgent userAgent) {
        var prefix = userAgent.getOsName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        return prefix + "-" + UUID.randomUUID();
    }
}
