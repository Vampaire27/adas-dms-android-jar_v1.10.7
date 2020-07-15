package com.hobot.sample.app.module.communicate;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.adas.sdk.config.ConfigConst;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.sample.app.manager.NebulaSDKManager;
import com.hobot.sdk.library.utils.JsonUtils;
import com.hobot.sdk.library.utils.NumberUtil;
import com.hobot.sdk.library.utils.SharePrefs;
import com.hobot.transfer.common.ErrorCode;
import com.hobot.transfer.common.HobotSocketSDK;
import com.hobot.transfer.common.callback.IProtocolModelCallback;
import com.hobot.transfer.common.model.ProtocolModel;
import com.hobot.transfer.common.protocol.Protocol;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

/**
 * Protocol解析派发类
 *
 * @author Hobot
 */
public class ProtocolProcessor {
    public static final String KEY_STATUS_ADAS_CAL = "KEY_STATUS_ADAS_CAL";
    public static final String KEY_STATUS_DMS_CAL = "KEY_STATUS_DMS_CAL";
    public static final String KEY_STATUS_OPT = "KEY_STATUS_OPT";
    private static final String TAG = "ProtocolProcessor";
    private static final int MSG_PROTOCOL_PROCESS = 0;
    private static final int MSG_CALIBRATION = 2;
    private static final int MSG_STOP_ADAS = 3;
    private static final int MSG_START_ADAS = 4;
    private static final int MSG_STOP_DMS = 5;
    private static final int MSG_START_DMS = 6;
    private static final boolean DEBUG = true;

    private Handler mHandler;
    private WeakReference<Context> mContext;
    private WeakReference<CalibrationStateCallback> mCalCallback;
    private SharePrefs mSp;
    /**
     * Receive CallBack and Send To Work Thread Handler.
     */
    private IProtocolModelCallback mCallback = new IProtocolModelCallback() {
        @Override
        public void onEvent(ProtocolModel model) {
            Log.d(TAG, "onEvent: code = " + 0 + ",model = " + model.toString());
            Message message = mHandler.obtainMessage();
            message.what = 0;
            message.obj = model;
            mHandler.sendMessage(message);
        }

        @Override
        public void onEvent(String msg) {

        }
    };

    public ProtocolProcessor(Context context, CalibrationStateCallback stateCallback) {
        mContext = new WeakReference<>(context);
        mCalCallback = new WeakReference<>(stateCallback);
        this.mSp = new SharePrefs(mContext.get());
    }

