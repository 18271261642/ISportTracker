package com.isport.tracker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.isport.tracker.R;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.util.ViewUtil;

/**
 * Created by XieJiaHua on 2016/2/22.
 * Company:PeanutRun Ltd.
 * Email:lylwo317@gmail.com
 */
public class CircularProgressDrawable extends Drawable {

    private int mPadding;
    private int mMinSweepAngle;
    private int mMaxSweepAngle;
    private int mStrokeSize;
    private int mStrokeColor;
    private int mInitialAngle;
    private int mRotateDuration;
    private int mTransformDuration;
    private int mKeepDuration;
    private Interpolator mTransformInterpolator;

    private Paint mPaint;
    private RectF mRect;

    private float mStartAngle;
    private float mSweepAngle;

    private long mLastUpdateTime;
    private long mLastProgressStateTime;
    private ProgressState mProgressState = ProgressState.PROGRESS_HIDE;
    private RunState mRunState = RunState.RUN_STATE_STOPPED;

    enum ProgressState{
        PROGRESS_HIDE,
        PROGRESS_STRETCH,
        PROGRESS_KEEP_STRETCH,
        PROGRESS_SHRINK,
        PROGRESS_KEEP_SHRINK
    }

    enum RunState{
        RUN_STATE_STOPPED,
        RUN_STATE_RUNNING
    }

