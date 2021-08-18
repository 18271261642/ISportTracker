package com.isport.tracker.main.settings.sport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.AutoSleep;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.tools.Constants;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetHeight;
import com.isport.tracker.dialogActivity.DialogSetTargetActivity;
import com.isport.tracker.dialogActivity.DialogSetTime;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.view.EasySwitchButton;
import com.ypy.eventbus.EventBus;

public class SleepActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton.OnOpenedListener {

    public static String CONFIG_SLEEP_PATH = "config_sleep_path";
    public static String CONFIG_SLEEP_TARGET_HOUR = "config_sleep_target_hour";
    public static String CONFIG_SLEEP_TARGET_MIN = "config_sleep_target_min";
    public static String CONFIG_IS_AUTOSLEEP = "config_is_auto_sleep";
    public static String CONFIG_SLEEP_START = "config_sleep_start";
    public static String CONFIG_SLEEP_END = "config_sleep_end";
    public static String CONFIG_IS_SLEEP = "config_is_sleep";
    public static String CONFIG_SLEEP_REMINDER = "config_sleep_reminder";
    public static String CONFIG_IS_SLEEP_REMIND = "config_is_sleep_remind";
    public static String CONFIG_NAP_START = "config_nap_start";
    public static String CONFIG_NAP_END = "config_nap_end";
    public static String CONFIG_IS_NAP = "config_is_nap";
    public static String CONFIG_NAP_REMINDER = "config_nap_reminder";
    public static String CONFIG_IS_NAP_REMIND = "config_is_nap_remind";

    boolean isAutoSleep, isSleep, isSleepRemind, isNap, isNapRemind;
    int sleepStartHour, sleepStartMin, sleepEndHour, sleepEndMin;
    int napStartHour, napStartMin, napEndHour, napEndMin;
    int sleepRemind, napRemind;
    int sleepTargetHour, sleepTargetMin;

