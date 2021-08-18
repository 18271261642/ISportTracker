package com.isport.tracker.main.settings.sport;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.view.NumberPickerView;
import com.ypy.eventbus.EventBus;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/11/21.
 */

public class CalibrateActivity extends BaseActivity implements View.OnClickListener, NumberPickerView.OnValueChangeListener {

    private static final String TAG = CalibrateActivity.class.getSimpleName();
    private ProgressDialog progressDialog;
    private NumberPickerView npHour;
    private NumberPickerView npMinute;
    private NumberPickerView npSeconds;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_calibrate);
        Calendar calendar = Calendar.getInstance();
        progressDialog = new ProgressDialog(this);

        npHour = (NumberPickerView) findViewById(R.id.alarm_hour);
        npMinute = (NumberPickerView) findViewById(R.id.alarm_min);
        npSeconds = (NumberPickerView) findViewById(R.id.alarm_sec);


        String[] strH = new String[24];
        for (int i = 0; i < strH.length; i++) {
            strH[i] = i + "";
        }
        npHour.setDisplayedValues(strH);
        strH = new String[60];
        for (int i = 0; i < strH.length; i++) {
            strH[i] = i + "";
        }
        npMinute.setDisplayedValues(strH);
        strH = new String[60];
        for (int i = 0; i < strH.length; i++) {
            strH[i] = i + "";
        }
        npSeconds.setDisplayedValues(strH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour = (hour >= 12 ? hour - 12 : hour);
        setData(npHour, 0, 11, hour);
        setData(npMinute, 0, 59, calendar.get(Calendar.MINUTE));
        setData(npSeconds, 0, 59, calendar.get(Calendar.SECOND));
        npHour.setValue(hour);
        npMinute.setValue(calendar.get(Calendar.MINUTE));
        npSeconds.setValue(calendar.get(Calendar.SECOND));

        npHour.setOnValueChangedListener(this);
        npMinute.setOnValueChangedListener(this);
        npMinute.setOnValueChangedListener(this);

        EventBus.getDefault().register(this);
        handler.sendEmptyMessageDelayed(0x02, 60000);

        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            mainService.sendCustomCmd(new byte[]{(byte) 0xbe, 01, 0x0A, (byte) 0xed});
        }else {
            Toast.makeText(CalibrateActivity.this, getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
        }

    }

    private void setData(NumberPickerView picker, int minValue, int maxValue, int value) {
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);
        picker.setValue(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    break;
                case 0x02:
                    finish();
                    break;
                case 0x03:
                    MainService mainService = MainService.getInstance(CalibrateActivity.this);
                    byte[] bs = {(byte) 0xbe, 0x01, 0x10, (byte) 0xfe, (byte) npHour.getValue(), (byte) npMinute.getValue(), (byte) npSeconds.getValue()};
                    if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                        mainService.sendCustomCmd(bs);
                    }else {
                        Toast.makeText(CalibrateActivity.this, getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }
    };

    public void onEventMainThread(String str) {
        Log.e(TAG,"str =="+str);
        if (str.equals("DE 01 10 ED")||str.contains("DE 01 10 ED")) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(this, getString(R.string.setting_seccess), Toast.LENGTH_SHORT).show();
            if (handler.hasMessages(0x01)) {
                handler.removeMessages(0x01);
            }
            if (handler.hasMessages(0x02)) {
                handler.removeMessages(0x02);
            }
            handler.sendEmptyMessageDelayed(0x02, 58000);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_tv:
                finish();
                break;
            case R.id.btn_calibrate_confirm:
                //BE+01+10+FE+指针时(1byte)＋指针分(1byte)＋指针秒(1byte)
                MainService mainService = MainService.getInstance(this);
                byte[] bs = {(byte) 0xbe, 0x01, 0x10, (byte) 0xfe, (byte) npHour.getValue(), (byte) npMinute.getValue(), (byte) npSeconds.getValue()};
                if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                    mainService.sendCustomCmd(bs);
                    progressDialog.setMessage(getString(R.string.calibrating));
                    progressDialog.show();
                    handler.sendEmptyMessageDelayed(0x01, 5000);

                } else {
                    Toast.makeText(CalibrateActivity.this, getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {

    }
}
