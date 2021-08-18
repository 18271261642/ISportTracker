package com.isport.tracker.main.settings.sport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.AlarmEntry;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.tools.Constants;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetTime;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.UIUtils;
import com.isport.tracker.view.EasySwitchButton;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;

public class AlarmActivity extends BaseActivity implements OnClickListener, EasySwitchButton.OnOpenedListener {

    private String ALARM_CONFIG = "alarm_config";

    private String ALARM_ONE_STATE = "alarm_one_state";
    private String ALARM_ONE_DESCRIP = "alarm_one_descrip";
    private String ALARM_ONE_REPEAT = "alarm_one_repeat";
    private String ALARM_ONE_TIME = "alarm_one_time";

    private String ALARM_TWO_STATE = "alarm_two_state";
    private String ALARM_TWO_DESCRIP = "alarm_two_descrip";
    private String ALARM_TWO_REPEAT = "alarm_two_repeat";
    private String ALARM_TWO_TIME = "alarm_two_time";

    private String ALARM_THREE_STATE = "alarm_three_state";
    private String ALARM_THREE_DESCRIP = "alarm_three_descrip";
    private String ALARM_THREE_REPEAT = "alarm_three_repeat";
    private String ALARM_THREE_TIME = "alarm_three_time";

    private String ALARM_FOUR_STATE = "alarm_four_state";
    private String ALARM_FOUR_DESCRIP = "alarm_four_descrip";
    private String ALARM_FOUR_REPEAT = "alarm_four_repeat";
    private String ALARM_FOUR_TIME = "alarm_four_time";

    private String ALARM_FRI_STATE = "alarm_fri_state";
    private String ALARM_FRI_DESCRIP = "alarm_fri_descrip";
    private String ALARM_FRI_REPEAT = "alarm_fri_repeat";
    private String ALARM_FRI_TIME = "alarm_fri_time";

    private int oneHour, oneMin;
    private int twoHour, twoMin;
    private int threeHour, threeMin;
    private int fourHour, fourMin;
    private int friHour, friMin;
    private byte oneRepeat, twoRepeat, threeRepeat, fourRepeat, friRepeat;
    private boolean isOneOn, isTwoOn, isThreeOn, isFourOn, isFriOn;
    private String oneDescrip, twoDescrip, threeDescrip, fourDescrip, friDescrip;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    String[] weeks;
    private ArrayList<AlarmEntry> listEntry;

    private TextView oneTvTitle, twoTvTitle, threeTvTitle, fourTvTitle, friTvTitle;
    private TextView alarm_e1_week, alarm_e2_week, alarm_e3_week, alarm_e4_week, alarm_e5_week;
    private TextView oneTvTime, twoTvTime, threeTvTime, fourTvTime, friTvTime;
    private EasySwitchButton esbOne, esbTwo, esbThree, esbFour, esbFri;
    private View viewAlarm1, viewAlarm2, viewAlarm3, viewAlarm4, viewAlarm5;
    private int curIndex = 1;
    int[] ids = {R.string.sun, R.string.mon, R.string.tue, R.string.wed,
            R.string.thu, R.string.fri, R.string.sat};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_alarm);
        initValue();
        initControl();
        updateUI();
        EventBus.getDefault().register(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_QUERY_ALARM);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            //mainService.queryAlarmInfo();
