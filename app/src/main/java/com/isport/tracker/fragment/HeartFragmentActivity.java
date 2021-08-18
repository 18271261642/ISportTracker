package com.isport.tracker.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.entry.BaseDevice;
import com.isport.isportlibrary.entry.DeviceInfo;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.HeartDataInfo;
import com.isport.tracker.hrs.LineGraphView;
import com.isport.tracker.main.HeartHistoryActivity;
import com.isport.tracker.main.HeartRateHistoryNActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.view.CircularProgressDrawable;
import com.isport.tracker.view.ConfirmDialog;
import com.isport.tracker.view.SaveHRDialog;
import com.isport.tracker.view.TasksCompletedView;
import com.ypy.eventbus.EventBus;

import org.achartengine.GraphicalView;

import java.text.DecimalFormat;
import java.util.List;

import static com.isport.tracker.main.settings.ActivityDeviceSetting.ACTION_HEART_AUTO_SAVE;
import static com.isport.tracker.main.settings.ActivityDeviceSetting.EXTRA_HEART_AUTO_SAVE;


/**
 * Created by feige on 2017/4/21.
 */

public class HeartFragmentActivity extends BaseFragment implements View.OnClickListener {

    private static final String TAG = HeartFragmentActivity.class.getSimpleName();
    private Context mContext;
    private TextView tvHistory, tvStartStop, tvHeartValue, tvHeartMax, tvHeartMin, tvHeartAvg, tvTotalCal;
    private TasksCompletedView tasksCompletedView;
    private GraphicalView mGraphView;
    private LineGraphView mLineGraph;
    private Chronometer chronometer;
    private CircularProgressDrawable circularProgressDrawable;
    //private View taskBackView;
    private ViewGroup layout;
    private boolean isRunning = false;

