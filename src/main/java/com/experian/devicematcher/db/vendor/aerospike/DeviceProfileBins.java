package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.Bin;
import com.aerospike.client.Record;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.domain.UserAgent;

public class DeviceProfileBins {
    static final String DEVICE_ID = "deviceId";
    static final String HIT_COUNT = "hitCount";
    static final String OS_NAME = "osName";
    static final String OS_VERSION = "osVersion";
    static final String BROWSER_NAME = "browserName";
    static final String BROWSER_VERSION = "browserVersion";

    public static DeviceProfile toEntity(Record record) {
        if (record == null) return null;

        UserAgent userAgent = new UserAgent(
            record.getString(OS_NAME),
            record.getString(OS_VERSION),
            record.getString(BROWSER_NAME),
            record.getString(BROWSER_VERSION)
        );

        return new DeviceProfile(
            record.getString(DEVICE_ID),
            record.getLong(HIT_COUNT),
            userAgent
        );
    }

    public static Bin[] toBins(DeviceProfile device) {
        return new Bin[]{
            new Bin(DEVICE_ID, device.deviceId()),
            new Bin(HIT_COUNT, device.hitCount()),
            new Bin(OS_NAME, device.userAgent().osName()),
            new Bin(OS_VERSION, device.userAgent().osVersion()),
            new Bin(BROWSER_NAME, device.userAgent().browserName()),
            new Bin(BROWSER_VERSION, device.userAgent().browserVersion())
        };
    }
}
