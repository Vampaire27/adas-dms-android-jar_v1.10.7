package com.hobot.sample.app.listener;

/**
 * 性能日志开关
 */
public interface IPerformanceLogSwitchListener {

    /**
     * 性能开关变化。
     *
     * @param isEnable 是否可用
     */
    void onPerformanceLogSwitchChanged(boolean isEnable);
}
