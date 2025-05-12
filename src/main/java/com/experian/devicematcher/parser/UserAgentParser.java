package com.experian.devicematcher.parser;

import com.experian.devicematcher.domain.UserAgent;
import com.experian.devicematcher.exceptions.UserAgentParsingException;

/**
 * Interface for parsing user agent strings.
 * This interface defines a method to parse a user agent string and return a UserAgent object.
 */
public interface UserAgentParser {
    /**
     * Parses a user agent string and returns a UserAgent object.
     * Example: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
     * @param userAgentString the user agent string to parse
     * @return a UserAgent object containing the parsed information
     * @throws UserAgentParsingException if an error occurs during parsing
     */
    UserAgent parse(String userAgentString) throws UserAgentParsingException;
}