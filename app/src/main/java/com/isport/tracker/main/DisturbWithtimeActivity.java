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
public class DisturbWithtimeActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton.OnOpenedListener {
    private TextView all_open;
    private TextView all_close;
    private EasySwitchButton alarm_item_swbutton;
    private TextView tv_start_time, tv_end_time;
    private int hour;
    private int min;
    private int index;
    private int startHour = 22, startMin = 0, endHour = 6, endMin = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String ALARM_CONFIG = "alarm_config";
    private boolean isAllOpen;
    private String disturb_open = "disturb_all_open";
    private String start_time_hour = "disturb_time_start_hour";
    private String start_time_min = "disturb_time_start_min";
    private String end_time_hour = "disturb_time_end_hour";
    private String end_time_min = "disturb_time_end_min";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disturb_withtime);
        initView();
    }

    private void initView() {
        all_open = (TextView) findViewById(R.id.all_open);
        all_close = (TextView) findViewById(R.id.all_close);
        alarm_item_swbutton = findViewById(R.id.alarm_item_swbutton);
        tv_start_time = findViewById(R.id.tv_start_time);
        tv_end_time = findViewById(R.id.tv_end_time);

        all_open.setOnClickListener(this);
        all_close.setOnClickListener(this);
        tv_start_time.setOnClickListener(this);
        tv_end_time.setOnClickListener(this);
        alarm_item_swbutton.setOnCheckChangedListener(this);

        sharedPreferences = getSharedPreferences(ALARM_CONFIG, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isAllOpen = sharedPreferences.getBoolean(disturb_open, false);

        startHour = sharedPreferences.getInt(start_time_hour, 22);
        startMin = sharedPreferences.getInt(start_time_min, 0);
        endHour = sharedPreferences.getInt(end_time_hour, 6);
        endMin = sharedPreferences.getInt(end_time_min, 0);

        alarm_item_swbutton.setStatus(isAllOpen);


        tv_start_time.setText(getTimeString(startHour, startMin));
        tv_end_time.setText(getTimeString(endHour, endMin));
    }

    @Override
    public void onClick(View view) {
        MainService mainService = MainService.getInstance(this);
        CmdController mCmdController = (CmdController) mainService.getCurrentController();
        switch (view.getId()) {
            case R.id.all_open:
                mCmdController.setDisturb(true, 21, 0, 8, 0);
                break;
            case R.id.all_close:
                mCmdController.setDisturb(false, 0, 0, 0, 0);
                break;
            case R.id.alarm_set_save:
                if (alarm_item_swbutton.getStatus()) {
                    if (isConnected()) {
                        save();
                    }
                    mCmdController.setDisturb(true, startHour, startMin, endHour, endMin);
                } else {
                    mCmdController.setDisturb(false, 0, 0, 0, 0);
                    if (isConnected()) {
                        editor.putBoolean(disturb_open, false).commit();
                    }

                }
                break;
            case R.id.tv_start_time:
                if (alarm_item_swbutton.getStatus())
                    setDialog(startHour, startMin, requsetStartCode);
                break;
            case R.id.tv_end_time:
                if (alarm_item_swbutton.getStatus())
                    setDialog(endHour, endMin, requestEndCode);
                break;
            case R.id.linear_back:
                finish();
                break;
        }
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        if (!isOpened) {
            MainService mainService = MainService.getInstance(this);
            CmdController mCmdController = (CmdController) mainService.getCurrentController();

        } else {

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
            editor.putBoolean(disturb_open, true).commit();
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
