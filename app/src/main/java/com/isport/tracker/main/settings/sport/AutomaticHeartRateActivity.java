package com.isport.tracker.main.settings.sport;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.view.EasySwitchButton;

/**
 * @Author xiongxing
 * @Date 2019/2/28
 * @Fuction
 */

public class AutomaticHeartRateActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton
        .OnOpenedListener, CompoundButton.OnCheckedChangeListener {

    private TextView         tv_right;
    private EasySwitchButton esb_allday_switch;
    private boolean          mAllday;
    private int              mTime;
    private LinearLayout     ll_checkbox;
    private CheckBox         checkbox_15;
    private CheckBox         checkbox_30;
    private CheckBox         checkbox_45;
    private CheckBox         checkbox_60;
    private boolean          mM5minType;
    private CheckBox         checkbox_5;
    private CheckBox         checkbox_10;
    private CheckBox         checkbox_20;
    private CheckBox         checkbox_25;
    private CheckBox         checkbox_35;
    private CheckBox         checkbox_40;
    private CheckBox         checkbox_50;
    private CheckBox         checkbox_55;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automaticheartrate_setting);
        initData();
        initControl();
        initValue();
    }

    private void initData() {
        mM5minType = getIntent().getBooleanExtra("m5minType", false);
    }

    private void initControl() {
        tv_right = (TextView) findViewById(R.id.tv_right);

        esb_allday_switch = (EasySwitchButton) findViewById(R.id.esb_allday_switch);//自动存储心率
        esb_allday_switch.setOnCheckChangedListener(this);

        ll_checkbox = (LinearLayout) findViewById(R.id.ll_checkbox);

        checkbox_15 = (CheckBox) findViewById(R.id.checkbox_15);
        checkbox_30 = (CheckBox) findViewById(R.id.checkbox_30);
        checkbox_45 = (CheckBox) findViewById(R.id.checkbox_45);
        checkbox_60 = (CheckBox) findViewById(R.id.checkbox_60);

        checkbox_5 = (CheckBox) findViewById(R.id.checkbox_5);
        checkbox_10 = (CheckBox) findViewById(R.id.checkbox_10);
        checkbox_20 = (CheckBox) findViewById(R.id.checkbox_20);
        checkbox_25 = (CheckBox) findViewById(R.id.checkbox_25);
        checkbox_35 = (CheckBox) findViewById(R.id.checkbox_35);
        checkbox_40 = (CheckBox) findViewById(R.id.checkbox_40);
        checkbox_50 = (CheckBox) findViewById(R.id.checkbox_50);
        checkbox_55 = (CheckBox) findViewById(R.id.checkbox_55);

        if (mM5minType) {
            checkbox_5.setVisibility(View.VISIBLE);
            checkbox_10.setVisibility(View.VISIBLE);
            checkbox_20.setVisibility(View.VISIBLE);
            checkbox_25.setVisibility(View.VISIBLE);
            checkbox_35.setVisibility(View.VISIBLE);
            checkbox_40.setVisibility(View.VISIBLE);
            checkbox_50.setVisibility(View.VISIBLE);
            checkbox_55.setVisibility(View.VISIBLE);
        } else {
            checkbox_5.setVisibility(View.GONE);
            checkbox_10.setVisibility(View.GONE);
            checkbox_20.setVisibility(View.GONE);
            checkbox_25.setVisibility(View.GONE);
            checkbox_35.setVisibility(View.GONE);
            checkbox_40.setVisibility(View.GONE);
            checkbox_50.setVisibility(View.GONE);
            checkbox_55.setVisibility(View.GONE);
        }
    }

    private void initValue() {
        //默认是all_off
        mAllday = ConfigHelper.getInstance(this).getBoolean(Constants.IS_AUTOMATICHEARTRATE, false);
        mTime = ConfigHelper.getInstance(this).getInt(Constants.IS_AUTOMATICHEARTRATE_TIME, 15);
        esb_allday_switch.setStatus(mAllday);
        if (mAllday) {
            ll_checkbox.setVisibility(View.VISIBLE);
            setCheckBox(mTime);
        } else {
            ll_checkbox.setVisibility(View.GONE);
            setCheckBox(mTime);
        }
        checkbox_15.setOnCheckedChangeListener(this);
        checkbox_30.setOnCheckedChangeListener(this);
        checkbox_45.setOnCheckedChangeListener(this);
        checkbox_60.setOnCheckedChangeListener(this);

        checkbox_5.setOnCheckedChangeListener(this);
        checkbox_10.setOnCheckedChangeListener(this);
        checkbox_20.setOnCheckedChangeListener(this);
        checkbox_25.setOnCheckedChangeListener(this);
        checkbox_35.setOnCheckedChangeListener(this);
        checkbox_40.setOnCheckedChangeListener(this);
        checkbox_50.setOnCheckedChangeListener(this);
        checkbox_55.setOnCheckedChangeListener(this);
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        int id = v.getId();
        Intent intent = null;
        switch (id) {
            case R.id.esb_allday_switch:
                if (isConnected()) {
                    mAllday = isOpened;
                    ll_checkbox.setVisibility(isOpened ? View.VISIBLE : View.GONE);
                    // TODO: 2018/12/7 关闭的情况下
                } else {
                    esb_allday_switch.setStatus(!isOpened);
                }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_tv:
                finish();
                break;
            case R.id.tv_right:
                if (isConnected()) {
                    MainService mainService = MainService.getInstance(this);
                    CmdController mCmdController = (CmdController) mainService.getCurrentController();
                    if (mAllday) {
                        // TODO: 2018/12/7 全天开启
                        ConfigHelper.getInstance(this).putBoolean(Constants.IS_AUTOMATICHEARTRATE, mAllday);
                        ConfigHelper.getInstance(this).putInt(Constants.IS_AUTOMATICHEARTRATE_TIME, getCheckBoxPistionValue());
                        mCmdController.setAutomaticHeartRateAndTime(mAllday, getCheckBoxValue());
                    } else {
                        ConfigHelper.getInstance(this).putBoolean(Constants.IS_AUTOMATICHEARTRATE, mAllday);
                        ConfigHelper.getInstance(this).putInt(Constants.IS_AUTOMATICHEARTRATE_TIME, 15);
                        mCmdController.setAutomaticHeartRateAndTime(mAllday, 15);
                    }
                    finish();
                }
                break;
            default:
                break;
        }
    }

    public int getCheckBoxValue() {
        if (checkbox_5.isChecked()) {
            return 5;
        }
        if (checkbox_10.isChecked()) {
            return 10;
        }
        if (checkbox_15.isChecked()) {
            return 15;
        }
        if (checkbox_20.isChecked()) {
            return 20;
        }
        if (checkbox_25.isChecked()) {
            return 25;
        }
        if (checkbox_30.isChecked()) {
            return 30;
        }
        if (checkbox_35.isChecked()) {
            return 35;
        }
        if (checkbox_40.isChecked()) {
            return 40;
        }
        if (checkbox_45.isChecked()) {
            return 45;
        }
        if (checkbox_50.isChecked()) {
            return 50;
        }
        if (checkbox_55.isChecked()) {
            return 55;
        }
        if (checkbox_60.isChecked()) {
            return 60;
        }
        return 15;
    }

    public int getCheckBoxPistionValue() {
        if (checkbox_5.isChecked()) {
            return 5;
        }
        if (checkbox_10.isChecked()) {
            return 10;
        }
        if (checkbox_15.isChecked()) {
            return 15;
        }
        if (checkbox_20.isChecked()) {
            return 20;
        }
        if (checkbox_25.isChecked()) {
            return 25;
        }
        if (checkbox_30.isChecked()) {
            return 30;
        }
        if (checkbox_35.isChecked()) {
            return 35;
        }
        if (checkbox_40.isChecked()) {
            return 40;
        }
        if (checkbox_45.isChecked()) {
            return 45;
        }
        if (checkbox_50.isChecked()) {
            return 50;
        }
        if (checkbox_55.isChecked()) {
            return 55;
        }
        if (checkbox_60.isChecked()) {
            return 60;
        }
        return 15;
    }

    public void setCheckBox(int checkBox) {
        checkbox_5.setChecked(false);
        checkbox_10.setChecked(false);
        checkbox_15.setChecked(false);
        checkbox_20.setChecked(false);
        checkbox_25.setChecked(false);
        checkbox_30.setChecked(false);
        checkbox_35.setChecked(false);
        checkbox_40.setChecked(false);
        checkbox_45.setChecked(false);
        checkbox_50.setChecked(false);
        checkbox_55.setChecked(false);
        checkbox_60.setChecked(false);
        switch (checkBox) {
            case 5:
                checkbox_5.setChecked(true);

                break;
            case 10:
                checkbox_10.setChecked(true);

                break;
            case 15:
                checkbox_15.setChecked(true);

                break;
            case 20:
                checkbox_20.setChecked(true);

                break;
            case 25:
                checkbox_25.setChecked(true);

                break;
            case 30:
                checkbox_30.setChecked(true);

                break;
            case 35:
                checkbox_35.setChecked(true);

                break;
            case 40:
                checkbox_40.setChecked(true);

                break;
            case 45:
                checkbox_45.setChecked(true);

                break;
            case 50:
                checkbox_50.setChecked(true);

                break;
            case 55:
                checkbox_55.setChecked(true);

                break;
            case 60:
                checkbox_60.setChecked(true);

                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            switch (compoundButton.getId()) {

                case R.id.checkbox_5:
                    setCheckBox(5);
                    break;
                case R.id.checkbox_10:
                    setCheckBox(10);
                    break;
                case R.id.checkbox_15:
                    setCheckBox(15);
                    break;
                case R.id.checkbox_20:
                    setCheckBox(20);
                    break;
                case R.id.checkbox_25:
                    setCheckBox(25);
                    break;
                case R.id.checkbox_30:
                    setCheckBox(30);
                    break;
                case R.id.checkbox_35:
                    setCheckBox(35);
                    break;
                case R.id.checkbox_40:
                    setCheckBox(40);
                    break;
                case R.id.checkbox_45:
                    setCheckBox(45);
                    break;
                case R.id.checkbox_50:
                    setCheckBox(50);
                    break;
                case R.id.checkbox_55:
                    setCheckBox(55);
                    break;
                case R.id.checkbox_60:
                    setCheckBox(60);
                    break;
            }
        }

    }
}
