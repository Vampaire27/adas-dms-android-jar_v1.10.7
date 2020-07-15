package com.hobot.sample.app.module.base;

import android.content.Context;
import android.location.Location;
import android.support.annotation.CallSuper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.nebula.common.listener.IWarningEventListener;
import com.hobot.nebula.common.unit.EventCell;
import com.hobot.sample.app.R;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.listener.ICarStateListener;
import com.hobot.sample.app.listener.IExhibitionStateListener;
import com.hobot.sample.app.listener.IPreviewStateListener;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.module.warn.WriteEventWarning;
import com.hobot.sample.app.view.WarnView;

import java.util.HashMap;
import java.util.Map;

/**
 * SDK View的抽象类，有布局，用于控制SDK的生命周期和布局。
 *
 * @author Hobot
 */
public abstract class BaseView extends CommonView implements View.OnClickListener, ICarStateListener,
        IPreviewStateListener, IWarningEventListener, IExhibitionStateListener {

    private static final boolean DEBUG = true;
    private Map<Integer, EventInfo> mEventMap = new HashMap<>();
    // 根布局
    private View mContainer;
    // 速度控件
    private TextView mCarSpeedView;
    // 距离控件
    private TextView mCarDistanceView;
    private SpeedRunnable mSpeedRunnable = new SpeedRunnable();
    private DistanceRunnable mDistanceRunnable = new DistanceRunnable();

    /**
     * 获取布局id
     *
     * @return 布局id
     */
    public abstract int layoutId();

    /**
     * 控件创建回调
     *
     * @param view 创建的控件
     */
    public abstract void onViewCreated(View view);

    /**
     * 当控件销毁回调
     */
    public abstract void onViewRelease();

    @Override
    public final View onCreateView(Context context) {
        super.onCreateView(context);
        mContext = context;
        mContainer = LayoutInflater.from(context).inflate(layoutId(), null, false);
        mCarSpeedView = (TextView) mContainer.findViewById(R.id.car_speed);
        mCarDistanceView = (TextView) mContainer.findViewById(R.id.car_distance);
        onViewCreated(mContainer);

        NebulaObservableManager.getInstance().registerPreviewStateListener(this);
        NebulaObservableManager.getInstance().registerSpeedViewStateListener(this);
        NebulaObservableManager.getInstance().registerExhibitionStateListener(this);
        NebulaObservableManager.getInstance().registerTestModeListener(this);
        HobotWarningSDK.getInstance().registWarningListener(this);
        return mContainer;
    }

    @Override
    public final void onDestroyView() {
        super.onDestroyView();
        onViewRelease();
        NebulaObservableManager.getInstance().unregisterPreviewStateListener(this);
        NebulaObservableManager.getInstance().unregisterSpeedViewStateListener(this);
        NebulaObservableManager.getInstance().unregisterExhibitionStateListener(this);
        NebulaObservableManager.getInstance().unregisterTestModeListener(this);
        HobotWarningSDK.getInstance().unregistWarningListener(this);
        mContext = null;
        mSpeedRunnable = null;
    }

    @CallSuper
    @Override
    public void onClick(View v) {
        // 点击事件，需要子类实现
    }

    @CallSuper
    @Override
    public void onPreviewState(boolean isShow) {
    }

    @Override
    public void onRenderState(boolean isShow) {

    }

    @CallSuper
    @Override
    public final void onSpeedState(boolean isShow) {
        // 速度控件变化
        if (isShow) {
            if (mCarSpeedView != null) {
                mCarSpeedView.setVisibility(View.VISIBLE);
            }
            if (mCarDistanceView != null) {
                mCarDistanceView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mCarSpeedView != null) {
                mCarSpeedView.setVisibility(View.GONE);
            }
            if (mCarDistanceView != null) {
                mCarDistanceView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onExhibitionState(boolean isShow) {
    }

    @Override
    public void onDVRState(boolean isShow) {
    }

    @Override
    public final void onWarningEvent(final EventCell eventCell) {
        if (DEBUG) {
            WriteEventWarning.saveEvent(
                    eventCell.getEventGroup(),
                    eventCell.getEventType(),
                    eventCell.getFrameId(),
                    eventCell.getTimestamp()
            );
        }
        int event = eventCell.getPosition();
        EventInfo eventInfo = mEventMap.get(event);
        // 设置当前事件的报警状态
        if (eventInfo != null) {
            eventInfo.setWarn(true);
            mEventMap.put(event, eventInfo);
        }
    }

    /**
     * 过滤报警事件。
     *
     * @param event 事件类型，用于区分事件。
     * @param id    事物id，用于区分引发报警的事物
     * @return 是否报警的结果
     */
    protected synchronized boolean flatSameEvent(int event, int id) {
        // 1.从列表获取对应的事件。
        EventInfo eventInfo = mEventMap.get(event);
        if (eventInfo != null) {
            // 2.如果有对应的事件。
            if (eventInfo.getId() == id) {
                // 2-1.判断ID是否一致，如果一致，返回当前是否已经报警。
                return eventInfo.isWarn();
            } else {
                // 2-2.如果不一致，更新ID，设置没有报警状态。
                eventInfo.setId(id);
                eventInfo.setWarn(false);
                return false;
            }
        } else {
            // 3.如果没有对应的事件，新生成一个事件。
            eventInfo = new EventInfo().setId(id).setWarn(false);
            mEventMap.put(event, eventInfo);
        }
        return false;
    }

    /**
     * 设置车辆速度
     *
     * @param carSpeed 车速
     */
    protected void setSpeed(final float carSpeed) {
        if (mCarSpeedView == null || mContext == null || mCarSpeedView.getVisibility() == View.GONE) {
            return;
        }
        mSpeedRunnable.carSpeed = carSpeed;
        mCarSpeedView.post(mSpeedRunnable);
    }

    /**
     * 设置车想距离
     *
     * @param carDistance 距离
     */
    protected void setDistance(final float carDistance) {
        if (mCarDistanceView == null || mContext == null || mCarDistanceView.getVisibility() == View.GONE) {
            return;
        }
        mDistanceRunnable.carDistance = carDistance;
        mCarDistanceView.post(mDistanceRunnable);
    }

    /**
     * 更新速度回调
     */
    private class SpeedRunnable implements Runnable {
        private float carSpeed;

        @Override
        public void run() {
            if (mCarSpeedView != null && mContext != null) {
                mCarSpeedView.setText(String.format(mContext.getString(R.string.car_speed),
                        carSpeed));
            }
        }
    }

    /**
     * 更新距离回调
     */
    private class DistanceRunnable implements Runnable {
        private float carDistance;

        @Override
        public void run() {
            if (mCarDistanceView != null && mContext != null) {
                mCarDistanceView.setText(String.format(mContext.getString(R.string.car_distance),
                        carDistance));
            }
        }
    }

    public class EventInfo {
        private int id;
        private boolean isWarn;

        public int getId() {
            return id;
        }

        public EventInfo setId(int id) {
            this.id = id;
            return this;
        }

        public boolean isWarn() {
            return isWarn;
        }

        public EventInfo setWarn(boolean warn) {
            isWarn = warn;
            return this;
        }

        @Override
        public String toString() {
            return "[id]: " + id + "  [isWarn]: " + isWarn;
        }
    }
}