    public static HeartFragmentActivity newInstance() {
        Bundle args = new Bundle();
        HeartFragmentActivity fragment = new HeartFragmentActivity();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainService mainService = MainService.getInstance(mContext);
        if (mainService != null) {
            HeartDataInfo heartDataInfo = mainService.getHeartDataInfo();
            updateHeartData(heartDataInfo);
        }
        EventBus.getDefault().register(this);
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HEART_AUTO_SAVE);
        filter.addAction(MainService.ACTION_CONNECTE_CHANGE);
        filter.addAction(MainService.ACTION_CONNECTE_ERROR);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
    }


    public void updateTvHeartCalState() {
        if (mContext != null) {
            MainService mainService = MainService.getInstance(mContext);
            if (mainService != null && mainService.getCurrentDevice() != null && mainService.getCurrentDevice()
                    .getDeviceType() == BaseDevice.TYPE_HEART_RATE) {
                tvTotalCal.setVisibility(View.VISIBLE);
            } else {
                tvTotalCal.setVisibility(View.GONE);
            }
        }
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timeHandler.hasMessages(0x01)) {
            timeHandler.removeMessages(0x01);
        }
        EventBus.getDefault().unregister(this);
        unregisterReceiver();
        if (tasksCompletedView != null) {
            tasksCompletedView.stopAnimation();
        }
    }

    boolean isSynCom = false;

    public void onEventMainThread(boolean isSynCom) {

        Log.i("isSynCom", isSynCom + "");

        this.isSynCom = isSynCom;
    }

    public void onEventMainThread(HeartDataInfo dataInfo) {
        if (tvHeartAvg == null)
            return;
        if (dataInfo == null) {
            updateWithOutHeartData();
        } else {
            updateHeartData(dataInfo);
        }
    }

    private void updateWithOutHeartData() {
        tvHeartAvg.setText(mContext.getString(R.string.hr_avg_default));
        tvHeartMax.setText(mContext.getString(R.string.hr_max_default));
        tvHeartMin.setText(mContext.getString(R.string.hr_min_default));
        tvHeartValue.setText(mContext.getString(R.string.hr_not_available_value));
        if (circularProgressDrawable.isRunning()) {
            circularProgressDrawable.stop();
            updateTaskBackground();
        }
        if (tasksCompletedView.getProgress() != 0) {
            tasksCompletedView.setProgress(0);
        }
        mLineGraph.clearGraph();
        mGraphView.repaint();
        chronometer.setText(longToString(0));
        tvTotalCal.setText("0" + mContext.getString(R.string.kcal));
        MainService mainService = MainService.getInstance(mContext);
        if (mainService.isStartHeart()) {
            tvStartStop.setText(mContext.getString(R.string.hr_end));
        } else {
            tvStartStop.setText(mContext.getString(R.string.hr_start));
        }
        if (timeHandler.hasMessages(0x01)) {
            timeHandler.removeMessages(0x01);
        }
    }

    boolean isFirst = true;

    public void updateHeartData(HeartDataInfo dataInfo) {
        List<Integer> list = dataInfo.getDataList();
        MainService mainService = MainService.getInstance(mContext);
        int size2 = list.size();
        if (size2 == 0 || mainService == null || !mainService.isHasReceivedHeartData()) {
            //todo 当是手动存储且点击了开始的情况时,弹出保存窗
            boolean isAutoSaveH = ConfigHelper.getInstance(mContext).getBoolean(Constants.IS_AUTO_SAVE_HEART, false);
            if (!isAutoSaveH && isRunning) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    showSaveDialog("111111111111111111  000");
                }
            } else {
                //1.未点开始说明不用存储
                //2.如果自动保存，那么直接清空
                updateWithOutHeartData();
            }
        } else {
            BaseDevice dbv = mainService.getCurrentDevice();
            tvHeartAvg.setText(mContext.getString(R.string.hr_avg, dataInfo.getAvg()));
            tvHeartMax.setText(mContext.getString(R.string.hr_max, dataInfo.getMax()));
            tvHeartMin.setText(mContext.getString(R.string.hr_min, dataInfo.getMin()));
            if (tvTotalCal.getVisibility() == View.VISIBLE) {
                tvTotalCal.setText((new DecimalFormat("0.00").format(dataInfo.getTotalCal() / 1000f)) + getString(R.string.kcal));
            }
            tvHeartValue.setText(mContext.getString(R.string.hr_heart_rate_value, (list != null && size2 > 0) ? list
                    .get(size2 - 1) : 0));
            long heartStart = dataInfo.getStartTime();

            long boot = SystemClock.elapsedRealtime();
            long current = System.currentTimeMillis();

            boolean isAutoSaveH = ConfigHelper.getInstance(mContext).getBoolean(Constants.IS_AUTO_SAVE_HEART, false);

            if ((!isAutoSaveH && mainService.isStartHeart()) || isAutoSaveH) {
                chronometer.setText(longToString(current - heartStart));
            } else {
                chronometer.setText(longToString(0));
            }

            Log.e(TAG, "***addGraph***");
            addGraph(dataInfo.getDataList());

            if (!circularProgressDrawable.isRunning()) {
                circularProgressDrawable.start();
            }
            if (tasksCompletedView.getProgress() != 100) {
                tasksCompletedView.setProgress(100);
            }
            if (!timeHandler.hasMessages(0x01)) {
                timeHandler.sendEmptyMessageDelayed(0x01, 900);
            }
        }
    }

    private void addGraph(List<Integer> list) {
        mLineGraph.clearGraph();
        mGraphView.repaint();
        if (list == null)
            return;
        for (int i = 0; i < list.size(); i++) {
            mLineGraph.addValue(new Point(i, list.get(i)));
        }
        mGraphView.repaint();
    }

    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainService mainService = MainService.getInstance(mContext);
            if (mainService != null && mainService.getHeartDataInfo() != null) {
                long heartStart = mainService.getHeartDataInfo().getStartTime();
                //if (heartStart != 0) {
                long boot = SystemClock.elapsedRealtime();
                long current = System.currentTimeMillis();

                boolean isAutoSaveH = ConfigHelper.getInstance(mContext).getBoolean(Constants.IS_AUTO_SAVE_HEART,
                        false);
                if ((!isAutoSaveH && mainService.isStartHeart()) || isAutoSaveH) {
                    chronometer.setText(longToString(current - heartStart));
                } else {
                    chronometer.setText(longToString(0));
                }
            }
            timeHandler.sendEmptyMessageDelayed(0x01, 900);
        }
    };

    private String longToString(long time) {
        time = time / 1000;
        return String.format("%02d", time / 3600) + ":" + String.format("%02d", (time % 3600) / 60) + ":" + String
                .format("%02d", time % 60);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_heart_reat, container, false);
        tvHistory = (TextView) view.findViewById(R.id.tv_heart_rate_history);
        tvStartStop = (TextView) view.findViewById(R.id.tv_operation);
        tvHeartValue = (TextView) view.findViewById(R.id.text_hrs_value);
        tvHeartMax = (TextView) view.findViewById(R.id.tv_max);
        tvHeartMin = (TextView) view.findViewById(R.id.tv_min);
        tvHeartAvg = (TextView) view.findViewById(R.id.tv_avg);
        tvTotalCal = (TextView) view.findViewById(R.id.tv_totalcal);
        chronometer = (Chronometer) view.findViewById(R.id.tv_time);
        //heartChartView = (HeartChartView) view.findViewById(R.id.heart_chart_view);
        tasksCompletedView = (TasksCompletedView) view.findViewById(R.id.tasks_view);
        //tasksCompletedView.setmRingColor(2);
        //tasksCompletedView = view.findViewById(R.id.heart_bk_view);
        mLineGraph = LineGraphView.getLineGraphView(mContext);
        updateTaskBackground();
        tvHistory.setOnClickListener(this);
        tvStartStop.setOnClickListener(this);
        showGraph(view);
        updateTvHeartCalState();
        return view;
    }

    private void showGraph(View view) {
        mGraphView = mLineGraph.getView(getActivity());
        layout = (ViewGroup) view.findViewById(R.id.graph_hrs);
        layout.addView(mGraphView);
        mLineGraph.clearGraph();
        mGraphView.repaint();
    }

    private void updateTaskBackground() {
        if (tasksCompletedView != null) {
            circularProgressDrawable = new CircularProgressDrawable.Build(getActivity().getApplicationContext(), R
                    .style.Material_Drawable_CircularProgress).build();
            tasksCompletedView.setBackgroundDrawable(circularProgressDrawable);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isRunning) {
            Log.e(TAG, "***no run***");
            if (mLineGraph != null && mGraphView != null) {
                Log.e(TAG, "***clear***");
                mLineGraph.clearGraph();
                mGraphView.repaint();
            }
        } else {
            Log.e(TAG, "***running***");
        }
        updateUI(ConfigHelper.getInstance(mContext).getBoolean(Constants.IS_AUTO_SAVE_HEART, false));
    }

    /**
     * 是否自动存储心率
     *
     * @param isAutoSave
     */
    public void updateUI(boolean isAutoSave) {
        MainService mainService = MainService.getInstance(mContext);
        ///是否开始心率
        if (mainService != null && mainService.isStartHeart()) {
            Log.e(TAG, "1111111111111");
            tvStartStop.setText(mContext.getString(R.string.hr_end));
        } else {
            Log.e(TAG, "22222222222222222");
            tvStartStop.setText(mContext.getString(R.string.hr_start));
        }
        if (mainService != null && mainService.isHasReceivedHeartData()) {
            timeHandler.sendEmptyMessage(0x01);
        }
        if (isAutoSave && mainService != null) {
            //自动保存的情况下要主动展现数据了
            isRunning = true;
//            if (!mainService.isStartHeart()){
//                mainService.startHeartMonitor();
//            }
            updateHeartData(mainService.getHeartDataInfo());
            tvStartStop.setVisibility(View.GONE);
        } else {
            tvStartStop.setVisibility(View.VISIBLE);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_HEART_AUTO_SAVE)) {
                boolean state = intent.getBooleanExtra(EXTRA_HEART_AUTO_SAVE, false);
                updateUI(state);
            } else {
                updateTvHeartCalState();
            }
        }
    };

    public boolean isSupportCmdHeart() {
        MainService mainService = MainService.getInstance(mContext);
        if (mainService != null && mainService.getCurrentDevice() != null) {
            return mainService.isSupportCmdHeart(mainService.getCurrentDevice());
        }
        return false;
    }

    private float getVersion() {
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        float version = Float.valueOf(deviceInfo.getFirmwareHighVersion() + "." + deviceInfo.getFirmwareLowVersion());
        return version;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        MainService mainService = MainService.getInstance(mContext);
        switch (v.getId()) {
            case R.id.tv_heart_rate_history:
                if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED) {

                    if (!MainService.isSynCom) {
                        Toast.makeText(getActivity(), R.string.header_hint_refresh_loading, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //如果没有同步完成不可用点进去获取数据
                    BaseDevice bd = mainService.getCurrentDevice();
                    String dname = bd.getName();
                    String tpName = (dname == null ? "" : dname.contains("_") ? dname.split("_")[0] : dname.contains("-") ?
                            dname.split("-")[0] : dname.split(" ")[0]).toLowerCase();
                    if ((dname.contains("W311N_") || dname.contains("BEAT")) && getVersion() >= 91.00f || ((dname
                            .contains("REFLEX") || tpName.contains("rush")) && getVersion() >= 90.70f)) {
                        intent = new Intent(mContext, HeartRateHistoryNActivity.class);
                        if (dname.contains("BEAT") && getVersion() >= 91.26f) {
                            intent.putExtra("is15MinType", true);
                            if (getVersion() > 91.45f) {
                                intent.putExtra("is5MinType", true);
                            } else {
                                intent.putExtra("is5MinType", false);
                            }
                        } else {
                            intent.putExtra("is15MinType", false);
                            intent.putExtra("is5MinType", false);
                        }
                        startActivity(intent);
                        return;
                    } else {
                        intent = new Intent(mContext, HeartHistoryActivity.class);
                        startActivity(intent);
                    }
                } else {
                    // TODO: 2018/11/15 没有连接的状态不能进入历史页面
                    Toast.makeText(getActivity(), R.string.please_bind, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_operation:
                if (isSupportCmdHeart() &&
                        (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED)) {
                    if (mainService.isStartHeart()) {
                        showSaveDialog("222222222222222  000");
                    } else {
                        isRunning = true;
                        mainService.startHeartMonitor();
                        updateHeartData(mainService.getHeartDataInfo());
                        Log.e(TAG, "33333333333333");
                        tvStartStop.setText(mContext.getString(R.string.hr_end));
                    }
                } else if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED &&
                        mainService.isHasReceivedHeartData()) {
                    if (mainService.isStartHeart()) {
                        showSaveDialog("333333333333  000");
                    } else {
                        isRunning = true;
                        mainService.startHeartMonitor();
                        updateHeartData(mainService.getHeartDataInfo());
                    }
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.connect_or_open_heartTest), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void showSaveDialog(String s) {
        Log.e(TAG, s);
        new SaveHRDialog(getActivity(),
                new SaveHRDialog.OnDialogclickListener() {
                    @Override
                    public void disCard(SaveHRDialog dialog) {
                        new ConfirmDialog(getActivity(),
                                new ConfirmDialog.OnDialogclickListener() {
                                    @Override
                                    public void cancel(ConfirmDialog dialog) {
                                        dialog.cancel();
                                    }

                                    @Override
                                    public void confirm(ConfirmDialog dialog) {
                                        MainService mainService = MainService.getInstance(mContext);
                                        if (isSupportCmdHeart() && mainService != null &&
                                                mainService.getConnectionState() == BaseController
                                                        .STATE_CONNECTED) {
                                            Log.e(TAG, "444444444444444444");
                                            tvStartStop.setText(mContext.getString(R.string.hr_start));
                                        }
                                        isRunning = false;
                                        isFirst = true;
                                        dialog.cancel();
                                        mainService.discardHeartData();
                                        mLineGraph.clearGraph();
                                        mGraphView.repaint();
                                    }
                                }).show();
                        dialog.cancel();
                    }

                    @Override
                    public void cancel(SaveHRDialog dialog) {
                        dialog.cancel();

                    }

                    @Override
                    public void save(SaveHRDialog dialog) {
                        MainService mainService = MainService.getInstance(mContext);
                        if (isSupportCmdHeart() && mainService != null && mainService.getConnectionState()
                                == BaseController.STATE_CONNECTED) {
                            Log.e(TAG, "55555555555555555555");
                            tvStartStop.setText(mContext.getString(R.string.hr_start));
                        }
                        isRunning = false;
                        isFirst = true;
                        mainService.stopHeartMonitor(0, true);
                        updateHeartData(mainService.getHeartDataInfo());
                        dialog.cancel();
                        mLineGraph.clearGraph();
                        mGraphView.repaint();
                    }
                }).show(0, 20);
    }
}
