package com.isport.tracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.isport.tracker.R;

import java.util.LinkedList;

public class HostoryChartFragment extends BaseFragment implements View.OnClickListener {
	private TextView date;
	//private AreaChart03View chart;
	private LinkedList<String> mLabels;
	private int mState;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_hostory_chart,
				container, false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}


	@Override
	public void onClick(View v) {

	}
}
