package com.hobot.sample.app.module.communicate;

import android.content.Context;
import android.util.Log;

import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.dms.sdk.listener.IDmsCalibrateListener;
import com.hobot.dms.sdk.listener.IDmsFrameListener;
import com.hobot.sdk.library.jni.input.HobotImage;
import com.hobot.transfer.common.HobotSocketSDK;
import com.hobot.transfer.common.model.ProtocolModel;
import com.hobot.transfer.common.protocol.Protocol;

import DMSOutputProtocol.DMSSDKOutputOuterClass;

import static DMSOutputProtocol.DMSSDKOutputOuterClass.FaceCalibEnum.FACE_CALIB_FINISHED;

/**
 * DMS 客户端标定管理类
 *
 * @author Hobot
 */
public class DmsCalibrationManager extends BaseCalibrationManager implements IDmsCalibrateListener, IDmsFrameListener {
    private static final String TAG = "DmsCalibrationManager";
    private volatile static DmsCalibrationManager mInstance;

    private DmsCalibrationManager() {
    }

    public static DmsCalibrationManager getInstance() {
        if (null == mInstance) {
            synchronized (DmsCalibrationManager.class) {
                if (null == mInstance) {
                    mInstance = new DmsCalibrationManager();
                }
            }
        }
        return mInstance;
    }


    /**
     * 开启标定
     * 开启数据传输服务
     * 设置发送监听
     *
     * @param context 上下文
     * @return 结果
     */
    @Override
    public int start(Context context) {
        Log.d(TAG, "start: +");
        super.start(context);
        // 注册监听
        HobotDmsSdk.getInstance().registerFrameListener(this);
        HobotDmsSdk.getInstance().registerCalibrateListener(this);
        Log.d(TAG, "start: -");
        return 0;
    }

    @Override
    public int stop() {
        Log.d(TAG, "stop: +");
        super.stop();
        // 取消监听
        HobotDmsSdk.getInstance().unregisterCalibrateListener(this);
        HobotDmsSdk.getInstance().unregisterFrameListener(this);
        Log.d(TAG, "stop: -");
        return 0;
    }

    /**
     * 客户端请求完成标定
     *
     * @return
     */
    @Override
    public int finish() {
        // 和ADAS标定不同 这里 设备端DMS需要一段时间进行标定
        // 这里只是指令 让DMS去标定
        // DMS 标定结果会用异步回调通知
        Log.d(TAG, "finishCalibration +");
        int rst = HobotDmsSdk.getInstance().finishCalibration();
        Log.d(TAG, "finishCalibration -");
        return rst;
    }

    @Override
    public String TAG() {
        return TAG;
    }

    @Override
    public void onByteStop() {
        // nop
    }

    @Override
    public void onByteError(int code, String msg) {
        Log.e(TAG, "onByteError: +");
        Log.d(TAG, "onByteError: code = " + code + ",msg = " + msg);
        HobotSocketSDK.Server.sendMsg(Protocol.DMS_CALIBRATION_CRASH);
        stop();
        Log.e(TAG, "onByteError: -");
    }

    @Override
    public void onByteNext(byte[] bytes, int i, int i1) {
    }

    @Override
    public void onDmsCalibResult(DMSSDKOutputOuterClass.FaceCalibEnum result, DMSSDKOutputOuterClass.Vector_3f vector3f) {
        switch (result) {
//            case FACE_CALIB_START:
            case FACE_CALIB_FINISHED:
            case FACE_CALIB_NOFACE:
            case FACE_CALIB_ABNORMALFACE:
            case FACE_CALIB_FAILED: {
                // 当DMS多帧标定完成到这里时
                Log.e(TAG, "onFinished: +");
                // 告知客户端 设备端标定结果
                ProtocolModel model = new ProtocolModel.Builder(Protocol.RSP)
                        .type(Protocol.TYPE_DMS_CALIBRATION)
                        .state(Protocol.STATE_IS_FINISHED)
                        .errorCode(String.valueOf(result.getNumber()))
                        .build();
                HobotSocketSDK.Server.sendMsg(model);
                Log.e(TAG, "onFinished: -");

                // 手动调用stop来停止
                if (result.equals(FACE_CALIB_FINISHED)) {
                    stop();
                }
            }
            break;
        }
    }

    @Override
    public void onDmsFrame(HobotImage dmsFrameModel) {
        // 判断是否可以JPG压缩
        if (!mCanJpeg) {
            return;
        }
        // 填充UV分量
        fakeUV(dmsFrameModel);

        boolean send = send(dmsFrameModel);
        if (!send) {
            Log.w(TAG, "onDmsFrame: cannot send!");
        }
    }

    private volatile boolean fakeUV = true;

    /**
     * 填充UV分量 解决绿屏问题
     *
     * @param image
     */
    private void fakeUV(HobotImage image) {
        if (!fakeUV) {
            return;
        }
        Log.w(TAG, "fakeUV: fake uv+");
        fakeUV = false;
        byte[] data = image.getData();
        int height = image.getHeight();
        int width = image.getWidth();
        int len = width * height + width * height / 2;
        int dataLen  = data.length;
        if(dataLen >= len){
            for (int i = width * height; i < len; i++) {
                data[i] = 127;
            }
        }
        Log.w(TAG, "fakeUV: fake uv -");
    }
}
