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

public class SleepStateView extends View {

    private Paint deepPaint;
    private Paint lightPaint;
    private Paint elightPaint;
    private Paint awakePaint;
    private Paint linePaint;
    private Paint labelPaint;

    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPaddingBottom;
    private int mPaddingRight;
    private int[] sleepData = new int[288];
    private String[] mLabels;
    private Rect rectLabel;
    private boolean disable;

    public SleepStateView(Context context) {
        super(context);
        init(context);
    }

    public SleepStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SleepStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        rectLabel = new Rect();

        deepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deepPaint.setStyle(Paint.Style.FILL);
        deepPaint.setStrokeWidth(1);
        deepPaint.setColor(0xff4e83b2);
        //deepPaint.setColor(0xffACDC88);
        deepPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightPaint.setStyle(Paint.Style.FILL);
        lightPaint.setColor(0xffff9565);
        //lightPaint.setColor(0xff9ED673);
        lightPaint.setStrokeWidth(1);
        lightPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        elightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        elightPaint.setStyle(Paint.Style.FILL);
        //elightPaint.setColor(0xff61bf1a);
        elightPaint.setColor(0xff35bd35);
        elightPaint.setStrokeWidth(1);
        elightPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        awakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        awakePaint.setStyle(Paint.Style.FILL);
        awakePaint.setColor(0xff87cd51);
//        awakePaint.setColor(0xffB1EE7F);
//        awakePaint.setColor(0xffD9EDC9);
        awakePaint.setStrokeWidth(1);
        awakePaint.setTextSize(DeviceConfiger.sp2Dp(10));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        mPaddingLeft = DeviceConfiger.dp2px(10);
        mPaddingRight = DeviceConfiger.dp2px(10);

