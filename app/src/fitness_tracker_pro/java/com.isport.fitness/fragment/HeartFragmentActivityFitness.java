package com.isport.fitness.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.HeartDataInfo;
import com.isport.tracker.fragment.BaseFragment;
import com.isport.tracker.hrs.LineGraphView;
import com.isport.tracker.main.HeartHistoryActivity;
import com.isport.tracker.util.ConfigHelper;
import com.isport.tracker.util.Constants;
import com.isport.tracker.view.CircularProgressDrawable;
import com.isport.tracker.view.ColorArcProgressBar;
import com.isport.tracker.view.ConfirmDialog;
import com.isport.tracker.view.HeartChartView;
import com.isport.tracker.view.SaveHRDialog;
import com.isport.tracker.view.TasksCompletedView;
import com.ypy.eventbus.EventBus;

import org.achartengine.GraphicalView;

import java.util.List;

import static com.isport.tracker.main.settings.ActivityDeviceSetting.ACTION_HEART_AUTO_SAVE;
import static com.isport.tracker.main.settings.ActivityDeviceSetting.EXTRA_HEART_AUTO_SAVE;


/**
 * Created by feige on 2017/4/21.
 */

public class HeartFragmentActivityFitness extends BaseFragment implements View.OnClickListener {

    private Context mContext;
    private TextView tvHistory, tvStartStop, tvHeartValue, tvHeartMax, tvHeartMin, tvHeartAvg;
    private TasksCompletedView tasksCompletedView;
    private GraphicalView mGraphView;
    private LineGraphView mLineGraph;
    private Chronometer chronometer;
    private CircularProgressDrawable circularProgressDrawable;
    //private View taskBackView;
    private ViewGroup layout;
    private ColorArcProgressBar progress;
    private HeartChartView chartView;

    public static HeartFragmentActivityFitness newInstance() {
        Bundle args = new Bundle();
        HeartFragmentActivityFitness fragment = new HeartFragmentActivityFitness();
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
        mContext.registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timeHandler.hasMessages(0x01)) {
            timeHandler.removeMessages(0x01);
        }
        EventBus.getDefault().unregister(this);
        unregisterReceiver();
        if(tasksCompletedView != null) {
            tasksCompletedView.stopAnimation();
        }
        if(progress != null) {
            progress.stopAnimation();
        }
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
        if (progress.getCurrentValue() != 0) {
            progress.setCurrentValues(0);
        }
        chronometer.setText(longToString(0));
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

    public void updateHeartData(HeartDataInfo dataInfo) {
        List<Integer> list = dataInfo.getDataList();
        int size2 = list.size();
        if (size2 == 0) {
            updateWithOutHeartData();
        } else {
            tvHeartAvg.setText(mContext.getString(R.string.hr_avg, dataInfo.getAvg()));
            tvHeartMax.setText(mContext.getString(R.string.hr_max, dataInfo.getMax()));
            tvHeartMin.setText(mContext.getString(R.string.hr_min, dataInfo.getMin()));
            tvHeartValue.setText(mContext.getString(R.string.hr_heart_rate_value, (list != null && size2 > 0) ? list.get(size2 - 1) : 0));
            long heartStart = dataInfo.getStartTime();

            long boot = SystemClock.elapsedRealtime();
            long current = System.currentTimeMillis();

            boolean isAutoSaveH =  ConfigHelper.getInstance(mContext).getBoolean(Constants.IS_AUTO_SAVE_HEART,false);
            MainService mainService = MainService.getInstance(mContext);
            if((!isAutoSaveH && mainService.isStartHeart()) || isAutoSaveH ) {
                chronometer.setText(longToString(current - heartStart));
            }else {
                chronometer.setText(longToString(0));
            }

            chartView.setmDataSerise(dataInfo.getDataList());
            addGraph(dataInfo.getDataList());

            if (!circularProgressDrawable.isRunning()) {
                circularProgressDrawable.start();
            }
            if (tasksCompletedView.getProgress() != 100) {
                tasksCompletedView.setProgress(100);
            }
            if (progress.getCurrentValue() != 100) {
                progress.setCurrentValues(100);
            }
            if (!timeHandler.hasMessages(0x01)) {
                timeHandler.sendEmptyMessageDelayed(0x01, 900);
            }
        }
    }

