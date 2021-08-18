package com.isport.tracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.isport.tracker.R;
import com.isport.tracker.fragment.PedoHistoryFragment;
import com.isport.tracker.util.DeviceConfiger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2016/10/24.
 * 绘制历史数据
 */

public class PedoHistView extends View {

    private List<String> mLabels;
    private List<Double> mListData;
    private Paint labelPaint;
    private Paint dataPaint;
    private Paint pathPaint;
    private Paint linePaint;
    private int paddingLeft;
    private int paddingRight;
    private int paddingTop;
    private int paddingBottom;
    private int oneDp, twoDp, friDp;
    private int type = PedoHistoryFragment.DATE_DAY;
    private Rect rect = new Rect();
    private Path path = new Path();

    public PedoHistView(Context context) {
        super(context);
        init(context);
    }

    public PedoHistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PedoHistView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        oneDp = DeviceConfiger.dp2px(1);
        twoDp = DeviceConfiger.dp2px(2);
        friDp = DeviceConfiger.dp2px(5);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(getResources().getColor(R.color.black));
        labelPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        dataPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dataPaint.setColor(getResources().getColor(R.color.gray));
        dataPaint.setTextSize(DeviceConfiger.sp2Dp(12));

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(getResources().getColor(R.color.hist_color));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(getResources().getColor(R.color.gray));
        linePaint.setStrokeWidth(oneDp);

        paddingTop = paddingLeft = paddingRight = DeviceConfiger.dp2px(15);
        paddingBottom = DeviceConfiger.dp2px(30);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() > 0) {
            int hei = getHeight() - paddingBottom;
            canvas.drawLine(paddingLeft, getHeight() - paddingBottom, getWidth() - paddingRight, getHeight() - paddingBottom, linePaint);
            if (mLabels != null && mLabels.size() > 0) {
                int wid = getWidth() - paddingRight - paddingLeft - friDp * 2;
                float dw = wid;
                if (mLabels.size() > 1) {
                    dw = wid / ((mLabels.size() - 1) * 1.0f);
                }
                int lh = hei + paddingBottom - friDp;


                if (mLabels.size() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    boolean isPaint = false;
                    for (int i = 0; i < mLabels.size(); i++) {
                        canvas.drawLine(paddingLeft + friDp + dw * i, hei, paddingLeft + friDp + dw * i, hei + friDp, linePaint);
                        if (mLabels.size() > 0) {
                            String tp = mLabels.get(i);
                            labelPaint.getTextBounds(tp, 0, tp.length(), rect);
                            labelPaint.setColor(getResources().getColor(R.color.black));
                            canvas.drawText(tp, paddingLeft + friDp + dw * i - rect.width() / 2, lh, labelPaint);
                        }
                    }
                }
                if (mListData != null && mListData.size() > 0) {
                    if (mListData.size() > 1 || mMaxData != 0) {
                        path.reset();
                        path.moveTo(paddingLeft + friDp, hei - oneDp / 2);
                        float dh = (float) ((hei - paddingTop) / ( mMaxData));
                        List<HistPoint> listPoints = Collections.synchronizedList(new ArrayList<HistPoint>());

                        for (int i = 0; i < mListData.size(); i++) {
                            double tp = mListData.get(i);
                            float x = paddingLeft + friDp + dw * i;
                            float y = (float) (hei - (tp) * dh);
                            listPoints.add(new HistPoint(x, y));
                            path.lineTo(x, y);
                        }
                        path.lineTo(getWidth() - paddingRight - friDp, hei);
                        //path.close();
                        canvas.drawPath(path, pathPaint);

                        for (int i = 0; i < listPoints.size(); i++) {
                            if (i < mListData.size()) {
                                double value1 = mListData.get(i);
                                String tp = null;
                                if (value1  != (((int)value1)+0d)) {
                                    tp = String.format(Locale.ENGLISH,"%.2f", value1);
                                } else {
                                    tp = ((int)(mListData.get(i).doubleValue())) + "";
                                }
                                if (tp != null) {
                                    String[] strs = tp.split("[^0-9]");
                                    if (strs.length == 2) {
                                        tp = strs[0] + "." + strs[1];
                                    }
                                }
                                if (Float.valueOf(tp) == 0)
                                    continue;
                                dataPaint.getTextBounds(tp, 0, tp.length(), rect);
                                HistPoint point = listPoints.get(i);
                                canvas.drawText(tp, point.x - rect.width() / 2, point.y - twoDp, dataPaint);
                            }
                        }
                    }
                }
            }
        }
    }

    class HistPoint {
        public float x;
        public float y;

        public HistPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private Double mMaxData;

    public void setmLabels(List labels, List<Double> dataList) {
        this.mMaxData = maxList(dataList);
        this.mListData = dataList;
        this.mLabels = labels;
        postInvalidate();
    }

    public double maxList(List<Double> list) {
        double max = 0;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                double tp = list.get(i);
                max = tp > max ? tp : max;

            }
        }
        return max;
    }

    public int maxIntegerList(List<Integer> list) {
        int max = 0;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                int tp = list.get(i);
                max = tp > max ? tp : max;
            }
        }
        return max;
    }
}
