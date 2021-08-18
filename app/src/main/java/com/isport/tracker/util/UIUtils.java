package com.isport.tracker.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.isport.tracker.MyApp;


public class UIUtils {

    public static Context getContext() {
        return MyApp.getInstance();
    }

    public static View inflate(int resId) {
        return LayoutInflater.from(getContext()).inflate(resId, null);
    }

    public static Resources getResources() {
        return getContext().getResources();
    }

    public static String getString(int resId) {
        return getResources().getString(resId);
    }

    public static Integer getIntegers(int resId) {
        return getResources().getInteger(resId);
    }

    public static Drawable getDrawable(int resId) {
        return getResources().getDrawable(resId);
    }

    public static int getColor(int resId) {
        return getResources().getColor(resId);
    }

    /**
     * 字数限制
     * @param editText
     * @param i
     */
    public static void setLengthFilter(EditText editText, int i) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(i)});
    }
}


