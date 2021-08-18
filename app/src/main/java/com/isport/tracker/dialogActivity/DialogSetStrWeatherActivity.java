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

public class DialogSetStrWeatherActivity extends BaseActivity implements OnClickListener, TosAdapterView.OnItemSelectedListener {

    public final static String EXTRA_TYPE = "extra_type";
    public final static String TPYE_sport_mode = "sport_mode";
    public final static String TYPE_CURRENT_POSITION = "current_position";
    public int currentPostion;

    private Button mPickerok = null;
    private Button mPickeresc = null;
    String[] mData;
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
        type = intent.getStringExtra(DialogSetStrWeatherActivity.EXTRA_TYPE);
        currentPostion = intent.getIntExtra(DialogSetStrWeatherActivity.TYPE_CURRENT_POSITION, 0);
        mPickerok = (Button) findViewById(R.id.numberPickerok);
        mPickeresc = (Button) findViewById(R.id.numberPickercanle);
        //mTvTitle = (TextView) findViewById(R.id.dialog_title);


        setTitle("运动模式");
        mData = new String[3];
        mData[0] = "室内走";
        mData[1] = "骑行";
        mData[2] = "室外走";

        mPickerok.setOnClickListener(this);
        mPickeresc.setOnClickListener(this);

        np = (WheelView) findViewById(R.id.numberPicker);
        np.setScrollCycle(false);
        np.setAdapter(new NumberAdapter());
        np.setSelection(currentPostion);

        int target;
        np.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.numberPickerok:
                Intent intent = new Intent();
                String tp = "0";
                tp = np.getSelectedItemPosition() + "";
                intent.putExtra(type, tp);
                setResult(205, intent);
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
            mHeight = (int) Utils.pixelToDp(DialogSetStrWeatherActivity.this, mHeight);
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
                convertView = new TextView(DialogSetStrWeatherActivity.this);
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
