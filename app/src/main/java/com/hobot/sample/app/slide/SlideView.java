package com.hobot.sample.app.slide;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hobot.sample.app.R;
import com.hobot.sample.app.config.Constants;
import com.hobot.sample.app.config.DefaultConfig;
import com.hobot.sample.app.slide.view.EventViewPagerAdapter;
import com.hobot.sample.app.slide.view.base.BaseView;
import com.hobot.sample.app.slide.view.pager.CommonSwitchView;
import com.hobot.sample.app.slide.view.pager.EventSelectorView;
import com.hobot.sample.app.slide.view.pager.EventWarnView;
import com.hobot.sample.app.slide.view.pager.SoundIntervalView;
import com.hobot.sample.app.slide.view.pager.SoundSwitchView;
import com.hobot.sample.app.slide.view.pager.SpeedSettingView;
import com.hobot.sample.app.slide.view.pager.ThresholdSettingView;
import com.hobot.sdk.library.utils.ScreenUtils;
import com.hobot.sdk.library.utils.SharePrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * DEBUG界面的弹窗
 *
 * @author Hobot
 */
public class SlideView extends PopupWindow implements View.OnClickListener, ViewPager.OnPageChangeListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = SlideView.class.getSimpleName();
    private final SharePrefs mSharePrefs;
    private Context mContext;
    private ViewPager mOtherSettingPager;
    private List<BaseView> mFragments = new ArrayList<>();
    private View mView;
    private Stack<Integer> mCurrentPageItem = new Stack<>();
    private TextView mPageItemText;
    private TextView mTitleView;
    private String[] mPageTitleArray;
    private EventViewPagerAdapter mPagerAdapter;
    private View mLogView;

    public SlideView(Context context) {
        mContext = context;
        mPageTitleArray = mContext.getResources().getStringArray(R.array.slide_setting_title);
        initView();
        initPopView(mView);
        mSharePrefs = new SharePrefs(mContext);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.view_slide_layout, null, false);
        mOtherSettingPager = (ViewPager) mView.findViewById(R.id.view_page_event_config);
        mOtherSettingPager.addOnPageChangeListener(this);
        mOtherSettingPager.setCurrentItem(0, true);
        mOtherSettingPager.setOffscreenPageLimit(0);
        mPageItemText = (TextView) mView.findViewById(R.id.text_page_item);
        mPageItemText.setText(String.format(mContext.getString(R.string.page_item), 1));
        mTitleView = (TextView) mView.findViewById(R.id.page_title_view);
        mTitleView.setText(mPageTitleArray[0]);
        mView.findViewById(R.id.left_view).setOnClickListener(this);
        mView.findViewById(R.id.right_view).setOnClickListener(this);
        mView.findViewById(R.id.language_select_btn).setOnClickListener(this);
        mView.findViewById(R.id.wifi_btn).setOnClickListener(this);
        mView.findViewById(R.id.wireless_btn).setOnClickListener(this);
        mView.findViewById(R.id.settings_btn).setOnClickListener(this);
        mView.findViewById(R.id.sound_btn).setOnClickListener(this);
        mView.findViewById(R.id.exit_btn).setOnClickListener(this);
        mView.findViewById(R.id.log_btn).setOnClickListener(this);

        CommonSwitchView commonView = new CommonSwitchView(mContext);
        EventSelectorView selectorView = new EventSelectorView(mContext);
        SoundIntervalView soundIntervalView = new SoundIntervalView(mContext);
        SoundSwitchView soundSwitchView = new SoundSwitchView(mContext);
        SpeedSettingView speedSettingView = new SpeedSettingView(mContext);
        ThresholdSettingView thresholdSettingView = new ThresholdSettingView(mContext);
        EventWarnView eventWarnView = new EventWarnView(mContext);
        mFragments.add(commonView);
        mFragments.add(selectorView);
        mFragments.add(soundIntervalView);
        mFragments.add(soundSwitchView);
        mFragments.add(speedSettingView);
        mFragments.add(thresholdSettingView);
        mFragments.add(eventWarnView);
        // 初始化Adapter
        mPagerAdapter = new EventViewPagerAdapter(mFragments);
        mOtherSettingPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
    }

    /**
     * 设置当前窗口
     */
    private void initPopView(View view) {
        setContentView(view);
        setWidth(ScreenUtils.getScreenWidth(mContext) * 3 / 5);
        setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        setBackgroundDrawable(dw);
        setAnimationStyle(R.style.SlidePop);
        setOutsideTouchable(true);
    }

    /**
     * 显示界面
     *
     * @param view
     */
    public void show(View view) {
        initData();
        showAtLocation(view, Gravity.START, 0, 0);
    }

    /**
     * 释放界面
     */
    public void release() {
        if (isShowing()) {
            dismiss();
        }
        for (BaseView baseView : mFragments) {
            baseView.release();
        }
        mFragments.clear();
        mPagerAdapter.notifyDataSetChanged();
        mPagerAdapter.release();
        mOtherSettingPager.removeOnPageChangeListener(this);
        mCurrentPageItem.clear();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        for (BaseView baseView : mFragments) {
            if (baseView.isViewCreated()) {
                baseView.initData();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPageItem.clear();
        mCurrentPageItem.push(position + 1);
        mPageItemText.setText(String.format(mContext.getString(R.string.page_item), (position + 1)));
        mTitleView.setText(mPageTitleArray[position]);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        int currentPage = mCurrentPageItem.peek();
        switch (v.getId()) {
            case R.id.left_view: {
                if (currentPage == 1) {
                    return;
                }
                currentPage--;
                mCurrentPageItem.clear();
                mCurrentPageItem.push(currentPage);
                break;
            }
            case R.id.right_view: {
                if (currentPage == mFragments.size()) {
                    return;
                }
                currentPage++;
                mCurrentPageItem.clear();
                mCurrentPageItem.push(currentPage);
                break;
            }
            case R.id.language_select_btn: {
                mContext.startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));
                break;
            }
            case R.id.wifi_btn: {
                mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
            }
            case R.id.wireless_btn: {
                mContext.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                break;
            }
            case R.id.sound_btn: {
                mContext.startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
                break;
            }
            case R.id.settings_btn: {
                mContext.startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
            }
            case R.id.exit_btn: {
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            }
            case R.id.log_btn:
                dismiss();
                showLogDialog(mContext);
                break;
            default: {
                break;
            }
        }
        mOtherSettingPager.setCurrentItem(mCurrentPageItem.peek() - 1);
        mPageItemText.setText(String.format(mContext.getString(R.string.page_item), (mCurrentPageItem.peek())));
        if (mCurrentPageItem.peek() > mPageTitleArray.length) {
            mTitleView.setText(R.string.unknow_page);
        } else {
            mTitleView.setText(mPageTitleArray[mCurrentPageItem.peek() - 1]);
        }
    }

    /**
     * 显示LogDialog
     *
     * @param context 上下文
     */
    private void showLogDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        mLogView = LayoutInflater.from(mContext).inflate(R.layout.view_log, null);

        builder.setView(mLogView);
        builder.show();
        ((CheckBox) mLogView.findViewById(R.id.box_print)).setChecked(DefaultConfig.SUPPORT_LOG_PRINT);
        ((CheckBox) mLogView.findViewById(R.id.box_print)).setOnCheckedChangeListener(this);

        ((CheckBox) mLogView.findViewById(R.id.box_tofile)).setChecked(DefaultConfig.SUPPORT_LOG_FILE);
        ((CheckBox) mLogView.findViewById(R.id.box_tofile)).setOnCheckedChangeListener(this);

        ((CheckBox) mLogView.findViewById(R.id.box_tonet)).setChecked(DefaultConfig.SUPPORT_LOG_OSS);
        ((CheckBox) mLogView.findViewById(R.id.box_tonet)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.box_print:
                DefaultConfig.SUPPORT_LOG_PRINT = isChecked;
                mSharePrefs.putCommit(Constants.KEY_LOG_PRINT_SWITCH, isChecked);
                break;
            case R.id.box_tofile:
                DefaultConfig.SUPPORT_LOG_FILE = isChecked;
                mSharePrefs.putCommit(Constants.KEY_LOG_FILE_SWITCH, isChecked);
                break;
            case R.id.box_tonet:
                DefaultConfig.SUPPORT_LOG_OSS = isChecked;
                mSharePrefs.putCommit(Constants.KEY_LOG_OSS_SWITCH, isChecked);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    }
}
