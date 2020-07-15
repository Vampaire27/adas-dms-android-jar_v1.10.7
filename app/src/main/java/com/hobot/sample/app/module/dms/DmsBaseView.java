package com.hobot.sample.app.module.dms;

import android.content.Context;
import android.graphics.ImageFormat;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hobot.camera.library.base.Option;
import com.hobot.dms.sdk.ErrorCode;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.dms.sdk.listener.IDmsProtoListener;
import com.hobot.dms.sdk.listener.IDmsRenderListener;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.nebula.common.proxy.EventProtoProxy;
import com.hobot.sample.app.R;
import com.hobot.sample.app.activity.BaseNewMainActivity;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.listener.IDmsStateListener;
import com.hobot.sample.app.listener.ITurnLightListener;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.manager.NebulaSDKManager;
import com.hobot.sample.app.module.base.BaseView;
import com.hobot.sample.app.view.BaseCalibrationView;
import com.hobot.sdk.widget.preview.OpenPreview;
import com.hobot.sdk.widget.preview.image.ImageFrame;

import java.util.ArrayList;
import java.util.List;

import DMSOutputProtocol.DMSSDKOutputOuterClass;

/**
 * DMS 基础控件。
 *
 * @author Hobot
 */
public class DmsBaseView extends BaseView implements IDmsRenderListener, IDmsProtoListener,
        BaseCalibrationView.OnStepListener, ITurnLightListener, IDmsStateListener {
    private static final String TAG = "DmsBaseView";
    private OpenPreview mPreview;
    private DmsMaskerView mDmsMaskerView;
    private DmsExhibitionView mDmsExhibitionView;
    private ImageButton mDmsCalibIb;
    private DmsCalibrationView mDmsCalibrationView;
    private View mLogoLayout;
    // 左转灯
    private int mLeftTurnLightState = HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_OFF;
    // 右转灯
    private int mRightTurnLightState = HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_OFF;

    public DmsBaseView() {
        mCameraId = DefaultConfig.DEFAULT_DMS_CAMERA_ID;
        mCameraType = DefaultConfig.DEFAULT_DMS_CAMERA_TYPE;
    }

    @Override
    public int layoutId() {
        return R.layout.view_dms_layout;
    }

    @Override
    public void onViewCreated(View view) {
        mPreview = (OpenPreview) view.findViewById(R.id.preview_dms);
        mPreview.setFormat(ImageFormat.UNKNOWN);
        mDmsMaskerView = (DmsMaskerView) view.findViewById(R.id.masker_view_dms);
        mDmsExhibitionView = (DmsExhibitionView) view.findViewById(R.id.exhibition_view_dms);
        mDmsCalibrationView = (DmsCalibrationView) view.findViewById(R.id.acv_dms_cali);
        mDmsCalibIb = (ImageButton) view.findViewById(R.id.dms_calib_iv);
        mLogoLayout = view.findViewById(R.id.logo_layout);
        mDmsCalibIb.setOnClickListener(this);
        mDmsCalibrationView.setOnStepListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        HobotDmsSdk.getInstance().registerProtoListener(this);
        HobotDmsSdk.getInstance().registerRenderListener(this);
        NebulaObservableManager.getInstance().registerTurnLightListener(this);
        NebulaObservableManager.getInstance().registerDmsStateListener(this);
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_DMS);
    }

    @Override
    public void onStop() {
        super.onStop();
        HobotDmsSdk.getInstance().unregisterRenderListener(this);
        HobotDmsSdk.getInstance().unregisterProtoListener(this);
        NebulaObservableManager.getInstance().unregisterTurnLightListener(this);
        NebulaObservableManager.getInstance().unregisterDmsStateListener(this);
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_DMS);
    }

    @Override
    public void onViewRelease() {
        mDmsCalibrationView.setOnStepListener(null);
        mDmsCalibrationView.release();
        mDmsCalibrationView = null;
        mDmsExhibitionView.release();
        mDmsExhibitionView = null;
        mDmsMaskerView.release();
        mDmsMaskerView = null;
    }

    @Override
    public void onRenderState(boolean isShow) {
        if (isShow) {
            mDmsMaskerView.setVisibility(View.VISIBLE);
        } else {
            mDmsMaskerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDVRState(boolean isShow) {
        if (isShow) {
            mPreview.setVisibility(View.VISIBLE);
        } else {
            mPreview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onExhibitionState(boolean isShow) {
        super.onExhibitionState(isShow);
        Log.d(TAG, "onExhibitionState isShow = " + isShow);
        if (isShow) {
            mDmsExhibitionView.setVisibility(View.VISIBLE);
            mPreview.setVisibility(View.VISIBLE);
            mLogoLayout.setVisibility(View.GONE);
            mDmsCalibIb.setImageResource(R.drawable.ic_calibration_dms_show);
            mDmsCalibIb.setBackgroundColor(0x3300D0BB);
        } else {
            mDmsCalibIb.setImageResource(R.drawable.ic_calibration_dms);
            mDmsCalibIb.setBackgroundColor(0x3358D2FC);
            mDmsExhibitionView.setVisibility(View.GONE);
            mPreview.setVisibility(View.GONE);
            mLogoLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDmsFrame(byte[] bytes, int i) {
        processImage(bytes, i);
    }

    @Override
    public void onProtoOut(DMSSDKOutputOuterClass.DMSSDKOutput output) {
        final DMSSDKOutputOuterClass.DMSSDKOutput finalOutput = output;
        if (mDmsMaskerView != null) {
            mDmsMaskerView.process(finalOutput);
        }
        if (mDmsExhibitionView != null) {
            mDmsExhibitionView.process(finalOutput);
        }

        List<DMSSDKOutputOuterClass.EventResult> results = finalOutput.getEventResultList();
        if (results.size() == 0) {
            return;
        }
        for (DMSSDKOutputOuterClass.EventResult result : results) {
            if (result.getEvent() == DMSSDKOutputOuterClass.EventEnum.EVENT_CALIB) {
                return;
            }
        }
        // 如果当前转向灯信号是开的状态
        final int leftTurnLightState = mLeftTurnLightState;
        final int rightTurnLightState = mRightTurnLightState;
        // 获取所有DMS事件，移除DDW相关的事件
        final List<DMSSDKOutputOuterClass.EventResult> tempResults = new ArrayList<>(results);
        for (DMSSDKOutputOuterClass.EventResult result : results) {

            int number = result.getEvent().getNumber();
            switch (number) {
//                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_D_VALUE:
//                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_U_VALUE:
                case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_L_VALUE:
                    if (leftTurnLightState == HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_LEFT) {
                        tempResults.remove(result);
                    }
                    break;
                case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_R_VALUE:
                    if (rightTurnLightState == HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_RIGHT) {
                        tempResults.remove(result);
                    }
                    break;
                // 如果当前是DAA事件，需要判断当前的镜头是否遮挡
                case DMSSDKOutputOuterClass.EventEnum.EVENT_DAA_VALUE: {
                    if (DefaultConfig.SUPPORT_IMAGE_QUALITY) {
                        NebulaObservableManager.getInstance().onCheckShelter(result.getAttachImgPath());
                        tempResults.remove(result);
                    }
                    break;
                }
                default:
                    break;
            }
        }
        // 移除之后如果没有事件，跳出该逻辑。
        if (tempResults.size() == 0) {
            return;
        }

        final EventProtoProxy protoProxy = new EventProtoProxy() {
            @Override
            public int eventProtoPos(int index) {
                return tempResults.get(index).getEvent().getNumber();
            }

            @Override
            public String eventProtoName(int index) {
                return tempResults.get(index).getEvent().name();
            }

            @Override
            public int eventNum() {
                return tempResults.size();
            }

            @Override
            public int frameId(int index) {
                return finalOutput.getFrameId();
            }

            @Override
            public String filePath(int index) {
                return tempResults.get(index).getAttachImgPath();
            }

            @Override
            public long eventTime(int index) {
                return finalOutput.getTimestamp();
            }
        };
        HobotWarningSDK.getInstance().warn(protoProxy);
        HobotWarningSDK.getInstance().upload(protoProxy);
    }



    boolean push = true;

    @Override
    public void onFrame(int camera, byte[] data, long timestamp, Option option) {
        if(!push){
            push =true;
            return;
        }
        push = false;
        if (DefaultConfig.SUPPORT_FAKE_SPEED) {
            float speed = mFakeSpeed;
            HobotDmsSdk.getInstance().feedCanInfo(0, (int) speed, 0, timestamp);
            setSpeed(speed);
        }
        // 喂Camera数据
        int width = option.previewWidth;
        int height = option.previewHeight;
        HobotDmsSdk.getInstance().feedImage(data, width, height, option.format, timestamp);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.dms_calib_iv: {
                if (!HobotDmsSdk.getInstance().isInit() && !HobotDmsSdk.getInstance().isStart()) {
                    Toast.makeText(mContext, R.string.calibration_error_dms_not_run,
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick() called dms not run,with: v = [" + v + "]");
                    return;
                }
                mDmsCalibIb.setVisibility(View.GONE);
                mDmsCalibrationView.startCalibration();
            }
            break;
        }
    }

    @Override
    public void onStep(int index) {
        switch (index) {
            case BaseCalibrationView.STATE_FINISH: {
                mDmsCalibIb.setVisibility(View.VISIBLE);
                break;
            }
            case BaseCalibrationView.STATE_CANCEL: {
                mDmsCalibIb.setVisibility(View.VISIBLE);
                break;
            }
            case BaseCalibrationView.STATE_FAIL: {
                mDmsCalibIb.setVisibility(View.VISIBLE);
                break;
            }
            case BaseCalibrationView.STATE_START: {
                break;
            }
            default: {
                break;
            }
        }
    }


    @Override
    public void onTurnLightChange(int direction) {
        switch (direction) {
            case NebulaObservableManager.TURN_LIGHT_CLOSE:
                mLeftTurnLightState = HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_OFF;
                mRightTurnLightState = HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_OFF;
                break;
            case NebulaObservableManager.TURN_LIGHT_LEFT:
                mLeftTurnLightState = HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_LEFT;
                break;
            case NebulaObservableManager.TURN_LIGHT_RIGHT:
                mRightTurnLightState = HobotDmsSdk.TURN_LIGHT_STATE.TURN_LIGHT_RIGHT;
                break;
        }
    }

    @Override
    public void onDmsError(final int code) {
        if (code == ErrorCode.SUCCESS) {
            return;
        }
        // 保留本地变量，防止mContext被置空
        final Context context = mContext;
        if (context instanceof BaseNewMainActivity) {
            // 防止Activity已经退出
            if (!((BaseNewMainActivity) context).isFinishing()) {
                ((BaseNewMainActivity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((BaseNewMainActivity) context).showErrorDialog(context.getString(R.string.dms_start_failed),
                                ErrorCode.parseCode(code));
                    }
                });
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!DefaultConfig.SUPPORT_FAKE_SPEED) {
            float speed = location.getSpeed() * 3.6f; // km/h
            HobotDmsSdk.getInstance().feedCanInfo(0, (int) speed, 0, location.getTime());
            setSpeed(speed);
        }
    }

    @Override
    public void onTestModeChanged(boolean isEnable) {
        super.onTestModeChanged(isEnable);
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_DMS);
        if (isEnable) {
            HobotDmsSdk.getInstance().initConfig(mContext, "etc_test");
        } else {
            HobotDmsSdk.getInstance().initConfig(mContext, "etc_common");
        }
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_DMS);

    }

    /**
     * 处理图像
     */
    private void processImage(byte[] image, int length) {
        if (mPreview.getVisibility() != View.VISIBLE) {
            return;
        }
        ImageFrame frame = mPreview.getImageFrame();
        if (null == frame) {
            return;
        }
        System.arraycopy(image, 0, frame.data, 0, length);
        frame.id = System.currentTimeMillis();
        frame.width = 1280;
        frame.height = 720;
        frame.color = ImageFormat.YV12;
        frame.size = length;
        mPreview.queue(frame);
    }
}
