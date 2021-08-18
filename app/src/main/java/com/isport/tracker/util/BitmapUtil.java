package com.isport.tracker.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtil {

	public byte[] getBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Bitmap smallBitmap(Bitmap bitmap , float scale) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	public static Bitmap getBitmap(String path) {
		File file = new File(path);
		if(!file.exists())
			return null;
		return BitmapFactory.decodeFile(path);
	}

	public static void save(Bitmap bm, String path) {
		if (bm == null)
			return;
		try {
			File file = new File(path);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists())
				file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			bm.compress(CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap getScalBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		float scale = scaleWidth < scaleHeight ? scaleWidth : scaleHeight;
		matrix.postScale(scale, scale);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	public static String getSDPath() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().getPath();
		}
		return null;
	}
	
	public static Bitmap getUserImage(Context context){
		return BitmapFactory.decodeFile(context.getApplicationContext().getFilesDir().getAbsolutePath()+Constants.SAVEUSERIMAGE);
	}
}