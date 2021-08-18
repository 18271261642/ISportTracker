package com.isport.fitness.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.HamaDeviceInfo;
import com.isport.tracker.entity.MyBaseDevice;
import com.isport.tracker.fragment.BaseFragment;
import com.isport.tracker.main.DeviceTypeActivity;
import com.isport.tracker.main.settings.AboutUsActivity;
import com.isport.tracker.main.settings.ActivityAddAccount;
import com.isport.tracker.main.settings.ActivityDeviceSetting;
import com.isport.tracker.main.settings.ManageDeviceActivity;
import com.isport.tracker.main.settings.UserInfoActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.Utils;

import static android.content.Context.MODE_PRIVATE;
import static com.isport.tracker.R.id.layout_switch_devicetype;

/**
 * Created by wj on 2017/8/9.
 */

public class HeadFragmentActivity extends BaseFragment implements View.OnClickListener {

    private static final String TAG = HeadFragmentActivity.class.getSimpleName();
    private Context mContext;
    LinearLayout llDeviceSetting, llBindDevcie, llUserInfo, llAddAcount, llDeviceType, llAbout, llDeviceChange;
    private TextView tvState;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int currentType = -1;

    public static HeadFragmentActivity newInstance() {
        Bundle args = new Bundle();
        HeadFragmentActivity fragment = new HeadFragmentActivity();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void regist() {
        IntentFilter filter = new IntentFilter(MainService.ACTION_CONNECTE_CHANGE);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume==");
        updateUI();
        super.onResume();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainService mainService = MainService.getInstance(context);
            if (mainService != null && mainService.getCurrentDevice() != null) {
                Log.e(TAG, "11111111111==");
                if (BuildConfig.PRODUCT==Constants.PRODUCT_FITNESS_TRACKER_PRO){
                    llDeviceSetting.setVisibility(View.GONE);
                }else{
                    llDeviceSetting.setVisibility(View.VISIBLE);
                }
                tvState.setText(Utils.replaceDeviceNameToCC431(mainService.getCurrentDevice().getName(),
                                                               Float.valueOf(HamaDeviceInfo.getInt(context, HamaDeviceInfo.LIDL_FIREWARE_HIGH, 0) + "." +
                                                                                     HamaDeviceInfo.getInt(context, HamaDeviceInfo.LIDL_FIREWARE_LOW, 0))) + "  " +
                                        (mainService.getConnectionState() == BaseController.STATE_CONNECTED ? getString(R.string.connected) : getString(R.string.disconnected)));
            } else {
                Log.e(TAG, "2222222222222==");
                llDeviceSetting.setVisibility(View.GONE);
            }
            updateUI();
        }
    };

