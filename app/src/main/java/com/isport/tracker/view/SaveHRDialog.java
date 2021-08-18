package com.isport.tracker.view;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.isport.tracker.R;

//自定义Dialog  
public class SaveHRDialog extends BaseDlg {
	private Window window = null;
	private TextView tv_hr_discard, tv_hr_save, tvCancel;
	private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
	private OnDialogclickListener listener;

	public SaveHRDialog(Context context, OnDialogclickListener listener) {
		super(context);
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.save_hr_dialog);
		initViews();
		initDatas();
		initListeners();
	}

	public void initViews() {
		tv_hr_discard = (TextView) findViewById(R.id.tv_hr_discard);
		tv_hr_save = (TextView) findViewById(R.id.tv_hr_save);
		tvCancel = (TextView) findViewById(R.id.cancel);
	}

	public void initDatas() {
	}

	public void initListeners() {
	  if(tv_hr_discard!=null){
		  tv_hr_discard.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.disCard(SaveHRDialog.this);
				}
			}) ;
		}
		if(tv_hr_save!=null){
			tv_hr_save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.save(SaveHRDialog.this);
				}
			}) ;
		}
		if(tvCancel!=null){
			tvCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.cancel(SaveHRDialog.this);
				}
			}) ;
		}
	}

	public interface OnDialogclickListener {
		void disCard(SaveHRDialog dialog);
		void cancel(SaveHRDialog dialog);
		void save(SaveHRDialog dialog);
	}

	// 设置窗口显示
	public void windowDeploy(int x, int y) {
		window = getWindow(); // 得到对话框
		window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
		window.setGravity(Gravity.BOTTOM);
		WindowManager.LayoutParams wl = window.getAttributes();
		// 根据x，y坐标设置窗口需要显示的位置
		wl.x = x; // x小于0左移，大于0右移
		wl.y = y; // y小于0上移，大于0下移
		// wl.alpha = 0.6f; //设置透明度
		// wl.gravity = Gravity.BOTTOM; //设置重力
		window.setAttributes(wl);
	}

	public void show(int x, int y) {
		windowDeploy(x, y);
		// 设置触摸对话框以外的地方取消对话框
		setCanceledOnTouchOutside(true);
		show();
	}

	public void setDialog(){

	}
}
