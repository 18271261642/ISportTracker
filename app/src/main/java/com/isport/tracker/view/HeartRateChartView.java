package com.isport.tracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.isport.tracker.R;
import com.isport.tracker.util.DeviceConfiger;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author xiongxing
 * @Date 2018/11/15
 * @Fuction
 */

public class HeartRateChartView extends View {

    private static final String        TAG           = HeartRateChartView.class.getSimpleName();
    private              Context       mContext;
    private              int           mMarginTop;
    private              int           mTextMarginBottom;
    private              int           mMarginBottom;
    private              int           mMarginLeft;
    private              int           mMarginRight;
    private              int           mTwoDp;
    private              int           mOneDp;
    private              int           mPathColor;
    private              int           mLabelColor;
    private              int           mDotColor;
    private              Paint         mPathPaint;
    private              Paint         mLabelPaint;
    private              Paint         mdotPaint;
    private              String[]      mYLabels      = new String[25];
    private              String[]      mXLabels      = new String[49];
    private              String[]      mXLabels15Min = new String[97];
    private              String[]      mXLabels5Min  = new String[289];
    private              List<Integer> mDataSerise;
    private              boolean       mIs15MinType;
    private              boolean       mIs5MinType;
    private              int           mFourDp;
    private              Path          mPath;

    public HeartRateChartView(Context context) {
        super(context);
        init(context);
    }

    public HeartRateChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeartRateChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mMarginBottom = DeviceConfiger.dp2px(20);
        mMarginTop = DeviceConfiger.dp2px(10);
        mTextMarginBottom = DeviceConfiger.dp2px(15);
        mMarginLeft = DeviceConfiger.dp2px(30);
        mMarginRight = DeviceConfiger.dp2px(25);
        mFourDp = DeviceConfiger.dp2px(4);
        mTwoDp = DeviceConfiger.dp2px(2);
        mOneDp = DeviceConfiger.dp2px(1);

        mPath = new Path();

        mPathColor = context.getResources().getColor(R.color.percent_color);
        mLabelColor = context.getResources().getColor(R.color.note_long_time_text);
        mDotColor = context.getResources().getColor(R.color.rl_back_topline_color);
        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPathPaint.setColor(mPathColor);
        mPathPaint.setStrokeWidth(mOneDp);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setTextSize(DeviceConfiger.dp2px(13));

        mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLabelPaint.setColor(mLabelColor);
        mLabelPaint.setStrokeWidth(mOneDp);
        mLabelPaint.setTextSize(DeviceConfiger.dp2px(13));

        mdotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mdotPaint.setColor(mDotColor);
        mdotPaint.setStrokeWidth(mOneDp);
        mdotPaint.setTextSize(DeviceConfiger.dp2px(13));

        for (int i = 0; i < mYLabels.length; i++) {
            mYLabels[i] = 10 * (mYLabels.length - 1 - i) + "";
        }

