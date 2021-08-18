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

import com.isport.isportlibrary.database.DbSportData337B;
import com.isport.isportlibrary.entry.AlarmEntry;
import com.isport.isportlibrary.entry.AutoSleep;
import com.isport.isportlibrary.entry.CallEntry;
import com.isport.isportlibrary.entry.HeartData;
import com.isport.isportlibrary.entry.SedentaryRemind;
import com.isport.isportlibrary.entry.SportData337B;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.services.bleservice.OnBloodOxygen;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.services.bleservice.OnHeartListener;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.isportlibrary.tools.ParserData;
import com.isport.isportlibrary.tools.Utils;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.isport.isportlibrary.tools.Constants.IS_DEBUG;

/**
 * Created by chengjiamei on 2016/8/24.
 * for new profile W337B
 */
public class Cmd337BController extends BaseController {
    public static String ACTION_SPORT_DATA = "com.w337b.controller.ACTION_SPORT_DATA";
    public static String EXTRA_SPORT_DATA = "com.w337b.controller.EXTRA_SPORT_DATA";

    static UUID MCCUserData = UUID.fromString("00001523-0000-1000-8000-00805f9b34fb");
    static UUID MCCTimeSync = UUID.fromString("00001524-0000-1000-8000-00805f9b34fb");
    static UUID RSCMeasurement = UUID.fromString("00002A53-0000-1000-8000-00805f9b34fb");
    static UUID MCCSportData = UUID.fromString("00001525-0000-1000-8000-00805f9b34fb");
    static UUID MCCComingPhone = UUID.fromString("00001526-0000-1000-8000-00805f9b34fb");
    static UUID MCCAlarmClock = UUID.fromString("00001527-0000-1000-8000-00805f9b34fb");
    static UUID MCCMessage = UUID.fromString("00001528-0000-1000-8000-00805f9b34fb");
    static UUID MCCFindPhone = UUID.fromString("00001529-0000-1000-8000-00805f9b34fb");

    static UUID MCCReminder = UUID.fromString("00001530-0000-1000-8000-00805f9b34fb");
    static UUID MCCSleepSet = UUID.fromString("00001531-0000-1000-8000-00805f9b34fb");
    static UUID MCCSedanty = UUID.fromString("00001532-0000-1000-8000-00805f9b34fb");///久坐提醒

    String baseuuid = "00001531-0000-1000-8000-00805F9B34FB";
    UUID mainUUID = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    private static Cmd337BController sInstance;
    private String TAG = "Cmd337BController";
    private BluetoothDevice bleDevice;
    private static Handler handler;
    private boolean hasSyncBaseTime = false;
    private OnDeviceSetting dsCallBack;
    private OnHeartListener onHeartListener;
    private OnBloodOxygen onBloodOxygen;

    private int dayCount = 0;
    private int startYear;
    private int startMonth;
    private int startDay;

    private List mCache;
    private Handler notiHandler = null;
    boolean isFF01Success = false;
    boolean isFF02Success = false;
    boolean isFF03Success = false;
    private Object mLock = new Object();

    private List<Map<Integer, byte[][]>> listNotiContent = Collections.synchronizedList(new ArrayList<Map<Integer, byte[][]>>());////消息队列,Integer 为消息类型
    private Handler enableHandler = null;

