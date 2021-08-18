package com.isport.tracker.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.main.CalendarActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.MainViewPager;
import com.isport.tracker.view.vertical.PagerAdapter;
import com.isport.tracker.view.vertical.VerticalViewPager;
import com.ypy.eventbus.EventBus;

import java.util.Calendar;

//import cn.sharesdk.onekeyshare.OnekeyShare;

@SuppressLint("NewApi")
public class SleepFragmentActivity extends BaseFragment implements View.OnClickListener {

    private VerticalViewPager verticalViewPager;
    private MainViewPager customViewPager;
    private ImageView imageToday, imageCalendar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_home_sleep, container, false);

        customViewPager = (MainViewPager) view.findViewById(R.id.mViewPager);
        customViewPager.setIsScrolled(true);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        customViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        customViewPager.getAdapter().notifyDataSetChanged();
        customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - 1);

        imageCalendar = (ImageView) view.findViewById(R.id.main_fragment_date);
        imageToday = (ImageView) view.findViewById(R.id.main_fragment_today_date);
        imageCalendar.setOnClickListener(this);
        imageToday.setOnClickListener(this);

        if (customViewPager.getCurrentItem() == customViewPager.getAdapter().getCount() - 1) {
            imageToday.setVisibility(View.GONE);
        }
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

        EventBus.getDefault().register(this);
    }

    @Override
    public void clearAdapter() {
        super.clearAdapter();
        if (customViewPager != null) {
            customViewPager.setAdapter(null);
        }
        if (verticalViewPager != null) {
            verticalViewPager.setAdapter(null);
        }

    }

    private boolean isViewCreated = false;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == 201) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.add(Calendar.DAY_OF_YEAR, 1);
            long temp = calendar1.getTimeInMillis();
            calendar.setTime(DateUtil.stringToDate(data.getStringExtra("date"), "yyyy-MM-dd"));
            long current = calendar.getTimeInMillis();
            if (current > temp) {
                Toast.makeText(getActivity(), getResources().getString(R.string.your_date_is_not_yet), Toast.LENGTH_LONG).show();
                return;
            } else {
                int day = (int) ((temp - current) / (3600 * 24 * 1000));
                if (day + 1 >= customViewPager.getAdapter().getCount()) {
                    Log.e("AKAK", "22222222");
                } else {
                    Log.e("AKAK", "11111111" + day);
                    customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - day - 1, false);
                }
            }
        }
    }

    public static SleepFragmentActivity newInstance() {

        Bundle args = new Bundle();

        SleepFragmentActivity fragment = new SleepFragmentActivity();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.main_fragment_date:
                intent = new Intent(getActivity(), CalendarActivity.class);
                intent.putExtra("index", customViewPager.getAdapter().getCount() - customViewPager.getCurrentItem() - 2);
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
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            tttcount = (int) UtilTools.getDaysBetween(Constants.INIT_DATE, calendar.getTime());
        }

        @Override
        public Fragment getItem(int position) {
            Fragment exerciseFragment = null;
            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
//                exerciseFragment = SleepFragmentFitness.newInstance(position, getCount());
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

    public void onEventMainThread(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_DATE_CHANGED)) {
            customViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
            customViewPager.getAdapter().notifyDataSetChanged();
            customViewPager.setCurrentItem(customViewPager.getAdapter().getCount() - 1);
        }
    }

}