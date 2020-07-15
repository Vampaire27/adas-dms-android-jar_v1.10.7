package com.hobot.sample.app.slide.view.pager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.sample.app.R;
import com.hobot.sample.app.slide.view.base.BaseView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hobot.nebula.common.module.base.WarningEventType.*;

/**
 * 报警音间隔设置界面
 * 设置已定义在event_config.json中可报警事件的报警间隔
 */
public class SoundIntervalView extends BaseView {
    private static final String TAG = "SoundIntervalView";
    private static final String[] SOUND_TYPES = new String[]{
            // ADAS
            TYPE_PCW, TYPE_FCW, TYPE_HMW, TYPE_LDW_LEFT, TYPE_LDW_RIGHT, TYPE_FVSA,
            // DMS
            TYPE_DAA, TYPE_DOA, TYPE_DSWA, TYPE_DEBA,
            TYPE_DDW_LEFT, TYPE_DDW_RIGHT, TYPE_DDW_UP, TYPE_DDW_DOWN,
            TYPE_DFW, TYPE_DYA, TYPE_DCA, TYPE_DSA, TYPE_DSBA, TYPE_DUA, TYPE_DDRA,
    };

    private WarningTypeRecyclerViewAdapter mRecyclerViewAdapter;

    public SoundIntervalView(Context context) {
        super(context);
    }

    @Override
    public String TAG() {
        return TAG;
    }

    @Override
    public int layoutId() {
        return R.layout.view_sound_interval_layout;
    }

    @Override
    public void initView(View view) {
        RecyclerView warnRecyclerView = (RecyclerView) view.findViewById(R.id.warning_interval);
        List<String> warningList = new ArrayList<>(Arrays.asList(SOUND_TYPES));
        mRecyclerViewAdapter = new WarningTypeRecyclerViewAdapter(warningList);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(OrientationHelper.VERTICAL);
        warnRecyclerView.setLayoutManager(manager);
        warnRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    public void initData() {
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    private class WarningTypeRecyclerViewAdapter extends RecyclerView.Adapter<WarningViewHolder> implements
            RadioGroup.OnCheckedChangeListener {
        List<String> mWarnTypes;

        public WarningTypeRecyclerViewAdapter(List<String> warningTypes) {
            super();
            mWarnTypes = warningTypes;
        }

        @Override
        public WarningViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_unit_sound_interval_layout, parent, false);
            return new WarningViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WarningViewHolder holder, int position) {
            holder.mTitleView.setText(String.format(mContext.getString(R.string.event_interval), mWarnTypes.get
                    (position)));
            holder.mWarningRadioGroup.setTag(mWarnTypes.get(position));
            holder.mWarningRadioGroup.setOnCheckedChangeListener(null);
            int interval = (int) (HobotWarningSDK.getInstance().getSoundInterval(mWarnTypes.get(position)) / 1000);
            switch (interval) {
                case 0: {
                    holder.mInterval0.setChecked(true);
                    break;
                }
                case 2: {
                    holder.mInterval2.setChecked(true);
                    break;
                }
                case 4: {
                    holder.mInterval4.setChecked(true);
                    break;
                }
                case 6: {
                    holder.mInterval6.setChecked(true);
                    break;
                }
                default: {
                    holder.mIntervalDefault.setChecked(true);
                    break;
                }

            }
            holder.mWarningRadioGroup.setOnCheckedChangeListener(this);
        }

        @Override
        public int getItemCount() {
            return mWarnTypes.size();
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            String tag = group.getTag().toString();
            switch (checkedId) {
                case R.id.interval_default: {
                    HobotWarningSDK.getInstance().setSoundInterval(tag, -1);
                    break;
                }
                case R.id.interval_0: {
                    HobotWarningSDK.getInstance().setSoundInterval(tag, 0 * 1000);
                    break;
                }
                case R.id.interval_2: {
                    HobotWarningSDK.getInstance().setSoundInterval(tag, 2 * 1000);
                    break;
                }
                case R.id.interval_4: {
                    HobotWarningSDK.getInstance().setSoundInterval(tag, 4 * 1000);
                    break;
                }
                case R.id.interval_6: {
                    HobotWarningSDK.getInstance().setSoundInterval(tag, 6 * 1000);
                    break;
                }
            }
        }
    }

    private class WarningViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleView;
        private RadioGroup mWarningRadioGroup;
        private RadioButton mIntervalDefault;
        private RadioButton mInterval0;
        private RadioButton mInterval2;
        private RadioButton mInterval4;
        private RadioButton mInterval6;

        public WarningViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.warning_type);
            mWarningRadioGroup = (RadioGroup) itemView.findViewById(R.id.sound_interval);
            mIntervalDefault = (RadioButton) itemView.findViewById(R.id.interval_default);
            mInterval0 = (RadioButton) itemView.findViewById(R.id.interval_0);
            mInterval2 = (RadioButton) itemView.findViewById(R.id.interval_2);
            mInterval4 = (RadioButton) itemView.findViewById(R.id.interval_4);
            mInterval6 = (RadioButton) itemView.findViewById(R.id.interval_6);
        }
    }
}