    private void addGraph(List<Integer> list) {
        mLineGraph.clearGraph();
        mGraphView.repaint();
        if(list == null)
            return;
        for(int i=0;i<list.size();i++){
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

                boolean isAutoSaveH =  ConfigHelper.getInstance(mContext).getBoolean(Constants.IS_AUTO_SAVE_HEART,false);
                if((!isAutoSaveH && mainService.isStartHeart()) || isAutoSaveH ) {
                    chronometer.setText(longToString(current - heartStart));
                }else {
                    chronometer.setText(longToString(0));
                }
            }
            timeHandler.sendEmptyMessageDelayed(0x01, 900);
        }
    };

    private String longToString(long time) {
        time = time / 1000;
        return String.format("%02d", time / 3600) + ":" + String.format("%02d", (time % 3600) / 60) + ":" + String.format("%02d", time % 60);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_heart_reat_fitness, container, false);
        tvHistory = (TextView) view.findViewById(R.id.tv_heart_rate_history);
        tvStartStop = (TextView) view.findViewById(R.id.tv_operation);
        tvHeartValue = (TextView) view.findViewById(R.id.text_hrs_value);
        chartView = (HeartChartView) view.findViewById(R.id.heart_chart_view);
        tvHeartMax = (TextView) view.findViewById(R.id.tv_max);
        tvHeartMin = (TextView) view.findViewById(R.id.tv_min);
        tvHeartAvg = (TextView) view.findViewById(R.id.tv_avg);
        chronometer = (Chronometer) view.findViewById(R.id.tv_time);
        //heartChartView = (HeartChartView) view.findViewById(R.id.heart_chart_view);
        tasksCompletedView = (TasksCompletedView) view.findViewById(R.id.tasks_view);
        progress = (ColorArcProgressBar) view.findViewById(R.id.progress);
        //tasksCompletedView.setmRingColor(2);
        //tasksCompletedView = view.findViewById(R.id.heart_bk_view);
        mLineGraph = LineGraphView.getLineGraphView(mContext);

        updateTaskBackground();

        tvHistory.setOnClickListener(this);
        tvStartStop.setOnClickListener(this);
        showGraph(view);

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
            circularProgressDrawable = new CircularProgressDrawable.Build(getActivity().getApplicationContext(), R.style.Material_Drawable_CircularProgress).build();
            tasksCompletedView.setBackgroundDrawable(circularProgressDrawable);
        }
        if (progress != null) {
            circularProgressDrawable = new CircularProgressDrawable.Build(getActivity().getApplicationContext(), R.style.Material_Drawable_CircularProgress).build();
            progress.setBackgroundDrawable(circularProgressDrawable);
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
            tvStartStop.setText(mContext.getString(R.string.hr_end));
        } else {
            tvStartStop.setText(mContext.getString(R.string.hr_start));
        }
        if (mainService != null && mainService.isHasReceivedHeartData()) {
            timeHandler.sendEmptyMessage(0x01);
        }
        if (isAutoSave) {
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
            }
        }
    };

    @Override
    public void onClick(View v) {
        Intent intent = null;
        MainService mainService = MainService.getInstance(mContext);
        switch (v.getId()) {
            case R.id.tv_heart_rate_history:
                intent = new Intent(mContext, HeartHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_operation:
                if (mainService != null && mainService.getConnectionState() == BaseController.STATE_CONNECTED && mainService.isHasReceivedHeartData()) {
                    if (mainService.isStartHeart()) {
                        showSaveDialog();
                    } else {
                        mainService.startHeartMonitor();
                        updateHeartData(mainService.getHeartDataInfo());
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.connect_or_open_heartTest), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void showSaveDialog(){
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
                                        dialog.cancel();
                                        MainService mainService = MainService.getInstance(mContext);
                                        mainService.discardHeartData();
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
                        mainService.stopHeartMonitor(0, true);
                        updateHeartData(mainService.getHeartDataInfo());
                        dialog.cancel();

                    }
                }).show(0, 20);
    }
}
