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


import com.isport.isportlibrary.entry.BroadcastInfo;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.tools.DateUtil;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;


/**
 * Created by Marcos on 2017/12/8.
 * weighing scale profile
 */

public class CmdWsController extends BaseController {

    private String TAG = CmdWsController.class.getCanonicalName();

    private static CmdWsController sInstance;

    /**
     * 非锁定数据
     */
    private UUID mainServiceUUID;
    private UUID mainCharactericsReceive; ///notify
    private UUID mainCharactericsSend;///////write
    /**
     * 锁定数据
     */
    private UUID lockServiceUUID; //  0x181B
    private UUID lockCharactericsReceive; ///0x2A9C read indicate 读取锁定数据
    private UUID lockCharactericsHistory;///0xFA9C indicate 历史数据


    private UUID timeServiceUUID;
    private UUID timeCharactericsUUID;/////notify

    private UUID otaServiceUUID;
    private UUID otaCharactericsNotify;
    private UUID otaCharactericsWrite;

    private BluetoothDevice bleDevice;
    private boolean hasSyncedTime;
    private Object mLock = new Object();
    private static Handler handler = null;
    private Handler notiHandler = null;

    public OnDeviceSetting dsCallBack;

    private CmdWsController(Context context) {
        super.context = context;
        if (notiHandler == null) {
            notiHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 0x01:
                            if (mBluetoothGatt != null) {
                                enableNotification(lockServiceUUID, lockCharactericsHistory, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            break;
                        case 0x02:
                            if (mBluetoothGatt != null) {
                                enableNotification(lockServiceUUID, lockCharactericsReceive, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            }
                            break;
                        case 0x03:
                            if (mBluetoothGatt != null) {
                                enableNotification(mainServiceUUID, mainCharactericsReceive, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            }
                            break;
                        case 0x04:
                            if (mBluetoothGatt != null) {
                                enableNotification(timeServiceUUID, timeCharactericsUUID, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            }
                            break;

                    }
                }
            };
        }
    }

    public static CmdWsController getInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (CmdWsController.class) {
                if (sInstance == null) {
                    sInstance = new CmdWsController(ctx.getApplicationContext());
                    if (handler == null) {
                        handler = new Handler(ctx.getApplicationContext().getMainLooper());
                    }
                }
            }
        }
        return sInstance;
    }

