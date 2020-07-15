package com.hobot.sample.app.module.communicate;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.hobot.sample.app.activity.BaseActivity;
import com.hobot.sample.app.module.base.BaseNoLayoutView;

/**
 * 网络传输控件
 *
 * @author Hobot
 */
public class CommunicateView extends BaseNoLayoutView implements ProtocolProcessor.CalibrationStateCallback {

    private ProtocolProcessor mProcessor;

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        mProcessor = new ProtocolProcessor(mContext, this);
        mProcessor.init();
    }

    @Override
    public void onStart() {
        super.onStart();
        mProcessor.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mProcessor.stop();
    }

    @Override
    public void onViewRelease() {
        super.onViewRelease();
        mProcessor.destroy();
    }

    @Override
    public void onStateEvent(final int code, final String msg) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (code) {
                    case ProtocolProcessor.CalibrationStateCallback.CODE_START:
                        ((BaseActivity) mContext).showLoadingDialog(msg, false);
                        break;
                    case ProtocolProcessor.CalibrationStateCallback.CODE_STOP:
                        ((BaseActivity) mContext).dismissLoadingDialog();
                        break;
                    case ProtocolProcessor.CalibrationStateCallback.CODE_CANCEL:
                        ((BaseActivity) mContext).dismissLoadingDialog();
                        break;
                }
            }
        });
    }
}
