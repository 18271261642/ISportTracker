package com.isport.tracker.main.settings.sport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.DisplaySet;
import com.isport.isportlibrary.entry.NotificationEntry;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.MyBaseDevice;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.DialogHelper;
import com.isport.tracker.view.EasySwitchButton;

import java.util.HashMap;

/**
 * @Author xiongxing
 * @Date 2018/12/7
 * @Fuction
 */

public class MessageActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton
        .OnOpenedListener {
    NotificationEntry notificationEntry;
    public final static String KEY_SHOW_CALORIES = "KEY_SHOW_CALORIES";///热量
    public final static String KEY_SHOW_DISTANCE = "KEY_SHOW_DISTANCE";////距离
    public final static String KEY_SHOW_TIME = "KEY_SHOW_TIME";///运动时间
    public final static String KEY_SHOW_PERCENT = "KEY_SHOW_PERCENT";///百分比
    public final static String KEY_SHOW_FACE = "KEY_SHOW_FACE";////笑脸
    public final static String KEY_SHOW_ALARM = "KEY_SHOW_ALARM";////闹钟
    public final static String KEY_SHOW_SMS = "KEY_SHOW_SMS";////信息提醒及内容推送
    public final static String KEY_SHOW_CALL = "KEY_SHOW_CALL";///来电提醒
    public final static String KEY_SHOW_SMS_CALL_MISSED = "KEY_SHOW_SMS_CALL_MISSED";///未接来电、短信数量
    public final static String KEY_SHOW_COUNTDOWN = "KEY_SHOW_COUNTDOWN";


    HashMap<String, EasySwitchButton> maps = new HashMap<>();

    private TextView tv_right;
    private EasySwitchButton esb_call_switch;
    private EasySwitchButton esb_message_switch;
    private EasySwitchButton esb_app_switch;
    private EasySwitchButton esb_qqswitch;
    private EasySwitchButton esb_wechat_switch;
    private EasySwitchButton esb_Skype_switch;
    private EasySwitchButton esb_whatapp_switch;
    private EasySwitchButton esb_facebook_switch;
    private EasySwitchButton esb_Twitter_switch;
    private EasySwitchButton esb_linkedin_switch;
    private EasySwitchButton esb_WhatsApp_switch;
    private EasySwitchButton esb_instagram_switch;
    private boolean mAllday;
    private boolean mOnlySleepmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_remind_setting);
        initControl();
        initValue();
    }

    private void initControl() {
        tv_right = (TextView) findViewById(R.id.tv_right);

        esb_call_switch = (EasySwitchButton) findViewById(R.id.esb_call_switch);//自动存储心率
        esb_call_switch.setOnCheckChangedListener(this);

        esb_message_switch = (EasySwitchButton) findViewById(R.id.esb_messge_switch);
        esb_message_switch.setOnCheckChangedListener(this);

        esb_app_switch = (EasySwitchButton) findViewById(R.id.esb_app_switch);
        esb_app_switch.setOnCheckChangedListener(this);

        esb_qqswitch = (EasySwitchButton) findViewById(R.id.esb_qq_switch);
        esb_qqswitch.setOnCheckChangedListener(this);

        esb_wechat_switch = (EasySwitchButton) findViewById(R.id.esb_wechat_switch);
        esb_wechat_switch.setOnCheckChangedListener(this);

        esb_Skype_switch = (EasySwitchButton) findViewById(R.id.esb_Skype_switch);
        esb_Skype_switch.setOnCheckChangedListener(this);

        esb_facebook_switch = (EasySwitchButton) findViewById(R.id.esb_facebook_switch);
        esb_facebook_switch.setOnCheckChangedListener(this);

        esb_Twitter_switch = (EasySwitchButton) findViewById(R.id.esb_Twitter_switch);
        esb_Twitter_switch.setOnCheckChangedListener(this);


        esb_linkedin_switch = (EasySwitchButton) findViewById(R.id.esb_linkedin_switch);
        esb_linkedin_switch.setOnCheckChangedListener(this);

        esb_instagram_switch = (EasySwitchButton) findViewById(R.id.esb_instagram_switch);
        esb_instagram_switch.setOnCheckChangedListener(this);

        esb_whatapp_switch = (EasySwitchButton) findViewById(R.id.esb_WhatsApp_switch);
        esb_whatapp_switch.setOnCheckChangedListener(this);


    }


    String mac;
    ConfigHelper helper;

    private void initValue() {
        tv_right.setVisibility(View.GONE);
        helper = ConfigHelper.getInstance(this);
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            mac = mainService.getCurrentDevice().getMac();
        } else {
            mac = "";
        }
        notificationEntry = NotificationEntry.getInstance(this);
        esb_call_switch.setStatus(helper.getBoolean(Constants.IS_CALL + mac, true));
        esb_message_switch.setStatus(helper.getBoolean(Constants.IS_MESSAGE + mac, true));
        esb_qqswitch.setStatus(helper.getBoolean(Constants.IS_qq + mac, true));
        esb_wechat_switch.setStatus(helper.getBoolean(Constants.IS_wechat + mac, true));
        esb_whatapp_switch.setStatus(helper.getBoolean(Constants.IS_WhatsApp + mac, true));
        esb_Twitter_switch.setStatus(helper.getBoolean(Constants.IS_twitter + mac, true));
        esb_Skype_switch.setStatus(helper.getBoolean(Constants.IS_skye + mac, true));
        esb_instagram_switch.setStatus(helper.getBoolean(Constants.IS_instagram + mac, true));
        esb_linkedin_switch.setStatus(helper.getBoolean(Constants.IS_linkedin + mac, true));
        esb_facebook_switch.setStatus(helper.getBoolean(Constants.IS_facebook + mac, true));
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        int id = v.getId();
        Intent intent = null;
        switch (id) {
            case R.id.esb_call_switch:
                ConfigHelper.getInstance(this).putBoolean(Constants.IS_CALL + mac, isOpened);
                helper.putBoolean(KEY_SHOW_CALL + mac, isOpened);
                notificationEntry.setAllowCall(isOpened);
                //clickSave();
                break;
            case R.id.esb_messge_switch:
                ConfigHelper.getInstance(this).putBoolean(Constants.IS_MESSAGE + mac, isOpened);
                helper.putBoolean(KEY_SHOW_SMS + mac, isOpened);
                notificationEntry.setAllowSMS(isOpened);
                //clickSave();
            case R.id.esb_qq_switch:
                helper.putBoolean(Constants.IS_qq + mac, isOpened);
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_13_PACKAGE, isOpened);///qq
                notificationEntry.isAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_13_PACKAGE, true);
                break;
            case R.id.esb_wechat_switch:
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_14_PACKAGE, isOpened);////wechat
                helper.putBoolean(Constants.IS_wechat + mac, isOpened);
                break;
            case R.id.esb_facebook_switch:
                helper.putBoolean(Constants.IS_facebook + mac, isOpened);
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_16_PACKAGE, isOpened);//facebook
                break;
            case R.id.esb_WhatsApp_switch:
                helper.putBoolean(Constants.IS_WhatsApp + mac, isOpened);
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_1B_PACKAGE, isOpened);//whatsapp
                break;
            case R.id.esb_Twitter_switch:
                helper.putBoolean(Constants.IS_twitter + mac, isOpened);
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_17_PACKAGE, isOpened);//twitter
                break;
            case R.id.esb_instagram_switch:
                helper.putBoolean(Constants.IS_instagram + mac, isOpened);
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_19_PACKAGE, isOpened);//whatsapp
                break;
            case R.id.esb_linkedin_switch:
                helper.putBoolean(Constants.IS_linkedin + mac, isOpened);
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_18_PACKAGE, isOpened);//whatsapp
                break;
            case R.id.esb_Skype_switch:
                notificationEntry.setAllowPackage(com.isport.isportlibrary.tools.Constants.KEY_15_PACKAGE, isOpened);//skype
                helper.putBoolean(Constants.IS_skye + mac, isOpened);
                break;
        }
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_tv:
                finish();
                break;
            case R.id.text_save:
                save();
                break;
            case R.id.tv_right:
                save();
               /* if (isConnected()) {
                    MainService mainService = MainService.getInstance(this);
                    CmdController mCmdController = (CmdController) mainService.getCurrentController();
                    if (mAllday) {
                        // TODO: 2018/12/7 全天开启
                        if (mOnlySleepmode) {
                            // TODO: 2018/12/7 只有睡眠模式开启
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ALLDAY, mAllday);
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ONLYSLEEPMODE, true);
                            mCmdController.raiseHandOnlySleepMode(true);
                        } else {
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ALLDAY, mAllday);
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ONLYSLEEPMODE, false);
                            mCmdController.raiseHandAllDayOrNot(mAllday);
                        }
                    } else {
                        ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ALLDAY, mAllday);
                        ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ONLYSLEEPMODE, false);
                        mCmdController.raiseHandAllDayOrNot(mAllday);
                    }
                    finish();
                }*/
                break;
            default:
                break;
        }
    }

    /**
     * 保存数据
     */
    private void clickSave() {
        if (isConnected()) {
            ConfigHelper helper = ConfigHelper.getInstance(this);
            MainService mainService = MainService.getInstance(this);
            byte[] data = new byte[20];
            data[0] = (byte) 0xbe;
            data[1] = (byte) 0x01;
            data[2] = (byte) 0x08;
            data[3] = (byte) 0xfe;
            int index = 4;

            if (helper.getBoolean(KEY_SHOW_CALORIES + mac, true)) {
                data[index] = 0x03;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_DISTANCE + mac, false)) {//displaySet.isShowDist()) {
                data[index] = 0x04;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_TIME + mac, false)) {//.isShowSportTime()) {
                data[index] = 0x05;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_PERCENT + mac, false)) {//displaySet.isShowProgress()) {
                data[index] = 0x06;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_FACE + mac, false)) {//displaySet.isShowEmotion()) {
                data[index] = 0x07;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_ALARM + mac, false)) {//displaySet.isShowAlarm()) {
                data[index] = 0x08;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_SMS_CALL_MISSED + mac, false)) {//displaySet.isShowSmsMissedCall()) {
                data[index] = 0x0A;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_CALL + mac, true)) {//displaySet.isShowIncomingReminder()) {
                data[index] = 0x0B;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_SMS + mac, true)) {//.isShowMsgContent()) {
                data[index] = 0x1D;
                index++;
            }