    public void updateUI() {
        MyBaseDevice myBaseDevice = null;
        if (!ConfigHelper.getInstance(getActivity()).getString(Constants.INFO_CURRENT_DEVICE, "").equals("")) {
            myBaseDevice = new Gson().fromJson(ConfigHelper.getInstance(getActivity()).getString(Constants.INFO_CURRENT_DEVICE, ""), MyBaseDevice.class);
        }
        MainService mainService = MainService.getInstance(getActivity());
        if (mainService != null && mainService.getCurrentDevice() != null) {
            Log.e(TAG, "333333333333==");
            if (BuildConfig.PRODUCT==Constants.PRODUCT_FITNESS_TRACKER_PRO){
                llDeviceSetting.setVisibility(View.GONE);
            }else{
                llDeviceSetting.setVisibility(View.VISIBLE);
            }
            /*float version = Float.valueOf(VivitarDeviceInfo.getInt(this,VivitarDeviceInfo.LIDL_FIREWARE_HIGH,0)+"."+
                    VivitarDeviceInfo.getInt(this,VivitarDeviceInfo.LIDL_FIREWARE_LOW,0));*/
            if (myBaseDevice == null || (myBaseDevice != null && !mainService.getCurrentDevice().getMac().equals(myBaseDevice.getMac()))) {
                tvState.setText(Utils.replaceDeviceNameToCC431(mainService.getCurrentDevice().getName(), 0)
                                        + "  " +
                                        (mainService.getConnectionState() == BaseController.STATE_CONNECTED ? getString(R.string.connected) : getString(R.string.disconnected)));
            } else {
                tvState.setText(Utils.replaceDeviceNameToCC431(mainService.getCurrentDevice().getName(), myBaseDevice.getVersion())
                                        + "  " +
                                        (mainService.getConnectionState() == BaseController.STATE_CONNECTED ? getString(R.string.connected) : getString(R.string.disconnected)));
            }
        } else {
            Log.e(TAG, "444444444444444==");
            llDeviceSetting.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_heart_setting, container, false);
        llDeviceSetting = (LinearLayout) view.findViewById(R.id.menu_device_setting);
        llBindDevcie = (LinearLayout) view.findViewById(R.id.menu_bind_device);
        llUserInfo = (LinearLayout) view.findViewById(R.id.ly_user_info);
        llAddAcount = (LinearLayout) view.findViewById(R.id.layout_addacount);
        llDeviceType = (LinearLayout) view.findViewById(layout_switch_devicetype);
        llAbout = (LinearLayout) view.findViewById(R.id.ly_about);
        llDeviceChange = (LinearLayout) view.findViewById(R.id.layout_device_change);
        tvState = (TextView) view.findViewById(R.id.menu_tv_connectstate);

        llDeviceSetting.setOnClickListener(this);
        llBindDevcie.setOnClickListener(this);
        llUserInfo.setOnClickListener(this);
        llAddAcount.setOnClickListener(this);
        llDeviceType.setOnClickListener(this);
        llAbout.setOnClickListener(this);

        if (!Constants.IS_FACTORY_VERSION) {
            llDeviceType.setVisibility(View.GONE);
        }
//        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity()) == ConnectionResult.SERVICE_MISSING) {
//            llAddAcount.setVisibility(View.GONE);
//        }

        sharedPreferences = getActivity().getSharedPreferences(DeviceTypeActivity.CONFIG_DEVICE, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        currentType = sharedPreferences.getInt(DeviceTypeActivity.KEY_DEVICE_TYPE, -1);

        MainService mainService = MainService.getInstance(mContext);
        regist();
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.menu_device_setting:
                intent = new Intent(getActivity(), ActivityDeviceSetting.class);
                startActivity(intent);
                break;
            case R.id.menu_bind_device:
                if (Constants.IS_FACTORY_VERSION) {
                    if (currentType == -1) {
                        Toast.makeText(getActivity(), getString(R.string.choose_devicetype_first), Toast.LENGTH_LONG).show();
                    } else {
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        if (adapter == null || !adapter.isEnabled()) {
                            Toast.makeText(getActivity(), getString(R.string.turn_bluetooth_first), Toast.LENGTH_LONG).show();
                        } else {
                            intent = new Intent(getActivity(), ManageDeviceActivity.class);
                            startActivity(intent);
                        }
                    }
                } else {
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                    if (adapter == null || !adapter.isEnabled()) {
                        Toast.makeText(getActivity(), getString(R.string.turn_bluetooth_first), Toast.LENGTH_LONG).show();
                    } else {
                        intent = new Intent(getActivity(), ManageDeviceActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            case R.id.ly_user_info:
                intent = new Intent(getActivity(), UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.layout_addacount:
                intent = new Intent(getActivity(), ActivityAddAccount.class);
                startActivity(intent);
                break;
            case layout_switch_devicetype:
                MainService mainService = MainService.getInstance(getActivity());
                if (mainService != null && mainService.getCurrentDevice() != null) {
                    Toast.makeText(getActivity(), getString(R.string.please_unbind), Toast.LENGTH_LONG).show();
                } else {
                    intent = new Intent(getActivity(), DeviceTypeActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.ly_about:
                intent = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