    private EasySwitchButton esbIsAutoSleep, esbIsSleep, esbIsSleepRemind, esbIsNap, esbIsNapRemind;
    private TextView tvSleepStart, tvSleepEnd, tvNapStart, tvNapEnd, tvSleepRemind, tvNapRemind, tvSleepTarget;


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ScrollView scrollView;
    private View napView, napRemindView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_sleep);
        sharedPreferences = getSharedPreferences(CONFIG_SLEEP_PATH, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initControl();
        initValue();

        updateUI();

        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter(Constants.ACTION_QUERY_SLEEP);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            //mainService.querySleepInfo();
        }
        if (mainService != null && mainService.getCurrentDevice() != null) {
            BaseDevice tpd = mainService.getCurrentDevice();
            if (tpd != null && !(tpd.getDeviceType() == BaseDevice.TYPE_W337B || tpd.getDeviceType() == BaseDevice
                    .TYPE_W311N ||
                    tpd.getDeviceType() == BaseDevice.TYPE_AT200 ||
                    tpd.getDeviceType() == BaseDevice.TYPE_AT100 || tpd.getDeviceType() == BaseDevice.TYPE_AS97 ||
                    tpd.getDeviceType() == BaseDevice.TYPE_W311T || tpd.getName().contains("AS87"))) {
                napView.setVisibility(View.GONE);
                napRemindView.setVisibility(View.GONE);
                isNap = false;
                isNapRemind = false;
            }
        }
    }

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case 0x08:
                Toast.makeText(this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void updateUI() {

        scrollView.setVisibility(!isAutoSleep ? View.GONE : View.VISIBLE);


        esbIsSleep.setEnabled(isAutoSleep);
        esbIsNapRemind.setEnabled(isAutoSleep);
        esbIsNap.setEnabled(isAutoSleep);
        esbIsSleepRemind.setEnabled(isAutoSleep);
        tvSleepTarget.setEnabled(isAutoSleep);

        tvSleepRemind.setEnabled(isAutoSleep);
        tvSleepStart.setEnabled(isAutoSleep);
        tvSleepEnd.setEnabled(isAutoSleep);
        tvNapStart.setEnabled(isAutoSleep);
        tvNapEnd.setEnabled(isAutoSleep);
        tvNapRemind.setEnabled(isAutoSleep);
        esbIsAutoSleep.setStatus(isAutoSleep);

        if (isAutoSleep) {
            isSleepRemind = isSleep && isSleepRemind;
            tvSleepEnd.setEnabled(isSleep);
            tvSleepStart.setEnabled(isSleep);
            tvSleepTarget.setEnabled(!isSleep);
            esbIsSleepRemind.setStatus(isSleepRemind);
            tvSleepRemind.setEnabled(isSleepRemind);
            esbIsSleep.setStatus(isSleep);
            esbIsSleepRemind.setEnabled(isSleep);

            isNapRemind = isNap && isNapRemind;
            tvNapEnd.setEnabled(isNap);
            tvNapStart.setEnabled(isNap);
            esbIsNapRemind.setStatus(isNapRemind);
            tvNapRemind.setEnabled(isNapRemind);
            esbIsNap.setStatus(isNap);
            esbIsNapRemind.setEnabled(isNap);
        }

        tvSleepStart.setText(getTimeString(sleepStartHour, sleepStartMin));
        tvSleepEnd.setText(getTimeString(sleepEndHour, sleepEndMin));
        tvNapStart.setText(getTimeString(napStartHour, napStartMin));
        tvNapEnd.setText(getTimeString(napEndHour, napEndMin));
        tvNapRemind.setText(napRemind + getString(R.string.minute));
        tvSleepRemind.setText(sleepRemind + getString(R.string.minute));
        String hour = sleepTargetHour < 10 ? ("0" + sleepTargetHour) : ("" + sleepTargetHour);
        String min = Math.round(sleepTargetMin % 60) < 10 ? ("0" + Math.round(sleepTargetMin % 60)) : Math.round(sleepTargetMin % 60) + "";
        tvSleepTarget.setText(hour + " : " + min);


    }

    private boolean is24Hour() {
        return android.text.format.DateFormat.is24HourFormat(this);
    }

    public String getTimeString(int hour, int minute) {
        boolean isAm = (hour < 12);
        hour = (is24Hour() ? hour : (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)));
        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + (((!is24Hour()) ?
                (isAm ? getString(R.string.AM) : getString(R.string.PM)) : ""));
    }

    private void initControl() {
        esbIsAutoSleep = (EasySwitchButton) findViewById(R.id.switch_sleep);
        esbIsSleep = (EasySwitchButton) findViewById(R.id.switch_sleep_time);
        esbIsSleepRemind = (EasySwitchButton) findViewById(R.id.switch_reminder);
        esbIsNap = (EasySwitchButton) findViewById(R.id.switch_lunch);
        esbIsNapRemind = (EasySwitchButton) findViewById(R.id.switch_lunch_reminder);
        scrollView = (ScrollView) findViewById(R.id.scrollView1);

        esbIsAutoSleep.setOnCheckChangedListener(this);
        esbIsSleep.setOnCheckChangedListener(this);
        esbIsSleepRemind.setOnCheckChangedListener(this);
        esbIsNap.setOnCheckChangedListener(this);
        esbIsNapRemind.setOnCheckChangedListener(this);

        tvSleepStart = (TextView) findViewById(R.id.text_sleep_time_begin);
        tvSleepEnd = (TextView) findViewById(R.id.text_sleep_time_end);
        tvSleepRemind = (TextView) findViewById(R.id.text_reminder);
        tvNapStart = (TextView) findViewById(R.id.text_lunch_begin);
        tvNapEnd = (TextView) findViewById(R.id.text_lunch_end);
        tvNapRemind = (TextView) findViewById(R.id.tv_luncher_reminder);

        tvSleepTarget = (TextView) findViewById(R.id.text_target);

        napView = findViewById(R.id.sleep_nap_linear);
        napRemindView = findViewById(R.id.lly_nap_remind);

    }

    private void initValue() {
        isAutoSleep = sharedPreferences.getBoolean(CONFIG_IS_AUTOSLEEP, false);
        isSleep = sharedPreferences.getBoolean(CONFIG_IS_SLEEP, false);
        isSleepRemind = sharedPreferences.getBoolean(CONFIG_IS_SLEEP_REMIND, false);
        isNap = sharedPreferences.getBoolean(CONFIG_IS_NAP, false);
        isNapRemind = sharedPreferences.getBoolean(CONFIG_IS_NAP_REMIND, false);
        if (!isAutoSleep) {
            scrollView.setVisibility(View.GONE);
        }

        String[] ss = sharedPreferences.getString(CONFIG_SLEEP_START, "22:00").split(":");
        sleepStartHour = Integer.valueOf(ss[0]);
        sleepStartMin = Integer.valueOf(ss[1]);
        ss = sharedPreferences.getString(CONFIG_SLEEP_END, "06:00").split(":");
        sleepEndHour = Integer.valueOf(ss[0]);
        sleepEndMin = Integer.valueOf(ss[1]);
        ss = sharedPreferences.getString(CONFIG_NAP_START, "13:00").split(":");
        napStartHour = Integer.valueOf(ss[0]);
        napStartMin = Integer.valueOf(ss[1]);
        ss = sharedPreferences.getString(CONFIG_NAP_END, "14:00").split(":");
        napEndHour = Integer.valueOf(ss[0]);
        napEndMin = Integer.valueOf(ss[1]);

        sleepRemind = sharedPreferences.getInt(CONFIG_SLEEP_REMINDER, 15);
        napRemind = sharedPreferences.getInt(CONFIG_NAP_REMINDER, 15);
        sleepTargetHour = sharedPreferences.getInt(CONFIG_SLEEP_TARGET_HOUR, 8);
        sleepTargetMin = sharedPreferences.getInt(CONFIG_SLEEP_TARGET_MIN, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 102) {
            String v = (data.getStringExtra(DialogSetHeight.EXTRA_SLEEP_TARGET));
            String[] strs = v.split(":");
            sleepTargetHour = Integer.valueOf(strs[0].trim());
            sleepTargetMin = Integer.valueOf(strs[1].trim());
        } else if (data != null && requestCode == 103) {
            napRemind = Integer.valueOf(data.getStringExtra(DialogSetTargetActivity.TYPE_LAUNCHER_REMINDER));
        } else if (data != null && requestCode == 104) {
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
            napStartHour = hour;
            napStartMin = min;
        } else if (data != null && requestCode == 105) {
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
            napEndHour = hour;
            napEndMin = min;
        } else if (data != null && requestCode == 106) {
            sleepRemind = Integer.valueOf(data.getStringExtra(DialogSetTargetActivity.TYPE_SLEEP_REMINDER));
        } else if (data != null && requestCode == 107) {
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
            sleepStartHour = hour;
            sleepStartMin = min;
        } else if (data != null && requestCode == 108) {
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
            sleepEndHour = hour;
            sleepEndMin = min;
        }
        updateUI();
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.return_back:
                finish();
                break;
            case R.id.text_target:
                intent = new Intent(this, DialogSetHeight.class);
                intent.putExtra(DialogSetHeight.EXTRA_TYPE, DialogSetHeight.TYPE_SLEEP_TARGET);
                intent.putExtra(CONFIG_SLEEP_TARGET_HOUR, sleepTargetHour);
                intent.putExtra(CONFIG_SLEEP_TARGET_MIN, sleepTargetMin);
                startActivityForResult(intent, 102);
                break;
            case R.id.tv_luncher_reminder:
                intent = new Intent(this, DialogSetTargetActivity.class);
                intent.putExtra(DialogSetTargetActivity.EXTRA_TYPE, DialogSetTargetActivity.TYPE_LAUNCHER_REMINDER);
                intent.putExtra(DialogSetTargetActivity.TYPE_LAUNCHER_REMINDER, napRemind);
                startActivityForResult(intent, 103);
                break;
            case R.id.text_lunch_begin:
                startActivityWithRequest(napStartHour, napStartMin, 104);
                break;
            case R.id.text_lunch_end:
                startActivityWithRequest(napEndHour, napEndMin, 105);
                break;
            case R.id.text_reminder:
                intent = new Intent(this, DialogSetTargetActivity.class);
                intent.putExtra(DialogSetTargetActivity.EXTRA_TYPE, DialogSetTargetActivity.TYPE_SLEEP_REMINDER);
                intent.putExtra(DialogSetTargetActivity.TYPE_SLEEP_REMINDER, sleepRemind);
                startActivityForResult(intent, 106);
                break;
            case R.id.text_sleep_time_begin:
                startActivityWithRequest(sleepStartHour, sleepStartMin, 107);
                break;
            case R.id.text_sleep_time_end:
                startActivityWithRequest(sleepEndHour, sleepEndMin, 108);
                break;
            case R.id.text_save:
                save();
                break;
        }
        updateUI();
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

    private void save() {
        if (isConnected()) {
            editor.putInt(CONFIG_SLEEP_TARGET_MIN, sleepTargetMin).commit();
            editor.putInt(CONFIG_SLEEP_TARGET_HOUR, sleepTargetHour).commit();
            editor.putBoolean(CONFIG_IS_AUTOSLEEP, isAutoSleep).commit();
            editor.putString(CONFIG_SLEEP_START, String.format("%02d", sleepStartHour) + ":" + String.format("%02d",
                    sleepStartMin)).commit();
            editor.putString(CONFIG_SLEEP_END, String.format("%02d", sleepEndHour) + ":" + String.format("%02d",
                    sleepEndMin))
                    .commit();
            editor.putBoolean(CONFIG_IS_SLEEP, isSleep).commit();
            editor.putInt(CONFIG_SLEEP_REMINDER, sleepRemind).commit();
            editor.putBoolean(CONFIG_IS_SLEEP_REMIND, isSleepRemind).commit();
            editor.putString(CONFIG_NAP_START, String.format("%02d", napStartHour) + ":" + String.format("%02d",
                    napStartMin))
                    .commit();
            editor.putString(CONFIG_NAP_END, String.format("%02d", napEndHour) + ":" + String.format("%02d",
                    napEndMin))
                    .commit();
            editor.putBoolean(CONFIG_IS_NAP, isNap).commit();
            editor.putBoolean(CONFIG_IS_NAP_REMIND, isNapRemind).commit();
            editor.putInt(CONFIG_NAP_REMINDER, napRemind).commit();
            MainService mainService = MainService.getInstance(this);
            AutoSleep autoSleep = AutoSleep.getInstance(this);
            autoSleep.setAutoSleep(isAutoSleep);

            autoSleep.setNaoStartMin(napStartMin);
            autoSleep.setNapStartHour(napStartHour);
            autoSleep.setNapEndHour(napEndHour);
            autoSleep.setNapEndMin(napEndMin);
            autoSleep.setNap(isNap);
            autoSleep.setNapRemind(isNapRemind);
            autoSleep.setNapRemindTime(napRemind);

            autoSleep.setSleep(isSleep);
            autoSleep.setSleepStartHour(sleepStartHour);
            autoSleep.setSleepStartMin(sleepStartMin);
            autoSleep.setSleepEndHour(sleepEndHour);
            autoSleep.setSleepEndMin(sleepEndMin);
            autoSleep.setSleepRemind(isSleepRemind);
            autoSleep.setSleepRemindTime(sleepRemind);
            autoSleep.setSleepTargetHour(sleepTargetHour);
            autoSleep.setSleepTargetMin(sleepTargetMin);


            if (mainService != null && mainService.getCurrentDevice() != null) {
                BaseDevice tpd = mainService.getCurrentDevice();
                if (tpd != null && !(tpd.getDeviceType() == BaseDevice.TYPE_W307H || tpd.getDeviceType() ==
                        BaseDevice.TYPE_W301H || tpd.getDeviceType() == BaseDevice.TYPE_W337B || tpd.getDeviceType() == BaseDevice.TYPE_W311N ||
                        tpd.getDeviceType() == BaseDevice.TYPE_AT200 ||
                        tpd.getDeviceType() == BaseDevice.TYPE_AT100 || tpd.getDeviceType() == BaseDevice.TYPE_AS97 ||
                        tpd.getDeviceType() == BaseDevice.TYPE_W311T || tpd.getName().contains("AS87"))) {
                    napView.setVisibility(View.GONE);
                    napRemindView.setVisibility(View.GONE);
                    isNap = false;
                    isNapRemind = false;
                    autoSleep.setNap(false);
                    autoSleep.setNapRemind(false);
                }
            }
            mainService.setAutoSleep(autoSleep);
        }
    }

    public void startActivityWithRequest(int hour, int min, int requestCode) {
        Intent intent = new Intent(this, DialogSetTime.class);
        intent.putExtra(DialogSetTime.EXTRA_FORMAT, "HH:mm");
        intent.putExtra(DialogSetTime.EXTRA_DATE, String.format("%02d", hour) + ":" + String.format("%02d", min));
        intent.putExtra(DialogSetTime.EXTRA_IS_24_HOUR, is24Hour());
        intent.putExtra(DialogSetTime.EXTRA_IS_AM, hour < 12);
        startActivityForResult(intent, requestCode);
    }


    @Override
    public void onChecked(View v, boolean isOpened) {
        switch (v.getId()) {
            case R.id.switch_sleep:
                isAutoSleep = isOpened;
                break;
            case R.id.switch_sleep_time:
                isSleep = isOpened;
                break;
            case R.id.switch_reminder:
                isSleepRemind = isOpened;
                break;
            case R.id.switch_lunch:
                isNap = isOpened;
                break;
            case R.id.switch_lunch_reminder:
                isNapRemind = isOpened;
                break;
        }
        updateUI();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_QUERY_SLEEP)) {
				/*SleepEntry sleepEntry = intent.getExtras().getParcelable(Constants.EXTRA_QUERY_SLEEP);
				isAutoSleep = sleepEntry.isAutoSleep();
				isNap = sleepEntry.isNap();
				isSleep = sleepEntry.isSleep();
				isSleepRemind = sleepEntry.isSleepRemind();
				isNapRemind = sleepEntry.isNapRemind();

				if(isNap){
					napStartHour = sleepEntry.getNapStartHour();
					napStartMin = sleepEntry.getNaoStartMin();
					napEndHour = sleepEntry.getNapEndHour();
					napEndMin = sleepEntry.getNapEndMin();

					if(isNapRemind){
						napRemind = sleepEntry.getNapRemindTime();
					}

				}

				if(isSleep){
					sleepStartHour = sleepEntry.getSleepStartHour();
					sleepStartMin = sleepEntry.getSleepStartMin();
					sleepEndHour = sleepEntry.getSleepEndHour();
					sleepEndMin = sleepEntry.getSleepEndMin();
					if(isSleepRemind){
						sleepRemind = sleepEntry.getSleepRemindTime();
					}
				}

				updateUI();*/
            }
        }
    };
}