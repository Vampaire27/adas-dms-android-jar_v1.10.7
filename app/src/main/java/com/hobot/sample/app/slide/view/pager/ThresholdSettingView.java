package com.hobot.sample.app.slide.view.pager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.dms.sdk.HobotDmsSdk;
import com.hobot.quality.sdk.HobotQualitySDK;
import com.hobot.quality.sdk.config.ConfigConst;
import com.hobot.sample.app.R;
import com.hobot.sample.app.slide.DebugSettingField;
import com.hobot.sample.app.slide.view.base.BaseView;

/**
 * 修改报警阈值
 * Created by Ryan on 2019/10/28.
 */
public class ThresholdSettingView extends BaseView {

    private static final String TAG = "ThresholdSettingView";
    private static final String ADAS = "ADAS";
    private static final String DMS = "DMS";
    private static final String QUALITY = "QUALITY";
    private static final ThresholdTypeHolder[] THRESHOLD_TYPE_HOLDERS = new ThresholdTypeHolder[]{
            // ADAS
            new ThresholdTypeHolder(DebugSettingField.HMW_LEVEL_1,
                    com.hobot.adas.sdk.config.ConfigConst.TYPE_HMW_VALUE,
                    ADAS, 10, 0, ThresholdTypeHolder.FLOAT),
            new ThresholdTypeHolder(DebugSettingField.FCW_VALUE,
                    com.hobot.adas.sdk.config.ConfigConst.TYPE_FCW_VALUE,
                    ADAS, 10, 0, ThresholdTypeHolder.FLOAT),
            //QUALITY
            new ThresholdTypeHolder(DebugSettingField.LOWER_LIMIT,
                    ConfigConst.TYPE_QUALITY_LOWER_LIMIT,
                    QUALITY, 100, 0, ThresholdTypeHolder.INTERGER),
            new ThresholdTypeHolder(DebugSettingField.LOWER_LIMIT_RATIO,
                    ConfigConst.TYPE_QUALITY_LOWER_LIMIT_RATIO,
                    QUALITY, 1, 0, ThresholdTypeHolder.FLOAT),
            new ThresholdTypeHolder(DebugSettingField.UPPER_LIMIT,
                    ConfigConst.TYPE_QUALITY_UPPER_LIMIT,
                    QUALITY, 100, 0, ThresholdTypeHolder.INTERGER),
            new ThresholdTypeHolder(DebugSettingField.UPPER_LIMIT_RATIO,
                    ConfigConst.TYPE_QUALITY_UPPER_LIMIT_RATIO,
                    QUALITY, 1, 0, ThresholdTypeHolder.FLOAT)

    };
    private EventThresholdAdapter mAdapter;

    public ThresholdSettingView(Context context) {
        super(context);
    }

    @Override
    public int layoutId() {
        return R.layout.view_event_speed_layout;
    }

    @Override
    public void initView(View view) {
        RecyclerView speedSetListView = (RecyclerView) view.findViewById(R.id.speed_limit_list);
        mAdapter = new EventThresholdAdapter(THRESHOLD_TYPE_HOLDERS);
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

    @Override
    public String TAG() {
        return TAG;
    }

    private class EventThresholdAdapter extends RecyclerView.Adapter<EventThresholdViewHolder> {
        private ThresholdTypeHolder[] thresholdTypeHolders;

        public EventThresholdAdapter(ThresholdTypeHolder[] thresholdTypeHolders) {
            super();
            this.thresholdTypeHolders = thresholdTypeHolders;
        }

        @Override
        public EventThresholdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_unit_threshold_layout, parent, false);
            EventThresholdViewHolder holder;
            if (view.getTag() == null) {
                holder = new EventThresholdViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (EventThresholdViewHolder) view.getTag();
            }

            return holder;
        }


        @Override
        public int getItemCount() {
            return THRESHOLD_TYPE_HOLDERS.length;
        }

