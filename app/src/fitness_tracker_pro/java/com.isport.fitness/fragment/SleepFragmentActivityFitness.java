package com.isport.fitness.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import androidx.core.app.Fragment;
import androidx.core.app.FragmentManager;
import androidx.core.app.FragmentStatePagerAdapter;
import androidx.core.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.fragment.BaseFragment;
import com.isport.tracker.fragment.SleepFragment;
import com.isport.tracker.main.CalendarActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.MainViewPager;
import com.isport.tracker.view.vertical.PagerAdapter;
import com.isport.tracker.view.vertical.VerticalViewPager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wj on 2017/8/14.
 */

public class SleepFragmentActivityFitness extends BaseFragment implements View.OnClickListener {

    private VerticalViewPager mVerticalViewPager;
    private MainViewPager customViewPager;
    private ImageView imageToday, imageCalendar;
    private boolean isViewCreated = false;
    private List<View> viewPageList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isViewCreated) {
            mVerticalViewPager.setAdapter(new MyExercisePager());
            if (mVerticalViewPager != null && mVerticalViewPager.getAdapter() != null) {
                mVerticalViewPager.getAdapter().notifyDataSetChanged();
            }
            if (customViewPager != null && customViewPager.getAdapter() != null) {
                customViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
                customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - 1, false);
                customViewPager.setIsScrolled(true);
                imageToday.setVisibility(View.GONE);
            }
//            if (histPager != null && histPager.getAdapter() != null) {
//                notifiHistoryDadpter();
//            }
            isViewCreated = false;
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_home_sleep_fitness, container, false);
        mVerticalViewPager = (VerticalViewPager) view.findViewById(R.id.home_vericalpager);
        viewPageList = new ArrayList<>();
        customViewPager = (MainViewPager) inflater.inflate(R.layout.fragment_sleep_top, null);
        customViewPager.setIsScrolled(true);
        viewPageList.add(customViewPager);

        View bottomView = inflater.inflate(R.layout.fragment_sleep_history_fitness, null);
        viewPageList.add(bottomView);

        mVerticalViewPager.setAdapter(new MyExercisePager());

        mVerticalViewPager.setOnPageChangeListener(new VerticalViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 1) {
                    imageCalendar.setVisibility(View.GONE);
                    imageToday.setVisibility(View.GONE);
                } else {
                    if (customViewPager != null && customViewPager.getCurrentItem() != customViewPager.getAdapter().getCount() - 1) {
                        imageToday.setVisibility(View.VISIBLE);
                    } else {
                        imageToday.setVisibility(View.GONE);
                    }
                    imageCalendar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mVerticalViewPager.getAdapter().notifyDataSetChanged();
        mVerticalViewPager.setCurrentItem(0, false);

        imageCalendar = (ImageView) view.findViewById(R.id.main_fragment_date);
        imageToday = (ImageView) view.findViewById(R.id.main_fragment_today_date);
        imageCalendar.setOnClickListener(this);
        imageToday.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        customViewPager.getAdapter().notifyDataSetChanged();
        customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - 1);


        customViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position + 1 == customViewPager.getAdapter().getCount()) {
                    imageToday.setVisibility(View.GONE);
                } else {
                    imageToday.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        registerReceiver();
        isViewCreated = true;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        getActivity().registerReceiver(mReceiver, filter);
    }

    @Override
    public void clearAdapter() {
        super.clearAdapter();
        if (customViewPager != null) {
            customViewPager.setAdapter(null);
        }
        if (mVerticalViewPager != null) {
            mVerticalViewPager.setAdapter(null);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mReceiver);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            getActivity().finish();
        }
    };

	/*public void finishActivity(){
        MyPagerAdapter adp = (MyPagerAdapter) customViewPager.getAdapter();
		if(adp != null){
			Map<Integer,SleepFragmentFitness> map = adp.getAdaptentContents();
			if(map != null && map.size()>0){
				Set<Integer> set = map.keySet();
				for (Integer position:set){
					SleepFragmentFitness ct = map.get(position);
					if(!ct.isDetached()){
						ct.unRegisterBroadcst();
					}
				}
			}
		}
		customViewPager.setAdapter(null);
		handler.sendEmptyMessageDelayed(0x01,16);
		//((MainActivity)getActivity())
	}*/

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
                customViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
                customViewPager.getAdapter().notifyDataSetChanged();
                customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - 1);
            }
        }
    };

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
                if (day + 1 >= customViewPager.getAdapter().getCount()) {

                } else {
                    customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - day - 1, false);
                }
            }

        }
    }

    public static SleepFragmentActivityFitness newInstance() {
        Bundle args = new Bundle();
        SleepFragmentActivityFitness fragment = new SleepFragmentActivityFitness();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.main_fragment_date:
                intent = new Intent(getActivity(), CalendarActivity.class);
                intent.putExtra("index", customViewPager.getAdapter().getCount() - customViewPager.getCurrentItem() - 1);
                startActivityForResult(intent, 10);
                break;
            case R.id.main_fragment_today_date:
                customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - 1, false);
                imageToday.setVisibility(View.GONE);
                break;
        }

    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        //private Map<Integer, SleepFragmentFitness> mapSleepFragment;
        private int tttcount = 0;

        public MyPagerAdapter(FragmentManager manager) {
            super(manager);
            //mapSleepFragment = new HashMap<>();
            Calendar calendar = Calendar.getInstance();
            tttcount = (int) UtilTools.getDaysBetween(Constants.INIT_DATE, calendar.getTime());
        }

        @Override
        public Fragment getItem(int position) {
            Fragment exerciseFragment;
            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
                exerciseFragment = SleepFragmentFitness.newInstance(position, getCount());
            } else {
                exerciseFragment = SleepFragment.newInstance(position, getCount());
            }
            //mapSleepFragment.put(position, exerciseFragment);

            return exerciseFragment;
        }

        @Override
        public int getCount() {
            return tttcount;
        }

		/*public Map<Integer,SleepFragmentFitness> getAdaptentContents(){
            return this.mapSleepFragment;
		}*/

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {

        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
