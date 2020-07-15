package com.hobot.sample.app.manager;

import android.database.Observable;

import com.hobot.sample.app.listener.IAdasStateListener;
import com.hobot.sample.app.listener.ICarStateListener;
import com.hobot.sample.app.listener.IDmsStateListener;
import com.hobot.sample.app.listener.IExhibitionStateListener;
import com.hobot.sample.app.listener.IOverStandardModeListener;
import com.hobot.sample.app.listener.IPerformanceLogSwitchListener;
import com.hobot.sample.app.listener.IPreviewStateListener;
import com.hobot.sample.app.listener.IQualityListener;
import com.hobot.sample.app.listener.ITestModeListener;
import com.hobot.sample.app.listener.ITurnLightListener;
import com.hobot.transfer.common.HobotSocketSDK;
import com.hobot.transfer.common.protocol.Protocol;

/**
 * Observable管理类，用于控制监听。
 *
 * @author Hobot
 */
public class NebulaObservableManager implements ICarStateListener, IPreviewStateListener,
        IExhibitionStateListener,
        ITurnLightListener, IAdasStateListener, IDmsStateListener, IQualityListener,
        IPerformanceLogSwitchListener,
        ITestModeListener, IOverStandardModeListener {
    public static final int TURN_LIGHT_CLOSE = 0;
    public static final int TURN_LIGHT_LEFT = 1;
    public static final int TURN_LIGHT_RIGHT = 2;
    private static NebulaObservableManager sInstance;
    private CarStateObservable mCarStateObservable = new CarStateObservable();
    private PreviewStateObservable mPreviewStateObservable = new PreviewStateObservable();
    private ExhibitionStateObservable mExhibitionStateObservable = new ExhibitionStateObservable();
    private TurnLightObservable mTurnLightObservable = new TurnLightObservable();
    private AdasStateObservable mAdasStateObservable = new AdasStateObservable();
    private DmsStateObservable mDmsStateObservable = new DmsStateObservable();
    private QualityObservable mQualityObservable = new QualityObservable();
    private PerformanceLogObservable mPerformanceLogObservable = new PerformanceLogObservable();
    private TestModeObservable mTestModeObservable = new TestModeObservable();
    private OverStandardModeObservable mOverStandardModeObservable =
            new OverStandardModeObservable();

    private NebulaObservableManager() {
    }

    public static NebulaObservableManager getInstance() {
        if (sInstance == null) {
            synchronized (NebulaObservableManager.class) {
                if (sInstance == null) {
                    sInstance = new NebulaObservableManager();
                }
            }
        }
        return sInstance;
    }

    public void registerSpeedViewStateListener(ICarStateListener listener) {
        mCarStateObservable.registerObserver(listener);
    }

    public void unregisterSpeedViewStateListener(ICarStateListener listener) {
        mCarStateObservable.unregisterObserver(listener);
    }

    public void registerPreviewStateListener(IPreviewStateListener listener) {
        mPreviewStateObservable.registerObserver(listener);
    }

    public void unregisterPreviewStateListener(IPreviewStateListener listener) {
        mPreviewStateObservable.unregisterObserver(listener);
    }

    public void registerExhibitionStateListener(IExhibitionStateListener listener) {
        mExhibitionStateObservable.registerObserver(listener);
    }

    public void unregisterExhibitionStateListener(IExhibitionStateListener listener) {
        mExhibitionStateObservable.unregisterObserver(listener);
    }

    public void registerTurnLightListener(ITurnLightListener listener) {
        mTurnLightObservable.registerObserver(listener);
    }

    public void unregisterTurnLightListener(ITurnLightListener listener) {
        mTurnLightObservable.unregisterObserver(listener);
    }


    public void registerAdasStateListener(IAdasStateListener listener) {
        mAdasStateObservable.registerObserver(listener);
    }

    public void unregisterAdasStateListener(IAdasStateListener listener) {
        mAdasStateObservable.unregisterObserver(listener);
    }

    public void registerDmsStateListener(IDmsStateListener listener) {
        mDmsStateObservable.registerObserver(listener);
    }

    public void unregisterDmsStateListener(IDmsStateListener listener) {
        mDmsStateObservable.unregisterObserver(listener);
    }

    public void registerQualityListener(IQualityListener listener) {
        mQualityObservable.registerObserver(listener);
    }

    public void unregisterQualityListener(IQualityListener listener) {
        mQualityObservable.unregisterObserver(listener);
    }

    public void registerPerformanceLogListener(IPerformanceLogSwitchListener listener) {
        mPerformanceLogObservable.registerObserver(listener);
    }

    public void unregisterPerformanceLogListener(IPerformanceLogSwitchListener listener) {
        mPerformanceLogObservable.unregisterObserver(listener);
    }

    public void registerTestModeListener(ITestModeListener listener) {
        mTestModeObservable.registerObserver(listener);
    }

    public void unregisterTestModeListener(ITestModeListener listener) {
        mTestModeObservable.unregisterObserver(listener);
    }

    public void registerOverStandardModeListener(IOverStandardModeListener listener) {
        mOverStandardModeObservable.registerObserver(listener);
    }

    public void unregisterOverStandardModeListener(IOverStandardModeListener listener) {
        mOverStandardModeObservable.unregisterObserver(listener);
    }

    @Override
    public void onPreviewState(boolean isShow) {
        mPreviewStateObservable.onPreviewState(isShow);
    }

    @Override
    public void onRenderState(boolean isShow) {
        mPreviewStateObservable.onRenderState(isShow);
    }

    @Override
    public void onDVRState(boolean isShow) {
        mPreviewStateObservable.onDVRState(isShow);
    }

    @Override
    public void onSpeedState(boolean isShow) {
        mCarStateObservable.onSpeedState(isShow);
    }

    @Override
    public void onExhibitionState(boolean isShow) {
        mExhibitionStateObservable.onExhibitionState(isShow);
    }

    @Override
    public void onAdasError(int code) {
        mAdasStateObservable.onAdasError(code);
    }

    @Override
    public void onDmsError(int code) {
        mDmsStateObservable.onDmsError(code);
    }

    @Override
    public void onTurnLightChange(int direction) {
        mTurnLightObservable.onTurnLightChange(direction);
    }

    /**
     * 转向灯变化
     *
     * @param direction 方向
     */
    public void openTurnLight(int direction) {
        int currentTurnLightState = coverTurnLightState(direction);
        HobotSocketSDK.Server.sendMsg(Protocol.sendTurnLightOnInfo(direction));
        onTurnLightChange(currentTurnLightState);
    }

    /**
     * 关闭转向灯
     */
    public void closeTurnLight() {
        HobotSocketSDK.Server.sendMsg(Protocol.sendTurnLightOffInfo());
        onTurnLightChange(TURN_LIGHT_CLOSE);
    }

    /**
     * 转向转向灯的信号。
     *
     * @param direction 方向
     * @return
     */
    private int coverTurnLightState(int direction) {
        if (direction == TURN_LIGHT_LEFT) {
            return TURN_LIGHT_LEFT;
        } else if (direction == TURN_LIGHT_RIGHT) {
            return TURN_LIGHT_RIGHT;
        } else {
            return TURN_LIGHT_CLOSE;
        }
    }

    @Override
    public void onPerformanceLogSwitchChanged(boolean isEnable) {
        mPerformanceLogObservable.onPerformanceLogSwitchChanged(isEnable);
    }

    @Override
    public void onTestModeChanged(boolean isEnable) {
        mTestModeObservable.onTestModeChanged(isEnable);
    }

    /**
     * 假速度改变
     *
     * @param speed 速度 km/h
     */
    @Override
    public void onFakeSpeedChanged(float speed) {
        mTestModeObservable.onFakeSpeedChanged(speed);
    }

    @Override
    public void onOverStandardModeChanged(boolean isEnable) {
        mOverStandardModeObservable.onOverStandardModeChanged(isEnable);
    }

    @Override
    public void onCheckShelter(String... params) {
        mQualityObservable.onCheckShelter(params);
    }


    /**
     * 速度显示界面变化监听
     */
    private class CarStateObservable extends Observable<ICarStateListener> implements ICarStateListener {
        @Override
        public void registerObserver(ICarStateListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(ICarStateListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onSpeedState(boolean isShow) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onSpeedState(isShow);
                }
            }
        }
    }

    /**
     * 预览界面显示控制
     */
    private class PreviewStateObservable extends Observable<IPreviewStateListener> implements IPreviewStateListener {
        @Override
        public void registerObserver(IPreviewStateListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(IPreviewStateListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onPreviewState(boolean isShow) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onPreviewState(isShow);
                }
            }
        }

        @Override
        public void onRenderState(boolean isShow) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onRenderState(isShow);
                }
            }
        }

        @Override
        public void onDVRState(boolean isShow) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onDVRState(isShow);
                }
            }
        }
    }

    /**
     * 展会状态控制
     */
    private class ExhibitionStateObservable extends Observable<IExhibitionStateListener> implements IExhibitionStateListener {
        @Override
        public void registerObserver(IExhibitionStateListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(IExhibitionStateListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onExhibitionState(boolean isShow) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onExhibitionState(isShow);
                }
            }
        }
    }

    /**
     * 转向灯控制
     */
    private class TurnLightObservable extends Observable<ITurnLightListener> implements ITurnLightListener {
        @Override
        public void registerObserver(ITurnLightListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(ITurnLightListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onTurnLightChange(int direction) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onTurnLightChange(direction);
                }
            }
        }
    }

    /**
     * SDK状态控制
     */
    private class AdasStateObservable extends Observable<IAdasStateListener> implements IAdasStateListener {
        @Override
        public void registerObserver(IAdasStateListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(IAdasStateListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onAdasError(int code) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onAdasError(code);
                }
            }
        }
    }

    /**
     * SDK状态控制
     */
    private class DmsStateObservable extends Observable<IDmsStateListener> implements IDmsStateListener {
        @Override
        public void registerObserver(IDmsStateListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(IDmsStateListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onDmsError(int code) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onDmsError(code);
                }
            }
        }
    }

    /**
     * 图像质量控制
     */
    private class QualityObservable extends Observable<IQualityListener> implements IQualityListener {
        @Override
        public void registerObserver(IQualityListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(IQualityListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onCheckShelter(String... params) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onCheckShelter(params);
                }
            }
        }
    }

    /**
     * 性能日志控制
     */
    private class PerformanceLogObservable extends Observable<IPerformanceLogSwitchListener> implements
            IPerformanceLogSwitchListener {
        @Override
        public void registerObserver(IPerformanceLogSwitchListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(IPerformanceLogSwitchListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onPerformanceLogSwitchChanged(boolean isEnable) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onPerformanceLogSwitchChanged(isEnable);
                }
            }
        }
    }

    /**
     * 测试模式控制
     */
    private class TestModeObservable extends Observable<ITestModeListener> implements
            ITestModeListener {
        @Override
        public void registerObserver(ITestModeListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(ITestModeListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onTestModeChanged(boolean isEnable) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onTestModeChanged(isEnable);
                }
            }
        }

        /**
         * 假速度改变
         *
         * @param speed 速度 km/h
         */
        @Override
        public void onFakeSpeedChanged(float speed) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onFakeSpeedChanged(speed);
                }
            }
        }
    }


    /**
     * 过标模式控制
     */
    private class OverStandardModeObservable extends Observable<IOverStandardModeListener> implements
            IOverStandardModeListener {
        @Override
        public void registerObserver(IOverStandardModeListener observer) {
            if (mObservers.contains(observer)) {
                return;
            }
            super.registerObserver(observer);
        }

        @Override
        public void unregisterObserver(IOverStandardModeListener observer) {
            if (!mObservers.contains(observer)) {
                return;
            }
            super.unregisterObserver(observer);
        }

        @Override
        public void onOverStandardModeChanged(boolean isEnable) {
            synchronized (mObservers) {
                for (int i = 0; i < mObservers.size(); ++i) {
                    mObservers.get(i).onOverStandardModeChanged(isEnable);
                }
            }
        }
    }
}
