package com.isport.tracker.main;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.isport.isportlibrary.entry.HeartData;
import com.isport.tracker.R;
import com.isport.tracker.db.DbHeart;
import com.isport.tracker.entity.HeartHistory;
import com.isport.tracker.hrs.LineGraphView;
import com.isport.tracker.util.TimeUtils;
import com.isport.tracker.view.HeartChartView;

import org.achartengine.GraphicalView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class HeartRateHistoryDetailActivity extends BaseActivity{
	private final String TAG = "HRSActivity";
	private TextView tv_content;
	private TextView tv_avg_bpm;
	private TextView tv_max_bpm;
	private TextView tv_min_bpm;
	private GraphicalView mGraphView;
	private LineGraphView mLineGraph;
	private RelativeLayout return_back;
	private TextView tv_total_time;
    private HeartHistory heartHistory;
	private HeartChartView chartView;
	private TableRow trTotalCal;
	private TextView tvTotalCal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heart_rate_history_detail);
        //heartHistory = (HeartHistory) getIntent().getSerializableExtra("history");
		final String mac = getIntent().getStringExtra("mac");
		final String date = getIntent().getStringExtra("time");
        if(mac == null || date == null || mac.equals("") || date.equals("")){
            finish();
			return;
        }
        initView();
		initListener();
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<HeartHistory> list = DbHeart.getIntance().getListHistory(DbHeart.COLUMN_MAC +"=? and "+DbHeart.COLUMN_DATE+"=?",new String[]{mac, date},null,null,null);
				if(list != null && list.size()>0) {
					heartHistory = list.get(0);
					handler.sendEmptyMessage(0x01);
				}
			}
		}).start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			initData();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(handler.hasMessages(0x01)){
			handler.removeMessages(0x01);
		}
	}

	public void initView(){
		mLineGraph = LineGraphView.getLineGraphView(this);
		mGraphView = mLineGraph.getView(this);
		ViewGroup layout = (ViewGroup) findViewById(R.id.graph_hrs);
		layout.addView(mGraphView);
		chartView = (HeartChartView)findViewById(R.id.heart_chart_view);
		tv_content = (TextView) findViewById(R.id.tv_content);
		return_back = (RelativeLayout) findViewById(R.id.return_back);
		tv_avg_bpm = (TextView) findViewById(R.id.tv_avg_bpm);
		tv_max_bpm = (TextView) findViewById(R.id.tv_max_bpm);
		tv_min_bpm = (TextView) findViewById(R.id.tv_min_bpm);
		tv_total_time = (TextView) findViewById(R.id.tv_total_time);
		trTotalCal = (TableRow) findViewById(R.id.tr_total_cal);
		tvTotalCal = (TextView) findViewById(R.id.tv_total_cal);
	}

	public void initData(){
		trTotalCal.setVisibility(heartHistory == null || heartHistory.getTotalCal() ==0?View.GONE:View.VISIBLE);
		if(heartHistory != null ) {
            String date = heartHistory.getStartDate();
            long time=heartHistory.getIsHistory()==0?heartHistory.getSize():heartHistory.getSize()*5;
            long startLong = TimeUtils.changeStrDateToLongDate(date);
			long endLong = startLong+time;
			String startDateStr = TimeUtils.unixTimeToBeijingTime(startLong);
			String endDateStr = TimeUtils.unixTimeToBeijingTime(endLong);
//            holder.tvDate.setText(UtilTools.getDateFormat().format(UtilTools.string2Date(history.getStartDate(),"yyyy-MM-dd HH:mm:ss"))+" "+date);
			tv_content.setText(startDateStr+" - "+endDateStr);
			tv_avg_bpm.setText(String.valueOf(heartHistory.getAvg()));
			tv_max_bpm.setText(String.valueOf(heartHistory.getMax()));
			tv_min_bpm.setText(String.valueOf(heartHistory.getMin()));
			tvTotalCal.setText((new DecimalFormat("0.00").format(heartHistory.getTotalCal()/1000f)) + getString(R.string.kcal));
			List<Integer> list = new ArrayList<>();
			if (heartHistory.getHeartDataList() != null && heartHistory.getHeartDataList().size() > 0) {
				List<HeartData> listData = heartHistory.getHeartDataList();
				int size = listData.size();
				for (int i = 0; i < size; i++) {
					list.add(listData.get(i).getHeartRate());
				}
			}
			chartView.setmDataSerise(list);
			addGraph(list);
			tv_total_time.setText(longToString(list.size()));
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

    private String longToString(long time){
		if (heartHistory.getIsHistory()==1){
			//是历史查询存储的数据
			time=time*5;
		}
        return String.format("%02d",time/3600)+":"+String.format("%02d",(time%3600)/60)+":"+String.format("%02d",time%60);
    }

	public void initListener(){
		return_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}