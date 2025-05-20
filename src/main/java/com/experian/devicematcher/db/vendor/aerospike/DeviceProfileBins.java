package com.experian.devicematcher.db.vendor.aerospike;

import com.aerospike.client.Bin;
import com.aerospike.client.Record;
import com.experian.devicematcher.domain.DeviceProfile;
import com.experian.devicematcher.domain.SemVersion;
import com.experian.devicematcher.domain.UserAgent;

public class DeviceProfileBins {
    static final String DEVICE_ID = "deviceId";
    static final String HIT_COUNT = "hitCount";
    static final String OS_NAME = "osName";
    static final String OS_VERSION = "osVersion";
    static final String BROWSER_NAME = "browserName";
    static final String BROWSER_VERSION = "browserVersion";

    private DeviceProfileBins() {
        throw new IllegalStateException("Utility class");
    }

    public static DeviceProfile toEntity(Record rec) {
        if (rec == null) return null;
        return new DeviceProfile(
            rec.getString(DEVICE_ID),
            rec.getLong(HIT_COUNT),
            new UserAgent(
                rec.getString(OS_NAME),
                SemVersion.parse(rec.getString(OS_VERSION)),
                rec.getString(BROWSER_NAME),
                SemVersion.parse(rec.getString(BROWSER_VERSION))
            )
        );
    }

    public static Bin[] toBins(DeviceProfile device) {
        return new Bin[]{
            new Bin(DEVICE_ID, device.deviceId()),
            new Bin(HIT_COUNT, device.hitCount()),
            new Bin(OS_NAME, device.userAgent().osName()),
            new Bin(OS_VERSION, device.userAgent().osVersion().toString()),
            new Bin(BROWSER_NAME, device.userAgent().browserName()),
            new Bin(BROWSER_VERSION, device.userAgent().browserVersion().toString())
        };
    }
}
