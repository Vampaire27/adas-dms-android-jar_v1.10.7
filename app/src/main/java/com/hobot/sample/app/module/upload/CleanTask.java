package com.hobot.sample.app.module.upload;

import android.util.Log;

import com.hobot.sdk.library.tasks.TimingTask;
import com.hobot.sdk.library.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.TimeUnit;

/**
 * 清理线程
 */
public class CleanTask extends TimingTask {
    private static final String TAG = CleanTask.class.getSimpleName();
    /**
     * 清理间隔
     */
    public static final long TIME_LIMIT = 1000 * 60 * 30;

    /**
     * 清理目录
     */
    public String[] mCleanFolder = new String[]{
            "/sdcard/hobot/media/adas/video/",
            "/sdcard/hobot/media/adas/image/",
            "/sdcard/hobot/media/dms/video/",
            "/sdcard/hobot/media/dms/image/",
            "/sdcard/hobot/media/dvr/"
    };

    public CleanTask(long delayTime, long period, TimeUnit timeUnit) {
        super(delayTime, period, timeUnit);
    }

    @Override
    protected void runTask() {
        long now = System.currentTimeMillis();
        long before = now - TIME_LIMIT;
        for (int i = 0; i < mCleanFolder.length; i++) {
            String parent = mCleanFolder[i];
            // 获取待删除文件
            File[] target = filterFileBefore(parent, before);
            if (null == target) {
                Log.w(TAG, "runTask: not has file at [" + parent + "]");
                continue;
            }

            // 删除文件
            deleteFiles(target);
        }
        long end = System.currentTimeMillis();
        Log.i(TAG, "runTask: clean total cost = [" + (end - now) + "]ms");
    }

    @Override
    protected String TAG() {
        return CleanTask.TAG;
    }


    /**
     * 过滤早于 time 之前的文件
     *
     * @param parent 目录
     * @param time   时间
     * @return
     */
    public File[] filterFileBefore(String parent, final long time) {
        Log.d(TAG, "filterFileBefore() called with: parent = [" + parent + "], time = [" + time + "]");
        long begin = System.currentTimeMillis();
        File file = new File(parent);
        if (!file.exists()) {
            Log.w(TAG, "filterFileBefore: not exist at [" + parent + "]");
            return null;
        }
        if (!file.isDirectory()) {
            Log.w(TAG, "filterFileBefore: not folder at [" + parent + "]");
            return null;
        }
        File[] list = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                // 最后更新时间 比 指定时间 还早
                long last = file.lastModified();
                return (last < time);
            }
        });
        long end = System.currentTimeMillis();
        Log.i(TAG, "filterFileBefore: cost [" + (end - begin) + "], get [" + (list == null ? 0 : list.length) + "], target = [" + parent + "]");
        return list;
    }

    /**
     * 删除文件
     *
     * @param files 文件列表
     */
    public void deleteFiles(File[] files) {
        int count = 0;
        for (File file : files) {
            boolean rst = FileUtils.deleteFile(file);
            if (!rst) {
                Log.w(TAG, "deleteFile: cannot delete [" + file + "]");
            } else {
                count++;
            }
        }
        Log.i(TAG, "deleteFiles: delete count [" + count + "]");
    }
}
