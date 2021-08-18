package com.isport.tracker.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.DfuService;
import com.isport.tracker.bluetooth.MainService;
import com.ypy.eventbus.EventBus;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * @创建者 bear
 * @创建时间 2019/3/7 8:26
 * @描述 beat V91.32以上升级不再需要发送OTA指令，直接升级
 */
public class DfuBeatActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "DfuBeatActivity";
    ProgressDialog progressDialog;
    private String mac = "";
    private String name = "";
    public static final String UPLOADING = "uploading";
    public static final String IDIL = "IDIL";
    private String state;
    private BluetoothDevice mBluetoothDevice;
    private int dfu_count;
    private BaseDevice mCurrentDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_dfu_noti);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        // TODO: 2018/3/3 main保存的地址改为app本地raw
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            mac = mainService.getCurrentDevice().getMac();
            name = mainService.getCurrentDevice().getName();
            mCurrentDevice = mainService.getCurrentDevice();
            mainService.setBleDiscByUser(true, mac);
        }
    }

    private Handler setHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x02:
                    scanDfu();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_back:
                finish();
                break;
            case R.id.btn_fresh:
                MainService mainService = MainService.getInstance(DfuBeatActivity.this);
                if (mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
                    state = IDIL;
                    dfu_count = 0;
                    if (mainService != null && mainService.getCurrentDevice() != null) {
                        mac = mainService.getCurrentDevice().getMac();
                        name = mainService.getCurrentDevice().getName();

                        //升级先解绑设备,升级完成了让用户自己去绑定
                        mainService.unBind(mainService.getCurrentDevice());
                        //更新MenuSSetActivity页面
                        EventBus.getDefault().post("Miss");
                        upload(mac, name);
                    }
                    //连接状态直接进入DFU去升级
                } else {
                    Log.e(TAG, "非连接状态再次点击，直接scanDfu");
                    state = IDIL;
                    dfu_count = 0;
                    scanDfu();
                    //UtilTools.showToast(DfuActivity.this, R.string.please_bind);
                }
                break;
        }
    }

    public boolean upload(String mac, String name) {
        state = UPLOADING;
        dfu_count++;
        cancelScan();
        Log.e(TAG, "upload操作展示弹窗 mac == " + mac + " name == " + name);
        progressDialog.setMessage(getString(R.string.ota_updating) + "...");
        progressDialog.setCancelable(false);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        final DfuServiceInitiator starter = new DfuServiceInitiator(mac)
                .setDeviceName(name)
                .setKeepBond(false)//升级后保持连接
                .setForceDfu(false)
                .setPacketsReceiptNotificationsEnabled(true)
                .setPacketsReceiptNotificationsValue(6)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
        //  starter.setZip(R.raw.w307_ref_autosleep_v9104);
        //starter.setZip(R.raw.w307_reflex_ekin_v9090);
        //starter.setZip(R.raw.w307s_reflex_v9089);
//        starter.setZip(R.raw.w307_fastrack_v9091);
        starter.setZip(R.raw.w307k_reflex2c_hc_v909803);
        //starter.setZip(R.raw.w311b_beat_rdn_v9161);
//        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_REFLEX)){
//            starter.setZip(R.raw.w307s_reflexv9046);
//        }else {
////            starter.setBinOrHex(DfuBaseService.TYPE_APPLICATION, R.raw.w311n_hama);
//            starter.setBinOrHex(DfuBaseService.TYPE_APPLICATION, R.raw.w311n_mc);
//        }
        Log.e(TAG, "去start DfuService");
        starter.start(this, DfuService.class);
        return true;
    }

    private String DFU_SERVICE_UUID = "00001530-1212-EFDE-1523-785FEABCD123";

    public void scanDfu() {
        Log.e(TAG, "执行scanDfu操作");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            Toast.makeText(this, R.string.bluetooth_hint, Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.setMessage(getString(R.string.msg_scan));
        progressDialog.show();
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelScan();
            }
        });
        bleAdapter.startLeScan(null, bleScanCallBack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        MainService mainService = MainService.getInstance(this);
        if (mainService != null) {
            MainService.getInstance(this).setBleDiscByUser(false, mac);
        }
    }

    public void cancelScan() {
        BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
        bleAdapter.stopLeScan(bleScanCallBack);
    }

    private Handler loadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    mBluetoothDevice = (BluetoothDevice) msg.obj;
                    upload(mBluetoothDevice.getAddress(), mBluetoothDevice.getName());
                    break;
                case 0x02:
                    scanDfu();
                    break;
                default:
                    break;
            }
        }
    };

    private BluetoothAdapter.LeScanCallback bleScanCallBack = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            String deviceName = device.getName();
            String address = device.getAddress();
            Log.e(TAG, "scan到设备，deviceName == " + deviceName);
