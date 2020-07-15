package com.hobot.sample.app.listener;

/**
 * 预览状态监听
 *
 * @author Hobot
 */
public interface IPreviewStateListener {
    /**
     * 预览状态变化
     *
     * @param isShow
     */
    void onPreviewState(boolean isShow);

    /**
     * 绘制状态
     *
     * @param isShow
     */
    void onRenderState(boolean isShow);

    /**
     *
     * @param isShow
     */
    void onDVRState(boolean isShow);
}