//            BaseController currentController = mainService.getCurrentController();
//            if (currentController instanceof CmdController){
//                ((CmdController) currentController).setDeviceSetting(new OnDeviceSetting() {
//                    @Override
//                    public void alarmSetting(boolean success) {
//                        super.alarmSetting(success);
//                        if (success){
//                            Toast.makeText(AlarmActivity.this,getString(R.string.alarm_clock_set_successfully),Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
        }
    }


    private void initControl() {

        viewAlarm1 = findViewById(R.id.alarm_e1);
        viewAlarm2 = findViewById(R.id.alarm_e2);
        viewAlarm3 = findViewById(R.id.alarm_e3);
        viewAlarm4 = findViewById(R.id.alarm_e4);
        viewAlarm5 = findViewById(R.id.alarm_e5);

        alarm_e1_week = (TextView) findViewById(R.id.alarm_e1_week);
        alarm_e2_week = (TextView) findViewById(R.id.alarm_e2_week);
        alarm_e3_week = (TextView) findViewById(R.id.alarm_e3_week);
        alarm_e4_week = (TextView) findViewById(R.id.alarm_e4_week);
        alarm_e5_week = (TextView) findViewById(R.id.alarm_e5_week);

        oneTvTitle = (TextView) findViewById(R.id.alarm_e1_tv);
        twoTvTitle = (TextView) findViewById(R.id.alarm_e2_tv);
        threeTvTitle = (TextView) findViewById(R.id.alarm_e3_tv);
        fourTvTitle = (TextView) findViewById(R.id.alarm_e4_tv);
        friTvTitle = (TextView) findViewById(R.id.alarm_e5_tv);

        oneTvTime = (TextView) findViewById(R.id.alarm_e1_time);
        twoTvTime = (TextView) findViewById(R.id.alarm_e2_time);
        threeTvTime = (TextView) findViewById(R.id.alarm_e3_time);
        fourTvTime = (TextView) findViewById(R.id.alarm_e4_time);
        friTvTime = (TextView) findViewById(R.id.alarm_e5_time);

        esbOne = (EasySwitchButton) findViewById(R.id.event1);
        esbTwo = (EasySwitchButton) findViewById(R.id.event2);
        esbThree = (EasySwitchButton) findViewById(R.id.event3);
        esbFour = (EasySwitchButton) findViewById(R.id.event4);
        esbFri = (EasySwitchButton) findViewById(R.id.event5);

        esbOne.setOnCheckChangedListener(this);
        esbTwo.setOnCheckChangedListener(this);
        esbThree.setOnCheckChangedListener(this);
        esbFour.setOnCheckChangedListener(this);
        esbFri.setOnCheckChangedListener(this);

        if (MainService.getInstance(this) == null) {
            finish();
            return;
        }
        BaseDevice baseDevice = MainService.getInstance(this).getCurrentDevice();
        if (baseDevice != null) {
            int type = baseDevice.getDeviceType();
            if (type == BaseDevice.TYPE_W337B) {
                viewAlarm5.setVisibility(View.GONE);
            }
            if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                viewAlarm5.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 从高位到低位，依次为星期6 星期5 星期4 星期3 星期2 星期1 星期日
     *
     * @param repeat
     * @return
     */
    private boolean[] getRepeatBytes(byte repeat) {
        boolean[] bs = new boolean[7];
        int rp = repeat & 0x7f;
        for (int i = 0; i < 7; i++) {
            int state = (((1 << i) & rp) >> i);
            bs[i] = state == 1;
        }

        return bs;
    }

    private void updateUI() {
        AlarmEntry alarmEntry1 = listEntry.get(0);
        oneDescrip = alarmEntry1.getDescription();
        isOneOn = alarmEntry1.isOn();
        oneHour = alarmEntry1.getStartHour();
        oneMin = alarmEntry1.getStartMin();
        oneRepeat = alarmEntry1.getRepeat();
        //TODO 展示闹钟星期
        boolean[] oneBs = getRepeatBytes(oneRepeat);
        String oneRepeatStr = "";
        for (int i = 0; i < 7; i++) {
            oneRepeatStr = oneRepeatStr + (oneBs[i] ? (UIUtils.getString(ids[i]) + " ") : "");
        }
        esbOne.setStatus(isOneOn);
        oneTvTitle.setText(oneDescrip);
        oneTvTime.setText(getTimeString(oneHour, oneMin));
        alarm_e1_week.setText(oneRepeatStr);

        AlarmEntry alarmEntry2 = listEntry.get(1);
        twoDescrip = alarmEntry2.getDescription();
        isTwoOn = alarmEntry2.isOn();
        twoHour = alarmEntry2.getStartHour();
        twoMin = alarmEntry2.getStartMin();
        twoRepeat = alarmEntry2.getRepeat();
        //TODO 展示闹钟星期
        boolean[] twoBs = getRepeatBytes(twoRepeat);
        String twoRepeatStr = "";
        for (int i = 0; i < 7; i++) {
            twoRepeatStr = twoRepeatStr + (twoBs[i] ? (UIUtils.getString(ids[i]) + " ") : "");
        }
        esbTwo.setStatus(isTwoOn);
        twoTvTitle.setText(twoDescrip);
        twoTvTime.setText(getTimeString(twoHour, twoMin));
        alarm_e2_week.setText(twoRepeatStr);

        AlarmEntry alarmEntry3 = listEntry.get(2);
        threeDescrip = alarmEntry3.getDescription();
        isThreeOn = alarmEntry3.isOn();
        threeHour = alarmEntry3.getStartHour();
        threeMin = alarmEntry3.getStartMin();
        threeRepeat = alarmEntry3.getRepeat();
        boolean[] threeBs = getRepeatBytes(threeRepeat);
        String threeRepeatStr = "";
        for (int i = 0; i < 7; i++) {
            threeRepeatStr = threeRepeatStr + (threeBs[i] ? (UIUtils.getString(ids[i]) + " ") : "");
        }
        esbThree.setStatus(isThreeOn);
        threeTvTitle.setText(threeDescrip);
        threeTvTime.setText(getTimeString(threeHour, threeMin));
        alarm_e3_week.setText(threeRepeatStr);

        AlarmEntry alarmEntry4 = listEntry.get(3);
        fourDescrip = alarmEntry4.getDescription();
        isFourOn = alarmEntry4.isOn();
        fourHour = alarmEntry4.getStartHour();
        fourMin = alarmEntry4.getStartMin();
        fourRepeat = alarmEntry4.getRepeat();
        boolean[] fourBs = getRepeatBytes(fourRepeat);
        String fourRepeatStr = "";
        for (int i = 0; i < 7; i++) {
            fourRepeatStr = fourRepeatStr + (fourBs[i] ? (UIUtils.getString(ids[i]) + " ") : "");
        }
        esbFour.setStatus(isFourOn);
        fourTvTitle.setText(fourDescrip);
        fourTvTime.setText(getTimeString(fourHour, fourMin));
        alarm_e4_week.setText(fourRepeatStr);

        if (listEntry.size() > 4) {
            AlarmEntry alarmEntry5 = listEntry.get(4);
            friDescrip = alarmEntry5.getDescription();
            isFriOn = alarmEntry5.isOn();
            friHour = alarmEntry5.getStartHour();
            friMin = alarmEntry5.getStartMin();
            friRepeat = alarmEntry5.getRepeat();
            boolean[] friBs = getRepeatBytes(friRepeat);
            String friRepeatStr = "";
            for (int i = 0; i < 7; i++) {
                friRepeatStr = friRepeatStr + (friBs[i] ? (UIUtils.getString(ids[i]) + " ") : "");
            }
            esbFri.setStatus(isFriOn);
            friTvTitle.setText(friDescrip);
            friTvTime.setText(getTimeString(friHour, friMin));
            alarm_e5_week.setText(friRepeatStr);
        }

    }

    private void initValue() {
        weeks = getResources().getStringArray(R.array.week);
        sharedPreferences = getSharedPreferences(ALARM_CONFIG, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String[] ones = sharedPreferences.getString(ALARM_ONE_TIME, "08:00").split(":");
        oneHour = Integer.valueOf(ones[0]);
        oneMin = Integer.valueOf(ones[1]);
        oneRepeat = (byte) sharedPreferences.getInt(ALARM_ONE_REPEAT, 0);
        oneDescrip = sharedPreferences.getString(ALARM_ONE_DESCRIP, getString(R.string.EVENT));
        isOneOn = sharedPreferences.getBoolean(ALARM_ONE_STATE, false);

        String[] twos = sharedPreferences.getString(ALARM_TWO_TIME, "08:00").split(":");
        twoHour = Integer.valueOf(twos[0]);
        twoMin = Integer.valueOf(twos[1]);
        twoRepeat = (byte) sharedPreferences.getInt(ALARM_TWO_REPEAT, 0);
        twoDescrip = sharedPreferences.getString(ALARM_TWO_DESCRIP, getString(R.string.EVENT));
        isTwoOn = sharedPreferences.getBoolean(ALARM_TWO_STATE, false);

        String[] threes = sharedPreferences.getString(ALARM_THREE_TIME, "08:00").split(":");
        threeHour = Integer.valueOf(threes[0]);
        threeMin = Integer.valueOf(threes[1]);
        threeRepeat = (byte) sharedPreferences.getInt(ALARM_THREE_REPEAT, 0);
        threeDescrip = sharedPreferences.getString(ALARM_THREE_DESCRIP, getString(R.string.EVENT));
        isThreeOn = sharedPreferences.getBoolean(ALARM_THREE_STATE, false);

        String[] fours = sharedPreferences.getString(ALARM_FOUR_TIME, "08:00").split(":");
        fourHour = Integer.valueOf(fours[0]);
        fourMin = Integer.valueOf(fours[1]);
        fourRepeat = (byte) sharedPreferences.getInt(ALARM_FOUR_REPEAT, 0);
        fourDescrip = sharedPreferences.getString(ALARM_FOUR_DESCRIP, getString(R.string.EVENT));
        isFourOn = sharedPreferences.getBoolean(ALARM_FOUR_STATE, false);

        String[] fris = sharedPreferences.getString(ALARM_FRI_TIME, "08:00").split(":");
        friHour = Integer.valueOf(fris[0]);
        friMin = Integer.valueOf(fris[1]);
        friRepeat = (byte) sharedPreferences.getInt(ALARM_FRI_REPEAT, 0);
        friDescrip = sharedPreferences.getString(ALARM_FRI_DESCRIP, getString(R.string.EVENT));
        isFriOn = sharedPreferences.getBoolean(ALARM_FRI_STATE, false);

        listEntry = new ArrayList<>();
        AlarmEntry alarmEntry1 = new AlarmEntry(oneDescrip, oneHour, oneMin, oneRepeat, isOneOn);
        listEntry.add(alarmEntry1);
        AlarmEntry alarmEntry2 = new AlarmEntry(twoDescrip, twoHour, twoMin, twoRepeat, isTwoOn);
        listEntry.add(alarmEntry2);
        AlarmEntry alarmEntry3 = new AlarmEntry(threeDescrip, threeHour, threeMin, threeRepeat, isThreeOn);
        listEntry.add(alarmEntry3);
        AlarmEntry alarmEntry4 = new AlarmEntry(fourDescrip, fourHour, fourMin, fourRepeat, isFourOn);
        listEntry.add(alarmEntry4);
        AlarmEntry alarmEntry5 = new AlarmEntry(friDescrip, friHour, friMin, friRepeat, isFriOn);
        listEntry.add(alarmEntry5);
    }

    private boolean isConnected() {
        MainService mainService = MainService.getInstance(this);
        if (mainService == null || (mainService != null && mainService.getConnectionState() != BaseController.STATE_CONNECTED)) {
            Toast.makeText(this, getString(R.string.please_bind), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case 0x03:
//                Toast.makeText(this,getString(R.string.alarm_clock_set_successfully),Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.linear_back:
                finish();
                break;
            case R.id.alarm_e1:
                if (isConnected()) {
                    intent = new Intent(this, AlarmItemActivity.class);
                    intent.putExtra("index", 0);
                    intent.putParcelableArrayListExtra("list", listEntry);
                    startActivityForResult(intent, 102);
                }
                break;
            case R.id.alarm_e2:
                if (isConnected()) {
                    intent = new Intent(this, AlarmItemActivity.class);
                    intent.putExtra("index", 1);
                    intent.putParcelableArrayListExtra("list", listEntry);
                    startActivityForResult(intent, 102);
                }
                break;
            case R.id.alarm_e3:
                if (isConnected()) {
                    intent = new Intent(this, AlarmItemActivity.class);
                    intent.putExtra("index", 2);
                    intent.putParcelableArrayListExtra("list", listEntry);
                    startActivityForResult(intent, 102);
                }
                break;
            case R.id.alarm_e4:
                if (isConnected()) {
                    intent = new Intent(this, AlarmItemActivity.class);
                    intent.putExtra("index", 3);
                    intent.putParcelableArrayListExtra("list", listEntry);
                    startActivityForResult(intent, 102);
                }
                break;
            case R.id.alarm_e5:
                if (isConnected()) {
                    intent = new Intent(this, AlarmItemActivity.class);
                    intent.putExtra("index", 4);
                    intent.putParcelableArrayListExtra("list", listEntry);
                    startActivityForResult(intent, 102);
                }
                break;
            case R.id.alarm_set_save:
                if (isConnected()) {
                    MainService mainService = MainService.getInstance(this);
                    if (mainService != null) {
                        mainService.setAlarm(listEntry);
                    }
                    save();
                    Toast.makeText(AlarmActivity.this, getString(R.string.alarm_clock_set_successfully), Toast.LENGTH_SHORT).show();
                }
                break;
            /*case R.id.alarm_e1_time:
                curIndex = 1;
                if(isConnected()){
                    intent = getTimeIntent();
                    startActivityForResult(intent,101);
                }
                break;
            case R.id.alarm_e2_time:
                curIndex = 2;
                if(isConnected()){
                    intent = getTimeIntent();
                    startActivityForResult(intent,101);
                }
                break;
            case R.id.alarm_e3_time:
                curIndex = 3;
                if(isConnected()){
                    intent = getTimeIntent();
                    startActivityForResult(intent,101);
                }
                break;
            case R.id.alarm_e4_time:
                curIndex = 4;
                if(isConnected()){
                    intent = getTimeIntent();
                    startActivityForResult(intent,101);
                }
                break;
            case R.id.alarm_e5_time:
                curIndex = 5;
                if(isConnected()){
                    intent = getTimeIntent();
                    startActivityForResult(intent,101);
                }
                break;*/

        }
        if (intent != null) {

        }
    }

    private Intent getTimeIntent() {
        Intent intent = new Intent(this, DialogSetTime.class);
        intent.putExtra(DialogSetTime.EXTRA_IS_24_HOUR, is24Hour());
        intent.putExtra(DialogSetTime.EXTRA_FORMAT, "HH:mm");
        switch (curIndex) {
            case 1:
                intent.putExtra(DialogSetTime.EXTRA_IS_AM, oneHour < 12);
                intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", oneHour) + ":" + String.format("%02d", oneMin));
                break;
            case 2:
                intent.putExtra(DialogSetTime.EXTRA_IS_AM, twoHour < 12);
                intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", twoHour) + ":" + String.format("%02d", twoMin));
                break;
            case 3:
                intent.putExtra(DialogSetTime.EXTRA_IS_AM, threeHour < 12);
                intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", threeHour) + ":" + String.format("%02d", threeMin));
                break;
            case 4:
                intent.putExtra(DialogSetTime.EXTRA_IS_AM, fourHour < 12);
                intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", fourHour) + ":" + String.format("%02d", fourMin));
                break;
            case 5:
                intent.putExtra(DialogSetTime.EXTRA_IS_AM, friHour < 12);
                intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", friHour) + ":" + String.format("%02d", friMin));
                break;

        }
        return intent;
    }

    private boolean is24Hour() {
        return android.text.format.DateFormat.is24HourFormat(this);
    }

    public String getTimeString(int hour, int minute) {
        boolean isAm = (hour < 12);
        hour = (is24Hour() ? hour : (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)));
        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + (((!is24Hour()) ? (isAm ? getString(R.string.AM) : getString(R.string.PM)) : ""));
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        if (isConnected()) {
            switch (v.getId()) {
                case R.id.event1:
                    listEntry.get(0).setOn(isOpened);
                    break;
                case R.id.event2:
                    listEntry.get(1).setOn(isOpened);
                    break;
                case R.id.event3:
                    listEntry.get(2).setOn(isOpened);
                    break;
                case R.id.event4:
                    listEntry.get(3).setOn(isOpened);
                    break;
                case R.id.event5:
                    listEntry.get(4).setOn(isOpened);
                    break;
            }

            updateUI();

        } else {
            ((EasySwitchButton) v).setStatus(!isOpened);
        }

    }

    public void save() {
        if (isConnected()) {
            editor.putString(ALARM_ONE_DESCRIP, oneDescrip).commit();
            editor.putInt(ALARM_ONE_REPEAT, oneRepeat & 0xff).commit();
            editor.putString(ALARM_ONE_TIME, String.format("%02d", oneHour) + ":" + String.format("%02d", oneMin)).commit();
            editor.putBoolean(ALARM_ONE_STATE, isOneOn).commit();

            editor.putString(ALARM_TWO_DESCRIP, twoDescrip).commit();
            editor.putInt(ALARM_TWO_REPEAT, twoRepeat & 0xff).commit();
            editor.putString(ALARM_TWO_TIME, String.format("%02d", twoHour) + ":" + String.format("%02d", twoMin)).commit();
            editor.putBoolean(ALARM_TWO_STATE, isTwoOn).commit();

            editor.putString(ALARM_THREE_DESCRIP, threeDescrip).commit();
            editor.putInt(ALARM_THREE_REPEAT, threeRepeat & 0xff).commit();
            editor.putString(ALARM_THREE_TIME, String.format("%02d", threeHour) + ":" + String.format("%02d", threeMin)).commit();
            editor.putBoolean(ALARM_THREE_STATE, isThreeOn).commit();

            editor.putString(ALARM_FOUR_DESCRIP, fourDescrip).commit();
            editor.putInt(ALARM_FOUR_REPEAT, fourRepeat & 0xff).commit();
            editor.putString(ALARM_FOUR_TIME, String.format("%02d", fourHour) + ":" + String.format("%02d", fourMin)).commit();
            editor.putBoolean(ALARM_FOUR_STATE, isFourOn).commit();

            editor.putString(ALARM_FRI_DESCRIP, friDescrip).commit();
            editor.putInt(ALARM_FRI_REPEAT, friRepeat & 0xff).commit();
            editor.putString(ALARM_FRI_TIME, String.format("%02d", friHour) + ":" + String.format("%02d", friMin)).commit();
            editor.putBoolean(ALARM_FRI_STATE, isFriOn).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 101) {
            String date = data.getStringExtra(DialogSetTime.EXTRA_DATE);
            boolean isAm = data.getBooleanExtra(DialogSetTime.EXTRA_IS_AM, true);
            boolean is24 = data.getBooleanExtra(DialogSetTime.EXTRA_IS_24_HOUR, true);
            int index = data.getIntExtra(DialogSetTime.EXTRA_INDEX, Integer.valueOf(date.split(":")[0]));
            String[] strs = date.split(":");
            int hour = Integer.valueOf(strs[0]);
            int min = Integer.valueOf(strs[1]);
            if (!is24 && !isAm) {
                if (hour < 12) {
                    hour = hour + 12;
                }
            } else if (!is24 && isAm && hour == 12) {
                hour = 0;
            }
            switch (curIndex) {
                case 1:
                    listEntry.get(0).setStartHour(hour);
                    listEntry.get(0).setStartMin(min);
                    break;
                case 2:
                    listEntry.get(1).setStartHour(hour);
                    listEntry.get(1).setStartMin(min);
                    break;
                case 3:
                    listEntry.get(2).setStartHour(hour);
                    listEntry.get(2).setStartMin(min);
                    break;
                case 4:
                    listEntry.get(3).setStartHour(hour);
                    listEntry.get(3).setStartMin(min);
                    break;
                case 5:
                    listEntry.get(4).setStartHour(hour);
                    listEntry.get(4).setStartMin(min);
                    break;
            }
        } else if (data != null && resultCode == RESULT_OK) {
            listEntry = data.getParcelableArrayListExtra("list");
        }
        updateUI();
        save();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_QUERY_ALARM)) {
               /* ArrayList<AlarmEntry> alarmEntries = intent.getParcelableArrayListExtra(Constants.EXTRA_QUERY_ALARM);
                if(alarmEntries.size()>0){
                    if(alarmEntries.size()<listEntry.size()){
                        for (int i=0;i<alarmEntries.size();i++){
                            listEntry.remove(i);
                            listEntry.add(i,alarmEntries.get(i));
                        }
                    }else if(alarmEntries.size()>listEntry.size()){
                        for (int i=0;i<listEntry.size();i++){
                            listEntry.remove(i);
                            listEntry.add(i,alarmEntries.get(i));
                        }
                    }else {
                        listEntry.clear();
                        for (int i = 0;i<alarmEntries.size();i++){
                            listEntry.add(alarmEntries.get(i));
                        }
                    }
                    updateUI();
                }*/
            }
        }
    };
}