//			if(helper.getBoolean(KEY_SHOW_COUNTDOWN+baseDevice.getMac(),true)){
//				data[index] = 0x0F;
//				index++;
//			}
            for (int i = index; i < 20; i++) {
                data[i] = (byte) 0xff;
            }
            BaseDevice baseDevice = mainService.getCurrentDevice();
            if (baseDevice != null && baseDevice.getDeviceType() != BaseDevice.TYPE_W311T) {
                mainService.sendCustomCmd(data);
            } else if (baseDevice != null) {
                /*boolean showLogo, boolean showCala, boolean showDist, boolean showSportTime, boolean showProgress, boolean showEmotion, boolean showAlarm,
                boolean showSmsMissedCall, boolean showIncomingReminder, boolean showMsgContentPush*/
                mainService.setDisplay(new DisplaySet(false, helper.getBoolean(KEY_SHOW_CALORIES + baseDevice.getMac(), true),
                        helper.getBoolean(KEY_SHOW_DISTANCE + baseDevice.getMac(), true), helper.getBoolean(KEY_SHOW_TIME + baseDevice.getMac(), true),
                        helper.getBoolean(KEY_SHOW_PERCENT + baseDevice.getMac(), true), helper.getBoolean(KEY_SHOW_FACE + baseDevice.getMac(), true),
                        helper.getBoolean(KEY_SHOW_ALARM + baseDevice.getMac(), true), helper.getBoolean(KEY_SHOW_SMS_CALL_MISSED + baseDevice.getMac(), true),
                        helper.getBoolean(KEY_SHOW_CALL + baseDevice.getMac(), true), helper.getBoolean(KEY_SHOW_SMS + baseDevice.getMac(), true)));
            }
            // showDialog(getString(R.string.setting));
        }
    }


    public void save() {
        MainService mainService = MainService.getInstance(this);
        if (isConnected(this)) {
            //initDisplay(this);
            notificationEntry.setAllowCall(esb_call_switch.getStatus());
            notificationEntry.setAllowSMS(esb_message_switch.getStatus());
            notificationEntry.setAllowApp(true);
            notificationEntry.setOpenNoti(esb_app_switch.getStatus());
            notificationEntry.setShowDetail(true);

            /*ConfigHelper.getInstance(this).putBoolean(DisplayActivity.KEY_SHOW_CALL + mainService.getCurrentDevice()
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
                ;
            }*/

        }
    }

    private static boolean isConnected(Context ctx) {
        MainService mainService = MainService.getInstance(ctx);
        if (mainService == null || (mainService != null && mainService.getConnectionState() != BaseController
                .STATE_CONNECTED)) {
            Toast.makeText(ctx, ctx.getResources().getString(R.string.please_bind), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
