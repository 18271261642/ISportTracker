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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.isport.isportlibrary.database.DbHeartRateHistory;
import com.isport.isportlibrary.entry.HeartRateData;
import com.isport.isportlibrary.entry.HeartRateHistory;
import com.isport.isportlibrary.tools.DateUtil;
import com.isport.tracker.R;
import com.isport.tracker.main.HeartHistoryActivity;
import com.isport.tracker.view.HeartRateChartView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;

/**
 * @Author xiongxing
 * @Date 2018/11/15
 * @Fuction
 */

public class HeartRateHistoryFragment extends BaseFragment {

    private static final String TAG = HeartRateHistoryFragment.class.getSimpleName();
    private Context mContext;
    private int position;
    private int count;
    private View view;
    TextView tv_date;
    ImageButton ivb_more;
    TextView avgValue;
    TextView maxValue;
    TextView minValue;
    private HeartRateChartView chartView;
    private String currentMac;
    private String mCurrentDateStr;
    private List<HeartRateHistory> mListHistory;
    private boolean is15MinType;
    private boolean is5MinType;

    public static HeartRateHistoryFragment newInstance(int position, int count, String currentMac, boolean is15MinType, boolean is5MinType) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("count", count);
        args.putString("currentMac", currentMac);
        args.putBoolean("is15MinType", is15MinType);
        args.putBoolean("is5MinType", is5MinType);
        HeartRateHistoryFragment fragment = new HeartRateHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        position = getArguments().getInt("position");
        count = getArguments().getInt("count");
        currentMac = getArguments().getString("currentMac");
        is15MinType = getArguments().getBoolean("is15MinType");
        is5MinType = getArguments().getBoolean("is5MinType");
        reloadData();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_heartrate_day, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        avgValue = view.findViewById(R.id.tv_avg_value);
        maxValue = view.findViewById(R.id.tv_max_value);
        minValue = view.findViewById(R.id.tv_min_value);
        tv_date = view.findViewById(R.id.tv_date);
        ivb_more = view.findViewById(R.id.ivb_more);
        chartView = view.findViewById(R.id.heart_chart_view);
        chartView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        ivb_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, HeartHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 请求设备的数据，同步的逻辑
     */
    private void reloadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //查询有数据的位置，展示在日历上，暂放
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1 * (count - position) + 1);
                mCurrentDateStr = DateUtil.dataToString(calendar.getTime(), "yyyy-MM-dd");
                Log.e(TAG, " mCurrentDateStr == " + mCurrentDateStr);
                String sql = DbHeartRateHistory.COLUMN_DATE + "=? and " + DbHeartRateHistory.COLUMN_MAC + "=?";
                mListHistory = DbHeartRateHistory.getIntance(mContext).findAll(sql, new
                        String[]{mCurrentDateStr, currentMac}, null);
                handler.sendEmptyMessage(0x01);
            }
        }).start();

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    tv_date.setText(mCurrentDateStr);
                    if (mListHistory != null && mListHistory.size() > 0) {
                        HeartRateHistory heartRateHistory = mListHistory.get(0);
                        avgValue.setText(heartRateHistory.getAvg() + "");
                        maxValue.setText(heartRateHistory.getMax() + "");
                        minValue.setText(heartRateHistory.getMin() + "");
                        ArrayList<HeartRateData> heartDataList = heartRateHistory.getHeartDataList();
                        List<Integer> list = new ArrayList<>();
                        for (int i = 0; i < heartDataList.size(); i++) {
                            list.add(heartDataList.get(i).getHeartRate());
                        }
                        if (chartView != null) {
                            Log.e(TAG, " mCurrentDateStr == 去绘制");
                            chartView.setmDataSerise(list, is15MinType, is5MinType);
                        }
//                        List<Integer> list = new ArrayList<>();
//                        Random random1 = new Random();
//                        for (int i = 0; i < 48; i++) {
//                            if (i<=8){
//                                list.add(0);
//                            }else{
//                                list.add((random1.nextInt(200) + 40));
//                            }
//                        }
//                        if (chartView != null) {
//                            Log.e(TAG, " mCurrentDateStr == 去绘制"+list.toString() );
//                            chartView.setmDataSerise(list);
//                        }
                    } else {
                        Log.e(TAG, " mCurrentDateStr == 没有数据，不用绘制");
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
