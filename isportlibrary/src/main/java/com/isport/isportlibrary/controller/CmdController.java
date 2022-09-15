package com.isport.isportlibrary.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
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
import com.isport.isportlibrary.scanner.ScanManager;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.services.bleservice.OnHeartListener;
import com.isport.isportlibrary.tools.BleConfig;
import com.isport.isportlibrary.tools.Constants;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.isportlibrary.tools.ParserData;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
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
import java.util.logging.Logger;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * @author Created by Marcos Cheng on 2016/8/24.
 * for W311 Serial that type is {@link BaseController#CMD_TYPE_W311} profile
 */

public class CmdController extends BaseController {
    private String TAG = CmdController.class.getCanonicalName();

    private BluetoothGattCharacteristic mRealTimeCharacteristic;
    private BluetoothGattCharacteristic mSendDataCharacteristic;
    private BluetoothGattCharacteristic mReviceDataCharacteristic;

    private static CmdController sInstance;
    public OnDeviceSetting dsCallBack;
    private BluetoothGattService mGattService_HeartRate;
    private OnHeartListener onHeartListener;
    private UUID MAIN_SERVICE;
    private UUID SEND_DATA_CHAR;
    private UUID RECEIVE_DATA_CHAR;
    private UUID REALTIME_RECEIVE_DATA_CHAR;

    private static Handler handler;
    private Handler notiHandler;
    private boolean isFisrtTimeSync = true;///is the first time to sync
    private int dayCount = 0;//同步历史数据的count
    private int heartRateDayCount = 0;//同步心率数据的count
    private int startYear;
    private int startMonth;
    private int startDay;
    private int heartRateStartYear;
    private int heartRateStartMonth;
    private int heartRateStartDay;
    private long startNoti = 0;


    private List<byte[]> mCache;
    private List<byte[]> mHeartRateCache;

    private byte[] mLastCommand;
    private Object mLock = new Object();
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()
            * 2 + 1);

    private byte[] cmdFailToWrite;

    /**
     * 从接收指令回复到再次发送指令中间需要一个短时间的间隔，否者可能会导致发送指令返回false
     * 或着 发送指令不成功status == BluetoothGatt.GATT_FAILURE 或者没有无法收到设备反馈（可能是设备不能及时处理）。
     * 由于一开始设计的时候没有使用队列来存储和发送指令，而Handler本来就是用来处理消息队列的，
     * 刚好可以弥补。
     */

    private int errorTimes = 0;
    private int mCurrentYear;
    private int mCurrentMonth;
    private int mCurrentDay;
    private boolean isSyncHeartRateHistorying;


    private CmdController(Context context) {
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
                            if (getBaseDevice() != null) {
                                if (getBaseDevice().getName().contains("W523") || getBaseDevice().getName().contains
                                        ("W525")) {
                                    enableNotification(MAIN_SERVICE, SEND_DATA_CHAR,
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                } else {
                                    enableNotification(MAIN_SERVICE, SEND_DATA_CHAR,
                                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                }
                            } else {
                                enableNotification(MAIN_SERVICE, SEND_DATA_CHAR,
                                        BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            break;
                        case 2:
                            //W311 >91.45 RECEIVE_DATA_CHAR 为 notify方式  心率5分钟一个点,288个点
                            //暂不考虑版本号，直接以notify方式使能
                            if (getBaseDevice() != null) {
                                if (getBaseDevice().getName().contains("W523") || getBaseDevice().getName().contains
                                        ("W525")) {
                                    enableNotification(MAIN_SERVICE, RECEIVE_DATA_CHAR,
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                } else {
                                    //  Log.e(TAG, TAG + "0x02 two enableNotification getBaseDevice().getName()" + getBaseDevice().getName());
                                    enableNotification(MAIN_SERVICE, RECEIVE_DATA_CHAR,
                                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                    enableNotiHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            enableNotification(MAIN_SERVICE, RECEIVE_DATA_CHAR,
                                                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        }
                                    }, 1000);

                                }
                            } else {
                                //  Log.e(TAG, TAG + "0x02 two enableNotification getBaseDevice().getName()" + getBaseDevice().getName());
                                enableNotification(MAIN_SERVICE, RECEIVE_DATA_CHAR,
                                        BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                enableNotiHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        enableNotification(MAIN_SERVICE, RECEIVE_DATA_CHAR,
                                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    }
                                }, 1000);
                            }
                            break;
                        case 3:
                            if (getBaseDevice() != null) {
                                // Log.e(TAG, TAG + "0x03 one enableNotification getBaseDevice().getName()" + getBaseDevice().getName());
                                if (getBaseDevice().getName().contains("W523") || getBaseDevice().getName().contains
                                        ("W525")) {
                                    enableNotification(MAIN_SERVICE, REALTIME_RECEIVE_DATA_CHAR,
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                } else {
                                    enableNotification(MAIN_SERVICE, REALTIME_RECEIVE_DATA_CHAR,
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                }
                            } else {
                                enableNotification(MAIN_SERVICE, REALTIME_RECEIVE_DATA_CHAR,
                                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            }
                            break;
                        case 4:
                            //Log.e(TAG, TAG + "0x04 two enableNotification getBaseDevice().getName()" + getBaseDevice().getName());
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
                            if (state == BaseController.STATE_CONNECTED && !hasOpenRealTime) {
                                setRealTime();
                            } else if (isFisrtTimeSync && syncState == STATE_SYNC_COMPLETED) {
                                commandHandler.sendEmptyMessage(0x12);
                            }
                            break;
                        case 0x12:
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
                        case 0x15:
                            sendVibrateConnected();
                            break;
                        case 0x16:
                            int[] inf = (int[]) msg.obj;
                            sendSyncDay(inf[0], inf[1], inf[2]);
                            break;
                        case 0x17:
                            sendPhoneNum();
                            break;
                        case 0x18:
                            clearHeartHistory();
                            break;
                        case 0x19:
                            ppbytes = calibrate307S8948();
                            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, ppbytes);
                            break;
                        case 0x1A:
                            reset();
                            break;
                        case 0x20:
                            int[] inf1 = (int[]) msg.obj;
                            queryHeartRateHistoryByDate(inf1[0], inf1[1], inf1[2]);
                            break;
                        case 0x21:
                            int[] infs = (int[]) msg.obj;
                            heartRateStartYear = infs[0];
                            heartRateStartMonth = infs[1];
                            heartRateStartDay = infs[2];
                            heartRateDayCount = 0;
                          /*  calendar.set(Calendar.YEAR, heartRateStartYear);
                            calendar.set(Calendar.MONTH, heartRateStartMonth - 1);
                            calendar.set(Calendar.DAY_OF_MONTH, heartRateStartDay);*/
                            syncHeartRateNextDate();
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
                            // TODO: 2019/2/27 同步超时改为error
                            syncState = STATE_SYNC_ERROR;
                            if (callback != null) {
                                callback.syncState(syncState);
                            }
                            break;
                        case 0x02:
                            if (dsCallBack != null) {
                                dsCallBack.onHeartHistorySynced(OnDeviceSetting.SYNC_HEART_STATE_FAIL);
                            }
                            break;
                        case 0x03:
                            if (dsCallBack != null) {
                                dsCallBack.onHeartRateHistorySynced(OnDeviceSetting.SYNC_HEART_STATE_FAIL);
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
                        ////重发机制，设备那边可能不会回复，因为只有收到了06 09消息才会认为是连接上的，如果收到了06 09 指令的回复会移除这个消息
                        if (state != BaseController.STATE_CONNECTED) {
                            deviceInfoHandler.sendEmptyMessageDelayed(0x01, 6000);
                        }
                    } else if (msg.what == 0x02) {
                        if (!hasSyncBaseTime)
                            sendBaseTime();
                    } else if (msg.what == 0x03) {////查找服务
                        Log.e(TAG, "查找服务");
                        if (mBluetoothGatt != null) {
                            Log.e(TAG, "mBluetoothGatt查找服务");
                            toDoServiceDiscovery();
                            // mBluetoothGatt.discoverServices();
                        }
                    } else if (msg.what == 0x04) {//针对90.69.01读取版本
                        readDeviceFirm();
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


    boolean isRunOnServicesDiscovered = false;

    /**
     * 找服务
     */
    private void toDoServiceDiscovery() {
        Log.e("toDoServiceDiscovery", "mNRFBluetoothGatt!=null 去找服务");
        isRunOnServicesDiscovered = false;
        doServiceDiscovery();
    }

    Handler mDelayFindServiceHandler = new Handler(Looper.getMainLooper());
    private static int DISCOVERSERVICEDELAY = 5000;//找服务的超时监听

    private void doServiceDiscovery() {


        if (mBluetoothGatt != null) {
            Log.e("doServiceDiscovery", "DiscoverServices begin");
            mDelayFindServiceHandler.removeCallbacks(checkoutServiceRunable);
            mDelayFindServiceHandler.postDelayed(checkoutServiceRunable, DISCOVERSERVICEDELAY);
            // Thread.sleep(5000);
            if (mBluetoothGatt != null && !mBluetoothGatt.discoverServices()) {
                Log.e("doServiceDiscovery", "discoverServices return false");
                /*if (mBluetoothGatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                    Log.e("doServiceDiscovery", "BluetoothDevice.BOND_BONDING");
                }*/
            } else {
                // Log.e("doServiceDiscovery", "DiscoverServices return true");
            }
            // }
        } else
            Log.e("doServiceDiscovery", "disCoverServiceRunable，Gatt is null!");


    }


    /**
     * 5秒后检查service是否获取到
     */
    Runnable checkoutServiceRunable = new Runnable() {
        @Override
        public void run() {
            Log.e("doServiceDiscovery", "DiscoverServices Again!");
            if (!isRunOnServicesDiscovered) {
                doServiceDiscovery();
            }
        }
    };

    /**
     * 设备configure设置
     *
     * @return
     */
    private byte[] calibrate307S8948() {
        if (this.state == STATE_CONNECTED) {
            byte[] bt_cmd = new byte[20];
            DeviceInfo deviceConfig = DeviceInfo.getInstance();
            if (deviceConfig.getStatePinCode() == 1) {
                bt_cmd[0] = (byte) 0xBE;
                bt_cmd[1] = (byte) 0x03;
                bt_cmd[2] = (byte) 0x09;
                bt_cmd[3] = (byte) 0xFE;
                float version = Float.valueOf(deviceConfig.getFirmwareHighVersion() + "." + deviceConfig
                        .getFirmwareLowVersion());

                bt_cmd[4] = (byte) ((deviceConfig.getStatePhoto() << 0) | (deviceConfig.getStateLock() << 1) |
                        (deviceConfig.getStateVibrate() << 2)
                        | (deviceConfig.getStateFindPhone() << 3) | (deviceConfig.getStateHigh() << 4)
                        | (deviceConfig.getStateMusic() << 5) + (deviceConfig.getStateBleInterface() << 6)
                        | (deviceConfig.getStateProtected() << 7));

                int startIndex = 0;
                int dtype = baseDevice.getDeviceType();
                if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || deviceConfig
                        .getFirmwareHighVersion() >= 89) {
                    bt_cmd[5] = (byte) ((deviceConfig.getStateMenu()) + (deviceConfig.getState5Vibrate() << 1) +
                            (deviceConfig.getStateCallMsg() << 2) + (deviceConfig.getStateConnectVibrate() << 3) +
                            (0 << 4) + (deviceConfig.getCalIconHeart() << 5) +
                            (deviceConfig.getCalCaculateMethod() << 6));
                    bt_cmd[6] = 0;
                    if (version >= 89.59) {
                        bt_cmd[5] = (byte) ((deviceConfig.getStateMenu()) + (deviceConfig.getState5Vibrate() << 1) +
                                (deviceConfig.getStateCallMsg() << 2) +
                                (deviceConfig.getStatePinCode() << 4) + (deviceConfig.getCalIconHeart() << 5) +
                                (deviceConfig.getCalCaculateMethod() << 6));
                        bt_cmd[6] = (byte) deviceConfig.getStateSleepInterfaceAndFunc();
                    }
                    bt_cmd[7] = (byte) ((deviceConfig.getBleRealTimeBroad()) + (deviceConfig.getStateleftRight() << 2) +
                            (deviceConfig.getStateAntiLost() << 3) + (deviceConfig.getStateCallRemind() << 4) +
                            (deviceConfig.getStateMessageContent() << 5) + (deviceConfig.getStateMessageIcon() << 7));
                    startIndex = 8;
                    if (version >= 89.59) {
                        bt_cmd[8] = (byte) ((deviceConfig.getStateSyncTime()) + (deviceConfig.getStateConnectVibrate
                                () << 1) +
                                (deviceConfig.getStateShowHook() << 2));
                        startIndex = 9;
                    }
                } else {
                    bt_cmd[5] = (byte) ((deviceConfig.getStateMenu()) + (deviceConfig.getState5Vibrate() << 1) +
                            (deviceConfig.getStateCallMsg() << 2)
                            + (deviceConfig.getStateConnectVibrate() << 3));
                    startIndex = 6;
                }
                for (int i = startIndex; i <= 18; i++) {
                    bt_cmd[i] = 0;
                }
                bt_cmd[19] = (byte) 0xED;
                return bt_cmd;
            }
        }
        return null;
    }

    /**
     * 标准通道读取版本号
     */
    public void readDeviceFirm() {
        internalReadFirmareVersion();
    }

    /**
     * Set indicate
     * Indication  使能
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
            if (gatt != null) {
                return gatt.writeDescriptor(descriptor);
            } else {
                return false;
            }
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

    public static CmdController getInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (CmdController.class) {
                if (sInstance == null) {
                    sInstance = new CmdController(ctx.getApplicationContext());
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

    /**
     * 设置心率监听
     *
     * @param listener
     */
    public void setOnHeartListener(OnHeartListener listener) {
        this.onHeartListener = listener;
    }

    /**
     * 设置设备设置回调
     *
     * @param dsCallBack
     */
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


    //开关1：bit0：SMS bit1: QQ bit2: WeChat bit3: Skype bit4: facebook bit5: Twitter bit6: linkedin bit7: WhatsApp
    //开关2：bit0: instagram bit1: Messenger bit2: 来电提醒
    //设备消息提醒开关
    public void sendMessgeSwith(boolean isSms0, boolean isQQ1, boolean isWeChat2, boolean isSkype3, boolean isfacebook4, boolean isTwitter5, boolean isLinkedin6, boolean isWhatsApp7, boolean value2Isinstagram3, boolean value2IsMessenger2, boolean value2IsCall1) {
        int value1 = 0;
        int value2 = 0;

        StringBuilder stringBuilder1 = new StringBuilder();
        if (isWhatsApp7) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        if (isLinkedin6) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        if (isTwitter5) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        if (isfacebook4) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        if (isSkype3) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        if (isWeChat2) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        if (isQQ1) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        if (isSms0) {
            stringBuilder1.append("1");
        } else {
            stringBuilder1.append("0");
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("00000");
        if (value2Isinstagram3) {
            stringBuilder2.append("1");
        } else {
            stringBuilder2.append("0");
        }
        if (value2IsMessenger2) {
            stringBuilder2.append("1");
        } else {
            stringBuilder2.append("0");
        }
        if (value2IsCall1) {
            stringBuilder2.append("1");
        } else {
            stringBuilder2.append("0");
        }


        value1 = Integer.valueOf(stringBuilder1.toString(), 2);
        value2 = Integer.valueOf(stringBuilder2.toString(), 2);
        byte[] data = new byte[]{(byte) 0xbe, 0x01, 0x22, (byte) 0xFE, (byte) value1, (byte) value2};
        sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
    }

    public void getMessageSwith() {
        byte[] data = new byte[]{(byte) 0xbe, 0x01, 0x22, (byte) 0xED};
        sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
    }

    //W520设备开关

    public void raiseHand(int type) {
        byte[] data = null;
        /**
         * 关闭是2
         *          全天开启0
         *          睡觉时间段关
         */
        switch (type) {
            case 0:
                data = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) 0x01, (byte) 0x01};
                break;
            case 1:
                data = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) 0x01, (byte) 0x00};
                break;
            case 2:
                data = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
                break;

        }
        sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
    }

    /**
     * Query the start and end time of the heart rate storage
     */
    public void getHeartRateRange() {

        //需要有延时

        if (state == STATE_CONNECTED) {
            syncHandler.sendEmptyMessageDelayed(0x03, HEARTRATE_SYNC_TIMEOUT);
            byte[] value = new byte[4];
            value[0] = (byte) 0xBE;
            value[1] = (byte) 0x02;
            value[2] = (byte) 0x12;
            value[3] = (byte) 0xED;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, value);
        }
    }

    /**
     * 工厂模式
     */
    public void goFactory() {
        if (state == STATE_CONNECTED) {
            byte[] value = new byte[4];
            value[0] = (byte) 0xBE;
            value[1] = (byte) 0x06;
            value[2] = (byte) 0x30;
            value[3] = (byte) 0xED;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, value);
        }
    }

    /**
     * 使能
     *
     * @param service
     * @param charac
     * @param value
     */
    private void enableNotification(final UUID service, final UUID charac, final byte[] value) {
        enableNotiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothGatt != null) {
                    BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(service);
                    enableNotification(mBluetoothGattService, charac, value);
                }
            }
        }, 500);

    }


    /**
     * 使能
     *
     * @param mBluetoothGattService
     * @param uuid
     * @param value
     * @return
     */
    private boolean enableNotification(BluetoothGattService mBluetoothGattService, final UUID uuid, final byte[]
            value) {
        if (uuid != null && mBluetoothGattService != null && tempConnectedState == BaseController.STATE_CONNECTED) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(uuid);
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    boolean result = internalEnableNotifications(mBluetoothGattCharacteristic);
                    String log = TAG + "enableNotification  internalEnableNotifications result:" + result;
                    Log.e(TAG, log);
                    /*if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController
                            .STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableNotiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 500);
                        }
                    }*/
                } else if (value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
                    boolean result = internalEnableIndications(mBluetoothGattCharacteristic);
                    String log = TAG + "enableNotification  internalEnableIndications result:" + result;
                    Log.e(TAG, log);
                   /* if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController
                            .STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableNotiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 500);
                        }
                    }*/
                }
            }
        }
        return false;
    }

    /**
     * 获取下一个同步计步历史数据的日期
     *
     * @return
     */
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

    /**
     * 获取下一个同步心率历史数据的日期
     *
     * @return
     */
    private Calendar getHeartRateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.YEAR, heartRateStartYear);
        calendar.set(Calendar.MONTH, heartRateStartMonth - 1);
        calendar.set(Calendar.DAY_OF_MONTH, heartRateStartDay);
        calendar.add(Calendar.DAY_OF_MONTH, heartRateDayCount);
        Log.e(TAG, "***last calendar***" + calendar.get(Calendar.DAY_OF_MONTH) + ",heartRateStartYear:" + heartRateStartYear + ",heartRateStartMonth - 1:" + (heartRateStartMonth - 1) + ",heartRateStartDay:" + heartRateStartDay + ",heartRateDayCount:" + heartRateDayCount);
        StringBuilder builderTp = new StringBuilder(String.format("%04d", calendar.get(Calendar.YEAR))).append("-")
                .append(String.format("%02d", calendar.get(Calendar.MONTH) + 1)).append("-")
                .append(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));
        putString(KEY_LAST_SYNC_HEARTRATE_TIME + currentMac, builderTp.toString());
        if (IS_DEBUG)
            Log.e(TAG, "***KEY_LAST_SYNC_HEARTRATE_TIME 当前同步成功或者失败的时间点***" + builderTp.toString());
        heartRateDayCount++;
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Log.e(TAG, "***next calendar***" + calendar.get(Calendar.DAY_OF_MONTH));
        return calendar;
    }

    /**
     * 获取当前时间0点
     *
     * @return
     */
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

    /**
     * @param calendar
     * @param syncs
     * @return state error -1  try again 0  success 1
     */
    private int syncFinishOrError(Calendar calendar, int syncs) {
        int state;
        if (syncs == 0) {
            if (errorTimes >= 4) {
                if (IS_DEBUG)
                    Log.e(TAG, "***多次同步失败***");
                //说明已经进过一次了，多次则提醒并归零
                errorTimes = 0;
                state = -1;
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                if (IS_DEBUG)
                    Log.e(TAG, "***第一次同步失败***");
                errorTimes += 1;
                state = 0;
            }
        } else {
            if (IS_DEBUG)
                Log.e(TAG, "***同步成功***");
            //同步成功则归零
            errorTimes = 0;
            state = 1;
        }
        if (errorTimes == 0) {
            //同步成功或者失败dayCount清0
            dayCount = 0;
            /* save last sync time */
            StringBuilder builderTp = new StringBuilder(String.format("%04d", calendar.get(Calendar.YEAR))).append("-")
                    .append(String.format("%02d", calendar.get(Calendar.MONTH) + 1)).append("-")
                    .append(String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)));
            putString(KEY_LAST_SYNC_TIME + currentMac, builderTp.toString());
            if (IS_DEBUG)
                Log.e(TAG, "***KEY_LAST_SYNC_TIME 当前同步成功或者失败的时间点***" + builderTp.toString());
            syncState = STATE_SYNC_COMPLETED;
            if (callback != null) {
                callback.syncState(syncs == 1 ? syncState : STATE_SYNC_ERROR);
            }
        }
        return state;
    }

    /**
     * @param syncs
     * @return state error 0 success 1
     */
    private int syncHeartRateFinishOrError(int syncs) {
        if (IS_DEBUG)
            Log.e(TAG, "***同步成功***");
        //同步成功则归零
        //同步成功或者失败dayCount清0
        heartRateDayCount = 0;
        isSyncHeartRateHistorying = false;
        /* save last sync time */
        if (dsCallBack != null) {
            dsCallBack.onHeartRateHistorySynced(syncs);
        }
        return syncs;
    }

    /**
     * 对除去标准心率通道返回的指令的解析
     *
     * @param gatt
     * @param characteristic
     */
    private void handleCharacterisicChanged(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        final StringBuilder stringBuilderAll = new StringBuilder();
        if (IS_DEBUG) {
            for (byte byteChar : data) {
                stringBuilderAll.append(String.format("%02X ", byteChar));
            }
            if (logBuilder == null)
                logBuilder = new StringBuilder();
            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " ReceiverCmd " +
                    stringBuilderAll.toString()).append("\r\n");
            Log.e(TAG, "ReceiverCmd"+"data.length="+data.length+"-------" + stringBuilderAll.toString().trim()+characteristic.getUuid());

        }


        if (characteristic.getUuid().equals(SEND_DATA_CHAR)) {
            if (data != null && data.length > 0) {

                if (data.length >= 4) {

                    if ((data[0] & 0xff) == 0xDE && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x22&& (data[3] & 0xff) == 0xFB) {
                        Log.e(TAG, "dsCallBack" + dsCallBack);
                        if (dsCallBack != null) {
                            dsCallBack.getReflex2cGetMessageSwitch(data[4] & 0xff, data[5] & 0xff);
                        }
                    }
                    else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 && (data[3]
                            & 0xff) == 0xed) {//("DE 02 01 ED".equals(stringBuilder.toString().trim()))) {
                        //同步完哪一天的数据结束包,移除超时监听
                        if (syncHandler.hasMessages(0x01))
                            syncHandler.removeMessages(0x01);
                        final int dtype = baseDevice.getDeviceType();
                        cancelDataTimer();
                        Calendar calendar = getCalendar();
                        Calendar curCalendar = getCurCalendar();
                        int y = calendar.get(Calendar.YEAR);
                        int m = calendar.get(Calendar.MONTH);
                        int d = calendar.get(Calendar.DAY_OF_MONTH);
                        int isError = 1;
                        if (mCache != null) {
                            if (mCache.size() < 4) {//sync error
                                if (IS_DEBUG)
                                    Log.e(TAG, "***00***<4");
                                isError = syncFinishOrError(curCalendar, 0);
//                                isError = true;
                            } else {
                                isError = checkSum(gatt, dtype, curCalendar, isError);
                            }
                        }
                        if (IS_DEBUG) {
                            Log.e(TAG, String.format("%04d", y) + "-" +
                                    String.format("%02d", m + 1) + "-" + String.format("%02d", d));
                        }
                        mCache = null;
                        if (isError == 1) {
                            if (calendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
//                                initDataTimer();
                                // TODO: 2018/3/17 同步的下一个日期
                                int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                                        calendar.get(Calendar.DAY_OF_MONTH)};
                                Message msgTp = Message.obtain();
                                msgTp.obj = tpi;
                                msgTp.what = 0x16;
                                commandHandler.sendMessageDelayed(msgTp, 150);
                                //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
                                // .get(Calendar.DAY_OF_MONTH));
                            } else {
                                if (IS_DEBUG)
                                    Log.e(TAG, "***44***同步成功");
                                syncFinishOrError(curCalendar, 1);
                            }
                        } else if (isError == 0) {
                            if (IS_DEBUG)
                                Log.e(TAG, "***55***失败了再次同步" + mCurrentYear + "-" + mCurrentMonth + "-" + mCurrentDay
                                        + " Name=" + getBaseDevice().getName());

                            if (getBaseDevice().getName().contains("REFLEX")) {
                                syncState = STATE_SYNC_ERROR;
                                StringBuilder builderTp = new StringBuilder(String.format("%04d", mCurrentYear))
                                        .append("-")
                                        .append(String.format("%02d", mCurrentMonth)).append("-")
                                        .append(String.format("%02d", mCurrentDay));
                                putString(KEY_LAST_SYNC_TIME + currentMac, builderTp.toString());
                                startSync();
                            } else {
                                dayCount--;
//                            initDataTimer();
                                //当进入这儿是说明同步失败了，此时应该执行再次请求的操作
                                int[] tpi = new int[]{mCurrentYear, mCurrentMonth,
                                        mCurrentDay};
                                Message msgTp = Message.obtain();
                                msgTp.obj = tpi;
                                msgTp.what = 0x16;
                                commandHandler.sendMessageDelayed(msgTp, 150);
                            }
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 &&
                            (data[3] & 0xff) == 0x06) {//"DE 02 01 06".equals(stringBuilder.toString().trim())) {
                        if (syncHandler.hasMessages(0x01))
                            syncHandler.removeMessages(0x01);
                        final int dtype = baseDevice.getDeviceType();
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
                        int isError = 1;
                        if (mCache != null) {
                            if (mCache.size() < 4) {//同步出错
                                if (IS_DEBUG)
                                    Log.e(TAG, "***66***<4");
                                isError = syncFinishOrError(curCalendar, 0);
//                                isError = true;
                            } else {
                                isError = checkSum(gatt, dtype, curCalendar, isError);
                            }
                        }

                        mCache = null;
                        if (isError == 1) {
                            if (calendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
                                if (IS_DEBUG)
                                    Log.e(TAG, "***99***继续同步下一个日期");
//                                initDataTimer();
                                // TODO: 2018/3/17 查询的日期没有数据，到下一个日期查询
                                int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                                        calendar.get(Calendar.DAY_OF_MONTH)};
                                Message msgTp = Message.obtain();
                                msgTp.obj = tpi;
                                msgTp.what = 0x16;
                                commandHandler.sendMessageDelayed(msgTp, 150);
                                //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
                                // .get(Calendar.DAY_OF_MONTH));
                            } else {
                                if (IS_DEBUG)
                                    Log.e(TAG, "***99***同步成功");
                                syncFinishOrError(curCalendar, 1);
                            }
                        } else if (isError == 0) {
                            if (IS_DEBUG)
                                Log.e(TAG, "***10***失败了再次同步" + mCurrentYear + "-" + mCurrentMonth + "-" + mCurrentDay);
                            //todo 再次请求失败以后记录这个日期，记录失败时间点
                            // TODO: 2019/2/27 reflex固件需要先发送02 05达到高速模式，然后再进入同步
                            if (getBaseDevice().getName().contains("REFLEX")) {
                                syncState = STATE_SYNC_ERROR;
                                //不管失败或者成功都存储一次，对于Reflex会重新发02 05请求数据范围
                                StringBuilder builderTp = new StringBuilder(String.format("%04d", mCurrentYear))
                                        .append("-")
                                        .append(String.format("%02d", mCurrentMonth)).append("-")
                                        .append(String.format("%02d", mCurrentDay));
                                putString(KEY_LAST_SYNC_TIME + currentMac, builderTp.toString());
                                startSync();
                            } else {
                                dayCount--;
                                //当进入这儿是说明同步失败了，此时应该执行再次请求的操作
                                int[] tpi = new int[]{mCurrentYear, mCurrentMonth,
                                        mCurrentDay};
                                Message msgTp = Message.obtain();
                                msgTp.obj = tpi;
                                msgTp.what = 0x16;
                                commandHandler.sendMessageDelayed(msgTp, 150);
                            }
                        } else if (isError == -1) {
                            //多次请求失败的情况，此时已经结束了请求，记录了失败点
                            if (IS_DEBUG)
                                Log.e(TAG, "***多次请求失败的情况，此时已经结束了请求，记录了失败点***");
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x0B &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 0B ED".equals(stringBuilder.toString().trim())) {//
                        // set wrist
                        // mode
                        if (dsCallBack != null) {
                            dsCallBack.wristSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x16 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 16 ED".equals(stringBuilder.toString().trim()))
                        // {//set alarm
                        // description
                        if (dsCallBack != null) {
                            dsCallBack.alarmDescripSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x04 &&
                            (data[3] & 0xff) == 0xed) {//"DE 02 04 ED".equals(stringBuilder.toString().trim())) { //
                        // 关闭实时同步成功

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
                            if (dtype == BaseDevice.TYPE_W307H || dtype == BaseDevice.TYPE_W301H || deviceInfo
                                    .getFirmwareHighVersion() >= 89) {
                                int height = ParserData.byteArrayToInt(new byte[]{data[4], data[5]});
                                int age = ParserData.byteToInt(data[6]);
                                int gender = ParserData.byteToInt(data[7]);
                                int weight = ParserData.byteArrayToInt(new byte[]{data[8], data[9]});
                                int targeStep = ParserData.byteArrayToInt(new byte[]{data[10], data[11], data[12]});
                                int strideLen = ParserData.byteArrayToInt(new byte[]{data[13], data[14]});
                                int targetSleepHour = ParserData.byteToInt(data[15]);
                                int targetSleepMin = ParserData.byteToInt(data[16]);
                                dsCallBack.currentUserInfo(new int[]{height, age, gender, weight, targeStep,
                                        strideLen, targetSleepHour, targetSleepMin});
                            } else {
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
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x07 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 07 ED".equals(stringBuilder.toString().trim())) {//
                        // set auto
                        // sleep
                        if (dsCallBack != null) {
                            dsCallBack.autoSleepSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x07 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().contains("DE 01 07 FE")) {
                        parserSleepInfo(data);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x15 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 15 ED".equals(stringBuilder.toString().trim())) { //
                        // set auto
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
                    handleNotiResponseMusic(context, data);
                    if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x0f && (data[3]
                            & 0xff) == 0xed) {//"DE 06 0F ED".equals(stringBuilder.toString().trim())) {

                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x11 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 11 ED".equals(stringBuilder.toString().trim())) {
                        if (dsCallBack != null) {
                            dsCallBack.bleBroadcastNameModify(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x0D &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 0D ED".equals(stringBuilder.toString().trim())) {//
                        // anti lost
                        // open
                        if (dsCallBack != null) {
                            dsCallBack.antiLost(1);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x0E &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 0E ED".equals(stringBuilder.toString().trim())) {//
                        // anti lost
                        // close
                        if (dsCallBack != null) {
                            dsCallBack.antiLost(0);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x0E &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 0E ED".equals(stringBuilder.toString().trim())) {//
                        // DE-01-20-ED  Beat 手动开启心率5分钟后自动关闭心率开关回调
                        if (dsCallBack != null) {
                            dsCallBack.onSetHeartRateAutoDownSuccess();
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
                        commandHandler.sendEmptyMessage(0x14);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x02 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 02 ED".equals(stringBuilder.toString().trim())) {  设置日期
                        if (isShouldSendVibrate()) {
                            if (IS_DEBUG) {
                                Log.e(TAG, "连接发送震动指令");
                                if (logBuilder == null)
                                    logBuilder = new StringBuilder();
                                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + "连接发送震动指令" +
                                        "\r\n");
                            }
                            commandHandler.sendEmptyMessage(0x15);//150
                        } else {
                            if (IS_DEBUG) {
                                Log.e(TAG, "重连，不用发送震动指令");
                                if (logBuilder == null)
                                    logBuilder = new StringBuilder();
                                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + "重连，不用发送震动指令"
                                        + "\r\n");
                            }
                            commandHandler.sendEmptyMessage(0x11);//150
                        }
                        hasSyncBaseTime = true;
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x31 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 31 ED".equalsIgnoreCase(stringBuilder.toString().trim
                        // ())) {
                        commandHandler.sendEmptyMessage(0x11);//150
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x03 &&
                            (data[3] & 0xff) == 0xed) {//"DE 02 03 ED".equals(stringBuilder.toString().trim())) {
                        hasOpenRealTime = true;
                        commandHandler.sendEmptyMessage(0x12);//100
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xed) {//"DE 01 09 ED".equals(stringBuilder.toString().trim())) {//
                        // set alarm
                        if (dsCallBack != null) {
                            dsCallBack.alarmSetting(true);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().equals("DE 01 09 FE")) {
                        parserAlarmInfo(data);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x02 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 02 ED".equals(stringBuilder.toString().trim())) {
                        commandHandler.sendEmptyMessage(0x13);//300
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x01 &&
                            (data[3] & 0xff) == 0xed) {//"DE 06 01 ED".equals(stringBuilder.toString().trim())) {
                        callCurrentIndex = 0;
                        if (callEntryList != null && callEntryList.size() > 0) {
                            callEntryList.remove(0);
                        }
                        //发送下一条
                        commandHandler.sendEmptyMessage(0x17);//300
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x05 &&
                            (data[3] & 0xff) == 0xfb) {//stringBuilder.toString().trim().startsWith("DE 02 05 FB"))
                        // {// get
                        // history date(start - end)，如果开始时间是2015-01-01
                        if (syncHandler.hasMessages(0x01))
                            syncHandler.removeMessages(0x01);
                        byte[] starts = new byte[4];
                        System.arraycopy(data, 4, starts, 0, 4);
                        byte[] ends = new byte[4];
                        System.arraycopy(data, 8, ends, 0, 4);
                        mCache = null;
                        if (starts[0] == 0 && starts[1] == 0) {// no history data
                            // TODO: 2018/7/5 没有历史的情况，直接请求当天的数据，历史不包含当天数据
//                            syncState = STATE_SYNC_COMPLETED;
//                            mCache = null;
//                            if (callback != null) {
//                                callback.syncState(syncState);
//                            }
                            Calendar cal = Calendar.getInstance();
                            startYear = cal.get(Calendar.YEAR);
                            startMonth = cal.get(Calendar.MONTH) + 1;
                            startDay = cal.get(Calendar.DAY_OF_MONTH);
                            dayCount = 0;
                            // TODO: 2018/3/17 开始同步时间
                            int[] tpi = new int[]{startYear, startMonth, startDay};
                            Message msgTp = Message.obtain();
                            msgTp.obj = tpi;
                            msgTp.what = 0x16;
                            commandHandler.sendMessageDelayed(msgTp, 150);
                        } else {
                            startYear = starts[0] * 256 + (starts[1] & 0x00ff);
                            startMonth = starts[2];//1-12
                            startDay = starts[3];
                            if (IS_DEBUG)
                                Log.e(TAG, "start time = " + startYear + "-" + startMonth + "-" + startDay);
                            Calendar sc = Calendar.getInstance();
                            long stime = sc.getTimeInMillis() / 1000;//当前时间
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

                            String lastSyncTime = getString(KEY_LAST_SYNC_TIME + currentMac, null);///the time of
/// last sync
                            if (IS_DEBUG)
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
                            // TODO: 2018/3/17 开始同步时间
                            int[] tpi = new int[]{startYear, startMonth, startDay};
                            Message msgTp = Message.obtain();
                            msgTp.obj = tpi;
                            msgTp.what = 0x16;
                            commandHandler.sendMessageDelayed(msgTp, 150);
                            //sendSyncDay(startYear, startMonth, startDay);
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x12 &&
                            (data[3] & 0xff) == 0xfb) {
                        //自动心率存储数据
//                        DE-02-12-FB-开始年(2Bytes)-开始月(1Bytes)-开始日(Bytes)-结束年(2Bytes)-结束月(1Bytes)-结束日(1Bytes)
                        // history date(start - end)，如果开始时间是2015-01-01
                        byte[] starts = new byte[4];
                        System.arraycopy(data, 4, starts, 0, 4);
                        byte[] ends = new byte[4];
                        System.arraycopy(data, 8, ends, 0, 4);
                        heartRateStartYear = starts[0] * 256 + (starts[1] & 0x00ff);
                        heartRateStartMonth = starts[2];//1-12
                        heartRateStartDay = starts[3];
                        if (IS_DEBUG)
                            Log.e(TAG, "start time = " + heartRateStartYear + "-" + heartRateStartMonth + "-" +
                                    heartRateStartDay);
                        Calendar sc = Calendar.getInstance();
                        long stime = sc.getTimeInMillis() / 1000;//当前时间
                        sc.set(heartRateStartYear, heartRateStartMonth - 1, heartRateStartDay);
                        long sttime = sc.getTimeInMillis() / 1000;
                        //当超出15天的范围时，将时间拉到15天的时间点
                        if (stime - sttime > 3600 * 24 * 16) {
                            sc = Calendar.getInstance();
                            sc.add(Calendar.DAY_OF_MONTH, -15);
                            heartRateStartYear = sc.get(Calendar.YEAR);
                            heartRateStartMonth = sc.get(Calendar.MONTH) + 1;
                            heartRateStartDay = sc.get(Calendar.DAY_OF_MONTH);
                        }
                        //如果数据开始时间点是当天的情况,或者返回是大于当天的情况
                        if (stime - sttime <= 0) {
                            sc = Calendar.getInstance();
                            heartRateStartYear = sc.get(Calendar.YEAR);
                            heartRateStartMonth = sc.get(Calendar.MONTH) + 1;
                            heartRateStartDay = sc.get(Calendar.DAY_OF_MONTH);
                        }
                        //获取当天的YYYY-MM-DD HH:mm:ss
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1;//0 - 11
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        //查询上次同步成功后的时间点
                        String lastSyncTime = getString(KEY_LAST_SYNC_HEARTRATE_TIME + currentMac, null);///the time of
/// last sync
                        if (IS_DEBUG)
                            Log.e(TAG, "lastSyncTime = " + lastSyncTime);
                        Calendar lastCalendar = Calendar.getInstance();
                        lastCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        lastCalendar.set(Calendar.MINUTE, 0);
                        lastCalendar.set(Calendar.SECOND, 0);
                        lastCalendar.set(Calendar.MILLISECOND, 0);
                        long startTime = 0;
                        if (lastSyncTime != null) {
                            //比对上次同步的地方，从最后的同步点开始同步数据
                            String[] strs = lastSyncTime.split("-");
                            int ty = Integer.valueOf(strs[0]);
                            int tm = Integer.valueOf(strs[1]);
                            int td = Integer.valueOf(strs[2]);
                            lastCalendar.set(Calendar.YEAR, ty);
                            lastCalendar.set(Calendar.MONTH, tm - 1);
                            lastCalendar.set(Calendar.DAY_OF_MONTH, td);
                            if (lastCalendar.after(calendar)) {
                                heartRateStartYear = year;
                                heartRateStartMonth = month;
                                heartRateStartDay = day;
                            } else {
                                heartRateStartYear = ty;
                                heartRateStartMonth = tm;
                                heartRateStartDay = td;
                            }
                        }
                        calendar.set(Calendar.YEAR, heartRateStartYear);
                        calendar.set(Calendar.MONTH, heartRateStartMonth - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, heartRateStartDay);
                        startTime = calendar.getTimeInMillis();
                        heartRateDayCount = 0;
                        // TODO: 2018/3/17 开始同步时间
                        int[] tpi = new int[]{heartRateStartYear, heartRateStartMonth, heartRateStartDay};
                        Message msgTp = Message.obtain();
                        msgTp.obj = tpi;
                        msgTp.what = 0x20;
                        commandHandler.sendMessageDelayed(msgTp, 150);
                        //sendSyncDay(startYear, startMonth, startDay);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[2] & 0xff) == 0x09 &&
                            (data[3] & 0xff) == 0xfb) {//stringBuilder.toString().trim().startsWith("DE 06 09 FB"))
                        // {// 收到设备信息
                        if (deviceInfoHandler.hasMessages(0x01))
                            deviceInfoHandler.removeMessages(0x01);
                        final DeviceInfo deviceInfo = DeviceInfo.getInstance();
                        byte[] model = new byte[6];
                        System.arraycopy(data, 4, model, 0, 6);
                        String str = new String(model);
                        deviceInfo.setDeviceModel(str);
                        deviceInfo.setHardwareVersion(data[10]);
                        deviceInfo.setFirmwareHighVersion(data[11] & 0xff);
                        deviceInfo.setFirmwareLowVersion(data[12] & 0xff);
                        deviceInfo.setPowerLevel(data[17] & 0xff);
                        byte[] btttt = new byte[6];
                        btttt[0] = data[13];
                        btttt[1] = data[14];
                        btttt[2] = data[15];
                        btttt[3] = data[16];
                        btttt[4] = data[18];
                        btttt[5] = data[19];
                        deviceInfo.paraserInfo(btttt);//设置的configure信息
                        int info2 = btttt[1] & 0xff;
                        int connectVibrate = (info2 >> 3) & 1;//心率存储提醒是否开启
                        if (dsCallBack != null) {
                            dsCallBack.isSaveHeartNotify(connectVibrate);
                            dsCallBack.currentDeviceInfo(deviceInfo);
                            dsCallBack.onBatteryChanged(deviceInfo.getPowerLevel());
                        } else {
                        }
                        ////06 09指令的回复标志着是否连接成功， 收到指令后会去同步时间，这个过程只需要执行一次，所以需要加个标志，如果后面因为其它原因
                        ////发送了06 09指令，收到回复后就不用执行后续代码了
                        if (!hasSyncBaseTime) {
                            int dtype = baseDevice.getDeviceType();
                            if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || deviceInfo
                                    .getFirmwareHighVersion() >= 89) {
                                if (deviceInfo.getStateProtected() == 0) {
                                    connectSuccess(gatt);
                                    if (baseDevice != null && (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N ||
                                            baseDevice.getDeviceType() == BaseDevice.TYPE_AT200 ||
                                            baseDevice.getDeviceType() == BaseDevice.TYPE_AS97)) {
//                                        setHeartDescription();
                                    }
                                } else {////隐私保护
                                    //initLidlTimer();
                                    sendPrivacy();
                                }
                            } else {

                                if (baseDevice != null && (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N ||
                                        baseDevice.getDeviceType() == BaseDevice.TYPE_AT200 ||
                                        baseDevice.getDeviceType() == BaseDevice.TYPE_AS97)) {
                                    if (deviceInfo.getFirmwareHighVersion() < 88) {
                                        connectSuccess(gatt);
//                                        setHeartDescription();
                                    } else {
                                        if (deviceInfo.getStateProtected() == 0) {

                                            connectSuccess(gatt);
//                                            setHeartDescription();
                                        } else {////隐私保护
                                            sendPrivacy();
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "connectSuccess 11111111111111");
                                    connectSuccess(gatt);
                                }
                            }
                        }
//                        if (!hasOpenRealTime) {
//                            commandHandler.sendEmptyMessageDelayed(0x11, 600);
//                        }
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
                        // {///隐私保护返回信息
                        Log.e(TAG, "connectSuccess 22222222222222，隐私保护返回");
                        cancelLidlTimer();
                        //   setHeartDescription();
                        connectSuccess(gatt);
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 &&
                            (data[3] & 0xff) == 0x1f) {//stringBuilder.toString().trim().startsWith("DE 02 01 1F")) {
                        commandHandler.sendEmptyMessage(0x10);//300
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x18 &&
                            (data[3] & 0xff) == 0xed) {
                        //抬手开关返回
                        if (dsCallBack != null) {
                            dsCallBack.onRaiseHandSetSuccessed();
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x19 &&
                            (data[3] & 0xff) == 0xed) {
                        //整点心率监测开关返回
                        if (dsCallBack != null) {
                            dsCallBack.onHourlyRateSetSuccessed();
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x01 && (data[2] & 0xff) == 0x10 &&
                            (data[3] & 0xff) == 0xed) {
                        //校准返回
                        if (dsCallBack != null) {
                            dsCallBack.onCalibrateSuccessed();
                        }
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x11 &&
                            (data[3] & 0xff) == 0xed) {
                        //清除自动心率数据返回
                    } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x08 &&
                            (data[3] & 0xff) == 0xfe) {//stringBuilder.toString().trim().startsWith("DE 02 08 FE")) {
                        // TODO: 2018/3/12 心率返回的总数
                        int totalCount = byte2Int(data[4]) * 256 + byte2Int(data[5]);
                        if (dsCallBack != null) {
                            dsCallBack.onHeartHistoryTotalCount(totalCount);
                            if (totalCount == 0) {
                                dsCallBack.onHeartHistorySynced(OnDeviceSetting.SYNC_HEART_NODATA);
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
                    } else if ((data[0] & 0xff) == 0xDE && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x10) {
//                回复：DE-02-10-YY-MM-DD-总数(1bytes)-心率数据1-心率数据2-...心率数据48（历史数据为48个，当天按实际数据）
//                结束：DE-02-10-ED
//                备注：固件这边：心率数据一天一共有48个， 即每半小时1条数据，如果没有心率数据，固件存心率就是00，
//                固件返回的心率总数的字节指的是有效非零数据。
//                固件某一天若没有心率数据，回复指令：DE-02-10-06，app收到此指令代表无需解析这一天的心率数据。
                        Log.e(TAG, "第一通道  ReceiverCmd 心率请求返回");
                        if ((data[3] & 0xff) == 0x06) {
                            //请求的当天没有数据，直接进入下一天
                            //没有数据的情况,清空cache
                            Log.e(TAG, "第一通道  ReceiverCmd 没有心率数据");
                            heartRateDayCount = 0;
                            syncHeartRateNextDate();
                        } else if ((data[3] & 0xff) == 0xED) {
                            //数据返回完毕，进入下一天的请求
                            //移除超时监听
                            Log.e(TAG, "第一通道  ReceiverCmd 心率数据返回结束");
                            final List<byte[]> listCacheTp = new ArrayList<>();
                            listCacheTp.addAll(mHeartRateCache);
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (IS_DEBUG)
                                        Log.e(TAG, "实时数据返回");
                                    ParserData.processHeartRateHistoryData((getBaseDevice().getName().contains("BEAT") && getVersion() > 91.45f) || (getBaseDevice().getName().contains("W520") && getVersion() >= 91.63f), context, gatt.getDevice().getAddress(),
                                            listCacheTp, commandHandler);
                                }
                            });
                        } else {
                            //数据返回存储或者替换
                        }
                    }
                }
                if (dsCallBack != null) {
                    dsCallBack.customeCmdResult(data);
                }
            }
            //}
            /*};
            handler.postDelayed(mCurrentTask, 100);*/
        } else if (characteristic.getUuid().equals(RECEIVE_DATA_CHAR)) {
            if (IS_DEBUG) {
                StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append("第二通道 ReceiverCmd " +
                        stringBuilder.toString()).append
                        ("\r\n");
                Log.e(TAG, "第二通道  ReceiverCmd " + stringBuilder.toString() + " data.length =" + data.length);
            }
            if (isSyncHeartRateHistorying) {
                //是请求的心率历史数据
                if (mHeartRateCache == null) {
                    mHeartRateCache = Collections.synchronizedList(new ArrayList<byte[]>());
                }
                if ((data[0] & 0xff) == 0xDE && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x10) {
                    if ((data[3] & 0xff) == 0x06) {
                        //请求的当天没有数据，直接进入下一天
                        //没有数据的情况,清空cache
                        syncHeartRateNextDate();
                    } else if ((data[3] & 0xff) == 0xED) {
                        //同步结束
                        Log.e(TAG, "第二通道  ReceiverCmd 心率数据返回结束");
                        final List<byte[]> result = new ArrayList<>();
                        result.addAll(mHeartRateCache);
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (IS_DEBUG)
                                    Log.e(TAG, "实时数据返回");
                                ParserData.processHeartRateHistoryData((getBaseDevice().getName().contains("BEAT") && getVersion() > 91.45f) || (getBaseDevice().getName().contains("W520") && getVersion() > 91.63f), context, gatt.getDevice().getAddress(),
                                        result, commandHandler);
                            }
                        });
                    } else {
                        mHeartRateCache.add(data);
                    }
                } else {
                    mHeartRateCache.add(data);
                }
            }

            if ((data[0] & 0xff) == 0xDE && (data[1] & 0xff) == 0xFE) {
                if (heartHisrotyRecord == null) {
                    heartHisrotyRecord = new HeartHisrotyRecord();
                }
                if (heartHisrotyRecord.getHeartHistList() == null) {
                    heartHisrotyRecord.setHeartHistList(Collections.synchronizedList(new ArrayList<byte[]>()));
                }

                //heart history data
                if ((data[2] & 0xff) == 0 && (data[3] & 0xff) == 0) {//一次新记录
                    syncHandler.removeMessages(0x02);
                    syncHandler.sendEmptyMessageDelayed(0x02, 10000);
                    tempReceiveCount = 0;
                    heartHisrotyRecord.setCheckSum(0);
                    heartHisrotyRecord.setTotalCount((data[4] & 0xff) * 256 + (data[5] & 0xff));
                    byte[] tpb = calCheckSum(data, 6, heartHisrotyRecord);
                    heartHisrotyRecord.getHeartHistList().clear();
                    heartHisrotyRecord.getHeartHistList().add(tpb);
                    tempReceiveCount = data.length - 6;
                    if (dsCallBack != null) {
                        dsCallBack.onHeartSyncProgress((int) (tempReceiveCount / (heartHisrotyRecord.getTotalCount()
                                * 1.0f) * 100));
                    }
                } else {
                    syncHandler.removeMessages(0x02);
                    syncHandler.sendEmptyMessageDelayed(0x02, 10000);
                    byte[] tpb = calCheckSum(data, 4, heartHisrotyRecord);
                    heartHisrotyRecord.getHeartHistList().add(tpb);

                    byte[] bytes = new byte[(data.length - 4) + (lastReceiveH == null ? 0 : lastReceiveH.length - 4)];
                    if (lastReceiveH != null) {
                        System.arraycopy(lastReceiveH, 4, bytes, 0, lastReceiveH.length - 4);
                    }
                    System.arraycopy(data, 4, bytes, lastReceiveH == null ? 0 : lastReceiveH.length - 4, data.length
                            - 4);
                    if ((bytes[bytes.length - 3] & 0xff) == 0xFF && (bytes[bytes.length - 4] & 0xff) == 0xFF &&
                            (bytes[bytes.length - 5] & 0xff) == 0xFF && (bytes[bytes.length - 6] & 0xff) == 0xFF &&
                            ((bytes[bytes.length - 1] & 0xff) != 0xFA || (bytes[bytes.length - 2] & 0xff) != 0xFA)) {
                        heartHisrotyRecord.setCheckSum(heartHisrotyRecord.getCheckSum() - (bytes[bytes.length - 1] &
                                0xff) - (bytes[bytes.length - 2] & 0xff));

                        if (heartHisrotyRecord.getHeartHistList() != null && heartHisrotyRecord.getHeartHistList()
                                .size() > 0) {
                            final List<byte[]> listtp = new ArrayList<>();
                            listtp.addAll(heartHisrotyRecord.getHeartHistList());
                            int tpnum = 0;
                            for (int i = 0; i < listtp.size(); i++) {
                                tpnum += listtp.get(i).length;
                            }
                            int csm = (int) (heartHisrotyRecord.getCheckSum() & 0xffff);
                            int csm2 = ((bytes[bytes.length - 2] & 0xff) * 256 + (bytes[bytes.length - 1] & 0xff));
                            // TODO: 2018/3/12 有可能出现checkSum为0的情况
                            if (IS_DEBUG) {
                                if (logBuilder == null)
                                    logBuilder = new StringBuilder();
                                logBuilder.append("getTotalCount = " + heartHisrotyRecord.getTotalCount() +
                                        " tpnum = " + (tpnum - 2) + " csm = " + csm + "  " + " csm2" +
                                        " = " + csm2).append
                                        ("\r\n");
                            }
                            final HeartHisrotyRecord tpRecord = heartHisrotyRecord;
                            if (IS_DEBUG)
                                Log.e(TAG, "sync heart history completed");
                            commandHandler.sendEmptyMessage(0x18);//
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    ParserData.processHeartHistory(context.getApplicationContext(), baseDevice
                                                    .getMac(),
                                            tpRecord.getTotalCount(), listtp, dsCallBack);
                                }
                            });
//                            if (heartHisrotyRecord.getTotalCount() == tpnum - 2 && csm == csm2) {
//                                final HeartHisrotyRecord tpRecord = heartHisrotyRecord;
//                                if (IS_DEBUG)
//                                    Log.e(TAG, "sync heart history completed");
//                                commandHandler.sendEmptyMessageDelayed(0x18, 100);
//                                executorService.execute(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        ParserData.processHeartHistory(context.getApplicationContext(), baseDevice
//                                                                               .getMac(),
//                                                                       tpRecord.getTotalCount(), listtp, dsCallBack);
//                                    }
//                                });
//                            } else {
//                                heartHisrotyRecord = null;
//                                if (dsCallBack != null) {
//                                    dsCallBack.onHeartHistorySynced(OnDeviceSetting.SYNC_HEART_STATE_FAIL);
//                                }
//                            }
                            heartHisrotyRecord = null;
                        }
                    }
                }
                lastReceiveH = data;
                tempReceiveCount += data.length - 4;
                if (dsCallBack != null) {
                    dsCallBack.onHeartSyncProgress(heartHisrotyRecord == null ? 100 : (int) (tempReceiveCount /
                            (heartHisrotyRecord.getTotalCount() * 1.0f) * 100));
                }
            } else {
                if (data != null) {
                    //历史数据的返回
                    if (data.length >= 4) {

                        if (IS_DEBUG) {
                            Log.e(TAG, " getVersion():" + getVersion() + ":" + "getBaseDevice().getName():" + getBaseDevice().getName().toLowerCase());


                        }
                        //从91.61   91.70
                        if ((getVersion() >= 91.70f && getBaseDevice().getName().toLowerCase().contains
                                ("beat")) || (getVersion() >= 91.02f && getBaseDevice().getName().toLowerCase().contains
                                ("reflex")) || (getVersion() >= 91.63f && getBaseDevice().getName().toLowerCase().contains
                                ("w520"))) {
                            if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 && (data[3]
                                    & 0xff) == 0xed) {//结束指令,1分钟一个数据
                                //同步完哪一天的数据结束包,移除超时监听
                                if (syncHandler.hasMessages(0x01))
                                    syncHandler.removeMessages(0x01);
                                cancelDataTimer();
                                Calendar calendar = getCalendar();
                                Calendar curCalendar = getCurCalendar();
                                int y = calendar.get(Calendar.YEAR);
                                int m = calendar.get(Calendar.MONTH);
                                int d = calendar.get(Calendar.DAY_OF_MONTH);
                                Log.e(TAG, " ParserData.processBeatData");

                                //收到结束指令，解析数据
                                final List<byte[]> result = new ArrayList<>();
                                result.addAll(mCache);
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        ParserData.processBeatData(context, gatt.getDevice().getAddress(),
                                                callback, result, CmdController.this);
                                    }
                                });
                                if (calendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
//                                initDataTimer();
                                    // TODO: 2018/3/17 同步的下一个日期
                                    int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                                            calendar.get(Calendar.DAY_OF_MONTH)};
                                    Message msgTp = Message.obtain();
                                    msgTp.obj = tpi;
                                    msgTp.what = 0x16;
                                    commandHandler.sendMessageDelayed(msgTp, 150);
                                    //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
                                    // .get(Calendar.DAY_OF_MONTH));
                                } else {
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***44***同步成功");
                                    syncFinishOrError(curCalendar, 1);
                                }
                            } else {
                                if (mCache == null) {
                                    mCache = Collections.synchronizedList(new ArrayList<byte[]>());
                                }
                                mCache.add(data);
                                Log.e(TAG, "第二通道  mCache.add " + mCache.size());
                            }
                        } else {
                            if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 && (data[3]
                                    & 0xff) == 0xed) {//("DE 02 01 ED".equals(stringBuilder.toString().trim()))) {
                                //同步完哪一天的数据结束包,移除超时监听
                                if (syncHandler.hasMessages(0x01))
                                    syncHandler.removeMessages(0x01);
                                final int dtype = baseDevice.getDeviceType();
                                cancelDataTimer();
                                Calendar calendar = getCalendar();
                                Calendar curCalendar = getCurCalendar();
                                int y = calendar.get(Calendar.YEAR);
                                int m = calendar.get(Calendar.MONTH);
                                int d = calendar.get(Calendar.DAY_OF_MONTH);
                                int isError = 1;
                                if (mCache != null) {
                                    if (mCache.size() < 4) {//sync error
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***00***<4");
                                        isError = syncFinishOrError(curCalendar, 0);
//                                isError = true;
                                    } else {
                                        isError = checkSum(gatt, dtype, curCalendar, isError);
                                    }
                                }
                                if (IS_DEBUG) {
                                    Log.e(TAG, String.format("%04d", y) + "-" +
                                            String.format("%02d", m + 1) + "-" + String.format("%02d", d));
                                }
                                mCache = null;
                                if (isError == 1) {
                                    if (calendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
//                                initDataTimer();
                                        // TODO: 2018/3/17 同步的下一个日期
                                        int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                                                calendar.get(Calendar.DAY_OF_MONTH)};
                                        Message msgTp = Message.obtain();
                                        msgTp.obj = tpi;
                                        msgTp.what = 0x16;
                                        commandHandler.sendMessageDelayed(msgTp, 150);
                                        //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
                                        // .get(Calendar.DAY_OF_MONTH));
                                    } else {
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***44***同步成功");
                                        syncFinishOrError(curCalendar, 1);
                                    }
                                } else if (isError == 0) {
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***55***失败了再次同步" + mCurrentYear + "-" + mCurrentMonth + "-" + mCurrentDay
                                                + " Name=" + getBaseDevice().getName());
                                    if (getBaseDevice().getName().contains("REFLEX")) {
                                        syncState = STATE_SYNC_ERROR;
                                        StringBuilder builderTp = new StringBuilder(String.format("%04d", mCurrentYear))
                                                .append("-")
                                                .append(String.format("%02d", mCurrentMonth)).append("-")
                                                .append(String.format("%02d", mCurrentDay));
                                        putString(KEY_LAST_SYNC_TIME + currentMac, builderTp.toString());
                                        startSync();
                                    } else {
                                        dayCount--;
//                            initDataTimer();
                                        //当进入这儿是说明同步失败了，此时应该执行再次请求的操作
                                        int[] tpi = new int[]{mCurrentYear, mCurrentMonth,
                                                mCurrentDay};
                                        Message msgTp = Message.obtain();
                                        msgTp.obj = tpi;
                                        msgTp.what = 0x16;
                                        commandHandler.sendMessageDelayed(msgTp, 200);
                                    }
                                }
                            } else if ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 && (data[2] & 0xff) == 0x01 &&
                                    (data[3] & 0xff) == 0x06) {//"DE 02 01 06".equals(stringBuilder.toString().trim())) {
                                if (syncHandler.hasMessages(0x01))
                                    syncHandler.removeMessages(0x01);
                                final int dtype = baseDevice.getDeviceType();
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
                                int isError = 1;
                                if (mCache != null) {
                                    if (mCache.size() < 4) {//同步出错
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***66***<4");
                                        isError = syncFinishOrError(curCalendar, 0);
//                                isError = true;
                                    } else {
                                        isError = checkSum(gatt, dtype, curCalendar, isError);
                                    }
                                }

                                mCache = null;
                                if (isError == 1) {
                                    if (calendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***99***继续同步下一个日期");
//                                initDataTimer();
                                        // TODO: 2018/3/17 查询的日期没有数据，到下一个日期查询
                                        int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                                                calendar.get(Calendar.DAY_OF_MONTH)};
                                        Message msgTp = Message.obtain();
                                        msgTp.obj = tpi;
                                        msgTp.what = 0x16;
                                        commandHandler.sendMessageDelayed(msgTp, 200);
                                        //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
                                        // .get(Calendar.DAY_OF_MONTH));
                                    } else {
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***99***同步成功");
                                        syncFinishOrError(curCalendar, 1);
                                    }
                                } else if (isError == 0) {
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***10***失败了再次同步" + mCurrentYear + "-" + mCurrentMonth + "-" + mCurrentDay);
                                    //todo 再次请求失败以后记录这个日期，记录失败时间点
                                    // TODO: 2019/2/27 reflex固件需要先发送02 05达到高速模式，然后再进入同步
                                    if (getBaseDevice().getName().contains("REFLEX")) {
                                        syncState = STATE_SYNC_ERROR;
                                        //不管失败或者成功都存储一次，对于Reflex会重新发02 05请求数据范围
                                        StringBuilder builderTp = new StringBuilder(String.format("%04d", mCurrentYear))
                                                .append("-")
                                                .append(String.format("%02d", mCurrentMonth)).append("-")
                                                .append(String.format("%02d", mCurrentDay));
                                        putString(KEY_LAST_SYNC_TIME + currentMac, builderTp.toString());
                                        startSync();
                                    } else {
                                        dayCount--;
                                        //当进入这儿是说明同步失败了，此时应该执行再次请求的操作
                                        int[] tpi = new int[]{mCurrentYear, mCurrentMonth,
                                                mCurrentDay};
                                        Message msgTp = Message.obtain();
                                        msgTp.obj = tpi;
                                        msgTp.what = 0x16;
                                        commandHandler.sendMessageDelayed(msgTp, 200);
                                    }
                                } else if (isError == -1) {
                                    //多次请求失败的情况，此时已经结束了请求，记录了失败点
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***多次请求失败的情况，此时已经结束了请求，记录了失败点***");
                                }
                            } else {
                                if (mCache == null) {
                                    mCache = Collections.synchronizedList(new ArrayList<byte[]>());
                                }
                                Log.e(TAG, "第二通道  mCache.add " + mCache.size());
                                mCache.add(data);
                            }
                        }
                    }

                    if (IS_DEBUG) {
                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for (byte byteChar : data) {
                            stringBuilder.append(String.format("%02X ", byteChar));
                        }
                        Log.e(TAG, "第二通道  ReceiverCmd " + stringBuilder.toString());
                        if (logBuilder == null)
                            logBuilder = new StringBuilder();
                        logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + "第二通道 RECEIVE_DATA = " +
                                stringBuilder.toString() + "\r\n");
                    }
                }
            }
        } else if (characteristic.getUuid().equals(REALTIME_RECEIVE_DATA_CHAR)) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            if (IS_DEBUG) {

                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.e("REALTIME_RECEIVE_DATA_", "第四通道  ReceiverCmd " + stringBuilder.toString());
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " REALTIME_RECEIVE_DATA_ " +
                        "= " +
                        stringBuilder.toString() + "\r\n");
            }
            Log.e("REALTIME_RECEIVE_DATA_", "传输运动数据" + stringBuilder);
            if (HandlerCommand.handleTakePhoto(data, dsCallBack))
                return;

            if (HandlerCommand.handleFindPhone(data, dsCallBack)) {//find mobile phone
                byte[] fff = new byte[]{(byte) 0xbe, 0x06, 0x10, (byte) 0xed};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, fff);
                return;
            }
            if (HandlerCommand.handleEndPhoneController(context, data)) {
                return;
            }
            if (HandlerCommand.handleMusicController(context, data, dsCallBack))////Music controll
                return;


            //传输运动数据
            if (data != null && data.length >= 4 && ((data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x02 &&
                    (data[2] & 0xff) == 0x01 && (data[3] & 0xff) == 0xfe)) {//stringBuilder.toString().trim()
                // .startsWith("DE 02 01 FE")) {////real time data
                final byte[] temp = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    temp[i] = data[i];
                }
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (IS_DEBUG)
                            Log.e(TAG, "实时数据返回");
                        ParserData.processRealTimeData(context, gatt.getDevice().getAddress(), callback, temp);
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

    /**
     * 心率历史同步的下一个日期
     */
    private void syncHeartRateNextDate() {
        if (mHeartRateCache != null)
            mHeartRateCache.clear();
        if (syncHandler.hasMessages(0x03))
            syncHandler.removeMessages(0x03);
        Calendar nextCalendar = getHeartRateCalendar();//获取下一个要同步的时间点
        Calendar curCalendar = getCurCalendar();//获取当前日期
        int y = nextCalendar.get(Calendar.YEAR);
        int m = nextCalendar.get(Calendar.MONTH);
        int d = nextCalendar.get(Calendar.DAY_OF_MONTH);
        if (IS_DEBUG) {
            Log.e(TAG, String.format("%04d", y) + "-" +
                    String.format("%02d", m + 1) + "-" + String.format("%02d", d));
        }
        if (nextCalendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
            if (IS_DEBUG)
                Log.e(TAG, "***99***继续同步下一个日期");
            int[] tpi = new int[]{nextCalendar.get(Calendar.YEAR), nextCalendar.get(Calendar.MONTH) + 1,
                    nextCalendar.get(Calendar.DAY_OF_MONTH)};
            Message msgTp = Message.obtain();
            msgTp.obj = tpi;
            msgTp.what = 0x20;
            commandHandler.sendMessageDelayed(msgTp, 150);
            //sendSyncDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar
            // .get(Calendar.DAY_OF_MONTH));
        } else {
            if (IS_DEBUG)
                Log.e(TAG, "***99***同步成功");
            syncHeartRateFinishOrError(1);
        }
    }

    /**
     * 检查返回的byte总数是否对的上，看是否有丢包现象
     *
     * @param gatt
     * @param dtype
     * @param curCalendar
     * @param isError
     * @return
     */
    private synchronized int checkSum(final BluetoothGatt gatt, final int dtype, Calendar curCalendar, int isError) {
        List<byte[]> tempCache = new ArrayList<>();
        tempCache.addAll(mCache);
        try {
            byte[] bst = tempCache.get(2);
           /* int a1 = (bst[9] & 0xff) <<8;
            int a2 = (bst[10] & 0xff);*/
            int len = ((bst[9] & 0xff) << 8) + (bst[10] & 0xff);
            Log.e("checkSum", "checkSum len" + len);
            //第三包9 10位返回byte总数
            byte[] lastB = tempCache.get(tempCache.size() - 1);
            //判断是否请求的历史
            Calendar instance = Calendar.getInstance();
            if (mCurrentYear == instance.get(Calendar.YEAR) && mCurrentMonth == instance.get
                    (Calendar.MONTH) + 1 && mCurrentDay == instance.get(Calendar.DAY_OF_MONTH)) {
                //请求的当天数据
                //查看当前时间的5分钟index和上一个5分钟的index，排查最后一包数据的最后一包的index位置
                int hourOfDay = instance.get(Calendar.HOUR_OF_DAY);//当前的小时
                int minuteOfDay = instance.get(Calendar.MINUTE);
                //当的index是到哪儿了
                int index = hourOfDay * 12 + minuteOfDay / 5;
                if (IS_DEBUG)
                    Log.e(TAG, "***00000当前小时和分钟" + "***hourOfDay***" + hourOfDay + "***minuteOfDay***" +
                            minuteOfDay + "***index***" + index);
                //最后一包数据的最近的index
                int length = 0;
                if (index > 255) {
                    index = index - 255;
                }
                boolean isLast = false;
                for (int i = 0; i < lastB.length; i++) {
                    if ((lastB[i] & 0xff) != 0x00) {
                        isLast = true;
                        break;
                    }
                }
                if (isLast) {
                    //最后一包有数据
                    for (int i = lastB.length - 1; i >= 0; i--) {
                        //此时时间序号正好和
                        if (ParserData.byteToInt(lastB[i]) == index) {
                            //这是最后一包的index
                            if (i == lastB.length - 1) {
                                length = 4;
                            } else {
                                if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                        (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) ==
                                        0x83) {
                                    // TODO: 2018/7/2
                                    //可能当前正好等于睡眠位置，128、129、130、131，此时排查其之前的位置是否为128、129、130、131，
                                    //可能会出现index和数据相等的情况，最极限的情况是，index=多个位置，而现有的逻辑只取了最先出现的那个位置，
                                    //这样就会造成计算sum有误
                                    //说明是睡眠包
                                    length = i + 3;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                } else {
                                    //说明是计步包
                                    length = i + 3;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                }
                                break;
                            }

                        } else {

                        }
                    }
                    if (length == 0) {
                        //会存在临界点问题，手机时间已经到index 204 历史数据返回可能还在203
                        for (int i = lastB.length - 1; i >= 0; i--) {
                            if (ParserData.byteToInt(lastB[i]) == index - 1) {
                                //这是最后一包的index
                                if (i == lastB.length - 1) {
                                    length = 4;
                                    break;
                                } else {
                                    if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                            (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) ==
                                            0x83) {
                                        //说明是睡眠包
                                        length = i + 3;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                    } else {
                                        //说明是计步包
                                        length = i + 3;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                    }
                                    break;
                                }
                            }
                        }

                        if (length == 0) {
                            //适配出现两个index可能没有存储的情况
                            for (int i = lastB.length - 1; i >= 0; i--) {
                                if (ParserData.byteToInt(lastB[i]) == index - 2) {
                                    //这是最后一包的index
                                    if (i == lastB.length - 1) {
                                        length = 4;
                                        break;
                                    } else {
                                        if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                                (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) ==
                                                0x83) {
                                            //说明是睡眠包
                                            length = i + 3;
                                            if (IS_DEBUG)
                                                Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                        } else {
                                            //说明是计步包
                                            length = i + 3;
                                            if (IS_DEBUG)
                                                Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    if (length == 0) {
                        //如果没有那么只能在倒数第二包去找这个index了
                        lastB = tempCache.get(tempCache.size() - 2);
                        isLast = false;
                        for (int i = lastB.length - 1; i >= 0; i--) {
                            if (ParserData.byteToInt(lastB[i]) == index) {
                                if (i == lastB.length - 1) {
                                    length = 4;
                                    break;
                                } else {
                                    //这是最后一包的index
                                    if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                            (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) ==
                                            0x83) {
                                        //说明是睡眠包
                                        length = i + 3;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                    } else {
                                        //说明是计步包
                                        length = i + 3;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                    }
                                    break;
                                }
                            }
                        }
                        if (length == 0) {
                            //会存在临界点问题，手机时间已经到index 204 历史数据返回可能还在203
                            for (int i = lastB.length - 1; i >= 0; i--) {
                                if (ParserData.byteToInt(lastB[i]) == index - 1) {
                                    //这是最后一包的index
                                    if (i == lastB.length - 1) {
                                        length = 4;
                                        break;
                                    } else {
                                        if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                                (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) ==
                                                0x83) {
                                            //说明是睡眠包
                                            length = i + 3;
                                            if (IS_DEBUG)
                                                Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                        } else {
                                            //说明是计步包
                                            length = i + 3;
                                            if (IS_DEBUG)
                                                Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (len <= ((tempCache.size() - (isLast ? 4 : 5)) * 20) + length) {
                        if (IS_DEBUG)
                            //  Log.e(TAG, "***当天相等***");
                            Log.e(TAG, "***当天相等***" + ((tempCache.size() - (isLast ? 4 : 5)) * 20) + "******" + len + "length" + length);
                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 相等 " +
                                    " App运算得到的总byte数 " + (((tempCache.size() - (isLast ? 4 : 5)) * 20) +
                                    length)).append("\r\n");

                        }
                        final List<byte[]> listCacheTp = new ArrayList<>();
                        listCacheTp.addAll(tempCache);
                        if (gatt != null) {
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***进了11111***");
                                    ParserData.processData(context, gatt.getDevice().getAddress(),
                                            callback, listCacheTp, dtype);
                                }
                            });
                        }
                    } else {
                        //sync error
                        Log.e(TAG, ((tempCache.size() - (isLast ? 4 : 5)) * 20) + length + "***当天不相等***" + len + "---lenth" + length);
                        isError = syncFinishOrError(curCalendar, 0);
                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 不相等 " +
                                    " App运算得到的总byte数 " + (((tempCache.size() - (isLast ? 4 : 5)) * 20) +
                                    length)).append("\r\n");
                        }
//                                    isError = true;
                    }
                } else {
                    //最后一包是补的0,取倒数第二包
                    lastB = tempCache.get(tempCache.size() - 2);
                    for (int i = lastB.length - 1; i >= 0; i--) {
                        if (ParserData.byteToInt(lastB[i]) == index) {
                            //这是最后一包的index
                            if (i == lastB.length - 1) {
                                length = i + 4;
                                break;
                            } else {
                                if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                        (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) ==
                                        0x83) {
                                    //说明是睡眠包
                                    length = i + 3;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                } else {
                                    //说明是计步包
                                    length = i + 3;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                }
                            }
                            break;
                        }
                    }
                    if (length == 0) {
                        //会存在临界点问题，手机时间已经到index 204 历史数据返回可能还在203
                        for (int i = lastB.length - 1; i >= 0; i--) {
                            if (ParserData.byteToInt(lastB[i]) == index - 1) {
                                //这是最后一包的index
                                if (i == lastB.length - 1) {
                                    length = i + 4;
                                    break;
                                } else {
                                    if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                            (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) ==
                                            0x83) {
                                        //说明是睡眠包
                                        length = i + 3;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                    } else {
                                        //说明是计步包
                                        length = i + 3;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    if (len <= ((tempCache.size() - 5) * 20) + length) {
                        if (IS_DEBUG)
                            Log.e(TAG, "***当天相等***" + ((tempCache.size() - 5) * 20) + length + "******" + len + "length" + length);
                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 相等 " +
                                    " App运算得到的总byte数 " + (((tempCache.size() - 5) * 20) + length)).append
                                    ("\r\n");
                        }
                        final List<byte[]> listCacheTp = new ArrayList<>();
                        listCacheTp.addAll(tempCache);
                        if (gatt != null) {
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    ParserData.processData(context, gatt.getDevice().getAddress(),
                                            callback, listCacheTp, dtype);
                                }
                            });
                        }
                    } else {
                        //sync error
                        if (IS_DEBUG)
                            Log.e(TAG, ((tempCache.size() - 5) * 20) + length + "***当天不相等***" + len + "length" + length);
                        isError = syncFinishOrError(curCalendar, 0);
                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 不相等 " +
                                    " App运算得到的总byte数 " + (((tempCache.size() - 5) * 20) + length)).append
                                    ("\r\n");
                        }
//                                    isError = true;
                    }


                }
            } else {
                int length = 0;
                boolean isLast = false;
                for (int i = 0; i < lastB.length; i++) {
                    if ((lastB[i] & 0xff) != 0x00) {
                        isLast = true;
                        break;
                    }
                }
                if (isLast) {
                    //请求的历史数据，判断最后一把数据的结尾位置
                    for (int i = lastB.length - 1; i >= 0; i--) {
                        if ((lastB[i] & 0xff) == 0x20) {
                            Log.e(TAG, "***最后一包包含20***");
                            //这是最后一包的index
                            if (i == lastB.length - 1) {
                                length = i + 3;
                            } else {
                                if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                        (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) == 0x83) {
                                    //说明是睡眠包
                                    length = i + 2;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                } else {
                                    //说明是计步包
                                    length = i + 3;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                }
                            }
                            break;
                        }
                        if ((lastB[i] & 0xff) == 0x21) {
                            length = i + 3;
                            break;
                        }
                    }
                    if (length == 0) {
                        //倒数第一包没有index 20
                        Log.e(TAG, "***倒数第二包包含20***");
                        lastB = tempCache.get(tempCache.size() - 2);
                        isLast = false;
                        for (int i = lastB.length - 1; i >= 0; i--) {
                            if ((lastB[i] & 0xff) == 0x20) {
                                //这是最后一包的index
                                if (i == lastB.length - 1) {
                                    Log.e(TAG, "***倒数第二包包含20，最后一个index是20***");
                                    //虽然是倒数第二包，但是如果最后一个index为20说明是最后一包，
                                    // 但是并不能确定最后一包是睡眠还是计步，所以不能直接+3，应该还要判断

                                    byte[] lastC = tempCache.get(tempCache.size() - 1);
                                    if ((lastC[0] & 0xff) == 0x80 || (lastC[0] & 0xff) == 0x81 ||
                                            (lastC[0] & 0xff) == 0x82 || (lastC[0] & 0xff) == 0x83) {
                                        //说明是睡眠包
                                        length = i + 2;
                                    } else {
                                        //否则是计步包
                                        length = i + 3;
                                    }
                                } else {
                                    Log.e(TAG, "***倒数第二包包含20，最后一个index不是20，需要获取后一个index的数据来判断是睡眠还是计步***");
                                    if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                            (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) == 0x83) {
                                        //说明是睡眠包
                                        length = i + 2;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                    } else {
                                        //说明是计步包
                                        length = i + 3;
                                        if (IS_DEBUG)
                                            Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                    }
                                }
                                break;
                            }

                            if ((lastB[i] & 0xff) == 0x21) {
                                length = i + 3;
                                break;
                            }
                        }
                    }
                    if (len <= ((tempCache.size() - (isLast ? 4 : 5)) * 20 + length)) {
                        if (IS_DEBUG)
                            //  Log.e(TAG, "***历史相等***" + len);
                            Log.e(TAG, "***历史相等***" + (tempCache.size() - (isLast ? 4 : 5)) * 20 + length + "******" + len + "length" + length);

                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 相等 " +
                                    " App运算得到的总byte数 " + ((tempCache.size() - (isLast ? 4 : 5)) * 20 +
                                    length)).append("\r\n");
                        }
                        final List<byte[]> listCacheTp = new ArrayList<>();
                        listCacheTp.addAll(tempCache);
                        if (gatt != null) {
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    ParserData.processData(context, gatt.getDevice().getAddress(),
                                            callback, listCacheTp, dtype);
                                }
                            });
                        }
                    } else {
                        //sync error
                        if (IS_DEBUG)
                            Log.e(TAG, ((tempCache.size() - 4) * 20 + length) + "***历史不相等000***" + len);
                        isError = syncFinishOrError(curCalendar, 0);
                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 不相等 " +
                                    " App运算得到的总byte数 " + ((tempCache.size() - 4) * 20 + length)).append
                                    ("\r\n");
                        }
