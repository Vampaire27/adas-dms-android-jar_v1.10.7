package com.hobot.sample.app.module.dms;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.dms.sdk.listener.IDmsCalibrateListener;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.sample.app.R;

import DMSOutputProtocol.DMSSDKOutputOuterClass;
import com.hobot.sample.app.view.BaseCalibrationView;

/**
 * DMS 标定控件
 */
public class DmsCalibrationView extends BaseCalibrationView implements IDmsCalibrateListener {
    // region 静态变量区域
    private static final String TAG = DmsCalibrationView.class.getSimpleName();
    private static final int INDEX_PAGE_CAMERA = 0x01;
    private static final int INDEX_START_CALIBRATION = 0x02;
    private static final int CALIB_START = 0;
    private static final int CALIB_SUCCESS = 1;
    private static final int CALIB_FAILED = 2;
    private static final int DELAY_STATUS = 4;
    private static final int CALIB_FAILED_NOFACE = 5;
    private static final int CALIB_FAILED_ABNORMALFACE = 6;
    private static final int DELAY_TIME = 5 * 1000; // 5sec
    // endregion 静态变量区域

    // region 控件布局区域
    private TextView mTitleTv;
    private Button mCancelBtn;
    private Button mConfirmBtn;
    private Button mBeforeBtn;
    // endregion 控件布局区域

