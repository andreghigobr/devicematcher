package com.experian.devicematcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua_parser.Parser;

@Configuration
public class UAParserConfig {
    @Bean
    public Parser uaParser() {
        return new Parser();
    }
}
