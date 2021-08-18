package com.isport.isportlibrary.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.isport.isportlibrary.entry.HeartData;
import com.isport.isportlibrary.services.bleservice.OnHeartListener;
import com.isport.isportlibrary.tools.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;


/**
 * Created by Marcos on 2018/1/17.
 */

public class CmdHrController extends BaseController {
    private String TAG = CmdHrController.class.getSimpleName();
    private Handler notiHandler = null;
    private static CmdHrController sInstance;
    private Object mLock = new Object();
    private OnHeartListener onHeartListener;

    private CmdHrController(Context context) {
        this .context = context;
        if(notiHandler == null) {
            notiHandler = new Handler(context.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    enableNotification(HEARTRATE_SERVICE_UUID, HEARTRATE_SERVICE_CHARACTER, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }
            };
        }
    }

    public void setOnHeartListener(OnHeartListener listener) {
        this.onHeartListener = listener;
    }

    public static CmdHrController getInstance(Context ctx) {
        if(sInstance == null) {
            synchronized (CmdHrController.class) {
                sInstance = new CmdHrController(ctx.getApplicationContext());
            }
        }
        return sInstance;
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.e(TAG,"onConnectionStateChange newState = "+newState);
        tempConnectedState = newState;
        if(status == BluetoothGatt.GATT_SUCCESS) {
            if(newState == BluetoothGatt.STATE_CONNECTED) {
                notiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (gatt != null) {
                            Log.e(TAG,"onConnectionStateChange discovering Services ");
                            gatt.discoverServices();
                        }
                    }
                }, 600);
            }else if(newState == BluetoothGatt.STATE_DISCONNECTED) {
                synchronized (mLock) {
                    close();
                }
                state = newState;
                mGattService = null;
                if (callback != null) {
                    callback.connectState(gatt.getDevice(), newState);
                }
            }
        }else {
            state = newState;
            /*if (IS_DEBUG) {
                BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connectstatechange error\r\n");
            }*/
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
        Log.e(TAG, "onServicesDiscovered");
        List<BluetoothGattService> listService = gatt.getServices();
        BluetoothGattService heartService = null;
        if(listService != null && listService.size()>0) {
            for (int i=0;i<listService.size();i++) {
                BluetoothGattService tps = listService.get(i);
                if(tps.getUuid().equals(BaseController.HEARTRATE_SERVICE_UUID)) {
                    heartService = tps;
                    break;
                }
            }
        }
        if(heartService != null) {
            notiHandler.sendEmptyMessageDelayed(0x01,200);
        }else {
            disconnect();
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
        super.onCharacteristicChanged(gatt, characteristic);
        if (characteristic != null && characteristic.getUuid().equals(HEARTRATE_SERVICE_CHARACTER)) {
            byte[] values = characteristic.getValue();
            int heartRate;
            if (isHeartRateInUINT16(characteristic.getValue()[0])) {
                heartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
            } else {
                heartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
            }

            if (IS_DEBUG) {
                String tp = "";
                for (int i = 0;i<values.length;i++) {
                    tp = tp + " " + (values[i] & 0xff);
                }
                Log.e(TAG, "onCharacteristicChanged heartRate = " + tp);
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " heartRate = " + heartRate + "\r\n");
            }

            if (heartRate <= 30) {
                return;
            }
            if (onHeartListener != null) {
                onHeartListener.onHeartChanged(new HeartData(heartRate, Calendar.getInstance().getTimeInMillis()));
            }
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);

        if(status == BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "onDescriptorWrite success "+descriptor.getCharacteristic().getUuid());
            state = BluetoothGatt.STATE_CONNECTED;
            if (callback != null) {
                callback.connectState(gatt.getDevice(), state);
            }
        }else {
            Log.e(TAG, "onDescriptorWrite failure "+descriptor.getCharacteristic().getUuid());
            notiHandler.sendEmptyMessageDelayed(0x01, 200);
        }
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
                            }, 100);
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
                            }, 100);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Set indicate
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
    public void syncTime() {

    }

    @Override
    public void syncUserInfo() {

    }

    @Override
    public void sendDeviceInfo() {

    }

    @Override
    public void readRemoteRssi() {

    }

    @Override
    public boolean startSync() {
        return false;
    }

    @Override
    public boolean startSync(long sync_time) {
        return false;
    }

    @Override
    public void setEnableNotification() {

    }

    @Override
    public boolean sendCommand(UUID characteristicID, BluetoothGattService mGattService, BluetoothGatt mBluetoothGatt, byte[] bytes) {
        return false;
    }

    @Override
    public void sendCustomeCmd(byte[] bytes) {

    }


}
