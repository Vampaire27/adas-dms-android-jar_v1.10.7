package com.hobot.sample.app.module.quality;

import android.content.Context;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.nebula.common.model.EventTypeInfo;
import com.hobot.nebula.common.module.base.WarningEventType;
import com.hobot.quality.sdk.HobotQualitySDK;
import com.hobot.sdk.library.jni.input.HobotImage;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 图像质量管理类。
 *
 * @author Hobot
 */
public class QualityProcessor {
    // region 静态变量区
    private static final String TAG = "QualityProcessor";
    private static final boolean DEBUG = false;
    // DOA检测间隔最大3秒。
    private static final int MAX_CHECK_DOA_TIME = 100;
    private static final int CODE_CHECK_DAA = 0x404;
    private static final int CODE_CHECK_DOA = 0x405;

    // 连续被遮挡帧数
    private static final int FLAG_COUNT_OCC = 5;
    // endregion 静态变量区

    // region 私有成员区
    // 是否初始化
    private final AtomicBoolean mIsInit = new AtomicBoolean(false);
    // 遮挡的FLAG，用来最终确认是否是被遮挡,默认是被遮挡了
    private final AtomicInteger mAtomicIsOcc = new AtomicInteger(0);
    // 是否可以DAA检测
    private boolean mCheckDAAEnable = false;
    // 下一帧是否需要遮挡检测标志位
    private final AtomicBoolean mAtomicCheckDAAFlag = new AtomicBoolean(false);
    // Handler子线程
    private HandlerThread mHandlerThread;
    // Handler处理
    private Handler mHandler;
    // 图像路径
    private String mFilePath;
    // endregion 私有成员区

    // region 暴露方法区域

    /**
     * 初始化
     *
     * @return 实例
     */
    public QualityProcessor init(Context context) {
        synchronized (mIsInit) {
            if (!mIsInit.compareAndSet(false, true)) {
                Log.w(TAG, "init: QualityProcessor is already init.");
                return this;
            }
            mHandlerThread = new HandlerThread(TAG + "-" + System.currentTimeMillis());
            mHandlerThread.start();
            mHandler = new QualityHandler(mHandlerThread.getLooper());
        }
        return this;
    }

    /**
     * 反初始化
     *
     * @return 实例
     */
    public QualityProcessor destroy() {
        synchronized (mIsInit) {
            if (!mIsInit.compareAndSet(true, false)) {
                Log.w(TAG, "destroy: QualityProcessor is not init.");
                return this;
            }
            mHandler.removeCallbacksAndMessages(null);
            mHandlerThread.quit();
            return this;
        }
    }

    /**
     * 主动遮挡检测描述
     * 当Native发出 DAA事件，主动开始持续3s的DAA检测
     *
     * @return 实例
     */
    public QualityProcessor activeCheckShelter(String filePath) {
        this.mFilePath = filePath;
        activeCheckShelter(true);
        return this;
    }

    /**
     * 主动遮挡检测描述
     * 当Native发出 DAA事件，主动开始持续3s的DAA检测Z
     *
     * @param isFromDAA 是否由DAA事件触发遮挡
     */
    public void activeCheckShelter(boolean isFromDAA) {
        Log.d(TAG, "activeCheckShelter() called with: isFromDAA = [" + isFromDAA + "]");
        synchronized (mAtomicCheckDAAFlag) {
            // 只有当前未在做DAA/DOA 检测才会主动检测
            if (mAtomicCheckDAAFlag.compareAndSet(false, true)) {
                // OCC判断Flag设置为默认值 默认是未被遮挡的
                mAtomicIsOcc.set(0);
                // DAA 检测开始,在processImage会触发处理
                mAtomicCheckDAAFlag.set(true);
                mHandler.removeMessages(CODE_CHECK_DOA);
                // 3s后做 DAA检测结果判断,判断是报DAA_NOFACE 还是DAA_OCC
                Message msg = mHandler.obtainMessage(CODE_CHECK_DOA);
                msg.obj = isFromDAA;
                mHandler.sendMessageDelayed(msg, MAX_CHECK_DOA_TIME);
            }
        }
    }

