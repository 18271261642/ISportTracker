package com.isport.tracker.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.main.CalendarActivity;
import com.isport.tracker.main.HorizontalScreenActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.MainViewPager;
import com.isport.tracker.view.vertical.PagerAdapter;
import com.isport.tracker.view.vertical.VerticalViewPager;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class ExerciseFragmentActivity extends BaseFragment implements View.OnClickListener {

    private static final String TAG = ExerciseFragmentActivity.class.getSimpleName();
    public MainViewPager mViewPager;
    private String tvContentValue;
    public ImageView imageDateIcon;
    public ImageView today;
    protected VerticalViewPager mVerticalViewPager;
    private List<View> viewPageList;
    private RadioButton rbDay, rbWeek, rbOneWeek, rbMonth, tbStep, rbCalor, rbDist;
    private RadioGroup radioGroupHistory, radioGroupType;
    private ViewPager histPager;
    private Context mContext;
    private boolean isViewCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static ExerciseFragmentActivity newInstance() {
        Bundle args = new Bundle();
        ExerciseFragmentActivity fragment = new ExerciseFragmentActivity();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_pedo, container, false);
        mVerticalViewPager = (VerticalViewPager) view.findViewById(R.id.home_vericalpager);
        viewPageList = new ArrayList<>();
        mViewPager = (MainViewPager) inflater.inflate(R.layout.fragment_exercise_top, null);
        //mViewPager = (MainViewPager) vtp.findViewById(R.id.exercise_top_pager);
        mViewPager.setIsScrolled(true);
        viewPageList.add(mViewPager);

        View bottomView = inflater.inflate(R.layout.fragment_step_history, null);
        rbDay = (RadioButton) bottomView.findViewById(R.id.day_rb);
        rbWeek = (RadioButton) bottomView.findViewById(R.id.day_week);
        rbOneWeek = (RadioButton) bottomView.findViewById(R.id.day_every_week);
        rbMonth = (RadioButton) bottomView.findViewById(R.id.day_month);
        rbCalor = (RadioButton) bottomView.findViewById(R.id.exercise_hostory_carles);
        tbStep = (RadioButton) bottomView.findViewById(R.id.exercise_hostory_steps);
        rbDist = (RadioButton) bottomView.findViewById(R.id.exercise_hostory_distance);
        histPager = (ViewPager) bottomView.findViewById(R.id.msviewPager);
        bottomView.findViewById(R.id.horizontalView).setOnClickListener(this);

        radioGroupType = (RadioGroup) bottomView.findViewById(R.id.hostory_foot_rg);
        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupTypeChecked(checkedId);
            }
        });
        radioGroupHistory = (RadioGroup) bottomView.findViewById(R.id.chart_rg);
        radioGroupHistory.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupHistoryChecked(checkedId);
            }
        });

        viewPageList.add(bottomView);

        mVerticalViewPager.setAdapter(new MyExercisePager());

        mVerticalViewPager.setOnPageChangeListener(new VerticalViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 1) {
                    imageDateIcon.setVisibility(View.GONE);
                    today.setVisibility(View.GONE);
                } else {
                    if (mViewPager != null && mViewPager.getCurrentItem() != mViewPager.getAdapter().getCount() - 1) {
                        today.setVisibility(View.VISIBLE);
                    } else {
                        today.setVisibility(View.GONE);
                    }
                    imageDateIcon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mVerticalViewPager.getAdapter().notifyDataSetChanged();
        mVerticalViewPager.setCurrentItem(0, false);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }


            @Override
            public void onPageSelected(int position) {
                if (position + 1 == mViewPager.getAdapter().getCount()) {
                    today.setVisibility(View.GONE);
                } else {
                    today.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        imageDateIcon = (ImageView) view.findViewById(R.id.main_fragment_date);
        today = (ImageView) view.findViewById(R.id.main_fragment_today_date);

        today.setOnClickListener(this);
        imageDateIcon.setOnClickListener(this);

        histPager.setAdapter(new HistPagerAdapter(getChildFragmentManager()));
        histPager.setCurrentItem(histPager.getAdapter().getCount() - 1);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void clearAdapter() {
        super.clearAdapter();
        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
        if (histPager != null) {
            histPager.setAdapter(null);
            histPager.getAdapter().notifyDataSetChanged();
        }
        if (mVerticalViewPager != null) {
            mVerticalViewPager.setAdapter(null);
        }
    }

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
        histPager.setAdapter(new HistPagerAdapter(getChildFragmentManager()));
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
        histPager.setAdapter(new HistPagerAdapter(getChildFragmentManager()));
        histPager.setCurrentItem(histPager.getAdapter().getCount() - 1);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1, false);
        mViewPager.setIsScrolled(true);
        today.setVisibility(View.GONE);
        EventBus.getDefault().register(this);
        isViewCreated = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("ExerciseFragment", "onrResume");
        if (isViewCreated) {
            mVerticalViewPager.setAdapter(new MyExercisePager());
            if (mVerticalViewPager != null && mVerticalViewPager.getAdapter() != null) {
                mVerticalViewPager.getAdapter().notifyDataSetChanged();
            }
            if (mViewPager != null && mViewPager.getAdapter() != null) {
                mViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
                mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1, false);
                mViewPager.setIsScrolled(true);
                today.setVisibility(View.GONE);
            }

            if (histPager != null && histPager.getAdapter() != null) {
                histPager.setAdapter(new HistPagerAdapter(getChildFragmentManager()));
                histPager.setCurrentItem(histPager.getAdapter().getCount() - 1);
            }
            isViewCreated = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void init() {
    }

    private String tempStr;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == 201) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long temp = calendar.getTimeInMillis();
            calendar.setTime(DateUtil.stringToDate(data.getStringExtra("date"), "yyyy-MM-dd"));
            long current = calendar.getTimeInMillis();
            if (current > temp) {
                Toast.makeText(getActivity(), getResources().getString(R.string.your_date_is_not_yet), Toast.LENGTH_LONG).show();
                return;
            } else {
                int day = (int) ((temp - current) / (3600 * 24 * 1000));
                if (day + 1 >= mViewPager.getAdapter().getCount()) {

                } else {
                    mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - day - 1, false);
                }
            }

        }
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.main_fragment_date:
                intent = new Intent(getActivity(), CalendarActivity.class);
                intent.putExtra("index", mViewPager.getAdapter().getCount() - mViewPager.getCurrentItem() - 1);
                startActivityForResult(intent, 10);
                break;
            case R.id.main_fragment_today_date:
                mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1, false);
                today.setVisibility(View.GONE);
                break;
            case R.id.horizontalView:
                intent = new Intent(getActivity(), HorizontalScreenActivity.class);
                startActivity(intent);
                break;
        }
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int tttcount = 0;

        public MyPagerAdapter(FragmentManager manager) {
            super(manager);
            Calendar calendar = Calendar.getInstance();
            tttcount = (int) UtilTools.getDaysBetween(Constants.INIT_DATE, calendar.getTime());

        }

        @Override
        public Fragment getItem(int position) {
            Fragment exerciseFragment = null;
            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
//                exerciseFragment = FragmentContentFitness.newInstance(position, getCount());
            } else {
                exerciseFragment = FragmentContent.newInstance(position, getCount());
            }
            return exerciseFragment;
        }

        @Override
        public int getCount() {
            return tttcount;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {

        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    private int dataType = PedoHistoryFragment.TYPE_STEP;
    private int dateType = PedoHistoryFragment.DATE_DAY;

    private class HistPagerAdapter extends FragmentStatePagerAdapter {

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
                case PedoHistoryFragment.DATE_ONE_WEEK://本星期
                    return 1;
                case PedoHistoryFragment.DATE_MONTH:///当年月
                    int month = UtilTools.getMonthBetween(Constants.INIT_DATE, calendar.getTime());
                    return month % 7 == 0 ? month / 7 : month / 7 + 1;
            }
            return cttount;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

    }

    private class MyExercisePager extends PagerAdapter {
        @Override
        public int getCount() {
            return viewPageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(viewPageList.get(position % viewPageList.size()));
            object = null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewPageList.get(position % viewPageList.size()));
            return viewPageList.get(position);
        }
    }

    public void onEventMainThread(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
            mViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
            mViewPager.getAdapter().notifyDataSetChanged();
            mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1);
            histPager.setAdapter(new HistPagerAdapter(getChildFragmentManager()));
            histPager.setCurrentItem(histPager.getAdapter().getCount() - 1);
        }
    }
}