package com.hobot.sample.app.module.base;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.View;

import com.hobot.camera.library.base.IPreviewCallback;
import com.hobot.sample.app.listener.ITestModeListener;

/**
 * SDK View的抽象类，没有布局，只用于控制SDK的生命周期。
 *
 * @author Hobot
 */
public abstract class BaseNoLayoutView extends CommonView implements ICommonView, ITestModeListener, IPreviewCallback {

    @Override
    public final View onCreateView(Context context) {
        super.onCreateView(context);
        onViewCreated();
        return null;
    }

    @Override
    public final void onDestroyView() {
        super.onDestroyView();
        onViewRelease();
    }

    /**
     * 控件创建回调
     */
    @CallSuper
    public void onViewCreated() {

    }

    /**
     * 当控件销毁回调
     */
    @CallSuper
    public void onViewRelease() {

    }
}
