package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.Bin;
import com.aerospike.client.Record;
import com.experian.devicematcher.domain.DeviceProfile;

public class DeviceProfileBins {
    static final String DEVICE_ID = "deviceId";
    static final String HIT_COUNT = "hitCount";
    static final String OS_NAME = "osName";
    static final String OS_VERSION = "osVersion";
    static final String BROWSER_NAME = "browserName";
    static final String BROWSER_VERSION = "browserVersion";

    public static DeviceProfile from(Record record) {
        if (record == null) return null;
        return new DeviceProfile(
            record.getString(DEVICE_ID),
            record.getLong(HIT_COUNT),
            record.getString(OS_NAME),
            record.getString(OS_VERSION),
            record.getString(BROWSER_NAME),
            record.getString(BROWSER_VERSION)
        );
    }

    public static Bin[] toBins(DeviceProfile device) {
        return new Bin[] {
            new Bin(DEVICE_ID, device.getDeviceId()),
            new Bin(HIT_COUNT, device.getHitCount()),
            new Bin(OS_NAME, device.getOsName()),
            new Bin(OS_VERSION, device.getOsVersion()),
            new Bin(BROWSER_NAME, device.getBrowserName()),
            new Bin(BROWSER_VERSION, device.getBrowserVersion())
        };
    }
}
