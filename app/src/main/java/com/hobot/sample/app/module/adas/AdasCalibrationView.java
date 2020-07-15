package com.hobot.sample.app.module.adas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.adas.sdk.config.ConfigConst;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.sample.app.R;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.manager.NebulaSDKManager;
import com.hobot.sample.app.view.BaseCalibrationView;
import com.hobot.sdk.library.utils.NumberUtil;
import com.hobot.sdk.library.utils.SharePrefsUtil;

/**
 * 设备端ADAS标定界面
 */
public class AdasCalibrationView extends BaseCalibrationView {
    // region 静态变量区域
    private static final String TAG = AdasCalibrationView.class.getSimpleName();
    // endregion 静态变量区域

    // region 静态成员变量
    public static final int INDEX_PAGE_CAMERA = 0x00;
    public static final int INDEX_PAGE_ENDPOINT = 0x01;
    public static final int INDEX_PAGE_PARAMETERS = 0x02;
    private static final int WHAT_CONFIG_CAMERA = 0x11;
    private static final int WHAT_CONFIG_RPY = 0x12;
    private static final int WHAT_CONFIG_FINISH = 0x13;

    // endregion 静态成员变量

    // region 私有成员变量
    private TextView mTitleTv;
    private LinearLayout mHelpLl;
    private FrameLayout mTargetFl;
    private Button mCancelBtn;
    private Button mFinishBtn;

    private CameraViewHolder mCameraHolder;
    private EndpointViewHolder mEndpointHolder;
    private ParametersViewHolder mParametersHolder;
    private AdasCalibrationHelper mCalibHelper;
    private Handler mWorkHandler;

    // 记录之前的ADAS状态
    private boolean mIsAdasSoundOpen;
    // 语音报警

    // endregion 私有成员变量

    // region 构造方法区域
    public AdasCalibrationView(@NonNull Context context) {
        this(context, null);
    }

    public AdasCalibrationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdasCalibrationView(@NonNull Context context, @Nullable AttributeSet attrs, int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // endregion 构造方法区域

    // region  暴露方法区域

    /**
     * 开启标定
     * 在此之前要让view visible
     */
    @Override
    public void startCalibration() {
        super.startCalibration();
        mPageFlag = INDEX_PAGE_CAMERA;
        updateUI();
        pause();
        play(R.raw.calib_adas_position);
    }

    /**
     * 释放
     */
    @Override
    public void release() {
        super.release();
        mCancelBtn.setOnClickListener(null);
        mFinishBtn.setOnClickListener(null);
        mWorkHandler.removeCallbacksAndMessages(null);
        mWorkHandler.getLooper().quitSafely();
        mWorkHandler = null;
    }

    // endregion  暴露方法区域

    // region 内部方法区
    @Override
    protected void initViews() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mWorkHandler = new WorkHandler(handlerThread.getLooper());
        mCalibHelper = new AdasCalibrationHelper();

        LayoutInflater.from(getContext()).inflate(R.layout.view_adas_calibration, this, true);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mHelpLl = (LinearLayout) findViewById(R.id.ll_help);
        mTargetFl = (FrameLayout) findViewById(R.id.fl_target);
        mCancelBtn = (Button) findViewById(R.id.btn_cancel);
        mFinishBtn = (Button) findViewById(R.id.btn_finish);
        mCameraHolder = new CameraViewHolder();
        mEndpointHolder = new EndpointViewHolder();
        mParametersHolder = new ParametersViewHolder();
        mParametersHolder.setListener(mCalibHelper);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListeners() {
        mCancelBtn.setOnClickListener(this);
        mFinishBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel: {
                back();
                break;
            }
            case R.id.btn_finish: {
                next();
                break;
            }
        }
    }

    @Override
    protected void next() {
        pause();
        switch (mPageFlag) {
            case INDEX_PAGE_CAMERA: {
                onStep();
                mPageFlag = INDEX_PAGE_ENDPOINT;
                updateUI();
                play(R.raw.calib_adas_endpoint);
                break;
            }
            case INDEX_PAGE_ENDPOINT: {
                // 最后再设置RPY
//                mWorkHandler.obtainMessage(WHAT_CONFIG_RPY).sendToTarget();
                if (DefaultConfig.SUPPORT_ADAS_PARAMS_CALIBRATION) {
                    onStep();
                    mPageFlag = INDEX_PAGE_PARAMETERS;
                    updateUI();
                } else {
                    onStep();
                    mWorkHandler.obtainMessage(WHAT_CONFIG_FINISH).sendToTarget();
                }
                break;
            }
            case INDEX_PAGE_PARAMETERS: {
                mWorkHandler.obtainMessage(WHAT_CONFIG_CAMERA).sendToTarget();
                onStep();
//                mPageFlag = STATE_NONE;
//                process();
                break;
            }
        }
    }

