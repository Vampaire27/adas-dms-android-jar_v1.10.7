package com.hobot.sample.app.slide.view.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

/**
 * DEBUG界面控件基类
 */
public abstract class BaseView implements IBaseView, View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    // 上下文
    protected Context mContext;
    // 控件
    private View view;
    // 是否创建成功
    private boolean mIsViewCreated;

    /**
     * 控件布局
     *
     * @return
     */
    public abstract int layoutId();

    /**
     * 初始化布局
     *
     * @param view
     */
    public abstract void initView(View view);

    /**
     * 注册监听
     */
    protected void registListeners() {

    }

    /**
     * 解注册监听
     */
    protected void unregistListeners() {

    }

    /**
     * 初始化数据
     */
    public abstract void initData();

    /**
     * 释放
     */
    public void release() {
        unregistListeners();
    }

    /**
     * View是否已经创建
     *
     * @return
     */
    public boolean isViewCreated() {
        return mIsViewCreated;
    }

    /**
     * 获取TAG
     *
     * @return
     */
    public abstract String TAG();

    public BaseView(Context context) {
        mContext = context;
    }

    @Override
    public final View onCreateView() {
        this.view = LayoutInflater.from(mContext).inflate(layoutId(), null, false);
        onViewCreate(view);
        return view;
    }

    @Override
    public final void onViewCreate(View view) {
        initView(view);
        registListeners();
        initData();
        mIsViewCreated = true;
    }

    @Override
    public final void onDestroyView() {
        unregistListeners();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }
}
