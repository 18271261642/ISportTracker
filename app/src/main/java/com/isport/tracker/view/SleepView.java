package com.isport.tracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.isport.tracker.R;
import com.isport.tracker.util.DeviceConfiger;


/**
 * Created by Administrator on 2016/10/25.
 */

public class SleepView extends View {

    public static int SLEEP_STATE_WAKE = 0;//醒
    public static int SLEEP_STATE_DEEP = 1;//深睡
    public static int SLEEP_STATE_LIGHT = 2;//浅睡
    public static int SLEEP_STATE_E_LIGHT = 3;//极浅睡
    public static int DEFAULT = -1;//极浅睡

    private Paint bottonPaint;
    private Paint topPaint;
    private int oneDp, twoDp, friDp, tenDp, fifDp, twnTy;
    private int lineCount = 288;
    private int[] sleepStatus = new int[lineCount];
    private int twnF;
    private int thrTy;
    private int thrF;

    public SleepView(Context context) {
        super(context);
        init();
    }

    public SleepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SleepView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        oneDp = DeviceConfiger.dp2px(1);
        twoDp = DeviceConfiger.dp2px(2);
        friDp = DeviceConfiger.dp2px(5);
        tenDp = DeviceConfiger.dp2px(10);
        fifDp = DeviceConfiger.dp2px(15);
        twnTy = DeviceConfiger.dp2px(10);
        twnF = DeviceConfiger.dp2px(15);
        thrTy = DeviceConfiger.dp2px(20);
        thrF = DeviceConfiger.dp2px(25);
        bottonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottonPaint.setColor(getResources().getColor(R.color.title_color));
        bottonPaint.setStrokeWidth(twnTy);
        bottonPaint.setStyle(Paint.Style.STROKE);

        topPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        topPaint.setColor(getResources().getColor(R.color.percent_color));
        topPaint.setStrokeWidth(oneDp);
        topPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getHeight() > 0) {
            int radius = getWidth() / 2 - twnTy;
            float dang = 360 / (lineCount * 2f);
//            RectF deepRect = new RectF(twnTy / 2, twnTy / 2, getWidth() - twnTy / 2, getHeight() - twnTy / 2);
//            RectF lightRect = new RectF(friDp + twnTy / 2, friDp + twnTy / 2, getWidth() - (friDp + twnTy / 2), getHeight() - (friDp + twnTy / 2));
//            RectF eliRect = new RectF(tenDp + twnTy / 2, tenDp + twnTy / 2, getWidth() - (tenDp + twnTy / 2), getHeight() - (tenDp + twnTy / 2));

            RectF deepRect = new RectF(friDp + tenDp + twnTy / 2, friDp + tenDp + twnTy / 2, getWidth() - (friDp + tenDp + twnTy / 2), getHeight() - (friDp + tenDp + twnTy / 2));
            RectF whiterRec = new RectF(friDp + tenDp + twnTy / 2, friDp + tenDp + twnTy / 2, getWidth() - (friDp + tenDp + twnTy / 2), getHeight() - (friDp + tenDp + twnTy / 2));
            RectF lightRect = new RectF(friDp + tenDp + twnTy / 2, friDp + tenDp + twnTy / 2, getWidth() - (friDp + tenDp + twnTy / 2), getHeight() - (friDp + tenDp + twnTy / 2));
            RectF eliRect = new RectF(friDp + tenDp + twnTy / 2, friDp + tenDp + twnTy / 2, getWidth() - (friDp + tenDp + twnTy / 2), getHeight() - (friDp + tenDp + twnTy / 2));
            RectF wakeRect = new RectF(friDp + tenDp + twnTy / 2, friDp + tenDp + twnTy / 2, getWidth() - (friDp + tenDp + twnTy / 2), getHeight() - (friDp + tenDp + twnTy / 2));
            /*RectF deepRect = new RectF(getWidth()/2 - radius-twnTy/2,getHeight()/2 - radius-twnTy/2,getWidth()/2+radius+twnTy/2,getHeight()/2+radius+twnTy/2);
            RectF lightRect = new RectF(getWidth()/2 - radius-fifDp/2,getHeight()/2 - radius-fifDp/2,getWidth()/2+radius+fifDp/2,getHeight()/2+radius+fifDp/2);
            RectF eliRect = new RectF(getWidth()/2 - radius-tenDp/2,getHeight()/2 - radius-tenDp/2,getWidth()/2+radius+tenDp/2,getHeight()/2+radius+tenDp/2);*/
            bottonPaint.setColor(getResources().getColor(R.color.title_color));
            if (sleepStatus != null) {
                for (int i = 0; i < lineCount * 2; i++) {
                    if (i / 2 <= sleepStatus.length - 1) {
//                        if (sleepStatus[i / 2] == SLEEP_STATE_WAKE && i % 2 == 0) {
//                            bottonPaint.setStrokeWidth(twnTy);
//                            bottonPaint.setColor(getResources().getColor(R.color.awake_color));
//                            canvas.drawArc(wakeRect, 90 + i * dang, dang, false, bottonPaint);
//                        } else if (sleepStatus[i / 2] == SLEEP_STATE_DEEP && i % 2 == 0) {
//                            bottonPaint.setStrokeWidth(twnTy);
//                            bottonPaint.setColor(getResources().getColor(R.color.deep_color));
//                            canvas.drawArc(deepRect, 90 + i * dang, dang, false, bottonPaint);
//                        } else if (sleepStatus[i / 2] == SLEEP_STATE_LIGHT && i % 2 == 0) {
//                            bottonPaint.setStrokeWidth(fifDp);
//                            bottonPaint.setColor(getResources().getColor(R.color.light_color));
//                            canvas.drawArc(lightRect, 90 + i * dang, dang, false, bottonPaint);
//                        } else if (sleepStatus[i / 2] == SLEEP_STATE_E_LIGHT && i % 2 == 0) {
//                            bottonPaint.setStrokeWidth(tenDp);
//                            bottonPaint.setColor(getResources().getColor(R.color.elight_color));
//                            canvas.drawArc(eliRect, 90 + i * dang, dang, false, bottonPaint);
//                        } else if (sleepStatus[i / 2] == DEFAULT && i % 2 == 0) {
//                            bottonPaint.setStrokeWidth(twnTy);
//                            bottonPaint.setColor(getResources().getColor(R.color.gray_line));
//                            canvas.drawArc(wakeRect, 90 + i * dang, dang, false, bottonPaint);
//                        }
                        if (sleepStatus[i / 2] == SLEEP_STATE_WAKE && i % 2 == 0) {
                            bottonPaint.setStrokeWidth(twnTy);
                            bottonPaint.setColor(getResources().getColor(R.color.awake_color));
                            canvas.drawArc(wakeRect, 90 + i * dang, dang, false, bottonPaint);
                        } else if (sleepStatus[i / 2] == SLEEP_STATE_DEEP && i % 2 == 0) {
                            bottonPaint.setStrokeWidth(thrF);
                            bottonPaint.setColor(getResources().getColor(R.color.deep_color));
                            canvas.drawArc(deepRect, 90 + i * dang, dang, false, bottonPaint);
                        } else if (sleepStatus[i / 2] == SLEEP_STATE_LIGHT && i % 2 == 0) {
                            bottonPaint.setStrokeWidth(thrTy);
                            bottonPaint.setColor(getResources().getColor(R.color.light_color));
                            canvas.drawArc(lightRect, 90 + i * dang, dang, false, bottonPaint);
                        } else if (sleepStatus[i / 2] == SLEEP_STATE_E_LIGHT && i % 2 == 0) {
                            bottonPaint.setStrokeWidth(twnF);
                            bottonPaint.setColor(getResources().getColor(R.color.elight_color));
                            canvas.drawArc(eliRect, 90 + i * dang, dang, false, bottonPaint);
                        } else if (sleepStatus[i / 2] == DEFAULT && i % 2 == 0) {
                            bottonPaint.setStrokeWidth(thrF);
                            bottonPaint.setColor(getResources().getColor(R.color.bg_divider_line));
                            canvas.drawArc(whiterRec, 90 + i * dang, dang, false, bottonPaint);
                        }
                    }
                }
            } else {
                for (int i = 0; i < lineCount * 2; i++) {
                    if (i % 2 == 0) {
                        bottonPaint.setStrokeWidth(twnTy);
                        bottonPaint.setColor(getResources().getColor(R.color.gray_line));
                        canvas.drawArc(wakeRect, 90 + i * dang, dang, false, bottonPaint);
                    }
                }
            }
        }
    }

    public void setSleepState(int[] sleepState, int mlineCount) {
        if (sleepState != null) {
            this.sleepStatus = sleepState;
            this.lineCount = mlineCount;
//            Log.e("SleepView == ", "!= null");
            postInvalidate();
        } else {
//            Log.e("SleepView == ", "== null");
            this.sleepStatus = sleepState;
            this.lineCount = mlineCount;
            postInvalidate();
        }
    }
}
