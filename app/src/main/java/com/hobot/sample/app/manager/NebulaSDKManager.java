package com.hobot.sample.app.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.adas.sdk.manager.AdasManager;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.dms.sdk.manager.DmsManager;
import com.hobot.sample.app.config.DefaultConfig;

/**
 * SDK管理类，主要用于控制SDK的生命周期。
 *
 * @author Hobot
 */
public class NebulaSDKManager {
    public static final int CODE_START_ADAS = 0x001;
    public static final int CODE_START_DMS = 0x002;
    public static final int CODE_STOP_ADAS = 0x003;
    public static final int CODE_STOP_DMS = 0x004;

    private static final String TAG = "NebulaSDKManager";
    private static NebulaSDKManager sInstance;
    private WorkHandler mAdasHandler;
    private WorkHandler mDmsHandler;

    private NebulaSDKManager() {
        HandlerThread adasThread = new HandlerThread(AdasManager.TAG);
        adasThread.start();
        mAdasHandler = new WorkHandler(adasThread.getLooper());
        HandlerThread dmsThread = new HandlerThread(DmsManager.TAG);
        dmsThread.start();
        mDmsHandler = new WorkHandler(dmsThread.getLooper());
    }

    public static NebulaSDKManager getInstance() {
        if (sInstance == null) {
            synchronized (NebulaSDKManager.class) {
                if (sInstance == null) {
                    sInstance = new NebulaSDKManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 发送消息
     *
     * @param code
     */
    public void postToHandler(int code) {
        postToHandler(code, "");
    }

    /**
     * 发送消息
     *
     * @param code
     * @param msg
     */
    public void postToHandler(int code, String msg) {
        Log.d(TAG, "postToHandler: code = " + code + ", msg = " + msg);
        switch (code) {
            case CODE_START_ADAS:
            case CODE_STOP_ADAS:
                if (mAdasHandler != null) {
                    Message message = mAdasHandler.obtainMessage();
                    message.what = code;
                    message.obj = msg;
                    mAdasHandler.sendMessage(message);
                }
                break;
            case CODE_START_DMS:
            case CODE_STOP_DMS:
                if (mDmsHandler != null) {
                    Message message = mDmsHandler.obtainMessage();
                    message.what = code;
                    message.obj = msg;
                    mDmsHandler.sendMessage(message);
                }
                break;
        }
    }

    private Object mutex = new Object();

   private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_START_ADAS: {
                    synchronized (mutex) {
                        if (HobotAdasSDK.getInstance().isStart()) {
                            return;
                        }
                        Log.d(TAG, "handleMessage: start ");
                        int adasRet = HobotAdasSDK.getInstance().start();
                        Log.d(TAG, "handleMessage: start adas end adasRet = " + adasRet);
                        NebulaObservableManager.getInstance().onAdasError(adasRet);
                    }
                    break;
                }
                case CODE_START_DMS: {
                    synchronized (mutex) {
                        if (HobotDmsSdk.getInstance().isStart()) {
                            return;
                        }
                        Log.d(TAG, "handleMessage: start dms");
                        int dmsRet = HobotDmsSdk.getInstance().start();
                        Log.d(TAG, "handleMessage: start dms end dmsRet = " + dmsRet);
                        NebulaObservableManager.getInstance().onDmsError(dmsRet);
                    }
                    break;
                }
                case CODE_STOP_ADAS: {
                    Log.d(TAG, "handleMessage: stop adas");
                    synchronized (mutex) {
                        HobotAdasSDK.getInstance().stop();
                    }
                    break;
                }
                case CODE_STOP_DMS: {
                    Log.d(TAG, "handleMessage: stop dms");
                    synchronized (mutex) {
                        HobotDmsSdk.getInstance().stop();
                    }
                    break;
                }
            }
        }
    }
}
