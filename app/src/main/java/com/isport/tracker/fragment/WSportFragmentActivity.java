package com.isport.tracker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.R;
import com.isport.tracker.main.CalendarActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.MainViewPager;
import com.isport.tracker.view.vertical.PagerAdapter;
import com.ypy.eventbus.EventBus;

import java.util.Calendar;
import java.util.List;

/**
 * Created by feige on 2017/4/30.
 */

public class WSportFragmentActivity extends BaseFragment {

    private MainViewPager mViewPager;
    private ImageView image_date_icon;
    private ImageView today;
    private ImageView share_iv;
    private MyPagerAdapter adapterViewPager;
    private static String date;
    private Context mContext;
    private static List<String> lists;

    public static WSportFragmentActivity newInstance() {
        Bundle args = new Bundle();

        WSportFragmentActivity fragment = new WSportFragmentActivity();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_home_sleep, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init(view);
        EventBus.getDefault().register(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (adapterViewPager != null) {
            adapterViewPager.notifyDataSetChanged();
        }
        super.onDestroyView();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void clearAdapter() {
        super.clearAdapter();
        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    private void init(View view) {
        image_date_icon = (ImageView) view.findViewById(R.id.main_fragment_date);
        today = (ImageView) view.findViewById(R.id.main_fragment_today_date);
        //share_iv = (ImageView) findViewById(R.id.share_iv);
        mViewPager = (MainViewPager) view.findViewById(R.id.mViewPager);
        mViewPager.setIsScrolled(true);
        adapterViewPager = new MyPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(adapterViewPager);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                                               @Override
                                               public void onPageSelected(int arg0) {
                                                   // TODO Auto-generated method stub
                                                   if (arg0 == adapterViewPager.getCount() - 1) {
                                                       today.setVisibility(View.GONE);
                                                   } else {
                                                       today.setVisibility(View.VISIBLE);
                                                   }
                                               }

                                               @Override
                                               public void onPageScrollStateChanged(int arg0) {
                                                   // TODO Auto-generated method stub

                                               }

                                               @Override
                                               public void onPageScrolled(int arg0, float arg1, int arg2) {
                                                   // TODO Auto-generated method stub

                                               }
                                           }
        );
        // set pager to current date
        adapterViewPager.notifyDataSetChanged();
        mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1, false);
        today.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1, false);
            }
        });
        image_date_icon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getActivity(),
                        CalendarActivity.class);
                startActivityForResult(intent, 10);
            }
        });
    }

    public void onEventMainThread(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_DATE_CHANGED) || action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
            mViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
            mViewPager.getAdapter().notifyDataSetChanged();
            mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - 1);
        }
    }

    public static class MyPagerAdapter extends FragmentStatePagerAdapter {

        private Calendar cal;
        private int ttcount = 0;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            Calendar calendar = Calendar.getInstance();
            ttcount = (int) UtilTools.getDaysBetween(Constants.INIT_DATE, calendar.getTime());
        }

        @Override
        public int getCount() {
            return ttcount;
        }

        @Override
        public Fragment getItem(int position) {
            return WSportFragment.newInstance(position, getCount());
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {

        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

    }

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
}
