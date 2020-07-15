package com.hobot.sample.app.listener;

/**
 * 转向灯监听
 *
 * @author Hobot
 */
public interface ITurnLightListener {
    /**
     * 转向灯变化
     *
     * @param direction 转向灯方向
     */
    void onTurnLightChange(int direction);
}
