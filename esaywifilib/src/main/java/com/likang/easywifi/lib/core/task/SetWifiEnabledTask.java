package com.likang.easywifi.lib.core.task;

import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public class SetWifiEnabledTask extends WifiTask<SetWifiEnabledTask.OnSetWifiEnabledCallback> {

    private boolean mEnabled;
    private long mSetWifiEnabledTimeout;

    public SetWifiEnabledTask(boolean enabled,
                              long setWifiEnabledTimeout,
                              OnSetWifiEnabledCallback onSetWifiEnabledCallback) {
        mEnabled = enabled;
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
        mWifiTaskCallback = onSetWifiEnabledCallback;

    }

    protected SetWifiEnabledTask(Parcel in) {
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
    public void run() {
        super.run();

        EasyWifi.setWifiEnabled(this);

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
        dest.writeByte((byte) (mEnabled ? 1 : 0));
        dest.writeLong(mSetWifiEnabledTimeout);
    }


    @Override
    public String toString() {
        return "SetWifiEnabledTask{" +
                "mEnabled=" + mEnabled +
                ", mSetWifiEnabledTimeout=" + mSetWifiEnabledTimeout +
                '}';
    }

    public interface OnSetWifiEnabledCallback extends WifiTaskCallback {

        void onSetWifiEnabledPreparing(boolean enabled);

        void onSetWifiEnabledPreparingNextStep(boolean enabled, int nextStep);

        void onSetWifiEnabledStart(boolean enabled);

        void onSetWifiEnabledSuccess(boolean enabled);

        void onSetWifiEnabledFail(boolean enabled, int setWifiEnabledFailReason);
    }


}
