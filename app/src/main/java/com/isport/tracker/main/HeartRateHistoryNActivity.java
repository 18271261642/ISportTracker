package com.isport.tracker.main;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.database.DbHeartRateHistory;
import com.isport.isportlibrary.entry.HeartRateHistory;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.R;
import com.isport.tracker.adapter.CachingFragmentStatePagerAdapter;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.SyncHeartRate;
import com.isport.tracker.fragment.HeartRateHistoryFragment;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.DialogHelper;
import com.isport.tracker.util.TimeUtils;
import com.isport.tracker.util.UIUtils;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.VpSwipeRefreshLayout;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Author xiongxing
 * @Date 2018/11/15
 * @Fuction
 */

public class HeartRateHistoryNActivity extends BaseActivity {

    private static final String TAG = HeartRateHistoryNActivity.class.getSimpleName();
    private RelativeLayout mRlBack;
    private ImageView mCalender;
    private ViewPager mViewPager;
    private VpSwipeRefreshLayout srl_data;
    float startY;
    float startX;
    private int mDayPosition;//viewpager的位置
    private List<HeartRateHistory> mListHistory;
    private boolean isRefleshing;//是否在请求心率数据
    private String mCurrentMac;
    private ProgressDialog mProgressDialog;
    private MaterialCalendarView calendarView;
    private List<String> hasDataList = new ArrayList<>();
    private Date currentDate = Calendar.getInstance().getTime();
    private Date mCurrentDate;
    boolean is15MinType;
    boolean is5MinType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_historyn);
        EventBus.getDefault().register(this);
        initView();
        initViewPager();
        initEvent();
        reLoadData();
    }

    private void initEvent() {
        mRlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calendarView.getVisibility() == View.GONE) {
                    finish();
                } else {
                    setLlDateVisible(1);
                }
            }
        });
        srl_data.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                srl_data.setRefreshing(false);
                reLoadData();
            }
        });
        mCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calendarView.getVisibility() == View.VISIBLE) {
                    setLlDateVisible(1);
                } else {
                    setLlDateVisible(2);
                }
            }
        });

        mCurrentDate = Calendar.getInstance().getTime();
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean
                    selected) {
                String currentDayStr = DateUtil.dataToString(mCurrentDate, "yyyy-MM-dd");//首次进来选择当天，然后回更新当前选择
                String selectDateStr = DateUtil.dataToString(date.getDate(), "yyyy-MM-dd");//这次选择
                Log.e(TAG, "onDateSelected day" + selectDateStr + " currentDayStr " + currentDayStr);
                if (!date.getDate().before(mCurrentDate)) {
                    Log.e(TAG, "onDateSelected day 000" + date.toString() + " mCurrentDate " + mCurrentDate.toString());
                    Toast.makeText(HeartRateHistoryNActivity.this, R.string.select_date_error, Toast.LENGTH_SHORT)
                            .show();
                    calendarView.setDateSelected(date, false);//取消当前的选择,并提示用户
                    for (int i = 0; i < hasDataList.size(); i++) {
                        Log.e(TAG, "onDateSelected 000 " + hasDataList.get(i));
                        calendarView.setDateSelected(TimeUtils.string2Date(hasDataList.get(i), "yyyy-MM-dd"), true);
                    }
                    return;
                } else {
                    //选中后不再消失
                    setLlDateVisible(1);
                    String current = DateUtil.dataToString(currentDate, "yyyy-MM-dd");
                    String select = DateUtil.dataToString(date.getDate(), "yyyy-MM-dd");
//            if (!current.equals(select)) {
                    //不是选择的当前，也就是重复选择，将不会刷新数据，而是只是让日历消失
                    Log.e(TAG, "onDateSelected day 222" + " current " + current + " select " + select);
                    //当前的选择
                    currentDate = date.getDate();
                    //如果有数据的日期里面没有选择的日期，那么取消选中，因为选中是
//            if (!hasDataList.contains(select))
                    calendarView.setDateSelected(date, false);//取消当前的选择,并提示用户
//                    widget.setDateSelected(date, true);//取消当前的选择,并提示用户
                    for (int i = 0; i < hasDataList.size(); i++) {
                        Log.e(TAG, "onDateSelected 111 " + hasDataList.get(i));
                        calendarView.setDateSelected(TimeUtils.string2Date(hasDataList.get(i), "yyyy-MM-dd"), true);
                    }
                    //选择日期，刷新
                    int daysBetween = (int) TimeUtils.getDaysBetween(currentDate, Calendar.getInstance().getTime());
                    mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
                    mDayPosition = mViewPager.getAdapter().getCount() - daysBetween;
                    mViewPager.setCurrentItem(mDayPosition, false);
//            }else{
//                Log.e(TAG, "onDateSelected day 333"+" current " + current+" select "+select);
//            }
                }
            }
        });
    }

    /***
     * 是否显示 选择日期条  , 日历
     *
     * @param type 1 是隐藏  2 是展示
     */
    private Animation loadImageAnimation;

    public void setLlDateVisible(int type) {
        if (calendarView == null)
            return;
        if (type == 1) {
            loadImageAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.btn_up);
            calendarView.startAnimation(loadImageAnimation);
            loadImageAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    calendarView.setVisibility(View.GONE);
                    mViewPager.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            calendarView.setDateSelected(currentDate, false);//取消当前的选择,并提示用户
            for (int i = 0; i < hasDataList.size(); i++) {
                Log.e(TAG, "onDateSelected 111 " + hasDataList.get(i));
                calendarView.setDateSelected(TimeUtils.string2Date(hasDataList.get(i), "yyyy-MM-dd"), true);
            }
            //日历会默认选中当天，如果无数据应该提示用户
            String current = DateUtil.dataToString(currentDate, "yyyy-MM-dd");
//            if (!hasDataList.contains(current))

//            calendarView.setDateSelected(currentDate, true);//取消当前的选择,并提示用户
            loadImageAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.btn_down);
            loadImageAnimation.setFillAfter(!loadImageAnimation.getFillAfter());
            calendarView.startAnimation(loadImageAnimation);
            //初始化日历
            calendarView.setVisibility(View.VISIBLE);
            mViewPager.setEnabled(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (calendarView.getVisibility() == View.GONE) {
                return super.onKeyDown(keyCode, event);
            } else {
                setLlDateVisible(1);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            DialogHelper.dismissDialog(mProgressDialog);
        }
        EventBus.getDefault().unregister(this);
    }

    private void reLoadData() {
        MainService mainService = MainService.getInstance(this);
        BaseController currentController = mainService.getCurrentController();
        if (currentController != null) {
//            srl_data.setRefreshing(true);
            mProgressDialog = DialogHelper.showProgressDialog(this, UIUtils.getString(R.string.syncing_heartrate_data));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
            isRefleshing = true;
            mCurrentMac = currentController.getBaseDevice().getMac();
            ((CmdController) currentController).getHeartRateRange();
        }
    }

    private void loadData() {
        MainService mainService = MainService.getInstance(this);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            final String mac = mainService.getCurrentDevice().getMac();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //查询有数据的位置，展示在日历上，暂放
                    String sql = DbHeartRateHistory.COLUMN_AVG + ">0 and " + DbHeartRateHistory.COLUMN_MAC + "=?";
                    mListHistory = DbHeartRateHistory.getIntance(HeartRateHistoryNActivity.this).findAll(sql, new
                            String[]{mac}, null);
//                    mListHistory = DbHeartRateHistory.getIntance(HeartRateHistoryNActivity.this).getListHistory
//                            (DbHeartRateHistory
//
//     .COLUMN_MAC + "=?", new String[]{mac},
//                                                                                                                null,
//                             null,
//
// "datetime(" +
//
//     DbHeartRateHistory.COLUMN_DATE +
//
//     ") DESC");
                    handler.sendEmptyMessage(0x01);
                }
            }).start();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hasDataList.clear();
            if (mListHistory != null && mListHistory.size() > 0) {
                for (int i = 0; i < mListHistory.size(); i++) {
                    Log.e(TAG, mListHistory.get(i).toString());
                    hasDataList.add(mListHistory.get(i).getStartDate());
                }
            }
            for (int i = 0; i < hasDataList.size(); i++) {
                calendarView.setDateSelected(TimeUtils.string2Date(hasDataList.get(i), "yyyy-MM-dd"), true);
            }
            if (mProgressDialog != null) {
                DialogHelper.dismissDialog(mProgressDialog);
            }
            isRefleshing = false;
            srl_data.setRefreshing(false);
            mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
            mViewPager.setCurrentItem(mDayPosition, false);
        }
    };

    private void initViewPager() {
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录手指按下的位置
                        startY = event.getY();
                        startX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取当前手指位置
                        float endY = event.getY();
                        float endX = event.getX();
                        float distanceX = Math.abs(endX - startX);
                        float distanceY = Math.abs(endY - startY);
                        // 如果X轴位移大于Y轴位移，那么将事件交给viewPager处理。
                        if (distanceX > distanceY) {
                            if (!isRefleshing)
                                srl_data.setEnabled(false);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        srl_data.setEnabled(true);
                        break;
                }
                return false;
            }
        });
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        mViewPager.setCurrentItem(mDayPosition, false);
    }

    public void onEventMainThread(SyncHeartRate entry) {
        if (entry != null) {
            if (entry.getState() == 1) {
                //成功，获取本地数据后刷新页面
            } else if (entry.getState() == 0) {
                //失败
//                Toast.makeText(this, getString(R.string.sync_heart_nodata), Toast.LENGTH_SHORT).show();
            }
            loadData();
        }
    }

    public class FragmentAdapter extends CachingFragmentStatePagerAdapter {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mDayPosition = getCount() - 1;
            Log.e(TAG, " position == " + position);
            HeartRateHistoryFragment stepDayFragment = HeartRateHistoryFragment.newInstance(position,
                    getCount(), mCurrentMac, is15MinType, is5MinType);
            return stepDayFragment;
        }

        @Override
        public int getCount() {
            Calendar calendar = Calendar.getInstance();
            long days = UtilTools.getDaysBetween(Constants.INIT_DATE, calendar.getTime());
            return (int) days;
        }
    }

    private void initView() {
        mRlBack = findViewById(R.id.return_back);
        mCalender = findViewById(R.id.iv_delete);
        mViewPager = findViewById(R.id.viewpager);
        srl_data = findViewById(R.id.srl_data);
        calendarView = findViewById(R.id.calendarView);
        is15MinType = getIntent().getBooleanExtra("is15MinType", false);
        is5MinType = getIntent().getBooleanExtra("is5MinType", false);
    }
}