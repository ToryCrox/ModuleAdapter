package com.tory.module_adapter.utils;

public interface ILog {
    void d(String TAG, String msg);
    void i(String TAG, String msg);
    void w(String TAG, String msg);
    void w(String TAG, String msg, Throwable e);
    void e(String TAG, String msg);
    void e(String TAG, String msg, Throwable e);
}
