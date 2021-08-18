package com.isport.tracker.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.Cmd337BController;
import com.isport.isportlibrary.database.DbSportData337B;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.SportData337B;
import com.isport.isportlibrary.entry.UserInfo;
import com.isport.tracker.MyApp;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.TasksCompletedView;
import com.ypy.eventbus.EventBus;

import java.util.Calendar;


/**
 * Created by Administrator on 2016/12/15.
 */

public class WSportFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvSportTime;
    private TextView tvRestTime;
    private TextView tvDist;
    private TextView tvDeep;
    private TextView tvLight;
    private TextView tvCaloric;
    private TextView tvTotalStep;
    private TextView tvPercent;
    private TasksCompletedView tasksCompletedView;

    private TextView tvWeek, tvDate;
    private int position, count;
    private String tvContentValue;
    private Context mContext;
    private String[] weeks;

    public static WSportFragment newInstance(int position, int count) {

        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("count", count);
        WSportFragment fragment = new WSportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    private void loadData() {
        MyApp.getInstance().executorService.submit(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });
    }


    private synchronized void initData() {
        MainService mainService = MainService.getInstance(mContext);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            SportData337B sportData = DbSportData337B.getIntance(mContext).findFirst(DbSportData337B.COLUMN_DATE + "=? and " + DbSportData337B.COLUMN_MAC + "=?",
                    new String[]{tvContentValue, mainService.getCurrentDevice().getMac()});
            //if (sportData != null) {
            Message msg = Message.obtain();
            msg.obj = sportData;
            msg.what = 0x01;
            handler.sendMessage(msg);
            //}
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wsport, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        tvCaloric = (TextView) view.findViewById(R.id.wsport_caloric);
        tvDist = (TextView) view.findViewById(R.id.wsport_dist);
        tvDeep = (TextView) view.findViewById(R.id.wsport_deeptime);
        tvLight = (TextView) view.findViewById(R.id.wsport_lighttime);
        tvRestTime = (TextView) view.findViewById(R.id.wsport_rest);
        tvSportTime = (TextView) view.findViewById(R.id.wsport_sporttime);
        tvTotalStep = (TextView) view.findViewById(R.id.wsport_stepnum);
        tasksCompletedView = (TasksCompletedView) view.findViewById(R.id.tasks_view);
        tvPercent = (TextView) view.findViewById(R.id.exercise_per);
        tvWeek = (TextView) view.findViewById(R.id.main_fragment_text_Week);
        tvDate = (TextView) view.findViewById(R.id.main_fragment_text_date);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * (count - position - 1));
        tvWeek.setText(weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        tvDate.setText(tvContentValue);


        view.findViewById(R.id.relativeLayout).setOnClickListener(this);

        updateUI();
        EventBus.getDefault().register(this);
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateUI() {
        if (tvRestTime != null) {
            if (mSportData == null) {
                tvRestTime.setText(0 + "h" + 0 + "min");
                tvSportTime.setText(0 + "h" + 0 + "min");
                tvDeep.setText(0 + "h" + 0 + "min");
                tvLight.setText(0 + "h" + 0 + "min");
                tvCaloric.setText(0 + mContext.getString(R.string.kcal));
                tvDist.setText(0 + mContext.getString(R.string.ride_km));
                tvTotalStep.setText(0 + "");
            } else {

                tvRestTime.setText(mSportData.getDayRestTime() / 60 + "h" + mSportData.getDayRestTime() % 60 + "min");
                tvSportTime.setText(mSportData.getSportTime() / 60 + "h" + mSportData.getSportTime() % 60 + "min");
                tvDeep.setText(mSportData.getDeepTime() / 60 + "h" + mSportData.getDeepTime() % 60 + "min");
                tvLight.setText(mSportData.getLightTime() / 60 + "h" + mSportData.getLightTime() % 60 + "min");
                tvCaloric.setText(/*String.format("%.3f",*/mSportData.getCalorics()/* * 0.001f)*/ + mContext.getString(R.string.kcal));
                BaseDevice baseDevice = MainService.getInstance(mContext).getCurrentDevice();
                UserInfo userInfo = UserInfo.getInstance(mContext);
                int metric = userInfo.getMetricImperial();
                if (metric == 0) {
                    float dist = (mSportData.getDistance() * 0.001f);
                    tvDist.setText(String.format(dist <= 1 ? "%.0f" : "%.3f", (dist <= 1 ? (int) (dist * 1000) : dist)) + (dist <= 1 ? mContext.getString(R.string.m) : mContext.getString(R.string.ride_km)));
                } else {
                    tvDist.setText(String.format("%.3f", (mSportData.getDistance() * 0.001f) * 0.6213712f) + mContext.getString(R.string.ride_mi));
                }

                tvTotalStep.setText(mSportData.getTotalStepNum() + "");
                int goal = userInfo.getTargetStep();
                tasksCompletedView.setProgress(mSportData.getTotalStepNum() * 100 / goal);
                tvPercent.setText(mSportData.getTotalStepNum() * 100 / goal + "%");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relativeLayout:
                if (MainService.getInstance(mContext) != null) {
                    MainService.getInstance(mContext).startSyncData();
                }
                break;
        }
    }

    @Override
    public void clearAdapter() {
        super.clearAdapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        weeks = context.getResources().getStringArray(R.array.week);
        position = getArguments().getInt("position");
        count = getArguments().getInt("count");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * (count - position - 1));
        tvContentValue = UtilTools.date2String(calendar.getTime(), "yyyy-MM-dd");
        loadData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        if (tasksCompletedView != null) {
            tasksCompletedView.stopAnimation();
        }
        tvSportTime = null;
        tvRestTime = null;
        tvDist = null;
        tvDeep = null;
        tvLight = null;
        tvCaloric = null;
        tvTotalStep = null;
        tvPercent = null;
        tasksCompletedView = null;
        super.onDestroyView();
    }

    private SportData337B mSportData;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    if (msg.obj != null) {
                        mSportData = (SportData337B) msg.obj;
                    } else {
                        mSportData = null;
                    }
                    updateUI();
                    break;
            }
        }
    };

    private BroadcastReceiver mreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Cmd337BController.ACTION_SPORT_DATA)) {
                SportData337B sportData = (SportData337B) intent.getSerializableExtra(Cmd337BController.EXTRA_SPORT_DATA);
                if (sportData != null && sportData.getDate().equals(tvContentValue)) {
                    Message msg = Message.obtain();
                    msg.obj = sportData;
                    msg.what = 0x01;
                    handler.sendMessage(msg);
                }
            } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
                int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
                if (state == BaseController.STATE_CONNECTED) {
                    loadData();
                }
            }
        }
    };

    public void onEventMainThread(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Cmd337BController.ACTION_SPORT_DATA)) {
            SportData337B sportData = (SportData337B) intent.getSerializableExtra(Cmd337BController.EXTRA_SPORT_DATA);
            if (sportData != null && sportData.getDate().equals(tvContentValue)) {
                Message msg = Message.obtain();
                msg.obj = sportData;
                msg.what = 0x01;
                handler.sendMessage(msg);
            }
        } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
            int state = intent.getIntExtra(MainService.EXTRA_CONNECTION_STATE, BaseController.STATE_DISCONNECTED);
            if (state == BaseController.STATE_CONNECTED) {
                loadData();
            }
        }
    }
}
