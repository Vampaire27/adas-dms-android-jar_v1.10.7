package com.hobot.sample.app.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.hobot.sample.app.BaseApplication;
import com.hobot.sample.app.R;
import com.hobot.sdk.library.tasks.FixedThreadPool;
import com.hobot.sdk.library.tasks.SingleTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动界面，用于权限申请和SDK的初始化。
 *
 * @author Hobot
 */
public class LauncherActivity extends BaseActivity {
    public static final String[] REQUEST_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.RECORD_AUDIO,
    };
    public static final int REQUEST_CODE = 1000;
    private static final String TAG = "LauncherActivity";
    private TextView mWelcomeText;
    private LottieAnimationView mInitSuccessView;
    private LottieAnimationView mWelcomeView;
    private LinearLayout mWelcomeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 重复启动判断，如果不是作为根task启动，直接结束
        if (!isTaskRoot()) {
            Log.w(TAG, "onCreate: is not task root!");
            finish();
            return;
        }
        setContentView(R.layout.activity_launcher);
        mWelcomeView = (LottieAnimationView) findViewById(R.id.welcome_animation_view);
        mInitSuccessView = (LottieAnimationView) findViewById(R.id.welcome_init_success_view);
        mWelcomeLayout = (LinearLayout) findViewById(R.id.layout_welcome);
        mWelcomeText = (TextView) findViewById(R.id.welcome_text);

        androidLevelCheck();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[]
                                                   grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        if (requestCode == REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    finish();
                    return;
                }
            }
            postInitSDK();
        }
    }

    /**
     * Android level检查
     */
    private void androidLevelCheck() {
        Log.d(TAG, "androidLevelCheck");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "need check");
            List<String> requestPermissions = new ArrayList<>();
            for (String permission : REQUEST_PERMISSIONS) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "need request: " + permission);
                    requestPermissions.add(permission);
                } else {
                    Log.d(TAG, "has permission: " + permission);
                }
            }
            if (requestPermissions.size() > 0) {
                Log.d(TAG, "requestPermissions");
                requestPermissions(requestPermissions.toArray(new String[]{}), REQUEST_CODE);
            } else {
                postInitSDK();
            }
        } else {
            postInitSDK();
        }
    }

    /**
     * 初始化SDK
     */
    private void postInitSDK() {
        // 已经获取到权限,显示Loading动画
        showAnimation();

        // 用线程池等待加载完成
        FixedThreadPool.get().execute(new SingleTask() {
            @Override
            protected void runTask() {
                // 等待初始化完成
                BaseApplication.waitLatch();
                // 配置加载完成,进入主界面
                cancelAnimator();
                Intent intent = new Intent(LauncherActivity.this, NewMainActivity.class);
                if (getIntent() != null && getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                startActivity(intent);
                finish();
            }

            @Override
            protected String TAG() {
                return TAG + "-WAIT";
            }
        });
    }

    /**
     * 显示初始化动画
     */
    private void showAnimation() {
        // 关闭欢迎动画，启动初始化动画
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWelcomeView.loop(false);
                mWelcomeView.cancelAnimation();
                mWelcomeLayout.setVisibility(View.INVISIBLE);
                mInitSuccessView.setVisibility(View.VISIBLE);
                mInitSuccessView.playAnimation();
                mWelcomeText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 取消初始化动画
     */
    private void cancelAnimator() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInitSuccessView.loop(false);
                mInitSuccessView.cancelAnimation();
            }
        });
    }
}
