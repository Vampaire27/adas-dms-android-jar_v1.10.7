package com.hobot.sample.app.module.base;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.listener.ITestModeListener;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.module.adas.AdasBaseView;
import com.hobot.sample.app.module.communicate.CommunicateView;
import com.hobot.sample.app.module.dms.DmsBaseView;
import com.hobot.sample.app.module.faceid.FaceIdView;
import com.hobot.sample.app.module.performance.PerformanceView;
import com.hobot.sample.app.module.quality.QualityView;
import com.hobot.sample.app.module.upload.BaseUploadView;
import com.hobot.sample.app.module.upload.UploadView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewPager适配器。
 * 包含以下功能：
 * 1.ADAS
 * 2.DMS
 * 3.FaceId
 * 4.语音
 * 5.图像质量
 *
 * @author Hobot
 */
public class ViewPageAdapter extends PagerAdapter implements ICommonView, ITestModeListener {
    private static final String TAG = "ViewPageAdapter";
    protected List<ICommonView> mBaseViews = new ArrayList<>();
    protected List<View> mViews = new ArrayList<>();
    private WeakReference<Context> mContext;

    public ViewPageAdapter() {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViews.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        container.removeView(mViews.get(position));
    }

    @Override
    public View onCreateView(Context context) {
        mContext = new WeakReference<>(context);

        // 是否支持ADAS功能
        if (DefaultConfig.SUPPORT_ADAS) {
            AdasBaseView adasView = new AdasBaseView();
            createView(adasView, context);
        }
        // 是否支持DMS功能
        if (DefaultConfig.SUPPORT_DMS) {
            DmsBaseView dmsBaseView = new DmsBaseView();
            createView(dmsBaseView, context);
        }

        // 是否支持图像质量
        if (DefaultConfig.SUPPORT_IMAGE_QUALITY) {
            QualityView qualityView = new QualityView();
            createView(qualityView, context);
        }

        // 是否支持网络传输
        if (DefaultConfig.SUPPORT_NET_TRANSFER_SERVER) {
            CommunicateView communicateView = new CommunicateView();
            createView(communicateView, context);
        }
        // 是否支持数据上传
        if (DefaultConfig.SUPPORT_UPLOAD) {
            BaseUploadView uploadView = new UploadView();
            createView(uploadView, context);
        }
        // 是否faceid
        if (DefaultConfig.SUPPORT_FACE_ID) {
            FaceIdView faceidView = new FaceIdView();
            createView(faceidView, context);
        }

        // 性能监视器
        PerformanceView performanceView = new PerformanceView();
        createView(performanceView, context);

        // 回调AdasBaseView
        NebulaObservableManager.getInstance().onExhibitionState(DefaultConfig.EXHIBITION_SHOW_SWITCH);
        NebulaObservableManager.getInstance().onDVRState(DefaultConfig.DVR_SHOW_SWITCH);
        NebulaObservableManager.getInstance().onRenderState(DefaultConfig.RENDER_SHOW_SWITCH);
        NebulaObservableManager.getInstance().onSpeedState(DefaultConfig.SPEED_RENDER_SWITCH);
        NebulaObservableManager.getInstance().onPreviewState(DefaultConfig.PREVIEW_SHOW_SWITCH);
        NebulaObservableManager.getInstance().onPerformanceLogSwitchChanged(DefaultConfig.PERFORMANCE_LOG_SWITCH);
        NebulaObservableManager.getInstance().onFakeSpeedChanged(DefaultConfig.FAKE_SPEED);
        NebulaObservableManager.getInstance().registerTestModeListener(this);
        return null;
    }

    public FragmentManager getFragmentManager() {
        return null;
    }

    @Override
    public void onStart() {
        for (ICommonView view : mBaseViews) {
            Log.d(TAG, "onStart: view = " + view);
            view.onStart();
        }
    }

    @Override
    public void onResume() {
        for (ICommonView view : mBaseViews) {
            Log.d(TAG, "onResume: view = " + view);
            view.onResume();
        }
    }

    @Override
    public void onPause() {
        for (ICommonView view : mBaseViews) {
            Log.d(TAG, "onPause: view = " + view);
            view.onPause();
        }
    }

    @Override
    public void onStop() {
        for (ICommonView view : mBaseViews) {
            Log.d(TAG, "onStop: view = " + view);
            view.onStop();
        }
    }

    @Override
    public void onDestroyView() {
        for (ICommonView view : mBaseViews) {
            view.onDestroyView();
        }
        mViews.clear();
        mBaseViews.clear();
        NebulaObservableManager.getInstance().unregisterTestModeListener(this);
        if (mContext != null) {
            mContext.clear();
        }
    }

    /**
     * 添加布局控件
     *
     * @param commonView SDK控件
     * @param context    上下文
     */
    private void createView(ICommonView commonView, Context context) {
        if (commonView == null || context == null) {
            return;
        }
        mBaseViews.add(commonView);
        View view = commonView.onCreateView(context);
        if (view != null) {
            mViews.add(view);
        }
    }

    @Override
    public void onTestModeChanged(boolean isEnable) {
        if (mContext != null) {
            if (isEnable) {
                HobotWarningSDK.getInstance().init(mContext.get(),true,true);
            } else {
                HobotWarningSDK.getInstance().init(mContext.get(), true);
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

    }
}
