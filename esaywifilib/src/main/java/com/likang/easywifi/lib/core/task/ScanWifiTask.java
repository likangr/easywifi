package com.likang.easywifi.lib.core.task;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.util.ApplicationHolder;


/**
 * @author likangren
 */
public class ScanWifiTask extends WifiTask<ScanWifiTask.OnScanWifiCallback> {

    private long mScanWifiTimeout;
    private long mSetWifiEnabledTimeout;
    private int mScanWifiWay;
    private boolean mIsAutoSwitchToThroughSystemWifi;
    private Activity mSingleTaskActivity;


    public ScanWifiTask(long scanWifiTimeout,
                        long setWifiEnabledTimeout,
                        int scanWifiWay,
                        boolean isAutoSwitchToThroughSystemWifi,
                        Activity singleTaskActivity,
                        OnScanWifiCallback onScanWifiCallback) {
        super(onScanWifiCallback);
        mScanWifiTimeout = scanWifiTimeout;
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
        mScanWifiWay = scanWifiWay;
        mIsAutoSwitchToThroughSystemWifi = isAutoSwitchToThroughSystemWifi;
        mSingleTaskActivity = singleTaskActivity;

        if (mScanWifiWay == EasyWifi.SCAN_WIFI_WAY_THROUGH_WIFI_SETTING || mIsAutoSwitchToThroughSystemWifi) {
            if (mSingleTaskActivity == null) {
                throw new IllegalArgumentException("singleTaskActivity can't be null!");
            }

            int launchMode = -1;
            try {
                Application application = ApplicationHolder.getApplication();
                ComponentName componentName = new ComponentName(application, singleTaskActivity.getClass());
                launchMode = application.getPackageManager().getActivityInfo(componentName, PackageManager.GET_META_DATA).launchMode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (launchMode != ActivityInfo.LAUNCH_SINGLE_TASK) {
                throw new IllegalArgumentException("singleTaskActivity's launch mode must be single task!");
            }
        }
    }


    protected ScanWifiTask(Parcel in) {
        mScanWifiTimeout = in.readLong();
        mSetWifiEnabledTimeout = in.readLong();
        mScanWifiWay = in.readInt();
        mIsAutoSwitchToThroughSystemWifi = in.readByte() == 1;
    }


    public long getScanWifiTimeout() {
        return mScanWifiTimeout;
    }

    public void setScanWifiTimeout(long scanWifiTimeout) {
        mScanWifiTimeout = scanWifiTimeout;
    }

    public long getSetWifiEnabledTimeout() {
        return mSetWifiEnabledTimeout;
    }

    public void setSetWifiEnabledTimeout(long setWifiEnabledTimeout) {
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
    }

    public int getScanWifiWay() {
        return mScanWifiWay;
    }

    public void setScanWifiWay(int scanWifiWay) {
        mScanWifiWay = scanWifiWay;
    }

    public boolean isIsAutoSwitchToThroughSystemWifi() {
        return mIsAutoSwitchToThroughSystemWifi;
    }

    public void setIsAutoSwitchToThroughSystemWifi(boolean isAutoSwitchToThroughSystemWifi) {
        mIsAutoSwitchToThroughSystemWifi = isAutoSwitchToThroughSystemWifi;
    }

    public Activity getSingleTaskActivity() {
        return mSingleTaskActivity;
    }

    public void setSingleTaskActivity(Activity singleTaskActivity) {
        mSingleTaskActivity = singleTaskActivity;
    }


    public static final Creator<ScanWifiTask> CREATOR = new Creator<ScanWifiTask>() {
        @Override
        public ScanWifiTask createFromParcel(Parcel in) {
            return new ScanWifiTask(in);
        }

        @Override
        public ScanWifiTask[] newArray(int size) {
            return new ScanWifiTask[size];
        }
    };

    @Override
    public void run() {
        super.run();
        EasyWifi.scanWifi(this);
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mScanWifiTimeout);
        dest.writeLong(mSetWifiEnabledTimeout);
        dest.writeInt(mScanWifiWay);
        dest.writeInt((byte) (mIsAutoSwitchToThroughSystemWifi ? 1 : 0));
    }

    @Override
    public String toString() {
        return "ScanWifiTask{" +
                "mScanWifiTimeout=" + mScanWifiTimeout +
                ", mSetWifiEnabledTimeout=" + mSetWifiEnabledTimeout +
                ", mScanWifiWay=" + mScanWifiWay +
                ", mIsAutoSwitchToThroughSystemWifi=" + mIsAutoSwitchToThroughSystemWifi +
                ", mSingleTaskActivity=" + mSingleTaskActivity +
                '}';
    }

    public interface OnScanWifiCallback extends WifiTaskCallback {

        void onScanWifiPreparing();

        void onScanWifiPreparingNextStep(int nextStep);

        void onScanWifiStart();

        void onScanWifiSuccess();

        void onScanWifiFail(int scanWifiFailReason);

    }

}
