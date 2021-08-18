package com.isport.tracker.view;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.util.Log;

public class MeityitianViewPager extends ViewPager {
	private boolean left = false;
	private boolean right = false;
	private boolean isScrolling = false;
	private int lastValue = -1;
	private ChangeViewCallback changeViewCallback = null;

	public MeityitianViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MeityitianViewPager(Context context) {
		super(context);
		init();
	}

	/**
	 * init method .
	 */
	private void init() {
		setOnPageChangeListener(listener);
	}

	/**
	 * 40 * listener ,to get move direction . 41
	 */
	public OnPageChangeListener listener = new OnPageChangeListener() {
		@Override
		public void onPageScrollStateChanged(int arg0) {
            isScrolling = arg0 == 1;

			if (arg0 == 2) {

				// notify ....
				if (changeViewCallback != null) {
					changeViewCallback.changeView(left, right);
				}
				right = left = false;
			}

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			if (isScrolling) {
				if (lastValue > arg2) {
					// 递减，向右侧滑动
					right = true;
					left = false;
				} else if (lastValue < arg2) {
					// 递减，向右侧滑动
					right = false;
					left = true;
				} else if (lastValue == arg2) {
					right = left = false;
				}
			}
			Log.i("meityitianViewPager",
					"meityitianViewPager onPageScrolled  last :arg2  ,"
							+ lastValue + ":" + arg2);
			lastValue = arg2;
		}

		@Override
		public void onPageSelected(int arg0) {
			if (changeViewCallback != null) {
				changeViewCallback.getCurrentPageIndex(arg0);
			}
		}
	};

	/**
	 * 100 * 得到是否向右侧滑动 101 * @return true 为右滑动 102
	 */
	public boolean getMoveRight() {
		return right;
	}

	/**
	 * 108 * 得到是否向左侧滑动 109 * @return true 为左做滑动 110
	 */
	public boolean getMoveLeft() {
		return left;
	}

	/**
	 * 116 * 滑动状态改变回调 117 * @author zxy 118 * 119
	 */
	public interface ChangeViewCallback {
		/**
		 * 122 * 切换视图 ？决定于left和right 。 123 * @param left 124 * @param right 125
		 */
        void changeView(boolean left, boolean right);

		void getCurrentPageIndex(int index);
	}

	/**
	 * 131 * set ... 132 * @param callback 133
	 */
	public void setChangeViewCallback(ChangeViewCallback callback) {
		changeViewCallback = callback;
	}
}
