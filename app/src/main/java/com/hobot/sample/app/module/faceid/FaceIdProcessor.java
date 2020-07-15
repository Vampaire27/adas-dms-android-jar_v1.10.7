package com.hobot.sample.app.module.faceid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hobot.dms.sdk.model.FaceInfo;
import com.hobot.sdk.library.utils.BitmapUtils;
import com.hobot.sdk.library.utils.StringUtils;
import com.hobot.xface.sdk.HobotXFaceSDK;
import com.hobot.xface.sdk.model.ErrorCode;
import com.hobot.xface.sdk.model.HobotXFaceFeature;
import com.hobot.xface.sdk.model.HobotXFaceImage;
import com.hobot.xface.sdk.model.HobotXFaceImageFeatures;
import com.hobot.xface.sdk.model.HobotXFaceImgType;
import com.hobot.xface.sdk.model.HobotXFaceMode;
import com.hobot.xface.sdk.model.HobotXFacePose;
import com.hobot.xface.sdk.model.HobotXWHCompareResult;
import com.hobot.xface.sdk.model.HobotXWHFeature;
import com.hobot.xface.sdk.model.HobotXWHIdScore;
import com.hobot.xface.sdk.model.HobotXWHRecord;
import com.hobot.xface.sdk.model.HobotXWHSearchParam;
import com.hobot.xface.sdk.model.HobotXWHSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FACE ID相关业务处理
 *
 * @author Hobot
 */
public class FaceIdProcessor {
    // region 静态变量区
    public static final String TAG = "FaceIdProcessor";
    public static final int CODE_RECOG_OPERATOR = 0x411;
    public static final int CODE_REGISTER_OPERATOR = 0x412;
    // 人脸查询或注册失败
    public static final int RST_FAIL = -1;
    // 人脸检测错误
    public static final int RST_DETECT_ERROR = -3;
    // 人脸特征错误
    public static final int RST_EXTRACT_ERROR = -4;
    // 人脸查询错误
    public static final int RST_QUERY_ERROR = -5;
    // 人脸比对错误
    public static final int RST_MATCH_ERROR = -6;
    // 人脸角度异常
    public static final int RST_FACE_ANGEL_ERROR = -7;
    // 人脸查询成功
    public static final int RST_SUCCESS = 1;
    // 最大ROLL角度
    public static final double MAX_ANGEL_ROLL = 10*3.1415926/180;
    // 最大PITCH角度
    public static final double MAX_ANGEL_PITCH = 10*3.1415926/180;
    // 最大YAW角度
    public static final double MAX_ANGEL_YAW = 15*3.1415926/180;
    private static final int CODE_RECOG_STRATEGY = 0x401;
    private static final int CODE_REGISTER_STRATEGY = 0x402;
    private static final int CODE_CLEAR_ALL_USERS = 0x403;
    // 不同人的最大差距
    private static final int MAX_DISTANCE = 120;
    // endregion 静态变量区

    // region 私有成员区
    // 回调监听
    private final List<OnEventListener> mListeners = new ArrayList<>();
    // 是否初始化
    private final AtomicBoolean mIsInit = new AtomicBoolean(false);
    // 识别
    private RecogStrategy mQueryStrategy;
    // 注册
    private RegisterStrategy mRegisterStrategy;
    // Handler子线程
    private HandlerThread mHandlerThread;
    // Handler处理
    private Handler mHandler;
    // 下一帧是否要注册标志位
    private AtomicBoolean mAtomicRegisterFlag = new AtomicBoolean(false);
    // 下一帧是否要识别标志位
    private AtomicBoolean mAtomicRecogFlag = new AtomicBoolean(false);
    // 下一帧是否需要遮挡检测标志位
    private AtomicBoolean mAtomicCheckDAAFlag = new AtomicBoolean(false);
    // 临时名字存放容易
    private AtomicReference<String> mAtomicTempName = new AtomicReference<>("");
    // 是否可用识别
    private boolean mRecogEnable = false;
    // 是否可以注册
    private boolean mRegisterEnable = false;

    private final static String DRIVER_CHANGE_SET = "driver_change_set";
    // endregion 私有成员区