        @Override
        public void onBindViewHolder(final EventThresholdViewHolder holder, final int position) {
            holder.typeTV.setText(mContext.getString(R.string.threshold_value_description,
                    thresholdTypeHolders[position].thresholdType, thresholdTypeHolders[position].minValue,
                    thresholdTypeHolders[position].maxValue));
            holder.valueTV.setText(getConfigValue(thresholdTypeHolders[position].sdk,
                    thresholdTypeHolders[position].thresholdConfigKey));
            switch (thresholdTypeHolders[position].intputType) {
                case ThresholdTypeHolder.INTERGER:
                    holder.valueED.setInputType(InputType.TYPE_CLASS_NUMBER);
                    break;
                case ThresholdTypeHolder.FLOAT:
                    holder.valueED.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    break;
                default:
                    break;
            }
            holder.setButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (TextUtils.isEmpty(holder.valueED.getText().toString())) {
                            Toast.makeText(mContext, "请输入正确的值", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        float value = Float.parseFloat(holder.valueED.getText().toString());
                        if (value > thresholdTypeHolders[position].maxValue || value < thresholdTypeHolders[position].minValue) {
                            Toast.makeText(mContext, "请输入正确的值", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean isSetSuccess = false;
                        if (thresholdTypeHolders[position].intputType == ThresholdTypeHolder.INTERGER) {
                            isSetSuccess = setConfigValue(thresholdTypeHolders[position].sdk,
                                    thresholdTypeHolders[position].thresholdConfigKey, String.valueOf((int) value));
                        } else {
                            isSetSuccess = setConfigValue(thresholdTypeHolders[position].sdk,
                                    thresholdTypeHolders[position].thresholdConfigKey, String.valueOf(value));
                        }


                        if (isSetSuccess) {
                            holder.valueTV.setText(getConfigValue(thresholdTypeHolders[position].sdk,
                                    thresholdTypeHolders[position].thresholdConfigKey));
                            Toast.makeText(mContext, "设置成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "设置失败", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
    }

    private String getConfigValue(String type, int key) {
        String value = "0";
        switch (type) {
            case ADAS:
                value = HobotAdasSDK.getInstance().getAdasConfig(key);
                break;
            case DMS:
                value = HobotDmsSdk.getInstance().getDMSConfig(key);
                break;
            case QUALITY:
                value = HobotQualitySDK.getInstance().getConfig(key);
            default:
                break;
        }
        return value;
    }


    private boolean setConfigValue(String type, int key, String value) {
        boolean flag = false;
        switch (type) {
            case ADAS:
                flag = HobotAdasSDK.getInstance().setAdasConfig(key, String.valueOf(value));
                break;
            case DMS:
                flag = HobotDmsSdk.getInstance().setDmsConfig(key, String.valueOf(value));
                break;
            case QUALITY:
                flag = HobotQualitySDK.getInstance().setConfig(key, String.valueOf(value));

            default:
                break;
        }
        return flag;
    }


    private class EventThresholdViewHolder extends RecyclerView.ViewHolder {
        private final TextView typeTV;
        private final TextView valueTV;
        private final EditText valueED;
        private final Button setButton;

        public EventThresholdViewHolder(View itemView) {
            super(itemView);
            typeTV = (TextView) itemView.findViewById(R.id.tv_type);
            valueTV = (TextView) itemView.findViewById(R.id.text_value);
            valueED = (EditText) itemView.findViewById(R.id.ed_value);
            setButton = (Button) itemView.findViewById(R.id.btn_set);
        }
    }

    private static class ThresholdTypeHolder {
        public String thresholdType;
        public int thresholdConfigKey;
        public String sdk;
        public int maxValue;
        public int minValue;
        public int intputType;
        public final static int INTERGER = 0;
        public final static int FLOAT = 1;

        public ThresholdTypeHolder(String thresholdType, int thresholdConfigKey, String sdk, int maxValue, int minValue) {
            this.thresholdType = thresholdType;
            this.thresholdConfigKey = thresholdConfigKey;
            this.sdk = sdk;
            this.maxValue = maxValue;
            this.minValue = minValue;
        }

        public ThresholdTypeHolder(String thresholdType, int thresholdConfigKey, String sdk, int maxValue, int minValue,
                                   int intputType) {
            this.thresholdType = thresholdType;
            this.thresholdConfigKey = thresholdConfigKey;
            this.sdk = sdk;
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.intputType = intputType;
        }
    }
}