        for (int i = 0; i < mXLabels.length; i++) {
            if (i % 4 == 0) {
                if (i == 0) {
                    mXLabels[i] = "0";
                } else {
                    int i1 = i / 2;
                    mXLabels[i] = i1 < 10 ? "0" + i1 + "" : i1 + "";
                }
            } else {
                mXLabels[i] = i < 10 ? "0" + i + "" : i + "";
            }
        }
    }

    public void setmDataSerise(List dataSerise, boolean is15MinType, boolean is5MinType) {
        if (dataSerise == null)
            return;
        this.mDataSerise = dataSerise;
        this.mIs15MinType = is15MinType;
        this.mIs5MinType = is5MinType;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAsxi(canvas);
        drawPath(canvas);
    }

    /**
     * ????????????0???????????????
     *
     * @param canvas
     */
    private void drawPath(Canvas canvas) {
        if (mDataSerise != null && mDataSerise.size() > 0) {
            String text = "BPM";
            Rect rect = new Rect();
            mLabelPaint.getTextBounds(text, 0, text.length(), rect);
            int textSize = rect.height();//BPM?????????
            //?????????????????????
            float pathHigh = (getHeight() - mMarginBottom - (mMarginTop + mTextMarginBottom + textSize));
            float dw = (getWidth() - mMarginLeft - mTwoDp * 5) / (((mIs5MinType ? mXLabels5Min.length : mIs15MinType ? mXLabels15Min.length : mXLabels.length) - 1) * 1.0f);

            mPath.reset();
            int length = mDataSerise.size();
            float lx = -100;
            float ly = -100;
            List<float[]> dotList = new ArrayList<>();
            //???????????????????????????????????????.?????????????????????
//            if (x == lx && y == ly)
//                continue;
            boolean isFirst = true;
            for (int i = 0; i < length; i++) {
                //??????0?????????????????????
                float x = mMarginLeft + (i + (mIs15MinType ? 0 : 1)) * dw;
                float y = mMarginTop + mTextMarginBottom + textSize + (pathHigh - mDataSerise.get(i) * pathHigh / 240 * 1.0f);
//                if (mDataSerise.get(i)!=0){
                if (isFirst) {
                    isFirst = false;
                    mPath.moveTo(x, y);
                    if (mIs5MinType) {
                        if (mDataSerise.get(i) > 0) {
                            dotList.add(new float[]{x, y});
                        }
                    } else {
                        dotList.add(new float[]{x, y});
                    }
                } else {
                    mPath.lineTo(x, y);
                    if (mIs5MinType) {
                        if (mDataSerise.get(i) > 0) {
                            dotList.add(new float[]{x, y});
                        }
                    } else {
                        dotList.add(new float[]{x, y});
                    }
                }
//                lx = x;
//                ly = y;
            }
            //??????path
            canvas.drawPath(mPath, mPathPaint);
            //???????????????????????????
            for (int i = 0; i < dotList.size(); i++) {
                float[] floats = dotList.get(i);
                canvas.drawCircle(floats[0], floats[1], mTwoDp, mdotPaint);
            }
        }
    }

    private void drawAsxi(Canvas canvas) {
        //???????????????
        Rect rect = new Rect();
        String text = "BPM";
        mLabelPaint.getTextBounds(text, 0, text.length(), rect);
        int textSize = rect.height();
        //??????BPM
        canvas.drawText(text, mMarginLeft - rect.width() / 2, mMarginTop + rect.height() / 2,
                mLabelPaint);
        //??????,???????????????BPM?????????
        canvas.drawLine(mMarginLeft, getHeight() - mMarginBottom, mMarginLeft, mMarginTop + mTextMarginBottom +
                textSize, mLabelPaint);
        //??????
        canvas.drawLine(mMarginLeft, getHeight() - mMarginBottom, getWidth() - mTwoDp * 5, getHeight() -
                mMarginBottom, mLabelPaint);
        //y???????????????,???????????????10?????????20???
        float dh = (getHeight() - mMarginBottom - (mMarginTop + mTextMarginBottom + textSize)) / ((mYLabels
                .length - 1) * 1.0f);
        for (int i = 0; i < mYLabels.length; i++) {
            if (i < mYLabels.length - 4) {
                text = mYLabels[i];
                //?????????????????????
                mLabelPaint.getTextBounds(text, 0, text.length(), rect);
                //??????label??????
                canvas.drawLine(mMarginLeft, mMarginTop + mTextMarginBottom + textSize + i * dh, mMarginLeft +
                        mFourDp, mMarginTop + mTextMarginBottom + textSize + i * dh, mLabelPaint);
                //??????label??????
                if (i % 2 == 0) {
                    canvas.drawText(text, mMarginLeft - mTwoDp * 5 - rect.width(), mMarginTop + mTextMarginBottom +
                                    textSize + i * dh + rect.height() / 2,
                            mLabelPaint);
                }
            }
        }
        float dw = (getWidth() - mMarginLeft - mTwoDp * 5) / ((mXLabels.length - 1) * 1.0f);
        for (int i = 0; i < mXLabels.length; i++) {
            if (i % 4 == 0) {
                text = mXLabels[i];
                mLabelPaint.getTextBounds(text, 0, text.length(), rect);
                //??????label??????
                canvas.drawText(text, mMarginLeft + i * dw - rect.width() / 2, getHeight() - mMarginBottom + mTwoDp *
                        5 + rect.height() /
                        2, mLabelPaint);
            }
            if (!mIs5MinType && !mIs15MinType) {
                if (i % 2 == 0) {
                    //??????label??????
                    canvas.drawLine(mMarginLeft + i * dw, getHeight() - mMarginBottom - mFourDp, mMarginLeft + i * dw,
                            getHeight() - mMarginBottom, mLabelPaint);
                }
            }
        }

        float dw15 = (getWidth() - mMarginLeft - mTwoDp * 5) / ((mXLabels15Min.length - 1) * 1.0f);
        if (mIs15MinType || mIs5MinType) {
            for (int i = 0; i < mXLabels15Min.length; i++) {
                if (i % 2 == 0) {
                    //??????label??????
                    canvas.drawLine(mMarginLeft + i * dw, getHeight() - mMarginBottom - mFourDp, mMarginLeft + i * dw,
                            getHeight() - mMarginBottom, mLabelPaint);
                }
            }
        }
    }
}
