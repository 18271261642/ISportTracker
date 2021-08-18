package com.isport.tracker.dialogActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.view.TosGallery;
import com.isport.tracker.view.WheelView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/14.
 * W316  蓝牙定时开关时间选择
 *
 */

public class DialogSetTimingAlarm extends BaseActivity {

    private Button mPickerok = null;
    private Button mPickeresc = null;
    private WheelView numberPicker;
    private List<String> stringList;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light2);
        /*int divierId = this.getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = findViewById(divierId);
        divider.setBackgroundColor(Color.parseColor("#754c50"));*/
        setTitle(getString(R.string.time));
        stringList = new ArrayList<>();
        for (int i=0;i<144;i++){
            String tp = ((i/6)<10?"0"+(i/6):(i/6)+"")+":"+((i*10)%60<10?"0"+((i*10)%60):((i*10)%60)+"");
            stringList.add(tp);
        }
        stringList.add(getString(R.string.close));

        mPickerok = (Button) findViewById(R.id.numberPickerok);
        mPickeresc = (Button) findViewById(R.id.numberPickercanle);

        mPickerok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("index",index);
                intent.putExtra("timeindex",numberPicker.getSelectedItemPosition());
                intent.putExtra("time",stringList.get(numberPicker.getSelectedItemPosition()));
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        mPickeresc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        int str = getIntent().getIntExtra("time",144);
        index = getIntent().getIntExtra("index",0);
        numberPicker = (WheelView) findViewById(R.id.numberPicker);
        numberPicker.setScrollCycle(true);
        numberPicker.setAdapter(new DialogSetTimingAlarm.NumberAdapter());
        numberPicker.setSelection(str);
        //for(int i=0;i<stringList.size();i++){
            /*if(str <144){
                numberPicker.setSelection(str);
            }else if(str == 144){
                numberPicker.setSelection(i);
            }*/
        //}
    }

    private class NumberAdapter extends BaseAdapter {
        int mHeight = 50;

        public NumberAdapter() {
            mHeight = DeviceConfiger.dp2px( mHeight);
        }

        @Override
        public int getCount() {
            return stringList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView txtView = null;

            if (null == convertView) {
                convertView = new TextView(DialogSetTimingAlarm.this);
                convertView.setLayoutParams(new TosGallery.LayoutParams(-1, mHeight));

                txtView = (TextView) convertView;
                txtView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                txtView.setTextColor(Color.BLACK);
                txtView.setGravity(Gravity.CENTER);
            }

            String text = String.valueOf(stringList.get(position));
            if (null == txtView) {
                txtView = (TextView) convertView;
            }

            txtView.setText(text);

            return convertView;
        }
    }
}
