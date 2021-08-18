package com.isport.tracker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class EasySwitchButton extends View{

	public interface OnOpenedListener {
		void onChecked(View v, boolean isOpened);
	}

	private String NAMESPACE = "http://schemas.android.com/apk/res-auto";
	private String ATTR_IS_OPENED = "isOpened";
	
	Bitmap mSwitchBackOn;
	Bitmap mSwitchBackOff;
	Bitmap mCurrSwitch;
	boolean isOpened = false;
	private OnOpenedListener onOpenedListener;
	
	public EasySwitchButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(attrs);
	}

	public EasySwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(attrs);
	}

	public EasySwitchButton(Context context) {
		super(context);
		initView(null);
	}
	
	private void initView(AttributeSet attrs) {
		if (attrs != null){
			int  switchBackOnId= attrs.getAttributeResourceValue(NAMESPACE, "switch_on", -1);
			mSwitchBackOn = BitmapFactory.decodeResource(getResources(), switchBackOnId);
			int  switchBackOffId= attrs.getAttributeResourceValue(NAMESPACE, "switch_off", -1);
			mSwitchBackOff = BitmapFactory.decodeResource(getResources(), switchBackOffId);
			if (mSwitchBackOn == null || mSwitchBackOff == null){
				throw new NullPointerException();
			}
		} 
		mCurrSwitch = mSwitchBackOff;
		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setStatus(!isOpened);
				if (onOpenedListener != null){
					onOpenedListener.onChecked(EasySwitchButton.this, isOpened);
				}
			}
		});
		initStatus(attrs);
	}

	public boolean getStatus(){
		return this.isOpened;
	}

	private void initStatus(AttributeSet attrs) {
		if (attrs != null){
			boolean isInitOpened = attrs.getAttributeBooleanValue(NAMESPACE, ATTR_IS_OPENED, false);
			setStatus(isInitOpened);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mSwitchBackOn.getWidth(), mSwitchBackOn.getHeight());
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mCurrSwitch, 0, 0, null);
	}
	
	public void setStatus(boolean flag){
		if (flag){
			mCurrSwitch = mSwitchBackOn;
			isOpened = true;
		} else {
			mCurrSwitch = mSwitchBackOff;
			isOpened = false;
		}
		invalidate();
	}

	public void setOnCheckChangedListener(OnOpenedListener onOpenedListener) {
		this.onOpenedListener = onOpenedListener;
	}
}