    @Override
    protected void back() {
        pause();
        if (mPageFlag > INDEX_PAGE_CAMERA) {
            // 如果当前页面大于CAMERA标定页面，退回上一步。
            mPageFlag -= 1;
        } else {
            // 如果当前页面已经在Camera标定页面，退出当前页面。
            stopCalibration();
            return;
        }
        updateUI();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            // 如果之前是打开的后续会恢复该状态
            if (mIsAdasSoundOpen) {
                HobotWarningSDK.getInstance().setSoundSwitchByGroup("ADAS", true);
            }
            mIsAdasSoundOpen = false;
        }
    }

    @Override
    protected void updateUI(int index) {
        mTitleTv.setText(getTitle(mPageFlag));
        switch (index) {
            case INDEX_PAGE_CAMERA: {   // 标定相机位置
                if (!mIsAdasSoundOpen) {
                    mIsAdasSoundOpen = HobotWarningSDK.getInstance().getSoundSwitchByGroup("ADAS");
                    // 如果之前是打开的修改并记录下来
                    if (mIsAdasSoundOpen) {
                        HobotWarningSDK.getInstance().setSoundSwitchByGroup("ADAS", false);
                    }
                }
                mHelpLl.setVisibility(VISIBLE);
                mCancelBtn.setText(R.string.calibration_cancel);
                mTargetFl.removeAllViews();
                mTargetFl.addView(mCameraHolder.content);
                mFinishBtn.setText(R.string.calibration_next);
                mFinishBtn.setEnabled(true);

                // 更新默认界面 RPY
                mCalibHelper.getRPY();
                break;
            }
            case INDEX_PAGE_ENDPOINT: {   // 标定消失点
                mHelpLl.setVisibility(VISIBLE);
                mCancelBtn.setText(R.string.calibration_prev);
                mFinishBtn.setText(R.string.calibration_next);
                mFinishBtn.setEnabled(true);
                mTargetFl.removeAllViews();
                mEndpointHolder.mCalibCiv.showCalLine();
                mTargetFl.addView(mEndpointHolder.content);
                break;
            }
            case INDEX_PAGE_PARAMETERS: {   // 标定参数
                mFinishBtn.setText(R.string.calibration_done);
                mFinishBtn.setEnabled(true);
                mCancelBtn.setText(R.string.calibration_prev);
                mTargetFl.removeAllViews();
                mTargetFl.addView(mParametersHolder.content);
                // 显示参数
                mCalibHelper.getCameraPosition();
                mHelpLl.setVisibility(GONE);
                break;
            }
            default: {
                // 如果之前是打开的后续会恢复该状态
                if (mIsAdasSoundOpen) {
                    HobotWarningSDK.getInstance().setSoundSwitchByGroup("ADAS", true);
                }
                mIsAdasSoundOpen = false;
                mTargetFl.removeAllViews();
                break;
            }
        }
    }

    @Override
    protected String getTip(int index) {
        int stringRes = 0;
        switch (index) {
            case INDEX_PAGE_CAMERA: {
                stringRes = R.string.calibration_adas_tip_camera;
                break;
            }
            case INDEX_PAGE_ENDPOINT: {
                stringRes = R.string.calibration_adas_tip_endpoint;
                break;
            }
            case INDEX_PAGE_PARAMETERS: {
                stringRes = R.string.calibration_adas_tip_parameters;
                break;
            }
        }
        if (0 == stringRes) {
            return "";
        }
        return getResources().getString(stringRes);
    }

    /**
     * 根据页面状态获取title
     *
     * @param index
     * @return
     */
    private String getTitle(int index) {
        int stringRes = 0;
        switch (index) {
            case INDEX_PAGE_CAMERA:
                stringRes = R.string.calibration_adas_title_camera;
                break;
            case INDEX_PAGE_ENDPOINT:
                stringRes = R.string.calibration_adas_title_endpoint;
                break;
            case INDEX_PAGE_PARAMETERS:
                stringRes = R.string.calibration_adas_title_parameters;
                break;
        }
        if (0 == stringRes) {
            return "";
        }
        return getResources().getString(stringRes);
    }

    // endregion 内部方法区

    // region 内部类区

    // 调整相机位置View
    private class CameraViewHolder {
        private View content;
        private AdasCameraPositionView mPositionCpv;

        private CameraViewHolder() {
            content = LayoutInflater.from(getContext()).inflate(R.layout.content_calib_camera, null, false);
            mPositionCpv = (AdasCameraPositionView) content.findViewById(R.id.cpv_position);
        }
    }

    // 调整消失线View
    private class EndpointViewHolder implements OnClickListener {
        private View content;
        private CalibrationImageView mCalibCiv;
        private ImageButton mLeftBtn;
        private ImageButton mUpBtn;
        private ImageButton mRightBtn;
        private ImageButton mDownBtn;

        private EndpointViewHolder() {
            content = LayoutInflater.from(getContext()).inflate(R.layout.content_calib_endpoint, null, false);
            mCalibCiv = (CalibrationImageView) content.findViewById(R.id.civ_calib);
            mLeftBtn = (ImageButton) content.findViewById(R.id.btn_left);
            mUpBtn = (ImageButton) content.findViewById(R.id.btn_up);
            mRightBtn = (ImageButton) content.findViewById(R.id.btn_right);
            mDownBtn = (ImageButton) content.findViewById(R.id.btn_down);

            mLeftBtn.setOnClickListener(this);
            mUpBtn.setOnClickListener(this);
            mRightBtn.setOnClickListener(this);
            mDownBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    mCalibCiv.lineLeft();
                    break;
                case R.id.btn_up:
                    mCalibCiv.lineUp();
                    break;
                case R.id.btn_right:
                    mCalibCiv.lineRight();
                    break;
                case R.id.btn_down:
                    mCalibCiv.lineDown();
                    break;
            }
        }
    }

    // 调整参数View
    private class ParametersViewHolder {
        private View content;
        private EditText mHeightEt;
        private EditText mHorizonEt;
        private EditText mLeftDisEt;
        private EditText mBackAxleEt;
        private EditText mWheelbaseEt;
        private EditText mWidthEt;

        private OnParametersChangeListener mListener;

        public void setListener(OnParametersChangeListener listener) {
            this.mListener = listener;
        }

        private ParametersViewHolder() {
            content = LayoutInflater.from(getContext()).inflate(R.layout.content_calib_parameters, null, false);
            mHeightEt = (EditText) content.findViewById(R.id.et_height);
            mHorizonEt = (EditText) content.findViewById(R.id.et_horizon);
            mLeftDisEt = (EditText) content.findViewById(R.id.et_dis_left);
            mBackAxleEt = (EditText) content.findViewById(R.id.et_back_axle);
            mWheelbaseEt = (EditText) content.findViewById(R.id.et_wheelbase);
            mWidthEt = (EditText) content.findViewById(R.id.et_width);

            mHeightEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (null != mListener) {
                        mListener.onChange(R.id.et_height, mHeightEt, s);
                        checkParams();
                    }
                }
            });
            mHorizonEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (null != mListener) {
                        mListener.onChange(R.id.et_horizon, mHorizonEt, s);
                        checkParams();
                    }
                }
            });
            mLeftDisEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (null != mListener) {
                        mListener.onChange(R.id.et_dis_left, mLeftDisEt, s);
                        checkParams();
                    }
                }
            });
            mBackAxleEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (null != mListener) {
                        mListener.onChange(R.id.et_back_axle, mBackAxleEt, s);
                        checkParams();
                    }
                }
            });
            mWheelbaseEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (null != mListener) {
                        mListener.onChange(R.id.et_wheelbase, mWheelbaseEt, s);
                        checkParams();
                    }
                }
            });
            mWidthEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (null != mListener) {
                        mListener.onChange(R.id.et_width, mWidthEt, s);
                        checkParams();
                    }
                }
            });
        }

        /**
         * 检查参数是否合法
         */
        private void checkParams() {
            if (mFinishBtn != null) {
                if (mHeightEt.getError() == null && mHorizonEt.getError() == null && mLeftDisEt.getError() == null &&
                        mBackAxleEt.getError() == null && mWheelbaseEt.getError() == null && mWidthEt.getError() ==
                        null) {
                    mFinishBtn.setEnabled(true);
                } else {
                    mFinishBtn.setEnabled(false);
                }
            }
        }
    }

    /**
     * ADAS标定参数代理设置类
     * 相当于MVP中的P层
     * 但还没有完全解耦
     */
    private class AdasCalibrationHelper implements OnParametersChangeListener {
        private SharePrefsUtil mSp;
        private static final String KEY_FIRST_CALIB = "key_first_calib";

        private AdasCalibrationHelper() {
            mSp = SharePrefsUtil.getInstance(getContext());
        }

        // 是否标定过
        @Deprecated
        public boolean isCalibrated() {
            return (mSp != null) && mSp.getBoolean(KEY_FIRST_CALIB, false);
        }

        // 完成标定了
        @Deprecated
        public boolean finishCalibrated() {
            return (mSp != null) && mSp.putCommit(KEY_FIRST_CALIB, true);
        }

        // 获取相机位置
        private void getCameraPosition() {
            // 摄像头安装高度
            String heightStr = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_CAMERA_HEIGHT);
            // 摄像头水平距离
            String horizonStr = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_CAMERA_HORIZON);
            // 车辆宽度
            String vehicleWidthStr = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_VEHICLE_WIDTH);
            // 车辆后轴到车辆距离
            String backAxlePosStr = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_VEHICLE_BACK_AXLE_POS);
            // 车辆轴距
            String wheelbaseStr = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_VEHICLE_WHEEL_BASE);
            // 距车左侧距离
            String leftGapStr = HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_CAMERA_X);

            float height = NumberUtil.getFloat(heightStr, -1F);
            float horizon = NumberUtil.getFloat(horizonStr, -1F);
            float backAxle = NumberUtil.getFloat(backAxlePosStr, -1F);
            float width = NumberUtil.getFloat(vehicleWidthStr, -1F);
            float wheelbase = NumberUtil.getFloat(wheelbaseStr, -1F);
            float leftGap = NumberUtil.getFloat(leftGapStr, -1F);

            if (!TextUtils.isEmpty(heightStr)) {
                mParametersHolder.mHeightEt.setText(String.valueOf(height * 100));
            }
            if (!TextUtils.isEmpty(horizonStr)) {
                mParametersHolder.mHorizonEt.setText(String.valueOf(horizon * 100));
            }

            if (!TextUtils.isEmpty(vehicleWidthStr)) {
                mParametersHolder.mWidthEt.setText(String.valueOf(width * 100));
            }

            if (!TextUtils.isEmpty(backAxlePosStr)) {
                mParametersHolder.mBackAxleEt.setText(String.valueOf(backAxle * 100));
            }

            if (!TextUtils.isEmpty(wheelbaseStr)) {
                mParametersHolder.mWheelbaseEt.setText(String.valueOf(wheelbase * 100));
            }

            if (!TextUtils.isEmpty(leftGapStr)
                    && !TextUtils.isEmpty(vehicleWidthStr)) {
                mParametersHolder.mLeftDisEt.setText(String.valueOf(
                        (width / 2 + leftGap) * 100
                ));
            }
        }

        /**
         * 设置相机位置
         *
         * @return
         */
        private boolean setCameraPosition() {
            String heightStr = mParametersHolder.mHeightEt.getText().toString();        // 高度
            String horizonStr = mParametersHolder.mHorizonEt.getText().toString();      // 水平
            String vehicleWidthStr = mParametersHolder.mWidthEt.getText().toString();   // 宽度
            String backAxlePosStr = mParametersHolder.mBackAxleEt.getText().toString(); // 后轴
            String wheelbaseStr = mParametersHolder.mWheelbaseEt.getText().toString();  // 轮距
            String leftGapStr = mParametersHolder.mLeftDisEt.getText().toString();      // 距车左距离

            if (TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(horizonStr)
                    || TextUtils.isEmpty(vehicleWidthStr) || TextUtils.isEmpty(backAxlePosStr)
                    || TextUtils.isEmpty(wheelbaseStr) || TextUtils.isEmpty(leftGapStr)) {
                // 有值为空,禁止配置
                Toast.makeText(getContext(), R.string.toast_input_all_params, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (null != mParametersHolder.mHeightEt.getError() || null != mParametersHolder.mHorizonEt.getError()
                    || null != mParametersHolder.mWidthEt.getError() || null != mParametersHolder.mBackAxleEt.getError()
                    || null != mParametersHolder.mWheelbaseEt.getError() || null != mParametersHolder.mLeftDisEt.getError()) {
                // 有值没过校验，禁止设置
                Toast.makeText(getContext(), R.string.toast_input_correct_params, Toast.LENGTH_SHORT).show();
                return false;
            }

            float height = Math.abs(NumberUtil.getFloat(heightStr) / 100f);
            float horizon = Math.abs(NumberUtil.getFloat(horizonStr) / 100f);
            float width = Math.abs(NumberUtil.getFloat(vehicleWidthStr) / 100f);
            float axle = Math.abs(NumberUtil.getFloat(backAxlePosStr) / 100f);
            float wheelbase = Math.abs(NumberUtil.getFloat(wheelbaseStr) / 100f);
            float left = Math.abs(NumberUtil.getFloat(leftGapStr) / 100f);
            float gap = left - width / 2;

            boolean[] rst = new boolean[6];
            rst[0] = HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_CAMERA_HEIGHT,
                    String.valueOf(height));
            rst[1] = HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_CAMERA_HORIZON,
                    String.valueOf(horizon));
            rst[2] = HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_VEHICLE_WIDTH,
                    String.valueOf(width));
            rst[3] = HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_VEHICLE_BACK_AXLE_POS,
                    String.valueOf(axle));
            rst[4] = HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_VEHICLE_WHEEL_BASE,
                    String.valueOf(wheelbase));
            rst[5] = HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_CAMERA_X,
                    String.valueOf(gap));
            for (boolean ret : rst) {
                if (!ret) {
                    return false;
                }
            }
            // native do calibration
            HobotAdasSDK.getInstance().finishCalibration();
            return true;
        }

        /**
         * 设置RPY坐标系
         */
        private void setRPY() {
            HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_CAMERA_ROLL, String.valueOf(0));
            HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_CAMERA_PITCH, String.valueOf(mEndpointHolder
                    .mCalibCiv.getPointY()));
            HobotAdasSDK.getInstance().setAdasConfig(ConfigConst.TYPE_CAL_CAMERA_YAW, String.valueOf(mEndpointHolder
                    .mCalibCiv.getPointX()));
        }

        /**
         * 获取RPY参数
         * 同步到UI
         */
        private void getRPY() {
//            HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_CAMERA_ROLL);
            final float pitch = NumberUtil.getFloat(HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_CAMERA_PITCH));
            final float yaw = NumberUtil.getFloat(HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_CAL_CAMERA_YAW));
            mEndpointHolder.mCalibCiv.post(new Runnable() {
                @Override
                public void run() {
                    mEndpointHolder.mCalibCiv.setPointX(yaw);
                    mEndpointHolder.mCalibCiv.setPointY(pitch);
                }
            });

        }

        @Override
        public void onChange(int id, View view, CharSequence value) {
            // TODO: 2019/9/29 输入完校验一下全部
            // 1. 获取参数String
            String heightStr = mParametersHolder.mHeightEt.getText().toString();
            String horizonStr = mParametersHolder.mHorizonEt.getText().toString();
            String axleStr = mParametersHolder.mBackAxleEt.getText().toString();
            String wheelBaseStr = mParametersHolder.mWheelbaseEt.getText().toString();
            String widthStr = mParametersHolder.mWidthEt.getText().toString();
            String leftDisStr = mParametersHolder.mLeftDisEt.getText().toString();

            // 2. 转化为浮点数
            float height = NumberUtil.getFloat(heightStr) / 100f;
            float horizon = NumberUtil.getFloat(horizonStr) / 100f;
            float axle = NumberUtil.getFloat(axleStr) / 100f;
            float wheelbase = NumberUtil.getFloat(wheelBaseStr) / 100f;
            float width = NumberUtil.getFloat(widthStr) / 100f;
            float leftDis = NumberUtil.getFloat(leftDisStr) / 100f;

            // 3. 校验
            // 3.1 校验高度
            {
                if (height <= 0.8f || height > 4f) {
                    // 只有编辑当前的 控件才弹Toast
                    if (mParametersHolder.mHeightEt.getId() == id) {
                        Toast.makeText(getContext(), R.string.calibration_error_device_height, Toast.LENGTH_SHORT).show();
                    }
                    String error = getContext().getString(R.string.calibration_error_device_height);
                    mParametersHolder.mHeightEt.setError(error);
                } else {
                    mParametersHolder.mHeightEt.setError(null);
                }
            }

            // 3.2 校验水平距离
            {
                if (horizon <= 0f || horizon > 5f) {
                    // 只有编辑当前的 控件才弹Toast
                    if (mParametersHolder.mHorizonEt.getId() == id) {
                        Toast.makeText(getContext(), R.string.calibration_error_camera_width, Toast.LENGTH_SHORT).show();
                    }
                    String error = getContext().getString(R.string.calibration_error_camera_width);
                    mParametersHolder.mHorizonEt.setError(error);
                } else {
                    mParametersHolder.mHorizonEt.setError(null);
                }
            }

            // 3.3 校验后轴距离
            {
                if (axle <= 0f || axle > 10f) {
                    // 只有编辑当前的 控件才弹Toast
                    if (mParametersHolder.mBackAxleEt.getId() == id) {
                        Toast.makeText(getContext(), R.string.calibration_error_back_shaft, Toast.LENGTH_SHORT).show();
                    }
                    String error = getContext().getString(R.string.calibration_error_back_shaft);
                    mParametersHolder.mBackAxleEt.setError(error);
                } else {
                    mParametersHolder.mBackAxleEt.setError(null);
                }
            }

            // 3.4 校验轴距
            {
                if (wheelbase <= 0f || wheelbase > 10f) {
                    // 只有编辑当前的 控件才弹Toast
                    if (mParametersHolder.mWheelbaseEt.getId() == id) {
                        Toast.makeText(getContext(), R.string.calibration_error_wheelbase, Toast.LENGTH_SHORT).show();
                    }
                    String error = getContext().getString(R.string.calibration_error_wheelbase);
                    mParametersHolder.mWheelbaseEt.setError(error);
                } else if (wheelbase >= axle) { // 若车辆轴距大于后轴位置
                    // 只有编辑当前的 控件才弹Toast
                    if (mParametersHolder.mWheelbaseEt.getId() == id) {
                        Toast.makeText(getContext(), R.string.calibration_error_back_shaft_above_wheelbase, Toast.LENGTH_SHORT).show();
                    }
                    String error = getContext().getString(R.string.calibration_error_back_shaft_above_wheelbase);
                    mParametersHolder.mWheelbaseEt.setError(error);
                } else {
                    mParametersHolder.mWheelbaseEt.setError(null);
                }

            }
            // 3.5 校验车宽
            {
                if (width <= 0f || width > 5f) {
                    // 只有编辑当前的 控件才弹Toast
                    if (mParametersHolder.mWidthEt.getId() == id) {
                        Toast.makeText(getContext(), R.string.calibration_error_car_width, Toast.LENGTH_SHORT).show();
                    }
                    String error = getContext().getString(R.string.calibration_error_car_width);
                    mParametersHolder.mWidthEt.setError(error);
                } else {
                    mParametersHolder.mWidthEt.setError(null);
                }
            }

            // 3.6 校验 距车左 距离
            {
                if (leftDis >= width) {
                    // 只有编辑当前的 控件才弹Toast
                    if (mParametersHolder.mLeftDisEt.getId() == id) {
                        Toast.makeText(getContext(), R.string.calibration_error_left_dis_above_width, Toast.LENGTH_SHORT).show();
                    }
                    String error = getContext().getString(R.string.calibration_error_left_dis_above_width);
                    mParametersHolder.mLeftDisEt.setError(error);
                } else {
                    mParametersHolder.mLeftDisEt.setError(null);
                }
            }
        }
    }

    // 第三步参数校验
    public interface OnParametersChangeListener {
        void onChange(@IdRes int id, View view, CharSequence value);
    }

    // 工作Handler
    public class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CONFIG_CAMERA: {
                    Toast.makeText(getContext(), R.string.calibration_adas_ing, Toast.LENGTH_SHORT).show();
                    // 1. 设置RPY
                    mCalibHelper.setRPY();
                    // 2. 设置Camera
                    boolean ret = mCalibHelper.setCameraPosition();
                    if (ret) {
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS, "");
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS, "");
                        pause();
                        play(R.raw.calib_success);
                        onStep(STATE_FINISH);
                    } else {
//                        onStep(STATE_FAIL);
                    }
                    break;
                }
                case WHAT_CONFIG_RPY: {
                    // 最后再设置RPY
//                    mCalibHelper.setRPY();
                    break;
                }
                case WHAT_CONFIG_FINISH: {
                    NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS, "");
                    NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS, "");
                    pause();
                    play(R.raw.calib_success);
                    onStep(STATE_FINISH);
                    break;
                }
            }
        }
    }

// endregion 内部类区
}