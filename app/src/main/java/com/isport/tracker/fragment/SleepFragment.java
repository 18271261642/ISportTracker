package com.isport.tracker.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.database.DbHistorySport;
import com.isport.isportlibrary.database.DbHistorySportN;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.HistorySport;
import com.isport.isportlibrary.entry.HistorySportN;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.MyApp;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.ContinousBarChartEntity;
import com.isport.tracker.entity.ContinousBarChartTotalEntity;
import com.isport.tracker.entity.sleepResultBean;
import com.isport.tracker.main.settings.sport.SleepActivity;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.ContinousBarChartView;
import com.isport.tracker.view.SleepStateView12;
import com.isport.tracker.view.SleepView;
import com.isport.tracker.view.TasksCompletedView;
import com.isport.tracker.view.XScrollView;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SleepFragment extends Fragment implements XScrollView.IXScrollViewListener, View.OnClickListener {


    private static final String TAG = SleepFragment.class.getSimpleName();
    private Context mContext;
    private String tvContentValue;
    private String[] weeks;
    private TextView tvWeek, tvDate;
    private TextView tvSleepTime, tvSleepPercent;
    private TasksCompletedView mPvView;
    //    private SleepStateView sleepStateView;
    private SleepStateView12 sleepStateView;
    private View viewSleep, viewWake;
    private XScrollView mScrollView;
    private View conentView;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int mPosition;
    private int mCount;
    private SleepView sleepView;
    private boolean isNewBeat;
    private ContinousBarChartView continousBarChartView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static SleepFragment newInstance(int position, int count) {
        SleepFragment fragmentFirst = new SleepFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("count", count);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mPosition = getArguments().getInt("position");
        mCount = getArguments().getInt("count");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * (mCount - mPosition - 2));
        tvContentValue = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
        Calendar calendar1 = Calendar.getInstance();
        sharedPreferences = mContext.getSharedPreferences(SleepActivity.CONFIG_SLEEP_PATH, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        loadData();
    }

    TextView tv_sleep_count;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvDate = (TextView) conentView.findViewById(R.id.main_fragment_text_date);
        tvWeek = (TextView) conentView.findViewById(R.id.main_fragment_text_Week);
        mPvView = (TasksCompletedView) conentView.findViewById(R.id.tasks_view);
        tvSleepPercent = (TextView) conentView.findViewById(R.id.tv_sleep_percent);
        tvSleepTime = (TextView) conentView.findViewById(R.id.exercise_time);
        sleepStateView = (SleepStateView12) conentView.findViewById(R.id.bargraph);
        sleepView = (SleepView) conentView.findViewById(R.id.sleep_content_sleepview);
        viewSleep = conentView.findViewById(R.id.linear_sleep);
        viewWake = conentView.findViewById(R.id.linear_wake);
        tv_sleep_count = conentView.findViewById(R.id.tv_sleep_count);
        continousBarChartView = conentView.findViewById(R.id.continousBarChartView);

        final RelativeLayout relativeLayout = (RelativeLayout) conentView.findViewById(R.id.layout_lativi);


        weeks = mContext.getResources().getStringArray(R.array.week);
        Calendar calendar = Calendar.getInstance();
        String dt = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
        calendar.add(Calendar.DAY_OF_MONTH, -1 * (mCount - mPosition - 2));
//        tvWeek.setText(weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
//        String s1 = DateUtil.dataToString(calendar.getTime(), "dd/MM/yyyy");
//        tvDate.setText(s1);
        String date = DateUtil.dataToString(calendar.getTime(), "dd/MM/yyyy");
        String week = weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        date = DateUtil.dataToString(calendar.getTime(), "dd/MM/yyyy") + "-" + date;
        week = weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1] + "-" + week;
        tvWeek.setText(week);
        tvDate.setText(date);


        sleepStateView.setSleepData(sleepStateAll, 288);
        sleepView.setSleepState(sleepState, 288);
        sleepStateView.post(new Runnable() {
            @Override
            public void run() {
//                handler.sendEmptyMessage(0x02);
                handler.sendEmptyMessage(0x03);
            }
        });

        registerBroadcast();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Log.e(TAG, "onDestroyView ");
        unRegisterBroadcst();
        if (mPvView != null) {
            mPvView.stopAnimation();
        }
        if (handler.hasMessages(0x01))
            handler.removeMessages(0x01);
        if (handler.hasMessages(0x02))
            handler.removeMessages(0x02);
        if (handler.hasMessages(0x03))
            handler.removeMessages(0x03);
        if (handler.hasMessages(0x04))
            handler.removeMessages(0x04);
        if (mScrollView != null) {
            mScrollView.removeAllViews();
        }
        tvWeek = null;
        tvDate = null;
        tvSleepPercent = null;
        tvSleepTime = null;
        mPvView = null;
        mScrollView = null;
        viewSleep = null;
        viewWake = null;
        conentView = null;
        sleepStateView = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        mScrollView = (XScrollView) inflater.inflate(R.layout.fragment_scroll_exercise, container, false);
        //mScrollView = (XScrollView) view.findViewById(R.id.scroll_view);

        conentView = inflater.inflate(R.layout.fragment_sleep, null);
        mScrollView.setView(conentView);
        mScrollView.setPullRefreshEnable(true);
        mScrollView.setPullLoadEnable(false);
        mScrollView.setAutoLoadEnable(false);
        mScrollView.setIXScrollViewListener(this);

        return mScrollView;
    }

    private void loadData() {

        MyApp.getInstance().executorService.submit(new Runnable() {
            @Override
            public void run() {
                //initData();
                //Log.e("sleepFragment", " loadData== ");
                readHistdata();
            }
        });
    }


    //***************************************smart active逻辑********************************************//

    List<HistorySport> listHistSport = Collections.synchronizedList(new ArrayList<HistorySport>());
    List<HistorySportN> listHistSportN = Collections.synchronizedList(new ArrayList<HistorySportN>());

    /**
     * 获取12-12睡眠
     */
    public synchronized void readHistdata() {
        listHistSport.clear();
        listHistSportN.clear();
        MainService mainService = MainService.getInstance(getContext());
        BaseDevice baseDevice = mainService.getCurrentDevice();
        BaseController currentController = mainService.getCurrentController();
        DbHistorySport dbHistorySport = DbHistorySport.getInstance(mContext);
        DbHistorySportN dbHistorySportN = DbHistorySportN.getInstance(mContext);
        if (baseDevice != null) {
            Date date = UtilTools.string2Date(tvContentValue, "yyyy-MM-dd");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            String dtStr = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
            // Log.e("dtStr:", dtStr + "tvContentValue:" + tvContentValue);
            //  Log.e("sleepStatus == ", ((CmdController) currentController).getVersion() + "版本" + baseDevice.getName());
            if ((baseDevice.getName().contains("BEAT") && ((CmdController) currentController).getVersion() >= 91.70f) || (baseDevice.getName().contains("REFLEX") && ((CmdController) currentController).getVersion() >= 91.02f) || (baseDevice.getName().contains("W520") && ((CmdController) currentController).getVersion() >= 91.63f)) {
                //  Log.e("sleepStatus == ", "高版本");
                isNewBeat = true;
                String sql = "datetime(" + DbHistorySportN.COLUMN_DATE + ")>=datetime('" + dtStr + " 12:00:00') and "
                        + "datetime(" + DbHistorySportN.COLUMN_DATE + ")<datetime('" + tvContentValue + " 12:00:00') and "
                        + DbHistorySportN.COLUMN_MAC + "=?";
                List<HistorySportN> tp = dbHistorySportN.findAll(sql, new String[]{baseDevice.getMac()}, null);
                if (tp != null && tp.size() > 0) {
                    listHistSportN = tp;
                    int duration = 300000;
                    if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) { //194协议不同
                        if ((baseDevice.getDeviceType() == BaseDevice.TYPE_MILLIONPEDOMETER || baseDevice.getDeviceType()
                                == BaseDevice.TYPE_P118)) {
                            duration = 1800000;
                        }
                        final long startt = DateUtil.stringToDate(listHistSport.get(0).getDateString(), "yyyy-MM-dd " +
                                "HH:mm").getTime();
                        long entt = DateUtil.stringToDate(listHistSport.get(listHistSport.size() - 1).getDateString(),
                                "yyyy-MM-dd HH:mm").getTime();
                        if (entt - startt > (listHistSport.size() - 1) * duration) {
                            List<HistorySport> tplist = new ArrayList<>();
                            tplist.addAll(listHistSport);
                            int startIndex = 0;
                            for (int i = 0; i < tplist.size() - 1; i++) {
                                long startttt = DateUtil.stringToDate(tplist.get(i).getDateString(), "yyyy-MM-dd HH:mm")
                                        .getTime();
                                entt = DateUtil.stringToDate(tplist.get(i + 1).getDateString(), "yyyy-MM-dd HH:mm")
                                        .getTime();
                                int tpl = (int) ((entt - startttt) / duration);
                                if (tpl > 1) {
                                    for (int j = 0; j < tpl; j++) {
                                        listHistSport.add(++startIndex, new HistorySport("", "", 0, 0));
                                    }
                                } else {
                                    startIndex++;
                                }
                            }
                        }
                        // TODO: 2018/4/26 要处理
                        Calendar tttcal = Calendar.getInstance();
                        tttcal.set(Calendar.MILLISECOND, 0);
                        tttcal.set(Calendar.HOUR_OF_DAY, 0);
                        tttcal.set(Calendar.MINUTE, 0);
                        tttcal.set(Calendar.SECOND, 0);
                        long dtv = (startt - tttcal.getTime().getTime()) / duration;
                        if (dtv >= 1) {
                            for (int m = 0; m < dtv; m++) {
                                listHistSport.add(0, new HistorySport("", "", 0, 0));
                            }
                        }
                    }
                }
            } else {
                // Log.e("sleepStatus == ", "低版本");
                String sql = "datetime(" + DbHistorySport.COLUMN_DATE + ")>=datetime('" + dtStr + " 12:00:00') and "
                        + "datetime(" + DbHistorySport.COLUMN_DATE + ")<datetime('" + tvContentValue + " 12:00:00') and "
                        + DbHistorySport.COLUMN_MAC + "=?";
                List<HistorySport> tp = dbHistorySport.findAll(sql, new String[]{baseDevice.getMac()}, null);
              /*  for (int i = 0; i < tp.size(); i++) {
                    Log.e("saveOrUpdate", tp.get(i).toString());
                }*/
                if (tp != null && tp.size() > 0) {
                    listHistSport = tp;
                    int duration = 300000;
                    if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {//194协议不同
                        if ((baseDevice.getDeviceType() == BaseDevice.TYPE_MILLIONPEDOMETER || baseDevice.getDeviceType()
                                == BaseDevice.TYPE_P118)) {
                            duration = 1800000;
                        }
                        final long startt = DateUtil.stringToDate(listHistSport.get(0).getDateString(), "yyyy-MM-dd " +
                                "HH:mm").getTime();
                        long entt = DateUtil.stringToDate(listHistSport.get(listHistSport.size() - 1).getDateString(),
                                "yyyy-MM-dd HH:mm").getTime();
                        if (entt - startt > (listHistSport.size() - 1) * duration) {
                            List<HistorySport> tplist = new ArrayList<>();
                            tplist.addAll(listHistSport);
                            int startIndex = 0;
                            for (int i = 0; i < tplist.size() - 1; i++) {
                                long startttt = DateUtil.stringToDate(tplist.get(i).getDateString(), "yyyy-MM-dd HH:mm")
                                        .getTime();
                                entt = DateUtil.stringToDate(tplist.get(i + 1).getDateString(), "yyyy-MM-dd HH:mm")
                                        .getTime();
                                int tpl = (int) ((entt - startttt) / duration);
                                if (tpl > 1) {
                                    for (int j = 0; j < tpl; j++) {
                                        listHistSport.add(++startIndex, new HistorySport("", "", 0, 0));
                                    }
                                } else {
                                    startIndex++;
                                }
                            }
                        }
                        // TODO: 2018/4/26 要处理
                        Calendar tttcal = Calendar.getInstance();
                        tttcal.set(Calendar.MILLISECOND, 0);
                        tttcal.set(Calendar.HOUR_OF_DAY, 0);
                        tttcal.set(Calendar.MINUTE, 0);
                        tttcal.set(Calendar.SECOND, 0);
                        long dtv = (startt - tttcal.getTime().getTime()) / duration;
                        if (dtv >= 1) {
                            for (int m = 0; m < dtv; m++) {
                                listHistSport.add(0, new HistorySport("", "", 0, 0));
                            }
                        }
                    }
                }
            }
        }

        parserSportData();
    }

    int[] sleepState = null;///睡觉状态,醒着的状态算作没睡
    int[] sleepStateAll = null;//睡眠状态，醒着的状态算作睡觉
    int[] allSleep = null;
    String[] allSleepTime;
    String startSleepTime;//开始睡觉时间
    String endSleepTime;///结束睡觉时间
    List<Integer> listAwake = Collections.synchronizedList(new ArrayList<Integer>());///醒着的
    List<Integer> listDeep = Collections.synchronizedList(new ArrayList<Integer>());//深睡
    List<Integer> listLight = Collections.synchronizedList(new ArrayList<Integer>());//浅睡
    List<Integer> listElight = Collections.synchronizedList(new ArrayList<Integer>());//极浅睡

    public synchronized void parserSportData() {
        Date date = UtilTools.string2Date(tvContentValue, "yyyy-MM-dd");
        ///前一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        String dtStrs = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
        // Log.e("parserSportData:dtStrs:", dtStrs + "tvContentValue:" + tvContentValue);
        HashMap<String, Integer> mapHistSport = new HashMap<>();
        HashSet hashSet = new HashSet();
        if (isNewBeat) {
            if (listHistSportN != null) {
                // Log.e("SleepView == ", "listHistSportN != null");
                for (int i = 0; i < listHistSportN.size(); i++) {
                    HistorySportN sport = listHistSportN.get(i);
                    mapHistSport.put(sport.getDateString(), i);
                    hashSet.add(listHistSportN.get(i).getDateString());
                    // BaseController.logBuilder.append("lsport.getDateString()" + sport.getDateString() + "state:" + sport.getSleepState() + "index:" + sport.getIndex());
                }
                List<HistorySportN> listTp = new ArrayList<>();
                startSleepTime = "";
                endSleepTime = "";
                sleepState = new int[1440];
                sleepStateAll = new int[1440];
                allSleep = new int[1440];
                allSleepTime = new String[1440];
                for (int i = 0; i < 1440; i++) {
                    sleepState[i] = -1;
                }
                boolean isStart = false;
                int endIndex = 0;
                int startIndex = 0;
                listAwake.clear();
                listDeep.clear();
                listElight.clear();
                listLight.clear();
                //以一分钟间隔算为1440
                for (int i = 0; i < 1440; i++) {
                    String dtStr = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd HH:mm");

                    HistorySportN histSport = null;
                    if (hashSet.contains(dtStr)) {
                        histSport = listHistSportN.get(mapHistSport.get(dtStr));
                    } else {
                        histSport = new HistorySportN(null, null, 0, 0, 0, 0);
                    }
                    int state = histSport.getSleepState();
                    // Log.e("SleepView state ", i + " == index != null == " + dtStr + "  ==state == " + state);
                    if (state > 0 && !isStart) {
                        isStart = true;
                        startIndex = i;
                        startSleepTime = dtStr;
                    }

                    if (state > 0) {
                        endIndex = i;
                        endSleepTime = dtStr;
                    }

                    if (state == 131) {//睡醒
                        sleepStateAll[i] = 1;
                        listAwake.add(i);
                        sleepState[i] = 0;
                        allSleep[i] = 1;
                    } else if (state == 130) {//极浅睡
                        sleepStateAll[i] = 2;
                        listElight.add(i);
                        sleepState[i] = 3;
                        allSleep[i] = 1;
                    } else if (state == 129) {//浅睡
                        sleepStateAll[i] = 3;
                        listLight.add(i);
                        sleepState[i] = 2;
                        allSleep[i] = 1;
                    } else if (state == 128) {///深睡
                        sleepStateAll[i] = 4;
                        listDeep.add(i);
                        sleepState[i] = 1;
                        allSleep[i] = 1;
                    } else {
                        sleepState[i] = -1;
                        allSleep[i] = 0;
                    }
//                sleepState[i] = state>0?(3 - (state-0x0080)):state;
                    calendar.add(Calendar.MINUTE, 1);
                    allSleepTime[i] = UtilTools.date2String(calendar.getTime(), "HH:mm");
                }


            }
        } else {
            if (listHistSport != null) {
                for (int i = 0; i < listHistSport.size(); i++) {
                    HistorySport sport = listHistSport.get(i);
                    mapHistSport.put(sport.getDateString(), i);
                    hashSet.add(listHistSport.get(i).getDateString());
                }
                List<HistorySport> listTp = new ArrayList<>();
                startSleepTime = "";
                endSleepTime = "";
                sleepState = new int[288];
                sleepStateAll = new int[288];
                allSleep = new int[288];
                for (int i = 0; i < 288; i++) {
                    sleepState[i] = -1;
                }
                boolean isStart = false;
                int endIndex = 0;
                int startIndex = 0;
                listAwake.clear();
                listDeep.clear();
                listElight.clear();
                listLight.clear();
                for (int i = 0; i < 288; i++) {
                    String dtStr = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd HH:mm");
                    // Log.e(TAG, "---flag----" + i + ":2019.8.11" + dtStr);
                    HistorySport histSport = null;
                    if (hashSet.contains(dtStr)) {
                        histSport = listHistSport.get(mapHistSport.get(dtStr));
                        // Log.e(TAG, "---flag----" + histSport.toString());
                    } else {
                        histSport = new HistorySport(null, null, 0, 0);
                    }
                    int state = histSport.getSleepState();
                    if (state > 0 && !isStart) {
                        isStart = true;
                        startIndex = i;
                        startSleepTime = dtStr;
                    }

                    if (state > 0) {
                        endIndex = i;
                        endSleepTime = dtStr;
                    }

                    if (state == 0x0083) {//睡醒
                        sleepStateAll[i] = 1;
                        listAwake.add(i);
                        sleepState[i] = 0;
                        allSleep[i] = 1;
                    } else if (state == 0x0082) {//极浅睡
                        sleepStateAll[i] = 2;
                        listElight.add(i);
                        sleepState[i] = 3;
                        allSleep[i] = 1;
                    } else if (state == 0x0081) {//浅睡
                        sleepStateAll[i] = 3;
                        listLight.add(i);
                        sleepState[i] = 2;
                        allSleep[i] = 1;
                    } else if (state == 0x0080) {///深睡
                        sleepStateAll[i] = 4;
                        listDeep.add(i);
                        sleepState[i] = 1;
                        allSleep[i] = 1;
                    } else {
                        sleepState[i] = -1;
                        allSleep[i] = 0;
                    }
//                sleepState[i] = state>0?(3 - (state-0x0080)):state;

                    calendar.add(Calendar.MINUTE, 5);
                }
            }


         /*   String strCount = getActivity().getResources().getString(R.string.deep_sleep) + ":" + listDeep.size() * 5 + "min," + getActivity().getResources().getString(R.string.light_sleep) + ":" + listLight.size() * 5 + "min," + getActivity().getResources().getString(R.string.elight_sleep) + ":" + listElight.size() * 5 + "min," + getActivity().getResources().getString(R.string.awake) + ":" + listAwake.size() * 5 + "min";
            tv_sleep_count.setText(strCount);*/

            /**
             *     <string name="deep_sleep">深睡</string>
             <string name="light_sleep">浅睡</string>
             <string name="elight_sleep">极浅睡</string>
             <string name="awake">睡醒</string>

             */
        }

        //需要去解析出來睡眠状态
        //Log.e(TAG, "---flag----" + allSleep.length);
        parserSleepTimes(allSleep);
        handler.sendEmptyMessage(0x02);
    }

    ArrayList<sleepResultBean> SleepDuration = new ArrayList<>();
    ArrayList<sleepResultBean> resultDuration = new ArrayList<>();

    //所有的睡眠时间段 记录前一个后一个状态
    private synchronized void parserSleepTimes(int[] tempsleepState) {
        SleepDuration.clear();
        int first = 0;
        int next = 0;
        int firstIndex = 0;
        int nextIndex = 0;
        int startIndex = 0;
        int endIndex = 0;
        String startTime = null, endTime = null;
        for (int i = 0; i < tempsleepState.length; i++) {
            // Log.e("SleepDuration ", "sleepState[" + i + "]" + sleepState[i] + "allSleepTime[" + i + "]" + allSleepTime[i]);

            if (i == 0) {
                first = tempsleepState[0];
                next = tempsleepState[0];
                firstIndex = 0;
                nextIndex = 0;
            } else {
                next = first;
                nextIndex = firstIndex - 1;
                first = tempsleepState[i];
                firstIndex = i;
            }
            if (first != next) {
                if (first == 1) {
                    //开始时间 ,上一个数据开始index
                    startTime = allSleepTime[nextIndex];
                    startIndex = nextIndex;
                    endTime = null;

                } else {
                    //结束时间 ,
                    endTime = allSleepTime[nextIndex];
                    endIndex = nextIndex;
                }
                if (!TextUtils.isEmpty(endTime) && !TextUtils.isEmpty(startTime)) {
                    sleepResultBean sleepResultBean = new sleepResultBean();
                    sleepResultBean.startIndex = startIndex;
                    sleepResultBean.endIndex = endIndex;
                    sleepResultBean.startTime = startTime;
                    sleepResultBean.endTime = endTime;
                    SleepDuration.add(sleepResultBean);
                }
            }

        }
        resultDuration.clear();
        sleepResultBean temp;
        if (SleepDuration.size() == 0) {
            return;
        }
        temp = SleepDuration.get(0);
        // Log.e("SleepDuration.size ", "SleepDuration.size()" + SleepDuration.size());
        for (int i = 1; i < SleepDuration.size(); i++) {
            if (SleepDuration.get(i).startIndex - temp.endIndex < 30) {
                temp.endIndex = SleepDuration.get(i).endIndex;
                temp.endTime = SleepDuration.get(i).endTime;
            } else {
                sleepResultBean temps = new sleepResultBean();
                temps.endTime = temp.endTime;
                temps.startIndex = temp.startIndex;
                temps.endIndex = temp.endIndex;
                temps.startTime = temp.startTime;
                resultDuration.add(temps);
                temp = SleepDuration.get(i);
            }
        }
        resultDuration.add(temp);


        for (int i = resultDuration.size() - 1; i > 0; i--) {
            int tempEndIndex = resultDuration.get(i).endIndex;
            // sleepStateAll[resultDuration.get(i).endIndex]==1 睡醒
            // Log.e("tempEndIndex", "sleepState:" + resultDuration.get(i).endIndex + "---resultDuration" + resultDuration.get(i).endIndex + " resultDuration.get(i).entime" + resultDuration.get(i).endTime);

            for (int j = tempEndIndex - 1; j >= resultDuration.get(i).startIndex; j--) {
                //  Log.e("tempEndIndex----------", "sleepStateAll[j]" + sleepStateAll[j]);

                if (sleepStateAll[j] == 1) {
                    //需要往前面移动一位
                    resultDuration.get(i).endIndex = j;
                    resultDuration.get(i).endTime = allSleepTime[j];
                    //sleepStateAll[j] = 0;
                    // Log.e("resultDuration", "sleepState:" + sleepState[j] + "---resultDuration" + resultDuration.get(i).endIndex + "" + resultDuration.get(i).endTime + " resultDuration.get(i).endIndex:" + resultDuration.get(i).endIndex + "resultDuration.get(i).endTime:" + resultDuration.get(i).endTime);
                } else {
                    break;
                }
            }
        }


        //  Log.e("SleepDuration ", "resultDuration" + resultDuration.size());
        handler.sendEmptyMessage(0x04);

    }

    //***************************************smart active逻辑********************************************//


    private synchronized void initData() {
        MainService mainService = MainService.getInstance(mContext);
        int total = 0;
        int light = 0;
        int deep = 0;
        int elight = 0;
        List<Double> historySteps = new ArrayList<>();
        List<Integer> listChartColor = new ArrayList<>();
        int[] sleepState = new int[288];
        if (mainService != null) {
            BaseDevice baseDevice = mainService.getCurrentDevice();

            if (baseDevice != null) {
                // Log.e(TAG, "***tvContentValue***" + tvContentValue + "***MAC***" + baseDevice.getMac());
                String sql = DbHistorySport.COLUMN_DATE + " like ? and " + DbHistorySport.COLUMN_MAC + "=?";
                List<HistorySport> tpH = DbHistorySport.getInstance(mContext).findAll(sql, new
                                String[]{tvContentValue
                                + "%", baseDevice
                                .getMac()},
                        "datetime(" + DbHistorySport
                                .COLUMN_DATE + ") ASC");

                if (tpH == null || tpH.size() == 0) {
                    //  Log.e(TAG, "***getSleepState***为空");

                    //historySteps  = LIST_SLEEP_D;
                } else {
                    int duration = 300000;
                    if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                        if ((baseDevice.getDeviceType() == BaseDevice.TYPE_MILLIONPEDOMETER || baseDevice
                                .getDeviceType() == BaseDevice.TYPE_P118)) {
                            duration = 1800000;
                        }
                        final long startt = DateUtil.stringToDate(tpH.get(0).getDateString(), "yyyy-MM-dd HH:mm")
                                .getTime();
                        long entt = DateUtil.stringToDate(tpH.get(tpH.size() - 1).getDateString(), "yyyy-MM-dd " +
                                "HH:mm").getTime();
                        if (entt - startt > (tpH.size() - 1) * duration) {
                            List<HistorySport> tplist = new ArrayList<>();
                            tplist.addAll(tpH);
                            int startIndex = 0;
                            for (int i = 0; i < tplist.size() - 1; i++) {
                                long startttt = DateUtil.stringToDate(tplist.get(i).getDateString(), "yyyy-MM-dd " +
                                        "HH:mm").getTime();
                                entt = DateUtil.stringToDate(tplist.get(i + 1).getDateString(), "yyyy-MM-dd HH:mm")
                                        .getTime();
                                int tpl = (int) ((entt - startttt) / duration);
                                if (tpl > 1) {
                                    for (int j = 0; j < tpl; j++) {
                                        tpH.add(++startIndex, new HistorySport("", "", 0, 0));
                                    }
                                } else {
                                    startIndex++;
                                }
                            }
                        }
                        // TODO: 2018/4/26 要处理
                        Calendar tttcal = Calendar.getInstance();
                        tttcal.set(Calendar.MILLISECOND, 0);
                        tttcal.set(Calendar.HOUR_OF_DAY, 0);
                        tttcal.set(Calendar.MINUTE, 0);
                        tttcal.set(Calendar.SECOND, 0);
                        long dtv = (startt - tttcal.getTime().getTime()) / duration;
                        if (dtv >= 1) {
                            for (int m = 0; m < dtv; m++) {
                                tpH.add(0, new HistorySport("", "", 0, 0));
                            }
                        }

                    }

                    int tpv = 0;
                    for (int i = 0; i < tpH.size(); i++) {
                        tpv = tpH.get(i).getSleepState();
                        //Log.e(TAG, "***getSleepState***" + tpv);
                        if (tpv == 0) {
                            listChartColor.add(0xffd9edc9);
                            //historySteps.add(0d);
                            sleepState[i] = 0;
                        } else if (tpv == 0x80) {//深睡
                            total++;
                            deep++;
                            listChartColor.add(0xff4e83b2);
                            //historySteps.add(130d);
                            sleepState[i] = 4;
                        } else if (tpv == 0x81) {///浅睡
                            total++;
                            light++;
                            listChartColor.add(0xffff9565);
                            //historySteps.add(100d);
                            sleepState[i] = 3;
                        } else if (tpv == 0x82) {///极浅睡
                            total++;
                            elight++;
                            listChartColor.add(0xff87cd51);
                            //historySteps.add(90d);
                            sleepState[i] = 2;
                        } else if (tpv == 0x83) {///醒着的
                            listChartColor.add(0xff87cd51);
                            //historySteps.add(60d);
                            sleepState[i] = 1;
                            total++;
                            elight++;
                        }
                    }
                    if (historySteps.size() <= 288) {
                        int size = historySteps.size();
                        for (int i = size; i <= 288; i++) {
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
        list.add(light);
        list.add(deep);
        list.add(elight);
        msg.what = 0x01;
        msg.obj = list;
        handler.sendMessage(msg);
    }

    //private List<BarData> sleepHistBarData = null;
    private int[] mySleepState;
    private int totalTime = 0;
    private int lightTime = 0;
    private int deepTime = 0;
    private int elightTime = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    if (isNewBeat) {
                        //1一个为间隔
                        List list = (List) msg.obj;
                        mySleepState = (int[]) list.get(0);
                        totalTime = (Integer) list.get(1);
                        lightTime = (Integer) list.get(2);
                        deepTime = (Integer) list.get(3);
                        elightTime = (Integer) list.get(4);
                    } else {
                        List list = (List) msg.obj;
                        mySleepState = (int[]) list.get(0);
                        totalTime = (Integer) list.get(1) * 5;
                        lightTime = (Integer) list.get(2) * 5;
                        deepTime = (Integer) list.get(3) * 5;
                        elightTime = (Integer) list.get(4) * 5;
                    }
                    updateUIWithData();
                    break;
                case 0x02:
//                    updateUIWithData();
                    updateUI();
                    break;
                case 0x03:
//                    updateUIWithData();
                    updateUI();
                    break;
                case 0x04:
                    break;
            }
        }
    };


    private void setContinousBarChartData(int[] sleepArry) {

        ContinousBarChartTotalEntity barChartTotalEntity = new ContinousBarChartTotalEntity();
        List<ContinousBarChartEntity> datas = new ArrayList<>();

        for (int i = 0; i <= sleepArry.length - 1; i++) {//i 最大为720
            int s = 0;
            if (sleepArry[i] == 0 || sleepArry[i] == 250 || sleepArry[i] == -1) {
                s = 0;
            } else if (sleepArry[i] == 251) {
                s = 1;
            } else if (sleepArry[i] == 252) {
                s = 2;
            } else if (sleepArry[i] == 253) {
                s = 3;
            }
            datas.add(new ContinousBarChartEntity(1, 200, sleepArry[i]));
        }
        continousBarChartView.setOnItemBarClickListener(new ContinousBarChartView.OnItemBarClickListener() {
            @Override
            public void onClick(int position, int hour, int minute) {
                //setHourMinute(hour, minute);
            }
        });
        barChartTotalEntity.startTime = "12";
        barChartTotalEntity.endTime = "12";
        barChartTotalEntity.continousBarChartEntitys = datas;
        continousBarChartView.setSleep(resultDuration);

        continousBarChartView.setData(barChartTotalEntity, "分组", "数量");
        continousBarChartView.startAnimation();
    }


    public void updateUI() {

        if (isNewBeat) {
            if (continousBarChartView != null) {
                continousBarChartView.setVisibility(View.VISIBLE);
                if (sleepStateView != null) {
                    sleepStateView.setVisibility(View.INVISIBLE);
                }
                //Log.e("sleepStateAll ", "continousBarChartView");
                if (sleepStateAll != null) {
                    for (int i = 0; i < sleepStateAll.length; i++) {
                        //  Log.e("sleepStateAll ", "sleepStateAll[" + i + "]" + sleepStateAll[i]);
                        //Log.e("sleepStateAll ", "sleepStateAll[" + i + "]" + sleepState[i]);
                    }
                }
                setContinousBarChartData(sleepStateAll == null ? (new int[1440]) : sleepStateAll);

                int sleepHour = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_HOUR, 8);
                int sleepMin = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_MIN, 0);

                int totalSleepTime;///分钟
                totalSleepTime = (listLight.size() + listElight.size() + listDeep.size() + listAwake.size());///分钟
                // Log.e("listLight.size() ", " == listLight.size() == " + listLight.size() + "  ==listElight.size() == " + listElight.size() + " ==listAwake.size() == " + listAwake.size() + "sleepStateView != null" + sleepStateView);
                float sleep_percent = Math.round(((listDeep.size() + 0.2f * listElight.size() + 0.6f * listLight.size())
                        * 1000) / 96) / 10.0f;
                if (sleep_percent > 100) {
                    sleep_percent = (float) 100.0;
                }
                // Log.e("listLight.size() ", " == sleep_percent == " + sleep_percent + "  ==listElight.size() == " + String.format("%02d", totalSleepTime / 60) + ":" + String.format("%02d",
                //         totalSleepTime % 60) + " ==listAwake.size() == " + listAwake.size() + "sleepStateView != null" + sleepStateView);

                if (tvSleepPercent != null) {
                    tvSleepPercent.setText(sleep_percent + "%");
                }
                if (tvSleepTime != null) {
                    tvSleepTime.setText(String.format("%02d", totalSleepTime / 60) + ":" + String.format("%02d",
                            totalSleepTime % 60));
                }

                if (isNewBeat) {
                    String strCount = getActivity().getResources().getString(R.string.deep_sleep) + ":" + listDeep.size() + "min," + getActivity().getResources().getString(R.string.light_sleep) + ":" + listLight.size() + "min," + getActivity().getResources().getString(R.string.elight_sleep) + ":" + listElight.size() + "min," + getActivity().getResources().getString(R.string.awake) + ":" + listAwake.size() + "min";
                    if (tv_sleep_count != null) {
                        tv_sleep_count.setText(strCount);

                        sleepStateView.setSleepData(sleepStateAll == null ? (new int[1440]) : sleepStateAll, 1440);
                        tv_sleep_count.setVisibility(View.GONE);
                        sleepView.setSleepState(sleepState == null ? null : sleepState, 1440);
                    }
                } else {
                    String strCount = getActivity().getResources().getString(R.string.deep_sleep) + ":" + listDeep.size() * 5 + "min," + getActivity().getResources().getString(R.string.light_sleep) + ":" + listLight.size() * 5 + "min," + getActivity().getResources().getString(R.string.elight_sleep) + ":" + listElight.size() * 5 + "min," + getActivity().getResources().getString(R.string.awake) + ":" + listAwake.size() * 5 + "min";
                    if (tv_sleep_count != null) {
                        tv_sleep_count.setText(strCount);
                        tv_sleep_count.setVisibility(View.VISIBLE);
                        totalSleepTime = (listLight.size() + listElight.size() + listDeep.size() + listAwake.size()) * 5;///分钟
                        sleepStateView.setSleepData(sleepStateAll == null ? (new int[288]) : sleepStateAll, 288);
                        sleepView.setSleepState(sleepState == null ? null : sleepState, 288);
                    }
                }
                int percent = 0;
                if (sleepHour != 0 || sleepMin != 0) {
                    percent = (totalSleepTime * 100) / (sleepHour * 60 + sleepMin);
                } else {
                    percent = 0;
                }
                if (mPvView != null) {
                    mPvView.setProgress(percent > 100 ? 100 : percent);
                }
//            100*深睡（H）/ 8 + 60%*浅睡 （H ） / 8 + 20%*极浅睡  （H ) / 8

//            int sleep_percent = (int)((100 * (listDeep.size()/60.0f) / 8) + (80*(listLight.size()/60.0f) / 8)  +
// (20*(listElight.size()/60.0f) / 8));


                //(deep + 0.6 * light + 0.2 * eLight) / 96.0
//            sleepStateView.setSleepData(sleepStateAll);
                //sleepStateView.setSleepData();
            }

        } else {
            Log.e("parserSportDataupdateUi", tvContentValue + "" + sleepStateView + "" + continousBarChartView + "" + sleepStateView);
            if (sleepStateView != null) {
                if (continousBarChartView != null) {
                    continousBarChartView.setVisibility(View.INVISIBLE);

                }
                if (sleepStateView != null) {
                    sleepStateView.setVisibility(View.VISIBLE);
                }
                int sleepHour = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_HOUR, 8);
                int sleepMin = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_MIN, 0);

                int totalSleepTime;///分钟
                totalSleepTime = (listLight.size() + listElight.size() + listDeep.size() + listAwake.size());///分钟
                // Log.e("listLight.size() ", " == listLight.size() == " + listLight.size() + "  ==listElight.size() == " + listElight.size() + " ==listAwake.size() == " + listAwake.size() + "sleepStateView != null" + sleepStateView);
                float sleep_percent = Math.round(((listDeep.size() + 0.2f * listElight.size() + 0.6f * listLight.size())
                        * 1000) / 96) / 10.0f;
                if (sleep_percent > 100) {
                    sleep_percent = (float) 100.0;
                }
                // Log.e("listLight.size() ", " == sleep_percent == " + sleep_percent + "  ==listElight.size() == " + String.format("%02d", totalSleepTime / 60) + ":" + String.format("%02d",
                //         totalSleepTime % 60) + " ==listAwake.size() == " + listAwake.size() + "sleepStateView != null" + sleepStateView);
                tvSleepPercent.setText(sleep_percent + "%");
                tvSleepTime.setText(String.format("%02d", totalSleepTime / 60) + ":" + String.format("%02d",
                        totalSleepTime % 60));

                if (isNewBeat) {
                    String strCount = getActivity().getResources().getString(R.string.deep_sleep) + ":" + listDeep.size() + "min," + getActivity().getResources().getString(R.string.light_sleep) + ":" + listLight.size() + "min," + getActivity().getResources().getString(R.string.elight_sleep) + ":" + listElight.size() + "min," + getActivity().getResources().getString(R.string.awake) + ":" + listAwake.size() + "min";
                    tv_sleep_count.setText(strCount);
                    tv_sleep_count.setVisibility(View.GONE);
                    sleepStateView.setSleepData(sleepStateAll == null ? (new int[1440]) : sleepStateAll, 1440);
                    sleepView.setSleepState(sleepState == null ? null : sleepState, 1440);
                } else {
                    String strCount = getActivity().getResources().getString(R.string.deep_sleep) + ":" + listDeep.size() * 5 + "min," + getActivity().getResources().getString(R.string.light_sleep) + ":" + listLight.size() * 5 + "min," + getActivity().getResources().getString(R.string.elight_sleep) + ":" + listElight.size() * 5 + "min," + getActivity().getResources().getString(R.string.awake) + ":" + listAwake.size() * 5 + "min";
                    tv_sleep_count.setText(strCount);
                    tv_sleep_count.setVisibility(View.VISIBLE);
                    totalSleepTime = (listLight.size() + listElight.size() + listDeep.size() + listAwake.size()) * 5;///分钟
                    sleepStateView.setSleepData(sleepStateAll == null ? (new int[288]) : sleepStateAll, 288);
                    sleepView.setSleepState(sleepState == null ? null : sleepState, 288);
                }
                int percent = 0;
                if (sleepHour != 0 || sleepMin != 0) {
                    percent = (totalSleepTime * 100) / (sleepHour * 60 + sleepMin);
                } else {
                    percent = 0;
                }
                mPvView.setProgress(percent > 100 ? 100 : percent);