//                                    isError = true;
                    }
                } else {
                    lastB = tempCache.get(tempCache.size() - 2);
                    //请求的历史数据，判断最后一把数据的结尾位置
                    for (int i = lastB.length - 1; i >= 0; i--) {
                        if ((lastB[i] & 0xff) == 0x20) {
                            //这是最后一包的index
                            if (i == lastB.length - 1) {
                                length = i + 3;
                            } else {
                                if ((lastB[i + 1] & 0xff) == 0x80 || (lastB[i + 1] & 0xff) == 0x81 ||
                                        (lastB[i + 1] & 0xff) == 0x82 || (lastB[i + 1] & 0xff) == 0x83) {
                                    //说明是睡眠包
                                    length = i + 2;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是睡眠包，最后一包有数据的长度为***" + length);
                                } else {
                                    //说明是计步包
                                    length = i + 3;
                                    if (IS_DEBUG)
                                        Log.e(TAG, "***是计步包，最后一包有数据的长度为***" + length);
                                }
                            }
                            break;
                        }
                        if ((lastB[i] & 0xff) == 0x21) {
                            length = i + 3;
                        }
                    }
                    if (len <= ((tempCache.size() - 5) * 20 + length)) {
                        if (IS_DEBUG)
                            Log.e(TAG, "***历史相等***" + len);
                        Log.e(TAG, "***历史相等***" + (tempCache.size() - 5) * 20 + length + "******" + len + "length" + length);

                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 相等 " +
                                    " App运算得到的总byte数 " + ((tempCache.size() - 5) * 20 + length)).append
                                    ("\r\n");
                        }
                        final List<byte[]> listCacheTp = new ArrayList<>();
                        listCacheTp.addAll(tempCache);
                        if (gatt != null) {
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    ParserData.processData(context, gatt.getDevice().getAddress(),
                                            callback, listCacheTp, dtype);
                                }
                            });
                        }
                    } else {
                        //sync error
                        if (IS_DEBUG)
                            Log.e(TAG, ((tempCache.size() - 5) * 20 + length) + "***历史不相等1111***" + len);
                        isError = syncFinishOrError(curCalendar, 0);
                        if (IS_DEBUG) {
                            if (logBuilder == null)
                                logBuilder = new StringBuilder();
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " 设备第三包返回总byte数 " +
                                    len +
                                    " 不相等 " +
                                    " App运算得到的总byte数 " + ((tempCache.size() - 5) * 20 + length)).append
                                    ("\r\n");
                        }
