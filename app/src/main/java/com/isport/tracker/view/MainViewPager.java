package com.isport.tracker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * Created by Administrator on 2016/10/13.
 */

public class MainViewPager extends ViewPager {

    public MainViewPager(Context context) {
        super(context);
    }

    private boolean mIsScrolled = false;////是否可以滑动

    public MainViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

    }


    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        try {
            if (mIsScrolled) {
                return super.onTouchEvent(arg0);
            }
            return false;
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        try {
            if(mIsScrolled){
                return super.onInterceptTouchEvent(arg0);
            }
            return false;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }
    }


    public void setIsScrolled(boolean isScrolled){
        this.mIsScrolled = isScrolled;
    }
}
