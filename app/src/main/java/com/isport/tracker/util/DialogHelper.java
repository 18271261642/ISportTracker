package com.isport.tracker.util;


import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;


import com.isport.tracker.R;

import java.lang.reflect.Field;

public class DialogHelper {
	
	
	public static ProgressDialog showProgressDialog(Context context, String message) {
		ProgressDialog mpDialog = new ProgressDialog(context);  
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条  
        mpDialog.setMessage(message);  
        return mpDialog;
	}
	
	public static ProgressDialog showSelectDialog(Context context, String message) {
		ProgressDialog mpDialog = new ProgressDialog(context);  
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条  
        mpDialog.setMessage(message);  
        return mpDialog;
	}
	
	
	/**
	 * 只有确定按键
	 * @param context
	 * @param title
	 * @param message
	 * @param listener
	 */
	public static void showAlertDialog(Context context, String title, String message,
			 OnClickListener listener) {
		Builder alertDialog = new Builder(context);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setPositiveButton(R.string.ok ,listener);
		alertDialog.show();
	}
	
//	/**
//	 * 有确定和取消按键
//	 * @param context
//	 * @param title
//	 * @param message
//	 * @param listenerPositive
//	 * @param listenerNegative
//	 */
//	public static void showAlertDialog_2(Context context, String title, String message, OnClickListener listenerPositive, OnClickListener listenerNegative) {
//		Builder alertDialog = new AlertDialog.Builder(context);
//		alertDialog.setTitle(title);
//		alertDialog.setMessage(message);
//		alertDialog.setNegativeButton(R.string.Cancel, listenerNegative);
//		alertDialog.setPositiveButton(R.string.Commit ,listenerPositive);
//		alertDialog.show();
//	}
	
	public static void closeDialogByClickButton(DialogInterface dialog, boolean isClose) {
		try { 
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing"); 
			field.setAccessible(true); 
			field.set(dialog, isClose);
			} catch (Exception e) { 
			e.printStackTrace(); 
			}
	}
	public static void dismissDialog(Dialog dialog) {
		if (dialog != null) {
			dialog.dismiss();
		}
	}
	
	
	public static void hideDialog(Dialog dialog) {
		if (dialog != null) {
			dialog.hide();
		}
	}
}

