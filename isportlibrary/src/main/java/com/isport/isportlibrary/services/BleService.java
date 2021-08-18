package com.isport.isportlibrary.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.isport.isportlibrary.App;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.Cmd194Controller;
import com.isport.isportlibrary.controller.Cmd337BController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.controller.CmdHrController;
import com.isport.isportlibrary.controller.CmdNmcController;
import com.isport.isportlibrary.controller.CmdWsController;
import com.isport.isportlibrary.controller.IBleCmdCallback;
import com.isport.isportlibrary.database.DbBaseDevice;
import com.isport.isportlibrary.entry.AlarmEntry;
import com.isport.isportlibrary.entry.AutoSleep;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.CallEntry;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.isportlibrary.entry.DisplaySet;
import com.isport.isportlibrary.entry.HeartTiming;
import com.isport.isportlibrary.entry.NotificationMsg;
import com.isport.isportlibrary.entry.ScreenSet;
import com.isport.isportlibrary.entry.SedentaryRemind;
import com.isport.isportlibrary.entry.SportDayData;
import com.isport.isportlibrary.entry.WristMode;
import com.isport.isportlibrary.scanner.ScanManager;
import com.isport.isportlibrary.scanner.ScanResult;
import com.isport.isportlibrary.services.bleservice.OnBloodOxygen;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.services.bleservice.OnHeartListener;
import com.isport.isportlibrary.services.bleservice.OnSportListener;
import com.isport.isportlibrary.tools.Constants;

import net.vidageek.mirror.bean.Bean;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

