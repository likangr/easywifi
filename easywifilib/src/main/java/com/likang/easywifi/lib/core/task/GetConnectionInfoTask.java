package com.likang.easywifi.lib.core.task;

import android.net.wifi.WifiInfo;
import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public class GetConnectionInfoTask extends SpecificWifiTask {

    private WifiInfo mWifiInfo;

    public GetConnectionInfoTask() {
    }

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
    void onEnvironmentPrepared() {
        mWifiInfo = EasyWifi.getWifiManager().getConnectionInfo();
        callOnTaskSuccess();
    }

    @Override
    void initPrepareEnvironment(PrepareEnvironmentTask prepareEnvironmentTask) {
        if (PrepareEnvironmentTask.isAboveLollipopMr1()) {
            prepareEnvironmentTask.setIsNeedLocationPermission(true);
            prepareEnvironmentTask.setIsNeedEnableLocation(true);
        }
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
                ", mLastRunningCurrentStep=" + mLastRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                ", mIsResumeTask=" + mIsResumeTask +
                '}';
    }
}
