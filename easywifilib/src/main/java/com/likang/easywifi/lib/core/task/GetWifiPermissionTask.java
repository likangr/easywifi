package com.likang.easywifi.lib.core.task;

import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public class GetWifiPermissionTask extends WifiTask {


    public GetWifiPermissionTask(WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
    }

    private GetWifiPermissionTask(Parcel in) {
        super(in);
    }


    @Override
    void checkParams() {

    }

    @Override
    public void run() {
        super.run();

        EasyWifi.getWifiPermission(this);
    }


    public static final Creator<GetWifiPermissionTask> CREATOR = new Creator<GetWifiPermissionTask>() {
        @Override
        public GetWifiPermissionTask createFromParcel(Parcel in) {
            return new GetWifiPermissionTask(in);
        }

        @Override
        public GetWifiPermissionTask[] newArray(int size) {
            return new GetWifiPermissionTask[size];
        }
    };

    @Override
    public String toString() {
        return "GetWifiPermissionTask{" +
                "mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                '}';
    }
}
