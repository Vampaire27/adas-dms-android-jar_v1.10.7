package com.hobot.sample.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * 中间矩形透明的View
 */
public class TransInsideView extends View {
    private int mRectWidth = dip2px(300);   // 矩形宽度
    private int mRectHeight = dip2px(200);  // 矩形高度
    private int mBoundColor = 0x6e272727;   // 周围颜色
    private int mRectColor = 0x6effffff;// 矩形颜色默认透明

    private int mCenterX = -1;  // 中心点X坐标
    private int mCenterY = -1;  // 中心点Y坐标

    private int mWidth = -1;    // View宽度
    private int mHeight = -1;   // View高度

    private Xfermode mXfermode;
    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.DST_OUT;
    private Paint mPaint = new Paint();

    public TransInsideView(Context context) {
        this(context, null);
    }

    public TransInsideView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransInsideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
        initView();
        initListener();
    }

    private void initData() {
        mXfermode = new PorterDuffXfermode(mPorterDuffMode);

    }

    private void initView() {

    }

    private void initListener() {

    }

    /**
     * 设置矩形框的宽高
     *
     * @param width
     * @param height
     */
    public void setWH(int width, int height) {
        this.mRectWidth = dip2px(width);
        this.mRectHeight = dip2px(height);
        postInvalidate();
    }

    /**
     * 设置周围颜色
     *
     * @param color argb
     */
    public void setColor(@ColorInt int color) {
        this.mBoundColor = color;
    }


    public int dip2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (-1 == mWidth || -1 == mHeight) {
            return;
        }
        int sc = canvas.saveLayer(0, 0, mWidth, mHeight, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBoundColor);

        // DST
        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);

        mPaint.setColor(mRectColor);
        mPaint.setXfermode(mXfermode);
        // SRC
        int left = mCenterX - mRectWidth / 2;
        int top = mCenterY - mRectHeight / 2;
        int right = mCenterX + mRectWidth / 2;
        int bottom = mCenterY + mRectHeight / 2;
        int length = dip2px(20);
        canvas.drawRect(left, top,
                right, bottom, mPaint);
        //设置混合模式
        //还原画布
        mPaint.setXfermode(null);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(0xbb008cd6);
        canvas.drawLine(left, top, left + length, top, mPaint);
        canvas.drawLine(left, top, left, top + length, mPaint);
        canvas.drawLine(right, top, right - length, top, mPaint);
        canvas.drawLine(right, top, right, top + length, mPaint);
        canvas.drawLine(left, bottom, left + length, bottom, mPaint);
        canvas.drawLine(left, bottom, left, bottom - length, mPaint);
        canvas.drawLine(right, bottom, right - length, bottom, mPaint);
        canvas.drawLine(right, bottom, right, bottom - length, mPaint);
        canvas.restoreToCount(sc);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mCenterX = w / 2;
        mCenterY = h / 2;
    }
}
