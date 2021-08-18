package com.isport.tracker.main.settings.sport;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.AlarmEntry;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetTime;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.DialogHelper;
import com.isport.tracker.util.Utils;
import com.isport.tracker.view.EasySwitchButton;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/30.
 */
public class AlarmItemActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton.OnOpenedListener {
    private static final String TAG = AlarmItemActivity.class.getSimpleName();
    private TextView mTvTitle;
    private TextView mTvTime;
    private EditText mEdName;
    private Button mBtnSet;
    private EasySwitchButton mSwButton;
    private String name;
    private byte repeat;
    private TextView[] weekdays = new TextView[7];
    private int hour;
    private int min;
    private int index;
    private AlarmEntry alarmEntry;
    private ArrayList<AlarmEntry> listEnrty;

    private String[] weeks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm_item);

        weeks = getResources().getStringArray(R.array.week_small);
        findViewById(R.id.linear_back).setOnClickListener(this);
        findViewById(R.id.alarm_item_rela_two).setOnClickListener(this);
        mEdName = (EditText) findViewById(R.id.alarm_itemt_edt);
        mTvTitle = (TextView) findViewById(R.id.alarm_item_tv_title);
        mTvTime = (TextView) findViewById(R.id.alarm_item_tv_time);
        mSwButton = (EasySwitchButton) findViewById(R.id.alarm_item_swbutton);
        mSwButton.setStatus(true);
        mSwButton.setEnabled(false);
        mSwButton.setVisibility(View.GONE);

        Utils.setEditTextInhibitInputChinese(mEdName);
        mBtnSet = (Button) findViewById(R.id.alarm_item_btn_set);
        mBtnSet.setSelected(true);
        mBtnSet.setOnClickListener(this);
        int[] ids = {R.id.alarm_item_tv_sun, R.id.alarm_item_tv_mon, R.id.alarm_item_tv_tue, R.id.alarm_item_tv_wed,
                R.id.alarm_item_tv_thu, R.id.alarm_item_tv_fri, R.id.alarm_item_tv_sat};

        Intent intent = getIntent();
        index = intent.getIntExtra("index", 0);
        listEnrty = intent.getParcelableArrayListExtra("list");
        if (listEnrty == null) {
            finish();
            return;
        }
        alarmEntry = listEnrty.get(index);
        name = alarmEntry.getDescription();
        mEdName.setText(name);
        mEdName.setSelection(mEdName.getText().length());
        repeat = alarmEntry.getRepeat();

