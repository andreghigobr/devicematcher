package com.experian.devicematcher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI deviceMatcherOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DeviceMatcher API")
                        .description("API for identifying and tracking devices based on User-Agent strings")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Experian")
                                .url("https://www.experian.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server")
                ));
    }
}
