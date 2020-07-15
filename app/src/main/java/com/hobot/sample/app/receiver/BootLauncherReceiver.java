package com.hobot.sample.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.hobot.sample.app.activity.LauncherActivity;

/**
 * 接收开机广播并启动测试应用
 *
 * @author Hobot
 */
public class BootLauncherReceiver extends BroadcastReceiver {
    private static final String TAG = BootLauncherReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action = " + action);
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case Intent.ACTION_BOOT_COMPLETED:
                        Intent activityIntent = new Intent(context, LauncherActivity.class);
                        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(activityIntent);
                        break;
                }
            }
        }
    }
}
