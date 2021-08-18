package com.isport.tracker.main.settings.sport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.ScreenSet;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.main.BaseActivity;

/**
 * Created by Administrator on 2016/4/16.
 */
public class ScreenSetting extends BaseActivity {

    private TextView tv_black , tv_white;
    private CheckBox cb_screen_set;
    private  int  color_status = 0 ;
    public final static String CONFIG_SCREEN = "screen_set";
    public final static String KEY_IS_SCREEN_OPEN = "is_screen_open";
    public final static String KEY_IS_SCREEN_COLOR = "is_screen_color";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
  //  private MyDialog mMyDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);
        sharedPreferences = getSharedPreferences(CONFIG_SCREEN,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        init();
    }

    private void init(){
        TextView re_back = (TextView) findViewById(R.id.return_back);
        re_back.setOnClickListener(myOnClickListener);
        TextView text_save = (TextView) findViewById(R.id.text_save);
        text_save.setOnClickListener(myOnClickListener);
        tv_black = (TextView) findViewById(R.id.tv_black);
        tv_white = (TextView) findViewById(R.id.tv_white);
        cb_screen_set = (CheckBox) findViewById(R.id.switch_screen);
        cb_screen_set.setOnCheckedChangeListener(myOnCheckedChangeListener);
        tv_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_black.setBackgroundResource(R.color.ride_27);
                tv_white.setBackgroundResource(R.color.ride_a8);
                color_status = 1;
            }
        });
        tv_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_black.setBackgroundResource(R.color.ride_a8);
                tv_white.setBackgroundResource(R.color.ride_27);
                color_status = 0;
            }
        });


        boolean isScreenOpen = sharedPreferences.getBoolean(KEY_IS_SCREEN_OPEN, false);
        cb_screen_set.setChecked(isScreenOpen);

        int color =  sharedPreferences.getInt(KEY_IS_SCREEN_COLOR, 0);
        if(color == 0){
            tv_black.setBackgroundResource(R.color.ride_27);
            tv_white.setBackgroundResource(R.color.ride_a8);
        }else if(color == 1){
            tv_black.setBackgroundResource(R.color.ride_a8);
            tv_white.setBackgroundResource(R.color.ride_27);
        }
    }

    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.return_back:
                    finish();
                    break;
                case R.id.text_save:
                    clickSave();
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 保存数据
     */
    private void clickSave() {
        // 自动睡眠开关

        MainService mainService = MainService.getInstance(this);
        // 向手环发送数据
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            editor.putBoolean(KEY_IS_SCREEN_OPEN, cb_screen_set.isChecked()).commit();
            editor.putInt(KEY_IS_SCREEN_COLOR, color_status).commit();
            mainService.setScreen(new ScreenSet(cb_screen_set.isChecked(), (byte) color_status));

        //    showDialog(getResources().getString(R.string.please_wait));
        } else {
            Toast.makeText(ScreenSetting.this, getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
        }
    }
    private CompoundButton.OnCheckedChangeListener myOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()) {
                case R.id.switch_screen:
                    showScreenSettings(isChecked);
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 是否显示屏幕设置
     *
     * @param isShow
     */
    private void showScreenSettings(boolean isShow) {
        cb_screen_set.setChecked(isShow);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