    // region 暴露方法区域
    /**
     * 初始化
     *
     * @return 实例
     */
    public FaceIdProcessor init(Context context) {
        synchronized (mIsInit) {
            if (!mIsInit.compareAndSet(false, true)) {
                Log.w(TAG, "init: FaceIdProcessor is already init.");
                return this;
            }
            int ret = HobotXFaceSDK.getInstance().init(context,"/sdcard/hobot/license");
            Log.d(TAG, "init: ret = "+ ret);
            mQueryStrategy = new RecogStrategy();
            mRegisterStrategy = new RegisterStrategy();
            mHandlerThread = new HandlerThread(TAG + "-" + System.currentTimeMillis());
            mHandlerThread.start();
            mHandler = new FaceIdHandler(mHandlerThread.getLooper());
        }
        return this;
    }

    /**
     * 反初始化
     *
     * @return 实例
     */
    public FaceIdProcessor destroy() {
        synchronized (mIsInit) {
            if (!mIsInit.compareAndSet(true, false)) {
                Log.w(TAG, "destroy: FaceIdProcessor is not init.");
                return this;
            }
            mHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
            mListeners.clear();
            return this;
        }
    }

    /**
     * 注册脸部
     *
     * @param name id
     * @return 实例
     */
    public FaceIdProcessor registerFaceId(String name) {
        if (!mIsInit.get()) {
            Log.w(TAG, "registerFaceId: FaceIdProcessor is not init.");
            return this;
        }
        mAtomicTempName.compareAndSet("", name);
        mRegisterStrategy.activeRegister();
        return this;
    }

    /**
     * 主动识别
     *
     * @return 实例
     */
    public FaceIdProcessor activeRecog() {
        if (!mIsInit.get()) {
            Log.w(TAG, "activeRecog: FaceIdProcessor is not init.");
            return this;
        }
        mQueryStrategy.activeRecog();
        return this;
    }

    /**
     * 重置识别
     *
     * @return 实例
     */
    public FaceIdProcessor resetRecog() {
        if (!mIsInit.get()) {
            Log.d(TAG, "resetRecog: FaceIdProcessor is not init.");
            return this;
        }
        mQueryStrategy.reset();
        return this;
    }

    /**
     * 主动注册
     *
     * @return 实例
     */
    public FaceIdProcessor activeRegister() {
        if (!mIsInit.get()) {
            Log.d(TAG, "activeRegister: FaceIdProcessor is not init.");
            return this;
        }
        mRegisterStrategy.activeRegister();
        return this;
    }

    /**
     * 注册事件回调监听
     *
     * @param listener 监听
     * @return 实例
     */
    public FaceIdProcessor addEventListener(OnEventListener listener) {
        if (!mIsInit.get()) {
//            throw new IllegalStateException("FaceIdProcessor is not init.");
            Log.d(TAG, "addEventListener: FaceIdProcessor is not init.");
            return this;
        }
        synchronized (mListeners) {
            if (mListeners.contains(listener)) {
                Log.w(TAG, "registerEventListener: not repeat register!");
                return this;
            }
            mListeners.add(listener);
        }
        return this;
    }

