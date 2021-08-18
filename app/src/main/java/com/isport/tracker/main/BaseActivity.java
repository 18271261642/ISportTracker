package com.isport.tracker.main;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.util.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            boolean result = fixOrientation();
            //XLog.i(XLog.BASE, "onCreate fixOrientation when Oreo, result = " + result);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int vis = getWindow().getDecorView().getSystemUiVisibility();
            if (BuildConfig.PRODUCT.equals(Constants.PRODUCT_FITNESS_TRACKER_PRO)) {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | vis);
            getWindow().setStatusBarColor(getColor(R.color.statusba_color));
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            // XLog.i(XLog.BASE, "avoid calling setRequestedOrientation when Oreo.");
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }


    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
