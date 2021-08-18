package com.isport.tracker.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.isport.tracker.R;

//自定义Dialog
public class ConfirmDialog extends BaseDlg {
	private Window window = null;
	private TextView btn_left, btn_right;
	private OnDialogclickListener listener;

	public ConfirmDialog(Context context, OnDialogclickListener listener) {
		super(context);
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_comfirm);
		initViews();
		initDatas();
		initListeners();
	}

	public void initViews() {
		btn_left = (TextView) findViewById(R.id.btn_left);
		btn_right = (TextView) findViewById(R.id.btn_right);
	}

	public void initDatas() {
	}

	public void initListeners() {
	  if(btn_left!=null){
		  btn_left.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					listener.cancel(ConfirmDialog.this);
				}
			}) ;
		}
		if(btn_right!=null){
			btn_right.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					listener.confirm(ConfirmDialog.this);
				}
			}) ;
		}
	}

	public interface OnDialogclickListener {
		void cancel(ConfirmDialog dialog);
		void confirm(ConfirmDialog dialog);
	}
}
