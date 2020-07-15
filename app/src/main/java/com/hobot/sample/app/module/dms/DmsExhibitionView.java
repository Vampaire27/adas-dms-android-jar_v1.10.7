package com.hobot.sample.app.module.dms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hobot.sample.app.R;
import com.hobot.sample.app.view.Shape;
import com.hobot.sample.app.view.WaveView;
import com.hobot.sdk.library.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import DMSOutputProtocol.DMSSDKOutputOuterClass;

/**
 * 展会模式控件
 *
 * @author Hobot
 */
public class DmsExhibitionView extends RelativeLayout {

    private static final String TAG = DmsExhibitionView.class.getSimpleName();
    private static final float STANDARD_VIEW_HEIGHT = 720;
    private static final float STANDARD_VIEW_WIDTH = 1280;
    private static final float STANDARD_VIDEO_HEIGHT = 720;
    private static final float STANDARD_VIDEO_WIDTH = 1280;

    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_BLUE = 0xFF00D0BB;
    private static final int COLOR_GOLD = 0xFFFFC400;

    private float mVideoHeight = STANDARD_VIDEO_HEIGHT;
    private float mVideoWidth = STANDARD_VIDEO_WIDTH;
    private float mViewXScale = 1;
    private float mViewYScale = 1;
    private float mVideoXScale = 1;
    private float mVideoYScale = 1;

    private TextView leftEyeStatus;
    private TextView rightEyeStatus;
    private TextView faceOrientation;
    private TextView headPosition;
    private TextView headRotation;
    private TextView fatigueStatus;
    private TextView callingStatus;
    private TextView smokingStatus;
    private TextView yawningStatus;
    private TextView distractionStatus;
    private TextView abnormalStatus;
    private CoverView coverView;
    private WaveView callWave;
    private WaveView smokeWave;

    private List<Shape.Rect> roiRecs = new LinkedList<>();
    private List<Shape.Text> roiTexts = new LinkedList<>();
    private List<Shape.Circle> featurePoints = new ArrayList<>();
    private List<Shape.Circle> tempFeaturePoints = new ArrayList<>();

    public DmsExhibitionView(Context context) {
        this(context, null);
    }

