package com.isport.tracker.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.isport.tracker.entity.Bar;

import java.util.ArrayList;

@SuppressLint("DrawAllocation")
public class SleepBarGraph extends View {

	private final static int VALUE_FONT_SIZE = 13, AXIS_LABEL_FONT_SIZE = 12;
	private ArrayList<Bar> mBars = new ArrayList<Bar>();
	private Paint mPaint = new Paint();
	private Rect mRectangle = null;
	private boolean mShowBarText = true;
	private boolean mShowAxis = true;
	private int mIndexSelected = -1;
	private OnBarClickedListener mListener;
	private Bitmap mFullImage;
	private boolean mShouldUpdate = false;
	private Boolean isDrawLine = true;
	private Context mContext = null;

	public SleepBarGraph(Context context) {
		super(context);
		mContext = context;
	}

	public SleepBarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void setShowBarText(boolean show) {
		mShowBarText = show;
	}

	public void setShowAxis(boolean show) {
		mShowAxis = show;
	}

	public void setBars(ArrayList<Bar> points) {
		this.mBars = points;
		mShouldUpdate = true;
		postInvalidate();
	}

	/*
	 * 画线函数
	 */
	public Boolean isLine(float lable, Boolean bool) {
		isDrawLine = bool;
		return isDrawLine;
	}

	public void setLine(float lable) {

	}

	/*
	 * popup_black
	 */
	public ArrayList<Bar> getBars() {
		return this.mBars;
	}

