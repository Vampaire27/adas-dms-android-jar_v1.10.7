package com.hobot.sample.app.module.adas;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.location.Location;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hobot.adas.sdk.ErrorCode;
import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.adas.sdk.listener.IAdasFrameListener;
import com.hobot.adas.sdk.listener.IAdasProtoListener;
import com.hobot.adas.sdk.listener.IAdasRenderListener;
import com.hobot.camera.library.base.Option;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.nebula.common.proxy.EventProtoProxy;
import com.hobot.sample.app.R;
import com.hobot.sample.app.activity.BaseNewMainActivity;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.listener.IAdasStateListener;
import com.hobot.sample.app.listener.ITurnLightListener;
import com.hobot.sample.app.manager.NebulaObservableManager;
import com.hobot.sample.app.manager.NebulaSDKManager;
import com.hobot.sample.app.module.base.BaseView;
import com.hobot.sample.app.view.BaseCalibrationView;
import com.hobot.sdk.library.jni.input.HobotImage;
import com.hobot.sdk.library.modules.log.HobotLog;
import com.hobot.sdk.widget.preview.OpenPreview;
import com.hobot.sdk.widget.preview.image.ImageFrame;
import com.hobot.sdk.widget.preview.shape.Line;
import com.hobot.sdk.widget.preview.shape.Rect;
import com.hobot.sdk.widget.preview.shape.ShapeFrame;
import com.hobot.sdk.widget.preview.shape.Text;

import java.util.ArrayList;
import java.util.List;

import ADASOutputEventProtocol.ADASOutputEventOuterClass;
import DisplayProtobuf.Display;

/**
 * ADAS 基础控件。
 *
 * @author Hobot
 */