    private Runnable mUpdate = new Runnable() {
        @Override
        public void run() {
            if (!isRunning()) {
                return;
            }
            long curTime = SystemClock.uptimeMillis();
            float offset = (curTime - mLastUpdateTime) * 360f / mRotateDuration;
            switch (mProgressState) {
                case PROGRESS_HIDE:
                    updateProgressState(ProgressState.PROGRESS_STRETCH);
                    break;
                case PROGRESS_STRETCH:
                    mStartAngle += offset;
                    if (mTransformDuration > 0) {
                        //int sweepOffset = (curTime - mLastProgressStateTime) / mTransformDuration * mMaxSweepAngle;
                        float value = (curTime - mLastProgressStateTime) /
                                (float)mTransformDuration;
                        mSweepAngle = (mTransformInterpolator.getInterpolation(value) * (mMaxSweepAngle - mMinSweepAngle)) + mMinSweepAngle;
                        if (value > 1) {
                            mSweepAngle = mMaxSweepAngle;
                            updateProgressState(ProgressState.PROGRESS_KEEP_STRETCH);
                        }
                    }
                    break;
                case PROGRESS_KEEP_STRETCH:
                    mStartAngle += offset;
                    if (curTime - mLastProgressStateTime > mKeepDuration) {
                        updateProgressState(ProgressState.PROGRESS_SHRINK);
                    }
                    break;
                case PROGRESS_SHRINK:
                    if (mTransformDuration > 0) {
                        float value = (curTime - mLastProgressStateTime) / (float)mTransformDuration;
                        float newSweepAngle = ((1f-mTransformInterpolator.getInterpolation(value)) * (mMaxSweepAngle - mMinSweepAngle)) + mMinSweepAngle;
                        mStartAngle = mStartAngle + offset + (mSweepAngle - newSweepAngle);
                        mSweepAngle = newSweepAngle;
                        if (value > 1) {
                            updateProgressState(ProgressState.PROGRESS_KEEP_SHRINK);
                            mSweepAngle = mMinSweepAngle;
                        }
                    }
                    break;
                case PROGRESS_KEEP_SHRINK:
                    mStartAngle += offset;
                    if (curTime - mLastProgressStateTime > mKeepDuration) {
                        updateProgressState(ProgressState.PROGRESS_STRETCH);
                    }
                    break;
            }
            mLastUpdateTime = curTime;
            scheduleSelf(mUpdate, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
            invalidateSelf();
        }
    };

    private void updateProgressState(ProgressState progressState) {
        mProgressState = progressState;
        mLastProgressStateTime = SystemClock.uptimeMillis();
    }

    public boolean isRunning() {
        return mRunState != RunState.RUN_STATE_STOPPED;
    }

    private CircularProgressDrawable(int padding, int minSweepAngle, int maxSweepAngle, int strokeSize, int strokeColor, int initialAngle, int rotateDuration, int transformDuration, int keepDuration, Interpolator transformInterpolator){
        mPadding = padding;
        mMinSweepAngle = minSweepAngle;
        mMaxSweepAngle = maxSweepAngle;
        mStrokeSize = strokeSize;
        mStrokeColor = strokeColor;
        mInitialAngle = initialAngle;
        mRotateDuration = rotateDuration;
        mTransformDuration = transformDuration;
        mKeepDuration = keepDuration;
        mTransformInterpolator = transformInterpolator;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mRect = new RectF();
    }

    public void start() {
        mStartAngle = mInitialAngle;
        mLastUpdateTime = SystemClock.uptimeMillis();
        mSweepAngle = mMinSweepAngle;
        //mProgressState = ProgressState.PROGRESS_ROTATE;
        mRunState = RunState.RUN_STATE_RUNNING;
        scheduleSelf(mUpdate, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
        invalidateSelf();
    }

    public void stop(){
        mRunState = RunState.RUN_STATE_STOPPED;
        unscheduleSelf(mUpdate);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        drawIndeterminate(canvas);
    }

    private void drawIndeterminate(Canvas canvas) {
        if (mProgressState != ProgressState.PROGRESS_HIDE) {
            Rect bounds = getBounds();
            float radius = (Math.min(bounds.width(), bounds.height())-mPadding*2-mStrokeSize)/2f;
            float x = (bounds.left + bounds.right) / 2f;
            float y = (bounds.top + bounds.bottom) / 2f;
            mRect.set(x - radius, y - radius, x + radius, y + radius);
            mPaint.setStrokeWidth(mStrokeSize);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mStrokeColor);
            canvas.drawArc(mRect, mStartAngle, mSweepAngle, false, mPaint);
        }

    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public static class Build{
        private int mPadding;
        private int mMinSweepAngle;
        private int mMaxSweepAngle;
        private int mStrokeSize;
        private int mStrokeColor;
        private int mInitialAngle;
        private int mRotateDuration;
        private int mTransformDuration;
        private int mKeepDuration;
        private Interpolator mTransformInterpolator;

        public Build() {
        }

        public Build(Context context, int defStyleRes) {
            this(context, null, 0, defStyleRes);
        }

        public Build(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressDrawable, defStyleAttr, defStyleRes);
            int resId;
            setPadding(a.getDimensionPixelSize(R.styleable.CircularProgressDrawable_cpd_padding, 0));
            setMinSweepAngle(a.getInteger(R.styleable.CircularProgressDrawable_cpd_minSweepAngle, 1));
            setMaxSweepAngle(a.getInteger(R.styleable.CircularProgressDrawable_cpd_maxSweepAngle, 270));
            setInitialAngle(a.getInteger(R.styleable.CircularProgressDrawable_cpd_initialAngle, 0));
            setKeepDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_keepDuration, context.getResources().getInteger(android.R.integer.config_shortAnimTime)));
            setRotateDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_rotateDuration, context.getResources().getInteger(android.R.integer.config_longAnimTime)));
            setTransformDuration(a.getInteger(R.styleable.CircularProgressDrawable_cpd_transformDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime)));
            if ((resId = a.getResourceId(R.styleable.CircularProgressDrawable_cpd_transformInterpolator, 0)) != 0) {
                setTransformInterpolator(AnimationUtils.loadInterpolator(context, resId));
            }
            setStrokeSize(a.getDimensionPixelSize(R.styleable.CircularProgressDrawable_cpd_strokeSize, DeviceConfiger.dp2px(8)));
            setStrokeColor(a.getColor(R.styleable.CircularProgressDrawable_cpd_strokeColor, context.getResources().getColor(R.color.red)));
            a.recycle();
        }

        public CircularProgressDrawable build() {
            if (mTransformInterpolator == null) {
                mTransformInterpolator = new DecelerateInterpolator();
            }

            return new CircularProgressDrawable(mPadding,
                    mMinSweepAngle,
                    mMaxSweepAngle,
                    mStrokeSize,
                    mStrokeColor,
                    mInitialAngle,
                    mRotateDuration,
                    mTransformDuration,
                    mKeepDuration,mTransformInterpolator);
        }

        public Build setMaxSweepAngle(int mMaxSweepAngle) {
            this.mMaxSweepAngle = mMaxSweepAngle;
            return this;
        }

        public Build setPadding(int mPadding) {
            this.mPadding = mPadding;
            return this;
        }

        public Build setMinSweepAngle(int mMinSweepAngle) {
            this.mMinSweepAngle = mMinSweepAngle;
            return this;
        }

        public Build setStrokeSize(int mStrokeSize) {
            this.mStrokeSize = mStrokeSize;
            return this;
        }

        public Build setStrokeColor(int mStrokeColor) {
            this.mStrokeColor = mStrokeColor;
            return this;
        }

        public Build setInitialAngle(int mInitialAngle) {
            this.mInitialAngle = mInitialAngle;
            return this;
        }

        public Build setRotateDuration(int mRotateDuration) {
            this.mRotateDuration = mRotateDuration;
            return this;
        }

        public Build setTransformDuration(int mTransformDuration) {
            this.mTransformDuration = mTransformDuration;
            return this;
        }

        public Build setKeepDuration(int mKeepDuration) {
            this.mKeepDuration = mKeepDuration;
            return this;
        }

        public Build setTransformInterpolator(Interpolator mTransformInterpolator) {
            this.mTransformInterpolator = mTransformInterpolator;
            return this;
        }
    }

}
