package com.hobot.sample.app.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hobot.sample.app.R;
import com.hobot.sample.app.view.ScreenDensityUtils;

/**
 * 基本的Activity类，包含了一些Dialog和Toast的操作。
 *
 * @author Hobot
 */
public abstract class BaseActivity extends AppCompatActivity {
    private AlertDialog progressDialog;
    private AlertDialog errorDialog;
    private TextView progressContentTxt;
    private Toast toast;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            return;
        }
        int bits = 0;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (enableTransparentBar()) {
            // transparent status bar
            bits |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        // 常亮
        bits |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        winParams.flags |= bits;
        win.setAttributes(winParams);
    }

    /**
     * 是否开启全屏模式，默认为{@code true} 。
     *
     * @return 是否全屏
     */
    public boolean enableTransparentBar() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenDensityUtils.setOrientation(this, ScreenDensityUtils.HEIGHT);
    }

    @Override
    protected void onDestroy() {
        if (null != errorDialog && errorDialog.isShowing()) {
            errorDialog.dismiss();
            errorDialog = null;
        }
        if (toast != null) {
            toast.cancel();
        }
        super.onDestroy();
    }

    /**
     * 显示加载对话框。
     */
    public void showLoadingDialog() {
        showLoadingDialog(getString(R.string.data_loading));
    }

    /**
     * 显示加载对话框。
     *
     * @param message 消息
     */
    public void showLoadingDialog(String message) {
        showLoadingDialog(message, true);
    }

    /**
     * 显示加载对话框。
     *
     * @param message      消息
     * @param isCancelable 是否可以取消
     */
    public void showLoadingDialog(String message, boolean isCancelable) {
        try {
            // 1.判断是否该Dialog，没有就新建一个
            if (progressDialog == null) {
                final View progressView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null);
                progressContentTxt = (TextView) progressView.findViewById(R.id.common_progress_content);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                progressDialog = builder.create();
                progressDialog.setContentView(progressView);
                progressDialog.setView(progressView);
                progressDialog.setCanceledOnTouchOutside(false);
            }
            // 2.设置Dialog状态
            progressDialog.setCancelable(isCancelable);
            if (!TextUtils.isEmpty(message)) {
                progressContentTxt.setText(message);
            } else {
                progressContentTxt.setText(R.string.data_loading);
            }
            // 3.显示Dialog
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏对话框。
     */
    public void dismissLoadingDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示Toast。
     *
     * @param text 消息
     */
    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                }
                toast.setText(text);
                toast.show();
            }
        });
    }

    /**
     * 显示提示对话框。
     *
     * @param title    标题
     * @param msg      内容
     * @param cancel   是否能够主动取消
     * @param listener 监听
     */
    public synchronized void showDialog(String title, String msg, boolean cancel, DialogInterface.OnClickListener
            listener) {
        if (errorDialog == null) {
            errorDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
                    .create();
        }
        errorDialog.setCancelable(cancel);
        errorDialog.setTitle(title);
        errorDialog.setMessage(msg);
        errorDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), listener);
        errorDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (isFinishing()) {
            Log.w(BaseActivity.class.getSimpleName(), "showDialog: the activity is destroy!");
            return;
        }
        if (!errorDialog.isShowing()) {
            errorDialog.show();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                hideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断是否需要隐藏键盘。
     *
     * @param v  当前焦点
     * @param ev 输入事件
     * @return 判断是否需要隐藏
     */
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 隐藏输入法。
     *
     * @param token 句柄
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

}
