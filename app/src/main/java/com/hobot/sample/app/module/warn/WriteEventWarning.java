package com.hobot.sample.app.module.warn;

import android.text.TextUtils;
import android.util.Log;

import com.hobot.adas.sdk.HobotAdasSDK;
import com.hobot.adas.sdk.config.ConfigConst;
import com.hobot.sdk.library.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * 报警事件格式化保存到指定文件中。
 *
 * @author Hobot
 */
public class WriteEventWarning {
    private static final String TAG = WriteEventWarning.class.getSimpleName();
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String FILE_PATH = "sdcard/hobot/warn";
    private static final String FILE_NAME = "all_event_message";

    private static String mEventMessageRecorder;

    /**
     * 保持事件
     *
     * @param eventGroup 事件分组
     * @param eventType  事件类型
     * @param frameID    当前帧数
     * @param eventTime  事件时间
     */
    public static void saveEvent(String eventGroup, String eventType, long frameID, long eventTime) {
        String oneLineMessage = formatPrintEvent(eventGroup, eventType, frameID, eventTime);
        if (!TextUtils.isEmpty(mEventMessageRecorder) && mEventMessageRecorder.contains(oneLineMessage)) {
            return;
        }

        writeToFile(oneLineMessage);

        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(mEventMessageRecorder)) {
            builder.append(oneLineMessage);
        } else {
            builder.append(mEventMessageRecorder).append("\n")
                    .append(oneLineMessage);
        }

        mEventMessageRecorder = builder.toString();
        Log.d(TAG, "event message recorder:\n" + mEventMessageRecorder);
    }

    /**
     * 写到指定文件
     *
     * @param message
     */
    private static void writeToFile(String message) {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        File eventFile = new File(FILE_PATH + File.separator + FILE_NAME);
        if (!eventFile.exists()) {
            try {
                eventFile.createNewFile();
            } catch (IOException e) {
                Log.d(TAG, "create file failed: " + e);
            }
        }

        StringBuilder builder = new StringBuilder(message);
        builder.append("\n");
        message = builder.toString();
        FileUtils.writeFileFromString(eventFile, message, true);
    }

    /**
     * 格式化事件。
     *
     * @param eventGroup 事件分组
     * @param eventType  事件类型
     * @param frameID    事件帧数
     * @param eventTime  事件时间
     * @return 格式化结果
     */
    private static String formatPrintEvent(String eventGroup, String eventType, long frameID, long eventTime) {
        StringBuilder builder = new StringBuilder();
        builder.append(formatTime(eventTime)).append(": ")
                .append("[").append(eventGroup).append("]")
                .append(eventType)
                .append("  frameId: ").append(frameID);

        if (eventGroup.equals("ADAS")) {
            builder.append("  {")
                    .append(decoderPackName(HobotAdasSDK.getInstance().getAdasConfig(ConfigConst.TYPE_PACK_NAME)))
                    .append("}");
        }
        String printEvent = builder.toString();
        Log.d(TAG, "print event: " + printEvent);
        return printEvent;
    }

    /**
     * 格式化时间。
     *
     * @param timestamp 时间戳
     * @return 格式化后的时间
     */
    private static String formatTime(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);
        String time = format.format(timestamp);
        return time;
    }

    /**
     * 格式化Pack名字。
     *
     * @param packString PACK路径
     * @return 格式化结果
     */
    private static String decoderPackName(String packString) {
        String[] packArray = packString.split("/");
        String packName = packArray[packArray.length - 1];
        Log.d(TAG, "pack name: " + packName);
        return packName;
    }
}
