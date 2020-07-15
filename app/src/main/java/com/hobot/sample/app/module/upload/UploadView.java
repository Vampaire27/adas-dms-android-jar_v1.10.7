package com.hobot.sample.app.module.upload;

import com.hobot.nebula.common.unit.EventCell;
import com.hobot.sdk.library.tasks.TimingTask;
import com.hobot.sdk.library.tasks.TimingThreadPool;

import java.util.concurrent.TimeUnit;

/**
 * 地平线蚂蚁计划数据上传
 */
public class UploadView extends BaseUploadView {
    private static final String TAG = UploadView.class.getSimpleName();
    // 清理线程
    private TimingTask mCleanTask = new CleanTask(10, CleanTask.TIME_LIMIT, TimeUnit.MILLISECONDS);

    @Override
    public void onViewCreated() {
        super.onViewCreated();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 运行清理线程
        TimingThreadPool.get().execute(mCleanTask);
    }

    @Override
    public void onStop() {
        super.onStop();
        // 结束清理线程
        TimingThreadPool.get().cancel(mCleanTask, true);
    }

    @Override
    public void onViewRelease() {
        super.onViewRelease();
    }

    @Override
    public void onUpload(EventCell eventCell) {
        // 上传ADAS
        if ("ADAS".equals(eventCell.getEventGroup())) {
            return;
        }
        // 上传DMS
        if ("DMS".equals(eventCell.getEventGroup())) {
            return;
        }

    }

}
