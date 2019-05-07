package com.likang.easywifi.lib.core.task;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.text.TextUtils;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.util.WifiUtils;

/**
 * @author likangren
 */
public class CheckIsAlreadyConnectedTask extends WifiTask {

    private String mSsid;
    private String mBssid;
    private ScanResult mScanResult;
    private boolean mIsAlreadyConnected;
    private GetConnectionInfoTask mGetConnectionInfoTask;

    public CheckIsAlreadyConnectedTask() {
    }


    public CheckIsAlreadyConnectedTask(String ssid, String bssid, WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
        mSsid = ssid;
        mBssid = bssid;
    }

    public CheckIsAlreadyConnectedTask(ScanResult scanResult, WifiTaskCallback wifiTaskCallback) {
        this(scanResult.SSID, scanResult.BSSID, wifiTaskCallback);
        mScanResult = scanResult;
    }

    private CheckIsAlreadyConnectedTask(Parcel in) {
        super(in);
        mSsid = in.readString();
        mBssid = in.readString();
        mScanResult = in.readParcelable(ScanResult.class.getClassLoader());
        mIsAlreadyConnected = in.readByte() == 1;
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

    public boolean getIsAlreadyConnected() {
        return mIsAlreadyConnected;
    }

    public void setIsAlreadyConnected(boolean isAlreadyConnected) {
        mIsAlreadyConnected = isAlreadyConnected;
    }

    @Override
    void checkParams() {
        if (TextUtils.isEmpty(mSsid)) {
            throw new IllegalArgumentException("Ssid can not be empty!");
        }
    }

    @Override
    public void run() {
        super.run();
        mGetConnectionInfoTask = new GetConnectionInfoTask(new WifiTaskCallback<GetConnectionInfoTask>() {
            @Override
            public void onTaskStartRun(GetConnectionInfoTask wifiTask) {
                callOnTaskStartRun();
            }

            @Override
            public void onTaskRunningCurrentStep(GetConnectionInfoTask wifiTask) {
                callOnTaskRunningCurrentStep(wifiTask.mRunningCurrentStep);
            }

            @Override
            public void onTaskSuccess(GetConnectionInfoTask wifiTask) {
                if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_PREPARE_SUCCESS)) {
                    return;
                }
                mIsAlreadyConnected = WifiUtils.isAlreadyConnected(mSsid, mBssid, EasyWifi.getWifiManager());
                callOnTaskSuccess();
            }

            @Override
            public void onTaskFail(GetConnectionInfoTask wifiTask) {
                callOnTaskFail(wifiTask.getFailReason());
            }

            @Override
            public void onTaskCancel(GetConnectionInfoTask wifiTask) {

            }
        });
        EasyWifi.executeTask(mGetConnectionInfoTask);
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        EasyWifi.cancelTask(mGetConnectionInfoTask);
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSsid);
        dest.writeString(mBssid);
        dest.writeParcelable(mScanResult, flags);
        dest.writeByte((byte) (mIsAlreadyConnected ? 1 : 0));
    }

    @Override
    public String toString() {
        return "CheckIsAlreadyConnectedTask{" +
                "mSsid='" + mSsid + '\'' +
                ", mBssid='" + mBssid + '\'' +
                ", mScanResult=" + mScanResult +
                ", mIsAlreadyConnected=" + mIsAlreadyConnected +
                ", mGetConnectionInfoTask=" + mGetConnectionInfoTask +
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mLastRunningCurrentStep=" + mLastRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                ", mIsResumeTask=" + mIsResumeTask +
                '}';
    }
}