    // region 成员变量区域
    // 是否已经启动标定
    private boolean mIsCalibStart;
    // 记录之前的DMS状态
    private boolean mIsDmsSoundOpen;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CALIB_START:
                    calibStart();
                    break;
                case CALIB_SUCCESS: {
                    calibFinish();
                    break;
                }
                case CALIB_FAILED: {
                    //重新正视前方，重新标定
                    calibFailed();
                    break;
                }
                case DELAY_STATUS: {
                    if (!mIsCalibStart && mBeforeBtn.getVisibility() == VISIBLE) {
                        calibFailed();
                    }
                    break;
                }
                case CALIB_FAILED_NOFACE: {
                    //重新正视前方，重新标定
                    calibNoFaceFailed();
                    break;
                }
                case CALIB_FAILED_ABNORMALFACE: {
                    //重新正视前方，重新标定
                    calibAbnormalFaceFailed();
                    break;
                }
            }
            return false;
        }
    });
    // endregion 成员变量区域

    // region 构造方法区域
    public DmsCalibrationView(Context context) {
        this(context, null);
    }

    public DmsCalibrationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DmsCalibrationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // endregion 构造方法区域

    // region 暴露方法区域
    @Override
    public void startCalibration() {
        super.startCalibration();
        mPageFlag = INDEX_PAGE_CAMERA;
        updateUI();
        pause();
        play(R.raw.calib_adjust_camera);
    }

    @Override
    public void release() {
        super.release();
        mConfirmBtn.setOnClickListener(null);
        mCancelBtn.setOnClickListener(null);
        mBeforeBtn.setOnClickListener(null);
        HobotDmsSdk.getInstance().unregisterCalibrateListener(this);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
    }

    // endregion 暴露方法区域

    // region 私有方法区域
    @Override
    protected void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_dms_calibration, this, true);
        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        mConfirmBtn = (Button) findViewById(R.id.confirm_btn);
        mBeforeBtn = (Button) findViewById(R.id.before_btn);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        updateUI();
    }

    @Override
    protected void initListeners() {
        mConfirmBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
        mBeforeBtn.setOnClickListener(this);
        HobotDmsSdk.getInstance().registerCalibrateListener(this);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            // 如果之前是打开的后续会恢复该状态
            if (mIsDmsSoundOpen) {
                HobotWarningSDK.getInstance().setSoundSwitchByGroup("DMS", true);
            }
            mIsDmsSoundOpen = false;
        }
    }

    @Override
    protected void updateUI(int index) {
        switch (index) {
            case STATE_NONE: {
                // 如果之前是打开的后续会恢复该状态
                if (mIsDmsSoundOpen) {
                    HobotWarningSDK.getInstance().setSoundSwitchByGroup("DMS", true);
                }
                mIsDmsSoundOpen = false;
                break;
            }
            case INDEX_PAGE_CAMERA: {
                if (!mIsDmsSoundOpen) {
                    mIsDmsSoundOpen = HobotWarningSDK.getInstance().getSoundSwitchByGroup("DMS");
                    // 如果之前是打开的修改并记录下来
                    if (mIsDmsSoundOpen) {
                        HobotWarningSDK.getInstance().setSoundSwitchByGroup("DMS", false);
                    }
                }
                mTitleTv.setText(R.string.calibration_dms_title_1);
                mCancelBtn.setText(R.string.calibration_cancel);
                mCancelBtn.setVisibility(View.VISIBLE);
                mConfirmBtn.setVisibility(VISIBLE);
                mConfirmBtn.setText(R.string.calibration_next);
                mBeforeBtn.setVisibility(View.GONE);
                break;
            }
            case INDEX_START_CALIBRATION: {
                mTitleTv.setText(R.string.calibration_dms_title_2);
                mCancelBtn.setVisibility(View.INVISIBLE);
                mConfirmBtn.setVisibility(View.GONE);
                mBeforeBtn.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public String getTip(int index) {
        int res = 0;
        if (index == INDEX_PAGE_CAMERA) {
            res = R.string.calibration_dms_adjust_camera;
        } else if (index == INDEX_START_CALIBRATION) {
            res = R.string.calibration_dms_watch_front;
        }
        if (0 == res) {
            return "";
        }
        return getResources().getString(res);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn: {
                next();
                break;
            }
            case R.id.cancel_btn:
            case R.id.before_btn: {
                back();
                break;
            }
        }
    }

    @Override
    protected void next() {
        pause();
        switch (mPageFlag) {
            case INDEX_PAGE_CAMERA: {
                mPageFlag = INDEX_START_CALIBRATION;
                updateUI();
                //播放语音
                play(R.raw.calib_watch_front);
                break;
            }
        }
    }

    @Override
    protected void back() {
        pause();
        switch (mPageFlag) {
            case INDEX_PAGE_CAMERA: {
                stopCalibration();
                break;
            }
            case INDEX_START_CALIBRATION: {
                mPageFlag = INDEX_PAGE_CAMERA;
                updateUI();
                break;
            }
        }
    }

    /**
     * 标定开始
     */
    private void calibStart() {
        DmsCalibrationView.this.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.calibration_start, Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * 标定结束
     */
    private void calibFinish() {
        if (!isPlaying()) {
            onStep(STATE_FINISH);
            mPageFlag = STATE_NONE;
            updateUI();
            pause();
            play(R.raw.calib_success);
        }
    }

    /**
     * 标定失败
     */
    private void calibFailed() {
        if (mPageFlag == INDEX_START_CALIBRATION && !isPlaying()) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), R.string.calibration_dms_again, Toast.LENGTH_SHORT).show();
                }
            });
            play(R.raw.calib_failed);
        }
    }

    /**
     * 无脸标定失败
     */
    private void calibNoFaceFailed() {
        if (mPageFlag == INDEX_START_CALIBRATION && !isPlaying()) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), R.string.calibration_dms_failed_noface, Toast.LENGTH_SHORT).show();
                }
            });
            play(R.raw.calib_noface_failed);
        }
    }

    /**
     * 脸部角度不正常标定失败
     */
    private void calibAbnormalFaceFailed() {
        if (mPageFlag == INDEX_START_CALIBRATION && !isPlaying()) {
            this.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), R.string.calibration_dms_failed_abnormalface, Toast.LENGTH_SHORT).show();
                }
            });
            play(R.raw.calib_abnormal_face_failed);
        }
    }

    @Override
    public void onCompletionPlayListener() {
        if (mPageFlag == INDEX_START_CALIBRATION && mBeforeBtn.getVisibility() == VISIBLE) {
            HobotDmsSdk.getInstance().finishCalibration();
            mIsCalibStart = false;
            mHandler.sendEmptyMessageDelayed(DELAY_STATUS, DELAY_TIME);
        }
    }

    @Override
    public void onDmsCalibResult(DMSSDKOutputOuterClass.FaceCalibEnum faceCalibEnum,
                                 DMSSDKOutputOuterClass.Vector_3f vector_3f) {
        switch (faceCalibEnum) {
            case FACE_CALIB_START: {
                mIsCalibStart = true;
                mHandler.sendEmptyMessage(CALIB_START);
                break;
            }
            case FACE_CALIB_FINISHED: {
                mHandler.sendEmptyMessage(CALIB_SUCCESS);
                break;
            }
            case FACE_CALIB_FAILED: {
                mHandler.sendEmptyMessage(CALIB_FAILED);
                break;
            }
            case FACE_CALIB_NOFACE: {
                mHandler.sendEmptyMessage(CALIB_FAILED_NOFACE);
                break;
            }
            case FACE_CALIB_ABNORMALFACE: {
                mHandler.sendEmptyMessage(CALIB_FAILED_ABNORMALFACE);
                break;
            }
        }
    }
    // endregion 私有方法区域
}
