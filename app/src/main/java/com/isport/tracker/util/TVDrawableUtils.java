package com.isport.tracker.util;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.NonNull;


/**
 * Created by Administrator on 2016/10/22 0022.
 * //    public void setCompoundDrawablesWithIntrinsicBounds (Drawable left, Drawable top, Drawable right, Drawable bottom)
 */

public class TVDrawableUtils {

    public static void drawLeft(TextView textView, int d) {
        textView.setCompoundDrawables(getDrawable(d), null, null, null);
    }

    public static void drawTop(TextView textView, int d) {
        textView.setCompoundDrawables(null, getDrawable(d), null, null);
    }

    public static void drawRight(TextView textView, int d) {
        textView.setCompoundDrawables(null, null, getDrawable(d), null);
    }

    public static void drawBottom(TextView textView, int d) {
        textView.setCompoundDrawables(null, null, null, getDrawable(d));
    }

    @NonNull
    private static Drawable getDrawable(int d) {
        Drawable drawable = UIUtils.getContext().getResources().getDrawable(d);
        // 这一步必须要做,否则不会显示.
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        return drawable;
    }
}
