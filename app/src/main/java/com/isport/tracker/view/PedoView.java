package com.isport.tracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.isport.tracker.R;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.util.UIUtils;

import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public class PedoView extends View {

    private int COL_NUMS = 24;
    private int ROW_NUMS = 5;

    private Paint gridPaint;//绘制网格
    private Paint pathPaint;///绘制路径
    private Paint linePaint;
    private Paint labelPaint;///绘制标签
    private int gridColor = 0xffB6D3FD;
    private int labelColor = 0xff000000;
    private int pathColor = 0xff7CDF2E;
    private int shadowBeginColor = 0xff7CDF2E;
    private int shadowEndColor = 0xff7CDF2E;
    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;
    private Path path;
    private Rect labelRect;
    private Shader pathShader;

    private double mMaxValue;
    private List<String> mLabels;
    private List<Double> mListData;


    public PedoView(Context context) {
        super(context);
        init(context);
    }

    public PedoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PedoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(gridColor);
        gridPaint.setColor(UIUtils.getColor(R.color.gridcolor));
        gridPaint.setStyle(Paint.Style.STROKE);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(UIUtils.getColor(R.color.pathcolor));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(UIUtils.getColor(R.color.pathcolor));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(DeviceConfiger.dp2px(1));

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(UIUtils.getColor(R.color.labelcolor));
        labelPaint.setTextSize(DeviceConfiger.sp2px(context, 10));

        paddingLeft = paddingRight = DeviceConfiger.dp2px(15);
        paddingBottom = paddingTop = DeviceConfiger.dp2px(15);

        labelRect = new Rect();
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0)
            return;
//        int cols = (mListData == null || mListData.size() < 1) ? COL_NUMS : (mListData.size() - 1);
        int cols = 24;
        int colsTemp = 48;
        int rows = ROW_NUMS;
        int wid = getWidth() - paddingLeft - paddingRight;
        int hei = getHeight() - paddingBottom - paddingTop;

        if (pathShader == null) {
            pathShader = new LinearGradient(paddingLeft, paddingTop, paddingLeft, getHeight() - paddingBottom,
                    new int[]{UIUtils.getIntegers(R.integer.pathShader00), UIUtils
                            .getIntegers(R.integer.pathShaderff)}, new float[]{0f, 1f},
                    Shader.TileMode.REPEAT);
        }

        float dw = (cols == 0 ? wid : (wid / cols));
        float dwTemp = (colsTemp == 0 ? wid : (wid / colsTemp));
        float dh = (rows == 0 ? hei : (hei / rows));

        //列
        for (int i = 0; i <= cols; i++) {
            canvas.drawLine(paddingLeft + dw * i, paddingTop, paddingLeft + dw * i, getHeight() - paddingBottom,
                    gridPaint);
        }
        //行
        for (int j = 0; j <= rows; j++) {
            canvas.drawLine(paddingLeft, paddingTop + dh * j, getWidth() - paddingRight, paddingTop + dh * j,
                    gridPaint);
        }
        if (mLabels != null && mLabels.size() == (cols + 1)) {
            for (int i = 0; i < mLabels.size(); i++) {
                labelPaint.getTextBounds(mLabels.get(i), 0, mLabels.get(i).length(), labelRect);
                canvas.drawText(mLabels.get(i), 0, mLabels.get(i).length(), paddingLeft + (dw * i - labelRect.width()
                                / 2),
                        getHeight() - paddingBottom + DeviceConfiger.dp2px(2) + labelRect.height(), labelPaint);
            }
        }

        drawPath(canvas, wid, hei, dw, dh);
    }

    public void setMaxValue(double maxValue) {
        this.mMaxValue = maxValue;
    }

    public void drawPath(Canvas canvas, int wid, int hei, float dw, float dh) {
        path.reset();
        path.moveTo(paddingLeft, getHeight() - paddingBottom);
        if (!(mListData == null || mListData.size() <= 0)) {
            for (int i = 0; i < mListData.size(); i++) {
                double value = mListData.get(i);
                path.lineTo(paddingLeft + i * dw / 2 + dw / 2, (float) (paddingTop + (hei * (1 - (value / mMaxValue)))));
            }
        }
        path.lineTo(getWidth() - paddingRight, getHeight() - paddingBottom);

        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setShader(pathShader);
        canvas.drawPath(path, pathPaint);

        canvas.drawPath(path, linePaint);
    }

    public void setmLabels(List<String> labels) {
        this.mLabels = labels;
    }

    public void setListData(List<Double> listData) {
        this.mListData = listData;
        invalidate();
    }

    @Override
    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    @Override
    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }

    @Override
    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }

    @Override
    public int getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
    }
}
