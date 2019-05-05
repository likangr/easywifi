package com.likang.easywifi.lib.core.task;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
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
    public static final int STATUS_CANCELED = 5;

    WifiTaskCallback mWifiTaskCallback;

    int mRunningCurrentStep;
    int mFailReason;
    int mCurrentStatus = STATUS_IDLE;
    private String mTag;

    private Runnable mTimeoutRunnable;
    private BroadcastReceiver mAutoReleaseBroadcastReceiver;

    WifiTask(Parcel in) {
        mCurrentStatus = in.readInt();
        mRunningCurrentStep = in.readInt();
        mFailReason = in.readInt();
        mTag = in.readString();
    }

    WifiTask(WifiTaskCallback wifiTaskCallback) {
        mWifiTaskCallback = wifiTaskCallback;
    }


    public WifiTaskCallback getWifiTaskCallback() {
        return mWifiTaskCallback;
    }

    public void setWifiTaskCallback(WifiTaskCallback wifiTaskCallback) {
        mWifiTaskCallback = wifiTaskCallback;
    }

    public synchronized int getRunningCurrentStep() {
        return mRunningCurrentStep;
    }

    public synchronized int getFailReason() {
        return mFailReason;
    }

    public synchronized int getCurrentStatus() {
        return mCurrentStatus;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }


    public synchronized boolean registerAutoReleaseReceiver(BroadcastReceiver receiver,
                                                            IntentFilter filter,
                                                            long timeout,
                                                            final int timeoutFailReason) {
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        mAutoReleaseBroadcastReceiver = receiver;
        ApplicationHolder.getApplication().registerReceiver(mAutoReleaseBroadcastReceiver, filter);

        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                unregisterAutoReleaseReceiver();
                callOnTaskFail(timeoutFailReason);
            }
        };
        EasyWifi.getHandler().postDelayed(mTimeoutRunnable, timeout);
        return true;
    }

    private synchronized void unregisterAutoReleaseReceiver() {
        if (mCurrentStatus == STATUS_CANCELED) {
            return;
        }
        if (mAutoReleaseBroadcastReceiver != null) {

            EasyWifi.getHandler().removeCallbacks(mTimeoutRunnable);
            mTimeoutRunnable = null;

            ApplicationHolder.getApplication().unregisterReceiver(mAutoReleaseBroadcastReceiver);
            mAutoReleaseBroadcastReceiver = null;

        }
    }


    abstract void checkParams();

    @Override
    public void run() {
        checkParams();
    }


    public synchronized void cancel() {

        if (mCurrentStatus == STATUS_RUNNING) {
            unregisterAutoReleaseReceiver();
        }
        callOnTaskCanceled();
    }

    public synchronized boolean callOnTaskStartRun() {
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        EasyWifi.getCurrentTasks().add(this);
        mCurrentStatus = STATUS_RUNNING;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskStartRun(this);
        }
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        return true;
    }

    public synchronized boolean callOnTaskRunningCurrentStep(int currentStep) {
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        mRunningCurrentStep = currentStep;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskRunningCurrentStep(this);
        }

        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        return true;
    }

    public synchronized boolean callOnTaskSuccess() {
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        unregisterAutoReleaseReceiver();
        EasyWifi.getCurrentTasks().remove(this);
        mCurrentStatus = STATUS_SUCCEED;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskSuccess(this);
        }
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        return true;
    }

    public synchronized boolean callOnTaskFail(int failReason) {
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        unregisterAutoReleaseReceiver();
        EasyWifi.getCurrentTasks().remove(this);
        mCurrentStatus = STATUS_FAILED;
        mFailReason = failReason;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskFail(this);
        }
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        return true;
    }

    public void callOnTaskCanceled() {
        EasyWifi.getCurrentTasks().remove(this);
        mCurrentStatus = STATUS_CANCELED;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskCancel(this);
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
        dest.writeString(mTag);
    }

}
