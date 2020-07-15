package com.hobot.sample.app.slide.view.pager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.adas.sdk.config.ConfigConst;
import com.hobot.adas.sdk.config.ConfigField;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.sample.app.R;
import com.hobot.sample.app.activity.SettingActivity;
import com.hobot.sample.app.config.Constants;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.fragment.MainSettingFragment;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.slide.view.base.BaseView;
import com.hobot.sdk.library.utils.NumberUtil;
import com.hobot.sdk.library.utils.SharePrefs;

import java.io.File;

/**
 * 通用设置界面
 * 现包含: 运行模式选择设置，报警时间间隔设置，速度限制开关选择
 */
public class EventSelectorView extends BaseView implements SeekBar.OnSeekBarChangeListener,
        CompoundButton.OnCheckedChangeListener, ConfigField {
    private static final String TAG = "EventSelectorView";

    /**
     * 默认模式
     */
    private static final int MODEL_GPS_ONLY = 3;

    /**
     * detect self car movement
     */
    private static final int MODEL_SELF_DETECT = 4;

    // ADAS
    private RadioGroup mAdasScenarioSelectGroup;
    private RadioGroup mAdasRunModeGroup;
    private LinearLayout mAdasPackPathLayout;
    private TextView mAdasPackPath;
    private LinearLayout mFakeSpeedSetLayout;
    private TextView mFakeSpeedText;
    private SeekBar mAdasFakeSpeedBar;
    private Switch mAdasTrackingVehiclesSwitch;
    private Switch mAdasTrackingLanesSwitch;
    private Switch mAdasFakeSpeedSwitch;

    // DMS
    private RadioGroup mDmsAutoCalibrationGroup;
    private RadioGroup mDmsScenarioSelectGroup;
    private RadioGroup mDmsSpeedLimitGroup;
    private RadioGroup mDmsPlaybackGroup;
    private Switch mDmsRecorderSwitch;
    private LinearLayout mDmsVideoPathLayout;
    private LinearLayout mDmsMetaPathLayout;
    private TextView mDmsVideoPath;
    private TextView mDmsMetaPath;
    private LinearLayout mDmsApiInLayout;
    private Switch mDmsRenderSwitch;

    // WHEEL
    private RadioGroup mWheelSpeedLimitGroup;

    private SharePrefs mSharePrefs;
    private RadioGroup mAdasRecorderTypeGroup;
    private RadioGroup mAdasShuffleModeGroup;

    public EventSelectorView(Context context) {
        super(context);
        mSharePrefs = new SharePrefs(context);
    }

    @Override
    public String TAG() {
        return TAG;
    }

    @Override
    public int layoutId() {
        return R.layout.view_selector_layout;
    }

    @Override
    public void initView(View view) {
        // ADAS
        mAdasScenarioSelectGroup = (RadioGroup) view.findViewById(R.id.group_adas_scenario);
        mAdasRunModeGroup = (RadioGroup) view.findViewById(R.id.group_adas_run_mode);
        mAdasPackPathLayout = (LinearLayout) view.findViewById(R.id.layout_adas_pack_path);
        mAdasPackPath = (TextView) view.findViewById(R.id.text_adas_pack_path);
        mFakeSpeedSetLayout = (LinearLayout) view.findViewById(R.id.fake_speed_set_layout);
        mFakeSpeedText = (TextView) view.findViewById(R.id.fake_speed_text);
        mAdasFakeSpeedBar = (SeekBar) view.findViewById(R.id.fake_speed_bar);
        mAdasTrackingVehiclesSwitch = (Switch) view.findViewById(R.id.switch_adas_tracking_vehicles);
        mAdasTrackingLanesSwitch = (Switch) view.findViewById(R.id.switch_adas_tracking_lanes);
        mAdasRecorderTypeGroup = (RadioGroup) view.findViewById(R.id.group_adas_recorder_type);
        mAdasShuffleModeGroup = (RadioGroup) view.findViewById(R.id.group_shuffle_mode);
        mAdasFakeSpeedSwitch = (Switch) view.findViewById(R.id.sw_fake_speed);

        // DMS
        mDmsAutoCalibrationGroup = (RadioGroup) view.findViewById(R.id.group_dms_auto_calibration);
        mDmsScenarioSelectGroup = (RadioGroup) view.findViewById(R.id.group_dms_scenario);
        mDmsPlaybackGroup = (RadioGroup) view.findViewById(R.id.group_dms_playback);
        mDmsSpeedLimitGroup = (RadioGroup) view.findViewById(R.id.group_dms_speed_limit);
        mDmsApiInLayout = (LinearLayout) view.findViewById(R.id.layout_dms_api_in);
        mDmsRecorderSwitch = (Switch) view.findViewById(R.id.switch_dms_recorder);
        mDmsVideoPathLayout = (LinearLayout) view.findViewById(R.id.layout_dms_video_path);
        mDmsMetaPathLayout = (LinearLayout) view.findViewById(R.id.layout_dms_meta_path);
        mDmsVideoPath = (TextView) view.findViewById(R.id.text_dms_video_path);
        mDmsMetaPath = (TextView) view.findViewById(R.id.text_dms_meta_path);
        mDmsRenderSwitch = (Switch) view.findViewById(R.id.switch_dms_native_show);

        // WHEEL
        mWheelSpeedLimitGroup = (RadioGroup) view.findViewById(R.id.group_wheel_speed_limit);
    }

    @Override
    public void initData() {
        initListeners(false);
        // ADAS
        checkAdasScenarioMode();
        checkAdasSpeedModel();
        checkAdasTrackingMode();
        checkRecorderType();
        checkShuffleMode();
        // DMS
        checkDmsScenarioMode();
        checkDmsRender();
        checkDmsAutoCalibration();
        checkDmsSpeedLimit();
        // WHEEL
        checkWheelSpeedLimit();
        initListeners(true);
    }

    @Override
    protected void registListeners() {
        initListeners(true);
    }

    @Override
    protected void unregistListeners() {
        initListeners(false);
    }

    /**
     * 初始化监听
     *
     * @param add 是否添加
     */
    private void initListeners(boolean add) {
        // ADAS
        mAdasScenarioSelectGroup.setOnCheckedChangeListener(add ? this : null);
        mAdasRunModeGroup.setOnCheckedChangeListener(add ? this : null);
        mAdasFakeSpeedBar.setOnSeekBarChangeListener(add ? this : null);
        mAdasPackPathLayout.setOnClickListener(add ? this : null);
        mAdasTrackingVehiclesSwitch.setOnCheckedChangeListener(add ? this : null);
        mAdasTrackingLanesSwitch.setOnCheckedChangeListener(add ? this : null);
        mAdasRecorderTypeGroup.setOnCheckedChangeListener(add ? this : null);
        mAdasShuffleModeGroup.setOnCheckedChangeListener(add ? this : null);
        mAdasFakeSpeedSwitch.setOnCheckedChangeListener(add ? this : null);

        // DMS
        mDmsAutoCalibrationGroup.setOnCheckedChangeListener(add ? this : null);
        mDmsScenarioSelectGroup.setOnCheckedChangeListener(add ? this : null);
        mDmsPlaybackGroup.setOnCheckedChangeListener(add ? this : null);
        mDmsSpeedLimitGroup.setOnCheckedChangeListener(add ? this : null);
        mDmsApiInLayout.setOnClickListener(add ? this : null);
        mDmsVideoPathLayout.setOnClickListener(add ? this : null);
        mDmsMetaPathLayout.setOnClickListener(add ? this : null);
        mDmsRenderSwitch.setOnCheckedChangeListener(add ? this : null);

        // WHEEL
        mWheelSpeedLimitGroup.setOnCheckedChangeListener(add ? this : null);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()) {
            case R.id.group_adas_scenario: { // ADAS 运行模式
                switch (checkedId) {
                    case R.id.radio_run: {
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_ADAS_MODEL, OBJ_RUN_RENDER);
                        break;
                    }
                    case R.id.radio_run_recorder: {
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_ADAS_MODEL, OBJ_RUN_RECORDER);
                        break;
                    }
                    case R.id.radio_run_test: {
                        if (!checkAdasFile()) {
                            Toast.makeText(mContext, R.string.toast_adas_file_int_path_invalid, Toast.LENGTH_SHORT)
                                    .show();
                            mAdasScenarioSelectGroup.setOnCheckedChangeListener(null);
                            RadioButton radioButton = (RadioButton) mAdasScenarioSelectGroup.getChildAt(0);
                            radioButton.setChecked(true);
                            mAdasScenarioSelectGroup.setOnCheckedChangeListener(this);
                            return;
                        }
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_ADAS_MODEL, OBJ_TEST);
                        break;
                    }
                }
                break;
            }
            case R.id.group_adas_run_mode: { // 运行模式
                switch (checkedId) {
                    case R.id.radio_gps_only: {
                        runGPSMode();
                        break;
                    }
                    case R.id.radio_self_detect: {
                        runSelfDetect();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case R.id.group_dms_speed_limit: { // 速度限制开关
                switch (checkedId) {
                    case R.id.radio_condition_none_close: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_SPEED_LIMIT, "0");
                        break;
                    }
                    case R.id.radio_condition_none_open: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_SPEED_LIMIT, "1");
                        break;
                    }
                    case R.id.radio_condition_open: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_SPEED_LIMIT, "2");
                        break;
                    }
                    default:
                        break;
                }
                break;
            }
            case R.id.group_dms_scenario: { // DMS 运行模式切换
                switch (checkedId) {
                    case R.id.radio_api_in: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_INPUT_MODE, String.valueOf(2));
                        break;
                    }
                    case R.id.radio_file_in: {
                        if (!checkDmsFile()) {
                            Toast.makeText(mContext, R.string.toast_dms_file_int_path_invalid, Toast.LENGTH_SHORT).show();
                            mDmsScenarioSelectGroup.setOnCheckedChangeListener(null);
                            RadioButton radioButton = (RadioButton) mDmsScenarioSelectGroup.getChildAt(0);
                            radioButton.setChecked(true);
                            mDmsScenarioSelectGroup.setOnCheckedChangeListener(this);
                            break;
                        }
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_INPUT_MODE, String.valueOf(0));
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_ENABLE_RECORDER, String.valueOf(0));
                        mDmsRecorderSwitch.setChecked(false);
                        break;
                    }
                }
            }
            case R.id.group_dms_playback: {
                switch (checkedId) {
                    case R.id.radio_normal: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_PLAY_BACK, String.valueOf(0));
                        break;
                    }
                    case R.id.radio_playback: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_PLAY_BACK, String.valueOf(1));
                        break;
                    }
                }
                break;
            }
            case R.id.group_dms_auto_calibration: {
                switch (checkedId) {
                    case R.id.radio_condition_none_close: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_AUTO_CALIBRATION, "0");
                        break;
                    }
                    case R.id.radio_condition_none_open: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_AUTO_CALIBRATION, "1");
                        break;
                    }
                    case R.id.radio_condition_open: {
                        HobotDmsSdk.getInstance().setDmsConfig(
                                com.hobot.dms.sdk.config.ConfigConst.TYPE_AUTO_CALIBRATION, "2");
                        break;
                    }
                    default:
                        break;
                }
                break;
            }
            case R.id.group_wheel_speed_limit: {
                switch (checkedId) {
                    case R.id.radio_condition_none_close: {
                        // nop
                        break;
                    }
                    case R.id.radio_condition_none_open: {
                        // nop
                        break;
                    }
                    case R.id.radio_condition_open: {
                        // nop
                        break;
                    }
                    default:
                        break;
                }
                break;
            }

            case R.id.group_adas_recorder_type:
                switch (checkedId) {
                    case R.id.btn_pack:
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_RECORDER_TYPE, String.valueOf(0));
                        break;
                    case R.id.btn_mp4:
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_RECORDER_TYPE, String.valueOf(1));
                        break;
                    case R.id.btn_net:
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_RECORDER_TYPE, String.valueOf(2));
                        break;
                }
                break;

            case R.id.group_shuffle_mode:
                switch (checkedId) {
                    case R.id.btn_shuffle_0:
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_SHUFFLE, String.valueOf(0));
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_SHUFFLE_OUTPUT_MODE, String.valueOf(0));
//                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_SHUFFLE_EVENT_MODE, String.valueOf
//                                (0));
                        break;
                    case R.id.btn_shuffle_1:
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_SHUFFLE, String.valueOf(1));
                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_SHUFFLE_OUTPUT_MODE, String.valueOf(1));
