package com.isport.tracker.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.isportlibrary.entry.HeartData;
import com.isport.isportlibrary.entry.HeartRecord;
import com.isport.isportlibrary.entry.SportDayData;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.services.BleService;
import com.isport.isportlibrary.services.bleservice.OnDeviceSetting;
import com.isport.isportlibrary.services.bleservice.OnHeartListener;
import com.isport.tracker.IRemoteConnection;
import com.isport.tracker.R;
import com.isport.tracker.db.DbHeart;
import com.isport.tracker.entity.HamaDeviceInfo;
import com.isport.tracker.entity.HeartDataInfo;
import com.isport.tracker.entity.HeartHistory;
import com.isport.tracker.entity.ProgressEntry;
import com.isport.tracker.entity.SyncHeartRate;
import com.isport.tracker.keeplive.RemoteService;
import com.isport.tracker.main.CamaraActivity;
import com.isport.tracker.main.MainActivityGroup;
import com.isport.tracker.main.settings.ActivityDeviceSetting;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UIUtils;
import com.isport.tracker.util.UtilTools;
import com.ypy.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/4/7.
 */

public class MainService extends BleService implements OnHeartListener {

    private String TAG = "MainService";
    public volatile static MainService sInstance;
    public static boolean isSynCom = false;
    public static String ACTION_USERSET = "com.isport.tracker.ACTION_USERSET";
    public static String ACTION_BATTERY_CHANGE = "com.isport.tracker.ACTION_BATTERY_CHANGE";
    public static String ACTION_CONNECTE_CHANGE = "com.isport.tracker.ACTION_CONNECT_CHANGE";
    public static String ACTION_CONNECTE_ERROR = "com.isport.tracker.ACTION_CONNECTE_ERROR";
    public static String ACTION_DAY_DATA_UPDATE = "com.isport.tracker.ACTION_DAY_DATA_UPDATE";
    public static String ACTION_LIDL_CONNECT_CHANGE = "com.isport.tracker.ACTION_LIDL_CONNECT_CHANGE";
    public static String ACTION_SYNC_COMPLETED = "com.isport.tracker.ACTION_SYNC_COMPLETED";
    public static String ACTION_HEART_DATA_UPDATE = "com.isport.tracker.ACTION_HEART_DATA_UPDATE";
    public static String ACTION_HEART_HISTORY_SYNCED = "com.isport.tracker.ACTION_HEART_HISTORY_SYNCED";

    public static String EXTRA_SYNC_STATE = "com.isport.tracker.syncstate";
    public static String EXTRA_DAY_DATA = "com.isport.tracker.EXTRA_DAY_DATA";
    public static String EXTRA_CONNECTION_STATE = "com.isport.tracker.EXTRA_CONNECTION_STATE";
    public static String EXTRA_CONNECT_DEVICE = "com.isport.tracker.EXTRA_CONNECT_DEVICE";
    public static String EXTRA_BATTERY_LEVEL = "com.isport.tracker.EXTRA_BATTERY_LEVEL";
    public static String ACTION_HEAD_MODIFY = "com.isport.tracker.ACTION_HEAD_MODIFY";


    private int batteryLevel = 0;
    private boolean isAutoSaveHeart;

    private static Handler serviceHandler = null;
    private boolean isInit = false;
    private boolean mIsBound;


