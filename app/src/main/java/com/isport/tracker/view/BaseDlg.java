package com.isport.tracker.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.isport.tracker.R;

public  class BaseDlg extends Dialog {

	private Window window;
	private Context context;
	private String TAG="MessageDlg";

	public BaseDlg(Context context) {
		super(context);
		this.context=context;
		
	}

 	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);//设置dialog圆角效果
		window = getWindow(); // 得到对话框
		window.setBackgroundDrawableResource(R.color.lucency); // 设置对话框背景为透明
		WindowManager.LayoutParams wl = window.getAttributes();
	    DisplayMetrics dm = new DisplayMetrics();  
	    dm =context.getResources().getDisplayMetrics();
	    int densityDPI=dm.densityDpi;
	    if(densityDPI==120||densityDPI==160)
		    wl.width = dm.widthPixels - 150;
	    else if (densityDPI==240 || densityDPI==320)
	     	wl.width = dm.widthPixels - 300; // 设置宽度
	    else 
	    	wl.width = dm.widthPixels - 450;
	    window.setAttributes(wl);
        /*
         * 将对话框的大小按屏幕大小的百分比设置
         */
//        WindowManager m = getWindowManager();
//        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
//        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
//        p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.95
//        window.setAttributes(p);
	}
}
