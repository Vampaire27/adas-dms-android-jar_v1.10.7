package com.hobot.sample.app.listener;

/**
 * 展会模式监听
 *
 * @author Hobot
 */
public interface IExhibitionStateListener {

    /**
     * 展会状态变化
     *
     * @param isShow
     */
    void onExhibitionState(boolean isShow);
}
