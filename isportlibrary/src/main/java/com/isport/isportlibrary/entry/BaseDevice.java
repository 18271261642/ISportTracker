package com.isport.isportlibrary.entry;

import com.isport.isportlibrary.scanner.ScanRecord;

import java.io.Serializable;

/**
 * Created by chengjiamei on 2016/8/18.
 * base Device class,only show some base info of device,example,name,mac and so on
 * you can inherit it you extend you device function
 */
public class BaseDevice implements Serializable {

    public final static int TYPE_W311N = 0;
    public final static int TYPE_W311T = 1;

    public final static int TYPE_W307N = 5;
    public final static int TYPE_W307S = 6;

    public final static int TYPE_W301N = 10;
    public final static int TYPE_W301S = 11;

    public final static int TYPE_W285S = 16;

    /**
     * p118s W240 activityTracker
     * p118 millionTracker
     * W194
     */

    public final static int TYPE_W194 = 20;

    public final static int TYPE_W240 = 21;
    public final static int TYPE_W240N = 22;
    public final static int TYPE_W240S = 23;
    public final static int TYPE_P118 = 24;
    public final static int TYPE_ACTIVITYTRACKER = 25;
    public final static int TYPE_P118S = 26;
    public final static int TYPE_MILLIONPEDOMETER = 27;
    public final static int TYPE_W240B = 28;
    public final static int TYPE_W285B = 29;
    public final static int TYPE_BLIN16_HEALTH = 30;
    public final static int TYPE_W337B = 31;
    public final static int TYPE_SAS80 = 32; ///285S
    public final static int TYPE_AT100 = 33; ///311N
    public final static int TYPE_AT200 = 34; ///307S
    public final static int TYPE_W316 = 35;
    public final static int TYPE_AS97 = 36;
    public final static int TYPE_W307H = 37;
    public final static int TYPE_W301H = 38;
    public final static int TYPE_W307S_SPACE = 39;

    public final static int TYPE_WEIGHTSCALE = 66;//体重秤

    public final static int TYPE_HEART_RATE = 76;///心率带


    private String mac;
    private int profileType;
    private int deviceType;
    /* uint is second */
    private long connectedTime;
    private String name;
    private int rssi;
    private boolean isConnected;///whether connected
    ///function
    private boolean isNotDisturb;//
    private boolean isMsgCallCount;///
    private boolean isNapRemind;///
    private boolean isAlarmHealthRemind;///
    private boolean isWristMode;///
    private boolean isAutoSleep;///
    private boolean isMediaControl;//
    private boolean isHeartTiming;///
    private boolean isHeartAuto;//
    private boolean isFindDevice;//
    private boolean isAntiLost;///
    private boolean isSedentaryRem;////
    private boolean isAlarmRemind;///
    private boolean isScreenSet;////
    private boolean isDisplaySet;///
    private byte[] scanRecord;
    private ScanRecord mScanRecord;

    public BaseDevice() {

    }

    public BaseDevice(String name, String mac, int rssi){
        super();
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
    }

