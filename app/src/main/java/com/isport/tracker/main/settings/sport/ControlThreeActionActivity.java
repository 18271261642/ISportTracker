package com.isport.tracker.main.settings.sport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.isportlibrary.tools.Constants;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.bluetooth.notifications.NotiServiceListener;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.main.settings.ActivityDeviceSetting;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.DialogHelper;
import com.isport.tracker.view.EasySwitchButton;
import com.ypy.eventbus.EventBus;

import java.util.Arrays;

import butterknife.ButterKnife;

/**
 * Created by 中庸 on 2016/5/16.
 */
public class ControlThreeActionActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton
        .OnOpenedListener {


    private static final String TAG = "ControlThreeActionActiv";

    TextView returnBack;
    TextView textSave;
    EasySwitchButton esbCallSwitch;
    EasySwitchButton esbSmsSwitch;
    EasySwitchButton esbSmsDetailSwitch;
    EasySwitchButton esbNotiSwitch;
    EasySwitchButton esbAppSwitch;
    LinearLayout appNotiLayout;
    LinearLayout notiLayout;
    LinearLayout layout_sms_details;
    LinearLayout notificationLayout;

    private boolean isAllowCall;
    private boolean isAllowNoti;
    private boolean isAllowSMS;
    private boolean isOpenApp;
    private boolean isShowDetail;

    NotificationEntry notificationEntry;
    private boolean isFirst = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_music_phone);

        notificationEntry = NotificationEntry.getInstance(this);
        if (BuildConfig.PRODUCT.equals(com.isport.tracker.util.Constants.PRODUCT_ENERGETICS)) {
            notificationEntry.setOpenNoti(true);
        }
        if (getIntent().getExtras() != null) {
            isFirst = getIntent().getExtras().getBoolean("isfirst");
        }
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initUi();
    }

    private void initUi() {
        appNotiLayout = (LinearLayout) findViewById(R.id.linear_app_noti);
        notiLayout = (LinearLayout) findViewById(R.id.linear_noti);
        layout_sms_details = (LinearLayout) findViewById(R.id.layout_sms_details);
        esbCallSwitch = (EasySwitchButton) findViewById(R.id.esb_call_switch);
        esbSmsSwitch = (EasySwitchButton) findViewById(R.id.esb_sms_switch);
        esbSmsDetailSwitch = (EasySwitchButton) findViewById(R.id.esb_sms_detail_switch);
        esbNotiSwitch = (EasySwitchButton) findViewById(R.id.esb_noti_switch);
        esbAppSwitch = (EasySwitchButton) findViewById(R.id.esb_app_switch);
        notificationLayout = (LinearLayout)findViewById(R.id.notification);

        appNotiLayout.setOnClickListener(this);
        notiLayout.setOnClickListener(this);

        esbCallSwitch.setOnCheckChangedListener(this);
        esbSmsSwitch.setOnCheckChangedListener(this);
        esbSmsDetailSwitch.setOnCheckChangedListener(this);
        esbNotiSwitch.setOnCheckChangedListener(this);
        esbAppSwitch.setOnCheckChangedListener(this);

        MainService mainService = MainService.getInstance(this);


        isAllowCall = notificationEntry.isAllowCall();
        isAllowNoti = notificationEntry.isOpenNoti();
        isShowDetail = notificationEntry.isShowDetail();
        //不论是否打开，高于90.69版本的设备都会关闭
        if (mainService != null && mainService.getCurrentDevice() != null) {
            BaseDevice currentDevice = mainService.getCurrentDevice();
            int devicetype = currentDevice.getDeviceType();
            String dname = currentDevice.getName();
            if (devicetype == BaseDevice.TYPE_AS97 && dname.contains("W311N_") && ((CmdController) mainService.getCurrentController()).getVersion() > 90.69f) {
                isAllowNoti = true;
                isShowDetail = true;
                esbSmsSwitch.setVisibility(View.GONE);
                esbSmsDetailSwitch.setVisibility(View.GONE);
                layout_sms_details.setVisibility(View.GONE);
                Log.e(TAG, "1111");
            }
            if (dname.contains("W307E") && dname.contains("W301B")) {
                isAllowNoti = true;
                isShowDetail = true;
                esbSmsSwitch.setVisibility(View.GONE);
                esbSmsDetailSwitch.setVisibility(View.GONE);
                layout_sms_details.setVisibility(View.GONE);
                Log.e(TAG, "222");
            }
            if (dname.contains("TA300") || dname.contains("TA400")) {
                isAllowNoti = true;
                isShowDetail = true;
                esbSmsSwitch.setVisibility(View.GONE);
                esbSmsDetailSwitch.setVisibility(View.GONE);
                layout_sms_details.setVisibility(View.GONE);
            }
            if (dname.contains("BEAT") && ((CmdController) mainService.getCurrentController()).getVersion() > 90.99f) {
                isAllowNoti = true;
                isShowDetail = true;
                esbSmsSwitch.setVisibility(View.GONE);
                esbSmsDetailSwitch.setVisibility(View.GONE);
                layout_sms_details.setVisibility(View.GONE);
            }
        }
        isAllowSMS = notificationEntry.isAllowSMS();
        if (notificationEntry.isAllowApp()) {
            Log.e(TAG, "333");
            isOpenApp = true;
            if (!NotiServiceListener.isEnabled(this)) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            } else {
                NotiServiceListener.toggleNotificationListenerService(this);
            }
        }
