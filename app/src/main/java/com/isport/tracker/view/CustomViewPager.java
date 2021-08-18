package com.isport.tracker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    private boolean isCanScroll = true;

    public CustomViewPager(Context context) {

        super(context);

    }

    public CustomViewPager(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

    public void setScanScroll(boolean isCanScroll) {

        this.isCanScroll = isCanScroll;

    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (isCanScroll) {
            return super.onTouchEvent(arg0);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isCanScroll) {
            return super.onInterceptTouchEvent(arg0);
        }
        return false;
    }

    /*@Override

    public void scrollTo(int x, int y) {

        if (isCanScroll) {

            super.scrollTo(x, y);

        }
    }*/
}
