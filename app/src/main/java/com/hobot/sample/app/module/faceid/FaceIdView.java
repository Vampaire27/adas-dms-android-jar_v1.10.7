package com.hobot.sample.app.module.faceid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.hobot.camera.library.Camera2Manager;
import com.hobot.camera.library.base.ICameraAPI;
import com.hobot.camera.library.base.IPreviewCallback;
import com.hobot.camera.library.base.Option;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.dms.sdk.listener.IDmsFaceListener;
import com.hobot.dms.sdk.model.FaceInfo;
import com.hobot.sample.app.R;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.media.MediaPlayer;
import com.hobot.sample.app.module.base.BaseNoLayoutView;
import com.hobot.sdk.library.utils.StringUtils;
import com.hobot.xface.sdk.HobotXFaceSDK;
import com.hobot.xface.sdk.model.HobotXWHListSetResult;

/**
 * FaceId控件
 *
 * @author Hobot
 */
public class FaceIdView extends BaseNoLayoutView implements FaceIdProcessor.OnEventListener, IPreviewCallback, IDmsFaceListener {
    private static final String TAG = "FaceIdView";
    private static final String DEFAULT_DRIVER_NAME = "DEFAULT_DRIVER";
    private static final int DEFAULT_INTERVAL = 5*60*1000;//5 * 60 * 1000; // 5min
    private int checkCount = 0;
    private static final int  TOTAL_CHECK= 3;

    private FaceIdProcessor mFaceIdProcessor = new FaceIdProcessor();
    private String mDriverName;

    private LocationManager mLocationManager;
    private MediaPlayer mPlayer;


    public FaceIdView() {
        mCameraId = DefaultConfig.DEFAULT_FACE_ID_CAMERA_ID;
        mCameraType = DefaultConfig.DEFAULT_FACE_ID_CAMERA_TYPE;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        Log.d(TAG,"onViewCreated");
        mDriverName = DEFAULT_DRIVER_NAME + "_" + System.currentTimeMillis();
        mFaceIdProcessor.init(mContext)
                .clearAllUsers()
                .addEventListener(this)
                .setRecogEnable(false)
                .setRegisterEnable(true)
                .setRecogInterval(DEFAULT_INTERVAL)
                .registerFaceId(mDriverName);
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        mPlayer = new MediaPlayer(mContext);
    }

    @Override
    public void onViewRelease() {
        super.onViewRelease();
        mFaceIdProcessor.removeEventListener(this);
        mFaceIdProcessor.destroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        HobotDmsSdk.getInstance().registerFaceListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Camera2Manager.getInstance().close(mCamera);
        if (mCamera != null) {
            mCamera.unregisterPreviewCallback(this);
        }
        HobotDmsSdk.getInstance().unregisterFaceListener(this);
    }

