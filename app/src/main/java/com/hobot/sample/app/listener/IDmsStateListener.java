package com.hobot.sample.app.listener;

/**
 * SDK状态监听。
 *
 * @author Hobot
 */
public interface IDmsStateListener {

    /**
     * DMS运行状态回调。
     *
     * @param code 错误码
     */
    void onDmsError(int code);
}
