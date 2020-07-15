package com.hobot.sample.app.slide.view.pager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hobot.nebula.common.HobotWarningSDK;
import com.hobot.nebula.common.model.EventTypeInfo;
import com.hobot.sample.app.R;
import com.hobot.sample.app.slide.view.base.BaseView;

import java.util.Arrays;
import java.util.List;

import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DAA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DCA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DDRA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DDW_DOWN;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DDW_LEFT;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DDW_RIGHT;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DDW_UP;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DEBA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DFW;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DOA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DSA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DSBA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DSWA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DUA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_DYA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_FCW;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_FVSA;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_HMW;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_INR_1;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_INR_2;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_INR_3;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_INR_4;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_LDW_LEFT;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_LDW_RIGHT;
import static com.hobot.nebula.common.module.base.WarningEventType.TYPE_PCW;

public class EventWarnView extends BaseView {
    private static final String TAG = EventWarnView.class.getSimpleName();
    private RecyclerView mAdasRecyclerView;
    private RecyclerView mDmsRecyclerView;
    private RecyclerView mOthersRecyclerView;

    private static final String[] ADAS_EVENT_TYPES = new String[] {
            // ADAS
            TYPE_PCW, TYPE_FCW, TYPE_HMW, TYPE_LDW_LEFT, TYPE_LDW_RIGHT, TYPE_FVSA
    };

    private static final String[] DMS_EVENT_TYPES = new String[] {
            // DMS
            TYPE_DAA, TYPE_DOA, TYPE_DSWA, TYPE_DEBA,
            TYPE_DDW_LEFT, TYPE_DDW_RIGHT, TYPE_DDW_UP, TYPE_DDW_DOWN,
            TYPE_DFW, TYPE_DYA, TYPE_DCA, TYPE_DSA, TYPE_DSBA, TYPE_DUA, TYPE_DDRA
    };

    private static final String[] OTHERS_EVENT_TYPES = new String[] {
            TYPE_INR_1, TYPE_INR_2, TYPE_INR_3, TYPE_INR_4
    };

    public EventWarnView(Context context) {
        super(context);
    }

    @Override
    public int layoutId() {
        return R.layout.view_event_warn_layout;
    }

    @Override
    public void initView(View view) {
        loadAdas(view);
        loadDms(view);
        loadOthers(view);
    }

    @Override
    public void initData() {

    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    public String TAG() {
        return TAG;
    }

    private void loadAdas(View view) {
        mAdasRecyclerView = (RecyclerView) view.findViewById(R.id.adas_recycler_view);
        mAdasRecyclerView.setVisibility(View.VISIBLE);
        CommonAdapter adasAdapter = new CommonAdapter(Arrays.asList(ADAS_EVENT_TYPES));
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(OrientationHelper.VERTICAL);
        mAdasRecyclerView.setLayoutManager(manager);
        mAdasRecyclerView.setAdapter(adasAdapter);
    }

    private void loadDms(View view) {
        mDmsRecyclerView = (RecyclerView) view.findViewById(R.id.dms_recycler_view);
        mDmsRecyclerView.setVisibility(View.VISIBLE);
        CommonAdapter dmsAdapter = new CommonAdapter(Arrays.asList(DMS_EVENT_TYPES));
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(OrientationHelper.VERTICAL);
        mDmsRecyclerView.setLayoutManager(manager);
        mDmsRecyclerView.setAdapter(dmsAdapter);
    }

    private void loadOthers(View view) {
        mOthersRecyclerView = (RecyclerView) view.findViewById(R.id.others_recycle_view);
        mOthersRecyclerView.setVisibility(View.VISIBLE);
        CommonAdapter othersAdapter = new CommonAdapter(Arrays.asList(OTHERS_EVENT_TYPES));
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(OrientationHelper.VERTICAL);
        mOthersRecyclerView.setLayoutManager(manager);
        mOthersRecyclerView.setAdapter(othersAdapter);
    }

    private class CommonAdapter extends RecyclerView.Adapter<CommonViewHolder> implements View.OnClickListener {
        private List<String> eventTypes;

        public CommonAdapter(List<String> eventTypes) {
            this.eventTypes = eventTypes;
        }

        @Override
        public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.view_item_warn, parent, false);
            return new CommonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommonViewHolder holder, int position) {
            holder.eventName.setText(String.format(mContext.getString(R.string.event_warn), eventTypes.get(position)));
            holder.updateBtn.setOnClickListener(this);
            holder.warnBtn.setOnClickListener(this);
            holder.updateBtn.setTag(eventTypes.get(position));
            holder.warnBtn.setTag(eventTypes.get(position));
        }

        @Override
        public int getItemCount() {
            return eventTypes.size();
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "click: " + String.valueOf(view.getTag()));
            EventTypeInfo typeInfo = new EventTypeInfo.Builder()
                    .eventType(String.valueOf(view.getTag()))
                    .eventTime(System.currentTimeMillis())
                    .build();
            switch (view.getId()) {
                case R.id.warn_btn:
                    HobotWarningSDK.getInstance().warn(typeInfo);
                    break;

                case R.id.update_btn:
                    HobotWarningSDK.getInstance().upload(typeInfo);
                    break;
            }
        }
    }

    private class CommonViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventName;
        private final Button warnBtn;
        private final Button updateBtn;

        public CommonViewHolder(View itemView) {
            super(itemView);
            eventName = (TextView) itemView.findViewById(R.id.event_type);
            warnBtn = (Button) itemView.findViewById(R.id.warn_btn);
            updateBtn = (Button) itemView.findViewById(R.id.update_btn);
        }
    }
}
