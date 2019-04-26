package com.likang.easywifi.lib.core.task;

import android.net.wifi.WifiInfo;
import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public class GetConnectionInfoTask extends WifiTask<GetConnectionInfoTask.OnGetConnectionInfoCallback> {


    public GetConnectionInfoTask(OnGetConnectionInfoCallback onGetConnectionInfoCallback) {
        super(onGetConnectionInfoCallback);

    }

    protected GetConnectionInfoTask(Parcel in) {
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
    public void run() {
        super.run();
        EasyWifi.getConnectionInfo(this);
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
    }

    @Override
    public String toString() {
        return "GetConnectionInfoTask{}";
    }

    public interface OnGetConnectionInfoCallback extends WifiTaskCallback {

        void onGetConnectionInfoPreparing();

        void onGetConnectionInfoPreparingNextStep(int nextStep);

        void onGetConnectionInfoSuccess(WifiInfo wifiInfo);

        void onGetConnectionInfoFail(int getConnectionFailReason);

    }

}
