package com.hobot.sample.app.slide.view.pager;

import android.content.Context;
import android.view.View;

import com.hobot.sample.app.R;
import com.hobot.sample.app.slide.EventSwitchSettings;
import com.hobot.sample.app.slide.view.base.BaseView;

/**
 * 通用开关设置界面。
 *
 * @author Hobot
 */
public class CommonSwitchView extends BaseView {
    private static final String TAG = "CommonSwitchView";

    private EventSwitchSettings eventSwitchSettings;

    public CommonSwitchView(Context context) {
        super(context);
    }

    @Override
    public int layoutId() {
        return R.layout.view_common_switch_layout;
    }

    @Override
    public void initView(View view) {
        eventSwitchSettings = (EventSwitchSettings) view.findViewById(R.id.switch_settings);
    }

    @Override
    protected void registListeners() {
        eventSwitchSettings.initListeners(true);
    }

    @Override
    protected void unregistListeners() {
        eventSwitchSettings.initListeners(false);
    }

    @Override
    public void release() {
        eventSwitchSettings.release();
    }

    @Override
    public void initData() {
        eventSwitchSettings.initData();
    }

    @Override
    public String TAG() {
        return TAG;
    }
}
