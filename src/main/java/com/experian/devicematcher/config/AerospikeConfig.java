package com.experian.devicematcher.config;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.IAerospikeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AerospikeConfig {
    @Value("${aerospike.host}")
    private String hostname;

    @Value("${aerospike.port}")
    private Integer port;

    private static final Logger logger = LoggerFactory.getLogger(AerospikeConfig.class);

    @Bean
    public IAerospikeClient aerospikeClient() {
        logger.info("Configuring Aerospike client | hostname={} port={}", hostname, port);
        return new AerospikeClient(hostname, port);
    }
}