    /**
     * 初始化
     */
    public void init() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.setPriority(Thread.MAX_PRIORITY);
        handlerThread.start();
        mHandler = new WorkHandler(handlerThread.getLooper());
    }

    /**
     * 启动
     */
    public void start() {
        HobotSocketSDK.Server.registerProtocolModelCallback(mCallback);
    }

    /**
     * 停止
     */
    public void stop() {
        HobotSocketSDK.Server.unregisterProtocolModelCallback(mCallback);
    }

    /**
     * 反初始化
     */
    public void destroy() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.getLooper().quitSafely();
        mCalCallback = null;
    }

    /**
     * 获取ADAS 配置参数
     *
     * @param code {@link com.hobot.adas.sdk.config.ConfigConst}
     * @return
     */
    private String getAdasConfig(int code) {
        return HobotAdasSDK.getInstance().getAdasConfig(code);
    }

    /**
     * 配置ADAS配置参数
     *
     * @param code  {@link com.hobot.adas.sdk.config.ConfigConst}
     * @param value 值
     * @return
     */
    public boolean setAdasConfig(int code, String value) {
        return HobotAdasSDK.getInstance().setAdasConfig(code, value);
    }

    /**
     * 标定状态回调
     * 用来通知View 状态的变更
     */
    public interface CalibrationStateCallback {
        int CODE_START = 0x101;
        int CODE_STOP = 0x102;
        int CODE_CANCEL = 0x103;
        int CODE_SUCCESS = 0x104;

        void onStateEvent(int code, String msg);
    }

    /**
     * Work Thread Handler to Process MSG.
     */
    public class WorkHandler extends Handler {
        WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            long start = System.currentTimeMillis();
            if (MSG_PROTOCOL_PROCESS == msg.what) {
                ProtocolModel model = (ProtocolModel) msg.obj;
                String type = model.getContent().getType();
                String state = model.getContent().getState();
                String errorCode = model.getErrorCode();
                String key = model.getContent().getKey();
                List<String> valueList = model.getContent().getValue();
                int action = model.getContent().getAction();
                Log.d(TAG, "handleMessage: msg = " + msg.obj);
                switch (type) {
                    // ADAS 命令
                    case Protocol.TYPE_ADAS: {
                        // 获取ADAS信息
                        if (Protocol.STATE_GET.equals(state)) {
                            // 获取ADAS是否开启
                            if (Protocol.KEY_FUCTION.equals(key)) {
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotAdasSDK.getInstance().isStart()));
                            }
                            // 获取 ADAS 报警是否开启
                            else if (Protocol.KEY_AUDIO_ALARM.equals(key)) {
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotWarningSDK.getInstance().getSoundSwitchByGroup("ADAS")));
                            }
                        } else if (Protocol.STATE_SET.equals(state)) {
                            if (Protocol.KEY_FUCTION.equals(key)) {
                                if (Protocol.OPEN == action) {
                                    HobotAdasSDK.getInstance().start();
                                } else {
                                    HobotAdasSDK.getInstance().stop();
                                }
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotAdasSDK.getInstance().isStart()));
                            } else if (Protocol.KEY_AUDIO_ALARM.equals(key)) {
                                HobotWarningSDK.getInstance().setSoundSwitchByGroup("ADAS",
                                        Protocol.OPEN == action);
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotWarningSDK.getInstance().getSoundSwitchByGroup("ADAS")));
                            }
                        }
                        // 回应消息
                        model.getContent().setValue(valueList);
                        HobotSocketSDK.Server.sendMsg(model);
                    }
                    break;
                    // DMS 命令
                    case Protocol.TYPE_DMS: {
                        // 获取DMS参数
                        if (Protocol.STATE_GET.equals(state)) {
                            // 获取DMS是否开启
                            if (Protocol.KEY_FUCTION.equals(key)) {
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotDmsSdk.getInstance().isStart()));
                            }
                            // 获取 DMS 报警是否开启
                            else if (Protocol.KEY_AUDIO_ALARM.equals(key)) {
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotWarningSDK.getInstance().getSoundSwitchByGroup("DMS")));
                            }
                        }
                        // 设置DMS参数
                        else if (Protocol.STATE_SET.equals(state)) {
                            if (Protocol.KEY_FUCTION.equals(key)) {
                                if (Protocol.OPEN == action) {
                                    HobotDmsSdk.getInstance().start();
                                } else {
                                    HobotDmsSdk.getInstance().stop();
                                }
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotDmsSdk.getInstance().isStart()));
                            } else if (Protocol.KEY_AUDIO_ALARM.equals(key)) {
                                HobotWarningSDK.getInstance().setSoundSwitchByGroup("DMS",
                                        Protocol.OPEN == action);
                                valueList =
                                        Collections.singletonList(String.valueOf(HobotWarningSDK.getInstance().getSoundSwitchByGroup("DMS")));
                            }
                        }
                        // 回应消息
                        model.getContent().setValue(valueList);
                        HobotSocketSDK.Server.sendMsg(model);
                    }
                    break;

                    // 状态同步命令
                    case Protocol.TYPE_DEV_STATUS_SYNC: {
                        String adasCal = String.valueOf(mSp.getBoolean(KEY_STATUS_ADAS_CAL, false));
                        String dmsCal = String.valueOf(mSp.getBoolean(KEY_STATUS_DMS_CAL, false));
                        String isOpt = String.valueOf(mSp.getBoolean(KEY_STATUS_OPT, false));
                        HobotSocketSDK.Server.sendMsg(Protocol.sendStatus(null, adasCal, dmsCal,
                                isOpt));
                    }
                    break;

                    // 客户端退出事件 取消标定
                    case Protocol.TYPE_CLIENT_EXIT: {
                        AdasCalibrationManager.getInstance().stop();
                        DmsCalibrationManager.getInstance().stop();
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS, "");
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_DMS, "");
                        mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_CANCEL, "");
                    }
                    break;
                    // 客户端进行参数配置
                    case Protocol.TYPE_DEV_CONFIG: {
                        // 客户端获取参数
                        if (Protocol.STATE_GET.equals(state)) {
                            DeviceWorkingModule dm = new DeviceWorkingModule.Builder()
                                    .ldw_level(NumberUtil.getInteger(getAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_LDW_VALUE)))
                                    .ldw_speed(NumberUtil.getFloat(getAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_LDW_SPEED)))
                                    .hmw_value(NumberUtil.getFloat(getAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_HMW_VALUE)))
                                    .fcw_value(NumberUtil.getFloat(getAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_FCW_VALUE)))
                                    .ldw_enable(NumberUtil.getBoolean(getAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_LDW_ENABLE)) ? 1 : 0)
                                    .build();
                            HobotSocketSDK.Server.sendMsg(Protocol.sendConfig(ErrorCode.SUCCESS.getCode(), JsonUtils.encode(dm)));

                            // 客户端配置参数
                        } else if (Protocol.STATE_SET.equals(state)) {
                            String cfg = valueList.get(0);
                            // 解码
                            DeviceWorkingModule dm = JsonUtils.decodeFormat(cfg,
                                    DeviceWorkingModule.class);

                            // 配置参数
                            setAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_LDW_VALUE,
                                    String.valueOf(dm.getLDW_Level()));
                            setAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_LDW_SPEED,
                                    String.valueOf(dm.getLDW_Speed()));
                            setAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_HMW_VALUE,
                                    String.valueOf(dm.getHMW_Value()));
                            setAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_FCW_VALUE,
                                    String.valueOf(dm.getFCW_Value()));
                            setAdasConfig(com.hobot.adas.sdk.config.ConfigConst.TYPE_LDW_ENABLE,
                                    String.valueOf(dm.getLDW_Enable()));
                            // 设置flag 配置过参数
                            mSp.putCommit(KEY_STATUS_OPT, true);

                            // 发送到客户端 参数设置成功
                            HobotSocketSDK.Server.sendMsg(Protocol.sendConfig(ErrorCode.SUCCESS.getCode()));
                        }

                    }
                    break;
                    // 客户端开启了ADAS标定
                    case Protocol.TYPE_CALIBRATION: {
                        Log.d(TAG, "handleMessage: TYPE_CALIBRATION +");
                        // 客户端首先请求一条开启消息
                        if (Protocol.STATE_START.equals(state)) {
                            // 先通知 客户端 开始接收图像
                            HobotSocketSDK.Server.sendMsg(Protocol.START_IMAGE_RECEIVE);
                            // 设备端调用标定的API
                            int ret = AdasCalibrationManager.getInstance().start(mContext.get());
                            Log.d(TAG, "handleMessage: TYPE_CALIBRATION start ret = " + ret);
                            // 向View回调标定开启成功
                            if (0 == ret) {
                                mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_START, "正在ADAS标定...");
                            }
                        } else if (Protocol.STATE_FINISH.equals(state)) {
                            // 客户端发送完成的指令
                            mSp.putCommit(KEY_STATUS_ADAS_CAL, true);
                            // 调用finish命令
                            AdasCalibrationManager.getInstance().finish();
                            mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_STOP, "");
                        } else if (Protocol.STATE_CANCEL.equals(state)) {
                            // 回复客户端 标定取消成功
                            HobotSocketSDK.Server.sendMsg(Protocol.CALIBRATION_IS_CANCELED);
                            // 客户端发送取消标定的指令
                            AdasCalibrationManager.getInstance().stop();

                            mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_CANCEL,
                                    "");
                        } else if (Protocol.STATE_CRASH.equals(state)) {
                            // 客户端发送自己崩溃的指令
                            AdasCalibrationManager.getInstance().stop();
                            mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_CANCEL,
                                    "");
                        }

                    }
                    break;
                    // DMS 标定
                    case Protocol.TYPE_DMS_CALIBRATION: {
                        // 客户端发送开启标定的指令
                        if (Protocol.STATE_START.equals(state)) {
                            // 先通知 客户端 开始接收图像
                            HobotSocketSDK.Server.sendMsg(Protocol.DMS_START_IMAGE_RECEIVE);
                            // 开启数据发送
                            int ret = DmsCalibrationManager.getInstance().start(mContext.get());

                            Log.d(TAG,
                                    "handleMessage: TYPE_DMS_CALIBRATION start cal ret = " + ret);
                            if (0 == ret) {
                                mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_START, "正在DMS标定...");
                            }
                        } else if (Protocol.STATE_FINISH.equals(state)) {
                            // 客户端请求开启标定
                            DmsCalibrationManager.getInstance().finish();
                            mSp.putCommit(KEY_STATUS_DMS_CAL, true);
                            mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_STOP, "");
                        } else if (Protocol.STATE_CANCEL.equals(state)) {
                            DmsCalibrationManager.getInstance().stop();
                            // 恢复DMS标定已经取消
                            HobotSocketSDK.Server.sendMsg(Protocol.DMS_CALIBRATION_IS_CANCELED);
                            mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_CANCEL,
                                    "");
                        } else if (Protocol.STATE_CRASH.equals(state)) {
                            DmsCalibrationManager.getInstance().stop();
                            mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_CANCEL,
                                    "");
                        }
                        break;
                    }
                    // 客户端回复 开启图像回调
                    case Protocol.TYPE_IMAGE_RECEIVE: {
                        //nop
                        break;
                    }
                    // 客户端回复 开启图像回调
                    case Protocol.TYPE_DMS_IMAGE_RECEIVE: {
                        // nop
                        break;
                    }
                    // 车辆设置和Camera设置
                    case Protocol.TYPE_VEHICLE_SETTING:
                    case Protocol.TYPE_CAMERA_SETTING: {
                        int configKet = -1;
                        switch (key) {
                            case Protocol.KEY_CAMERA_HORIZON:
                                configKet = ConfigConst.TYPE_CAL_CAMERA_HORIZON;
                                break;
                            case Protocol.KEY_CAMERA_HEIGHT:
                                configKet = ConfigConst.TYPE_CAL_CAMERA_HEIGHT;
                                break;
                            case Protocol.KEY_CAMERA_PITCH:
                                configKet = ConfigConst.TYPE_CAL_CAMERA_PITCH;
                                break;
                            case Protocol.KEY_CAMERA_YAW:
                                configKet = ConfigConst.TYPE_CAL_CAMERA_YAW;
                                break;
                            case Protocol.KEY_CAMERA_ROLL:
                                configKet = ConfigConst.TYPE_CAL_CAMERA_ROLL;
                                break;
                            case Protocol.KEY_VEHICLE_WIDTH:
                                configKet = ConfigConst.TYPE_CAL_VEHICLE_WIDTH;
                                break;
                            case Protocol.KEY_VEHICLE_BACK_AXLE_POS:
                                configKet = ConfigConst.TYPE_CAL_VEHICLE_BACK_AXLE_POS;
                                break;
                            case Protocol.KEY_VEHICLE_WHEEL_BASE:
                                configKet = ConfigConst.TYPE_CAL_VEHICLE_WHEEL_BASE;
                                break;
                            case Protocol.KEY_CAMERA_X:
                                configKet = ConfigConst.TYPE_CAL_CAMERA_X;
                                break;
                        }
                        if (Protocol.STATE_GET.equals(state)) {
                            String value =
                                    String.valueOf(HobotAdasSDK.getInstance().getAdasConfig(configKet));
                            model.getContent().setValue(Collections.singletonList(value));
                            HobotSocketSDK.Server.sendMsg(
                                    model
                            );
                        } else {
                            HobotAdasSDK.getInstance().setAdasConfig(configKet, valueList.get(0));
                        }
                    }
                    break;
                }
            } else if (MSG_CALIBRATION == msg.what) {
                switch (msg.arg1) {
                    case 0:
                        //mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_STOP, "");
                        break;
                    case -1:
                        mCalCallback.get().onStateEvent(CalibrationStateCallback.CODE_STOP, "");
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_DMS, "");
                        break;
                }
            } else if (MSG_STOP_ADAS == msg.what) {
                NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS, "");
            } else if (MSG_START_ADAS == msg.what) {
                NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS, "");
            } else if (MSG_STOP_DMS == msg.what) {
                NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_DMS, "");
            } else if (MSG_START_DMS == msg.what) {
                NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_DMS, "");
            }

            long end = System.currentTimeMillis();
            if (DEBUG) {
                Log.i(TAG,
                        "handleMessage: process cost [" + (end - start) + "] ,msg [" + msg.what + "," + msg.obj + "] ");
            }
        }
    }
}