package com.isport.tracker.main.settings.sport;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.dialogActivity.DialogSetSex;
import com.isport.tracker.dialogActivity.DialogSetWeatherActivity;
import com.isport.tracker.entity.WheatherBean;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.view.EasySwitchButton;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityWeather extends BaseActivity implements View.OnClickListener, EasySwitchButton.OnOpenedListener {

    LinearLayout layout_temp_unit, layout_hight_temp, layout_low_temp, layout_air_qua, layout_wheather;
    TextView tvTodayDetail, tvNextDayDetail, tvAfterDayDetail;
    TextView tvTempUnit, tvAirQua, tvWheather;
    TextView tvHightTemp, tvLowTemp;
    EasySwitchButton dataSwitch;
    TextView tvSave;


    HashMap<Integer, String> tempWeather = new HashMap<>();
    HashMap<Integer, String> quaValue = new HashMap<>();
    HashMap<Integer, String> tempUnit = new HashMap<>();

    RadioButton todayRadio, nextRadio, afterRadio;

    ArrayList<WheatherBean> list;
    WheatherBean wheatherBean1;
    WheatherBean wheatherBean2;
    WheatherBean wheatherBean3;

    WheatherBean currentWheather;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        tempWeather.put(0, "无");
        tempWeather.put(1, "晴天");
        tempWeather.put(2, "多云");
        tempWeather.put(3, "阴天");
        tempWeather.put(4, "雨天");
        tempWeather.put(5, "雷雨");
        tempWeather.put(6, "有雨");
        tempWeather.put(7, "有风");
        tempWeather.put(8, "有雾霾");
        tempWeather.put(9, "沙尘暴");

        quaValue.put(0, "无");
        quaValue.put(1, "优");
        quaValue.put(2, "良");
        quaValue.put(3, "轻度");
        quaValue.put(4, "中度");
        quaValue.put(5, "重度");
        quaValue.put(6, "严重");

        tempUnit.put(0, "摄氏度");
        tempUnit.put(1, "华摄氏度");
        setContentView(R.layout.activity_send_wheather);
        initValue();
        initControl();
        updateUI();
    }

    private void updateUI() {
    }

    private void initControl() {
        dataSwitch.setOnCheckChangedListener(this);
        todayRadio.setOnClickListener(this);
        nextRadio.setOnClickListener(this);
        afterRadio.setOnClickListener(this);

        list = new ArrayList<>();
        wheatherBean1 = new WheatherBean();
        wheatherBean2 = new WheatherBean();
        wheatherBean3 = new WheatherBean();
        list.add(wheatherBean1);
        list.add(wheatherBean2);
        list.add(wheatherBean3);
        currentWheather = wheatherBean1;

        setShowValue(wheatherBean1);
        setDetailValue();
        layout_wheather.setOnClickListener(this);
        layout_air_qua.setOnClickListener(this);
        layout_low_temp.setOnClickListener(this);
        layout_hight_temp.setOnClickListener(this);
        layout_temp_unit.setOnClickListener(this);

    }

    private void initValue() {

        dataSwitch = findViewById(R.id.esb_data_switch);


        layout_temp_unit = findViewById(R.id.layout_temp_unit);
        layout_hight_temp = findViewById(R.id.layout_hight_temp);
        layout_low_temp = findViewById(R.id.layout_low_temp);
        layout_air_qua = findViewById(R.id.layout_air_qua);
        layout_wheather = findViewById(R.id.layout_wheather);

        tvAirQua = findViewById(R.id.tv_airqua_value);
        tvTempUnit = findViewById(R.id.tv_temp_unit_value);
        tvWheather = findViewById(R.id.tv_wheather_value);
        tvHightTemp = findViewById(R.id.tv_high_temp_value);
        tvLowTemp = findViewById(R.id.tv_low_temp_value);

        tvTodayDetail = findViewById(R.id.tv_tody_detail);
        tvNextDayDetail = findViewById(R.id.tv_next_detail);
        tvAfterDayDetail = findViewById(R.id.tv_after_detail);

        todayRadio = findViewById(R.id.tv_today);
        nextRadio = findViewById(R.id.tv_next);
        afterRadio = findViewById(R.id.tv_after);


    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.linear_back:
                finish();
                break;
            case R.id.tv_save:
                if (isConnected()) {
                    MainService mainService = MainService.getInstance(this);
                    if (mainService != null) {
                        mainService.setWeatherCmd(dataSwitch.getStatus(), wheatherBean1.wheather, wheatherBean1.tempUnit, wheatherBean1.highTemp, wheatherBean1.lowTemp, wheatherBean1.airQua, wheatherBean2.wheather, wheatherBean2.tempUnit, wheatherBean2.highTemp, wheatherBean2.lowTemp, wheatherBean2.airQua, wheatherBean3.wheather, wheatherBean3.tempUnit, wheatherBean3.highTemp, wheatherBean3.lowTemp, wheatherBean3.airQua);
                    }
                    //save();
                    Toast.makeText(ActivityWeather.this, "温度设置成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityWeather.this, "请连接设备", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_today:
                currentWheather = wheatherBean1;
                setShowValue(currentWheather);
                break;
            case R.id.tv_next:
                currentWheather = wheatherBean2;
                setShowValue(currentWheather);
                break;
            case R.id.tv_after:
                currentWheather = wheatherBean3;
                setShowValue(currentWheather);
                break;
            case R.id.layout_temp_unit:
                intent = new Intent(this, DialogSetSex.class);
                intent.putExtra(DialogSetSex.EXTRA_TYPE, DialogSetSex.TYPE_TEMP_NUNIT);
                intent.putExtra(DialogSetSex.EXTRA_IS_LEFTHAND, currentWheather.tempUnit == 0);
                startActivityForResult(intent, 201);
                break;
            case R.id.layout_hight_temp:
                intent = new Intent(this, DialogSetWeatherActivity.class);
                intent.putExtra(DialogSetWeatherActivity.EXTRA_TYPE, DialogSetWeatherActivity.TPYE_temp_high);
                startActivityForResult(intent, 203);
                break;
            case R.id.layout_low_temp:
                intent = new Intent(this, DialogSetWeatherActivity.class);
                intent.putExtra(DialogSetWeatherActivity.EXTRA_TYPE, DialogSetWeatherActivity.TPYE_temp_low);
                startActivityForResult(intent, 204);
                break;
            case R.id.layout_air_qua:
                intent = new Intent(this, DialogSetWeatherActivity.class);
                intent.putExtra(DialogSetWeatherActivity.EXTRA_TYPE, DialogSetWeatherActivity.TYPE_qua);
                startActivityForResult(intent, 202);
                break;
            case R.id.layout_wheather:
                intent = new Intent(this, DialogSetWeatherActivity.class);
                intent.putExtra(DialogSetWeatherActivity.EXTRA_TYPE, DialogSetWeatherActivity.TYPE_wheather);
                startActivityForResult(intent, 200);
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        if (data != null && resultCode == 201) {
            boolean tempUnit = data.getBooleanExtra(DialogSetSex.EXTRA_IS_TEMP_UNIT, false);
            if (tempUnit) {
                currentWheather.tempUnit = 1;
            } else {
                currentWheather.tempUnit = 0;
            }

            setShowValue(currentWheather);
            setDetailValue();

        } else if (data != null && resultCode == 200) {
            String value = data.getStringExtra(DialogSetWeatherActivity.TYPE_wheather);
            currentWheather.wheather = Integer.parseInt(value);
            setShowValue(currentWheather);
            setDetailValue();
        } else if (data != null && resultCode == 202) {
            String value = data.getStringExtra(DialogSetWeatherActivity.TYPE_qua);
            currentWheather.airQua = Integer.parseInt(value);
            setShowValue(currentWheather);
            setDetailValue();
        } else if (data != null && resultCode == 203) {
            String value = data.getStringExtra(DialogSetWeatherActivity.TPYE_temp_high);
            currentWheather.highTemp = Integer.parseInt(value);
            setShowValue(currentWheather);
            setDetailValue();
        } else if (data != null && resultCode == 204) {
            String value = data.getStringExtra(DialogSetWeatherActivity.TPYE_temp_low);
            currentWheather.lowTemp = Integer.parseInt(value);
            setShowValue(currentWheather);
            setDetailValue();
        }
    }


    public void setShowValue(WheatherBean wheatherBean) {
        tvWheather.setText(wheatherBean.wheather + "(" + tempWeather.get(wheatherBean.wheather) + ")");
        tvTempUnit.setText(wheatherBean.tempUnit + "(" + tempUnit.get(wheatherBean.tempUnit) + ")");
        tvHightTemp.setText(wheatherBean.highTemp + "");
        tvLowTemp.setText(wheatherBean.lowTemp + "");
        tvAirQua.setText(wheatherBean.airQua + "(" + quaValue.get(wheatherBean.airQua) + ")");
    }

    public void setDetailValue() {
        String detailToday = String.format(getResources().getString(R.string.today_detail), wheatherBean1.wheather + "(" + tempWeather.get(wheatherBean1.wheather) + ")", wheatherBean1.tempUnit + "(" + tempUnit.get(wheatherBean1.tempUnit) + ")", wheatherBean1.highTemp + "", wheatherBean1.lowTemp + "", wheatherBean1.airQua + "(" + quaValue.get(wheatherBean1.airQua) + ")");
        tvTodayDetail.setText(detailToday);
        String detailNext = String.format(getResources().getString(R.string.next_detail), wheatherBean2.wheather + "(" + tempWeather.get(wheatherBean2.wheather) + ")", wheatherBean2.tempUnit + "(" + tempUnit.get(wheatherBean1.tempUnit) + ")", wheatherBean2.highTemp + "", wheatherBean2.lowTemp + "", wheatherBean2.airQua + "(" + quaValue.get(wheatherBean2.airQua) + ")");
        tvNextDayDetail.setText(detailNext);
        String detailAfter = String.format(getResources().getString(R.string.after_detail), wheatherBean3.wheather + "(" + tempWeather.get(wheatherBean1.wheather) + ")", wheatherBean3.tempUnit + "(" + tempUnit.get(wheatherBean3.tempUnit) + ")", wheatherBean3.highTemp + "", wheatherBean3.lowTemp + "", wheatherBean3.airQua + "(" + quaValue.get(wheatherBean3.airQua) + ")");
        tvAfterDayDetail.setText(detailAfter);
    }

    @Override
    public void onChecked(View v, boolean isOpened) {

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