    public static MainService getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (MainService.class) {
                if (sInstance == null) {
                    if (!BootReceive.isServiceStart(context, MainService.class.getName())) {

                        try {
                            Intent intent = new Intent(context, MainService.class);
                            context.startService(intent);

                        } catch (Exception e) {

                        }


                      /*  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            Intent intent = new Intent(context, MainService.class);
                            context.startService(intent);
                        } else {
                            if (serviceHandler == null) {
                                serviceHandler = new Handler(Looper.getMainLooper());
                            }
                            serviceHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        try {
                                            context.startForegroundService(new Intent(context, MainService.class));
                                            MyJobService.scheduleJob(context, MyJobService.getJobInfo(context, 10));
                                        } catch (Exception e) {
                                            if (!BootReceive.isServiceStart(context, MainService.class.getName())) {
                                                Intent intent = new Intent(context, MainService.class);
                                                context.startService(intent);
                                                MyJobService.scheduleJob(context, MyJobService.getJobInfo(context, 10));

                                            }
                                        }

                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        try {
                                            Intent intent = new Intent(context, MainService.class);
                                            context.startService(intent);
                                            // context.startForegroundService(new Intent(context, MainService.class));
                                            MyJobService.scheduleJob(context, MyJobService.getJobInfo(context, 10));
                                        } catch (Exception e) {
                                            if (!BootReceive.isServiceStart(context, MainService.class.getName())) {
                                                Intent intent = new Intent(context, MainService.class);
                                                context.startService(intent);

                                            }
                                        }
                                    } else {
                                        try {
                                            Intent intent = new Intent(context, MainService.class);
                                            context.startService(intent);
                                            //  context.startForegroundService(new Intent(context, MainService.class));
                                            MyJobService.scheduleJob(context, MyJobService.getJobInfo(context, 10));
                                        } catch (Exception e) {
                                            if (!BootReceive.isServiceStart(context, MainService.class.getName())) {
                                                Intent intent = new Intent(context, MainService.class);
                                                context.startService(intent);

                                            }
                                        }

                                    }
                                }
                            });

                        } */
                    }
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (myBinder == null) {
            myBinder = new MyBinder();
        }
        myServiceConnection = new MyServiceConnection();
        sInstance = this;
        // startForeground(this);
    }


    public static void startForeground(Service context) {


        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE) != PackageManager
                    .PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder builder = new Notification.Builder(context.getApplicationContext()); //获取一个Notification构造器
                    Intent nfIntent = new Intent(context, MainActivityGroup.class);
                    builder.setContentIntent(PendingIntent.getActivity(context, 0, nfIntent, 0)) // 设置 PendingIntent
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_isport)) // 设置下拉列表中的图标(大图标)
                            .setContentTitle(context.getString(R.string.app_name)) // 设置下拉列表里的标题
                            .setSmallIcon(R.drawable.logo_isport) // 设置状态栏内的小图标
                            .setContentText("") // 设置上下文内容
                            .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间


                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//修改安卓8.1以上系统报错
                        NotificationChannel notificationChannel = new NotificationChannel("notification_id", "bonlala", NotificationManager.IMPORTANCE_MIN);
                        notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
                        notificationChannel.setShowBadge(false);//是否显示角标
                        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
                        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                        manager.createNotificationChannel(notificationChannel);
                        builder.setChannelId("notification_id");
                    }

                    Notification notification = builder.build(); // 获取构建好的Notification
                    notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
                    // 参数一：唯一的通知标识；参数二：通知消息。
                    context.startForeground(112, notification);// 开始前台服务
                }

            } else {

            }

        } catch (Exception e) {

        }



     /*   NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notif = builder
                .setContentText(context.getString(R.string.application_keep_running))
                .setContentTitle(context.getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo_isport)
                .build();

        //notif.contentIntent = pendingIntent;
        notif.flags |= Notification.FLAG_NO_CLEAR; // 点击清除按钮时就会清除消息通知,但是点击通知栏的通知时不会消失
        //notif.flags |= Notification.FLAG_ONGOING_EVENT;
        notif.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        //notif.contentIntent =
        context.startForeground(112, notif);*/
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //com.isport.isportlibrary.tools.Constants.IS_DEBUG = true;
        BootstrapService.startForeground(this);
        startService(new Intent(this, BootstrapService.class));
        MainService.this.bindService(new Intent(MainService.this, RemoteService.class), myServiceConnection, Context
                .BIND_AUTO_CREATE);
        mIsBound = true;
        sInstance = this;
        isAutoSaveHeart = ConfigHelper.getInstance(this).getBoolean(Constants.IS_AUTO_SAVE_HEART, false);
        if (!isInit) {
            initDb();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        try {
            if (myServiceConnection != null && mIsBound) {
                MainService.this.unbindService(myServiceConnection);
                mIsBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void needDeviceInfoSettingCallBack() {
        super.needDeviceInfoSettingCallBack();
        Log.e("connectVibrate", "需要设置监听的回调，设置监听");
        setOnDeviceSetting(w311DeviceSetting);
    }

    @Override
    public void connectState(BluetoothDevice device, final int state) {
//        if (isDisconnectedByUser(device.getAddress())) {
//            setVibrateTimeAndShowHook(2, true);
//        } else {
//            setVibrateTimeAndShowHook(0, false);
//        }
        if (!isConnectedByUser()) {
            Log.e(TAG, "自动连接");
            setVibrateTimeAndShowHook(0, false);
        } else {
            Log.e(TAG, "主动连接");
            setVibrateTimeAndShowHook(2, true);
        }
        super.connectState(device, state);
        hasReceivedHeartData = false;
        Log.e("connectVibrate", "连接成功设置监听");
        setOnDeviceSetting(w311DeviceSetting);
        Intent intent = new Intent(ACTION_CONNECTE_CHANGE);
        intent.putExtra(EXTRA_CONNECT_DEVICE, getCurrentDevice());
        intent.putExtra(EXTRA_CONNECTION_STATE, state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        if (state != BaseController.STATE_CONNECTED) {
            if (batteryHandler.hasMessages(0x01))
                batteryHandler.removeMessages(0x01);
            isLostReminderMode = true;
            boolean isAntiLost = ConfigHelper.getInstance(this).getBoolean(ActivityDeviceSetting.DEVICE_ANTILOST,
                    false);
            if (isAntiLost) {
                lostReminder();
            }
            cancelAntiTimer();///取消获取信号定时器
            cancelLostRemindTimer();////取消提醒定时器
        } else {
            //2018/3/2 断开时也要更新version信息
            DeviceInfo deviceInfo = DeviceInfo.getInstance();
            HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FIREWARE_HIGH, deviceInfo
                    .getFirmwareHighVersion());
            HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FIREWARE_LOW, deviceInfo
                    .getFirmwareLowVersion());
            lidlConnectSuccess();
            BaseDevice baseDevice = getCurrentDevice();
            if (baseDevice != null && (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice
                    .getDeviceType() == BaseDevice.TYPE_AT200
                    || baseDevice.getDeviceType() == BaseDevice.TYPE_W337B || baseDevice.getDeviceType() ==
                    BaseDevice.TYPE_AS97 ||
                    baseDevice.getDeviceType() == BaseDevice.TYPE_HEART_RATE || baseDevice.getDeviceType() ==
                    BaseDevice.TYPE_W311T)) {
                setHeartListener(this);
            }
        }
    }

    @Override
    public void connectionError(BluetoothDevice device, int state) {
        if (batteryHandler.hasMessages(0x01))
            batteryHandler.removeMessages(0x01);
        super.connectionError(device, state);
        boolean isAntiLost = ConfigHelper.getInstance(this).getBoolean(ActivityDeviceSetting.DEVICE_ANTILOST, false);
        if (isAntiLost) {
            lostReminder();
        }
        Intent intent = new Intent(ACTION_CONNECTE_CHANGE);
        intent.putExtra(EXTRA_CONNECTION_STATE, state);
        intent.putExtra(EXTRA_CONNECT_DEVICE, getCurrentDevice());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Intent intent1 = new Intent(ACTION_CONNECTE_ERROR);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        isLostReminderMode = true;
        cancelAntiTimer();
        cancelLostRemindTimer();
    }

    @Override
    public boolean setEnableHeart(boolean isEnable) {
        return super.setEnableHeart(isEnable);
    }

    @Override
    public void syncState(int state) {
        if (state == BaseController.STATE_SYNC_COMPLETED) {
            settingHandler.sendEmptyMessage(0x17);
        } else if (state == BaseController.STATE_SYNC_ERROR) {
            settingHandler.sendEmptyMessage(0x18);
        }
        super.syncState(state);
        Intent intent = new Intent(ACTION_SYNC_COMPLETED);
        intent.putExtra(EXTRA_SYNC_STATE, state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void realTimeDayData(SportDayData dayData) {
        super.realTimeDayData(dayData);
        if (dayData == null)
            return;
        Calendar calendar = Calendar.getInstance();
        String date = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");

        Intent intent = new Intent(ACTION_DAY_DATA_UPDATE);
        intent.putExtra(EXTRA_DAY_DATA, dayData);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private Timer antilostTimer;
    private TimerTask antilostTask;

    private void initAntiTimer() {
        cancelAntiTimer();
        antilostTimer = new Timer();
        antilostTask = new TimerTask() {
            @Override
            public void run() {
                readRemoteRssi();
            }
        };
        antilostTimer.schedule(antilostTask, 0, 5000);
    }

    private void cancelAntiTimer() {
        if (antilostTask != null) {
            antilostTask.cancel();
            antilostTask = null;
        }
        if (antilostTimer != null) {
            antilostTimer.cancel();
            antilostTimer = null;
        }
    }

    private Timer lostRemindTime;
    private TimerTask lostReimerTask;
    private boolean isLostReminderMode = true;

    private void cancelLostRemindTimer() {
        if (lostReimerTask != null) {
            lostReimerTask.cancel();
            lostReimerTask = null;
        }

        if (lostRemindTime != null) {
            lostRemindTime.cancel();
            lostRemindTime = null;
        }
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    public void initLostRemindTimer() {
        cancelLostRemindTimer();
        lostReimerTask = new TimerTask() {
            @Override
            public void run() {
                isLostReminderMode = false;
                readRemoteRssi();
            }
        };
        lostRemindTime = new Timer();
        lostRemindTime.schedule(lostReimerTask, 300000, 300000);
    }

    private Ringtone ringtone;

    private void lostReminder() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this, notification);
        if (ringtone == null) {
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
        }
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{400, 800, 400, 800, 400, 800, 400, 800, 400, 800}, -1);
    }

    //TODO 设置的回调
    @SuppressLint("HandlerLeak")
    private final Handler settingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x00:

                    break;
                case 0x01:
                    //Toast.makeText(MainService.this, getString(R.string.accessibility_set_success), Toast
                    // .LENGTH_SHORT).show();
                    break;
                case 0x02:
                    ///是否开启了防丢提醒
                    boolean isAntiLost = ConfigHelper.getInstance(MainService.this).getBoolean(ActivityDeviceSetting
                            .DEVICE_ANTILOST, false);
                    if (isAntiLost) {
                        int rssi = msg.arg1;///信号强度
                        Log.e("mainService", "rssi = " + rssi);
                        if (rssi < -100) {
                            isLostReminderMode = true;
                            lostReminder();
                            initLostRemindTimer();
                        } else {
                            isLostReminderMode = false;
                            cancelLostRemindTimer();
                        }
                    } else {
                        isLostReminderMode = false;
                        cancelLostRemindTimer();
                    }
                    break;
                case 0x03:
                    Log.e(TAG, "str ==11111111111111");
                    EventBus.getDefault().post(msg);
                    break;
                case 0x04:
                    Log.e(TAG, "str ==222222222222");
                    EventBus.getDefault().post(msg);
                    break;
                case 0x05:
                    Toast.makeText(MainService.this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                    break;
                case 0x06:
                    LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(new Intent(ACTION_USERSET));
                    Toast.makeText(MainService.this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "str ==333333333333333");
                    EventBus.getDefault().post(msg);
                    break;
                case 0x07:
                    Toast.makeText(MainService.this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                    break;
                case 0x08:
                    Log.e(TAG, "str ==4444444444");
                    EventBus.getDefault().post(msg);
                    break;
                case 0x09:
                    //Toast.makeText(MainService.this,"displaySetting",Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "str ==5555555555555");
                    EventBus.getDefault().post(msg);
                    break;
                case 0x0A:
                    //Toast.makeText(MainService.this, "bleBroadcastNameModify", Toast.LENGTH_SHORT).show();
                    break;
                case 0x0B:
                    //Toast.makeText(MainService.this, "mutilMediaSetting", Toast.LENGTH_SHORT).show();
                    break;
                case 0x0C:
                    //Toast.makeText(MainService.this, "findDeviceResult", Toast.LENGTH_SHORT).show();
                    break;
                case 0x0D:
                    //Toast.makeText(MainService.this, getString(R.string.anti_lost_set_success), Toast.LENGTH_SHORT)
                    // .show();
                    break;
                case 0x0E:
                    Toast.makeText(MainService.this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                    break;
                case 0x10:
                    //Toast.makeText(MainService.this, "deleteDataSetting", Toast.LENGTH_SHORT).show();
                    break;
                case 0x11:
                    findMobilePhone();
                    break;
                case 0x12:
                    Log.e("mainService", "" +
                            "指令有返回");
                    EventBus.getDefault().post(msg);
//                    PackageManager packageManager = getPackageManager();
//                    if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {///判断是否支持相机
//                        Toast.makeText(MainService.this, getString(R.string.not_support_camera), Toast.LENGTH_LONG)
//                                .show();
//                        return;
//                    }
//                    //检查是否有拍照权限
//                    if (ActivityCompat.checkSelfPermission(MainService.this, Manifest.permission.CAMERA) !=
//                            PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(MainService.this, getString(R.string.camera_permission), Toast.LENGTH_SHORT)
//                                .show();
//                        return;
//                    }
//                    //检查是否有拍照权限
//                    if (ActivityCompat.checkSelfPermission(MainService.this, Manifest.permission
//                            .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(MainService.this, getString(R.string.storage_permission), Toast.LENGTH_SHORT)
//                                .show();
//                        return;
//                    }
//                    Log.e("mainService", "有权限");
//                    //获取电源管理器对象
//                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//
//                    //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
//                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager
//                            .SCREEN_BRIGHT_WAKE_LOCK, "bright");
//                    Log.e("mainService", "1111");
//                    //点亮屏幕
//                    wl.acquire();
//                    Log.e("mainService", "2222");
//                    //得到键盘锁管理器对象
//                    KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//                    KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
//
//                    //解锁
//                    kl.disableKeyguard();
//                    Log.e("mainService", "3333");
//                    wl.release();
//                    Log.e("mainService", "4444");
//                    if (!isTopActivity()) {
//                        Log.e("mainService", "5555");
//                        Intent intent = new Intent(MainService.this, CamaraActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    } else {
//                        Log.e("mainService", "6666");
//                        EventBus.getDefault().post(msg);
//                    }
                    break;
                case 0x13:
                    ConfigHelper.getInstance(MainService.this).putBoolean(ActivityDeviceSetting.DEVICE_ANTILOST, msg
                            .arg1 == 1);
                    if (msg.arg1 == 1) {
                        initAntiTimer();
                    } else {
                        cancelAntiTimer();
                        cancelLostRemindTimer();
                    }
                    break;
                case 0x14:
                    Toast.makeText(MainService.this, getString(R.string.connection_successful), Toast.LENGTH_SHORT)
                            .show();
                    Log.e(TAG, "str ==66666666666");
                    EventBus.getDefault().post(msg);
                    break;
                case 0x15:
                    Log.e(TAG, "str ==777777777777777");
                    EventBus.getDefault().post(msg);
                    break;
                case 0x16:
                    //Toast.makeText(MainService.this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                    break;
                case 0x17:
                    Log.i("isSynCom", true + "");
                    EventBus.getDefault().post(true);
                    isSynCom = true;
                    Toast.makeText(MainService.this, getString(R.string.successful_synchronization), Toast
                            .LENGTH_SHORT).show();
                    break;
                case 0x18:
                    Toast.makeText(MainService.this, getString(R.string.synchronization_failure), Toast.LENGTH_SHORT)
                            .show();
                    isSynCom = true;
                    EventBus.getDefault().post(true);
                    break;
                case 0x19:
                    Log.e(TAG, "str ==88888888888888");
                    EventBus.getDefault().post("DE 01 10 ED");
                    break;
            }
        }
    };

    public void onEventMainThread(Message msg) {

    }

    public void onEventMainThread(HeartDataInfo info) {

    }

    private boolean isTopActivity() {
        boolean isTop = false;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().contains(CamaraActivity.class.getName())) {
            isTop = true;
        }
        return isTop;
    }

    private MediaPlayer mMediaPlayer;
    private Vibrator vibrator1;

    private Uri getSystemDefultRingtoneUri() {
        Uri tp = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_RINGTONE);
        if (tp == null) {
            tp = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        if (tp == null) {
            tp = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        return tp;
    }

    /**
     * 停止查找手机的音频播放
     */
    public void stopFindMolibePhone() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        if (vibrator1 != null && vibrator1.hasVibrator()) {
            vibrator1.cancel();
        }
        findPhoneHandler.removeMessages(0x01);
        /*Intent intent = new Intent(DialogFindPhone.ACTION_STOP_FINDPHONE);
        sendBroadcast(intent);*/
    }

    @SuppressLint("HandlerLeak")
    private final Handler findPhoneHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
            if (vibrator1 != null && vibrator1.hasVibrator()) {
                vibrator1.cancel();
            }
            /*Intent intent = new Intent(DialogFindPhone.ACTION_STOP_FINDPHONE);
            sendBroadcast(intent);*/
        }
    };

    public void initFindPhoneDialog() {
        /*Intent intent = new Intent(this, DialogFindPhone.class);
        intent.putExtra(DialogFindPhone.EXTRA_DIALOG_TYPE, DialogFindPhone.DIALOG_TYPE_FINDPHONE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    public void findMobilePhone() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        if (vibrator1 == null) {
            vibrator1 = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);  //音量控制,初始化定义
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//最大音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
        if (currentVolume == 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0); //tempVolume:音量绝值
        }
        if (mMediaPlayer.isPlaying()) {
            stopFindMolibePhone();
        } else {
            initFindPhoneDialog();
            try {
                mMediaPlayer.setDataSource(this, getSystemDefultRingtoneUri());
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            findPhoneHandler.sendEmptyMessageDelayed(0x01, 10000);
            mMediaPlayer.start();
            vibrator1.vibrate(new long[]{700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700, 300, 700,
                    300, 700, 300, 700, 300}, -1);
        }
    }


    public Message getMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        return msg;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * 连接成功调用
     */
    private void lidlConnectSuccess() {
        cancelLeScan();
        Message msg = getMessage(0x14);
        settingHandler.sendMessage(msg);

        cancelAntiTimer();
        cancelLostRemindTimer();
        boolean isAntiLostTemp = ConfigHelper.getInstance(this).getBoolean(ActivityDeviceSetting.DEVICE_ANTILOST,
                false);
        if (isAntiLostTemp) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (getConnectionState() == BaseController.STATE_CONNECTED) {
                        isLostReminderMode = false;
                        ////是否开启了防丢提醒
                        boolean isAntiLost = ConfigHelper.getInstance(MainService.this).getBoolean
                                (ActivityDeviceSetting.DEVICE_ANTILOST, false);
                        if (isAntiLost) {
                            initAntiTimer();
                        }
                    }
                }
            }, 10000);
        }
        batteryHandler.sendEmptyMessageDelayed(0x01, 10000);
        Intent intent3 = new Intent(ACTION_BATTERY_CHANGE);
        intent3.putExtra(EXTRA_BATTERY_LEVEL, batteryLevel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent3);
    }

    @Override
    public boolean startLeScan() {
        return super.startLeScan();
    }


    private final OnDeviceSetting w311DeviceSetting = new OnDeviceSetting() {

        @Override
        public void isReadySync(boolean b) {
            cancelLeScan();
        }

        @Override
        public void isSaveHeartNotify(int isSaveHeartNotify) {
            Log.e("connectVibrate", "是否开启心率存储提醒 == " + isSaveHeartNotify);
            ConfigHelper.getInstance(UIUtils.getContext()).putBoolean(Constants.IS_NOTIF_SAVE_HEART,
                    isSaveHeartNotify != 0);
        }

        @Override
        public void isHeartRateDevice(Boolean state) {
            ConfigHelper.getInstance(UIUtils.getContext()).putBoolean(Constants.IS906901, state);
        }

        @Override
        public void currentDeviceInfo(DeviceInfo deviceInfo) {
            if (deviceInfo != null) {
                //todo 将信息更新到本地
                HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FIREWARE_HIGH, deviceInfo
                        .getFirmwareHighVersion());
                HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FIREWARE_LOW, deviceInfo
                        .getFirmwareLowVersion());
            }
        }

        @Override
        public void onBatteryChanged(int i) {

        }

        @Override
        public void accessibleySetting(int state) {
            settingHandler.sendMessage(getMessage(0x01));
        }

        @Override
        public void readRssiCompleted(int rssi) {
            Message msg = getMessage(0x02);
            msg.arg1 = rssi;
            settingHandler.sendMessage(msg);
        }

        @Override
        public void alarmSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x03));
        }

        @Override
        public void alarmDescripSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x04));
        }

        @Override
        public void onCalibrateSuccessed() {
            settingHandler.sendMessage(getMessage(0x19));
        }

        @Override
        public void onSetHeartRateAutoDownSuccess() {
            settingHandler.sendMessage(getMessage(0x05));
        }

        @Override
        public void wristSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x05));
        }

        @Override
        public void userInfoSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x06));
        }

        @Override
        public void sedentarySetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x07));
        }

        @Override
        public void autoSleepSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x08));
        }

        @Override
        public void displaySetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x09));
        }

        @Override
        public void bleBroadcastNameModify(boolean success) {
            settingHandler.sendMessage(getMessage(0x0A));
        }

        @Override
        public void mutilMediaSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x0B));
        }

        @Override
        public void findDeviceResult(boolean success) {
            settingHandler.sendMessage(getMessage(0x0C));
        }

        @Override
        public void antiLostSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x0D));
        }

        @Override
        public void heartTimingSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x0E));
        }

        @Override
        public void deleteDataSetting(boolean success) {
            settingHandler.sendMessage(getMessage(0x10));
        }

        @Override
        public void findMobilePhone() {
            settingHandler.sendMessage(getMessage(0x11));
        }

        @Override
        public void findMobilePhone(byte b) {

        }

        @Override
        public void takePhoto() {
            settingHandler.sendMessage(getMessage(0x12));
        }

        @Override
        public void antiLost(int state) {
            Message msg = getMessage(0x13);
            msg.arg1 = state;
            settingHandler.sendMessage(msg);
        }

        @Override
        public void customeCmdResult(byte[] datas) {
            if (datas != null) {
                if (datas.length >= 4) {
                    if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x01 && (datas[2] & 0xff) == 0x05 &&
                            (datas[3] & 0xff) == 0xed) {//s.startsWith("DE 01 05 ED")) {//隐私保护

                    } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x01 && (datas[2] & 0xff) == 0x15 &&
                            (datas[3] & 0xff) == 0xed) {//s.startsWith("DE 01 15 ED")) {///心率
                        Message msg = getMessage(0x15);
                        settingHandler.sendMessage(msg);
                    } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x06 && (datas[2] & 0xff) == 0x09 &&
                            (datas[3] & 0xff) == 0xfb) {//s.startsWith("DE 06 09 FB")) {///收到设备信息
                        int info1 = datas[13];
                        int info2 = datas[14];

                        int photo = info1 & 1;
                        int lock = (info1 >> 1) & 1;
                        int vibrate = (info1 >> 2) & 1;
                        int findphone = (info1 >> 3) & 1;
                        int high = (info1 >> 4) & 1;
                        int music = (info1 >> 5) & 1;
                        int bleinterface = (info1 >> 6) & 1;
                        int isprotected = (info1 >> 7) & 1;

                        int menu = (info2) & 1;
                        int heart5Vibrate = (info2 >> 1) & 1;
                        int callMsg = (info2 >> 2) & 1;
                        int connectVibrate = (info2 >> 3) & 1;

                        int hardware = UtilTools.byteToInt(datas[10]);
                        int fireWareHigh = UtilTools.byteToInt(datas[11]);
                        int fireWareLow = UtilTools.byteToInt(datas[12]);

                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_PHOTO, photo);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_KEY_LOCK, lock);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_VIBRATE, vibrate);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FINDPHONE, findphone);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_HIGH, high);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_MUSIC, music);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_BLE_INTERFACE, bleinterface);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_PRIVACY, isprotected);

                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_CONNECT_VIBTATE, 1);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_MENU, 1);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_HEART_VIBRATE, 1);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_CALL_MSG, 1);

                        //LidlDeviceInfo.putInt(MainService.this, LidlDeviceInfo.LIDL_PHOTOMUSIC, photo);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FIREWARE_HIGH, fireWareHigh);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FIREWARE_LOW, fireWareLow);
                        batteryLevel = UtilTools.byteToInt(datas[17]);
                        if (getConnectionState() == BaseController.STATE_CONNECTED) {
                            Intent intent = new Intent(ACTION_BATTERY_CHANGE);
                            intent.putExtra(EXTRA_BATTERY_LEVEL, batteryLevel);
                            LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(intent);
                        }
                    } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x02 && (datas[2] & 0xff) == 0x07 &&
                            (datas[3] & 0xff) == 0xfe) {//s.startsWith("DE 02 07 FE")) {
                        Message msg = getMessage(0x07);
                        settingHandler.sendMessage(msg);
                        byte state = datas[4];
                        int photoMusic = state & 0x01;///照相拍照 默认1
                        int keyLock = state & 0x02;///按键锁，默认为0
                        int vibrate = state & 0x04;///震动，默认1
                        int findPhone = state & 0x08;///是否开启查找手机功能，默认1

                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_KEY_LOCK, keyLock == 0x02 ? 1 : 0);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_PHOTOMUSIC, photoMusic == 0x01 ?
                                1 : 0);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_FINDPHONE, findPhone == 0x08 ? 1
                                : 0);
                        HamaDeviceInfo.putInt(MainService.this, HamaDeviceInfo.LIDL_VIBRATE, vibrate == 0x04 ? 1 : 0);
                    } else if ((datas[0] & 0xff) == 0x86 && (datas[1] & 0xff) == 0x00 && (datas[2] & 0xff) == 0x01 &&
                            (datas[3] & 0xff) == 0x02
                            && (datas[4] & 0xff) == 0xff && (datas[5] & 0xff) == 0xfa && (datas[6] & 0xff) == 0xfc &&
                            (datas[7] & 0xff) == 0xf7 && (datas[8] & 0xff) == 0x00) {
                        //s.equals("86 00 01 02 FF FA FC F7 00")) {
                        sendCustomCmd(new byte[]{(byte) 0x86, 0x00, 0x01, 0x03, (byte) 0xed});
                    } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x06 && (datas[2] & 0xff) == 0x30 &&
                            (datas[3] & 0xff) == 0xed) {//.equals("DE 06 30 ED")) {
                        settingHandler.sendMessage(getMessage(0x16));
                    } else if ((datas[0] & 0xff) == 0xde && (datas[1] & 0xff) == 0x02 && (datas[2] & 0xff) == 0x01 &&
                            (datas[3] & 0xff) == 0xfe) {//s.toString().trim().startsWith("DE 02 01 FE")) {
                        batteryLevel = DeviceInfo.getInstance().getPowerLevel();
                        if (getConnectionState() == BaseController.STATE_CONNECTED) {
                            Intent intent = new Intent(ACTION_BATTERY_CHANGE);
                            intent.putExtra(EXTRA_BATTERY_LEVEL, batteryLevel);
                            LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(intent);
                        }
                    }
                }
            }
        }

        @Override
        public void deivceTimeZoneMetric24Hour(int[] result) {

        }

        @Override
        public void currentUserInfo(int[] result) {

        }

        @Override
        public void onHeartHistorySynced(List<HeartRecord> list) {
            super.onHeartHistorySynced(list);
            Log.e("MainService", Thread.currentThread().getName());
            if (list != null && list.size() > 0) {
                List<HeartHistory> listHistory = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    HeartRecord record = list.get(i);
                    HeartHistory history = new HeartHistory(HeartHistory.TYPE_NORMAL, record.getMac(), record
                            .getStartTime(),
                            record.getDataList(), record.getAvg(), record.getMax(),
                            record.getMin(), record.getTotal(), 0, 1);
                    listHistory.add(history);
                }
                DbHeart.getIntance().saveOrUpdate(listHistory);
                LocalBroadcastManager.getInstance(MainService.this).sendBroadcast(new Intent
                        (ACTION_HEART_HISTORY_SYNCED));
                EventBus.getDefault().post(new ProgressEntry(100));
            }
        }

        @Override
        public void onHeartSyncProgress(int progress) {
            super.onHeartSyncProgress(progress);
            EventBus.getDefault().post(new ProgressEntry(progress));
        }

        @Override
        public void onHeartHistorySynced(int state) {
            super.onHeartHistorySynced(state);
            if (state == OnDeviceSetting.SYNC_HEART_STATE_SUCCESS) {
                EventBus.getDefault().post(new ProgressEntry(100));
            } else if (state == OnDeviceSetting.SYNC_HEART_STATE_FAIL) {
                EventBus.getDefault().post(new ProgressEntry(-1));
            } else if (state == OnDeviceSetting.SYNC_HEART_NODATA) {
                EventBus.getDefault().post(new ProgressEntry(-2));
            }

        }

        @Override
        public void onHeartRateHistorySynced(int state) {
            super.onHeartRateHistorySynced(state);
            if (state == OnDeviceSetting.SYNC_HEART_STATE_SUCCESS) {
                EventBus.getDefault().post(new SyncHeartRate(1));
            } else if (state == OnDeviceSetting.SYNC_HEART_STATE_FAIL) {
                EventBus.getDefault().post(new SyncHeartRate(0));
            }
        }
    };

    public void onEventMainThread(ProgressEntry entry) {

    }


    /**
     * 读取电池电量
     */
    @SuppressLint("HandlerLeak")
    private final Handler batteryHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (getConnectionState() == BaseController.STATE_CONNECTED) {
                //getDeviceInfo();
                //batteryHandler.sendEmptyMessageDelayed(0x01, 10000);
            }
        }
    };

    ///是否已经接收到心率数据
    private boolean hasReceivedHeartData;
    private ArrayList<HeartData> heartDataList;
    private HeartDataInfo heartDataInfo = new HeartDataInfo();
    private boolean isStartHeart;///是否打开心率监测
    private long heartStartTime;///心率开始时间

    public HeartDataInfo getHeartDataInfo() {
        if (heartDataInfo == null) {
            heartDataInfo = new HeartDataInfo();
        }
        return this.heartDataInfo;
    }

    public boolean isStartHeart() {
        return this.isStartHeart;
    }

    public boolean isHasReceivedHeartData() {
        return this.hasReceivedHeartData;
    }

    ///停止心率监测，是否是手動保存
    public void stopHeartMonitor(int type, boolean manual) {
        if (heartHandler.hasMessages(0x01))
            heartHandler.removeMessages(0x01);
        ArrayList<HeartData> tempList = heartDataList;
        BaseDevice currentDevice = getCurrentDevice();
        //if (manual) {
        saveHeartData(currentDevice == null ? null : currentDevice.getMac(), type, tempList);
        /*}
        if (!manual && isStartHeart && heartDataList != null && heartDataList.size()>0) {
            Intent intent = new Intent(this, DialogSaveHrData.class);
            intent.putExtra(DialogSaveHrData.EXTRA_DIALOG_TYPE, DialogSaveHrData.DIALOG_TYPE_SAVE_HRDATA);
            intent.putExtra(DialogSaveHrData.EXTRA_DATA_LIST, tempList);
            intent.putExtra(DialogSaveHrData.EXTRA_INFO, heartDataInfo);
            intent.putExtra(DialogSaveHrData.EXTRA_MAC, currentDevice == null ? null : currentDevice.getMac());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }*/
        isStartHeart = false;
        hasReceivedHeartData = false;
        heartDataList.clear();
        heartDataInfo = new HeartDataInfo();
        if (heartDataList != null) {
            heartDataList.clear();
        }
        EventBus.getDefault().post(new HeartDataInfo());///如果列表大小为0，说明不能接受到数据了，那么在界面上上显示开始
    }

    public void discardHeartData() {
        isStartHeart = false;
        hasReceivedHeartData = false;
        heartDataList.clear();
        heartDataInfo = new HeartDataInfo();
        if (heartDataList != null) {
            heartDataList.clear();
        }
        EventBus.getDefault().post(new HeartDataInfo());
    }

    public boolean startHeartMonitor() {

        heartDataInfo = new HeartDataInfo();
        if (heartDataList == null) {
            heartDataList = new ArrayList<>();
        }
        heartDataList.clear();
        if (isSupportCmdHeart(getCurrentDevice()) && BaseController.STATE_CONNECTED == getConnectionState()) {
            isStartHeart = true;
            return true;
        }
        if (getConnectionState() != BaseController.STATE_CONNECTED || !hasReceivedHeartData) {
            isStartHeart = false;
            return false;
        }
        isStartHeart = true;
        return true;
    }

    ///保存心率数据,保存类型 --- normal,resting,sleep,sport,auto
    public void saveHeartData(final String mac, final int type,
                              final List<HeartData> list) {
        isStartHeart = false;
        if (list == null || list.size() <= 5) {
            heartDataList.clear();
            heartDataInfo = new HeartDataInfo();
            return;
        }
        if (mac != null) {
            /////////////保存数据到数据库/////////////////////
            //int type,String mac,String startDate,List<HeartData> list,int avg,int max,int min
            HeartHistory history = new HeartHistory(type, getCurrentDevice().getMac(),
                    UtilTools.date2String(UtilTools.long2Date(heartDataInfo
                            .getStartTime()
                    ), "yyyy-MM-dd HH:mm:ss"),
                    heartDataList, heartDataInfo.getAvg(), heartDataInfo.getMax(),
                    heartDataInfo.getMin(), heartDataInfo.getTotal(), heartDataInfo
                    .getTotalCal(), 0);
            DbHeart.getIntance().update(history);
            Intent update = new Intent(ACTION_HEART_DATA_UPDATE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(update);
        }
        ////////////////////////////////////////////////
        heartDataList.clear();
        heartDataInfo = new HeartDataInfo();
    }

    ///心率超时定时器，超过十秒没有数据自动保存数据
   /* private Timer heartTimer;
    private TimerTask heartTimerTask;*/
    private Handler heartHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isAutoSaveHeart = ConfigHelper.getInstance(MainService.this).getBoolean(Constants.IS_AUTO_SAVE_HEART,
                    false);
            if (isAutoSaveHeart) {
                Log.e(TAG, "111111111111");
                stopHeartMonitor(0, false);
            } else {
                Log.e(TAG, "222222222222");
                isStartHeart = false;
                hasReceivedHeartData = false;
                EventBus.getDefault().post(new HeartDataInfo());
            }
        }
    };

    private void cancelHeartTimer() {
        if (heartHandler.hasMessages(0x01))
            heartHandler.removeMessages(0x01);
        /*if (heartTimerTask != null) {
            heartTimerTask.cancel();
        }
        if (heartTimer != null) {
            heartTimer.cancel();
        }
        heartTimerTask = null;
        heartTimer = null;*/
    }

    public void initHeartTimer() {
        cancelHeartTimer();
        heartHandler.sendEmptyMessageDelayed(0x01, 5000);
    }

    public HeartDataInfo getHeartDateInfo() {
        return this.heartDataInfo;
    }

    @Override
    public void onHeartChanged(HeartData heartData) {

        Log.e("mainService", UtilTools.date2String(UtilTools.long2Date(heartData.getHeartTime()), "yyyy-MM-dd " +
                "HH:mm:ss") + "  heartrate = " + heartData.getHeartRate());
        if (heartDataList == null) {
            heartDataList = new ArrayList<>();
        }
        if (heartDataInfo == null) {
            heartDataInfo = new HeartDataInfo();
        }
        long currenttime = System.currentTimeMillis();
        //if (isStartHeart) {///手动打开心率监测
        if (heartDataInfo.getDataList().size() == 0) {
            heartDataInfo.setStartTime(currenttime);
        }
        heartDataInfo.setCurrentTime(currenttime);
        /*} else {
            heartDataInfo.setCurrentTime(currenttime);
        }*/
        if (heartDataInfo.getStartTime() == 0) {
            heartDataInfo.setStartTime(currenttime);
        }
        if (heartDataList.size() == 0) {
            heartDataInfo.setTotal(0);
            heartDataInfo.setTotalCal(0);
        }
        int rate = heartData.getHeartRate();
        heartDataList.add(heartData);
        heartDataInfo.getDataList().add(rate);
        UserInfo userInfo = UserInfo.getInstance(this);
        if (getCurrentDevice() != null && getCurrentDevice().getDeviceType() == BaseDevice.TYPE_HEART_RATE) {
            heartDataInfo.setTotalCal((long) (heartDataInfo.getTotal() + (((-55.0969f + 0.6309f * rate + 0.1988f *
                    userInfo.getWeight() + 0.2017f * getAge()) / 4.184f / 60))));
        }
        int max = heartDataInfo.getMax();
        int min = heartDataInfo.getMin();
        int avg = heartDataInfo.getAvg();
        min = (min == 0 ? rate : min);
        min = (min > rate ? rate : min);
        max = (max < rate ? rate : max);
        int size = heartDataList.size();
        if (size == 1) {
            avg = rate;
            heartDataInfo.setTotal(rate);
        } else {
            heartDataInfo.setTotal(heartDataInfo.getTotal() + rate);
            avg = ((int) (heartDataInfo.getTotal() / (size * 1.0f)));
        }

        Log.e("heartdata", "max = " + max + " , avg = " + avg + " , min = " + min);
        heartDataInfo.setAvg(avg);
        heartDataInfo.setMax(max);
        heartDataInfo.setMin(min);
        hasReceivedHeartData = true;
        EventBus.getDefault().post(heartDataInfo);
//        if (isSupportCmdHeart(getCurrentDevice())) {
//            isStartHeart = true;
//        }
        initHeartTimer();
    }

    public int getAge() {
        UserInfo userInfo = UserInfo.getInstance(this);
        String birthDay = userInfo.getBirthday();
        int year = Integer.valueOf(birthDay.split("-")[0]);
        return Calendar.getInstance().get(Calendar.YEAR) - year;
    }

    @Override
    public void onHistHeartData(List<HeartData> list) {

    }

    private MyBinder myBinder;
    private MyServiceConnection myServiceConnection;

    @Override
    public void unBind(BaseDevice device) {
        //unBind(device);
        super.unBind(device);
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        //com.isport.isportlibrary.tools.Constants.IS_DEBUG = false;
        // com.isport.isportlibrary.tools.Constants.IS_DEBUG = true;
        isAutoSaveHeart = ConfigHelper.getInstance(this).getBoolean(Constants.IS_AUTO_SAVE_HEART, false);
        if (!isInit) {
            initDb();
            isInit = true;
        }
        return myBinder;
    }

    class MyBinder extends IRemoteConnection.Stub {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString)
                throws RemoteException {

        }

        @Override
        public String getProcessName() throws RemoteException {
            return "com.sys.keepliveprocess.RemoteService";
        }
    }

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG, "com.isport.tracker.bluetooth.keeplive.RemoteService onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "com.isport.tracker.bluetooth.keeplive.RemoteService onServiceDisconnected");
            MainService.this.bindService(new Intent(MainService.this, RemoteService.class), myServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }
}
