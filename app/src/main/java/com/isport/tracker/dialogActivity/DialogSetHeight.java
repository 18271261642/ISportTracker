package com.isport.tracker.dialogActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.isport.isportlibrary.entry.UserInfo;
import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.main.settings.sport.SleepActivity;
import com.isport.tracker.util.Utils;
import com.isport.tracker.view.TosAdapterView;
import com.isport.tracker.view.TosGallery;
import com.isport.tracker.view.WheelView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DialogSetHeight extends BaseActivity implements OnClickListener, TosAdapterView.OnItemSelectedListener {
    public final static String EXTRA_TYPE = "extra_type";
    public final static String EXTRA_HEIGHT = "height";
    public final static String EXTRA_WEIGHT = "weight";
    public final static String EXTRA_SLEEP_TARGET = "sleep_target";
    public final static String TYPE_HEIGHT = "height";
    public final static String TYPE_WEIGHT = "weight";
    public final static String TYPE_SLEEP_TARGET = "sleep_target";

    private Button mPickerok = null;
    private Button mPickeresc = null;
    List<Integer> mListOne = new ArrayList<Integer>();
    List<Integer> mListTwo = new ArrayList<Integer>();
    String[] mData;
    String[] mData2;
    String src = "120";
    String src2 = "0";
    private int selectPosition;
    private int selectPosition2;
    private WheelView np = null;
    private WheelView np2 = null;
    private SharedPreferences share;
    private String type;
    private int mNormalColor;
    private int metric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light1);
        UserInfo userInfo = UserInfo.getInstance(this);
        metric = userInfo.getMetricImperial();
        type = getIntent().getStringExtra(DialogSetHeight.EXTRA_TYPE);
        mNormalColor = getResources().getColor(R.color.gray_text);
        mPickerok = (Button) findViewById(R.id.numberPickerok);
        mPickeresc = (Button) findViewById(R.id.numberPickercanle);

        if (type.equals(TYPE_HEIGHT)) {
            setTitle(getString(R.string.user_info_set_height_title));
        } else if (type.equals(TYPE_WEIGHT)) {
            setTitle(getString(R.string.user_info_set_weight_title));
        } else if (type.equals(TYPE_SLEEP_TARGET)) {
            setTitle(getString(R.string.Sleep_Target));
        }

        mPickeresc.setOnClickListener(this);
        mPickerok.setOnClickListener(this);
        np = (WheelView) findViewById(R.id.numberPicker);
        np2 = (WheelView) findViewById(R.id.numberPicker2);
        np.setScrollCycle(true);
        np2.setScrollCycle(true);


        mListOne = (type.equals(TYPE_HEIGHT) ? (metric == 0 ? initList(50, 250) : initList(20, 99)) : (initList(20,
                                                                                                                 300)));
        mListTwo = (type.equals(TYPE_HEIGHT) ? (metric == 0 ? initList(0, 9) : initList(0, 9)) : (initList(0, 9)));
        if (type.equals(TYPE_WEIGHT)) {
//			mListOne = (metric == 0?initList(30,149):initList((int)(300000/4535.9237f),329));
            mListOne = (metric == 0 ? initList(20, 300) : initList((int) (200000 / 4535.9237f), (int) (3000000 /
					4535.9237f)));
        }

        np.setAdapter(new WheelAdapter(np, mListOne, 0));
        np2.setAdapter(new WheelAdapter(np2, mListTwo, 1));


        String strheight = null;
        if (type.equals(TYPE_HEIGHT)) {
            strheight = getIntent().getStringExtra(EXTRA_HEIGHT);
            float height = Float.valueOf(strheight);
            if (metric == 0) {
                if (height > 250.9f)
                    height = 250.9f;
                strheight = String.format(Locale.ENGLISH, "%.1f", Math.round(height * 10) * 0.1);
            } else {
                //int inch = (int)Math.ceil(height*10000/25400);///将厘米转换成inch
                float inch = (Math.round(height * 100000 / 25400)) * 0.1f;///将厘米转换成inch
                if (inch > 99.9f)
                    inch = 99.9f;
                strheight = String.format("%.1f", inch);
                //strheight = (Math.floor(inch))+"."+(inch -Math.floor(inch));
            }
            //strheight = share.getString(TYPE_HEIGHT,"170");
            //mTvTitle.setText(getString(R.string.user_info_set_height_title));
        } else if (type.equals(TYPE_WEIGHT)) {
            strheight = getIntent().getStringExtra(EXTRA_WEIGHT);
            float weight = Float.valueOf(strheight);
            if (metric == 1) {
                float lbs = Math.round((weight * 100000) / 4535.9237f) * 0.1f;
                if (lbs > 661.9f) {
                    lbs = 661.9f;
                }
                strheight = String.format(Locale.ENGLISH, "%.1f", lbs);
                //np2.setVisibility(View.GONE);
            } else {
                weight = Math.round(weight * 10) * 0.1f;
                if (weight > 300.9f) {
                    weight = 300.9f;
                }
                strheight = String.format(Locale.ENGLISH, "%.1f", weight);
            }
            //mTvTitle.setText(R.string.user_info_set_weight_title);
        } else if (type.equals(TYPE_SLEEP_TARGET)) {
            mListOne = initList(0, 23);
            mListTwo = initList(0, 59);
            np2.setAdapter(new WheelAdapter(np2, mListTwo, 1));
            np.setAdapter(new WheelAdapter(np, mListOne, 0));
            int hour = getIntent().getIntExtra(SleepActivity.CONFIG_SLEEP_TARGET_HOUR, 8);
            int minute = getIntent().getIntExtra(SleepActivity.CONFIG_SLEEP_TARGET_MIN, 0);
            String value = hour + " : " + minute;
            strheight = value;
            //mTvTitle.setText(getString(R.string.time_setting));
        }
        String[] strs = strheight.split("\\.");
        if (strheight.contains(".")) {
            strs = strheight.split("\\.");
        } else if (strheight.contains(",")) {
            strs = strheight.split(",");
        } else if (strheight.contains(":")) {
            strs = strheight.split(":");
        }

        if (strheight != null && strs.length == 0) {
            strs = new String[]{strheight};
        }
        if (!type.equals(TYPE_SLEEP_TARGET)) {
            np.setSelection(Integer.valueOf(strs[0]) - (type.equals(TYPE_HEIGHT) ? (metric == 0 ? 50 : 20) : (type
					.equals(TYPE_WEIGHT) ? (metric == 0 ? 20 : (int) (200000 / 4535.9237f)) : 20)));
        } else {
            np.setSelection(Integer.valueOf(strs[0].trim()));
        }

        if (strs.length == 1) {
            np2.setSelection(0);
        } else {
            np2.setSelection(Integer.valueOf(strs[1].trim()));
        }
        np.setOnItemSelectedListener(this);
        np2.setOnItemSelectedListener(this);
    }

    public List<Integer> initList(int start, int end) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = start; i <= end; i++) {
            list.add(i);
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        Intent data = null;
        int id = v.getId();
        switch (id) {
            case R.id.numberPickerok:
                if (type.equals(TYPE_WEIGHT)) {
                    int tp1 = mListOne.get(np.getSelectedItemPosition());
                    int tp2 = mListTwo.get(np2.getSelectedItemPosition());
                    data = new Intent();
                    if (metric == 1) {
                        data.putExtra(EXTRA_WEIGHT, ((Float.valueOf((tp1 + "." + tp2)) * 4535.9237f) / 10000) + "");
                    } else {
                        data.putExtra(EXTRA_WEIGHT, mListOne.get(np.getSelectedItemPosition()) + "." + mListTwo.get
								(np2.getSelectedItemPosition()));
                    }

                    setResult(210, data);
                } else if (type.equals(TYPE_HEIGHT)) {
                    int tp1 = mListOne.get(np.getSelectedItemPosition());
                    int tp2 = mListTwo.get(np2.getSelectedItemPosition());
                    data = new Intent();
                    if (metric == 0) {
                        data.putExtra(EXTRA_HEIGHT, mListOne.get(np.getSelectedItemPosition()) + "." + mListTwo.get
								(np2.getSelectedItemPosition()));
                    } else {
                        data.putExtra(EXTRA_HEIGHT, (Float.valueOf(tp1 + "." + tp2) * 25400 / 10000) + "");
                    }
                    setResult(211, data);
                } else if (type.equals(TYPE_SLEEP_TARGET)) {
                    data = new Intent();
                    data.putExtra(EXTRA_SLEEP_TARGET, mListOne.get(np.getSelectedItemPosition()) + ":" + mListTwo.get
							(np2.getSelectedItemPosition()));
                    setResult(212, data);
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
        ((WheelAdapter) parent.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(TosAdapterView<?> parent) {

    }

    class WheelAdapter extends BaseAdapter {
        List<Integer> list;
        int mHeight = 40;
        int mTp;
        WheelView wheelView;

        public WheelAdapter(WheelView view, List<Integer> list, int type) {
            this.list = list;
            mHeight = (int) Utils.pixelToDp(DialogSetHeight.this, mHeight);
            this.mTp = type;
            wheelView = view;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;

            if (null == convertView) {
                holder = new Holder();
                convertView = new TextView(DialogSetHeight.this);
                convertView.setLayoutParams(new TosGallery.LayoutParams(-1, mHeight));
                holder.tvNum = (TextView) convertView;
                holder.tvNum.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                holder.tvNum.setTextColor(Color.BLACK);
                holder.tvNum.setGravity(Gravity.CENTER);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            String text = String.valueOf(list.get(position));
            if (mTp == 1) {
                if (!type.equals(TYPE_SLEEP_TARGET)) {
                    text = (type.equals(TYPE_HEIGHT) ? text + " " + (metric == 0 ? getString(R.string.ride_cm) : getString(R.string.inch)) : metric == 0 ? getString(R.string.format_kg, text) : getString(R.string.format_lbs, text));
                } else {

                }
            } else if (mTp == 0) {
                if (type.equals(TYPE_HEIGHT)) {
                    text = (metric == 0 ? text + "." : text + ".");
                } else if (type.equals(TYPE_WEIGHT)) {
                    text = (metric == 0 ? text + "." : text + ".");
                }
            }
            if (position == wheelView.getSelectedItemPosition()) {
                holder.tvNum.setTextColor(Color.BLACK);
            } else {
                holder.tvNum.setTextColor(mNormalColor);
            }
            holder.tvNum.setText(text);
            return convertView;
        }

        class Holder {
            TextView tvNum;
        }
    }

}