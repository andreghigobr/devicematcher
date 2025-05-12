package com.experian.devicematcher.config;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.IAerospikeClient;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.IndexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AerospikeConfig {
    private static final Logger logger = LoggerFactory.getLogger(AerospikeConfig.class);

    @Value("${aerospike.host}")
    private String hostname;

    @Value("${aerospike.port}")
    private Integer port;

    @Value("${aerospike.namespace}")
    private String namespace;

    @Value("${aerospike.set}")
    private String setName;

    @Value("${aerospike.query-policy.max-records}")
    private Long maxRecords;

    @Value("${aerospike.policy.timeout}")
    private Integer timeout;

    @Bean
    public IAerospikeClient aerospikeClient(
        @Autowired @Qualifier("aerospikeDefaultPolicy") Policy aerospikeDefaultPolicy
    ) {
        try {
            logger.info("Configuring Aerospike client | hostname={} port={}", hostname, port);
            var client = new AerospikeClient(hostname, port);
            createIndex(client, namespace, setName);
            return client;
        } catch (Exception ex) {
            logger.error("Error creating Aerospike client: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    private Map<String, String> binsByIndex() {
        return Map.of(
            "osname_idx", "osName"
        );
    }

    private void createIndex(AerospikeClient client, String namespace, String setName) {
        try {
            binsByIndex().forEach((indexName, binName) -> {
                logger.info("Creating Aerospike Index | namespace={} setName={} indexName={} binName={}", namespace, setName, indexName, binName);
                client.createIndex(null, namespace, setName, indexName, binName, IndexType.STRING).waitTillComplete();
            });
        } catch (Exception ex) {
            logger.error("Error creating Aerospike index: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Bean(name = "aerospikeDefaultPolicy")
    public Policy aerospikeDefaultPolicy() {
        logger.info("Configuring Aerospike Default Policy | timeout={}", timeout);
        Policy policy = new Policy();
        policy.setTimeout(timeout); // Set timeout to 1 second
        return policy;
    }

    @Bean(name = "aerospikeQueryPolicy")
    public QueryPolicy aerospikeQueryPolicy() {
        logger.info("Configuring Aerospike Query Policy | timeout={} maxRecords={}", timeout, maxRecords);
        QueryPolicy queryPolicy = new QueryPolicy();
        queryPolicy.setTimeout(timeout);
        queryPolicy.setMaxRecords(maxRecords);
        return queryPolicy;
    }

    @Bean(name = "aerospikeWritePolicy")
    public WritePolicy aerospikeWritePolicy() {
        logger.info("Configuring Aerospike Write Policy | timeout={}", timeout);
        WritePolicy writePolicy = new WritePolicy();
        writePolicy.setTimeout(timeout);
        return writePolicy;
    }
}
