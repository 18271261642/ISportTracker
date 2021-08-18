package com.isport.isportlibrary.services.bleservice;

import android.view.KeyEvent;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BroadcastInfo;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.isportlibrary.entry.HeartRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by chengjiamei on 2016/8/25.
 * setting callback
 */
public abstract class OnDeviceSetting {

    public static int SYNC_HEART_STATE_SUCCESS = 1;
    public static int SYNC_HEART_STATE_FAIL = 0;
    public static int SYNC_HEART_NODATA = 2;

    /**
     * play media
     */
    public static int MEDIA_PLAY = KeyEvent.KEYCODE_MEDIA_PLAY;
    /**
     * stop media
     */
    public static int MEDIA_STOP = KeyEvent.KEYCODE_MEDIA_STOP;
    /**
     * previous
     */
    public static int MEDIA_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    /**
     * next
     */
    public static int MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT;
    /**
     * pause
     */
    public static int MEDIA_PAUSE = KeyEvent.KEYCODE_MEDIA_PAUSE;

    public void isReadySync(boolean isReady){};

    public void customeCmdResult(byte[] datas){};

    public void accessibleySetting(int state){};

    /**
     * 90.69.01版本心率提醒 true 是90.69.01版本  false  不是
     * @param state
     */
    public void isHeartRateDevice(Boolean state){};

    /**
     * it will be called when you read rssi of remote device, {@link BaseController#readRemoteRssi()}
     * @param rssi rssi of remote device
     */
    public void readRssiCompleted(int rssi){};

    /**
     * set alarm
     * @param success
     */
    public void alarmSetting(boolean success){};

    /**
     * set alarm description success
     * @param success
     */
    public void alarmDescripSetting(boolean success){};

    /**
     * set wrist mode
     * @param success
     */
    public void wristSetting(boolean success){};

    /**
     * setting weight / step distance / target step / birthday and so on
     * @param success
     */
    public void userInfoSetting(boolean success){};

    /**
     * set sedentary setting
     * @param success
     */
    public void  sedentarySetting(boolean success){};

    /**
     * set auto sleep
     * @param success
     */
    public void  autoSleepSetting(boolean success){};

    /**
     * set display
     * @param success
     */
    public void displaySetting(boolean success){};

    /**
     * modify broadcast name
     * @param success
     */
    public void bleBroadcastNameModify(boolean success){};

    /**
     *
     * @param success
     */
    public void mutilMediaSetting(boolean success){};

    /**
     * find device
     * @param success
     */
    public void findDeviceResult(boolean success){};

    /**
     * anti lost
     * @param success
     */
    public void antiLostSetting(boolean success){};

    /**
     * heart timing  test setting
     * @param success
     */
    public void heartTimingSetting(boolean success){};

    /**
     * delete data of device
     * @param success
     */
    public void deleteDataSetting(boolean success){};

    /**
     * find mobilephone
     * W311 series {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W311} if there are the function,start
     * find or stop find that controlled by remote device
     * when start to find mobile phone you can play ringtone or vibrate for a short time
     */
    public void findMobilePhone(){};

    /**
     * W337B series {@link com.isport.isportlibrary.controller.BaseController#CMD_TYPE_W337B}, start find or stop find that controlled by remote device
     * when start to find mobile phone you can play ringtone or vibrate for a short time
     * @param state start find or stop find
     */
    public void findMobilePhone(byte state){};

    /**
     * take photo controlled by ble device, if this callback, you need achieve custom camera
     */


    public void getReflex2cGetMessageSwitch(int value,int value2){};

    public void takePhoto(){};

    /**
     * open anti lost or close
     * @param state 1 open 0 close
     */
    public void antiLost(int state){};

    /**
     *      * Beat device, manually turn on the heart rate, 5 minutes after the automatic switch off
     */
    public void onSetHeartRateAutoDownSuccess(){};

    /**
     * {metricImp,is24Hour,activeTimezone,timeZone,year,month,
     * day,week,hour,minute,second}
     * @param result
     */
    public void deivceTimeZoneMetric24Hour(int[] result){};

    /**
     * {year,month,day,weight,targeStep,strideLen,targetSleepHour,targetSleepMin}
     * uint is metric
     * the real height = weight/100
     * the real strideLen = strideLen/100
     * @param result
     */
    public void currentUserInfo(int[] result){};

    public void currentDeviceInfo(DeviceInfo deviceInfo){};

    public void isSaveHeartNotify(int isSaveHeartNotify){};

    public void onBatteryChanged(int level){};

    /**
     * for w194 screen set
     * @param success
     */
    public void screenSetting(boolean success){};

    /**
     * add in 1.7 support heart history data that saved in ble device
     */
    /**
     * whether heart rate history was cleared
     * @param isCleared
     */
    public void onHeartHistoryCleared(boolean isCleared){};

    /**
     * heart histoty total count
     * @param count
     */
    public void onHeartHistoryTotalCount(int count){};

    /**
     *
     * @param state if state == 1 , synced successed or failed
     */
    public void onHeartHistorySynced(int state){};

    /**
     * RaiseHand Set
     */
    public void onRaiseHandSetSuccessed(){};

    /**
     * Hourly rate
     */
    public void onHourlyRateSetSuccessed(){};


    /**
     * Calibrate 316 523 525
     */
    public void onCalibrateSuccessed(){};

    /**
     *
     * @param state if state == 1 , synced successed or failed
     */
    public void onHeartRateHistorySynced(int state){};

    /**
     * synced successed with heartrate record
     * @param list heart record list
     */
    public void onHeartHistorySynced(List<HeartRecord> list){};

    /**
     * the progess of sync heart data
     * @param progress the progess of sync heart data
     */
    public void onHeartSyncProgress(int progress){};

    /**
     *  for W311 series , see {@link BaseController#CMD_TYPE_W311}
     * @param state media state, too see {@link #MEDIA_NEXT},{@link #MEDIA_PLAY}，{@link #MEDIA_PAUSE}，{@link #MEDIA_PLAY}，{@link #MEDIA_STOP}
     */
    public void onMusicControl(int state) {

    }

    /**
     * if unittype == {@link BroadcastInfo#UNIT_STLB},
     * Number of decimal places is always equalWith 1 , and the dotnumber is the number if decimal places when transfer to other unit type
     * @param date date
     * @param weight weight
     * @param dotnumber Number of decimal places
     * @param unittype unit type of weight
     * @param flag 电子秤标志，0x0002 体重秤(电阻为0)， 0x0306 体脂称（第一电极电阻），0x2306 体脂称（第一极电阻，第二极电阻）
     * @param fisrtR 第一极电阻
     * @param secondR 第二极电阻
     */
    public void onWeightChanged(Date date, float weight, int dotnumber, int unittype, int flag, float fisrtR, float secondR){}
}
