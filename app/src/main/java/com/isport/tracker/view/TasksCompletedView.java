package com.isport.tracker.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.isport.tracker.R;

public class TasksCompletedView extends View {
	// 画实心圆的画笔
	private Paint mCirclePaint2;
	// 画内圆的画笔
	private Paint mCirclePaint;
	// 画圆环的画笔
	private Paint mRingPaint;
	// 画圆环背景的画笔
	private Paint mRingPaint2;
	// 圆形颜色
	// private int mCircleColor;
	// 圆环颜色
	private int mRingColor;
	// 半径
	private float mRadius;
	// 圆环半径
	private float mRingRadius;
	// 圆环宽度
	private float mStrokeWidth;
	// 圆心x坐标
	private int mXCenter;
	// 圆心y坐标
	private int mYCenter;
	// 总进度
	private int mTotalProgress = 100;
	// 当前进度
	private int mProgress;
	private int mAllProgress;
	public static long TIME = 1;
	private int i;

	public TasksCompletedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 获取自定义的属性
		initAttrs(context, attrs);
		initVariable();
	}

	private void initAttrs(Context context, AttributeSet attrs) {
		TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.TasksCompletedView, 0, 0);
		mRadius = typeArray.getDimension(R.styleable.TasksCompletedView_radius,
				80);
		mStrokeWidth = typeArray.getDimension(
				R.styleable.TasksCompletedView_strokeWidth, 10);
//		 mCircleColor =
//		 typeArray.getColor(R.styleable.TasksCompletedView_circleColor,
//		 0xFFFFFF);
		mRingColor = typeArray.getColor(
				R.styleable.TasksCompletedView_ringColor, 0xFFFFFF);
		mRingRadius = mRadius + mStrokeWidth / 2;
	}

	private void initVariable() {
		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		// mCirclePaint.setColor(mCircleColor);
//		    <color name="ride_6a">#6AC328</color>
		mCirclePaint.setColor(mRingColor);
		mCirclePaint.setStyle(Paint.Style.FILL);// STROKE为空心，fill为实心
		mCirclePaint.setStrokeWidth(0);

		mCirclePaint2 = new Paint();
		mCirclePaint2.setAntiAlias(true);
		mCirclePaint2.setColor(0xffffffff);
		mCirclePaint2.setStyle(Paint.Style.FILL);// STROKE为空心，fill为实心
		mCirclePaint2.setStrokeWidth(0);

		mRingPaint = new Paint();
		mRingPaint.setAntiAlias(true);
		mRingPaint.setColor(mRingColor);
		// LinearGradient mShader = new LinearGradient(50, 50, 100, 150, new
		// int[] { Color.GRAY,Color.RED,Color.GRAY, Color.BLUE,Color.YELLOW
		// },null, Shader.TileMode.MIRROR);
		// mRingPaint.setShader(mShader);
		mRingPaint.setStyle(Paint.Style.STROKE);
		mRingPaint.setStrokeWidth(mStrokeWidth);

		mRingPaint2 = new Paint();
		mRingPaint2.setAntiAlias(true);
		mRingPaint2.setColor(0xffeaeaea);//底色
		mRingPaint2.setStyle(Paint.Style.STROKE);
		mRingPaint2.setStrokeWidth(mStrokeWidth);

	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		float a = ((float) mProgress / mTotalProgress) * 360;
		if (a >= 360) {
			a = 360;
		}
		mXCenter = getWidth() / 2;
		mYCenter = getHeight() / 2;
		if (mProgress >= 0) {
			RectF oval = new RectF();
			oval.left = (mXCenter - mRingRadius);
			oval.top = (mYCenter - mRingRadius);
			oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
			oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
			// canvas.drawCircle(mXCenter, mYCenter, mRingRadius,mRingPaint2);
			int n = 360;
			for (int i = 0; i < n; i += 3) {
				canvas.drawArc(oval, -90 + i, 2, false, mRingPaint2);
			}
			float m = (1.0f * mProgress / mTotalProgress) * 360;
			for (int i = 0; i < m; i += 3) {
				canvas.drawArc(oval, -90 + i, 2, false, mRingPaint);
			}
		}
	}

	private ValueAnimator mAnimator;
	public void setProgress(int progress) {
		if(progress != mAllProgress) {
			stopAnimation();
			mAllProgress = progress;
			mAnimator = ValueAnimator.ofInt(mProgress, progress > 100 ? 100 : progress);
			mAnimator.setInterpolator(new LinearInterpolator());
			mAnimator.setRepeatCount(0);
			mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mProgress = (int) animation.getAnimatedValue();
					invalidate();
				}
			});
			mAnimator.setDuration(1500);
			mAnimator.start();
		}
		
		//handler.postDelayed(runnable, TIME);
	}

	public boolean isAnimatorStarted() {
		if(mAnimator != null && mAnimator.isStarted()){
			return true;
		}
		return false;
	}

	public void stopAnimation(){
		if(isAnimatorStarted()){
			mAnimator.cancel();
		}
	}

	public int getProgress(){
		return this.mAllProgress;
	}
}