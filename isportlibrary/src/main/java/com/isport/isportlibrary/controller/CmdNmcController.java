package com.isport.isportlibrary.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.entry.AlarmEntry;
import com.isport.isportlibrary.entry.AutoSleep;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.CallEntry;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.isportlibrary.entry.DisplaySet;
import com.isport.isportlibrary.entry.HeartData;
import com.isport.isportlibrary.entry.HeartHisrotyRecord;
import com.isport.isportlibrary.entry.HeartTiming;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.entry.NotificationMsg;
import com.isport.isportlibrary.entry.SedentaryRemind;
import com.isport.isportlibrary.entry.SleepEntry;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.entry.WristMode;
import com.isport.isportlibrary.managers.NotiManager;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.services.bleservice.OnHeartListener;
import com.isport.isportlibrary.tools.BleConfig;
import com.isport.isportlibrary.tools.Constants;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.isportlibrary.tools.ParserData;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * Created by Marcos on 2018/1/22.
 */

public class CmdNmcController extends BaseController {

    private String TAG = CmdNmcController.class.getSimpleName();

    private BluetoothGattCharacteristic mRealTimeCharacteristic;
    private BluetoothGattCharacteristic mSendDataCharacteristic;
    private BluetoothGattCharacteristic mReviceDataCharacteristic;

    private static CmdNmcController sInstance;
    public OnDeviceSetting dsCallBack;
    private BluetoothGattService mGattService_HeartRate;
    private OnHeartListener onHeartListener;
    private UUID MAIN_SERVICE;
    private UUID SEND_DATA_CHAR;
    private UUID RECEIVE_DATA_CHAR;
    private UUID REALTIME_RECEIVE_DATA_CHAR;

    private static Handler handler;
    private Handler notiHandler;
    private int dayCount = 0;
    private int startYear;
    private int startMonth;
    private int startDay;
    private long startNoti = 0;

