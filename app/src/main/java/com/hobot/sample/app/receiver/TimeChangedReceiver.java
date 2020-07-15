package com.hobot.sample.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hobot.sample.app.manager.NebulaSDKManager;

/**
 * 时间变化广播。
 *
 * @author Hobot
 */
public class TimeChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    // 时间变化，日期变化，时区变化都要重新启动ADAS/DMS。
                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_DATE_CHANGED:
                    case Intent.ACTION_TIMEZONE_CHANGED:
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_ADAS, "");
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_ADAS, "");

                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_STOP_DMS, "");
                        NebulaSDKManager.getInstance().postToHandler(NebulaSDKManager.CODE_START_DMS, "");
                        break;
                }
            }
        }
    }
}
