package com.isport.tracker.main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.MyBaseDevice;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.Utils;

import java.util.Timer;


public class BindDeviceActivity extends BaseActivity implements OnClickListener {
    private RelativeLayout re_back;
    private TextView title;
    private Button bindDevice, deleteDevice;
    private int position;
    private ProgressDialog dialog_bounding;
    private String mac;
    private String name;
    private Timer timer_bound_timeout;
    private String mac_bounded;
    private String name_bounded;
    private SharedPreferences share;
    private MyBaseDevice myBaseDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_device_bind);

        initControl();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_CONNECTE_ERROR);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }


    private void initControl() {
        bindDevice = (Button) findViewById(R.id.bind_device_bind);
        deleteDevice = (Button) findViewById(R.id.bind_device_delete);
        title = (TextView) findViewById(R.id.title_name);
        findViewById(R.id.return_back_icon).setOnClickListener(this);

        myBaseDevice = (MyBaseDevice) getIntent().getSerializableExtra("device");
        if (myBaseDevice != null) {
            title.setText(Utils.replaceDeviceNameToCC431(myBaseDevice.getName(), myBaseDevice.getVersion()));
        } else {
            finish();
            return;
        }

        bindDevice.setOnClickListener(this);
        deleteDevice.setOnClickListener(this);

        MainService mainService = MainService.getInstance(this);
        BaseDevice currentDevice = null;
        if (mainService != null) {
            currentDevice = mainService.getCurrentDevice();
        }
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED && currentDevice != null && myBaseDevice.getMac().equals(currentDevice.getMac())) {
            bindDevice.setVisibility(View.GONE);
            deleteDevice.setText(getString(R.string.unbind));
        } else {
            bindDevice.setVisibility(View.VISIBLE);
            deleteDevice.setText(getString(R.string.ignore_equipment));
        }
    }

    private void showProgressDialog() {

        //if(dialog_bounding == null) {
        dialog_bounding = new ProgressDialog(this);
        dialog_bounding.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
        dialog_bounding.setMessage(getString(R.string.connecting));

        dialog_bounding.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                MainService.getInstance(BindDeviceActivity.this).disconnectDevice(myBaseDevice);
                if (handler.hasMessages(0x01))
                    handler.removeMessages(0x01);
            }
        });
        //}
        if (!dialog_bounding.isShowing()) {
            dialog_bounding.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (dialog_bounding != null && dialog_bounding.isShowing()) {
            dialog_bounding.dismiss();
        }
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        if (handler.hasMessages(0x01))
            handler.removeMessages(0x01);
    }


    @Override
    public void onClick(View v) {
        MainService mainService = MainService.getInstance(this);
        switch (v.getId()) {
            case R.id.return_back:
                finish();
                break;
            case R.id.bind_device_bind:
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null || !adapter.isEnabled()) {
                    Toast.makeText(this, getString(R.string.turn_bluetooth_first), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mainService != null && myBaseDevice != null) {
                    mainService.connect(myBaseDevice, true);
                    ConfigHelper.getInstance(this).putString(Constants.INFO_CURRENT_DEVICE, new Gson().toJson(myBaseDevice));
                    showProgressDialog();
                    int dt = myBaseDevice.getDeviceType();
                    if (dt == BaseDevice.TYPE_AT100 || dt == BaseDevice.TYPE_AT200 || dt == BaseDevice.TYPE_SAS80 || dt == BaseDevice.TYPE_AS97) {
                        handler.sendEmptyMessageDelayed(0x01, 60000);
                    } else {
                        handler.sendEmptyMessageDelayed(0x01, 40000);
                    }
                }
                break;
            case R.id.bind_device_delete:
                if (mainService != null) {
                    BaseDevice currentDevice = mainService.getCurrentDevice();
                    if (mainService != null && mainService.getCurrentDevice() != null && myBaseDevice != null && myBaseDevice.getMac().equals(currentDevice.getMac())) {
                        mainService.unBind(mainService.getCurrentDevice());
                        ConfigHelper.getInstance(this).remove(Constants.INFO_CURRENT_DEVICE);
                        Toast.makeText(this, getString(R.string.Successfully_deleted), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;
        }
    }

    private void dismissDialog() {
        if (dialog_bounding != null && dialog_bounding.isShowing()) {
            dialog_bounding.dismiss();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    MainService.getInstance(BindDeviceActivity.this).disconnectDevice(myBaseDevice);
                    dismissDialog();
                    if (handler.hasMessages(0x01))
                        handler.removeMessages(0x01);
                    break;

            }
        }
    };

    private final byte[] viberByte = new byte[]{(byte) 0xBE,0x06,0x0F, (byte) 0xED};

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;
            if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
                // if(handler.hasMessages(0x01))
                // handler.removeMessages(0x01);

                int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if (state == BaseController.STATE_CONNECTED) {

                    if (handler.hasMessages(0x01)) {
                        handler.removeMessages(0x01);
                    }
                    dismissDialog();
                    Intent intent1 = null;

                    MainService mainService = MainService.getInstance(BindDeviceActivity.this);
                    if(mainService!=null){
                        mainService.sendCustomCmd(viberByte);
                    }

                    if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
                        //intent1 = new Intent(BindDeviceActivity.this, com.isport.fitness.activity.MainActivityGroup.class);
                    } else {
                        intent1 = new Intent(BindDeviceActivity.this, MainActivityGroup.class);
                    }
                    startActivity(intent1);
                    finish();
                } else if (state == BaseController.STATE_DISCONNECTED) {
                    if (handler.hasMessages(0x01)) {
                        handler.removeMessages(0x01);
                    }
                    dismissDialog();
                }
            } else if (action.equals(MainService.ACTION_CONNECTE_ERROR)) {
                //if(handler.hasMessages(0x01))
                //handler.removeMessages(0x01);
                //dismissDialog();
            }
        }
    };
}