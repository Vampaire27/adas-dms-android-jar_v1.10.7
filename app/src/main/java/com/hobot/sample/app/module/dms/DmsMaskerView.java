package com.hobot.sample.app.module.dms;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hobot.sample.app.R;
import com.hobot.sample.app.view.Shape;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import DMSOutputProtocol.DMSSDKOutputOuterClass;

/**
 * Frame's Masker Layer to Show Event Like Facing/EyeStatus
 *
 * @author zhuo.chen
 * @version 2017-07-09
 */
public class DmsMaskerView extends RelativeLayout {
    private static final String TAG = "DmsMaskerView";
    private static final float STANDARD_VIEW_HEIGHT = 720;
    private static final float STANDARD_VIEW_WIDTH = 1280;
    private static final float STANDARD_VIDEO_HEIGHT = 720;
    private static final float STANDARD_VIDEO_WIDTH = 1280;
    // 背景图片默认大小
    private static final int BACKGROUND_HEIGHT = 520;
    private static final int BACKGROUND_WIDTH = 360;

    // 小图标的大小
    private static final int SMALL_LOGO_SIZE = 30;

    private static final int COLOR_GRAY = 0xFF808080;
    private static final int COLOR_RED = 0xFFFF0000;
    private static final int COLOR_ORANGE = 0xFFFF7E26;
    private static final int COLOR_PINK = 0xFFFEADC8;
    private static final int COLOR_GREEN = 0xFF00FF00;
    private static final int COLOR_BLUE = 0xFF1EAFF8;
    private static final int COLOR_WHITE = 0xFFFFFFFF;

    private static final int POS_DFW = 0;
    private static final int POS_DDW = 1;
    private static final int POS_DYA = 2;

    private static final int POS_DSA = 3;
    private static final int POS_DCA = 4;
    private static final int POS_DAA = 5;

    private static final int POS_DEBA = 6;
    private static final int POS_DDRA = 7;
    private static final int POS_DSBA = 8;

    private float mVideoHeight = STANDARD_VIDEO_HEIGHT;
    private float mVideoWidth = STANDARD_VIDEO_WIDTH;
    private float mViewXScale = 1;
    private float mViewYScale = 1;
    private float mVideoXScale = 1;
    private float mVideoYScale = 1;

    private final List<Shape.Rect> roiRecs = new LinkedList<>();
    private final List<Shape.Text> roiTexts = new LinkedList<>();
    private final List<Shape.Text> eyeStatusTexts = new LinkedList<>();
    private final List<Shape.Text> facingDirTexts = new LinkedList<>();
    private final List<Shape.Text> driverStatusTexts = new LinkedList<>();
    private final List<Shape.Text> evtStatusTexts = new LinkedList<>();
    private Shape.Text userText;
    private final List<Shape.Text> otherTexts = new LinkedList<>();
    private final List<Shape.Circle> featurePoints = new ArrayList<>();
    private final List<Shape.Circle> mTempFeaturePoints = new ArrayList<>();
    private final Path mLdmkPath = new Path();

