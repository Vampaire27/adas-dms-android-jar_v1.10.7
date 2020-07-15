package com.hobot.sample.app.module.upload;

import android.os.Process;

import com.hobot.sdk.library.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Log工具类。
 *
 * @author Hobot
 */
public class LogUtils {
    private static final String DEFAULT_LOG_PATH = "/sdcard/hobot/cache/log/";
    private static final SimpleDateFormat DEFAULT_DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取Log输出的格式。
     * <p>
     * 年-月-月-时-分-秒
     *
     * @return 日期格式
     */
    public static String getLogSuffix() {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int min = calendar.get(Calendar.MINUTE);
        final int second = calendar.get(Calendar.SECOND);
        final StringBuilder dateStrB = new StringBuilder();
        dateStrB.append(year).append("-");
        if (month < 10) {
            dateStrB.append("0").append(month);
        } else {
            dateStrB.append(month);
        }
        dateStrB.append("-");
        if (day < 10) {
            dateStrB.append("0").append(day);
        } else {
            dateStrB.append(day);
        }
        dateStrB.append("-");
        if (hour < 10) {
            dateStrB.append("0").append(hour);
        } else {
            dateStrB.append(hour);
        }
        if (min < 10) {
            dateStrB.append("0").append(min);
        } else {
            dateStrB.append(min);
        }
        if (second < 10) {
            dateStrB.append("0").append(second);
        } else {
            dateStrB.append(second);
        }
        return dateStrB.toString();
    }

    /**
     * 获取Log文件路径。
     *
     * @return 文件路径
     */
    public static String getTempLogFileName() {
        return new StringBuilder().append(DEFAULT_LOG_PATH).append(getLogSuffix()).append("-temp").append(".log").toString();
    }

    /**
     * 抓取指定时长的Log并写入文件。
     *
     * @param time          抓取时间
     * @param singleProcess 是否抓取全进程的
     * @param filter        是否添加过滤
     * @return 文件路径
     */
    public static String logcat(long time, boolean singleProcess, boolean filter) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("logcat")
                .append(" -v")
                .append(" time");
        if (singleProcess) {
            // TODO: 2019/12/4 不同设备适配
            logBuilder.append(" --pid=" + Process.myPid());
        }
        if (filter) {
            logBuilder.append(" -s")
                    .append(" HobotLog-LogPrint")
                    .append(" DmsManager")
                    .append(" DMS-JNI")
                    .append(" WarnModule")
                    .append(" EventSelector")
                    .append(" PlayEvent")
                    .append(" DmsBaseView:D")
                    .append(" AdasManager")
                    .append(" AdasBaseView:D")
                    .append(" zk_log");
        }

        List<String> cmds = new ArrayList<>();
        String cmdLOG = logBuilder.toString();
        cmds.add(cmdLOG);

        String logFilePath = getTempLogFileName();
        File logFile = null;
        if (!FileUtils.isFileExists(logFilePath)) {
            logFile = new File(logFilePath);
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        FileOutputStream fos = null;
        BufferedReader reader = null;
        try {
            java.lang.Process process = Runtime.getRuntime().exec(cmdLOG);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            fos = new FileOutputStream(logFile);

            long start = System.currentTimeMillis();
            String str = null;
            while ((str = reader.readLine()) != null) {
                fos.write(str.getBytes());
                fos.write(System.getProperty("line.separator").getBytes());
                long now = System.currentTimeMillis();
                if (now - start > time) { //抓取3分钟日志
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logFilePath;
    }

    /**
     * 抓取指定时长的Log并写入文件。
     *
     * @param timestamp 触发时间
     * @return 文件路径
     */
    public static String logcat(long timestamp) {
        File logPath = new File(DEFAULT_LOG_PATH);
        if (FileUtils.isFileExists(logPath)) {
            File[] logFiles = logPath.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith("logcat_")) {
                        return true;
                    }
                    return false;
                }
            });
            for (File file : logFiles) {
                String fileName = file.getName();
                String date = fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf("."));
                try {
                    Date fileDate = DEFAULT_DATA_FORMAT.parse(date);
                    long fileTime = fileDate.getTime();
                    if (timestamp - fileTime > 0 && timestamp - fileTime < 24 * 60 * 60 * 1000) {
                        return file.getAbsolutePath();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