    /**
     * 设置是否可以进行DAA识别。
     *
     * @param checkDAAEnable 状态
     * @return 实例
     */
    public QualityProcessor setCheckDAAEnable(boolean checkDAAEnable) {
        mCheckDAAEnable = checkDAAEnable;
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
        if (!mCheckDAAEnable) {
            return;
        }
        HobotImage image = new HobotImage();
        image.setWidth(width);
        image.setHeight(height);
        image.setStep(width);
        image.setChannel(1);
        image.setColorMode(colorMode);
        image.setData(data);
        image.setTimestamp(timestamp);

        // 遮挡DAA操作
        synchronized (mAtomicCheckDAAFlag) {
            if (mAtomicCheckDAAFlag.get()) {
                if (DEBUG) {
                    Log.d(TAG, "processImage: do check DAA");
                }
                mHandler.removeMessages(CODE_CHECK_DAA);
                mHandler.obtainMessage(CODE_CHECK_DAA, image).sendToTarget();
            }
        }
    }
    // endregion 暴露方法区域

    /**
     * QualityHandler to do NO-UI TASK
     */
    private class QualityHandler extends Handler {
        private QualityHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_CHECK_DAA: {

                    // 0. 如果连续5帧被判断为遮挡，则认为是被遮挡了，不再继续判断等待报警
                    synchronized (mAtomicIsOcc) {
                        if (mAtomicIsOcc.get() >= FLAG_COUNT_OCC) {
                            Log.i(TAG, "handleMessage: CODE_CHECK_DAA: occ count > [" + FLAG_COUNT_OCC + "]!");
                            return;
                        }
                    }

                    // 否则做遮挡检测
                    // 1. 做遮挡检测
                    HobotImage image = (HobotImage) msg.obj;
                    image.setColorMode(ImageFormat.NV21);
                    boolean shelterRst = HobotQualitySDK.getInstance().checkShelter((HobotImage) msg.obj);

                    // 2. 遮挡结果判断 如果被遮挡了 则计数 +1
                    if (shelterRst) {
                        mAtomicIsOcc.incrementAndGet();
                    }
                    // 3. 没有被遮挡，则计数归0
                    else {
                        mAtomicIsOcc.set(0);
                    }
                    break;
                }
                case CODE_CHECK_DOA: {
                    Boolean isFromDAA = (Boolean) msg.obj;
                    // 3s 后 触发DOA结果判断
                    Log.w(TAG, "handleMessage: CODE_CHECK_DOA:" +
                            "mAtomicIsOcc = [" + mAtomicIsOcc.get() + "]," +
                            "FLAG_COUNT_OCC = [" + FLAG_COUNT_OCC + "]," +
                            "isFromDAA = [" + isFromDAA + "]");

                    // 1. 不再做遮挡判断 移除msg 设置为false,并清除Handler
                    synchronized (mAtomicCheckDAAFlag) {
                        mAtomicCheckDAAFlag.set(false);
                        mHandler.removeMessages(CODE_CHECK_DAA);
                    }

                    // 2. 根据 mAtomicIsOcc 判断结果
                    String eventType;
                    synchronized (mAtomicIsOcc) {
                        // 大于5帧连续遮挡，认为被遮挡了 报 OCC
                        if (mAtomicIsOcc.get() >= FLAG_COUNT_OCC) {
                            eventType = WarningEventType.TYPE_DOA;
                        } else if (!isFromDAA) {
                            // 如果是刚开始主动检测 直接移除
                            Log.d(TAG, "handleMessage: CODE_CHECK_DOA not from DAA, drop it!");
                            break;
                        }
                        // 否则认为 没有被遮挡 报 NO FACE DAA
                        else {
                            eventType = WarningEventType.TYPE_DAA;
                        }
                    }

                    // 3. 报警
                    EventTypeInfo eventTypeInfo = new EventTypeInfo.Builder()
                            .eventType(eventType)
                            .eventTime(System.currentTimeMillis())
                            .filePath(mFilePath)
                            .build();
                    HobotWarningSDK.getInstance().warn(eventTypeInfo);
                    HobotWarningSDK.getInstance().upload(eventTypeInfo);
                    break;
                }
            }
        }
    }
}
