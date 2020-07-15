package com.hobot.sample.app.listener;

/**
 * 测试模式监听
 *
 * @author Hobot
 */
public interface ITestModeListener {
    /**
     * 测试模式变化。
     *
     * @param isEnable 是否进入测试模式
     */
    void onTestModeChanged(boolean isEnable);


    /**
     * 假速度改变
     *
     * @param speed 速度 km/h
     */
    void onFakeSpeedChanged(float speed);
}
