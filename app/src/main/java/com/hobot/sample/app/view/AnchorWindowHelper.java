package com.hobot.sample.app.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.hobot.sample.app.R;

import java.lang.ref.WeakReference;

public class AnchorWindowHelper {
    private static final String TAG = "AnchorWindowHelper";
    private PopupWindow mPopWindow;
    private WeakReference<Context> mContext;
    private View mContentView;
    private WeakReference<View> mTargetView;
    private TextView mContentTv;
    private int xOffset, yOffset;

    private AnchorWindowHelper(Context context) {
        this.mContext = new WeakReference<>(context);
        mPopWindow = new PopupWindow(context);
        mPopWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(0));
        mContentView = LayoutInflater.from(mContext.get()).inflate(R.layout.content_tips, null, false);
        mContentTv = (TextView) mContentView.findViewById(R.id.tv_content);
        mPopWindow.setContentView(mContentView);
    }

    public static AnchorWindowHelper create(Context context) {
        return new AnchorWindowHelper(context);
    }

    public AnchorWindowHelper target(View view) {
        return target(view, 0, 0);
    }

    public AnchorWindowHelper target(View view, int xOffset, int yOffset) {
        mTargetView = new WeakReference<>(view);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        return this;
    }

    public void show(String text, boolean cancelable) {
        if (null == mTargetView.get()) {
            return;
        }
        View anchor = mTargetView.get();
        if (null == mContentTv) {
            return;
        }
        mContentTv.setText(text);
        mPopWindow.setOutsideTouchable(cancelable);
        mPopWindow.setFocusable(true);
        mPopWindow.showAsDropDown(anchor, xOffset, yOffset);
    }

    public void show(String text) {
        show(text, true);
    }


    public void dismiss() {
        if (null == mPopWindow) {
            return;
        }
        if (!mPopWindow.isShowing()) {
            return;
        }
        mPopWindow.dismiss();
    }

    public boolean isShowing() {
        return mPopWindow != null && mPopWindow.isShowing();
    }
}