    public void getPowerLevel() {
        if (state == BaseController.STATE_CONNECTED) {
            BluetoothGattService service = mBluetoothGatt.getService(BATTERY_SERVICE);
            if (service != null) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC);
                if (characteristic != null) {
                    mBluetoothGatt.readCharacteristic(characteristic);
                }
            }

        }
    }


    public boolean sendCommand(UUID characteristicID, UUID serviceUUID, BluetoothGatt bluetoothGatt, byte[] bytes) {
        if (bluetoothGatt == null)
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
            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " sendCommand " + temp).append("\r\n");
            Log.e("controller", "sendCommand = " + builder.toString());
        }

        BluetoothGattService tpService = bluetoothGatt.getService(serviceUUID);
        if (tpService != null) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = tpService.getCharacteristic(characteristicID);
            boolean writeState = false;
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                mBluetoothGattCharacteristic.setWriteType(mBluetoothGattCharacteristic.getWriteType());
                mBluetoothGattCharacteristic.setValue(bytes);

                setDeviceBusy(mBluetoothGatt);
                writeState = mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
            }
            return writeState;
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
        if (mBluetoothGatt != null && state == BaseController.STATE_CONNECTED) {
            //sendCommand(sendUUID, mGattService, mBluetoothGatt, bytes);
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        hasSyncedTime = false;
        if (notiHandler.hasMessages(0x01))
            notiHandler.removeMessages(0x01);
        mainServiceUUID = null;
        otaServiceUUID = null;
        lockServiceUUID = null;
        timeServiceUUID = null;
        mBluetoothGatt = gatt;

        tempConnectedState = newState;
        if (status == BluetoothGatt.GATT_SUCCESS) {
            bleDevice = gatt.getDevice();

            if (tempConnectedState == BluetoothGatt.STATE_CONNECTED) {
                if (IS_DEBUG) {
                   // BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connectstatechange connected\r\n");
                }
                notiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothGatt.discoverServices();
                    }
                }, 600);
            } else if (tempConnectedState == BluetoothGatt.STATE_DISCONNECTED) {
                if (IS_DEBUG) {
                   // BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connectstatechange disconnected\r\n");
                }
                synchronized (mLock) {
                    close();
                }
                state = newState;
                mGattService = null;
                if (callback != null) {
                    callback.connectionError(gatt.getDevice(), newState);
                }
            }
        } else {
            if (IS_DEBUG) {
               // BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connectstatechange error\r\n");
            }
            state = BaseController.STATE_DISCONNECTED;
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                synchronized (mLock) {
                    close();
                }
            } else {
                disconnect();
            }
            Log.e(TAG, "GATT operation error: error code = " + status);
            if (callback != null) {
                callback.connectionError(gatt.getDevice(), newState);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            List<BluetoothGattService> list = gatt.getServices();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    BluetoothGattService service = list.get(i);
                    String subString = service.getUuid().toString().substring(4, 8).toLowerCase();
                    if (subString.equalsIgnoreCase("1805")) {
                        timeServiceUUID = service.getUuid();
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for (int j = 0; j < characteristics.size(); j++) {
                            BluetoothGattCharacteristic characteristic = characteristics.get(i);
                            String charString = characteristic.getUuid().toString().substring(4, 8).toLowerCase();
                            if (charString.equalsIgnoreCase("2A08")) {
                                timeCharactericsUUID = characteristic.getUuid();
                                break;
                            }
                        }
                    } else if (subString.equalsIgnoreCase("faa0")) {
                        otaServiceUUID = service.getUuid();
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for (int j = 0; j < characteristics.size(); j++) {
                            BluetoothGattCharacteristic characteristic = characteristics.get(i);
                            String charString = characteristic.getUuid().toString().substring(4, 8).toLowerCase();
                            if (charString.equalsIgnoreCase("faa1")) {
                                otaCharactericsNotify = characteristic.getUuid();
                            } else if (charString.equalsIgnoreCase("faa2")) {
                                otaCharactericsWrite = characteristic.getUuid();
                            }
                        }
                    } else if (subString.equalsIgnoreCase("fff0")) {
                        mainServiceUUID = service.getUuid();
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for (int j = 0; j < characteristics.size(); j++) {
                            BluetoothGattCharacteristic characteristic = characteristics.get(i);
                            String charString = characteristic.getUuid().toString().substring(4, 8).toLowerCase();
                            if (charString.equalsIgnoreCase("fff1")) {
                                mainCharactericsReceive = characteristic.getUuid();
                            } else if (charString.equalsIgnoreCase("fff2")) {
                                mainCharactericsSend = characteristic.getUuid();
                            }
                        }
                    } else if (subString.equalsIgnoreCase("181B")) {
                        lockServiceUUID = service.getUuid();
                        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                        for (int j = 0; j < characteristics.size(); j++) {
                            BluetoothGattCharacteristic characteristic = characteristics.get(i);
                            String charString = characteristic.getUuid().toString().substring(4, 8).toLowerCase();
                            if (charString.equalsIgnoreCase("2A9C")) {
                                lockCharactericsReceive = characteristic.getUuid();
                            } else if (charString.equalsIgnoreCase("FA9C")) {
                                lockCharactericsHistory = characteristic.getUuid();
                            }
                        }
                    }
                }
            }
            if (mainServiceUUID == null || timeServiceUUID == null || lockServiceUUID == null || otaServiceUUID == null) {
                disconnect();
            } else {
                notiHandler.sendEmptyMessageDelayed(0x01, 150);
            }
        } else {

        }
    }

    public void setDeviceSetting(OnDeviceSetting dsCallBack) {
        this.dsCallBack = dsCallBack;
    }

    private void enableNotification(UUID service, UUID charac, byte[] value) {
        if (mBluetoothGatt != null) {
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(service);
            enableNotification(mBluetoothGattService, charac, value);
        }
    }

    private boolean enableNotification(BluetoothGattService mBluetoothGattService, final UUID uuid, final byte[] value) {
        if (mBluetoothGattService != null && tempConnectedState == BaseController.STATE_CONNECTED) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(uuid);
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    boolean result = internalEnableNotifications(mBluetoothGattCharacteristic);

                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            notiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 200);
                        }
                    }
                } else if (value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
                    boolean result = internalEnableIndications(mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            notiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 200);
                        }
                    }
                }
            }
        }
        return false;
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

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        byte[] values = characteristic.getValue();
        UUID uuid = characteristic.getUuid();
        if (uuid.equals(BATTERY_LEVEL_CHARACTERISTIC)) {
            if (dsCallBack != null) {
                dsCallBack.onBatteryChanged(values[0] & 0xff);
            }
        } else if (uuid.equals(lockCharactericsReceive) || uuid.equals(lockCharactericsHistory) || uuid.equals(mainCharactericsReceive)) {
            if (values != null && values.length == 20) {
                int flags = ((values[1] & 0xff) << 8) + (values[0] & 0xff);
                int year = ((values[3] & 0xff) << 8) + (values[2] & 0xff);
                int month = (values[4] & 0xff);
                int day = (values[5] & 0xff);
                int hour = (values[6] & 0xff);
                int minute = (values[7] & 0xff);
                int second = (values[8] & 0xff);
                float firstR = ((values[10] & 0xff) << 8) + (values[9] & 0xff);
                int weight = ((values[12] & 0xff) << 8) + (values[11] & 0xff);
                float secondR = ((values[14] & 0xff) << 8) + (values[13] & 0xff);

                int attr = ((values[15] & 0xff));
                /**
                 * 如果单位是 ST：LB(see {@link BroadcastInfo#UNIT_STLB}，固定为一位小数，小数点表示为转换为其他单位保留的小数位数
                 */
                int unitType = (attr >> 3) & 0x03;
                int dotNumber = (attr >> 1) & 0x03;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);
                Date date = calendar.getTime();
                float wei = unitType == BroadcastInfo.UNIT_STLB ? weight / 10 : (dotNumber == 0 ? weight : (float) (weight / (Math.pow(10, dotNumber))));
                if (dsCallBack != null) {
                    dsCallBack.onWeightChanged(date, wei, dotNumber, unitType, flags, (flags == 0x0002 ? 0 : (firstR)), (flags == 0x0002 || flags == 0x0306) ? 0 : secondR);
                }
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);

        byte[] values = characteristic.getValue();
        UUID uuid = characteristic.getUuid();
        if (uuid.equals(BATTERY_LEVEL_CHARACTERISTIC)) {
            if (dsCallBack != null) {
                dsCallBack.onBatteryChanged(values[0] & 0xff);
            }
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (characteristic.getUuid().equals(lockCharactericsHistory) && characteristic.getService().getUuid().equals(lockServiceUUID)) {
                notiHandler.sendEmptyMessageDelayed(0x02, 150);
            } else if (characteristic.getUuid().equals(lockCharactericsReceive) && characteristic.getService().getUuid().equals(lockServiceUUID)) {
                notiHandler.sendEmptyMessageDelayed(0x03, 150);
            } else if (characteristic.getUuid().equals(mainCharactericsReceive) && characteristic.getService().getUuid().equals(mainServiceUUID)) {
                notiHandler.sendEmptyMessageDelayed(0x04, 150);
            } else if (characteristic.getUuid().equals(timeCharactericsUUID) && characteristic.getService().getUuid().equals(timeServiceUUID)) {
                if (callback != null) {
                    state = BaseController.STATE_CONNECTED;
                    callback.connectState(gatt.getDevice(), BaseController.STATE_CONNECTED);
                }
                notiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        syncTime();
                    }
                }, 150);
            }
        } else if (status == BluetoothGatt.GATT_FAILURE) {
            if (characteristic.getUuid().equals(lockCharactericsHistory) && characteristic.getService().getUuid().equals(lockServiceUUID)) {
                notiHandler.sendEmptyMessageDelayed(0x01, 150);
            } else if (characteristic.getUuid().equals(lockCharactericsReceive) && characteristic.getService().getUuid().equals(lockServiceUUID)) {
                notiHandler.sendEmptyMessageDelayed(0x02, 150);
            } else if (characteristic.getUuid().equals(mainCharactericsReceive) && characteristic.getService().getUuid().equals(mainServiceUUID)) {
                notiHandler.sendEmptyMessageDelayed(0x03, 150);
            } else if (characteristic.getUuid().equals(timeCharactericsUUID) && characteristic.getService().getUuid().equals(timeServiceUUID)) {
                notiHandler.sendEmptyMessageDelayed(0x04, 150);
            }
        }
    }

    @Override
    public void readRemoteRssi() {

    }

    @Override
    public boolean startSync() {
        return false;
    }

    @Override
    public boolean startSync(long time) {
        return false;
    }

    @Override
    public void setEnableNotification() {

    }

    @Override
    public boolean sendCommand(UUID characteristicID, BluetoothGattService mGattService, BluetoothGatt mBluetoothGatt, byte[] bytes) {
        return false;
    }

    /**
     * sync time with device
     */
    public void syncTime() {
        if (state == STATE_CONNECTED) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int seconde = calendar.get(Calendar.SECOND);
            byte[] cmds = new byte[]{(byte) (year >> 8), (byte) year, (byte) month, (byte) day, (byte) hour, (byte) minute, (byte) seconde};
            sendCommand(timeCharactericsUUID, timeServiceUUID, mBluetoothGatt, cmds);
        }
    }

    @Override
    public void syncUserInfo() {

    }

    @Override
    public void sendDeviceInfo() {

    }

    /**
     * switch unit, see {@link BroadcastInfo#UNIT_KG} and so on.
     *
     * @param type
     */
    public void switchUnit(int type) {
        if (state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{0x01, (byte) type};
            sendCommand(mainServiceUUID, mainCharactericsSend, mBluetoothGatt, cmds);
        }
    }

    public static BroadcastInfo parseBroadcast(byte[] values) {
        if (values == null || values.length < 20)
            return null;
        int idd = ((values[1] & 0xff) << 8) + (values[0] & 0xff);
        int algorithmVersion = ((values[13] & 0xff) << 8) + (values[12] & 0xff);
        int version = (values[2] & 0xff);
        int weight = ((values[5] & 0xff) << 8) + (values[4] & 0xff);
        int productId = ((values[9] & 0xff) << 24) + ((values[8] & 0xff) << 16) + ((values[7] & 0xff) << 8) + (values[6] & 0xff);
        String mac = new StringBuilder(String.format("%02X", values[19]))
                .append(":").append(String.format("%02X", values[18]))
                .append(":").append(String.format("%02X", values[17]))
                .append(":").append(String.format("%02X", values[16]))
                .append(":").append(String.format("%02X", values[15]))
                .append(":").append(String.format("%02X", values[14])).toString();
        int bleVersion = ((values[11] & 0xff) << 8) + (values[10] & 0xff);
        int unitType = ((values[3] & 0xff) >> 3) & 0x03;
        int dotNumber = ((values[3] & 0xff) >> 1) & 0x03;
        return new BroadcastInfo(idd, algorithmVersion, version, weight, productId, mac, bleVersion, unitType, dotNumber);
    }

}
