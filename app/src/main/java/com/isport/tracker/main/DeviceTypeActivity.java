package com.isport.tracker.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.main.settings.UserInfoActivity;
import com.isport.tracker.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by feige on 2017/6/15.
 */

public class DeviceTypeActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    public final static String CONFIG_DEVICE = "com.isport.tracker.CONFIG_DEVICE";
    public final static String KEY_DEVICE_TYPE = "com.isport.tracker.KEY_DEVICE_TYPE";///当前设备类型
    public final static String KEY_DEVICE_INDEX = "com.isport.tracker.KEY_DEVICE_INDEX";///當前設備索引，同一个类型设备可能有几个设备型号

    private TextView tvCurrentType;
    private ListView listView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private List<Map<String, String>> listDeviceName = new ArrayList<>();

    private int[] types = new int[]{BaseDevice.TYPE_W307H, BaseDevice.TYPE_W301H, BaseDevice.TYPE_W311N, BaseDevice.TYPE_W307S, BaseDevice.TYPE_W307N, BaseDevice.TYPE_W301N, BaseDevice.TYPE_W301S, BaseDevice.TYPE_W285S,
            BaseDevice.TYPE_W194, BaseDevice.TYPE_W337B, BaseDevice.TYPE_AT100, BaseDevice.TYPE_AT200, BaseDevice.TYPE_SAS80, BaseDevice.TYPE_P118, BaseDevice.TYPE_ACTIVITYTRACKER,
            BaseDevice.TYPE_MILLIONPEDOMETER, BaseDevice.TYPE_W240, BaseDevice.TYPE_W240B, BaseDevice.TYPE_W240S};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_type);

        sharedPreferences = getSharedPreferences(CONFIG_DEVICE, MODE_PRIVATE);
        editor = sharedPreferences.edit();


        String[][] strName = new String[][]{{"W307H"}, {"W301H"}, {"W311N", "W338"}, {"W307S"}, {"W307N"}, {"W301N"}, {"W301S"}, {"W285S"}, {"W194"}, {"W337B"}, {"AT100"}, {"AT200"}, {"SAS80"}, {"P118"},
                {"activitytracker"}, {"MillionPedometer"}, {"W240"}, {"W240B"}, {"W240S"}};
        for (int i = 0; i < strName.length; i++) {
            for (int j = 0; j < strName[i].length; j++) {
                Map<String, String> map = new HashMap<>();
                map.put("name", strName[i][j]);
                map.put("index", j + "");
                map.put("type", i + "");
                listDeviceName.add(map);
                ///type + index
                editor.putString(types[i] + " " + j, strName[i][j]).commit();
            }
        }

        tvCurrentType = (TextView) findViewById(R.id.tv_current_device_type);
        listView = (ListView) findViewById(R.id.device_type_list);
        listView.setAdapter(new SimpleAdapter(this, listDeviceName, R.layout.activity_select_item, new String[]{"name"}, new int[]{R.id.select_device_item_tv}));
        listView.setOnItemClickListener(this);
        updateCurrentDeviceType();
        if (!WelcomeActivity.isDeviceTypeSelected(this)) {
            findViewById(R.id.back_tv).setVisibility(View.GONE);
        }
    }

    private void updateCurrentDeviceType() {
        int deviceType = sharedPreferences.getInt(KEY_DEVICE_TYPE, -1);
        int deviceIndex = sharedPreferences.getInt(KEY_DEVICE_INDEX, -1);
        String tp = deviceType + " " + deviceIndex;
        tvCurrentType.setText(getString(R.string.current_device_type, (sharedPreferences.getString(tp, ""))));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String, String> map = listDeviceName.get(position);
        String index = map.get("index");
        String type = map.get("type");

        currentDeviceType = types[Integer.parseInt(type)];
        currentDeviceIndex = Integer.parseInt(index);
        String tp = currentDeviceType + " " + currentDeviceIndex;
        //tvCurrentType.setText(getString(R.string.current_device_type,(sharedPreferences.getString(tp, ""))));
        showDialog();
    }

    private AlertDialog alertDialog = null;
    private int currentDeviceType = -1;
    private int currentDeviceIndex = -1;

    private void showDialog() {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.choose_devicetype));
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int tpType = sharedPreferences.getInt(KEY_DEVICE_TYPE, -1);
                    int tyIndex = sharedPreferences.getInt(KEY_DEVICE_INDEX, -1);


                    Intent intent = null;
                    if (!WelcomeActivity.isDeviceTypeSelected(DeviceTypeActivity.this)) {
                        intent = new Intent(DeviceTypeActivity.this, UserInfoActivity.class);
                        WelcomeActivity.setDeviceTypeSelected(DeviceTypeActivity.this, true);
                        editor.putInt(KEY_DEVICE_TYPE, currentDeviceType).commit();
                        editor.putInt(KEY_DEVICE_INDEX, currentDeviceIndex).commit();
                        startActivity(intent);
                        finish();
                    } else {
                        WelcomeActivity.setDeviceTypeSelected(DeviceTypeActivity.this, true);
                        if (tpType != currentDeviceType) {
                            editor.putInt(KEY_DEVICE_TYPE, currentDeviceType).commit();
                            editor.putInt(KEY_DEVICE_INDEX, currentDeviceIndex).commit();
                            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
                                //intent = new Intent(DeviceTypeActivity.this, com.isport.fitness.activity.MainActivityGroup.class);
                            } else {
                                intent = new Intent(DeviceTypeActivity.this, MainActivityGroup.class);
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }

                }
            });
            alertDialog = builder.create();
        }
        alertDialog.setMessage(getString(R.string.current_devicetype_selecte, sharedPreferences.getString(currentDeviceType + "", "")));
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }


    @Override
    protected void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !WelcomeActivity.isDeviceTypeSelected(this)) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
