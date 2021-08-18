package com.isport.fitness.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.database.DbHistorySport;
import com.isport.isportlibrary.database.DbRealTimePedo;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.HistorySport;
import com.isport.isportlibrary.entry.PedoRealData;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.MyApp;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.fragment.BaseFragment;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.ColorArcProgressBar;
import com.isport.tracker.view.PedoView;
import com.isport.tracker.view.TasksCompletedView;
import com.isport.tracker.view.XScrollView;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wj on 2017/8/9.
 */

public class FragmentContentFitness extends BaseFragment implements XScrollView.IXScrollViewListener{//, OnClickListener {
    private final static String TAG = "FragmentContent";
    private String tvContentValue;

    private XScrollView mScrollView;
    private Context mContext;
    private TextView tvWeek, tvDate;
    private ColorArcProgressBar progress;
    private String[] weeks;
    private TasksCompletedView mPvView;
    private RadioGroup radioGroup;
    private ImageView imageLogo;
    private TextView mTvValue;
    private TextView mTvPercent;
    private List<Double> historySteps = Collections.synchronizedList(new ArrayList<Double>());

    private PedoRealData pedoRealData;
    private RadioButton rbSteps, rbCaloric, rbDistance;
    //private AreaChart02View stepChartView;
    private PedoView pedoView;
    private static LinkedList<String> mLabels;
    private static List<Double> listTp;
    static {
        mLabels = new LinkedList<>();
        listTp = new ArrayList<>();
        for (int i=0;i<=24;i++){
            listTp.add(0d);
            if(i%3 == 0){
                mLabels.add((i)+"h");
            }else {
                mLabels.add("");
            }
        }
    }

    public static FragmentContentFitness newInstance(int postion, int count) {
        FragmentContentFitness fragmentFirst = new FragmentContentFitness();
        Bundle args = new Bundle();
        args.putInt("position", postion);
        args.putInt("count", count);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_step, container, false);
        //verticalViewPager = (VerticalViewPager) inflater.inflate(R.layout.fragment_step, container, false);

        mScrollView = inflater.inflate(R.layout.fragment_scroll_exercise, container, false);
        //mScrollView = (XScrollView) view1.findViewById(R.id.scroll_view);
        View conentView = inflater.inflate(R.layout.fragment_step_daily_fitness, null);

