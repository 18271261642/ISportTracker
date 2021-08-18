package com.isport.isportlibrary.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.CallEntry;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.isportlibrary.entry.HeartHisrotyRecord;
import com.isport.isportlibrary.services.bleservice.OnSportListener;
import net.vidageek.mirror.dsl.Mirror;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * @author Created by Marcos Cheng on 2016/9/13.
 * this is the basecontrol , if you want to achieve other profile,you need extend it
 * there are some method add on 1.7 allow you to do what you want to do,they are {@link #writeCharacter(UUID, UUID, byte[])},{@link #readCharacter(UUID, UUID)},
 * {@link #readDescriptor(BluetoothGattDescriptor)} , {@link #writeCharacter(UUID, UUID, byte[])} and {@link #enableNotificationIndicate(UUID, UUID, boolean)}
 * for example, if you want to get Model Number String, you can call
 * readCharacter(UUID.from("0000180A-0000-1000-8000-00805f9b34fb"), UUID.from("00002A24-0000-1000-8000-00805f9b34fb")),
 * and it will callback {@link #onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)}.
 */

public abstract class BaseController {
    protected Handler syncHandler = null;
    protected Handler commandHandler = null;
    protected Handler enableNotiHandler = null;
    protected Handler deviceInfoHandler = null;
    protected Handler writeHandler = null;
    protected boolean hasOpenRealTime = false;
    protected int unReadPhoneCount = 0;
    protected boolean isFisrtTimeSync = true;///is the first time to sync
    protected int unReadSMSCount = 0;
    protected byte[] lastReceiveH;///上一次接收到的数据
    protected HeartHisrotyRecord heartHisrotyRecord;
    protected Vector<CallEntry> callEntryList = new Vector<>();
    protected int currentNotiIndex = 0;
    protected int tempReceiveCount = 0;
    private String TAG = "BaseController";

    public static final UUID HEARTRATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");//UUID
    public static final UUID HEARTRATE_SERVICE_CHARACTER = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public final static UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    public final static UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");


    public final static UUID DEVICEINFORMATION_SERVICE = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    public final static UUID FIRMWAREREVISION_CHARACTERISTIC = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");
    /**
     * the real time data of device
     */
    public static final String ACTION_REAL_DATA = "com.isport.isportlibrary.controller.ACTION_REAL_DATA";
    public static final String EXTRA_REAL_DATE = "com.isport.isportlibrary.controller.EXTRA_REAL_DATE";
    public static final String EXTRA_REAL_CALORIC = "com.isport.isportlibrary.controller.EXTRA_REAL_CALORIC";
    public static final String EXTRA_REAL_DIST = "com.isport.isportlibrary.controller.EXTRA_REAL_DIST";
    public static final String EXTRA_REAL_STEPS = "com.isport.isportlibrary.controller.EXTRA_REAL_STEPS";
    public static final String EXTRA_REAL_SPORTTIME = "com.isport.isportlibrary.controller.EXTRA_REAL_SPORTTIME";

    /**
     * discover services completed, you can listen the action and save the service list if you want to communicate with ble device,
     * and then you can call {@link #writeCharacter(UUID, UUID, byte[])}, {@link #readDescriptor(BluetoothGattDescriptor)},
     * {@link #readCharacter(UUID, UUID)}, {@link #enableNotificationIndicate(UUID, UUID, boolean)}
     */
    public static final String ACTION_SERVICE_DISCOVERED = "com.isport.isportlibrary.controller.ACTION_SERVICE_DISCOVERED";
    public static final String EXTRA_SERVICE_LIST = "com.isport.isportlibrary.controller.EXTRA_SERVICE_LIST";

    public static final String CMD_CONFIG = "cmd_config";
    public static final String KEY_LAST_SYNC_TIME = "cmd_last_sync_time";
    public static final String KEY_LAST_SYNC_HEARTRATE_TIME = "cmd_last_sync_heartrate_time";
    /**
     * include heart rate and privacy protect
     */
    public static final int CMD_TYPE_W311 = 1;
    public static final int CMD_TYPE_W194 = 2;///194 protocol
    public static final int CMD_TYPE_W337B = 3;///W337B protocol
    public static final int CMD_TYPE_WEIGHTSCALE = 4;////体重秤
    public static final int CMD_TYPE_HEARTRATE = 5;////心率带
    public static final int CMD_TYPE_NMC = 6;///W311T系列（新W311系列）
    private OnBaseController onBaseController;
    protected int tempConnectedState = STATE_DISCONNECTED;////


    public static long SYNC_TIMEOUT = 30000;
    public static long DEFAULT_SYNC_TIMEOUT = 30000;
    public static long HEARTRATE_SYNC_TIMEOUT = 10000;
    /**
     * The profile is in disconnected state
     */
    public static final int STATE_DISCONNECTED = 0;
    /**
     * The profile is in connecting state
     */
    public static final int STATE_CONNECTING = 1;
    /**
     * The profile is in connected state
     */
    public static final int STATE_CONNECTED = 2;
    /**
     * The profile is in disconnecting state
     */
    public static final int STATE_DISCONNECTING = 3;
    /**
     * sync completed
     */
    public static final int STATE_SYNC_COMPLETED = 0;
    /**
     * is syncing
     */
    public static final int STATE_SYNCING = 1;
    /**
     * sync error, you need sync again
     */
    public static final int STATE_SYNC_ERROR = 2;

    /**
     * sync time out
     */
    public static final int STATE_SYNC_TIMEOUT = 3;
    /**
     * save device type for the protocol
     */
    public static Map<Integer, List<Integer>> MAP_DEVICE_TYPE;
    public static StringBuilder logBuilder;
    protected BaseDevice baseDevice;
    private Object mLock = new Object();
    private boolean mConnected = false;
    private BluetoothAdapter mBluetoothAdapter;

    public static void saveLog(StringBuilder builder) {
        if (builder != null) {
            if (logBuilder == null) {
                logBuilder = new StringBuilder();
            }
            logBuilder.append(builder.toString()).append("\r\n");
        }
    }

    static {
        MAP_DEVICE_TYPE = new HashMap<Integer, List<Integer>>();
        List<Integer> list311 = new ArrayList<>();
        list311.add(BaseDevice.TYPE_W311N);
        list311.add(BaseDevice.TYPE_W301N);
        list311.add(BaseDevice.TYPE_W301S);
        list311.add(BaseDevice.TYPE_W307N);
        list311.add(BaseDevice.TYPE_W307S);
        list311.add(BaseDevice.TYPE_W285S);
        list311.add(BaseDevice.TYPE_AT100);
        list311.add(BaseDevice.TYPE_AT200);
        list311.add(BaseDevice.TYPE_SAS80);
        list311.add(BaseDevice.TYPE_W307H);
        list311.add(BaseDevice.TYPE_W301H);
        list311.add(BaseDevice.TYPE_W307S_SPACE);

        MAP_DEVICE_TYPE.put(CMD_TYPE_W311, list311);
    }

    public IBleCmdCallback callback;
    public OnSportListener spCallBack;
    public BluetoothGatt mBluetoothGatt;
    public BluetoothGattService mGattService;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    int state;
    /**
     * * vibrate time is between 0 and 4
     */
    int vibrateTime = 2;
    /**
     * show hook or no when connected
     */
    boolean isShowHook = true;
    boolean isShowHeartRate = true;//默认有数据来就展示
    String currentMac;
    protected boolean hasSyncBaseTime = false;
    /**
     * state of sync sport and sleep data
     */
    int syncState;

    public boolean isHasSyncBaseTime() {
        return hasSyncBaseTime;
    }

    public void sendBaseTime() {

    }

    public void setOnBaseController(OnBaseController onBaseController) {
        this.onBaseController = onBaseController;
    }

    public BaseDevice getBaseDevice() {
        return baseDevice;
    }

    public void setBaseDevice(BaseDevice device) {
        this.baseDevice = device;
    }

    public static String getBtAddressViaReflection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
        if (bluetoothManagerService == null) {
            return null;
        }
        Object address = new Mirror().on(bluetoothManagerService).invoke().method("getAddress").withoutArgs();
        if (address != null && address instanceof String) {
            return (String) address;
        } else {
            return null;
        }
    }

    public void clearDeviceInfo() {
        if (editor != null) {
            editor.clear().commit();
        }
    }

    public DeviceInfo getDeviceInfo() {
        return DeviceInfo.getInstance();
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

    public void putString(String key, String value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CMD_CONFIG, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        editor.putString(key, value).commit();
    }

    public void removeString(String key) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CMD_CONFIG, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        if (sharedPreferences.contains(key)) {
            editor.remove(key).commit();
        }
    }

    public String getString(String key, String defaultValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CMD_CONFIG, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return sharedPreferences.getString(key, defaultValue);
    }


    /**
     * reset device
     */
    public void reset() {

    }

    /**
     * sync time to ble device
     */
    public abstract void syncTime();

    public abstract void syncUserInfo();

    public void setCallback(IBleCmdCallback callback) {
        this.callback = callback;
    }

    public void setOnSportListener(OnSportListener listener) {
        this.spCallBack = listener;
    }

    /**
     * @return current state of ble
     */
    public int getConnectState() {
        return this.state;
    }

    /**
     * return the state of sync
     *
     * @return
     */
    public int getSyncState() {
        return this.syncState;
    }

    public abstract void sendDeviceInfo();

    /**
     * read remote rssi
     */
    public abstract void readRemoteRssi();

    /**
     * start sync data from the time of last synced
     *
     * @return whether to sync data
     */
    public abstract boolean startSync();

    /**
     * start sync data from the time of last synced
     *
     * @return whether to sync data
     */
    public abstract boolean startSync(long time);

    /**
     * connect,
     * if the version of system is Android.M or Up, you need to enable location service to avoid connect fail
     *
     * @param mac mac of bluetoothdevice that you want to connect
     */


    public synchronized void connect(String mac) {
        if (IS_DEBUG) {
            Log.e(TAG, "connect mac = " + mac);
           /* if (BaseController.logBuilder == null) {
                BaseController.logBuilder = new StringBuilder();
            }*/
            String info = TAG + " brand:" + Build.BRAND + ",model:" + Build.MODEL
                    + ",sdkLevel:" + Build.VERSION.SDK_INT + ",release:" + Build.VERSION.RELEASE + ",thread:" + Thread.currentThread().getName() + ",mBluetoothGatt:" + mBluetoothGatt;
            Log.e(TAG, "connect  phone info:" + info);
            //  BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " BaseController connect()\r\n");
        }
        closeGatt();
        currentMac = mac;
        if (TextUtils.isEmpty(mac)) {
            mac = "";
        }
        final BluetoothDevice device = getDeviceWithAdress(mac);
       /* if (device != null) {
            if (device.getAddress().equals(mac)) {

            } else {
                device = getDeviceWithAdress(mac);
            }
        } else {
            device = getDeviceWithAdress(mac);
        }*/

        Log.e("connect:", " " + device);
        //  final BluetoothDevice device = getDeviceWithAdress(mac);
        if (device == null) {
            Log.e("connect:", "Device not found.  Unable to connect.");
            // TODO: 2019/4/10 当找不到当前设备时,重新连接
            /*if (isBleEnable() && isCanRe(deviceType) && Constants.CAN_RECONNECT) {
                mReconnectHandler.removeMessages(0);
                mReconnectHandler.sendEmptyMessageDelayed(0x01, reConTimes);
                peripheralDisconnected();
            } else {
                //如果没有找到设备，那么要回调连接断开到app,这样可以在app里面主动做连接
                peripheralDisconnected();
            }*/
            return;
        }


        getCallBack();
        state = STATE_CONNECTING;
        /*if (connectHandler.hasMessages(0x07)) {
            connectHandler.removeMessages(0x07);
        }
        connectHandler.sendEmptyMessageDelayed(0x07, 30000);*/

        // There are 2 ways of reconnecting to the same device:
        // 1. Reusing the same BluetoothGatt object and calling connect() - this will force
        //    the autoConnect flag to true
        // 2. Closing it and reopening a new instance of BluetoothGatt object.
        // The gatt.close() is an asynchronous method. It requires some time before it's
        // finished and device.connectGatt(...) can't be called immediately or service
        // discovery may never finish on some older devices (Nexus 4, Android 5.0.1).
        // If shouldAutoConnect() method returned false we can't call gatt.connect() and
        // have to close gatt and open it again.
        // Instead, the gatt.connect() method will be used to reconnect to the same device.
        // This method forces autoConnect = true even if the gatt was created with this
        // flag set to false.
        //mBluetoothGatt.connect();
        Log.e("connect", "mBluetoothGatt" + mBluetoothGatt);
        if (mBluetoothGatt != null) {//万一Gatt不是Null,下面会获取一个新的,这个不关闭就导致clientif增加
            Log.e("connect", "mNRFBluetoothGatt!=null,需要执行close操作");
            try {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            } catch (Exception ex) {
                Log.e("connect", ex.toString());
            }
            SystemClock.sleep(1000);

            // return;
        }

      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // connectRequest will never be null here.
            final int preferredPhy = 1;

            // A variant of connectGatt with Handled can't be used here.
            // Check https://github.com/NordicSemiconductor/Android-BLE-Library/issues/54
            String info = TAG + " brand:" + Build.BRAND + ",model:" + Build.MODEL
                    + ",sdkLevel:" + Build.VERSION.SDK_INT + ",release:" + Build.VERSION.RELEASE;

            Log.e("connect", "gatt = device.connectGatt(autoConnect = false, TRANSPORT_LE, "
                    + preferredPhy + ")" + "info:" + info);
            if (Build.MODEL.contains("ONEPLUS")) {
                mBluetoothGatt = device.connectGatt(context, true, baseBleCallBack, BluetoothDevice.TRANSPORT_LE);
               *//* mBluetoothGatt = device.connectGatt(context, true, baseBleCallBack,
                        BluetoothDevice.TRANSPORT_LE);*//*
         *//*  mBluetoothGatt = device.connectGatt(context, true, baseBleCallBack,
                        BluetoothDevice.TRANSPORT_LE);*//*
         *//* mBluetoothGatt = device.connectGatt(context, true, baseBleCallBack,
                        BluetoothDevice.TRANSPORT_LE, preferredPhy*//**//*, mHandler*//**//*);*//*
            } else {
                mBluetoothGatt = device.connectGatt(context, true, baseBleCallBack, BluetoothDevice.TRANSPORT_LE);
               *//* mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack,
                        BluetoothDevice.TRANSPORT_LE, preferredPhy*//**//*, mHandler*//**//*);*//*
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("connect", "gatt = device.connectGatt(autoConnect = false, TRANSPORT_LE)");
            mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack,
                    BluetoothDevice.TRANSPORT_LE);
        } else {
            Log.e("connect", "gatt = device.connectGatt(autoConnect = false)");
            mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack);
        }*/
        //6.0以上


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // connectRequest will never be null here.
            final int preferredPhy = 1;
           /* log(Log.DEBUG, "gatt = device.connectGatt(autoConnect = false, TRANSPORT_LE, "
                    + phyMaskToString(preferredPhy) + ")");*/
            // A variant of connectGatt with Handled can't be used here.
            // Check https://github.com/NordicSemiconductor/Android-BLE-Library/issues/54
            mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack,
                    BluetoothDevice.TRANSPORT_LE, preferredPhy/*, mHandler*/);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.e("connect", "gatt = device.connectGatt(autoConnect = false, TRANSPORT_LE)");
            mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack,
                    BluetoothDevice.TRANSPORT_LE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("connect", "gatt = device.connectGatt(autoConnect = false, TRANSPORT_LE)");
            mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack,
                    BluetoothDevice.TRANSPORT_LE);
        } else {
            Log.e("connect", "gatt = device.connectGatt(autoConnect = false)");
            mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(context, true, baseBleCallBack, BluetoothDevice.TRANSPORT_LE);
            return;
        }
        //5.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt = connectGattApi21(device, context, false, baseBleCallBack);
            return;
        }
        //5.0一下
        mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack);*/


       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //23 6.0
            if (Build.BRAND.equals("samsung") && Build.MODEL.equals("SM-J250F")) {
                mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack, BluetoothDevice.TRANSPORT_LE);
            } else {
                mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack, BluetoothDevice.TRANSPORT_LE);
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt = connectGattApi21(device, context, false, baseBleCallBack);
            return;
        }
        mBluetoothGatt = device.connectGatt(context, false, baseBleCallBack);*/
    }


    /**
     * if the version of system is Android.M or Up, you need to enable location service to avoid connect fail.
     * to connect
     *
     * @param dv the device you want to connect
     */


   /* protected Handler connectHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case 0x07:
                    //连接超时
                    if (callback != null) {
                        Log.e(TAG, "" +
                                "连接超时，进行重新连接");
                        //  disconnect();
                        //callback.reconnect();
                    }
                    break;
            }

            //isConnectedByUser = false;
        }
    };*/
    public void connect(BaseDevice dv) {
        baseDevice = dv;
        connect(dv.getMac());
        /*if (connectHandler.hasMessages(0x07)) {
            connectHandler.removeMessages(0x07);
        }
        connectHandler.sendEmptyMessageDelayed(0x07, 30000);*/
    }

    public void connectTimeout() {
        //connect(currentMac);
        if (baseBleCallBack != null) {
            // baseBleCallBack.onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_DISCONNECTED);
        }
    }

    private BluetoothGatt connectGattApi21(BluetoothDevice device, Context context, boolean autoconnect, BluetoothGattCallback callback1) {
        try {
            Method mod = device.getClass().getMethod("connectGatt", new Class[]{Context.class, Boolean.TYPE, BluetoothGattCallback.class, Integer.TYPE});
            if (mod != null) {
                BluetoothGatt gatt = (BluetoothGatt) mod.invoke(device, new Object[]{context, autoconnect, callback1, Integer.valueOf(2)});
                Log.e(TAG, "connectGattApi21  gatt:" + gatt);
                return gatt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BluetoothGatt res = device.connectGatt(context, autoconnect, callback1);
        Log.e(TAG, "connectGattApi21 err------res:" + res);
        return res;
    }


    public void closeGatt() {
        if (mBluetoothGatt != null) {
            synchronized (mLock) {
                close();
            }
        }
    }

    public void close() {
        if (mBluetoothGatt != null) {
            refresh(mBluetoothGatt);
            if (IS_DEBUG) {
                Log.e("CmdController", "BluetoothGatt.close()");
            }
            try {
                mBluetoothGatt.close();
            } catch (Exception e) {
                if (IS_DEBUG)
                    Log.e(TAG, " mBluetoothGatt.close()");
            }
            baseBleCallBack = null;
            mBluetoothGatt = null;
        }
    }

    private void getCallBack() {
        if (baseBleCallBack == null)
            baseBleCallBack = new BaseBleCallBack();
    }

    private BaseBleCallBack baseBleCallBack = new BaseBleCallBack();

    public void unbind(String mac) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            BluetoothDevice bleDevice = bluetoothAdapter.getRemoteDevice(mac);
            if (bleDevice != null) {
                unpairDevice(bleDevice);
            }
        }
    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            //  Log.e(TAG, e.getMessage());
        }
    }

    /**
     * disonnect connection
     */
    public synchronized void disconnect() {
        if (IS_DEBUG)
            Log.e(TAG, " disconnect----------mBluetoothGatt:" + mBluetoothGatt);
        baseDevice = null;
        state = STATE_DISCONNECTED;
        if (mBluetoothGatt == null)
            return;

        unReadPhoneCount = 0;
        syncHandler.removeMessages(0);
        unReadSMSCount = 0;
        lastReceiveH = null;
        heartHisrotyRecord = null;
        tempReceiveCount = 0;
        if (callEntryList != null) {
            callEntryList.clear();
        }
        currentNotiIndex = 0;
        isFisrtTimeSync = true;
        tempConnectedState = BaseController.STATE_DISCONNECTED;
        syncState = STATE_SYNC_COMPLETED;
        hasSyncBaseTime = false;
        hasOpenRealTime = false;
        state = BaseController.STATE_DISCONNECTED;


        if (syncHandler != null) {
            syncHandler.removeMessages(0);
            if (syncHandler.hasMessages(0x01)) {
                syncHandler.removeMessages(0x01);
            }
        }
        if (commandHandler != null) {
            commandHandler.removeMessages(0);
        }
        if (enableNotiHandler != null) {
            enableNotiHandler.removeMessages(0);
        }
        if (enableNotiHandler != null) {
            enableNotiHandler.removeMessages(0);
        }
        if (writeHandler != null) {
            writeHandler.removeMessages(0);
        }


        synchronized (mLock) {
            if (mBluetoothGatt != null) {
                try {
                    Log.e(TAG, " mBluetoothGatt.disconnect()");
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                } catch (Exception nullPointerException) {
                    if (IS_DEBUG)
                        Log.e(TAG, " mBluetoothGatt.disconnect()");
                }
            }
        }
       /* if (BaseController.logBuilder == null) {
            BaseController.logBuilder = new StringBuilder();
        }
        if (IS_DEBUG) {
            BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " BaseController disconnect()\r\n");
        }*/
    }

    public abstract void setEnableNotification();

    public abstract boolean sendCommand(UUID characteristicID, BluetoothGattService mGattService, BluetoothGatt mBluetoothGatt, byte[] bytes);

    /**
     * how many times to vibrate
     *
     * @return
     */
    public int getVibrateTime() {
        return vibrateTime;
    }

    /**
     * vibrate time is between 0 and 4
     *
     * @param vibrateTime
     */
    public void setVibrateTime(int vibrateTime) {
        this.vibrateTime = vibrateTime;
    }

    /**
     * weather to show HeartRate data
     *
     * @return
     */
    public boolean isShowHeartRate() {
        return isShowHeartRate;
    }

    /**
     * vibrate time is between 0 and 4
     *
     * @param isShowHeartRate
     */
    public void setShowHeartRate(boolean isShowHeartRate) {
        this.isShowHeartRate = isShowHeartRate;
    }

    public boolean isShowHook() {
        return isShowHook;
    }

    /**
     * to set whether show hook when connected
     *
     * @param showHook whether to show hook when connected
     */
    public void setShowHook(boolean showHook) {
        isShowHook = showHook;
    }

    public void readBattery() {

    }

    public void release() {
        if (mGattService != null) {
            mGattService = null;
        }
        state = STATE_DISCONNECTED;
    }

    /**
     * send custome command
     *
     * @param bytes
     */
    public abstract void sendCustomeCmd(byte[] bytes);


    /**
     * add on 1.7
     * read data from ble device, onCharacteristicRead will callback
     *
     * @param serviceUUID   uuid of bluetoothgattservice
     * @param characterUUID uuid of bluetoothgattcharacteristic
     * @return state of read
     */
    public boolean readCharacter(UUID serviceUUID, UUID characterUUID) {
        if (mBluetoothGatt != null && state == BaseController.STATE_CONNECTED) {
            BluetoothGattService gattService = mBluetoothGatt.getService(serviceUUID);
            if (gattService != null) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(characterUUID);
                return mBluetoothGatt.readCharacteristic(characteristic);
            }
        }
        return false;
    }

    /**
     * add on 1.7
     * read bluetooth gatt descriptor from ble device, onReadDescriptor will callback
     *
     * @param descriptor the descriptor you want to read
     */
    public void readDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothGatt != null && state == STATE_CONNECTED) {
            mBluetoothGatt.readDescriptor(descriptor);
        }
    }

    /**
     * add on 1.7
     * write data to ble device if you kown the uuid of service  and characteristic, onCharacteristicWrite will be callback
     *
     * @param serviceUUID   uuid of bluetoothgattservice
     * @param characterUUID uuid of  bluetoothgattcharacteristic
     * @param value         what you write
     * @return state of write
     */
    public boolean writeCharacter(UUID serviceUUID, UUID characterUUID, byte[] value) {
        if (mBluetoothGatt != null && state == BaseController.STATE_CONNECTED) {
            BluetoothGattService gattService = mBluetoothGatt.getService(serviceUUID);
            if (gattService != null) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(characterUUID);
                if (characteristic != null) {
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    characteristic.setValue(value);
                    return mBluetoothGatt.writeCharacteristic(characteristic);
                }
            }
        }
        return false;
    }

    /**
     * add on 1.7
     * set notify or indicate enable or disable
     *
     * @param serviceUUID
     * @param characteristicUUID
     * @param isenable
     * @return
     */
    public boolean enableNotificationIndicate(UUID serviceUUID, UUID characteristicUUID, boolean isenable) {
        if (tempConnectedState == STATE_CONNECTED) {
            BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
            if (service == null)
                return false;
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic == null)
                return false;
            final int properties = characteristic.getProperties();
            if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == BluetoothGattCharacteristic.PROPERTY_INDICATE) {
                return internalEnableIndications(characteristic, isenable);
            } else if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                return internalEnableNotifications(characteristic, isenable);
            }
            return false;
        }
        return false;
    }

    public BaseController() {
        super();
    }


    private boolean internalEnableIndications(BluetoothGattCharacteristic characteristic, boolean isenable) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, isenable);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getDescriptors().get(0).getUuid());
        if (descriptor != null) {
            descriptor.setValue(isenable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            return mBluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    private boolean internalEnableNotifications(BluetoothGattCharacteristic characteristic, boolean isenable) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, isenable);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getDescriptors().get(0).getUuid());
        if (descriptor != null) {
            descriptor.setValue(isenable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            return mBluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    public boolean internalReadFirmareVersion() {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null)
            return false;

        final BluetoothGattService batteryService = gatt.getService(DEVICEINFORMATION_SERVICE);
        if (batteryService == null)
            return false;

        final BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.getCharacteristic
                (FIRMWAREREVISION_CHARACTERISTIC);
        if (batteryLevelCharacteristic == null)
            return false;

        // Check characteristic property
        final int properties = batteryLevelCharacteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
            return false;

        return internalReadCharacteristic(batteryLevelCharacteristic);
    }

    private boolean internalReadCharacteristic(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
            return false;

        return gatt.readCharacteristic(characteristic);
    }

    protected boolean refresh(BluetoothGatt gatt) {
        try {
            Method localMethod = gatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                boolean res = (Boolean) localMethod.invoke(gatt);
                Log.e(TAG, "refreshServices res:" + res);
                return res;
            }
        } catch (Exception e) {
            Log.e(TAG, TAG + " refresh An exception occured while refreshing device:" + e.getMessage());
            return false;
        }
        return false;
    }

    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {

        } else {

        }
    }

    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

    }


    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

    }

    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {

    }

    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

    }

    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

    }

    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

    }

    protected class BaseBleCallBack extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BaseController.this.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BaseController.this.onServicesDiscovered(gatt, status);
            ArrayList<BluetoothGattService> list = new ArrayList<>();
            if (gatt.getServices() != null) {
                list = (ArrayList<BluetoothGattService>) gatt.getServices();
            }

            Intent intent = new Intent();
            intent.setAction(ACTION_SERVICE_DISCOVERED);
            intent.putExtra(EXTRA_SERVICE_LIST, list);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            BaseController.this.onCharacteristicRead(gatt, characteristic, status);
            if (onBaseController != null) {
                onBaseController.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            BaseController.this.onCharacteristicWrite(gatt, characteristic, status);
            if (onBaseController != null) {
                onBaseController.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            BaseController.this.onCharacteristicChanged(gatt, characteristic);
            if (onBaseController != null) {
                onBaseController.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            BaseController.this.onDescriptorWrite(gatt, descriptor, status);
            if (onBaseController != null) {
                onBaseController.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            BaseController.this.onReliableWriteCompleted(gatt, status);
            if (onBaseController != null) {
                onBaseController.onReliableWriteCompleted(gatt, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            BaseController.this.onReadRemoteRssi(gatt, rssi, status);
            if (onBaseController != null) {
                onBaseController.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            BaseController.this.onMtuChanged(gatt, mtu, status);
            if (onBaseController != null) {
                onBaseController.onMtuChanged(gatt, mtu, status);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            BaseController.this.onDescriptorRead(gatt, descriptor, status);
            if (onBaseController != null) {
                onBaseController.onDescriptorRead(gatt, descriptor, status);
            }
        }

    }

    /**
     * the interface will be callback if method of  {@link BluetoothGattCallback} was called
     */
    abstract class OnBaseController {
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        }

        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

    }

}
