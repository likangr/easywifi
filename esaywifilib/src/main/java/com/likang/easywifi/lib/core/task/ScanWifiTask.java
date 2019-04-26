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
public class ScanWifiTask extends WifiTask {

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
                        WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
        mScanWifiTimeout = scanWifiTimeout;
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
        mScanWifiWay = scanWifiWay;
        mIsAutoSwitchToThroughSystemWifi = isAutoSwitchToThroughSystemWifi;
        mSingleTaskActivity = singleTaskActivity;

    }


    protected ScanWifiTask(Parcel in) {
        super(in);
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


    @Override
    void checkParams() {

        if (mSetWifiEnabledTimeout < 0) {
            throw new IllegalArgumentException("SetWifiEnabledTimeout must more than 0!");
        }
        if (mScanWifiTimeout < 0) {
            throw new IllegalArgumentException("ScanWifiTimeout must more than 0!");
        }

        if (mScanWifiWay != EasyWifi.SCAN_WIFI_WAY_THROUGH_WIFI_SETTING && mScanWifiWay != EasyWifi.SCAN_WIFI_WAY_INITIATIVE) {
            throw new IllegalArgumentException("ScanWifiWay must be one of EasyWifi.SCAN_WIFI_WAY_THROUGH_WIFI_SETTING or EasyWifi.SCAN_WIFI_WAY_INITIATIVE");
        }

        if (mScanWifiWay == EasyWifi.SCAN_WIFI_WAY_THROUGH_WIFI_SETTING || mIsAutoSwitchToThroughSystemWifi) {
            if (mSingleTaskActivity == null) {
                throw new IllegalArgumentException("SingleTaskActivity can't be null!");
            }

            int launchMode = -1;
            try {
                Application application = ApplicationHolder.getApplication();
                ComponentName componentName = new ComponentName(application, mSingleTaskActivity.getClass());
                launchMode = application.getPackageManager().getActivityInfo(componentName, PackageManager.GET_META_DATA).launchMode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (launchMode != ActivityInfo.LAUNCH_SINGLE_TASK) {
                throw new IllegalArgumentException("SingleTaskActivity's launch mode must be single task!");
            }
        }
    }

    @Override
    public void run() {
        super.run();
        EasyWifi.scanWifi(this);
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
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
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
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                '}';
    }
}
