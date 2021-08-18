package com.isport.tracker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Chronometer;

/**
 * Created by Administrator on 2016/4/24 0024.
 */
public class CustomChronometer extends Chronometer {

    public CustomChronometer(Context context) {
        super(context);
    }

    public CustomChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    int miss=0;

    public static String FormatMiss(int miss){
        String hh=miss/3600>9?miss/3600+"":"0"+miss/3600;
        String  mm=(miss % 3600)/60>9?(miss % 3600)/60+"":"0"+(miss % 3600)/60;
        String ss=(miss % 3600) % 60>9?(miss % 3600) % 60+"":"0"+(miss % 3600) % 60;
        return hh+":"+mm+":"+ss;
    }

}
