package com.hobot.sample.app.module.faceid;

import android.util.Log;

import com.hobot.dms.sdk.model.FaceInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import DMSOutputProtocol.DMSSDKOutputOuterClass;

/**
 * 查询策略处理
 * 业务相关
 */
public class RecogStrategy {
    // region 静态变量区
    private static final String TAG = "RecogStrategy";
    // 识别间隔
    private int mRecogInterval = TIMER_INTERVAL;
    private static final int TIMER_INTERVAL = 10000;
    // 人脸消失累计帧数
    private static final int DISAPPEAR_COUNT_DOWN = 100;
    // endregion 静态变量区

    // region 私有成员区
    private AtomicLong mLatestTime = new AtomicLong();
    private AtomicInteger mDisappearCount = new AtomicInteger(0);   // 人脸重现计数
    private AtomicBoolean mActiveRecog = new AtomicBoolean(false);  // 主动识别
    // endregion 私有成员区

    // region 暴露方法区域

    /**
     * 识别策略处理，需要每一帧回调时调用
     *
     * @param faceInfo 接收一个faceInfo数据
     * @return true 需要人脸识别，false 不需要
     */
    public boolean process(FaceInfo faceInfo) {
        if (hasRoi(faceInfo)) { // 如果有roi
            if (!isDirFront(faceInfo)) { // 如果不是正脸
                return false;
            }
            if (!isLimitAngle(faceInfo)) {
                return false;
            }
            if (isActiveRecog() || isReappear() || isTimerRefresh()) {  // 主动识别？重现？定时？->刷新
                reset();
                return true;
            } else {
                mDisappearCount.set(0);
            }
        } else {
            mDisappearCount.incrementAndGet();
        }
        return false;
    }

    /**
     * 主动识别
     */
    public boolean activeRecog() {
        return mActiveRecog.compareAndSet(false, true);
    }

    /**
     * 重置方法
     */
    public void reset() {
        mActiveRecog.set(false);
        mDisappearCount.set(0);
        mLatestTime.set(System.currentTimeMillis());
    }

    /**
     * 设置识别间隔
     *
     * @param interval
     */
    public void setInterval(int interval) {
        mRecogInterval = interval;
    }
    // endregion 暴露方法区域

    // region 私有方法区域
    // 朝前?
    private boolean isDirFront(FaceInfo faceInfo) {
        return !faceInfo.getDirection().getFaceDirList().isEmpty()
                && DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_FRONT ==
                faceInfo.getDirection().getFaceDirList().get(0);
    }

    // 角度
    private boolean isLimitAngle(FaceInfo faceInfo) {
        float roll = faceInfo.getDirection().getCurAngleRpy().getRoll();
        float pitch = faceInfo.getDirection().getCurAngleRpy().getPitch();
        float yaw = faceInfo.getDirection().getCurAngleRpy().getYaw();
        return Math.abs(roll) <= FaceIdProcessor.MAX_ANGEL_ROLL && Math.abs(pitch) < FaceIdProcessor.MAX_ANGEL_PITCH
                && Math.abs(yaw) <= FaceIdProcessor.MAX_ANGEL_YAW;
    }

    // 有人脸框?
    private boolean hasRoi(FaceInfo faceInfo) {
        return null != faceInfo.getRoi();
    }

    // 定时刷新?
    private boolean isTimerRefresh() {
        return System.currentTimeMillis() - mLatestTime.get() >= mRecogInterval;
    }

    // 主动识别?
    private boolean isActiveRecog() {
        return mActiveRecog.get();
    }

    // 消失N帧后重现?
    private boolean isReappear() {
        if (mDisappearCount.get() >= DISAPPEAR_COUNT_DOWN) {
            Log.e(TAG, "isReappear: = true");
            return true;
        }
        return false;
    }
    // endregion 私有方法区域
}
