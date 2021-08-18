package com.isport.isportlibrary.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.isport.isportlibrary.entry.AlarmEntry;
import com.isport.isportlibrary.entry.AutoSleep;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.DisplaySet;
import com.isport.isportlibrary.entry.ScreenSet;
import com.isport.isportlibrary.entry.SedentaryRemind;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.entry.WristMode;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.isportlibrary.tools.ParserData;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * @author Created by Marcos Cheng on 2017/3/30.
 * W194 Series {@link BaseController#CMD_TYPE_W194}
 */

public class Cmd194Controller extends BaseController {
    private final String TAG = "Cmd194Controller";
    static final UUID UUID_SERVICE = UUID.fromString("d0a2ff00-2996-d38b-e214-86515df5a1df");
    static final UUID UUID_CHAR_INDICATE1 = UUID.fromString("d0a2ff01-2996-d38b-e214-86515df5a1df");
    static final UUID UUID_CHAR_INDICATE2 = UUID.fromString("d0a2ff02-2996-d38b-e214-86515df5a1df");
    static final UUID UUID_CHAR_NOTIFY = UUID.fromString("d0a2ff04-2996-d38b-e214-86515df5a1df");
    static final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    static final UUID UUID_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    static final UUID UUID_BATTERY_CHAR = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    private OnDeviceSetting dsCallBack;
    private static Cmd194Controller sInstance = null;
    private Handler notiHandler = null;
    private static Handler handler;
    private boolean isFF02Success = false;////onDescriptorWrite
    private boolean isFF01Success = false;
    private boolean isFF03Success = false;
    private Vector<byte[]> mListData = new Vector<>();
    private boolean isFinished = true;
    private Object mLock = new Object();
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);

    private Handler enableHandler = null;

    private Cmd194Controller(Context context) {
        logBuilder = new StringBuilder();
        this.context = context;
        if (commandHandler == null) {
            commandHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 0x01:
                            startSync();
                            break;
                        case 0x02:
                            if (!hasSyncBaseTime) {
                                sendBaseTime();
                            }
                            break;
                        case 0x03:
                            sendDeviceInfo();
                            break;
                        case 0x04:
                            cleatDeviceData();
                            break;
                    }
                }
            };
        }
        if (enableHandler == null) {
            enableHandler = new Handler(context.getMainLooper());
        }
        if (syncHandler == null) {
            syncHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (callback != null) {
                        syncState = STATE_SYNC_COMPLETED;
                        callback.syncState(syncState);
                    }
                }
            };
        }
        if (notiHandler == null) {
            notiHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what) {
                        case 1:
                            if (!isFF01Success) {
                                enableNotification(UUID_SERVICE, UUID_CHAR_INDICATE1, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            break;
                        case 2:
                            if (!isFF02Success) {
                                enableNotification(UUID_SERVICE, UUID_CHAR_INDICATE2, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            break;
                        case 3:
                            if (!isFF03Success) {
                                enableNotification(UUID_SERVICE, UUID_CHAR_NOTIFY, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            }
                            break;
                        case 4:
                            enableNotification(UUID_BATTERY_SERVICE, UUID_BATTERY_CHAR, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            break;
                    }
                }
            };
        }
    }


    public static Cmd194Controller getInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (Cmd194Controller.class) {
                if (sInstance == null) {
                    sInstance = new Cmd194Controller(ctx.getApplicationContext());
                    if (handler == null) {
                        handler = new Handler(ctx.getApplicationContext().getMainLooper());
                    }

                }
            }
        }
        return sInstance;
    }

    public void enableNotification(UUID service, UUID charac, byte[] value) {
        if (mBluetoothGatt != null) {
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(service);
            enableNotification(mBluetoothGattService, charac, value);
        }
    }


    public boolean enableNotification(final BluetoothGattService mBluetoothGattService, final UUID uuid, final byte[] value) {
        if (mBluetoothGattService != null && tempConnectedState == BaseController.STATE_CONNECTED) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(uuid);
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    boolean result = internalEnableNotifications(mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 100);
                        }
                    }
                } else if (value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
                    boolean result = internalEnableIndications(mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableHandler.postDelayed(new Runnable() {
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

    public BluetoothDevice getDeviceWithAdress(String address) {
        return address == null ? null : BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (syncHandler.hasMessages(0x01))
            syncHandler.removeMessages(0x01);
        hasSyncBaseTime = false;
        if (gatt != null) {
            mBluetoothGatt = gatt;
        }
        final BluetoothGatt gt = gatt;
        tempConnectedState = newState;
        isFF02Success = false;
        isFF01Success = false;
        isFF03Success = false;
        this.checkSum = 0;
        syncState = STATE_SYNC_COMPLETED;
        BluetoothDevice tpdevice = gatt.getDevice();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BaseController.STATE_CONNECTED) {//连接成功
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (gt != null) {
                            mBluetoothGatt = gt;
                        }
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.discoverServices();
                        }
                    }
                }).start();
            } else {
                state = newState;
                synchronized (mLock) {
                    close();
                }
                mGattService = null;
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
            if (callback != null) {
                callback.connectionError(tpdevice, newState);
            }
        }

    }

    private void releaseGatt(int status, int newState, BluetoothGatt gatt, BluetoothDevice tpDevice) {
        removerCommandHandlerMsg();
        state = newState;
        if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            synchronized (mLock) {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
            }
        } else {
            disconnect();
        }
        if (callback != null) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                callback.connectState(tpDevice, state);
            } else {
                callback.connectionError(tpDevice, state);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mGattService = gatt.getService(UUID_SERVICE);
            if (mGattService != null) {
                setEnableNotification();
            } else {
                disconnect();
            }
        } else {
            disconnect();
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        byte[] value = characteristic.getValue();
        if (value != null && value.length > 0) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < value.length; i++) {
                    builder.append(String.format("%02X", value[i]) + " ");
                }
                if (builder.toString().startsWith("BE 06 09 FB")) {
                    commandHandler.sendEmptyMessageDelayed(0x02, 2000);
                }
                Log.e(TAG, "gatt success,characteristic.value = " + builder.toString());
            } else {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < value.length; i++) {
                    builder.append(String.format("%02X", value[i]) + " ");
                }
                Log.e(TAG, "gatt fail,characteristic.value = " + builder.toString());
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (characteristic != null) {
            byte[] datas = characteristic.getValue();
            if (datas != null) {
                final StringBuilder stringBuilder = new StringBuilder(datas.length);
                for (byte byteChar : datas) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                if (IS_DEBUG) {
                    logBuilder.append(DateUtil.dataToString(new Date(), "yyyy/MM/dd HH:mm:ss") + " ReceiverCmd " + stringBuilder.toString()).append("\r\n");
                }
                UUID uuid = characteristic.getUuid();
                if (uuid.equals(UUID_CHAR_INDICATE1)) {
                    Log.e(TAG, "ReceiverCmd indicate1 " + stringBuilder.toString());
                    handleChangeIndicate1(gatt.getDevice().getAddress(), datas);
                } else if (uuid.equals(UUID_CHAR_INDICATE2)) {
                    Log.e(TAG, "ReceiverCmd indicate2 " + stringBuilder.toString());
                    handleChangeIndicate2(gatt.getDevice().getAddress(), stringBuilder, datas);
                } else if (uuid.equals(UUID_CHAR_NOTIFY)) {
                    Log.e(TAG, "ReceiverCmd notify " + stringBuilder.toString());
                    handleChangeNotify(gatt.getDevice().getAddress(), stringBuilder, datas);
                } else if (uuid.equals(UUID_BATTERY_CHAR)) {
                    Log.e(TAG, "ReceiverCmd battery " + stringBuilder.toString());
                    handleBattery(gatt.getDevice().getAddress(), stringBuilder, datas);
                }
            }
        }
    }

    private void removeMsg(Handler handler, int what) {
        if (handler != null && handler.hasMessages(what)) {
            handler.removeMessages(what);
        }
    }

    @Override
    public void readBattery() {
        if (state == STATE_CONNECTED && mBluetoothGatt != null) {
            BluetoothGattService service = mBluetoothGatt.getService(UUID_BATTERY_SERVICE);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_BATTERY_CHAR);
            if (characteristic != null) {
                mBluetoothGatt.readCharacteristic(characteristic);
            }
        }
    }

    private void removerCommandHandlerMsg() {
        removeMsg(commandHandler, 0x01);
    }


    private void handleChangeIndicate1(String mac, byte[] datas) {
        if (datas != null && datas.length >= 4) {
            if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x06 && (datas[2] & 0xff) == 0x09 && (datas[3] & 0xff) == 0xfb) {
                //strResult.trim().startsWith("DE 06 09 FB")) {
                commandHandler.sendEmptyMessageDelayed(0x02, 1000);
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x01 && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 01 01")) {
                if (!hasSyncBaseTime) {
                    commandHandler.sendEmptyMessageDelayed(0x01, 100);
                    hasSyncBaseTime = true;
                }
                if (dsCallBack != null) {
                    dsCallBack.userInfoSetting(true);
                }
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x0D && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 0D 01")) {
                if (dsCallBack != null) {
                    dsCallBack.sedentarySetting(true);
                }
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x0A && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 0A 01")) {
                if (dsCallBack != null) {
                    dsCallBack.alarmSetting(true);
                }
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x07 && (datas[3] & 0xff) == 0x06) {
                //strResult.trim().startsWith("86 00 07 06")) {

            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x07 && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 07 01")) {///p118 同步完成
                syncComplete(mac);
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x0E && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 0E 01")) {//清除设备数据命令;

            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x0B && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 0B 01")) {

            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x0C && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 0C 01")) {
                if (dsCallBack != null) {
                    dsCallBack.alarmSetting(true);
                }

            } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x01 && (datas[2] & 0xff) == 0x04 && (datas[3] & 0xff) == 0xed) {
                //strResult.trim().startsWith("DE 01 04 ED")) {
                if (dsCallBack != null) {
                    dsCallBack.screenSetting(true);
                }
            } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x01 && (datas[2] & 0xff) == 0x03 && (datas[3] & 0xff) == 0xed) {
                //strResult.trim().startsWith("DE 01 03 ED")) {

                //sendBroadcast(new Intent(Constants.OLD_STEP_LENGHT_OK));
            } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x01 && (datas[2] & 0xff) == 0x07 && (datas[3] & 0xff) == 0xed) {
                //strResult.trim().equals("DE 01 07 ED")) {
                if (dsCallBack != null) {
                    dsCallBack.autoSleepSetting(true);
                }
            } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x01 && (datas[2] & 0xff) == 0x08 && (datas[3] & 0xff) == 0xed) {
                //strResult.trim().equals("DE 01 08 ED")) {
                if (dsCallBack != null) {
                    dsCallBack.displaySetting(true);
                }
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x0E && (datas[2] & 0xff) == 0x01 && (datas[3] & 0xff) == 0x00) {
                //strResult.trim().equals("86 0E 01 00")) {

            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x06 && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().startsWith("86 00 06 01")) {////数据总量
                this.checkSum = 0;
                isFinished = false;
                processDataChecksum(datas);

            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0xAA && (datas[2] & 0xff) == 0x07 && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().equals("86 AA 07 01")) {///同步数据完成
                syncComplete(mac);
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0xAA && (datas[2] & 0xff) == 0x07 && (datas[3] & 0xff) == 0x06) {
                //strResult.trim().startsWith("86 AA 07 06")) { // 没有5分钟的数据;
                if (mListData != null) {
                    mListData.clear();
                }
                syncState = STATE_SYNC_COMPLETED;
                if (callback != null) {
                    callback.syncState(syncState);
                }
                isFinished = true;
            } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x14 && (datas[3] & 0xff) == 0x01) {
                //strResult.trim().equals("86 00 14 01")) {

            } else if (((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x0C && (datas[3] & 0xff) == 0x01) ||
                    ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x0B && (datas[3] & 0xff) == 0x01)) {
                //strResult.trim().equals("86 00 0C 01") || strResult.trim().equals("86 00 0B 01")) {

            }
        }
        if (dsCallBack != null) {
            dsCallBack.customeCmdResult(datas);
        }
    }

    private void syncComplete(final String mac) {
        if (mListData != null && checkSum > 0) {

            if (!(checkSum - 2 < 0)) {
                final Vector<byte[]> tpv = new Vector<>();
                tpv.addAll(mListData);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        ParserData.proccessData194Data(context, mac, callback, tpv, checkSum, baseDevice.getDeviceType());
                    }
                });
                if (mListData.size() * 19 >= checkSum && !isFinished) {
                    cleatDeviceData();
                }
            } else {
                syncState = STATE_SYNC_COMPLETED;
                if (callback != null) {
                    callback.syncState(syncState);
                }
            }
            mListData.clear();
            mListData = null;
        } else {
            syncState = STATE_SYNC_COMPLETED;
            if (callback != null) {
                callback.syncState(syncState);
            }
        }
        syncState = STATE_SYNC_COMPLETED;
        isFinished = true;
    }

    private int checkSum = 0;

    //老设备 解析历史数据长度.
    private void processDataChecksum(byte[] data) {
        byte[] checksum = new byte[2];
        System.arraycopy(data, 4, checksum, 0, 2);
        byte[] tpb = new byte[]{checksum[1], checksum[0]};
        int sum = ParserData.byteArrayToInt(tpb);
        this.checkSum = sum;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendSyncDay();
            }
        }, 100);

    }

    private void handleChangeIndicate2(final String mac, StringBuilder builder, byte[] datas) {
        if (mListData == null)
            mListData = new Vector<>();
        mListData.add(datas);


        if (mListData.size() * 19 >= checkSum && !isFinished && checkSum > 0) {///同步完成
            final Vector<byte[]> tpv = new Vector<>();
            tpv.addAll(mListData);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    ParserData.proccessData194Data(context, mac, callback, tpv, checkSum, baseDevice.getDeviceType());
                }
            });
            mListData.clear();
            mListData = null;
            isFinished = true;
            commandHandler.sendEmptyMessageDelayed(0x04, 150);

        } else {
            isFinished = false;
        }
    }

    private void handleChangeNotify(String mac, StringBuilder builder, byte[] datas) {

    }

    private void handleBattery(String mac, StringBuilder builder, byte[] datas) {
        if (dsCallBack != null) {
            dsCallBack.onBatteryChanged(datas[0] & 0xff);
        }
    }

    public void setDeviceSetting(OnDeviceSetting dsCallBack) {
        this.dsCallBack = dsCallBack;
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        UUID uuid = descriptor.getCharacteristic().getUuid();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "onDescriptorWrite success uuid = " + uuid.toString());
            if (uuid.equals(UUID_CHAR_INDICATE1)) {
                isFF01Success = true;
                state = STATE_CONNECTED;
                if (callback != null) {
                    callback.connectState(gatt.getDevice(), STATE_CONNECTED);
                }
                if (dsCallBack != null) {
                    dsCallBack.isReadySync(true);
                }
                readBattery();
                commandHandler.sendEmptyMessageDelayed(0x03, 1000);

            } else if (uuid.equals(UUID_CHAR_INDICATE2)) {
                isFF02Success = true;
            } else if (uuid.equals(UUID_CHAR_NOTIFY)) {
                isFF03Success = true;
            }
        } else {
            if (tempConnectedState == BaseController.STATE_CONNECTED) {
                if (uuid.equals(UUID_CHAR_INDICATE1)) {
                    notiHandler.sendEmptyMessageDelayed(0x01, 300);
                } else if (uuid.equals(UUID_CHAR_INDICATE2)) {
                    notiHandler.sendEmptyMessageDelayed(0x02, 300);
                } else if (uuid.equals(UUID_CHAR_NOTIFY)) {
                    notiHandler.sendEmptyMessageDelayed(0x03, 300);
                }
            }
        }
    }


    @Override
    public void syncTime() {
        if (state == STATE_CONNECTED) {
            sendBaseTime();
        }
    }

    @Override
    public void syncUserInfo() {
        sendBaseTime();
    }

    @Override
    public void readRemoteRssi() {
        if (state == STATE_CONNECTED) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (dsCallBack != null) {
            dsCallBack.readRssiCompleted(rssi);
        }
    }

    @Override
    public void setEnableNotification() {
        if (tempConnectedState == BaseController.STATE_CONNECTED) {
            notiHandler.sendEmptyMessageDelayed(0x01, 1200);
            notiHandler.sendEmptyMessageDelayed(0x02, 900);
            notiHandler.sendEmptyMessageDelayed(0x03, 600);
            notiHandler.sendEmptyMessageDelayed(0x04, 300);
        }
    }


    /**
     * get time interval of history data
     */
    private boolean sendCmdSync() {
        if (state == BaseController.STATE_CONNECTED) {
            byte[] data = new byte[]{0x06, 0x01, 0x00};
            return sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, data);
        }
        return false;
    }

    //get history data
    private void sendSyncDay() {
        if (state == BaseController.STATE_CONNECTED) {
            byte[] time = new byte[]{0x07, 0x01, 0x00};
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, time);
        }
    }

    public void sendScreenSet(ScreenSet screenSet) {
        if (state == BaseController.STATE_CONNECTED && screenSet != null) {

            byte[] value = new byte[6];
            value[0] = (byte) 0xbe;
            value[1] = (byte) 0x01;
            value[2] = (byte) 0x04;
            value[3] = (byte) 0xfe;
            value[4] = screenSet.getScreenColor();
            value[5] = (byte) (screenSet.isProtected() ? 1 : 0);
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, value);
        }
    }

    public void sendBaseTime() {
        if (state == BaseController.STATE_CONNECTED) {
            Calendar calendar = Calendar.getInstance();
            int curYear = calendar.get(Calendar.YEAR);
            int curMonth = calendar.get(Calendar.MONTH) + 1;
            int curDay = calendar.get(Calendar.DAY_OF_MONTH);
            int curHour = calendar.get(Calendar.HOUR_OF_DAY);
            int curMin = calendar.get(Calendar.MINUTE);
            int curSec = calendar.get(Calendar.SECOND);
            UserInfo userInfo = UserInfo.getInstance(context);
            String birthday = userInfo.getBirthday();
            calendar.setTime(DateUtil.stringToDate(birthday, "yyyy-MM-dd"));
            int birYear = calendar.get(Calendar.YEAR);
            int birMon = calendar.get(Calendar.MONTH) + 1;
            int birDay = calendar.get(Calendar.DAY_OF_MONTH);

            int stridlen = (int) userInfo.getStrideLength();
            int target = userInfo.getTargetStep();
            int weight = (int) (userInfo.getWeight() * 10);
            int uinit = userInfo.getMetricImperial();//0 1

            byte[] time = new byte[]{(byte) 0x01, 0x01, 0x00, (byte) (curYear - 2000),
                    (byte) curMonth, (byte) curDay, (byte) (birYear >> 8), (byte) (birYear & 0xff), (byte) birMon, (byte) (weight & 0xff), (byte) ((weight >> 8) & 0xff),
                    (byte) (target & 0xff), (byte) ((target >> 8) & 0xff), (byte) ((target >> 16) & 0xff), (byte) ((target >> 24) & 0xff), (byte) stridlen, (byte) uinit,
                    (byte) curHour, (byte) curMin, (byte) curSec};
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, time);
        }
    }

    @Override
    public void sendDeviceInfo() {
        Log.e(TAG, "sendDeviceInfo");
        byte[] time;
        if (tempConnectedState == STATE_CONNECTED) {
            time = new byte[]{(byte) 0xbe, (byte) 0x06, (byte) 0x09, (byte) 0xfb};
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, time);
        }
    }

    // delete the data of device
    public void cleatDeviceData() {
        if (state == BaseController.STATE_CONNECTED) {
            byte[] time = new byte[]{0x0E, 0x01, 0x00};
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, time);

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
            int index = 4;
            if (displaySet.isShowLogo()) {
                data[index] = 0x00;
                index++;
            }
            if (displaySet.isShowCala()) {
                data[index] = 0x03;
                index++;
            }
            if (displaySet.isShowDist()) {
                data[index] = 0x04;
                index++;
            }
            if (displaySet.isShowSportTime()) {
                data[index] = 0x05;
                index++;
            }
            if (displaySet.isShowProgress()) {
                data[index] = 0x06;
                index++;
            }
            if (displaySet.isShowEmotion()) {
                data[index] = 0x07;
                index++;
            }
            if (displaySet.isShowAlarm()) {
                data[index] = 0x08;
                index++;
            }
            if (displaySet.isShowSmsMissedCall()) {
                data[index] = 0x0A;
            }
            if (displaySet.isShowIncomingReminder()) {
                data[index] = 0x0B;
                index++;
            }
            if (displaySet.isShowMsgContent()) {
                data[index] = 0x1D;
                index++;
            }
            for (int i = index; i < 20; i++) {
                data[i] = (byte) 0xff;
            }
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, data);
        }
    }

    @Override
    public void reset() {
        super.reset();
        if (state == BaseController.STATE_CONNECTED) {
            byte[] time = new byte[]{(byte) 0xbe, 0x01, 0x0d, (byte) 0xed};
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, time);
        }
    }

    public void setWristMode(WristMode wristMode) {
        if (state == STATE_CONNECTED) {
            boolean isLeft = wristMode.isLeftHand();
            byte[] time = new byte[]{(byte) (isLeft ? 0x0b : 0x0c), 0x01, 0x00};
            ;
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, time);
        }
    }

    public void setAutoSleep(AutoSleep autoSleep) {
        if (state == STATE_CONNECTED) {
            byte[] data = null;
            int switchOpen = (autoSleep.isAutoSleep() ? 1 : 0);
            if (switchOpen == 0) {
                data = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x07, (byte) 0xfe, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
                        , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            } else {
                boolean isSleep = autoSleep.isSleep();
                boolean isSleepRemind = autoSleep.isSleepRemind();
                boolean isNap = autoSleep.isNap();
                boolean isNapRemind = autoSleep.isNapRemind();
                if (baseDevice != null && (baseDevice.getDeviceType() != BaseDevice.TYPE_W311N && baseDevice.getDeviceType() != BaseDevice.TYPE_AT200 ||
                        baseDevice.getDeviceType() == BaseDevice.TYPE_AS97)) {
                    isNapRemind = false;
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

                data = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x07, (byte) 0xfe, (byte) 0x01, (byte) (isSleep ? autoSleep.getSleepStartHour() : 0xfe),
                        (byte) (isSleep ? autoSleep.getSleepStartMin() : 0xfe), (byte) (isSleepRemind ? sleepRemindHour : 0xfe),
                        (byte) (isSleepRemind ? sleepReminMin : 0xfe),
                        (byte) (isSleep ? autoSleep.getSleepEndHour() : 0xfe), (byte) (isSleep ? autoSleep.getSleepEndMin() : 0xfe),
                        (byte) (isNap ? autoSleep.getNapStartHour() : 0xfe),
                        (byte) (isNap ? autoSleep.getNapStartMin() : 0xfe), (byte) (isNap ? autoSleep.getNapEndHour() : 0xfe),
                        (byte) (isNap ? autoSleep.getNapEndMin() : 0xfe), (byte) (isNapRemind ? napRemindHour : 0xfe),
                        (byte) (isNapRemind ? (napRemindMin) : 0xfe)};
            }
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, data);
        }
    }

    public void setSedintaryRemind(List<SedentaryRemind> list) {
        if (state == STATE_CONNECTED) {
            if (list != null && list.size() > 0) {
                SedentaryRemind remind = list.get(0);
                byte[] bytes = new byte[10];
                bytes[0] = 0x0d;
                bytes[1] = 0x01;
                bytes[2] = 0x00;
                if (remind != null) {
                    bytes[3] = (byte) remind.getBeginHour();
                    bytes[4] = (byte) remind.getBeginMin();
                    bytes[5] = (byte) remind.getEndHour();
                    bytes[6] = (byte) remind.getEndMin();
                    bytes[7] = (byte) (remind.getNoExerceseTime() / 60);
                    bytes[8] = (byte) (remind.getNoExerceseTime() % 60);
                    bytes[9] = (byte) (remind.isOn() ? 1 : 0);
                    sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, bytes);
                }
            }
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
    public boolean startSync(long sync_time) {
        if (state != STATE_CONNECTED)
            return false;
        if (syncState != STATE_SYNCING) {
            syncState = STATE_SYNCING;
            if (!sendCmdSync()) {
                if (callback != null) {
                    syncState = STATE_SYNC_COMPLETED;
                    callback.syncState(syncState);
                }
            } else {
                SYNC_TIMEOUT = sync_time;
                syncHandler.sendEmptyMessageDelayed(0x01, SYNC_TIMEOUT);
                if (callback != null) {
                    callback.syncState(syncState);
                }
            }
            return true;
        }
        if (callback != null) {
            callback.syncState(syncState);
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
            syncState = STATE_SYNCING;
            if (!sendCmdSync()) {
                if (callback != null) {
                    syncState = STATE_SYNC_COMPLETED;
                    callback.syncState(syncState);
                }
            } else {
                syncHandler.sendEmptyMessageDelayed(0x01, DEFAULT_SYNC_TIMEOUT);
                if (callback != null) {
                    callback.syncState(syncState);
                }
            }
            return true;
        }
        if (callback != null) {
            callback.syncState(syncState);
        }
        return false;
    }

    /**
     * if one of the alarms  is on,the alarm is on
     *
     * @param list the size no more than 4
     */
    public void setAlarm(List<AlarmEntry> list) {

        if (state == STATE_CONNECTED && list != null && list.size() > 0) {
            byte[] data = new byte[16];
            data[0] = (byte) 0x0a;
            data[1] = 0x01;
            data[2] = 0x00;


            int states = 0;
            int index = 4;
            for (int i = 0; i < (list.size() > 4 ? 4 : list.size()); i++) {

                AlarmEntry entry = list.get(i);

                if (entry.isOn()) {
                    data[index++] = (byte) entry.getStartHour();
                    data[index++] = (byte) entry.getStartMin();
                    data[index++] = entry.getRepeat();
                    states = states + (1 << i);
                } else {
                    data[index++] = (byte) 0;
                    data[index++] = (byte) 0;
                    data[index++] = 0;
                }
            }
            data[3] = (byte) states;
            if (list.size() < 4) {
                for (int i = list.size(); i < 4; i++) {
                    data[index] = (byte) 0xff;
                    index++;
                    data[index] = (byte) 0xff;
                    index++;
                    data[index] = 0x00;
                    index++;
                }
            }
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, data);
        }
    }

    @Override
    public synchronized boolean sendCommand(UUID characteristicID, BluetoothGattService gattService, BluetoothGatt bluetoothgatt, byte[] bytes) {
        if (bluetoothgatt == null)
            return false;
        if (gattService == null) {
            return false;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            builder.append(String.format("%02X ", bytes[i]));
        }
        if (logBuilder == null)
            logBuilder = new StringBuilder();
        if (IS_DEBUG) {
            logBuilder.append(DateUtil.dataToString(new Date(), "yyyy/MM/dd HH:mm:ss") + " sendCommand " + builder.toString()).append("\r\n");
        }
        Log.e("Cmd194Controller", "sendCommand = " + builder.toString());

        BluetoothGattCharacteristic mBluetoothGattCharacteristic = gattService.getCharacteristic(characteristicID);
        if (bluetoothgatt != null && mBluetoothGattCharacteristic != null) {
            mBluetoothGattCharacteristic.setWriteType(mBluetoothGattCharacteristic.getWriteType());
            mBluetoothGattCharacteristic.setValue(bytes);
            setDeviceBusy(mBluetoothGatt);
            return bluetoothgatt.writeCharacteristic(mBluetoothGattCharacteristic);
        }
        return false;
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
    public void sendCustomeCmd(byte[] bytes) {
        if (state == STATE_CONNECTED) {
            sendCommand(UUID_CHAR_INDICATE1, mGattService, mBluetoothGatt, bytes);
        }
    }

}
