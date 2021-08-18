package com.isport.tracker.dialogActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.util.Utils;
import com.isport.tracker.view.TosAdapterView;
import com.isport.tracker.view.TosGallery;
import com.isport.tracker.view.WheelView;

public class DialogSetTargetActivity extends BaseActivity implements OnClickListener, TosAdapterView.OnItemSelectedListener {

    public final static String EXTRA_TYPE = "extra_type";
    public final static String TYPE_TARGET = "target";
    public final static String TYPE_INDEX = "index";
    public final static String TYPE_SLEEP_REMINDER = "sleep_reminder";//睡眠提醒
    public final static String TYPE_LAUNCHER_REMINDER = "KEY_LUNCH_REMINDER";//午休提醒

    private Button mPickerok = null;
    private Button mPickeresc = null;
    int[] mData;
    WheelView np = null;
    String type;
    TextView mTvTitle;
    int mNormalColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light2);


        mNormalColor = getResources().getColor(R.color.gray_text);
        Intent intent = getIntent();
        type = intent.getStringExtra(DialogSetTargetActivity.EXTRA_TYPE);

        mPickerok = (Button) findViewById(R.id.numberPickerok);
        mPickeresc = (Button) findViewById(R.id.numberPickercanle);
        //mTvTitle = (TextView) findViewById(R.id.dialog_title);
        if (type.equals(TYPE_TARGET)) {
            mData = new int[300];
            for (int i = 1; i <= 300; i++) {
                mData[i - 1] = i * 100;
            }
            setTitle(getString(R.string.target_title));
        } else if (type.equals(TYPE_INDEX)) {
            mData = new int[49];
            for (int i = 0; i < 49; i++) {
                mData[i] = i;
            }
            setTitle(getString(R.string.index));
            //mTvTitle.setText(getString(R.string.set_target_title));
        } else if (type.equals(TYPE_SLEEP_REMINDER) || type.equals(TYPE_LAUNCHER_REMINDER)) {
            mData = new int[60];
            for (int i = 0; i < 60; i++) {
                mData[i] = i + 1;
            }
            setTitle(getString(R.string.set_time));
            //mTvTitle.setText(getString(R.string.minutes_setting));
        }


        mPickerok.setOnClickListener(this);
        mPickeresc.setOnClickListener(this);

        np = (WheelView) findViewById(R.id.numberPicker);
        np.setScrollCycle(true);
        np.setAdapter(new NumberAdapter());

        int target;
        if (type.equals(TYPE_TARGET)) {
            target = intent.getIntExtra(TYPE_TARGET, 10000);
            np.setSelection(target / 100 - 1);
        } else if (type.equals(TYPE_INDEX)) {
            target = intent.getIntExtra(TYPE_INDEX, 0);
            np.setSelection(target-1);
        } else if (type.equals(TYPE_SLEEP_REMINDER)) {
            target = getIntent().getIntExtra(TYPE_SLEEP_REMINDER, 15);
            np.setSelection(target - 1);
        } else if (type.equals(TYPE_LAUNCHER_REMINDER)) {
            target = getIntent().getIntExtra(TYPE_LAUNCHER_REMINDER, 15);
            np.setSelection(target-1);
        }
        np.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.numberPickerok:
                Intent intent = new Intent();
                String tp = mData[np.getSelectedItemPosition()] + "";
                intent.putExtra(type, tp);
                setResult(200, intent);
                finish();
                break;
            case R.id.numberPickercanle:
                finish();
                break;
        }
    }

    @Override
    public void onItemSelected(TosAdapterView<?> parent, View view, int position, long id) {
        ((NumberAdapter) parent.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(TosAdapterView<?> parent) {

    }

    /**
     * 瀵嗙爜鏍廰dapter
     *
     * @author zr
     */
    private class NumberAdapter extends BaseAdapter {
        int mHeight = 40;

        public NumberAdapter() {
            mHeight = (int) Utils.pixelToDp(DialogSetTargetActivity.this, mHeight);
        }

        @Override
        public int getCount() {
            return (null != mData) ? mData.length : 0;
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
            Holder holder = null;

            if (null == convertView) {
                holder = new Holder();
                convertView = new TextView(DialogSetTargetActivity.this);
                convertView.setLayoutParams(new TosGallery.LayoutParams(-1, mHeight));
                holder.tvContent = (TextView) convertView;
                holder.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                holder.tvContent.setTextColor(Color.BLACK);
                holder.tvContent.setGravity(Gravity.CENTER);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            if (position >= 0 && position < mData.length) {
                String text = String.valueOf(mData[position]);
                holder.tvContent.setText(text);
                if (position == np.getSelectedItemPosition()) {
                    holder.tvContent.setTextColor(Color.BLACK);
                } else {
                    holder.tvContent.setTextColor(mNormalColor);
                }
            }
            return convertView;
        }

        class Holder {
            TextView tvContent;
        }

    }
}
