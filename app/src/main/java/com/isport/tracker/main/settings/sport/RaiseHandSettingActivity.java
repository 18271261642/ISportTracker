package com.isport.tracker.main.settings.sport;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
 * @Date 2018/12/7
 * @Fuction
 */

public class RaiseHandSettingActivity extends BaseActivity implements View.OnClickListener, EasySwitchButton
        .OnOpenedListener {

    private TextView tv_right;
    private EasySwitchButton esb_alloff_switch;
    private EasySwitchButton esb_allday_switch;
    private EasySwitchButton esb_sleepmode_switch;
    private boolean mAllday;
    private boolean mOnlySleepmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raisehand_setting);
        initControl();
        initValue();
    }

    private void initControl() {
        tv_right = (TextView) findViewById(R.id.tv_right);

        esb_allday_switch = (EasySwitchButton) findViewById(R.id.esb_allday_switch);//自动存储心率
        esb_allday_switch.setOnCheckChangedListener(this);

        esb_sleepmode_switch = (EasySwitchButton) findViewById(R.id.esb_sleepmode_switch);
        esb_sleepmode_switch.setOnCheckChangedListener(this);
    }

    private void initValue() {
        //默认是all_off
        mAllday = ConfigHelper.getInstance(this).getBoolean(Constants.IS_RAISEHAND_ALLDAY, false);
        mOnlySleepmode = ConfigHelper.getInstance(this).getBoolean(Constants
                                                                           .IS_RAISEHAND_ONLYSLEEPMODE,
                                                                   false);
        esb_allday_switch.setStatus(mAllday);
        esb_sleepmode_switch.setStatus(mOnlySleepmode);
        esb_sleepmode_switch.setVisibility(mAllday ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onChecked(View v, boolean isOpened) {
        int id = v.getId();
        Intent intent = null;
        switch (id) {
            case R.id.esb_allday_switch:
                if (isConnected()) {
                    mAllday = isOpened;
                    esb_sleepmode_switch.setVisibility(isOpened ? View.VISIBLE : View.GONE);
                    // TODO: 2018/12/7 关闭的情况下
                } else {
                    esb_allday_switch.setStatus(!isOpened);
                }
                break;
            case R.id.esb_sleepmode_switch:
                if (isConnected()) {
                    mOnlySleepmode = isOpened;
                } else {
                    esb_sleepmode_switch.setStatus(!isOpened);
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
                        if (mOnlySleepmode) {
                            // TODO: 2018/12/7 只有睡眠模式开启
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ALLDAY, mAllday);
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ONLYSLEEPMODE, true);
                            mCmdController.raiseHandOnlySleepMode(true);
                        } else {
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ALLDAY, mAllday);
                            ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ONLYSLEEPMODE, false);
                            mCmdController.raiseHandAllDayOrNot(mAllday);
                        }
                    } else {
                        ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ALLDAY, mAllday);
                        ConfigHelper.getInstance(this).putBoolean(Constants.IS_RAISEHAND_ONLYSLEEPMODE, false);
                        mCmdController.raiseHandAllDayOrNot(mAllday);
                    }
                    finish();
                }
                break;
            default:
                break;
        }
    }
}
