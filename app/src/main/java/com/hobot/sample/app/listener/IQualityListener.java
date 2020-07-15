package com.hobot.sample.app.listener;

/**
 * 图像质量监听
 *
 * @author Hobot
 */
public interface IQualityListener {

    /**
     * 图像质量检测回调。
     */
    void onCheckShelter(String... params);
}
