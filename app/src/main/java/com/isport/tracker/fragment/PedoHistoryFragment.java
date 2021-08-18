package com.isport.tracker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.database.DbRealTimePedo;
import com.isport.isportlibrary.database.DbSprotDayData;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.PedoRealData;
import com.isport.isportlibrary.entry.SportDayData;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.tracker.MyApp;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.PedoHistView;
import com.ypy.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by feige on 2017/4/19.
 */

public class PedoHistoryFragment extends Fragment {

    public static final int TYPE_STEP = 1;
    public static final int TYPE_CALOR = 2;
    public static final int TYPE_DIST = 3;
    public static final int DATE_DAY = 1;
    public static final int DATE_WEEK = 2;
    public static final int DATE_ONE_WEEK = 3;
    public static final int DATE_MONTH = 4;
    private static final String TAG = PedoHistoryFragment.class.getSimpleName();

    private List<Double> listStep = Collections.synchronizedList(new ArrayList<Double>());
    private List<Double> listCalo = Collections.synchronizedList(new ArrayList<Double>());
    private List<Double> listDist = Collections.synchronizedList(new ArrayList<Double>());

    private List<String> listStart = Collections.synchronizedList(new ArrayList<String>());
    private List<String> listEnd = Collections.synchronizedList(new ArrayList<String>());
    private List<String> listStr = Collections.synchronizedList(new LinkedList<String>());

    private Context mContext;
    private int dateType;
    private int dataType;
    private int position;
    private int count;
    private String[] months;
    private String[] weeks;

    private PedoHistView pedoHistView;
    private TextView tvDate;

    public static PedoHistoryFragment newInstance(int datatype, int datetype, int position, int count) {
        Bundle args = new Bundle();
        args.putInt("datetype", datetype);
        args.putInt("datatype", datatype);
        args.putInt("position", position);
        args.putInt("count", count);
        PedoHistoryFragment fragment = new PedoHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        months = context.getResources().getStringArray(R.array.month_small);
        weeks = context.getResources().getStringArray(R.array.week);
        dataType = getArguments().getInt("datatype");
        dateType = getArguments().getInt("datetype");
        position = getArguments().getInt("position");
        count = getArguments().getInt("count");
        reloadData();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        updateUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);

