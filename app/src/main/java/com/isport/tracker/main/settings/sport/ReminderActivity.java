package com.isport.tracker.main.settings.sport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.SedentaryRemind;
import com.isport.isportlibrary.tools.Constants;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetTargetActivity;
import com.isport.tracker.dialogActivity.DialogSetTime;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.view.EasySwitchButton;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin �˶����ѽ���
 */
public class ReminderActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = ReminderActivity.class.getSimpleName();
    private String REMIND_CONFIG = "sedentary_remind_config";
    private String CONFIG_IS_ON = "config_is_on";
    private String CONFIG_BEGIN_TIME = "config_begin_time";
    private String CONFIG_END_TIME = "config_end_time";
    private String CONFIG_NO_EXCISE = "config_no_excise";

    private Button button;
    private EasySwitchButton tvSwitch;
    private TextView tvNoExcixe;
    private TextView tvBegintTime;
    private TextView tvEndTime;
    private SedentaryRemind sedentaryRemind;
    private int beginHour, beginMinue, endHour, endMin, noExciseTime;
    private boolean isOn;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_reminder);

        button = (Button) findViewById(R.id.reminder_save);
        button.setSelected(true);
        tvSwitch = (EasySwitchButton) findViewById(R.id.reminder_switch);
        tvNoExcixe = (TextView) findViewById(R.id.reminder_net_tv);
        tvBegintTime = (TextView) findViewById(R.id.reminder_starttime_tv);
        tvEndTime = (TextView) findViewById(R.id.reminder_endtime_tv);
        tvSwitch.setOnCheckChangedListener(new EasySwitchButton.OnOpenedListener() {
            @Override
            public void onChecked(View v, boolean isOpened) {
                isOn = isOpened;
                switchState(isOn);
            }
        });
        initValue();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_QUERY_SEDENTARY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            //mainService.querySedentaryInfo();
        }
    }

    private void initValue() {
        sharedPreferences = getSharedPreferences(REMIND_CONFIG, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isOn = sharedPreferences.getBoolean(CONFIG_IS_ON, false);
        String[] bs = sharedPreferences.getString(CONFIG_BEGIN_TIME, "08:00").split(":");
        beginHour = Integer.valueOf(bs[0]);
        beginMinue = Integer.valueOf(bs[1]);
        String[] es = sharedPreferences.getString(CONFIG_END_TIME, "18:00").split(":");
        endHour = Integer.valueOf(es[0]);
        endMin = Integer.valueOf(es[1]);
        noExciseTime = sharedPreferences.getInt(CONFIG_NO_EXCISE, 15);
        Log.e(TAG, "***noExciseTime***" + noExciseTime);
        sedentaryRemind = new SedentaryRemind(isOn, beginHour, beginMinue, endHour, endMin);
        SedentaryRemind.noExerceseTime = noExciseTime;
        updateUI();

    }

    private void updateUI() {
        tvSwitch.setStatus(isOn);
        tvEndTime.setText(getTimeString(endHour, endMin));
        tvBegintTime.setText(getTimeString(beginHour, beginMinue));
        tvNoExcixe.setText(String.format("%02d", noExciseTime) + getString(R.string.minute));
        Log.e(TAG, "***noExciseTime ***" + noExciseTime);
        switchState(isOn);
    }

    private boolean is24Hour() {
        return android.text.format.DateFormat.is24HourFormat(this);
    }

    public String getTimeString(int hour, int minute) {
        boolean isAm = (hour < 12);
        hour = (is24Hour() ? hour : (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)));
        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + (((!is24Hour()) ? (isAm ? getString(R.string.AM) : getString(R.string.PM)) : ""));
    }

    private void switchState(boolean isEnable) {
        isOn = isEnable;
        tvSwitch.setStatus(isEnable);
        tvBegintTime.setEnabled(isEnable);
        tvEndTime.setEnabled(isEnable);
        tvNoExcixe.setEnabled(isEnable);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.reminder_back:
                finish();
                break;
            case R.id.reminder_starttime:
                intent = new Intent(this, DialogSetTime.class);
                intent.putExtra(DialogSetTime.EXTRA_FORMAT, "HH:mm");
                intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", beginHour) + ":" + String.format("%02d", beginMinue));
                intent.putExtra(DialogSetTime.EXTRA_IS_24_HOUR, is24Hour());
                intent.putExtra(DialogSetTime.EXTRA_IS_AM, beginHour < 12);
                startActivityForResult(intent, 101);
                break;
            case R.id.reminder_endtime:
                intent = new Intent(this, DialogSetTime.class);
                intent.putExtra(DialogSetTime.EXTRA_FORMAT, "HH:mm");
                intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", endHour) + ":" + String.format("%02d", endMin));
                intent.putExtra(DialogSetTime.EXTRA_IS_24_HOUR, is24Hour());
                intent.putExtra(DialogSetTime.EXTRA_IS_AM, endHour < 12);
                startActivityForResult(intent, 102);
                break;
            case R.id.reminder_net:
                intent = new Intent(this, DialogSetTargetActivity.class);
                intent.putExtra(DialogSetTargetActivity.EXTRA_TYPE, DialogSetTargetActivity.TYPE_SLEEP_REMINDER);
                intent.putExtra(DialogSetTargetActivity.TYPE_SLEEP_REMINDER, noExciseTime);
                startActivityForResult(intent, 103);
                break;

            case R.id.reminder_save:
                if (isConnected()) {
                    SedentaryRemind remind = new SedentaryRemind(isOn, beginHour, beginMinue, endHour, endMin);
                    SedentaryRemind.noExerceseTime = noExciseTime;
                    editor.putString(CONFIG_BEGIN_TIME, beginHour + ":" + beginMinue).commit();
                    editor.putString(CONFIG_END_TIME, endHour + ":" + endMin).commit();
                    editor.putBoolean(CONFIG_IS_ON, isOn).commit();
                    editor.putInt(CONFIG_NO_EXCISE, noExciseTime).commit();
                    List<SedentaryRemind> list = new ArrayList<>();
                    list.add(remind);
                    MainService.getInstance(this).setSedentaryRemind(list);
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            beginHour = hour;
            beginMinue = min;
        } else if (data != null && requestCode == 102) {
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
            endHour = hour;
            endMin = min;
        } else if (data != null && requestCode == 103) {
            noExciseTime = Integer.valueOf(data.getStringExtra(DialogSetTargetActivity.TYPE_SLEEP_REMINDER));
        }
        updateUI();
        if (resultCode == RESULT_OK) {

        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_QUERY_SEDENTARY)) {
                /*ArrayList<SedentaryRemind> list = intent.getParcelableArrayListExtra(Constants.EXTRA_QUERY_SEDENTARY);
                if(list.size()>0) {
                    SedentaryRemind sedentaryRemind = list.get(0);
                    isOn = sedentaryRemind.isOn();
                    if(isOn) {
                        beginHour = sedentaryRemind.getBeginHour();
                        beginMinue = sedentaryRemind.getBeginMin();
                        endHour = sedentaryRemind.getEndHour();
                        endMin = sedentaryRemind.getEndMin();
                        noExciseTime = SedentaryRemind.noExerceseTime;
                        updateUI();
                    }
                }*/
            }
        }
    };
}
