package com.isport.fitness.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import androidx.core.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.database.DbHistorySport;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.HistorySport;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.MyApp;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.main.settings.sport.SleepActivity;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.ColorArcProgressBar;
import com.isport.tracker.view.SleepStateView;
import com.isport.tracker.view.TasksCompletedView;
import com.isport.tracker.view.XScrollView;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SleepFragmentFitness extends Fragment implements XScrollView.IXScrollViewListener, View.OnClickListener {


    private Context mContext;
    private String tvContentValue;
    private String[] weeks;
    private TextView tvWeek,tvDate;
    private TextView tvSleepTime,tvSleepPercent;
    private TasksCompletedView mPvView;
    //private BarChart03View barChart03View;
    private SleepStateView sleepStateView;
    private View viewSleep,viewWake;
    private XScrollView mScrollView;
    private View conentView;
    private List<String> listChartLabel;
    /*public static List<Integer> LIST_SLEEP;
    public static List<Double> LIST_SLEEP_D;*/

    private List<Integer> listToday;
    private List<Integer> listYestoday;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    static {
        /*LIST_SLEEP = new LinkedList<>();
        LIST_SLEEP_D = new LinkedList<>();
        for (int i=0;i<288;i++){
            LIST_SLEEP.add(0);
            LIST_SLEEP_D.add(0d);
        }*/
    }

    private ColorArcProgressBar progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static SleepFragmentFitness newInstance(int position, int count) {
        SleepFragmentFitness fragmentFirst = new SleepFragmentFitness();
        Bundle args = new Bundle();
        args.putInt("position",position);
        args.putInt("count",count);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        int position = getArguments().getInt("position");
        int count = getArguments().getInt("count");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,-1*(count - position -1));
        tvContentValue = UtilTools.date2String(calendar.getTime(),"yyyy-MM-dd");
        sharedPreferences = mContext.getSharedPreferences(SleepActivity.CONFIG_SLEEP_PATH,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        loadData();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDate = (TextView) conentView.findViewById(R.id.main_fragment_text_date);
        tvWeek = (TextView) conentView.findViewById(R.id.main_fragment_text_Week);
        mPvView = (TasksCompletedView) conentView.findViewById(R.id.tasks_view);
        progress = (ColorArcProgressBar) conentView.findViewById(R.id.progress);
        tvSleepPercent = (TextView) conentView.findViewById(R.id.tv_sleep_percent);
        tvSleepTime = (TextView) conentView.findViewById(R.id.exercise_time);
        sleepStateView = (SleepStateView) conentView.findViewById(R.id.bargraph);
        viewSleep = conentView.findViewById(R.id.linear_sleep);
        viewWake = conentView.findViewById(R.id.linear_wake);

        final RelativeLayout relativeLayout = (RelativeLayout) conentView.findViewById(R.id.layout_lativi);


        weeks = mContext.getResources().getStringArray(R.array.week);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.stringToDate(tvContentValue, "yyyy-MM-dd"));
        tvWeek.setText(weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        String s1 = DateUtil.dataToString(calendar.getTime(), "dd/MM/yyyy");
        //calendar.add(Calendar.DAY_OF_MONTH,-1);
        //String s2 = DateUtil.dataToString(calendar.getTime(), "yyyy/MM/dd");
        tvDate.setText(s1);
        final View tv = view;
        final View mViewSleepInfo = conentView.findViewById(R.id.layout_def);
        tvDate.post(new Runnable() {
            @Override
            public void run() {
               /* LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mViewSleepInfo.getLayoutParams();
                float y = mViewSleepInfo.getY();
                FrameLayout.LayoutParams lparams = (FrameLayout.LayoutParams) relativeLayout.getLayoutParams();
                RelativeLayout.LayoutParams fpara = (RelativeLayout.LayoutParams) sleepStateView.getLayoutParams();
                float mt = lparams.topMargin;
                int h = tv.getHeight();
                fpara.height = (int) (h - mt - y - mt / 2);
                sleepStateView.setLayoutParams(fpara);
                sleepInfo();*/
            }
        });
        /*listChartLabel = new LinkedList<>();
        for (int i=0;i<=288;i++){
            int t = i%12;
            if(t == 0){
                int m = i/12;
                listChartLabel.add(m+"h");
            }else {
                listChartLabel.add("");
            }

        }*/
        sleepStateView.setSleepData(mySleepState);
        sleepStateView.post(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x02);
            }
        });
       /* barChart03View.setChartLabels(listChartLabel);
        barChart03View.post(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x02);
            }
        });*/
        registerBroadcast();

    }

    private void sleepInfo() {
        /*SharedPreferences sharedPreferences = mContext.getSharedPreferences(SleepActivity.CONFIG_SLEEP_PATH,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String[] ss = sharedPreferences.getString(SleepActivity.CONFIG_SLEEP_START,"22:00").split(":");
        int sleepHour = Integer.valueOf(ss[0]);
        int sleepMin = Integer.valueOf(ss[1]);
        ss = sharedPreferences.getString(SleepActivity.CONFIG_SLEEP_END,"06:00").split(":");
        int wakeHour = Integer.valueOf(ss[0]);
        int wakeMin = Integer.valueOf(ss[1]);
        float startSleepTime = (sleepHour + sleepMin / 60.0f) / 24.0f;
        float wakeTime = (wakeHour + wakeMin / 60.0f) / 24.0f;

        int paddingLeft = sleepStateView.getPaddingLeft();
        int paddingRight = barChart03View.getChartPaddingRight();
        int dw = barChart03View.getWidth() - paddingLeft - paddingRight;

        FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) viewSleep.getLayoutParams();
        layoutParams1.leftMargin = (int) (paddingLeft + dw * startSleepTime - viewSleep.getWidth() / 2);
        viewSleep.setLayoutParams(layoutParams1);
        layoutParams1 = (FrameLayout.LayoutParams) viewWake.getLayoutParams();
        layoutParams1.leftMargin = (int) (paddingLeft + dw *wakeTime - viewWake.getWidth() / 2);
        viewWake.setLayoutParams(layoutParams1);*/
		/*}else {
			mViewSleepInfo.setVisibility(View.GONE);
		}*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unRegisterBroadcst();
        if(mPvView != null){
            mPvView.stopAnimation();
        }
        if(progress != null){
            progress.stopAnimation();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mScrollView = inflater.inflate(R.layout.fragment_scroll_exercise,container,false);
        //mScrollView = (XScrollView) view.findViewById(R.id.scroll_view);
        conentView = inflater.inflate(R.layout.fragment_sleep_fitness, null);
        mScrollView.setView(conentView);
        mScrollView.setPullRefreshEnable(true);
        mScrollView.setPullLoadEnable(false);
        mScrollView.setAutoLoadEnable(false);
        mScrollView.setIXScrollViewListener(this);
        return mScrollView;
    }

    private void loadData() {
        MyApp.getIntance().executorService.submit(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });
    }

    private synchronized void initData() {
        MainService mainService = MainService.getInstance(mContext);
        int total = 0;
        List<Double> historySteps = new ArrayList<>();
        List<Integer> listChartColor = new ArrayList<>();
        int[] sleepState = new int[288];
        if (mainService != null) {
            BaseDevice baseDevice = mainService.getCurrentDevice();

            if (baseDevice != null) {

                /*Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateUtil.stringToDate(tvContentValue,"yyyy-MM-dd"));
                calendar.add(Calendar.DAY_OF_MONTH,-1);
                String tpdate = DateUtil.dataToString(calendar.getTime(),"yyyy-MM-dd");*/

                /*String sql = "datetime("+ DbHistorySport.COLUMN_DATE+")>=datetime('"+tpdate+" 12:00:00') and "
                        + "datetime("+ DbHistorySport.COLUM
                        N_DATE+")<datetime('"+tvContentValue+" 12:00:00') and "
                        + DbHistorySport.COLUMN_MAC+"=?";*/
                String sql = DbHistorySport.COLUMN_DATE+" like ? and "+DbHistorySport.COLUMN_MAC+"=?";
                List<HistorySport> tpH = DbHistorySport.getInstance().findAll(sql,new String[]{tvContentValue+"%",baseDevice.getMac()},null);

                if(tpH == null){
                    //historySteps  = LIST_SLEEP_D;
                }else {
                    int tpv = 0;
                    for(int i=0;i<tpH.size();i++){
                        tpv = tpH.get(i).getSleepState();
                        if(tpv == 0){
                            listChartColor.add(0xffd9edc9);
                            //historySteps.add(0d);
                            sleepState[i] = 0;
                        }else if(tpv == 0x80){//深睡
                            total++;
                            listChartColor.add(0xff4e83b2);
                            //historySteps.add(130d);
                            sleepState[i] = 4;
                        }else if(tpv == 0x81){///浅睡
                            total++;
                            listChartColor.add(0xffff9565);
                            //historySteps.add(100d);
                            sleepState[i] = 3;
                        }else if(tpv == 0x82){///极浅睡
                            total++;
                            listChartColor.add(0xff87cd51);
                            //historySteps.add(90d);
                            sleepState[i] = 2;
                        }else if(tpv == 0x83){///醒着的
                            listChartColor.add(0xff87cd51);
                            //historySteps.add(60d);
                            sleepState[i] = 1;
                            total++;
                        }
                    }
                    if(historySteps.size()<=288){
                        int size = historySteps.size();
                        for (int i=size;i<=288;i++){
                            listChartColor.add(0xffd9edc9);
                            historySteps.add(0d);
                        }
                    }

                }
            } else {
               /* historySteps.clear();
                historySteps = LIST_SLEEP_D;
                listChartColor.clear();*/
            }
        } else {
            /*historySteps.clear();
            historySteps = LIST_SLEEP_D;
            listChartColor.clear();*/
        }
        List list = new ArrayList();
        Message msg = Message.obtain();
        /*List<BarData> chartData = new LinkedList<>();
        chartData.add(new BarData("",historySteps,listChartColor, Color.rgb(53,169,239)));*/
        list.add(sleepState);
        list.add(total);
        msg.what = 0x01;
        msg.obj = list;
        handler.sendMessage(msg);
    }

    //private List<BarData> sleepHistBarData = null;
    private int[] mySleepState;
    private int totalTime = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    List list = (List) msg.obj;
                    mySleepState = (int[]) list.get(0);
                    totalTime = (Integer) list.get(1)*5;
                    handler.sendEmptyMessage(0x02);
                    break;
                case 0x02:
                    int sleepHour = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_HOUR,8);
                    int sleepMin = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_MIN,0);
                    int percent = 0;
                    if(sleepHour != 0 || sleepMin != 0){
                        percent = (totalTime*100)/(sleepHour*60+sleepMin);
                    }else {
                        percent = 0;
                    }
                    if(sleepStateView != null){
                        progress.setCurrentValues(percent>100?100:percent);
                        mPvView.setProgress(percent>100?100:percent);
                        tvSleepPercent.setText(percent+"%");
                        tvSleepTime.setText(String.format("%02d",totalTime/60)+":"+String.format("%02d",totalTime%60));
                        sleepStateView.setSleepData(mySleepState == null?(new int[288]):mySleepState);
                        /*barChart03View.setChartData(sleepHistBarData);
                        barChart03View.setChartLabels(listChartLabel);
                        barChart03View.initView();
                        barChart03View.postInvalidate();*/
                    }
                    break;
            }
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
    }

    static String tp;

    static {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 288; i++) {
            builder.append("0,");
        }
        tp = builder.substring(0, builder.toString().length() - 1);
    }


    @Override
    public void onResume() {
        super.onResume();
        handler.sendEmptyMessage(0x02);
    }

    @Override
    public void onClick(View v) {
        //((SleepFragmentActivity)getParentFragment()).finishActivity();
    }

    @Override
    public void onRefresh() {
        MainService mainService = MainService.getInstance(mContext);
        if(mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED){
            if(!mainService.startSyncData()){
                mScrollView.stopRefresh();
                Toast.makeText(mContext,mContext.getString(R.string.header_hint_refresh_loading),Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,mContext.getString(R.string.please_bind),Toast.LENGTH_LONG).show();
            mScrollView.stopRefresh();
        }
    }

    private boolean isRegisterBroadcast = false;
    private void registerBroadcast(){
        isRegisterBroadcast = true;
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_SYNC_COMPLETED);
        filter.addAction(BaseController.ACTION_REAL_DATA);
        mContext.registerReceiver(mReceiver,filter);*/
        EventBus.getDefault().register(this);
    }

    public void unRegisterBroadcst(){
        if(mContext != null && isRegisterBroadcast) {
            /*mContext.unregisterReceiver(mReceiver);
            isRegisterBroadcast = false;*/
            EventBus.getDefault().unregister(this);
        }
    }



    @Override
    public void onLoadMore() {

    }

    public void onEventMainThread(Intent intent) {
        String action = intent.getAction();
        if(action.equals(MainService.ACTION_CONNECTE_CHANGE)){
            if(mScrollView != null){
                mScrollView.stopRefresh();
            }
            int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
            if(state == BaseController.STATE_CONNECTED){
                loadData();
            }
        }else if(action.equals(MainService.ACTION_SYNC_COMPLETED)) {
            int state = intent.getIntExtra(MainService.EXTRA_SYNC_STATE, BaseController.STATE_SYNC_COMPLETED);
            if(state != BaseController.STATE_SYNCING) {
                if (mScrollView != null) {
                    mScrollView.stopRefresh();
                }
                loadData();
            }
        }else if(action.equals(BaseController.ACTION_REAL_DATA)){
            String date = intent.getStringExtra(BaseController.EXTRA_REAL_DATE);
            if (date.equals(tvContentValue)) {
                loadData();
            }
        }
    }
}