        tvWeek = (TextView) conentView.findViewById(R.id.main_fragment_text_Week);
        progress= (ColorArcProgressBar) conentView.findViewById(R.id.progress);
        tvDate = (TextView) conentView.findViewById(R.id.main_fragment_text_date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.stringToDate(tvContentValue, "yyyy-MM-dd"));
        tvWeek.setText(weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        tvDate.setText(DateUtil.dataToString(calendar.getTime(), "dd/MM/yyyy"));

        mPvView = (TasksCompletedView) conentView.findViewById(R.id.tasks_view);
        radioGroup = (RadioGroup) conentView.findViewById(R.id.foot_rg);
        imageLogo = (ImageView) conentView.findViewById(R.id.img_logo);
        mTvValue = (TextView) conentView.findViewById(R.id.exercise_time);
        mTvPercent = (TextView) conentView.findViewById(R.id.exercise_per);
        rbSteps = (RadioButton) conentView.findViewById(R.id.exercise_total_steps);
        rbCaloric = (RadioButton) conentView.findViewById(R.id.exercise_total_carles);
        rbDistance = (RadioButton) conentView.findViewById(R.id.exercise_total_distance);
        pedoView = (PedoView) conentView.findViewById(R.id.chart_area);

        mScrollView.setView(conentView);
        mScrollView.setPullRefreshEnable(true);
        mScrollView.setPullLoadEnable(false);
        mScrollView.setAutoLoadEnable(false);
        mScrollView.setIXScrollViewListener(this);



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sportRadioGroupSelected(checkedId);
            }
        });


        return mScrollView;
    }

    boolean isRegisterBroadcast = false;

    private void registerBroadcast() {
        isRegisterBroadcast = true;
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_SYNC_COMPLETED);
        filter.addAction(BaseController.ACTION_REAL_DATA);
        mContext.registerReceiver(mReceiver, filter);*/
        EventBus.getDefault().register(this);
    }

    public void unRegisterBroadcst() {
        if(mContext != null && isRegisterBroadcast) {
           /* mContext.unregisterReceiver(mReceiver);
            isRegisterBroadcast = false;*/
            EventBus.getDefault().unregister(this);
        }
    }

    private void sportRadioGroupSelected(int checkedId) {
        if (mTvValue == null)
            return;
        UserInfo userInfo = UserInfo.getInstance(mContext);
        int percent = (pedoRealData != null ? (Math.round(pedoRealData.getPedoNum() * 100) / userInfo.getTargetStep()) : 0);
        mTvPercent.setText(percent + "%");
        mPvView.setProgress(percent);
        progress.setCurrentValues(percent);
        rbSteps.setText((pedoRealData == null ? 0 : pedoRealData.getPedoNum()) + "");
        if(pedoRealData == null){
            rbCaloric.setText("0");
        }else {
            int caltp = (int) (pedoRealData.getPedoNum() * ((userInfo.getWeight() - 13.63636) * 0.000693 + 0.000495));
            //rbCaloric.setText((pedoRealData == null ? 0 : pedoRealData.getCaloric()) + "");
            rbCaloric.setText(caltp+"");
        }
        rbDistance.setText((pedoRealData == null ? 0 : pedoRealData.getDistance()) + "");
        switch (checkedId) {
            case R.id.exercise_total_steps:
                mTvValue.setText((pedoRealData == null ? 0 : pedoRealData.getPedoNum()) + "");
                rbSteps.setChecked(true);
                imageLogo.setImageResource(R.drawable.foot_logo);
                break;
            case R.id.exercise_total_carles:
                mTvValue.setText((pedoRealData == null ? 0 : pedoRealData.getCaloric()) + "");
                rbCaloric.setChecked(true);
                imageLogo.setImageResource(R.drawable.calorie_logo);
                break;
            case R.id.exercise_total_distance:
                mTvValue.setText((pedoRealData == null ? 0 : pedoRealData.getDistance()) + "");
                imageLogo.setImageResource(R.drawable.location_logo);
                rbDistance.setChecked(true);
                break;
        }
    }

    private void historyRadioGroupSelected(int checkedId) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        int position = getArguments().getInt("position");
        int count = getArguments().getInt("count");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * (count - position - 1));
        tvContentValue = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
        weeks = mContext.getResources().getStringArray(R.array.week);

        loadData();
    }

    private void loadData() {
        MyApp.getIntance().executorService.submit(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });
    }

    private void initHistorySteps() {
        historySteps.clear();
        historySteps.addAll(listTp);
    }

    private synchronized void initData() {
        MainService mainService = MainService.getInstance(mContext);
        int total = 0;
        if (mainService != null) {
            BaseDevice baseDevice = mainService.getCurrentDevice();
            if (baseDevice != null) {
                pedoRealData = DbRealTimePedo.getInstance().findFirst(tvContentValue, baseDevice.getMac());
                int nozero = 0;

                String sql = DbHistorySport.COLUMN_DATE + " like ? and " + DbHistorySport.COLUMN_MAC + "=?";
                List<HistorySport> tpH = DbHistorySport.getInstance().findAll(sql, new String[]{tvContentValue + "%", baseDevice.getMac()}, null);
                if (tpH == null) {
                    historySteps.clear();
                    historySteps.addAll(listTp);
                } else {
                    historySteps.clear();
                    int tpv = 0;
                    int index = 0;
                    double tempTotal = 0;
                    for (int i = 1; i <= tpH.size(); i++) {
                        int tpnum = tpH.get(i-1).getStepNum();
                        tempTotal += tpnum;
                        index++;
                        total += tpnum;
                        if (index % 12 == 0) {
                            historySteps.add(tempTotal);
                            if (tempTotal > 0) {
                                nozero = index / 12 - 1;
                            }
                            tempTotal = 0;
                        }
                    }
                    if (historySteps.size() < 25) {
                        int size = historySteps.size();
                        for (int i = size; i < 25; i++) {
                            if (tempTotal > 0) {
                                historySteps.add(tempTotal);
                                if (tempTotal > 0) {
                                    nozero = index / 12 - 1;
                                }
                                tempTotal = 0;
                            } else {
                                historySteps.add(0d);
                            }
                        }
                    }
                }
                if (pedoRealData != null) {
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    String tppp = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                    calendar.setTime(DateUtil.stringToDate(tvContentValue, "yyyy-MM-dd"));
                    int index = 0;

                    if (tvContentValue.equals(tppp)) {
                        index = hour;
                    } else {
                        index = nozero;
                    }

                    if (total < pedoRealData.getPedoNum()) {
                        double temp = historySteps.get(index);
                        historySteps.set(index, temp + pedoRealData.getPedoNum() - total);
                    }
                }
            } else {
                pedoRealData = null;
                initHistorySteps();
            }
        } else

        {
            initHistorySteps();
            pedoRealData = null;
        }

        handler.sendEmptyMessage(0x01);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
//                    if (mPvView != null) {
                    if (progress != null) {
                        int id = radioGroup.getCheckedRadioButtonId();
                        sportRadioGroupSelected(id);

                        if(historySteps.size()<25){
                            int size = historySteps.size();
                            for (int i=size;i<25;i++){
                                historySteps.add(0d);
                            }
                        }

                        List<Double> tp = new ArrayList<>();
                        tp.addAll(historySteps);
                        Collections.sort(tp);
                        //stepChartView.setmDataset(mDataset);
                        int itx = historySteps.size()-1;
                        if(itx<0){
                            itx = 0;
                        }
                        if(itx>=tp.size()){
                            itx = tp.size() - 1;
                        }
                        if(tp.size()==0){
                            tp.addAll(listTp);
                        }
                        pedoView.setMaxValue((int) (itx<0?0:tp.get(itx).doubleValue())+5);
                        pedoView.setmLabels(mLabels);
                        pedoView.setListData(historySteps);
                        //stepChartView.setAxisMax((int) (itx<0?0:tp.get(itx).doubleValue())+5);
                        //stepChartView.setmLabels(mLabels);
                        //stepChartView.initView();
                    }
                    break;
            }
        }
    };

    private class MyCollectionSort implements Comparator<Double> {

        @Override
        public int compare(Double o1, Double o2) {
            return o1 < o2?1:0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler.hasMessages(0x01))
            handler.removeMessages(0x01);
        unRegisterBroadcst();
//        if(mPvView != null){
        if(progress != null){
            mPvView.stopAnimation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
       /* if (verticalViewPager != null) {
            verticalViewPager.getAdapter().notifyDataSetChanged();
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    View cell_bottom;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerBroadcast();
        handler.sendEmptyMessage(0x01);
    }

    /*@Override
    public void onClick(View v) {
        //MainService mainService = MainService.getInstance(mContext);
        //if(mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            //((ExerciseFragmentActivityFitness)getParentFragment()).finishActivity();
        //}
    }*/



    @Override
    public void onRefresh() {
        MainService mainService = MainService.getInstance(mContext);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            if (!mainService.startSyncData()) {
                mScrollView.stopRefresh();
                Toast.makeText(mContext, mContext.getString(R.string.header_hint_refresh_loading), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.please_bind), Toast.LENGTH_LONG).show();
            mScrollView.stopRefresh();
        }
    }

    @Override
    public void onLoadMore() {

    }


    public void onEventMainThread(Intent intent) {
        String action = intent.getAction();
        if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
            if (mScrollView != null) {
                mScrollView.stopRefresh();
            }
            int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
            if (state == BaseController.STATE_CONNECTED) {
                loadData();
            }
        } else if (action.equals(MainService.ACTION_SYNC_COMPLETED)) {
            int state = intent.getIntExtra(MainService.EXTRA_SYNC_STATE, BaseController.STATE_SYNC_COMPLETED);
            if (state != BaseController.STATE_SYNCING) {
                if (mScrollView != null) {
                    mScrollView.stopRefresh();
                }
                loadData();
            }
        } else if (action.equals(BaseController.ACTION_REAL_DATA)) {
            String date = intent.getStringExtra(BaseController.EXTRA_REAL_DATE);
            if (date.equals(tvContentValue) && MainService.getInstance(mContext).getCurrentDevice() != null ) {
                //loadData();
                pedoRealData = DbRealTimePedo.getInstance().findFirst(tvContentValue, MainService.getInstance(mContext).getCurrentDevice().getMac());
                if (pedoRealData != null) {
                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    String tppp = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                    calendar.setTime(DateUtil.stringToDate(tvContentValue, "yyyy-MM-dd"));
                    int index = 0;

                    if (tvContentValue.equals(tppp)) {
                        index = hour;
                    }
                    int total = 0;
                    for (int i=0;i<historySteps.size();i++){
                        total += historySteps.get(i);
                    }
                    if (total < pedoRealData.getPedoNum()) {
                        if(historySteps.size()<=24){
                            for (int i= historySteps.size();i<=24;i++){
                                historySteps.add(0d);
                            }
                        }
                        if(index<historySteps.size()) {
                            double temp = historySteps.get(index);
                            historySteps.set(index, temp + pedoRealData.getPedoNum() - total);
                        }
                    }
                    handler.sendEmptyMessage(0x01);
                }
            }
        }
    }
}