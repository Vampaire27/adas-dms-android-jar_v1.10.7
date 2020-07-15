package com.hobot.sample.app.slide.view.pager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.dms.sdk.config.ConfigConst;
import com.hobot.sample.app.R;
import com.hobot.sample.app.slide.DebugSettingField;
import com.hobot.sample.app.slide.view.base.BaseView;

import java.util.List;

/**
 * 事件触发速度设置
 * 设置ADAS/DMS事件的触发速度
 */
public class SpeedSettingView extends BaseView implements DebugSettingField {
    private static final String TAG = "SpeedSettingView";
    private static final SpeedTypeHolder[] SPEED_TYPES = new SpeedTypeHolder[]{
            // ADAS
            new SpeedTypeHolder(SPEED_PCW_MIN, com.hobot.adas.sdk.config.ConfigConst.TYPE_PCW_MIN_SPEED, "ADAS"),
            new SpeedTypeHolder(SPEED_PCW_MAX, com.hobot.adas.sdk.config.ConfigConst.TYPE_PCW_MAX_SPEED, "ADAS"),
            new SpeedTypeHolder(SPEED_FCW_MIN, com.hobot.adas.sdk.config.ConfigConst.TYPE_FCW_MIN_SPEED, "ADAS"),
            new SpeedTypeHolder(SPEED_FCW_MAX, com.hobot.adas.sdk.config.ConfigConst.TYPE_FCW_MAX_SPEED, "ADAS"),
            new SpeedTypeHolder(SPEED_HMW_MIN, com.hobot.adas.sdk.config.ConfigConst.TYPE_HMW_MIN_SPEED, "ADAS"),
            new SpeedTypeHolder(SPEED_LDW, com.hobot.adas.sdk.config.ConfigConst.TYPE_LDW_SPEED, "ADAS"),
            // DMS
            new SpeedTypeHolder(SPEED_DDW_MIN, ConfigConst.TYPE_DDW_LOWER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DDW_MAX, ConfigConst.TYPE_DDW_UPPER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DFW_MIN, ConfigConst.TYPE_DFW_LOWER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DFW_MAX, ConfigConst.TYPE_DFW_UPPER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DCA_MIN, ConfigConst.TYPE_DCA_LOWER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DCA_MAX, ConfigConst.TYPE_DCA_UPPER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DSA_MIN, ConfigConst.TYPE_DSA_LOWER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DSA_MAX, ConfigConst.TYPE_DSA_UPPER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DYA_MIN, ConfigConst.TYPE_DYA_LOWER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DYA_MAX, ConfigConst.TYPE_DYA_UPPER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DAA_MIN, ConfigConst.TYPE_DAA_LOWER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DAA_MAX, ConfigConst.TYPE_DAA_UPPER_SPEED, "DMS"),
            new SpeedTypeHolder(SPEED_DEBA_LIMIT, ConfigConst.TYPE_DEBA_LIMIT_SPEED, "DMS")
    };
    private EventSpeedAdapter mAdapter;

    public SpeedSettingView(Context context) {
        super(context);
    }

    @Override
    public String TAG() {
        return TAG;
    }

    @Override
    public int layoutId() {
        return R.layout.view_event_speed_layout;
    }

    @Override
    public void initView(View view) {
        RecyclerView speedSetListView = (RecyclerView) view.findViewById(R.id.speed_limit_list);
        mAdapter = new EventSpeedAdapter(SPEED_TYPES);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(OrientationHelper.VERTICAL);
        speedSetListView.setLayoutManager(manager);
        speedSetListView.setAdapter(mAdapter);
    }

    @Override
    public void initData() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class EventSpeedAdapter extends RecyclerView.Adapter<EventSpeedViewHolder> implements SeekBar.OnSeekBarChangeListener {
        private SpeedTypeHolder[] speedTypeHolders;

        public EventSpeedAdapter(SpeedTypeHolder[] speedTypeHolders) {
            super();
            this.speedTypeHolders = speedTypeHolders;
        }

        @Override
        public EventSpeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_unit_speed_limit_layout, parent, false);
            EventSpeedViewHolder holder;
            if (view.getTag() == null) {
                holder = new EventSpeedViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (EventSpeedViewHolder) view.getTag();
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(EventSpeedViewHolder holder, int position, List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                holder.speedValueText.setText(String.valueOf((float) payloads.get(0)));
            }
        }

        @Override
        public void onBindViewHolder(EventSpeedViewHolder holder, final int position) {
            String speedStr = "0";
            switch (speedTypeHolders[position].sdk) {
                case "ADAS":
                    speedStr = HobotAdasSDK.getInstance().getAdasConfig(speedTypeHolders[position].speedConfigKey);
                    break;
                case "DMS":
                    speedStr = HobotDmsSdk.getInstance().getDMSConfig(speedTypeHolders[position].speedConfigKey);
                    break;
                case "WHEEL":
//                    float speed = HobotWheelSDK.getInstance().getConfig(speedTypeHolders[position].speedConfigKey, 0.0f);
//                    speedStr = String.valueOf(speed);
                    break;
            }
            if (TextUtils.isEmpty(speedStr)) {
                Log.w(TAG, "onBindViewHolder: speed str is empty!");
                speedStr = String.valueOf(0.0f);
            }
            Log.d(TAG(), "title: " + speedTypeHolders[position].speedType + "----- speed: " + speedStr);
            holder.eventTypeText.setText(speedTypeHolders[position].speedType);
            float speed = Float.valueOf(speedStr);
            holder.speedValueText.setText(speedStr);
            holder.speedValueText.setTag(holder.speedSetBar);
            holder.speedSetBar.setTag(position);
            holder.speedSetBar.setProgress(Math.round(speed));
            holder.speedSetBar.setOnSeekBarChangeListener(this);
        }

        @Override
        public int getItemCount() {
            return speedTypeHolders.length;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            int tagPosition = Integer.valueOf(seekBar.getTag().toString());
            Log.d(TAG, "onStopTrackingTouch: " + progress + " -- tagPosition: " + tagPosition);
            SpeedTypeHolder holders = speedTypeHolders[tagPosition];
            notifyItemChanged(tagPosition, Float.valueOf(String.valueOf(progress)));
            switch (holders.sdk) {
                case "ADAS":
                    HobotAdasSDK.getInstance().setAdasConfig(holders.speedConfigKey, String.valueOf(progress));
                    break;
                case "DMS":
                    HobotDmsSdk.getInstance().setDmsConfig(holders.speedConfigKey, String.valueOf(progress));
                    break;
                case "WHEEL":
                    // nop
                    break;
            }
        }
    }

    private class EventSpeedViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventTypeText;
        private final TextView speedValueText;
        private final SeekBar speedSetBar;

        public EventSpeedViewHolder(View itemView) {
            super(itemView);
            eventTypeText = (TextView) itemView.findViewById(R.id.text_speed_type);
            speedValueText = (TextView) itemView.findViewById(R.id.text_speed_value);
            speedSetBar = (SeekBar) itemView.findViewById(R.id.speed_limit_set);
        }
    }

    private static class SpeedTypeHolder {
        public String speedType;
        public int speedConfigKey;
        public String sdk;

        public SpeedTypeHolder(String speedType, int speedConfigKey, String sdk) {
            this.speedType = speedType;
            this.speedConfigKey = speedConfigKey;
            this.sdk = sdk;
        }
    }
}
