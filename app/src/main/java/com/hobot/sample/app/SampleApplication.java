package com.hobot.sample.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.quality.sdk.HobotQualitySDK;
import com.hobot.sample.app.config.Constants;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.util.CameraHelper;
import com.hobot.sample.app.util.GpsManager;
import com.hobot.sample.app.view.ScreenDensityUtils;
import com.hobot.sdk.library.modules.log.HobotLog;
import com.hobot.sdk.library.modules.log.LogFile;
import com.hobot.sdk.library.tasks.FixedThreadPool;
import com.hobot.sdk.library.tasks.SingleTask;
import com.hobot.sdk.library.utils.SharePrefs;
import com.hobot.transfer.common.HobotSocketSDK;

import java.util.concurrent.CountDownLatch;

/**
 * 示例应用上下文
 *
 * @author Hobot
 */
public class SampleApplication extends BaseApplication {
    private static final String TAG = "SampleApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Application context = this;
        initConfig(context);
        initSDK(context);
        // 初始化当前应用的分辨率和密度
        ScreenDensityUtils.setDensity(context);
    }

    /**
     * 初始化配置项目
     */
    private void initConfig(Context context) {
        SharePrefs sharePrefs = new SharePrefs(context);
        DefaultConfig.SUPPORT_LOG_PRINT = sharePrefs.getBoolean(Constants.KEY_LOG_PRINT_SWITCH, DefaultConfig.SUPPORT_LOG_PRINT);
        DefaultConfig.SUPPORT_LOG_FILE = sharePrefs.getBoolean(Constants.KEY_LOG_FILE_SWITCH, DefaultConfig.SUPPORT_LOG_FILE);
        DefaultConfig.SUPPORT_LOG_OSS = sharePrefs.getBoolean(Constants.KEY_LOG_OSS_SWITCH, DefaultConfig.SUPPORT_LOG_OSS);
        if (!DefaultConfig.SUPPORT_LOG_PRINT) {
            HobotLog.removeDefaultLogger();
        }
        if (DefaultConfig.SUPPORT_LOG_FILE) {
            HobotLog.addLogger(new LogFile());
        }
        DefaultConfig.SUPPORT_ADAS = sharePrefs.getBoolean(Constants.KEY_SUPPORT_ADAS_SWITCH, DefaultConfig.SUPPORT_ADAS);
        DefaultConfig.SUPPORT_DMS = sharePrefs.getBoolean(Constants.KEY_SUPPORT_DMS_SWITCH, DefaultConfig.SUPPORT_DMS);
        DefaultConfig.SUPPORT_FACE_ID = sharePrefs.getBoolean(Constants.KEY_SUPPORT_FACE_ID_SWITCH, DefaultConfig.SUPPORT_FACE_ID);
        DefaultConfig.SUPPORT_FACE_CLOUD = sharePrefs.getBoolean(Constants.KEY_SUPPORT_FACE_CLOUD_SWITCH, DefaultConfig.SUPPORT_FACE_CLOUD);
        DefaultConfig.SUPPORT_SIGN_IN = sharePrefs.getBoolean(Constants.KEY_SUPPORT_SIGN_IN_SWITCH, DefaultConfig.SUPPORT_SIGN_IN);
        DefaultConfig.SUPPORT_LIVING = sharePrefs.getBoolean(Constants.KEY_SUPPORT_LIVING_SWITCH, DefaultConfig.SUPPORT_LIVING);
        DefaultConfig.SUPPORT_IMAGE_QUALITY = sharePrefs.getBoolean(Constants.KEY_SUPPORT_IMAGE_QUALITY_SWITCH, DefaultConfig.SUPPORT_IMAGE_QUALITY);
        DefaultConfig.SUPPORT_SPEECH = sharePrefs.getBoolean(Constants.KEY_SUPPORT_SPEECH_SWITCH, DefaultConfig.SUPPORT_SPEECH);
        DefaultConfig.SUPPORT_NET_TRANSFER_SERVER = sharePrefs.getBoolean(Constants.KEY_SUPPORT_NET_TRANSFER_SWITCH, DefaultConfig.SUPPORT_NET_TRANSFER_SERVER);
        DefaultConfig.SUPPORT_INR = sharePrefs.getBoolean(Constants.KEY_SUPPORT_INR, DefaultConfig.SUPPORT_INR);
        DefaultConfig.SUPPORT_UPLOAD = sharePrefs.getBoolean(Constants.KEY_SUPPORT_UPLOAD, DefaultConfig.SUPPORT_UPLOAD);
        DefaultConfig.SUPPORT_PAAS = sharePrefs.getBoolean(Constants.KEY_SUPPORT_PAAS, DefaultConfig.SUPPORT_PAAS);

        DefaultConfig.PREVIEW_SHOW_SWITCH = sharePrefs.getBoolean(Constants.KEY_PREVIEW_SHOW_SWITCH, DefaultConfig.PREVIEW_SHOW_SWITCH);
        DefaultConfig.RENDER_SHOW_SWITCH = sharePrefs.getBoolean(Constants.KEY_RENDER_SHOW_SWITCH, DefaultConfig.RENDER_SHOW_SWITCH);
        DefaultConfig.DVR_SHOW_SWITCH = sharePrefs.getBoolean(Constants.KEY_DVR_SHOW_SWITCH, DefaultConfig.DVR_SHOW_SWITCH);
        DefaultConfig.SPEED_RENDER_SWITCH = sharePrefs.getBoolean(Constants.KEY_SPEED_RENDER_SWITCH, DefaultConfig.SPEED_RENDER_SWITCH);
        DefaultConfig.EXHIBITION_SHOW_SWITCH = sharePrefs.getBoolean(Constants.KEY_EXHIBITION_SHOW_SWITCH, DefaultConfig.EXHIBITION_SHOW_SWITCH);
        DefaultConfig.PERFORMANCE_LOG_SWITCH = sharePrefs.getBoolean(Constants.KEY_PERFORMANCE_LOG_SWITCH, DefaultConfig.PERFORMANCE_LOG_SWITCH);
        DefaultConfig.OVER_Standard_SWITCH = sharePrefs.getBoolean(Constants.KEY_OVER_STANDARD_MODE_SWITCH, DefaultConfig.OVER_Standard_SWITCH);
        // 默认进入正式模式
//        DefaultConfig.TEST_MODE_SWITCH = mSP.getBoolean(Constants.KEY_TEST_MODE_SWITCH,
//        DefaultConfig.TEST_MODE_SWITCH);
        DefaultConfig.SUPPORT_ADAS_VEHICLE_TRACKING = sharePrefs.getBoolean(Constants.KEY_ADAS_VEHICLE_TRACKING, DefaultConfig.SUPPORT_ADAS_VEHICLE_TRACKING);
        DefaultConfig.SUPPORT_ADAS_LANE_TRACKING = sharePrefs.getBoolean(Constants.KEY_ADAS_LANE_TRACKING, DefaultConfig.SUPPORT_ADAS_LANE_TRACKING);
        // 获取默认是否开启假速度
        DefaultConfig.SUPPORT_FAKE_SPEED = sharePrefs.getBoolean(Constants.KEY_FAKE_SPEED_SWITCH, DefaultConfig.SUPPORT_FAKE_SPEED);
    }

    /**
     * 初始化SDK
     */
    private void initSDK(Context context) {
        int count = 1; // CommonWarning
        if (DefaultConfig.SUPPORT_ADAS) {
            count++; // ADAS
        }
        if (DefaultConfig.SUPPORT_DMS) {
            count++; // DMS
        }
        if (DefaultConfig.SUPPORT_IMAGE_QUALITY) {
            count++;// Quality
        }
        sLatch = new CountDownLatch(count);
        // ======================以下SDK需要异步加载========================
        // 初始化ADAS
        if (DefaultConfig.SUPPORT_ADAS) {
            initAdasConfig(context);
        }
        // 初始化DMS
        if (DefaultConfig.SUPPORT_DMS) {
            initDmsConfig(context);
        }
        // 初始化图片质量
        if (DefaultConfig.SUPPORT_IMAGE_QUALITY) {
            initQualitySDK(context);
        }
        // 初始化报警模块
        initWarningConfig(context);
        // ======================以下SDK不需要异步加载========================
        // 初始化Server端的Socket连接
        if (DefaultConfig.SUPPORT_NET_TRANSFER_SERVER) {
            HobotSocketSDK.Server.init(context);
            HobotSocketSDK.Server.initSocket();
        }
        // 初始化Camera模块
        CameraHelper.init(this);
        // 初始化GPS模块
        GpsManager.getInstance().init(this);
    }

    /**
     * 加载ADAS SDK配置
     *
     * @param context 上下文
     */
    private void initAdasConfig(final Context context) {
        // 加载Adas配置文件
        FixedThreadPool.get().execute(new SingleTask() {
            @Override
            protected void runTask() {
                // 初始化ADAS SDK
                HobotAdasSDK.getInstance().autoInit(context);
                sLatch.countDown();
            }

            @Override
            protected void catchException(Exception e) {
                super.catchException(e);
                sLatch.countDown();
            }

            @Override
            protected String TAG() {
                return TAG + "-ADAS";
            }
        });
    }

    /**
     * 加载DMS SDK配置
     *
     * @param context 上下文
     */
    private void initDmsConfig(final Context context) {
        // 加载Dms配置文件
        FixedThreadPool.get().execute(new SingleTask() {
            @Override
            protected void runTask() {
                HobotDmsSdk.getInstance().autoInit(context);
                sLatch.countDown();
            }

            @Override
            protected void catchException(Exception e) {
                super.catchException(e);
                sLatch.countDown();
            }

            @Override
            protected String TAG() {
                return TAG + "-DMS";
            }
        });
    }

    /**
     * 加载CommonWarning配置
     *
     * @param context 上下文
     */
    private void initWarningConfig(final Context context) {
        // 加载CommonWarning配置文件
        FixedThreadPool.get().execute(new SingleTask() {
            @Override
            protected void runTask() {
                HobotWarningSDK.getInstance().autoInit(context);
                sLatch.countDown();
            }

            @Override
            protected void catchException(Exception e) {
                super.catchException(e);
                sLatch.countDown();
            }

            @Override
            protected String TAG() {
                return TAG + "-WARN";
            }
        });
    }

    /**
     * 初始化图像质量
     *
     * @param context 上下文
     */
    private void initQualitySDK(final Context context) {
        FixedThreadPool.get().execute(new SingleTask() {
            @Override
            protected void runTask() {
                HobotQualitySDK.getInstance().autoInit(context);
                sLatch.countDown();
            }

            @Override
            protected void catchException(Exception e) {
                super.catchException(e);
                sLatch.countDown();
            }

            @Override
            protected String TAG() {
                return TAG + "-QUALITY";
            }
        });
    }

    /**
     * 释放SDK
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        destroySDK();
    }

    /**
     * 释放SDK
     */
    private void destroySDK() {
        // 销毁Server端的Socket
        if (DefaultConfig.SUPPORT_NET_TRANSFER_SERVER) {
            HobotSocketSDK.Server.destroySocket();
        }
        // 释放ADAS SDK
        if (DefaultConfig.SUPPORT_ADAS) {
            HobotAdasSDK.getInstance().release();
        }
        // 释放DMS SDK
        if (DefaultConfig.SUPPORT_DMS) {
            HobotDmsSdk.getInstance().release();
        }
        // 释放图像检测 SDK
        if (DefaultConfig.SUPPORT_IMAGE_QUALITY) {
            HobotQualitySDK.getInstance().destroy();
        }
        // 释放Camera模块
        CameraHelper.release();
        // 释放GPS模块
        GpsManager.getInstance().destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged this = " + this);
        // 语言变化后，重新初始化HobotWarning SDK
        HobotWarningSDK.getInstance().init(this, false);
    }
}
