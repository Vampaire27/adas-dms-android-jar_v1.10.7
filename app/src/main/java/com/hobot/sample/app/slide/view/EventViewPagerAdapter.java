package com.hobot.sample.app.slide.view;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.hobot.sample.app.slide.view.base.BaseView;

import java.util.ArrayList;
import java.util.List;

public class EventViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "EventViewPagerAdapter";

    private List<BaseView> mViews;
    private List<View> mLayoutViews = new ArrayList<>();

    public EventViewPagerAdapter(List<BaseView> views) {
        super();
        mViews = views;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, mViews.size() + "-- position: " + position);
        View view = mViews.get(position).getView();
        if (view == null) {
            view = mViews.get(position).onCreateView();
        }
        if (!mLayoutViews.contains(view)) {
            mLayoutViews.add(view);
        }
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "destroyItem: " + position);
        View view = mLayoutViews.get(position);
        container.removeView(view);
    }

    public void release() {
        mLayoutViews.clear();
    }
}
