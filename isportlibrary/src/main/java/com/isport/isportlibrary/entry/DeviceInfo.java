package com.isport.isportlibrary.entry;

import java.io.Serializable;

/**
 * @author Created by Marcos Cheng on 2016/9/14.
 * Device config, all the information are read from ble device, can't be set
 */
public class DeviceInfo implements Serializable {
    private static final String TAG = DeviceInfo.class.getSimpleName();
    public static int INFO_INVAILID = -1;

    private String deviceModel;
    private byte hardwareVersion;
    /*
    for example,the current version is v88.86,
    so the firmwareHighVersion is 88,the firmwareLowVersion is 86
     */
    private int firmwareHighVersion;
    private int firmwareLowVersion;//
    private int powerLevel;

    ///info 1
    private int statePhoto;
    private int stateLock;
    private int stateVibrate;
    private int stateFindPhone;
    private int stateHigh;
    private int stateMusic;
    private int stateBleInterface;
    private int stateProtected;

    ///info2
    private int stateMenu;//有无二级返回界面 0 无  1有
    private int state5Vibrate;//心率5/10分钟是否震动提醒  0 不提醒 1 提醒
    private int stateCallMsg;//来电信息有无震动提醒  0 不提醒  1 提醒
    private int stateConnectVibrate;///连接时震动

    private int statePinCode;//设备重启后有无 6位PIN码
    private int calIconHeart;//卡路里Icon是心 还是火
    private int calCaculateMethod;//卡路里计算方法 bit6 bit7

    ///info 3
    private int stateSleepInterfaceAndFunc;//是否有睡眠功能

    ///info 4
    private int bleRealTimeBroad;///蓝牙实时广播  bit1 bit0
    private int stateleftRight;///左右手
    private int stateAntiLost;///防丢提醒

    private int stateCallRemind;///来电提醒
    private int stateMessageContent;///信息提醒开关和内容 bit6 bit5 00 10 11
    private int stateMessageIcon;//信息提醒界面 有无icon  bit7

    ///info5
    private int stateSyncTime;//是否已设置时间 0  1
    private int stateShowHook;//连接时是否显示对勾

    private static DeviceInfo sInstance;

    private DeviceInfo() {

    }

    public static DeviceInfo getInstance(){
        if (sInstance == null){
            synchronized (DeviceInfo.class) {
                if(sInstance == null) {
                    sInstance = new DeviceInfo();
                }
            }
        }
        return sInstance;
    }

    public int getStateSleepInterfaceAndFunc() {
        return stateSleepInterfaceAndFunc;
    }

