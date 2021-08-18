package com.isport.tracker.entity;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/10/31.
 */

public class HamaDeviceInfo {


    /*int photoMusic = info1 & 0x01;///照相拍照 默认1
    int keyLock = info1 & 0x02;///按键锁，默认为0
    int vibrate = info1 & 0x04;///震动，默认1
    int findPhone = info1 & 0x08;///是否开启查找手机功能，默认1
    int privacy = info1 & 0x80;//是否开启隐私保护，默认为0*/
    public static String LIDL_CONFIG = "lidl_config";
    public static String LIDL_KEY_LOCK = "lidl_key_lock";
    public static String LIDL_VIBRATE = "lidl_key_vibrate";
    public static String LIDL_FINDPHONE = "lidl_key_findphone";
    public static String LIDL_PRIVACY = "lidl_key_privacy";
    public static String LIDL_PHOTOMUSIC = "lidl_key_photomusic";
    public static String LIDL_FIREWARE_HIGH = "lidl_fireware_high";//版本高位
    public static String LIDL_FIREWARE_LOW = "lidl_fireware_low";//版本高位
    public static String LIDL_PHOTO = "lidl_key_photo";//照相
    public static String LIDL_MUSIC = "lidl_key_music";//音乐
    public static String LIDL_CONNECT_VIBTATE = "lidl_key_connect_vibrate";
    public static String LIDL_CALL_MSG = "lidl_key_call_msg";
    public static String LIDL_HEART_VIBRATE = "lidl_key_heart_vibrate";
    public static String LIDL_MENU = "lidl_key_menu";
    public static String LIDL_BLE_INTERFACE = "lidl_key_bleinterface";
    public static String LIDL_HIGH = "lidl_key_high";

    private static HamaDeviceInfo sInstance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static void init(Context context){
        if(sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(LIDL_CONFIG,Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

    public void putString(Context context,String key,String defValue){
        init(context);
        editor.putString(key,defValue);
    }

    public static void putInt(Context context,String key,int defValue){
        init(context);
        editor.putInt(key,defValue).commit();
    }

    public static int getInt(Context context,String key,int defValue){
        init(context);
        return sharedPreferences.getInt(key,defValue);
    }

}
