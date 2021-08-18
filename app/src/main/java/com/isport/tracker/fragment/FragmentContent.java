package com.isport.tracker.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.CmdController;
import com.isport.isportlibrary.database.DbHistorySport;
import com.isport.isportlibrary.database.DbHistorySportN;
import com.isport.isportlibrary.database.DbRealTimePedo;
import com.isport.isportlibrary.database.DbSprotDayData;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.HistorySport;
import com.isport.isportlibrary.entry.HistorySportN;
import com.isport.isportlibrary.entry.PedoRealData;
import com.isport.isportlibrary.entry.SportDayData;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.BuildConfig;
import com.isport.tracker.MyApp;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.main.settings.ActivityDeviceSetting;
import com.isport.tracker.util.CalculateHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.PedoView;
import com.isport.tracker.view.TasksCompletedView;
import com.isport.tracker.view.XScrollView;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class FragmentContent extends BaseFragment implements XScrollView.IXScrollViewListener {//, OnClickListener {
    private final static String TAG = "FragmentContent";
    private String tvContentValue;

    private XScrollView mScrollView;
    private Context mContext;
    private TextView tvWeek, tvDate;
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
        for (int i = 0; i <= 49; i++) {
            listTp.add(0d);
        }
        for (int i = 0; i <= 24; i++) {
            listTp.add(0d);
            if (i % 3 == 0) {
                mLabels.add((i) + "h");
            } else {
                mLabels.add("");
            }
        }
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ActivityDeviceSetting.ACTION_STEP_TARGET_CHANGE)) {
                Log.e(TAG, "=ACTION_STEP_TARGET_CHANGE=");
                String dt = DateUtil.dataToString(Calendar.getInstance().getTime(), "yyyy-MM-dd");
                if (dt.equals(tvContentValue)) {
                    handler.sendEmptyMessage(0x01);
                }
            }
        }
    };

    public static FragmentContent newInstance(int postion, int count) {
        FragmentContent fragmentFirst = new FragmentContent();
        Bundle args = new Bundle();
        args.putInt("position", postion);
        args.putInt("count", count);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate " + ppppppppp);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_step, container, false);
        //verticalViewPager = (VerticalViewPager) inflater.inflate(R.layout.fragment_step, container, false);

        mScrollView = (XScrollView) inflater.inflate(R.layout.fragment_scroll_exercise, container, false);
        //mScrollView = (XScrollView) view1.findViewById(R.id.scroll_view);
        View conentView = inflater.inflate(R.layout.fragment_step_daily, null);

        tvWeek = (TextView) conentView.findViewById(R.id.main_fragment_text_Week);
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(ActivityDeviceSetting.ACTION_STEP_TARGET_CHANGE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);
        EventBus.getDefault().register(this);
    }

    public void unRegisterBroadcst() {
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    private int calCaloric(double step) {
        UserInfo userInfo = UserInfo.getInstance(mContext);
        return (int) (step * ((userInfo.getWeight() - 13.63636) * 0.000693 + 0.000495));
    }

    private float calDistance(double step) {
        UserInfo userInfo = UserInfo.getInstance(mContext);
        return (float) (step * userInfo.getStrideLength() / 1000f);
    }

    private synchronized void sportRadioGroupSelected(int checkedId) {

        try {
            if (mTvValue == null)
                return;
            UserInfo userInfo = UserInfo.getInstance(mContext);

            BaseDevice baseDevice = null;
            MainService mainService = MainService.getInstance(mContext);
            if (mainService != null) {
                baseDevice = mainService.getCurrentDevice();
            }
            boolean is194 = (baseDevice != null && baseDevice.getProfileType() == BaseController.CMD_TYPE_W194);
            double total = 0;
       /* int caloric = 0;
        float distance = 0;*/

            if (historySteps != null) {
                for (int i = 0; i < historySteps.size(); i++) {
                    total = historySteps.get(i) + total;
                }
            }
            String dt = DateUtil.dataToString(Calendar.getInstance().getTime(), "yyyy-MM-dd");

            if (pedoRealData != null) {
                if (dt.equals(tvContentValue)) {
                    total = (!is194 ? (pedoRealData.getPedoNum() < total ? total : pedoRealData.getPedoNum()) :
                            pedoRealData.getPedoNum());
                } else {
                    total = (pedoRealData.getPedoNum() < total ? total : pedoRealData.getPedoNum());
                }
            }

            int metric = UserInfo.getInstance(mContext).getMetricImperial();

            int target = 10000;
            Calendar calendar = Calendar.getInstance();
            String currentDateString = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
            if (currentDateString.equals(tvContentValue)) {
                target = userInfo.getTargetStep();
            } else {
                if (mainService != null) {
                    if (baseDevice != null) {
                        DbSprotDayData dbSprotDayData = DbSprotDayData.getInstance(mContext);
                        SportDayData sportDayData = dbSprotDayData.findFirst(DbSprotDayData.COLUMN_DATE + "=? and " +
                                        DbSprotDayData.COLUMN_MAC + "=?",
                                new String[]{tvContentValue, baseDevice
                                        .getMac()}, null);

                        if (sportDayData == null) {
                            target = userInfo.getTargetStep();
                        } else {
                            //  Log.e("DbSprotDayData ", sportDayData.toString());
                            target = sportDayData.getTargetStep() == 0 ? 10000 : sportDayData.getTargetStep();
                        }
                    }
                }
            }

            int percent = pedoRealData != null ? (int) (Math.round((!is194 ? (pedoRealData == null ? 0 : pedoRealData
                    .getPedoNum()) : total) * 100) / target) : 0;
            if (percent > 999) {
                percent = 999;
            }
            mTvPercent.setText(percent + "%");
            mPvView.setProgress(percent);
            rbSteps.setText((int) ((!is194 ? (pedoRealData == null ? 0 : pedoRealData.getPedoNum()) : total)) + "");
            rbCaloric.setText((pedoRealData == null ? 0 : pedoRealData.getCaloric()) + "");
            String format = CalculateHelper.df_0_000.format(pedoRealData == null ? 0 : (metric == 0 ? pedoRealData
                    .getDistance() : CalculateHelper.kmToMile(pedoRealData.getDistance())));
            Log.e("fValue 00 == ", format);
            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_ENERGETICS)) {
                if (format.contains(",")) {
                    format = format.replace(",", ".");
                }
                if (format.contains("٫")) {
                    format = format.replace("٫", ".");
                }
            } else {
                if (format.contains(",")) {
                    format = format.replace(",", ".");
                }
                if (format.contains("٫")) {
                    format = format.replace("٫", ".");
                }
            }
            Log.e("fValue 11 == ", format);
            float fValue = Float.valueOf(format);
            float reslut = (float) (Math.round(fValue * 100)) / 100;
            rbDistance.setText(reslut + "");
            //rbDistance.setText(distance+"");
            switch (checkedId) {
                case R.id.exercise_total_steps:
                    mTvValue.setText(!is194 ? ((pedoRealData == null ? 0 : pedoRealData.getPedoNum()) + "") : ((int)
                            total + ""));
                    rbSteps.setChecked(true);
                    imageLogo.setImageResource(R.drawable.foot_logo);
                    break;
                case R.id.exercise_total_carles:
                    mTvValue.setText(((pedoRealData == null ? 0 : pedoRealData.getCaloric()) + ""));
                    rbCaloric.setChecked(true);
                    imageLogo.setImageResource(R.drawable.calorie_logo);
                    break;
                case R.id.exercise_total_distance:
                    mTvValue.setText(reslut + "");
                    imageLogo.setImageResource(R.drawable.location_logo);
                    rbDistance.setChecked(true);
                    break;
            }

        } catch (Exception e) {

        }


    }

    private void historyRadioGroupSelected(int checkedId) {

    }

    private int ppppppppp = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        int position = getArguments().getInt("position");
        ppppppppp = position;
        int count = getArguments().getInt("count");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * (count - position - 1));
        tvContentValue = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
        weeks = mContext.getResources().getStringArray(R.array.week);

        loadData();
        Log.e(TAG, "onAttach " + ppppppppp);
    }

    private void loadData() {
        MyApp.getInstance().executorService.submit(new Runnable() {
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
            BaseController currentController = mainService.getCurrentController();
            if (baseDevice != null) {
                pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(tvContentValue, baseDevice.getMac());
                if (pedoRealData != null) {
                    Log.e("***pedoRealData实时***", pedoRealData.getDateString() + "*" + pedoRealData.getMac() + "*" +
                            pedoRealData.getPedoNum());
                }
                int nozero = 0;

                if (currentController instanceof CmdController) {
                    if ((baseDevice.getName().contains("BEAT") && ((CmdController) currentController).getVersion() >= 91.70f) || (baseDevice.getName().contains("REFLEX") && ((CmdController) currentController).getVersion() >= 91.02f)) {
                        String sql = DbHistorySportN.COLUMN_DATE + " like ? and " + DbHistorySportN.COLUMN_MAC + "=?";
                        List<HistorySportN> tpH = DbHistorySportN.getInstance(mContext).findAll(sql, new
                                        String[]{tvContentValue
                                        + "%", baseDevice
                                        .getMac()},
                                "datetime(" + DbHistorySportN
                                        .COLUMN_DATE + ") ASC");
                        //Log.e(TAG, "###" + "sql****" + sql);
                        if (tpH == null || tpH.size() == 0) {
                            historySteps.clear();
                            historySteps.addAll(listTp);
                        } else {
                            /*for (int i = 0; i < tpH.size(); i++) {
                                Log.e(TAG, tpH.get(i).getDateString() + "***" + tpH.get(i).getStepNum());
                            }*/
                            int tpv = 0;
                            int index = 0;
                            double tempTotal = 0;

                            //W194数据是30分钟，其他设备5分钟
                            //现在Beat 91.61数据改为1分钟
//                            int duration = 60000;
//                            if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
//                                if ((baseDevice.getDeviceType() == BaseDevice.TYPE_MILLIONPEDOMETER || baseDevice
//                                        .getDeviceType() == BaseDevice.TYPE_P118)) {
//                                    duration = 1800000;
//                                }
//                                long startt = DateUtil.stringToDate(tpH.get(0).getDateString(), "yyyy-MM-dd HH:mm").getTime();
//                                long entt = DateUtil.stringToDate(tpH.get(tpH.size() - 1).getDateString(), "yyyy-MM-dd " +
//                                        "HH:mm").getTime();
//                                if (entt - startt > (tpH.size() - 1) * duration) {
//                                    List<HistorySportN> tplist = new ArrayList<>();
//                                    tplist.addAll(tpH);
//                                    int startIndex = 0;
//                                    for (int i = 0; i < tplist.size() - 1; i++) {
//                                        startt = DateUtil.stringToDate(tplist.get(i).getDateString(), "yyyy-MM-dd HH:mm")
//                                                .getTime();
//                                        entt = DateUtil.stringToDate(tplist.get(i + 1).getDateString(), "yyyy-MM-dd HH:mm")
//                                                .getTime();
//                                        int tpl = (int) ((entt - startt) / duration);
//                                        if (tpl > 1) {
//                                            for (int j = 0; j < tpl; j++) {
//                                                tpH.add(++startIndex, new HistorySportN("", "", 0, 0, 0, 0));
//                                            }
//                                        } else {
//                                            startIndex++;
//                                        }
//                                    }
//                                }
//                            }
                            historySteps.clear();
                            for (int i = 1; i <= tpH.size(); i++) {
                                int tpnum = tpH.get(i - 1).getStepNum();
                                if (i < 6) {
                                    //  Log.e(TAG, "###" + tpH.get(i - 1).getDateString() + "***" + tpH.get(i - 1).getStepNum());
                                }
                                tempTotal += tpnum;
                                index++;
                                total += tpnum;
                                //当index为12说明是当前小时的最后一包数据
                                //之前是以5分钟算，所以在00:30或者00:00时清空
                                //现在是1分钟，也应该在00:30或者00:00时清空
                                if (index % 60 == 0 || index % 30 == 0) {
                                    historySteps.add(tempTotal);
                                    if (tempTotal > 0) {
                                        //直接noZero为0吧
//                                nozero = index / 12 - 1;
                                        nozero = 0;
                                    }
                                    tempTotal = 0;
                                }
                            }

//                            if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
//                                // TODO: 2018/4/11 194的要做什么特别的处理
//                                String dts = tpH.get(0).getDateString();
//                                Date dttt = DateUtil.stringToDate(dts, "yyyy-MM-dd HH:mm");
//                                Calendar ccc = Calendar.getInstance();
//                                ccc.setTime(dttt);
//                                ccc.set(Calendar.HOUR_OF_DAY, 0);
//                                ccc.set(Calendar.MINUTE, 0);
//                                ccc.set(Calendar.SECOND, 0);
//                                ccc.set(Calendar.MILLISECOND, 0);
//                                long tttttt = ccc.getTimeInMillis();
//                                ccc.setTime(dttt);
//                                long dddddd = ccc.getTimeInMillis();
//                                int dm = (int) ((dddddd - tttttt) / (3600 * 1000));
//                                if (historySteps.size() == 0) {
//                                    historySteps.add(tempTotal);
//                                }
//                                for (int i = 0; i < dm; i++) {
//                                    historySteps.add(0, 0d);
//                                }
//                            }

                            if (historySteps.size() < 49) {
                                int size = historySteps.size();
                                for (int i = size; i < 49; i++) {
                                    if (tempTotal > 0) {
                                        historySteps.add(tempTotal);
                                        if (tempTotal > 0) {
//                                    nozero = index / 12 - 1;
                                            nozero = 0;
                                        }
                                        tempTotal = 0;
                                    } else {
                                        historySteps.add(0d);
                                    }
                                }
                            } else {
                                while (historySteps.size() > 49) {
                                    historySteps.remove(historySteps.size() - 1);
                                }
                            }
                        }
                    } else {
                        String sql = DbHistorySport.COLUMN_DATE + " like ? and " + DbHistorySport.COLUMN_MAC + "=?";
                        List<HistorySport> tpH = DbHistorySport.getInstance(mContext).findAll(sql, new
                                        String[]{tvContentValue
                                        + "%", baseDevice
                                        .getMac()},
                                "datetime(" + DbHistorySport
                                        .COLUMN_DATE + ") ASC");
                        if (tpH == null || tpH.size() == 0) {
                            historySteps.clear();
                            historySteps.addAll(listTp);
                        } else {
                            for (int i = 0; i < tpH.size(); i++) {
                                // Log.e(TAG, tpH.get(i).getDateString() + "***" + tpH.get(i).getStepNum());
                            }
                            int tpv = 0;
                            int index = 0;
                            double tempTotal = 0;

                            int duration = 300000;
                            if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                                if ((baseDevice.getDeviceType() == BaseDevice.TYPE_MILLIONPEDOMETER || baseDevice
                                        .getDeviceType() == BaseDevice.TYPE_P118)) {
                                    duration = 1800000;
                                }
                                long startt = DateUtil.stringToDate(tpH.get(0).getDateString(), "yyyy-MM-dd HH:mm").getTime();
                                long entt = DateUtil.stringToDate(tpH.get(tpH.size() - 1).getDateString(), "yyyy-MM-dd " +
                                        "HH:mm").getTime();
                                if (entt - startt > (tpH.size() - 1) * duration) {
                                    List<HistorySport> tplist = new ArrayList<>();
                                    tplist.addAll(tpH);
                                    int startIndex = 0;
                                    for (int i = 0; i < tplist.size() - 1; i++) {
                                        startt = DateUtil.stringToDate(tplist.get(i).getDateString(), "yyyy-MM-dd HH:mm")
                                                .getTime();
                                        entt = DateUtil.stringToDate(tplist.get(i + 1).getDateString(), "yyyy-MM-dd HH:mm")
                                                .getTime();
                                        int tpl = (int) ((entt - startt) / duration);
                                        if (tpl > 1) {
                                            for (int j = 0; j < tpl; j++) {
                                                tpH.add(++startIndex, new HistorySport("", "", 0, 0));
                                            }
                                        } else {
                                            startIndex++;
                                        }
                                    }
                                }
                            }
                            historySteps.clear();
                            for (int i = 1; i <= tpH.size(); i++) {
                                int tpnum = tpH.get(i - 1).getStepNum();
                                if (i < 6) {
                                    //Log.e(TAG, "###" + tpH.get(i - 1).getDateString() + "***" + tpH.get(i - 1).getStepNum());
                                }
                                tempTotal += tpnum;
                                index++;
                                total += tpnum;
                                //当index为12说明是当前小时的最后一包数据
                                if (index % 12 == 0 || index % 6 == 0) {
                                    historySteps.add(tempTotal);
                                    if (tempTotal > 0) {
                                        //直接noZero为0吧
//                                nozero = index / 12 - 1;
                                        nozero = 0;
                                    }
                                    tempTotal = 0;
                                }
                            }

                            if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                                // TODO: 2018/4/11 194的要做什么特别的处理
                                String dts = tpH.get(0).getDateString();
                                Date dttt = DateUtil.stringToDate(dts, "yyyy-MM-dd HH:mm");
                                Calendar ccc = Calendar.getInstance();
                                ccc.setTime(dttt);
                                ccc.set(Calendar.HOUR_OF_DAY, 0);
                                ccc.set(Calendar.MINUTE, 0);
                                ccc.set(Calendar.SECOND, 0);
                                ccc.set(Calendar.MILLISECOND, 0);
                                long tttttt = ccc.getTimeInMillis();
                                ccc.setTime(dttt);
                                long dddddd = ccc.getTimeInMillis();
                                int dm = (int) ((dddddd - tttttt) / (3600 * 1000));
                                if (historySteps.size() == 0) {
                                    historySteps.add(tempTotal);
                                }
                                for (int i = 0; i < dm; i++) {
                                    historySteps.add(0, 0d);
                                }
                            }

                            if (historySteps.size() < 49) {
                                int size = historySteps.size();
                                for (int i = size; i < 49; i++) {
                                    if (tempTotal > 0) {
                                        historySteps.add(tempTotal);
                                        if (tempTotal > 0) {
//                                    nozero = index / 12 - 1;
                                            nozero = 0;
                                        }
                                        tempTotal = 0;
                                    } else {
                                        historySteps.add(0d);
                                    }
                                }
                            } else {
                                while (historySteps.size() > 49) {
                                    historySteps.remove(historySteps.size() - 1);
                                }
                            }
                        }
                    }
                } else {
                    String sql = DbHistorySport.COLUMN_DATE + " like ? and " + DbHistorySport.COLUMN_MAC + "=?";
                    List<HistorySport> tpH = DbHistorySport.getInstance(mContext).findAll(sql, new
                                    String[]{tvContentValue
                                    + "%", baseDevice
                                    .getMac()},
                            "datetime(" + DbHistorySport
                                    .COLUMN_DATE + ") ASC");
                    if (tpH == null || tpH.size() == 0) {
                        historySteps.clear();
                        historySteps.addAll(listTp);
                    } else {
                        for (int i = 0; i < tpH.size(); i++) {
                            Log.e(TAG, tpH.get(i).getDateString() + "***" + tpH.get(i).getStepNum());
                        }
                        int tpv = 0;
                        int index = 0;
                        double tempTotal = 0;

                        int duration = 300000;
                        if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                            if ((baseDevice.getDeviceType() == BaseDevice.TYPE_MILLIONPEDOMETER || baseDevice
                                    .getDeviceType() == BaseDevice.TYPE_P118)) {
                                duration = 1800000;
                            }
                            long startt = DateUtil.stringToDate(tpH.get(0).getDateString(), "yyyy-MM-dd HH:mm").getTime();
                            long entt = DateUtil.stringToDate(tpH.get(tpH.size() - 1).getDateString(), "yyyy-MM-dd " +
                                    "HH:mm").getTime();
                            if (entt - startt > (tpH.size() - 1) * duration) {
                                List<HistorySport> tplist = new ArrayList<>();
                                tplist.addAll(tpH);
                                int startIndex = 0;
                                for (int i = 0; i < tplist.size() - 1; i++) {
                                    startt = DateUtil.stringToDate(tplist.get(i).getDateString(), "yyyy-MM-dd HH:mm")
                                            .getTime();
                                    entt = DateUtil.stringToDate(tplist.get(i + 1).getDateString(), "yyyy-MM-dd HH:mm")
                                            .getTime();
                                    int tpl = (int) ((entt - startt) / duration);
                                    if (tpl > 1) {
                                        for (int j = 0; j < tpl; j++) {
                                            tpH.add(++startIndex, new HistorySport("", "", 0, 0));
                                        }
                                    } else {
                                        startIndex++;
                                    }
                                }
                            }
                        }
                        historySteps.clear();
                        for (int i = 1; i <= tpH.size(); i++) {
                            int tpnum = tpH.get(i - 1).getStepNum();
                            if (i < 6) {
                                Log.e(TAG, "###" + tpH.get(i - 1).getDateString() + "***" + tpH.get(i - 1).getStepNum());
                            }
                            tempTotal += tpnum;
                            index++;
                            total += tpnum;
                            //当index为12说明是当前小时的最后一包数据
                            if (index % 12 == 0 || index % 6 == 0) {
                                historySteps.add(tempTotal);
                                if (tempTotal > 0) {
                                    //直接noZero为0吧
//                                nozero = index / 12 - 1;
                                    nozero = 0;
                                }
                                tempTotal = 0;
                            }
                        }

                        if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W194) {
                            // TODO: 2018/4/11 194的要做什么特别的处理
                            String dts = tpH.get(0).getDateString();
                            Date dttt = DateUtil.stringToDate(dts, "yyyy-MM-dd HH:mm");
                            Calendar ccc = Calendar.getInstance();
                            ccc.setTime(dttt);
                            ccc.set(Calendar.HOUR_OF_DAY, 0);
                            ccc.set(Calendar.MINUTE, 0);
                            ccc.set(Calendar.SECOND, 0);
                            ccc.set(Calendar.MILLISECOND, 0);
                            long tttttt = ccc.getTimeInMillis();
                            ccc.setTime(dttt);
                            long dddddd = ccc.getTimeInMillis();
                            int dm = (int) ((dddddd - tttttt) / (3600 * 1000));
                            if (historySteps.size() == 0) {
                                historySteps.add(tempTotal);
                            }
                            for (int i = 0; i < dm; i++) {
                                historySteps.add(0, 0d);
                            }
                        }

                        if (historySteps.size() < 49) {
                            int size = historySteps.size();
                            for (int i = size; i < 49; i++) {
                                if (tempTotal > 0) {
                                    historySteps.add(tempTotal);
                                    if (tempTotal > 0) {
//                                    nozero = index / 12 - 1;
                                        nozero = 0;
                                    }
                                    tempTotal = 0;
                                } else {
                                    historySteps.add(0d);
                                }
                            }
                        } else {
                            while (historySteps.size() > 49) {
                                historySteps.remove(historySteps.size() - 1);
                            }
                        }
                    }
                }


                if (baseDevice.getProfileType() == BaseController.CMD_TYPE_W311) {
                    if (pedoRealData != null) {
                        Calendar calendar = Calendar.getInstance();
                        //获取当前日期
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        String tppp = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                        calendar.setTime(DateUtil.stringToDate(tvContentValue, "yyyy-MM-dd"));
                        int index = 0;

                        if (tvContentValue.equals(tppp)) {
                            index = hour * 2;
                            Log.e("***pedoRealDataIndex***", index + "");
                            Log.e("***pedoRealData当前历史总数*", total + "");
                            Log.e("***pedoRealData当前实时返回*", pedoRealData.getPedoNum() + "");
                            if (total < pedoRealData.getPedoNum()) {
                                double temp = historySteps.get(index);
                                Log.e("pedoRealData当前半小时的历史步数", temp + "");
                                Log.e("pedoRealData半小时的实时返回步数", (pedoRealData.getPedoNum() - total) + "");
                                Log.e("pedoRealData半小时的总步数", (temp + pedoRealData.getPedoNum() - total) + "");
                                historySteps.set(index, temp + pedoRealData.getPedoNum() - total);
//                                historyStepsIndex***: 20
//                                historySteps当前历史总数*: 45472
//                                historySteps当前半小时的历史步数: 923.0
//                                historySteps半小时的实时返回步数: 12939
//                                historySteps半小时的总步数: 13862.0
                            }
                        } else {
//                            index = nozero;
                        }


                    }
                }
            } else {
                pedoRealData = null;
                initHistorySteps();
            }
        } else {
            initHistorySteps();
            pedoRealData = null;
        }

        handler.sendEmptyMessage(0x01);
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    if (mPvView != null) {
                        int id = radioGroup.getCheckedRadioButtonId();
                        if (historySteps.size() < 49) {
                            int size = historySteps.size();
                            for (int i = size; i < 49; i++) {
                                historySteps.add(0d);
                            }
                        }
                        sportRadioGroupSelected(id);
                        List<Double> tp = new ArrayList<>();
                        tp.addAll(historySteps);
                        Collections.sort(tp);
                        //stepChartView.setmDataset(mDataset);
                        int itx = historySteps.size() - 1;
                        if (itx < 0) {
                            itx = 0;
                        }
                        if (itx >= tp.size()) {
                            itx = tp.size() - 1;
                        }
                        if (tp.size() == 0) {
                            tp.addAll(listTp);
                        }
                        pedoView.setMaxValue((int) (itx < 0 ? 0 : tp.get(itx).doubleValue()) + 5);
                        pedoView.setmLabels(mLabels);
                        pedoView.setListData(historySteps);
                        String dt = DateUtil.dataToString(Calendar.getInstance().getTime(), "yyyy-MM-dd");
                        if (dt.equals(tvContentValue)) {
                            Log.e("***historySteps***", historySteps.toString());
                        }
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
            return o1 < o2 ? 1 : 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView " + ppppppppp);
        if (handler.hasMessages(0x01))
            handler.removeMessages(0x01);
        unRegisterBroadcst();
        if (mPvView != null) {
            mPvView.stopAnimation();
        }
        if (mScrollView != null) {
            mScrollView.removeAllViews();
        }
        mScrollView = null;
        tvWeek = null;
        tvDate = null;
        mPvView = null;
        radioGroup = null;
        imageLogo = null;
        mTvValue = null;
        mTvPercent = null;
        pedoView = null;
        historySteps = null;
        rbSteps = null;
        rbCaloric = null;
        rbDistance = null;
        pedoRealData = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume " + ppppppppp + "  isuser " + getUserVisibleHint());
       /* if (verticalViewPager != null) {
            verticalViewPager.getAdapter().notifyDataSetChanged();
        }*/
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "onDetach " + ppppppppp);
    }

    View cell_bottom;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerBroadcast();
        handler.sendEmptyMessage(0x01);
        Log.e(TAG, "onViewCreated " + ppppppppp);
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
                if (mScrollView != null) {
                    mScrollView.stopRefresh();
                }
                MainService.isSynCom = false;
                Toast.makeText(mContext, mContext.getString(R.string.header_hint_refresh_loading), Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.please_bind), Toast.LENGTH_LONG).show();
            if (mScrollView != null) {
                mScrollView.stopRefresh();
            }
        }
    }

    @Override
    public void onLoadMore() {

    }

    private synchronized void loadRealD() {
        pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(tvContentValue, MainService.getInstance
                (mContext).getCurrentDevice().getMac());
        if (pedoRealData != null) {
            Calendar calendar = Calendar.getInstance();
            //当前的hour
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            String tppp = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
            calendar.setTime(DateUtil.stringToDate(tvContentValue, "yyyy-MM-dd"));
            int index = 0;

            if (tvContentValue.equals(tppp)) {
                index = hour * 2;
                // Log.e("***historyStepsIndex***", index + "");
            }
            int total = 0;
            //获取当前历史总步数
            for (int i = 0; i < historySteps.size(); i++) {
                total += historySteps.get(i);
            }
            //如果实时大于总数
            if (total < pedoRealData.getPedoNum()) {
                if (historySteps.size() <= 49) {
                    for (int i = historySteps.size(); i <= 49; i++) {
                        historySteps.add(0d);
                    }
                }
                if (index < historySteps.size()) {
                    double temp = historySteps.get(index);
                    // Log.e("historySteps当前半小时的历史步数", temp + "");
                    //  Log.e("historySteps半小时的实时返回步数", (pedoRealData.getPedoNum() - total) + "");
                    //  Log.e("historySteps半小时的总步数", (temp + pedoRealData.getPedoNum() - total) + "");
                    historySteps.set(index, temp + pedoRealData.getPedoNum() - total);
                }
            }
            handler.sendEmptyMessage(0x01);
        }
    }

    public void loadRealData() {
        MyApp.getInstance().executorService.submit(new Runnable() {
            @Override
            public void run() {
                loadRealD();
            }
        });
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
            // Log.e(TAG, "***state***" + state);
            if (state != BaseController.STATE_SYNCING) {
                if (mScrollView != null) {
                    mScrollView.stopRefresh();
                }
                loadData();
            }
        } else if (action.equals(BaseController.ACTION_REAL_DATA)) {
            String date = intent.getStringExtra(BaseController.EXTRA_REAL_DATE);
            if (date.equals(tvContentValue) && MainService.getInstance(mContext).getCurrentDevice() != null) {
                loadRealData();
            }
        }
    }
}