    public void setStateSleepInterfaceAndFunc(int stateSleepInterfaceAndFunc) {
        this.stateSleepInterfaceAndFunc = stateSleepInterfaceAndFunc;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public byte getHardwareVersion() {
        return hardwareVersion;
    }

    public int getFirmwareHighVersion() {
        return firmwareHighVersion;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getStatePinCode() {
        return statePinCode;
    }

    public int getCalIconHeart() {
        return calIconHeart;
    }

    public int getCalCaculateMethod() {
        return calCaculateMethod;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setHardwareVersion(byte hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public void setFirmwareHighVersion(int firmwareVersion) {
        this.firmwareHighVersion = firmwareVersion;
    }

    public int getFirmwareLowVersion() {
        return firmwareLowVersion;
    }

    public void setFirmwareLowVersion(int firmwareLowVersion) {
        this.firmwareLowVersion = firmwareLowVersion;
    }

    public void setPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    public void resetDeviceInfo(){
        this.powerLevel = 0;
        this.deviceModel = "";
        this.firmwareHighVersion = 0;
        this.firmwareLowVersion = 0;
        this.hardwareVersion = 0;
    }

    /**
     * whether there is a function to take photo
     * @return
     */
    public int getStatePhoto() {
        return statePhoto;
    }

    /**
     * whether there is a function that is long touch to lock screen, if lock, will show lock icon on ble device
     * @return
     */
    public int getStateLock() {
        return stateLock;
    }

    public int getStateVibrate() {
        return stateVibrate;
    }

    public int getStateFindPhone() {
        return stateFindPhone;
    }

    public int getStateHigh() {
        return stateHigh;
    }

    public int getStateMusic() {
        return stateMusic;
    }

    public int getBleRealTimeBroad() {
        return bleRealTimeBroad;
    }

    public int getStateleftRight() {
        return stateleftRight;
    }

    public int getStateAntiLost() {
        return stateAntiLost;
    }

    public int getStateCallRemind() {
        return stateCallRemind;
    }

    public int getStateMessageContent() {
        return stateMessageContent;
    }

    public int getStateMessageIcon() {
        return stateMessageIcon;
    }

    public int getStateBleInterface() {
        return stateBleInterface;
    }

    public int getStateProtected() {
        return stateProtected;
    }

    public int getStateMenu() {
        return stateMenu;
    }

    public int getState5Vibrate() {
        return state5Vibrate;
    }

    public int getStateCallMsg() {
        return stateCallMsg;
    }

    public int getStateConnectVibrate() {
        return stateConnectVibrate;
    }

    public void setStatePhoto(int statePhoto) {
        this.statePhoto = statePhoto;
    }

    public void setStateLock(int stateLock) {
        this.stateLock = stateLock;
    }

    public void setStateVibrate(int stateVibrate) {
        this.stateVibrate = stateVibrate;
    }

    public void setStateFindPhone(int stateFindPhone) {
        this.stateFindPhone = stateFindPhone;
    }

    public void setStateHigh(int stateHigh) {
        this.stateHigh = stateHigh;
    }

    public void setStateMusic(int stateMusic) {
        this.stateMusic = stateMusic;
    }

    public void setStateBleInterface(int stateBleInterface) {
        this.stateBleInterface = stateBleInterface;
    }

    public void setStateProtected(int stateProtected) {
        this.stateProtected = stateProtected;
    }

    public void setStateMenu(int stateMenu) {
        this.stateMenu = stateMenu;
    }

    public void setState5Vibrate(int state5Vibrate) {
        this.state5Vibrate = state5Vibrate;
    }

    public void setStateCallMsg(int stateCallMsg) {
        this.stateCallMsg = stateCallMsg;
    }

    public void setStateConnectVibrate(int stateConnectVibrate) {
        this.stateConnectVibrate = stateConnectVibrate;
    }

    public void setStatePinCode(int statePinCode) {
        this.statePinCode = statePinCode;
    }

    public void setCalIconHeart(int calIconHeart) {
        this.calIconHeart = calIconHeart;
    }

    public void setCalCaculateMethod(int calCaculateMethod) {
        this.calCaculateMethod = calCaculateMethod;
    }

    public void setBleRealTimeBroad(int bleRealTimeBroad) {
        this.bleRealTimeBroad = bleRealTimeBroad;
    }

    public void setStateleftRight(int stateleftRight) {
        this.stateleftRight = stateleftRight;
    }

    public void setStateAntiLost(int stateAntiLost) {
        this.stateAntiLost = stateAntiLost;
    }

    public void setStateCallRemind(int stateCallRemind) {
        this.stateCallRemind = stateCallRemind;
    }

    public void setStateMessageContent(int stateMessageContent) {
        this.stateMessageContent = stateMessageContent;
    }

    public void setStateMessageIcon(int stateMessageIcon) {
        this.stateMessageIcon = stateMessageIcon;
    }

    public int getStateSyncTime() {
        return stateSyncTime;
    }


    public int getStateShowHook() {
        return stateShowHook;
    }

    public void setStateSyncTime(int stateSyncTime) {
        this.stateSyncTime = stateSyncTime;
    }


    public void setStateShowHook(int stateShowHook) {
        this.stateShowHook = stateShowHook;
    }

    public void paraserInfo(byte[] datas){
        if(datas != null){
            int info1 = datas[0] & 0xff;
            int info2 = datas[1] & 0xff;
            int info3 = datas[2] & 0xff;
            int info4 = datas[3] & 0xff;
            int info5 = datas[4] & 0xff;
            int info6 = datas[5] & 0xff;

            int photo = info1 & 1;
            int lock = (info1 >> 1) & 1 ;
            int vibrate = (info1 >>2) &1;
            int findphone = (info1 >>3) & 1;
            int high = (info1 >>4) & 1;
            int music = (info1 >>5) & 1;
            int bleinterface = (info1 >>6) & 1;
            int isprotected = (info1 >>7) & 1;

            int menu = (info2 >>0) & 1;
            int heart5Vibrate = (info2 >>1) & 1;;
            int callMsg = (info2 >>2) & 1;
            int connectVibrate = (info2 >>3) & 1;//心率存储提醒是否开启

            int statePinCode1 = (info2 >> 4) & 1;
            int calIconHeart1 = (info2 >>5 ) & 1;//卡路里Icon是心 还是火
            int calCaculateMethod1 = (info2 >> 6) & 0x03;//卡路里计算方法 bit6 bit7

            //info 3
            int sleepInfoFunc = (info3 >>0 ) & 1;

            ///info4
            int bleRealTimeBroad1 = info4 & 0x03;///蓝牙实时广播  bit1 bit0
            int stateleftRight1 = (info4 >> 2) & 0x01;///左右手
            int stateAntiLost1 = (info4 >>3) & 0x01;///防丢提醒

            int stateCallRemind1 = (info4 >> 4) & 0x01;///来电提醒
            int stateMessageContent1 = (info4 >> 5) & 0x03;///信息提醒开关和内容 bit6 bit5
            int stateMessageIcon1 = (info4 >> 7) & 0x01;//信息提醒界面 有无icon  bit7

            int stateSyncTime1 = (info5 >>0 ) & 0x01;

            int stateShowHook1 = (info5 >>2) & 0x01;

            float version = Float.valueOf(firmwareHighVersion+"."+firmwareLowVersion);
            setStatePhoto(photo);
            setStateVibrate(vibrate);
            setStateLock(lock);
            setStateFindPhone(findphone);
            setStateHigh(high);
            setStateMusic(music);
            setStateBleInterface(bleinterface);
            setStateProtected(isprotected);

            setStateMenu(menu);
            setState5Vibrate(heart5Vibrate);
            setStateCallMsg(callMsg);
            setStateConnectVibrate(connectVibrate);

            if(firmwareHighVersion>=89){
                setStatePinCode(statePinCode1);
                setCalIconHeart(calIconHeart1);
                setCalCaculateMethod(calCaculateMethod1);
                setBleRealTimeBroad(bleRealTimeBroad1);
                setStateleftRight(stateleftRight1);
                setStateAntiLost(stateAntiLost1);
                setStateCallRemind(stateCallRemind1);
                setStateMessageContent(stateMessageContent1);
                setStateMessageIcon(stateMessageIcon1);
                if(version >= 89.59){
                    connectVibrate = (info5 >>1) & 0x01;
                    setStateSleepInterfaceAndFunc(sleepInfoFunc);
                    setStateSyncTime(stateSyncTime1);
                    setStateConnectVibrate(connectVibrate);
                }
            }else {
                setStatePinCode(INFO_INVAILID);
                setCalIconHeart(INFO_INVAILID);
                setCalCaculateMethod(INFO_INVAILID);
                setBleRealTimeBroad(INFO_INVAILID);
                setStateleftRight(INFO_INVAILID);
                setStateAntiLost(INFO_INVAILID);
                setStateCallRemind(INFO_INVAILID);
                setStateMessageContent(INFO_INVAILID);
                setStateMessageIcon(INFO_INVAILID);
            }


        }
    }
}
