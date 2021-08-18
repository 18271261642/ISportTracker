package com.isport.tracker.main;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.isport.tracker.R;
import com.isport.tracker.adapter.HeartRateHistoryAdapter;


public class HeartRateHistoryActivity extends BaseActivity {
	private final String TAG = "HRSActivity";
	private ListView lv_history;
	private RelativeLayout return_back;
	private ImageView iv_delete;
	private HeartRateHistoryAdapter adapter;
	public static boolean isRefresh = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heart_rate_history);
		initView();
		initData();

	}

	public void initView() {
		lv_history = (ListView) findViewById(R.id.lv_hr_history);
		return_back = (RelativeLayout) findViewById(R.id.return_back);
		iv_delete = (ImageView) findViewById(R.id.iv_delete);
	}

	public void initData() {


	}
}