//            100*深睡（H）/ 8 + 60%*浅睡 （H ） / 8 + 20%*极浅睡  （H ) / 8

//            int sleep_percent = (int)((100 * (listDeep.size()/60.0f) / 8) + (80*(listLight.size()/60.0f) / 8)  +
// (20*(listElight.size()/60.0f) / 8));


                //(deep + 0.6 * light + 0.2 * eLight) / 96.0
//            sleepStateView.setSleepData(sleepStateAll);
            }
        }

        MainService mainService = MainService.getInstance(mContext);
        if (mainService != null) {
            BaseDevice baseDevice = mainService.getCurrentDevice();
            if (baseDevice != null) {
                if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307H || baseDevice.getDeviceType() ==
                        BaseDevice.TYPE_W301H) {
                    if (sleepStateView != null) {
                        sleepStateView.setDisable(true);
                    }
                }
            }
        }

    }

    public void updateUIWithData() {
        int sleepHour = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_HOUR, 8);
        int sleepMin = sharedPreferences.getInt(SleepActivity.CONFIG_SLEEP_TARGET_MIN, 0);
        int percent = 0;
        if (sleepHour != 0 || sleepMin != 0) {
            percent = (totalTime * 100) / (sleepHour * 60 + sleepMin);
        } else {
            percent = 0;
        }
        int sleep_percent = (int) ((100 * (deepTime / 60.0f) / 8) + (80 * (lightTime / 60.0f) / 8) + (20 *
                (elightTime / 60.0f) / 8));
        if (sleepStateView != null) {
            mPvView.setProgress(percent > 100 ? 100 : percent);
            tvSleepPercent.setText(sleep_percent + "%");
            tvSleepTime.setText(String.format("%02d", totalTime / 60) + ":" + String.format("%02d", totalTime % 60));
            MainService mainService = MainService.getInstance(mContext);
            if (mainService != null) {
                BaseDevice baseDevice = mainService.getCurrentDevice();
                if (baseDevice != null) {
                    if (baseDevice.getDeviceType() == BaseDevice.TYPE_W307H || baseDevice.getDeviceType() ==
                            BaseDevice.TYPE_W301H) {
//                        sleepStateView.setDisable(true);
                    }
                }
            }
            sleepStateView.setSleepData(mySleepState == null ? (new int[288]) : mySleepState, 288);
                        /*barChart03View.setChartData(sleepHistBarData);
                        barChart03View.setChartLabels(listChartLabel);
                        barChart03View.initView();
                        barChart03View.postInvalidate();*/
        }
    }

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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            handler.sendEmptyMessage(0x02);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //  ;
        //Log.e("onResume:", "onResume");
        // handler.sendEmptyMessage(0x02);
        // loadData();
       /* if (getUserVisibleHint()) {

            handler.sendEmptyMessage(0x02);
        }*/
        //
    }

    @Override
    public void onClick(View v) {
        //((SleepFragmentActivity)getParentFragment()).finishActivity();

        switch (v.getId()) {
        }
    }

    @Override
    public void onRefresh() {
        MainService mainService = MainService.getInstance(mContext);
        if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {
            if (!mainService.startSyncData()) {
                MainService.isSynCom = false;
                mScrollView.stopRefresh();
                Toast.makeText(mContext, mContext.getString(R.string.header_hint_refresh_loading), Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.please_bind), Toast.LENGTH_LONG).show();
            mScrollView.stopRefresh();
        }
    }

    private boolean isRegisterBroadcast = false;

    private void registerBroadcast() {
        isRegisterBroadcast = true;
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_SYNC_COMPLETED);
        filter.addAction(BaseController.ACTION_REAL_DATA);
        mContext.registerReceiver(mReceiver,filter);*/
        EventBus.getDefault().register(this);
    }

    public void unRegisterBroadcst() {
        if (mContext != null && isRegisterBroadcast) {
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
        if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
            if (mScrollView != null) {
                mScrollView.stopRefresh();
            }
            int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
            if (state == BaseController.STATE_CONNECTED) {
                //loadData();
            }
        } else if (action.equals(MainService.ACTION_SYNC_COMPLETED)) {

            int state = intent.getIntExtra(MainService.EXTRA_SYNC_STATE, BaseController.STATE_SYNC_COMPLETED);
            if (state != BaseController.STATE_SYNCING) {
                if (mScrollView != null) {
                    mScrollView.stopRefresh();
                }
                // Log.e("ACTION_SYNC_COMPLETED", "ACTION_SYNC_COMPLETED");
                loadData();
            }
        } else if (action.equals(BaseController.ACTION_REAL_DATA)) {
            String date = intent.getStringExtra(BaseController.EXTRA_REAL_DATE);
            // TODO: 2018/4/26 刷新逻辑要整理
            if (date.equals(tvContentValue)) {
                //loadData();
            }
        }
    }
}