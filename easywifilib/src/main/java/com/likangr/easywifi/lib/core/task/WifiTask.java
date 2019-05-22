package com.likangr.easywifi.lib.core.task;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;

import com.likangr.easywifi.lib.EasyWifi;
import com.likangr.easywifi.lib.util.ApplicationHolder;

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

    protected WifiTaskCallback mWifiTaskCallback;

    protected int mRunningCurrentStep;
    protected int mLastRunningCurrentStep;
    protected int mFailReason;
    protected int mCurrentStatus = STATUS_IDLE;
    protected String mTag;
    protected boolean mIsResumeTask;

    private Runnable mTimeoutRunnable;
    private BroadcastReceiver mAutoReleaseBroadcastReceiver;


    protected WifiTask() {
    }

    protected WifiTask(Parcel in) {
        mLastRunningCurrentStep = in.readInt();
        mTag = in.readString();
        mIsResumeTask = true;
    }

    protected WifiTask(WifiTaskCallback wifiTaskCallback) {
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

    public boolean isResumedTask() {
        return mIsResumeTask;
    }

    public int getLastRunningCurrentStep() {
        return mLastRunningCurrentStep;
    }

    protected synchronized boolean registerAutoReleaseReceiver(BroadcastReceiver receiver,
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

    protected void unregisterAutoReleaseReceiver() {
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

    protected abstract void checkParams();

    @Override
    public synchronized void run() {
        checkParams();
    }


    public synchronized void cancel() {

        if (mCurrentStatus == STATUS_RUNNING) {
            unregisterAutoReleaseReceiver();
        }
        callOnTaskCanceled();
    }

    protected synchronized boolean callOnTaskStartRun() {
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        mCurrentStatus = STATUS_RUNNING;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskStartRun(this);
        }
        if (mCurrentStatus == STATUS_CANCELED) {
            return false;
        }
        return true;
    }

    protected synchronized boolean callOnTaskRunningCurrentStep(int currentStep) {
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

    protected synchronized void callOnTaskSuccess() {
        callOnTaskSuccess(true);
    }

    protected synchronized void callOnTaskSuccess(boolean isCallExecuteNextWifiTaskIfHasMethod) {
        if (mCurrentStatus == STATUS_CANCELED) {
            return;
        }
        unregisterAutoReleaseReceiver();
        mCurrentStatus = STATUS_SUCCEED;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskSuccess(this);
        }
        if (isCallExecuteNextWifiTaskIfHasMethod) {
            EasyWifi.executeNextWifiTaskIfHas();
        }
    }

    protected synchronized void callOnTaskFail(int failReason) {
        callOnTaskFail(failReason, true);
    }

    protected synchronized void callOnTaskFail(int failReason, boolean isCallExecuteNextWifiTaskIfHasMethod) {
        if (mCurrentStatus == STATUS_CANCELED) {
            return;
        }
        unregisterAutoReleaseReceiver();
        mCurrentStatus = STATUS_FAILED;
        mFailReason = failReason;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskFail(this);
        }
        if (isCallExecuteNextWifiTaskIfHasMethod) {
            EasyWifi.executeNextWifiTaskIfHas();
        }
    }

    protected synchronized void callOnTaskCanceled() {
        callOnTaskCanceled(true);
    }

    protected synchronized void callOnTaskCanceled(boolean isCallExecuteNextWifiTaskIfHasMethod) {
        mCurrentStatus = STATUS_CANCELED;
        if (mWifiTaskCallback != null) {
            mWifiTaskCallback.onTaskCancel(this);
        }
        if (isCallExecuteNextWifiTaskIfHasMethod) {
            EasyWifi.executeNextWifiTaskIfHas();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mRunningCurrentStep);
        dest.writeString(mTag);
    }

}
