package com.hobot.sample.app.module.upload;

import android.util.Log;

import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.nebula.common.listener.IEventUpdateListener;
import com.hobot.nebula.common.unit.EventCell;
import com.hobot.sample.app.module.base.BaseNoLayoutView;

/**
 * 数据上传控件
 *
 * @author hobot
 */
public abstract class BaseUploadView extends BaseNoLayoutView implements IEventUpdateListener {

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        // 注册上传监听
        HobotWarningSDK.getInstance().registEventUpdateListener(this);
    }

    @Override
    public void onViewRelease() {
        super.onViewRelease();
        // 取消上传监听
        HobotWarningSDK.getInstance().unregistEventUpdateListener(this);
    }

    @Override
    public void onEventUpdate(EventCell eventCell) {
        Log.d(TAG(), "onEventUpdate() called with: eventCell = [" + eventCell + "]");
        onUpload(eventCell);
    }

    public abstract void onUpload(EventCell eventCell);
}