    public DmsExhibitionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DmsExhibitionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 处理数据
     *
     * @param out 数据
     */
    public void process(final DMSSDKOutputOuterClass.DMSSDKOutput out) {
        if (getVisibility() == VISIBLE) {
            // 人脸框
            if (roiRecs.size() > 0) {
                DMSSDKOutputOuterClass.FaceROIResult faceRoiResult = out.getFaceRoiResult();
                DMSSDKOutputOuterClass.Rect faceRoi = faceRoiResult.getFaceRoi();
                int faceRoiLeft = faceRoi.getLeft();
                int faceRoiRight = faceRoi.getRight();
                int faceRoiTop = faceRoi.getTop();
                int faceRoiBottom = faceRoi.getBottom();

                roiRecs.get(0).left = faceRoiLeft * mVideoXScale;
                roiRecs.get(0).right = faceRoiRight * mVideoXScale;
                roiTexts.get(0).x = faceRoiLeft * mVideoXScale;

                roiRecs.get(0).top = faceRoiTop * mVideoYScale;
                roiRecs.get(0).bottom = faceRoiBottom * mVideoYScale;
                roiTexts.get(0).y = (faceRoiTop - 5) * mVideoYScale;
            }
            // 人脸特征点
            List<Shape.Circle> tempPoints = new ArrayList<>();
            DMSSDKOutputOuterClass.FaceFeaturePoint faceFeaturePoint = out.getFaceFeatureResult();
            List<DMSSDKOutputOuterClass.Point> points = faceFeaturePoint.getFeaturePointList();
            for (DMSSDKOutputOuterClass.Point point : points) {
                float x = point.getX() * mVideoXScale;
                tempPoints.add(Shape.circle(x, point.getY() * mVideoYScale, 3 * mViewXScale,
                        COLOR_WHITE));
            }
            synchronized (coverView.sync) {
                tempFeaturePoints.clear();
                tempFeaturePoints.addAll(tempPoints);
            }
            coverView.postInvalidate();
            float smokeScore = out.getSmokeResult().getExistScore();
            smokeWave.addData(smokeScore);
            smokeWave.postInvalidate();
            float phoneScore = out.getPhoneResult().getExistScore();
            callWave.addData(phoneScore);
            callWave.postInvalidate();
//            Log.d(TAG, "process: smokeScore = " + smokeScore + ", phoneScore = " + phoneScore);
            post(new Runnable() {
                @Override
                public void run() {
                    cook(out);
                }
            });
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (coverView != null) {
            coverView.release();
        }
        if (roiRecs != null) {
            roiRecs.clear();
        }
        if (roiTexts != null) {
            roiTexts.clear();
        }
        if (featurePoints != null) {
            featurePoints.clear();
        }
        if (tempFeaturePoints != null) {
            tempFeaturePoints.clear();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            resizeView();
            initData();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewXScale = w / STANDARD_VIEW_WIDTH;
        mViewYScale = h / STANDARD_VIEW_HEIGHT;
        mVideoXScale = w / mVideoWidth;
        mVideoYScale = h / mVideoHeight;
        Log.d(TAG, "onSizeChanged: WxH = " + w + "x" + h + ", mViewXScale = " + mViewXScale + ", mViewYScale = " +
                mViewYScale);
    }

    /**
     * 初始化布局
     */
    private void initView() {
        inflate(getContext(), R.layout.view_dms_exhibition, this);
        leftEyeStatus = (TextView) findViewById(R.id.left_eye_status);
        rightEyeStatus = (TextView) findViewById(R.id.right_eye_status);
        faceOrientation = (TextView) findViewById(R.id.face_orientation);
        headPosition = (TextView) findViewById(R.id.head_position);
        headRotation = (TextView) findViewById(R.id.head_rotation);
        fatigueStatus = (TextView) findViewById(R.id.fatigue_status);
        callingStatus = (TextView) findViewById(R.id.calling_status);
        smokingStatus = (TextView) findViewById(R.id.smoking_status);
        yawningStatus = (TextView) findViewById(R.id.yawning_status);
        distractionStatus = (TextView) findViewById(R.id.distraction_status);
        abnormalStatus = (TextView) findViewById(R.id.abnormal_status);
        callWave = (WaveView) findViewById(R.id.call_wave);
        callWave.y(0, 1, 1);
        smokeWave = (WaveView) findViewById(R.id.smoke_wave);
        smokeWave.y(0, 1, 1);

        // 绘制图层
        coverView = new DmsExhibitionView.CoverView(getContext());
        coverView.setTranslationZ(2);
        addView(coverView);
    }

    /**
     * 重新设置每个控件的大小
     */
    private void resizeView() {
        LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        coverView.setLayoutParams(params);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 字体缩放尺寸
        float scale = mViewXScale * mVideoYScale * 1.5f;

        // 人脸框的描述
        roiTexts.clear();
        roiTexts.add(Shape.text("ROI_FACE", 0, 0, COLOR_BLUE, 15 * scale));
        roiRecs.clear();
        roiRecs.add(Shape.rect(0, 0, 0, 0, COLOR_WHITE));
    }

    /**
     * 处理事件
     *
     * @param out 数据
     */
    private void cook(DMSSDKOutputOuterClass.DMSSDKOutput out) {
        if (out != null) {
            // 眼睛状态
            DMSSDKOutputOuterClass.EyeStateResult eyeStateResult = out.getEyeResult();
            // 左眼状态
            int leftEyeState = eyeStateResult.getLeft().getNumber();
            leftEyeStatus.setText(getEyeStatus(leftEyeState));
            // 右眼状态
            int rightEyeState = eyeStateResult.getRight().getNumber();
            rightEyeStatus.setText(getEyeStatus(rightEyeState));

            // 人脸朝向
            DMSSDKOutputOuterClass.FaceDirectionResult faceDirectionResult = out.getFaceDirectionResult();
            List<DMSSDKOutputOuterClass.FaceDirEnum> faceDirEnumList = faceDirectionResult
                    .getFaceDirList();
            if (!faceDirEnumList.isEmpty()) {
                int faceDir = faceDirEnumList.get(0).getNumber();
                faceOrientation.setText(getFaceDir(faceDir));
            }

            // 头部位置和转角
            DMSSDKOutputOuterClass.AngleUnitVector angleUnitVector = faceDirectionResult.getCurAngleUnitVector();
            DMSSDKOutputOuterClass.Vector_3f vector3f = angleUnitVector.getUnitVector();
            int vector3fX = (int) (vector3f.getX() / 10f);
            int vector3fY = (int) (vector3f.getY() / 10f);
            int vector3fZ = (int) (vector3f.getZ() / 10f);
            if (vector3fX == 0 && vector3fY == 0 && vector3fZ == 0) {
                headPosition.setText("");
            } else {
                headPosition.setText(getContext().getString(R.string.dms_exhibition_head_position_values, vector3fX,
                        vector3fY, vector3fZ));
            }
            DMSSDKOutputOuterClass.AngleRPY angleRPY = faceDirectionResult.getCurAngleRpy();
            final double pi = 3.1415926;
            int roll = (int) (-180 * angleRPY.getRoll() / pi);
            int pitch = (int) (180 * angleRPY.getPitch() / pi);
            int yaw = (int) (-180 * angleRPY.getYaw() / pi);
            if (roll == 0 && pitch == 0 && yaw == 0) {
                headRotation.setText("");
            } else {
                headRotation.setText(getContext().getString(R.string.dms_exhibition_head_rotation_values, roll, pitch,
                        yaw));
            }
            // 事件状态
            List<DMSOutputProtocol.DMSSDKOutputOuterClass.EventResult> eventResults = out.getEventResultList();
            for (DMSSDKOutputOuterClass.EventResult eventResult : eventResults) {
                DMSOutputProtocol.DMSSDKOutputOuterClass.EventEnum eventEnum = eventResult.getEvent();
                switch (eventEnum.getNumber()) {
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_NONE_VALUE: {
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_L_VALUE:
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_R_VALUE:
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_U_VALUE:
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DDW_D_VALUE: {
                        distractionStatus.setTextColor(COLOR_GOLD);
                        distractionStatus.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                distractionStatus.setTextColor(COLOR_WHITE);
                            }
                        }, 500);
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DFW_VALUE: {
                        fatigueStatus.setTextColor(COLOR_GOLD);
                        fatigueStatus.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fatigueStatus.setTextColor(COLOR_WHITE);
                            }
                        }, 500);
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DYA_VALUE: {
                        yawningStatus.setTextColor(COLOR_GOLD);
                        yawningStatus.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                yawningStatus.setTextColor(COLOR_WHITE);
                            }
                        }, 500);
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DCA_VALUE: {
                        callingStatus.setTextColor(COLOR_GOLD);
                        callingStatus.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                callingStatus.setTextColor(COLOR_WHITE);
                            }
                        }, 500);
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DSA_VALUE: {
                        smokingStatus.setTextColor(COLOR_GOLD);
                        smokingStatus.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                smokingStatus.setTextColor(COLOR_WHITE);
                            }
                        }, 500);
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_LDR_VALUE: {
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_DAA_VALUE: {
                        abnormalStatus.setTextColor(COLOR_GOLD);
                        abnormalStatus.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                abnormalStatus.setTextColor(COLOR_WHITE);
                            }
                        }, 500);
                        break;
                    }
                    case DMSSDKOutputOuterClass.EventEnum.EVENT_CALIB_VALUE: {
                        break;
                    }
                }
            }
