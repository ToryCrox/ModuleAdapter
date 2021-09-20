package com.tory.module_adapter.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

public class TimeRecorder {

    private static final String TAG = "TimeRecorder";
    /**
     * release版下通过以下命令打印log，但是需要重启应用
     * $: adb shell setprop log.tag.TimeRecorder V
     * 关闭命令:
     * $: adb shell setprop log.tag.TimeRecorder D
     */
    public static final String TIME_RECORDER_LOG = "TimeRecorder";
    /**
     * debug模式下打开log，但是如果放在library里面会失效，因为library编译的一直是release版
     */
    private static boolean ENABLED = isPropertyEnabled(TIME_RECORDER_LOG);

    private static long t1;
    private static ConcurrentHashMap<String, Long> sTimeMap;
    private static ConcurrentHashMap<String, CountValue> sNanoCountTimeMap;

    private static DecimalFormat sDecimalFormat;

    private static Printer sPrinter;

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static void setDebug(boolean debug) {
        ENABLED = debug;
    }

    public static void setPrinter(Printer printer) {
        sPrinter = printer;
    }

    /**
     *
     */
    private static void logd(@NonNull String msg) {
        //这里的TAG最好整个模块一个统一的
        //LogUtils.d(TAG, msg);
        if (sPrinter != null) {
            sPrinter.print(msg + "\n");
        }
        // DuLogger.d(TAG + " " + msg);
        LogUtils.d(TAG, msg);
    }

    /**
     * 开始记录时间较短的耗时情况
     * 调用方式为：
     * {@link #beginNanoCount(String)}开始计时 -> {@link #pauseNanoCount(String)} 暂停计时
     * 以上重复调用
     * ->  {@link #endNanoCount(String, String)} 输出总耗时情况
     */
    public static void beginNanoCount(@NonNull String tag) {
        if (!ENABLED) {
            return;
        }
        ensureNanoCountTimeMap();
        CountValue countValue = sNanoCountTimeMap.get(tag);
        if (countValue == null) {
            countValue = new CountValue();
            sNanoCountTimeMap.put(tag, countValue);
            countValue.eclipseTime = 0;
            countValue.count = 0;
        }
        countValue.nanoTime = System.nanoTime();
    }

    /**
     * 暂停计时
     * 调用方式为：
     * {@link #beginNanoCount(String)}开始计时 -> {@link #pauseNanoCount(String)} 暂停计时
     * 以上重复调用
     * ->  {@link #endNanoCount(String, String)} 输出总耗时情况
     */
    public static void pauseNanoCount(@NonNull String tag) {
        if (!ENABLED) {
            return;
        }
        ensureNanoCountTimeMap();
        CountValue countValue = sNanoCountTimeMap.get(tag);
        if (countValue == null || countValue.nanoTime == 0) {
            return;
        }
        countValue.eclipseTime += System.nanoTime() - countValue.nanoTime;
        countValue.nanoTime = 0;
        countValue.count++;
    }

    /**
     * 输出耗时 {@link #endNanoCount(String, String)}
     */
    public static void endNanoCount(@NonNull String tag) {
        endNanoCount(tag, null);
    }

    /**
     * 输出耗时
     * 调用方式为：
     * {@link #beginNanoCount(String)}开始计时 -> {@link #pauseNanoCount(String)} 暂停计时
     * 以上重复调用
     * ->  {@link #endNanoCount(String, String)} 输出总耗时情况
     */
    public static void endNanoCount(@NonNull String tag, @Nullable String call) {
        if (!ENABLED) {
            return;
        }
        ensureNanoCountTimeMap();
        CountValue countValue = sNanoCountTimeMap.get(tag);
        if (countValue == null || countValue.count <= 0) {
            return;
        }
        logd(tag + " " + (call == null ? "" : call) +
                " time spent=" + nanoToMillis(countValue.eclipseTime) +
                ", count=" + countValue.count + ", per time spent="
                + nanoToMillis(countValue.eclipseTime / countValue.count) + "ms");
        sNanoCountTimeMap.remove(tag);
    }

    public static String nanoToMillis(long nanoTime) {
        return getDecimalFormat().format(nanoTime * 1.0 / 1000000);
    }

    /**
     * 开始记录时间，供临时开发调用
     * 对应调用无参数的end或者一个参数的end
     * {@link #end()}
     */
    public static void begin() {
        if (ENABLED) {
            t1 = currentTimeNanos();
        }
    }

    public static long end() {
        if (ENABLED) {
            return currentTimeNanos() - t1;
        }
        return 0;
    }

    /**
     * 开始记录时间
     * 对应调用两个参数的end
     * {@link #end(String, String)}
     *
     * @param tag : 记录时间的惟一标识
     */
    public static void begin(@NonNull String tag) {
        if (ENABLED) {
            ensureTimeMap();
            sTimeMap.put(tag, currentTimeNanos());
        }
    }

    /**
     * 输出耗时
     * {@link #begin(String)}
     */
    public static void end(@NonNull String tag) {
        end(tag, null);
    }

    /**
     * 输出耗时
     * {@link #begin(String)}
     *
     * @param tag  记录时间的惟一标识
     * @param call :log输出内容,可以为空
     */
    public static void end(@NonNull String tag, @Nullable String call) {
        if (ENABLED) {
            ensureTimeMap();
            Long timeStamp = sTimeMap.get(tag);
            if (timeStamp == null) {
                return;
            }
            logd(tag + " " + (call == null ? "" : call)
                    + " time spent=" + nanoToMillis(currentTimeNanos() - timeStamp) + "ms");
            sTimeMap.remove(tag);
        }
    }

    /**
     * 记录时间,可考虑以后使用其它单位
     */
    private static long currentTimeNanos() {
        return System.nanoTime();
    }

    private static void ensureTimeMap() {
        if (ENABLED) {
            if (sTimeMap == null) {
                sTimeMap = new ConcurrentHashMap<>();
            }
        }
    }

    private static void ensureNanoCountTimeMap() {
        if (ENABLED) {
            if (sNanoCountTimeMap == null) {
                sNanoCountTimeMap = new ConcurrentHashMap<>();
            }
        }
    }

    private static DecimalFormat getDecimalFormat() {
        if (sDecimalFormat == null) {
            DecimalFormat format = new DecimalFormat("0.##");
            //未保留小数的舍弃规则，RoundingMode.HALF_UP表示四舍五入。
            format.setRoundingMode(RoundingMode.HALF_UP);
            sDecimalFormat = format;
        }
        return sDecimalFormat;
    }


    private static class CountValue {
        int count;
        long nanoTime;
        long eclipseTime;
    }

    interface Printer {
        void print(@NonNull String msg);
    }
}
