package com.hobot.sample.app.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hobot.sample.app.R;
import com.hobot.sample.app.fragment.MainSettingFragment;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        FragmentManager fragmentManager = getFragmentManager();
        MainSettingFragment mainSettingFragment = new MainSettingFragment();
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            mainSettingFragment.setArguments(bundle);
        }
        fragmentManager.beginTransaction().add(R.id.settings_container, mainSettingFragment).commitAllowingStateLoss();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
