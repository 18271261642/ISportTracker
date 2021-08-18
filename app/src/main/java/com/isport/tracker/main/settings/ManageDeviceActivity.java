package com.isport.tracker.main.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.services.BleService;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.MyBaseDevice;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.main.BindDeviceActivity;
import com.isport.tracker.main.DeviceTypeActivity;
import com.isport.tracker.main.WelcomeActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/12/5.
 */

public class ManageDeviceActivity extends BaseActivity implements View.OnClickListener, AdapterView
        .OnItemClickListener {

    private static final String TAG = ManageDeviceActivity.class.getSimpleName();
    private ListView listView;
    //private List<BaseDevice> listDevices;
    private Map<String, MyBaseDevice> listDevices;
    private List<String> macList;
    private Set<String> macSet;
    private ProgressBar progressBar;
    private View viewBack;
    private Button btnNext;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String currentTypeName;
    private int currentType = -1;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_settings_device_scan);

        handler = new MyHandler(this);

        sharedPreferences = getSharedPreferences(DeviceTypeActivity.CONFIG_DEVICE, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        currentType = sharedPreferences.getInt(DeviceTypeActivity.KEY_DEVICE_TYPE, -1);
        currentIndex = sharedPreferences.getInt(DeviceTypeActivity.KEY_DEVICE_INDEX, 0);
        MainService mainService = MainService.getInstance(this);

        if (mainService != null && mainService.getCurrentDevice() != null && currentType == -1) {
            currentType = mainService.getCurrentDevice().getDeviceType();
        }
        if (currentType != -1) {
            currentTypeName = sharedPreferences.getString(currentType + " " + currentIndex, "");
        }


        verifyPermission();

        listDevices = new HashMap<String, MyBaseDevice>();
        macSet = new HashSet<>();
        macList = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.manage_device_fresh_bar);
        listView = (ListView) findViewById(R.id.manage_device_list);
        listView.setOnItemClickListener(this);
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter();
        listView.setAdapter(deviceListAdapter);

        viewBack = findViewById(R.id.return_back);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.ACTION_FOUND);
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_CONNECTE_ERROR);
        //registerReceiver(mReceivcer, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceivcer, filter);
        refresh();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceivcer);
        if (handler.hasMessages(0x01)) {
            handler.removeMessages(0x01);
        }
        if (handler.hasMessages(0x02)) {
            handler.removeMessages(0x02);
        }
        if (handler.hasMessages(0x03)) {
            handler.removeMessages(0x03);
        }
        if (handler.hasMessages(0x04)) {
            handler.removeMessages(0x04);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MainService mainService = MainService.getInstance(this);
        BaseDevice curBaseDevice = null;
        if (mainService != null) {
            mainService.cancelLeScan();
            curBaseDevice = mainService.getCurrentDevice();
        }
        MyBaseDevice bdv = listDevices.get(macList.get(position));
        if (curBaseDevice != null && !curBaseDevice.getMac().equals(bdv.getMac())) {
            Toast.makeText(this, getString(R.string.please_unbind), Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, BindDeviceActivity.class);
            intent.putExtra("device", bdv);
            startActivity(intent);
        }
    }

    private void refresh() {

        BluetoothAdapter bleAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bleAdapter == null || !bleAdapter.isEnabled() || bleAdapter.getState() != BluetoothAdapter.STATE_ON) {
            Toast.makeText(this, getString(R.string.open_bluetooth_first), Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, getString(R.string.open_location_service_first), Toast.LENGTH_SHORT).show();
                return;
            }
        }*/
        listDevices.clear();
        macSet.clear();
        macList.clear();
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter();
        listView.setAdapter(deviceListAdapter);
        deviceListAdapter.notifyDataSetChanged();

        MainService mainService = MainService.getInstance(this);
        if (mainService != null) {
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            BaseDevice baseDevice = mainService.getCurrentDevice();

            Log.e(TAG,"--BaseDevice="+new Gson().toJson(baseDevice));


            if (baseDevice != null) {
                Float version = Float.valueOf(ConfigHelper.getInstance(this).getString(baseDevice.getMac(), "0.0"));
                MyBaseDevice myBaseDevice = null;
                if (!ConfigHelper.getInstance(this).getString(Constants.INFO_CURRENT_DEVICE, "").equals("")) {
                    myBaseDevice = new Gson().fromJson(ConfigHelper.getInstance(this).getString(Constants
                            .INFO_CURRENT_DEVICE, ""), MyBaseDevice.class);
                }
                if (myBaseDevice != null && myBaseDevice.getMac().equals(baseDevice.getMac())) {

                } else {
                    myBaseDevice = null;
                }
                if (myBaseDevice == null) {
                    myBaseDevice = new MyBaseDevice(version, baseDevice);
                }
                listDevices.put(myBaseDevice.getMac(), myBaseDevice);
                macSet.add(myBaseDevice.getMac());
                macList.add(myBaseDevice.getMac());
                deviceListAdapter.notifyDataSetChanged();
            }
            mainService.startLeScan();
            handler.sendEmptyMessageDelayed(0x02, 10000);
        }
    }

    private Handler handler = null;

    private static class MyHandler extends Handler {
        private WeakReference<ManageDeviceActivity> manageDeviceActivity;

        public MyHandler(ManageDeviceActivity activity) {
            manageDeviceActivity = new WeakReference<ManageDeviceActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:

                    MyBaseDevice baseDevice = (MyBaseDevice) msg.obj;
                    if (manageDeviceActivity.get() != null) {
                        if (manageDeviceActivity.get().macSet.contains(baseDevice.getMac())) {
                            manageDeviceActivity.get().listDevices.put(baseDevice.getMac(), baseDevice);
                            ((DeviceListAdapter) manageDeviceActivity.get().listView.getAdapter())
                                    .notifyDataSetChanged();
                            return;
                        }
                        manageDeviceActivity.get().progressBar.setVisibility(View.GONE);
                        manageDeviceActivity.get().listView.setVisibility(View.VISIBLE);
                        manageDeviceActivity.get().listDevices.put(baseDevice.getMac(), baseDevice);
                        manageDeviceActivity.get().macSet.add(baseDevice.getMac());
                        manageDeviceActivity.get().macList.add(baseDevice.getMac());
                        ((DeviceListAdapter) manageDeviceActivity.get().listView.getAdapter()).notifyDataSetChanged();
                    }
                    break;
                case 0x02:
                    if (manageDeviceActivity.get() != null) {
                        manageDeviceActivity.get().listView.setVisibility(View.VISIBLE);
                        manageDeviceActivity.get().progressBar.setVisibility(View.GONE);
                        if (MainService.getInstance(manageDeviceActivity.get()) != null) {
                            MainService.getInstance(manageDeviceActivity.get()).cancelLeScan();
                        }
                    }
                    break;
                case 0x03:
                    if (manageDeviceActivity.get() != null) {
                        manageDeviceActivity.get().refresh();
                    }
                    break;
                case 0x04:
                    ArrayList<MyBaseDevice> tplist = (ArrayList<MyBaseDevice>) msg.obj;
                    if (manageDeviceActivity.get() != null) {
                        if (tplist.size() > 0) {
                            manageDeviceActivity.get().progressBar.setVisibility(View.GONE);
                            manageDeviceActivity.get().listView.setVisibility(View.VISIBLE);
                        }
                        for (int i = 0; i < tplist.size(); i++) {
                            MyBaseDevice tpd = tplist.get(i);
                            if (manageDeviceActivity.get().listDevices.get(tpd.getMac()) == null) {
                                manageDeviceActivity.get().listDevices.put(tpd.getMac(), tpd);
                                manageDeviceActivity.get().macList.add(tpd.getMac());
                            }
                        }
                        if (manageDeviceActivity.get().listView.getAdapter() != null) {
                            ((DeviceListAdapter) manageDeviceActivity.get().listView.getAdapter())
                                    .notifyDataSetChanged();
                        }
                    }
                    break;
            }
        }
    }

    ;

    public void verifyPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refresh();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_back:
                WelcomeActivity.setFirst(this, false);
                finish();
                break;
            case R.id.manage_device_fresh:
                refresh();
                break;

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //if (keyCode == KeyEvent.KEYCODE_BACK && WelcomeActivity.isFirst(this)) {
        // moveTaskToBack(true);
        //}
        return super.onKeyDown(keyCode, event);
    }

    class DeviceListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return listDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(ManageDeviceActivity.this);
                convertView = inflater.inflate(R.layout.view_scan_device_item, null);
                holder = new Holder();
                holder.content = (TextView) convertView.findViewById(R.id.manage_device_name);
                holder.bind_icon = (ImageView) convertView.findViewById(R.id.bind_device_item_icon);
                holder.rssi_icon = (ImageView) convertView.findViewById(R.id.bind_device_item_rssi);
                holder.detail_icon = (ImageView) convertView.findViewById(R.id.bind_device_item_detail);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();

            MainService mainService = MainService.getInstance(ManageDeviceActivity.this);
            BaseDevice currentDevice = null;
            int connectState = BaseController.STATE_DISCONNECTED;
            if (mainService != null) {
                currentDevice = mainService.getCurrentDevice();
                connectState = mainService.getConnectionState();
            }
            BaseDevice entity = listDevices.get(macList.get(position));
            String name = entity.getName();
            String mac = entity.getMac();
            String address = (currentDevice == null ? "" : currentDevice.getMac());
            if (entity.getMac().equals(address)) {
                holder.bind_icon.setVisibility(View.VISIBLE);
            } else {
                holder.bind_icon.setVisibility(View.GONE);
            }
