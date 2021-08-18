package com.isport.tracker.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.gson.Gson;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.adapter.DeviceBondAdapter;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.HamaDeviceInfo;
import com.isport.tracker.entity.MyBaseDevice;
import com.isport.tracker.main.settings.AboutUsActivity;
import com.isport.tracker.main.settings.ActivityAddAccount;
import com.isport.tracker.main.settings.ActivityDeviceSetting;
import com.isport.tracker.main.settings.LogActivity;
import com.isport.tracker.main.settings.ManageDeviceActivity;
import com.isport.tracker.main.settings.UserInfoActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.Utils;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MenuSetActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = MenuSetActivity.class.getSimpleName();
    private LinearLayout ly_bound, ly_user_info, ly_about, layout_switch_devicetype, layout_device_change, layout_device_version,
            layout_check_update;
    private ImageButton mImgBack;
    private ListView lv_layout;
    private DeviceBondAdapter mAdapter;
    private TextView tvState;
    private View viewState;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int currentType = -1;
    private int currentIndex = -1;
    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        EventBus.getDefault().register(this);
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        tvState = (TextView) findViewById(R.id.menu_tv_connectstate);
        viewState = findViewById(R.id.menu_device_setting);
        layout_switch_devicetype = (LinearLayout) findViewById(R.id.layout_switch_devicetype);
        layout_device_change = (LinearLayout) findViewById(R.id.layout_device_change);
        layout_check_update = (LinearLayout) findViewById(R.id.layout_check_update);
        layout_device_version = (LinearLayout) findViewById(R.id.layout_device_version);
        tvVersion = findViewById(R.id.tv_version);
        IntentFilter filter = new IntentFilter(MainService.ACTION_CONNECTE_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        tvVersion.setText(getVersion() + "");

        if (BuildConfig.DEBUG) {
            layout_device_change.setVisibility(View.GONE);
            layout_device_version.setVisibility(View.GONE);
        } else {
            layout_device_change.setVisibility(View.GONE);
            layout_device_version.setVisibility(View.GONE);
        }
        layout_device_change.setVisibility(View.VISIBLE);
        layout_device_version.setVisibility(View.VISIBLE);
        if (!Constants.IS_FACTORY_VERSION) {
            layout_switch_devicetype.setVisibility(View.GONE);
        }
//        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult
//                .SERVICE_MISSING) {
//            findViewById(R.id.layout_addacount).setVisibility(View.GONE);
//        }

        sharedPreferences = getSharedPreferences(DeviceTypeActivity.CONFIG_DEVICE, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        currentType = sharedPreferences.getInt(DeviceTypeActivity.KEY_DEVICE_TYPE, -1);
        currentIndex = sharedPreferences.getInt(DeviceTypeActivity.KEY_DEVICE_INDEX, -1);
        MainService mainService = MainService.getInstance(this);
        if (mainService != null) {
            List<BaseDevice> listDevice = mainService.getHistoryDevice();
            if (listDevice != null) {

            }
        }
    }

    private float getVersion() {
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        float version = Float.valueOf(deviceInfo.getFirmwareHighVersion() + "." + deviceInfo.getFirmwareLowVersion());
        return version;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    public void updateUI() {
        MyBaseDevice myBaseDevice = null;
        if (!ConfigHelper.getInstance(this).getString(Constants.INFO_CURRENT_DEVICE, "").equals("")) {
            myBaseDevice = new Gson().fromJson(ConfigHelper.getInstance(this).getString(Constants
                            .INFO_CURRENT_DEVICE,
                    ""), MyBaseDevice.class);
        }
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            if (BuildConfig.PRODUCT == Constants.PRODUCT_FITNESS_TRACKER_PRO) {
                viewState.setVisibility(View.GONE);
            } else {
                viewState.setVisibility(View.VISIBLE);
            }
            Log.e(TAG, "mainService.getCurrentDevice() != null");
            /*float version = Float.valueOf(VivitarDeviceInfo.getInt(this,VivitarDeviceInfo.LIDL_FIREWARE_HIGH,0)+"."+
                    VivitarDeviceInfo.getInt(this,VivitarDeviceInfo.LIDL_FIREWARE_LOW,0));*/
            if (myBaseDevice == null || (myBaseDevice != null && !mainService.getCurrentDevice().getMac().equals
                    (myBaseDevice.getMac()))) {
                tvState.setText(Utils.replaceDeviceNameToCC431(mainService.getCurrentDevice().getName(), 0)
                        + "  " +
                        (mainService.getConnectionState() == BaseController.STATE_CONNECTED ?
                                getString(R.string.connected) : getString(R.string.disconnected)));
            } else {
                tvState.setText(Utils.replaceDeviceNameToCC431(mainService.getCurrentDevice().getName(), myBaseDevice
                        .getVersion())
                        + "  " +
                        (mainService.getConnectionState() == BaseController.STATE_CONNECTED ?
                                getString(R.string.connected) : getString(R.string.disconnected)));
            }

        } else {
            Log.e(TAG, "mainService.getCurrentDevice() == null");
            viewState.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    /***
     * 退出页面调用 从右到左退出
     *
     * @param at
     */
    public static void overridePendingTransitionExit(Activity at) {
        at.overridePendingTransition(R.anim.in_lefttoright, R.anim.out_to_left);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        MainService mainService = MainService.getInstance(this);
        switch (v.getId()) {
            case R.id.back_tv:
                finish();
                overridePendingTransitionExit(MenuSetActivity.this);
                break;
            case R.id.menu_device_setting:
                intent = new Intent(this, ActivityDeviceSetting.class);
                startActivity(intent);
                break;
            case R.id.menu_bind_device:

                /* if (true) {
                 *//* intent = new Intent(this, ActivityWeather.class);
                    startActivity(intent);*//*

                    intent = new Intent(this, ActivityDeviceSetting.class);
                    startActivity(intent);
                    return;
                }
*/
                if (Constants.IS_FACTORY_VERSION) {
                    if (currentType == -1) {
                        Toast.makeText(this, getString(R.string.choose_devicetype_first), Toast.LENGTH_LONG).show();
                    } else {
                        goManagerDeviceAct();
                    }
                } else {
                    goManagerDeviceAct();
                }
                break;
            case R.id.ly_about:
                if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {
//                    if(ActivityCompat.checkSelfPermission(this , Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
//                            PackageManager.PERMISSION_GRANTED) {
//                        ActivityCompat.requestPermissions(this , new String[]{Manifest.permission
//                                .READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE} ,1);
//                    }else {
//                        intent = new Intent(this, com.energetics.tracker.main.settings.AboutUsActivity.class);
//                        startActivity(intent);
//                    }
                } else {
                    intent = new Intent(this, AboutUsActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.ly_user_info:
                intent = new Intent(this, UserInfoActivity.class);
                intent.putExtra(Constants.EXTRA_GOUSERINFO, false);
                startActivity(intent);
                break;
            case R.id.layout_device_change:
                intent = new Intent(this, LogActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_switch_devicetype:
                if (mainService != null && mainService.getCurrentDevice() != null) {
                    Toast.makeText(this, getString(R.string.please_unbind), Toast.LENGTH_LONG).show();
                } else {
                    intent = new Intent(this, DeviceTypeActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.layout_addacount:
                intent = new Intent(this, ActivityAddAccount.class);
                startActivity(intent);
                break;
            case R.id.layout_check_update:
                //查询更新

                break;
        }
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

    private void goManagerDeviceAct() {
        Intent intent;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.turn_bluetooth_first), Toast.LENGTH_LONG).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                verifyPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            } else {
                intent = new Intent(this, ManageDeviceActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions != null) {
            List<String> list = new ArrayList<>();
            boolean isShouldShow = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (!isShouldShow) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MenuSetActivity.this);
                                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                    builder.setMessage(getString(R.string.location_permission));
                                }
                                //builder.create().show();
                            }
                        }
                    }
                    list.add(permissions[i]);
                } else {
                    Intent intent = new Intent(this, ManageDeviceActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    public void verifyPermission(String[] permissions) {
        if (permissions != null) {
            List<String> lists = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {

                    }
                    lists.add(permissions[i]);
                } else if (ActivityCompat.checkSelfPermission(MenuSetActivity.this, permissions[i]) == PackageManager
                        .PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, ManageDeviceActivity.class);
                    startActivity(intent);
                    break;
                }
            }
            if (lists.size() > 0) {
                String[] ps = new String[lists.size()];
                for (int i = 0; i < lists.size(); i++) {
                    ps[i] = lists.get(i);
                }
                ActivityCompat.requestPermissions(this, ps, 1);
            }
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainService mainService = MainService.getInstance(context);
            if (mainService != null && mainService.getCurrentDevice() != null) {
                if (BuildConfig.PRODUCT == Constants.PRODUCT_FITNESS_TRACKER_PRO) {
                    viewState.setVisibility(View.GONE);
                } else {
                    viewState.setVisibility(View.VISIBLE);
                }
                tvState.setText(Utils.replaceDeviceNameToCC431(mainService.getCurrentDevice().getName(),
                        Float.valueOf(HamaDeviceInfo.getInt(context,
                                HamaDeviceInfo
                                        .LIDL_FIREWARE_HIGH, 0) + "." +
                                HamaDeviceInfo.getInt(context,
                                        HamaDeviceInfo.LIDL_FIREWARE_LOW, 0))) + "  " +
                        (mainService.getConnectionState() == BaseController.STATE_CONNECTED ?
                                getString(R.string.connected) : getString(R.string.disconnected)));
            } else {
                viewState.setVisibility(View.GONE);
            }
            updateUI();
        }
    };

    public void onEventMainThread(String s) {
        if (s.equals("Miss")) {
            viewState.setVisibility(View.GONE);
        }
    }
}