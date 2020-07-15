package com.hobot.sample.app.module.adas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * ADAS标定配置Camera位置的界面
 *
 * @author zhuo.chen
 * @version 2018-11-05
 */
public class AdasCameraPositionView extends View {

    private Paint mPaint;
    private Path mPath;
    private DashPathEffect mDashEffect;
    private int mXOffset = 66;
    private int mYOffset = 66;

    public AdasCameraPositionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPath = new Path();
        mDashEffect = new DashPathEffect(new float[]{15, 5}, 0);
    }

    /**
     * 设置上下边界线距离中心的高度
     *
     * @param x 暂时不用
     * @param y Y坐标距离
     */
    public void setOffset(int x, int y) {
        this.mXOffset = x;
        this.mYOffset = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centerY = getHeight() / 2;
        canvas.save();
        mPath.reset();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xFF27EBBA);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setPathEffect(mDashEffect);
        mPath.moveTo(0, centerY);
        mPath.lineTo(getWidth(), centerY);
        canvas.drawPath(mPath, mPaint);
        mPaint.reset();
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3);
        int offset = dpToPx(getContext(), mYOffset);
        canvas.drawLine(0, centerY + offset, getWidth(), centerY + offset, mPaint);
        canvas.drawLine(0, centerY - offset, getWidth(), centerY - offset, mPaint);
        canvas.restore();
    }

    public static int dpToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