        mLabels = new String[25];
        for (int i=0;i<=24;i++){
            mLabels[i] = i+"h";
            /*if(i == 0 || i == 24){
                mLabels[i] = 12+":00";
            }else if(i<12){
                mLabels[i] = (12+i)+":00";
            }else{
                mLabels[i] = (i-12)+":00";
            }*/
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getWidth()>0){
            //mPaddingBottom = getHeight()*2/
            mPaddingBottom = DeviceConfiger.dp2px(40);
            mPaddingTop = DeviceConfiger.dp2px(50);
            float dw = (getWidth() - mPaddingLeft-mPaddingRight)/48.0f;
            float dw2 = (getWidth() - mPaddingLeft-mPaddingRight)/(sleepData.length*1.0f);
            int friDp = DeviceConfiger.dp2px(5);
            int tenDp = DeviceConfiger.dp2px(10);
            int h1 = getHeight() - mPaddingBottom;
            int h2 = getHeight() - mPaddingBottom+friDp;
            int h3 = getHeight() - mPaddingBottom + tenDp;
            float dh = (h1 - mPaddingTop)/4;
            float deeph = mPaddingTop;
            float lighth = mPaddingTop + dh;
            float elighth = mPaddingTop + dh*2;
            float awakeh = mPaddingTop + dh*3;
            awakePaint.setStrokeWidth(1);
            elightPaint.setStrokeWidth(1);
            lightPaint.setStrokeWidth(1);
            deepPaint.setStrokeWidth(1);
            for (int i =0;i<288;i++){

                switch (sleepData[i]){
                    case 0:
                        break;
                    case 1:
                        //canvas.drawRect(mPaddingLeft+dw2*i,awakeh,mPaddingLeft+dw2*(i+1),h1,awakePaint);
                        canvas.drawRect(mPaddingLeft+dw2*i,deeph,mPaddingLeft+dw2*(i+1),h1,awakePaint);
                        break;
                    case 2:
                        //canvas.drawRect(mPaddingLeft+dw2*i,elighth,mPaddingLeft+dw2*(i+1),h1,elightPaint);
                        canvas.drawRect(mPaddingLeft+dw2*i,deeph,mPaddingLeft+dw2*(i+1),h1,elightPaint);
                        break;
                    case 3:
                        //canvas.drawRect(mPaddingLeft+dw2*i,lighth,mPaddingLeft+dw2*(i+1),h1,lightPaint);
                        canvas.drawRect(mPaddingLeft+dw2*i,deeph,mPaddingLeft+dw2*(i+1),h1,lightPaint);
                        break;
                    case 4:
                        //canvas.drawRect(mPaddingLeft+dw2*i,deeph,mPaddingLeft+dw2*(i+1),h1,deepPaint);
                        canvas.drawRect(mPaddingLeft+dw2*i,deeph,mPaddingLeft+dw2*(i+1),h1,deepPaint);
                        break;
                }

            }
            labelPaint.setColor(Color.BLACK);
            for (int i=0;i<=48;i++){
                if(i%2 != 0) {
                    canvas.drawLine(mPaddingLeft + dw * i, h1, mPaddingLeft + dw * i, h2, linePaint);
                }else {
                    canvas.drawLine(mPaddingLeft + dw * i, h1, mPaddingLeft + dw * i, h3, linePaint);
                    String tp = mLabels[i/2];
                    Rect rect = new Rect();
                    labelPaint.getTextBounds(tp,0,tp.length(),rect);
                    Path path = new Path();
                    path.moveTo(mPaddingLeft+dw*i-rect.height()/2,h3+friDp);
                    path.lineTo(mPaddingLeft+dw*i-rect.height()/2,h3+friDp+rect.width());
                    canvas.drawTextOnPath(mLabels[i/2],path,0,0,labelPaint);
                }
            }
            canvas.drawLine(mPaddingLeft,h1,getWidth() - mPaddingRight,h1,linePaint);
            drawAxisLabel(canvas);
        }
    }

    private void drawAxisLabel(Canvas canvas){
        String[] strs;
        Paint[] colors;
        if (disable){
            strs= new String[]{getContext().getString(R.string.deep_sleep), getContext().getString(R.string.light_sleep), getContext().getString(R.string.awake)};
            colors= new Paint[]{deepPaint, lightPaint, awakePaint};
        }else {
            strs = new String[]{getContext().getString(R.string.deep_sleep), getContext().getString(R.string.light_sleep),

                    getContext().getString(R.string.elight_sleep), getContext().getString(R.string.awake)};
            colors = new Paint[]{deepPaint, lightPaint, elightPaint, awakePaint};
        }
        int[] strW = new int[strs.length];
        int wid = 0;
        for (int i=0;i<strs.length;i++){
            colors[i].setStrokeWidth(DeviceConfiger.dp2px(3));
            colors[i].getTextBounds(strs[i],0 ,strs[i].length(),rectLabel);
            wid = rectLabel.width()+wid;
            strW[i] = rectLabel.width();
        }
        float topD = DeviceConfiger.dp2px(30);
        float tpH = rectLabel.height();
        float dw = (getWidth() - mPaddingLeft - mPaddingRight - DeviceConfiger.dp2px(3)*strs.length - wid - DeviceConfiger.dp2px(2)*strs.length)/(strs.length-1);
        float tpT = (topD - tpH)/2;
        float sw = mPaddingLeft;
        for (int i=0;i<strs.length;i++){
            canvas.drawLine(sw,tpT-DeviceConfiger.dp2px(2),sw,topD- tpT + DeviceConfiger.dp2px(2),colors[i]);
            sw = sw+DeviceConfiger.dp2px(2);
            canvas.drawText(strs[i],0,strs[i].length(), sw,topD - tpT,labelPaint);
            sw = sw+dw+labelPaint.getStrokeWidth()+strW[i] ;

        }
    }

    public void setSleepData(int[] sleepData){
        if(sleepData != null) {
            this.sleepData = sleepData;
            postInvalidate();
        }
    }

    public void setDisable(boolean b) {
        this.disable=b;
    }
}
