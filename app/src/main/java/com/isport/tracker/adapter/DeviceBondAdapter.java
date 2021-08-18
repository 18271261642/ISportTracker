package com.isport.tracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.isport.tracker.R;
import com.isport.tracker.entity.MyBaseDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceBondAdapter extends BaseAdapter {
	private List<MyBaseDevice> lists;
	private Context context;

	public DeviceBondAdapter(ArrayList<MyBaseDevice> lists, Context context) {
		if (lists != null)
			this.lists = lists;
		else
			this.lists = new ArrayList<MyBaseDevice>();
		this.context = context;
	}

	@Override
	public int getCount() {
		return lists.size();
	}

	public void notifymDataSetChanged(ArrayList<MyBaseDevice> lists) {
		if (lists != null) {
			this.lists = lists;
			notifyDataSetChanged();
		} else {
			this.lists = new ArrayList<MyBaseDevice>();
			notifyDataSetChanged();
		}
	}

	@Override
	public Object getItem(int position) {
		return lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.view_settings_device_item, null);
		TextView content = (TextView) convertView.findViewById(R.id.deviceName);
		MyBaseDevice device = lists.get(position);

		return convertView;
	}
}
