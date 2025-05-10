package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.UserAgentParsingException;

public interface UserAgentDeviceParser {
    UserAgent parse(String userAgent) throws UserAgentParsingException;
}