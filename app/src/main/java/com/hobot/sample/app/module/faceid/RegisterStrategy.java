package com.hobot.sample.app.module.faceid;

import com.hobot.dms.sdk.model.FaceInfo;

import java.util.concurrent.atomic.AtomicBoolean;

import DMSOutputProtocol.DMSSDKOutputOuterClass;

/**
 * 注册脸部策略
 * 业务相关
 */
public class RegisterStrategy {
    // region 静态变量区
    private static final String TAG = "RecogStrategy";
    // endregion 静态变量区

    // region 私有成员区
    private AtomicBoolean mActiveRegister = new AtomicBoolean(false);  // 开启注册
    // endregion 私有成员区

    // region 暴露方法区域

    /**
     * 注册策略处理，需要每一帧回调时调用
     *
     * @param faceInfo 接收一个faceInfo数据
     * @return true 需要人脸识别，false 不需要
     */
    public boolean process(FaceInfo faceInfo) {
        if (mActiveRegister.get()
                && hasRoi(faceInfo)
                && isDirFront(faceInfo)
                && isLimitAngle(faceInfo)) {
            reset();
            return true;
        }
        return false;
    }

    /**
     * 主动注冊
     */
    public boolean activeRegister() {
        return mActiveRegister.compareAndSet(false, true);
    }

    /**
     * 主动取消注册
     */
    public void reset() {
        mActiveRegister.compareAndSet(true, false);
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
    // endregion 私有方法区域
}
