package com.isport.tracker.bluetooth;

import com.isport.isportlibrary.call.SMSBroadcastReceiver;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.entry.NotificationMsg;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.MyApp;
import com.isport.tracker.util.Constants;

import java.util.Map;

/**
 * Created by Administrator on 2016/11/25.
 */

public class HamaSmsListener extends SMSBroadcastReceiver {

    @Override
    public void sendNotiCmd(byte[] bs, int index, int type) {
        super.sendNotiCmd(bs, index, type);
        /*MainService mainService = MainService.getInstance(MyApp.getInstance());
        if(mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED){
            BaseDevice baseDevice = mainService.getCurrentDevice();
            if(baseDevice != null && baseDevice.getDeviceType() != BaseDevice.TYPE_W307N) {
                mainService.sendNotiCmd(bs, index, type);
            }
        }*/
    }

    @Override
    public void sendNotiCmd(NotificationMsg msg) {
        super.sendNotiCmd(msg);
        NotificationEntry entry = NotificationEntry.getInstance(MyApp.getInstance());
        if(entry.isOpenNoti() && entry.isAllowSMS()) {
            MainService mainService = MainService.getInstance(MyApp.getInstance());
            if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                BaseDevice baseDevice = mainService.getCurrentDevice();
                if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ACTIVA_T) &&( mainService.getCurrentDevice().getDeviceType() == BaseDevice.TYPE_W307S ||mainService.getCurrentDevice().getDeviceType() == BaseDevice.TYPE_W307S_SPACE)) {
                    return;
                }
                if (baseDevice != null && baseDevice.getDeviceType() != BaseDevice.TYPE_W307N) {
                    mainService.sendNotiCmd(msg);
                }
            }
        }
    }

    @Override
    public void sendNotiCmd(Map<Integer, byte[][]> map) {
        super.sendNotiCmd(map);
        NotificationEntry entry = NotificationEntry.getInstance(MyApp.getInstance());
        if(entry.isOpenNoti() && entry.isAllowSMS()) {
            MainService mainService = MainService.getInstance(MyApp.getInstance());
            if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                BaseDevice baseDevice = mainService.getCurrentDevice();
                if (baseDevice != null && baseDevice.getDeviceType() == BaseDevice.TYPE_W337B) {
                    mainService.sendNotiCmd(map);
                }
            }
        }
    }
}
