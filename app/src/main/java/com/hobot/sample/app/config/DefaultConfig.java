package com.hobot.sample.app.config;

import android.graphics.ImageFormat;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.sample.app.BuildConfig;

/**
 * 默认配置入口
 *
 * @author Hobot
 */
public class DefaultConfig {

    public static final boolean DEBUG = true;
    // 默认Camera
    public static final int CAMERA_INPUT_WIDTH = 1280;
    public final static int CAMERA_INPUT_HEIGHT = 720;
    public static final int CAMERA_FORMAT = ImageFormat.YV12;
    // -------------------------- Render -------------------------
    // 预览显示开关
    public static boolean PREVIEW_SHOW_SWITCH = BuildConfig.PREVIEW_SHOW_SWITCH;
    // 绘制显示开关
    public static boolean RENDER_SHOW_SWITCH = BuildConfig.RENDER_SHOW_SWITCH;
    // DVR显示开关
    public static boolean DVR_SHOW_SWITCH = BuildConfig.DVR_SHOW_SWITCH;
    // 展会模式开关
    public static boolean EXHIBITION_SHOW_SWITCH = BuildConfig.EXHIBITION_SHOW_SWITCH;
    // 速度显示开关
    public static boolean SPEED_RENDER_SWITCH = BuildConfig.SPEED_RENDER_SWITCH;
    // -------------------------- ADAS -------------------------
    // 是否需要支持ADAS
    public static boolean SUPPORT_ADAS =
            BuildConfig.SUPPORT_ADAS && HobotAdasSDK.getInstance().isSupportFunc();
    // 默认ADAS的Camera类型
    public static int DEFAULT_ADAS_CAMERA_TYPE = BuildConfig.DEFAULT_ADAS_CAMERA_TYPE;
    // 默认ADAS的Camera ID
    public static int DEFAULT_ADAS_CAMERA_ID = BuildConfig.DEFAULT_ADAS_CAMERA_ID;
    // ADAS车辆追踪
    public static boolean SUPPORT_ADAS_VEHICLE_TRACKING = BuildConfig.SUPPORT_ADAS_VEHICLE_TRACKING;
    // ADAS车道线追踪
    public static boolean SUPPORT_ADAS_LANE_TRACKING = BuildConfig.SUPPORT_ADAS_LANE_TRACKING;
    // ADAS参数标定
    public static boolean SUPPORT_ADAS_PARAMS_CALIBRATION =
            BuildConfig.SUPPORT_ADAS_PARAMS_CALIBRATION;
    // -------------------------- DMS -------------------------
    // 是否需要支持DMS
    public static boolean SUPPORT_DMS =
            BuildConfig.SUPPORT_DMS && HobotDmsSdk.getInstance().isSupportFunc();
    // 默认DMS的Camera类型
    public static int DEFAULT_DMS_CAMERA_TYPE = BuildConfig.DEFAULT_DMS_CAMERA_TYPE;
    // 默认DMS的Camera ID
    public static int DEFAULT_DMS_CAMERA_ID = BuildConfig.DEFAULT_DMS_CAMERA_ID;
    // ------------- 数据上传 -------------
    // 数据上传开关
    public static boolean SUPPORT_UPLOAD = BuildConfig.SUPPORT_UPLOAD;

    // -------------------------- FaceId -------------------------
    // 默认FaceId开关
    public static boolean SUPPORT_FACE_ID = BuildConfig.SUPPORT_FACE_ID;
    // 默认FaceId的Camera类型
    public static int DEFAULT_FACE_ID_CAMERA_TYPE = BuildConfig.DEFAULT_DMS_CAMERA_TYPE;
    // 默认FaceId的Camera ID
    public static int DEFAULT_FACE_ID_CAMERA_ID = BuildConfig.DEFAULT_DMS_CAMERA_ID;
    // -------------------------- FaceCloud -------------------------
    // 云端人脸识别开关
    public static boolean SUPPORT_FACE_CLOUD = BuildConfig.SUPPORT_FACE_CLOUD;
    // -------------------------- NetTransfer -------------------------
    // 默认数据传输开关
    public static boolean SUPPORT_NET_TRANSFER_SERVER = BuildConfig.SUPPORT_NET_TRANSFER_SERVER;
    // ------------- 图像质量 -------------
    // 默认图像质量开关
    public static boolean SUPPORT_IMAGE_QUALITY = BuildConfig.SUPPORT_QUALITY;
    // 默认图像质量的Camera类型
    public static int DEFAULT_IMAGE_QUALITY_CAMERA_TYPE = BuildConfig.DEFAULT_DMS_CAMERA_TYPE;
    // 默认图像质量的Camera ID
    public static int DEFAULT_IMAGE_QUALITY_CAMERA_ID = BuildConfig.DEFAULT_DMS_CAMERA_ID;
    // ------------- 语音 -------------
    public static boolean SUPPORT_SPEECH = BuildConfig.SUPPORT_SPEECH;
    // ------------- 签到 -------------
    public static boolean SUPPORT_SIGN_IN = BuildConfig.SUPPORT_SIGN_IN;
    public static boolean SUPPORT_LIVING = BuildConfig.SUPPORT_LIVING;
    //---------------数人头-------------
    public static boolean SUPPORT_INR = BuildConfig.SUPPORT_INR;
    // 默认DMS的Camera类型
    public static int DEFAULT_INR_CAMERA_TYPE = BuildConfig.DEFAULT_DMS_CAMERA_TYPE;
    // 默认DMS的Camera ID
    public static int DEFAULT_INR_CAMERA_ID = BuildConfig.DEFAULT_DMS_CAMERA_ID;
    //--------------方向盘-------------
    public static boolean SUPPORT_WHEEL = BuildConfig.SUPPORT_WHEEL;
    // 默认DMS的Camera类型
    public static int DEFAULT_WHEEL_CAMERA_TYPE = BuildConfig.DEFAULT_DMS_CAMERA_TYPE;
    // 默认DMS的Camera ID
    public static int DEFAULT_WHEEL_CAMERA_ID = BuildConfig.DEFAULT_DMS_CAMERA_ID;
    //--------------PAAS服务-------------
    public static boolean SUPPORT_PAAS = BuildConfig.SUPPORT_PAAS;
    //--------------Log服务-------------
    // 输出LOG到终端
    public static boolean SUPPORT_LOG_PRINT = true;
    // 输出LOG到文件
    public static boolean SUPPORT_LOG_FILE = false;
    // 输出LOG到OSS
    public static boolean SUPPORT_LOG_OSS = false;
    //  性能日志开关
    public static boolean PERFORMANCE_LOG_SWITCH = true;
    // 测试模式开关
    public static boolean TEST_MODE_SWITCH = false;
    //过标模式开关
    public static boolean OVER_Standard_SWITCH = false;
    // 假速度开关
    public static boolean SUPPORT_FAKE_SPEED = false;
    public static int FAKE_SPEED = 0;
}
