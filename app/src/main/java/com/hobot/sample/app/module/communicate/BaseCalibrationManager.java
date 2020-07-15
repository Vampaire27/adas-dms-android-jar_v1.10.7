package com.hobot.sample.app.module.communicate;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.hobot.sdk.library.jni.input.HobotImage;
import com.hobot.sdk.yuv.library.YuvUtils;
import com.hobot.transfer.common.callback.OnByteListener;
import com.hobot.transfer.server.ByteSender;

/**
 * SDK标定抽象类
 *
 * @author Hobot
 */
public abstract class BaseCalibrationManager implements OnByteListener {

    protected static final String SERVER_IP = "tcp://localhost:20181";
    protected static final int SERVER_PORT = 21352;

    private ByteSender mByteSender = new ByteSender();

    /**
     * 是否可以进行编码
     */
    protected volatile boolean mCanJpeg = false;

    /**
     * 缓存JPG编码数据
     */
    private byte[] mJpegData = new byte[(int) (1280 * 720 * 0.3)];


    /**
     * 开启标定
     * 开启数据传输服务
     * 设置发送监听
     *
     * @param context 上下文
     * @return 结果
     */
    @CallSuper
    public int start(Context context) {
        // 开启发送服务
        mByteSender.setListener(this);
        mByteSender.start(SERVER_IP, SERVER_PORT);
        return 0;
    }

    /**
     * 停止标定
     *
     * @return 结果
     */
    @CallSuper
    public int stop() {
        // 不再编码
        mCanJpeg = false;
        // 停止发送
        mByteSender.stop();
        return 0;
    }

    /**
     * 当Sender被连接 才认为可以开启编码
     */
    @CallSuper
    @Override
    public void onByteStart() {
        // 数据发送准备好
        // 开启JPG编码
        mCanJpeg = true;
    }

    /**
     * 编码&发送数据
     *
     * @param image
     */
    public boolean send(HobotImage image) {
        // 判断是否可以JPG压缩
        if (!mCanJpeg) {
            return false;
        }
        int rst = YuvUtils.convertYV12ToJPEG(image.getData(), (int) (1280 * 720 * 1.5), mJpegData, 1280, 720, 20);
        if (rst == 0) {
            Log.w(TAG(), "send: cannot convert to jpg cause [" + rst + "]");
            return false;
        }

        boolean send = mByteSender.send(mJpegData, 0, rst);
        if (!send) {
            Log.w(TAG(), "send: cannot send [" + mJpegData + "], length = [" + rst + "]");
        }
        return send;
    }

    /**
     * 结束标定
     *
     * @return 结果
     */
    public abstract int finish();

    /**
     * 获取TAG
     *
     * @return TAG
     */
    public abstract String TAG();
}
