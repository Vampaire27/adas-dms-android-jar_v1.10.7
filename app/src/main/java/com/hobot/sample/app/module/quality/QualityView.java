package com.hobot.sample.app.module.quality;

import com.hobot.camera.library.base.IPreviewCallback;
import com.hobot.camera.library.base.Option;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.listener.IQualityListener;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.module.base.BaseNoLayoutView;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 图像质量控件。
 *
 * @author Hobot
 */
public class QualityView extends BaseNoLayoutView implements IPreviewCallback, IQualityListener {

    private static final String TAG = "QualityView";
    // 图像质量
    private QualityProcessor mQualityProcessor = new QualityProcessor();
    // 只在开机时坐一次检测
    private AtomicBoolean mAtomicCheckShelterFlag = new AtomicBoolean(true);

    public QualityView() {
        mCameraId = DefaultConfig.DEFAULT_IMAGE_QUALITY_CAMERA_ID;
        mCameraType = DefaultConfig.DEFAULT_IMAGE_QUALITY_CAMERA_TYPE;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();
        mQualityProcessor.init(mContext).setCheckDAAEnable(true);
    }

    @Override
    public void onViewRelease() {
        super.onViewRelease();
        mQualityProcessor.destroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        NebulaObservableManager.getInstance().registerQualityListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        NebulaObservableManager.getInstance().unregisterQualityListener(this);
    }

    @Override
    public void onFrame(int camera, byte[] data, long timestamp, Option option) {
        int width = option.previewWidth;
        int height = option.previewHeight;
        if (mAtomicCheckShelterFlag.getAndSet(false)) {
            mQualityProcessor.activeCheckShelter(false);
        }
        mQualityProcessor.processImage(data, width, height, option.format, timestamp);
    }

    @Override
    public void onCheckShelter(String... params) {
        mQualityProcessor.activeCheckShelter(params[0]);
    }
}
