package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.UserAgentParsingException;

public interface UserAgentParser {
    UserAgent parse(String userAgentString) throws UserAgentParsingException;
}