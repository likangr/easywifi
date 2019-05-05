package com.likang.easywifi.lib.core.task;

import android.net.wifi.WifiInfo;
import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public class GetConnectionInfoTask extends WifiTask {

    private WifiInfo mWifiInfo;

    public GetConnectionInfoTask(WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
    }

    private GetConnectionInfoTask(Parcel in) {
        super(in);
        in.readParcelable(WifiInfo.class.getClassLoader());
    }

    public WifiInfo getWifiInfo() {
        return mWifiInfo;
    }


    @Override
    void checkParams() {

    }

    @Override
    public void run() {
        super.run();
        EasyWifi.getConnectionInfo(this);
    }

    @Override
    public boolean callOnTaskSuccess() {
        mWifiInfo = EasyWifi.getWifiManager().getConnectionInfo();
        return super.callOnTaskSuccess();
    }

    public static final Creator<GetConnectionInfoTask> CREATOR = new Creator<GetConnectionInfoTask>() {
        @Override
        public GetConnectionInfoTask createFromParcel(Parcel in) {
            return new GetConnectionInfoTask(in);
        }

        @Override
        public GetConnectionInfoTask[] newArray(int size) {
            return new GetConnectionInfoTask[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(mWifiInfo, flags);
    }

    @Override
    public String toString() {
        return "GetConnectionInfoTask{" +
                "mWifiInfo=" + mWifiInfo +
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                '}';
    }
}
