package com.hobot.sample.app.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hobot.sample.app.BuildConfig;
import com.hobot.sample.app.R;
import com.hobot.sample.app.module.base.ViewPageAdapter;
import com.hobot.sample.app.slide.SlideView;
import com.hobot.sample.app.view.DragFloatingButton;

/**
 * 默认Activity基类。
 * <p>
 * {@link BaseNewMainActivity}包含的地平线的基本功能，各个厂商需要实现{@link NewMainActivity} 实现各自功能。
 *
 * @author Hobot
 */
public abstract class BaseNewMainActivity extends BaseActivity implements View.OnClickListener {
    protected static final String TAG = "BaseNewMainActivity";
    private static final int EXIT_TIME_OUT = 2000;
    protected ViewPageAdapter mAdapter;
    private DragFloatingButton mFloatingBtn;
    private SlideView mSlideView;
    private long mLastClickTime = -1L;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_container);

        //是否使用自己开启相机功能


        mAdapter = getAdapter();
        mAdapter.onCreateView(this);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(mAdapter.getCount());
        viewPager.setAdapter(mAdapter);

        mFloatingBtn = (DragFloatingButton) findViewById(R.id.floating_slide_btn);
        mFloatingBtn.setOnClickListener(this);
        TextView versionTv = (TextView) findViewById(R.id.tv_version);
        versionTv.setText(String.format(getResources().getString(R.string.info_version_name),
                BuildConfig.VERSION_NAME));
        versionTv.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.onStop();
        if (mSlideView != null && mSlideView.isShowing()) {
            mSlideView.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.onDestroyView();
            mAdapter = null;
        }
        if (mSlideView != null) {
            mSlideView.release();
            mSlideView = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_slide_btn:
                if (mSlideView == null) {
                    mSlideView = new SlideView(this);
                }
                if (!mSlideView.isShowing()) {
                    mSlideView.show(mFloatingBtn);
                }
                break;
            case R.id.tv_version:
                break;
            default:
                break;
        }
    }


    /**
     * 获取对应的Adapter
     *
     * @return
     */
    public ViewPageAdapter getAdapter() {
        return new ViewPageAdapter();
    }

    /**
     * 显示错误提示
     *
     * @param title 标题
     * @param msg   内容
     */
    public void showErrorDialog(String title, String msg) {
        showDialog(title, msg, false,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Process.killProcess(Process.myPid());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mLastClickTime < EXIT_TIME_OUT) {
            super.onBackPressed();
        } else {
            mLastClickTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), R.string.toast_press_again_exit_program,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
