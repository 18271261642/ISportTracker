package com.isport.tracker.dialogActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;


/**
 * Created by Administrator on 2017/2/13.
 * 设置久坐提醒时间
 */

public class DialogSetSedentaryTime extends BaseActivity implements View.OnClickListener {

    private Button mPickerok = null;
    private Button mPickeresc = null;
    private TextView tvUinit = null;
    private Boolean is_from_foot;
    int src = 0;

    NumberPicker np = null;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_light);

        mPickerok = (Button) findViewById(R.id.numberPickerok);
        mPickeresc = (Button) findViewById(R.id.numberPickercanle);

        mPickeresc.setOnClickListener(this);
        mPickerok.setOnClickListener(this);
        np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(999);
        np.setFocusable(true);
        np.setFocusableInTouchMode(true);
        np.setMinimumWidth(100);
        //np.setValue(getIntent().getIntExtra(SystemConfig.KEY_REMINDER_TIME,14));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.numberPickerok:
                Intent intent = new Intent();
                //intent.putExtra(SystemConfig.KEY_REMINDER_TIME,Integer.valueOf(np.getValue())-1);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.numberPickercanle:
                finish();
                break;
        }
    }
}
