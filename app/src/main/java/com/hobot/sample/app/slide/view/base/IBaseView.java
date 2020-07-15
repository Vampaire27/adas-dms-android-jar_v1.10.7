package com.hobot.sample.app.slide.view.base;

import android.view.View;

/**
 * 控件接口
 */
public interface IBaseView {
    /**
     * 创建布局
     *
     * @return
     */
    View onCreateView();

    /**
     * 布局创建
     *
     * @param view
     */
    void onViewCreate(View view);

    /**
     * 布局销毁
     */
    void onDestroyView();

    View getView();
}
