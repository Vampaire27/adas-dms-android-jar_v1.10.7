package com.hobot.sample.app.module.performance;

import android.util.Log;

import com.hobot.sample.app.listener.IPerformanceLogSwitchListener;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.module.base.BaseNoLayoutView;
import com.hobot.sdk.library.tasks.PerformanceMonitor;
import com.hobot.sdk.library.tasks.TimingThreadPool;

/**
 * 管理性能监视器
 */
public class PerformanceView extends BaseNoLayoutView implements IPerformanceLogSwitchListener {
    private static final String TAG = PerformanceView.class.getSimpleName();

    private PerformanceMonitor mMonitor = new PerformanceMonitor();

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        NebulaObservableManager.getInstance().registerPerformanceLogListener(this);
    }

    @Override
    public void onViewRelease() {
        super.onViewRelease();
        NebulaObservableManager.getInstance().unregisterPerformanceLogListener(this);
    }

    @Override
    public void onPerformanceLogSwitchChanged(boolean isEnable) {
        Log.d(TAG, "onPerformanceLogSwitchChanged() called with: isEnable = [" + isEnable + "]");
        if (isEnable) {
            TimingThreadPool.get().execute(mMonitor);
        } else {
            TimingThreadPool.get().cancel(mMonitor, true);
        }
    }
}
