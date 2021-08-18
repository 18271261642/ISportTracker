package com.isport.tracker.entity;


import com.isport.isportlibrary.entry.BaseDevice;

/**
 * Created by feige on 2017/4/20.
 */

public class MyBaseDevice extends BaseDevice {

    private float version;

    private MyBaseDevice(float version,String name, String mac, int rssi, long connectedTime, int profileType, int deviceType){
        super(name,mac,rssi,connectedTime,profileType,deviceType);
        this.version = version;
    }

    public MyBaseDevice(float version,BaseDevice baseDevice){
        this(version,baseDevice.getName(),baseDevice.getMac(),baseDevice.getRssi(),baseDevice.getConnectedTime(),baseDevice.getProfileType(),baseDevice.getDeviceType());
    }

    public float getVersion() {
        return version;
    }

    public void setVersion(float version) {
        this.version = version;
    }
}