//            List<DMSSDKOutputOuterClass.StateResult> stateResults = out.getStateResultList();
//            for (DMSSDKOutputOuterClass.StateResult stateResult : stateResults) {
//                DMSSDKOutputOuterClass.StateEnum stateEnum = stateResult.getState();
//                switch (stateEnum.getNumber()) {
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_NONE_VALUE: {
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_DDW_VALUE: {
//                        distractionStatus.setTextColor(COLOR_GOLD);
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_DFW_VALUE: {
//                        fatigueStatus.setTextColor(COLOR_GOLD);
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_DYA_VALUE: {
//                        yawningStatus.setTextColor(COLOR_GOLD);
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_DCA_VALUE: {
//                        callingStatus.setTextColor(COLOR_GOLD);
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_DSA_VALUE: {
//                        smokingStatus.setTextColor(COLOR_GOLD);
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_LDR_VALUE: {
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_DAA_VALUE: {
//                        abnormalStatus.setTextColor(COLOR_GOLD);
//                        break;
//                    }
//                    case DMSSDKOutputOuterClass.StateEnum.STATE_CALIB_VALUE: {
//                        break;
//                    }
//                }
//            }
        }
    }

    /**
     * 获取眼睛状态
     *
     * @param num 状态
     * @return 状态
     */
    private String getEyeStatus(int num) {
        switch (num) {
            case DMSSDKOutputOuterClass.EyeStateEnum.EYE_ST_OPEN_VALUE:
                return "Open";
            case DMSSDKOutputOuterClass.EyeStateEnum.EYE_ST_CLOSE_VALUE:
                return "Close";
            case DMSSDKOutputOuterClass.EyeStateEnum.EYE_ST_UNKNOW_VALUE:
                return "Unknown";
            default:
                return "";
        }
    }

    /**
     * 获取人脸朝向
     *
     * @param num 方向
     * @return 方向
     */
    private String getFaceDir(int num) {
        switch (num) {
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_FRONT_VALUE:
                return "Front";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_LEFT_VALUE:
                return "Left";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_RIGHT_VALUE:
                return "Right";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_UP_VALUE:
                return "Up";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_DOWN_VALUE:
                return "Down";
            default:
                return "";
        }
    }

    private class CoverView extends View {
        private static final String TAG = "Exhibition" + "_CoverView";
        private final Object sync = new Object();
        private Paint paint;
        private Path path;
        private PaintFlagsDrawFilter paintFilter;
        private Bitmap bitmap;
        private Rect srcRect;

        public CoverView(Context context) {
            this(context, null);
        }

        public CoverView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public CoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setBackgroundColor(Color.argb(0, 0, 0, 0));
            paint = new Paint();
            paintFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            path = new Path();
            bitmap = BitmapUtils.obtain(context, R.raw.exhibition_detect);
            srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }

        @Override
        protected void onDraw(Canvas canvas) {
//            Log.d(TAG, "onDraw: ");
            super.onDraw(canvas);
            canvas.save();
            canvas.setDrawFilter(paintFilter);
            // 画人脸框
            paint.reset();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(COLOR_WHITE);
            paint.setStrokeWidth(2);
            for (Shape.Rect rect : roiRecs) {
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, srcRect, rect.rectf(), paint);
                }
            }

            // 画人脸框显示的字
            paint.reset();
            paint.setTypeface(Typeface.MONOSPACE);
            for (Shape.Text text : roiTexts) {
                paint.setColor(text.color);
                paint.setTextSize(text.textSize);
                canvas.drawText(text.text, text.x, text.y, paint);
            }

            // 画特征点
            paint.reset();
            paint.setColor(COLOR_WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(2);
            synchronized (sync) {
                featurePoints.clear();
                featurePoints.addAll(tempFeaturePoints);
            }
            for (Shape.Circle point : featurePoints) {
                canvas.drawCircle(point.x, point.y, point.r, paint);
            }
            paint.setStyle(Paint.Style.STROKE);
            if (!featurePoints.isEmpty() && featurePoints.size() > 27) {
                path.reset();
                // 左眉
                path.moveTo(featurePoints.get(0).x, featurePoints.get(0).y);
                path.lineTo(featurePoints.get(1).x, featurePoints.get(1).y);
                path.lineTo(featurePoints.get(2).x, featurePoints.get(2).y);

                // 右眉
                path.moveTo(featurePoints.get(3).x, featurePoints.get(3).y);
                path.lineTo(featurePoints.get(4).x, featurePoints.get(4).y);
                path.lineTo(featurePoints.get(5).x, featurePoints.get(5).y);

                // 左眼
                path.moveTo(featurePoints.get(6).x, featurePoints.get(6).y);
                path.lineTo(featurePoints.get(7).x, featurePoints.get(7).y);
                path.lineTo(featurePoints.get(8).x, featurePoints.get(8).y);
                path.lineTo(featurePoints.get(9).x, featurePoints.get(9).y);
                path.lineTo(featurePoints.get(6).x, featurePoints.get(6).y);

                // 右眼
                path.moveTo(featurePoints.get(11).x, featurePoints.get(11).y);
                path.lineTo(featurePoints.get(12).x, featurePoints.get(12).y);
                path.lineTo(featurePoints.get(13).x, featurePoints.get(13).y);
                path.lineTo(featurePoints.get(14).x, featurePoints.get(14).y);
                path.lineTo(featurePoints.get(11).x, featurePoints.get(11).y);

                // 鼻子
                path.moveTo(featurePoints.get(16).x, featurePoints.get(16).y);
                path.lineTo(featurePoints.get(17).x, featurePoints.get(17).y);
                path.lineTo(featurePoints.get(18).x, featurePoints.get(18).y);
                path.lineTo(featurePoints.get(16).x, featurePoints.get(16).y);

                // 眼睛和鼻子连线
                path.moveTo(featurePoints.get(8).x, featurePoints.get(8).y);
                path.lineTo(featurePoints.get(17).x, featurePoints.get(17).y);
                path.moveTo(featurePoints.get(11).x, featurePoints.get(11).y);
                path.lineTo(featurePoints.get(18).x, featurePoints.get(18).y);

                // 嘴唇
                path.moveTo(featurePoints.get(19).x, featurePoints.get(19).y);
                path.lineTo(featurePoints.get(20).x, featurePoints.get(20).y);
                path.lineTo(featurePoints.get(21).x, featurePoints.get(21).y);
                path.lineTo(featurePoints.get(22).x, featurePoints.get(22).y);
                path.lineTo(featurePoints.get(19).x, featurePoints.get(19).y);

                path.moveTo(featurePoints.get(19).x, featurePoints.get(19).y);
                path.lineTo(featurePoints.get(23).x, featurePoints.get(23).y);
                path.lineTo(featurePoints.get(21).x, featurePoints.get(21).y);

                path.moveTo(featurePoints.get(19).x, featurePoints.get(19).y);
                path.lineTo(featurePoints.get(24).x, featurePoints.get(24).y);
                path.lineTo(featurePoints.get(21).x, featurePoints.get(21).y);
                canvas.drawPath(path, paint);
            }
            canvas.restore();
        }

        /**
         * 释放资源
         */
        public void release() {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }
}