        if (handler.hasMessages(0x01)) {
            handler.removeMessages(0x01);
        }
        pedoHistView = null;
        tvDate = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pedo_hist_item, container, false);
        tvDate = (TextView) view.findViewById(R.id.pedo_hist_item_date);
        pedoHistView = (PedoHistView) view.findViewById(R.id.pedo_hist_histview);
        return view;
    }

    public void clearList() {
        listStart.clear();
        listCalo.clear();
        listDist.clear();
        listEnd.clear();
        listStep.clear();
        listStr.clear();
    }

    public synchronized void initData() {
        Calendar calendar = Calendar.getInstance();
        Calendar curCalendar = calendar;
        String currentDateString = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
        Log.e(TAG, "***当前日期***" + currentDateString);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        DbSprotDayData dbSprotDayData = DbSprotDayData.getInstance(mContext);
        MainService mainService = MainService.getInstance(getContext());
        BaseDevice baseDevice = mainService.getCurrentDevice();
        clearList();
        int metric = UserInfo.getInstance(MyApp.getInstance()).getMetricImperial();///0 公制  1英制
        switch (dateType) {
            case PedoHistoryFragment.DATE_DAY: {
                calendar.add(Calendar.DAY_OF_MONTH, -7 * (count - position) + 1);
                for (int i = 0; i < 7; i++) {
                    String date = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
                    listStart.add(date);
                    listEnd.add(date);
                    listStr.add(UtilTools.date2String(calendar.getTime(), "MM/dd"));
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (baseDevice == null) {
                        listStep.add(0d);
                        listDist.add(0d);
                        listCalo.add(0d);
                    } else {
                        float[] value1 = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                "date(" + DbSprotDayData.COLUMN_DATE + ")=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                new String[]{date, baseDevice.getMac()});
                        SportDayData sportDayData = dbSprotDayData.findFirst(DbSprotDayData.COLUMN_DATE + "=? and " + DbSprotDayData.COLUMN_MAC + "=?",
                                new String[]{date, baseDevice.getMac()}, null);
                        PedoRealData pedoRealData = null;
                        if (date.equals(currentDateString)) {////如果是当天
                            pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(date, baseDevice.getMac());
                        }
                        if (sportDayData == null) {
                            if (pedoRealData != null) {
                                listStep.add(pedoRealData.getPedoNum() + 0d);
                                int td = ((int) ((metric == 0 ? pedoRealData.getDistance() : pedoRealData.getDistance() * 0.6213712d) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                                listCalo.add(pedoRealData.getCaloric() + 0d);
                            } else {
                                listStep.add(0d);
                                listDist.add(0d);
                                listCalo.add(0d);
                            }
                        } else {
                            if (pedoRealData != null) {
                                sportDayData.setTotalCaloric(pedoRealData.getCaloric());
                                sportDayData.setTotalDist(pedoRealData.getDistance());
                                sportDayData.setTotalStep(pedoRealData.getPedoNum());
                            }
                            listStep.add(sportDayData.getTotalStep() + 0d);
                            int td = ((int) (((metric == 0 ? sportDayData.getTotalDist() : sportDayData.getTotalDist() * 0.6213712d)) * 1000));
                            listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                            listCalo.add(sportDayData.getTotalCaloric() + 0d);
                        }
                    }

                }
            }
            break;
            case PedoHistoryFragment.DATE_WEEK:
                calendar.add(Calendar.WEEK_OF_YEAR, -7 * (count - position - 1) - 6);
                for (int i = 0; i < 7; i++) {
                    String sssss = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
                    String sssssssss = calendar.get(Calendar.WEEK_OF_YEAR) + " " + (getContext() == null ? "week" : getContext().getString(R.string.week));
                    listStr.add(sssssssss);
                    int dayOfWeek1 = calendar.get(Calendar.DAY_OF_WEEK);
                    calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeek1 + 1);
                    Calendar calendar1 = calendar;
                    String start = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
                    listStart.add(start);
                    calendar.add(Calendar.DAY_OF_MONTH, 6);
                    Calendar calendar2 = calendar;
                    String end = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
                    listEnd.add(end);
                    //Log.e("pedo", "weekOfYear = " + calendar.get(Calendar.WEEK_OF_YEAR) + "   start = " + start + "  end = " + end);
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    if (baseDevice == null) {
                        listStep.add(0d);
                        listDist.add(0d);
                        listCalo.add(0d);
                    } else {
                        PedoRealData pedoRealData = null;
                        float[] value1 = new float[]{0, 0, 0};
                        Date ddddd = UtilTools.string2Date(currentDateString, "yyyy-MM-dd");
                        Date dstart = UtilTools.string2Date(start, "yyyy-MM-dd");
                        Date dend = UtilTools.string2Date(end, "yyyy-MM-dd");
                        if (ddddd.getTime() >= dstart.getTime() && ddddd.getTime() <= dend.getTime()) {
                            pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(currentDateString, baseDevice.getMac());
                            value1 = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                    new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                    "date(" + DbSprotDayData.COLUMN_DATE + ")=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                    new String[]{currentDateString, baseDevice.getMac()});
                        }
                        float[] value = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                "date(" + DbSprotDayData.COLUMN_DATE + ")>=date(?) and date(" + DbSprotDayData.COLUMN_DATE + ")<=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                new String[]{start, end, baseDevice.getMac()});
                        if (value != null) {
                            if (pedoRealData != null && value1 != null && pedoRealData.getPedoNum() > value1[0]) {
                                listStep.add((int) (value[0] + pedoRealData.getPedoNum() - value1[0]) + 0d);
                                listCalo.add((int) (value[1] + pedoRealData.getCaloric() - value1[1]) + 0d);
                                int td = ((int) ((metric == 0 ? (value[2] + pedoRealData.getDistance() - value1[2]) : (value[2] + pedoRealData.getDistance() - value1[2]) * 0.6213712d) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));// td*0.001+0d);
                            } else {

                                listStep.add((int) value[0] + 0d);
                                listCalo.add((int) value[1] + 0d);
                                int td = ((int) ((metric == 0 ? value[2] : value[2] * 0.6213712d) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));// td*0.001+0d);
                            }
                        } else {
                            if (pedoRealData != null) {
                                listStep.add(pedoRealData.getPedoNum() + 0d);
                                listCalo.add(pedoRealData.getCaloric() + 0d);
                                int td = ((int) ((metric == 0 ? pedoRealData.getDistance() : pedoRealData.getDistance() * 0.6213712d) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                            } else {
                                listStep.add(0d);
                                listDist.add(0d);
                                listCalo.add(0d);
                            }
                        }
                    }
                }
                break;
            case PedoHistoryFragment.DATE_MONTH:
                calendar.add(Calendar.MONTH, -6 - 7 * (count - position - 1));
                for (int i = 0; i < 7; i++) {
                    String date = UtilTools.date2String(calendar.getTime(), "yyyy-MM");
                    listStart.add(date);
                    listEnd.add(date);
                    listStr.add(months[calendar.get(Calendar.MONTH)]);
                    calendar.add(Calendar.MONTH, 1);

                    if (baseDevice == null) {
                        listStep.add(0d);
                        listDist.add(0d);
                        listCalo.add(0d);
                    } else {
                        PedoRealData pedoRealData = null;
                        float[] value1 = new float[]{0, 0, 0};
                        if (currentDateString.startsWith(date)) {
                            pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(currentDateString, baseDevice.getMac());
                            value1 = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                    new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                    "date(" + DbSprotDayData.COLUMN_DATE + ")=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                    new String[]{currentDateString, baseDevice.getMac()});
                        }
                        float[] value = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                DbSprotDayData.COLUMN_DATE + " like ? and " + DbSprotDayData.COLUMN_MAC + "=?",
                                new String[]{date + "%", baseDevice.getMac()});

                        if (value != null) {
                            if (pedoRealData != null && value1 != null && pedoRealData.getPedoNum() > value1[0]) {
                                listStep.add((int) (value[0] + pedoRealData.getPedoNum() - value1[0]) + 0d);
                                listCalo.add((int) (value[1] + pedoRealData.getCaloric() - value1[1]) + 0d);
                                int td = (((int) (metric == 0 ? (value[2] + pedoRealData.getDistance() - value1[2]) : (value[2] + pedoRealData.getDistance() - value1[2]) * 0.6213712d) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                            } else {

                                listStep.add((int) value[0] + 0d);
                                listCalo.add((int) value[1] + 0d);
                                int td = ((int) ((metric == 0 ? value[2] : value[2] * 0.6213712d) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                            }
                        } else {
                            if (pedoRealData != null) {
                                listStep.add(pedoRealData.getPedoNum() + 0d);
                                listCalo.add(pedoRealData.getCaloric() + 0d);
                                int td = ((int) ((metric == 0 ? pedoRealData.getDistance() : pedoRealData.getDistance() * 0.6213712d) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                            } else {
                                listStep.add(0d);
                                listDist.add(0d);
                                listCalo.add(0d);
                            }
                        }
                    }
                }
                break;
            case PedoHistoryFragment.DATE_ONE_WEEK:

                calendar.add(Calendar.DAY_OF_MONTH, (7 - dayOfWeek) - 7 * (count - position) + 1);
                for (int i = 0; i < 7; i++) {
                    String date = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
                    listStart.add(date);
                    listEnd.add(date);
                    listStr.add(weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (baseDevice == null) {
                        listStep.add(0d);
                        listDist.add(0d);
                        listCalo.add(0d);
                    } else {
                        float[] value1 = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                "date(" + DbSprotDayData.COLUMN_DATE + ")=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                new String[]{date, baseDevice.getMac()});
                        SportDayData sportDayData = dbSprotDayData.findFirst(DbSprotDayData.COLUMN_DATE + "=? and " + DbSprotDayData.COLUMN_MAC + "=?",
                                new String[]{date, baseDevice.getMac()}, null);
                        PedoRealData pedoRealData = null;
                        if (date.equals(currentDateString)) {////如果是当天
                            pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(date, baseDevice.getMac());
                        }
                        if (sportDayData == null) {
                            if (pedoRealData != null) {
                                listStep.add(pedoRealData.getPedoNum() + 0d);
                                int td = ((int) ((metric == 0 ? pedoRealData.getDistance() : pedoRealData.getDistance() * 0.6213712f) * 1000));
                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                                listCalo.add(pedoRealData.getCaloric() + 0d);
                            } else {
                                listStep.add(0d);
                                listDist.add(0d);
                                listCalo.add(0d);
                            }
                        } else {
                            if (pedoRealData != null) {
                                sportDayData.setTotalCaloric(pedoRealData.getCaloric());
                                sportDayData.setTotalDist(pedoRealData.getDistance());
                                sportDayData.setTotalStep(pedoRealData.getPedoNum());
                            }
                            listStep.add(sportDayData.getTotalStep() + 0d);
                            int td = ((int) ((metric == 0 ? sportDayData.getTotalDist() : sportDayData.getTotalDist() * 0.6213712f) * 1000));
                            listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                            listCalo.add(sportDayData.getTotalCaloric() + 0d);
                        }
                    }

                }
                break;
        }
        handler.sendEmptyMessage(0x01);
    }

    public void reloadData() {
        //if(listStr != null && listStr.size() == 0) {
        //new Thread(loadRunnable).start();
        Log.e(TAG, "*****HAHAH");
        loadRealtimeData(1, null);
        //}
    }

    public void updateUI() {
        if (pedoHistView != null) {

            List<Double> tpl = new ArrayList<>();
            switch (dataType) {
                case PedoHistoryFragment.TYPE_STEP:
                    tpl.addAll(listStep);
                    break;
                case PedoHistoryFragment.TYPE_CALOR:

                    tpl.addAll(listCalo);
                    break;
                case PedoHistoryFragment.TYPE_DIST:

                    tpl.addAll(listDist);
                    break;
            }
            pedoHistView.setmLabels(listStr, tpl);

            if (mContext != null) {
                if (dateType == PedoHistoryFragment.DATE_DAY) {
                    tvDate.setText(getWeek() + mContext.getString(R.string.week) + "/" + getCurentYear());
                } else {
                    tvDate.setText(getCurentYear());
                }
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x01) {
                updateUI();
            } else {

            }
        }
    };

    public String getWeek() {
        Calendar calendar = Calendar.getInstance();
        if (dateType == PedoHistoryFragment.DATE_DAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -7 * (count - position) + 1);
            for (int i = 0; i < 7; i++) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            return calendar.get(Calendar.WEEK_OF_YEAR) + "";
        }
        return "";
    }

    public String getCurentYear() {
        String curYe = "";
        Calendar calendar = Calendar.getInstance();
        switch (dateType) {
            case PedoHistoryFragment.DATE_DAY: {
                calendar.add(Calendar.DAY_OF_MONTH, -7 * (count - position) + 1);
                for (int i = 0; i < 7; i++) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                curYe = calendar.get(Calendar.YEAR) + "";
            }
            break;
            case PedoHistoryFragment.DATE_WEEK:
                calendar.add(Calendar.WEEK_OF_YEAR, -7 * (count - position - 1) - 6);
                for (int i = 0; i < 7; i++) {
                    int dayOfWeek1 = calendar.get(Calendar.DAY_OF_WEEK);
                    calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeek1 + 1);
                }
                curYe = calendar.get(Calendar.YEAR) + "";
                break;
            case PedoHistoryFragment.DATE_ONE_WEEK:
                curYe = calendar.get(Calendar.YEAR) + "";
                break;
            case PedoHistoryFragment.DATE_MONTH:
                calendar.add(Calendar.MONTH, -6 - 7 * (count - position - 1));
                for (int i = 0; i < 7; i++) {
                    calendar.add(Calendar.MONTH, 1);
                }
                curYe = calendar.get(Calendar.YEAR) + "";
                break;
        }
        return curYe;
    }

    private synchronized void updateIfHasRealData(final Intent intent) {

        String dateString = intent.getStringExtra(BaseController.EXTRA_REAL_DATE);
        Calendar calendar = Calendar.getInstance();
        Calendar curCalendar = calendar;
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        try {
            if (position == count - 1) {
                int metric = UserInfo.getInstance(MyApp.getInstance()).getMetricImperial();///0 公制  1英制
                DbSprotDayData dbSprotDayData = DbSprotDayData.getInstance(mContext);
                MainService mainService = MainService.getInstance(getContext());
                BaseDevice baseDevice = mainService.getCurrentDevice();
                if (baseDevice == null) {
                    return;
                }
                if (listStep.size() < 7) {
                    for (int i = listStep.size(); i < 7; i++) {
                        listStep.add(0d);
                    }
                }
                if (listCalo.size() < 7) {
                    for (int i = listCalo.size(); i < 7; i++) {
                        listCalo.add(0d);
                    }
                }
                if (listDist.size() < 7) {
                    for (int i = listDist.size(); i < 7; i++) {
                        listDist.add(0d);
                    }
                }
                switch (dateType) {
                    case PedoHistoryFragment.DATE_DAY: {
                        calendar.add(Calendar.DAY_OF_MONTH, -7 * (count - position) + 1);
                        if (listDist.size() < 7) {
                            reloadData();
                        } else {
                            for (int i = 0; i < 7; i++) {
                                String date = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
                                if (date.equals(dateString)) {
                                    PedoRealData pedoRealData = null;
                                    //if (curCalendar.getTime().getTime()>=calendar1.getTime().getTime() && curCalendar.getTime().getTime()<=calendar2.getTime().getTime()){
                                    pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(dateString, baseDevice.getMac());
                                    //}

                                    int tSteps = intent.getIntExtra(BaseController.EXTRA_REAL_STEPS, i < listStep.size() ? listStep.get(i).intValue() : 0);
                                    int tCalo = intent.getIntExtra(BaseController.EXTRA_REAL_CALORIC, i < listCalo.size() ? listCalo.get(i).intValue() : 0);
                                    float tDist = intent.getFloatExtra(BaseController.EXTRA_REAL_DIST, i < listDist.size() ? listDist.get(i).floatValue() : 0);

                                    int td = (((int) ((metric == 0 ? tDist : tDist * 0.6213712f) * 1000)));
                                    if (i < listDist.size()) {
                                        listDist.remove(i);
                                        listStep.remove(i);
                                        listCalo.remove(i);
                                        listDist.add(i, Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td *0.001+0d);
                                        listCalo.add(i, tCalo + 0d);
                                        listStep.add(i, tSteps + 0d);
                                    } else {
                                        listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td *0.001+0d);
                                        listCalo.add(tCalo + 0d);
                                        listStep.add(tSteps + 0d);
                                    }
                                    break;
                                }
                                calendar.add(Calendar.DAY_OF_MONTH, 1);

                            }
                        }
                    }
                    break;
                    case PedoHistoryFragment.DATE_WEEK:
                        calendar.add(Calendar.WEEK_OF_YEAR, -7 * (count - position - 1) - 6);
                        if (listDist.size() < 7) {
                            reloadData();
                        } else {
                            for (int i = 0; i < 7; i++) {
                                int dayOfWeek1 = calendar.get(Calendar.DAY_OF_WEEK);

                                calendar.add(Calendar.DAY_OF_MONTH, -dayOfWeek1 + 1);
                                Calendar calendar1 = calendar;
                                String start = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");

                                calendar.add(Calendar.DAY_OF_MONTH, 6);
                                Calendar calendar2 = calendar;
                                String end = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");

                                Date now = UtilTools.string2Date(dateString, "yyyy-MM-dd");
                                Date dstart = UtilTools.string2Date(start, "yyyy-MM-dd");
                                Date dend = UtilTools.string2Date(end, "yyyy-MM-dd");
                                if (dstart.getTime() <= now.getTime() && now.getTime() <= dend.getTime()) {
                                    PedoRealData pedoRealData = null;
                                    float[] value1 = new float[]{0, 0, 0};
                                    pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(dateString, baseDevice.getMac());
                                    value1 = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                            new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                            "date(" + DbSprotDayData.COLUMN_DATE + ")=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                            new String[]{dateString, baseDevice.getMac()});

                                    float[] value = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                            new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                            "date(" + DbSprotDayData.COLUMN_DATE + ")>=date(?) and date(" + DbSprotDayData.COLUMN_DATE + ")<=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                            new String[]{start, end, baseDevice.getMac()});


                                    if (value != null) {
                                        if (i < listDist.size()) {
                                            listDist.remove(i);
                                            listStep.remove(i);
                                            listCalo.remove(i);
                                            if (pedoRealData != null && value1 != null && pedoRealData.getPedoNum() > value1[0]) {
                                                listStep.add(i, (int) (value[0] + pedoRealData.getPedoNum() - value1[0]) + 0d);
                                                listCalo.add(i, (int) (value[1] + pedoRealData.getCaloric() - value1[1]) + 0d);
                                                int td = ((int) ((metric == 0 ? (value[2] + pedoRealData.getDistance() - value1[2]) : (value[2] + pedoRealData.getDistance() - value1[2]) * 0.6213712f) * 1000));
                                                listDist.add(i, Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                                            } else {

                                                listStep.add(i, (int) value[0] + 0d);
                                                listCalo.add(i, (int) value[1] + 0d);
                                                int td = ((int) ((metric == 0 ? value[2] : value[2] * 0.6213712f) * 1000));
                                                listDist.add(i, Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                                            }
                                        } else {
                                            if (pedoRealData != null && value1 != null && pedoRealData.getPedoNum() > value1[0]) {
                                                listStep.add((int) (value[0] + pedoRealData.getPedoNum() - value1[0]) + 0d);
                                                listCalo.add((int) (value[1] + pedoRealData.getCaloric() - value1[1]) + 0d);
                                                int td = ((int) ((metric == 0 ? (value[2] + pedoRealData.getDistance() - value1[2]) : (value[2] + pedoRealData.getDistance() - value1[2]) * 0.6213712f) * 1000));
                                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                                            } else {

                                                listStep.add((int) value[0] + 0d);
                                                listCalo.add((int) value[1] + 0d);
                                                int td = ((int) ((metric == 0 ? value[2] : value[2] * 0.6213712f) * 1000));
                                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));//td*0.001+0d);
                                            }
                                        }
                                    }

                                    break;
                                }
                                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                            }
                        }
                        break;
                    case PedoHistoryFragment.DATE_MONTH:
                        calendar.add(Calendar.MONTH, -6 - 7 * (count - position - 1));
                        if (listDist.size() < 7) {
                            reloadData();
                        } else {
                            for (int i = 0; i < 7; i++) {
                                String date = UtilTools.date2String(calendar.getTime(), "yyyy-MM");
                                if (dateString.startsWith(date)) {
                                    float[] value = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                            new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                            DbSprotDayData.COLUMN_DATE + " like ? and " + DbSprotDayData.COLUMN_MAC + "=?",
                                            new String[]{date + "%", baseDevice.getMac()});
                                    float[] value1 = dbSprotDayData.sum(DbSprotDayData.TABLE_NAME,
                                            new String[]{"sum(" + DbSprotDayData.COLUMN_T_STEP + ")", "sum(" + DbSprotDayData.COLUMN_T_CALOR + ")", "sum(" + DbSprotDayData.COLUMN_T_DIST + ")"},
                                            "date(" + DbSprotDayData.COLUMN_DATE + ")=date(?) and " + DbSprotDayData.COLUMN_MAC + "=?",
                                            new String[]{dateString, baseDevice.getMac()});
                                    int tSteps = intent.getIntExtra(BaseController.EXTRA_REAL_STEPS, i < listStep.size() ? listStep.get(i).intValue() : 0);
                                    int tCalo = intent.getIntExtra(BaseController.EXTRA_REAL_CALORIC, i < listCalo.size() ? listCalo.get(i).intValue() : 0);
                                    float tDist = intent.getFloatExtra(BaseController.EXTRA_REAL_DIST, i < listDist.size() ? listDist.get(i).floatValue() : 0);

                                    if (value != null) {

                                        if (i < listDist.size()) {
                                            listDist.remove(i);
                                            listStep.remove(i);
                                            listCalo.remove(i);
                                            if (value1 != null && tSteps > value1[0]) {
                                                listStep.add(i, (int) (value[0] + tSteps - value1[0]) + 0d);
                                                listCalo.add(i, (int) (value[1] + tCalo - value1[1]) + 0d);
                                                int td = ((int) (((metric == 0 ? (value[2] + tDist - value1[2]) : (value[2] + tDist - value1[2]) * 0.6213712f)) * 1000));
                                                listDist.add(i, Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));// td*0.001+0d);
                                            } else {

                                                listStep.add(i, (int) value[0] + 0d);
                                                listCalo.add(i, (int) value[1] + 0d);
                                                int td = ((int) ((metric == 0 ? value[2] : value[2] * 0.6213712f) * 1000));
                                                listDist.add(i, Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));// td*0.001+0d);
                                            }
                                        } else {
                                            if (value1 != null && tSteps > value1[0]) {
                                                listStep.add((int) (value[0] + tSteps - value1[0]) + 0d);
                                                listCalo.add((int) (value[1] + tCalo - value1[1]) + 0d);
                                                int td = ((int) (((metric == 0 ? (value[2] + tDist - value1[2]) : (value[2] + tDist - value1[2]) * 0.6213712f)) * 1000));
                                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));// td*0.001+0d);
                                            } else {

                                                listStep.add((int) value[0] + 0d);
                                                listCalo.add((int) value[1] + 0d);
                                                int td = ((int) ((metric == 0 ? value[2] : value[2] * 0.6213712f) * 1000));
                                                listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));// td*0.001+0d);
                                            }
                                        }

                                    }
                                    break;
                                }
                                calendar.add(Calendar.MONTH, 1);
                            }
                        }
                        break;
                    case PedoHistoryFragment.DATE_ONE_WEEK:
                        calendar.add(Calendar.DAY_OF_MONTH, (7 - dayOfWeek) - 7 * (count - position) + 1);
                        if (listDist.size() < 7) {
                            reloadData();
                        } else {
                            for (int i = 0; i < 7; i++) {
                                String date = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
                                if (date.equals(dateString)) {
                                    PedoRealData pedoRealData = null;
                                    //if (curCalendar.getTime().getTime()>=calendar1.getTime().getTime() && curCalendar.getTime().getTime()<=calendar2.getTime().getTime()){
                                    pedoRealData = DbRealTimePedo.getInstance(mContext).findFirst(dateString, baseDevice.getMac());
                                    //}
                                    int tSteps = intent.getIntExtra(BaseController.EXTRA_REAL_STEPS, i < listStep.size() ? listStep.get(i).intValue() : 0);
                                    int tCalo = intent.getIntExtra(BaseController.EXTRA_REAL_CALORIC, i < listCalo.size() ? listCalo.get(i).intValue() : 0);
                                    float tDist = intent.getFloatExtra(BaseController.EXTRA_REAL_DIST, i < listDist.size() ? listDist.get(i).floatValue() : 0);
                                    if (i < listDist.size()) {
                                        listDist.remove(i);
                                        listStep.remove(i);
                                        listCalo.remove(i);
                                        int td = ((int) ((metric == 0 ? tDist : tDist * 0.6213712f) * 1000));
                                        listDist.add(i, Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));
                                        listCalo.add(i, tCalo + 0d);
                                        listStep.add(i, tSteps + 0d);
                                    } else {
                                        int td = ((int) ((metric == 0 ? tDist : tDist * 0.6213712f) * 1000));
                                        listDist.add(Double.valueOf(String.format(Locale.ENGLISH, "%.2f", td * 0.001f)));
                                        listCalo.add(tCalo + 0d);
                                        listStep.add(tSteps + 0d);
                                    }

                                    break;
                                }
                                calendar.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        }
                        break;
                }
                handler.sendEmptyMessage(0x01);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }

    public synchronized void syncData(int type, Intent intent) {
        if (type == 1) {
            initData();
        } else {
            updateIfHasRealData(intent);
        }
    }

    private void loadRealtimeData(final int type, final Intent intent) {
        MyApp.getInstance().executorService.submit(new Runnable() {
            @Override
            public void run() {
                syncData(type, intent);
            }
        });
    }

    public void onEventMainThread(final Intent intent) {
        String action = intent.getAction();
        if (action.equals(MainService.ACTION_SYNC_COMPLETED)) {
            int state = intent.getIntExtra(MainService.EXTRA_SYNC_STATE, BaseController.STATE_SYNC_COMPLETED);
            if (state == BaseController.STATE_SYNC_COMPLETED || state == BaseController.STATE_SYNC_ERROR) {
                reloadData();
            }
        } else if (action.equals(BaseController.ACTION_REAL_DATA)) {
            loadRealtimeData(0, intent);
        }
    }
}
