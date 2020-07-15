package com.hobot.sample.app.module.adas;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 标定控件
 * Created by lgw on 2017/5/23.
 */
public class CalibrationImageView extends FrameLayout {
    // region 静态成员变量
    private static final String TAG = "CalibrationImageView";
    // endregion 静态成员变量

    // region 私有成员变量
    // 标定控件
    private ImageView mCalibrationImage;
    // 标定线
    private CalibrationLineView mCalibrationLineView;
    // 屏幕宽度
    private int mScreenWidth;
    // 控件宽度
    private int mViewWidth;
    // 控件高度
    private int mViewHeight;
    // endregion 私有成员变量

    // region 构造方法区
    public CalibrationImageView(@NonNull Context context) {
        this(context, null);
    }

    public CalibrationImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalibrationImageView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mCalibrationImage = new ImageView(context);
        mCalibrationImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mCalibrationLineView = new CalibrationLineView(context);
        addView(mCalibrationImage);
    }
    // endregion 构造方法区

    // region 回调方法区
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float ratio;
        // 只适配720*1280及以上分辨率的屏幕
        if (mScreenWidth > 1280) {
            ratio = 5 / 6.0f;
        } else {
            ratio = 1 / 2.0f;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //宽高中有任一个为wrap_content就按默认大小设置view
        if (widthMode == MeasureSpec.AT_MOST) {
            mViewWidth = Math.round(1280 * ratio);
        } else {
            mViewWidth = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            mViewHeight = Math.round(720 * ratio);
        } else {
            mViewHeight = heightSize;
        }
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mCalibrationImage.layout(0, 0, mViewWidth, mViewHeight);
        //mGridLineView.layout(0, 0, mViewWidth, mViewHeight);
        mCalibrationLineView.layout(0, 0, mViewWidth, mViewHeight);
    }
    // region 回调方法区

    // region 暴露方法区

    /**
     * 显示标定线
     */
    public void showCalLine() {
        if (getChildCount() > 1) {
            removeView(mCalibrationLineView);
        }
        addView(mCalibrationLineView);
//        mCalibrationLineView.startCalibrationLine();
    }

    /**
     * 隐藏标定线
     */
    public void hideCalLine() {
        if (getChildCount() > 1) {
            removeView(mCalibrationLineView);
        }
    }

    /**
     * 标定线左移
     */
    public void lineLeft() {
        mCalibrationLineView.left();
    }

    /**
     * 标定线右移
     */
    public void lineRight() {
        mCalibrationLineView.right();
    }

    /**
     * 标定线上移
     */
    public void lineUp() {
        mCalibrationLineView.up();
    }

    /**
     * 标定线下移
     */
    public void lineDown() {
        mCalibrationLineView.down();
    }

    /**
     * 获取标定线Y值
     *
     * @return
     */
    public double getPointY() {
        return mCalibrationLineView.getPointY();
    }

    /**
     * 获取标定线X值
     *
     * @return
     */
    public double getPointX() {
        return mCalibrationLineView.getPointX();
    }

    /**
     * 设置界面标定线X值
     *
     * @param pointX
     */
    public void setPointX(double pointX) {
        mCalibrationLineView.setPointX(pointX);
    }

    /**
     * 设置界面标定线Y值
     *
     * @param pointY
     */
    public void setPointY(double pointY) {
        mCalibrationLineView.setPointY(pointY);
    }
    // endregion 暴露方法区
}
