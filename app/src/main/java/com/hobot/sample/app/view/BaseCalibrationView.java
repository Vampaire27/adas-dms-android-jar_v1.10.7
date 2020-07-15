package com.hobot.sample.app.view;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hobot.sample.app.R;
import com.hobot.sample.app.media.MediaPlayer;

/**
 * 标定控件基类。
 */
public abstract class BaseCalibrationView extends RelativeLayout implements View.OnClickListener,
        View.OnTouchListener, MediaPlayer.OnCompletionPlayListener {

    public static final int STATE_NONE = -0x01;
    public static final int STATE_START = 0x13;
    public static final int STATE_FINISH = 0x14;
    public static final int STATE_CANCEL = 0x15;
    public static final int STATE_FAIL = 0x16;
    protected int mPageFlag = STATE_NONE; // 初始状态
    protected OnStepListener mListener;
    protected MediaPlayer mPlayer;
    protected AnchorWindowHelper mAnchorTips;

    public BaseCalibrationView(Context context) {
        this(context, null);
    }

    public BaseCalibrationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCalibrationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPlayer = new MediaPlayer(getContext());
        mPlayer.registerCompletionPlayListener(this);
        initViews();
        initListeners();
        // 初始化帮助提示
        if (null != findViewById(R.id.ll_help)) {
            mAnchorTips = AnchorWindowHelper.create(getContext()).target(findViewById(R.id.ll_help), -300, 0);
            findViewById(R.id.ll_help).setOnTouchListener(this);
        }
    }

    /**
     * 初始化布局
     */
    protected abstract void initViews();

    /**
     * 初始化接听
     */
    protected abstract void initListeners();

    /**
     * 开启标定
     */
    @CallSuper
    @MainThread
    public void startCalibration() {
        onStep(STATE_START);
    }

    /**
     * 结束标定
     */
    @CallSuper
    @MainThread
    public void stopCalibration() {
        onStep(STATE_CANCEL);
        mPageFlag = STATE_NONE;
        post(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        });
    }

    /**
     * 下一步
     */
    protected abstract void next();

    /**
     * 上一步
     */
    protected abstract void back();

    /**
     * 释放资源
     */
    @CallSuper
    @MainThread
    public void release() {
        stop();
        mListener = null;
        mAnchorTips = null;
        if (findViewById(R.id.ll_help) != null) {
            findViewById(R.id.ll_help).setOnTouchListener(null);
        }
    }

    /**
     * 标定状态回调
     */
    public interface OnStepListener {
        void onStep(int index);
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public final void setOnStepListener(OnStepListener listener) {
        this.mListener = listener;
    }

    /**
     * 通知当前的Listener
     *
     * @param index
     */
    public final void notifyListener(int index) {
        if (mListener != null) {
            mListener.onStep(index);
        }
    }

    /**
     * 指定一步的回调
     *
     * @param index
     */
    @CallSuper
    protected void onStep(final int index) {
        post(new Runnable() {
            @Override
            public void run() {
                switch (index) {
                    case STATE_START: { // 进入标定界面
                        setVisibility(VISIBLE);
                        break;
                    }
                    case STATE_CANCEL: { // 取消退出标定界面
                        Toast.makeText(getContext(), R.string.calibration_cancel, Toast.LENGTH_SHORT).show();
                        setVisibility(GONE);
                        break;
                    }
                    case STATE_FINISH: { // 标定界面退出标定界面
                        Toast.makeText(getContext(), R.string.calibration_success, Toast.LENGTH_SHORT).show();
                        setVisibility(GONE);
                        break;
                    }
                    case STATE_FAIL: { // 失败退出标定界面
                        setVisibility(GONE);
                        break;
                    }
                }
                notifyListener(index);
            }
        });
    }

    /**
     * 当前步骤回调
     */
    protected void onStep() {
        onStep(mPageFlag);
    }

    /**
     * 更新指定页面
     *
     * @param index
     */
    protected abstract void updateUI(int index);

    /**
     * 更新当前页面
     */
    protected final void updateUI() {
        updateUI(mPageFlag);
    }

    /**
     * 播放
     *
     * @param id
     */
    protected final void play(int id) {
        if (null != mPlayer) {
            mPlayer.play(id);
        }
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    protected final boolean isPlaying() {
        if (null != mPlayer) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    /**
     * 暂停播放
     */
    protected final void pause() {
        if (null != mPlayer) {
            mPlayer.pause();
        }
    }

    /**
     * 停止播放
     */
    protected final void stop() {
        if (null != mPlayer) {
            mPlayer.unregisterCompletionPlayListener(this);
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onCompletionPlayListener() {
        // TODO:
    }

    /**
     * 根据页面状态获取页面提示
     *
     * @param index
     * @return
     */
    protected abstract String getTip(int index);

    /**
     * 帮助Tips显示控制
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mAnchorTips.show(getTip(mPageFlag));
                return true;
            case MotionEvent.ACTION_UP:
//                mAnchorTips.dismiss();
                break;
        }
        return false;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            requestLayout();
        }
    }
}
