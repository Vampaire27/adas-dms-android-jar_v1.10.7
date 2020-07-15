package com.hobot.sample.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.Locale;

public class WaveView extends View implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "WaveView";

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GOLD = 0xFFFFC400;
    private int COUNT_X = 50;

    private int mWidth;
    private int mHeight;
    // X轴偏移量
    private int mXOffset = 10;
    // Y轴偏移量
    private int mYOffset = 10;
    // X轴
    private float mMinX = -1f;
    private float mMaxX = -1f;
    private float mXLevel = -1f;
    private int mXColor = COLOR_WHITE;
    private int mXWidth = 2;
    private int mXTextSize = 5;

    // Y轴
    private float mMinY = -1;
    private float mMaxY = -1;
    private float mYLevel = -1;
    private int mYColor = COLOR_WHITE;
    private int mYWidth = 2;
    private int mYTextSize = 5;

    // 波形的颜色
    private int mWaveColor = COLOR_GOLD;
    private int mWaveWidth = 1;

    private int mCount = COUNT_X;
    private long mRate = 67;
    private float tempSample = 0;

    private Paint mPaint;
    private PaintFlagsDrawFilter mPaintFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Path mPath;
    private PathEffect mPathEffect;

    private LinkedList<Float> mDeque = new LinkedList<>();

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mPaint = new Paint();
        mPath = new Path();
        mPathEffect = new CornerPathEffect(25);
    }

    /**
     * X - AXIS
     *
     * @param min   X-min
     * @param max   X-max
     * @param level X-level
     */
    public WaveView x(float min, float max, float level) {
        this.mMinX = min;
        this.mMaxX = max;
        this.mXLevel = level;
        postInvalidate();
        return this;
    }

    /**
     * Y - AXIS
     *
     * @param min   Y-min
     * @param max   Y-max
     * @param level Y-level
     */
    public WaveView y(float min, float max, float level) {
        this.mMinY = min;
        this.mMaxY = max;
        this.mYLevel = level;
        postInvalidate();
        return this;
    }

    /**
     * 设置Y轴颜色
     *
     * @param color
     * @return
     */
    public WaveView xColor(int color) {
        mXColor = color;
        return this;
    }

    /**
     * 设置X轴颜色
     *
     * @param color
     * @return
     */
    public WaveView yColor(int color) {
        mYColor = color;
        return this;
    }

    /**
     * 设置X轴宽度
     *
     * @param width
     * @return
     */
    public WaveView xWidth(int width) {
        mXWidth = width;
        return this;
    }

    /**
     * 设置Y轴宽度
     *
     * @param width
     * @return
     */
    public WaveView yWidth(int width) {
        mYWidth = width;
        return this;
    }

    /**
     * 设置波形颜色
     *
     * @param color
     * @return
     */
    public WaveView setWaveColor(int color) {
        this.mWaveColor = color;
        return this;
    }

    /**
     * 设置波形宽度
     *
     * @param width
     * @return
     */
    public WaveView setWaveWidth(int width) {
        this.mWaveWidth = width;
        return this;
    }

    /**
     * 设置刷新率
     *
     * @param rate mils
     */
    public void setRate(long rate) {
        this.mRate = rate;
    }

    /**
     * 添加数据
     *
     * @param percent percent
     */
    public void addData(float percent) {
        tempSample = percent;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.setDrawFilter(mPaintFilter);

        // 画波形
        drawWave(canvas, mPaint, mDeque);
        // 画X轴
        drawX(canvas, mPaint);
        // 画Y轴
        drawY(canvas, mPaint);

        canvas.restore();
        postInvalidateDelayed(mRate);
    }

    /**
     * 画X轴
     *
     * @param canvas
     * @param paint
     */
    private void drawX(Canvas canvas, Paint paint) {
        int width = mWidth - mXOffset * 2;
        int height = mHeight - mYOffset * 2;
        float startX = mXOffset;
        float startY = mHeight - mYOffset;
        float stopX = mWidth - mXOffset;
        float stopY = mHeight - mYOffset;
        // 画X坐标轴
        paint.reset();
        paint.setColor(mXColor);
        paint.setStrokeWidth(mXWidth);
        paint.setTextSize(mXTextSize);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        // 画X轴刻度
        if (mXLevel > 0) {
            for (float i = mMinX; i <= mMaxX; ) { // X-AXIS
                float x = (mMaxX - i) * width / mXLevel + startX;
                canvas.drawLine(x, stopY - mXTextSize + 2, x, stopY, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%1.0f", i), x, stopY + mXTextSize + 2, paint);
                i += mXLevel;
            }
        }
    }

    /**
     * 画Y轴
     *
     * @param canvas
     * @param paint
     */
    private void drawY(Canvas canvas, Paint paint) {
        int width = mWidth - mXOffset * 2;
        int height = mHeight - mYOffset * 2;
        float startX = mXOffset;
        float startY = mYOffset;
        float stopX = mXOffset;
        float stopY = mHeight - mYOffset;
        // 画Y坐标轴
        paint.reset();
        paint.setColor(mYColor);
        paint.setStrokeWidth(mYWidth);
        paint.setTextSize(5);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        // 画Y轴刻度
        if (mYLevel > 0) {
            for (float i = mMinY; i <= mMaxY; ) { // Y-AXIS
                float y = (mMaxY - i) * height / mYLevel + startY;
                canvas.drawLine(startX, y, startX + mYTextSize - 2, y, paint);
                canvas.drawText(String.format(Locale.getDefault(), "%1.0f", i), startX - mYTextSize - 2, y, paint);
                i += mYLevel;
            }
        }
    }

    /**
     * 画波形
     *
     * @param canvas
     */
    private void drawWave(Canvas canvas, Paint paint, LinkedList<Float> data) {
        // 添加当前的数据到队列中
        if (data.size() > mCount) {
            data.pollFirst();
        }
        data.add(tempSample);
        int width = mWidth - mXOffset * 2;
        int height = mHeight - mYOffset * 2;
        float startX = mXOffset;
        float startY = mHeight - mYOffset;
        float stopX = mWidth - mXOffset;
        float stopY = mHeight - mYOffset;
        paint.reset();
        paint.setColor(mWaveColor);
        paint.setStrokeWidth(mWaveWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(mPathEffect);

        int size = data.size();
        float all = 0;
        if (size > 0) {
            mPath.moveTo(startX, startY);
            for (int i = 0; i < size - 1; i++) {
                float y = data.get(i);
                float value = mMaxY - y;
                all += y;
                if (y >= mMinY && y <= mMaxY) {
                    Log.d(TAG, "drawWave i = " + i + ", y = " + y + ", value * height + mYOffset = " + value * height + mYOffset);
                    mPath.lineTo(1f * width / mCount * i + startX, value * height + mYOffset);
                }
            }
        }
        canvas.drawPath(mPath, paint);
        mPath.reset();

        // 画平均值
        paint.reset();
        paint.setColor(mYColor);
        paint.setStrokeWidth(mYWidth);
        paint.setTextSize(mYTextSize);
        float avg = all / size;
        if (avg > 0) {
            float y = (mMaxY - avg) * height + mYOffset;
            canvas.drawLine(startX, y, stopX, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "%1.1f", avg), startX - mYTextSize - 4, y,
                    paint);
        }
    }

    @Override
    public void onGlobalLayout() {
        mWidth = getWidth();
        mHeight = getHeight();
    }
}
