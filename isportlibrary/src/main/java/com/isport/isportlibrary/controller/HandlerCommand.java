package com.isport.isportlibrary.controller;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.tools.Constants;
import com.isport.isportlibrary.tools.Utils;

/**
 * @author Created by Marcos Cheng on 2016/9/2.
 */
class HandlerCommand {


    protected static boolean handleEndPhoneController(Context context, byte[] data) {
        //DE+08+02+FE+01+ED DE 08 02 FE 01 ED
        if (data == null || data.length < 6)
            return false;
        //  08  	02  FE  01	  ED

        if ((data[0] & 0xff) == 0x08 && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 &&
                (data[3] & 0xff) == 0xED) {
            Utils.sendEndCall(context, new KeyEvent(0, KeyEvent.KEYCODE_HEADSETHOOK));
            return true;
        }

        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x08 && (data[2] & 0xff) == 0x02 &&
                (data[3] & 0xff) == 0xFE && (data[4] & 0xff) == 0x01 && (data[5] & 0xff) == 0xed) {

            Utils.sendEndCall(context, new KeyEvent(0, KeyEvent.KEYCODE_HEADSETHOOK));
            return true;
        }
        return false;
    }

    /**
     * @param context
     * @param data
     */
    protected static boolean handleMusicController(Context context, byte[] data, OnDeviceSetting deviceSetting) {
        if (data == null || data.length < 6)
            return false;
        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x08 &&
                (data[3] & 0xff) == 0xfe && (data[4] & 0xff) == 0x01 && (data[5] & 0xff) == 0xed) { //stringBuilder.toString().trim().startsWith("DE 06 08 FE 01 ED")) {
            if (Constants.MUSIC_DEFAULT) {
                Utils.sendMusicKey(context, new KeyEvent(0, KeyEvent.KEYCODE_MEDIA_PLAY));
                Utils.sendMusicKey(context, new KeyEvent(1, KeyEvent.KEYCODE_MEDIA_PLAY));
            } else {
                if (deviceSetting != null) {
                    deviceSetting.onMusicControl(OnDeviceSetting.MEDIA_PLAY);
                }
            }
            return true;
        }
        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x08 &&
                (data[3] & 0xff) == 0xfe && (data[4] & 0xff) == 0x00 && (data[5] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 06 08 FE 00 ED")) {
            if (Constants.MUSIC_DEFAULT) {
                Utils.sendMusicKey(context, new KeyEvent(0, KeyEvent.KEYCODE_MEDIA_STOP));
                Utils.sendMusicKey(context, new KeyEvent(1, KeyEvent.KEYCODE_MEDIA_STOP));
            } else {
                if (deviceSetting != null) {
                    deviceSetting.onMusicControl(OnDeviceSetting.MEDIA_STOP);
                }
            }
            return true;
        }
        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x08 &&
                (data[3] & 0xff) == 0xfe && (data[4] & 0xff) == 0x02 && (data[5] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 06 08 FE 02 ED")) {
            if (Constants.MUSIC_DEFAULT) {
                Utils.sendMusicKey(context, new KeyEvent(0, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                Utils.sendMusicKey(context, new KeyEvent(1, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            } else {
                if (deviceSetting != null) {
                    deviceSetting.onMusicControl(OnDeviceSetting.MEDIA_PREVIOUS);
                }
            }
            return true;
        }
        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x08 &&
                (data[3] & 0xff) == 0xfe && (data[4] & 0xff) == 0x03 && (data[5] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 06 08 FE 03 ED")) {
            if (Constants.MUSIC_DEFAULT) {
                Utils.sendMusicKey(context, new KeyEvent(0, KeyEvent.KEYCODE_MEDIA_NEXT));
                Utils.sendMusicKey(context, new KeyEvent(1, KeyEvent.KEYCODE_MEDIA_NEXT));
            } else {
                if (deviceSetting != null) {
                    deviceSetting.onMusicControl(OnDeviceSetting.MEDIA_NEXT);
                }
            }
            return true;
        }
        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x08 &&
                (data[3] & 0xff) == 0xfe && (data[4] & 0xff) == 0xf0 && (data[5] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 06 08 FE F0 ED")) {
            if (Constants.MUSIC_DEFAULT) {
                Utils.sendMusicKey(context, new KeyEvent(0, KeyEvent.KEYCODE_MEDIA_PAUSE));
                Utils.sendMusicKey(context, new KeyEvent(1, KeyEvent.KEYCODE_MEDIA_PAUSE));
            } else {
                if (deviceSetting != null) {
                    deviceSetting.onMusicControl(OnDeviceSetting.MEDIA_PAUSE);
                }
            }
            return true;
        }
        return false;
    }

    protected static boolean handleFindPhone(byte[] data, OnDeviceSetting deviceSetting) {
        if (data == null || data.length < 4)
            return false;
        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x10 &&
                (data[3] & 0xff) == 0xed) {// if (!stringBuilder.toString().trim().startsWith("DE 06 10 ED")) {
            if (deviceSetting != null) {
                deviceSetting.findMobilePhone();
            }
            return true;
        }
        return false;
    }

    protected static boolean handleTakePhoto(byte[] data, OnDeviceSetting deviceSetting) {
        if (data == null || data.length < 6)
            return false;
        if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x07 &&
                (data[3] & 0xff) == 0xfe && (data[4] & 0xff) == 0x01 && (data[5] & 0xff) == 0xed) {//if(!stringBuilder.toString().startsWith("DE 06 07 FE 01 ED")){
            if (deviceSetting != null) {
                deviceSetting.takePhoto();
            }
            return true;
        }
        return false;
    }

}
