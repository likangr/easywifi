package com.likang.easywifi.lib.core.task;

import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public class SetWifiEnabledTask extends WifiTask {

    private boolean mEnabled;
    private long mSetWifiEnabledTimeout;

    public SetWifiEnabledTask(boolean enabled,
                              long setWifiEnabledTimeout,
                              WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
        mEnabled = enabled;
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
    }

    private SetWifiEnabledTask(Parcel in) {
        super(in);
        mEnabled = in.readByte() == 1;
        mSetWifiEnabledTimeout = in.readLong();
    }


    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public long getSetWifiEnabledTimeout() {
        return mSetWifiEnabledTimeout;
    }

    public void setSetWifiEnabledTimeout(long setWifiEnabledTimeout) {
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
    }

    @Override
    void checkParams() {
        if (mSetWifiEnabledTimeout < 0) {
            throw new IllegalArgumentException("SetWifiEnabledTimeout must more than 0!");
        }
    }

    @Override
    public void run() {
        super.run();
        EasyWifi.setWifiEnabled(this);
    }

    public static final Creator<SetWifiEnabledTask> CREATOR = new Creator<SetWifiEnabledTask>() {
        @Override
        public SetWifiEnabledTask createFromParcel(Parcel in) {
            return new SetWifiEnabledTask(in);
        }

        @Override
        public SetWifiEnabledTask[] newArray(int size) {
            return new SetWifiEnabledTask[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (mEnabled ? 1 : 0));
        dest.writeLong(mSetWifiEnabledTimeout);
    }

    @Override
    public String toString() {
        return "SetWifiEnabledTask{" +
                "mEnabled=" + mEnabled +
                ", mSetWifiEnabledTimeout=" + mSetWifiEnabledTimeout +
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                '}';
    }
}
