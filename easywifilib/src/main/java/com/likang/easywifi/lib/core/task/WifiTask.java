package com.likang.easywifi.lib.core.task;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.util.ApplicationHolder;

/**
 * @author likangren
 */
public abstract class WifiTask implements Runnable, Parcelable {

    protected final String TAG = getClass().getSimpleName();

    public static final int STATUS_IDLE = 1;
    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_SUCCEED = 3;
    public static final int STATUS_FAILED = 4;

    WifiTaskCallback mWifiTaskCallback;

    int mRunningCurrentStep;
    int mFailReason;
    int mCurrentStatus = STATUS_IDLE;

    private Runnable mPostDelayRunnable;
    private BroadcastReceiver mBroadcastReceiver;

    WifiTask(Parcel in) {
        mCurrentStatus = in.readInt();
        mRunningCurrentStep = in.readInt();
        mFailReason = in.readInt();
    }

    WifiTask(WifiTaskCallback wifiTaskCallback) {
        mWifiTaskCallback = wifiTaskCallback;
    }

    void customCancel(Application application) {

    }

    public void cancel() {
        EasyWifi.getCurrentTasks().remove(this);
        final Handler handler = EasyWifi.getHandler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mCurrentStatus == STATUS_RUNNING) {
                    Application application = ApplicationHolder.getApplication();
                    customCancel(application);
                    if (mBroadcastReceiver != null) {
                        application.unregisterReceiver(mBroadcastReceiver);
                        mBroadcastReceiver = null;
                    }
                    if (mPostDelayRunnable != null) {
                        handler.removeCallbacks(mPostDelayRunnable);
                        mPostDelayRunnable = null;
                    }

                    mCurrentStatus = STATUS_IDLE;
                }
            }
        });
    }

    public WifiTaskCallback getWifiTaskCallback() {
        return mWifiTaskCallback;
    }

    public void setWifiTaskCallback(WifiTaskCallback wifiTaskCallback) {
        mWifiTaskCallback = wifiTaskCallback;
    }

    public int getRunningCurrentStep() {
        return mRunningCurrentStep;
    }

    public int getFailReason() {
        return mFailReason;
    }

    public int getCurrentStatus() {
        return mCurrentStatus;
    }

    public Runnable getPostDelayRunnable() {
        return mPostDelayRunnable;
    }

    public void setPostDelayRunnable(Runnable postDelayRunnable) {
        mPostDelayRunnable = postDelayRunnable;
    }

    public BroadcastReceiver getBroadcastReceiver() {
        return mBroadcastReceiver;
    }

    public void setBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
        mBroadcastReceiver = broadcastReceiver;
    }

    abstract void checkParams();

    @Override
    public void run() {
        checkParams();
    }

    public void callOnTaskStartRun() {
        EasyWifi.getCurrentTasks().add(this);
        mCurrentStatus = STATUS_RUNNING;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskStartRun(this);
        }
    }

    public void callOnTaskRunningCurrentStep(int currentStep) {
        mRunningCurrentStep = currentStep;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskRunningCurrentStep(this);
        }
    }

    public void callOnTaskSuccess() {
        EasyWifi.getCurrentTasks().remove(this);
        mCurrentStatus = STATUS_SUCCEED;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskSuccess(this);
        }
    }

    public void callOnTaskFail(int failReason) {
        EasyWifi.getCurrentTasks().remove(this);
        mCurrentStatus = STATUS_FAILED;
        mFailReason = failReason;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskFail(this);
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCurrentStatus);
        dest.writeInt(mRunningCurrentStep);
        dest.writeInt(mFailReason);
    }

}