    @Override
    public void onEvent(int code, int rst, String msg, Object reference) {
        Log.e(TAG, "onEvent code:"+code+" rst:"+rst);
        switch (code) {
            case FaceIdProcessor.CODE_RECOG_OPERATOR: {
                switch (rst) {
                    case FaceIdProcessor.RST_FAIL: {
                        Log.d(TAG, "人脸检测失败！");
                        if (mFaceIdProcessor != null) {
                            mFaceIdProcessor.activeRecog();
                        }
                        break;
                    }
                    case FaceIdProcessor.RST_DETECT_ERROR: {
                        Log.d(TAG, "人脸检测失败！");
                        if (mFaceIdProcessor != null) {
                            mFaceIdProcessor.activeRecog();
                        }
                        break;
                    }
                    case FaceIdProcessor.RST_EXTRACT_ERROR: {
                        Log.d(TAG, "特征提取失败！");
                        if (mFaceIdProcessor != null) {
                            mFaceIdProcessor.activeRecog();
                        }
                        break;
                    }
                    case FaceIdProcessor.RST_QUERY_ERROR: {
                        HobotXWHListSetResult setResult = HobotXFaceSDK.getInstance().listSet();
                        if(setResult == null || setResult.getSets() == null || setResult.getSets().length<=0){
                            return;
                        }
                        Log.d(TAG, "人脸查询失败！");
                        if(isNoSpeed()&& checkCount >= TOTAL_CHECK){
                            checkCount = 0;
                            showToast("不同的驾驶员！");
                            mFaceIdProcessor.clearAllUsers();
                            mFaceIdProcessor.setRecogEnable(false);
                            mFaceIdProcessor.setRegisterEnable(true);
                            mFaceIdProcessor.registerFaceId(mDriverName);;
                            mPlayer.play(R.raw.face);
                            return;
                        }
                        checkCount ++;
                        if (mFaceIdProcessor != null) {
                            mFaceIdProcessor.activeRecog();
                        }
                        break;
                    }
                    case FaceIdProcessor.RST_FACE_ANGEL_ERROR: {
                        Log.d(TAG, "人脸角度异常！");
                        if (mFaceIdProcessor != null) {
                            mFaceIdProcessor.activeRecog();
                        }
                        break;
                    }
                    case FaceIdProcessor.RST_MATCH_ERROR: {
                        if(isNoSpeed()){
                            checkCount = 0;
                            showToast("不同的驾驶员！");
                            mFaceIdProcessor.clearAllUsers();
                            mFaceIdProcessor.setRecogEnable(false);
                            mFaceIdProcessor.setRegisterEnable(true);
                            mFaceIdProcessor.registerFaceId(mDriverName);;
                            mPlayer.play(R.raw.face);
                        }
                        break;
                    }
                    case FaceIdProcessor.RST_SUCCESS: {
                        if (!StringUtils.isNullOrEmpty(mDriverName) && mDriverName.equals(msg)) {
                            checkCount = 0;
                            showToast("相同的驾驶员！");
                            break;
                        } else {
                            if(isNoSpeed()){
                                checkCount = 0;
                                showToast("不同的驾驶员！");
                                mFaceIdProcessor.clearAllUsers();
                                mFaceIdProcessor.setRecogEnable(false);
                                mFaceIdProcessor.setRegisterEnable(true);
                                mFaceIdProcessor.registerFaceId(mDriverName);;
                                mPlayer.play(R.raw.face);
                            }
                            break;
                        }
                    }
                }
                break;
            }
            case FaceIdProcessor.CODE_REGISTER_OPERATOR: {
                if (rst == FaceIdProcessor.RST_SUCCESS) {
                    showToast("注册成功！");
                    if (mFaceIdProcessor != null) {
                        // 开始识别
                        mFaceIdProcessor.setRecogEnable(true);
                        mFaceIdProcessor.setRegisterEnable(false);
                        // 重置识别时间
                        mFaceIdProcessor.resetRecog();
                    }
                } else {
                    // 注册失败，主动再注册一次
                    if (mFaceIdProcessor != null) {
                        mFaceIdProcessor.activeRegister();
                    }
                }
                break;
            }
        }
    }

    private boolean isNoSpeed() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            float speed = location.getSpeed();
            if(speed > 5) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onFrame(int camera, byte[] data, long timestamp, Option option) {
        if(DefaultConfig.SUPPORT_FACE_ID){
            int width = option.previewWidth;
            int height = option.previewHeight;
            mFaceIdProcessor.processImage(data, width, height, option.format, timestamp);
        }
    }

    private long noFaceTime = -1;
    private static final long DAA_TIME = 5*1000;

    @Override
    public void onDmsFace(FaceInfo faceInfo) {
//        Log.d(TAG,"onDmsFace faceInfo = "+ faceInfo);
        mFaceIdProcessor.onDmsFace(faceInfo);
        if(DefaultConfig.SUPPORT_FACE_ID && isNoSpeed()){
            if(faceInfo.getRoi() == null|| !faceInfo.getRoi().hasFaceRoi()){
                if(noFaceTime == -1 ){
                    noFaceTime = System.currentTimeMillis();
//                    Log.d(TAG,"onDmsFace noFaceTime = "+ noFaceTime);
                }
            }else{
                long time = System.currentTimeMillis();
//                Log.d(TAG,"onDmsFace noFaceTime = "+ noFaceTime +";time = "+ time);
                if(noFaceTime >0 && time - noFaceTime > DAA_TIME){
//                    mPlayer.play(R.raw.back);
                    // 开始识别
                    mFaceIdProcessor.setRecogEnable(true);
                    mFaceIdProcessor.setRegisterEnable(false);
                    mFaceIdProcessor.activeRecog();
                }
                noFaceTime = -1;
            }
        }
    }
}
