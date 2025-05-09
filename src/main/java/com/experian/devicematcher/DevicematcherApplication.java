package com.experian.devicematcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
public class DevicematcherApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT-3"));
		Locale.setDefault(Locale.forLanguageTag("pt-BR"));

		SpringApplication.run(DevicematcherApplication.class, args);
	}

}
