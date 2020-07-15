package com.hobot.sample.app.module.base;

import android.content.Context;
import android.view.View;

/**
 * View Common 接口
 *
 * @author Hobot
 */
public interface ICommonView {
    /**
     * 创建View
     *
     * @param context 上下文
     * @return 创建的View
     */
    View onCreateView(Context context);

    /**
     * 销毁View
     */
    void onDestroyView();

    /**
     * 进入onStart状态
     */
    void onStart();

    /**
     * 进入Resume状态
     */
    void onResume();

    /**
     * 进入Pause状态
     */
    void onPause();

    /**
     * 进入onStop状态
     */
    void onStop();
}
