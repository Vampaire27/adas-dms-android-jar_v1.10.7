package com.hobot.sample.app.util;

import android.content.Context;

import com.hobot.camera.library.Camera2API;
import com.hobot.camera.library.Camera2Manager;
import com.hobot.camera.library.CameraAPI;
import com.hobot.camera.library.USBCameraAPI;
import com.hobot.camera.library.base.ICameraAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera帮助类。
 *
 * @author Hobot
 */
public class CameraHelper {
    private static final String TAG = CameraHelper.class.getSimpleName();
    private static final List<CameraBean> sCameraList = new ArrayList<>();

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Camera2Manager.getInstance().init(context);
    }

    /**
     * 反初始化
     */
    public static void release() {
        Camera2Manager.getInstance().release();
    }

    /**
     * 获取所有Camera列表。
     *
     * @return Camera列表
     */
    public static List<ICameraAPI> getCameras() {
        return Camera2Manager.getInstance().getCameras();
    }

    /**
     * 查询Camera示例。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return 示例
     */
    private static CameraBean getOrCreateCamera(int cameraId, int cameraType) {
        synchronized (sCameraList) {
            // 查询Camera
            for (CameraBean cameraBean : sCameraList) {
                if (cameraId == cameraBean.cameraId && cameraType == cameraBean.cameraType) {
                    return cameraBean;
                }
            }
            // 创建Camera
            CameraBean cameraBean = new CameraBean(cameraId, cameraType);
            sCameraList.add(cameraBean);
            return cameraBean;
        }
    }

    /**
     * 查询Camera示例。
     *
     * @param cameraAPI camera示例
     * @return 示例
     */
    private static CameraBean getOrCreateCamera(ICameraAPI cameraAPI) {
        synchronized (sCameraList) {
            // 查询Camera
            for (CameraBean cameraBean : sCameraList) {
                if (cameraAPI == cameraBean.cameraAPI) {
                    return cameraBean;
                }
            }
            // 创建Camera
            CameraBean cameraBean = new CameraBean(cameraAPI);
            sCameraList.add(cameraBean);
            return cameraBean;
        }
    }

    /**
     * 移除Camera示例。
     *
     * @param cameraAPI camera示例
     * @return 移除结果
     */
    private static boolean removeCamera(ICameraAPI cameraAPI) {
        synchronized (sCameraList) {
            // 查询Camera
            for (CameraBean cameraBean : sCameraList) {
                if (cameraAPI == cameraBean.cameraAPI) {
                    if (cameraBean.isPreviewed()) {
                        cameraBean.stopPreview(true);
                    }
                    return sCameraList.remove(cameraBean);
                }
            }
            return false;
        }
    }

    /**
     * 移除Camera示例。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return 移除结果
     */
    private static boolean removeCamera(int cameraId, int cameraType) {
        synchronized (sCameraList) {
            // 查询Camera
            for (CameraBean cameraBean : sCameraList) {
                if (cameraId == cameraBean.cameraId && cameraType == cameraBean.cameraType) {
                    if (cameraBean.isPreviewed()) {
                        cameraBean.stopPreview(true);
                    }
                    return sCameraList.remove(cameraBean);
                }
            }
            return false;
        }
    }

    /**
     * 创建Camera。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI createCamera(int cameraId, int cameraType) {
        CameraBean cameraBean = getOrCreateCamera(cameraId, cameraType);
        return cameraBean.cameraAPI;
    }

    /**
     * 打开Camera。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI openCamera(int cameraId, int cameraType) {
        return openCamera(cameraId, cameraType, false);
    }

    /**
     * 打开Camera。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI openCamera(int cameraId, int cameraType, boolean force) {
        CameraBean cameraBean = getOrCreateCamera(cameraId, cameraType);
        cameraBean.startPreview(force);
        return cameraBean.cameraAPI;
    }

    /**
     * 打开Camera。
     *
     * @param cameraAPI camera示例
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI openCamera(ICameraAPI cameraAPI) {
        return openCamera(cameraAPI, false);
    }

    /**
     * 打开Camera。
     *
     * @param cameraAPI camera示例
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI openCamera(ICameraAPI cameraAPI, boolean force) {
        CameraBean cameraBean = getOrCreateCamera(cameraAPI);
        cameraBean.startPreview(force);
        return cameraBean.cameraAPI;
    }

    /**
     * 关闭Camera。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI closeCamera(int cameraId, int cameraType) {
        return closeCamera(cameraId, cameraType, false);
    }

    /**
     * 关闭Camera。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI closeCamera(int cameraId, int cameraType, boolean force) {
        CameraBean cameraBean = getOrCreateCamera(cameraId, cameraType);
        cameraBean.stopPreview(force);
        return cameraBean.cameraAPI;
    }

    /**
     * 关闭Camera。
     *
     * @param cameraAPI camera示例
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI closeCamera(ICameraAPI cameraAPI) {
        return closeCamera(cameraAPI, false);
    }

    /**
     * 关闭Camera。
     *
     * @param cameraAPI camera示例
     * @return {@link ICameraAPI}
     */
    public static ICameraAPI closeCamera(ICameraAPI cameraAPI, boolean force) {
        CameraBean cameraBean = getOrCreateCamera(cameraAPI);
        cameraBean.stopPreview(force);
        return cameraBean.cameraAPI;
    }

    /**
     * 销毁Camera。
     *
     * @param cameraId   camera id
     * @param cameraType camera类型
     * @return 移除结果
     */
    public static boolean destroyCamera(int cameraId, int cameraType) {
        return removeCamera(cameraId, cameraType);
    }

    /**
     * 销毁Camera。
     *
     * @param cameraAPI camera示例
     * @return 移除结果
     */
    public static boolean destroyCamera(ICameraAPI cameraAPI) {
        return removeCamera(cameraAPI);
    }

    /**
     * Camera封装类。
     */
    private static class CameraBean {
        ICameraAPI cameraAPI;
        int count;
        int cameraId;
        int cameraType;

        public CameraBean(int cameraId, int cameraType) {
            this.cameraId = cameraId;
            this.cameraType = cameraType;
            cameraAPI = Camera2Manager.getInstance().create(cameraId, cameraType);
        }

        public CameraBean(ICameraAPI cameraAPI) {
            if (cameraAPI instanceof CameraAPI) {
                this.cameraType = Camera2Manager.CameraType.CAMERA_1;
            } else if (cameraAPI instanceof Camera2API) {
                this.cameraType = Camera2Manager.CameraType.CAMERA_2;
            } else if (cameraAPI instanceof USBCameraAPI) {
                this.cameraType = Camera2Manager.CameraType.CAMERA_USB;
            } else {
                this.cameraType = Camera2Manager.CameraType.CAMERA_OTHER;
            }
            cameraId = cameraAPI.getCameraId();
            this.cameraAPI = cameraAPI;
        }

        public void startPreview(boolean force) {
            if (cameraAPI != null) {
                if (count == 0 || force) {
                    Camera2Manager.getInstance().open(cameraAPI);
                }
                if (!force) {
                    count++;
                }
                // 记录Camera打开次数，强制打开不计数
            }
        }

        public void stopPreview(boolean force) {
            if (cameraAPI != null) {
                // 只有一个人用的时候才真正关闭
                if (count == 1 || force) {
                    Camera2Manager.getInstance().close(cameraAPI);
                }
                if (!force) {
                    count--;
                }
                // 记录Camera关闭次数，强制打开不计数
            }
        }

        public boolean isPreviewed() {
            if (cameraAPI != null) {
                return cameraAPI.isPreviewed();
            }
            return false;
        }
    }

}
