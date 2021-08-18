package com.isport.tracker.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetTime;
import com.isport.tracker.view.EasySwitchButton;

/**
 * @创建者 bear
 * @创建时间 2019/4/19 16:37
 * @描述
 */
public class RaiseHandWithtimeActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton.OnOpenedListener {

    private TextView all_open;
    private TextView all_close;
    private TextView open_time, tv_start_time, tv_end_time;
    private EasySwitchButton raised_hands_screen, raised_hands_time;
    private int hour;
    private int min;
    private int index;
    private int startHour = 22, startMin = 0, endHour = 6, endMin = 0;

    private String ALARM_CONFIG = "alarm_config";
    private boolean isAllOpen, isTimeOpen;
    private String raise_open = "raise_all_open";
    private String raise_time_open = "raise_time_open";
    private String start_time_hour = "raise_time_start_hour";
    private String start_time_min = "raise_time_start_min";
    private String end_time_hour = "raise_time_end_hour";
    private String end_time_min = "raise_time_end_min";


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raisehand_withtime);
        initView();
    }

    private void initView() {
        all_open = (TextView) findViewById(R.id.all_open);
        all_close = (TextView) findViewById(R.id.all_close);
        open_time = (TextView) findViewById(R.id.open_time);
        tv_start_time = (TextView) findViewById(R.id.tv_start_time);
        tv_end_time = (TextView) findViewById(R.id.tv_end_time);
        raised_hands_screen = findViewById(R.id.raised_hands_screen);
        raised_hands_time = findViewById(R.id.alarm_item_swbutton);
        raised_hands_screen.setOnCheckChangedListener(this);
        raised_hands_time.setOnCheckChangedListener(this);
        all_open.setOnClickListener(this);
        all_close.setOnClickListener(this);
        open_time.setOnClickListener(this);

        sharedPreferences = getSharedPreferences(ALARM_CONFIG, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isAllOpen = sharedPreferences.getBoolean(raise_open, false);
        isTimeOpen = sharedPreferences.getBoolean(raise_time_open, false);

        startHour = sharedPreferences.getInt(start_time_hour, 22);
        startMin = sharedPreferences.getInt(start_time_min, 0);
        endHour = sharedPreferences.getInt(end_time_hour, 6);
        endMin = sharedPreferences.getInt(end_time_min, 0);

        raised_hands_screen.setStatus(isAllOpen);
        raised_hands_time.setStatus(isTimeOpen);


        tv_start_time.setText(getTimeString(startHour, startMin));
        tv_end_time.setText(getTimeString(endHour, endMin));
    }

    @Override
    public void onClick(View view) {
        MainService mainService = MainService.getInstance(this);
        CmdController mCmdController = (CmdController) mainService.getCurrentController();
        switch (view.getId()) {
            case R.id.all_open:
                mCmdController.raiseHand(1, 0, 0, 0, 0);
                break;
            case R.id.all_close:
                mCmdController.raiseHand(2, 0, 0, 0, 0);
                break;
            case R.id.open_time:
                mCmdController.raiseHand(0, 21, 0, 8, 0);
                break;
            case R.id.linear_back:
                finish();
                break;
            case R.id.alarm_set_save:
                if (raised_hands_screen.getStatus()) {
                    if (isConnected()) {
                        editor.putBoolean(raise_open, true).commit();
                    }
                    mCmdController.raiseHand(1, 0, 0, 23, 59);
                } else if (!raised_hands_screen.getStatus() && !raised_hands_time.getStatus()) {
                    if (isConnected()) {
                        editor.putBoolean(raise_open, false).commit();
                    }
                    if (isConnected()) {
                        editor.putBoolean(raise_time_open, false).commit();
                    }
                    mCmdController.raiseHand(2, 0, 0, 23, 59);
                } else if (raised_hands_time.getStatus()) {
                    //这里的时间才需要去保存
                    mCmdController.raiseHand(0, endHour, endMin, startHour, startMin);
                    save();
                }
                break;
            case R.id.alarm_item_start_time:
                if (raised_hands_time.getStatus()) {
                    setDialog(startHour, startMin, requsetStartCode);
                }
                break;
            case R.id.alarm_item_end_time:
                if (raised_hands_time.getStatus()) {
                    setDialog(endHour, endMin, requestEndCode);
                }
                break;
        }
    }


    @Override
    public void onChecked(View v, boolean isOpened) {

        if (!isOpened) {

            if (v.getId() == R.id.raised_hands_screen) {

            } else {

            }
        } else {
            if (v.getId() == R.id.raised_hands_screen) {
                raised_hands_time.setStatus(false);
                /*if (isConnected()) {
                    editor.putBoolean(raise_open, true).commit();
                }
                mCmdController.raiseHand(1, 0, 0, 23, 59);*/
            } else {
                raised_hands_screen.setStatus(false);
            }
        }
    }

    public final int requsetStartCode = 1, requestEndCode = 2;

    public void setDialog(int time, int minute, int requestCode) {
        Intent intent = new Intent(this, DialogSetTime.class);
        intent.putExtra(DialogSetTime.EXTRA_FORMAT, "HH:mm");
        intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", time) + ":" + String.format("%02d", minute));
        intent.putExtra(DialogSetTime.EXTRA_IS_24_HOUR, is24Hour());
        intent.putExtra(DialogSetTime.EXTRA_IS_AM, hour < 12);
        /*Intent intent = new Intent(this, DialogSetTime.class);
        intent.putExtra(DialogSetTime.EXTRA_IS_24_HOUR, is24Hour());
        intent.putExtra(DialogSetTime.EXTRA_IS_AM, time < 12);
        if (!is24Hour()) {
            if (time == 0) {
                time = 12;
            } else if (time == 12) {
                time = 12;
            } else {
                time = time - 12;
            }
        }*/
        /*intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d",time) + ":" +  String.format("%02d",minute));
        intent.putExtra(DialogSetTime.EXTRA_FORMAT, "HH:mm");*/
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String date = data.getStringExtra(DialogSetTime.EXTRA_DATE);
            boolean isAm = data.getBooleanExtra(DialogSetTime.EXTRA_IS_AM, true);
            boolean is24 = data.getBooleanExtra(DialogSetTime.EXTRA_IS_24_HOUR, true);
            int index = data.getIntExtra(DialogSetTime.EXTRA_INDEX, Integer.valueOf(date.split(":")[0]));
            String[] strs = date.split(":");
            int tphour = Integer.valueOf(strs[0]);
            min = Integer.valueOf(strs[1]);
            if (!is24 && !isAm) {
                if (tphour < 12) {
                    hour = tphour + 12;
                }
            } else if (!is24 && isAm && tphour == 12) {
                hour = 0;
            } else {
                hour = tphour;
            }

            if (requestCode == requsetStartCode) {
                startHour = hour;
                startMin = min;
                tv_start_time.setText(getTimeString(hour, min));
            } else {
                if (hour == 0 && min == 0) {
                    endHour = 23;
                    endMin = 59;
                } else {
                    endHour = hour;
                    endMin = min;
                }
                tv_end_time.setText(getTimeString(hour, min));
            }
        }
    }

    public String getTimeString(int hour, int minute) {
        boolean isAm = (hour < 12);
        int tphour = (is24Hour() ? hour : (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)));
        return String.format("%02d", tphour) + ":" + String.format("%02d", minute) + (((!is24Hour()) ? (isAm ?
                getString(R.string.AM) : getString(R.string.PM)) : ""));
    }

    private boolean is24Hour() {
        return android.text.format.DateFormat.is24HourFormat(this);
    }


    public void save() {
        if (isConnected()) {
            editor.putBoolean(raise_time_open, raised_hands_time.getStatus()).commit();
            editor.putInt(start_time_hour, startHour).commit();
            editor.putInt(start_time_min, startMin).commit();
            editor.putInt(end_time_hour, endHour).commit();
            editor.putInt(end_time_min, endMin).commit();
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

}