    private boolean isCall = false;
    private boolean isSmoke = false;
    private ImageView boundIv;
    private ImageView smokeIv;
    private ImageView callIv;
    private CoverView coverView;
    /**
     * 刷新抽烟和打电话图标
     */
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (callIv != null) {
                callIv.setSelected(isCall);
            }
            if (smokeIv != null) {
                smokeIv.setSelected(isSmoke);
            }
            isCall = false;
            isSmoke = false;
        }
    };

    public DmsMaskerView(Context context) {
        this(context, null);
    }

    public DmsMaskerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DmsMaskerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化控件
     */
    private void init() {
        // 背景
        boundIv = new ImageView(getContext());
        boundIv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.background_resized));
        boundIv.setTranslationZ(1);
        // 抽烟图标
        smokeIv = new ImageView(getContext());
        smokeIv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_selector_smoke));
        smokeIv.setTranslationZ(1);
        // 打电话图标
        callIv = new ImageView(getContext());
        callIv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_selector_call));
        callIv.setTranslationZ(1);
        // 绘制图层
        coverView = new CoverView(getContext());
        coverView.setTranslationZ(2);

        addView(boundIv);
        addView(smokeIv);
        addView(callIv);
        addView(coverView);
    }

    /**
     * 释放资源
     */
    public void release() {

    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 字体缩放尺寸
        float scale = mViewXScale * mVideoYScale * 1.5f;
        // 用户姓名
        // TODO:不显示当前用户名
        userText = Shape.text("", 170 * mViewXScale, 188 * mViewYScale, COLOR_RED, 20 * scale);

        // 眼睛状态
        eyeStatusTexts.clear();
        // 左眼
        eyeStatusTexts.add(Shape.text("NA", 150 * mViewXScale, 315 * mViewYScale, COLOR_ORANGE, 15 * scale));
        // 右眼
        eyeStatusTexts.add(Shape.text("NA", 270 * mViewXScale, 315 * mViewYScale, COLOR_ORANGE, 15 * scale));

        // 人脸朝向
        facingDirTexts.clear();
        facingDirTexts.add(Shape.text("", 170 * mViewXScale, 403 * mViewYScale, COLOR_PINK, 20 * scale));

        // 人脸框的描述
        roiTexts.clear();
        roiTexts.add(Shape.text("ROI_FACE", 0, 0, COLOR_BLUE, 15 * scale));
        roiRecs.clear();
        roiRecs.add(Shape.rect(0, 0, 0, 0, COLOR_GRAY));

        // 驾驶员状态
        driverStatusTexts.clear();
        // DFW
        driverStatusTexts.add(Shape.text("DFW", 1060 * mViewXScale, 350 * mViewYScale, COLOR_WHITE, 16 * scale));
        // DDW
        driverStatusTexts.add(Shape.text("DDW", 1060 * mViewXScale, 380 * mViewYScale, COLOR_WHITE, 16 * scale));
        // DYA
        driverStatusTexts.add(Shape.text("DYA", 1060 * mViewXScale, 410 * mViewYScale, COLOR_WHITE, 16 * scale));

        // DSA
        driverStatusTexts.add(Shape.text("DSA", 1130 * mViewXScale, 350 * mViewYScale, COLOR_WHITE, 16 * scale));
        // DCA
        driverStatusTexts.add(Shape.text("DCA", 1130 * mViewXScale, 380 * mViewYScale, COLOR_WHITE, 16 * scale));
        // DAA
        driverStatusTexts.add(Shape.text("DAA", 1130 * mViewXScale, 410 * mViewYScale, COLOR_WHITE, 16 * scale));

        // DEBA
        driverStatusTexts.add(Shape.text("DEBA", 1190 * mViewXScale, 350 * mViewYScale, COLOR_WHITE, 16 * scale));
        // DDRA
        driverStatusTexts.add(Shape.text("DDRA", 1190 * mViewXScale, 380 * mViewYScale, COLOR_WHITE, 16 * scale));
        // DSBA
        driverStatusTexts.add(Shape.text("DSBA", 1190 * mViewXScale, 410 * mViewYScale, COLOR_WHITE, 16 * scale));

        // 事件状态
        evtStatusTexts.clear();
        // DFW
        evtStatusTexts.add(Shape.text("ET_DFW", 100 * mViewXScale, 515 * mViewYScale, COLOR_WHITE, 15 * scale));
        // DDW
        evtStatusTexts.add(Shape.text("ET_DDW", 100 * mViewXScale, 550 * mViewYScale, COLOR_WHITE, 15 * scale));
        // DYA
        evtStatusTexts.add(Shape.text("ET_DYA", 100 * mViewXScale, 585 * mViewYScale, COLOR_WHITE, 15 * scale));

        // DSA
        evtStatusTexts.add(Shape.text("ET_DSA", 170 * mViewXScale, 515 * mViewYScale, COLOR_WHITE, 15 * scale));
        // DCA
        evtStatusTexts.add(Shape.text("ET_DCA", 170 * mViewXScale, 550 * mViewYScale, COLOR_WHITE, 15 * scale));
        // DAA
        evtStatusTexts.add(Shape.text("ET_DAA", 170 * mViewXScale, 585 * mViewYScale, COLOR_WHITE, 15 * scale));

        // DEBA
        evtStatusTexts.add(Shape.text("ET_DEBA", 240 * mViewXScale, 515 * mViewYScale, COLOR_WHITE, 15 * scale));
        // DDRA
        evtStatusTexts.add(Shape.text("ET_DDRA", 240 * mViewXScale, 550 * mViewYScale, COLOR_WHITE, 15 * scale));
        // DSBA
        evtStatusTexts.add(Shape.text("ET_DSBA", 240 * mViewXScale, 585 * mViewYScale, COLOR_WHITE, 15 * scale));
    }

    /**
     * 重新设置每个控件的大小
     */
    private void resizeView() {
        // 背景图片
        LayoutParams params = new RelativeLayout.LayoutParams((int) (BACKGROUND_WIDTH * mViewXScale), (int)
                (BACKGROUND_HEIGHT * mViewYScale));
        Log.d(TAG, "resizeView -- boundIv: width = " + params.width + ", height = " + params
                .height + ", mViewXScale = " + mViewXScale + ", mViewYScale = " + mViewYScale);
        params.addRule(CENTER_VERTICAL);
        params.setMargins((int) (20 * mViewXScale), 0, 0, 0);
        boundIv.setLayoutParams(params);

        // 绘制图层
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        coverView.setLayoutParams(params);

        // 抽烟图片
        params = new RelativeLayout.LayoutParams((int) (SMALL_LOGO_SIZE * mViewXScale), (int) (SMALL_LOGO_SIZE *
                mViewXScale));
        params.setMargins((int) (1035 * mViewXScale), (int) (485 * mViewYScale), 0, 0);
        smokeIv.setLayoutParams(params);

        // 电话图片
        params = new RelativeLayout.LayoutParams((int) (SMALL_LOGO_SIZE * mViewXScale), (int) (SMALL_LOGO_SIZE *
                mViewXScale));
        params.setMargins((int) (1176 * mViewXScale), (int) (485 * mViewYScale), 0, 0);
        callIv.setLayoutParams(params);
    }

    /**
     * 设置视频源宽高
     *
     * @param width
     * @param height
     */
    public void setWH(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d(TAG, "onLayout changed = " + changed);
        if (changed) {
            resizeView();
            initData();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewXScale = w * 1.0f / STANDARD_VIEW_WIDTH;
        mViewYScale = h * 1.0f / STANDARD_VIEW_HEIGHT;
        mVideoXScale = w * 1.0f / mVideoWidth;
        mVideoYScale = h * 1.0f / mVideoHeight;
        Log.d(TAG, "onSizeChanged: w = " + w + ", h = " + h + ", mViewXScale = " + mViewXScale + ", mViewYScale=" +
                mViewYScale + ", S_WIDTH = " + STANDARD_VIEW_WIDTH + ", S_HEIGHT = " + STANDARD_VIEW_HEIGHT);
    }

    /**
     * 处理数据
     *
     * @param out 数据
     */
    public void process(DMSSDKOutputOuterClass.DMSSDKOutput out) {
        if (getVisibility() == VISIBLE) {
            cook(out);
            coverView.postInvalidate();
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
                return "OPEN";
            case DMSSDKOutputOuterClass.EyeStateEnum.EYE_ST_CLOSE_VALUE:
                return "CLOSE";
            case DMSSDKOutputOuterClass.EyeStateEnum.EYE_ST_UNKNOW_VALUE:
                return "UNKNOW";
            case DMSSDKOutputOuterClass.EyeStateEnum.EYE_ST_BLOCK_VALUE:
                return "BLOCK";
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
                return "FRONT";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_LEFT_VALUE:
                return "LEFT";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_RIGHT_VALUE:
                return "RIGHT";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_UP_VALUE:
                return "UP";
            case DMSSDKOutputOuterClass.FaceDirEnum.FACE_DIR_DOWN_VALUE:
                return "DOWN";
            default:
                return "";
        }
    }

    /**
     * 处理事件
     *
     * @param out 数据
     */
    private void cook(DMSSDKOutputOuterClass.DMSSDKOutput out) {
        if (out != null) {
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

            // 眼睛状态
            if (eyeStatusTexts.size() > 1) {
                DMSSDKOutputOuterClass.EyeStateResult eyeStateResult = out.getEyeResult();
                // 左眼状态
                int leftEyeState = eyeStateResult.getLeft().getNumber();
                eyeStatusTexts.get(1).text = getEyeStatus(leftEyeState);
                // 右眼状态
                int rightEyeState = eyeStateResult.getRight().getNumber();
                eyeStatusTexts.get(0).text = getEyeStatus(rightEyeState);
            }

            // 人脸朝向
            if (facingDirTexts.size() > 0) {
                DMSSDKOutputOuterClass.FaceDirectionResult faceDirectionResult = out.getFaceDirectionResult();
                List<DMSSDKOutputOuterClass.FaceDirEnum> faceDirEnumList = faceDirectionResult
                        .getFaceDirList();
                if (!faceDirEnumList.isEmpty()) {
                    int faceDir = faceDirEnumList.get(0).getNumber();
                    facingDirTexts.get(0).text = getFaceDir(faceDir);
                }
            }

            // 驾驶状态
            for (int i = 0; i < driverStatusTexts.size(); i++) {
                driverStatusTexts.get(i).color = COLOR_WHITE;
            }
            if (driverStatusTexts.size() > 3) {
                List<DMSSDKOutputOuterClass.StateResult> stateResults = out.getStateResultList();
                for (DMSSDKOutputOuterClass.StateResult stateResult : stateResults) {
                    DMSSDKOutputOuterClass.StateEnum stateEnum = stateResult.getState();
                    switch (stateEnum) {
                        case STATE_NONE:
                            break;
                        case STATE_DDW:
                            driverStatusTexts.get(POS_DDW).color = COLOR_ORANGE;
                            break;
                        case STATE_DFW:
                            driverStatusTexts.get(POS_DFW).color = COLOR_ORANGE;
                            break;
                        case STATE_DYA:
                            driverStatusTexts.get(POS_DYA).color = COLOR_ORANGE;
                            break;
                        case STATE_DCA:
                            driverStatusTexts.get(POS_DCA).color = COLOR_ORANGE;
                            isCall = true;
                            break;
                        case STATE_DSA:
                            driverStatusTexts.get(POS_DSA).color = COLOR_ORANGE;
                            isSmoke = true;
                            break;
                        case STATE_LDR:
                            break;
                        case STATE_DAA:
                            driverStatusTexts.get(POS_DAA).color = COLOR_ORANGE;
                            break;
                        case STATE_CALIB:
                            break;
                        case STATE_DEBA:
                            driverStatusTexts.get(POS_DEBA).color = COLOR_ORANGE;
                            break;
                        case STATE_DSBA:
                            driverStatusTexts.get(POS_DSBA).color = COLOR_ORANGE;
                            break;
                        case STATE_DDRA:
                            driverStatusTexts.get(POS_DDRA).color = COLOR_ORANGE;
                            break;
                        case STATE_SIZE:
                            break;
                    }
                }
            }

            // 事件状态
            if (evtStatusTexts.size() > 4) {
                for (int i = 0; i < evtStatusTexts.size(); i++) {
                    evtStatusTexts.get(i).color = COLOR_WHITE;
                }
                List<DMSSDKOutputOuterClass.EventResult> eventResults = out.getEventResultList();
                for (DMSSDKOutputOuterClass.EventResult eventResult : eventResults) {
                    DMSSDKOutputOuterClass.EventEnum eventEnum = eventResult.getEvent();
                    switch (eventEnum) {
                        case EVENT_NONE:
                            break;
                        case EVENT_DDW_L:
                        case EVENT_DDW_R:
                        case EVENT_DDW_U:
                        case EVENT_DDW_D:
                            evtStatusTexts.get(POS_DDW).color = COLOR_RED;
                            break;
                        case EVENT_DFW:
                            evtStatusTexts.get(POS_DFW).color = COLOR_RED;
                            break;
                        case EVENT_DYA:
                            evtStatusTexts.get(POS_DYA).color = COLOR_RED;
                            break;
                        case EVENT_DCA:
                            evtStatusTexts.get(POS_DCA).color = COLOR_RED;
                            isCall = true;
                            break;
                        case EVENT_DSA:
                            evtStatusTexts.get(POS_DSA).color = COLOR_RED;
                            isSmoke = true;
                            break;
                        case EVENT_LDR:
                            break;
                        case EVENT_DAA:
                            evtStatusTexts.get(POS_DAA).color = COLOR_RED;
                            break;
                        case EVENT_CALIB:
                            break;
                        case EVENT_DEBA:
                            evtStatusTexts.get(POS_DEBA).color = COLOR_RED;
                            break;
                        case EVENT_DSBA:
                            evtStatusTexts.get(POS_DSBA).color = COLOR_RED;
                            break;
                        case EVENT_DDRA:
                            evtStatusTexts.get(POS_DDRA).color = COLOR_RED;
                            break;
                        case EVENT_SIZE:
                            break;
                    }
                }
            }

            // 人脸特征点
            DMSSDKOutputOuterClass.FaceFeaturePoint faceFeaturePoint = out.getFaceFeatureResult();
            List<DMSSDKOutputOuterClass.Point> points = faceFeaturePoint.getFeaturePointList();
            synchronized (mTempFeaturePoints) {
                mTempFeaturePoints.clear();
                for (int i = 0; i < points.size(); i++) {
                    float x = points.get(i).getX() * mVideoXScale;
                    float y = points.get(i).getY() * mVideoYScale;
                    mTempFeaturePoints.add(Shape.circle(x, y, 3 * mViewXScale, COLOR_BLUE));
                }
            }

            // 特征点转绘制Path
            synchronized (mLdmkPath) {
                ldmk2path(mLdmkPath, mTempFeaturePoints);
            }

            // warning ico
            this.removeCallbacks(mRunnable);
            this.post(mRunnable);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            requestLayout();
        }
    }

    private class CoverView extends View {
        private static final String TAG = "DmsMaskerView" + "_CoverView";
        //        private final Object sync = new Object();
        private Paint paint;
        private PaintFlagsDrawFilter flagsDrawFilter;

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
            flagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }

        @Override
        protected void onDraw(Canvas canvas) {
//            Log.d(TAG, "onDraw: ");
            super.onDraw(canvas);
            canvas.save();
            canvas.setDrawFilter(flagsDrawFilter);
            // 画人脸框
            paint.reset();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(COLOR_BLUE);
            paint.setStrokeWidth(2);
            for (Shape.Rect rect : roiRecs) {
                canvas.drawRect(rect.rectf(), paint);
            }

            // 画人脸框显示的字
            paint.reset();
            paint.setTypeface(Typeface.MONOSPACE);
            for (Shape.Text text : roiTexts) {
                paint.setColor(text.color);
                paint.setTextSize(text.textSize);
                canvas.drawText(text.text, text.x, text.y, paint);
            }

            // 画眼睛状态
            paint.reset();
            paint.setTypeface(Typeface.DEFAULT);
            for (Shape.Text text : eyeStatusTexts) {
                paint.setColor(text.color);
                paint.setTextSize(text.textSize);
                canvas.drawText(text.text, text.x, text.y, paint);
            }

            // 画人脸朝向状态
            paint.reset();
            paint.setTypeface(Typeface.MONOSPACE);
            for (Shape.Text text : facingDirTexts) {
                paint.setColor(text.color);
                paint.setTextSize(text.textSize);
                canvas.drawText(text.text, text.x, text.y, paint);
            }

            // 画驾驶员状态
            paint.reset();
            paint.setTypeface(Typeface.MONOSPACE);
            for (Shape.Text text : driverStatusTexts) {
                paint.setColor(text.color);
                paint.setTextSize(text.textSize);
                canvas.drawText(text.text, text.x, text.y, paint);
            }

            // 画其他文字
            paint.reset();
            for (Shape.Text text : otherTexts) {
                paint.setColor(text.color);
                canvas.drawText(text.text, text.x, text.y, paint);
            }


            // 画特征点
            featurePoints.clear();
            synchronized (mTempFeaturePoints) {
                featurePoints.addAll(mTempFeaturePoints);
            }
            paint.reset();
            paint.setColor(COLOR_BLUE);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(2);
            for (Shape.Circle point : featurePoints) {
                canvas.drawCircle(point.x, point.y, point.r, paint);
            }

            // 画特征点连线
            paint.setStyle(Paint.Style.STROKE);
            synchronized (mLdmkPath) {
                canvas.drawPath(mLdmkPath, paint);
            }

            // 画用户的名字
            if (userText != null && !TextUtils.isEmpty(userText.text)) {
                paint.reset();
                paint.setColor(userText.color);
                paint.setTextSize(userText.textSize);
                paint.setStrokeWidth(0);
                paint.setTypeface(Typeface.SANS_SERIF);
                canvas.drawText(userText.text, userText.x, userText.y, paint);
            }

            // 画事件
            paint.reset();
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            for (Shape.Text text : evtStatusTexts) {
                paint.setColor(text.color);
                paint.setTextSize(text.textSize);
                canvas.drawText(text.text, text.x, text.y, paint);
            }
            canvas.restore();
        }

    }

    /**
     * 特征点转成Path
     */
    private void ldmk2path(Path path, List<Shape.Circle> featurePoints) {
        path.reset();
        if (featurePoints.isEmpty()) {
            return;
        }

        // LDMK 21
        if (featurePoints.size() == 21) {
            // 左眼
            path.moveTo(featurePoints.get(0).x, featurePoints.get(0).y);
            path.lineTo(featurePoints.get(1).x, featurePoints.get(1).y);
            path.lineTo(featurePoints.get(2).x, featurePoints.get(2).y);

            // 右眼
            path.moveTo(featurePoints.get(3).x, featurePoints.get(3).y);
            path.lineTo(featurePoints.get(4).x, featurePoints.get(4).y);
            path.lineTo(featurePoints.get(5).x, featurePoints.get(5).y);

            // 鼻子
            path.moveTo(featurePoints.get(6).x, featurePoints.get(6).y);
            path.lineTo(featurePoints.get(7).x, featurePoints.get(7).y);
            path.lineTo(featurePoints.get(8).x, featurePoints.get(8).y);
            path.lineTo(featurePoints.get(12).x, featurePoints.get(12).y);
            path.lineTo(featurePoints.get(13).x, featurePoints.get(13).y);
            path.lineTo(featurePoints.get(14).x, featurePoints.get(14).y);
            path.lineTo(featurePoints.get(9).x, featurePoints.get(9).y);
            path.lineTo(featurePoints.get(10).x, featurePoints.get(10).y);
            path.lineTo(featurePoints.get(11).x, featurePoints.get(11).y);

            // 鼻子底端
            path.moveTo(featurePoints.get(12).x, featurePoints.get(12).y);
            path.lineTo(featurePoints.get(14).x, featurePoints.get(14).y);

            // 嘴巴
            path.moveTo(featurePoints.get(15).x, featurePoints.get(15).y);
            path.lineTo(featurePoints.get(16).x, featurePoints.get(16).y);
            path.lineTo(featurePoints.get(20).x, featurePoints.get(20).y);

            path.moveTo(featurePoints.get(15).x, featurePoints.get(15).y);
            path.lineTo(featurePoints.get(17).x, featurePoints.get(17).y);
            path.lineTo(featurePoints.get(20).x, featurePoints.get(20).y);

            path.moveTo(featurePoints.get(15).x, featurePoints.get(15).y);
            path.lineTo(featurePoints.get(18).x, featurePoints.get(18).y);
            path.lineTo(featurePoints.get(20).x, featurePoints.get(20).y);

            path.moveTo(featurePoints.get(15).x, featurePoints.get(15).y);
            path.lineTo(featurePoints.get(19).x, featurePoints.get(19).y);
            path.lineTo(featurePoints.get(20).x, featurePoints.get(20).y);
        }

        // LDMK 28
        if (featurePoints.size() == 28 || featurePoints.size() == 25) {
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
        }
    }
}