//import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @author Created by Marcos Cheng on 2016/8/24.
 * you just need to extend the class if you want you communicate with Ble device
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BleService extends Service implements IBleCmdCallback {

    public static int RECONNECT_INTERVAL = 7000;

    /**
     * Register BroadcastReceiver with LocalBroadcastManager
     */
    public final static String ACTION_FOUND = "com.isport.ble.ACTION_FOUND";

    @Deprecated
    public final static String EXTRA_DEVICE_FOUND = "com.isport.ble.EXTRA_DEVICE_FOUND";
    /**
     * the type of EXTRA_DEVICE_LIST_FOUNT is List<ScanResult>
     */
    public final static String EXTRA_DEVICE_LIST_FOUND = "com.isport.ble.EXTRA_DEVICE_LIST_FOUNT";

    @Deprecated
    public final static String EXTRA_DEVICE_SCANRECORD = "com.isport.ble.EXTRA_DEVICE_SCANRECORD";
    private static String DEVICE_CONFIG_PATH = "ble_config_path";
    /**
     * device disconnected manualy
     */
    private static String BLE_DISC_BY_USER = "ble_disc_by_user";
    private static String BLE_CONNECT_DEVICE = "ble_connect_device";
    private static String TAG = "BleService";
    Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private BluetoothManager bluetoothManager;
    private Map<String, BluetoothDevice> macCache = new HashMap<>();
    private BaseDevice currentDevice;
    protected BaseController baseController;
    private boolean disconnectedByUser = true;//disconnected by user
    private String connectedMac;
    private OnDeviceSetting onDeviceSetting;
    private OnBleServiceCallBack bleServiceCallBack;
    private boolean isConnectedByUser = false;//手动连接


    @SuppressLint("HandlerLeak")
    private final Handler connectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    connect(currentDevice, isConnectedByUser);
                    break;
                case 0x02:
                    connect(currentDevice, false);
                    break;
                case 0x03:

                    if (baseController != null) {
                        if (IS_DEBUG)
                            Log.e(TAG, "connectHandler连接超时disconnect");
                        baseController.disconnect();
                    }
                    break;
                case 0x04:
                    openBlu();
                    //打开蓝牙
                    break;
                case 0x05:
                    closeBlu();
                    break;
                case 0x07:
                    //连接超时
                    /*int state = -1;
                    if (baseController != null) {
                        state = baseController.getConnectState();
                    }
                    String log = TAG + " connectTimeoutTask state:" + state;
                    Log.e(TAG, log);
                    if (baseController != null && baseController.getConnectState() != BaseController.STATE_CONNECTED) {
                       *//* baseController.connectTimeout();
                        if (connectHandler.hasMessages(0x02))
                            connectHandler.removeMessages(0x02);
                        ///蓝牙开启后不能马上去连接，否则可能会出现DeadObject异常
                        disconnectDevice(currentDevice);
                        connectHandler.sendEmptyMessageDelayed(0x02, 5000);*//*
                    }*/
                    break;
            }

            //isConnectedByUser = false;
        }
    };

    private void openBlu() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {

            bluetoothAdapter.enable();
        }
    }

    public void closeBlu() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.disable();

        }
    }

    ////在扫描的时候去连接可以加快连接
    @SuppressLint("HandlerLeak")
    private final Handler scanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            startLeScan();
        }
    };

    MyScanManagerCallBack scanCallback = new MyScanManagerCallBack();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;
            if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                if (baseController != null) {
                    baseController.syncTime();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (IS_DEBUG)
                    Log.e("BleService", "ACTION_STATE_CHANGED");
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                //蓝牙状态打开
                if (state == BluetoothAdapter.STATE_ON) {
                    if (!disconnectedByUser && currentDevice != null) {
                        if (scanHandler.hasMessages(0x01))
                            scanHandler.removeMessages(0x01);
                        // TODO: 2018/3/29 暂不搜索，打开连接后直接连接，延时3s不变
//                        scanHandler.sendEmptyMessageDelayed(0x01, 2000);
                        if (connectHandler.hasMessages(0x02))
                            connectHandler.removeMessages(0x02);
                        ///蓝牙开启后不能马上去连接，否则可能会出现DeadObject异常
                        connectHandler.sendEmptyMessageDelayed(0x02, 5000);
                    }
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    if (scanHandler.hasMessages(0x01))
                        scanHandler.removeMessages(0x01);
                    if (connectHandler.hasMessages(0x02))
                        connectHandler.removeMessages(0x02);
                    if (connectHandler.hasMessages(0x01))
                        connectHandler.removeMessages(0x01);
                    if (baseController != null) {
                        if (connectHandler.hasMessages(0x03))
                            connectHandler.removeMessages(0x03);
                        ///不能马上去调用disconnect,否则可能会出现DeadObject异常
                        connectHandler.sendEmptyMessageDelayed(0x03, 3000);
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                BluetoothDevice dv = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String key = intent.getStringExtra(BluetoothDevice.EXTRA_PAIRING_KEY);
                if (bondState == BluetoothDevice.BOND_NONE) {//取消、出错、超时配对后不尝试自动重连
                    if (dv != null && currentDevice != null && currentDevice.getMac().trim().equals(dv.getAddress()
                            .trim())
                            && (currentDevice.getDeviceType() != BaseDevice.TYPE_W311T)) {
                        disconnectedByUser = true;
                        isConnectedByUser = true;
                        Log.e(TAG, " disconnectDevice:BLE_DISC_BY_USER" + currentDevice.getMac());
                        editor.putBoolean(BLE_DISC_BY_USER + "" + currentDevice.getMac(), true).commit();
                        if (connectHandler.hasMessages(0x01))
                            connectHandler.removeMessages(0x01);
                        if (connectHandler.hasMessages(0x02))
                            connectHandler.removeMessages(0x02);
                    }
                }
            }
        }
    };

    /**
     * get the current device that connected or disconnected
     *
     * @return current device that connected or disconnected
     */
    public BaseDevice getCurrentDevice() {
        if (baseController != null) {

        }
        return currentDevice;
    }

    /**
     * is connect by manual or not
     *
     * @return
     */
    public boolean isConnectedByUser() {
        return isConnectedByUser;
    }

    /**
     * it's the callback show connection state
     *
     * @param device the device that you want to connect or disconnect
     * @param state  state of connection
     */
    @Override
    public void connectState(BluetoothDevice device, int state) {
        if (initHandler.hasMessages(0x02))
            initHandler.removeMessages(0x02);
        if (initHandler.hasMessages(0x01))
            initHandler.removeMessages(0x01);

        if (editor == null) {
            sharedPreferences = getSharedPreferences(DEVICE_CONFIG_PATH, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        if (state == BaseController.STATE_CONNECTED) {
            //移除超时的触发逻辑
           /* if (connectHandler.hasMessages(0x07)) {
                connectHandler.removeMessages(0x07);
            }*/
            isConnectedByUser = false;
            disconnectedByUser = false;
            editor.remove(BLE_DISC_BY_USER + currentDevice.getMac());
            editor.putBoolean(BLE_DISC_BY_USER + currentDevice.getMac(), false).commit();
            currentDevice.setConnected(true);
            currentDevice.setConnectedTime(Calendar.getInstance().getTimeInMillis() / 1000);
            saveOrUpdate(currentDevice);
            if (baseController != null) {
                baseController.setBaseDevice(currentDevice);
            }
            cancelLeScan();
            ScanManager.getInstance(this).exit();
        } else {
            if (state == BaseController.STATE_DISCONNECTED) {
                if (device != null && !TextUtils.isEmpty(device.getAddress())) {
                    Log.e(TAG, " disconnectDevice:BLE_DISC_BY_USER" + device.getAddress());
                    boolean b = sharedPreferences.getBoolean(BLE_DISC_BY_USER + "" + device.getAddress(), true);
                    Log.e(TAG, "connectState device:" + device + ",state:" + state + ",b:" + b + ",baseController:" + baseController);
                    if (baseController != null) {
                        baseController.disconnect();
                    }
                }
                if (currentDevice != null) {
                    disconnectedByUser = sharedPreferences.getBoolean(BLE_DISC_BY_USER + "" + currentDevice.getMac(), true);
                    Log.e(TAG, "connectState device:" + device + ",state:" + state + ",disconnectedByUser:" + disconnectedByUser + ",baseController:" + baseController);
                    if (!disconnectedByUser) {
                        if (baseController != null) {
                            baseController.disconnect();
                        }
                        if (scanHandler.hasMessages(0x01))
                            scanHandler.removeMessages(0x01);
                        scanHandler.sendEmptyMessageDelayed(0x01, 200);
                    }
                }
            }
        }
    }

    @Override
    public void reconnect() {
        if (connectHandler.hasMessages(0x02))
            connectHandler.removeMessages(0x02);
        if (scanHandler.hasMessages(0x01)) {
            scanHandler.removeMessages(0x01);
        }
        ///蓝牙开启后不能马上去连接，否则可能会出现DeadObject异常
        connectHandler.sendEmptyMessageDelayed(0x02, 5000);

      /*  if (baseController != null && currentDevice != null) {
            baseController.setCallback(this);
            baseController.connect(currentDevice);
        }*/
    }

    @Override
    public void closeBluAndOpenBlu() {
        if (connectHandler.hasMessages(0x05)) {
            connectHandler.removeMessages(0x05);
        }
        connectHandler.sendEmptyMessageDelayed(0x05, 500);
        if (connectHandler.hasMessages(0x04)) {
            connectHandler.removeMessages(0x04);
        }
        connectHandler.sendEmptyMessageDelayed(0x04, 5000);

    }

    /**
     * if there are error it will be called
     *
     * @param device the device that you want to connect or disconnect
     * @param state  state of connection
     */
    @Override
    public void connectionError(BluetoothDevice device, int state) {
        if (device != null && device.getAddress() != null && !device.getAddress().equals("")) {
            boolean b = sharedPreferences.getBoolean(BLE_DISC_BY_USER + "" + currentDevice.getMac(), true);
            Log.e(TAG, "connectionError device:" + device + ",state:" + state + ",b:" + b + ",baseController:" + baseController);
            if (baseController != null) {
                baseController.disconnect();
            }
        }
        if (currentDevice != null) {
            disconnectedByUser = sharedPreferences.getBoolean(BLE_DISC_BY_USER + "" + currentDevice.getMac(), true);
            Log.e(TAG, "connectionError device:" + device + ",state:" + state + ",disconnectedByUser:" + disconnectedByUser + ",baseController:" + baseController);
            if (!disconnectedByUser) {
                if (connectHandler.hasMessages(0x02))
                    connectHandler.removeMessages(0x02);
                if (scanHandler.hasMessages(0x01)) {
                    scanHandler.removeMessages(0x01);
                }
                ///蓝牙开启后不能马上去连接，否则可能会出现DeadObject异常
                // connectHandler.sendEmptyMessageDelayed(0x02, 5000);
                if (currentDevice != null) {
                    connectHandler.sendEmptyMessageDelayed(0x02, 5000);
                } else {
                    scanHandler.sendEmptyMessageDelayed(0x01, 1000);
                }

            }
        }
    }

    /**
     * it will be called when sync completed,
     * see {@link BaseController#STATE_SYNC_COMPLETED}, {@link BaseController#STATE_SYNC_ERROR},
     *
     * @param state state of Synchronize
     * @{@link BaseController#STATE_SYNC_TIMEOUT} , {@link BaseController#STATE_SYNCING}
     */
    @Override
    public void syncState(int state) {

    }


    /**
     * real time data if the ble device support real time data
     *
     * @param dayData
     */
    @Override
    public void realTimeDayData(SportDayData dayData) {

    }

    @Override
    public void needDeviceInfoSettingCallBack() {

    }

    /**
     * get connection state
     **/
    public int getConnectionState() {
        if (this.baseController == null)
            return 0;
        return this.baseController.getConnectState();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(DEVICE_CONFIG_PATH, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);

        String info = TAG + " brand:" + Build.BRAND + ",model:" + Build.MODEL
                + ",sdkLevel:" + Build.VERSION.SDK_INT + ",release:" + Build.VERSION.RELEASE;
        Log.e(TAG, "onCreate  phone info:" + info);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * disconnect device and clear the connection record of the device,it is different from the
     * which is just to disconnect.
     *
     * @param device the device you want to unbind
     */
    public void unBind(BaseDevice device) {
        if (editor == null) {
            sharedPreferences = getSharedPreferences(DEVICE_CONFIG_PATH, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }

        if (device == null)
            return;

       // Log.e(TAG, " unBind device:" + device, new Throwable());

        if (baseController != null) {
            baseController.removeString(device.getMac());
            baseController.unbind(device.getMac());
        }

        DbBaseDevice.getInstance(this).delete("mac = ?", new String[]{device.getMac()});
        editor.clear().commit();///
        disconnectDevice(device);
        currentDevice = null;

    }

    /**
     * get devices list that you ever connected
     * the device will not in the devices list if you call {@link #unBind(BaseDevice)}
     *
     * @return null if never connect device
     */
    public List<BaseDevice> getHistoryDevice() {
        return DbBaseDevice.getInstance(this).findAll(null, null, "connectedtime desc");

    }

    /**
     * 配置设备信息
     *
     * @param info
     */
    public void setDeviceInfo(DeviceInfo info) {
        if (getConnectionState() == BaseController.STATE_CONNECTED) {
            if (baseController instanceof CmdController) {
                ((CmdController) baseController).setInfoForDevice(info);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:

                    break;
                case 0x02:

                    if (currentDevice != null) {
                        connect(currentDevice, true);
                    } else {
                        startLeScan();
                    }
                    //initHandler.sendEmptyMessageDelayed(0x03, SCAN_DURATION);
                    break;
                case 0x03:
                    cancelLeScan();
                    break;
            }
        }
    };

    /**
     * get history record that you ever connected, and to do reconnect when service start or binded
     */
    public void initDb() {
        DbBaseDevice.getInstance(BleService.this);
        List<BaseDevice> list = getHistoryDevice();
        Log.e(TAG,"-----saved-BaseDevie="+new Gson().toJson(list));
        if (list != null && list.size() > 0) {
            currentDevice = list.get(0);
            disconnectedByUser = sharedPreferences.getBoolean(BLE_DISC_BY_USER + "" + currentDevice.getMac(), false);
            if (currentDevice == null)
                return;
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                baseController = CmdController.getInstance(getApplicationContext());
                ((CmdController) baseController).setDeviceSetting(onDeviceSetting);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                baseController = Cmd194Controller.getInstance(getApplicationContext());
                ((Cmd194Controller) baseController).setDeviceSetting(onDeviceSetting);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W337B) {
                baseController = Cmd337BController.getInstance(getApplicationContext());
                ((Cmd337BController) baseController).setDeviceSetting(onDeviceSetting);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_WEIGHTSCALE) {
                baseController = CmdWsController.getInstance(getApplicationContext());
                ((CmdWsController) baseController).setDeviceSetting(onDeviceSetting);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_HEARTRATE) {
                baseController = CmdHrController.getInstance(getApplicationContext());
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                baseController = CmdNmcController.getInstance(getApplicationContext());
                ((CmdNmcController) baseController).setDeviceSetting(onDeviceSetting);
            }
        }

        if (currentDevice != null) {
            disconnectedByUser = sharedPreferences.getBoolean(BLE_DISC_BY_USER + "" + currentDevice.getMac(), true);
            if (!disconnectedByUser) {
                initHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connect(currentDevice, true);
                        isConnectedByUser = true;
                        //startLeScan();
                    }
                }, 3000);
            }
        }
    }

    /**
     * set scannerSetting for the scanner when use  {@link ScanManager#SCAN_TYPE_COMPATIBLE} or
     * {@link ScanManager#SCAN_TYPE_LOLLIPOP} to scan
     *
     * @param scanerSetting
     */
    public void setScanerSetting(ScanSettings scanerSetting) {
        ScanManager.getInstance(this).setScanSettings(scanerSetting);
    }

    /**
     * see {@link ScanManager#setScanType(int)}
     *
     * @param type
     */
    public void setScanerType(int type) {
        ScanManager.getInstance(this).setScanType(type);
    }

    /**
     * get device information
     */
    public void getDeviceInfo() {
        if (baseController != null && baseController.getConnectState() == BaseController.STATE_CONNECTED) {
            baseController.sendDeviceInfo();
        }
    }

    /**
     * @param bd
     * @param manual if manual is true, the autoconnect will be false or true
     */
    public boolean connect(final BaseDevice bd, final boolean manual) {
        if (ActivityCompat.checkSelfPermission(App.INSTANCE, Manifest.permission.BLUETOOTH) != PackageManager
                .PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(App.INSTANCE, Manifest.permission.BLUETOOTH_ADMIN) !=
                        PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }
        if (bd == null) {
            return false;
        }

        String log = TAG + " connect getConnectionState:" + getConnectionState() + ",isConnectedByUser:" + isConnectedByUser + ",baseController:" + baseController + "ScanManager.getInstance(this).isScaning()" + ScanManager.getInstance(this).isScaning() + ",baseController:" + baseController + ",currentDevice:" + currentDevice;
        Log.e(TAG, log);

        if (getConnectionState() == BaseController.STATE_CONNECTED && baseController != null) {
            return true;
        }
        if (getConnectionState() == BaseController.STATE_DISCONNECTED) {
            if (connectHandler.hasMessages(0x01))
                connectHandler.removeMessages(0x01);
            if (scanHandler.hasMessages(0x01))
                scanHandler.removeMessages(0x01);


            int type = bd.getProfileType();

            if (baseController != null) {
//                baseController.close();
                baseController.closeGatt();
            }

         /*   if (ScanManager.getInstance(this).isScaning()) {
                ScanManager.getInstance(this).cancelLeScan();
            }
*/
            currentDevice = bd;
            switch (type) {
                case BaseController.CMD_TYPE_W311:
                    baseController = CmdController.getInstance(this);
                    ((CmdController) baseController).setDeviceSetting(onDeviceSetting);
                    break;
                case BaseController.CMD_TYPE_W194:
                    baseController = Cmd194Controller.getInstance(this);
                    ((Cmd194Controller) baseController).setDeviceSetting(onDeviceSetting);
                    break;
                case BaseController.CMD_TYPE_W337B:
                    baseController = Cmd337BController.getInstance(this);
                    ((Cmd337BController) baseController).setDeviceSetting(onDeviceSetting);
                    break;
                case BaseController.CMD_TYPE_WEIGHTSCALE:
                    baseController = CmdWsController.getInstance(this);
                    ((CmdWsController) baseController).setDeviceSetting(onDeviceSetting);
                    break;
                case BaseController.CMD_TYPE_HEARTRATE:
                    baseController = CmdHrController.getInstance(this);
                    break;
                case BaseController.CMD_TYPE_NMC:
                    baseController = CmdNmcController.getInstance(this);
                    ((CmdNmcController) baseController).setDeviceSetting(onDeviceSetting);
                    break;
            }


            log = TAG + " connect getConnectionState:" + getConnectionState() + ",isConnectedByUser:" + isConnectedByUser + ",baseController:" + baseController + "ScanManager.getInstance(this).isScaning()" + ScanManager.getInstance(this).isScaning() + ",baseController:" + baseController + ",currentDevice:" + currentDevice + "currentDevice.getProfileType():" + currentDevice.getProfileType();
            Log.e(TAG, log);

            if (baseController == null) {
                if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                    baseController = CmdController.getInstance(getApplicationContext());
                    ((CmdController) baseController).setDeviceSetting(onDeviceSetting);
                } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                    baseController = Cmd194Controller.getInstance(getApplicationContext());
                    ((Cmd194Controller) baseController).setDeviceSetting(onDeviceSetting);
                } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W337B) {
                    baseController = Cmd337BController.getInstance(getApplicationContext());
                    ((Cmd337BController) baseController).setDeviceSetting(onDeviceSetting);
                } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_WEIGHTSCALE) {
                    baseController = CmdWsController.getInstance(getApplicationContext());
                    ((CmdWsController) baseController).setDeviceSetting(onDeviceSetting);
                } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_HEARTRATE) {
                    baseController = CmdHrController.getInstance(getApplicationContext());
                } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                    baseController = CmdNmcController.getInstance(getApplicationContext());
                    ((CmdNmcController) baseController).setDeviceSetting(onDeviceSetting);
                }
            }

            if (baseController != null && currentDevice != null) {
                baseController.setCallback(this);
                //保证没有再扫描
                // if (Build.BRAND.equals("samsung") && Build.MODEL.equals("SM-J250F")) {
               /* if (ScanManager.getInstance(this).isScaning()) {
                    ScanManager.getInstance(this).cancelLesScan(true);
                    baseController.connect(currentDevice);
                    isConnectedByUser = manual;
                } else {
                    baseController.connect(currentDevice);
                    isConnectedByUser = manual;
                }*/
              /*  } else {
                    baseController.connect(currentDevice);
                    isConnectedByUser = manual;
                }*/
                //连接超时设置
                // baseController.connect(currentDevice);
                baseController.connect(currentDevice);
               /* isConnectedByUser = manual;
                if (connectHandler.hasMessages(0x07)) {
                    connectHandler.removeMessages(0x07);
                }*/
                // connectHandler.sendEmptyMessageDelayed(0x07, 30000);

            }
        }
        return true;
    }

    private Runnable connectTimeoutTask = new Runnable() {
        @Override
        public void run() {
            int state = -1;
            if (baseController != null) {
                state = baseController.getConnectState();
            }
            String log = TAG + " connectTimeoutTask state:" + state;
            Log.e(TAG, log);
            if (baseController != null && baseController.getConnectState() != BaseController.STATE_CONNECTED) {
                baseController.connectTimeout();
            }
        }
    };

    /**
     * @param onBloodOxygen
     */
    public void setOnBloodOxygen(OnBloodOxygen onBloodOxygen) {
        if (baseController != null && (baseController instanceof Cmd337BController)) {
            Cmd337BController controller = (Cmd337BController) baseController;
            controller.setOnBloodOxygen(onBloodOxygen);
        }
    }


    /**
     * read remote device rssi
     * {@link OnDeviceSetting#readRssiCompleted(int)}
     */
    public void readRemoteRssi() {
        if (baseController != null && baseController.getConnectState() == BaseController.STATE_CONNECTED) {
            baseController.readRemoteRssi();
        }
    }

    /**
     * find device
     */
    public void goFactory() {
        if (baseController.getConnectState() == BaseController.STATE_CONNECTED && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).goFactory();
        }
    }

    public void getMessageSwith() {
        if (baseController.getConnectState() == BaseController.STATE_CONNECTED && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).getMessageSwith();
        }
    }

    public void sendMessgeSwith(boolean isSms0, boolean isQQ1, boolean isWeChat2, boolean isSkype3, boolean isfacebook4, boolean isTwitter5, boolean isLinkedin6, boolean isWhatsApp7, boolean value2Isinstagram3, boolean value2IsMessenger2, boolean value2IsCall1){
        ((CmdController) baseController).sendMessgeSwith(isSms0,isQQ1,isWeChat2,isSkype3,isfacebook4,isTwitter5,isLinkedin6,isWhatsApp7,value2Isinstagram3,value2IsMessenger2,value2IsCall1);
    }

    public void sendmusic(String text) {
        if (!TextUtils.isEmpty(text)) {
            byte[] src = text.getBytes(Charset.forName("UTF-8"));
            int len = 44;
            if (src.length >= 44) {
                len = 44;
            } else {
                len = src.length;
            }
            len=len+1;
            int pkNum = (len % 15 > 0 ? len / 15 + 1 : len / 15);
            byte[] bs = new byte[pkNum*15];
            for (int i = 0; i < bs.length; i++) {
                bs[i] = 0;
            }
            int srcLen=src.length<bs.length?src.length:bs.length;
            if(srcLen>40){
                srcLen=40;
            }
            for (int i = 0; i <srcLen; i++) {
                bs[i] = src[i];
            }

            pkNum = (pkNum < 0 ? 0 : pkNum);
            byte[][] contents = new byte[pkNum][20];
            ArrayList<byte[]> lists = new ArrayList<>();
            for (int i = 0; i < contents.length; i++) {
                byte[] value = contents[i];
                value[0] = (byte) 0xBE;
                value[1] = (byte) 0x08;
                value[2] = (byte) 0x03;
                value[3] = (byte) 0xFE;
                value[4] = (byte) (i);
                System.arraycopy(bs, 15 * i, value, 5, i < contents.length - 1 ? 15 : bs.length - 15 * (i));
                // contents[i] = value;
                lists.add(value);
            }
            if (baseController.getConnectState() == BaseController.STATE_CONNECTED && currentDevice.getProfileType() ==
                    BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).sendNotiCmdMusic(lists);
            }

        }
    }


    public void raiseHand(int type) {
        if (baseController.getConnectState() == BaseController.STATE_CONNECTED && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).raiseHand(type);
        }
    }

    /**
     * find device
     */
    public void findDevice() {
        if (baseController.getConnectState() == BaseController.STATE_CONNECTED) {
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).findDevice();
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                ((CmdNmcController) baseController).findDevice();
            }
        }
    }


    /**
     * reset device
     */
    public void reset() {
        if (baseController != null && baseController.getConnectState() == BaseController.STATE_CONNECTED) {
            baseController.reset();
        }
    }

    /**
     * send custome command
     *
     * @param cmd
     */
    public void sendCustomCmd(byte[] cmd) {
        if (baseController != null && baseController.getConnectState() == BaseController.STATE_CONNECTED) {
            baseController.sendCustomeCmd(cmd);
        }
    }

    /**
     * if connection is not BaseController.STATE_CONNECTE or sync state is STATE_SYNCING  will return false
     *
     * @return
     */
    public boolean startSyncData() {


        if (IS_DEBUG)
            Log.e(TAG, "***startSyncData*** getConnectionState():" + getConnectionState() + "baseController.getSyncState():" + baseController.getSyncState());
        if (getConnectionState() != BaseController.STATE_CONNECTED)
            return false;
        if (baseController != null && baseController.getSyncState() != BaseController.STATE_SYNCING) {
            return baseController.startSync();
        }
        return false;
    }

    /**
     * if connection is not BaseController.STATE_CONNECTE or sync state is STATE_SYNCING  will return false
     *
     * @return
     */
    public boolean startSyncData(long time) {
        if (getConnectionState() != BaseController.STATE_CONNECTED)
            return false;
        if (baseController != null && baseController.getSyncState() != BaseController.STATE_SYNCING) {
            return baseController.startSync(time);
        }
        return false;
    }

    public BaseController getCurrentController() {
        return this.baseController;
    }

    /**
     * send notification to ble device
     *
     * @param bs
     * @param index
     * @param type
     */
    public void sendNotiCmd(byte[] bs, int index, int type) {
        if (currentDevice != null && baseController != null) {
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).sendNotiCmd(new NotificationMsg(type, bs));
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                ((CmdNmcController) baseController).sendNotiCmd(new NotificationMsg(type, bs));
            }
        }
    }

    /**
     * 311 serial
     *
     * @param msg
     */
    public void sendNotiCmd(NotificationMsg msg) {
        if (currentDevice != null && baseController != null) {
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).sendNotiCmd(msg);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                ((CmdNmcController) baseController).sendNotiCmd(msg);
            }
        }
    }

    /**
     * set info remind
     */
    public void setInfoRemind(byte[] bs) {
        if (currentDevice != null && currentDevice.getProfileType() == BaseController.CMD_TYPE_W337B &&
                baseController != null) {
            ((Cmd337BController) baseController).setInfoReminder(bs);
        }
    }

    /**
     * w337B message
     *
     * @param map
     */
    public void sendNotiCmd(Map<Integer, byte[][]> map) {
        if (currentDevice != null && currentDevice.getProfileType() == BaseController.CMD_TYPE_W337B &&
                baseController != null) {
            ((Cmd337BController) baseController).sendMessage(map);
        }
    }

    /**
     * set anti state,if state == 1,you should start a thread to read remote rssi of bluetooth,
     * acording the rssi,you can judge the device lost or not.
     *
     * @param state
     */
    public void setAntiLost(int state) {
        if (currentDevice != null && baseController != null) {
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).sendAntiLost(state);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W337B) {
                ((Cmd337BController) baseController).sendAntiLost(state);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                ((CmdNmcController) baseController).sendAntiLost(state);
            }
        }
    }

    /**
     * Beat device, manually turn on the heart rate, 5 minutes after the automatic switch off
     *
     * @param open
     */
    public void setHeartRateAutoDown(boolean open) {
        if (currentDevice != null && baseController != null) {
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).setHeartRateAutoDown(open);
            }
        }
    }

    /**
     * Set whether the heart rate storage is full or not,0 will turn off the reminder, 1 will open the reminder.
     *
     * @param state
     */
    public void setSaveHeartRateNotify(int state) {
        if (currentDevice != null && baseController != null) {
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).sendSaveHeartRateNotify(state);
            }
        }
    }

    /**
     * set bluetooth broadcast name. Normal ,we judge the type of device by name,so do not call the method just if
     * you can judge the type of device by other info
     *
     * @param name    bluetooth broadcast name
     * @param switchS can modify or not
     */
    public void modifyBleBroadcastName(String name, boolean switchS) {
        if (currentDevice != null && baseController != null) {
            if (currentDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                ((CmdController) baseController).sendBleBroadcastName(name, switchS);
            } else if (currentDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                ((CmdNmcController) baseController).sendBleBroadcastName(name, switchS);
            }
        }
    }

    /**
     * CMD_TYPE_W311 CMD_TYPE_W337
     * if the device has the function that is heart rate detect
     * set heart listener
     *
     * @param listener
     */
    public void setHeartListener(OnHeartListener listener) {
        if (baseController != null && (baseController instanceof CmdController)) {
            CmdController controller = (CmdController) baseController;
            controller.setOnHeartListener(listener);
        } else if (baseController != null && (baseController instanceof Cmd337BController)) {
            Cmd337BController controller = (Cmd337BController) baseController;
            controller.setOnHeartListener(listener);
        } else if (baseController != null && (baseController instanceof CmdHrController)) {
            CmdHrController controller = (CmdHrController) baseController;
            controller.setOnHeartListener(listener);
        } else if (baseController != null && (baseController instanceof CmdNmcController)) {
            CmdNmcController controller = (CmdNmcController) baseController;
            controller.setOnHeartListener(listener);
        }
    }


    public void setHeartDescription() {
        if (baseController != null && (baseController instanceof CmdController)) {
            CmdController controller = (CmdController) baseController;
            controller.setHeartDescription();
        } else if (baseController != null && (baseController instanceof CmdNmcController)) {
            CmdNmcController controller = (CmdNmcController) baseController;
            controller.setHeartDescription();
        }
    }

    /**
     * set screen color or screen protect
     *
     * @param screenSet
     */
    public void setScreen(ScreenSet screenSet) {
        if (baseController != null && baseController instanceof Cmd194Controller) {
            ((Cmd194Controller) baseController).sendScreenSet(screenSet);
        }
    }

    /**
     * set wrist mode,left hand or right hand
     *
     * @param wristMode
     */
    public void setWristMode(WristMode wristMode) {
        if (baseController != null && (baseController instanceof CmdController)) {
            CmdController controller = (CmdController) baseController;
            controller.setWristMode(wristMode);
        } else if (baseController != null && (baseController instanceof Cmd194Controller)) {
            Cmd194Controller controller = (Cmd194Controller) baseController;
            controller.setWristMode(wristMode);
        } else if (baseController != null && (baseController instanceof CmdNmcController)) {
            CmdNmcController controller = (CmdNmcController) baseController;
            controller.setWristMode(wristMode);
        }
    }

    /**
     * @param state open or close(1,0) accessible(take photo,music control,find mobile phone)
     */
    public void setAccessibley(int state) {
        if (baseController != null && (baseController instanceof CmdController)) {

            ((CmdController) baseController).sendAccessibly(state);
        }
    }

    /**
     * CMD_TYPE_W311
     * listener the device setting that success or not
     *
     * @param deviceSetting
     */
    public void setOnDeviceSetting(OnDeviceSetting deviceSetting) {
        this.onDeviceSetting = deviceSetting;
        if (baseController != null && (baseController instanceof CmdController)) {
            CmdController controller = (CmdController) baseController;
            controller.setDeviceSetting(deviceSetting);
        } else if (baseController != null && (baseController instanceof Cmd194Controller)) {
            Cmd194Controller controller = (Cmd194Controller) baseController;
            controller.setDeviceSetting(deviceSetting);
        } else if (baseController != null && (baseController instanceof Cmd337BController)) {
            Cmd337BController controller = (Cmd337BController) baseController;
            controller.setDeviceSetting(deviceSetting);
        } else if (baseController != null && (baseController instanceof CmdNmcController)) {
            CmdNmcController controller = (CmdNmcController) baseController;
            controller.setDeviceSetting(deviceSetting);
        }
    }

    public boolean isHasSyncBaseTime() {
        if (baseController != null) {
            return baseController.isHasSyncBaseTime();
        }
        return true;
    }

    public void sendBaseTime() {
        if (baseController != null) {
            baseController.sendBaseTime();
        }
    }

    /**
     * set which interface show on device
     *
     * @param display
     */
    public void setDisplay(DisplaySet display) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).setDisplayInterface(display);
        } else if (baseController != null && (baseController instanceof Cmd194Controller) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W194) {
            ((Cmd194Controller) baseController).setDisplayInterface(display);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            ((CmdNmcController) baseController).setDisplayInterface(display);
        }
    }

    /**
     * @param list the size no more than 5
     */
    public void setAlarm(List<AlarmEntry> list) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).setAlarm(list);
        } else if (baseController != null && (baseController instanceof Cmd194Controller) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W194) {
            ((Cmd194Controller) baseController).setAlarm(list);
        } else if (baseController != null && (baseController instanceof Cmd337BController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W337B) {
            ((Cmd337BController) baseController).setAlarm(list);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            ((CmdNmcController) baseController).setAlarm(list);
        }
    }

    public void setWeatherCmd(boolean havsData, int todayWeather, int todaytempUnit, int todayhightTemp, int todaylowTemp, int todayaqi, int nextWeather, int nexttempUnit, int nexthightTemp, int nextlowTemp, int nextaqi, int afterWeather, int aftertempUnit, int afterhightTemp, int afterlowTemp, int afteryaqi) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).sendWeatherCmd(havsData, todayWeather, todaytempUnit, todayhightTemp, todaylowTemp, todayaqi, nextWeather, nexttempUnit, nexthightTemp, nextlowTemp, nextaqi, afterWeather, aftertempUnit, afterhightTemp, afterlowTemp, afteryaqi);
        }
    }


    public void setReadMemorySportData() {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).sendReadMemorySportData();
        }
    }

    public void setPointerdial(boolean enable) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).sendPointerdial(enable);
        }
    }


    public void setOpenSportMode(int type, boolean enable) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).sendOpenSportMode(type, enable);
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
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).setAlarmDescription(description, index, showDescrip);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            ((CmdNmcController) baseController).setAlarmDescription(description, index, showDescrip);
        }
    }

    /**
     * @param list max size is 3
     */
    public void setSedentaryRemind(List<SedentaryRemind> list) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).setSedintaryRemind(list);
        } else if (baseController != null && (baseController instanceof Cmd194Controller) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W194) {
            ((Cmd194Controller) baseController).setSedintaryRemind(list);
        } else if (baseController != null && (baseController instanceof Cmd337BController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W337B) {
            ((Cmd337BController) baseController).setSedintaryRemind(list);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            ((CmdNmcController) baseController).setSedintaryRemind(list);
        }
    }

    public void syncUserInfo() {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            baseController.syncUserInfo();
        } else if (baseController != null && (baseController instanceof Cmd194Controller) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W194) {
            baseController.syncUserInfo();
        } else if (baseController != null && (baseController instanceof Cmd337BController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W337B) {
            baseController.syncUserInfo();
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            baseController.syncUserInfo();
        }
    }

    /**
     * {@link CmdController#syncUserInfo()}
     *
     * @param autoSleep
     */
    public void setAutoSleep(AutoSleep autoSleep) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).setAutoSleep(autoSleep);
        } else if (baseController != null && (baseController instanceof Cmd194Controller) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W194) {
            ((Cmd194Controller) baseController).setAutoSleep(autoSleep);
        } else if (baseController != null && (baseController instanceof Cmd337BController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_W337B) {
            ((Cmd337BController) baseController).setAutoSleep(autoSleep);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            ((CmdNmcController) baseController).setAutoSleep(autoSleep);
        }
    }

    /**
     * set timing heart detect
     *
     * @param heartTimingTest
     */
    public void setHeartTimingTest(HeartTiming heartTimingTest) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            ((CmdController) baseController).setHeartTimingTest(heartTimingTest);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            ((CmdNmcController) baseController).setHeartTimingTest(heartTimingTest);
        }
    }

    /**
     * * open or close heart detect at once,only the fireware version is up 89.59
     *
     * @param isEnable
     */
    public boolean setEnableHeart(boolean isEnable) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            return ((CmdController) baseController).setEnableHeart(isEnable);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            return ((CmdNmcController) baseController).setEnableHeart(isEnable);
        }
        return false;

    }

    public boolean isSupportCmdHeart(BaseDevice device) {
        if (baseController != null && (baseController instanceof CmdController) && currentDevice.getProfileType() ==
                BaseController.CMD_TYPE_W311) {
            return ((CmdController) baseController).isSupportCmdHeart(device);
        } else if (baseController != null && (baseController instanceof CmdNmcController) && currentDevice
                .getProfileType() == BaseController.CMD_TYPE_NMC) {
            return ((CmdNmcController) baseController).isSupportCmdHeart(device);
        }
        return false;
    }

    /**
     * CMD_TYPE_W311
     *
     * @param listener
     */
    public void setOnSportListener(OnSportListener listener) {
        if (baseController != null && (baseController instanceof CmdController)) {
            CmdController controller = (CmdController) baseController;
            controller.setOnSportListener(listener);
        } else if (baseController != null && (baseController instanceof CmdNmcController)) {
            CmdNmcController controller = (CmdNmcController) baseController;
            controller.setOnSportListener(listener);
        }
    }

    public void saveOrUpdate(BaseDevice bd) {
        DbBaseDevice.getInstance(this).saveOrUpdate(bd);
    }

    /**
     * disconnect device and not remove the connection record of the deivce, it is different from unbind, see
     * {@link #unBind(BaseDevice)}
     * if you call the function to disconnect, it will not to reconnect after disconnected
     *
     * @param bd
     */
    public void disconnectDevice(BaseDevice bd) {
        if (editor == null) {
            sharedPreferences = getSharedPreferences(DEVICE_CONFIG_PATH, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }

        int state = -1;
        if (baseController != null) {
            state = baseController.getConnectState();
        }
        Log.e(TAG, " before disconnect state:" + state);
        disconnectedByUser = true;
        try {
            if (bd != null)
                Log.e(TAG, " disconnectDevice:BLE_DISC_BY_USER" + bd.getMac());
            editor.putBoolean(BLE_DISC_BY_USER + "" + bd.getMac(), true).commit();
        } catch (NullPointerException nullPointerException) {
            if (IS_DEBUG)
                Log.e(TAG, " BleService.disconnect()");
        }
        if (baseController != null) {
            if (IS_DEBUG)
                Log.e(TAG, " 主动出发 BleService.disconnect() device:" + "deviceMac:" + bd.getMac() + "deviceName:" + bd.getName() + "disconnectedByUser:" + disconnectedByUser);
            if (currentDevice != null) {
                if (IS_DEBUG)
                    Log.e(TAG, " currentDevice" + currentDevice.getMac() + currentDevice.getName());
            }
            baseController.disconnect();
        }
    }

    public void setBleDiscByUser(boolean discByUser, String connectedMac) {
        if (editor == null) {
            sharedPreferences = getSharedPreferences(DEVICE_CONFIG_PATH, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        editor.putBoolean(BLE_DISC_BY_USER + "" + connectedMac, discByUser).commit();
    }

    /**
     * set state of device when connected,up version 89.59
     *
     * @param times      between 0 and 4
     * @param isShowHook
     */
    public void setVibrateTimeAndShowHook(int times, boolean isShowHook) {
        if (baseController != null) {
            if (times < 0) {
                times = 0;
            }
            if (times > 4) {
                times = 4;
            }
            baseController.setVibrateTime(times);
            baseController.setShowHook(isShowHook);
        }
    }

    public void setOnBleServiceCallBack(OnBleServiceCallBack callBack) {
        this.bleServiceCallBack = callBack;

    }

    public boolean isDisconnectedByUser(String mac) {
        if (editor == null) {
            sharedPreferences = getSharedPreferences(DEVICE_CONFIG_PATH, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return sharedPreferences.getBoolean(BLE_DISC_BY_USER + "" + mac, true);
    }

    private int SCAN_DURATION = 7000;

    /**
     * Scan for {@link BleService#SCAN_DURATION }seconds and then stop scanning when a BluetoothLE device is found
     * then mLEScanCallback is activated
     * This will perform regular scan for custom BLE Service UUID and then filter out.
     * using class ScannerServiceParser
     * you need set import {compile 'no.nordicsemi.android.support.v18:scanner:1.0.0'} int your app gradle
     * <p>
     * Since Android 6.0 we need to obtain either Manifest.permission.ACCESS_COARSE_LOCATION or Manifest.permission
     * .ACCESS_FINE_LOCATION to be able to scan for
     * Bluetooth LE devices. This is related to beacons as proximity devices.
     * On API older than Marshmallow the following code does nothing.
     */
    public boolean startLeScan() {

        if (ScanManager.getInstance(this).isScaning()) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                if (bleServiceCallBack != null) {
                    bleServiceCallBack.requestPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION});
                }
                Log.e(TAG, "request permission:" + Manifest.permission_group.LOCATION);
                return false;
            }
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        }
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        macCache.clear();
        ScanManager.getInstance(this).setScanListener(scanCallback);
        ScanManager.getInstance(this).setScanTime(SCAN_DURATION);
        return ScanManager.getInstance(this).startLeScan();
    }


    public void sendUnreadPhoneCount(int count) {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).sendUnreadPhoneCount(count);
        } else if (baseController != null && baseController instanceof CmdNmcController) {
            ((CmdNmcController) baseController).sendUnreadPhoneCount(count);
        }
    }

    public void sendUnreadSmsCount(int count) {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).sendUnreadSmsCount(count);
        } else if (baseController != null && baseController instanceof CmdNmcController) {
            ((CmdNmcController) baseController).sendUnreadSmsCount(count);
        }
    }

    public void queryHeartHist(byte type) {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).queryHeartHistory(type);
        }
    }

    public void queryTimingHeartDetectInfo() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).queryTimingHeartDetectInfo();
        }
    }

    public void queryUserInfoFromDevice() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).queryUserInfoFromDevice();
        }
    }

    public void querySleepInfo() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).querySleepInfo();
        }
    }

    public void queryDisplayAndDoNotDisturb() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).queryDisplayAndDoNotDisturb();
        }
    }

    public void queryAlarmInfo() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).queryAlarmInfo();
        }
    }

    public void querySedentaryInfo() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).querySedentaryInfo();
        }
    }

    public void sendCommingPhoneNumber(int type, String phoneNum, String name) {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).sendPhoneNum(new CallEntry(type, phoneNum, name));
        } else if (baseController != null && baseController instanceof CmdNmcController) {
            ((CmdNmcController) baseController).sendPhoneNum(new CallEntry(type, phoneNum, name));
        } else if (baseController != null && baseController instanceof Cmd337BController) {
            ((Cmd337BController) baseController).sendPhoneNum(new CallEntry(type, phoneNum, name));
        }
    }

    public void sendCommingPhoneNumber(CallEntry entry) {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).sendPhoneNum(entry);
        } else if (baseController != null && baseController instanceof CmdNmcController) {
            ((CmdNmcController) baseController).sendPhoneNum(entry);
        } else if (baseController != null && baseController instanceof Cmd337BController) {
            ((Cmd337BController) baseController).sendPhoneNum(entry);
        }
    }


    /**
     * Stop scan if user tap Cancel button
     */
    public void cancelLeScan() {
        ScanManager.getInstance(this).cancelLeScan();
    }

    public void getTimeMetric24HourToPhone() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).getTimeMetric24HourToPhone();
        }
    }

    public void getUserInfoFromeDevice() {
        if (baseController != null && baseController instanceof CmdController) {
            ((CmdController) baseController).getUserInfoFromeDevice();
        }
    }


    public interface OnBleServiceCallBack {
        public void requestPermission(String[] permissions);
    }


    public BluetoothDevice getDeviceWithAdress(String address) {
        return address == null ? null : BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    }

    /**
     * @param tpd
     * @return if return -1 that crateBound error
     */
    public int createBond(BaseDevice tpd) {
        if (tpd != null) {
            BluetoothDevice device = getDeviceWithAdress(tpd.getMac());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED || device.getBondState() == BluetoothDevice
                    .BOND_BONDING) {
                return device.getBondState();
            } else {
                if (device.createBond()) {
                    return device.getBondState();
                } else {
                    return -1;
                }
            }

        }
        return -1;
    }

    private class MyScanManagerCallBack implements ScanManager.OnScanManagerListener {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            results = (results == null ? (new ArrayList<ScanResult>()) : results);

            Log.e(TAG,"-----扫描回调="+new Gson().toJson(results));

            ArrayList<BaseDevice> tp = new ArrayList<>();
            if (Constants.IS_DEBUG) {
                Log.e(TAG, "onBatchScanResults  size = " + results.size());
            }
            for (int i = 0; i < results.size(); i++) {
                ScanResult scanResult = results.get(i);
                BluetoothDevice device = scanResult.getDevice();
                //同一个设备，再次扫描到不做处理
                if (macCache.containsKey(device.getAddress())) {
                    continue;
                }

                String dname = scanResult.getScanRecord().getDeviceName();

                if (!TextUtils.isEmpty(dname)) {
                    dname = dname.trim();
                }

                String name = device.getName() == null ? dname : device.getName();

                //name = "FASTRACK_";

                // Log.e(TAG, "onBatchScanResults  find device dname" + dname + "tpdevice.getMac():" + device.getAddress() + "device.getName()" + device.getName() + "-------name：" + name);

                /*if (!TextUtils.isEmpty(name)) {
                    String newName = name.toUpperCase();

                    // boolean isReflex = newName.contains("REFLEX_EKIN");
                    boolean isReflex = newName.contains("FASTRACK_");
                    boolean isDFU = newName.contains("DFU");

                    boolean isTrue = true;
                    if (isReflex) {
                        isTrue = false;
                    }
                    if (isDFU) {
                        isTrue = false;
                    }

                    if (isTrue) {
                        continue;
                    }

                    // Log.e(TAG, "onBatchScanResults  REFLEX_EKIN_ find device dname" + dname + "tpdevice.getMac():" + device.getAddress() + "device.getName()" + device.getName() + "-------name：" + name);
                }
*/

                BaseDevice tpdevice = null;
                tpdevice = new BaseDevice(name, device.getAddress(),
                        scanResult.getRssi(),
                        scanResult.getScanRecord().getBytes(), scanResult.getScanRecord());

                macCache.put(device.getAddress(), device);
                tp.add(tpdevice);
                Log.e(TAG, "onBatchScanResults  find device tpdevice.getMac()" + tpdevice.getName() + "tpdevice.getMac():" + tpdevice.getMac() + "getConnectionState()" + getConnectionState());
                if (!disconnectedByUser && currentDevice != null && device.getAddress().equals(currentDevice.getMac())) {
                    cancelLeScan();
                    if (TextUtils.isEmpty(currentDevice.getName())) {
                        currentDevice.setName(device.getName());
                    }
                    //Log.e(TAG, "contains currentDevice" + currentDevice.getName() + "currentDevice.getMac():" + currentDevice.getMac() + "cancelLeScan");
                    if (scanHandler.hasMessages(0x01))
                        scanHandler.removeMessages(0x01);
                    //scanHandler.sendEmptyMessageDelayed(0x01, 2000);

                    Log.e(TAG, "onBatchScanResults  find device connect------------");
                    if (getConnectionState() == BaseController.STATE_DISCONNECTED) {
                        connectHandler.sendEmptyMessageDelayed(0x01, 200);
                    }
                }
            }
            Log.e(TAG, "onBatchScanResults  find device=" + tp.size());
            Intent intent = new Intent(ACTION_FOUND);
            intent.putExtra(EXTRA_DEVICE_LIST_FOUND, tp);
            LocalBroadcastManager.getInstance(BleService.this).sendBroadcast(intent);
        }

        @Override
        public void onScanFinished() {
            if (currentDevice != null && !macCache.containsKey(currentDevice.getMac()) && getConnectionState() == BaseController.STATE_DISCONNECTED) {
                Log.e(TAG, "onBatchScanResults  not find device connect------------isScaning:" + ScanManager.getInstance(BleService.this).isScaning());

                connect(currentDevice, true);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "errorCode = " + errorCode);
        }
    }
}
