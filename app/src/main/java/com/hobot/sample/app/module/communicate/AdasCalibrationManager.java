package com.hobot.sample.app.module.communicate;

import android.content.Context;
import android.util.Log;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.adas.sdk.listener.IAdasRenderListener;
import com.hobot.sample.app.manager.NebulaSDKManager;
import com.hobot.sdk.library.jni.input.HobotImage;
import com.hobot.transfer.common.HobotSocketSDK;
import com.hobot.transfer.common.protocol.Protocol;

/**
 * ADAS 客户端标定管理类
 *
 * @author Hobot
 */
public class AdasCalibrationManager extends BaseCalibrationManager implements IAdasRenderListener {
    private static final String TAG = "AdasCalibrationManager";
    private volatile static AdasCalibrationManager mInstance;

    private AdasCalibrationManager() {
    }

    public static AdasCalibrationManager getInstance() {
        if (null == mInstance) {
            synchronized (AdasCalibrationManager.class) {
                if (null == mInstance) {
                    mInstance = new AdasCalibrationManager();
                }
            }
        }
        return mInstance;
    }


    @Override
    public int start(Context context) {
        Log.d(TAG, "start: +");
        super.start(context);
        // 注册数据监听
        HobotAdasSDK.getInstance().registerRenderListener(this);
        Log.d(TAG, "start: -");
        return 0;
    }

    @Override
    public int stop() {
        super.stop();
        Log.d(TAG, "stop: +");

        // 解除监听
        HobotAdasSDK.getInstance().unregisterRenderListener(this);
        // 重新打开
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS, "");
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS, "");
        Log.d(TAG, "stop: -");
        return 0;
    }

    @Override
    public int finish() {
        Log.d(TAG, "finishCalibration +");
        // finish方法执行完成即认为 标定完成
        int rst = HobotAdasSDK.getInstance().finishCalibration();
        // 告知客户端 设备标定完成 客户端不会响应
        HobotSocketSDK.Server.sendMsg(Protocol.CALIBRATION_IS_FINISHED);
        // 手动stop
        stop();
        Log.d(TAG, "finishCalibration -");
        return rst;
    }

    @Override
    public String TAG() {
        return TAG;
    }


    @Override
    public void onByteStop() {
        Log.d(TAG, "onByteStop() called");
    }

    @Override
    public void onByteError(int code, String msg) {
        Log.d(TAG, "onByteError() called with: code = [" + code + "], msg = [" + msg + "]");
        HobotSocketSDK.Server.sendMsg(Protocol.CALIBRATION_CRASH);
        stop();
    }

    @Override
    public void onByteNext(byte[] bytes, int i, int i1) {

    }

    @Override
    public void onAdasRender(HobotImage adasRenderOutput) {
        // 判断是否可以JPG压缩
        if (!mCanJpeg) {
            return;
        }

        // 发送数据
        boolean send = send(adasRenderOutput);
        if (!send) {
            Log.w(TAG, "onAdasRender: cannot send!");
        }

    }
}
