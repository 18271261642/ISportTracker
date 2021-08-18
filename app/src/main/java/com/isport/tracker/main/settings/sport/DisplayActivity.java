package com.isport.tracker.main.settings.sport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.DisplaySet;
import com.isport.isportlibrary.tools.Constants;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.view.MyDialog;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class DisplayActivity extends BaseActivity implements View.OnClickListener {

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

    private MyDialog mMyDialog;
    private GridView mGridView;
    private GridAdapter mAdapter;
    private List<ItemInfo> mItemInfoList = new ArrayList<ItemInfo>();
    BaseDevice baseDevice;
    boolean isRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_display);

        MainService mainService = MainService.getInstance(this);
        if (mainService == null || (mainService != null && mainService.getCurrentDevice() == null)) {
            finish();
        } else {
            isRegister = true;
            baseDevice = mainService.getCurrentDevice();
            initUI();
            EventBus.getDefault().register(this);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_QUERY_DISPLAY);
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
            if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                mainService.queryDisplayAndDoNotDisturb();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (isRegister) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }
        if (mMyDialog != null && mMyDialog.isShowing()) {
            mMyDialog.dismiss();
        }
    }

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case 0x09:
                Toast.makeText(this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                if (mMyDialog != null && mMyDialog.isShowing()) {
                    mMyDialog.dismiss();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_back:
                DisplayActivity.this.finish();
                break;
            case R.id.text_save:
                clickSave();
                break;
            default:
                break;
        }
    }

    private boolean isConnected() {
        MainService mainService = MainService.getInstance(this);
        if (mainService == null || (mainService != null && mainService.getConnectionState() != BaseController.STATE_CONNECTED)) {
            Toast.makeText(this, getString(R.string.please_bind), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 保存数据
     */
    private void clickSave() {
        if (isConnected()) {
            ConfigHelper helper = ConfigHelper.getInstance(this);
            for (int i = 0; i < mItemInfoList.size(); i++) {
                ItemInfo info = mItemInfoList.get(i);
                helper.putBoolean(info.type + baseDevice.getMac(), info.isSelected());
            }
            MainService mainService = MainService.getInstance(this);
            byte[] data = new byte[20];
            data[0] = (byte) 0xbe;
            data[1] = (byte) 0x01;
            data[2] = (byte) 0x08;
            data[3] = (byte) 0xfe;
            int index = 4;

            if (helper.getBoolean(KEY_SHOW_CALORIES + baseDevice.getMac(), true)) {
                data[index] = 0x03;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_DISTANCE + baseDevice.getMac(), true)) {//displaySet.isShowDist()) {
                data[index] = 0x04;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_TIME + baseDevice.getMac(), true)) {//.isShowSportTime()) {
                data[index] = 0x05;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_PERCENT + baseDevice.getMac(), true)) {//displaySet.isShowProgress()) {
                data[index] = 0x06;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_FACE + baseDevice.getMac(), true)) {//displaySet.isShowEmotion()) {
                data[index] = 0x07;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_ALARM + baseDevice.getMac(), true)) {//displaySet.isShowAlarm()) {
                data[index] = 0x08;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_SMS_CALL_MISSED + baseDevice.getMac(), false)) {//displaySet.isShowSmsMissedCall()) {
                data[index] = 0x0A;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_CALL + baseDevice.getMac(), true)) {//displaySet.isShowIncomingReminder()) {
                data[index] = 0x0B;
                index++;
            }
            if (helper.getBoolean(KEY_SHOW_SMS + baseDevice.getMac(), true)) {//.isShowMsgContent()) {
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
            showDialog(getString(R.string.setting));
        }
    }


    /**
     * 显示等待对话框
     *
     * @param msg
     */
    private void showDialog(String msg) {
        if (mMyDialog == null)
            mMyDialog = new MyDialog(this, msg, false);
        mMyDialog.setMsg(msg);

        if (mMyDialog.isShowing() == false)
            mMyDialog.show();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        ConfigHelper helper = ConfigHelper.getInstance(this);
        int deviceType = baseDevice.getDeviceType();
        String deviceName = baseDevice.getName();
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
        if (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice.getDeviceType() == BaseDevice.TYPE_W311T || baseDevice.getDeviceType() == BaseDevice.TYPE_AT200 || baseDevice.getDeviceType() == BaseDevice.TYPE_AS97) {
            calSelected = true;///卡路里
            callSelected = true;//电话
            smsSelected = true;//短信
        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307N || baseDevice.getDeviceType() == BaseDevice.TYPE_W240N ||
                baseDevice.getDeviceType() == BaseDevice.TYPE_W240B || baseDevice.getDeviceType() == BaseDevice.TYPE_W285B ||
                baseDevice.getDeviceType() == BaseDevice.TYPE_W301N) {
            calSelected = true;///卡路里
            callSelected = true;
            distSelected = true;///距离
            smscallMissedCallSelected = true;///未接来电短信数量
            timeSelected = true;///运动时间
            alarmSelected = true;///闹钟
            perSelected = true;///百分比
            faceSelected = true;///笑脸

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307S || baseDevice.getDeviceType() == BaseDevice.TYPE_W307S_SPACE || baseDevice.getDeviceType() == BaseDevice.TYPE_W240S || baseDevice.getDeviceType() == BaseDevice.TYPE_AT100) {
            calSelected = true;///卡路里
            callSelected = true;//电话
            smsSelected = true;///短信
            distSelected = true;///距离
            timeSelected = true;///运动时间
            alarmSelected = true;///闹钟
            perSelected = true;///百分比
            faceSelected = true;///笑脸
            if (baseDevice.getName().toLowerCase().contains("w301b") || baseDevice.getName().toLowerCase().contains("w307e")) {
                faceSelected = false;
            }
        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W301S) {
            calSelected = true;///卡路里
            callSelected = true;//电话
            smsSelected = true;///短信
            distSelected = true;///距离
            timeSelected = true;///运动时间
            alarmSelected = true;///闹钟
            perSelected = true;///百分比
            faceSelected = true;///笑脸

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W285S || baseDevice.getDeviceType() == BaseDevice.TYPE_SAS80) {
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
            faceSelected = true;
        }
        if (baseDevice.getName().toLowerCase().contains("beat")) {
            faceSelected = true;
        }

        ItemInfo infoCal = new ItemInfo(KEY_SHOW_CALORIES, R.drawable.selector_device_calories, true, helper.getBoolean(KEY_SHOW_CALORIES + baseDevice.getMac(), calSelected));
        ItemInfo infoDist = new ItemInfo(KEY_SHOW_DISTANCE, R.drawable.selector_device_distance, true, helper.getBoolean(KEY_SHOW_DISTANCE + baseDevice.getMac(), distSelected));
        ItemInfo infoCall = new ItemInfo(KEY_SHOW_CALL, R.drawable.selector_device_call, true, helper.getBoolean(KEY_SHOW_CALL + baseDevice.getMac(), callSelected));
        ItemInfo infoSms = new ItemInfo(KEY_SHOW_SMS, R.drawable.selector_device_sms, true, helper.getBoolean(KEY_SHOW_SMS + baseDevice.getMac(), smsSelected));
        ItemInfo infoSmsCallMissed = new ItemInfo(KEY_SHOW_SMS_CALL_MISSED, R.drawable.selector_device_sms, true, helper.getBoolean(KEY_SHOW_SMS_CALL_MISSED + baseDevice.getMac(), smscallMissedCallSelected));
        ItemInfo infoTime = new ItemInfo(KEY_SHOW_TIME, R.drawable.selector_device_time, true, helper.getBoolean(KEY_SHOW_TIME + baseDevice.getMac(), timeSelected));
        ItemInfo infoAlarm = new ItemInfo(KEY_SHOW_ALARM, R.drawable.selector_device_alarm, true, helper.getBoolean(KEY_SHOW_ALARM + baseDevice.getMac(), alarmSelected));
        ItemInfo infoPer = new ItemInfo(KEY_SHOW_PERCENT, R.drawable.selector_device_percent, true, helper.getBoolean(KEY_SHOW_PERCENT + baseDevice.getMac(), perSelected));
        ItemInfo infoFace = new ItemInfo(KEY_SHOW_FACE, R.drawable.selector_device_face, true, helper.getBoolean(KEY_SHOW_FACE + baseDevice.getMac(), faceSelected));
        //ItemInfo infoCountDown = new ItemInfo(KEY_SHOW_COUNTDOWN, R.drawable.selector_countdown, true, helper.getBoolean(KEY_SHOW_COUNTDOWN+baseDevice.getMac(),  countDownSelected));

        if (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice.getDeviceType() == BaseDevice.TYPE_W311T || baseDevice.getDeviceType() == BaseDevice.TYPE_AT200 || baseDevice.getDeviceType() == BaseDevice.TYPE_AS97) {
            infoSmsCallMissed.setShow(false);
            infoCall.setShow(false);
            infoSms.setShow(false);
            if (baseDevice.getName().toLowerCase().contains("w509a")) {
                infoFace.setShow(false);
            }
        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307N) {
            infoSms.setShow(false);

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307S || baseDevice.getDeviceType() == BaseDevice.TYPE_W307S_SPACE || baseDevice.getDeviceType() == BaseDevice.TYPE_W240S || baseDevice.getDeviceType() == BaseDevice.TYPE_AT100) {
            infoSmsCallMissed.setShow(false);
            infoCall.setShow(false);
            infoSms.setShow(false);
            if (baseDevice.getName().toLowerCase().contains("w301b") || baseDevice.getName().toLowerCase().contains("w307e")) {
                infoFace.setShow(false);
            }

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W301S) {
            infoSmsCallMissed.setShow(false);
            infoCall.setShow(false);
            infoSms.setShow(false);

        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W285S || baseDevice.getDeviceType() == BaseDevice.TYPE_SAS80) {
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
        } else if (baseDevice.getDeviceType() == BaseDevice.TYPE_W240N || baseDevice.getDeviceType() == BaseDevice.TYPE_W285B ||
                baseDevice.getDeviceType() == BaseDevice.TYPE_W240B || baseDevice.getDeviceType() == BaseDevice.TYPE_W301N) {
            infoSms.setShow(false);
        }

        if (baseDevice.getName().toLowerCase().contains("as87")) {
            infoFace.setShow(false);
        }

        if (baseDevice.getName().toLowerCase().contains("as97")) {
            infoFace.setShow(false);
        }
        if (baseDevice.getName().toLowerCase().contains("sas87")) {
            infoFace.setShow(true);
        }
        if (baseDevice.getName().toLowerCase().contains("beat")) {
            infoFace.setShow(true);
        }
        ItemInfo[] infos;
        if (baseDevice.getName().toLowerCase().contains("w520")) {
            infos = new ItemInfo[2];
            infos[0] = infoCal;
            infos[1] = infoDist;
        } else {
            infos = new ItemInfo[9];
            infos[0] = infoCal;
            infos[1] = infoDist;
            infos[2] = infoCall;
            infos[3] = infoSms;
            infos[4] = infoSmsCallMissed;
            infos[5] = infoTime;
            infos[6] = infoAlarm;
            infos[7] = infoPer;
            infos[8] = infoFace;
        }
        //ItemInfo[] infos = {infoCal, infoDist, infoCall, infoSms, infoSmsCallMissed, infoTime, infoAlarm, infoPer, infoFace};
        for (int i = 0; i < infos.length; i++) {
            ItemInfo info = infos[i];
            helper.putBoolean(info.type + baseDevice.getMac(), info.isSelected());
        }
        mItemInfoList.clear();
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].isShow()) {
                mItemInfoList.add(infos[i]);
            }
        }
        mGridView = (GridView) findViewById(R.id.display_gridview);
        mAdapter = new GridAdapter();
        mGridView.setAdapter(mAdapter);
    }

    class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItemInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            final int pos = position;
            final ItemInfo info = mItemInfoList.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(DisplayActivity.this).inflate(R.layout.item_display_setting_listview, null);
                holder = new Holder();
                holder.imgIcon = (ImageView) convertView.findViewById(R.id.item_img);
                holder.imgIcon.setImageResource(info.resId);
                holder.imgIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemInfoList.get(pos).setSelected(!info.isSelected);
                        notifyDataSetChanged();
                    }
                });
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();

            holder.imgIcon.setSelected(info.isSelected);
            return convertView;
        }

        class Holder {
            ImageView imgIcon;
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_QUERY_DISPLAY)) {
               /* DisplaySet displaySet = intent.getExtras().getParcelable(Constants.EXTRA_QUERY_DISPLAY);
                ConfigHelper configHelper = ConfigHelper.getInstance(context);
                MainService mainService = MainService.getInstance(context);
                if(mainService != null){
                    BaseDevice baseDevice = mainService.getCurrentDevice();
                    if(baseDevice != null){
                        String mac = baseDevice.getMac();
                        configHelper.putBoolean(KEY_SHOW_CALORIES+mac,displaySet.isShowCala());
                        configHelper.putBoolean(KEY_SHOW_DISTANCE+mac,displaySet.isShowDist());
                        configHelper.putBoolean(KEY_SHOW_TIME+mac,displaySet.isShowSportTime());
                        configHelper.putBoolean(KEY_SHOW_PERCENT+mac,displaySet.isShowProgress());
                        configHelper.putBoolean(KEY_SHOW_FACE+mac,displaySet.isShowEmotion());
                        configHelper.putBoolean(KEY_SHOW_ALARM+mac,displaySet.isShowAlarm());
                        configHelper.putBoolean(KEY_SHOW_SMS+mac,displaySet.isShowMsgContent());
                        configHelper.putBoolean(KEY_SHOW_CALL+mac,displaySet.isShowIncomingReminder());
                        configHelper.putBoolean(KEY_SHOW_SMS_CALL_MISSED+mac,displaySet.isShowSmsMissedCall());
                        initUI();
                    }

                }*/
            }
        }
    };

}

class ItemInfo {
    String type;
    boolean isShow;
    boolean isSelected;///是否选择
    int resId;///资源ID

    public ItemInfo(String type, int resId, boolean isShow, boolean isSelected) {
        this.type = type;
        this.resId = resId;
        this.isShow = isShow;
        this.isSelected = isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public boolean isShow() {
        return this.isShow;
    }

    public boolean isSelected() {
        return this.isSelected;
    }
}