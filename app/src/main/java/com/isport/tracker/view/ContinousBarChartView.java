package com.isport.tracker.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.isport.tracker.R;
import com.isport.tracker.entity.ContinousBarChartEntity;
import com.isport.tracker.entity.ContinousBarChartTotalEntity;
import com.isport.tracker.entity.sleepResultBean;
import com.isport.tracker.util.CalculateUtil;
import com.isport.tracker.util.DeviceConfiger;
import com.isport.tracker.util.DisplayUtils;
import com.isport.tracker.util.SleepFormatUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ContinousBarChartView extends View {
    //deepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //        deepPaint.setStyle(Paint.Style.FILL);
    //        deepPaint.setStrokeWidth(1);
    //        deepPaint.setColor(0xff4e83b2);
    //
    //        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //        lightPaint.setStyle(Paint.Style.FILL);
    //        lightPaint.setColor(0xffff9565);
    //        lightPaint.setStrokeWidth(1);
    //
    //        elightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //        elightPaint.setStyle(Paint.Style.FILL);
    //        //elightPaint.setColor(0xff61bf1a);
    //        elightPaint.setColor(0xff059045);
    //        elightPaint.setStrokeWidth(1);
    //        elightPaint.setTextSize(DeviceConfiger.sp2Dp(10));
    //
    //        awakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //        awakePaint.setStyle(Paint.Style.FILL);
    //        awakePaint.setColor(0xff87cd51);
    //        awakePaint.setStrokeWidth(1);


    private final int color[] = {0xffffff, 0xff87cd51, 0xffff0000, 0xffff9565, 0xff4e83b2};//文字的颜色
    private final int color1[] = {0xffffff, 0xff4e83b2, 0xffff9565, 0xffff0000, 0xff87cd51};//柱形图的颜色
    private final String type1[] = {"", getContext().getString(R.string.deep_sleep), getContext().getString(R.string.light_sleep), getContext().getString(R.string.elight_sleep), getContext().getString(R.string.awake)};
    private final String type[] = {"", getContext().getString(R.string.awake), getContext().getString(R.string.elight_sleep), getContext().getString(R.string.light_sleep), getContext().getString(R.string.deep_sleep)};
    private Context mContext;
    /**
     * 视图的宽和高  刻度区域的最大值
     */
    private int mTotalWidth, mTotalHeight, maxHeight;
    private int paddingRight, paddingBottom, paddingTop;
    //距离底部的多少 用来显示底部的文字
    private int bottomMargin;
    //距离顶部的多少 用来显示顶部的文字
    private int topMargin;
    private int rightMargin;
    private int leftMargin;
    /**
     * 画笔 轴 刻度 柱子 点击后的柱子 单位
     */
    private Paint axisPaint, textPaint, barPaint, borderPaint, unitPaint;
    private List<ContinousBarChartEntity> mData;//数据集合
    /**
     * item中的Y轴最大值
     */
    private float maxYValue;
    private int leftMoving;
    /**
     * Y轴最大的刻度值
     */
    private float maxYDivisionValue;
    /**
     * 柱子的矩形
     */
    private RectF mBarRect; //mBarRect,
    private Rect mBarRectClick;
    /**
     * 绘制的区域
     */
    private RectF mDrawArea;
    /**
     * 每一个bar的宽度
     */
    private float barWidth;
    /**
     * 每个bar之间的距离
     */
    private int barSpace;
    /**
     * 下面两个相当于图表的原点
     */
    private float mStartX;
    private int mStartY;
    /**
     * 柱形图左边的x轴坐标 和右边的x轴坐标
     */
    private List<Float> mBarLeftXPoints = new ArrayList<>();
    private List<Float> mBarRightXPoints = new ArrayList<>();

    /* 用户点击到了无效位置 */
    public static final int INVALID_POSITION = -1;
    private OnItemBarClickListener mOnItemBarClickListener;
    private GestureDetector mGestureListener;
    /**
     * 是否绘制点击效果
     */
    private boolean isDrawBorder;
    /**
     * 点击的地方
     */
    private int mClickPosition;

    //x轴 y轴的单位
    private String unitX;
    private String unitY;

    public void setOnItemBarClickListener(OnItemBarClickListener onRangeBarClickListener) {
        this.mOnItemBarClickListener = onRangeBarClickListener;
    }

    public interface OnItemBarClickListener {
        void onClick(int position, int hour, int minute);
    }

    public ContinousBarChartView(Context context) {
        this(context, null);
    }

    public ContinousBarChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContinousBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Paint deepPaint;
    private Paint lightPaint;
    private Paint awakePaint;
    private Paint linePaint;
    private Paint labelPaint;
    private Paint elightPaint;
    private Rect rectLabel;

    private void init(Context context) {
        mContext = context;


        rectLabel = new Rect();


        color1[0] = 0xffffff;
        color1[4] = context.getResources().getColor(R.color.deep_color);
        color1[3] = context.getResources().getColor(R.color.light_color);
        color1[2] = context.getResources().getColor(R.color.elight_color);
        color1[1] = context.getResources().getColor(R.color.awake_color);
        color[0] = 0xffffff;
        color[1] = context.getResources().getColor(R.color.deep_color);
        color[2] = context.getResources().getColor(R.color.light_color);
        color[3] = context.getResources().getColor(R.color.elight_color);
        color[4] = context.getResources().getColor(R.color.awake_color);

        //  /**
        //     *  <color name="deep_color">@color/green</color>52B615
        //     <color name="light_color">@color/font.blue</color>
        //     <color name="elight_color">#ffffff00</color>
        //     <color name="awake_color">@color/red</color>

        deepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deepPaint.setStyle(Paint.Style.FILL);
        deepPaint.setStrokeWidth(1);
        deepPaint.setColor(color[4]);
        //deepPaint.setColor(0xffACDC88);
        deepPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightPaint.setStyle(Paint.Style.FILL);
        lightPaint.setColor(color[3]);
        //lightPaint.setColor(0xff9ED673);
        lightPaint.setStrokeWidth(1);
        lightPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        elightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        elightPaint.setStyle(Paint.Style.FILL);
        //elightPaint.setColor(0xff61bf1a);
        elightPaint.setColor(color[2]);
        elightPaint.setStrokeWidth(1);
        elightPaint.setTextSize(DeviceConfiger.sp2Dp(10));

        awakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        awakePaint.setStyle(Paint.Style.FILL);
        awakePaint.setColor(color[1]);
//        awakePaint.setColor(0xffB1EE7F);
//        awakePaint.setColor(0xffD9EDC9);
        awakePaint.setStrokeWidth(1);
        awakePaint.setTextSize(DeviceConfiger.sp2Dp(10));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(DeviceConfiger.sp2Dp(10));


        topMargin = DisplayUtils.dip2px(getContext(), 10);
        bottomMargin = DisplayUtils.dip2px(getContext(), 30);
        rightMargin = DisplayUtils.dip2px(getContext(), 20);
        leftMargin = DisplayUtils.dip2px(getContext(), 10);

        mGestureListener = new GestureDetector(context, new RangeBarOnGestureListener());

        axisPaint = new Paint();
        axisPaint.setColor(ContextCompat.getColor(mContext, R.color.common_bg));
        axisPaint.setStrokeWidth(1);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(DisplayUtils.dip2px(getContext(), 10));

        unitPaint = new Paint();
        unitPaint.setAntiAlias(true);
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        unitPaint.setTypeface(typeface);
        unitPaint.setTextSize(DisplayUtils.dip2px(getContext(), 10));

        barPaint = new Paint();
        barPaint.setColor(Color.parseColor("#FF0000"));
//        barPaint.setStrokeWidth(3);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.WHITE);
//        borderPaint.setAlpha(120);


        mBarRectClick = new Rect(0, 0, 0, 0);
        mBarRect = new RectF(0, 0, 0, 0);
        mDrawArea = new RectF(0, 0, 0, 0);
    }

    private String startTime;
    private String endTime;

    public void setData(ContinousBarChartTotalEntity continousBarChartTotalEntity, String unitX, String unitY) {
        this.startTime = continousBarChartTotalEntity.startTime;
        this.endTime = continousBarChartTotalEntity.endTime;
        this.mData = continousBarChartTotalEntity.continousBarChartEntitys;
        this.unitX = unitX;
        this.unitY = unitY;
        if (mData != null && mData.size() > 0) {
            maxYValue = calculateMax(mData);
            getRange(maxYValue);
        }
    }

    /**
     * 计算出Y轴最大值
     *
     * @return
     */
    private float calculateMax(List<ContinousBarChartEntity> list) {
        float start = list.get(0).yValue;
        for (ContinousBarChartEntity entity : list) {
            if (entity.yValue > start) {
                start = entity.yValue;
            }
        }
        return start;
    }

    /**
     * 得到柱状图的最大和最小的分度值
     */
    private void getRange(float maxYValue) {
        int scale = CalculateUtil.getScale(maxYValue);//获取这个最大数 数总共有几位
        float unScaleValue = (float) (maxYValue / Math.pow(10, scale));//最大值除以位数之后剩下的值  比如1200/1000 后剩下1.2
        maxYDivisionValue = (float) (CalculateUtil.getRangeTop(unScaleValue) * Math.pow(10, scale));//获取Y轴的最大的分度值
        mStartX = CalculateUtil.getDivisionTextMaxWidth(maxYDivisionValue, mContext) + 20;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
        maxHeight = h - getPaddingTop() - getPaddingBottom() - bottomMargin - topMargin;
        paddingBottom = getPaddingBottom();
        paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();

    }

    //获取滑动范围和指定区域
    private void getArea() {
        //规定1分钟一个BarWidth的间距
        int size = Integer.parseInt(endTime) + 24 - Integer.parseInt(startTime);//12
        int miniuteSize = size * 60 / 1;
        barWidth = (float) (mTotalWidth - mStartX * 2) / miniuteSize;
        barSpace = 0;
        mStartY = mTotalHeight - bottomMargin - paddingBottom;
        mDrawArea = new RectF(mStartX, paddingTop, mTotalWidth - paddingRight - rightMargin, mTotalHeight - paddingBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null || mData.isEmpty()) return;
        getArea();
        //画左边的Y轴
//        canvas.drawLine(mStartX, mStartY+bottomMargin/2, mStartX, topMargin / 2, axisPaint);
        //画右边的Y轴
//        canvas.drawLine(mTotalWidth - paddingRight - rightMargin, mStartY+bottomMargin/2, mTotalWidth - paddingRight - rightMargin, topMargin / 2, axisPaint);
        //绘制刻度线 和 刻度
//        drawScaleLine(canvas);
        //调用clipRect()方法后，只会显示被裁剪的区域
//        canvas.clipRect(mDrawArea.left, mDrawArea.top, mDrawArea.right, mDrawArea.bottom + mDrawArea.height());
        //绘制柱子
        drawBar(canvas);
        drawAxisLabel(canvas);
        //绘制X轴的text
        drawXAxisText(canvas);
    }

    private void drawXAxisText(Canvas canvas) {
        //这里设置 x 轴的字一条最多显示3个，大于三个就换行
        int totalPeriod = Integer.parseInt(endTime) + 24 - Integer.parseInt(startTime);
        textPaint.setColor(Color.parseColor("#9399A5"));
        int size = 4;
        int distance = totalPeriod / size;//3
        for (int i = 0; i <= size; i++) {//0--4
            int xAxis = Integer.parseInt(startTime) + distance * i;
            if (xAxis > 24) {
                xAxis = xAxis - 24;
            }
            String text = xAxis + ":00";
            float left = mStartX + distance * i * 60 * barWidth;
            canvas.drawText(text, left - (textPaint.measureText(text)) / 2, mTotalHeight - bottomMargin * 2 / 3 + 10, textPaint);
        }
    }

    private float percent = 1f;
    private TimeInterpolator pointInterpolator = new DecelerateInterpolator();

    public void startAnimation() {
        ValueAnimator mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(2000);
        mAnimator.setInterpolator(pointInterpolator);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                percent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    private float lastEndX;

    private void drawBar(Canvas canvas) {
        mBarLeftXPoints.clear();
        mBarRightXPoints.clear();

        Rect topRect = new Rect(0, 0, mTotalWidth, mStartY);
        Rect bottomRect = new Rect(0, mStartY, mTotalWidth, mTotalHeight);
        canvas.drawRect(topRect, axisPaint);
        canvas.drawRect(bottomRect, borderPaint);

        mBarRect.bottom = mStartY;
        lastEndX = (int) mStartX;
        for (int i = 0; i < mData.size(); i++) {
         /*   if (mData.get(i).type == 0) {
                continue;
            }*/
            barPaint.setColor(color1[mData.get(i).type]);

            if (mData.get(i).type == 0) {
                mBarRect.top = mStartY - DisplayUtils.dip2px(mContext, 60);//(int) ((maxHeight * (mData.get(i).yValue / maxYDivisionValue)) * percent);
            } else if (mData.get(i).type == 1) { //睡醒
                mBarRect.top = mStartY - DisplayUtils.dip2px(mContext, 80);//(int) ((maxHeight * (mData.get(i).yValue / maxYDivisionValue)) * percent);
            } else if (mData.get(i).type == 2) {//极浅睡
                mBarRect.top = mStartY - DisplayUtils.dip2px(mContext, 100);//(int) ((maxHeight * (mData.get(i).yValue / maxYDivisionValue)) * percent);
            } else if (mData.get(i).type == 3) {//浅睡
                mBarRect.top = mStartY - DisplayUtils.dip2px(mContext, 120);//(int) ((maxHeight * (mData.get(i).yValue / maxYDivisionValue)) * percent);
            } else if (mData.get(i).type == 4) {
                mBarRect.top = mStartY - DisplayUtils.dip2px(mContext, 150);//(int) ((maxHeight * (mData.get(i).yValue / maxYDivisionValue)) * percent);
            }
            int scale = mData.get(i).period;//间隔时间多少份
            float length = barWidth * scale;

            mBarRect.left = lastEndX;// + barSpace * (i + 1) - leftMoving
            mBarRect.right = (mBarRect.left + length);
            lastEndX = (lastEndX + length);

            canvas.drawRect(mBarRect, barPaint);

            mBarLeftXPoints.add(mBarRect.left);
            mBarRightXPoints.add(mBarRect.right);
        }
        if (isDrawBorder) {
            drawTringle(canvas);
            drawHint(canvas, drawRoundRect(canvas));
        }
    }

    private void drawAxisLabel(Canvas canvas) {
        String[] strs;
        Paint[] colors;
        strs = new String[]{type[4], type[3], type[2], type[1]};
        colors = new Paint[]{deepPaint, lightPaint, elightPaint, awakePaint};
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
        float dw = (getWidth() - leftMargin - paddingRight - DeviceConfiger.dp2px(3) * strs.length - wid - DeviceConfiger.dp2px(2) * strs.length) / (strs.length - 1);
        float tpT = (topD - tpH) / 2;
        float sw = leftMargin;
        for (int i = 0; i < strs.length; i++) {
            canvas.drawLine(sw, tpT - DeviceConfiger.dp2px(2), sw, topD - tpT + DeviceConfiger.dp2px(2), colors[i]);
            sw = sw + DeviceConfiger.dp2px(2);
            canvas.drawText(strs[i], 0, strs[i].length(), sw, topD - tpT, labelPaint);
            sw = sw + dw + labelPaint.getStrokeWidth() + strW[i];

        }
    }

    private void drawTringle(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(DisplayUtils.dip2px(mContext, 2));

        int len = DisplayUtils.dip2px(mContext, 5);
        int left = (int) (centerX - len);
        int right = (int) (centerX + len);
        Path path2 = new Path();
        path2.moveTo(left, mStartY);
        path2.lineTo(right, mStartY);
        path2.lineTo(centerX, mStartY - len);
        path2.close();
        canvas.drawPath(path2, paint);

        canvas.drawLine(centerX, mStartY - len + 1, centerX, mStartY - DisplayUtils.dip2px(mContext, 40), paint);

//        paint.setColor(Color.parseColor("#1DCE74"));
//        canvas.drawLine(centerX, mStartY - 80, centerX, mStartY - 82, paint);
//
//        Path path3 = new Path();
//        left = (int) (centerX - 5);
//        right = (int) (centerX + 5);
//        path3.moveTo(left, mStartY - 87);
//        path3.lineTo(right, mStartY - 87);
//        path3.lineTo(centerX, mStartY - 82);
//        path3.close();
//        canvas.drawPath(path3, paint);
    }

    private RectF drawRoundRect(Canvas canvas) {
        barPaint.setColor(Color.TRANSPARENT);
        int left = mTotalWidth / 2 - DisplayUtils.dip2px(mContext, 107) / 2;//(int) (centerX - 48);
        int right = mTotalWidth / 2 + DisplayUtils.dip2px(mContext, 107) / 2;//(int) (centerX + 48);
//        if (left <= mStartX) {
//            left = (int) mStartX;
//            right = (int) mStartX + 96;
//        }
//        if (right >= mTotalWidth - paddingRight - rightMargin) {
//            right = mTotalWidth - paddingRight - rightMargin;
//            left = right - 96;
//        }
        RectF rect = new RectF(left, (int) (mStartY - DisplayUtils.dip2px(mContext, 40) - DisplayUtils.dip2px(mContext, 40)), right, (mStartY - DisplayUtils.dip2px(mContext, 40) - DisplayUtils.dip2px(mContext, 20)));
        canvas.drawRect(rect, barPaint);
        return rect;
    }

    private boolean isHint = true;
    ArrayList<sleepResultBean> resultDuration;

    public void setSleep(ArrayList<sleepResultBean> resultDuration) {
        this.resultDuration = resultDuration;
    }

    private void drawHint(Canvas canvas, RectF rect) {
        ContinousBarChartEntity barChartEntity = mData.get(mClickPosition);

        if (isHint) {
            int len = (int) (centerX - mStartX);
            int i = (int) (len / barWidth);

            ArrayList<String> result = SleepFormatUtils.sleepTimeFormatByIndex(startPosition, resultDuration, mData, type);
            String startTime = "";
            String textState = "";
            if (result.size() > 1) {
                startTime = result.get(0);
            }
            if (result.size() >= 2) {
                textState = result.get(1);
            }


            String text = startTime;//barChartEntity.getxLabel()+":00  "+barChartEntity.getyValue();

            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(DisplayUtils.dip2px(mContext, 15));
            textPaint.setAntiAlias(true);
            textPaint.setStyle(Paint.Style.FILL);
            //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
            textPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
            float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom

            int baseLineY = (int) (rect.centerY() - rect.centerY() + DisplayUtils.dip2px(mContext, 50) - top / 2 - bottom / 2);//基线中间点的y轴计算公式
            canvas.drawText(text, rect.centerX(), baseLineY, textPaint);
            baseLineY = (int) (rect.centerY() - rect.centerY() + DisplayUtils.dip2px(mContext, 70) - top / 2 - bottom / 2);//基线中间点的y轴计算公式
            canvas.drawText(textState, rect.centerX(), baseLineY, textPaint);
        }
    }

    private String getTime(int position) {
        int hour = Integer.parseInt(startTime) + position / 60;
        if (hour > 24) {
            hour = hour - 24;
        }
        int minute = position % 60;
        return hour + ":" + minute;
    }

    int startPosition, endPosition;

    private void setHourAndMinute() {
        ContinousBarChartEntity barChartEntity = mData.get(mClickPosition);
        int targetType = barChartEntity.type;

        startPosition = mClickPosition;
        endPosition = mClickPosition;

        for (int i = mClickPosition; i >= 0; i--) {
            if (mData.get(i).type == targetType) {
                startPosition = i;
            } else {
                break;
            }
        }

        for (int i = mClickPosition; i < mData.size(); i++) {
            if (mData.get(i).type == targetType) {
                endPosition = i;
            } else {
                break;
            }
        }
        //由于index是由0开始要+1,而且要包含startPosition，所以还要+1
        int lenTime = endPosition - startPosition + 1;
        hour = lenTime / 60;
        minute = lenTime % 60;

    }


    /**
     * Y轴上的text (1)当最大值大于1 的时候 将其分成5份 计算每个部分的高度  分成几份可以自己定
     * （2）当最大值大于0小于1的时候  也是将最大值分成5份
     * （3）当为0的时候使用默认的值
     */
    private void drawScaleLine(Canvas canvas) {
        float eachHeight = (maxHeight / 3f);
        float textValue = 0;
        if (maxYValue > 1) {
            for (int i = 0; i <= 3; i++) {
                float startY = mStartY - eachHeight * i;
                BigDecimal maxValue = new BigDecimal(maxYDivisionValue);
                BigDecimal fen = new BigDecimal(0.2 * i);
                String text = null;
                //因为图表分了5条线，如果能除不进，需要显示小数点不然数据不准确
                if (maxYDivisionValue % 3 != 0) {
                    text = String.valueOf(maxValue.multiply(fen).floatValue());
                } else {
                    text = String.valueOf(maxValue.multiply(fen).longValue());
                }
                canvas.drawText(text, mStartX - textPaint.measureText(text) - 5, startY + textPaint.measureText("0") / 2, textPaint);
                canvas.drawLine(mStartX, startY, mTotalWidth - paddingRight - rightMargin, startY, axisPaint);
            }
        } else if (maxYValue > 0 && maxYValue <= 1) {
            for (int i = 0; i <= 3; i++) {
                float startY = mStartY - eachHeight * i;
                textValue = CalculateUtil.numMathMul(maxYDivisionValue, (float) (0.2 * i));
                String text = String.valueOf(textValue);
                canvas.drawText(text, mStartX - textPaint.measureText(text) - 5, startY + textPaint.measureText("0") / 2, textPaint);
                canvas.drawLine(mStartX, startY, mTotalWidth - paddingRight - rightMargin, startY, axisPaint);
            }
        } else {
            for (int i = 0; i <= 3; i++) {
                float startY = mStartY - eachHeight * i;
                String text = String.valueOf(10 * i);
                canvas.drawText(text, mStartX - textPaint.measureText(text) - 5, startY + textPaint.measureText("0") / 2, textPaint);
                canvas.drawLine(mStartX, startY, mTotalWidth - paddingRight - rightMargin, startY, axisPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                lastPointX = event.getX();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float movex = event.getX();
//                movingThisTime = lastPointX - movex;
//                leftMoving = leftMoving + movingThisTime;
//                lastPointX = movex;
////                invalidate();
//                break;
//            case MotionEvent.ACTION_UP:
////                invalidate();
//                lastPointX = event.getX();
//                break;
//            case MotionEvent.ACTION_CANCEL:
////                recycleVelocityTracker();
//                break;
//            default:
//                return super.onTouchEvent(event);
//        }
        if (mGestureListener != null) {
            mGestureListener.onTouchEvent(event);
        }
        return true;
    }

    /**
     * 点击
     */
    private class RangeBarOnGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int position = identifyWhichItemClick(e.getX(), e.getY());

            if (position != INVALID_POSITION && mOnItemBarClickListener != null) {
                setClicked(position);
                setHourAndMinute();
                mOnItemBarClickListener.onClick(position, hour, minute);
                invalidate();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    /**
     * 设置选中的位置
     *
     * @param position
     */
    public void setClicked(int position) {
        isDrawBorder = true;
        mClickPosition = position;
    }

    private float centerX = 0;

    private int hour, minute;

    /**
     * 根据点击的手势位置识别是第几个柱图被点击
     *
     * @param x
     * @param y
     * @return -1时表示点击的是无效位置
     */
    private int identifyWhichItemClick(float x, float y) {
        float leftx = 0;
        float rightx = 0;
        if (mData != null) {
            for (int i = 0; i < mData.size(); i++) {
                leftx = mBarLeftXPoints.get(i);
                rightx = mBarRightXPoints.get(i);
                if (x < leftx) {
                    break;
                }
                if (leftx <= x && x <= rightx) {
                    centerX = (leftx + rightx) / 2;
                    return i;
                }
            }
        }

        return INVALID_POSITION;
    }
}
