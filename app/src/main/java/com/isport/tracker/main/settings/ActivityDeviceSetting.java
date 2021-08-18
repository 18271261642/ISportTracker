package com.isport.tracker.main.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.isportlibrary.entry.DisplaySet;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.entry.WristMode;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.bluetooth.notifications.NotiServiceListener;
import com.isport.tracker.dialogActivity.DialogSetAge;
import com.isport.tracker.dialogActivity.DialogSetSex;
import com.isport.tracker.dialogActivity.DialogSetTargetActivity;
import com.isport.tracker.entity.HamaDeviceInfo;
import com.isport.tracker.entity.MyBaseDevice;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.main.CalendarTestActivity;
import com.isport.tracker.main.CamaraActivity;
import com.isport.tracker.main.DfuActivity;
import com.isport.tracker.main.DfuBeatActivity;
import com.isport.tracker.main.DisturbWithtimeActivity;
import com.isport.tracker.main.RaiseHandWithtimeActivity;
import com.isport.tracker.main.settings.sport.ActivitySportMode;
import com.isport.tracker.main.settings.sport.ActivityWeather;
import com.isport.tracker.main.settings.sport.AlarmActivity;
import com.isport.tracker.main.settings.sport.AutomaticHeartRateActivity;
import com.isport.tracker.main.settings.sport.BluetoothSwitchActivity;
import com.isport.tracker.main.settings.sport.CalibrateActivity;
import com.isport.tracker.main.settings.sport.ControlThreeActionActivity;
import com.isport.tracker.main.settings.sport.DisplayActivity;
import com.isport.tracker.main.settings.sport.HeartRateAutoActivity;
import com.isport.tracker.main.settings.sport.MessageActivity;
import com.isport.tracker.main.settings.sport.RaiseHandSettingActivity;
import com.isport.tracker.main.settings.sport.ReminderActivity;
import com.isport.tracker.main.settings.sport.ScreenSetting;
import com.isport.tracker.main.settings.sport.SleepActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.TimeUtils;
import com.isport.tracker.util.UIUtils;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.util.Utils;
import com.isport.tracker.view.EasySwitchButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class ActivityDeviceSetting extends BaseActivity implements OnClickListener, EasySwitchButton.OnOpenedListener {

    private static final String TAG = ActivityDeviceSetting.class.getSimpleName();
    public static String ACTION_ANTI_LOST = "com.isport.tracker.main.settings.ActivityDeviceSetting.ACTION_ANTI_LOST";
    public static String ACTION_STEP_TARGET_CHANGE = "com.isport.tracker.main.settings.ActivityDeviceSetting" +
            ".ACTION_STEP_TARGET_CHANGE";
    public static String ACTION_HEART_AUTO_SAVE = "com.isport.tracker.main.settings.ActivityDeviceSetting" +
            ".ACTION_HEART_AUTO_SAVE";///自动保存心率的设置变化
    public static String EXTRA_HEART_AUTO_SAVE = "com.isport.tracker.main.settings.ActivityDeviceSetting" +
            ".EXTRA_HEART_AUTO_SAVE";
    public static String DEVICE_ANTILOST = "device_antilost";
    public static String MUTILE_MEDIA_CONTROL = "MUTILE_MEDIA_CONTROL";

    private View lyGoal, lyLeftRight, lyDistance, lyRemind, lyAlarm, lySleep, lyDisplay, lyUnbind, lyCalibrate,
            lyBleSwitch, layout_automatic_heart_rate_open_interval, lThridMessage, lWheather, lSportMode, lPointDial, lMemorySportData;
    private View lyAutoHeartRateTest, lyFindDevice, lyControlCallSms, lyAccess, lyAntiLostDevice, lyScreenSet,
            lyFactory, llOta;

    private EasySwitchButton esbSwitch, esbAccess, esbAntilost, esbAutoSaveHeart, esbHeartSave, esbHeartAutoDown, esb_pointer_dial;
    private View lyAutoSaveHrData;
    private View lyHeartSaveTip;
    private View lyHeartAutoDown;
    public static final String SET_BY_USER = "set_by_user";//首次启动APP时，bleservice sendAccess一次指令;假如用户操作过该设置,就不再发送access同步
    private ViewGroup mSelectedView;
    private int metric;
    private TextView tvTitle;
    MyBaseDevice myBaseDevice = null;
    public final int FIREWARE_HIGH = 90;
    public int FIREWARE_LOW = 46;
    private EasySwitchButton esb_raiseHand_switch;
    private EasySwitchButton esb_heartRate_switch;
    private TextView tv_clear_date;
    private TextView tv_clear;
    private TextView tv_read_date;
    private TextView tv_index;
    private TextView tv_read;
    private Calendar mClearCalendar;
    private Calendar mReadCalendar;
    private int mReadIndex;
    private View layout_raiseHand;
    private View layout_clearHeartRateData;
    private View layout_heartRate;
    private View layout_readHeartRateData;
    private boolean canClick = false;
    private boolean m5minType = false;
    private View layout_raiseHand_withtime;
    private View layout_setDisturb_withtime;


    @SuppressLint("HandlerLeak")
    private final Handler unbindHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0x00){
                try {
                    BaseDevice baseDevice = (BaseDevice) msg.obj;
                    if(baseDevice == null)
                        return;
                    MainService mainService = MainService.getInstance(ActivityDeviceSetting.this);
                    if(mainService == null)
                        return;
                    mainService.unBind(baseDevice);
                }catch (Exception e){
                    e.printStackTrace();
                }

                finally {
                    finish();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserInfo userInfo = UserInfo.getInstance(this);
        metric = userInfo.getMetricImperial();
        setContentView(R.layout.activity_settings_connected);

        initControl();
        initDevice();
        initValue();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (alertDialog2 != null && alertDialog2.isShowing()) {
            alertDialog2.dismiss();
        }
    }

    private void initControl() {
        tvTitle = (TextView) findViewById(R.id.title_text);

        tv_clear_date = (TextView) findViewById(R.id.tv_clear_date);

        tv_clear = (TextView) findViewById(R.id.tv_clear);
        tv_read_date = (TextView) findViewById(R.id.tv_read_date);
        tv_index = (TextView) findViewById(R.id.tv_index);
        tv_read = (TextView) findViewById(R.id.tv_read);
        mReadCalendar = Calendar.getInstance();
        mClearCalendar = Calendar.getInstance();
        String todayYYYYMMDD = TimeUtils.getTodayYYYYMMDD();
        tv_clear_date.setText(todayYYYYMMDD);
        tv_read_date.setText(todayYYYYMMDD);
        tv_index.setText("0");

        layout_raiseHand = findViewById(R.id.layout_raiseHand);///抬腕开关
        layout_heartRate = findViewById(R.id.layout_heartRate);///整点心率存储开关
        layout_clearHeartRateData = findViewById(R.id.layout_clearHeartRateData);///清除历史心率数据
        layout_readHeartRateData = findViewById(R.id.layout_readHeartRateData);///读取历史心率数据


        lyGoal = findViewById(R.id.target_layout);///目标
        lyDistance = findViewById(R.id.foot_layout);///步距
        lyLeftRight = findViewById(R.id.left_right_device);//左右手
        lyRemind = findViewById(R.id.long_time_alert);///久坐提醒
        lyAlarm = findViewById(R.id.alarm_layout);///闹钟
        layout_automatic_heart_rate_open_interval = findViewById(R.id.layout_automatic_heart_rate_open_interval);///闹钟
        lySleep = findViewById(R.id.layout_sleep);///睡眠
        lyDisplay = findViewById(R.id.layout_display);///界面显示
        lyAccess = findViewById(R.id.layout_access);//多媒体控制
        lyAutoHeartRateTest = findViewById(R.id.lly_auto_heart_rate_test);///心率测试
        lyAutoSaveHrData = findViewById(R.id.lly_auto_save_hr_data);///自动保存心率
        lyHeartSaveTip = findViewById(R.id.layout_heart_save_tip);///w354心率存储提醒18小时 感叹号  20小时则满了
        lyHeartAutoDown = findViewById(R.id.layout_heart_auto_down);///beat手动开启心率5分钟后自动关闭
        lyControlCallSms = findViewById(R.id.layout_control_call_sms);///勿扰模式
        lyFindDevice = findViewById(R.id.lly_find_device);///查找设备
        lyAntiLostDevice = findViewById(R.id.layout_AntiLost_device);///防丢提醒
        lyUnbind = findViewById(R.id.unBond_layout);
        lyScreenSet = findViewById(R.id.layout_screenset);
        lyCalibrate = findViewById(R.id.calibration_layout);
        lyBleSwitch = findViewById(R.id.bluetooth_layout);
        lyFactory = findViewById(R.id.factory_linear);
        llOta = findViewById(R.id.lly_ota);
        //w520设置
        lThridMessage = findViewById(R.id.layout_message);//第三方应用打开
        lWheather = findViewById(R.id.layout_wheather);//设置天气
        lSportMode = findViewById(R.id.layout_open_sport_mode);//设置运动模式
        lMemorySportData = findViewById(R.id.layout_read_memory_sport_data);
        lPointDial = findViewById(R.id.layout_pointer_dial);
        /**** 设置 **/
        layout_raiseHand_withtime = findViewById(R.id.layout_raiseHand_withtime);
        layout_setDisturb_withtime = findViewById(R.id.layout_setDisturb_withtime);

       /* tvDistance = (TextView) findViewById(R.id.foot_distance);///步长
        tvGoal = (TextView) findViewById(R.id.target_distance);///目标步数
        tvLeftRight = (TextView) findViewById(R.id.left_right_text);///左右手*/

        esbAutoSaveHeart = (EasySwitchButton) findViewById(R.id.esb_switch);//自动存储心率
        esbAutoSaveHeart.setOnCheckChangedListener(this);

        esbAccess = (EasySwitchButton) findViewById(R.id.esb_access_switch);
        esbAccess.setOnCheckChangedListener(this);

        esbAntilost = (EasySwitchButton) findViewById(R.id.esb_Antilost_switch);
        esbAntilost.setOnCheckChangedListener(this);

        esbHeartSave = (EasySwitchButton) findViewById(R.id.esb_heart_save_switch);
        esbHeartSave.setOnCheckChangedListener(this);

        esbHeartAutoDown = (EasySwitchButton) findViewById(R.id.esb_heart_autodown_switch);
        esbHeartAutoDown.setOnCheckChangedListener(this);

        esb_raiseHand_switch = (EasySwitchButton) findViewById(R.id.esb_raiseHand_switch);
        esb_raiseHand_switch.setOnCheckChangedListener(this);

        esb_heartRate_switch = (EasySwitchButton) findViewById(R.id.esb_heartRate_switch);
        esb_heartRate_switch.setOnCheckChangedListener(this);

        esb_pointer_dial = findViewById(R.id.esb_pointer_dial);
        esb_pointer_dial.setOnCheckChangedListener(this);

        layout_raiseHand.setOnClickListener(this);

        MainService mainService = MainService.getInstance(this);

        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ACTIVA_T)) {
            lyFactory.setVisibility(View.VISIBLE);
        } else {
            lyFactory.setVisibility(View.GONE);
        }

        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_REFLEX)) {
            llOta.setVisibility(View.VISIBLE);
        } else {
            llOta.setVisibility(View.VISIBLE);
        }
    }

    private float getVersion() {
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        float version = Float.valueOf(deviceInfo.getFirmwareHighVersion() + "." + deviceInfo.getFirmwareLowVersion());
        return version;
    }

    private void initValue() {
        UserInfo userInfo = UserInfo.getInstance(this);
        esb_raiseHand_switch.setStatus(ConfigHelper.getInstance(this).getBoolean(Constants.IS_RAISEHAND, false));
        esb_pointer_dial.setStatus(ConfigHelper.getInstance(this).getBoolean(Constants.IS_POINTER_DIAL, false));
        esb_heartRate_switch.setStatus(ConfigHelper.getInstance(this).getBoolean(Constants.IS_HEARTRATE, false));
        esbAutoSaveHeart.setStatus(ConfigHelper.getInstance(this).getBoolean(Constants.IS_AUTO_SAVE_HEART, false));
        esbHeartSave.setStatus(ConfigHelper.getInstance(this).getBoolean(Constants.IS_NOTIF_SAVE_HEART, false));
        esbHeartAutoDown.setStatus(ConfigHelper.getInstance(this).getBoolean(Constants.IS_HEART_AUTODOWN, false));
        //默认出厂是关闭
        esbAccess.setStatus(ConfigHelper.getInstance(this).getBoolean(MUTILE_MEDIA_CONTROL, false));
        esbAntilost.setStatus(ConfigHelper.getInstance(this).getBoolean(DEVICE_ANTILOST, false));
    }

    public void initDevice() {
        if (!ConfigHelper.getInstance(this).getString(Constants.INFO_CURRENT_DEVICE, "").equals("")) {
            myBaseDevice = new Gson().fromJson(ConfigHelper.getInstance(this).getString(Constants
                            .INFO_CURRENT_DEVICE,
                    ""), MyBaseDevice.class);
        }
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            //tvTitle.setText("338852");
            BaseDevice bd = mainService.getCurrentDevice();
            if (myBaseDevice == null || (myBaseDevice != null && !bd.getMac().equals(myBaseDevice.getMac()))) {
                tvTitle.setText(Utils.replaceDeviceNameToCC431(bd.getName(), 0));
            } else {
                tvTitle.setText(Utils.replaceDeviceNameToCC431(mainService.getCurrentDevice().getName(), myBaseDevice
                        .getVersion()));
            }

        } else {
            tvTitle.setText("");
        }
        if (myBaseDevice != null) {
            int devicetype = myBaseDevice.getDeviceType();
            String dname = myBaseDevice.getName();

            lyLeftRight.setVisibility(View.GONE);//左右手
            lyRemind.setVisibility(View.GONE);///久坐提醒
            lyAlarm.setVisibility(View.GONE);///闹钟
            lySleep.setVisibility(View.GONE);///睡眠
            lyDisplay.setVisibility(View.GONE);///界面显示
            lyAccess.setVisibility(View.GONE);//多媒体控制
            lyAutoHeartRateTest.setVisibility(View.GONE);///心率测试
            lyAutoSaveHrData.setVisibility(View.GONE);///自动保存心率
            lyControlCallSms.setVisibility(View.GONE);///勿扰模式
            lyFindDevice.setVisibility(View.GONE);///查找设备
           // lyAntiLostDevice.setVisibility(View.GONE);///防丢提醒
            lyScreenSet.setVisibility(View.GONE);
            lyCalibrate.setVisibility(View.GONE);
            lyBleSwitch.setVisibility(View.GONE);
            lyHeartSaveTip.setVisibility(View.GONE);//心率存储提醒
            lyHeartAutoDown.setVisibility(View.GONE);//手动开启心率5分钟自动关闭开关
            layout_automatic_heart_rate_open_interval.setVisibility(View.GONE);// 自动心率开启指令和监测间隔时间设置

            if (devicetype == BaseDevice.TYPE_W194) {
                lyScreenSet.setVisibility(View.VISIBLE);
            }
            if (devicetype == BaseDevice.TYPE_W311T) {
                lyLeftRight.setVisibility(View.VISIBLE);//左右手
                lyRemind.setVisibility(View.VISIBLE);///久坐提醒
                lyAlarm.setVisibility(View.VISIBLE);///闹钟
                lySleep.setVisibility(View.VISIBLE);///睡眠
                lyDisplay.setVisibility(View.VISIBLE);///界面显示
                lyAutoHeartRateTest.setVisibility(View.VISIBLE);///心率测试
                lyAutoSaveHrData.setVisibility(View.VISIBLE);///自动保存心率
                lyControlCallSms.setVisibility(View.VISIBLE);///勿扰模式
                lyFindDevice.setVisibility(View.VISIBLE);///查找设备
            } else if (devicetype == BaseDevice.TYPE_W311N || devicetype == BaseDevice.TYPE_AT200) {

                if (dname.contains("W520") || getVersion() >= 91.63) {
                    lyLeftRight.setVisibility(View.GONE);//左右手
                    lySleep.setVisibility(View.GONE);///睡眠
                    lyControlCallSms.setVisibility(View.VISIBLE);///勿扰模式
                    lThridMessage.setVisibility(View.VISIBLE);

                    lSportMode.setVisibility(View.VISIBLE);
                    lMemorySportData.setVisibility(View.VISIBLE);
                    lPointDial.setVisibility(View.VISIBLE);
                    lWheather.setVisibility(View.VISIBLE);
                    //需要加消息提醒
                    //需要加来电提醒
                    //需要加短信提醒
                } else {
                    lyLeftRight.setVisibility(View.VISIBLE);//左右手
                    lySleep.setVisibility(View.VISIBLE);///睡眠
                    lThridMessage.setVisibility(View.GONE);
                    lyControlCallSms.setVisibility(View.VISIBLE);///勿扰模式
                }

                lyRemind.setVisibility(View.VISIBLE);///久坐提醒
                lyAlarm.setVisibility(View.VISIBLE);///闹钟

                lyDisplay.setVisibility(View.VISIBLE);///界面显示
                lyAccess.setVisibility(View.VISIBLE);//多媒体控制
                lyAutoHeartRateTest.setVisibility(View.VISIBLE);///心率测试
                lyAutoSaveHrData.setVisibility(View.VISIBLE);///自动保存心率

                lyFindDevice.setVisibility(View.VISIBLE);///查找设备
                lyAntiLostDevice.setVisibility(View.VISIBLE);///防丢提醒
            } else if (devicetype == BaseDevice.TYPE_AS97) {
                lyRemind.setVisibility(View.VISIBLE);///久坐提醒
                lyAlarm.setVisibility(View.VISIBLE);///闹钟
                lySleep.setVisibility(View.VISIBLE);///睡眠
                lyDisplay.setVisibility(View.VISIBLE);///界面显示
                lyAccess.setVisibility(View.VISIBLE);//多媒体控制
                lyAutoHeartRateTest.setVisibility(View.VISIBLE);///心率测试
                lyAutoSaveHrData.setVisibility(View.VISIBLE);///自动保存心率
                lyControlCallSms.setVisibility(View.VISIBLE);///勿扰模式
                lyFindDevice.setVisibility(View.VISIBLE);///查找设备
                lyAntiLostDevice.setVisibility(View.VISIBLE);///防丢提醒
                if (ConfigHelper.getInstance(this).getBoolean(Constants.IS906901, false)) {
                    lyHeartSaveTip.setVisibility(View.VISIBLE);///心率存储提醒 90.69.01独有
                }
                if (dname.contains("W311N_")) {
                    lyLeftRight.setVisibility(View.VISIBLE);//左右手
                }
            } else if (devicetype == BaseDevice.TYPE_W285S || devicetype == BaseDevice.TYPE_SAS80) {
                if (dname.contains("W240N")) {
                    lyLeftRight.setVisibility(View.VISIBLE);
                    lyRemind.setVisibility(View.VISIBLE);
                    lyAlarm.setVisibility(View.VISIBLE);
                    lyDisplay.setVisibility(View.VISIBLE);
                } else {
                    lyLeftRight.setVisibility(View.VISIBLE);
                    lyRemind.setVisibility(View.VISIBLE);
                    lyAlarm.setVisibility(View.VISIBLE);
                    lySleep.setVisibility(View.VISIBLE);
                    lyDisplay.setVisibility(View.VISIBLE);
                    lyControlCallSms.setVisibility(View.VISIBLE);
                }
            } else if (devicetype == BaseDevice.TYPE_W301S) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
                lyControlCallSms.setVisibility(View.VISIBLE);
                lyAntiLostDevice.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_W285B) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_W307S || devicetype == BaseDevice.TYPE_W307S_SPACE || devicetype
                    == BaseDevice.TYPE_W240S) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
                lyControlCallSms.setVisibility(View.VISIBLE);
                //refrex
                String tpName = (dname == null ? "" : dname.contains("_") ? dname.split("_")[0] : dname.contains("-") ?
                        dname.split("-")[0] : dname.split(" ")[0]).toLowerCase();
                ;
                if (dname.contains("REFLEX"))
                    lyFindDevice.setVisibility(View.VISIBLE);
                if (tpName.contains("rush")) {
                    llOta.setVisibility(View.GONE);
                    lyFindDevice.setVisibility(View.VISIBLE);
                }

            } else if (devicetype == BaseDevice.TYPE_W307N) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_AT100) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
                lyControlCallSms.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_W307H || devicetype == BaseDevice.TYPE_W301H) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.GONE);
                lyControlCallSms.setVisibility(View.VISIBLE);

            } else if (devicetype == BaseDevice.TYPE_W337B) {
                lySleep.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lyControlCallSms.setVisibility(View.VISIBLE);
                lyAntiLostDevice.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);

            } else if (devicetype == BaseDevice.TYPE_W194) {
                lyScreenSet.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);

            } else if (devicetype == BaseDevice.TYPE_W240N) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_ACTIVITYTRACKER) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_P118) {

            } else if (devicetype == BaseDevice.TYPE_MILLIONPEDOMETER) {

            } else if (devicetype == BaseDevice.TYPE_W301N) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_W307N) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_W307H || devicetype == BaseDevice.TYPE_W301H) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.GONE);
            } else if (devicetype == BaseDevice.TYPE_W240N) {
                lyLeftRight.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_W240B) {
                lyControlCallSms.setVisibility(View.VISIBLE);
                lyDisplay.setVisibility(View.VISIBLE);
                lyLeftRight.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);
            } else if (devicetype == BaseDevice.TYPE_W316) {
                lyGoal.setVisibility(View.VISIBLE);
                lyRemind.setVisibility(View.VISIBLE);
                lyAlarm.setVisibility(View.VISIBLE);
                lyCalibrate.setVisibility(View.VISIBLE);
                lyBleSwitch.setVisibility(View.VISIBLE);
                lySleep.setVisibility(View.VISIBLE);

            } else if (devicetype == BaseDevice.TYPE_HEART_RATE) {
                lyAutoSaveHrData.setVisibility(View.VISIBLE);
                lyGoal.setVisibility(View.GONE);
                lyDistance.setVisibility(View.GONE);
            }

            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ACTIVA_T) && (devicetype == BaseDevice.TYPE_W307S ||
                    devicetype == BaseDevice.TYPE_W307S_SPACE)) {
                lyControlCallSms.setVisibility(View.GONE);
            }

            if (dname.equalsIgnoreCase(Constants.DEVIE_P674A)) {
                lySleep.setVisibility(View.GONE);
            }
            if (dname.contains("W311N_")) {// W311N -MC-V   BEAT  91.00 版本及以上加
                Log.e(TAG, " version " + getVersion());
                if (getVersion() >= 91.12f) {
                    canClick = true;
                    esb_raiseHand_switch.setVisibility(View.GONE);
                } else if (getVersion() >= 91.00f && getVersion() < 91.12f) {
                    esb_raiseHand_switch.setVisibility(View.VISIBLE);
                    canClick = false;
                }
                layout_raiseHand.setVisibility(View.VISIBLE);
                layout_heartRate.setVisibility(View.VISIBLE);
//                layout_clearHeartRateData.setVisibility(View.VISIBLE);
//                layout_readHeartRateData.setVisibility(View.VISIBLE);
            }
            if (dname.contains("SAS87")) {
                canClick = true;
                lyLeftRight.setVisibility(View.VISIBLE);
                esb_raiseHand_switch.setVisibility(View.GONE);
                layout_raiseHand.setVisibility(View.VISIBLE);
                lyHeartSaveTip.setVisibility(View.VISIBLE);
            }
            if (dname.contains("BEAT")) {// W311N -MC-V   BEAT  91.00 版本及以上加
                Log.e(TAG, " version " + getVersion());
                layout_raiseHand.setVisibility(View.VISIBLE);
                lyHeartAutoDown.setVisibility(View.GONE);
                layout_heartRate.setVisibility(View.VISIBLE);
                layout_automatic_heart_rate_open_interval.setVisibility(View.GONE);
                if (getVersion() >= 91.00f && getVersion() < 91.12f) {
                    esb_raiseHand_switch.setVisibility(View.VISIBLE);
                    canClick = false;
                } else if (getVersion() >= 91.12f && getVersion() < 91.26f) {
                    canClick = true;
                    esb_raiseHand_switch.setVisibility(View.GONE);
                } else if (getVersion() >= 91.26f) {
                    canClick = true;
                    esb_raiseHand_switch.setVisibility(View.GONE);
                    lyHeartAutoDown.setVisibility(View.VISIBLE);
                    layout_heartRate.setVisibility(View.GONE);
                    layout_automatic_heart_rate_open_interval.setVisibility(View.VISIBLE);
                    if (getVersion() > 91.45f) {
                        m5minType = true;
                    } else {
                        m5minType = false;
                    }
                }
                if (getVersion() >= 91.70f) {
                    layout_raiseHand.setVisibility(View.GONE);
                    lySleep.setVisibility(View.GONE);
                    layout_raiseHand_withtime.setVisibility(View.VISIBLE);
                    layout_setDisturb_withtime.setVisibility(View.VISIBLE);
                } else {
                    layout_raiseHand_withtime.setVisibility(View.GONE);
                    layout_setDisturb_withtime.setVisibility(View.GONE);
                }
//                layout_clearHeartRateData.setVisibility(View.VISIBLE);
//                layout_readHeartRateData.setVisibility(View.VISIBLE);
            }


            if (dname.contains("REFLEX") && getVersion() >= 90.70f) {// REFLEX 90.70 版本及以上加 抬手
                Log.e(TAG, " version " + getVersion());
                if (getVersion() >= 90.88f) {
                    esb_raiseHand_switch.setVisibility(View.GONE);
                    canClick = true;
                } else if (getVersion() >= 90.70f && getVersion() < 90.88f) {
                    canClick = false;
                    esb_raiseHand_switch.setVisibility(View.VISIBLE);
                }
                layout_raiseHand.setVisibility(View.VISIBLE);
                if (getVersion() >= 91.02f) {
                    layout_raiseHand.setVisibility(View.GONE);
                    lySleep.setVisibility(View.GONE);
                    layout_raiseHand_withtime.setVisibility(View.VISIBLE);
                    layout_setDisturb_withtime.setVisibility(View.VISIBLE);
                } else {
                    layout_raiseHand_withtime.setVisibility(View.GONE);
                    layout_setDisturb_withtime.setVisibility(View.GONE);
                }
//                layout_heartRate.setVisibility(View.VISIBLE);
//                layout_clearHeartRateData.setVisibility(View.VISIBLE);
//                layout_readHeartRateData.setVisibility(View.VISIBLE);
            }
            String tpName = (dname == null ? "" : dname.contains("_") ? dname.split("_")[0] : dname.contains("-") ?
                    dname.split("-")[0] : dname.split(" ")[0]).toLowerCase();
            if (tpName.equalsIgnoreCase("rush")) {// REFLEX 90.70 版本及以上加 抬手
                Log.e(TAG, " version " + getVersion());
                //默认抬腕和睡眠模式
                esb_raiseHand_switch.setVisibility(View.GONE);
                canClick = true;
                layout_raiseHand.setVisibility(View.VISIBLE);
//                layout_heartRate.setVisibility(View.VISIBLE);
//                layout_clearHeartRateData.setVisibility(View.VISIBLE);
//                layout_readHeartRateData.setVisibility(View.VISIBLE);
            }

           /* if (dname.contains("BEAT")) {
                llOta.setVisibility(View.VISIBLE);
            }*/

            if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                int type = mainService.getCurrentDevice().getDeviceType();
                if ((type == BaseDevice.TYPE_AS97 || type == BaseDevice.TYPE_AT200 || type == BaseDevice.TYPE_W311N)
                        && getVersion() >= 89) {
                    lyAccess.setVisibility(View.GONE);
                }
            }

            if (devicetype == BaseDevice.TYPE_AT200 || devicetype == BaseDevice.TYPE_AS97) {
                lyAccess.setVisibility(View.GONE);
            }
            if (dname.equalsIgnoreCase("w301h") || dname.equalsIgnoreCase("w307h")) {
                lyDisplay.setVisibility(View.GONE);
            }

        }
        //lyControlCallSms.setVisibility(View.GONE);
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        MainService mainService = MainService.getInstance(this);
        int id = v.getId();
        Intent intent = null;
        switch (id) {
            case R.id.esb_pointer_dial:
                if (isConnected()) {
                    ConfigHelper.getInstance(this).putBoolean(Constants.IS_POINTER_DIAL, isOpened);
                    mainService.setPointerdial(isOpened);
                    Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "连接设备", Toast.LENGTH_SHORT).show();
                    esb_pointer_dial.setStatus(!isOpened);
                }
                break;
            case R.id.esb_access_switch:
                if (isConnected()) {
                    ConfigHelper.getInstance(this).putBoolean(MUTILE_MEDIA_CONTROL, isOpened);
                    mainService.setAccessibley(isOpened ? 1 : 0);
                } else {
                    esbAccess.setStatus(!isOpened);
                }
                break;
            case R.id.esb_Antilost_switch:
                if (isConnected()) {
                    ConfigHelper.getInstance(this).putBoolean(DEVICE_ANTILOST, isOpened);
                    intent = new Intent();
                    intent.setAction(ACTION_ANTI_LOST);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    mainService.setAntiLost(isOpened ? 1 : 0);
                } else {
                    esbAntilost.setStatus(!isOpened);
                }
                break;
            case R.id.esb_heart_save_switch:
                if (isConnected()) {
                    //连接状态设置w354 W311N_MAC设备是否打开提醒
                    ConfigHelper.getInstance(this).putBoolean(Constants.IS_NOTIF_SAVE_HEART, isOpened);
                    DeviceInfo deviceInfo = DeviceInfo.getInstance();
                    deviceInfo.setStateConnectVibrate(isOpened ? 1 : 0);
                    mainService.setDeviceInfo(deviceInfo);
                    if (isOpened) {
                        Toast.makeText(ActivityDeviceSetting.this, UIUtils.getString(R.string.device_id), Toast
                                .LENGTH_LONG).show();
                    }
                } else {
                    esbAutoSaveHeart.setStatus(!isOpened);
                }
                break;
            case R.id.esb_heart_autodown_switch:
                if (isConnected()) {
                    //连接状态设置w354 W311N_MAC设备是否打开提醒
                    ConfigHelper.getInstance(this).putBoolean(Constants.IS_HEART_AUTODOWN, isOpened);
//                    DeviceInfo deviceInfo = DeviceInfo.getInstance();
//                    deviceInfo.setStateConnectVibrate(isOpened ? 1 : 0);
                    mainService.setHeartRateAutoDown(isOpened);
                } else {
                    esbHeartAutoDown.setStatus(!isOpened);
                }
                break;
            case R.id.esb_switch:
                ConfigHelper.getInstance(this).putBoolean(Constants.IS_AUTO_SAVE_HEART, isOpened);
                intent = new Intent();
                intent.setAction(ACTION_HEART_AUTO_SAVE);
                intent.putExtra(EXTRA_HEART_AUTO_SAVE, isOpened);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                break;
            case R.id.esb_raiseHand_switch:
                ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND, isOpened);
                BaseController currentController = mainService.getCurrentController();
                if (currentController != null) {
                    ((CmdController) currentController).raiseHand(isOpened);
                }
                break;
            case R.id.esb_heartRate_switch:
                ConfigHelper.getInstance(this).putBoolean(Constants.IS_HEARTRATE, isOpened);
                BaseController currentController1 = mainService.getCurrentController();
                if (currentController1 != null) {
                    ((CmdController) currentController1).heartRate(isOpened);
                }
                break;

              /*  ConfigHelper.getInstance(this).putBoolean(Constants.IS_MESSAGE, isOpened);
                BaseController currentControllerMessage = mainService.getCurrentController();
                if (currentControllerMessage != null) {
                    ((CmdController) currentControllerMessage).heartRate(isOpened);
                }*/

        }
    }

    public void setSubVuewSelected(boolean selected) {
        if (mSelectedView == null)
            return;
        mSelectedView.setSelected(selected);
        for (int i = 0; i < mSelectedView.getChildCount(); i++) {
            mSelectedView.getChildAt(i).setSelected(selected);
        }
    }

    AlertDialog alertDialog2 = null;

    private final byte[] viberByte = new byte[]{(byte) 0xBE,0x06,0x0F, (byte) 0xED};

    private void showUpairDialog() {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage(getString(R.string.unbind) + "?");
        builder2.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder2.setPositiveButton(getString(R.string.unbind), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainService mainService = MainService.getInstance(ActivityDeviceSetting.this);
                try {
                    if (mainService != null) {

                        mainService.sendCustomCmd(viberByte);
                        BaseDevice baseDevice = mainService.getCurrentDevice();

                        Message message = unbindHandler.obtainMessage();
                        message.obj = baseDevice;
                        message.what = 0x00;
                        unbindHandler.sendMessageDelayed(message,1500);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }


            }
        });
        alertDialog2 = builder2.create();
        alertDialog2.show();
    }

    AlertDialog alertDialog3 = null;

    private void showFactoryDialog() {
        if (alertDialog3 == null) {
            AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
            builder3.setMessage(getString(R.string.will_lost_data));
            builder3.setTitle(getString(R.string.return_factory));
            builder3.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainService mainService = MainService.getInstance(ActivityDeviceSetting.this);
                    if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                        byte[] bt = new byte[]{(byte) 0xff, (byte) 0xfa, (byte) 0xfc, (byte) 0xf7, 0x00, 0x01, 0x02,
                                0x07, 0x55, 0x33, 0x66, 0x31, 0x18, (byte) 0x89, 0x60, 0x00};
                        mainService.sendCustomCmd(bt);

                    } else {
                        Toast.makeText(ActivityDeviceSetting.this, getString(R.string.please_bind), Toast
                                .LENGTH_LONG).show();
                    }
                }
            });
            builder3.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog3 = builder3.create();
        }
        alertDialog3.show();
    }

    private boolean isConnected() {
        MainService mainService = MainService.getInstance(this);
        if (mainService == null || (mainService != null && mainService.getConnectionState() != BaseController
                .STATE_CONNECTED)) {
            Toast.makeText(this, getString(R.string.please_bind), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    public void onClick(View arg0) {
        Intent intent = null;
        UserInfo userInfo = UserInfo.getInstance(this);
        MainService mainService = MainService.getInstance(this);
        switch (arg0.getId()) {
            //运动模式
            case R.id.layout_open_sport_mode:
                if (isConnected()) {
                    intent = new Intent(this, ActivitySportMode.class);
                    startActivity(intent);
                }
                break;
            //天气设置
            case R.id.layout_wheather:
                if (isConnected()) {
                    intent = new Intent(this, ActivityWeather.class);
                    startActivity(intent);
                }
                break;
            //自定义表盘设置
            case R.id.layout_pointer_dial:
                break;
            //运动存储获取
            case R.id.layout_read_memory_sport_data:

                if (isConnected()) {
                    MainService.getInstance(this).setReadMemorySportData();
                    Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "连接设备", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.layout_message:
                if (!NotiServiceListener.isEnabled(this)) {
                    // Logger.myLog("没有打开权限，要去打开权限");
                    Intent intents = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intents);
                } else {
                    //已经打开了权限
                    //Logger.myLog("已有权限，打开信息通知");
                    //跳转到第三方APP通知页面
                   /* ConfigHelper.getInstance(this).putBoolean(Constants.IS_THRID, isOpened);
                    ConfigHelper helperthird = ConfigHelper.getInstance(this);
                    helperthird.putBoolean(KEY_SHOW_SMS + myBaseDevice.getMac(), isOpened);
                    clickSave();*/
                    intent = new Intent(this, MessageActivity.class);
                    startActivity(intent);
                }

                break;
            case R.id.back_tv:
                finish();
                break;
            case R.id.layout_raiseHand:
                // TODO: 2018/12/7 跳转到设置页面
                if (canClick) {
                    intent = new Intent(this, RaiseHandSettingActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layout_raiseHand_withtime:
                // TODO: 2018/12/7 跳转到设置页面
                if (canClick) {
                    intent = new Intent(this, RaiseHandWithtimeActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layout_setDisturb_withtime:
                // TODO: 2018/12/7 跳转到设置页面
                if (canClick) {
                    intent = new Intent(this, DisturbWithtimeActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layout_automatic_heart_rate_open_interval:
                if (isConnected()) {
                    intent = new Intent(this, AutomaticHeartRateActivity.class);
                    intent.putExtra("m5minType", m5minType);
                    startActivity(intent);
                }
                break;
            case R.id.tv_clear_date:
                intent = new Intent(this, CalendarTestActivity.class);
                String trim = tv_clear_date.getText().toString().trim();
                Calendar calendar = Calendar.getInstance();
                if (UIUtils.getString(R.id.date).equals(trim)) {
                    //没有选择时，为当前时间
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date date = format.parse(trim);
                        calendar.setTime(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                intent.putExtra("calendar", calendar);
                startActivityForResult(intent, 10);
                break;
            case R.id.tv_clear:
                if (isConnected()) {
                    BaseController currentController1 = mainService.getCurrentController();
                    if (currentController1 != null) {
                        String trim1 = tv_clear_date.getText().toString().trim();
                        if (UIUtils.getString(R.id.date).equals(trim1)) {
                            Toast.makeText(ActivityDeviceSetting.this, UIUtils.getString(R.string.tips), Toast
                                    .LENGTH_SHORT).show();
                        } else {
                            ((CmdController) currentController1).clearHeartRateData(mClearCalendar);
                        }
                    }
                }
                break;
            case R.id.tv_read_date:
                intent = new Intent(this, CalendarTestActivity.class);
                String trim1 = tv_read_date.getText().toString().trim();
                Calendar calendar1 = Calendar.getInstance();
                if (UIUtils.getString(R.id.date).equals(trim1)) {
                    //没有选择时，为当前时间
                } else {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date date = format.parse(trim1);
                        calendar1.setTime(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                intent.putExtra("calendar", calendar1);
                startActivityForResult(intent, 20);
                break;
            case R.id.tv_index:
                if (isConnected()) {
                    intent = new Intent(this, DialogSetTargetActivity.class);
                    intent.putExtra(DialogSetTargetActivity.EXTRA_TYPE, DialogSetTargetActivity.TYPE_INDEX);
                    intent.putExtra(DialogSetTargetActivity.TYPE_INDEX, Integer.parseInt(tv_index.getText().toString
                            ().trim()));
                    startActivityForResult(intent, 30);
                }
                break;
            case R.id.tv_read:
                if (isConnected()) {
                    BaseController currentController1 = mainService.getCurrentController();
                    if (currentController1 != null) {
                        String trim2 = tv_read_date.getText().toString().trim();
                        if (UIUtils.getString(R.id.date).equals(trim2)) {
                            Toast.makeText(ActivityDeviceSetting.this, UIUtils.getString(R.string.tips), Toast
                                    .LENGTH_SHORT).show();
                        } else {
                            ((CmdController) currentController1).readHeartRateData(mReadCalendar, mReadIndex);
                        }
                    }
                }
                break;
            case R.id.unBond_layout:
                showUpairDialog();
                break;
            case R.id.target_layout:
                if (isConnected()) {
                    intent = new Intent(this, DialogSetTargetActivity.class);
                    intent.putExtra(DialogSetTargetActivity.EXTRA_TYPE, DialogSetTargetActivity.TYPE_TARGET);
                    intent.putExtra(DialogSetTargetActivity.TYPE_TARGET, userInfo.getTargetStep());
                    startActivityForResult(intent, 101);
                }
                break;
            case R.id.left_right_device:
                if (isConnected()) {
                    intent = new Intent(this, DialogSetSex.class);
                    intent.putExtra(DialogSetSex.EXTRA_TYPE, DialogSetSex.TYPE_HAND);
                    Log.e(TAG, "isLeftHand ==" + userInfo.getWristMode().isLeftHand());
                    intent.putExtra(DialogSetSex.EXTRA_IS_LEFTHAND, userInfo.getWristMode().isLeftHand());
                    startActivityForResult(intent, 103);
                }
                break;
            case R.id.foot_layout:
                if (isConnected()) {
                    intent = new Intent(this, DialogSetAge.class);
                    intent.putExtra(DialogSetAge.EXTRA_TYPE, DialogSetAge.TYPE_STRIDE);
                    intent.putExtra(DialogSetAge.EXTRA_STRIDE, userInfo.getStrideLength());
                    startActivityForResult(intent, 102);
                }
                break;
            case R.id.long_time_alert:
                if (isConnected()) {
                    intent = new Intent(this, ReminderActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.alarm_layout:
                if (isConnected()) {
                    intent = new Intent(this, AlarmActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.layout_sleep:
                if (isConnected()) {
                    intent = new Intent(this, SleepActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layout_display:
                if (isConnected()) {
                    intent = new Intent(this, DisplayActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layout_screenset:
                if (isConnected()) {
                    intent = new Intent(this, ScreenSetting.class);
                    startActivity(intent);
                }
                break;
            case R.id.lly_auto_heart_rate_test:
                if (isConnected()) {
                    intent = new Intent(this, HeartRateAutoActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.lly_find_device:
                if (isConnected()) {
                    mainService.findDevice();
                }
                break;
            case R.id.layout_control_call_sms:
                if (isConnected()) {
                    intent = new Intent(this, ControlThreeActionActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.calibration_layout:
                if (isConnected()) {
                    intent = new Intent(this, CalibrateActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.bluetooth_layout:
                if (isConnected()) {
                    intent = new Intent(this, BluetoothSwitchActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.lly_ota:
                if (mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                    int hhh = HamaDeviceInfo.getInt(this, HamaDeviceInfo.LIDL_FIREWARE_HIGH, 0);
                    int lll = HamaDeviceInfo.getInt(this, HamaDeviceInfo.LIDL_FIREWARE_LOW, 0);

                    String version = hhh + "." + lll;///Float.valueOf(version)>=88.85 &&
                    //Toast.makeText(this,"newVersion="+(FIREWARE_HIGH + "." + FIREWARE_LOW) +"
                    // oldVersion="+version,Toast.LENGTH_LONG).show();
                    Log.e(TAG, "***version***" + version);
                    if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_REFLEX)) {
                        FIREWARE_LOW = 19;
                    }
//                    if (Float.valueOf(FIREWARE_HIGH + "." + FIREWARE_LOW) > Float.valueOf(version)) {
                    //alertDialog.show();
                    if (myBaseDevice != null) {
                        String dname = myBaseDevice.getName();
                        if (dname.contains("BEAT") && getVersion() >= 91.33f) {
                            intent = new Intent(ActivityDeviceSetting.this, DfuBeatActivity.class);
                        } else {
                            intent = new Intent(ActivityDeviceSetting.this, DfuActivity.class);
                        }
                        startActivity(intent);
                    }
//                    } else {
//                        UtilTools.showToast(this, getString(R.string.is_latest));
//                    }
                } else {
                    //未连接状态不让进入升级页面
                    UtilTools.showToast(this, R.string.please_bind);
//                    intent = new Intent(ActivityDeviceSetting.this, DfuActivity.class);
//                    startActivity(intent);
                }
                break;
            case R.id.factory_linear:
                if (isConnected()) {
                    showFactoryDialog();
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 101) {///target
            if (isConnected()) {
                UserInfo.getInstance(this).setTargetStep(Integer.valueOf(data.getStringExtra(DialogSetTargetActivity
                        .TYPE_TARGET)));
                MainService.getInstance(this).syncUserInfo();
                initValue();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STEP_TARGET_CHANGE));
            }
        } else if (data != null && requestCode == 102) {//stide length
            if (isConnected()) {
                UserInfo.getInstance(this).setStrideLength(Integer.valueOf(data.getStringExtra(DialogSetAge
                        .EXTRA_STRIDE)));
                MainService.getInstance(this).syncUserInfo();
                initValue();
            }
        } else if (data != null && requestCode == 103) {///left hand
            if (isConnected()) {
                UserInfo.getInstance(this).setWristMode(new WristMode(data.getBooleanExtra(DialogSetSex
                        .EXTRA_IS_LEFTHAND, true)));
                MainService.getInstance(this).setWristMode(UserInfo.getInstance(this).getWristMode());
                initValue();
            }
        } else if (data != null && requestCode == 105) {
            if (isConnected()) {
                int cout = Integer.valueOf(data.getStringExtra(DialogSetAge.EXTRA_COUNTDOWN));
                ConfigHelper.getInstance(this).putInt(Constants.W285S_COUNTDOWN, cout);
                byte[] bytes = new byte[]{(byte) 0xbe, 0x01, 0x17, (byte) 0xfe, 0, 0, 0, 0, (byte) cout, 0, 0, 0, 0, 0};
                MainService.getInstance(this).sendCustomCmd(bytes);
                initValue();
            }
        } else if (data != null && requestCode == 10) {
            mClearCalendar = (Calendar) data.getSerializableExtra("calendar");
            Log.e(TAG, mClearCalendar.toString());
            tv_clear_date.setText(TimeUtils.getTimeByYYYYMMDD(mClearCalendar.getTimeInMillis()));
        } else if (data != null && requestCode == 20) {
            mReadCalendar = (Calendar) data.getSerializableExtra("calendar");
            Log.e(TAG, mReadCalendar.toString());
            tv_read_date.setText(TimeUtils.getTimeByYYYYMMDD(mReadCalendar.getTimeInMillis()));
        } else if (data != null && requestCode == 30) {
            mReadIndex = Integer.valueOf(data.getStringExtra(DialogSetTargetActivity.TYPE_INDEX));
            Log.e(TAG, " mReadIndex == " + mReadIndex);
            tv_index.setText(mReadIndex + "");
        }
        setSubVuewSelected(false);

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
                MainService mainService = MainService.getInstance(context);
                if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {

                } else {

                }
            }
        }
    };


    //遥控拍照
    public void takePhotoMenu(View view) {
        verticalPermiss();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0x0A){
            //先判断是否有权限
            verticalPermiss();
        }
    }


    private void verticalPermiss(){
        //先判断是否有权限
        if(ActivityCompat.checkSelfPermission(ActivityDeviceSetting.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ActivityDeviceSetting.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},0x0A);
            return;
        }

        if(ActivityCompat.checkSelfPermission(ActivityDeviceSetting.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ActivityDeviceSetting.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0x0A);
            return;
        }


        startActivity(new Intent(ActivityDeviceSetting.this, CamaraActivity.class));
    }
}