    private List<byte[]> mCache;
    private byte[] mLastCommand;
    private Object mLock = new Object();
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()
            * 2 + 1);
    private byte[] cmdFailToWrite;



    private CmdNmcController(Context context) {
        logBuilder = new StringBuilder();
        this.context = context;
        enableNotiHandler = new Handler(context.getMainLooper());
        if (notiHandler == null) {
            notiHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what) {
                        case 1:
                            enableNotification(MAIN_SERVICE, SEND_DATA_CHAR,
                                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            break;
                        case 2:
                            enableNotification(MAIN_SERVICE, RECEIVE_DATA_CHAR,
                                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            break;
                        case 3:
                            enableNotification(MAIN_SERVICE, REALTIME_RECEIVE_DATA_CHAR,
                                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            break;
                        case 4:
                            enableNotification(HEARTRATE_SERVICE_UUID, HEARTRATE_SERVICE_CHARACTER,
                                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            break;
                    }
                }
            };
        }

        if (commandHandler == null) {
            commandHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 0x10:
                            if (state == BaseController.STATE_CONNECTED) {
                                if (mLastCommand != null) {
                                }
                                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, mLastCommand);
                            }
                            break;
                        case 0x11:
                            if (syncState == STATE_SYNC_COMPLETED && isFisrtTimeSync) {
                                isFisrtTimeSync = false;
                                startSync();
                            }
                            break;
                        case 0x13:
                            if (callEntryList != null && callEntryList.size() > 0) {
                                CallEntry callEntry = callEntryList.get(0);
                                sendPhoneName(callEntry.getName());
                                callCurrentIndex = 1;
                            }
                            break;
                        case 0x14:
                            syncTime();
                            break;
                        case 0x16: {
                            int[] inf = (int[]) msg.obj;
                            sendSyncDay(inf[0], inf[1], inf[2]);
                            break;
                        }
                        case 0x17:
                            sendPhoneNum();
                            break;
                    }
                }
            };
        }
        if (syncHandler == null) {
            syncHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 0x01:
                            if (callback != null) {
                                syncState = STATE_SYNC_COMPLETED;
                                callback.syncState(syncState);
                            }
                            break;
                    }
                }
            };
        }

        if (deviceInfoHandler == null) {
            deviceInfoHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0x01) {
                        sendDeviceInfo();
                        if (state != BaseController.STATE_CONNECTED) {
                            deviceInfoHandler.sendEmptyMessageDelayed(0x01, 6000);
                        }
                    } else if (msg.what == 0x02) {
                        if (!hasSyncBaseTime)
                            sendBaseTime();
                    } else if (msg.what == 0x03) {////���ҷ���
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.discoverServices();
                        }
                    }
                }
            };
        }

        if (writeHandler == null) {
            writeHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmdFailToWrite);
                }
            };
        }
    }

    /**
     * Set indicate
     *
     * @param characteristic
     * @return
     */
    private boolean internalEnableIndications(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;
        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, true);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    /**
     * Set notification
     *
     * @param characteristic
     * @return
     */
    private boolean internalEnableNotifications(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, true);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    public static CmdNmcController getInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (CmdNmcController.class) {
                if (sInstance == null) {
                    sInstance = new CmdNmcController(ctx.getApplicationContext());
                    if (handler == null) {
                        handler = new Handler(ctx.getApplicationContext().getMainLooper());
                    }

                }
            }
        }
        return sInstance;
    }

    /**
     * get bluetooth device with address
     *
     * @param address
     * @return
     */
    public BluetoothDevice getDeviceWithAdress(String address) {
        return address == null ? null : BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    }

    public void unbindDevice(String mac) {
        removeString(KEY_LAST_SYNC_TIME + mac);
    }

    public void setOnHeartListener(OnHeartListener listener) {
        this.onHeartListener = listener;
    }

    public void setDeviceSetting(OnDeviceSetting dsCallBack) {
        this.dsCallBack = dsCallBack;
    }

    /**
     * find ble device, if phone is connecting with ble device,it will vibrate
     */
    public void findDevice() {
        if (state == STATE_CONNECTED) {
            byte[] value = new byte[4];
            value[0] = (byte) 0xBE;
            value[1] = (byte) 0x06;
            value[2] = (byte) 0x0F;
            value[3] = (byte) 0xED;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, value);
        }
    }

    private void enableNotification(UUID service, UUID charac, byte[] value) {
        if (mBluetoothGatt != null) {
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(service);
            enableNotification(mBluetoothGattService, charac, value);
        }
    }


    private boolean enableNotification(BluetoothGattService mBluetoothGattService, final UUID uuid, final byte[]
            value) {
        if (mBluetoothGattService != null && tempConnectedState == BaseController.STATE_CONNECTED) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(uuid);
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    boolean result = internalEnableNotifications(mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController
                            .STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableNotiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 100);
                        }
                    }
                } else if (value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
                    boolean result = internalEnableIndications(mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController
                            .STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableNotiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 100);
                        }
                    }
                }
            }
        }
        return false;
    }

    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.YEAR, startYear);
        calendar.set(Calendar.MONTH, startMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, startDay);
        dayCount++;
        calendar.add(Calendar.DAY_OF_MONTH, dayCount);
        return calendar;
    }

    private Calendar getCurCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    //handle the sceen that time our get history data
    private Timer dataTimer;
    private TimerTask dataTask;

    private void initDataTimer() {
        dataTimer = new Timer();
        dataTask = new TimerTask() {
            @Override
            public void run() {
                if (syncState == STATE_SYNCING) {
                    if (callback != null) {
                        callback.syncState(STATE_SYNC_TIMEOUT);
                    }
                }
            }
        };
        dataTimer.schedule(dataTask, 10000);
    }

    private void cancelDataTimer() {
        if (dataTimer != null) {
            dataTimer.cancel();
        }
        if (dataTask != null) {
            dataTask.cancel();
        }
        dataTask = null;
        dataTimer = null;
    }

    private void syncFinishOrError(Calendar calendar, int syncs) {
        if (syncs == 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        dayCount = 0;
        /* save last sync time */
        StringBuilder builderTp = new StringBuilder(String.format("%04d", calendar.get(Calendar.YEAR))).append("-")
                .append(String.format("%02d", calendar.get(Calendar.MONTH) + 1)).append("-")
                .append(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));

        putString(KEY_LAST_SYNC_TIME + currentMac, builderTp.toString());
        syncState = STATE_SYNC_COMPLETED;
        if (callback != null) {
            callback.syncState(syncs == 1 ? syncState : STATE_SYNC_ERROR);
        }
    }

    private void handleCharacterisicChanged(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        if (characteristic.getUuid().equals(SEND_DATA_CHAR)) {
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder();
                if (IS_DEBUG) {
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02X ", byteChar));
                    }
                    if (logBuilder == null)
                        logBuilder = new StringBuilder();
                    logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " FF01 R " + stringBuilder
                            .toString()).append("\r\n");
                    Log.e(TAG, "��һͨ��  ReceiverCmd" + stringBuilder.toString().trim());
                }
                if (data.length >= 4) {
                    if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x0B && (data[3]
                            & 0xff) == 0xed) {//"DE 01 0B ED".equals(stringBuilder.toString().trim())) {// set wrist mode
                        if (dsCallBack != null) {
                            dsCallBack.wristSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x16 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 16 ED".equals(stringBuilder.toString().trim())) {//set alarm
                        // description
                        if (dsCallBack != null) {
                            dsCallBack.alarmDescripSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x04 &&
                            (data[3] & 0xff) == 0xed) {//"DE 02 04 ED".equals(stringBuilder.toString().trim())) { // �ر�ʵʱͬ���ɹ�

                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x0C &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 0C ED".equals(stringBuilder.toString().trim())) {// set
                        // sedentary
                        if (dsCallBack != null) {
                            dsCallBack.sedentarySetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x0c &&
                            (data[3] & 0xff) == 0xfe) {//"DE 01 0C FE".equals(stringBuilder.toString().trim())) {
                        parserSedentaryInfo(data);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x03 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 03 ED".equals(stringBuilder.toString().trim())) {
                        if (dsCallBack != null) {
                            dsCallBack.userInfoSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x03 &&
                            (data[3] & 0xff) == 0xfb) {//"DE 01 03 FB".equals(stringBuilder.toString().trim())) {
                        if (dsCallBack != null) {
                            DeviceInfo deviceInfo = DeviceInfo.getInstance();
                            int dtype = baseDevice.getDeviceType();
                            int year = ParserData.byteArrayToInt(new byte[]{data[4], data[5]});
                            int month = ParserData.byteToInt(data[6]);
                            int day = ParserData.byteToInt(data[7]);
                            int weight = ParserData.byteArrayToInt(new byte[]{data[8], data[9]});
                            int targeStep = ParserData.byteArrayToInt(new byte[]{data[10], data[11], data[12]});
                            int strideLen = ParserData.byteArrayToInt(new byte[]{data[13], data[14]});
                            int targetSleepHour = ParserData.byteToInt(data[15]);
                            int targetSleepMin = ParserData.byteToInt(data[16]);
                            dsCallBack.currentUserInfo(new int[]{year, month, day, weight, targeStep, strideLen,
                                    targetSleepHour, targetSleepMin});

                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x07 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 07 ED".equals(stringBuilder.toString().trim())) {// set auto
                        // sleep
                        if (dsCallBack != null) {
                            dsCallBack.autoSleepSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x07 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().contains("DE 01 07 FE")) {
                        parserSleepInfo(data);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x15 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 15 ED".equals(stringBuilder.toString().trim())) { // set auto
                        // heart test
                        if (dsCallBack != null) {
                            dsCallBack.heartTimingSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x15 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().startsWith("DE 01 15 FE")) {
                        parserTimingHeartDetect(data);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x08 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 08 ED".equals(stringBuilder.toString().trim())) {
                        if (dsCallBack != null) {
                            dsCallBack.displaySetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x08 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().contains("DE 01 08 FE")) {
                        parserDisplay(data);
                    }
                    handleNotiResponse(context, data);
                    if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x0f && (data[3]
                            & 0xff) == 0xed) {//"DE 06 0F ED".equals(stringBuilder.toString().trim())) {

                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x11 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 11 ED".equals(stringBuilder.toString().trim())) {
                        if (dsCallBack != null) {
                            dsCallBack.bleBroadcastNameModify(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x0D &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 0D ED".equals(stringBuilder.toString().trim())) {// anti lost
                        // open
                        if (dsCallBack != null) {
                            dsCallBack.antiLost(1);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x0E &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 0E ED".equals(stringBuilder.toString().trim())) {// anti lost
                        // close
                        if (dsCallBack != null) {
                            dsCallBack.antiLost(0);
                        }
                    } else if (data.length >= 6 && ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2]
                            & 0xff) == 0x07 && (data[3] & 0xfe) == 0xfe &&
                            (data[4] & 0xff) == 0x01 && (data[5] & 0xff) == 0xed)) {//"DE 02 07 FE 01 ED".equals
                        // (stringBuilder.toString().trim())) {
                        if (dsCallBack != null) {
                            dsCallBack.accessibleySetting(1);
                        }
                    } else if (data.length >= 6 && ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2]
                            & 0xff) == 0x07 && (data[3] & 0xfe) == 0xfe &&
                            (data[4] & 0xff) == 0x00 && (data[5] & 0xff) == 0xed)) {//"DE 02 07 FE 00 ED".equals
                        // (stringBuilder.toString().trim())) {
                        if (dsCallBack != null) {
                            dsCallBack.accessibleySetting(0);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x01 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 01 ED".equals(stringBuilder.toString().trim())) {// set
                        // 24-hour metricImperial succes
                        deviceInfoHandler.removeMessages(0x02);
                        commandHandler.sendEmptyMessageDelayed(0x11, 150);
                        hasSyncBaseTime = true;
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x02 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 02 ED".equals(stringBuilder.toString().trim())) {

                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x31 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 31 ED".equalsIgnoreCase(stringBuilder.toString().trim())) {
                        commandHandler.sendEmptyMessageDelayed(0x11, 150);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 09 ED".equals(stringBuilder.toString().trim())) {// set alarm
                        if (dsCallBack != null) {
                            dsCallBack.alarmSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().equals("DE 01 09 FE")) {
                        parserAlarmInfo(data);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x02 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 02 ED".equals(stringBuilder.toString().trim())) {
                        commandHandler.sendEmptyMessageDelayed(0x13, 300);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x01 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 01 ED".equals(stringBuilder.toString().trim())) {
                        callCurrentIndex = 0;
                        if (callEntryList != null && callEntryList.size() > 0) {
                            callEntryList.remove(0);
                        }
                        //������һ��
                        commandHandler.sendEmptyMessageDelayed(0x17, 300);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x05 &&
                            (data[3] & 0xff) == 0xfb) {//stringBuilder.toString().trim().startsWith("DE 02 05 FB")) {// get
                        // history date(start - end)�������ʼʱ����2015-01-01
                        if (syncHandler.hasMessages(0x01))
                            syncHandler.removeMessages(0x01);
                        byte[] starts = new byte[4];
                        System.arraycopy(data, 4, starts, 0, 4);
                        byte[] ends = new byte[4];
                        System.arraycopy(data, 8, ends, 0, 4);

                        mCache = null;
                        if (starts[0] == 0 && starts[1] == 0) {// no history data
                            syncState = STATE_SYNC_COMPLETED;
                            mCache = null;
                            if (callback != null) {
                                callback.syncState(syncState);
                            }
                        } else {
                            startYear = starts[0] * 256 + (starts[1] & 0x00ff);
                            startMonth = starts[2];//1-12
                            startDay = starts[3];
                            Calendar sc = Calendar.getInstance();
                            long stime = sc.getTimeInMillis() / 1000;//��ǰʱ��
                            sc.set(startYear, startMonth - 1, startDay);
                            long sttime = sc.getTimeInMillis() / 1000;
                            if (stime - sttime > 3600 * 24 * 16) {
                                sc = Calendar.getInstance();
                                sc.add(Calendar.DAY_OF_MONTH, -15);
                                startYear = sc.get(Calendar.YEAR);
                                startMonth = sc.get(Calendar.MONTH) + 1;
                                startDay = sc.get(Calendar.DAY_OF_MONTH);
                            }
                            if (stime - sttime <= 0) {
                                sc = Calendar.getInstance();
                                startYear = sc.get(Calendar.YEAR);
                                startMonth = sc.get(Calendar.MONTH) + 1;
                                startDay = sc.get(Calendar.DAY_OF_MONTH);
                            }

                            if (startYear == 2015 && startMonth == 1 && startDay == 1) {
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DAY_OF_MONTH, -15);
                                startYear = cal.get(Calendar.YEAR);
                                startMonth = cal.get(Calendar.MONTH) + 1;
                                startDay = cal.get(Calendar.DAY_OF_MONTH);
                            }
                            int endYear = ends[0] * 256 + (ends[1] & 0x00ff);
                            int endMonth = ends[2];
                            int endDay = ends[3];

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            calendar.set(Calendar.MINUTE, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;//0 - 11
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            long currentTime = calendar.getTimeInMillis();

                            String lastSyncTime = null;// getString(KEY_LAST_SYNC_TIME + currentMac, null);///the
                            // time of last sync
                            Log.e(TAG, "lastSyncTime = " + lastSyncTime);
                            Calendar lastCalendar = Calendar.getInstance();
                            lastCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            lastCalendar.set(Calendar.MINUTE, 0);
                            lastCalendar.set(Calendar.SECOND, 0);
                            lastCalendar.set(Calendar.MILLISECOND, 0);
                            long startTime = 0;
                            if (lastSyncTime != null) {
                                String[] strs = lastSyncTime.split("-");
                                int ty = Integer.valueOf(strs[0]);
                                int tm = Integer.valueOf(strs[1]);
                                int td = Integer.valueOf(strs[2]);
                                lastCalendar.set(Calendar.YEAR, ty);
                                lastCalendar.set(Calendar.MONTH, tm - 1);
                                lastCalendar.set(Calendar.DAY_OF_MONTH, td);
                                if (lastCalendar.after(calendar)) {
                                    startYear = year;
                                    startMonth = month;
                                    startDay = day;
                                } else {
                                    startYear = ty;
                                    startMonth = tm;
                                    startDay = td;
                                }
                            }
                            calendar.set(Calendar.YEAR, startYear);
                            calendar.set(Calendar.MONTH, startMonth - 1);
                            calendar.set(Calendar.DAY_OF_MONTH, startDay);
                            startTime = calendar.getTimeInMillis();
                            dayCount = 0;
                            int[] tpi = new int[]{startYear, startMonth, startDay};
                            Message msgTp = Message.obtain();
                            msgTp.obj = tpi;
                            msgTp.what = 0x16;
                            commandHandler.sendMessageDelayed(msgTp, 150);
                            //sendSyncDay(startYear, startMonth, startDay);
                        }

                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xfb) {//stringBuilder.toString().trim().startsWith("DE 06 09 FB")) {// �յ��豸��Ϣ
                        if (deviceInfoHandler.hasMessages(0x01))
                            deviceInfoHandler.removeMessages(0x01);
                        DeviceInfo deviceInfo = DeviceInfo.getInstance();
                        byte[] model = new byte[6];
                        System.arraycopy(data, 4, model, 0, 6);
                        String str = new String(model);
                        deviceInfo.setDeviceModel(str);
                        deviceInfo.setHardwareVersion(data[10]);
                        deviceInfo.setFirmwareHighVersion(data[11] & 0xff);
                        deviceInfo.setFirmwareLowVersion(data[12] & 0xff);
                        deviceInfo.setPowerLevel(data[17] & 0xff);

                        if (dsCallBack != null) {
                            dsCallBack.currentDeviceInfo(deviceInfo);
                            dsCallBack.onBatteryChanged(deviceInfo.getPowerLevel());
                        }
                        ////06 09ָ��Ļظ���־���Ƿ����ӳɹ���
                        /// �յ�ָ����ȥͬ��ʱ�䣬�������ֻ��Ҫִ��һ�Σ�������Ҫ�Ӹ���־�����������Ϊ����ԭ��
                        ////������06 09ָ��յ��ظ���Ͳ���ִ�к���������
                        if (!hasSyncBaseTime) {
                            connectSuccess(gatt);
                            setHeartDescription();
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x03 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 03 09 ED")) {
                        if (dsCallBack != null) {
                            dsCallBack.accessibleySetting(0);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x01 &&
                            (data[3] & 0xff) == 0xfb) {//stringBuilder.toString().trim().startsWith("DE 01 01 FB")) {
                        if (dsCallBack != null) {
                            int metricImp = data[4];
                            int is24Hour = data[5];
                            int activeTimezone = (((data[6] & 0x80) == 0x80) ? -1 * (data[6] & 0x40) / 2 : (data[6] &
                                    0x40));
                            int timeZone = (((data[7] & 0x80) == 0x80) ? -1 * (data[7] & 0x40) / 2 : (data[7] & 0x40));
                            int year = ParserData.byteArrayToInt(new byte[]{data[8], data[9]});
                            int month = ParserData.byteToInt(data[10]);
                            int day = ParserData.byteToInt(data[11]);
                            int week = ParserData.byteToInt(data[12]);
                            int hour = ParserData.byteToInt(data[13]);
                            int minute = ParserData.byteToInt(data[14]);
                            int second = ParserData.byteToInt(data[15]);
                            dsCallBack.deivceTimeZoneMetric24Hour(new int[]{metricImp, is24Hour, activeTimezone,
                                    timeZone, year, month,
                                    day, week, hour, minute, second});
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x03 &&
                            (data[3] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 06 03 ED")) {

                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x04 &&
                            (data[3] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 06 04 ED")) {

                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x05 &&
                            (data[3] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("DE 01 05 ED"))
                        // {///��˽����������Ϣ
                        cancelLidlTimer();
                        setHeartDescription();
                        connectSuccess(gatt);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 &&
                            (data[3] & 0xff) == 0x1f) {//stringBuilder.toString().trim().startsWith("DE 02 01 1F")) {
                        commandHandler.sendEmptyMessageDelayed(0x10, 300);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x08 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().startsWith("DE 02 08 FE")) {
                        int totalCount = byte2Int(data[4]) * 256 + byte2Int(data[5]);
                        if (dsCallBack != null) {
                            dsCallBack.onHeartHistoryTotalCount(totalCount);
                            if (totalCount == 0) {
                                dsCallBack.onHeartHistorySynced(OnDeviceSetting.SYNC_HEART_STATE_SUCCESS);
                            }
                        }

                        if (heartHisrotyRecord == null) {
                            heartHisrotyRecord = new HeartHisrotyRecord();
                        }
                        heartHisrotyRecord.setTotalCount(totalCount);

                    } else if ((data[0] & 0xff) == 0xbe && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xed) {//stringBuilder.toString().trim().startsWith("BE 02 09 ED")) {
                        if (dsCallBack != null) {
                            dsCallBack.onHeartHistoryCleared(data[4] == (byte) 0x01 ? true : false);
                        }
                    }
                }
                if (dsCallBack != null) {
                    dsCallBack.customeCmdResult(data);
                }
            }
        } else if (characteristic.getUuid().equals(RECEIVE_DATA_CHAR)) {
            if (IS_DEBUG) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.e(TAG, "�ڶ�ͨ��  ReceiverCmd " + stringBuilder.toString());
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " FF02 R = " + stringBuilder
                        .toString() + "\r\n");
            }
            if (data.length >= 8 && (data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01
                    && (data[3] & 0xff) == 0xed) {//("DE 02 01 ED".equals(stringBuilder.toString().trim()))) {
                if (syncHandler.hasMessages(0x01))
                    syncHandler.removeMessages(0x01);
                cancelDataTimer();
                Calendar calendar = getCalendar();
                Calendar curCalendar = getCurCalendar();
                int y = calendar.get(Calendar.YEAR);
                int m = calendar.get(Calendar.MONTH);
                int d = calendar.get(Calendar.DAY_OF_MONTH);

                boolean isError = false;
                if (mCache != null) {
                    if (mCache.size() < 4) {//sync error
                        syncFinishOrError(curCalendar, 0);
                        isError = true;
                    } else {
                        byte[] bst = mCache.get(2);
                        int a1 = (bst[9] & 0xff) * 256;
                        int a2 = (bst[10] & 0xff);
                        int len = ((bst[9] & 0xff) * 256) + (bst[10] & 0xff);
                        if (!(len <= ((mCache.size() - 3) * 20))) {//sync error
                            syncFinishOrError(curCalendar, 0);
                            isError = true;
                        } else {
                            final List<byte[]> listCacheTp = new ArrayList<>();
                            listCacheTp.addAll(mCache);
                            if (gatt != null) {
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        ParserData.processW311TData(context, gatt.getDevice().getAddress(), callback,
                                                listCacheTp);
                                    }
                                });
                            }

                        }
                    }

                }
                if (IS_DEBUG) {
                    Log.e(TAG, String.format("%04d", y) + "-" +
                            String.format("%02d", m + 1) + "-" + String.format("%02d", d));
                }
                mCache = null;
                if (!isError) {
                    if (calendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
                        initDataTimer();
                        int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
                                .get(Calendar.DAY_OF_MONTH)};
                        Message msgTp = Message.obtain();
                        msgTp.obj = tpi;
                        msgTp.what = 0x16;
                        commandHandler.sendMessageDelayed(msgTp, 150);
                        //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get
                        // (Calendar.DAY_OF_MONTH));
                    } else {
                        syncFinishOrError(curCalendar, 1);
                    }
                }
            } else if (data.length >= 8 && (data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff)
                    == 0x01 && (data[3] & 0xff) == 0x06) {//"DE 02 01 06".equals(stringBuilder.toString().trim())) {
                if (syncHandler.hasMessages(0x01))
                    syncHandler.removeMessages(0x01);
                cancelDataTimer();
                Calendar calendar = getCalendar();
                Calendar curCalendar = getCurCalendar();
                int y = calendar.get(Calendar.YEAR);
                int m = calendar.get(Calendar.MONTH);
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                if (IS_DEBUG) {
                    Log.e(TAG, String.format("%04d", y) + "-" +
                            String.format("%02d", m + 1) + "-" + String.format("%02d", d));
                }
                boolean isError = false;
                if (mCache != null) {
                    if (mCache.size() < 4) {//ͬ������
                        syncFinishOrError(curCalendar, 0);
                        isError = true;
                    } else {
                        byte[] bst = mCache.get(2);
                        int a1 = (bst[9] & 0xff) * 256;
                        int a2 = (bst[10] & 0xff);
                        int len = ((bst[9] & 0xff) * 256) + ((bst[10] & 0xff));
                        if (!(len <= ((mCache.size() - 3) * 20))) {//ͬ������
                            syncFinishOrError(curCalendar, 0);
                            isError = true;
                        } else {
                            final List<byte[]> listCacheTp = new ArrayList<>();
                            listCacheTp.addAll(mCache);
                            if (gatt != null) {
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        ParserData.processW311TData(context, gatt.getDevice().getAddress(), callback,
                                                listCacheTp);
                                    }
                                });
                            }
                            if (IS_DEBUG) {
                                Log.e(TAG, String.format("%04d", y) + "-" +
                                        String.format("%02d", m) + "-" + String.format("%02d", d));
                            }
                        }
                    }

                }

                mCache = null;
                if (!isError) {
                    if (calendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
                        initDataTimer();
                        int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
                                .get(Calendar.DAY_OF_MONTH)};
                        Message msgTp = Message.obtain();
                        msgTp.obj = tpi;
                        msgTp.what = 0x16;
                        commandHandler.sendMessageDelayed(msgTp, 150);
                        //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get
                        // (Calendar.DAY_OF_MONTH));
                    } else {
                        syncFinishOrError(curCalendar, 1);
                    }
                }
            } else {

                if (mCache == null) {
                    mCache = Collections.synchronizedList(new ArrayList<byte[]>());
                }
                if (data != null) {
                    mCache.add(data);
                }
            }
        } else if (characteristic.getUuid().equals(REALTIME_RECEIVE_DATA_CHAR)) {
            if (IS_DEBUG) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.e("REALTIME_RECEIVE_DATA_", "����ͨ��  ReceiverCmd " + stringBuilder.toString());
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " FF04 R = " + stringBuilder
                        .toString() + "\r\n");
            }
            if (HandlerCommand.handleTakePhoto(data, dsCallBack))
                return;

            if (HandlerCommand.handleFindPhone(data, dsCallBack)) {//find mobile phone
                byte[] fff = new byte[]{(byte) 0xbe, 0x06, 0x10, (byte) 0xed};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, fff);
                return;
            }
            if (HandlerCommand.handleMusicController(context, data, dsCallBack))////Music controll
                return;

            if (data != null && data.length >= 4 && ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 &&
                    (data[2] & 0xff) == 0x01 && (data[3] & 0xff) == 0xfe)) {//stringBuilder.toString().trim()
                // .startsWith("DE 02 01 FE")) {////real time data
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        ParserData.processRealTimeData(context, gatt.getDevice().getAddress(), callback, data);
                    }
                });

                if (dsCallBack != null) {
                    dsCallBack.customeCmdResult(data);
                }
            } else {
                DeviceInfo deviceInfo = DeviceInfo.getInstance();
                deviceInfo.setPowerLevel(data[data.length - 2] & 0xff);
                if (dsCallBack != null) {
                    dsCallBack.customeCmdResult(data);
                }
            }
        }
    }


    private byte[] calCheckSum(byte[] data, int dn, HeartHisrotyRecord record) {
        if (data == null)
            return null;
        byte[] bs = new byte[data.length - dn];
        long checknum = 0;
        for (int i = dn; i < data.length; i++) {
            bs[i - dn] = data[i];
            checknum += (data[i] & 0xff);
        }
        record.setCheckSum(record.getCheckSum() + checknum);
        return bs;
    }


    private Timer lidlTimeOut;
    private TimerTask lidlTimeOutTask;

    private void initLidlTimer() {
        cancelLidlTimer();
        lidlTimeOut = new Timer();
        lidlTimeOutTask = new TimerTask() {
            @Override
            public void run() {
                if (tempConnectedState == BaseController.STATE_CONNECTED) {
                    sendPrivacy();
                }
            }
        };
        lidlTimeOut.schedule(lidlTimeOutTask, 2500);///
    }

    private void cancelLidlTimer() {
        if (lidlTimeOutTask != null) {
            lidlTimeOutTask.cancel();
            lidlTimeOutTask = null;
        }
        if (lidlTimeOut != null) {
            lidlTimeOut.cancel();
            lidlTimeOut = null;
        }
    }

    /**
     * read battery for ble device
     */
    @Override
    public void readBattery() {
        if (state == STATE_CONNECTED) {
            sendDeviceInfo();
        }
    }

    private void sendPrivacy() {
        ///����������ַ
        BluetoothAdapter.getDefaultAdapter().getAddress();
                        /*BE+01+05+FE+���ÿ��أ�1byte��+4λ����루2byte��
                        +�ֻ���MAC��6byte��*/
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            cancelLidlTimer();
            return;
        }
        if (baseDevice == null) {
            cancelLidlTimer();
            return;
        }
        byte[] bs = new byte[13];
        bs[0] = (byte) 0xbe;
        bs[1] = 0x01;
        bs[2] = 0x05;
        bs[3] = (byte) 0xfe;
        bs[4] = (byte) 0xff;
        bs[5] = 0x00;
        bs[6] = 0x00;
        String maccccc = getBtAddressViaReflection();
        if (maccccc == null) {
            maccccc = baseDevice.getMac();
        }
        String[] macs = maccccc.split(":");
        for (int i = 0; i < macs.length; i++) {
            bs[7 + i] = (byte) Integer.parseInt(macs[i], 16);
        }

        if (IS_DEBUG) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bs.length; i++) {
                builder.append(String.format("%02X ", bs[i]));
            }
            Log.e("MainService", builder.toString());
        }
        sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, bs);
        initLidlTimer();
    }

    public int byte2Int(byte bt) {
        return bt & 0x000000ff;
    }

    /**
     * send number of incoming call to bledevice if the device support
     *
     * @param count
     */
    public void sendUnreadPhoneCount(int count) {
        if (state != STATE_CONNECTED || baseDevice == null)
            return;
        if (unReadPhoneCount != count) {
            byte[] time = new byte[]{(byte) 0xbe, 0x06, 0x03, (byte) 0xfe, (byte) count};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
            unReadPhoneCount = count;
        }
    }

    /**
     * query User info from ble device
     */
    public void queryUserInfoFromDevice() {
        if (mBluetoothGatt != null) {
            if (state == STATE_CONNECTED) {
                byte[] time = new byte[]{(byte) 0xbe, 0x01, 0x03, (byte) 0xed};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
            }
        }
    }

    /**
     * send count of unread sms to ble device
     *
     * @param count
     */
    public void sendUnreadSmsCount(int count) {
        if (state != STATE_CONNECTED || baseDevice == null)
            return;
        if (unReadSMSCount != count) {
            byte[] time = new byte[]{(byte) 0xbe, 0x06, 0x04, (byte) 0xfe, (byte) count};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
            unReadSMSCount = count;
        }
    }

    /**
     * read metric and time format(12 or 24)
     */
    public void getTimeMetric24HourToPhone() {
        if (state == STATE_CONNECTED) {
            byte[] time = new byte[]{(byte) 0xbe, 0x01, 0x01, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
        }
    }

    /**
     * read user info that saved to device
     */
    public void getUserInfoFromeDevice() {
        if (state == STATE_CONNECTED) {
            byte[] time = new byte[]{(byte) 0xbe, 0x01, 0x03, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
        }
    }

    /**
     * send custom command to ble device
     *
     * @param cmd
     */
    @Override
    public void sendCustomeCmd(byte[] cmd) {
        if (state == STATE_CONNECTED) {
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmd);
        }
    }

    //
    public void sendPhoneName(String comming_phone) {
        if (state == STATE_CONNECTED) {
            comming_phone = (comming_phone == null ? "" : comming_phone);
            byte[] time = new byte[20];
            int c = 0;
            time[c++] = (byte) 0xbe;
            time[c++] = (byte) 0x06;
            time[c++] = (byte) 0x01;
            time[c++] = (byte) 0xfe;
            byte[] bs = comming_phone.getBytes();
            Byte phoneLength = (byte) bs.length;

            byte len = phoneLength > 15 ? 15 : phoneLength;
            time[c++] = len;
            //if (!IsChineseOrNot.isChinese(comming_phone)) {
            for (int i = 0; i < len; i++) {
                byte b = bs[i];
                time[c++] = b;
            }
        /*} else {
            time[c++] = 0x00;
		}*/

            if (c < 20) {
                for (int t = c; t < 20; t++) {
                    time[c++] = (byte) 0xff;
                }
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
        }
    }

    private int callCurrentIndex = 0;

    /**
     * APP send phone number first, then send the contact name if get the response of device, it will vibrate
     */
    public void sendPhoneNum(CallEntry callEntry) {
        /** APP �ȷ��� ������� ��ָ����յ� �ֻ��ظ� ָ���APP ��Ҫ�ٷ��� ��ϵ������ ��ָ���
         * �����յ� �ظ� ָ��������������ϵ�˵�����ʱ�����ֲ���ȫ���� FF
         * ���档�豸������ʾ��ϵ�˵����֣�������ϵ�˵�����ʱ������ʾ�绰���롣
         **/
        callCurrentIndex = 0;
        if (callEntryList == null) {
            callEntryList = new Vector<>();
        }
        if (callEntryList.size() >= 2) {
            int dl = callEntryList.size() - 1;
            for (int i = 0; i < dl; i++) {
                callEntryList.remove(callEntryList.size() - 1);
            }
        }
        callEntryList.add(callEntry);
        sendPhoneNum();
    }

    private void sendPhoneNum() {
        if (callCurrentIndex == 0) {
            if (callEntryList != null && callEntryList.size() > 0) {
                CallEntry msg = callEntryList.get(0);
                sendPhoneNum(msg.getPhoneNum(), 0);
                callCurrentIndex = 2;
            }
        }
    }


    /**
     * send phone number or contact name
     *
     * @param phoneOrName
     * @param type        0 phone 1 name
     */
    private void sendPhoneNum(String phoneOrName, int type) {
        if (state == STATE_CONNECTED) {
            if (type == 0 && phoneOrName == null)
                return;
            byte[] time = new byte[20];
            int c = 0;
            time[c++] = (byte) 0xbe;
            time[c++] = (byte) 0x06;
            time[c++] = (byte) (type == 0 ? 0x02 : 0x01);
            time[c++] = (byte) 0xfe;
            phoneOrName = (phoneOrName == null ? "" : phoneOrName);
            try {
                byte[] bs = phoneOrName.getBytes("UTF-8");
                int len = (bs == null ? 0 : bs.length);
                len = (len > 15 ? 15 : len);
                time[c++] = (byte) len;

                for (int i = 0; i < len; i++) {
                    time[c++] = bs[i];
                }
                if (c < 20) {
                    for (int t = c; t < 20; t++) {
                        time[c++] = (byte) 0xff;
                    }
                }
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }

    private void sendSyncDay(int year, int month, int day) {
        if (state == STATE_CONNECTED) {
            byte[] data = new byte[9];
            data[0] = (byte) 0xbe;
            data[1] = 0x02;
            data[2] = 0x01;
            data[3] = (byte) 0xfe;
            data[4] = (byte) (year >> 8);
            data[5] = (byte) year;
            data[6] = (byte) month;
            data[7] = (byte) day;
            data[8] = 0;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }


    /**
     * return true if start sync data success
     * or syncing
     * return false If synchronization is complete or fails or after an error, synchronization can be started again
     * from the last recorded synchronization point in time
     *
     * @return
     */
    @Override
    public boolean startSync(long time) {
        if (state != STATE_CONNECTED)
            return false;
        if (syncState != STATE_SYNCING) {

            if (!sendCmdSync()) {
                SYNC_TIMEOUT = time;
                syncHandler.sendEmptyMessageDelayed(0x01, SYNC_TIMEOUT);
                if (callback != null) {
                    syncState = STATE_SYNC_COMPLETED;
                    callback.syncState(syncState);
                }
                return false;
            } else {
                syncHandler.sendEmptyMessageDelayed(0x01, SYNC_TIMEOUT);
                syncState = STATE_SYNCING;
                if (callback != null) {
                    callback.syncState(syncState);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * return true if start sync data success
     * or syncing
     *
     * @return
     */
    @Override
    public boolean startSync() {
        if (state != STATE_CONNECTED)
            return false;
        if (syncState != STATE_SYNCING) {

            if (!sendCmdSync()) {
                syncHandler.sendEmptyMessageDelayed(0x01, DEFAULT_SYNC_TIMEOUT);
                if (callback != null) {
                    syncState = STATE_SYNC_COMPLETED;
                    callback.syncState(syncState);
                }
                return false;
            } else {
                syncHandler.sendEmptyMessageDelayed(0x01, DEFAULT_SYNC_TIMEOUT);
                syncState = STATE_SYNCING;
                if (callback != null) {
                    callback.syncState(syncState);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * get time interval of history data
     */
    public boolean sendCmdSync() {
        if (state == BaseController.STATE_CONNECTED) {
            byte[] data = new byte[]{(byte) 0xbe, 0x02, 0x05, (byte) 0xed};
            return sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
        return false;
    }

    /**
     * you do not call the method, because it will make it different
     * you call it only your device support to control phone to take photo, play music and find phone     *
     *
     * @param state open or close(1,0) accessible(take photo,music control,find mobile phone)
     */
    public void sendAccessibly(int state) {
        if (this.state == STATE_CONNECTED && baseDevice != null) {
            DeviceInfo deviceInfo = DeviceInfo.getInstance();
            int dtype = baseDevice.getDeviceType();
            sendLow88AccessCmd(state);
        }
    }

    private void sendLow88AccessCmd(int state) {
        if (this.state == STATE_CONNECTED) {
            byte[] data = new byte[6];
            data[0] = (byte) 0xbe;
            data[1] = 0x02;
            data[2] = 0x07;
            data[3] = (byte) 0xfe;
            data[4] = (byte) state;
            data[5] = (byte) 0xed;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * read Remote Rssi
     */
    @Override
    public void readRemoteRssi() {
        if (state == STATE_CONNECTED && mBluetoothGatt != null) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    /**
     * set anti lost state
     *
     * @param st
     */
    public void sendAntiLost(int st) {
        if (this.state == STATE_CONNECTED) {
            byte[] data = new byte[]{(byte) 0xbe, 0x06, (byte) (st == 1 ? 0x0D : 0x0E), (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * set bluetooth broadcast name, you needn't to call it in general
     *
     * @param name    bluetooth broadcast name
     * @param switchS can modify or not
     */
    public void sendBleBroadcastName(String name, boolean switchS) {
        if (state == STATE_CONNECTED && name != null) {
            byte[] data = new byte[20];
            data[0] = (byte) 0xbe;
            data[1] = 0x06;
            data[2] = 0x11;
            data[3] = (byte) 0xfe;
            data[4] = (byte) (switchS ? 1 : 0);
            byte[] tps = null;
            try {
                tps = name.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                tps = name.getBytes();
                e.printStackTrace();
            }
            int tpsL = tps.length;
            System.arraycopy(tps, 0, data, 5, tpsL > 15 ? 15 : tpsL);
            if (tps.length < 15) {
                for (int i = 19; i > tpsL + 4; i--) {
                    data[i] = (byte) 0xff;
                }
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * send command to device to get device info
     */
    @Override
    public void sendDeviceInfo() {
        Log.e(TAG, "sendDeviceInfo");
        byte[] time;
        if (tempConnectedState == STATE_CONNECTED) {
            time = new byte[]{(byte) 0xbe, (byte) 0x06, (byte) 0x09, (byte) 0xfb};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
        }
    }

    /**
     * set base time, but you needn't call it in general
     */
    public void sendBaseTime() {
        if (state == STATE_CONNECTED) {
            UserInfo userInfo = UserInfo.getInstance(context);
            byte is24 = (byte) (DateUtil.is24Hour(context) ? 0 : 1);
            byte metricImperal = (byte) userInfo.getMetricImperial();
            int timezone = userInfo.getActiveTimeZone();
            byte activeTimeZone = (byte) (timezone < 0 ? Math.abs(timezone) * 2 + 0x80 : Math.abs(timezone) * 2);
            timezone = DateUtil.getTimeZone();
            byte currentTimeZone = (byte) (timezone < 0 ? Math.abs(timezone) * 2 + 0x80 : Math.abs(timezone) * 2);
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int seconds = calendar.get(Calendar.SECOND);

            byte[] time = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x01, (byte) 0xfe, metricImperal, is24,
                    activeTimeZone, currentTimeZone,
                    (byte) (year >> 8), (byte) year, (byte) month, (byte) day, (byte) week, (byte) hour, (byte)
                    minute, (byte) seconds};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
        }
    }


    private byte[] ppbytes = null;

    private void connectSuccess(BluetoothGatt g) {
        state = BaseController.STATE_CONNECTED;
        if (callback != null) {
            callback.connectState(g == null ? null : g.getDevice(), state);
        }
        deviceInfoHandler.sendEmptyMessageDelayed(0x02, 3000);


        /*if (IS_DEBUG) {
            if (BaseController.logBuilder == null) {
                BaseController.logBuilder = new StringBuilder();
            }
            BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " connect success\r\n");
        }*/

    }

    /**
     * to reset device
     */
    @Override
    public void reset() {
        super.reset();
        if (state == STATE_CONNECTED) {
            byte[] bs = new byte[]{(byte) 0xBE, 0x06, 0x30, (byte) 0xED};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, bs);
        }
    }

    /**
     * sync time to ble device
     */
    public void sendCurrentTime() {
        if (state == STATE_CONNECTED) {
            int timezone = DateUtil.getTimeZone();
            byte currentTimeZone = (byte) (timezone < 0 ? Math.abs(timezone) * 2 + 0x80 : Math.abs(timezone) * 2);
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int seconds = calendar.get(Calendar.SECOND);
            byte is24 = (byte) (DateUtil.is24Hour(context) ? 0 : 1);

            byte[] time = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x02, (byte) 0xfe,
                    (byte) (year >> 8), (byte) year, (byte) month, (byte) day, (byte) week, currentTimeZone, (byte)
                    hour, (byte) minute, (byte) seconds, is24};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
        }
    }

    /**
     * send user info to device, for example birthday, height, weight, target step ,stride length and so on.
     * see {@link UserInfo}
     */
    public void sendUserInfo() {
        if (state == STATE_CONNECTED) {
            DeviceInfo deviceInfo = DeviceInfo.getInstance();
            UserInfo userInfo = UserInfo.getInstance(context);
            String birthday = userInfo.getBirthday();
            String[] birs = birthday.split("-");
            int year = Integer.valueOf(birs[0]);
            int month = Integer.valueOf(birs[1]);
            int day = Integer.valueOf(birs[2]);
            int targetSteps = userInfo.getTargetStep();
            int metricImperial = userInfo.getMetricImperial();//metric imperial/ 0 1
            int weight = (int) (userInfo.getWeight() * 100);
            //weight = (int) ((metricImperial == 0 ? weight : weight / 0.45359237f));
            int strideLength = (int) (userInfo.getStrideLength() * 100);
            //strideLength = (metricImperial == 0 ? strideLength : (int) (strideLength * 2.54));
            int height = Math.round(userInfo.getHeight() * 100);
            AutoSleep autoSleep = AutoSleep.getInstance(context);
            int sleepHour = autoSleep.getSleepTargetHour();
            int sleepMin = autoSleep.getSleepTargetMin();
            byte[] data = null;
            int dtype = baseDevice.getDeviceType();
            data = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x03, (byte) 0xfe, (byte) ((height & 0xffff) >> 8),
                    (byte) height, (byte) getAge(), (byte) userInfo.getGender(),
                    (byte) (weight >> 8), (byte) weight, (byte) (targetSteps >> 16),
                    (byte) (targetSteps >> 8), (byte) targetSteps, (byte) (strideLength >> 8), (byte) strideLength,
                    (byte) sleepHour, (byte) sleepMin};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    private int getAge() {
        UserInfo userInfo = UserInfo.getInstance(context);
        String birthDay = userInfo.getBirthday();
        Calendar calendar = Calendar.getInstance();
        int curyear = calendar.get(Calendar.YEAR);
        calendar.setTime(DateUtil.stringToDate(birthDay, "yyyy-MM-dd"));
        int birYear = calendar.get(Calendar.YEAR);
        return curyear - birYear;
    }

    /**
     * query heart timing detect info,only the fireware version is up 89.059
     */
    public void queryTimingHeartDetectInfo() {
        float version = getVersion();
        if (state == STATE_CONNECTED) {
            byte[] sleepcmd = new byte[]{(byte) 0xbe, 0x01, 0x15, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, sleepcmd);
        }
    }

    /**
     * query sleep info
     */
    public void querySleepInfo() {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] sleepcmd = new byte[]{(byte) 0xbe, 0x01, 0x07, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, sleepcmd);
        }
    }

    private void parserSleepInfo(byte[] data) {
        if (data != null && data.length == 17) {
            SleepEntry entry = new SleepEntry();
            boolean isOn = ((data[4] & 0x01) == 1);

            int[] tpb = new int[17];
            boolean isSleep = false;
            boolean isNap = false;
            boolean isSleepRemind;
            boolean isNapRemind;
            for (int i = 5; i < 17; i++) {
                tpb[i - 5] = (data[i] & 0xff);
            }
            int sleepStartHour = 0;
            int sleepStartMin = 0;
            int sleepEndHour = 0;
            int sleepEndMin = 0;
            int sleepRemindHour = 0;
            int sleepRemindMin = 0;

            int napStartHour = 0;
            int napStartMin = 0;
            int napEndHour = 0;
            int napEndMin = 0;
            int napRemindHour = 0;
            int napRemindMin = 0;

            int sleepRemindTime = 0;
            int napRemindTime = 0;

            if (tpb[2] == tpb[3] && tpb[2] == 0xfe) {
                isSleepRemind = false;
            } else {
                isSleepRemind = true;
                sleepRemindHour = tpb[2];
                sleepRemindMin = tpb[3];
            }

            if (tpb[10] == tpb[11] && tpb[10] == 0xfe) {
                isNapRemind = false;
            } else {
                isNapRemind = true;
                napRemindHour = tpb[10];
                napRemindMin = tpb[11];
            }

            if (tpb[0] == tpb[1] && tpb[0] == 0xfe && tpb[4] == tpb[5] && tpb[5] == 0xfe) {
                isSleep = false;
            } else {
                isSleep = true;
                sleepStartHour = tpb[0];
                sleepStartMin = tpb[1];
                sleepRemindHour = tpb[2];
                sleepRemindMin = tpb[3];
                sleepEndHour = tpb[4];
                sleepEndMin = tpb[5];
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR, sleepStartHour);
                calendar.set(Calendar.MINUTE, sleepStartMin);
                long mill = calendar.getTimeInMillis();
                calendar = Calendar.getInstance();
                if (sleepRemindHour > sleepStartHour) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                }
                calendar.set(Calendar.HOUR, sleepRemindHour);
                calendar.set(Calendar.MINUTE, sleepRemindMin);
                long tpppp = calendar.getTimeInMillis();
                sleepRemindTime = (int) ((mill - tpppp) / (1000 * 60));

            }

            if (tpb[6] == tpb[7] && tpb[8] == tpb[9] && tpb[6] == tpb[8] && tpb[6] == 0xfe) {
                isNap = false;
            } else {
                isNap = true;
                napStartHour = tpb[6];
                napStartMin = tpb[7];
                napEndHour = tpb[8];
                napEndMin = tpb[9];
                napRemindHour = tpb[10];
                napRemindMin = tpb[11];

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR, napStartHour);
                calendar.set(Calendar.MINUTE, napStartMin);
                long mill = calendar.getTimeInMillis();
                calendar = Calendar.getInstance();
                if (napRemindHour > napStartHour) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                }

                calendar.set(Calendar.HOUR, napRemindHour);
                calendar.set(Calendar.MINUTE, napRemindMin);
                long tttt = calendar.getTimeInMillis();
                napRemindTime = (int) ((mill - tttt) / (1000 * 60));
            }
            entry.setAutoSleep(isOn);
            entry.setSleep(isSleep);
            entry.setNap(isNap);
            entry.setSleepRemind(isSleepRemind);
            entry.setNapRemind(isNapRemind);

            entry.setSleepStartHour(sleepStartHour);
            entry.setSleepStartMin(sleepStartMin);
            entry.setSleepEndHour(sleepEndHour);
            entry.setSleepEndMin(sleepEndMin);
            entry.setSleepRemindTime(sleepRemindTime);
            entry.setNapStartHour(napStartHour);
            entry.setNaoStartMin(napStartMin);
            entry.setNapEndHour(napEndHour);
            entry.setNapEndMin(napEndMin);
            entry.setNapRemindTime(napRemindTime);
            Intent intent = new Intent(Constants.ACTION_QUERY_SLEEP);
            intent.putExtra(Constants.EXTRA_QUERY_SLEEP, entry);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    /**
     * query state of display and do not disturb
     */
    public void queryDisplayAndDoNotDisturb() {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x08, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    private void parserTimingHeartDetect(byte[] data) {
        if (data != null && data.length == 17) {

            boolean isEnable = ((data[4] & 0x01) == 1);
            boolean isFirst = true;
            boolean isSec = true;
            boolean isThird = true;

            int firstStartH = (data[5] & 0xff);
            int firstStartM = (data[6] & 0xff);
            int firstEndH = (data[7] & 0xff);
            int firstEndM = (data[8] & 0xff);
            int secStartH = (data[9] & 0xff);
            int secStartM = (data[10] & 0xff);
            int secEndH = (data[11] & 0xff);
            int secEndMin = (data[12] & 0xff);
            int thirdStartH = (data[13] & 0xff);
            int thirdStartM = (data[14] & 0xff);
            int thirdEndH = (data[15] & 0xff);
            int thirdEndM = (data[16] & 0xff);

            if (firstEndH == firstStartH && firstEndM == firstStartM && firstEndH == firstEndM && firstEndH == 0xff) {
                isFirst = false;
            }
            if (secEndH == secStartH && secEndH == secEndMin && secEndH == secStartM && secStartM == 0xff) {
                isSec = false;
            }
            if (thirdEndH == thirdStartH && thirdStartM == thirdEndM && thirdStartH == thirdEndM && thirdStartH ==
                    0xff) {
                isThird = false;
            }

            HeartTiming heartTiming = new HeartTiming(isEnable, isFirst, isSec, isThird, firstStartH, firstStartM,
                    firstEndH, firstEndM,
                    secStartH, secStartM, secEndH, secEndMin, thirdStartH,
                    thirdStartM, thirdEndH, thirdEndM);
            Intent intent = new Intent(Constants.ACTION_QUERY_TIMING_HEART_DETECT);
            intent.putExtra(Constants.EXTRA_QUERY_TIMING_HEART_DETECT, heartTiming);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private void parserDisplay(byte[] data) {
        if (data != null && data.length == 20) {
            List<Integer> tpDisp = new ArrayList<>();
            for (int i = 4; i < 20; i++) {
                tpDisp.add(data[i] & 0xff);
            }
            DisplaySet displaySet = new DisplaySet();
            for (int i = 0; i < tpDisp.size(); i++) {
                int val = tpDisp.get(i);
                if (val == 0x00) {
                    displaySet.setShowLogo(true);
                } else if (val == 0x03) {
                    displaySet.setShowCala(true);
                } else if (val == 0x04) {
                    displaySet.setShowDist(true);
                } else if (val == 0x05) {
                    displaySet.setShowSportTime(true);
                } else if (val == 0x06) {
                    displaySet.setShowProgress(true);
                } else if (val == 0x07) {
                    displaySet.setShowEmotion(true);
                } else if (val == 0x08) {
                    displaySet.setShowAlarm(true);
                } else if (val == 0x0A) {
                    displaySet.setShowSmsMissedCall(true);///����
                } else if (val == 0x0B) {
                    displaySet.setShowIncomingReminder(true);
                } else if (val == 0x0D || val == 0x1D) {
                    displaySet.setShowMsgContentPush(true);
                } else if (val == 0x0F) {///����ʱ
                    displaySet.setShowCountDown(true);
                }
            }
            Intent intent = new Intent(Constants.ACTION_QUERY_DISPLAY);
            intent.putExtra(Constants.EXTRA_QUERY_DISPLAY, displaySet);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    /**
     * query state of alarm
     */
    public void queryAlarmInfo() {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x09, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    private void parserAlarmInfo(byte[] data) {
        if (data != null && data.length == 20) {
            int isOn = data[4];

            ArrayList<AlarmEntry> listAlarm = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                int startHour1 = data[5] & 0xff;
                int startMin1 = data[6] & 0xff;
                byte repeat1 = data[7];
                listAlarm.add(new AlarmEntry(startHour1, startMin1, repeat1, ((isOn >> i) & 0x01) == 1));
            }

            Intent intent = new Intent(Constants.ACTION_QUERY_ALARM);
            intent.putParcelableArrayListExtra(Constants.EXTRA_QUERY_ALARM, listAlarm);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    /**
     * query state of Sedentary
     */
    public void querySedentaryInfo() {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x0c, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    private void parserSedentaryInfo(byte[] data) {
        if (data != null && data.length == 19) {
            int beginHour1 = data[5] & 0xff;
            int beginMin1 = data[6] & 0xff;
            int endHour1 = data[7] & 0xff;
            int endMin1 = data[8] & 0xff;
            SedentaryRemind sedentaryRemind1 = new SedentaryRemind(beginHour1 == 0 && beginMin1 == 0 && endHour1 == 0
                    && endMin1 == 0,
                    beginHour1, beginMin1, endHour1, endMin1);

            int beginHour2 = data[9] & 0xff;
            int beginMin2 = data[10] & 0xff;
            int endHour2 = data[11] & 0xff;
            int endMin2 = data[12] & 0xff;
            SedentaryRemind sedentaryRemind2 = new SedentaryRemind(beginHour2 == 0 && beginMin2 == 0 && endHour2 == 0
                    && endMin2 == 0,
                    beginHour1, beginMin1, endHour1, endMin1);

            int beginHour3 = data[13] & 0xff;
            int beginMin3 = data[14] & 0xff;
            int endHour3 = data[15] & 0xff;
            int endMin3 = data[16] & 0xff;
            SedentaryRemind sedentaryRemind3 = new SedentaryRemind(beginHour3 == 0 && beginMin3 == 0 && endHour3 == 0
                    && endMin3 == 0,
                    beginHour1, beginMin1, endHour1, endMin1);

            int noexcise = (data[17] & 0xff) * 60 + (data[18] & 0xff);
            ArrayList<SedentaryRemind> list = new ArrayList<>();
            list.add(sedentaryRemind1);
            list.add(sedentaryRemind2);
            list.add(sedentaryRemind3);
            SedentaryRemind.noExerceseTime = noexcise;

            Intent intent = new Intent(Constants.ACTION_QUERY_SEDENTARY);
            intent.putParcelableArrayListExtra(Constants.EXTRA_QUERY_SEDENTARY, list);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        }
    }

    public void setRealTime() {
        if (tempConnectedState == STATE_CONNECTED) {
            byte[] datas = new byte[]{(byte) 0xbe, 0x02, 0x03, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, datas);
        }
    }

    /**
     * set wrist mode,left hand or right hand
     *
     * @param wristMode
     */
    public void setWristMode(WristMode wristMode) {
        if (state == STATE_CONNECTED && wristMode != null && baseDevice != null) {
            int devicetype = baseDevice.getDeviceType();
            byte[] data = new byte[]{(byte) 0xbe, 0x01, 0x0b, (byte) 0xfe,
                    (byte) (wristMode.isLeftHand() ? 0 : 1)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * set heart timing test
     *
     * @param heartTimingTest
     */
    public void setHeartTimingTest(HeartTiming heartTimingTest) {
        if (state == STATE_CONNECTED) {
            byte[] data = new byte[17];
            data[0] = (byte) 0xbe;
            data[1] = 0x01;
            data[2] = 0x15;
            data[3] = (byte) 0xfe;

            if (!heartTimingTest.isEnable()) {
                data[4] = 0;
                for (int i = 5; i < 17; i++) {
                    data[i] = (byte) 0xff;
                }
            } else {
                data[4] = 1;
                data[5] = heartTimingTest.isFirstEnable() ? (byte) heartTimingTest.getFirStartHour() : (byte) 0xff;
                data[6] = heartTimingTest.isFirstEnable() ? (byte) heartTimingTest.getFirstStartMin() : (byte) 0xff;
                data[7] = heartTimingTest.isFirstEnable() ? (byte) heartTimingTest.getFirstEndHour() : (byte) 0xff;
                data[8] = heartTimingTest.isFirstEnable() ? (byte) heartTimingTest.getFirstEndMin() : (byte) 0xff;
                data[9] = heartTimingTest.isSecondEnable() ? (byte) heartTimingTest.getSecStartHour() : (byte) 0xff;
                data[10] = heartTimingTest.isSecondEnable() ? (byte) heartTimingTest.getSecStartMin() : (byte) 0xff;
                data[11] = heartTimingTest.isSecondEnable() ? (byte) heartTimingTest.getSecEndHour() : (byte) 0xff;
                data[12] = heartTimingTest.isSecondEnable() ? (byte) heartTimingTest.getSecEndMin() : (byte) 0xff;
                data[13] = heartTimingTest.isThirdEnable() ? (byte) heartTimingTest.getThirdStartHour() : (byte) 0xff;
                data[14] = heartTimingTest.isThirdEnable() ? (byte) heartTimingTest.getThirdStartMin() : (byte) 0xff;
                data[15] = heartTimingTest.isThirdEnable() ? (byte) heartTimingTest.getThirdEndHour() : (byte) 0xff;
                data[16] = heartTimingTest.isThirdEnable() ? (byte) heartTimingTest.getThirdEndMin() : (byte) 0xff;
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * if one of the alarms  is on,the alarm is on
     *
     * @param list the size no more than 5
     */
    public void setAlarm(List<AlarmEntry> list) {

        if (state == STATE_CONNECTED && list != null && list.size() > 0) {
            byte[] data = new byte[20];
            data[0] = (byte) 0xbe;
            data[1] = 0x01;
            data[2] = 0x09;
            data[3] = (byte) 0xfe;

            int states = 0;
            int index = 5;
            for (int i = 0; i < (list.size() > 5 ? 5 : list.size()); i++) {

                AlarmEntry entry = list.get(i);
                data[index] = (byte) entry.getStartHour();
                index++;
                data[index] = (byte) entry.getStartMin();
                index++;
                //byte repeat = ((byte) (entry.getRepeat() > 0 ? (entry.getRepeat() | 0x80) : entry.getRepeat()));
                byte repeat = ((byte) ((entry.getRepeat() & 0xff) > 0 ? ((entry.getRepeat() & 0xff) & 0x7F) : entry
                        .getRepeat()));
                data[index] = repeat;
                index++;
                if (entry.isOn()) {
                    states = states + (1 << i);
                }
            }
            data[4] = (byte) states;
            if (list.size() < 5) {
                for (int i = list.size(); i < 5; i++) {
                    data[index] = (byte) 0xff;
                    index++;
                    data[index] = (byte) 0xff;
                    index++;
                    data[index] = 0x00;
                    index++;
                }
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * {@link #setAlarm(List)}
     * set alarm description
     *
     * @param description no more than 15 byte
     * @param index       the value is between 0 and 4,the order is same as {@link #setAlarm(List)}
     * @param showDescrip show description content on device
     */
    public void setAlarmDescription(String description, int index, boolean showDescrip) {
        if (state == STATE_CONNECTED) {
            description = (description == null ? "" : description);
            byte[] data = new byte[20];
            data[0] = (byte) 0xbe;
            data[1] = 0x01;
            data[2] = 0x16;
            data[3] = (byte) 0xfe;
            if (description.trim().equals(""))
                showDescrip = false;
            data[4] = (byte) (showDescrip ? 1 : 0);
            data[5] = (byte) index;
            byte[] tps = null;
            try {
                tps = description.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                tps = description.getBytes();
                e.printStackTrace();
            }
            int tpsL = tps.length;
            System.arraycopy(tps, 0, data, 6, tpsL > 14 ? 14 : tpsL);
            if (tps.length < 14) {
                for (int i = 19; i > tpsL + 5; i--) {
                    data[i] = (byte) 0xff;
                }
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * one of list is on,the sedentary remnid is on
     *
     * @param list max size is 3
     */
    public void setSedintaryRemind(List<SedentaryRemind> list) {
        if (state == STATE_CONNECTED) {
            if (list != null && list.size() > 0) {
                byte[] data = new byte[19];
                data[0] = (byte) 0xbe;
                data[1] = 0x01;
                data[2] = 0x0c;
                data[3] = (byte) 0xfe;
                boolean isOn = false;
                List<byte[]> listD = new ArrayList<>();
                int index = -1;
                for (int i = 0; i < (list.size() > 3 ? 3 : list.size()); i++) {
                    if (list.get(i).isOn()) {
                        SedentaryRemind remind = list.get(i);
                        isOn = true;
                        index = i;
                        byte[] tp = new byte[4];
                        tp[0] = (byte) remind.getBeginHour();
                        tp[1] = (byte) remind.getBeginMin();
                        tp[2] = (byte) remind.getEndHour();
                        tp[3] = (byte) remind.getEndMin();
                        listD.add(tp);
                    } else {
                        byte[] tp = new byte[]{0, 0, 0, 0};
                        listD.add(tp);
                    }
                }
                data[4] = (byte) (isOn ? 1 : 0);
                if (!isOn) {
                    for (int i = 5; i < 19; i++) {
                        data[i] = 0;
                    }
                } else {
                    if (index != -1) {
                        System.arraycopy(listD.get(index), 0, data, 5, 4);
                        if (listD.size() > 1) {
                            System.arraycopy(list.get(listD.size() - 1 - index), 0, data, 5 + 4, 4);
                            for (int i = 0; i < listD.size(); i++) {
                                if (i != index && (i != listD.size() - 1 - index)) {
                                    System.arraycopy(list.get(i), 0, data, 13, 4);
                                }
                            }
                        }
                    }
                    data[17] = (byte) (SedentaryRemind.noExerceseTime / 60);
                    data[18] = (byte) (SedentaryRemind.noExerceseTime % 60);
                }
                Thread.currentThread();
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
            }
        }
    }

    @Override
    public synchronized boolean sendCommand(UUID characteristicID, BluetoothGattService mGattService, BluetoothGatt
            mBluetoothGatt, byte[] bytes) {
        Log.e(TAG, "ThreadName = " + Thread.currentThread().getName());
        if (mBluetoothGatt == null)
            return false;
        if (bytes == null)
            return false;

        if (IS_DEBUG) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                builder.append(String.format("%02X ", bytes[i]));
            }
            String temp = builder.toString();
            if (logBuilder == null)
                logBuilder = new StringBuilder();
            logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " sendCommand " + temp).append("\r\n");
            Log.e("controller", "sendCommand = " + builder.toString());
        }

        BluetoothGattService tpService = mBluetoothGatt.getService(MAIN_SERVICE);
        if (tpService != null) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = tpService.getCharacteristic(characteristicID);
            boolean writeState = false;
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                mBluetoothGattCharacteristic.setWriteType(mBluetoothGattCharacteristic.getWriteType());
                mBluetoothGattCharacteristic.setValue(bytes);

                setDeviceBusy(mBluetoothGatt);
                writeState = mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                if (writeState == true) {
                    if (bytes != null && bytes.length >= 4 &&
                            (((bytes[0] & 0xff) == 0xbe && (bytes[1] & 0xff) == 0x06 && (bytes[2] & 0xff) == 0x09 &&
                                    (bytes[3] & 0xff) == 0xfb) ||
                                    ((bytes[0] & 0xff) == 0xbe && (bytes[1] & 0xff) == 0x06 && (bytes[2] & 0xff) ==
                                            0x0B && (bytes[3] & 0xff) == 0xed))) {
                        // temp.contains("BE 06 09 FB") || temp.contains("BE 06 0B ED"))) {
                        mLastCommand = null;
                    } else {
                        mLastCommand = bytes;
                    }

                    if (deviceInfoHandler.hasMessages(0x01))
                        deviceInfoHandler.removeMessages(0x01);
                }
            }
            Log.e(TAG, "sendCommand writeState = " + writeState);
            return writeState;
        }
        return false;
    }

    /**
     * send notification or sms to ble device
     *
     * @param notiContent
     * @param packageIndex
     * @param notitype
     */
    public void sendNotiCmd(byte[] notiContent, int packageIndex, int notitype) {
        if (state != STATE_CONNECTED || baseDevice == null)
            return;

        if (null == mBluetoothGatt || null == mBluetoothGatt.getDevice()) {
            return;
        }
        byte[] btCmd = new byte[20];
        btCmd[0] = (byte) 0xbe;
        btCmd[1] = (byte) 0x06;
        btCmd[2] = (byte) notitype;
        btCmd[3] = (byte) 0xfe;
        btCmd[4] = (byte) packageIndex;
        if (notiContent != null && notiContent.length <= 15) {
            System.arraycopy(notiContent, 0, btCmd, 5, notiContent.length);
        }
        int length = (notiContent == null ? 0 : notiContent.length);
        if (length < 15 && length >= 0) {
            for (int i = length; i < 15; i++) {
                btCmd[5 + i] = (byte) 0xff;
            }
        }
        startNoti = System.currentTimeMillis();
        sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, btCmd);

    }

    ///current notification package index

    ///handle notification DE 06 type index ED
    public void handleNotiResponse(Context context, byte[] data) {
        NotificationEntry entry = NotificationEntry.getInstance(context);
        if (data != null && data.length == 5 && (data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[4] &
                0xff) == 0xed) {
            int type = (data[2] & 0xff);
            if (!(type >= 0x12 && type <= 0x2B)) {
                currentNotiIndex = 0;
                return;
            }
            int index = (data[3] & 0xff);
            if (index == 1 && !entry.isShowDetail()) {///not send more info
                byte[] pppp = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
                        0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
                sendNotiCmd(pppp, index + 1, type);
                return;
            } else if (!entry.isShowDetail()) {
                currentNotiIndex = 0;
                removeNotificationMsg();
                sendNotiCmd();
                return;
            }
            if (NotiManager.msgVector != null && NotiManager.msgVector.size() > 0) {
                NotificationMsg msg = NotiManager.msgVector.get(0);
                byte[] tp = msg.getMsgContent();

                if (index == 1 && entry.isShowDetail()) {
                    byte[] ppp = new byte[15];
                    System.arraycopy(tp, 15, ppp, 0, 15);
                    sendNotiCmd(ppp, index + 1, type);
                } else if (index > 1) {
                    //�յ����ĸ��� ���� ���Ͱ������һ���ֽ���0xff  ���������
                    if ((index + 1) * 15 > 60 || (tp[index * 15 - 1] == (byte) 0xFF)) {
                        currentNotiIndex = 0;
                        removeNotificationMsg();
                        sendNotiCmd();
                    } else {
                        byte[] nn = new byte[15];
                        System.arraycopy(tp, index * 15, nn, 0, nn.length);
                        sendNotiCmd(nn, index + 1, type);
                    }
                }
            } else {
                currentNotiIndex = 0;
                removeNotificationMsg();
                sendNotiCmd();
            }
        }
    }

    /**
     * @param type query heart history total number if type equal 00 or query total number and get history if type
     *             equal 01
     */
    public void queryHeartHistory(byte type) {
        if (state == STATE_CONNECTED && baseDevice != null) {
            int typ = baseDevice.getDeviceType();
            if (typ == BaseDevice.TYPE_AS97) {
                byte[] cmds = new byte[]{(byte) 0xBE, 02, 0x08, (byte) 0xFE, type};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
            }
        }
    }

    /**
     * clear heart history�� devicetype == AS97
     */
    public void clearHeartHistory() {
        if (state == STATE_CONNECTED && baseDevice != null) {
            int typ = baseDevice.getDeviceType();
            if (typ == BaseDevice.TYPE_AS97) {
                byte[] cmds = new byte[]{(byte) 0xBE, 02, 0x09, (byte) 0xED};
                //sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
            }
        }
    }

    @Override
    public void syncTime() {
        sendCurrentTime();
    }

    /**
     * sync user info,for example,weight ,strideLength,target steps,sleep target and so on
     */
    @Override
    public void syncUserInfo() {
        sendUserInfo();
    }

    /**
     * set Auto Sleep
     * {@link #syncUserInfo()}     *
     *
     * @param autoSleep
     */
    public void setAutoSleep(AutoSleep autoSleep) {
        if (state == STATE_CONNECTED) {
            byte[] data = null;
            int switchOpen = (autoSleep.isAutoSleep() ? 1 : 0);
            if (switchOpen == 0) {
                data = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x07, (byte) 0xfe, (byte) 0x00, (byte) 0xff,
                        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
                        , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            } else {
                boolean isSleep = autoSleep.isSleep();
                boolean isSleepRemind = autoSleep.isSleepRemind();
                boolean isNap = autoSleep.isNap();
                boolean isNapRemind = autoSleep.isNapRemind();
                if (baseDevice != null && (baseDevice.getDeviceType() != BaseDevice.TYPE_W311N && baseDevice
                        .getDeviceType() != BaseDevice.TYPE_AT200 &&
                        baseDevice.getDeviceType() != BaseDevice.TYPE_AS97 && baseDevice.getDeviceType() !=
                        BaseDevice.TYPE_W311T)) {
                    isNapRemind = false;
                    isNap = false;
                }
                int sleepStartHour = autoSleep.getSleepStartHour();
                int sleepStartMinute = autoSleep.getSleepStartMin();
                Calendar ccc = Calendar.getInstance();
                ccc.set(Calendar.HOUR_OF_DAY, sleepStartHour);
                ccc.set(Calendar.MINUTE, sleepStartMinute);
                ccc.add(Calendar.MINUTE, -1 * autoSleep.getSleepRemindTime());
                int sleepRemindHour = ccc.get(Calendar.HOUR_OF_DAY);
                int sleepReminMin = ccc.get(Calendar.MINUTE);

                int napStartHour = autoSleep.getNapStartHour();
                int napStartMin = autoSleep.getNapStartMin();
                ccc = Calendar.getInstance();
                ccc.set(Calendar.HOUR_OF_DAY, napStartHour);
                ccc.set(Calendar.MINUTE, napStartMin);
                ccc.add(Calendar.MINUTE, -1 * autoSleep.getNapRemindTime());
                int napRemindHour = ccc.get(Calendar.HOUR_OF_DAY);
                int napRemindMin = ccc.get(Calendar.MINUTE);
                /*17Byte BE��01��07+FE+���ؿ��ƣ�1byte��
                +�ƻ�˯��Сʱ(1byte)+�ƻ�˯�߷�(1byte)
                +˯������Сʱ(1byte) +˯�����ѷ�(1byte)
                +�ƻ���Сʱ(1byte) +�ƻ��𴲷�(1byte)
                +�ƻ�����Сʱ(1byte) +�ƻ����ݷ�(1byte)
                +��������Сʱ(1byte) +�������ݷ�(1byte)
                +��������Сʱ(1byte) +�������ѷ�(1byte)*/
                data = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x07, (byte) 0xfe, (byte) 0x01, (byte) (isSleep ?
                        autoSleep.getSleepStartHour() : 0xfe),
                        (byte) (isSleep ? autoSleep.getSleepStartMin() : 0xfe), (byte) (isSleepRemind ?
                        sleepRemindHour : 0xfe),
                        (byte) (isSleepRemind ? sleepReminMin : 0xfe),
                        (byte) (isSleep ? autoSleep.getSleepEndHour() : 0xfe), (byte) (isSleep ? autoSleep
                        .getSleepEndMin() : 0xfe),
                        (byte) (isNap ? autoSleep.getNapStartHour() : 0xfe),
                        (byte) (isNap ? autoSleep.getNapStartMin() : 0xfe), (byte) (isNap ? autoSleep.getNapEndHour()
                        : 0xfe),
                        (byte) (isNap ? autoSleep.getNapEndMin() : 0xfe), (byte) (isNapRemind ? napRemindHour : 0xfe),
                        (byte) (isNapRemind ? (napRemindMin) : 0xfe)};
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * set which interface show on device
     *
     * @param displaySet
     */
    public void setDisplayInterface(DisplaySet displaySet) {
        if (state == STATE_CONNECTED && displaySet != null) {
            byte[] data = new byte[20];
            data[0] = (byte) 0xbe;
            data[1] = (byte) 0x01;
            data[2] = (byte) 0x08;
            data[3] = (byte) 0xfe;
            int index = 0;
            data[4] = 20;
            data[5] = (byte) 0xff;
            if (!displaySet.isShowLogo()) {
                data[5] = ~0x03;//111111100
            }
            data[6] = (byte) 0xff;
            if (!displaySet.isShowCala()) {
                data[6] = ~0x03;
            }
            if (!displaySet.isShowDist()) {
                data[6] = (byte) (data[6] & (~(0x03 << 2)));
            }
            if (!displaySet.isShowSportTime()) {
                data[6] = (byte) (data[6] & (~(0x03 << 4)));
            }
            if (!displaySet.isShowProgress()) {
                data[6] = (byte) (data[6] & (~(0x03 << 6)));
            }
            data[7] = (byte) 0xff;
            if (!displaySet.isShowEmotion()) {
                data[7] = ~0x03;
            }
            if (!displaySet.isShowAlarm()) {
                data[7] = (byte) (data[7] & (~(0x03 << 2)));
            }
            if (!displaySet.isShowSmsMissedCall()) {
                data[7] = (byte) (data[7] & (~(0x03 << 4)));
            }
            if (!displaySet.isShowIncomingReminder()) {
                data[7] = (byte) (data[7] & (~(0x03 << 6)));
            }
            data[8] = (byte) 0xff;
            if (!displaySet.isShowMsgContent()) {
                data[8] = ~0x03;
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * get fireware version
     *
     * @return
     */
    private float getVersion() {
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        float version = Float.valueOf(deviceInfo.getFirmwareHighVersion() + (deviceInfo.getFirmwareLowVersion() /
                1000.0f));
        return version;
    }

    /**
     * @param isEnable
     */
    public boolean setEnableHeart(boolean isEnable) {
        setShowHeartRate(isEnable);
        return true;
//        float version = getVersion();
//        if (state == STATE_CONNECTED) {
//            byte[] bs = new byte[20];
//            bs[0] = (byte) 0xbe;
//            bs[1] = 0x01;
//            bs[2] = 0x15;
//            bs[3] = (byte) 0xfe;
//            bs[4] = (byte) (isEnable ? 0x81 : 0x80);
//            for (int i = 5; i < 20; i++) {
//                bs[i] = (byte) 0xff;
//            }
//            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, bs);
//            return true;
//        }
//        return false;
    }

    /**
     * judge whether support heart command to control state of  heart test that is on or off
     *
     * @param db
     * @return
     */
    public boolean isSupportCmdHeart(BaseDevice db) {
        float version = getVersion();
        return true;
    }


    /**
     * complete interface BluetoothGattCallback
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.e("CmdNmcController", "onConnectionStateChange" + "state" + status + "newState" + newState);
      /*  if (BaseController.logBuilder == null) {
            BaseController.logBuilder = new StringBuilder();
        }*/
        unReadPhoneCount = 0;
        unReadSMSCount = 0;
        lastReceiveH = null;
        heartHisrotyRecord = null;
        tempReceiveCount = 0;
        if (callEntryList != null) {
            callEntryList.clear();
        }
        currentNotiIndex = 0;
        isFisrtTimeSync = true;
        mBluetoothGatt = gatt;
        tempConnectedState = newState;
        syncState = STATE_SYNC_COMPLETED;
        hasSyncBaseTime = false;
        state = BaseController.STATE_DISCONNECTED;

        if (NotiManager.msgVector != null) {
            NotiManager.msgVector.clear();
        }
        callCurrentIndex = 0;
        cancelLidlTimer();
        BluetoothDevice tpdevice = gatt.getDevice();
        deviceInfoHandler.removeMessages(0x01);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (tempConnectedState == BluetoothGatt.STATE_CONNECTED) {
                /*if (IS_DEBUG) {
                    BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " " +
                            "connectstatechange connected\r\n");
                }*/
                deviceInfoHandler.sendEmptyMessageDelayed(0x03, 600);
            } else {
              /*  if (IS_DEBUG) {
                    BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " connectstatechange" +
                            " error+status" + status + "newState" + newState + "\r\n");
                }*/
                state = newState;
                synchronized (mLock) {
                    close();
                }
                mGattService = null;
                dayCount = 0;
                DeviceInfo.getInstance().resetDeviceInfo();
                hasSyncBaseTime = false;
                if (callback != null) {
                    callback.connectState(tpdevice, state);
                }
            }

        } else {
            state = newState;
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                synchronized (mLock) {
                    close();
                }
            } else {
                disconnect();
            }
          /*  if (IS_DEBUG) {
                BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " connectstatechange" +
                        " error+status" + status + "newState" + newState + "\r\n");
            }*/
            Log.e(TAG, "GATT operation error: error code = " + status);
            DeviceInfo.getInstance().resetDeviceInfo();
            if (callback != null) {
                callback.connectionError(tpdevice, newState);
            }
        }
        super.onConnectionStateChange(gatt, status, newState);
    }

    private void parserGattService(BluetoothGattService service) {
        try {

            Method getDevice = service.getClass().getMethod("getDevice");
            if (getDevice != null) {
                BluetoothDevice device = (BluetoothDevice) getDevice.invoke(service);
                if (device != null) {

                }
            }
        } catch (Exception localException) {
            Log.e("parserGattService()", localException.getMessage());

        }
    }

    private void parserGatt(BluetoothGatt gatt) {
        try {
            Field mDeviceBusy = gatt.getClass().getDeclaredField("mDeviceBusy");
            if (mDeviceBusy != null) {
                mDeviceBusy.setAccessible(true);
                boolean device = (boolean) mDeviceBusy.get(gatt);
                if (device) {

                }
                Log.e("parserGatt()", "mDeviceBusy = " + device);
            }
        } catch (Exception localException) {
            Log.e("parserGatt()", localException.getMessage());
        }
    }

    private void setDeviceBusy(BluetoothGatt gatt) {
        if (gatt != null) {
            try {
                Field mDeviceBusy = gatt.getClass().getDeclaredField("mDeviceBusy");
                if (mDeviceBusy != null) {
                    mDeviceBusy.setAccessible(true);
                    boolean device = (boolean) mDeviceBusy.get(gatt);
                    if (device) {
                        mDeviceBusy.set(gatt, false);
                    }

                }
            } catch (Exception localException) {
                Log.e("parserGatt()", localException.getMessage());
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (BaseController.logBuilder == null) {
            BaseController.logBuilder = new StringBuilder();
        }
    /*    if (IS_DEBUG) {
            BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " " +
                    "onServicesDiscovered\r\n");
        }*/
        if (IS_DEBUG) {
            Log.e(TAG, "onServicesDiscovered");
        }
        MAIN_SERVICE = null;
        if (status == BluetoothGatt.GATT_SUCCESS) {
            List<BluetoothGattService> listService = gatt.getServices();
            if (listService != null && listService.size() > 0) {
                for (int i = 0; i < listService.size(); i++) {
                    BluetoothGattService service = listService.get(i);
                    String subString = service.getUuid().toString().substring(4, 8).toLowerCase();
                    if (BleConfig.UUID_MAIN_SERVICE.equals(subString)) {
                        MAIN_SERVICE = service.getUuid();
                        break;
                    }
                }
            }
            mGattService = null;
            if (MAIN_SERVICE != null) {
                mGattService = gatt.getService(MAIN_SERVICE);
            } else {
                disconnect();
            }

            if (mGattService != null) {
                List<BluetoothGattCharacteristic> listCharact = mGattService.getCharacteristics();
                if (listCharact != null && listCharact.size() > 0) {
                    for (int i = 0; i < listCharact.size(); i++) {
                        BluetoothGattCharacteristic characteristic = listCharact.get(i);
                        UUID uuid = characteristic.getUuid();
                        String subStr = uuid.toString().substring(4, 8).toLowerCase();
                        if (BleConfig.UUID_SEND_DATA_CHAR.equals(subStr)) {
                            mSendDataCharacteristic = characteristic;
                            SEND_DATA_CHAR = uuid;
                        } else if (BleConfig.UUID_RECEIVE_DATA_CHAR.equals(subStr)) {
                            mReviceDataCharacteristic = characteristic;
                            RECEIVE_DATA_CHAR = uuid;
                        } else if (subStr.equals("ff03")) {

                        } else if (BleConfig.UUID_REALTIME_RECEIVE_DATA_CHAR.equals(subStr)) {
                            mRealTimeCharacteristic = characteristic;
                            REALTIME_RECEIVE_DATA_CHAR = uuid;
                        }
                    }
                    setEnableNotification();
                } else {
                    disconnect();////�Ҳ����ͶϿ�
                }

            }
        } else {
            disconnect();////�Ҳ����ͶϿ�
        }
        super.onServicesDiscovered(gatt, status);
    }

    @Override
    public void setEnableNotification() {
        if (tempConnectedState == BaseController.STATE_CONNECTED) {
            notiHandler.sendEmptyMessageDelayed(0x02, 300);
        }
    }

    /**
     * This method will check if Heart rate value is in 8 bits or 16 bits
     */
    private boolean isHeartRateInUINT16(final byte value) {
        return ((value & 0x01) != 0);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic != null && characteristic.getUuid().equals(HEARTRATE_SERVICE_CHARACTER)) {
            int heartRate;
            if (isHeartRateInUINT16(characteristic.getValue()[0])) {
                heartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
            } else {
                heartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
            }

            if (IS_DEBUG) {
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " heartRate = " + heartRate + "\r\n");
            }
            //Log.e(TAG, "onCharacteristicChanged heartRate = " + heartRate);
            if (heartRate <= 30) {
                return;
            }
            if (onHeartListener != null) {
                if (isShowHeartRate)
                    onHeartListener.onHeartChanged(new HeartData(heartRate, Calendar.getInstance().getTimeInMillis()));
            }
        } else {
            handleCharacterisicChanged(gatt, characteristic);
        }

        super.onCharacteristicChanged(gatt, characteristic);
    }

    public void setHeartDescription() {
        if (mBluetoothGatt != null && baseDevice != null) {
            notiHandler.sendEmptyMessageDelayed(0x04, 200);
        }
    }


    @Override
    public void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.e(TAG, "**** onDescriptorWrite  ****" + ",  " + descriptor.getUuid() + ",  " + descriptor
                .getCharacteristic().getUuid() + BluetoothGatt.GATT_SUCCESS + "==" + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "gatt success");
            final UUID uuid = descriptor.getCharacteristic().getUuid();
            if (uuid.equals(SEND_DATA_CHAR)) {
                ///׼�����������Է��Ϳ���ָ���ͬ�������ˣ���ΪRECEIVE_DATA_CHAR ��֮ǰ�ʹ���
                setDeviceBusy(mBluetoothGatt);
                deviceInfoHandler.sendEmptyMessageDelayed(0x01, 1000);
                if (dsCallBack != null) {
                    dsCallBack.isReadySync(true);
                }
                ////����ʵʱ����ͨ��
                notiHandler.sendEmptyMessageDelayed(0x03, 200);
            } else if (uuid.equals(RECEIVE_DATA_CHAR)) {////��ʷ���ݽ���ͨ��, ���óɹ��󣬿����������ͨ�� SEND_DATA_CHAR
                notiHandler.sendEmptyMessageDelayed(0x01, 200);
            } else if (uuid.equals(REALTIME_RECEIVE_DATA_CHAR)) {////ʵʱ����ͨ�������ɹ������ſ�����������ͨ��,
/// ���õ�����û�����ʷ���
                notiHandler.sendEmptyMessageDelayed(0x04, 200);
            }

        } else if (status == BluetoothGatt.GATT_FAILURE) {
            Log.e(TAG, "gatt failed");
            ///����Ҫ��һ����ʱ��������ܻ����һֱ���ò��ɹ�����������ѭ���������ڴ����
            final UUID duuid = descriptor.getCharacteristic().getUuid();
            if (duuid.equals(SEND_DATA_CHAR)) {
                notiHandler.sendEmptyMessageDelayed(0x01, 200);
            } else if (duuid.equals(RECEIVE_DATA_CHAR)) {
                notiHandler.sendEmptyMessageDelayed(0x02, 200);
            } else if (duuid.equals(REALTIME_RECEIVE_DATA_CHAR)) {
                notiHandler.sendEmptyMessageDelayed(0x03, 200);
            } else if (duuid.equals(HEARTRATE_SERVICE_CHARACTER)) {
                notiHandler.sendEmptyMessageDelayed(0x04, 200);
            }
        }
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (dsCallBack != null) {
            dsCallBack.readRssiCompleted(rssi);
        }
    }


    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        byte[] value = characteristic.getValue();
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        if (value != null && value.length > 0) {
            StringBuilder builder = new StringBuilder();
            if (IS_DEBUG) {
                if (BaseController.logBuilder == null) {
                    BaseController.logBuilder = new StringBuilder();
                }
                for (int i = 0; i < value.length; i++) {
                    builder.append(String.format("%02X", value[i]) + " ");
                }
                logBuilder.append(DateUtil.dataToString(new Date(), "HH:mm:ss") + " onCharacteristicWrite success " +
                        builder.toString() + "\r\n");
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (IS_DEBUG) {
                    Log.e(TAG, "gatt success,characteristic.value = " + builder.toString());
                }
            } else {
                if (IS_DEBUG) {
                    Log.e(TAG, "gatt fail,characteristic.value = " + builder.toString());
                }

                if (cmdFailToWrite == null || !cmdFailToWrite.equals(characteristic.getValue())) {
                    cmdFailToWrite = characteristic.getValue();
                    writeHandler.sendEmptyMessageDelayed(0x01, 150);
                } else if (value.length > 4) {
                    if (value[0] == (byte) 0xbe && value[1] == 0x06 && (value[2] >= 0x12 && value[2] <= 0x2B)) {//���͵�����Ϣ�����
                        ///�������ʧ�ܣ���ȡ������������Ϣ
                        if (NotiManager.msgVector != null && NotiManager.msgVector.size() > 0) {
                            NotiManager.msgVector.remove(0);
                        }
                        ///������һ����Ϣ
                        currentNotiIndex = 0;
                        removeNotificationMsg();
                        sendNotiCmd();
                    } else if ((value[0] == (byte) 0xbe && value[1] == 0x06 && value[2] == 0x02 && value[3] == (byte)
                            0xFE)
                            || (value[0] == (byte) 0xbe && value[1] == 0x06 && value[2] == 0x01 && value[3] == (byte)
                            0xFE)) {///��������ʧ��
                        //������һ������
                        callCurrentIndex = 0;
                        if (callEntryList != null && callEntryList.size() > 0) {
                            callEntryList.remove(0);
                        }
                        sendPhoneNum();
                    }
                }
            }
        }
    }

    private void removeNotificationMsg() {
        if (NotiManager.msgVector != null && NotiManager.msgVector.size() > 0) {
            NotiManager.msgVector.remove(0);
        }
    }

    /**
     * send notification
     */
    public void sendNotiCmd() {
        if (currentNotiIndex == 0) {
            if (NotiManager.msgVector != null && NotiManager.msgVector.size() > 0) {
                startNoti = System.currentTimeMillis();
                NotificationMsg msg = NotiManager.msgVector.get(0);
                byte[] tp = msg.getMsgContent();
                byte[] ppp = new byte[15];
                System.arraycopy(tp, 0, ppp, 0, 15);
                sendNotiCmd(ppp, 1, msg.getMsgType());
                currentNotiIndex = 1;
            }
        }
    }

    /**
     * send notification
     *
     * @param msg the notification will sync with ble device
     */
    public void sendNotiCmd(NotificationMsg msg) {
        if (state == BaseController.STATE_CONNECTED) {
            if (NotiManager.msgVector == null) {
                NotiManager.msgVector = new Vector<>();
            }
            if (NotiManager.msgVector.size() >= 1) {
                NotiManager.msgVector.remove(NotiManager.msgVector.size() - 1);
            }
            NotiManager.msgVector.add(msg);
            if (System.currentTimeMillis() - startNoti > 5000) {
                currentNotiIndex = 0;
            }
            sendNotiCmd();
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
        } else {
        }
    }
}
