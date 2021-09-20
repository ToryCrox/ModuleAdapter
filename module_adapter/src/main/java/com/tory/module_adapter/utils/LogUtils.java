package com.tory.module_adapter.utils;

import android.util.Log;

import androidx.annotation.NonNull;

public class LogUtils {
    private static final String TAG = "NoName";

    private static ILog sLog = new LogImpl();

    public static void setThemeLog(@NonNull ILog log) {
        sLog = log;
    }

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static void d(String TAG, String msg) {
        sLog.d(TAG, msg);
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void i(String TAG, String msg) {
        sLog.i(TAG, msg);
    }

    public static void w(String TAG, String msg) {
        sLog.w(TAG, msg);
    }

    public static void w(String msg) {
        sLog.w(TAG, msg);
    }

    public static void w(String msg, Throwable e) {
        sLog.w(TAG, msg, e);
    }

    public static void e(String TAG, String msg) {
        sLog.e(TAG, msg);
    }

    public static void e(String TAG, String msg, Throwable e) {
        sLog.e(TAG, msg, e);
    }
    public static void e(String msg, Throwable e) {
        sLog.e(TAG, msg, e);
    }

    public static class LogImpl implements ILog {

        @Override
        public void d(String TAG, String msg) {
            Log.d(TAG, msg);
        }

        @Override
        public void i(String TAG, String msg) {
            Log.i(TAG, msg);
        }

        @Override
        public void w(String TAG, String msg) {
            Log.w(TAG, msg);
        }

        @Override
        public void w(String TAG, String msg, Throwable e) {
            Log.w(TAG, msg, e);
        }

        @Override
        public void e(String TAG, String msg) {
            Log.e(TAG, msg);
        }

        @Override
        public void e(String TAG, String msg, Throwable e) {
            Log.e(TAG, msg, e);
        }
    }
}
