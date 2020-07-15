package com.hobot.sample.app.listener;

/**
 * 过标模式监听
 *
 * @author Hobot
 */
public interface IOverStandardModeListener {
    /**
     * 过标模式变化。
     *
     * @param isEnable 是否进入过标模式
     */
    void onOverStandardModeChanged(boolean isEnable);
}
