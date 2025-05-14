package com.experian.devicematcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
public class DeviceMatcherApplication {

    public static final String TIMEZONE = "GMT-3";
    public static final String LOCALE = "pt-BR";

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(TIMEZONE));
        Locale.setDefault(Locale.forLanguageTag(LOCALE));
        SpringApplication.run(DeviceMatcherApplication.class, args);
    }

}
