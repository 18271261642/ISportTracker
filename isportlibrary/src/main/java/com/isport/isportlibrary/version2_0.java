package com.isport.isportlibrary;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.isport.isportlibrary.entry.CallEntry;
import com.isport.isportlibrary.services.BleService;
import com.isport.isportlibrary.managers.IsportManager;

import java.util.List;

/**
 * @author Created by Marcos Cheng on 2017/9/21.
 */

public class version2_0 {

    /**
     * Attention:In order to use ble correctly on android M and up, you need access to Location service and enable location service in some device.
     * if you do not access to Location, you can't to use bluetooth adapter, and if you disable location service ,
     * you may be can not to connect to ble device
     *
     * This version is very different from the old, it fixed some bug, for example, after synced the data, it will to check the result,if error
     * {@link com.isport.isportlibrary.controller.IBleCmdCallback#syncState(int)}  will be called and it will terminate the sync process, if need,
     * you should sync again. If not check the result, it will lose the detail data. Beside that, {@link com.isport.isportlibrary.services.IsportApp}
     * was discarded, use {@link IsportManager} that function is same as IsportApp instead. The reason for the change
     * is to make it more expand, beacuse the java is a single inheritance language.
     * In addition to the above mentioned, there are other changes,it will list below:
     *
     * 1.   optimize the call and  notification push from phone to ble device. In the last version, the new message will cover the old even the old is pushing
     *      band that cause it may be confused. Now, if there are new notification of call,it will wait util the old one had been pushed to ble device. The size of
     *      the message list do no more than two that are old and newest, the message in the middle will be discard that do not push to ble device.
     * 2.   Give up to use {'no.nordicsemi.android.support.v18:scanner:1.0.0'} beacuse it can not scan any device on some device , for example SAMSUNG S7. So i use the
     *      scan api of Android SDK directly and make it has same behavior up Android4.3, so that you can scan device more simple. more information can see the package
     *      {@link com.isport.isportlibrary.scanner} and the demo of the sdk.
     *      you can set scanType to choose which way you want to scan,for detail to see {@link com.isport.isportlibrary.scanner.ScanManager#setScanType(int)}
     * 3.   {@link com.isport.isportlibrary.services.IsportApp} was discard, use {@link IsportManager} instead. if you want to observe
     *      the change of sms count and imcomming count, you need to instance {@link IsportManager} and call
     *      {@link IsportManager#init(Context)}, in addition, you need to call {@link IsportManager#registerObserver()} after you access
     *      to {@link android.Manifest.android.Manifest.permission#READ_CALL_LOG} and {@link android.Manifest.android.Manifest.permission#READ_WRITE_LOG}.
     *      In the last version, the {@link com.isport.isportlibrary.services.IsportApp} will limit you to do some extend beacuse of the single inheritance feature of Java,
     *
     * 4.   Add new profile that can read heart rate history data from band, that only support the device that type is {@link com.isport.isportlibrary.entry.BaseDevice#TYPE_AS97}
     *      and fireware version is up 89.79. After connected, call {@link com.isport.isportlibrary.controller.CmdController#queryHeartHistory(byte)} to query history heart record.
     *      {@link com.isport.isportlibrary.services.bleservice.OnDeviceSetting#onHeartSyncProgress(int)},
     *      {@link com.isport.isportlibrary.services.bleservice.OnDeviceSetting#onHeartHistorySynced(int)},
     *      {@link com.isport.isportlibrary.services.bleservice.OnDeviceSetting#onHeartHistorySynced(List)},
     *      {@link com.isport.isportlibrary.services.bleservice.OnDeviceSetting#onHeartHistoryCleared(boolean)},
     *      {@link com.isport.isportlibrary.services.bleservice.OnDeviceSetting#onHeartHistoryTotalCount(int)} will be called if to query history data.
     * 5.   Add some interface in {@link com.isport.isportlibrary.controller.BaseController} so that you can do many thing by yourself if you know the sevice uuid and
     *      characteristic uuid, for example, if you want to get Model Number String, you can call
     *      readCharacter(UUID.from("0000180A-0000-1000-8000-00805f9b34fb"), UUID.from("00002A24-0000-1000-8000-00805f9b34fb")),
     *      and it will callback {@link #onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)}.
     *      for more detail information to see {@link com.isport.isportlibrary.controller.BaseController} please.
     * 6.   {@link com.isport.isportlibrary.services.BleService} has some changes. When you start or bind service, you need call
     *      {@link BleService#initDb()} to init current device if you ever connected , if you did not to disconnect by manual or unbind, it will to reconnect the last device
     *      that you ever connected. {@link BleService#initDb()} you just need to call once.
     *      At before, it will be completed automatic when the BleService started, however, for Android O, there are many limitation on background task,
     *      so ,in order to make it possible to adapt Android O by yourself, make it change.
     * 7.   about {@link com.isport.isportlibrary.call.CallListener#sendCommingPhoneNumber(CallEntry)} and
     *      {@link com.isport.isportlibrary.call.CallListener#sendCommingPhoneNumber(int, String, String)}, just need to use one of them,
     *      suggest to use {@link com.isport.isportlibrary.call.CallListener#sendCommingPhoneNumber(CallEntry)}
     *      about {@link com.isport.isportlibrary.call.CallReceiver#sendCommingPhoneNumber(Context, CallEntry)} and
     *      {@link com.isport.isportlibrary.call.CallReceiver#sendCommingPhoneNumber(int, Context, String, String)}, just need to use one of them
     * 8.   Attention: {@link com.isport.isportlibrary.services.NotificationService} is a AccessibilityService, you need to start the service in Setting->Accessibility of phone,
     *      if the app is crash when AccessibilityService was started , may be you need restart phone
     * 9.   Change the {@link com.isport.isportlibrary.controller.BaseController#ACTION_REAL_DATA} to local,need to use LocalBroadcastManager to register
     * 10.  if want to see log of sdk, need to set {@link com.isport.isportlibrary.tools.Constants#IS_DEBUG} to true
     *
     *  If there are any questions, you can contact me by email jiamei6248@qq.com  or  Wechat chengjiatai
     */
}