    public BaseDevice(String name, String mac, int rssi,byte[] scanRecord){
        super();
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public BaseDevice(String name, String mac, int rssi,byte[] scanRecord, ScanRecord record){
        super();
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        this.mScanRecord = record;
    }

    public BaseDevice(String name, String mac, int rssi,long connectedTime,int profileType,int deviceType) {
        super();
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
        this.profileType = profileType;
        this.deviceType = deviceType;
        this.connectedTime = connectedTime;
        initDeviceFunction();
    }

    public ScanRecord getmScanRecord() {
        return this.mScanRecord;
    }

    /**
     * init the function of device according devicetype
     */
    private void initDeviceFunction(){
        switch (deviceType){

        }
    }

    /**
     * init the type of device according devicetype
     * the default type is {#Ba}
     */
    private void initDeviceType(){
        if(name == null || name.trim().equals(""))
            return;
        if(name.startsWith("W311N"))
            deviceType = TYPE_W311N;
        else if(name.startsWith("W307S"))
            deviceType = TYPE_W307S;
        else if(name.startsWith("W307H"))
            deviceType = TYPE_W307H;
        else if(name.startsWith("W307N"))
            deviceType = TYPE_W307N;
        else if(name.startsWith("W301N"))
            deviceType = TYPE_W301N;
        else if(name.startsWith("W301S"))
            deviceType = TYPE_W301S;
        else if(name.startsWith("W301H"))
            deviceType = TYPE_W301H;
        else if(name.startsWith("W285S"))
            deviceType = TYPE_W285S;
    }

    /**
     * you can parser the scan record according to the standard method to get information that you need,
     * you can parse by {@link com.isport.isportlibrary.scanner.ScanRecord#parseFromBytes(byte[])}
     * @return return the scanrecord
     */
    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    public int getProfileType() {
        return this.profileType;
    }

    public void setProfileType(int profileType) {
        this.profileType = profileType;
    }


    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    /**
     *
     * @return Device name
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return Device Mac
     */
    public String getMac() {
        return this.mac;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    /**
     *
     * @return  Whether connected
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    public boolean isNotDisturb() {
        return this.isNotDisturb;
    }

    public void setNotDisturb(boolean notDisturb) {
        this.isNotDisturb = notDisturb;
    }

    public boolean isMsgCallCount() {
        return this.isMsgCallCount;
    }

    public void setMsgCallCount(boolean msgCallCount) {
        this.isMsgCallCount = msgCallCount;
    }

    public boolean isNapRemind() {
        return this.isNapRemind;
    }

    public void setNapRemind(boolean napRemind) {
        this.isNotDisturb = napRemind;
    }

    public boolean isAlarmHealthRemind() {
        return isAlarmHealthRemind;
    }

    public void setAlarmHealthRemind(boolean healthRemind) {
        this.isAlarmHealthRemind = healthRemind;
    }

    public boolean isWristMode() {
        return this.isWristMode;
    }

    public void setWristMode(boolean wristMode) {
        this.isWristMode = wristMode;
    }

    public boolean isAutoSleep() {
        return this.isAutoSleep;
    }

    public void setAutoSleep(boolean autoSleep) {
        this.isAutoSleep = autoSleep;
    }

    public boolean isMediaControl() {
        return this.isMediaControl;
    }

    public void setMediaControl(boolean mediaControl) {
        this.isMediaControl = mediaControl;
    }

    public boolean isHeartTiming() {
        return this.isHeartTiming;
    }

    public void setHeartTiming(boolean heartTiming) {
        this.isHeartTiming = heartTiming;
    }

    public boolean isHeartAuto() {
        return this.isHeartAuto;
    }

    public void setHeartAuto(boolean heartAuto) {
        this.isHeartAuto = heartAuto;
    }

    public boolean isFindDevice() {
        return this.isFindDevice;
    }

    public void setFindDevice(boolean findDevice) {
        this.isFindDevice = findDevice;
    }

    public boolean isAntiLost() {
        return this.isAntiLost;
    }

    public void setAntiLost(boolean antiLost) {
        this.isAntiLost = antiLost;
    }

    public boolean isSedentaryRem() {
        return this.isSedentaryRem;
    }

    public void setSedentaryRem(boolean sedentaryRem) {
        this.isSedentaryRem = sedentaryRem;
    }

    public boolean isAlarmRemind() {
        return this.isAlarmRemind;
    }

    public void setAlarmRemind(boolean alarmRemind) {
        this.isAlarmRemind = alarmRemind;
    }

    public boolean isScreenSet() {
        return this.isScreenSet;
    }

    public void setScreenSet(boolean screenSet) {
        this.isScreenSet = screenSet;
    }

    public boolean isDisplaySet() {
        return this.isDisplaySet;
    }

    public void setDisplaySet(boolean displaySet) {
        this.isDisplaySet = displaySet;
    }

    public long getConnectedTime() {
        return this.connectedTime;
    }

    public void setConnectedTime(long time) {
        this.connectedTime = time;
    }
}
