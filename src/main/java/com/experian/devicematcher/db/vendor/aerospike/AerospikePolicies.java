package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.WritePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AerospikePolicies {
    private final Policy defaultPolicy;
    private final QueryPolicy queryPolicy;
    private final WritePolicy writePolicy;

    @Autowired
    public AerospikePolicies(
        @Qualifier("aerospikeDefaultPolicy") Policy defaultPolicy,
        @Qualifier("aerospikeQueryPolicy") QueryPolicy queryPolicy,
        @Qualifier("aerospikeWritePolicy") WritePolicy writePolicy
    ) {
        this.defaultPolicy = defaultPolicy;
        this.queryPolicy = queryPolicy;
        this.writePolicy = writePolicy;
    }

    Policy newDefaultPolicy() {
        return new Policy(defaultPolicy);
    }

    QueryPolicy newQueryPolicy() {
        return new QueryPolicy(queryPolicy);
    }

    WritePolicy newWritePolicy() {
        return new WritePolicy(writePolicy);
    }
}
