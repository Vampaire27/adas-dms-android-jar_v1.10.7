package com.hobot.sample.app.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.hobot.sdk.library.modules.log.HobotLog;
import com.hobot.sdk.library.tasks.TimingTask;
import com.hobot.sdk.library.tasks.TimingThreadPool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GPS 管理类
 *
 * @author Hobot
 */
public class GpsManager implements LocationListener, GpsStatus.Listener {
    private static final String TAG = "GpsManager";
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final long GPS_MIN_TIME = 100; // 100 ms
    private static final int GPS_MIN_DISTANCE = 0; // 0 meters
    private static final int DELAY_TIME = 3000; // 3 sec
    private static final int PERIOD_TIME = 3000; // 3 sec
    private final List<LocationListener> mListeners = new LinkedList<>();
    private LocationManager mLocationManager;
    private AtomicBoolean mAtomicIsInit = new AtomicBoolean(false); // 初始化状态

    private TimingTask mGpsMonitorTask = new GpsMonitorTask(DELAY_TIME, PERIOD_TIME, TimeUnit.MILLISECONDS);

    private boolean mIsUpdate = false;

    private GpsManager() {
    }

    public static GpsManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     * @return 是否初始化成功
     */
    public boolean init(Context context) {
        if (isInitOk()) {
            return true;
        }
        if (!checkPermission(context, PERMISSIONS)) {
            Log.e(TAG, "init: please confirm the permission!");
            return false;
        }
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (null == mLocationManager) {
            Log.e(TAG, "init: mLocationManager is null!");
            return false;
        }
        // 是否存在GPS
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, this);
        }
        mLocationManager.addGpsStatusListener(this);

        // 开启gps检查
        TimingThreadPool.get().execute(mGpsMonitorTask);
        mAtomicIsInit.set(true);
        return true;
    }

    public boolean isInitOk() {
        return mAtomicIsInit.get();
    }

    /**
     * 反初始化
     */
    @SuppressLint("MissingPermission")
    public void destroy() {
        if (null == mLocationManager) {
            Log.e(TAG, "destroy: mLocationManager is null");
            return;
        }
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);

        TimingThreadPool.get().cancel(mGpsMonitorTask, true);
    }

    /**
     * 注册监听
     *
     * @param listener 监听
     */
    public void registerListener(LocationListener listener) {
        synchronized (mListeners) {
            if (mListeners.contains(listener)) {
                Log.e(TAG, "registerListener() called with: listener = [" + listener + "] has been register!");
                return;
            }
            mListeners.add(listener);
        }
    }

    /**
     * 解注册监听
     *
     * @param listener 监听
     */
    public void unregisterListener(LocationListener listener) {
        synchronized (mListeners) {
            if (!mListeners.contains(listener)) {
                Log.e(TAG, "unregisterListener() called with: listener = [" + listener + "] not been register!");
                return;
            }
            mListeners.remove(listener);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        HobotLog.d(TAG, "onLocationChanged() called with: location = [" + location + "]");
        // 说明有回调
        mIsUpdate = true;
        synchronized (mListeners) {
            for (LocationListener listener : mListeners) {
                listener.onLocationChanged(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        HobotLog.e(TAG, "onStatusChanged() called with: provider = [" + provider + "], status = [" + status + "], extras = [" + extras + "]");
        synchronized (mListeners) {
            for (LocationListener listener : mListeners) {
                listener.onStatusChanged(provider, status, extras);
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        HobotLog.e(TAG, "onProviderEnabled() called with: provider = [" + provider + "]");
        synchronized (mListeners) {
            for (LocationListener listener : mListeners) {
                listener.onProviderEnabled(provider);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        HobotLog.d(TAG, "onProviderDisabled() called with: provider = [" + provider + "]");
        synchronized (mListeners) {
            for (LocationListener listener : mListeners) {
                listener.onProviderDisabled(provider);
            }
        }
    }

    /**
     * 权限检查。
     *
     * @param context     上下文
     * @param permissions 权限检查
     * @return 检查结果
     */
    private boolean checkPermission(Context context, String... permissions) {
        if (null == context) {
            Log.e(TAG, "checkPermission: context is null");
            return false;
        }
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            Log.e(TAG, "checkPermission: permission: " + permission + " not granted.");
            return false;
        }
        return true;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        GpsStatus status = mLocationManager.getGpsStatus(null); //取当前状态
        updateGpsStatus(event, status); // 更新GPS的卫星状态
    }

    /**
     * 获取GPS卫星数量
     *
     * @param event  事件类型
     * @param status 状态
     * @return 数量。
     */
    private List<GpsSatellite> updateGpsStatus(int event, GpsStatus status) {
        List<GpsSatellite> satelliteList = new ArrayList<>();
        if (status == null) {
            // No satellite
        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            satelliteList.clear();
            int count = 0;
            int useCount = 0;
            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite satellite = it.next();
                if (satellite.usedInFix()) {
                    satelliteList.add(satellite);
                    useCount++;
                }
                count++;
            }
            HobotLog.d(TAG, "updateGpsStatus count = " + count + ", useCount = " + useCount);
        }
        return satelliteList;
    }

    private final static class SingletonHolder {
        private static final GpsManager INSTANCE = new GpsManager();
    }

    /**
     * 手动判断有无GPS服务
     */
    private class GpsMonitorTask extends TimingTask {

        public GpsMonitorTask(long delayTime, long period, TimeUnit timeUnit) {
            super(delayTime, period, timeUnit);
        }

        @Override
        protected void runTask() {
            // 如果速度没有更新 回调异常
            if (!mIsUpdate) {
                onStatusChanged(LocationManager.GPS_PROVIDER, LocationProvider.OUT_OF_SERVICE, null);
            }
            // 如果 速度有更新 下次再判断
            else {
                mIsUpdate = false;
            }
        }

        @Override
        protected String TAG() {
            return TAG + "-" + GpsMonitorTask.class.getSimpleName();
        }
    }


}
