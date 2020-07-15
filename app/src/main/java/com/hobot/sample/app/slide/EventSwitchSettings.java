package com.hobot.sample.app.slide;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.camera.library.base.ICameraAPI;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.sample.app.R;
import com.hobot.sample.app.config.Constants;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.manager.NebulaSDKManager;
import com.hobot.sample.app.util.CameraHelper;
import com.hobot.sdk.library.utils.SharePrefs;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用事件转换控件。
 *
 * @author Hobot
 */
public class EventSwitchSettings extends RelativeLayout implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = EventSwitchSettings.class.getSimpleName();

    private Switch mAdasSwitch;
    private Switch mDmsSwitch;
    private Switch mExhibitionSwitch;
    private Switch mAdasSoundSwitch;
    private Switch mDmsSoundSwitch;
    private Switch mDvrShowSwitch;
    private LinearLayout mImageShowLayout;
    private Switch mRenderShowSwitch;
    private Switch mSpeedSwitch;
    private Switch mPreviewSwitch;
    private Switch mPerformanceLogSwitch;
    private Switch mTestModeSwitch;
    private Switch mOverStandardSwitch;

    private Switch mSupportAdasSwitch;
    private Switch mSupportDmsSwitch;
    private Switch mSupportFaceIdSwitch;
    private Switch mSupportFaceCloudSwitch;
    private Switch mSupportSignInSwitch;
    private Switch mSupportLivingSwitch;
    private Switch mSupportImageQualitySwitch;
    private Switch mSupportSpeechSwitch;
    private Switch mSupportNetTransferSwitch;
    private Switch mSupportInrSwitch;
    private Switch mSupportUploadSwitch;
    private Switch mSupportWheelSwitch;
    private Switch mSupportPaasSwitch;

    private SparseArray<Switch> mCameraSwitch = new SparseArray<>();
    private List<ICameraAPI> mCameras = new ArrayList<>();

    private SharePrefs mSP;

    public EventSwitchSettings(Context context) {
        this(context, null);
    }

    public EventSwitchSettings(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventSwitchSettings(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSP = new SharePrefs(context);
        inflate(context, R.layout.view_event_switch_layout, this);
        initView(this);
    }

    /**
     * 初始化数据
     */
    public void initData() {
        // 同步开关状态
        initListeners(false);
        // SDK开关
        mAdasSwitch.setChecked(HobotAdasSDK.getInstance().isStart());
        mDmsSwitch.setChecked(HobotDmsSdk.getInstance().isStart());
        // 报警声音开关
        mAdasSoundSwitch.setChecked(HobotWarningSDK.getInstance().getSoundSwitchByGroup("ADAS"));
        mDmsSoundSwitch.setChecked(HobotWarningSDK.getInstance().getSoundSwitchByGroup("DMS"));
        // 1.移除旧View
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        if (mCameraSwitch.size() > 0) {
            for (int i = 0; i < mCameraSwitch.size(); i++) {
                container.removeView(mCameraSwitch.get(i));
            }
        }
        // 2.清空旧的数据
        mCameraSwitch.clear();
        mCameras.clear();
        // 3.添加新的数据
        mCameras.addAll(CameraHelper.getCameras());
        int count = mCameras.size();
        for (int i = 0; i < count; i++) {
            Switch cameraSwitch = (Switch) inflate(getContext(), R.layout.view_switch_layout, null);
            ICameraAPI cameraAPI = mCameras.get(i);
            cameraSwitch.setText(getContext().getString(R.string.slide_action_camera_switch,
                    cameraAPI.getCameraName()));
            container.addView(cameraSwitch);
            mCameraSwitch.put(i, cameraSwitch);
            cameraSwitch.setChecked(cameraAPI.isPreviewed());
        }

        // 功能支持开关
        mSupportAdasSwitch.setChecked(DefaultConfig.SUPPORT_ADAS);
        mSupportDmsSwitch.setChecked(DefaultConfig.SUPPORT_DMS);
        mSupportFaceIdSwitch.setChecked(DefaultConfig.SUPPORT_FACE_ID);
        mSupportFaceCloudSwitch.setChecked(DefaultConfig.SUPPORT_FACE_CLOUD);
        mSupportSignInSwitch.setChecked(DefaultConfig.SUPPORT_SIGN_IN);
        mSupportLivingSwitch.setChecked(DefaultConfig.SUPPORT_LIVING);
        mSupportImageQualitySwitch.setChecked(DefaultConfig.SUPPORT_IMAGE_QUALITY);
        mSupportSpeechSwitch.setChecked(DefaultConfig.SUPPORT_SPEECH);
        mSupportNetTransferSwitch.setChecked(DefaultConfig.SUPPORT_NET_TRANSFER_SERVER);
        mSupportInrSwitch.setChecked(DefaultConfig.SUPPORT_INR);
        mSupportUploadSwitch.setChecked(DefaultConfig.SUPPORT_UPLOAD);
        mSupportPaasSwitch.setChecked(DefaultConfig.SUPPORT_PAAS);
        mSupportWheelSwitch.setChecked(DefaultConfig.SUPPORT_WHEEL);

        // 绘制开关
        mPreviewSwitch.setChecked(DefaultConfig.PREVIEW_SHOW_SWITCH);
        mRenderShowSwitch.setChecked(DefaultConfig.RENDER_SHOW_SWITCH);
        mDvrShowSwitch.setChecked(DefaultConfig.DVR_SHOW_SWITCH);
        mSpeedSwitch.setChecked(DefaultConfig.SPEED_RENDER_SWITCH);
        mImageShowLayout.setVisibility(DefaultConfig.PREVIEW_SHOW_SWITCH ? VISIBLE : GONE);

        // 展会模式
        mExhibitionSwitch.setChecked(DefaultConfig.EXHIBITION_SHOW_SWITCH);
        // 性能显示开关
        mPerformanceLogSwitch.setChecked(DefaultConfig.PERFORMANCE_LOG_SWITCH);
        // 测试模式开关
        mTestModeSwitch.setChecked(DefaultConfig.TEST_MODE_SWITCH);
        //过标模式开关
        mOverStandardSwitch.setChecked(DefaultConfig.OVER_Standard_SWITCH);
        initListeners(true);
    }

    /**
     * 释放
     */
    public void release() {
        initListeners(false);
        mCameraSwitch.clear();
        mCameras.clear();
    }

    /**
     * 初始化布局
     *
     * @param view
     */
    private void initView(View view) {
        mAdasSwitch = (Switch) view.findViewById(R.id.sw_adas);
        mDmsSwitch = (Switch) view.findViewById(R.id.sw_dms);
        mExhibitionSwitch = (Switch) view.findViewById(R.id.sw_exhibition);
        mAdasSoundSwitch = (Switch) view.findViewById(R.id.sw_sound_adas);
        mDmsSoundSwitch = (Switch) view.findViewById(R.id.sw_sound_dms);
        mPreviewSwitch = (Switch) view.findViewById(R.id.sw_preview_show);
        mRenderShowSwitch = (Switch) view.findViewById(R.id.sw_render_show);
        mDvrShowSwitch = (Switch) view.findViewById(R.id.sw_dvr_show);
        mSpeedSwitch = (Switch) view.findViewById(R.id.sw_speed_show);
        mImageShowLayout = (LinearLayout) view.findViewById(R.id.ll_image_show);
        mPerformanceLogSwitch = (Switch) view.findViewById(R.id.sw_performance_log);
        mTestModeSwitch = (Switch) view.findViewById(R.id.sw_test_mode);
        mOverStandardSwitch = (Switch) view.findViewById(R.id.sw_over_standard_mode);

        mSupportAdasSwitch = (Switch) view.findViewById(R.id.sw_support_adas);
        mSupportDmsSwitch = (Switch) view.findViewById(R.id.sw_support_dms);
        mSupportFaceIdSwitch = (Switch) view.findViewById(R.id.sw_support_face_id);
        mSupportFaceCloudSwitch = (Switch) view.findViewById(R.id.sw_support_face_cloud);
        mSupportSignInSwitch = (Switch) view.findViewById(R.id.sw_support_sign_in);
        mSupportLivingSwitch = (Switch) view.findViewById(R.id.sw_support_living);
        mSupportImageQualitySwitch = (Switch) view.findViewById(R.id.sw_support_image_quality);
        mSupportSpeechSwitch = (Switch) view.findViewById(R.id.sw_support_speech);
        mSupportNetTransferSwitch = (Switch) view.findViewById(R.id.sw_support_net_transfer);
        mSupportInrSwitch = (Switch) view.findViewById(R.id.sw_support_inr);
        mSupportUploadSwitch = (Switch) view.findViewById(R.id.sw_support_upload);
        mSupportWheelSwitch = (Switch) view.findViewById(R.id.sw_support_wheel);
        mSupportPaasSwitch = (Switch) view.findViewById(R.id.sw_support_paas);
    }

    /**
     * 初始化监听
     *
     * @param add 是否添加
     */
    public void initListeners(boolean add) {
        mAdasSwitch.setOnCheckedChangeListener(add ? this : null);
        mDmsSwitch.setOnCheckedChangeListener(add ? this : null);
        mExhibitionSwitch.setOnCheckedChangeListener(add ? this : null);
        mAdasSoundSwitch.setOnCheckedChangeListener(add ? this : null);
        mDmsSoundSwitch.setOnCheckedChangeListener(add ? this : null);
        mPreviewSwitch.setOnCheckedChangeListener(add ? this : null);
        mRenderShowSwitch.setOnCheckedChangeListener(add ? this : null);
        mDvrShowSwitch.setOnCheckedChangeListener(add ? this : null);
        mSpeedSwitch.setOnCheckedChangeListener(add ? this : null);
        mPerformanceLogSwitch.setOnCheckedChangeListener(add ? this : null);
        mTestModeSwitch.setOnCheckedChangeListener(add ? this : null);
        mOverStandardSwitch.setOnCheckedChangeListener(add ? this : null);
        for (int i = 0; i < mCameraSwitch.size(); i++) {
            Switch camera = mCameraSwitch.get(i);
            final ICameraAPI cameraAPI = mCameras.get(i);
            camera.setOnCheckedChangeListener(add ? new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        CameraHelper.openCamera(cameraAPI, true);
                    } else {
                        CameraHelper.closeCamera(cameraAPI, true);
                    }
                }
            } : null);
        }

        mSupportAdasSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportDmsSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportFaceIdSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportFaceCloudSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportSignInSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportLivingSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportImageQualitySwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportSpeechSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportNetTransferSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportInrSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportUploadSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportPaasSwitch.setOnCheckedChangeListener(add ? this : null);
        mSupportWheelSwitch.setOnCheckedChangeListener(add ? this : null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged() called with: buttonView = [" + buttonView + "], isChecked " +
                "= [" + isChecked + "]");
        switch (buttonView.getId()) {
            case R.id.sw_adas: {
                if (isChecked) {
                    NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS
                            , "");
                } else {
                    NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS,
                            "");
                }
                break;
            }
            case R.id.sw_dms: {
                if (isChecked) {
                    NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_DMS,
                            "");
                } else {
                    NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_DMS,
                            "");
                }
                break;
            }
            case R.id.sw_exhibition: {
                if (isChecked && mPreviewSwitch.isChecked()) {
                    mPreviewSwitch.setChecked(false);
                    mSP.putCommit(Constants.KEY_PREVIEW_SHOW_SWITCH, false);
                    DefaultConfig.PREVIEW_SHOW_SWITCH = false;
                }
                DefaultConfig.EXHIBITION_SHOW_SWITCH = isChecked;
                mSP.putCommit(Constants.KEY_EXHIBITION_SHOW_SWITCH, isChecked);
                NebulaObservableManager.getInstance().onExhibitionState(isChecked);
                break;
            }
            case R.id.sw_sound_adas: {
                HobotWarningSDK.getInstance().setSoundSwitchByGroup("ADAS", isChecked);
                break;
            }
            case R.id.sw_sound_dms: {
                HobotWarningSDK.getInstance().setSoundSwitchByGroup("DMS", isChecked);
                break;
            }
            //控制preview是否显示
            case R.id.sw_preview_show: {
                if (isChecked && mExhibitionSwitch.isChecked()) {
                    mExhibitionSwitch.setChecked(false);
                    DefaultConfig.EXHIBITION_SHOW_SWITCH = false;
                    mSP.putCommit(Constants.KEY_EXHIBITION_SHOW_SWITCH, false);
                }
                mSP.putCommit(Constants.KEY_PREVIEW_SHOW_SWITCH, isChecked);
                DefaultConfig.PREVIEW_SHOW_SWITCH = isChecked;
                //显示/隐藏控制图像显示的开关界面
                if (isChecked) {
                    mImageShowLayout.setVisibility(View.VISIBLE);
                } else {
                    mImageShowLayout.setVisibility(View.GONE);
                }

                // 2018-11-05 显示adas/dms界面
                NebulaObservableManager.getInstance().onPreviewState(isChecked);
                if (!isChecked) {
                    mRenderShowSwitch.setChecked(false);
                    mDvrShowSwitch.setChecked(false);
                }
                break;
            }
            //控制车道线等图像绘制
            case R.id.sw_render_show: {
                mSP.putCommit(Constants.KEY_RENDER_SHOW_SWITCH, isChecked);
                NebulaObservableManager.getInstance().onRenderState(isChecked);
                DefaultConfig.RENDER_SHOW_SWITCH = isChecked;
                break;
            }
            //控制dvr图像是否绘制
            case R.id.sw_dvr_show: {
                mSP.putCommit(Constants.KEY_DVR_SHOW_SWITCH, isChecked);
                NebulaObservableManager.getInstance().onDVRState(isChecked);
                DefaultConfig.DVR_SHOW_SWITCH = isChecked;
                break;
            }
            case R.id.sw_speed_show: {
                mSP.putCommit(Constants.KEY_SPEED_RENDER_SWITCH, isChecked);
                NebulaObservableManager.getInstance().onSpeedState(isChecked);
                DefaultConfig.SPEED_RENDER_SWITCH = isChecked;
                break;
            }
            case R.id.sw_performance_log: {
                mSP.putCommit(Constants.KEY_PERFORMANCE_LOG_SWITCH, isChecked);
                NebulaObservableManager.getInstance().onPerformanceLogSwitchChanged(isChecked);
                DefaultConfig.PERFORMANCE_LOG_SWITCH = isChecked;
                break;
            }
            case R.id.sw_test_mode: {
                mSP.putCommit(Constants.KEY_TEST_MODE_SWITCH, isChecked);
                DefaultConfig.TEST_MODE_SWITCH = isChecked;
                NebulaObservableManager.getInstance().onTestModeChanged(isChecked);
                break;
            }
            case R.id.sw_over_standard_mode: {
                mSP.putCommit(Constants.KEY_OVER_STANDARD_MODE_SWITCH, isChecked);
                NebulaObservableManager.getInstance().onOverStandardModeChanged(isChecked);
                DefaultConfig.OVER_Standard_SWITCH = isChecked;
                break;
            }

            case R.id.sw_support_adas: {
                mSP.putCommit(Constants.KEY_SUPPORT_ADAS_SWITCH, isChecked);
                DefaultConfig.SUPPORT_ADAS = isChecked;
                break;
            }
            case R.id.sw_support_dms: {
                mSP.putCommit(Constants.KEY_SUPPORT_DMS_SWITCH, isChecked);
                DefaultConfig.SUPPORT_DMS = isChecked;
                break;
            }
            case R.id.sw_support_face_id: {
                mSP.putCommit(Constants.KEY_SUPPORT_FACE_ID_SWITCH, isChecked);
                DefaultConfig.SUPPORT_FACE_ID = isChecked;
                break;
            }
            case R.id.sw_support_face_cloud: {
                mSP.putCommit(Constants.KEY_SUPPORT_FACE_CLOUD_SWITCH, isChecked);
                DefaultConfig.SUPPORT_FACE_CLOUD = isChecked;
                break;
            }
            case R.id.sw_support_sign_in: {
                mSP.putCommit(Constants.KEY_SUPPORT_SIGN_IN_SWITCH, isChecked);
                DefaultConfig.SUPPORT_SIGN_IN = isChecked;
                break;
            }
            case R.id.sw_support_living: {
                mSP.putCommit(Constants.KEY_SUPPORT_LIVING_SWITCH, isChecked);
                DefaultConfig.SUPPORT_LIVING = isChecked;
                break;
            }
            case R.id.sw_support_image_quality: {
                mSP.putCommit(Constants.KEY_SUPPORT_IMAGE_QUALITY_SWITCH, isChecked);
                DefaultConfig.SUPPORT_IMAGE_QUALITY = isChecked;
                break;
            }
            case R.id.sw_support_speech: {
                mSP.putCommit(Constants.KEY_SUPPORT_SPEECH_SWITCH, isChecked);
                DefaultConfig.SUPPORT_SPEECH = isChecked;
                break;
            }
            case R.id.sw_support_net_transfer: {
                mSP.putCommit(Constants.KEY_SUPPORT_NET_TRANSFER_SWITCH, isChecked);
                DefaultConfig.SUPPORT_NET_TRANSFER_SERVER = isChecked;
                break;
            }
            case R.id.sw_support_inr: {
                mSP.putCommit(Constants.KEY_SUPPORT_INR, isChecked);
                DefaultConfig.SUPPORT_INR = isChecked;
                break;
            }
            case R.id.sw_support_upload: {
                mSP.putCommit(Constants.KEY_SUPPORT_UPLOAD, isChecked);
                DefaultConfig.SUPPORT_UPLOAD = isChecked;
                break;
            }
            case R.id.sw_support_paas: {
                mSP.putCommit(Constants.KEY_SUPPORT_PAAS, isChecked);
                DefaultConfig.SUPPORT_PAAS = isChecked;
                break;
            }
            case R.id.sw_support_wheel: {
                mSP.putCommit(Constants.KEY_SUPPORT_WHEEL, isChecked);
                DefaultConfig.SUPPORT_WHEEL = isChecked;
                break;
            }
            default: {
                break;
            }
        }
    }
}