//            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {
//                StringBuilder builder = new StringBuilder(Utils.replaceDeviceNameToCC431(name, 0));
//                holder.content.setText(builder.toString());
//            } else {
            StringBuilder builder = new StringBuilder(Utils.replaceDeviceNameToCC431(name, 0)).append("\r\n")
                    .append(entity.getMac());
           /* StringBuilder builder = new StringBuilder(name).append("\r\n")
                    .append(entity.getMac());*/
            holder.content.setText(builder.toString());
//            }
            if (entity.getMac().equals(address) && connectState == BaseController.STATE_CONNECTED) {
                holder.content.setTextColor(getResources().getColor(R.color.green));
            } else {
                holder.content.setTextColor(getResources().getColor(R.color.title_color));
            }
            if (entity.getRssi() > -70) {
                holder.rssi_icon.setImageResource(R.drawable.ic_rssi_3_bars);
            } else if (entity.getRssi() > -85) {
                holder.rssi_icon.setImageResource(R.drawable.ic_rssi_2_bars);
            } else {
                holder.rssi_icon.setImageResource(R.drawable.ic_rssi_1_bars);
            }
            return convertView;
        }

        public class Holder {
            TextView content;
            ImageView bind_icon;
            ImageView rssi_icon;
            ImageView detail_icon;
        }

    }

    //
    private final BroadcastReceiver mReceivcer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;
            if (action.equals(BleService.ACTION_FOUND)) {
                ArrayList<BaseDevice> listD = (ArrayList<BaseDevice>) intent.getSerializableExtra(BleService
                        .EXTRA_DEVICE_LIST_FOUND);
                if (listD == null) {
                    BaseDevice device = (BaseDevice) intent.getSerializableExtra(BleService.EXTRA_DEVICE_FOUND);
                    device = handleActionFount(device);
                    if (device != null) {
                        Message msg = Message.obtain();
                        msg.obj = device;
                        msg.what = 0x01;
                        handler.sendMessage(msg);
                    }
                } else {
                    ArrayList<MyBaseDevice> listDevicesTp = new ArrayList<MyBaseDevice>();
                    for (int i = 0; i < listD.size(); i++) {
                        BaseDevice result = listD.get(i);
                        Log.e(TAG, "name1 = " + result.getName());
                        if (result.getName() == null) {
                            result.setName(Utils.parseFromBytes(result.getScanRecord()));
                        }
                        Log.e(TAG, "name1 = " + result.getName());
                        //BluetoothDevice device = result.getDevice();
                        String dn = (result.getName() == null ? "" : result.getName());
                        MyBaseDevice tpbaseDevice = new MyBaseDevice(0, new BaseDevice(dn, result.getMac(), result
                                .getRssi()));
                        tpbaseDevice = handleActionFount(tpbaseDevice);
                        if (tpbaseDevice != null) {
                            listDevicesTp.add(tpbaseDevice);
                        }

                    }
                    if (listDevicesTp.size() > 0) {
                        Message msg = Message.obtain();
                        msg.obj = listDevicesTp;
                        msg.what = 0x04;
                        handler.sendMessage(msg);
                    }
                }
            } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
                int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if (state == BaseController.STATE_DISCONNECTED) {
                    handler.sendEmptyMessageDelayed(0x03, 2000);
                }
            }
        }
    };


    //TODO 设备类型区分
    private MyBaseDevice handleActionFount(BaseDevice device) {
        String name = device.getName();
        String tpName;
        if (name.contains("W311N_")) {
            tpName = "W311N_";
            device.setName(name);
        } else if (name.contains("Heart Rate Sensor")) {
            tpName = name;
            device.setName(name);
        } else {
            tpName = (name == null ? "" : name.contains("_") ? name.split("_")[0] : name.contains("-") ? name.split("-")[0] : name.split(" ")[0]);
            if (tpName.equalsIgnoreCase("SADP")) {
                device.setName("SADP 4 A1");
            } else if (tpName.equalsIgnoreCase("SF")) {
                device.setName("SF Band");
            } else if (tpName.equalsIgnoreCase("medel")) {
                device.setName("Medel CW");
            } else {
                device.setName(tpName);
            }
        }
        String tpLN = tpName.toLowerCase();
        Log.e(TAG, "00000name == " + name + " tpName == " + tpName + " tpLN ==" + tpLN + " mac == " + device.getMac());

        if (Constants.IS_FACTORY_VERSION) {
            if (currentTypeName == null && currentTypeName.equals("") || tpName.equals("") || !tpName
                    .equalsIgnoreCase(currentTypeName))
                return null;
        } else {
            if ((BuildConfig.PRODUCT.equals(Constants.PRODUCT_HU_TRACKER) || BuildConfig.PRODUCT.equals(Constants
                    .PRODUCT_ACTIVA_T)) && !(tpLN.equals("w307s") || tpLN.equals("w311n"))) {
                return null;
            } else if ((BuildConfig.PRODUCT.equals(Constants.PRODUCT_HU_TRACKER) || BuildConfig.PRODUCT.equals
                    (Constants.PRODUCT_ACTIVA_T))) {

            } else if ((BuildConfig.PRODUCT.equals(Constants.PRODUCT_ETEK)) && !tpLN.contains("w311")) {
                return null;
            } else if ((BuildConfig.PRODUCT.equals(Constants.PRODUCT_REFLEX)) && !(tpLN.equals("reflex") || tpLN
                    .equals("rush"))) {
                return null;
            } else if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS) && !(tpLN.equals("p118") || tpLN
                    .equals("millionpedometer") ||
                    tpLN.equals("w311n") || tpLN.equals("w301s") || tpLN.equals("w307n") || tpLN.equals("ta400hr") ||
                    tpLN.equals("ta300"))) {
                Log.e(TAG, "111111name == " + name + " tpName == " + tpName + " tpLN ==" + tpLN + " mac == " + device
                        .getMac());
                return null;
            } else if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {
                Log.e(TAG, "66666name == " + name + " tpName == " + tpName + " tpLN ==" + tpLN + " mac == " + device
                        .getMac());
            } else if (!(tpLN.equals("w311n") || tpLN.equals("w311n_") || tpLN.equals("w509a") || tpLN.equals("beat")
                    || tpLN.equals("w510") || tpLN.equals("w301s") || tpLN.equals("ta400hr") || tpLN.equals("ta300")
                    || tpLN.equals("sadp") || tpLN.startsWith("w307s") || tpLN.equals("w285s") || tpLN.equals
                    ("w285b") ||
                    tpLN.equals("w194") || tpLN.equals("w240n") || tpLN.equals("w240b") || tpLN.equals("w240s") ||
                    name.equalsIgnoreCase("p118") ||
                    tpLN.equals("millionpedometer") || tpLN.equalsIgnoreCase("goodmans") || tpLN.equalsIgnoreCase
                    ("activitytracker") || tpLN.equals
                    ("w311g") ||
                    tpLN.equals("w301n") || tpLN.equals("w301b") || tpLN.equals("w307e") || tpLN.equals("w307n") ||
                    tpLN.startsWith("sf") || tpLN.equals("w338") || tpLN.equals("w311h") ||
                    tpLN.equals("w337b") || tpLN.equals("sas80") || tpLN.equals("sas87") || tpLN.equals("at100") ||
                    tpLN.equals("at200") || tpLN.equals("w316") || tpLN.equals("p674a") ||
                    tpLN.equals("as87") || tpLN.equals("as97") || tpLN.equals("jjtracker 1") ||
                    tpLN.equals("rbr100") || tpLN.equals("w376") || tpLN.equalsIgnoreCase("hp-w311n") ||
                    tpLN.equals("w301h") || tpLN.equals("w307h") || tpLN.equals("heart rate sensor") || tpLN.equals
                    ("w523") || tpLN.equals("w525") || tpLN.equals("rush") || tpLN.equals
                    ("w505") || tpLN.equals
                    ("w311t") || tpLN.equals
                    ("medel") || tpLN.equals
                    ("sas82") || tpLN.contains
                    ("reflex") || tpLN.contains("w520") || tpLN.contains("w311") || tpLN.contains("W520") || tpLN.contains("w516") || tpLN.contains("blin16")
                    || tpLN.contains("FASTRACK")
                    || tpLN.contains("fastrack")
                    || tpLN.contains("W681") || tpLN.contains("w681"))) {
                return null;
            }
        }
        if (name.contains(" ") && tpName.startsWith("W307S")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W307S_SPACE);
        } else if (tpName.equalsIgnoreCase("W311N") || (tpName.equalsIgnoreCase("BLIN16")) || tpName.equalsIgnoreCase("SADP") || tpName.equalsIgnoreCase
                ("Medel") || tpName.equalsIgnoreCase
                ("BEAT") || tpName.equalsIgnoreCase
                ("W509A") || tpName.contains("W520") || tpName.contains("W516") || tpName.contains("W311")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W311N);
        } else if (tpName.equalsIgnoreCase("TA400HR")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W311N);
        } else if (tpName.equalsIgnoreCase("goodmans")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W311N);
        } else if (tpName.equalsIgnoreCase("W301N")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W301N);
        } else if (tpName.equalsIgnoreCase("W301S") || tpName.equalsIgnoreCase("TA300")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W301S);
        } else if (tpName.equalsIgnoreCase("W307N")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W307N);
        } else if (tpName.startsWith("W307S") || tpName.equalsIgnoreCase("W505")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W307S);
        } else if (tpName.startsWith("SF") || tpName.startsWith("W301B") || tpName.startsWith("W307E")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W307S);
        } else if (tpName.contains("REFLEX") || tpName.contains("FASTRACK") || tpName.equalsIgnoreCase("Reflex2C")) {
            Log.e("REFLEX", "REFLEX:" + device.getName());
            device = new BaseDevice(device.getName() + "_" + device.getMac(), device.getMac(), device.getRssi(), 0,
                    BaseController.CMD_TYPE_W311, BaseDevice.TYPE_W307S);
        } else if (tpName.equalsIgnoreCase("Rush")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0,
                    BaseController.CMD_TYPE_W311, BaseDevice.TYPE_W307S);
        } else if (tpName.equalsIgnoreCase("w301h")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W301H);
        } else if (tpName.equalsIgnoreCase("w307h")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W307H);
        } else if (tpName.equalsIgnoreCase("W285S") || tpName.equalsIgnoreCase("W240n") || tpName.equalsIgnoreCase
                ("W240s")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W285S);
        } else if (tpName.equalsIgnoreCase("W194")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W194, BaseDevice.TYPE_W194);
        } else if (tpName.equalsIgnoreCase("MillionPedometer")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W194, BaseDevice.TYPE_MILLIONPEDOMETER);
        } else if (tpName.equalsIgnoreCase("P118")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W194, BaseDevice.TYPE_P118);
        } else if (tpName.equalsIgnoreCase("activitytracker")) {//W240
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W194, BaseDevice.TYPE_ACTIVITYTRACKER);
        } else if (tpName.equalsIgnoreCase("P118S")) {//W240
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W194, BaseDevice.TYPE_P118S);
        } else if (tpName.equalsIgnoreCase("W337B")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W337B, BaseDevice.TYPE_W337B);
        } else if (tpName.equalsIgnoreCase("sas80") || tpName.equalsIgnoreCase("SAS82")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_SAS80);
        } else if (tpName.equalsIgnoreCase("at100")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AT100);
        } else if (tpName.equalsIgnoreCase("at200")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AT200);
        } else if (tpName.equalsIgnoreCase("w338")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W311N);
        } else if (tpName.equalsIgnoreCase("w311H")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AT200);
        } else if (tpName.equalsIgnoreCase("w311G")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AT200);
        } else if (tpName.equalsIgnoreCase("P674A")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AT200);
        } else if (tpName.equalsIgnoreCase("W316")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W316);
        } else if (tpName.equalsIgnoreCase("W523") || tpName.equalsIgnoreCase("W525")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W316);
        } else if (tpName.equalsIgnoreCase("AS87") || tpName.equalsIgnoreCase("W510")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_SAS80);
        } else if (tpName.equalsIgnoreCase("SAS87") || tpName.equalsIgnoreCase("AS97") || tpName.equalsIgnoreCase("W311N_")) {
            //对应的是W354，心率存储不同
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AS97);
        } else if (tpName.equalsIgnoreCase("JJTracker 1")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W311N);
        } else if (tpName.equalsIgnoreCase("RBR100")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AT200);
        } else if (tpName.equalsIgnoreCase("W376")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_AT200);
        } else if (tpName.equalsIgnoreCase("HP-W311N")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W311N);
        } else if (tpName.equalsIgnoreCase("Heart Rate Sensor") || name.contains("W681") || name.contains("w681")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_HEARTRATE, BaseDevice.TYPE_HEART_RATE);
        } else if (tpName.equalsIgnoreCase("w311t")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_NMC, BaseDevice.TYPE_W311T);
        } else if (tpName.equalsIgnoreCase("W240B")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W240B);
        } else if (tpName.equalsIgnoreCase("W285B")) {
            device = new BaseDevice(device.getName(), device.getMac(), device.getRssi(), 0, BaseController
                    .CMD_TYPE_W311, BaseDevice.TYPE_W285B);
        }
        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ETEK)) {
            Log.e("scan", "device name = " + tpName);
            device.setName("ETEK 944745");
        }
        if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {
            Log.e(TAG, "33333name == " + name + " tpName == " + tpName + " tpLN ==" + tpLN + " mac == " + device
                    .getMac());
            if (tpLN.equals("p118")) {
                device.setName("PODO200");
            }
            if (tpLN.equals("w307n")) {
                device.setName("TA200");
            } else if (tpLN.equals("millionpedometer")) {
                device.setName("PODO300");
            } else if (tpLN.equals("w301s") || tpLN.equals("w301n")) {
                device.setName("TA300");
            } else if (tpLN.equals("w311n")) {
                device.setName("TA400HR");
            }
        }
        return (new MyBaseDevice(0, device));
    }

    /**
     * 倒叙
     *
     * @return
     */
    public String macToString(String mac) {
        if (mac != null) {
            String[] ms = mac.split(":");
            StringBuilder builder = new StringBuilder();
            int len = ms.length;
            for (int i = 1; i <= ms.length; i++) {
                builder.append(ms[len - i] + " ");
            }
            return builder.toString().trim();
        }
        return mac;
    }
}
