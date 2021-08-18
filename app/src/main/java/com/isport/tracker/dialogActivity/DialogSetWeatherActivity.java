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

public class DialogSetWeatherActivity extends BaseActivity implements OnClickListener, TosAdapterView.OnItemSelectedListener {

    public final static String EXTRA_TYPE = "extra_type";
    public final static String TYPE_wheather = "wheather";
    public final static String TYPE_qua = "qua";
    public final static String TPYE_temp_high = "temp_high";
    public final static String TPYE_temp_low = "temp_low";
    public final static String TPYE_sport_mode = "sport_mode";
    public final static String TYPE_SLEEP_REMINDER = "sleep_reminder";//睡眠提醒
    public final static String TYPE_LAUNCHER_REMINDER = "KEY_LUNCH_REMINDER";//午休提醒

    private Button mPickerok = null;
    private Button mPickeresc = null;
    int[] mData;
    String[] strData;
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
        type = intent.getStringExtra(DialogSetWeatherActivity.EXTRA_TYPE);

        mPickerok = (Button) findViewById(R.id.numberPickerok);
        mPickeresc = (Button) findViewById(R.id.numberPickercanle);
        //mTvTitle = (TextView) findViewById(R.id.dialog_title);
        if (type.equals(TYPE_wheather)) {
            mData = new int[10];
            for (int i = 0; i < 10; i++) {
                mData[i] = i;
            }
            setTitle("天气情况设置");
        } else if (type.equals(TYPE_qua)) {
            mData = new int[10];
            for (int i = 0; i < 10; i++) {
                mData[i] = i;
            }
            setTitle("空气质量设置");
            //mTvTitle.setText(getString(R.string.set_target_title));
        } else if (type.equals(TPYE_temp_high)) {
            mData = new int[101];
            for (int i = 0; i < 101; i++) {
                mData[i] = i - 50;
            }
            setTitle("最低温度设置");

        } else if (type.equals(TPYE_temp_low)) {
            setTitle("最高温度设置");
            mData = new int[101];
            for (int i = 0; i < 101; i++) {
                mData[i] = i - 50;
            }
        } else if (type.equals(TPYE_sport_mode)) {
            strData = new String[3];
            strData[0] = "室内走";
            strData[1] = "骑行";
            strData[2] = "室外走";
        }

        mPickerok.setOnClickListener(this);
        mPickeresc.setOnClickListener(this);

        np = (WheelView) findViewById(R.id.numberPicker);
        np.setScrollCycle(false);
        np.setAdapter(new NumberAdapter());

        int target;
        if (type.equals(TYPE_wheather)) {
            target = intent.getIntExtra(TYPE_wheather, 10000);
            //np.setSelection(target);
            //np.setSelection(target / 100 - 1);
        } else if (type.equals(TYPE_qua)) {
            target = intent.getIntExtra(TYPE_qua, 0);
            //np.setSelection(target);
        } else if (type.equals(TPYE_temp_high)) {
            target = getIntent().getIntExtra(TYPE_SLEEP_REMINDER, 15);
            np.setSelection(50);
        } else if (type.equals(TPYE_temp_low)) {
            target = getIntent().getIntExtra(TYPE_LAUNCHER_REMINDER, 15);
            np.setSelection(50);
        }
        np.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.numberPickerok:
                Intent intent = new Intent();
                String tp = "0";
                if (type.equals(TPYE_sport_mode)) {
                    tp = np.getSelectedItemPosition() + "";
                } else {
                    tp = mData[np.getSelectedItemPosition()] + "";
                }
                intent.putExtra(type, tp);
                if (type.equals(TYPE_wheather)) {
                    setResult(200, intent);
                } else if (type.equals(TYPE_qua)) {
                    setResult(202, intent);
                } else if (type.equals(TPYE_temp_high)) {
                    setResult(203, intent);
                } else if (type.equals(TPYE_temp_low)) {
                    setResult(204, intent);
                } else if (type.equals(TPYE_sport_mode)) {
                    setResult(205, intent);
                }
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
            mHeight = (int) Utils.pixelToDp(DialogSetWeatherActivity.this, mHeight);
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
                convertView = new TextView(DialogSetWeatherActivity.this);
                convertView.setLayoutParams(new TosGallery.LayoutParams(-1, mHeight));
                holder.tvContent = (TextView) convertView;
                holder.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                holder.tvContent.setTextColor(Color.BLACK);
                holder.tvContent.setGravity(Gravity.CENTER);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            String text = String.valueOf(mData[position]);
            holder.tvContent.setText(text);
            if (position == np.getSelectedItemPosition()) {
                holder.tvContent.setTextColor(Color.BLACK);
            } else {
                holder.tvContent.setTextColor(mNormalColor);
            }
            return convertView;
        }

        class Holder {
            TextView tvContent;
        }

    }
}
