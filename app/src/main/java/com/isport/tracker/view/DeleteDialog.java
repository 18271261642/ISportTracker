package com.isport.tracker.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.isport.tracker.R;

//自定义Dialog  
public class DeleteDialog extends BaseDlg {
	private TextView tv_delete;
	private OnDialogclickListener listener;

	public DeleteDialog(Context context, OnDialogclickListener listener) {
		super(context);
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_delete);
		initViews();
		initDatas();
		initListeners();
	}

	public void initViews() {
		tv_delete = (TextView) findViewById(R.id.tv_delete);
	}

	public void initDatas() {
	}

	public void initListeners() {
	  if(tv_delete!=null){
		  tv_delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.delete(DeleteDialog.this);
				}
			}) ;
		}
	}

	public interface OnDialogclickListener {
		void delete(DeleteDialog dialog);
	}
}
