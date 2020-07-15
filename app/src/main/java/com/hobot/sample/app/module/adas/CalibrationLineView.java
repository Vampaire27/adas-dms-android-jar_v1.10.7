package com.hobot.sample.app.module.adas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

/**
 * 标定线控件
 * Created by lgw on 2016/11/29.
 */
public class CalibrationLineView extends View implements View.OnTouchListener {
    // region 静态成员变量
    private static final String TAG = "CalibrationLineView";
    // endregion 静态成员变量

    // region 私有成员变量
    // 画笔
    private Paint mPaint;
    // Handler
    // TODO: 2019/4/2 优化
    private Handler mHandler = new Handler();
    // 标定线的X轴
    private float mStartX;
    // 标定线的Y轴
    private float mStartY;
    // 标定线的X轴最小值
    private float mMinX;
    // 标定线的X轴最大值
    private float mMaxX;
    // 标定线的Y轴最小值
    private float mMinY;
    // 标定线的Y轴最大值
    private float mMaxY;
    // 控件的宽度
    private int mViewWidth;
    // 控件的高度
    private int mViewHeight;
    // X轴的缩放比例
    private float mRatioX;
    // Y轴的缩放比例
    private float mRatioY;
    // 上下文
    private Context mContext;
    private String mCalibrationHint;
    // 手势识别
    private GestureDetectorCompat mGestureDetectorCompat;
    // 滑动追踪
    private VelocityTracker mVelocityTracker;
    // endregion 私有成员变量

    // region 构造方法区
    public CalibrationLineView(@NonNull Context context) {
        this(context, null);

    }

    public CalibrationLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalibrationLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mCalibrationHint = "";
        mPaint = new Paint();
        mPaint.setColor(Color.YELLOW);
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(25);
        mPaint.setTextAlign(Paint.Align.CENTER);
        setOnTouchListener(this);
        mGestureDetectorCompat = new GestureDetectorCompat(mContext, new GestureListener());
    }
    // endregion 构造方法区

    // region 回调方法区
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mViewWidth = getWidth();
            mViewHeight = getHeight();
            mRatioX = mViewWidth / 1280f;
            mRatioY = mViewHeight / 720f;
            mStartX = mViewWidth / 2f;
            mStartY = 300 * mRatioY;
            mMinX = mStartX - 200 * mRatioX;
            mMaxX = mStartX + 200 * mRatioX;
            mMinY = mStartY - 200 * mRatioY;
            mMaxY = mStartY + 200 * mRatioY;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //竖线
        canvas.drawLine(mStartX, 0, mStartX, mViewHeight, mPaint);
        //横线
        canvas.drawLine(0, mStartY, mViewWidth, mStartY, mPaint);
        //文字
//        canvas.drawText(mCalibrationHint, mViewWidth / 2, mViewHeight - 100, mPaint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mVelocityTracker = VelocityTracker.obtain();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                mVelocityTracker.addMovement(event);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mVelocityTracker.clear();
                mVelocityTracker.recycle();
                break;
            }
            default:
                break;
        }
        return mGestureDetectorCompat.onTouchEvent(event);
    }
    // endregion 回调方法区

    // region 暴露方法区

    /**
     * 开始显示标定线
     */
    public void startCalibrationLine() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(colorChangeTask, 1000);
    }

    private Runnable colorChangeTask = new Runnable() {

        private int[] basicColors = new int[]{
                Color.parseColor("#FF0000"),
                Color.parseColor("#FFA500"),
                Color.parseColor("#FFFF00"),
                Color.parseColor("#00FF00"),
                Color.parseColor("#00FFFF"),
                Color.parseColor("#0000FF"),
                Color.parseColor("#800080"),
        };

        private int i = 0;

        @Override
        public void run() {
            if (i >= 7) {
                i = 0;
            }
            mPaint.setColor(basicColors[i++]);
            invalidate();
            mHandler.postDelayed(this, 500);
        }
    };

    /**
     * 获取标定线Y轴位置
     *
     * @return
     */
    public double getPointY() {
        // 因为算法图像是720*1280，所以此时要按比例还原回720*1280
        return (mStartY / mRatioY);
    }

    /**
     * 获取标定线X轴位置
     *
     * @return
     */
    public double getPointX() {
        // 因为算法图像是720*1280，所以此时要按比例还原回720*1280
        return (mStartX / mRatioX);
    }

    /**
     * 设置标定线X轴位置
     *
     * @param x
     * @return
     */
    public boolean setPointX(double x) {
        mStartX = (float) (x * mRatioX);
        // 请求重绘
        postInvalidate();
        return true;
    }

    /**
     * 设置标定线Y轴位置
     *
     * @param y
     * @return
     */
    public boolean setPointY(double y) {
        mStartY = (float) (y * mRatioY);
        // 请求重绘
        postInvalidate();
        return true;
    }

    /**
     * 标定线左移
     */
    public void left() {
        mStartX -= 1;
        if (mStartX < mMinX) {
            mStartX = mMinX;
        }
        invalidate();
    }

    /**
     * 标定线右移
     */
    public void right() {
        mStartX += 1;
        if (mStartX > mMaxX) {
            mStartX = mMaxX;
        }
        invalidate();
    }

    /**
     * * 标定线上移
     */
    public void up() {
        mStartY -= 1;
        if (mStartY < mMinY) {
            mStartY = mMinY;
        }
        invalidate();
    }

    /**
     * * 标定线下移
     */
    public void down() {
        mStartY += 1;
        if (mStartY > mMaxY) {
            mStartY = mMaxY;
        }
        invalidate();
    }

    @Override
    public String toString() {
        return "CalibrationLineView{" +
                "mStartX=" + mStartX +
                ", mStartY=" + mStartY +
                ", mMinX=" + mMinX +
                ", mMaxX=" + mMaxX +
                ", mMinY=" + mMinY +
                ", mMaxY=" + mMaxY +
                ", mViewWidth=" + mViewWidth +
                ", mViewHeight=" + mViewHeight +
                ", mRatioX=" + mRatioX +
                ", mRatioY=" + mRatioY +
                '}';
    }
    // endregion 暴露方法区

    // region 内部类定义区

    /**
     * 手势监听
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        //onDown必须拦截，不然无法触发onScroll
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        /**
         * 屏幕拖动事件，如果按下的时间过长，调用了onLongPress，再拖动屏幕不会触发onScroll。拖动屏幕会多次触发
         *
         * @param e1        开始拖动的第一次按下down操作,也就是第一个ACTION_DOWN
         * @param distanceX 当前的x坐标与最后一次触发scroll方法的x坐标的差值。
         * @param distanceY 当前的y坐标与最后一次触发scroll方法的y坐标的差值。
         * @parem e2 触发当前onScroll方法的ACTION_MOVE
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mStartX += distanceX * (-1);
            mStartY += distanceY * (-1);
            if (mStartX < mMinX) {
                mStartX = mMinX;
            }
            if (mStartX > mMaxX) {
                mStartX = mMaxX;
            }
            if (mStartY < mMinY) {
                mStartY = mMinY;
            }
            if (mStartY > mMaxY) {
                mStartY = mMaxY;
            }
            invalidate();
            return true;
        }
    }
    // endregion 内部类定义区
}
