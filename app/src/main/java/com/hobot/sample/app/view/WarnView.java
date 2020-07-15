package com.hobot.sample.app.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hobot.nebula.common.unit.EventCell;

public class WarnView extends ScrollView implements View.OnTouchListener {
    private static final String TAG = "WarnView";

    private Context mContext;
    private LinearLayout mLinearLayout;
    private int mParentWidth;
    private int mParentHeight;
    private int mPreX;
    private int mPreY;
    private float mOriginalX = 0;
    private float mOriginalY = 0;

    public WarnView(Context context) {
        super(context);
        initView(context);
    }

    public WarnView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public WarnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        Log.d(TAG, "initView");
        mContext = context;
        setVerticalScrollBarEnabled(false);
        mLinearLayout = new LinearLayout(context);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mLinearLayout.setLayoutParams(params);
        addView(mLinearLayout, params);
        setOnTouchListener(this);
    }

    private void createTextView(String warning) {
        Log.d(TAG, "create textView: " + warning);
        TextView textView = new TextView(mContext);
        textView.setTextSize(10);
        //d4237a
        textView.setTextColor(Color.rgb(0xd4, 0x23, 0x7a));
        textView.setText(warning);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 0, 0, 10);
        mLinearLayout.addView(textView);
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo(0, mLinearLayout.getMeasuredHeight() - getHeight());
            }
        });
    }

    public void updateEvent(EventCell eventCell) {
        Log.d(TAG, "update event: " + eventCell.getEventType());
        createTextView(eventCell.getEventType());
        //postInvalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mOriginalX == 0 || mOriginalY == 0) {
            mOriginalX = v.getX();
            mOriginalY = v.getY();
        }
        int nowX = (int) event.getRawX();
        int nowY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                mPreX = nowX;
                mPreY = nowY;
                ViewGroup parent;
                if (getParent() != null) {
                    parent = (ViewGroup) getParent();
                    mParentWidth = parent.getWidth();
                    mParentHeight = parent.getHeight();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                int dX = nowX - mPreX;
                int dY = nowY - mPreY;
                int distance = (int) Math.sqrt(dX * dX + dY * dY);
                if (distance == 0) {
                    break;
                }

                float x = getX() + dX;
                float y = getY() + dY; //检测是否到达边缘 左上右下
                x = x < 0 ? 0 : x > mParentWidth - getWidth() ? mParentWidth - getWidth() : x;
                y = getY() < 0 ? 0 : getY() + getHeight() > mParentHeight ? mParentHeight - getHeight() : y;
                setX(x);
                setY(y);
                mPreX = nowX;
                mPreY = nowY;
                if (y == 0) {
                    setVisibility(GONE);
                }
                break;

            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        switch (visibility) {
            case VISIBLE:
                setX(mOriginalX);
                setY(mOriginalY);
                post(new Runnable() {
                    @Override
                    public void run() {
                        scrollTo(0, mLinearLayout.getMeasuredHeight() - getHeight());
                    }
                });
                break;
        }
    }
}