//        mSwButton.setStatus(alarmEntry.isOn());
        mSwButton.setOnCheckChangedListener(this);
        boolean[] bs = getRepeatBytes(repeat);
        for (int i = 0; i < weekdays.length; i++) {
            weekdays[i] = (TextView) findViewById(ids[i]);
            weekdays[i].setSelected(bs[i]);
            weekdays[i].setOnClickListener(this);
        }
        mTvTitle.setText(name);
        hour = alarmEntry.getStartHour();
        min = alarmEntry.getStartMin();

        mTvTime.setText(getTimeString(hour, min));

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (progressDialog != null && progressDialog.isShowing()) {
            DialogHelper.dismissDialog(progressDialog);
        }
    }

    public void onEventMainThread(Message msg) {
        MainService mainService = MainService.getInstance(this);
        BaseDevice baseDevice = null;
        String dname;
        String tpName = null;
        if (mainService != null) {
            baseDevice = mainService.getCurrentDevice();
            dname = baseDevice.getName();
            tpName = (dname == null ? "" : dname.contains("_") ? dname.split("_")[0] : dname.contains("-") ?
                    dname.split("-")[0] : dname.split(" ")[0]).toLowerCase();
        }
        switch (msg.what) {
            case 0x03:
//                Log.e(TAG,"getDeviceType***"+baseDevice.getDeviceType()+"***baseDevice**"+(mainService
// .getCurrentController() instanceof CmdController)+"***"+(mainService.getCurrentController() instanceof
// CmdNmcController));
                if (baseDevice.getDeviceType() == BaseDevice.TYPE_W311N || baseDevice.getDeviceType() == BaseDevice
                        .TYPE_W311T || baseDevice.getDeviceType() == BaseDevice.TYPE_AT200 || baseDevice
                        .getDeviceType() == BaseDevice.TYPE_AT100 || baseDevice
                        .getDeviceType() == BaseDevice.TYPE_AS97 || baseDevice
                        .getDeviceType() == BaseDevice.TYPE_SAS80 || baseDevice.getDeviceType() == BaseDevice
                        .TYPE_W307H || baseDevice.getDeviceType() == BaseDevice.TYPE_W301H) {
                    Log.e(TAG, "设置标志-开始");
                    //进入页面存储上次的描述，如果描述没有改变那么不发指令，如果描述有改变则发指令
//                    if (name.equals(mEdName.getText().toString().trim())){
//                        setSuccess();
//                    }else{
                    mainService.setAlarmDescription(mEdName.getText().toString().trim(), index, true);
//                    }
                } else if (baseDevice.getName().contains("REFLEX")) {
                    mainService.setAlarmDescription(mEdName.getText().toString().trim(), index, true);
                } else if ("rush".equalsIgnoreCase(tpName)) {
                    mainService.setAlarmDescription(mEdName.getText().toString().trim(), index, true);
                } else {
                    setSuccess();
                }
                break;
            case 0x04:
                Log.e(TAG, "设置标志-结束");
                setSuccess();
                break;
        }
    }

    private void setSuccess() {
        if (progressDialog != null && progressDialog.isShowing()) {
            DialogHelper.dismissDialog(progressDialog);
        }
        Toast.makeText(this, getString(R.string.alarm_clock_set_successfully), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("list", listEnrty);
        setResult(RESULT_OK, intent);
        finish();
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

    private boolean is24Hour() {
        return android.text.format.DateFormat.is24HourFormat(this);
    }

    public String getTimeString(int hour, int minute) {
        boolean isAm = (hour < 12);
        int tphour = (is24Hour() ? hour : (hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour)));
        return String.format("%02d", tphour) + ":" + String.format("%02d", minute) + (((!is24Hour()) ? (isAm ?
                getString(R.string.AM) : getString(R.string.PM)) : ""));
    }


    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();
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

    private Dialog progressDialog;

    private void store() {
        if (isConnected()) {
            String trim = mEdName.getText().toString().trim();
//            if (trim.length()!=0){
            alarmEntry.setOn(mSwButton.getStatus());
            alarmEntry.setRepeat(repeatMode());
            alarmEntry.setStartHour(hour);
            alarmEntry.setStartMin(min);
            alarmEntry.setDescription(mEdName.getText().toString().trim());
            listEnrty.remove(index);
            listEnrty.add(index, alarmEntry);
            MainService mainService = MainService.getInstance(this);
            mainService.setAlarm(listEnrty);
            progressDialog = DialogHelper.showProgressDialog(this, getString(R.string.setting));
            if (mainService.getCurrentDevice() != null && mainService.getCurrentDevice().getDeviceType() ==
                    BaseDevice.TYPE_W337B) {
                Intent intent = new Intent();
                intent.putExtra("list", listEnrty);
                setResult(RESULT_OK, intent);
                finish();
            }
//            }
        }
    }


    public byte repeatMode() {
        int tp = 0;
        int mp = 1;
        for (byte i = 0; i < weekdays.length; i++) {
            if (weekdays[i].isSelected()) {
                tp = tp + (int) Math.pow(2, i);
            }
        }
        return (byte) tp;
    }


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

            mTvTime.setText(getTimeString(hour, min));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.linear_back:
                finish();
                break;
            case R.id.alarm_item_rela_two:
                setDialog(hour, min, 101);
                break;
            case R.id.alarm_item_tv_save:
                store();
                break;
            case R.id.alarm_item_btn_set:
                store();
                break;
            case R.id.alarm_item_tv_sun:
                weekdays[0].setSelected(!weekdays[0].isSelected());
                break;
            case R.id.alarm_item_tv_mon:
                weekdays[1].setSelected(!weekdays[1].isSelected());
                break;
            case R.id.alarm_item_tv_tue:
                weekdays[2].setSelected(!weekdays[2].isSelected());
                break;
            case R.id.alarm_item_tv_wed:
                weekdays[3].setSelected(!weekdays[3].isSelected());
                break;
            case R.id.alarm_item_tv_thu:
                weekdays[4].setSelected(!weekdays[4].isSelected());
                break;
            case R.id.alarm_item_tv_fri:
                weekdays[5].setSelected(!weekdays[5].isSelected());
                break;
            case R.id.alarm_item_tv_sat:
                weekdays[6].setSelected(!weekdays[6].isSelected());
                break;
        }
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        alarmEntry.setOn(isOpened);
    }
}
