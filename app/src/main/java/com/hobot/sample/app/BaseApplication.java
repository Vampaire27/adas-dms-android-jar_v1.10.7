package com.hobot.sample.app;

import android.app.Application;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class BaseApplication extends Application {
    protected static CountDownLatch sLatch;
    protected static BaseApplication sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
    }

    /**
     * 等待加载完成
     */
    public static void waitLatch() {
        try {
            if (sLatch.getCount() > 0) {
                // 仅等待5S 防止无法进入主界面
                sLatch.await(10000, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BaseApplication getApplication() {
        return sApp;
    }
}
