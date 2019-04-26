package com.likang.easywifi.lib.core.task;

import android.os.Parcelable;

/**
 * @author likangren
 */
public abstract class WifiTask<T extends WifiTaskCallback> implements Runnable, Parcelable {

    protected final String TAG = getClass().getSimpleName();
    protected T mWifiTaskCallback;
    protected boolean mIsRunning;

    public WifiTask(T wifiTaskCallback) {
        mWifiTaskCallback = wifiTaskCallback;
    }

    public WifiTask() {
    }

    @Override
    public void run() {
        mIsRunning = true;
    }

    public T getWifiTaskCallback() {
        return mWifiTaskCallback;
    }

    public void setWifiTaskCallback(T wifiTaskCallback) {
        mWifiTaskCallback = wifiTaskCallback;
    }

    public void cancel() {
        if (mIsRunning) {
            mWifiTaskCallback = null;
        }
    }

}