//                                    isError = true;
                    }
                }
            }

        } catch (Exception e) {
            if (IS_DEBUG)
                Log.e(TAG, e.toString());
            isError = syncFinishOrError(curCalendar, 0);
        } finally {
            return isError;
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

    /**
     * 发送隐私保护指令
     */
    private void sendPrivacy() {
        ///本机蓝牙地址
        BluetoothAdapter.getDefaultAdapter().getAddress();
                        /*BE+01+05+FE+启用开关（1byte）+4位配对码（2byte）
                        +手机的MAC（6byte）*/
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
//        BE 01 05 FE FF 00 00 98 E7 F5 A1 D7 4A
        // String maccccc = getBtAddressViaReflection();
        String maccccc = null;
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
        Log.e(TAG, "connectSuccess 000000000000 隐私保护" + maccccc);
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
            int dtype = baseDevice.getDeviceType();
            if (dtype == BaseDevice.TYPE_W301N || dtype == BaseDevice.TYPE_W240B || dtype == BaseDevice.TYPE_W307N ||
                    dtype == BaseDevice.TYPE_W285B
                    || dtype == BaseDevice.TYPE_W240N) {
                byte[] time = new byte[]{(byte) 0xbe, 0x06, 0x03, (byte) 0xfe, (byte) count};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
            }
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
            int dtype = baseDevice.getDeviceType();
            if (dtype == BaseDevice.TYPE_W301N || dtype == BaseDevice.TYPE_W240B || dtype == BaseDevice.TYPE_W307N ||
                    dtype == BaseDevice.TYPE_W285B
                    || dtype == BaseDevice.TYPE_W240N) {
                byte[] time = new byte[]{(byte) 0xbe, 0x06, 0x04, (byte) 0xfe, (byte) count};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, time);
            }
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

    /**
     * 发送联系人
     *
     * @param comming_phone
     */
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
        /** APP 先发送 来电号码 的指令，接收到 手环回复 指令后，APP 需要再发送 联系人姓名 的指令，设
         * 备接收到 回复 指令后才震动马达。若无联系人的名字时，名字部分全部以 FF
         * 代替。设备优先显示联系人的名字，若无联系人的名字时，才显示电话号码。
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
        } else {
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
            } else {
            }
        } else {
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

    /**
     * 同步的具体指令
     *
     * @param year
     * @param month
     * @param day
     */
    public void sendSyncDay(int year, int month, int day) {
        if (state == STATE_CONNECTED) {
            mCache = null;
            mCurrentYear = year;
            mCurrentMonth = month;
            mCurrentDay = day;
            byte[] data = new byte[10];
            data[0] = (byte) 0xbe;
            data[1] = 0x02;
            data[2] = 0x01;
            data[3] = (byte) 0xfe;
            data[4] = (byte) (year >> 8);
            data[5] = (byte) year;
            data[6] = (byte) month;
            data[7] = (byte) day;
            data[8] = 0;
            data[9] = 0;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
            //同步哪一天时，发送超时
            syncHandler.sendEmptyMessageDelayed(0x01, DEFAULT_SYNC_TIMEOUT);
        }
    }

    /**
     * Query the heart rate stored data for the specified date
     * BE-02-10-FE-YY(年2byte)-MM(月)-DD(日)-序号  (序号表示从第几个开始，APP默认发00）
     */
    public void queryHeartRateHistoryByDate(int year, int month, int day) {


        if (mHeartRateCache != null)
            mHeartRateCache.clear();
        if (syncHandler.hasMessages(0x03))
            syncHandler.removeMessages(0x03);

        if (state == STATE_CONNECTED) {
            isSyncHeartRateHistorying = true;

            heartRateStartYear = year;
            heartRateStartMonth = month;
            heartRateStartDay = day;
            /*int[] tpi = new int[]{year, month,
                    day};*/
           /* Message msgTp = Message.obtain();
            msgTp.obj = tpi;
            msgTp.what = 0x20;
            commandHandler.sendMessageDelayed(msgTp, 150);*/
            syncHandler.sendEmptyMessageDelayed(0x03, HEARTRATE_SYNC_TIMEOUT);
            byte[] value = new byte[9];
            value[0] = (byte) 0xBE;
            value[1] = (byte) 0x02;
            value[2] = (byte) 0x10;
            value[3] = (byte) 0xFE;
            value[4] = (byte) (year >> 8);
            value[5] = (byte) year;
            value[6] = (byte) month;
            value[7] = (byte) day;
            value[8] = 0x00;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, value);

            //syncHandler.sendEmptyMessageDelayed(0x03, HEARTRATE_SYNC_TIMEOUT);

           /* byte[] value = new byte[9];
            value[0] = (byte) 0xBE;
            value[1] = (byte) 0x02;
            value[2] = (byte) 0x10;
            value[3] = (byte) 0xFE;
            value[4] = (byte) (year >> 8);
            value[5] = (byte) year;
            value[6] = (byte) month;
            value[7] = (byte) day;
            value[8] = 0x00;

            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, value);*/
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
        /*if(!hasSyncBaseTime){
            sendBaseTime();
        }*/
        if (state != STATE_CONNECTED)
            return false;
        if (syncState != STATE_SYNCING) {
            SYNC_TIMEOUT = sync_time;
            if (!sendCmdSync()) {
                syncHandler.sendEmptyMessageDelayed(0x01, SYNC_TIMEOUT);
                syncState = STATE_SYNC_COMPLETED;
                if (callback != null) {
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
        /*if(!hasSyncBaseTime){
            sendBaseTime();
        }*/


        if (state != STATE_CONNECTED)
            return false;
        if (syncState != STATE_SYNCING) {
            if (!sendCmdSync()) {
                syncHandler.sendEmptyMessageDelayed(0x01, DEFAULT_SYNC_TIMEOUT);
                syncState = STATE_SYNC_COMPLETED;
                if (callback != null) {
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
        if (IS_DEBUG)
            Log.e(TAG, "***startSyncData*** state:" + state + "syncState:" + syncState);
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
     * if version that can get from ({@link DeviceInfo#getFirmwareHighVersion()} + "."
     * +{@link DeviceInfo#getFirmwareLowVersion()})  is up 89.33,
     * you do not call the method, because it will make it different
     * you call it only your device support to control phone to take photo, play music and find phone     *
     *
     * @param state open or close(1,0) accessible(take photo,music control,find mobile phone)
     */
    public void sendAccessibly(int state) {
        if (this.state == STATE_CONNECTED && baseDevice != null) {
            DeviceInfo deviceInfo = DeviceInfo.getInstance();
            int dtype = baseDevice.getDeviceType();
            if (dtype == BaseDevice.TYPE_W307H || dtype == BaseDevice.TYPE_W301H || deviceInfo.getFirmwareHighVersion
                    () >= 89) {
                send88AccessCmd(state);
            } else {
                if (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N) {
                    if (deviceInfo.getFirmwareHighVersion() < 88) {
                        sendLow88AccessCmd(state);
                    } else {
                        send88AccessCmd(state);
                    }
                } else {
                    sendLow88AccessCmd(state);
                }
            }
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
     * you can call this function to set a Accessibly that your version is equal and up 88 or lower than 89.
     * Of course,you can call {@link #sendAccessibly} instead;
     * however,if the version of you device firewarm is equal or up 89 ,you can use the function that is
     * {@link #setInfoForDevice} instead
     * <p>
     * if version that can get from ({@link DeviceInfo#getFirmwareHighVersion()} + "."
     * +{@link DeviceInfo#getFirmwareLowVersion()})  is up 89.33,
     * you do not call the method, because it will make it different
     * you call it only your device support to control phone to take photo, play music and find phone
     *
     * @param state
     */
    public void send88AccessCmd(int state) {
        if (this.state == STATE_CONNECTED) {
            byte[] bt_cmd = new byte[20];
            DeviceInfo deviceConfig = DeviceInfo.getInstance();
            if (deviceConfig.getStatePhoto() == 1 && deviceConfig.getStateMusic() == 1 && deviceConfig
                    .getStateFindPhone() == 1) {
                boolean isOpen_access = (state == 1);
                bt_cmd[0] = (byte) 0xBE;
                bt_cmd[1] = (byte) 0x03;
                bt_cmd[2] = (byte) 0x09;
                bt_cmd[3] = (byte) 0xFE;
                float version = Float.valueOf(deviceConfig.getFirmwareHighVersion() + "." + deviceConfig
                        .getFirmwareLowVersion());
                if (isOpen_access) {
                    bt_cmd[4] = (byte) (1 | (deviceConfig.getStateLock() << 1) | (deviceConfig.getStateVibrate() << 2)
                            | (1 << 3) | (deviceConfig.getStateHigh() << 4)
                            | (1 << 5) + (deviceConfig.getStateBleInterface() << 6)
                            | (deviceConfig.getStateProtected() << 7));
                } else {
                    bt_cmd[4] = (byte) ((deviceConfig.getStateLock() << 1) | (deviceConfig.getStateVibrate() << 2)
                            | (deviceConfig.getStateHigh() << 4)
                            | (deviceConfig.getStateBleInterface() << 6)
                            | (deviceConfig.getStateProtected() << 7));
                }
                int startIndex = 0;
                int dtype = baseDevice.getDeviceType();
                if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || deviceConfig
                        .getFirmwareHighVersion() >= 89) {
                    bt_cmd[5] = (byte) ((deviceConfig.getStateMenu()) + (deviceConfig.getState5Vibrate() << 1) +
                            (deviceConfig.getStateCallMsg() << 2) + (deviceConfig.getStateConnectVibrate() << 3) +
                            (deviceConfig.getStatePinCode() << 4) + (deviceConfig.getCalIconHeart() << 5) +
                            (deviceConfig.getCalCaculateMethod() << 6));
                    bt_cmd[6] = 0;
                    if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || version >= 89.059) {
                        bt_cmd[5] = (byte) ((deviceConfig.getStateMenu()) + (deviceConfig.getState5Vibrate() << 1) +
                                (deviceConfig.getStateCallMsg() << 2) +
                                (deviceConfig.getStatePinCode() << 4) + (deviceConfig.getCalIconHeart() << 5) +
                                (deviceConfig.getCalCaculateMethod() << 6));
                        bt_cmd[6] = (byte) deviceConfig.getStateSleepInterfaceAndFunc();
                    }
                    bt_cmd[7] = (byte) ((deviceConfig.getBleRealTimeBroad()) + (deviceConfig.getStateleftRight() << 2) +
                            (deviceConfig.getStateAntiLost() << 3) + (deviceConfig.getStateCallRemind() << 4) +
                            (deviceConfig.getStateMessageContent() << 5) + (deviceConfig.getStateMessageIcon() << 7));
                    startIndex = 8;
                    if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || version >= 89.059) {
                        bt_cmd[8] = (byte) ((deviceConfig.getStateSyncTime()) + (deviceConfig.getStateConnectVibrate
                                () << 1) +
                                (deviceConfig.getStateShowHook() << 2));
                        startIndex = 9;
                    }
                } else {
                    bt_cmd[5] = (byte) ((deviceConfig.getStateMenu()) + (deviceConfig.getState5Vibrate() << 1) +
                            (deviceConfig.getStateCallMsg() << 2)
                            + (deviceConfig.getStateConnectVibrate() << 3));
                    startIndex = 6;
                }
                for (int i = startIndex; i <= 18; i++) {
                    bt_cmd[i] = 0;
                }
                bt_cmd[19] = (byte) 0xED;
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, bt_cmd);
            }
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
     * how long to vibrate when connected,add on version 89.59 and up
     */
    public void sendVibrateConnected() {
        if (this.state == STATE_CONNECTED) {
            if (IS_DEBUG)
                Log.e(TAG, "重连发送震动指令" + vibrateTime);
            byte[] data = new byte[]{(byte) 0xbe, 0x06, 0x31, (byte) vibrateTime, (byte) (isShowHook ? 1 : 0),
                    (byte)
                            0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * 判断是否发送震动指令
     *
     * @return
     */
    private boolean isShouldSendVibrate() {
        int dtype = -1;
        if (baseDevice != null) {
            dtype = baseDevice.getDeviceType();
        }
        if (this.state == STATE_CONNECTED) {
            float version = getVersion();
            if (vibrateTime > 0) {
                if (dtype == BaseDevice.TYPE_W307S_SPACE || dtype == BaseDevice.TYPE_W307H || dtype == BaseDevice
                        .TYPE_W301H || ((dtype == BaseDevice.TYPE_W301S
                        || dtype == BaseDevice.TYPE_W307S) && version >= 89.37) ||
                        ((dtype == BaseDevice.TYPE_AS97) && version >= 89.00) ||
                        ((dtype == BaseDevice.TYPE_AT100) && version >= 89.41) ||
                        ((dtype == BaseDevice.TYPE_AT200) && version >= 89.79) ||
                        ((dtype == BaseDevice.TYPE_SAS80) && version >= 89.37) ||
                        ((dtype != BaseDevice.TYPE_AS97 && dtype != BaseDevice.TYPE_AT100 && dtype != BaseDevice
                                .TYPE_AT200 &&
                                dtype != BaseDevice.TYPE_SAS80 && dtype != BaseDevice.TYPE_W301S && dtype != BaseDevice
                                .TYPE_W307S && dtype != BaseDevice.TYPE_W307S_SPACE) && version >= 89.59)
                ) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
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
     * Beat device, manually turn on the heart rate, 5 minutes after the automatic switch off
     * 手动开心率5分钟自动关心率指令:（开启后心率开启5分钟后自动关闭）
     * 格式：BE-01-20-FE-开关 (1：使能自动关心率); 0：不使能自动关心率)
     * 回复：DE-01-20-ED
     */
    public void setHeartRateAutoDown(boolean open) {
        if (this.state == STATE_CONNECTED) {
            byte[] data = new byte[]{(byte) 0xbe, 0x01, 0x20, (byte) 0xFE, (byte) (open ? 1 : 0)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * Set whether the heart rate storage is full or not
     *
     * @param st
     */
    public void sendSaveHeartRateNotify(int st) {
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

    /**
     * 连接成功
     */
    private void connectSuccess() {
        state = BaseController.STATE_CONNECTED;
    }

    /**
     * 连接成功的回调
     *
     * @param g
     */
    private void connectSuccess(BluetoothGatt g) {
        state = BaseController.STATE_CONNECTED;
//        callCurrentIndex=0;
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        if (baseDevice != null) {
            if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307S && getVersion() == 89.48f &&
                    deviceInfo.getStatePinCode() == 1) {
                commandHandler.sendEmptyMessageDelayed(0x19, 150);
                deviceInfoHandler.sendEmptyMessageDelayed(0x02, 3000);
                // TODO: 2018/7/2 89.48f 版本功能保留
//            } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307S_SPACE &&
//                    deviceInfo.getStatePinCode() == 1) {
//                commandHandler.sendEmptyMessageDelayed(0x19, 150);
            } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307H &&
                    deviceInfo.getStatePinCode() == 1) {
                commandHandler.sendEmptyMessageDelayed(0x19, 150);
                deviceInfoHandler.sendEmptyMessageDelayed(0x02, 3000);
            } else {
                deviceInfoHandler.sendEmptyMessage(0x02);
            }
        } else {
            deviceInfoHandler.sendEmptyMessage(0x02);
        }
        if (callback != null) {
            reconnectCount = 0;
            callback.connectState(g == null ? null : g.getDevice(), state);
        }
        /*if (IS_DEBUG) {
            if (BaseController.logBuilder == null) {
                BaseController.logBuilder = new StringBuilder();
            }
            BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connect " +
                    "success\r\n");
        }*/

    }

    /**
     * to reset device
     */
    @Override
    public void reset() {
        super.reset();
        if (state == STATE_CONNECTED) {
            //byte[] bs = new byte[]{(byte) 0xff, (byte) 0xfa, (byte) 0xfc, (byte) 0xf7, 0x00, 0x01, 0x02, 0x07,
            // 0x55, 0x33, 0x66, 0x31, 0x18, (byte) 0x89, 0x60, 0x00};
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
            if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || getVersion() >= 89.033) {
                data = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x03, (byte) 0xfe, (byte) ((height & 0xffff) >> 8)
                        , (byte) height, (byte) getAge(), (byte) userInfo.getGender(),
                        (byte) (weight >> 8), (byte) weight, (byte) (targetSteps >> 16),
                        (byte) (targetSteps >> 8), (byte) targetSteps, (byte) (strideLength >> 8), (byte)
                        strideLength, (byte) sleepHour, (byte) sleepMin};
            } else {
                data = new byte[]{(byte) 0xbe, (byte) 0x01, (byte) 0x03, (byte) 0xfe, (byte) (year >> 8), (byte)
                        year, (byte) month, (byte) day, (byte) (weight >> 8), (byte) weight, (byte) (targetSteps >> 16),
                        (byte) (targetSteps >> 8), (byte) targetSteps, (byte) (strideLength >> 8), (byte)
                        strideLength, (byte) sleepHour, (byte) sleepMin};
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * 获取年龄
     *
     * @return
     */
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
     * set info for device
     *
     * @param deviceInfo the info that will be setted for device that version is equal to 88 or up
     */
    public void setInfoForDevice(DeviceInfo deviceInfo) {
        if (this.state == STATE_CONNECTED && deviceInfo != null && deviceInfo.getFirmwareHighVersion() >= 88) {
            byte info1 = (byte) ((deviceInfo.getStatePhoto()) + (deviceInfo.getStateLock() << 1) + (deviceInfo
                    .getStateVibrate() << 2) +
                    (deviceInfo.getStateFindPhone() << 3) + (deviceInfo.getStateHigh() << 4) + (deviceInfo
                    .getStateMusic() << 5) +
                    (deviceInfo.getStateBleInterface() << 6) + (deviceInfo.getStateProtected() << 7));

            int dtype = baseDevice.getDeviceType();
            String name = baseDevice.getName();
            byte info2 = 0;
            byte info3 = 0;
            byte info4 = 0;
            if (dtype == BaseDevice.TYPE_W301H && name.contains("W311N_") && deviceInfo.getFirmwareHighVersion
                    () >= 90.59) {
                //来的什么设备信息则发什么设备信息，针对设置心率存储满提醒
                Log.e("connectVibrate", "W311N_ 是否开启心率存储提醒 == " + deviceInfo.getStateConnectVibrate());
                info2 = (byte) ((deviceInfo.getStateMenu()) + (deviceInfo.getState5Vibrate() << 1) +
                        (deviceInfo.getStateCallMsg() << 2) + (deviceInfo.getStateConnectVibrate() << 3) +
                        (deviceInfo.getStatePinCode() << 4) + (deviceInfo.getCalIconHeart() << 5) +
                        (deviceInfo.getCalCaculateMethod() << 6));
                info3 = (byte) ((deviceInfo.getStateSleepInterfaceAndFunc()));
                info4 = (byte) ((deviceInfo.getBleRealTimeBroad()) + (deviceInfo.getStateleftRight() << 2) +
                        (deviceInfo.getStateAntiLost() << 3) + (deviceInfo.getStateCallRemind() << 4) +
                        (deviceInfo.getStateMessageContent() << 5) + (deviceInfo.getStateMessageIcon() << 7));
            } else if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || deviceInfo
                    .getFirmwareHighVersion
                            () >= 89) {
                info2 = (byte) ((deviceInfo.getStateMenu()) + (deviceInfo.getState5Vibrate() << 1) +
                        (deviceInfo.getStateCallMsg() << 2) + (deviceInfo.getStateConnectVibrate() << 3) +
                        (deviceInfo.getStatePinCode() << 4) + (deviceInfo.getCalIconHeart() << 5) +
                        (deviceInfo.getCalCaculateMethod() << 6));
                info3 = (byte) ((deviceInfo.getStateSleepInterfaceAndFunc()));
                info4 = (byte) ((deviceInfo.getBleRealTimeBroad()) + (deviceInfo.getStateleftRight() << 2) +
                        (deviceInfo.getStateAntiLost() << 3) + (deviceInfo.getStateCallRemind() << 4) +
                        (deviceInfo.getStateMessageContent() << 5) + (deviceInfo.getStateMessageIcon() << 7));
            } else {
                info2 = (byte) ((deviceInfo.getStateMenu()) + (deviceInfo.getState5Vibrate() << 1) + (deviceInfo
                        .getStateCallMsg() << 2)
                        + (deviceInfo.getStateConnectVibrate() << 3));
            }
            byte[] bt_cmd = new byte[20];
            bt_cmd[0] = (byte) 0xBE;
            bt_cmd[1] = (byte) 0x03;
            bt_cmd[2] = (byte) 0x09;
            bt_cmd[3] = (byte) 0xFE;
            bt_cmd[4] = info1;
            bt_cmd[5] = info2;
            bt_cmd[6] = info3;
            bt_cmd[7] = info4;
            for (int i = 8; i <= 18; i++) {
                bt_cmd[i] = 0;
            }
            bt_cmd[19] = (byte) 0xED;
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, bt_cmd);
        }
    }


    /**
     * query heart timing detect info,only the fireware version is up 89.59
     */
    public void queryTimingHeartDetectInfo() {
        float version = getVersion();
        if (state == STATE_CONNECTED && version >= 89.59 && (baseDevice != null &&
                (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice.getDeviceType() == BaseDevice
                        .TYPE_AT200 ||
                        baseDevice.getDeviceType() == BaseDevice.TYPE_AS97))) {
            byte[] sleepcmd = new byte[]{(byte) 0xbe, 0x01, 0x15, (byte) 0xed};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, sleepcmd);
        }
    }

    /**
     * query sleep info
     */
    public void querySleepInfo() {
        if (this.state == BaseController.STATE_CONNECTED) {
            int dtype = baseDevice.getDeviceType();
            if (dtype == BaseDevice.TYPE_W307H || dtype == BaseDevice.TYPE_W301H || DeviceInfo.getInstance()
                    .getFirmwareHighVersion() >= 89) {
                byte[] sleepcmd = new byte[]{(byte) 0xbe, 0x01, 0x07, (byte) 0xed};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, sleepcmd);
            }
        }
    }

    /**
     * 解析睡眠时间设置
     *
     * @param data
     */
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
            int dtype = baseDevice.getDeviceType();
            if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || DeviceInfo.getInstance()
                    .getFirmwareHighVersion() >= 89) {
                byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x08, (byte) 0xed};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
            }
        }
    }

    /**
     * 解析心率监测设置
     *
     * @param data
     */
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

    /**
     * 解析display信息
     *
     * @param data
     */
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
                    displaySet.setShowSmsMissedCall(true);///数量
                } else if (val == 0x0B) {
                    displaySet.setShowIncomingReminder(true);
                } else if (val == 0x0D || val == 0x1D) {
                    displaySet.setShowMsgContentPush(true);
                } else if (val == 0x0F) {///倒计时
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
            int dtype = baseDevice.getDeviceType();
            if (dtype == BaseDevice.TYPE_W307H || dtype == BaseDevice.TYPE_W301H || DeviceInfo.getInstance()
                    .getFirmwareHighVersion() >= 89) {
                byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x09, (byte) 0xed};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
            }

        }
    }

    /**
     * 解析闹钟信息
     *
     * @param data
     */
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
            int dtype = baseDevice.getDeviceType();
            if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || DeviceInfo.getInstance()
                    .getFirmwareHighVersion() >= 89) {
                byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x0c, (byte) 0xed};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
            }

        }
    }

    /**
     * 解析久坐提醒
     *
     * @param data
     */
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

    /**
     * 请求实时数据指令
     */
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
                    (byte) (((devicetype == BaseDevice.TYPE_W311N || devicetype == BaseDevice.TYPE_AT200 ||
                            devicetype == BaseDevice.TYPE_AT100 ||
                            devicetype == BaseDevice.TYPE_SAS80 ||
                            devicetype == BaseDevice.TYPE_W307H ||
                            devicetype == BaseDevice.TYPE_W240B ||
                            devicetype == BaseDevice.TYPE_W285S ||
                            devicetype == BaseDevice.TYPE_W301H ||
                            devicetype == BaseDevice.TYPE_AS97) ? wristMode.isLeftHand() : !wristMode.isLeftHand())
                            ? 0 : 1)};
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
            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " sendCommand " + temp).append
                    ("\r\n");
            Log.e("controller", "sendCommand = " + builder.toString() + ",getConnectState():" + getConnectState());
        } else {
        }

        BluetoothGattService tpService = mBluetoothGatt.getService(MAIN_SERVICE);
        if (tpService != null) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = tpService.getCharacteristic(characteristicID);
            boolean writeState = false;
            //连接成功的情况才会发送指令
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null && getConnectState() == BaseController.STATE_CONNECTED) {
                mBluetoothGattCharacteristic.setWriteType(mBluetoothGattCharacteristic.getWriteType());
                mBluetoothGattCharacteristic.setValue(bytes);
                setDeviceBusy(mBluetoothGatt);
                // writeState = mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                //这里需要对指令进行重新发送
                //需要重复去发送指令
                mSendOk = false;
                int mRetry = 0;
                //  while (mRetry < 5 && mBluetoothGatt != null && !writeState) {

                while (mRetry < 5 && mBluetoothGatt != null && !writeState && getConnectState() == BaseController.STATE_CONNECTED) {
                    writeState = mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
                    mRetry++;
                    Log.e(TAG, "Write data status:" + writeState + ",mRetry:" + mRetry + ",mSendOk:" + mSendOk + ",mBluetoothGatt:" + mBluetoothGatt + "writeState:" + writeState);
                    if (writeState) {
                        //   mSendOk = true;
                    }
                    if (!writeState) {
                        SystemClock.sleep(200);
                    }
                }
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
            Log.e(TAG, "sendCommand writeState = " + mSendOk);
            return writeState;
        } else {
            if (IS_DEBUG) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    builder.append(String.format("%02X ", bytes[i]));
                }
                String temp = builder.toString();
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " sendCommand tpService == " +
                        "null").append
                        ("\r\n");
            }
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
        Log.e(TAG, "**** sendNotiCmd  ****" + ",  state:" + state + ",  baseDevice:" + baseDevice);
        if (state != STATE_CONNECTED || baseDevice == null)
            return;
        int dtype = baseDevice.getDeviceType();
        Log.e(TAG, "**** sendNotiCmd  ****" + ",  dtype:" + dtype + "mBluetoothGatt:" + mBluetoothGatt);
        if (dtype == BaseDevice.TYPE_W301H || dtype == BaseDevice.TYPE_W307H || dtype == BaseDevice.TYPE_W301S ||
                dtype == BaseDevice.TYPE_W307S || dtype == BaseDevice.TYPE_W307S_SPACE || dtype == BaseDevice
                .TYPE_W311N ||
                dtype == BaseDevice.TYPE_AT100 ||
                dtype == BaseDevice.TYPE_W285S || dtype == BaseDevice.TYPE_W240S || dtype == BaseDevice
                .TYPE_BLIN16_HEALTH ||
                dtype == BaseDevice.TYPE_AT200 || dtype == BaseDevice.TYPE_SAS80 || dtype == BaseDevice.TYPE_AS97) {

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
    }

    public void sendMusic(byte[] notiContent) {
        Log.e(TAG, "**** sendNotiCmd  ****" + ",  state:" + state + ",  baseDevice:" + baseDevice);
        if (state != STATE_CONNECTED || baseDevice == null)
            return;

        sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, notiContent);
    }

    ///current notification package index


    ///handle notification DE 06 type index ED
    public void handleNotiResponse(Context context, byte[] data) {
        NotificationEntry entry = NotificationEntry.getInstance(context);
        if (data != null && data.length == 5 && (data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x06 && (data[4] &
                0xff) == 0xed) {
            int type = (data[2] & 0xff);
            //判断信息类别，如果不再范围内，那么index为0
            if (!(type >= 0x12 && type <= 0x2B)) {
                currentNotiIndex = 0;
                return;
            }
            int index = (data[3] & 0xff);
            //index为1，发完title部分了
            Log.e("handleNotiResponse", "entry.isShowDetail():" + entry.isShowDetail());
            if (index == 1 && !entry.isShowDetail()) {///not send more info
                byte[] pppp = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte)
                        0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                        (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
                //第二包发ff
                sendNotiCmd(pppp, index + 1, type);
                return;
            } else if (!entry.isShowDetail()) {
                currentNotiIndex = 0;
                removeNotificationMsg();
                sendNotiCmd();
                return;
            }
            if (NotiManager.msgVector != null && NotiManager.msgVector.size() > 0) {
                if (IS_DEBUG)
                    Log.e(TAG, "***消息不是空的***");
                NotificationMsg msg = NotiManager.msgVector.get(0);
                byte[] tp = msg.getMsgContent();
                if (index == 1 && entry.isShowDetail()) {
                    byte[] ppp = new byte[15];
                    System.arraycopy(tp, 15, ppp, 0, 15);
                    sendNotiCmd(ppp, index + 1, type);
                    if (IS_DEBUG)
                        Log.e(TAG, "**0000发第一包后，发第二包**index== " + (index + 1) + "***内容***" + new String(ppp));
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < ppp.length; i++) {
                        builder.append(String.format("%02X ", ppp[i]));
                    }
                    /*if (IS_DEBUG) {
                        if (BaseController.logBuilder == null) {
                            BaseController.logBuilder = new StringBuilder();
                        }
                        for (int i = 0; i < ppp.length; i++) {
                            builder.append(String.format("%02X", ppp[i]) + " ");
                        }
                        logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + "**发送index== " +
                                (index + 1) + "***内容***" + new String(ppp) + " hex str" + builder
                                .toString() + "\r\n");
                    }*/
                    if (IS_DEBUG)
                        Log.e(TAG, "**0000发第一包后，发第二包**index== " + (index + 1) + "***内容***" + builder.toString());
                } else if (index > 1) {
                    //收到第四个包 或者 发送包的最后一个字节是0xff  即发送完成
                    if ((index + 1) * 15 > 60 || (tp[index * 15 - 1] == (byte) 0xFF)) {
                        if (IS_DEBUG)
                            Log.e(TAG, "**22222发第四包后，进入下一个消息发送**");
                        currentNotiIndex = 0;
                        removeNotificationMsg();
                        sendNotiCmd();
                    } else {
                        byte[] nn = new byte[15];
                        System.arraycopy(tp, index * 15, nn, 0, nn.length);
                        sendNotiCmd(nn, index + 1, type);
                        if (IS_DEBUG)
                            Log.e(TAG, "111111**发第" + index + "一包后，发第" + (index + 1) + "包" + "***内容***" + new String
                                    (nn));
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < nn.length; i++) {
                            builder.append(String.format("%02X ", nn[i]));
                        }
                       /* if (IS_DEBUG) {
                            if (BaseController.logBuilder == null) {
                                BaseController.logBuilder = new StringBuilder();
                            }
                            for (int i = 0; i < nn.length; i++) {
                                builder.append(String.format("%02X", nn[i]) + " ");
                            }
                            logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + "**发送index== " +
                                    (index + 1) + "***内容***" + new String(nn) + " hex str" +
                                    builder.toString() + "\r\n");
                        }*/
                        if (IS_DEBUG)
                            Log.e(TAG, "111111**发第" + index + "一包后，发第" + (index + 1) + "包" + "***内容***" + builder
                                    .toString());
                    }
                }
            } else {
                if (IS_DEBUG)
                    Log.e(TAG, "***消息列表是空的***");
                currentNotiIndex = 0;
                removeNotificationMsg();
                sendNotiCmd();
            }
        }
    }

    public void handleNotiResponseMusic(Context context, byte[] data) {
        NotificationEntry entry = NotificationEntry.getInstance(context);
        if (data != null && data.length == 4 && (data[0] & 0xff) == 0xde && (data[1] & 0xff) == 0x08 && (data[2] & 0xff) == 0x03 && (data[3] &
                0xff) == 0xed) {

            if (musicList.size() > 0) {
                sendNotiCmdMusic();
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
     * clear heart history， devicetype == AS97
     */
    public void clearHeartHistory() {
        if (state == STATE_CONNECTED && baseDevice != null) {
            int typ = baseDevice.getDeviceType();
            if (typ == BaseDevice.TYPE_AS97) {
                byte[] cmds = new byte[]{(byte) 0xBE, 02, 0x09, (byte) 0xED};
                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
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
                        baseDevice.getDeviceType() != BaseDevice.TYPE_AS97)) {
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
                /*17Byte BE＋01＋07+FE+开关控制（1byte）
                +计划睡眠小时(1byte)+计划睡眠分(1byte)
                +睡眠提醒小时(1byte) +睡眠提醒分(1byte)
                +计划起床小时(1byte) +计划起床分(1byte)
                +计划午休小时(1byte) +计划午休分(1byte)
                +结束午休小时(1byte) +结束午休分(1byte)
                +午休提醒小时(1byte) +午休提醒分(1byte)*/
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
                index++;
            }
            if (displaySet.isShowIncomingReminder()) {
                data[index] = 0x0B;
                index++;
            }
            if (displaySet.isShowMsgContent()) {
                data[index] = 0x1D;
                index++;
            }
            if (displaySet.isShowCountDown()) {
                data[index] = 0x0f;
                index++;
            }
            for (int i = index; i < 20; i++) {
                data[i] = (byte) 0xff;
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }

    /**
     * get fireware version
     *
     * @return
     */
    public float getVersion() {
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        float version = Float.valueOf(deviceInfo.getFirmwareHighVersion() + "." + deviceInfo.getFirmwareLowVersion());
        return version;
    }

    /**
     * start or end recive heart rate data at once
     *
     * @param isEnable
     */
    public boolean setEnableHeart(boolean isEnable) {
        // TODO: 2018/3/30 修改为app不控制开始和结束，而是只控制收到数据展示与否，而只有Smart Active会控制心率测试的开始和结束
//        if (BuildConfig.SDKPRODUCT.equals(Constants.PRODUCT_UFIT)) {
//            //U fit依然采用开始结束的模式
//            float version = getVersion();
//            if (state == STATE_CONNECTED && version >= 89.59 && (baseDevice != null &&
//                    (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice.getDeviceType() == BaseDevice
//                            .TYPE_AT200 ||
//                            baseDevice.getDeviceType() == BaseDevice.TYPE_AS97))) {
//                byte[] bs = new byte[20];
//                bs[0] = (byte) 0xbe;
//                bs[1] = 0x01;
//                bs[2] = 0x15;
//                bs[3] = (byte) 0xfe;
//                bs[4] = (byte) (isEnable ? 0x81 : 0x80);
//                for (int i = 5; i < 20; i++) {
//                    bs[i] = (byte) 0xff;
//                }
//                sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, bs);
//                return true;
//            }
//            return false;
//        } else {
        //isport tracker 修改为只是设置展示
        setShowHeartRate(isEnable);
        return true;
//        }
    }

    /**
     * open or close heart detect at once,only the fireware version is up 89.59
     *
     * @param isEnable
     */
    public boolean setSmartActiveEnableHeart(boolean isEnable) {
        // TODO: 2018/3/30 修改为app不控制开始和结束，而是只控制收到数据展示与否，而只有Smart Active会控制心率测试的开始和结束
        float version = getVersion();
        if (state == STATE_CONNECTED && version >= 89.59 && (baseDevice != null &&
                (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice.getDeviceType() == BaseDevice
                        .TYPE_AT200 ||
                        baseDevice.getDeviceType() == BaseDevice.TYPE_AS97))) {
            byte[] bs = new byte[20];
            bs[0] = (byte) 0xbe;
            bs[1] = 0x01;
            bs[2] = 0x15;
            bs[3] = (byte) 0xfe;
            bs[4] = (byte) (isEnable ? 0x81 : 0x80);
            for (int i = 5; i < 20; i++) {
                bs[i] = (byte) 0xff;
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, bs);
            return true;
        }
        return false;
    }

    /**
     * judge whether support heart command to control state of  heart test that is on or off
     *
     * @param db
     * @return
     */
    public boolean isSupportCmdHeart(BaseDevice db) {
        float version = getVersion();
        if (version >= 89.59 && (db != null &&
                (db.getDeviceType() == BaseDevice.TYPE_W311N || db.getDeviceType() == BaseDevice.TYPE_AT200 ||
                        db.getDeviceType() == BaseDevice.TYPE_AS97))) {
            return true;
        }
        return false;
    }


    /**
     * complete interface BluetoothGattCallback
     */


    private int reconnectCount = 1;


    public void clear() {

    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (IS_DEBUG)
            Log.e("CmdController", "onConnectionStateChange status:" + status + ",newState:" + newState);
       /* if (BaseController.logBuilder == null) {
            BaseController.logBuilder = new StringBuilder();
        }*/
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
        mBluetoothGatt = gatt;
        tempConnectedState = newState;
        syncState = STATE_SYNC_COMPLETED;
        hasSyncBaseTime = false;
        hasOpenRealTime = false;
        state = BaseController.STATE_DISCONNECTED;

        if (NotiManager.msgVector != null) {
            NotiManager.msgVector.clear();
        }
        callCurrentIndex = 0;
        cancelLidlTimer();
        BluetoothDevice tpdevice = gatt.getDevice();
        deviceInfoHandler.removeMessages(0x01);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            //connectHandler.removeMessages(0x07);
            if (tempConnectedState == BluetoothGatt.STATE_CONNECTED) {
                state = BaseController.STATE_CONNECTING;
                if (IS_DEBUG)
                    Log.e("CmdController", "连上了");
               /* if (IS_DEBUG) {
                    BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " " +
                            "connectstatechange connected\r\n");
                }*/
                //去发现服务
                int delay = 1600;
                if (gatt != null) {
                    final boolean bonded = gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED;

                    //   Logger.myLog("baseGattCallback: bonded" + bonded);

                    //int tempdelay = 300;

                    //体脂秤值需要300ms
                    if (bonded) {
                        delay = 1600;
                    } else {
                        delay = 1600;
                    }
                }
                deviceInfoHandler.sendEmptyMessageDelayed(0x03, delay);
            } else if (tempConnectedState == BluetoothGatt.STATE_DISCONNECTED) {
                //connectHandler.removeMessages(0x07);
                if (IS_DEBUG)
                    Log.e("CmdController", "断开了 tpdevice:" + tpdevice);
                /*if (IS_DEBUG) {
                    BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " " +
                            "connectstatechange disconnected\r\n");
                }*/
                state = newState;
                closeGatt();
                mGattService = null;
                dayCount = 0;
                heartRateDayCount = 0;
                isSyncHeartRateHistorying = false;
                DeviceInfo.getInstance().resetDeviceInfo();
                hasSyncBaseTime = false;
                if (callback != null) {
                    callback.connectState(tpdevice, state);
                }
            }
        } else {
            //connectHandler.removeMessages(0x07);
            state = BluetoothGatt.STATE_DISCONNECTED;
            closeGatt();
            /*if (IS_DEBUG) {
                BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " " +
                        "connectstatechange error\r\n");
            }*/
            DeviceInfo.getInstance().resetDeviceInfo();
            reconnectCount++;

            //需要做品牌的兼容
            //brand:samsung,model:SM-J250F
            //brand:xiaomi,model:Mi A1

            String info = TAG + " brand:" + Build.BRAND + ",model:" + Build.MODEL
                    + ",sdkLevel:" + Build.VERSION.SDK_INT + ",release:" + Build.VERSION.RELEASE;
            Log.e(TAG, "GATT operation error: error code = " + status + ",tpdevice:" + tpdevice + "new Status:" + newState + "info:" + info + "reconnectCount:" + reconnectCount);


            // Log.e(TAG, "GATT operation error: error code = " + status + ",tpdevice:" + tpdevice);
            //断开连接的时候在同步需要把同步的移除 需要把所有线程都移除
            syncHandler.removeMessages(0);
            commandHandler.removeMessages(0);
            enableNotiHandler.removeMessages(0);
            deviceInfoHandler.removeMessages(0);
            writeHandler.removeMessages(0);
            if (syncHandler.hasMessages(0x01)) {
                syncHandler.removeMessages(0x01);
            }
            if (Build.BRAND.equals("samsung") && Build.MODEL.equals("SM-J250F")) {
                //关闭蓝牙再打开蓝牙
                if (reconnectCount >= 4) {
                    if (callback != null) {
                        reconnectCount = 0;
                        callback.connectionError(tpdevice, newState);
                    }

                } else {
                    if (callback != null) {
                        Log.e(TAG, "closeBluAndOpenBlu:reconnect:" + "reconnectCount:" + reconnectCount);
                        callback.reconnect();
                    }

                }
            } else if (Build.BRAND.equals("xiaomi") && Build.MODEL.equals("Mi A1")) {

                if (reconnectCount >= 3) {
                    if (callback != null) {
                        reconnectCount = 0;
                        callback.connectionError(tpdevice, newState);
                    }
                } else {
                    if (callback != null) {
                        callback.reconnect();
                    }
                }

            } else {
                if (callback != null) {
                    if (reconnectCount <= 3) {
                        callback.reconnect();
                    } else {
                        reconnectCount = 0;
                        callback.connectionError(tpdevice, newState);
                    }
                }
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
            if (IS_DEBUG)
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
                if (IS_DEBUG)
                    Log.e("parserGatt()", "mDeviceBusy = " + device);
            }
        } catch (Exception localException) {
            if (IS_DEBUG)
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
                if (IS_DEBUG)
                    Log.e("parserGatt()", localException.getMessage());
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.e("onServicesDiscovered", "onServicesDiscovered: " + status);
        isRunOnServicesDiscovered = true;
        if (status == 0) {
            if (null != mDelayFindServiceHandler)
                mDelayFindServiceHandler.removeCallbacks(checkoutServiceRunable);
          /*  if (BaseController.logBuilder == null) {
                BaseController.logBuilder = new StringBuilder();
            }*/
            if (IS_DEBUG) {
               /* BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " " +
                        "onServicesDiscovered\r\n");*/
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
                                Log.e(TAG, MAIN_SERVICE.toString() + "***" + SEND_DATA_CHAR.toString());
                            } else if (BleConfig.UUID_RECEIVE_DATA_CHAR.equals(subStr)) {
                                mReviceDataCharacteristic = characteristic;
                                RECEIVE_DATA_CHAR = uuid;
                            } else if (subStr.equals("ff03")) {

                            } else if (BleConfig.UUID_REALTIME_RECEIVE_DATA_CHAR.equals(subStr)) {
                                mRealTimeCharacteristic = characteristic;
                                REALTIME_RECEIVE_DATA_CHAR = uuid;
                            }
                        }
                        if (SEND_DATA_CHAR == null || RECEIVE_DATA_CHAR == null || REALTIME_RECEIVE_DATA_CHAR == null) {
                            disconnect();////找不到就断开
                        } else {
                            setEnableNotification();
                        }
                    } else {
                        disconnect();////找不到就断开
                    }

                }
            } else {
                disconnect();////找不到就断开
            }


        } else {

            //发现服务失败，继续去发现服务
        }


    }

    @Override
    public void setEnableNotification() {
        if (tempConnectedState == BaseController.STATE_CONNECTED) {
            notiHandler.sendEmptyMessage(0x02);//300
        }
    }

    /**
     * This method will check if Heart rate value is in 8 bits or 16 bits
     */
    private boolean isHeartRateInUINT16(final byte value) {
        return ((value & 0x01) != 0);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        byte[] values = characteristic.getValue();
        if (characteristic.getService().getUuid().equals(BaseController.DEVICEINFORMATION_SERVICE) &&
                characteristic.getUuid()
                        .equals(BaseController.FIRMWAREREVISION_CHARACTERISTIC)) {
            Log.e(TAG, "***FirmwareVersion111***" + new String(values));
            String s = new String(values);
            boolean result = false;
//            if ("V90.69.01".equals(s.split(" ")[0])) {
            if ("V90.85.01".equals(s.split(" ")[0])) {
                result = true;
            }
            if (dsCallBack != null) {
                dsCallBack.isHeartRateDevice(result);
            }
//            deviceInfoHandler.sendEmptyMessage(0x01);//1000
        }
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
                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " onCharacteristicChanged " +
                        "heartRate = " + heartRate +
                        "\r\n");
            }
            //Log.e(TAG, "onCharacteristicChanged heartRate = " + heartRate);
            if (heartRate <= 30) {
                return;
            }
            if (onHeartListener != null) {
                if (isShowHeartRate) {
                    logBuilder.append("isShowHeartRate 展示").append("\r\n");
                    onHeartListener.onHeartChanged(new HeartData(heartRate, Calendar.getInstance().getTimeInMillis()));
                } else {
                    logBuilder.append("isShowHeartRate 不展示").append("\r\n");
                }
            }
        } else {
            handleCharacterisicChanged(gatt, characteristic);
        }

        super.onCharacteristicChanged(gatt, characteristic);
    }

    public void setHeartDescription() {
        if (mBluetoothGatt != null && baseDevice != null && (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N ||
                baseDevice.getDeviceType() == BaseDevice.TYPE_AT200 || baseDevice.getDeviceType() == BaseDevice
                .TYPE_AS97)) {
            notiHandler.sendEmptyMessageDelayed(0x04, 200);
        }
    }


    @Override
    public void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (IS_DEBUG)
            Log.e(TAG, "**** onDescriptorWrite  ****" + ",  " + descriptor.getUuid() + ",  " + descriptor
                    .getCharacteristic().getUuid() + BluetoothGatt.GATT_SUCCESS + "==" + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (IS_DEBUG)
                Log.e(TAG, "gatt success");
            final UUID uuid = descriptor.getCharacteristic().getUuid();
            if (uuid.equals(SEND_DATA_CHAR)) {
                ///准备就绪，可以发送控制指令，并同步数据了，因为RECEIVE_DATA_CHAR 在之前就打开了
                if (callback != null) {
                    callback.needDeviceInfoSettingCallBack();
                }
                setDeviceBusy(mBluetoothGatt);
                if (dsCallBack != null) {
                    dsCallBack.isReadySync(true);
                }
                ////开启实时数据通道
                notiHandler.sendEmptyMessage(0x03);//200
            } else if (uuid.equals(RECEIVE_DATA_CHAR)) {////历史数据接收通道, 设置成功后，开启命令控制通道 SEND_DATA_CHAR
                notiHandler.sendEmptyMessage(0x01);//200
            } else if (uuid.equals(REALTIME_RECEIVE_DATA_CHAR)) {////实时数据通道开启成功，接着开启心率数据通道,不用担心有没有心率服务
                notiHandler.sendEmptyMessage(0x04);//200
                connectSuccess();
                deviceInfoHandler.sendEmptyMessageDelayed(0x04, 1000);//400
                deviceInfoHandler.sendEmptyMessageDelayed(0x01, 2000);//1000


            } else if (uuid.equals(HEARTRATE_SERVICE_CHARACTER)) {

            }
        } else if (status == BluetoothGatt.GATT_FAILURE) {
            if (IS_DEBUG)
                Log.e(TAG, "gatt failed");
            ///都需要做一个延时，否则可能会出现一直设置不成功，导致无限循环，出现内存溢出
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

    protected boolean mSendOk;

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);

        Log.e(TAG, "onCharacteristicWrite, status: " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
//            Logger.myLog("GATT_WRITE_SUCCESS!");
            mSendOk = true;
        } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
            Log.e(TAG, "GATT_WRITE_NOT_PERMITTED");
        } else {
            Log.e(TAG, "Write failed , Status is " + status);
        }

        byte[] value = characteristic.getValue();
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        if (value != null && value.length > 0) {
            StringBuilder builder = new StringBuilder();
           /* if (IS_DEBUG) {
                if (BaseController.logBuilder == null) {
                    BaseController.logBuilder = new StringBuilder();
                }
                for (int i = 0; i < value.length; i++) {
                    builder.append(String.format("%02X", value[i]) + " ");
                }
                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " onCharacteristicWrite " +
                        "success " + builder.toString() + "\r\n");
            }*/
            // TODO: 2018/7/2 89.48f 版本功能保留
            if (baseDevice != null && ((baseDevice.getDeviceType() == BaseDevice.TYPE_W307S && getVersion() == 89.48f
                    && deviceInfo.getStatePinCode() == 1) || (baseDevice.getDeviceType() == BaseDevice.TYPE_W307H &&
                    deviceInfo.getStatePinCode() == 1))) {
                if (value.length >= 4) {
                    if (value[0] == (byte) 0xBE && value[1] == 0x03 && value[2] == 0x09 && value[3] == (byte) 0xfe) {
                        //关闭设备的返回
                        if (value == ppbytes) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                reset();
                            } else {
                                commandHandler.sendEmptyMessage(0x19);//150
                            }
                        }
                    } else if (value == new byte[]{(byte) 0xbe, 06, 30, (byte) 0xed}) {
                        //关闭设备的返回
                        commandHandler.sendEmptyMessage(0x1A);//150
                    }
                }

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
                    writeHandler.sendEmptyMessage(0x01);//150
                } else if (value.length > 4) {
                    if (value[0] == (byte) 0xbe && value[1] == 0x06 && (value[2] >= 0x12 && value[2] <= 0x2B)) {//发送的是信息或短信
                        ///如果发送失败，则取消发送这条信息
//                        if (NotiManager.msgVector != null && NotiManager.msgVector.size() > 0) {
//                            NotiManager.msgVector.remove(0);
//                        }
                        ///发送下一条信息
                        currentNotiIndex = 0;
                        removeNotificationMsg();
                        sendNotiCmd();
                    } else if ((value[0] == (byte) 0xbe && value[1] == 0x06 && value[2] == 0x02 && value[3] == (byte)
                            0xFE)
                            || (value[0] == (byte) 0xbe && value[1] == 0x06 && value[2] == 0x01 && value[3] == (byte)
                            0xFE)) {///发送来电失败
                        //发送下一个来电
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
                if (IS_DEBUG)
                    Log.e(TAG, "***开始发送的时间戳***");
                //这儿的意思是永远保持消息列表只有一条消息，取第一条就行了
                if (NotiManager.msgVector.size() == 0) {
                    return;
                }
                try {
                    NotificationMsg msg = NotiManager.msgVector.get(0);
                    byte[] tp = msg.getMsgContent();
                    //先发title部分
                    //todo reflex客户需要title 和 content衔接在一起
                    byte[] ppp = new byte[15];
                    System.arraycopy(tp, 0, ppp, 0, 15);
                    //Convert back to String
//                String s = new String(bytes);
                    if (IS_DEBUG)
                        Log.e(TAG, "**发送title部分" + new String(ppp));
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < ppp.length; i++) {
                        builder.append(String.format("%02X ", ppp[i]));
                    }
                    if (IS_DEBUG)
                        Log.e(TAG, "**发送title部分**" + builder.toString());
                    sendNotiCmd(ppp, 1, msg.getMsgType());
                    currentNotiIndex = 1;

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    public void sendNotiCmdMusic() {
        if (currentNotiIndex == 0) {
            if (musicList.size() > 0) {
                sendMusic(musicList.get(0));
                musicList.remove(0);


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
            //如果存储消息的list为null创建list
            if (NotiManager.msgVector == null) {
                NotiManager.msgVector = new Vector<>();
            }
            if (IS_DEBUG)
                Log.e(TAG, "***NotiManager.msgVector长度***" + NotiManager.msgVector.size());
            //当消息列表的长度大于等于1，那么移除最后一条，以达到刷新最新的目的
            if (NotiManager.msgVector.size() >= 1) {
                if (IS_DEBUG)
                    Log.e(TAG, "**消息列表长度>=15**移除最后一条");
                NotiManager.msgVector.remove(NotiManager.msgVector.size() - 1);
            }
            if (IS_DEBUG)
                Log.e(TAG, "**添加新消息到消息列表中");
            //添加最新的消息到消息list中
            NotiManager.msgVector.add(msg);
            //当消息间隔大于5秒时，判定为此时的消息为最新的一条
            if (System.currentTimeMillis() - startNoti > 5000) {
                if (IS_DEBUG)
                    Log.e(TAG, "***两条发送间隔5s,不然就不发送***");
                currentNotiIndex = 0;
            }
            sendNotiCmd();
        }
    }

    ArrayList<byte[]> musicList = new ArrayList<>();

    public void sendNotiCmdMusic(ArrayList<byte[]> list) {
        musicList.clear();
        musicList.addAll(list);
        if (state == BaseController.STATE_CONNECTED) {
            //如果存储消息的list为null创建list
            sendNotiCmdMusic();
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
        } else {
        }
    }

//******************************************************测试指令******************************************************************//

    /**
     * 1.抬手亮屏开关指令
     * 格式：BE-01-18-FE-开关（00：关 01：开）
     * 回复：DE-01-18-ED  W311以及BEAT 固件版本号 91.12     、307 REFLEX 固件版本号90.88 以上
     */
    public void raiseHand(boolean enable) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) (enable ? 1 : 0)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    /**
     * 1.抬手亮屏开关指令
     * 格式：BE-01-18-FE-开关（00：关 01：开）抬手总开关  （00：关 01：开）睡眠抬手开关
     * 回复：DE-01-18-ED  W311以及BEAT 固件版本号 91.12     、307 REFLEX 固件版本号90.88 以上
     */
    public void raiseHandOnlySleepMode(boolean enable) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) 1, (byte) (enable ? 1 : 0)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    /**
     * 1.抬手亮屏开关指令,
     * 格式：BE-01-18-FE-开关（00：关 01：开）抬手总开关  （00：关 01：开）睡眠抬手开关
     * 回复：DE-01-18-ED
     */
    public void raiseHandAllDayOrNot(boolean enable) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) (enable ? 1 : 0), (byte) (0)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    /**
     * 自动心率开启指令和监测间隔时间设置
     * 格式：BE-01-19-FE-开关-时间  开关(00：关 01：开） 监测间隔：15分钟，30分钟，45分钟，60 分钟
     * 回复：DE-01-19-ED
     * 备注：若自动心率关闭，则无后面的监测间隔，若自动心率 打开，则会还有监测监测的设置项，分别是15分钟，30分钟，45分钟，60分钟，
     * 供用户选择。
     * BE 01 19 FE 01
     * <p>
     * 5到60都可以选择,最小基数5分钟叠加
     * <p>
     * time 15min  30min  45min  60min
     */
    public void setAutomaticHeartRateAndTime(boolean enable, int time) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x19, (byte) 0xFE, (byte) (enable ? 1 : 0), (byte) time};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    /**
     * 2心率整点监测开关指令
     * 格式：BE-01-20-FE-开关（00：关 01：开）
     * 回复：DE-01-19-ED
     */

    public void heartRate(boolean enable) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x20, (byte) 0xFE, (byte) (enable ? 1 : 0)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    /**
     * 4.清除自动心率数据指令
     * 格式：BE-02-11-FE-YY(年2byte)-MM(月)-DD(日)
     * 回复：DE-02-11-ED
     */
    public void clearHeartRateData(Calendar calendar) {
        int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)};
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x01, 0x19, (byte) 0xFE, (byte) (tpi[0] >> 8), (byte) tpi[0],
                    (byte) tpi[1], (byte) tpi[2]};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    /**
     * 3.读自动心率数据指令
     * 格式：BE-02-10-FE-YY(年2byte)-MM(月)-DD(日)-序号(表示从第几个开始，默认为0）0-48
     * 回复：DE-02-10-YY-MM-DD-总数(2bytes)-数据（历史数据为48个，当天按实际数据）
     * 如果没有数据回复：DE-02-10-ED（MCU这边，app不用管）
     */
    public void readHeartRateData(Calendar calendar, int index) {
        int[] tpi = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)};
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[]{(byte) 0xbe, 0x02, 0x10, (byte) 0xFE, (byte) (tpi[0] >> 8), (byte) tpi[0],
                    (byte) tpi[1], (byte) tpi[2], (byte) index};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

    /**
     * 1.抬手亮屏开关指令，加入时间段
     * 格式：BE-01-18-FE-开关(bit7:总开关，bit0：时间段使能开关，
     * 00 关闭）-时间段（开始
     * （小时-分钟）-结束（小时-分钟））
     * 80 时间段开启，81 全天开启，00 全天关闭
     * 回复：DE-01-18-ED  W311以及BEAT 固件版本号 91.61
     * type 0  时间段   1  全天开启  2  全天关闭
     * startHour endHour  0-23
     * endHour   endMin   0-59
     */
    public void raiseHand(int type, int startHour, int startMin, int endHour, int endMin) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[9];
            if (type == 0) {
                cmds = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) 0x80, (byte) startHour, (byte) startMin, (byte) endHour, (byte) endMin};
            } else if (type == 1) {
                cmds = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) 0x81, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
            } else if (type == 2) {
                cmds = new byte[]{(byte) 0xbe, 0x01, 0x18, (byte) 0xFE, (byte) 0x00, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }


    /**
     * 勿扰模式指令
     * 格式：BE-01-21-FE-
     * <p>
     * 开关(00:关 01:开)-开始（小时-分钟）-结
     * 束（小时-分钟）
     * 回复：DE-01-21-ED
     */
    public void setDisturb(boolean open, int startHour, int startMin, int endHour, int endMin) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] cmds = new byte[9];
            if (open) {
                cmds = new byte[]{(byte) 0xbe, 0x01, 0x21, (byte) 0xFE, (byte) 0x01, (byte) startHour, (byte) startMin, (byte) endHour, (byte) endMin};
            } else {
                cmds = new byte[]{(byte) 0xbe, 0x01, 0x21, (byte) 0xFE, (byte) 0x00, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
            }
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, cmds);
        }
    }

   /* public  void endcall() {
        //加载serviceManager的字节码
        Class clazz = CallSmsSafeService.class.getClassLoader().loadClass("android.os.ServiceManager");
        Method method = clazz.getDeclaredMethod("getservice", String.class);
        IBinder ibind = method.invoke(null, TELEPHONY_SERVICE);
        ITelephony.Stub.asInterface(ibind).endCall();

    }*/


    public static int type_out = 1;
    public static int type_bike = 2;
    public static int type_indoor = 3;

    /**
     * 手机开启运动模式指令 格式：BE-01-24-ED 回复
     * ：DE-01-24-FB-运动模式-开关(00:关 01:开)
     * 运动模式:01:室外走，02:骑行，03：室内走。
     */
    public void sendOpenSportMode(int type, boolean enable) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] data = new byte[]{(byte) 0xbe, 0x01, 0x24, (byte) 0xfb, (byte) type, (byte) (enable ? 1 : 0)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }


    /**
     * 格式：BE-01-27-FE-首页（00-默认 01-指针表盘） 回复：DE-01-27-00
     */

    public void sendPointerdial(boolean enable) {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] data = new byte[]{(byte) 0xbe, 0x01, 0x27, (byte) 0xfb, (byte) (enable ? 1 : 0)};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }


    /**
     * 读取存储运动数据指令
     * BE-02-16-ED
     */

    public void sendReadMemorySportData() {
        if (this.state == BaseController.STATE_CONNECTED) {
            byte[] data = new byte[]{(byte) 0xbe, 0x02, 0x16, (byte) 0xED};
            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }


    public int currentTemp(int temp) {
        if (temp < 0) {
            temp = Math.abs(temp) + 511;
        }
        return temp;
    }

    public void sendWeatherCmd(boolean havsData, int todayWeather, int todaytempUnit, int todayhightTemp, int todaylowTemp, int todayaqi, int nextWeather, int nexttempUnit, int nexthightTemp, int nextlowTemp, int nextaqi, int afterWeather, int aftertempUnit, int afterhightTemp, int afterlowTemp, int afteryaqi) {
        if (this.state == BaseController.STATE_CONNECTED) {
            todayhightTemp = currentTemp(todayhightTemp);
            todaylowTemp = currentTemp(todaylowTemp);
            nexthightTemp = currentTemp(nexthightTemp);
            nextlowTemp = currentTemp(nextlowTemp);
            afterhightTemp = currentTemp(afterhightTemp);
            afterlowTemp = currentTemp(afterlowTemp);


            long currentDay = (todayWeather << 26) | (todaytempUnit << 25) | (todayhightTemp << 15) | (todaylowTemp << 5) | (todayaqi);
            long nextDay = (nextWeather << 26) | (nexttempUnit << 25) | (nexthightTemp << 15) | (nextlowTemp << 5) | (nextaqi);
            long afterDay = (afterWeather << 26) | (aftertempUnit << 25) | (afterhightTemp << 15) | (afterlowTemp << 5) | (afteryaqi);

      /*  //(byte) ((height & 0xffff) >> 8)
        int one = (byte) currentDay >> 24 & 0xff;
        int two = (byte) currentDay >> 16 & 0xff;
        int three = (byte) currentDay >> 8 & 0xff;
        int four = (byte) currentDay & 0xff;*/

            byte[] data = new byte[]{(byte) 0xbe, 0x01, 0x26, (byte) 0xFE, (byte) (havsData ? 1 : 0), (byte) (currentDay >> 24 & 0xff), (byte) (currentDay >> 16 & 0xff), (byte) (currentDay >> 8 & 0xff), (byte) (currentDay & 0xff)
                    , (byte) (nextDay >> 24 & 0xff), (byte) (nextDay >> 16 & 0xff), (byte) (nextDay >> 8 & 0xff), (byte) (nextDay & 0xff)
                    , (byte) (afterDay >> 24 & 0xff), (byte) (afterDay >> 16 & 0xff), (byte) (afterDay >> 8 & 0xff), (byte) (afterDay & 0xff)};
            //return data;

            sendCommand(SEND_DATA_CHAR, mGattService, mBluetoothGatt, data);
        }
    }


}