    /**
     * 移除监听
     *
     * @param listener 监听
     * @return 实例
     */
    public FaceIdProcessor removeEventListener(OnEventListener listener) {
        if (!mIsInit.get()) {
//            throw new IllegalStateException("FaceIdProcessor is not init.");
            Log.d(TAG, "removeEventListener: FaceIdProcessor is not init.");
            return this;
        }
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                Log.w(TAG, "unregisterEventListener: not register listener!");
                return this;
            }
            mListeners.remove(listener);
        }
        return this;
    }

    /**
     * 喂数据
     *
     * @param data      图像数据
     * @param width     图像的宽（参考值：1280）
     * @param height    图像的高（参考值：720）
     * @param colorMode 图像的颜色样式（参考值：0或1）
     * @param timestamp 图像的时间戳
     */
    public void processImage(byte[] data, int width, int height, int colorMode, long timestamp) {
        HobotXFaceImage image = new HobotXFaceImage();
        image.setWidth(width);
        image.setHeight(height);
        image.setPredictMode(HobotXFaceMode.PREDICT_MODE_METRIC);
        image.setMaxFaceCount(1);
        image.setImgType(HobotXFaceImgType.IMG_TYPE_YV12);
        image.setData(data);
        if (data == null) {
            return;
        }

        if (!mAtomicRecogFlag.get() && !mAtomicRegisterFlag.get() && !mAtomicCheckDAAFlag.get()) {  // 识别 or 注册
            // 当前帧不需要识别或注册，跳过当前帧。
            return;
        }

        // 识别操作
        if (mRecogEnable && mAtomicRecogFlag.getAndSet(false)) {
            Log.d(TAG, "processImage: do recog");
            mHandler.removeMessages(CODE_RECOG_OPERATOR);
            mHandler.obtainMessage(CODE_RECOG_OPERATOR, image).sendToTarget();
        }
        // 注册操作
        if (mRegisterEnable && mAtomicRegisterFlag.getAndSet(false)) {
            Log.d(TAG, "processImage: do register");
            mHandler.removeMessages(CODE_REGISTER_OPERATOR);
            mHandler.obtainMessage(CODE_REGISTER_OPERATOR, image).sendToTarget();
        }
    }

    /**
     * 设置识别间隔
     *
     * @param interval 间隔
     * @return 实例
     */
    public FaceIdProcessor setRecogInterval(int interval) {
        if (!mIsInit.get()) {
//            throw new IllegalStateException("FaceIdProcessor is not init.");
            Log.d(TAG, "setRecogInterval: FaceIdProcessor is not init.");
            return this;
        }
        Log.d(TAG, "setRecogInterval interval = " + interval);
        mQueryStrategy.setInterval(interval);
        return this;
    }

    /**
     * 设置是否可以进行识别。
     *
     * @param recogEnable 状态
     * @return 实例
     */
    public FaceIdProcessor setRecogEnable(boolean recogEnable) {
        mRecogEnable = recogEnable;
        return this;
    }

    /**
     * 设置是否可以进行注册。
     *
     * @param registerEnable 状态
     * @return 实例
     */
    public FaceIdProcessor setRegisterEnable(boolean registerEnable) {
        mRegisterEnable = registerEnable;
        return this;
    }

    /**
     * 清空所有用户
     *
     * @return 实例
     */
    public FaceIdProcessor clearAllUsers() {
        mHandler.sendEmptyMessage(CODE_CLEAR_ALL_USERS);
        return this;
    }
    // endregion 暴露方法区域

    // region 接口回调区
    public void onDmsFace(FaceInfo faceInfo) {
        if (!mIsInit.get()) {
            Log.w(TAG, "onDmsFace: FaceIdProcessor is not init.");
        }
        if (null == mHandler) {
            Log.w(TAG, "onDmsFace: mHandler is null");
            return;
        }

        // 注册策略
        if (mRegisterEnable) {
            mHandler.obtainMessage(CODE_REGISTER_STRATEGY, faceInfo).sendToTarget();
        }

        // 识别策略,每一帧都要过策略
        if (mRecogEnable) {
            mHandler.obtainMessage(CODE_RECOG_STRATEGY, faceInfo).sendToTarget();
        }
    }
    // endregion 接口回调区

    // region 内部类

    /**
     * 处理事件
     *
     * @param what
     * @param rst
     */
    private void handleEvent(int what, int rst) {
        handleEvent(what, rst, "", null);
    }
    // endregion 内部类

    // region 私有方法区域

    /**
     * 处理事件
     *
     * @param what
     * @param rst
     */
    private void handleEvent(int what, int rst, String msg) {
        handleEvent(what, rst, msg, null);
    }

    /**
     * 处理事件
     *
     * @param what
     * @param rst
     * @param msg
     * @param reference
     */
    private void handleEvent(int what, int rst, String msg, Object reference) {
        Log.d(TAG, "handleEvent what = " + what + ", rst = " + rst);
        synchronized (mListeners) {
            for (OnEventListener listener : mListeners) {
                listener.onEvent(what, rst, msg, reference);
            }
        }
    }

    private void compare(){

        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/1.jpg");
        byte[] mByte = BitmapUtils.bitmap2yuv(bitmap);
        Log.d(TAG, "mByte= "+ mByte);
        HobotXFaceImage image = new HobotXFaceImage();
        image.setWidth(bitmap.getWidth());
        image.setHeight(bitmap.getHeight());
        image.setPredictMode(HobotXFaceMode.PREDICT_MODE_METRIC);
        image.setMaxFaceCount(1);
        image.setImgType(HobotXFaceImgType.IMG_TYPE_YV12);
        image.setData(mByte);

        Bitmap bitmap2 = BitmapFactory.decodeFile("/sdcard/2.jpg");
        byte[] mByte2 = BitmapUtils.bitmap2yuv(bitmap2);
        Log.d(TAG, "mByte2= "+ mByte2);
        HobotXFaceImage image2 = new HobotXFaceImage();
        image2.setWidth(bitmap2.getWidth());
        image2.setHeight(bitmap2.getHeight());
        image2.setPredictMode(HobotXFaceMode.PREDICT_MODE_METRIC);
        image2.setMaxFaceCount(1);
        image2.setImgType(HobotXFaceImgType.IMG_TYPE_YV12);
        image2.setData(mByte2);

        HobotXFaceImageFeatures features = HobotXFaceSDK.getInstance().extractFeature(image);
        if (null == features || features.getFeatures() == null||features.getFeatures().length <=0) {
            Log.e(TAG,"compare extractFeature 1 failed");
            return;
        }
        HobotXFaceFeature feature = features.getFeatures()[0];
        if(feature == null){
            Log.e(TAG,"compare getFeatures 1 failed");
            return;
        }
        HobotXFacePose pose = feature.getFacePose();
        if(pose == null){
            Log.e(TAG,"compare getFacePose 1 failed");
            return;
        }
        float pich = pose.getPitch();
        float roll = pose.getRoll();
        float yaw = pose.getYaw();
        Log.e(TAG, "FaceIdHandler pich = "+ pich +";roll = "+ roll +";yaw = "+ yaw);
//        if(Math.abs(pich) > MAX_ANGEL_PITCH || Math.abs(roll)>MAX_ANGEL_ROLL||Math.abs(yaw)>MAX_ANGEL_YAW){
//            handleEvent(msg.what, RST_FACE_ANGEL_ERROR,null,image,0);
//            return;
//        }
        HobotXWHCompareResult result = HobotXFaceSDK.getInstance().compare1V1(image,image2);
        Log.e(TAG, "result isMatch= "+ result.isMatch()+";getDistance = "+ result.getDistance()+"; getSimilar= "+ result.getSimilar());
    }

    /**
     * 事件回调接口
     */
    public interface OnEventListener {
        void onEvent(int code, int rst, String msg, Object reference);
    }
    // endregion 私有方法区域

    // region 内部类区域

    /**
     * FaceIdHandler to do NO-UI TASK
     */
    private class FaceIdHandler extends Handler {
        private FaceIdHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_RECOG_OPERATOR: {
                    // 人脸识别
                    HobotXFaceImage image = (HobotXFaceImage) msg.obj;
                    HobotXFaceImageFeatures features = HobotXFaceSDK.getInstance().extractFeature(image);
                    if (null == features || features.getFeatures() == null||features.getFeatures().length <=0) {
                        handleEvent(msg.what, RST_DETECT_ERROR);
                        return;
                    }

                    HobotXFaceFeature feature = features.getFeatures()[0];
                    if(feature == null){
                        handleEvent(msg.what, RST_DETECT_ERROR,null,image);
                        return;
                    }
                    HobotXFacePose pose = feature.getFacePose();
                    if(pose == null){
                        handleEvent(msg.what, RST_FACE_ANGEL_ERROR,null,image);
                        return;
                    }
                    float pich = pose.getPitch();
                    float roll = pose.getRoll();
                    float yaw = pose.getYaw();
                    if(Math.abs(pich) > 15 || Math.abs(roll)>15||Math.abs(yaw)>20){
                        Log.e(TAG, "FaceIdHandler pich = "+ pich +";roll = "+ roll +";yaw = "+ yaw);
                        handleEvent(msg.what, RST_FACE_ANGEL_ERROR,null,image);
                        return;
                    }
                    HobotXWHSearchResult search = HobotXFaceSDK.getInstance().search(DRIVER_CHANGE_SET,feature.getMetric());
                    if(search != null && search.getIdScores() != null) {
                        Log.d(TAG,"search: match " + search.isMatch());
                        HobotXWHIdScore[] scores = search.getIdScores();
                        for(int i = 0; i < scores.length; i++) {
                            Log.d(TAG,"search: score " + i + " getId:" + scores[i].getId());
                            Log.d(TAG,"search: score " + i + " getDistance:" + scores[i].getDistance());
                            Log.d(TAG,"search: score " + i + " getSimilar:" + scores[i].getSimilar());
                        }
                        if(scores!= null && search.isMatch()){
                            // Listener回调
                            handleEvent(msg.what, RST_SUCCESS, scores[0].getId());
                        }else{
                            handleEvent(msg.what, RST_QUERY_ERROR);
                        }
                    }else{
                        handleEvent(msg.what, RST_QUERY_ERROR);
                    }
                    break;
                }
                case CODE_RECOG_STRATEGY: {
                    if (mQueryStrategy.process((FaceInfo) msg.obj)) {
                        // 策略通过 做识别
                        Log.w(TAG, "handleMessage: need query onetime");
                        mAtomicRecogFlag.compareAndSet(false, true);
                    }
                    break;
                }
                case CODE_REGISTER_OPERATOR: {
                    String name = mAtomicTempName.get();
                    if (StringUtils.isNullOrEmpty(name)) {
                        Log.e(TAG, "CODE_REGISTER_OPERATOR -- name is null.");
                        handleEvent(msg.what, RST_FAIL);
                        return;
                    }

                    HobotXFaceImage image = (HobotXFaceImage) msg.obj;
                    HobotXFaceImageFeatures features = HobotXFaceSDK.getInstance().extractFeature(image);
                    if (null == features || features.getFeatures() == null||features.getFeatures().length <=0) {
                        handleEvent(msg.what, RST_DETECT_ERROR);
                        return;
                    }

                    HobotXFaceFeature feature = features.getFeatures()[0];
                    if(feature == null){
                        handleEvent(msg.what, RST_DETECT_ERROR,null,image);
                        return;
                    }
                    HobotXFacePose pose = feature.getFacePose();
                    if(pose == null){
                        handleEvent(msg.what, RST_FACE_ANGEL_ERROR,null,image);
                        return;
                    }
                    float pich = pose.getPitch();
                    float roll = pose.getRoll();
                    float yaw = pose.getYaw();
                    Log.e(TAG, "FaceIdHandler pich = "+ pich +";roll = "+ roll +";yaw = "+ yaw);
                    if(Math.abs(pich) > 15 || Math.abs(roll)>15||Math.abs(yaw)>20){
                        handleEvent(msg.what, RST_FACE_ANGEL_ERROR,null,image);
                        return;
                    }

                    int ret = HobotXFaceSDK.getInstance().dropSet(DRIVER_CHANGE_SET);
                    Log.d(TAG,"dropSet ret = "+ ret);
                    ret = HobotXFaceSDK.getInstance().createSet(DRIVER_CHANGE_SET);
                    Log.d(TAG,"dropSet createSet = "+ ret);
                    HobotXWHRecord recoder = new HobotXWHRecord();
                    HobotXWHFeature XWHfeature = new HobotXWHFeature();
                    XWHfeature.setFeatureId(name);
                    XWHfeature.setFeature(feature.getMetric());
                    recoder.setId(name);
                    recoder.setFeatures(new HobotXWHFeature[]{XWHfeature});
                    ret = HobotXFaceSDK.getInstance().addRecord(DRIVER_CHANGE_SET,recoder);
                    Log.d(TAG,"dropSet addRecord = "+ ret);

                    if(ret== ErrorCode.SUCCESS){
                        handleEvent(msg.what, RST_SUCCESS, name);
                    }else{
                        Log.e(TAG, "CODE_REGISTER_OPERATOR -- register error.ret = "+ ret);
                        handleEvent(msg.what, RST_FAIL);
                    }
                    mAtomicTempName.set("");
                    break;
                }
                case CODE_REGISTER_STRATEGY: {
                    if (mRegisterStrategy.process((FaceInfo) msg.obj)) {
                        // 策略通过 做识别
                        Log.w(TAG, "handleMessage: need register onetime");
                        mAtomicRegisterFlag.compareAndSet(false, true);
                    }
                    break;
                }
                case CODE_CLEAR_ALL_USERS: {
                    HobotXFaceSDK.getInstance().dropSet(DRIVER_CHANGE_SET);

                    compare();
                    break;
                }
            }
        }
    }
    // endregion 内部类区域
}

