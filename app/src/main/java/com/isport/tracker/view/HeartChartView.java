package com.isport.tracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.isport.tracker.R;
import com.isport.tracker.util.DeviceConfiger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/8.
 */
public class HeartChartView extends View {

    private int mMarginTop;
    private int mMarginBottom;
    private int mMarginLeft;
    private int mMarginRight;
    private int mFourDp;
    private int mTwoDp;
    private int mOneDp;
    private Context mContext;
    private Paint mAsxiPaint;
    private Paint mPathPaint;
    private int mViviGray;
    private int mViviRed;
    private int mViviLightGray;
    private String[] mYLabels = new String[8];
    private String[] mXLabels = new String[5];
    private int mTotalCount = 150;
    private int mInterval = 30;
    private List<Integer> mDataSerise;///数据

    public HeartChartView(Context context) {
        super(context);
        init(context);
    }

    public HeartChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeartChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        mContext = context;

        mDataSerise = new LinkedList<Integer>();
        mMarginBottom = DeviceConfiger.dp2px(20);
        mMarginTop = DeviceConfiger.dp2px(10);
        mMarginLeft = DeviceConfiger.dp2px(30);
        mMarginRight = DeviceConfiger.dp2px(25);
        mFourDp = DeviceConfiger.dp2px(4);
        mTwoDp = DeviceConfiger.dp2px(2);
        mOneDp = DeviceConfiger.dp2px(1);

        mViviGray = getResources().getColor(R.color.gray);
        mViviLightGray = getResources().getColor(R.color.gray);
        mViviRed = context.getResources().getColor(R.color.percent_color);

        mAsxiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAsxiPaint.setColor(mViviGray);
        mAsxiPaint.setStrokeWidth(mTwoDp);
        mAsxiPaint.setColor(mViviGray);
        mAsxiPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, getResources()
                .getDisplayMetrics()));

        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPathPaint.setColor(mViviRed);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(mTwoDp);

        for (int i=1;i<=mYLabels.length;i++){
            mYLabels[i-1] = 30*i+"";
        }
        setXIntervalTime(30);
    }

    public synchronized void addValue(int index,int value){
        mDataSerise.add(value);

        int count = mDataSerise.size();
        if(count<120){
            setXIntervalTime(30);
        }
        if(count<240) {
            setXIntervalTime(60);
        }

        if(count>mInterval*4){
            mInterval = (count+(4-count%4))/4;
            setXIntervalTime(mInterval+60);
        }
        postInvalidate();
    }

    public void clearGraph(){
        mDataSerise.clear();
        setmDataSerise(new ArrayList());
        setXIntervalTime(30);
    }

    public void setmDataSerise(List dataSerise){
        if(dataSerise == null)
            return;
        this.mDataSerise = dataSerise;
        int count = mDataSerise.size();
        if(count<120){
            setXIntervalTime(30);
        }else if(count<240) {
            setXIntervalTime(60);
        }else if(count>mInterval*4){
            mInterval = (count +60 - count%60)/5;
            setXIntervalTime(mInterval+60);
        }
       /* this.mDataSerise = dataSerise;
        int count = mDataSerise.size();
        if(count<120){
            setXIntervalTime(30);
        }
        if(count<240) {
            setXIntervalTime(60);
        }

        if(count>mInterval*4){
            mInterval = (count+(4-count%4))/4;
            setXIntervalTime(mInterval+60);
        }*/
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAsxi(canvas);
        drawPath(canvas);
    }

    public void drawPath(Canvas canvas){
        float dw = (getWidth() - mMarginLeft - mMarginRight)/(mTotalCount*1.0f);
        float dh = (getHeight() - mMarginTop - mMarginBottom)/240.0f;
        int w = getWidth();
        int h = getHeight();
        Path path = new Path();
        //path.moveTo(mMarginLeft,h-mMarginBottom);
        int length = mDataSerise.size();
        float lx = -100;
        float ly = -100;
        for (int i=0;i<length;i++){
            float x = mMarginLeft+i*dw;

            float y = h -mMarginBottom-(mDataSerise.get(i) == null?0:mDataSerise.get(i))*dh;
            if(x == lx && y == ly)
                continue;
            if(i == 0){
                path.moveTo(x,y);
            }else {
                path.lineTo(x, y);
            }
            lx = x;
            ly = y;

        }
        /*path.lineTo(length*dw+mMarginLeft,h - mMarginBottom);
        path.close();*/
        canvas.drawPath(path,mPathPaint);
    }

    public void setXIntervalTime(int time){
        mInterval = time;
        for (int i = 1; i <= mXLabels.length; i++) {
            if(time<60) {
                mXLabels[i - 1] = time * i + "";
            }else {
                mXLabels[i - 1] = (time/60) * i + "";
            }
        }
        mTotalCount = time*mXLabels.length;
        postInvalidate();
    }

    private void drawAsxi(Canvas canvas){//绘制坐标轴
        Rect rect = new Rect();
        canvas.drawLine(mMarginLeft,getHeight()-mMarginBottom+mFourDp*2,mMarginLeft,mMarginTop-mFourDp*2,mAsxiPaint);
        canvas.drawLine(mMarginLeft-mFourDp*2,getHeight()-mMarginBottom,getWidth(),getHeight()-mMarginBottom,mAsxiPaint);
        float dh = (getHeight() - (mMarginBottom+mMarginTop))/(mYLabels.length*2.0f);
        String tp = "0";
        mAsxiPaint.getTextBounds(tp,0,tp.length(),rect);
        canvas.drawText(tp,mMarginLeft-mTwoDp*5-rect.width(),getHeight()-mMarginBottom+rect.height()/2,mAsxiPaint);
        canvas.drawText(tp,mMarginLeft-rect.width()/2,getHeight()-mMarginBottom+mTwoDp*5+rect.height(),mAsxiPaint);
        for (int i=0;i<mYLabels.length*2;i++){
            if(i%2 == 0){
                tp = mYLabels[mYLabels.length -1-i/2];
                mAsxiPaint.getTextBounds(tp,0,tp.length(),rect);
                canvas.drawLine(mMarginLeft,mMarginTop+i*dh,mMarginLeft-mFourDp,mMarginTop+i*dh,mAsxiPaint);
                canvas.drawText(tp,mMarginLeft-mTwoDp*5-rect.width(),mMarginTop+i*dh+rect.height()/2,mAsxiPaint);
            }else {
                canvas.drawLine(mMarginLeft,mMarginTop+i*dh,mMarginLeft-mTwoDp,mMarginTop+i*dh,mAsxiPaint);
            }
        }
        float dw = (getWidth() - (mMarginRight+mMarginLeft))/(mXLabels.length*2.0f);
        for(int i = 0;i<mXLabels.length*2;i++){
            if(i%2 == 0){
                tp = mXLabels[i/2];
                mAsxiPaint.getTextBounds(tp,0,tp.length(),rect);
                canvas.drawLine(mMarginLeft+(i+2)*dw,getHeight()-mMarginBottom+mFourDp,mMarginLeft+(i+2)*dw,getHeight()-mMarginBottom,mAsxiPaint);
                canvas.drawText(tp,mMarginLeft+(i+2)*dw - rect.width()/2,getHeight()-mMarginBottom+mTwoDp*5+rect.height(),mAsxiPaint);
            }else {
                canvas.drawLine(mMarginLeft+i*dw,getHeight()-mMarginBottom+mTwoDp,mMarginLeft+i*dw,getHeight()-mMarginBottom,mAsxiPaint);
            }
        }
        tp = "(S)";
        if(mInterval>=60) {
           tp="(M)";
        }
        mAsxiPaint.getTextBounds(tp, 0, tp.length(), rect);
        canvas.drawText(tp, mMarginLeft + (mXLabels.length * 2) * dw + mMarginRight / 2, getHeight() - mMarginBottom + mTwoDp * 5 + rect.height(), mAsxiPaint);
    }
}