//                        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_SHUFFLE_EVENT_MODE, String.valueOf
//                                (1));
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_dms_api_in: {
                int dmsScenario = NumberUtil.getInteger(HobotDmsSdk.getInstance().getDMSConfig(
                        com.hobot.dms.sdk.config.ConfigConst.TYPE_INPUT_MODE));
                if (dmsScenario != 2) {
                    Toast.makeText(mContext, R.string.toast_input_mode_illegal, Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean isChecked = mDmsRecorderSwitch.isChecked();
                HobotDmsSdk.getInstance().setDmsConfig(
                        com.hobot.dms.sdk.config.ConfigConst.TYPE_ENABLE_RECORDER, String.valueOf(!mDmsRecorderSwitch
                                .isChecked() ? 1 : 0));
                mDmsRecorderSwitch.setChecked(!isChecked);
                break;
            }
            case R.id.layout_adas_pack_path: {
                Intent intent = new Intent(mContext, SettingActivity.class);
                intent.putExtra(MainSettingFragment.EXTRA_PATH, MainSettingFragment.EXTRA_ADAS_PACK_PATH);
                mContext.startActivity(intent);
                break;
            }
            case R.id.layout_dms_video_path: {
                Intent intent = new Intent(mContext, SettingActivity.class);
                intent.putExtra(MainSettingFragment.EXTRA_PATH, MainSettingFragment.EXTRA_DMS_VIDEO_PATH);
                mContext.startActivity(intent);
                break;
            }
            case R.id.layout_dms_meta_path: {
                Intent intent = new Intent(mContext, SettingActivity.class);
                intent.putExtra(MainSettingFragment.EXTRA_PATH, MainSettingFragment.EXTRA_DMS_META_PATH);
                mContext.startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        DefaultConfig.FAKE_SPEED = progress;
        mSharePrefs.putCommit(Constants.KEY_FAKE_SPEED, progress);
        mFakeSpeedText.setText(String.format(mContext.getString(R.string.fake_speed), progress));
        NebulaObservableManager.getInstance().onFakeSpeedChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_dms_native_show: {
                // 切换DMS绘制模式
                HobotDmsSdk.getInstance().setDmsConfig(com.hobot.dms.sdk.config.ConfigConst.TYPE_ENABLE_DRAW,
                        (isChecked ? 1 : 0) + "");
                break;
            }
            case R.id.switch_adas_tracking_vehicles: {
                // 切换开启车辆跟踪
                DefaultConfig.SUPPORT_ADAS_VEHICLE_TRACKING = isChecked;
                mSharePrefs.putCommit(Constants.KEY_ADAS_VEHICLE_TRACKING, isChecked);
                break;
            }
            case R.id.switch_adas_tracking_lanes: {
                // 切换开启车道线追踪
                DefaultConfig.SUPPORT_ADAS_LANE_TRACKING = isChecked;
                mSharePrefs.putCommit(Constants.KEY_ADAS_LANE_TRACKING, isChecked);
                break;
            }
            case R.id.sw_fake_speed: {
                // 回调开启假速度模式
                DefaultConfig.SUPPORT_FAKE_SPEED = isChecked;
                // 保存喂速度状态
                mSharePrefs.putCommit(Constants.KEY_FAKE_SPEED_SWITCH, isChecked);
                // 开启速度进度条
                mFakeSpeedSetLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
            }
        }
    }

    /**
     * 运行GPS 模式
     * 和喂不喂假速度 无关系
     */
    private void runGPSMode() {
        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_RUNNING_MODE, String.valueOf(MODEL_GPS_ONLY));
    }

    /**
     * 运行自检查 模式
     * 和喂不喂假速度无关
     */
    private void runSelfDetect() {
        HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_RUNNING_MODE, String.valueOf(MODEL_SELF_DETECT));
    }


    /**
     * 检查ADAS测试文件是否存在。
     *
     * @return 是否存在
     */
    private boolean checkAdasFile() {
        String packPath = mAdasPackPath.getText().toString();
        File packFile = new File(packPath);
        return packFile.exists();
    }

    /**
     * 检查DMS测试文件是否存在。
     *
     * @return 是否存在
     */
    private boolean checkDmsFile() {
        String videoPath = mDmsVideoPath.getText().toString();
        String metaPath = mDmsVideoPath.getText().toString();
        File videoFile = new File(videoPath);
        File metaFile = new File(metaPath);
        return videoFile.exists() && metaFile.exists();
    }

    /**
     * 检查ADAS运行模式。
     */
    private void checkAdasScenarioMode() {
        RadioButton scenarioRadio;
        if (!HobotAdasSDK.getInstance().isInit()) {
            Log.w(TAG, "checkAdasScenarioMode: adas not init ");
            return;
        }
        switch (HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_ADAS_MODEL)) {
            case OBJ_RUN_RENDER:
                scenarioRadio = (RadioButton) mAdasScenarioSelectGroup.getChildAt(0);
                scenarioRadio.setChecked(true);
                break;
            case OBJ_RUN_RECORDER:
                scenarioRadio = (RadioButton) mAdasScenarioSelectGroup.getChildAt(1);
                scenarioRadio.setChecked(true);
                break;
            case OBJ_TEST:
                scenarioRadio = (RadioButton) mAdasScenarioSelectGroup.getChildAt(2);
                scenarioRadio.setChecked(true);
                break;
        }
        String packName = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_PACK_NAME);
        mAdasPackPath.setText(packName);
    }

    /**
     * 检查ADAS速度模式。
     */
    private void checkAdasSpeedModel() {
        if (!HobotAdasSDK.getInstance().isInit()) {
            Log.w(TAG, "checkAdasSpeedModel: adas not init ");
            return;
        }
        String value = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_RUNNING_MODE);
        if (TextUtils.isEmpty(value)) {
            Log.w(TAG, "checkAdasSpeedModel: mode [" + value + "], is empty!");
            return;
        }
        int mode = NumberUtil.getInteger(value);

        RadioButton radioButton;
        switch (mode) {

            case MODEL_GPS_ONLY: {
                radioButton = (RadioButton) mAdasRunModeGroup.getChildAt(0);
                radioButton.setChecked(true);
                break;
            }

            case MODEL_SELF_DETECT: {
                radioButton = (RadioButton) mAdasRunModeGroup.getChildAt(1);
                radioButton.setChecked(true);
                break;
            }
            default: {
                Log.w(TAG, "checkAdasSpeedModel: not support mode at [" + mode + "]");
                break;
            }
        }

        // 设置假速度模式
        mAdasFakeSpeedSwitch.setChecked(DefaultConfig.SUPPORT_FAKE_SPEED);
        mAdasFakeSpeedBar.setProgress(DefaultConfig.FAKE_SPEED);
        mFakeSpeedText.setText(String.format(mContext.getString(R.string.fake_speed), DefaultConfig.FAKE_SPEED));
        // 速度控制
        mFakeSpeedSetLayout.setVisibility(DefaultConfig.SUPPORT_FAKE_SPEED ? View.VISIBLE : View.GONE);
    }

    /**
     * 检查ADAS跟踪状态
     */
    private void checkAdasTrackingMode() {
        // 车辆跟踪
        mAdasTrackingVehiclesSwitch.setChecked(DefaultConfig.SUPPORT_ADAS_VEHICLE_TRACKING);
        // 车道线跟踪
        mAdasTrackingLanesSwitch.setChecked(DefaultConfig.SUPPORT_ADAS_LANE_TRACKING);
    }

    /**
     * 检查DMS运行模式。
     */
    private void checkDmsScenarioMode() {
        if (!HobotDmsSdk.getInstance().isInit()) {
            Log.w(TAG, "checkDmsScenarioMode: dms not init ");
            return;
        }
        // 1.设置运行模式
        int dmsScenario = NumberUtil.getInteger(HobotDmsSdk.getInstance().getDMSConfig(
                com.hobot.dms.sdk.config.ConfigConst.TYPE_INPUT_MODE));
        RadioButton scenarioRadio;
        switch (dmsScenario) {
            case 2: {
                scenarioRadio = (RadioButton) mDmsScenarioSelectGroup.getChildAt(0);
                scenarioRadio.setChecked(true);
                break;
            }
            case 0: {
                scenarioRadio = (RadioButton) mDmsScenarioSelectGroup.getChildAt(1);
                scenarioRadio.setChecked(true);
                break;
            }
        }
        // 2.设置录像模式
        boolean isRecorder = NumberUtil.getInteger(HobotDmsSdk.getInstance().getDMSConfig(
                com.hobot.dms.sdk.config.ConfigConst.TYPE_ENABLE_RECORDER)) == 1;
        mDmsRecorderSwitch.setChecked(isRecorder);
        // 3.设置回灌/回看模式
        int playbackMode = NumberUtil.getInteger(HobotDmsSdk.getInstance().getDMSConfig(
                com.hobot.dms.sdk.config.ConfigConst.TYPE_PLAY_BACK));
        RadioButton playbackModeButton;
        switch (playbackMode) {
            case 0: {
                playbackModeButton = (RadioButton) mDmsPlaybackGroup.getChildAt(0);
                playbackModeButton.setChecked(true);
                break;
            }
            case 1: {
                playbackModeButton = (RadioButton) mDmsPlaybackGroup.getChildAt(1);
                playbackModeButton.setChecked(true);
                break;
            }
        }
        // 4.设置回灌/回看路径
        String videoPath = HobotDmsSdk.getInstance().getDMSConfig(
                com.hobot.dms.sdk.config.ConfigConst.TYPE_PATH_INPUT_VIDEO);
        mDmsVideoPath.setText(videoPath);
        String metaPath = HobotDmsSdk.getInstance().getDMSConfig(
                com.hobot.dms.sdk.config.ConfigConst.TYPE_PATH_INPUT_META);
        mDmsMetaPath.setText(metaPath);
    }

    /**
     * 检查DMS速度限制。
     */
    private void checkDmsSpeedLimit() {
        RadioButton radioButton;
        if (!HobotDmsSdk.getInstance().isInit()) {
            Log.w(TAG, "checkDmsSpeedLimit: dms not init ");
            return;
        }
        switch (HobotDmsSdk.getInstance().getDMSConfig(com.hobot.dms.sdk.config.ConfigConst.TYPE_SPEED_LIMIT)) {
            case "0": {
                radioButton = (RadioButton) mDmsSpeedLimitGroup.getChildAt(0);
                radioButton.setChecked(true);
                break;
            }
            case "1": {
                radioButton = (RadioButton) mDmsSpeedLimitGroup.getChildAt(1);
                radioButton.setChecked(true);
                break;
            }
            case "2": {
                radioButton = (RadioButton) mDmsSpeedLimitGroup.getChildAt(2);
                radioButton.setChecked(true);
                break;
            }
        }
    }

    /**
     * 检查DMS自动标定。
     */
    private void checkDmsAutoCalibration() {
        RadioButton radioButton;
        if (!HobotDmsSdk.getInstance().isInit()) {
            Log.w(TAG, "checkDmsAutoCalibration: dms not init ");
            return;
        }
        switch (HobotDmsSdk.getInstance().getDMSConfig(com.hobot.dms.sdk.config.ConfigConst.TYPE_AUTO_CALIBRATION)) {
            case "0": {
                radioButton = (RadioButton) mDmsAutoCalibrationGroup.getChildAt(0);
                radioButton.setChecked(true);
                break;
            }
            case "1": {
                radioButton = (RadioButton) mDmsAutoCalibrationGroup.getChildAt(1);
                radioButton.setChecked(true);
                break;
            }
            case "2": {
                radioButton = (RadioButton) mDmsAutoCalibrationGroup.getChildAt(2);
                radioButton.setChecked(true);
                break;
            }
        }
    }

    /**
     * 检查DMS底层绘制。
     */
    private void checkDmsRender() {
        if (!HobotDmsSdk.getInstance().isInit()) {
            Log.w(TAG, "checkDmsAutoCalibration: dms not init ");
            return;
        }
        mDmsRenderSwitch.setChecked(NumberUtil.getInteger(
                HobotDmsSdk.getInstance().getDMSConfig(
                        com.hobot.dms.sdk.config.ConfigConst.TYPE_ENABLE_DRAW)) == 1);
    }

    /**
     * 检查Wheel速度限制。
     */
    private void checkWheelSpeedLimit() {
        RadioButton radioButton;
        if (!HobotDmsSdk.getInstance().isInit()) {
            Log.w(TAG, "checkWheelSpeedLimit: wheel not init ");
        }
    }

    /**
     * ADAS 录制模式检查
     */
    private void checkRecorderType() {
        String type = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_RECORDER_TYPE);
        RadioButton radioButton;
        switch (NumberUtil.getInteger(type)) {
            case 0:
                radioButton = (RadioButton) mAdasRecorderTypeGroup.getChildAt(0);
                radioButton.setChecked(true);
                break;
            case 1:
                radioButton = (RadioButton) mAdasRecorderTypeGroup.getChildAt(1);
                radioButton.setChecked(true);
                break;
            case 2:
                radioButton = (RadioButton) mAdasRecorderTypeGroup.getChildAt(2);
                radioButton.setChecked(true);
                break;
        }
    }

    /**
     * ADAS Shuffle模式检查
     */
    private void checkShuffleMode() {
        String shuffer = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_SHUFFLE);
        String outputShuffle = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_SHUFFLE_OUTPUT_MODE);
        RadioButton radioButton;
        switch (NumberUtil.getInteger(shuffer) | NumberUtil.getInteger(outputShuffle)) {
            case 0:
                radioButton = (RadioButton) mAdasShuffleModeGroup.getChildAt(0);
                radioButton.setChecked(true);
                break;
            case 1:
                radioButton = (RadioButton) mAdasShuffleModeGroup.getChildAt(1);
                radioButton.setChecked(true);
                break;
        }
    }
}