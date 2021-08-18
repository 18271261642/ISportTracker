package com.isport.tracker.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.isport.isportlibrary.controller.BaseController;
import com.isport.isportlibrary.controller.Cmd337BController;
import com.isport.isportlibrary.entry.OxygenData;
import com.isport.isportlibrary.entry.SportData337B;
import com.isport.tracker.R;
import com.isport.tracker.bluetooth.MainService;
import com.isport.tracker.entity.HeartDataInfo;
import com.isport.tracker.hrs.LineGraphView;
import com.isport.tracker.main.HeartHistoryActivity;
import com.isport.tracker.view.ConfirmDialog;
import com.isport.tracker.view.SaveHRDialog;
import com.ypy.eventbus.EventBus;

import org.achartengine.GraphicalView;

/**
 * Created by Administrator on 2016/12/15.
 */

public class WHeartFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvStartStop;
    private TextView tvMax;
    private TextView tvMin;
    private TextView tvAvg;
    private TextView tvTime;
    private TextView tvOxygen;
    private TextView tvHeartRate;
    private GraphicalView gvView;
    private LineGraphView mLineGraph;
    private FrameLayout layout;
    private Context mContext;


    public static WHeartFragment newInstance() {

        Bundle args = new Bundle();

        WHeartFragment fragment = new WHeartFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wheart, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initControl(view);
        EventBus.getDefault().register(this);
        super.onViewCreated(view, savedInstanceState);
    }

    public void initControl(View view) {
        mLineGraph = LineGraphView.getLineGraphView(getActivity());
        gvView = mLineGraph.getView(getActivity());

        view.findViewById(R.id.tv_heart_rate_history).setOnClickListener(this);
        tvAvg = (TextView) view.findViewById(R.id.tv_avg);
        tvMax = (TextView) view.findViewById(R.id.tv_max);
        tvMin = (TextView) view.findViewById(R.id.tv_min);
        tvOxygen = (TextView) view.findViewById(R.id.text_oxy_value);
        tvHeartRate = (TextView) view.findViewById(R.id.text_hrs_value);
        tvTime = (TextView) view.findViewById(R.id.tv_time);
        layout = (FrameLayout) view.findViewById(R.id.graph_hrs);
        layout.addView(gvView);
        mLineGraph.clearGraph();
        gvView.repaint();

        tvStartStop = (TextView) view.findViewById(R.id.tv_operation);
        tvStartStop.setOnClickListener(this);
        ///tvStartStop.setVisibility(View.GONE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        tvStartStop = null;
        tvMax = null;
        tvMin = null;
        tvAvg = null;
        tvTime = null;
        tvOxygen = null;
        tvHeartRate = null;
        gvView = null;
        mLineGraph = null;
        layout = null;
    }

    public void onEventMainThread(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Cmd337BController.ACTION_SPORT_DATA)) {
            SportData337B sportData = (SportData337B) intent.getSerializableExtra(Cmd337BController.EXTRA_SPORT_DATA);
            Message msg = Message.obtain();
            msg.obj = sportData;
            msg.what = 0x01;
        } else if (action.equals(MainService.ACTION_CONNECTE_CHANGE)) {
            if (MainService.getInstance(mContext).isStartHeart()) {
                tvStartStop.setText(getString(R.string.hr_end));
            } else {
                tvStartStop.setText(getString(R.string.hr_start));
            }
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
        MainService bleService = MainService.getInstance(mContext);
        if (bleService != null && bleService.isStartHeart()) {
            tvStartStop.setText(getString(R.string.hr_end));
        } else {
            tvStartStop.setText(getString(R.string.hr_start));
        }
    }

    @Override
    public void onClick(View v) {
        final MainService service = MainService.getInstance(mContext);
        switch (v.getId()) {
            case R.id.tv_operation:
                if (service.getConnectionState() != BaseController.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.please_connect), Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean state = !MainService.getInstance(mContext).isStartHeart();
                if (state == false) {
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

                                                    MainService bleService1 = MainService.getInstance(mContext);
                                                    bleService1.discardHeartData();
                                                    dialog.cancel();
                                                    mLineGraph.clearGraph();
                                                    gvView.repaint();
                                                    if (bleService1.isStartHeart()) {
                                                        tvStartStop.setText(mContext.getString(R.string.hr_end));
                                                    } else {
                                                        tvStartStop.setText(mContext.getString(R.string.hr_start));
                                                    }
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
                                    dialog.cancel();
                                    MainService.getInstance(mContext).stopHeartMonitor(0, true);
                                    mLineGraph.clearGraph();
                                    gvView.repaint();
                                    if (MainService.getInstance(mContext).isStartHeart()) {
                                        tvStartStop.setText(mContext.getString(R.string.hr_end));
                                    } else {
                                        tvStartStop.setText(mContext.getString(R.string.hr_start));
                                    }
                                }
                            }).show(0, 20);
                } else {
                    mLineGraph.clearGraph();
                    gvView.repaint();
                    boolean iss = service.startHeartMonitor();
                    if (!iss) {
                        Toast.makeText(mContext, mContext.getString(R.string.connect_or_open_heartTest), Toast.LENGTH_SHORT).show();
                    }
                    if (iss && service.isStartHeart()) {
                        tvStartStop.setText(mContext.getString(R.string.hr_end));
                    } else {
                        tvStartStop.setText(mContext.getString(R.string.hr_start));
                    }
                }


                break;
            case R.id.tv_heart_rate_history:
                Intent intent = new Intent(getActivity(), HeartHistoryActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void onEventMainThread(HeartDataInfo dataInfo) {
        if (dataInfo == null || dataInfo.getCurrentTime() == 0) {////已停止
            tvMax.setText(getString(R.string.hr_max_default));
            tvMin.setText(getString(R.string.hr_min_default));
            tvAvg.setText(getString(R.string.hr_avg_default));
            tvHeartRate.setText(getString(R.string.hr_not_available_value));
            tvOxygen.setText("");
            tvTime.setText("00:00:00");
            boolean isenableHeart = MainService.getInstance(mContext).isStartHeart();

            mLineGraph.clearGraph();
            gvView.repaint();
        } else {
            tvMax.setText(getString(R.string.hr_max, dataInfo.getMax()));
            tvMin.setText(getString(R.string.hr_min, dataInfo.getMin()));
            tvAvg.setText(getString(R.string.hr_avg, dataInfo.getAvg()));

            int index = dataInfo.getDataList().size();
            int heartRate = dataInfo.getDataList().get(index - 1);
            mLineGraph.addValue(new Point(index, heartRate));
            gvView.repaint();
            tvHeartRate.setText(getString(R.string.hr_heart_rate_value, dataInfo.getDataList().get(dataInfo.getDataList().size() - 1)));
            long dv = (dataInfo.getCurrentTime() - dataInfo.getStartTime()) / 1000;
            int hour = (int) (dv / 3600);
            int min = (int) ((dv / 60) % 60);
            int sec = (int) (dv % 60);
            MainService bleService = MainService.getInstance(mContext);
            if (bleService.isStartHeart()) {
                tvTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec));
            } else {
                tvTime.setText("00:00:00");
            }
        }
        Log.e("WHeartFragment", "HeartDataInfo");
    }

    public void onEventMainThread(OxygenData oxygenData) {
        Log.e("WHeartFragment", "OxygenData");
        tvOxygen.setText(oxygenData.getoxygenRate() + "%");
    }


}
