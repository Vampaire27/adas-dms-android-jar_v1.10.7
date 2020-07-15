package com.hobot.sample.app.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 悬浮窗权限检查
 */
public class FloatPermission {
    private static final String TAG = FloatPermission.class.getSimpleName();

    /**
     * 请求权限
     *
     * @param context
     * @return 是否请求成功
     */
    public static boolean requestPermission(Context context) {
        Class clazz = Settings.class;
        Field field = null;
        try {
            field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (field == null) {
            Log.w(TAG, "requestPermission: cannot get field!");
            return false;
        }

        Intent intent = null;
        try {
            intent = new Intent(field.get(null).toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (intent == null) {
            Log.w(TAG, "requestPermission: cannot create intent");
            return false;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
        return true;
    }

    /**
     * 检查是否有权限
     *
     * @param context 上下文
     * @return 是否有权限
     */
    public static boolean checkPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= 28) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }
}