//        isOpenApp = (NotifService.getNotificationIsRun(this) && notificationEntry.isAllowApp());
        BaseDevice currentDevice = mainService.getCurrentDevice();
        int devicetype = currentDevice.getDeviceType();
        if (devicetype == BaseDevice.TYPE_W307S) {
            esbCallSwitch.setStatus(!isAllowCall);
            esbNotiSwitch.setStatus(!isAllowNoti);
        } else {
            esbCallSwitch.setStatus(!isAllowCall);
            esbNotiSwitch.setStatus(!isAllowNoti);
        }
        esbAppSwitch.setStatus(!isOpenApp);
        esbSmsDetailSwitch.setStatus(!isShowDetail);
        esbSmsSwitch.setStatus(!isAllowSMS);
        if (!notificationEntry.isOpenNoti()) {
            notiLayout.setVisibility(View.GONE);
        }
        if (isFirst) {
            findViewById(R.id.return_back).setVisibility(View.GONE);
        }
    }

    public static void initDisplay(Context ctx) {
        BaseDevice baseDevice = MainService.getInstance(ctx).getCurrentDevice();
        ConfigHelper helper = ConfigHelper.getInstance(ctx);
        int deviceType = baseDevice.getDeviceType();
        boolean calSelected = false;
        boolean distSelected = false;
        boolean callSelected = false;
        boolean smsSelected = false;
        boolean smscallMissedCallSelected = false;
        boolean timeSelected = false;
        boolean alarmSelected = false;
        boolean perSelected = false;
        boolean faceSelected = false;
        boolean countDownSelected = false;
        if (deviceType == BaseDevice.TYPE_W311N || deviceType == BaseDevice.TYPE_W311T || deviceType == BaseDevice
                .TYPE_AT200 || deviceType == BaseDevice.TYPE_AS97) {
            calSelected = true;///卡路里
            callSelected = true;//电话
            smsSelected = true;//短信
        } else if (deviceType == BaseDevice.TYPE_W307H || deviceType == BaseDevice.TYPE_W301H || deviceType ==
                BaseDevice.TYPE_W307N || deviceType == BaseDevice.TYPE_W301N || deviceType == BaseDevice.TYPE_W240N
                || deviceType == BaseDevice.TYPE_W240B ||
                deviceType == BaseDevice.TYPE_W285B) {
            calSelected = true;///卡路里
            callSelected = true;
            distSelected = true;///距离
            smscallMissedCallSelected = true;///未接来电短信数量
            timeSelected = true;///运动时间
            alarmSelected = true;///闹钟
            perSelected = true;///百分比
            faceSelected = true;///笑脸

        } else if (deviceType == BaseDevice.TYPE_W307S || deviceType == BaseDevice.TYPE_W307S_SPACE || deviceType == BaseDevice.TYPE_W240S || deviceType ==
                BaseDevice.TYPE_AT100) {
            calSelected = true;///卡路里
            callSelected = true;//电话
            smsSelected = true;///短信
            distSelected = true;///距离
            timeSelected = true;///运动时间
            alarmSelected = true;///闹钟
            perSelected = true;///百分比
            faceSelected = true;///笑脸


        } else if (deviceType == BaseDevice.TYPE_W301S) {
            calSelected = true;///卡路里
            callSelected = true;//电话
            smsSelected = true;///短信
            distSelected = true;///距离
            timeSelected = true;///运动时间
            alarmSelected = true;///闹钟
            perSelected = true;///百分比
            faceSelected = true;///笑脸

        } else if (deviceType == BaseDevice.TYPE_W285S || deviceType == BaseDevice.TYPE_SAS80) {
            calSelected = true;///卡路里
            callSelected = true;//电话
            smsSelected = true;///短信
            distSelected = true;///距离
            timeSelected = true;///运动时间
            alarmSelected = true;///闹钟
            perSelected = true;///百分比
            faceSelected = true;///笑脸
            countDownSelected = true;//倒计时
        } else if (deviceType == BaseDevice.TYPE_W194) {
            alarmSelected = true;///闹钟
            calSelected = true;///卡路里
            distSelected = true;
        }

        if (baseDevice.getName().toLowerCase().contains("as87")) {
            faceSelected = false;
        }
        if (baseDevice.getName().toLowerCase().contains("as97")) {
            faceSelected = false;
        }
        if (baseDevice.getName().toLowerCase().contains("sas87")) {
            faceSelected = false;
        }
        ItemInfo infoCal = new ItemInfo(DisplayActivity.KEY_SHOW_CALORIES, R.drawable.selector_device_calories, true,
                helper.getBoolean(DisplayActivity.KEY_SHOW_CALORIES + baseDevice.getMac(),
                        calSelected));
        ItemInfo infoDist = new ItemInfo(DisplayActivity.KEY_SHOW_DISTANCE, R.drawable.selector_device_distance,
                true, helper.getBoolean(DisplayActivity.KEY_SHOW_DISTANCE + baseDevice
                .getMac(), distSelected));
        ItemInfo infoCall = new ItemInfo(DisplayActivity.KEY_SHOW_CALL, R.drawable.selector_device_call, true, helper
                .getBoolean(DisplayActivity.KEY_SHOW_CALL + baseDevice.getMac(), callSelected));
        ItemInfo infoSms = new ItemInfo(DisplayActivity.KEY_SHOW_SMS, R.drawable.selector_device_sms, true, helper
                .getBoolean(DisplayActivity.KEY_SHOW_SMS + baseDevice.getMac(), smsSelected));
        ItemInfo infoSmsCallMissed = new ItemInfo(DisplayActivity.KEY_SHOW_SMS_CALL_MISSED, R.drawable
                .selector_device_sms, true, helper.getBoolean(DisplayActivity.KEY_SHOW_SMS_CALL_MISSED + baseDevice
                .getMac(), smscallMissedCallSelected));
        ItemInfo infoTime = new ItemInfo(DisplayActivity.KEY_SHOW_TIME, R.drawable.selector_device_time, true, helper
                .getBoolean(DisplayActivity.KEY_SHOW_TIME + baseDevice.getMac(), timeSelected));
        ItemInfo infoAlarm = new ItemInfo(DisplayActivity.KEY_SHOW_ALARM, R.drawable.selector_device_alarm, true,
                helper.getBoolean(DisplayActivity.KEY_SHOW_ALARM + baseDevice.getMac(),
                        alarmSelected));
        ItemInfo infoPer = new ItemInfo(DisplayActivity.KEY_SHOW_PERCENT, R.drawable.selector_device_percent, true,
                helper.getBoolean(DisplayActivity.KEY_SHOW_PERCENT + baseDevice.getMac(),
                        perSelected));
        ItemInfo infoFace = new ItemInfo(DisplayActivity.KEY_SHOW_FACE, R.drawable.selector_device_face, true, helper
                .getBoolean(DisplayActivity.KEY_SHOW_FACE + baseDevice.getMac(), faceSelected));
        // ItemInfo infoCountDown = new ItemInfo(DisplayActivity.KEY_SHOW_COUNTDOWN, R.drawable.selector_countdown,
        // true, helper.getBoolean(DisplayActivity.KEY_SHOW_COUNTDOWN+baseDevice.getMac(),  countDownSelected));

        if (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice.getDeviceType() == BaseDevice
                .TYPE_W311T || baseDevice.getDeviceType() == BaseDevice.TYPE_AT200 || baseDevice.getDeviceType() ==
                BaseDevice.TYPE_AS97) {
            infoSmsCallMissed.setShow(false);
            //infoCountDown.setShow(false);
            infoCall.setShow(false);
            infoSms.setShow(false);

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307N || baseDevice.getDeviceType() == BaseDevice
                .TYPE_W307H || baseDevice.getDeviceType() == BaseDevice.TYPE_W301H) {
            infoSms.setShow(false);
            //infoCountDown.setShow(false);

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307S || baseDevice.getDeviceType() == BaseDevice.TYPE_W307S_SPACE || baseDevice.getDeviceType() == BaseDevice
                .TYPE_W240S || baseDevice.getDeviceType() == BaseDevice.TYPE_AT100) {
            infoSmsCallMissed.setShow(false);
            //infoCountDown.setShow(false);
            infoCall.setShow(false);
            infoSms.setShow(false);

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W301S) {
            infoSmsCallMissed.setShow(false);
            //infoCountDown.setShow(false);
            infoCall.setShow(false);
            infoSms.setShow(false);

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W285S || baseDevice.getDeviceType() == BaseDevice
                .TYPE_SAS80) {
            infoSmsCallMissed.setShow(false);
            infoCall.setShow(false);
            infoSms.setShow(false);
        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W194) {
            infoCall.setShow(false);
            infoSms.setShow(false);
            infoSmsCallMissed.setShow(false);
            infoTime.setShow(false);
            infoPer.setShow(false);
            infoFace.setShow(false);
        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W240N || baseDevice.getDeviceType() == BaseDevice
                .TYPE_W301N ||
                baseDevice.getDeviceType() == BaseDevice.TYPE_W240B || baseDevice.getDeviceType() == BaseDevice
                .TYPE_W285B) {
            infoSms.setShow(false);
        }

        if (baseDevice.getName().toLowerCase().contains("as87")) {
            infoFace.setShow(false);
        }

        if (baseDevice.getName().toLowerCase().contains("as97")) {
            infoFace.setShow(false);
        }
        if (baseDevice.getName().toLowerCase().contains("sas87")) {
            infoFace.setShow(false);
        }

        ItemInfo[] infos = {infoCal, infoDist, infoCall, infoSms, infoSmsCallMissed, infoTime, infoAlarm, infoPer,
                infoFace};
        for (int i = 0; i < infos.length; i++) {
            ItemInfo info = infos[i];
            helper.putBoolean(info.type + baseDevice.getMac(), info.isSelected());
        }
    }

    public void setDisplay(Context ctx) {
        if (isConnected(ctx)) {


            BaseDevice baseDevice = MainService.getInstance(ctx).getCurrentDevice();
            ConfigHelper helper = ConfigHelper.getInstance(ctx);

            MainService mainService = MainService.getInstance(ctx);
            byte[] data = new byte[20];
            data[0] = (byte) 0xbe;
            data[1] = (byte) 0x01;
            data[2] = (byte) 0x08;
            data[3] = (byte) 0xfe;
            int index = 4;

            if (helper.getBoolean(DisplayActivity.KEY_SHOW_CALORIES + baseDevice.getMac(), true)) {
                data[index] = 0x03;
                index++;
            }
            if (helper.getBoolean(DisplayActivity.KEY_SHOW_DISTANCE + baseDevice.getMac(), true)) {//displaySet
                // .isShowDist()) {
                data[index] = 0x04;
                index++;
            }
            if (helper.getBoolean(DisplayActivity.KEY_SHOW_TIME + baseDevice.getMac(), true)) {//.isShowSportTime()) {
                data[index] = 0x05;
                index++;
            }
            if (helper.getBoolean(DisplayActivity.KEY_SHOW_PERCENT + baseDevice.getMac(), true)) {//displaySet
                // .isShowProgress()) {
                data[index] = 0x06;
                index++;
            }
            if (helper.getBoolean(DisplayActivity.KEY_SHOW_FACE + baseDevice.getMac(), true)) {//displaySet
                // .isShowEmotion()) {
                data[index] = 0x07;
                index++;
            }
            if (helper.getBoolean(DisplayActivity.KEY_SHOW_ALARM + baseDevice.getMac(), true)) {//displaySet
                // .isShowAlarm()) {
                data[index] = 0x08;
                index++;
            }
            // TODO: 2018/2/6 307H 301H没有数量
            if (baseDevice.getDeviceType() != BaseDevice.TYPE_W307H && baseDevice.getDeviceType() != BaseDevice
                    .TYPE_W301H) {
                Log.e(TAG, "***77777***");

                if (!helper.getBoolean(DisplayActivity.KEY_SHOW_SMS_CALL_MISSED + baseDevice.getMac(), false)) {//displaySet.isShowSmsMissedCall()) {
                    data[index] = 0x0A;
                    Log.e(TAG, "***88888***");
                    index++;
                }
            }

            //来电
            boolean isCall = helper.getBoolean(DisplayActivity.KEY_SHOW_CALL + baseDevice.getMac(), true);

            if (!isCall) {//displaySet
                // .isShowIncomingReminder()) {
                data[index] = 0x0B;
                index++;
            }
            Log.e(TAG, "***baseDevice.getDeviceType()***" + baseDevice.getDeviceType());
            // TODO: 2018/2/6 针对307H 301H关闭则发0D
            if (baseDevice.getDeviceType() != BaseDevice.TYPE_W307H && baseDevice.getDeviceType() != BaseDevice
                    .TYPE_W301H) {
                Log.e(TAG, "***55555***");
                if (!helper.getBoolean(DisplayActivity.KEY_SHOW_SMS + baseDevice.getMac(), true)) {//.isShowMsgContent
                    // ()) {
                    Log.e(TAG, "***66666***");
                    data[index] = 0x1D;
                    index++;
                }
            }
           /* if (helper.getBoolean(DisplayActivity.KEY_SHOW_COUNTDOWN + baseDevice.getMac(), true)) {
                data[index] = 0x0F;
                index++;
            }*/

            for (int i = index; i < 20; i++) {
                if ((baseDevice.getDeviceType() == BaseDevice.TYPE_W307H || baseDevice.getDeviceType() == BaseDevice
                        .TYPE_W301H) && i == 17) {
                    //打开了Notif则推送
                    if (!isAllowNoti) {
                        Log.e(TAG, "***22222***");
                        //信息内容打开怎1D
                        if (!isShowDetail) {
                            Log.e(TAG, "***33333***");
                            data[i] = 0x1D;
                        } else {
                            Log.e(TAG, "***444444***");
                            data[i] = 0x0D;
                        }
                    } else {
                        data[i] = (byte) 0xff;
                    }
                } else {
                    data[i] = (byte) 0xff;
                }
            }
            Log.e(TAG, "***bytesToHexString***" + bytesToHexString(data));

            Log.e(TAG,"---notificationEntry="+new Gson().toJson(notificationEntry));

            notificationEntry.setAllowCall(!isAllowCall);

            notificationEntry.setAllowApp(!isOpenApp);

            notificationEntry.setOpenNoti(!isAllowNoti);

            notificationEntry.setAllowSMS(!isAllowSMS);

            notificationEntry.setShowDetail(!isShowDetail);

            mainService.sendCustomCmd(data);

        }
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private void setW337BReminder() {
        ConfigHelper helper = ConfigHelper.getInstance(this);
        byte[] values = new byte[7];
        values[0] = 0x00;
        values[1] = (byte) (notificationEntry.isAllowCall() ? 1 : 0);///电话
        values[2] = (byte) (notificationEntry.isAllowSMS() ? 1 : 0);////短信
        if (!notificationEntry.isAllowApp()) {
            for (int i = 3; i < 7; i++) {
                values[i] = 0x00;
            }
        } else {
            values[3] = (byte) (notificationEntry.isAllowPackage(Constants.KEY_14_PACKAGE, true) ? 1 : 0);////wechat
            values[4] = (byte) (notificationEntry.isAllowPackage(Constants.KEY_13_PACKAGE, true) ? 1 : 0);///qq
            values[5] = (byte) (notificationEntry.isAllowPackage(Constants.KEY_15_PACKAGE, true) ? 1 : 0);//skype
            values[6] = (byte) (notificationEntry.isAllowPackage(Constants.KEY_1B_PACKAGE, true) ? 1 : 0);//whatsapp
        }

        Log.e(TAG,"--byte="+ Arrays.toString(values));



        if (MainService.getInstance(ControlThreeActionActivity.this) != null) {
            MainService.getInstance(ControlThreeActionActivity.this).setInfoRemind(values);
        }
    }

    public void save() {
        MainService mainService = MainService.getInstance(this);
        if (isConnected(this)) {
            initDisplay(this);

            Log.e("handleNotiResponse", "isShowDetail" + isShowDetail);
            notificationEntry.setAllowApp(isOpenApp);
            notificationEntry.setAllowCall(isAllowCall);
            notificationEntry.setAllowSMS(isAllowSMS);
            notificationEntry.setShowDetail(isShowDetail);
            notificationEntry.setOpenNoti(isAllowNoti);


            ConfigHelper.getInstance(this).putBoolean(DisplayActivity.KEY_SHOW_CALL + mainService.getCurrentDevice()
                    .getMac(), isAllowCall);
            ConfigHelper.getInstance(this).putBoolean(DisplayActivity.KEY_SHOW_SMS + mainService.getCurrentDevice()
                    .getMac(), isAllowSMS);

            BaseDevice baseDevice = mainService.getCurrentDevice();
            if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                setDisplay(this);
                progressDialog = DialogHelper.showProgressDialog(this, getString(R.string.setting_seccess));
            } else if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W337B) {
                setW337BReminder();
                Toast.makeText(this, getString(R.string.setting_seccess), Toast.LENGTH_LONG).show();
            } else if (baseDevice.getProfileType() == BaseController.CMD_TYPE_NMC) {
                finish();
            }

        }
    }

    private static boolean isConnected(Context ctx) {
        MainService mainService = MainService.getInstance(ctx);
        if (mainService == null || mainService.getConnectionState() != BaseController
                .STATE_CONNECTED) {
            Toast.makeText(ctx, ctx.getResources().getString(R.string.please_bind), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void updateUI(boolean isopen) {
        if (isopen) {
            notiLayout.setVisibility(View.VISIBLE);
        } else {
            notiLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        int id = v.getId();
        MainService mainService = MainService.getInstance(this);
        BaseDevice currentDevice = mainService.getCurrentDevice();
        int devicetype = currentDevice.getDeviceType();
        switch (id) {
            case R.id.esb_call_switch:
                if (devicetype == BaseDevice.TYPE_W307S) {
                    isAllowCall = isOpened;
                } else {
                    isAllowCall = isOpened;
                }
                break;
            case R.id.esb_noti_switch:
                if (devicetype == BaseDevice.TYPE_W307S) {
                    isAllowNoti = isOpened;
                } else {
                    isAllowNoti = isOpened;
                }
                notiLayout.setVisibility(isOpened ? View.VISIBLE : View.GONE);
                break;
            case R.id.esb_sms_switch:
                isAllowSMS = isOpened;
                break;
            case R.id.esb_app_switch:
                if (isOpened && !NotiServiceListener.isEnabled(this)) {
                    esbAppSwitch.setStatus(false);
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intent);
                } else {
                    isOpenApp = isOpened;
                }
                break;
            case R.id.esb_sms_detail_switch:
                isShowDetail = isOpened;
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_back:
                ControlThreeActionActivity.this.finish();
                break;
            case R.id.text_save:
                save();
                break;
            case R.id.linear_app_noti:
                openAccessibility();
                break;
            default:
                break;
        }
    }


    private void openAccessibility() {
        if (!NotiServiceListener.isEnabled(this)) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, NotiActivity.class);
            startActivity(intent);
            esbAppSwitch.setStatus(true);
            isOpenApp = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (NotiServiceListener.isEnabled(this)) {
            Intent intent = new Intent(this, NotiActivity.class);
            startActivity(intent);
            esbAppSwitch.setStatus(true);
            isOpenApp = true;
        } else {
            esbAppSwitch.setStatus(false);
            isOpenApp = false;
        }
    }

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case 0x09:
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                if (isFirst) {
                    Intent intent = new Intent(this, ActivityDeviceSetting.class);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
