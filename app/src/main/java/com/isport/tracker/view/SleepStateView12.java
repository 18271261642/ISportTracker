package com.isport.tracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.isport.tracker.R;
import com.isport.tracker.util.DeviceConfiger;


/**
 * Created by Administrator on 2016/10/25.
 */

public class SleepStateView12 extends View {

    private Paint deepPaint;
    private Paint lightPaint;
    private Paint awakePaint;
    private Paint whitePaint;
    private Paint linePaint;
    private Paint labelPaint;
    private Paint elightPaint;
    private Rect rectLabel;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingBottom;
    private int mPaddingRight;
    private int[] sleepData;
    private String[] mLabels;
    private boolean disable;
    private int mLength;

    public SleepStateView12(Context context) {
        super(context);
        init(context);
    }

    public SleepStateView12(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SleepStateView12(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {

        rectLabel = new Rect();

        deepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deepPaint.setStyle(Paint.Style.FILL);
        deepPaint.setStrokeWidth(1);
        deepPaint.setColor(getResources().getColor(R.color.deep_color));

        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightPaint.setStyle(Paint.Style.FILL);
        lightPaint.setColor(getResources().getColor(R.color.light_color));
        lightPaint.setStrokeWidth(1);

        elightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        elightPaint.setStyle(Paint.Style.FILL);
        //elightPaint.setColor(0xff61bf1a);
        elightPaint.setColor(getResources().getColor(R.color.elight_color));
        elightPaint.setStrokeWidth(1);
        elightPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        /**
         *  <color name="deep_color">#ff4e83b2</color>
         <color name="light_color">#ffff9565</color>
         <color name="elight_color">@color/red</color>
         <color name="awake_color">#ff87cd51</color>
         */
        awakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        awakePaint.setStyle(Paint.Style.FILL);
        awakePaint.setColor(getResources().getColor(R.color.awake_color));
        awakePaint.setStrokeWidth(1);

        whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStrokeWidth(1);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        mPaddingLeft = DeviceConfiger.dp2px(10);
        mPaddingRight = DeviceConfiger.dp2px(10);

        mLabels = new String[25];
        for (int i = 0; i <= 24; i++) {
            if (i == 0 || i == 24) {
                mLabels[i] = 12 + "h";
            } else if (i < 12) {
                mLabels[i] = (12 + i) + "h";
            } else {
                mLabels[i] = (i - 12) + "h";
            }
        }

    }

    /**
     * 醒 1   极浅   2   浅  3   深  4
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() > 0 && sleepData != null) {
            mPaddingBottom = getHeight() / 5;
            float dw = (getWidth() - mPaddingLeft - mPaddingRight) / 48.0f;
            float dw2 = (getWidth() - mPaddingLeft - mPaddingRight) / (sleepData.length * 1.0f);
            int friDp = DeviceConfiger.dp2px(5);
            int tenDp = DeviceConfiger.dp2px(10);
            int topDeep = DeviceConfiger.dp2px(30);
            int topLight = DeviceConfiger.dp2px(60);
            int topELight = DeviceConfiger.dp2px(90);
            int topAwake = DeviceConfiger.dp2px(120);
            int h1 = getHeight() - mPaddingBottom;
            int h2 = getHeight() - mPaddingBottom + friDp;
            int h3 = getHeight() - mPaddingBottom + tenDp;
            for (int i = 0; i < mLength; i++) {
                if (i <= sleepData.length - 1) {
                    switch (sleepData[i]) {
                        case 0:
                           // canvas.drawRect(mPaddingLeft + dw2 * i, topDeep, mPaddingLeft + dw2 * (i + 1), h1, whitePaint);
                            break;
                        case 1:
                            canvas.drawRect(mPaddingLeft + dw2 * i, topAwake, mPaddingLeft + dw2 * (i + 1), h1, awakePaint);
                            break;
                        case 2:
                            canvas.drawRect(mPaddingLeft + dw2 * i, topELight, mPaddingLeft + dw2 * (i + 1), h1, elightPaint);
                            break;
                        case 3:
                            canvas.drawRect(mPaddingLeft + dw2 * i, topLight, mPaddingLeft + dw2 * (i + 1), h1, lightPaint);
                            break;
                        case 4:
                            canvas.drawRect(mPaddingLeft + dw2 * i, topDeep, mPaddingLeft + dw2 * (i + 1), h1, deepPaint);
                            break;
                    }
                }
            }
            for (int i = 0; i <= 48; i++) {
                if (i % 2 != 0) {
                    canvas.drawLine(mPaddingLeft + dw * i, h1, mPaddingLeft + dw * i, h2, linePaint);
                } else {
                    canvas.drawLine(mPaddingLeft + dw * i, h1, mPaddingLeft + dw * i, h3, linePaint);
                    String tp = mLabels[i / 2];
                    Rect rect = new Rect();
                    labelPaint.getTextBounds(tp, 0, tp.length(), rect);
                    Path path = new Path();
                    path.moveTo(mPaddingLeft + dw * i - rect.height() / 2, h3 + friDp);
                    path.lineTo(mPaddingLeft + dw * i - rect.height() / 2, h3 + friDp + rect.width());
                    canvas.drawTextOnPath(mLabels[i / 2], path, 0, 0, labelPaint);
                }
            }
            drawAxisLabel(canvas);
            canvas.drawLine(mPaddingLeft, h1, getWidth() - mPaddingRight, h1, linePaint);
        }
    }

    public void setDisable(boolean b) {
        this.disable = b;
    }

    private void drawAxisLabel(Canvas canvas) {
        String[] strs;
        Paint[] colors;
        if (disable) {
            strs = new String[]{getContext().getString(R.string.deep_sleep), getContext().getString(R.string.light_sleep), getContext().getString(R.string.awake)};
            colors = new Paint[]{deepPaint, lightPaint, awakePaint};
        } else {
            strs = new String[]{getContext().getString(R.string.deep_sleep), getContext().getString(R.string.light_sleep),

                    getContext().getString(R.string.elight_sleep), getContext().getString(R.string.awake)};
            colors = new Paint[]{deepPaint, lightPaint, elightPaint, awakePaint};
        }
        int[] strW = new int[strs.length];
        int wid = 0;
        for (int i = 0; i < strs.length; i++) {
            colors[i].setStrokeWidth(DeviceConfiger.dp2px(3));
            colors[i].getTextBounds(strs[i], 0, strs[i].length(), rectLabel);
            wid = rectLabel.width() + wid;
            strW[i] = rectLabel.width();
        }
        float topD = DeviceConfiger.dp2px(30);
        float tpH = rectLabel.height();
        float dw = (getWidth() - mPaddingLeft - mPaddingRight - DeviceConfiger.dp2px(3) * strs.length - wid -
                DeviceConfiger.dp2px(2) * strs.length) / (strs.length - 1);
        float tpT = (topD - tpH) / 2;
        float sw = mPaddingLeft;
        for (int i = 0; i < strs.length; i++) {
            if (i != strs.length - 1) {
                sw = sw + DeviceConfiger.dp2px(2);
            } else {
                sw = sw - DeviceConfiger.dp2px(2);
            }
            canvas.drawLine(sw - DeviceConfiger.dp2px(2), tpT - DeviceConfiger.dp2px(4), sw - DeviceConfiger.dp2px(2), topD - tpT, colors[i]);
            canvas.drawText(strs[i], 0, strs[i].length(), sw, topD - tpT, labelPaint);
            sw = sw + dw + labelPaint.getStrokeWidth() + strW[i];
        }
    }


    public void setSleepData(int[] sleepData, int length) {
        if (sleepData != null) {
            this.sleepData = sleepData;
            this.mLength = length;
            postInvalidate();
        }
    }
}
