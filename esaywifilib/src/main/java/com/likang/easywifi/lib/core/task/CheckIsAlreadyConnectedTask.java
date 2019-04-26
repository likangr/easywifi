package com.likang.easywifi.lib.core.task;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.util.WifiUtils;

/**
 * @author likangren
 */
public class CheckIsAlreadyConnectedTask extends WifiTask<CheckIsAlreadyConnectedTask.OnCheckIsAlreadyConnectedCallback> {

    private String mSsid;
    private String mBssid;
    private ScanResult mScanResult;
    private GetConnectionInfoTask mGetConnectionInfoTask;

    public CheckIsAlreadyConnectedTask(String ssid, String bssid, OnCheckIsAlreadyConnectedCallback onCheckIsAlreadyConnectedCallbackCallback) {
        super(onCheckIsAlreadyConnectedCallbackCallback);
        mSsid = ssid;
        mBssid = bssid;
    }

    public CheckIsAlreadyConnectedTask(ScanResult scanResult, OnCheckIsAlreadyConnectedCallback onCheckIsAlreadyConnectedCallbackCallback) {
        this(scanResult.SSID, scanResult.BSSID, onCheckIsAlreadyConnectedCallbackCallback);
        mScanResult = scanResult;
    }

    protected CheckIsAlreadyConnectedTask(Parcel in) {
        mSsid = in.readString();
        mBssid = in.readString();
        mScanResult = in.readParcelable(ScanResult.class.getClassLoader());
    }


    public String getSsid() {
        return mSsid;
    }

    public void setSsid(String ssid) {
        mSsid = ssid;
    }

    public String getBssid() {
        return mBssid;
    }

    public void setBssid(String bssid) {
        mBssid = bssid;
    }

    public ScanResult getScanResult() {
        return mScanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        mScanResult = scanResult;
    }

    public GetConnectionInfoTask getGetConnectionInfoTask() {
        return mGetConnectionInfoTask;
    }

    public void setGetConnectionInfoTask(GetConnectionInfoTask getConnectionInfoTask) {
        mGetConnectionInfoTask = getConnectionInfoTask;
    }

    public static final Creator<CheckIsAlreadyConnectedTask> CREATOR = new Creator<CheckIsAlreadyConnectedTask>() {
        @Override
        public CheckIsAlreadyConnectedTask createFromParcel(Parcel in) {
            return new CheckIsAlreadyConnectedTask(in);
        }

        @Override
        public CheckIsAlreadyConnectedTask[] newArray(int size) {
            return new CheckIsAlreadyConnectedTask[size];
        }
    };


    @Override
    public void run() {
        super.run();

        mGetConnectionInfoTask = new GetConnectionInfoTask(new GetConnectionInfoTask.OnGetConnectionInfoCallback() {
            @Override
            public void onGetConnectionInfoPreparing() {
                if (mWifiTaskCallback != null) {
                    mWifiTaskCallback.onCheckIsAlreadyConnectedCallbackPreparing();
                }
            }

            @Override
            public void onGetConnectionInfoPreparingNextStep(int nextStep) {
                if (mWifiTaskCallback != null) {
                    mWifiTaskCallback.onCheckIsAlreadyConnectedCallbackPreparingNextStep(nextStep);
                }
            }

            @Override
            public void onGetConnectionInfoSuccess(WifiInfo wifiInfo) {
                if (mWifiTaskCallback != null) {
                    if (mScanResult == null) {
                        mWifiTaskCallback.onCheckIsAlreadyConnectedCallbackSuccess(WifiUtils.isAlreadyConnected(mSsid, mBssid, EasyWifi.getWifiManager()), mSsid, mBssid);
                    } else {
                        mWifiTaskCallback.onCheckIsAlreadyConnectedCallbackSuccess(WifiUtils.isAlreadyConnected(mSsid, mBssid, EasyWifi.getWifiManager()), mScanResult);
                    }
                }
            }

            @Override
            public void onGetConnectionInfoFail(int getConnectionFailReason) {
                if (mWifiTaskCallback != null) {
                    mWifiTaskCallback.onCheckIsAlreadyConnectedCallbackFail(getConnectionFailReason);
                }
            }
        });
        EasyWifi.executeTask(mGetConnectionInfoTask);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSsid);
        dest.writeString(mBssid);
        dest.writeParcelable(mScanResult, flags);
    }

    @Override
    public String toString() {
        return "CheckIsAlreadyConnectedTask{" +
                "mSsid='" + mSsid + '\'' +
                ", mBssid='" + mBssid + '\'' +
                ", mScanResult=" + mScanResult +
                '}';
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    public interface OnCheckIsAlreadyConnectedCallback extends WifiTaskCallback {

        void onCheckIsAlreadyConnectedCallbackPreparing();

        void onCheckIsAlreadyConnectedCallbackPreparingNextStep(int nextStep);

        void onCheckIsAlreadyConnectedCallbackSuccess(boolean isAlreadyConnected, String ssid, String bssid);

        void onCheckIsAlreadyConnectedCallbackSuccess(boolean isAlreadyConnected, ScanResult scanResult);

        void onCheckIsAlreadyConnectedCallbackFail(int getConnectionFailReason);

    }

}