//            if (deviceName != null && deviceName.contains("DFU_MDOE")) {
            if (address.equals(mac)) {
                Log.e(TAG, "scan到设备，mac == " + mac + " 并等待4s去执行upload操作" + state);
                if (state.equals(IDIL)) {
                    state = UPLOADING;
                    cancelScan();
                    Message msg = Message.obtain();
                    msg.obj = device;
                    msg.what = 0x01;
                    loadHandler.sendMessageDelayed(msg, 4000);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private void showToast() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setCancelable(false);
        builder1.setMessage(getString(R.string.go_to_pair_mode));
        builder1.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(DfuActivity.this,getString(R.string.go_to_pair_mode),Toast.LENGTH_LONG).show();
                final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(DfuService.NOTIFICATION_ID);
//                MainService mainService = MainService.getInstance(DfuBeatActivity.this);
//                if (mainService != null) {
//                    MainService.getInstance(DfuBeatActivity.this).setBleDiscByUser(false, mac);
//                    mainService.connect(mCurrentDevice, false);
//                }
                //直接到首页
                Intent intent = new Intent(DfuBeatActivity.this, MainActivityGroup.class);
                startActivity(intent);
                finish();
            }
        });
        builder1.create().show();
    }

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            Log.e(TAG, "onDeviceConnecting deviceAddress = " + deviceAddress);
            if (!progressDialog.isShowing()) {
                //progressDialog.show();
            }
            progressDialog.setMessage(getString(R.string.ota_connecting));
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            Log.e(TAG, "onDfuProcessStarting deviceAddress = " + deviceAddress);
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            progressDialog.setMessage(getString(R.string.ota_startsoon));
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            Log.e(TAG, "onEnablingDfuMode deviceAddress = " + deviceAddress);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            Log.e(TAG, "onFirmwareValidating deviceAddress = " + deviceAddress);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            Log.e(TAG, "onDeviceDisconnecting deviceAddress = " + deviceAddress);
//            if(progressDialog != null && progressDialog.isShowing()){
//                progressDialog.dismiss();
//            }
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            ///OTA 升级完成
            state = IDIL;
            dfu_count = -1;
            Log.e(TAG, "OTA 升级完成 = " + deviceAddress);
            progressDialog.setMessage(getString(R.string.ota_updating) + " " + 100 + "%");
            //Toast.makeText(DfuActivity.this, "ota update " + deviceAddress + " completed", Toast.LENGTH_LONG).show();

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            showToast();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                    /*MainService mainService = MainService.getInstance(getApplicationContext());
                    if(mainService != null){
                        mainService.connect(mainService.getCurrentDevice());
                    }*/
                    //finish();

                }
            }, 1000);

        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            Log.e(TAG, "OTA 终止了 = ");
            // TODO: 2018/6/30  终止了，重试
            uploadOnFailure();
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            Log.e(TAG, "OTA 进度变化 = " + percent);
            progressDialog.setMessage(getString(R.string.ota_updating) + " " + percent + "%");
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            Log.e(TAG, "OTA onError error = " + error + " errorType = " + " message = " + message);
            uploadOnFailure();
        }
    };

    private void uploadOnFailure() {
        if (dfu_count == -1) {

        } else {
            if (dfu_count >= 5) {
                Log.e(TAG, "OTA 重试5次都失败 ");
                state = IDIL;
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // if this activity is still open and upload process was completed, cancel the notification
                        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(DfuService.NOTIFICATION_ID);
                    }
                }, 1000);
            } else {
                Log.e(TAG, "OTA 当前重试次数 = " + dfu_count);
                state = IDIL;
                loadHandler.sendEmptyMessageDelayed(0x02, 5000);
            }
        }
    }
}