    private Cmd337BController(Context context) {
        logBuilder = new StringBuilder();
        this.context = context;
        if (syncHandler == null) {
            syncHandler = new Handler(context.getMainLooper());
        }
        if (enableHandler == null) {
            enableHandler = new Handler(context.getMainLooper());
        }
        if (notiHandler == null) {
            notiHandler = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what) {
                        case 1:
                            enableNotification(mainUUID, MCCFindPhone, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            break;
                        case 2:
                            enableHeart(true);
                            break;
                        case 3:

                            break;
                        case 4:

                            break;
                    }
                }
            };
        }
    }


    public static Cmd337BController getInstance(Context ctx) {

        if (sInstance == null) {
            synchronized (Cmd337BController.class) {
                if (sInstance == null) {
                    sInstance = new Cmd337BController(ctx.getApplicationContext());
                    if (handler == null) {
                        handler = new Handler(ctx.getApplicationContext().getMainLooper());
                    }

                }
            }
        }
        return sInstance;
    }

    public BluetoothDevice getDeviceWithAdress(String address) {
        return address == null ? null : BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    }

    public void enableHeart(boolean charstate) {
        enableNotification(mainUUID, RSCMeasurement, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }

    public void setOnHeartListener(OnHeartListener listener) {
        this.onHeartListener = listener;
    }

    public void setOnBloodOxygen(OnBloodOxygen onBloodOxygen) {
        this.onBloodOxygen = onBloodOxygen;
    }

    public void setDeviceSetting(OnDeviceSetting onDeviceSetting) {
        this.dsCallBack = dsCallBack;
    }

    public void unbindDevice(String mac) {
        removeString(KEY_LAST_SYNC_TIME + mac);
    }

    public int byte2Int(byte bt) {
        return bt & 0x000000ff;
    }

    private Runnable syncRunnable = new Runnable() {
        @Override
        public void run() {
            if (state == BaseController.STATE_CONNECTED) {
                startSync();
            } else {
                syncHandler.removeCallbacks(this);
            }
        }
    };

    /**
     * return true if start sync data success
     * or syncing
     *
     * @return
     */
    @Override
    public boolean startSync(long sync_time) {
        if (mGattService != null && mBluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = mGattService.getCharacteristic(RSCMeasurement);
            boolean readstate = mBluetoothGatt.readCharacteristic(characteristic);
            if (readstate) {

            }
        }
        SYNC_TIMEOUT = sync_time;
        syncHandler.postDelayed(syncRunnable, SYNC_TIMEOUT);
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
        try {
            if (mGattService != null && mBluetoothGatt != null) {
                BluetoothGattCharacteristic characteristic = mGattService.getCharacteristic(RSCMeasurement);
                boolean readstate = mBluetoothGatt.readCharacteristic(characteristic);
                if (readstate) {

                }
            }
        } catch (Exception e) {
                e.printStackTrace();
        } finally {
            syncHandler.postDelayed(syncRunnable, DEFAULT_SYNC_TIMEOUT);
            return false;
        }


    }


    public void readData() {

    }

    public void enableNotification(UUID service, UUID charac, byte[] value) {
        if (mBluetoothGatt != null) {
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(service);
            enableNotification(mBluetoothGattService, charac, value);
        }
    }

    public void enableNotification(boolean charstate, UUID service, UUID charac, byte[] value) {
        if (mBluetoothGatt != null) {
            BluetoothGattService mBluetoothGattService = mBluetoothGatt.getService(service);
            enableNotification(charstate, mBluetoothGattService, charac, value);
        }
    }


    public boolean enableNotification(final boolean charstate, final BluetoothGattService mBluetoothGattService, final UUID uuid, final byte[] value) {
        if (mBluetoothGattService != null && state == BaseController.STATE_CONNECTED) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(uuid);
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    boolean result = internalEnableNotifications(charstate, mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(charstate, tps, uuid, value);
                                }
                            }, 200);
                        }
                    }
                } else if (value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
                    boolean result = internalEnableIndications(charstate, mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && tempConnectedState == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(charstate, tps, uuid, value);
                                }
                            }, 200);

                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean enableNotification(final BluetoothGattService mBluetoothGattService, final UUID uuid, final byte[] value) {
        if (mBluetoothGattService != null && state == BaseController.STATE_CONNECTED) {
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattService.getCharacteristic(uuid);
            if (mBluetoothGatt != null && mBluetoothGattCharacteristic != null) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    boolean result = internalEnableNotifications(mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && state == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableNotification(tps, uuid, value);
                                }
                            }, 200);
                        }
                    }
                } else if (value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) {
                    boolean result = internalEnableIndications(mBluetoothGattCharacteristic);
                    if (!result && adapter != null && adapter.isEnabled() && state == BaseController.STATE_CONNECTED) {
                        if (mBluetoothGattService != null && mBluetoothGattService.getUuid() != null) {
                            final BluetoothGattService tps = mBluetoothGatt.getService(mBluetoothGattService.getUuid());
                            enableHandler.postDelayed(new Runnable() {
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

    private boolean internalEnableIndications(boolean charstate, final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, charstate);
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

    private boolean internalEnableNotifications(boolean charstate, final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, charstate);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESCRIPTOR);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    //设置信息提醒
    public void setInfoReminder(byte[] values) {
        if (getConnectState() == BaseController.STATE_CONNECTED) {
            sendCommand(MCCReminder, mGattService, mBluetoothGatt, values);
        }
    }

    @Override
    public void readRemoteRssi() {
        if (state == STATE_CONNECTED) {
            //mBluetoothGatt.readRemoteRssi();
        }
    }

    public boolean syncHistoryData() {
        if (mBluetoothGatt != null && mGattService != null) {
            BluetoothGattCharacteristic characteristic = mGattService.getCharacteristic(MCCSportData);
            boolean tpstate = mBluetoothGatt.readCharacteristic(characteristic);
            if (tpstate) {

            }
        }
        return false;
    }

    @Override
    public void syncTime() {
        Calendar calendar = Calendar.getInstance();
        byte[] value = {(byte) (calendar.get(Calendar.YEAR) & 0xFF), (byte) (calendar.get(Calendar.YEAR) >> 8), (byte) (calendar.get(Calendar.MONTH) + 1),
                (byte) (calendar.get(Calendar.DAY_OF_MONTH)),
                (byte) (calendar.get(Calendar.HOUR_OF_DAY)), (byte) (calendar.get(Calendar.MINUTE)), (byte) (calendar.get(Calendar.SECOND))};
        sendCommand(MCCTimeSync, mGattService, mBluetoothGatt, value);
    }

    public void setAlarm(List<AlarmEntry> result) {
        if (result != null && result.size() > 0) {
            if (result.size() > 4) {
                result.remove(result.size() - 1);
            }
            byte[] value = new byte[20];
            Calendar calendar = Calendar.getInstance();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < result.size() * 5; i += 5) {
                AlarmEntry model = result.get(i / 5);
                if (!model.isOn()) {
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                }
                int year = calendar.get(Calendar.YEAR) - 2000;
                byte month = (byte) (calendar.get(Calendar.MONTH) + 1);
                byte day = (byte) calendar.get(Calendar.DAY_OF_MONTH);
                byte hour = (byte) model.getStartHour();
                byte minute = (byte) model.getStartMin();
                if (model.isOn()) {
                    value[i] = (byte) ((year << 2) | ((month & 0x0f) >> 2));
                    value[i + 1] = (byte) ((month & 0x3) << 6 | (day & 0x1f) << 1 | (hour & 0x1f) >> 4);
                    value[i + 2] = (byte) ((hour & 0xF) << 4 | (minute & 0x3f) >> 2);
                    value[i + 3] = (byte) ((minute & 0x03) << 6 | ((i / 5 + 1) & 0x07) << 3);
                    value[i + 4] = (byte) ((model.getRepeat() & 0x7f) >> 1 | ((model.getRepeat() & 0x1) << 6));
                } else {
                    value[i] = (byte) 0xff;
                    value[i + 1] = (byte) 0xff;
                    value[i + 2] = (byte) 0xff;
                    value[i + 3] = (byte) (0xc7 | (i & 0x07) << 3);
                    value[i + 4] = (byte) 0xff;
                }
                builder.append(String.format("%02X", model.getRepeat()) + " ");
            }
            if (result.size() < 4) {
                for (int i = result.size(); i < 4; i++) {
                    value[i] = (byte) 0xff;
                    value[i + 1] = (byte) 0xff;
                    value[i + 2] = (byte) 0xff;
                    value[i + 3] = (byte) (0xc7 | (i & 0x07) << 3);
                    value[i + 4] = (byte) 0xff;
                }
            }
            builder.append("\r\n");
            for (int i = 0; i < value.length; i++) {
                builder.append(String.format("%02X", value[i]) + " ");
            }
            Log.e("alarm", builder.toString());
            sendCommand(MCCAlarmClock, mGattService, mBluetoothGatt, value);
        }
    }

    public void sendUserInfo() {
        if (getConnectState() == BaseController.STATE_CONNECTED) {
            UserInfo userInfo = UserInfo.getInstance(context);
            float footDistance = userInfo.getStrideLength();
            int metric = userInfo.getMetricImperial();///0 公制  1 英制
            int weight = (int) ((metric == 0 ? Float.valueOf(userInfo.getWeight()) : Float.valueOf(userInfo.getWeight()) * 0.45359237f));
            int height = (int) ((metric == 0 ? Float.valueOf(userInfo.getHeight()) : Float.valueOf(userInfo.getHeight()) * 2.54f));
            int foot = (int) ((metric == 0 ? Math.round(footDistance) : Math.round((footDistance * 2.54f))));
            int targetDistance = userInfo.getTargetStep();
            byte[] values = new byte[9];
            values[0] = (byte) (userInfo.getGender());
            values[1] = (byte) (Utils.calueAge(userInfo.getBirthday(), "yyyy-MM-dd"));
            ;
            values[2] = (byte) foot;
            values[3] = (byte) height;
            values[4] = (byte) (height >> 8);
            values[5] = (byte) weight;
            values[6] = (byte) (weight >> 8);
            sendCommand(MCCUserData, mGattService, mBluetoothGatt, values);
        }
    }

    @Override
    public void syncUserInfo() {
        sendUserInfo();
    }

    @Override
    public void sendDeviceInfo() {

    }

    public void sendPhoneNum(CallEntry entry) {
        if (entry == null)
            return;
        String content = (entry.getName() == null || entry.getName().equals("")) ? ((entry.getPhoneNum() == null || entry.getPhoneNum().equals("")) ? "" : entry.getPhoneNum()) : entry.getName();
        if (content.equals(""))
            return;
        byte[] nameOrNum = parser337BNumberNameToByte(entry.getType(), content);
        if (nameOrNum == null)
            return;
        if (getConnectState() != BaseController.STATE_CONNECTED) {
            return;
        }
        sendCommand(MCCComingPhone, mGattService, mBluetoothGatt, nameOrNum);
    }

    public void setAutoSleep(AutoSleep autoSleep) {
        if (getConnectState() != BaseController.STATE_CONNECTED) {
            return;
        }
        byte[] values = new byte[17];
        values[0] = (byte) 0xbe;
        values[1] = 0x01;
        values[2] = 0x07;
        values[3] = (byte) 0xfe;
        values[4] = (byte) (autoSleep.isAutoSleep() ? 1 : 0);
        if (autoSleep.isSleep()) {///是否开启睡眠
            values[5] = (byte) autoSleep.getSleepStartHour();
            values[6] = (byte) autoSleep.getSleepStartMin();

            values[9] = (byte) autoSleep.getSleepEndHour();
            values[10] = (byte) autoSleep.getSleepEndMin();
        } else {
            values[5] = (byte) 0xff;
            values[6] = (byte) 0xff;
            values[9] = (byte) 0xff;
            values[10] = (byte) 0xff;
        }
        if (autoSleep.isSleepRemind()) {
            int beginHour = autoSleep.getSleepStartHour();
            int beginMin = autoSleep.getSleepStartMin();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, beginHour);
            calendar.set(Calendar.MINUTE, beginMin);
            calendar.add(calendar.MINUTE, -1 * autoSleep.getSleepRemindTime());
            values[7] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            values[8] = (byte) calendar.get(Calendar.MINUTE);
        } else {
            values[7] = (byte) 0xff;
            values[8] = (byte) 0xff;
        }
        if (autoSleep.isNapRemind()) {///是否午休
            values[11] = (byte) autoSleep.getNapStartHour();
            values[12] = (byte) autoSleep.getNapStartMin();
            values[13] = (byte) autoSleep.getNapEndHour();
            values[14] = (byte) autoSleep.getNapEndMin();
        } else {
            values[11] = (byte) 0xff;
            values[12] = (byte) 0xff;
            values[13] = (byte) 0xff;
            values[14] = (byte) 0xff;
        }
        if (autoSleep.isNapRemind()) {
            int beginHour = autoSleep.getNapStartHour();
            int beginMin = autoSleep.getNapStartMin();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, beginHour);
            calendar.set(Calendar.MINUTE, beginMin);
            calendar.add(Calendar.MINUTE, -1 * autoSleep.getNapRemindTime());
            values[15] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            values[16] = (byte) calendar.get(Calendar.MINUTE);
        } else {
            values[15] = (byte) 0xff;
            values[16] = (byte) 0xff;
        }
        sendCommand(MCCSleepSet, mGattService, mBluetoothGatt, values);
    }

    /**
     * 打开防丢或者关闭防丢功能
     */
    public void sendAntiLost(int tps) {
        if (getConnectState() != BaseController.STATE_CONNECTED) {
            return;
        }
        byte[] values = new byte[]{0, (byte) tps};
        sendCommand(MCCFindPhone, mGattService, mBluetoothGatt, values);

    }


    public void setSedintaryRemind(List<SedentaryRemind> list) {
        if (getConnectState() != BaseController.STATE_CONNECTED) {
            return;
        }
        byte[] bs = new byte[19];
        bs[0] = (byte) 0xbe;
        bs[1] = 0x01;
        bs[2] = 0x0c;
        bs[3] = (byte) 0xfe;
        if (list != null && list.size() > 0) {
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
            bs[4] = (byte) (isOn ? 1 : 0);
            if (!isOn) {
                for (int i = 5; i < 19; i++) {
                    bs[i] = 0;
                }
            } else {
                if (index != -1) {
                    System.arraycopy(listD.get(index), 0, bs, 5, 4);
                    if (listD.size() > 1) {
                        System.arraycopy(list.get(listD.size() - 1 - index), 0, bs, 5 + 4, 4);
                        for (int i = 0; i < listD.size(); i++) {
                            if (i != index && (i != listD.size() - 1 - index)) {
                                System.arraycopy(list.get(i), 0, bs, 13, 4);
                            }
                        }
                    }
                }
                bs[17] = (byte) (SedentaryRemind.noExerceseTime / 60);
                bs[18] = (byte) (SedentaryRemind.noExerceseTime % 60);
            }
        } else {
            for (int i = 4; i <= 18; i++) {
                bs[i] = 0;
            }
        }
        sendCommand(MCCSedanty, mGattService, mBluetoothGatt, bs);
    }

    /* complete interface BluetoothGattCallback */
    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
        Log.e("Cmd337BController", "onConnectionStateChange");
        if (BaseController.logBuilder == null) {
            BaseController.logBuilder = new StringBuilder();
        }
        mBluetoothGatt = gatt;
        state = newState;
        tempConnectedState = state;
        syncState = STATE_SYNC_COMPLETED;
        hasSyncBaseTime = false;
        listNotiContent.clear();
        currentFailTimes = 0;
        if (status == BluetoothGatt.GATT_SUCCESS) {
            bleDevice = gatt.getDevice();

            if (state == BluetoothGatt.STATE_CONNECTED) {
                if (IS_DEBUG) {
                    // BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connectstatechange connected\r\n");
                }
                notiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.discoverServices();
                        } else if (gatt != null) {
                            gatt.discoverServices();
                        } else {
                            disconnect();
                        }
                    }
                }, 600);
            } else if (state == BluetoothGatt.STATE_DISCONNECTED) {
                /*if (IS_DEBUG) {
                    BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connectstatechange disconnected\r\n");
                }*/
                synchronized (mLock) {
                    close();
                }
                mGattService = null;
                dayCount = 0;

                hasSyncBaseTime = false;
            }
            if (callback != null) {
                callback.connectState(gatt.getDevice(), state);
            }
        } else {
           /* if (IS_DEBUG) {
                BaseController.logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " connectstatechange error\r\n");
            }*/
            state = newState;
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
        super.onConnectionStateChange(gatt, status, newState);
    }

    private void releaseGatt(int status, int newState, BluetoothGatt gatt, BluetoothDevice tpDevice) {
        state = newState;
        if (status == BluetoothGatt.GATT_SUCCESS) {
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

    /**
     * parser number to bytes for W337B
     *
     * @param type    1 new call and remind??2 miss call??3 listen call??4 hook call
     * @param numName
     * @return
     */
    private byte[] parser337BNumberNameToByte(int type, String numName) {
        int ypType = 3;
        switch (type) {
            case 0:
                ypType = 3;
                break;
            case 1:
                ypType = 1;
                break;
            case 2:
                ypType = 3;
                break;
        }
        if (numName == null || numName.length() == 0)
            return null;
        byte[] values = new byte[20];
        values[0] = (byte) (ypType == 1 ? 0x01 : 0x00);
        values[1] = (byte) (ypType == 2 ? 0x01 : 0x00);
        byte[] bs = numName.getBytes(Charset.forName("UTF-8"));
        if (bs.length > 18) {
            byte[] tp = new byte[18];
            System.arraycopy(bs, 0, tp, 0, 18);
            bs = tp;
        }
        System.arraycopy(bs, 0, values, 2, bs.length);
        return values;
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        List<BluetoothGattService> list = gatt.getServices();
        for (BluetoothGattService service : list) {
            List<BluetoothGattCharacteristic> listChar = service.getCharacteristics();
            if (listChar != null) {

            }
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mGattService = mBluetoothGatt.getService(mainUUID);
        } else {

        }
        setEnableNotification();
        syncTime();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startSync();

            }
        }, 1000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                syncHistoryData();
            }
        }, 2000);
        super.onServicesDiscovered(gatt, status);
    }


    @Override
    public void setEnableNotification() {
        if (state == BaseController.STATE_CONNECTED) {
            notiHandler.sendEmptyMessageDelayed(0x01, 1200);
            notiHandler.sendEmptyMessageDelayed(0x02, 900);
            notiHandler.sendEmptyMessageDelayed(0x03, 600);
            notiHandler.sendEmptyMessageDelayed(0x04, 300);
        }
    }

    @Override
    public synchronized boolean sendCommand(UUID characteristicID, BluetoothGattService gattService, BluetoothGatt bluetoothgatt, byte[] bytes) {
        if (bluetoothgatt == null)
            return false;
        if (gattService == null) {
            return false;
        }
        if (bytes == null)
            return false;

        if (IS_DEBUG) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                builder.append(String.format("%02X ", bytes[i]));
            }
            if (logBuilder == null)
                logBuilder = new StringBuilder();
            logBuilder.append(DateUtil.dataToString(new Date(), "yyyy/MM/dd HH:mm:ss") + " sendCommand " + builder.toString()).append("\r\n");
            Log.e("Cmd194Controller", "sendCommand = " + builder.toString());
        }


        BluetoothGattCharacteristic mBluetoothGattCharacteristic = gattService.getCharacteristic(characteristicID);
        if (bluetoothgatt != null && mBluetoothGattCharacteristic != null) {
            mBluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            mBluetoothGattCharacteristic.setValue(bytes);
            return bluetoothgatt.writeCharacteristic(mBluetoothGattCharacteristic);
        }
        return false;
    }

    @Override
    public void sendCustomeCmd(byte[] bytes) {

    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        UUID uuid = characteristic.getUuid();
        byte[] values = characteristic.getValue();
        if (uuid.equals(MCCFindPhone)) {
            if (IS_DEBUG) {
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " " + uuid.toString() + " from 1529" + "\r\n");
            }
            if (dsCallBack != null) {
                dsCallBack.findMobilePhone(values[0]);
            }
        } else if (uuid.equals(RSCMeasurement)) {///心率通知
            SportData337B sportData = ParserData.proccessData337BData(context, gatt.getDevice().getAddress(), callback, characteristic.getValue(), 0, baseDevice.getDeviceType(), 0);
            if (sportData != null && sportData.getHeartRate() > 0) {

                if (IS_DEBUG) {
                    if (logBuilder == null)
                        logBuilder = new StringBuilder();
                    logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " " + uuid.toString() + "\r\nheartRate = " + sportData.getHeartRate() + " from 2A53" + "\r\n");
                }
                Log.e(TAG, "heartRate = " + sportData.getHeartRate());
                DbSportData337B.getIntance(this.context).saveOrUpdate(sportData);
                if (this.onHeartListener != null) {
                    this.onHeartListener.onHeartChanged(new HeartData(sportData.getHeartRate(), Calendar.getInstance().getTimeInMillis()));
                }
                if (this.onBloodOxygen != null) {
                    this.onBloodOxygen.onBloodOxygenChanged(sportData.getBloodOxygen());
                }
            }
        }
    }


    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.e(TAG, "onDescriptorWrite");
        BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (characteristic.getUuid().equals(RSCMeasurement)) {///设置心率成功
                if (dsCallBack != null) {
                    dsCallBack.heartTimingSetting(true);
                }
            }
        } else {
            if (characteristic.getUuid().equals(RSCMeasurement)) {///设置心率成功
                if (dsCallBack != null) {
                    dsCallBack.heartTimingSetting(false);
                }
            }
        }
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);

    }

    private int MSG_FAIL_MAX = 3;///每条信息的数据包最大发送失败次数
    private int currentFailTimes = 0;//当前发送失败次数

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, final int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        final byte[] value = characteristic.getValue();
        UUID uuid = characteristic.getUuid();
        if (value != null && value.length > 0) {


            if (IS_DEBUG) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < value.length; i++) {
                    builder.append(String.format("%02X", value[i]) + " ");
                }
                if (logBuilder == null)
                    logBuilder = new StringBuilder();
                logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " " + uuid.toString() + "onCharacteristicWrite " + builder.toString() + "\r\n");
                Log.e(TAG, "MCCMessage onCharacteristicWrite " + builder.toString() + "  , status = " + status);
            }

            if (uuid.equals(MCCMessage)) {
                notiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleMessage(status, value);
                    }
                }, 100);

            }
        }
    }

    private void handleMessage(int status, byte[] value) {
        //List<Map<Integer,byte[]>> listNotiContent
        if (listNotiContent.size() > 0) {
            Map<Integer, byte[][]> map = listNotiContent.get(0);
            Set<Integer> set = map.keySet();
            int type = 0;///信息类型
            for (Integer i : set) {
                type = i;
                break;
            }
            byte[][] contents = map.get(type);////信息内容
            int pkCount = value[1];///包数，最多30个包
            int pkIndex = value[2];//当前第几个包，从1开始
            if (status == BluetoothGatt.GATT_FAILURE) {
                currentFailTimes++;
                if (currentFailTimes > MSG_FAIL_MAX) {
                    if (listNotiContent.size() > 0) {
                        listNotiContent.remove(0);
                    }
                    currentFailTimes = 0;
                    if (listNotiContent.size() > 0) {
                        map = listNotiContent.get(0);
                        set = map.keySet();
                        type = 0;///信息类型
                        for (Integer i : set) {
                            type = i;
                            break;
                        }
                        contents = map.get(type);
                        sendCommand(MCCMessage, mGattService, mBluetoothGatt, contents[0]);
                    }
                } else {
                    sendCommand(MCCMessage, mGattService, mBluetoothGatt, contents[pkIndex - 1]);
                }
            } else {
                if (pkIndex >= pkCount) {//发送下一条信息
                    if (listNotiContent.size() > 0) {
                        listNotiContent.remove(0);
                    }
                    currentFailTimes = 0;
                    if (listNotiContent.size() > 0) {
                        map = listNotiContent.get(0);
                        set = map.keySet();
                        type = 0;///信息类型
                        for (Integer i : set) {
                            type = i;
                            break;
                        }
                        contents = map.get(type);
                        sendCommand(MCCMessage, mGattService, mBluetoothGatt, contents[0]);
                    }
                } else {
                    if (contents.length > pkIndex) {///继续发送下一包
                        sendCommand(MCCMessage, mGattService, mBluetoothGatt, contents[pkIndex]);
                    } else {
                        if (listNotiContent.size() > 0) {
                            listNotiContent.remove(0);
                        }
                        currentFailTimes = 0;
                        if (listNotiContent.size() > 0) {
                            map = listNotiContent.get(0);
                            set = map.keySet();
                            type = 0;///信息类型
                            for (Integer i : set) {
                                type = i;
                                break;
                            }
                            contents = map.get(type);
                            sendCommand(MCCMessage, mGattService, mBluetoothGatt, contents[0]);
                        }
                    }
                }
            }
        }
    }

    ///发送消息
    public void sendMessage(Map<Integer, byte[][]> map) {
        if (mGattService != null && mBluetoothGatt != null && getConnectState() == BaseController.STATE_CONNECTED && map != null) {
            listNotiContent.add(map);
            if (listNotiContent.size() == 1) {
                Set<Integer> set = map.keySet();
                int type = 0;///信息类型
                for (Integer i : set) {
                    type = i;
                    break;
                }
                byte[][] contents = map.get(type);
                if (contents.length > 0) {
                    sendCommand(MCCMessage, mGattService, mBluetoothGatt, contents[0]);
                }
            } else if (listNotiContent.size() > 1) {
                if (listNotiContent.size() > 0) {
                    listNotiContent.remove(0);
                }
                if (listNotiContent.size() > 0) {
                    Set<Integer> set = listNotiContent.get(0).keySet();
                    int type = 0;///信息类型
                    for (Integer i : set) {
                        type = i;
                        break;
                    }
                    byte[][] contents = map.get(type);
                    if (contents.length > 0) {
                        sendCommand(MCCMessage, mGattService, mBluetoothGatt, contents[0]);
                    }
                }
            }
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {

        } else {

        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            byte[] value = characteristic.getValue();


            if (characteristic.getUuid().equals(RSCMeasurement)) {///心率通知
                if (IS_DEBUG) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < value.length; i++) {
                        builder.append(String.format("%02X", value[i]) + " ");
                    }
                    if (logBuilder == null)
                        logBuilder = new StringBuilder();
                    logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " onCharacteristicRead " + builder.toString() + " from 2A53" + "\r\n");
                }
                SportData337B sportData = ParserData.proccessData337BData(context, gatt.getDevice().getAddress(), callback, characteristic.getValue(), 0, baseDevice.getDeviceType(), 0);
                if (sportData != null) {
                    DbSportData337B.getIntance(this.context).saveOrUpdate(sportData);
                    Intent intent = new Intent(ACTION_SPORT_DATA);
                    intent.putExtra(EXTRA_SPORT_DATA, sportData);
                    LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
                }
            } else if (characteristic.getUuid().equals(MCCSportData)) {
                if (IS_DEBUG) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < value.length; i++) {
                        builder.append(String.format("%02X", value[i]) + " ");
                    }
                    if (logBuilder == null)
                        logBuilder = new StringBuilder();
                    logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " onCharacteristicRead " + builder.toString() + " from 1525" + "\r\n");
                }
                SportData337B sportData = ParserData.proccessData337BData(context, gatt.getDevice().getAddress(), callback, characteristic.getValue(), 0, baseDevice.getDeviceType(), -1);
                if (sportData != null) {
                    DbSportData337B.getIntance(this.context).saveOrUpdate(sportData);
                    Intent intent = new Intent(ACTION_SPORT_DATA);
                    intent.putExtra(EXTRA_SPORT_DATA, sportData);
                    LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
                }
            } else {
                if (IS_DEBUG) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < value.length; i++) {
                        builder.append(String.format("%02X", value[i]) + " ");
                    }
                    if (logBuilder == null)
                        logBuilder = new StringBuilder();
                    logBuilder.append(DateUtil.dataToString(new Date(), "MM/dd HH:mm:ss") + " onCharacteristicRead " + builder.toString() + "\r\n");
                }
            }
            Log.e(TAG, "read success");
        } else {
            Log.e(TAG, "read fail");
        }
    }
}
