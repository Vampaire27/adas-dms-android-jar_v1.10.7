package com.hobot.sample.app.slide.view.pager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.sample.app.R;
import com.hobot.sample.app.slide.view.base.BaseView;

import static com.hobot.nebula.common.module.base.WarningEventType.*;

/**
 * 报警音开关设置
 * 控制已定义在event_config.json中的可报警事件的报警音开关。
 *
 * @author Hobot
 */
public class SoundSwitchView extends BaseView {
    private static final String TAG = "SoundSwitchView";
    private static final String[] SOUND_TYPES = new String[]{
            // ADAS
            TYPE_PCW, TYPE_FCW, TYPE_HMW, TYPE_LDW_LEFT, TYPE_LDW_RIGHT, TYPE_FVSA,
            // DMS
            TYPE_DAA, TYPE_DOA, TYPE_DSWA, TYPE_DEBA,
            TYPE_DDW_LEFT, TYPE_DDW_RIGHT, TYPE_DDW_UP, TYPE_DDW_DOWN,
            TYPE_DFW, TYPE_DYA, TYPE_DCA, TYPE_DSA, TYPE_DSBA, TYPE_DUA, TYPE_DDRA,
    };
    private SoundSwitchAdapter mAdapter;

    public SoundSwitchView(Context context) {
        super(context);
    }

    @Override
    public String TAG() {
        return TAG;
    }

    @Override
    public int layoutId() {
        return R.layout.view_warn_sound_switch_layout;
    }

    @Override
    public void initView(View view) {
        RecyclerView soundSwitchListView = (RecyclerView) view.findViewById(R.id.sound_switch_list);
        mAdapter = new SoundSwitchAdapter(SOUND_TYPES);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(OrientationHelper.VERTICAL);
        soundSwitchListView.setLayoutManager(manager);
        soundSwitchListView.setAdapter(mAdapter);
    }

    @Override
    public void initData() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class SoundSwitchAdapter extends RecyclerView.Adapter<SoundSwitchViewHolder>
            implements CompoundButton.OnCheckedChangeListener {
        private String[] eventTypes;

        public SoundSwitchAdapter(String[] eventTypes) {
            super();
            this.eventTypes = eventTypes;
        }

        @Override
        public SoundSwitchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_unit_sound_switch_layout, parent, false);
            return new SoundSwitchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SoundSwitchViewHolder holder, int position) {
            boolean isOpenSound = HobotWarningSDK.getInstance().getSoundSwitchByType(eventTypes[position]);
            Log.d(TAG, "onBindViewHolder: position = " + position + ", eventTypes[position] = " +
                    eventTypes[position] + ", isOpenSound = " + isOpenSound);
            String soundStatus = isOpenSound ? "opened" : "closed";
            holder.titleText.setText(String.format(mContext.getString(R.string.sound_switch), eventTypes[position],
                    soundStatus));
            holder.soundSwitch.setTag(eventTypes[position]);
            holder.soundSwitch.setOnCheckedChangeListener(null);
            holder.soundSwitch.setChecked(isOpenSound);
            holder.soundSwitch.setOnCheckedChangeListener(this);
        }

        @Override
        public void onViewRecycled(SoundSwitchViewHolder holder) {
            super.onViewRecycled(holder);
            holder.soundSwitch.setOnCheckedChangeListener(null);
        }

        @Override
        public int getItemCount() {
            return eventTypes.length;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String tag = buttonView.getTag().toString();
            HobotWarningSDK.getInstance().setSoundSwitchByType(tag, isChecked);
            notifyDataSetChanged();
        }
    }

    private class SoundSwitchViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final Switch soundSwitch;

        public SoundSwitchViewHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.sound_switch_title);
            soundSwitch = (Switch) itemView.findViewById(R.id.sound_switch);
        }
    }
}
