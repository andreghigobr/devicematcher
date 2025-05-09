package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.exceptions.UserAgentParsingException;

public interface UserAgentDeviceParser {
    public DeviceProfile parse(String userAgent) throws UserAgentParsingException;
}