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
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.TosAdapterView;
import com.isport.tracker.view.TosGallery;
import com.isport.tracker.view.WheelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/7/8.
 */
public class DialogSetTime extends BaseActivity implements View.OnClickListener,TosAdapterView.OnItemSelectedListener{
    public final static String EXTRA_IS_24_HOUR = "is24hour";
    public final static String EXTRA_IS_AM = "isAm";
    public final static String EXTRA_DATE = "date";
    public final static String EXTRA_FORMAT = "format";
    public final static String EXTRA_INDEX = "index";
    private final static int TYPE_HOUR = 0x001;
    private final static int TYPE_MINU = 0x010;
    private final static int TYPE_APM = 0x100;

    private WheelView mWheelHour;//小时
    private WheelView mWheelMinute;//
    private WheelView mWheelApm;///显示是上午还是下午
    private TextView mTvTitle;
    private Button mBtnOk;
    private Button mBtnCancel;
    private int mHour;
    private int mMinu;
    private boolean isAm;///是否是上午
    private boolean is24Hour = true;//是否是24小时制


    private List<Integer> mListHour = new ArrayList<Integer>();
    private List<Integer> mListMinu = new ArrayList<Integer>();
    private List<String> mListApm = new ArrayList<String>();
    private int mNormalColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_set_time);
        is24Hour = getIntent().getBooleanExtra(EXTRA_IS_24_HOUR,true);
        isAm = getIntent().getBooleanExtra(EXTRA_IS_AM,true);
        String format = getIntent().getStringExtra(EXTRA_FORMAT);
        String date = getIntent().getStringExtra(EXTRA_DATE);
        parserTime(date,format,isAm);
        mNormalColor = getResources().getColor(R.color.gray_text);
        initValue();
        initControl();
    }

    public void parserTime(String time,String fmt,boolean isAm){
        try {
            SimpleDateFormat format = new SimpleDateFormat(fmt);
            Date date = format.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinu = calendar.get(Calendar.MINUTE);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }


    public void initValue(){

        for (int i=0;i<24;i++){
            mListHour.add(is24Hour?i:(i==0?12:(i<=12?i:i-12)));
        }
        for (int i=0;i<60;i++){
            mListMinu.add(i);
        }
        mListApm.add(getString(R.string.AM));
        mListApm.add(getString(R.string.PM));
    }

    public void initControl(){
        mTvTitle = (TextView) findViewById(R.id.dialog_title);
        mTvTitle.setText(getString(R.string.set_time));
        mWheelHour = (WheelView) findViewById(R.id.wheel_hour);
        mWheelMinute = (WheelView) findViewById(R.id.wheel_minute);
        mWheelApm = (WheelView) findViewById(R.id.wheel_am_pm);
        mBtnCancel = (Button) findViewById(R.id.numberPickercanle);
        mBtnOk = (Button) findViewById(R.id.numberPickerok);
        mBtnCancel.setOnClickListener(this);
        mBtnOk.setOnClickListener(this);

        mWheelMinute.setScrollCycle(true);
        mWheelHour.setScrollCycle(true);
        mWheelApm.setScrollCycle(false);

        if(is24Hour){///是24小时制则隐藏AM PM
            mWheelApm.setVisibility(View.GONE);
        }
        mWheelApm.setAdapter(new WheelAdapter(mWheelApm,mListApm,TYPE_APM));
        mWheelHour.setAdapter(new WheelAdapter(mWheelHour,mListHour,TYPE_HOUR));
        mWheelMinute.setAdapter(new WheelAdapter(mWheelMinute,mListMinu,TYPE_MINU));
        mWheelApm.setOnItemSelectedListener(this);
        mWheelHour.setOnItemSelectedListener(this);
        mWheelMinute.setOnItemSelectedListener(this);
        mWheelHour.setSelection(mHour);
        mWheelApm.setSelection(isAm?0:1);
        mWheelMinute.setSelection(mMinu);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            Intent intent = null;
            switch (id){
            case R.id.numberPickerok:
                intent = new Intent();
                String date = String.format("%02d",mListHour.get(mWheelHour.getSelectedItemPosition()))+":"+
                        String.format("%02d",mListMinu.get(mWheelMinute.getSelectedItemPosition()));
                isAm = (mWheelApm.getSelectedItemPosition()==0);
                intent.putExtra(EXTRA_DATE,date);
                intent.putExtra(EXTRA_IS_AM,isAm);
                intent.putExtra(EXTRA_IS_24_HOUR,is24Hour);
                intent.putExtra(EXTRA_INDEX,mWheelHour.getSelectedItemPosition());
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.numberPickercanle:
                finish();
                break;
        }
    }

    @Override
    public void onItemSelected(TosAdapterView<?> parent, View view, int position, long id) {
        if(parent.equals(mWheelHour)){
            if(!is24Hour){
                if(isAm && position>11){
                    isAm = false;
                    mWheelApm.setSelection(1);
                }else if(!isAm &&position<12){
                    isAm = true;
                    mWheelApm.setSelection(0);
                }
            }
        }else if(parent.equals(mWheelApm)){
            if(!is24Hour){
                if(position == 0){
                    isAm = true;
                    if(mWheelHour.getSelectedItemPosition()>11){
                        mWheelHour.setSelection(mWheelHour.getSelectedItemPosition()-12);
                    }
                }else {
                    isAm = false;
                    if(mWheelHour.getSelectedItemPosition()<12){
                        mWheelHour.setSelection(mWheelHour.getSelectedItemPosition()+12);
                    }
                }
            }
        }
        ((WheelAdapter)parent.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(TosAdapterView<?> parent) {

    }


    class WheelAdapter extends BaseAdapter{
        private List list;
        private int mHeight = 50;
        private int type;
        private WheelView wheelview;
        public WheelAdapter(WheelView view,List list,int type){
            this.list = list;
            mHeight = (int) UtilTools.pixelToDp(DialogSetTime.this,mHeight);
            this.type = type;
            this.wheelview = view;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if(convertView == null){
                convertView = new TextView(DialogSetTime.this);
                convertView.setLayoutParams(new TosGallery.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,mHeight));
                holder = new Holder();
                holder.tvContent = (TextView) convertView;
                holder.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                holder.tvContent.setTextColor(getResources().getColor(R.color.gray_text));
                holder.tvContent.setGravity(Gravity.CENTER);
                convertView.setTag(holder);
            }
            holder = (Holder) convertView.getTag();
            String text = "";
            if((type == TYPE_HOUR && is24Hour) || type == TYPE_MINU){
                holder.tvContent.setText(list.get(position)+"");
            }else {
                holder.tvContent.setText(list.get(position)+"");
            }
            if(position == wheelview.getSelectedItemPosition()){
                holder.tvContent.setTextColor(Color.BLACK);
            }else {
                holder.tvContent.setTextColor(mNormalColor);
            }
            return convertView;
        }

        class Holder {
            TextView tvContent;
        }
    }
}
