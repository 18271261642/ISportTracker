/**
 * 
 */
package com.isport.tracker.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.isport.tracker.R;

public class MyDialog extends ProgressDialog {

	protected boolean isCanCancel;

	private TextView tvMsg;
	private String msg;
	private ProgressBar progressBar;

	public MyDialog(Context context, String msg) {
		super(context);
		this.isCanCancel = true;
		this.msg = msg;
	}

	public MyDialog(Context context, boolean isCanCancel) {
		super(context);

		this.isCanCancel = isCanCancel;
	}
	
	public MyDialog(Context context, String msg, boolean isCanCancel) {
		super(context);
		this.msg = msg;
		this.isCanCancel = isCanCancel;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout tmp = (LinearLayout) inflater.inflate(
				R.layout.dialog_layout, null);

		tvMsg = (TextView) tmp.findViewById(R.id.tv_msga);
		tvMsg.setText(this.msg);
		
		progressBar = (ProgressBar) tmp.findViewById(R.id.progressBar_progress);

		this.setContentView(tmp);

		this.setCanceledOnTouchOutside(isCanCancel);
		
		this.setCancelable(true);
		
		this.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				progressBar.setVisibility(View.GONE);
			}
		});
	}

	public void setMsg(String msg) {
		if(this.tvMsg!=null)
			this.tvMsg.setText(msg);
	}

	public void setTips(String msg) {
		if (this.tvMsg != null)
			this.tvMsg.setText(msg);
	}
	
	//���ý������ٷֱ�
	public void setProgress(int progress){
		this.progressBar.setVisibility(View.VISIBLE);
		this.progressBar.setProgress(progress);
	}

//	private boolean canceled;
//
//	public void cancel() {
//		if (this.isCanCancel) {
//			super.cancel();
//			canceled = true;
//		}
//	}

	public void setCancelable(boolean flag) {
		super.setCancelable(flag);
//		canceled = false;
	}

//	public boolean isCanceled() {
//
//		return canceled;
//	}

	public void dismiss() {
		try {
			this.progressBar.setVisibility(View.GONE);
			super.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