public class AdasBaseView extends BaseView implements IAdasRenderListener, IAdasProtoListener, AdasCalibrationView
        .OnStepListener, ITurnLightListener, IAdasStateListener, IAdasFrameListener {
    private static final String TAG = "AdasBaseView";
    private static final int INVALID_CAR_ID = -1;
    private static final int INVALID_LANE_ID = -1;
    private static final int LINE_LEFT = 1;
    private static final int LINE_RIGHT = 2;
    private OpenPreview mPreview;
    private AdasCalibrationView mCaliAcv;
    private ImageButton mCaliIb;
    // 左转灯
    private int mLeftTurnLightState = HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_OFF;
    // 右转灯
    private int mRightTurnLightState = HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_OFF;

    public AdasBaseView() {
        mCameraId = DefaultConfig.DEFAULT_ADAS_CAMERA_ID;
        mCameraType = DefaultConfig.DEFAULT_ADAS_CAMERA_TYPE;
    }

    @Override
    public int layoutId() {
        return R.layout.view_adas_layout;
    }

    @Override
    public void onViewCreated(View view) {
        mPreview = (OpenPreview) view.findViewById(R.id.preview_adas);
        // 开启缩放
        mPreview.enableScale();
        mPreview.setDisplayMode(OpenPreview.DISPLAY_MODE_IMAGE | OpenPreview.DISPLAY_MODE_SHAPE);

        mCaliAcv = (AdasCalibrationView) view.findViewById(R.id.acv_adas_cali);
        mCaliIb = (ImageButton) view.findViewById(R.id.ib_cali);
        mCaliIb.setOnClickListener(this);
        mCaliAcv.setOnStepListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        HobotAdasSDK.getInstance().registerProtoListener(this);
        HobotAdasSDK.getInstance().registerRenderListener(this);
        HobotAdasSDK.getInstance().registerFrameListener(this);
        NebulaObservableManager.getInstance().registerAdasStateListener(this);
        NebulaObservableManager.getInstance().registerTurnLightListener(this);
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS);
    }

    @Override
    public void onStop() {
        super.onStop();
        HobotAdasSDK.getInstance().unregisterProtoListener(this);
        HobotAdasSDK.getInstance().unregisterRenderListener(this);
        NebulaObservableManager.getInstance().unregisterTurnLightListener(this);
        NebulaObservableManager.getInstance().unregisterAdasStateListener(this);
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS);
    }

    @Override
    public void onViewRelease() {
        mCaliAcv.setOnStepListener(null);
        mCaliAcv.release();
        mCaliAcv = null;
    }

    @Override
    public void onFrame(int camera, byte[] data, long timestamp, Option option) {
        // 如果需要喂GPS速度就喂当前的GPS速度
        if (DefaultConfig.SUPPORT_FAKE_SPEED) {
            // 假速度模式固定喂设置速度。
            HobotAdasSDK.getInstance().feedSpeed(mFakeSpeed, timestamp);
        }
        int width = option.previewWidth;
        int height = option.previewHeight;
        HobotAdasSDK.getInstance().feedImage(data, width, height, option.format, timestamp);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.ib_cali: {
                if (!HobotAdasSDK.getInstance().isInit() && !HobotAdasSDK.getInstance().isStart()) {
                    Toast.makeText(mContext, R.string.calibration_error_adas_not_run, Toast.LENGTH_SHORT).show();
                    HobotLog.d(TAG, "onClick() called adas not run,with: v = [" + v + "]");
                    return;
                }
                mCaliIb.setVisibility(View.GONE);
                mCaliAcv.startCalibration();
            }
            break;
        }
    }

    //Adas render/车辆/车道线 显示绘制控制
    @Override
    public void onRenderState(boolean isShow) {
        // 保留图像模式
        int mode = (mPreview.getDisplayMode() & OpenPreview.DISPLAY_MODE_IMAGE);
        // 如果显示 设置图形模式
        if (isShow) {
            mPreview.setVisibility(View.VISIBLE);
            mode |= OpenPreview.DISPLAY_MODE_SHAPE;
        }
        // 如果是不显示
        else {
            // 判断是否啥都不显示
            if (mode == OpenPreview.DISPLAY_MODE_NONE) {
                mPreview.setVisibility(View.GONE);
            }
        }
        // 设置模式
        mPreview.setDisplayMode(mode);
        // 清屏
        mPreview.clearScreen();
    }

    //Adas dvr/网格线 显示控制
    @Override
    public void onDVRState(boolean isShow) {
        // 保留图像模式
        int mode = (mPreview.getDisplayMode() & OpenPreview.DISPLAY_MODE_SHAPE);
        // 如果显示 设置图像模式
        if (isShow) {
            mPreview.setVisibility(View.VISIBLE);
            mode |= OpenPreview.DISPLAY_MODE_IMAGE;
        }
        // 如果是不显示
        else {
            if (mode == OpenPreview.DISPLAY_MODE_NONE) {
                mPreview.setVisibility(View.GONE);
            }
        }
        // 设置模式
        mPreview.setDisplayMode(mode);
        // 清屏
        mPreview.clearScreen();

    }



    @Override
    public void onProtoOut(ADASOutputEventOuterClass.ADASOutputEvent outputEvent) {
        List<ADASOutputEventOuterClass.EventResult> events = outputEvent.getEventResultList();
        if (events == null || events.size() == 0) {
            return;
        }
        boolean hasLDW = false;
        boolean hasHWM = false;
        // 如果当前转向灯信号是开的状态
        int leftTurnLightState = mLeftTurnLightState;
        int rightTurnLightState = mRightTurnLightState;
        // 获取所有ADAS事件，移除LDW相关的事件
        final List<ADASOutputEventOuterClass.EventResult> tempResults = new ArrayList<>(events);
        for (ADASOutputEventOuterClass.EventResult result : events) {
            int number = result.getEvent().getNumber();
            switch (number) {
                case ADASOutputEventOuterClass.EventEnum.kEvent_Ldw_Warning_Left_VALUE:
                    hasLDW = true;
                    if (leftTurnLightState == HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_LEFT) {
                        //转向灯
                        tempResults.remove(result);
                    }
                    if (DefaultConfig.SUPPORT_ADAS_LANE_TRACKING) {
                        List<ADASOutputEventOuterClass.Line> lines = outputEvent.getLinesList();
                        for (ADASOutputEventOuterClass.Line line : lines) {
                            if (line.getType() == LINE_LEFT) {
                                int id = line.getId();
                                if (flatSameEvent(number, id)) {
                                    tempResults.remove(result);
                                }
                            }
                        }
                    }
                    break;
                case ADASOutputEventOuterClass.EventEnum.kEvent_Ldw_Warning_Right_VALUE:
                    hasLDW = true;
                    if (rightTurnLightState == HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_RIGHT) {
                        tempResults.remove(result);
                    }
                    if (DefaultConfig.SUPPORT_ADAS_LANE_TRACKING) {
                        List<ADASOutputEventOuterClass.Line> lines = outputEvent.getLinesList();
                        for (ADASOutputEventOuterClass.Line line : lines) {
                            if (line.getType() == LINE_RIGHT) {
                                int id = line.getId();
                                if (flatSameEvent(number, id)) {
                                    tempResults.remove(result);
                                }
                            }
                        }
                    }
                    break;
                case ADASOutputEventOuterClass.EventEnum.kEvent_Hmw_Warning_VALUE:
                    hasHWM = true;
                    if (DefaultConfig.SUPPORT_ADAS_VEHICLE_TRACKING) {
                        List<ADASOutputEventOuterClass.Vehicle> vehicles = outputEvent.getVehiclesList();
                        for (ADASOutputEventOuterClass.Vehicle vehicle : vehicles) {
                            if (vehicle.getKeyObject()) {
                                int id = vehicle.getId();
                                if (flatSameEvent(number, id)) {
                                    tempResults.remove(result);
                                }
                            }
                        }
                    }
                    break;
            }
        }
        if (!hasLDW) {
            flatSameEvent(ADASOutputEventOuterClass.EventEnum.kEvent_Ldw_Warning_Left_VALUE, INVALID_LANE_ID);
            flatSameEvent(ADASOutputEventOuterClass.EventEnum.kEvent_Ldw_Warning_Right_VALUE, INVALID_LANE_ID);
        }
        if (!hasHWM) {
            flatSameEvent(ADASOutputEventOuterClass.EventEnum.kEvent_Hmw_Warning_VALUE, INVALID_CAR_ID);
        }
        processEvents(tempResults);
    }

    /**
     * 处理事件
     *
     * @param results 事件列表
     */
    private void processEvents(final List<ADASOutputEventOuterClass.EventResult> results) {
        if (results.size() == 0) {
            return;
        }
        final EventProtoProxy priorityProxy = new EventProtoProxy() {
            @Override
            public int eventProtoPos(int index) {
                return results.get(index).getEvent().getNumber();
            }

            @Override
            public String eventProtoName(int index) {
                return results.get(index).getEvent().name();
            }

            @Override
            public int eventNum() {
                return results.size();
            }

            @Override
            public long eventTime(int index) {
                return results.get(index).getTimestamp();
            }

            @Override
            public int frameId(int index) {
                return results.get(index).getFrameId();
            }

            @Override
            public float distance(int index) {
                return results.get(index).getDistance();
            }

            @Override
            public float speed(int index) {
                return results.get(index).getSpeed();
            }

            @Override
            public String filePath(int index) {
                if (results.get(index).getScreenshotPathCount() > 0) {
                    return results.get(index).getScreenshotPath(0);
                }
                return null;
            }
        };
        // 报警策略
        HobotWarningSDK.getInstance().warn(priorityProxy);
        // 上传策略
        HobotWarningSDK.getInstance().upload(priorityProxy);
    }

    @Override
    public void onStep(int index) {
        switch (index) {
            case BaseCalibrationView.STATE_FINISH:
            case BaseCalibrationView.STATE_CANCEL:
            case BaseCalibrationView.STATE_FAIL: {
                mCaliIb.setVisibility(View.VISIBLE);
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
                mLeftTurnLightState = HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_OFF;
                mRightTurnLightState = HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_OFF;
                break;
            case NebulaObservableManager.TURN_LIGHT_LEFT:
                mLeftTurnLightState = HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_LEFT;
                break;
            case NebulaObservableManager.TURN_LIGHT_RIGHT:
                mRightTurnLightState = HobotAdasSDK.TURN_LIGHT_STATE.TURN_LIGHT_RIGHT;
                break;
        }
    }

    @Override
    public void onAdasError(final int code) {
        if (code == ErrorCode.ADAS_SUCCESS) {
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
                        ((BaseNewMainActivity) context).showErrorDialog(context.getString(R.string.adas_start_failed),
                                ErrorCode.parseCode(code));
                    }
                });
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!DefaultConfig.SUPPORT_FAKE_SPEED) {
            HobotAdasSDK.getInstance().feedLocation(location);
        }
    }


    @Override
    public void onTestModeChanged(boolean isEnable) {
        super.onTestModeChanged(isEnable);
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS);
        if (isEnable) {
            HobotAdasSDK.getInstance().initConfig(mContext, "config_test");
        } else {
            HobotAdasSDK.getInstance().initConfig(mContext, "config");
        }
        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS);
    }

    /**
     * 处理图像
     *
     * @param output
     */
    private void processImage(HobotImage output) {
        if (mPreview.getVisibility() != View.VISIBLE) {
            return;
        }
        ImageFrame frame = mPreview.getImageFrame();
        if (null == frame) {
            return;
        }
        int size = (int) (1280 * 720 * 1.5);
        System.arraycopy(output.getData(), 0, frame.data, 0, size);
        frame.id = output.getId();
        frame.width = 1280;
        frame.height = 720;
        frame.color = ImageFormat.YV12;
        frame.size = size;
        mPreview.queue(frame);
    }

    /**
     * 处理图形
     */
    private void processShape(Display.DisplayFrame output) {
        if (mPreview.getVisibility() != View.VISIBLE) {
            return;
        }
        long begin = System.currentTimeMillis();
        ShapeFrame frame = mPreview.getShapeFrame();
        if (null == frame) {
            return;
        }
        // 车辆
        for (Display.VehicleResult result : output.getVehiclesList()) {
            Display.Rect rect = result.getRectObs();
            Rect rect1 = frame.takeRect();
            rect1.bold = 2f;
            rect1.left = (int) rect.getLeft();
            rect1.top = (int) rect.getTop();
            rect1.right = (int) rect.getRight();
            rect1.bottom = (int) rect.getBottom();
            int color = Color.GREEN;
            switch (result.getWarningLevel()) {
                case 0:
                    color = Color.GREEN;
                    break;
                case 1024:
                    color = Color.YELLOW;
                    break;
                case 1:
                case 2:
                    color = Color.RED;
                    break;
                default:
                    color = Color.GREEN;
                    break;
            }

            //车辆ID
            rect1.color = color;
            frame.queue(rect1);
            Text text = frame.takeText();
            text.text = "VID: " + result.getId();
            text.x = (int) rect.getLeft();
            text.y = (int) rect.getTop() - 5;
            text.color = Color.GREEN;
            frame.queue(text);

            //车辆距离
            Text distanceText = frame.takeText();
            distanceText.text = "DIS:  " + Math.round(result.getRelativeLocation().getZ());
            distanceText.x = (int) rect.getLeft();
            distanceText.y = (int) rect.getTop() - 20;
            distanceText.color = Color.YELLOW;
            frame.queue(distanceText);
        }

        // 行人
        for (Display.VehicleResult result : output.getPedestriansList()) {
            Display.Rect rect = result.getRectObs();
            Rect rect1 = frame.takeRect();
            rect1.bold = 2f;
            rect1.left = (int) rect.getLeft();
            rect1.top = (int) rect.getTop();
            rect1.right = (int) rect.getRight();
            rect1.bottom = (int) rect.getBottom();
            int color = Color.GREEN;
            switch (result.getWarningLevel()) {
                case 0:
                    color = Color.GREEN;
                    break;
                case 1024:
                    color = Color.YELLOW;
                    break;
                case 1:
                case 2:
                    color = Color.RED;
                    break;
                default:
                    color = Color.GREEN;
                    break;
            }

            rect1.color = color;
            frame.queue(rect1);
            Text text = frame.takeText();
            text.text = "PID: " + result.getId();
            text.x = (int) rect.getLeft();
            text.y = (int) rect.getTop() - 5;
            text.color = Color.GREEN;
            frame.queue(text);
        }

        // 车道线
        for (Display.LineResult result : output.getLinesList()) {
            int color;
            switch (result.getWarningLevel()) {
                case 0:
                    color = Color.GREEN;
                    break;
                case 1024:
                    color = Color.YELLOW;
                    break;
                case 1:
                case 2:
                    color = Color.RED;
                    break;
                default:
                    color = Color.GREEN;
                    break;
            }
            if (result.getLinesList().size() > 0) {
                Text text = frame.takeText();
                text.text = "LID: " + result.getId();
                text.x = result.getLines(result.getLinesCount() - 1).getStartPoint().getX();
                text.y = result.getLines(result.getLinesCount() - 1).getStartPoint().getY();
                text.color = color;
                frame.queue(text);
            }

            for (Display.Line line : result.getLinesList()) {
                Line line1 = frame.takeLine();
                line1.bold = 3f;
                line1.begin.x = (int) line.getStartPoint().getX();
                line1.begin.y = (int) line.getStartPoint().getY();
                line1.end.x = (int) line.getEndPoint().getX();
                line1.end.y = (int) line.getEndPoint().getY();
                line1.color = color;
                frame.queue(line1);
            }
        }

        // 网格线
        for (Display.Line line : output.getGridLines().getLinesList()) {
            Line line1 = frame.takeLine();
            line1.bold = 1f;
            line1.begin.x = (int) line.getStartPoint().getX();
            line1.begin.y = (int) line.getStartPoint().getY();
            line1.end.x = (int) line.getEndPoint().getX();
            line1.end.y = (int) line.getEndPoint().getY();
            line1.color = Color.YELLOW;
            frame.queue(line1);
        }

        // 天地消失点
        {
            Display.Point point = output.getVanishPoint();

            Line line1 = frame.takeLine();

            line1.bold = 1.5f;
            line1.begin.x = (int) point.getX() - 20;
            line1.begin.y = (int) point.getY();
            line1.end.x = (int) point.getX() + 20;
            line1.end.y = (int) point.getY();
            line1.color = Color.YELLOW;
            frame.queue(line1);

            Line line2 = frame.takeLine();
            line2.bold = 1.5f;
            line2.begin.x = (int) point.getX();
            line2.begin.y = (int) point.getY() + 20;
            line2.end.x = (int) point.getX();
            line2.end.y = (int) point.getY() - 20;
            line2.color = Color.YELLOW;

            frame.queue(line2);
        }

        mPreview.queue(frame);
        long end = System.currentTimeMillis();
        long cost = end - begin;
        if (cost > 10) {
            HobotLog.w(TAG, "processShape: cost = [" + cost + "ms] at [" + frame.hashCode() + "]");
        }
    }

    /**
     * 每一帧图像的ADAS绘制回调。
     *
     * @param output 绘制 {@link HobotImage}
     */
    @Override
    public void onAdasRender(HobotImage output) {
        processImage(output);
    }

    /**
     * 每一帧图像的ADAS绘制回调。
     *
     * @param frame 绘制 {@link Display.DisplayFrame}
     */
    @Override
    public void onAdasFrame(Display.DisplayFrame frame) {
        // 显示Frame
        processShape(frame);
        // 设置速度
        float speed = frame.getMotion().getSpeed();
        setSpeed(speed);
        // 设置距离
        float distance = frame.getDistance();
        setDistance(distance);
    }
}
