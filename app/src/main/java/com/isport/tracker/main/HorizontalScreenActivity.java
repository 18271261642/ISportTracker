package com.isport.tracker.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.isport.tracker.R;
import com.isport.tracker.adapter.CachingFragmentStatePagerAdapter;
import com.isport.tracker.fragment.PedoHistoryFragment;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.util.UtilTools;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HorizontalScreenActivity extends FragmentActivity implements View.OnClickListener {
    private ViewPager histPager;
    private RadioGroup chartRG;
    private static int state = 0;
    private static String tvContentValue = "";
    private ImageView ivBack;
    private ImageView horizontalView;
    private RadioGroup radioGroupType, radioGroupDate;
    private static int select;


    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        setContentView(R.layout.activity_horizontal_history);

        histPager = (ViewPager) findViewById(R.id.msviewPager);
        radioGroupType = (RadioGroup) findViewById(R.id.hostory_foot_rg);
        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupTypeChecked(checkedId);
            }
        });
        radioGroupDate = (RadioGroup) findViewById(R.id.chart_rg);
        radioGroupDate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupHistoryChecked(checkedId);
            }
        });
        radioGroupType.post(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) radioGroupType.getLayoutParams();
                params.bottomMargin = DeviceConfiger.dp2px(5);
                params.topMargin = DeviceConfiger.dp2px(5);
                radioGroupType.setLayoutParams(params);

                View v = findViewById(R.id.step_history_top_rela);
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) v.getLayoutParams();
                params1.bottomMargin = DeviceConfiger.dp2px(20);
                params1.topMargin = DeviceConfiger.dp2px(20);
                v.setLayoutParams(params1);
            }
        });
        histPager.setAdapter(new HistPagerAdapter(getSupportFragmentManager()));
        histPager.setCurrentItem(histPager.getAdapter().getCount() - 1);
    }

    private int dataType = PedoHistoryFragment.TYPE_STEP;
    private int dateType = PedoHistoryFragment.DATE_DAY;

    private void radioGroupHistoryChecked(int checkedId) {
        switch (checkedId) {
            case R.id.day_every_week:
                dateType = PedoHistoryFragment.DATE_ONE_WEEK;
                break;
            case R.id.day_month:
                dateType = PedoHistoryFragment.DATE_MONTH;
                break;
            case R.id.day_week:
                dateType = PedoHistoryFragment.DATE_WEEK;
                break;
            case R.id.day_rb:
                dateType = PedoHistoryFragment.DATE_DAY;
                break;
        }
        histPager.setAdapter(new HistPagerAdapter(getSupportFragmentManager()));
        histPager.setCurrentItem(histPager.getAdapter().getCount() - 1);
    }

    private void radioGroupTypeChecked(int checkedId) {
        switch (checkedId) {
            case R.id.exercise_hostory_carles:
                dataType = PedoHistoryFragment.TYPE_CALOR;
                break;
            case R.id.exercise_hostory_distance:
                dataType = PedoHistoryFragment.TYPE_DIST;
                break;
            case R.id.exercise_hostory_steps:
                dataType = PedoHistoryFragment.TYPE_STEP;
                break;
        }
        histPager.setAdapter(new HistPagerAdapter(getSupportFragmentManager()));
        histPager.setCurrentItem(histPager.getAdapter().getCount() - 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.horizontalView:
                finish();
                break;
        }
    }

    private class HistPagerAdapter extends CachingFragmentStatePagerAdapter {

        private Map<Integer, PedoHistoryFragment> mapHistFragment;

        public HistPagerAdapter(FragmentManager manager) {
            super(manager);
            mapHistFragment = new HashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            PedoHistoryFragment exerciseFragment = mapHistFragment.get(position);
            exerciseFragment = PedoHistoryFragment.newInstance(dataType, dateType, position, getCount());
            mapHistFragment.put(position, exerciseFragment);

            return exerciseFragment;
        }

        @Override
        public int getCount() {
            Calendar calendar = Calendar.getInstance();
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            int cttount = 0;

            switch (dateType) {
                case PedoHistoryFragment.DATE_DAY:
                    long days = UtilTools.getDaysBetween(Constants.INIT_DATE, calendar.getTime());
                    return (int) (days % 7 == 0 ? days / 7 : days / 7 + 1);
                case PedoHistoryFragment.DATE_WEEK:
                    int weekNum = UtilTools.getWeekBetween(Constants.INIT_DATE, calendar.getTime());
                    return weekNum % 7 == 0 ? weekNum / 7 : (weekNum + 7 - weekNum % 7) / 7;
                case PedoHistoryFragment.DATE_ONE_WEEK://������
                    return 1;
                case PedoHistoryFragment.DATE_MONTH:///������
                    int month = UtilTools.getMonthBetween(Constants.INIT_DATE, calendar.getTime());
                    return month % 7 == 0 ? month / 7 : month / 7 + 1;
            }
            return cttount;
        }

    }

}
