package com.isport.tracker.dialogActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;

public class DialogSetSex extends BaseActivity implements View.OnClickListener {
    public final static String TYPE_GENDER = "gender";
    public final static String TYPE_METRIC = "metric";
    public final static String TYPE_HAND = "hand";
    public final static String TYPE_TEMP_NUNIT = "temp_UNIT";
    public final static String EXTRA_TYPE = "type";
    public final static String EXTRA_IS_LEFTHAND = "is_left_hand";
    public final static String EXTRA_IS_TEMP_UNIT = "is_temp_unit";


    private ImageView oneImg, twoImg;
    private TextView mTvOne;
    private TextView mTvTwo;
    private TextView mTvTitle;
    private String type;
    private boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_select);

        type = getIntent().getStringExtra(EXTRA_TYPE);
        status = getIntent().getBooleanExtra(EXTRA_IS_LEFTHAND, true);
        init();
    }


    private void init() {
        mTvOne = (TextView) findViewById(R.id.set_tv_one);
        mTvTwo = (TextView) findViewById(R.id.set_tv_two);

        oneImg = (ImageView) findViewById(R.id.set_sex_man_image);
        twoImg = (ImageView) findViewById(R.id.set_sex_woman_image);

        mTvOne.setOnClickListener(this);
        mTvTwo.setOnClickListener(this);

        if (type.equals(TYPE_GENDER)) {
            setTitle(getString(R.string.user_info_set_sex));
            mTvOne.setText(getString(R.string.user_info_man));
            mTvTwo.setText(getString(R.string.user_info_woman));
            //mTvTitle.setText(getString(R.string.user_info_set_sex));
            mTvOne.setTextColor(status ? getResources().getColor(R.color.black) : getResources().getColor(R.color.vivi_gray));
            mTvTwo.setTextColor(status ? getResources().getColor(R.color.gray_text) : getResources().getColor(R.color.black));
            oneImg.setVisibility(status ? View.VISIBLE : View.GONE);
            twoImg.setVisibility(status ? View.GONE : View.VISIBLE);
        } else if (type.equals(TYPE_HAND)) {
            setTitle(getString(R.string.setting_wear_way));
            mTvOne.setText(getString(R.string.right_hand));
            mTvTwo.setText(getString(R.string.left_hand));
            //mTvTitle.setText(getString(R.string.setting_wear_way));
            mTvOne.setTextColor(status ? getResources().getColor(R.color.vivi_gray) : getResources().getColor(R.color.black));
            mTvTwo.setTextColor(status ? getResources().getColor(R.color.black) : getResources().getColor(R.color.gray_text));
            oneImg.setVisibility(status ? View.GONE : View.VISIBLE);
            twoImg.setVisibility(status ? View.VISIBLE : View.GONE);
        } else if (type.equals(TYPE_METRIC)) {
            setTitle(getString(R.string.set_metric));
            mTvOne.setText(getString(R.string.metric));
            mTvTwo.setText(getString(R.string.Inch));
            //mTvTitle.setText(getString(R.string.user_info_set_sex));
            mTvOne.setTextColor(status ? getResources().getColor(R.color.black) : getResources().getColor(R.color.vivi_gray));
            mTvTwo.setTextColor(status ? getResources().getColor(R.color.gray_text) : getResources().getColor(R.color.black));
            oneImg.setVisibility(status ? View.VISIBLE : View.GONE);
            twoImg.setVisibility(status ? View.GONE : View.VISIBLE);
        } else if (type.equals(TYPE_TEMP_NUNIT)) {
            setTitle(getString(R.string.temp_unit_setting));
            mTvOne.setText("0" + getString(R.string.temp_Fahrenheit));
            mTvTwo.setText("1" + getString(R.string.temp_degree_centigrade));
            //mTvTitle.setText(getString(R.string.user_info_set_sex));
            mTvOne.setTextColor(status ? getResources().getColor(R.color.black) : getResources().getColor(R.color.vivi_gray));
            mTvTwo.setTextColor(status ? getResources().getColor(R.color.gray_text) : getResources().getColor(R.color.black));
            oneImg.setVisibility(status ? View.VISIBLE : View.GONE);
            twoImg.setVisibility(status ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;
        switch (id) {
            case R.id.set_tv_one:
                if (type.equals(TYPE_GENDER)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_LEFTHAND, true);
                    setResult(209, intent);
                } else if (type.equals(TYPE_HAND)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_LEFTHAND, false);
                    setResult(208, intent);
                } else if (type.equals(TYPE_METRIC)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_LEFTHAND, true);
                    setResult(207, intent);
                } else if (type.equals(TYPE_TEMP_NUNIT)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_TEMP_UNIT, false);
                    setResult(201, intent);
                }
                break;
            case R.id.set_tv_two:
                if (type.equals(TYPE_GENDER)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_LEFTHAND, false);
                    setResult(209, intent);
                } else if (type.equals(TYPE_HAND)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_LEFTHAND, true);
                    setResult(208, intent);
                } else if (type.equals(TYPE_METRIC)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_LEFTHAND, false);
                    setResult(207, intent);
                } else if (type.equals(TYPE_TEMP_NUNIT)) {
                    intent = new Intent();
                    intent.putExtra(EXTRA_IS_TEMP_UNIT, true);
                    setResult(201, intent);
                }
                break;


        }
        finish();
    }
}