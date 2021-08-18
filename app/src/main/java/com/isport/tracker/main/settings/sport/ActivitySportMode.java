package com.isport.tracker.main.settings.sport;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.services.BleService;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetSex;
import com.isport.tracker.dialogActivity.DialogSetStrWeatherActivity;
import com.isport.tracker.dialogActivity.DialogSetWeatherActivity;
import com.isport.tracker.entity.WheatherBean;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.view.EasySwitchButton;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivitySportMode extends BaseActivity implements View.OnClickListener, EasySwitchButton.OnOpenedListener {

    EasySwitchButton esb_out_switch;
    LinearLayout layoutSportMode;
    TextView tvSportMode;
    TextView tvSave;

    HashMap<Integer, String> maps = new HashMap<>();


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        maps.put(0, "室内走");
        maps.put(1, "骑行");
        maps.put(2, "室外走");
        setContentView(R.layout.activity_sport_mode);
        initValue();
        initControl();
        updateUI();
    }

    private void updateUI() {
    }

    private void initControl() {
        esb_out_switch.setOnCheckChangedListener(this);


    }

    private void initValue() {

        esb_out_switch = findViewById(R.id.esb_out_switch);
        layoutSportMode = findViewById(R.id.layout_sport_mode);
        tvSportMode = findViewById(R.id.tv_sport_value);
        esb_out_switch.setStatus(ConfigHelper.getInstance(this).getBoolean(Constants.IS_SPORT_MODE, false));
        tvSportMode.setText(maps.get(ConfigHelper.getInstance(this).getInt(Constants.SPORT_MODE_VALUE, 0)));


    }

    int type;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.linear_back:
                finish();
                break;
            case R.id.tv_save:
                send(type, esb_out_switch.getStatus());
                break;
            case R.id.layout_sport_mode:
                Intent intent;
                intent = new Intent(this, DialogSetStrWeatherActivity.class);
                intent.putExtra(DialogSetStrWeatherActivity.EXTRA_TYPE, DialogSetStrWeatherActivity.TPYE_sport_mode);
                intent.putExtra(DialogSetStrWeatherActivity.TYPE_CURRENT_POSITION, type);
                startActivityForResult(intent, 205);
                break;

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        if (data != null && resultCode == 205) {
            String value = data.getStringExtra(DialogSetWeatherActivity.TPYE_sport_mode);
            type = Integer.valueOf(value);
            if (maps.containsKey(type)) {
                tvSportMode.setText(maps.get(type));
            }
        }
    }


    @Override
    public void onChecked(View v, boolean isOpened) {
        if (v == esb_out_switch) {
        }
    }

    public void send(int type, boolean enable) {
        if (isConnected()) {
            MainService mainService = MainService.getInstance(this);
            if (mainService != null) {
                mainService.setOpenSportMode(type, enable);
            }
            //save();
            ConfigHelper.getInstance(this).putBoolean(Constants.IS_SPORT_MODE, enable);
            ConfigHelper.getInstance(this).putInt(Constants.SPORT_MODE_VALUE, type);
            Toast.makeText(ActivitySportMode.this, "运动模式开启成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ActivitySportMode.this, "请连接设备", Toast.LENGTH_SHORT).show();
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
}
