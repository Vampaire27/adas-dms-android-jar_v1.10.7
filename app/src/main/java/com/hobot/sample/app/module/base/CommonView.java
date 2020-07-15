package com.hobot.sample.app.module.base;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.hobot.camera.library.base.ICameraAPI;
import com.hobot.camera.library.base.IPreviewCallback;
import com.hobot.camera.library.base.Option;
import com.hobot.sample.app.activity.BaseActivity;
import com.hobot.sample.app.config.Constants;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.listener.ITestModeListener;
import com.hobot.sample.app.util.CameraHelper;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.util.GpsManager;

/**
 * 控件基类。
 *
 * @author Hobot
 */
public class CommonView implements ICommonView, ITestModeListener, IPreviewCallback, LocationListener {
    private static final int INVALID_CAMERA_PARAMS = -1;
    // 上下文
    protected Context mContext;
    protected ICameraAPI mCamera;

    protected int mCameraType = INVALID_CAMERA_PARAMS;
    protected int mCameraId = INVALID_CAMERA_PARAMS;
    protected boolean mTestMode;
    protected float mFakeSpeed = -1; // 默认假速度

    /**
     * 日志TAG。
     *
     * @return 日志TAG
     */
    public String TAG() {
        return "Common_" + this.getClass().getSimpleName();
    }

    @CallSuper
    @Override
    public View onCreateView(Context context) {
        mContext = context;
        GpsManager.getInstance().registerListener(this);
        initCamera();
        NebulaObservableManager.getInstance().registerTestModeListener(this);
        return null;
    }

    @CallSuper
    @Override
    public void onDestroyView() {
        releaseCamera();
        GpsManager.getInstance().unregisterListener(this);
        NebulaObservableManager.getInstance().unregisterTestModeListener(this);
    }

    @CallSuper
    @Override
    public void onStart() {
        startCamera();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @CallSuper
    @Override
    public void onStop() {
        stopCamera();
    }

    /**
     * 初始化配置文件。
     *
     * @return {@link Option}
     */
    protected Option initOption() {
        Option option = new Option.Builder()
                .format(DefaultConfig.CAMERA_FORMAT)
                .previewHeight(DefaultConfig.CAMERA_INPUT_HEIGHT)
                .previewWidth(DefaultConfig.CAMERA_INPUT_WIDTH)
                .build();
        return option;
    }

    /**
     * 初始化Camera绘制控制，默认使用SurfaceTexture进行绘制。
     *
     * @return 绘制界面
     */
    protected SurfaceTexture initSurfaceTexture() {
        return new SurfaceTexture(10);
    }

    /**
     * 初始化Camera绘制控制，默认不使用SurfaceView进行绘制。
     *
     * @return 绘制界面
     */
    protected SurfaceView initSurfaceView() {
        return null;
    }

    /**
     * 初始化Camera
     */
    protected final void initCamera() {
        Log.d(TAG(), "initCamera -- mCameraId = " + mCameraId + ", mCameraType = " + mCameraType);
        if (mCameraId != INVALID_CAMERA_PARAMS && mCameraType != INVALID_CAMERA_PARAMS) {
            mCamera = CameraHelper
                    // 1.创建Camera示例。
                    .createCamera(mCameraId, mCameraType)
                    // 2.设置绘制界面
                    .target(initSurfaceTexture())
                    // 3.设置绘制控件
                    .target(initSurfaceView())
                    // 4.设置配置
                    .options(initOption());
//            mCamera.setFakeEnable(DefaultConfig.ENABLE_SERVICE);
        }
    }

    /**
     * 释放Camera
     */
    protected final void releaseCamera() {
        Log.d(TAG(), "releaseCamera -- mCamera = " + mCamera);
        if (mCamera != null) {
            CameraHelper.destroyCamera(mCameraType, mCameraId);
        }
    }

    /**
     * 打开Camera
     */
    protected final void startCamera() {
        Log.d(TAG(), "startCamera -- mCamera = " + mCamera);
        if (mCamera != null) {
            CameraHelper.openCamera(mCamera);
            mCamera.registerPreviewCallback(this);
        }
    }

    /**
     * 关闭Camera
     */
    protected final void stopCamera() {
        Log.d(TAG(), "stopCamera -- mCamera = " + mCamera);
        if (mCamera != null) {
            CameraHelper.closeCamera(mCamera);
            mCamera.unregisterPreviewCallback(this);
        }
    }

    /**
     * 显示Toast。
     *
     * @param text 显示内容
     */
    protected final void showToast(final String text) {
        if (mContext instanceof BaseActivity) {
            ((BaseActivity) mContext).showToast(text);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (!TextUtils.isEmpty(provider) && LocationManager.GPS_PROVIDER.equals(provider)) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    break;
                }
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onFrame(int i, byte[] bytes, long l, Option option) {

    }

    @Override
    public void onTestModeChanged(boolean isEnable) {
        mTestMode = isEnable;
    }

    /**
     * 假速度改变
     *
     * @param speed 速度 km/h
     */
    @Override
    public void onFakeSpeedChanged(float speed) {
        Log.d(TAG(), "onFakeSpeedChanged() called with: speed = [" + speed + "]");
        mFakeSpeed = speed;
    }
}