	public void onDraw(Canvas ca) {

		if (mFullImage == null || mShouldUpdate) {
			mFullImage = Bitmap.createBitmap(getWidth(), getHeight(),Config.ARGB_8888);
			Canvas canvas = new Canvas(mFullImage);
			canvas.drawColor(Color.TRANSPARENT);
			float maxValue = 3000;
			float padding = 0 * mContext.getResources().getDisplayMetrics().density;
			int selectPadding = (int) (1 * mContext.getResources().getDisplayMetrics().density);
			float bottomPadding = 30 * mContext.getResources().getDisplayMetrics().density;
			float leftPadding = 20 * mContext.getResources().getDisplayMetrics().density;
			float usableHeight;
			if (mShowBarText) {
				this.mPaint.setTextSize(VALUE_FONT_SIZE* mContext.getResources().getDisplayMetrics().scaledDensity);
				Rect r3 = new Rect();
				this.mPaint.getTextBounds("$", 0, 1, r3);
				usableHeight = getHeight() - bottomPadding- Math.abs(r3.top - r3.bottom) - 24* mContext.getResources().getDisplayMetrics().density;
			} else {
				usableHeight = getHeight() - bottomPadding;
			}
			if (mShowAxis) {//x轴
				mPaint.setColor(0xffe4e3e1);
				// 设置空心线宽
				mPaint.setStrokeWidth(1 * mContext.getResources().getDisplayMetrics().density);
				// 设置透明度
				mPaint.setAlpha(100);
				mPaint.setAntiAlias(true);
				canvas.drawLine(leftPadding,getHeight()- bottomPadding, getWidth() - 1 * leftPadding,
						getHeight()- bottomPadding,mPaint);
			}
			float barWidth = (float) ((getWidth() - 3.0 * leftPadding - (padding * 2)* mBars.size()) / mBars.size());

			mRectangle = new Rect();
			int count = 0;
			for (final Bar bar : mBars) {
				// Set bar bounds
				int left = (int) ((padding * 2) * count + padding + barWidth* count + leftPadding + 10 * mContext.getResources().getDisplayMetrics().density);
				int top = (int) (getHeight() - bottomPadding - (usableHeight * ((bar.getValue()) / maxValue)));
				int right = (int) ((padding * 2) * count + padding + barWidth* (count + 1) + leftPadding + 10 * mContext.getResources().getDisplayMetrics().density);
				int bottom = (int) (getHeight() - bottomPadding + 1* mContext.getResources().getDisplayMetrics().density);
				mRectangle.set(left, top, right, bottom);

				// Draw bar
				this.mPaint.setColor(bar.getColor());
				this.mPaint.setAlpha(255);
				if(bar.getValue()!=0)
				canvas.drawRect(mRectangle, this.mPaint);

				// Create selection region
				Path path = new Path();
				path.addRect(new RectF(mRectangle.left - selectPadding,mRectangle.top - selectPadding, mRectangle.right + selectPadding, mRectangle.bottom + selectPadding), Path.Direction.CW);
				bar.setPath(path);
				bar.setRegion(new Region(mRectangle.left - selectPadding,mRectangle.top - selectPadding, mRectangle.right + selectPadding, mRectangle.bottom + selectPadding));

				// Draw x-axis label text
				if (mShowAxis) {
					mPaint.setColor(Color.rgb(0, 0, 0));
					// 设置空心线宽
					mPaint.setStrokeWidth(1 * mContext.getResources().getDisplayMetrics().density);
					// 设置透明度
					mPaint.setAlpha(100);
					mPaint.setAntiAlias(true);
					mPaint.setTextSize((float)1.2*AXIS_LABEL_FONT_SIZE* mContext.getResources().getDisplayMetrics().scaledDensity);
					int x = (int) (((mRectangle.left + mRectangle.right) / 2) - (this.mPaint.measureText(bar.getName()) / 2));
					int y = (int) (getHeight() - 10 * mContext.getResources().getDisplayMetrics().scaledDensity);
					canvas.drawText(bar.getName(), x, y, mPaint);
				}

				// Draw value text
				if (mShowBarText) {
					this.mPaint.setTextSize(VALUE_FONT_SIZE* mContext.getResources().getDisplayMetrics().scaledDensity);
					// px值改为SP值
					this.mPaint.setColor(0xff185e79);
					Rect r2 = new Rect();
					this.mPaint.getTextBounds(bar.getValueString(), 0, 1, r2);
					int boundTop = (int) (mRectangle.top + (r2.top - r2.bottom) - 30 * mContext.getResources().getDisplayMetrics().density);
					Boolean is = bar.isSetvaluetext();
					if (is) {
						this.mPaint.setTextSize(60);
						canvas.drawText(bar.getValueString(),(int) (((mRectangle.left + mRectangle.right) / 2) - (this.mPaint.measureText(bar.getValueString())) / 2),
								mRectangle.top - (mRectangle.top - boundTop)/ 2f+ (float) Math.abs(r2.top - r2.bottom)/ 2f * 0.7f, this.mPaint);
					}
				}
				if (mIndexSelected == count && mListener != null) {
					this.mPaint.setColor(0xff33B5E5);
					this.mPaint.setAlpha(100);
					canvas.drawPath(bar.getPath(), this.mPaint);
					this.mPaint.setAlpha(255);
				}
				count++;
			}
			mShouldUpdate = false;
		}
		ca.drawBitmap(mFullImage, 0, 0, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Point point = new Point();
		try {
			point.x = (int) event.getX();
			point.y = (int) event.getY();
		}catch (IllegalArgumentException e){
			e.printStackTrace();
		}

		int count = 0;
		for (Bar bar : mBars) {
			Region r = new Region();
			r.setPath(bar.getPath(), bar.getRegion());
			if (r.contains(point.x, point.y)
					&& event.getAction() == MotionEvent.ACTION_DOWN) {
				mIndexSelected = count;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				if (r.contains(point.x, point.y)
						&& mListener != null) {
					if (mIndexSelected > -1)
						mListener.onClick(mIndexSelected);
					mIndexSelected = -1;
				}
			} else if (event.getAction() == MotionEvent.ACTION_CANCEL)
				mIndexSelected = -1;

			count++;
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN|| event.getAction() == MotionEvent.ACTION_UP|| event.getAction() == MotionEvent.ACTION_CANCEL) {
			mShouldUpdate = true;
			postInvalidate();
		}

		return true;
	}

	public void setTruemShouldUpdate() {
		mShouldUpdate = true;
		postInvalidate();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mFullImage != null)
			mFullImage.recycle();

		super.onDetachedFromWindow();
	}

	public void setOnBarClickedListener(OnBarClickedListener listener) {
		this.mListener = listener;
	}

	public interface OnBarClickedListener {
		void onClick(int index);
	}
}