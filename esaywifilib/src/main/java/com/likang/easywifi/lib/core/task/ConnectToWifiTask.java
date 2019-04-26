package com.likang.easywifi.lib.core.task;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.util.WifiEncryptionScheme;

/**
 * @author likangren
 */
public class ConnectToWifiTask extends WifiTask<ConnectToWifiTask.OnConnectToWifiCallback> {

    private long mSetWifiEnabledTimeout;
    private String mSsid;
    private String mBssid;
    private String mPassword;
    private String mEncryptionScheme;
    private long mConnectToWifiTimeout;
    private WifiConfiguration mWifiConfiguration;

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             String ssid,
                             String bssid,
                             String password,
                             String encryptionScheme,
                             long connectToWifiTimeout,
                             ConnectToWifiTask.OnConnectToWifiCallback onConnectToWifiCallback) {
        super(onConnectToWifiCallback);
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        mEncryptionScheme = encryptionScheme;
        mConnectToWifiTimeout = connectToWifiTimeout;
    }

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             ScanResult scanResult,
                             String password,
                             long connectToWifiTimeout,
                             ConnectToWifiTask.OnConnectToWifiCallback onConnectToWifiCallback) {
        this(setWifiEnabledTimeout,
                scanResult.SSID,
                scanResult.BSSID,
                password,
                WifiEncryptionScheme.getEncryptionSchemeByScanResult(scanResult),
                connectToWifiTimeout,
                onConnectToWifiCallback);
    }

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             long connectToWifiTimeout,
                             WifiConfiguration wifiConfiguration,
                             ConnectToWifiTask.OnConnectToWifiCallback onConnectToWifiCallback) {
        super(onConnectToWifiCallback);
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
        mSsid = wifiConfiguration.SSID;
        mBssid = wifiConfiguration.BSSID;
        mConnectToWifiTimeout = connectToWifiTimeout;
        mWifiConfiguration = wifiConfiguration;
    }


    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             String ssid,
                             String bssid,
                             long connectToWifiTimeout,
                             ConnectToWifiTask.OnConnectToWifiCallback onConnectToWifiCallback) {
        this(setWifiEnabledTimeout, connectToWifiTimeout, EasyWifi.getConfiguredWifiConfiguration(ssid, bssid), onConnectToWifiCallback);
    }

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             ScanResult scanResult,
                             long connectToWifiTimeout,
                             ConnectToWifiTask.OnConnectToWifiCallback onConnectToWifiCallback) {
        this(setWifiEnabledTimeout,
                scanResult.SSID,
                scanResult.BSSID,
                connectToWifiTimeout,
                onConnectToWifiCallback);
    }

    protected ConnectToWifiTask(Parcel in) {
        mSetWifiEnabledTimeout = in.readLong();
        mSsid = in.readString();
        mBssid = in.readString();
        mPassword = in.readString();
        mEncryptionScheme = in.readString();
        mConnectToWifiTimeout = in.readLong();
        mWifiConfiguration = in.readParcelable(WifiConfiguration.class.getClassLoader());
    }

    public long getSetWifiEnabledTimeout() {
        return mSetWifiEnabledTimeout;
    }

    public void setSetWifiEnabledTimeout(long setWifiEnabledTimeout) {
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
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

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getEncryptionScheme() {
        return mEncryptionScheme;
    }

    public void setEncryptionScheme(String encryptionScheme) {
        mEncryptionScheme = encryptionScheme;
    }

    public long getConnectToWifiTimeout() {
        return mConnectToWifiTimeout;
    }

    public void setConnectToWifiTimeout(long connectToWifiTimeout) {
        mConnectToWifiTimeout = connectToWifiTimeout;
    }

    public WifiConfiguration getWifiConfiguration() {
        return mWifiConfiguration;
    }

    public void setWifiConfiguration(WifiConfiguration wifiConfiguration) {
        mWifiConfiguration = wifiConfiguration;
    }

    public static final Creator<ConnectToWifiTask> CREATOR = new Creator<ConnectToWifiTask>() {
        @Override
        public ConnectToWifiTask createFromParcel(Parcel in) {
            return new ConnectToWifiTask(in);
        }

        @Override
        public ConnectToWifiTask[] newArray(int size) {
            return new ConnectToWifiTask[size];
        }
    };

    @Override
    public void run() {
        super.run();
        if (mWifiConfiguration != null) {
            EasyWifi.connectToConfiguredWifi(this);
        } else {
            EasyWifi.connectToUnConfiguredWifi(this);
        }
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
        dest.writeLong(mSetWifiEnabledTimeout);
        dest.writeString(mSsid);
        dest.writeString(mBssid);
        dest.writeString(mPassword);
        dest.writeString(mEncryptionScheme);
        dest.writeLong(mConnectToWifiTimeout);
        dest.writeParcelable(mWifiConfiguration, flags);
    }

    @Override
    public String toString() {
        return "ConnectToWifiTask{" +
                "mSetWifiEnabledTimeout=" + mSetWifiEnabledTimeout +
                ", mSsid='" + mSsid + '\'' +
                ", mBssid='" + mBssid + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mEncryptionScheme='" + mEncryptionScheme + '\'' +
                ", mConnectToWifiTimeout=" + mConnectToWifiTimeout +
                ", mWifiConfiguration=" + mWifiConfiguration +
                '}';
    }

    public interface OnConnectToWifiCallback extends WifiTaskCallback {

        void onConnectToWifiPreparing();

        void onConnectToWifiPreparingNextStep(int nextStep);

        void onConnectToWifiStart();

        void onConnectToWifiConnecting(int connectingDetail);

        void onConnectToWifiSuccess();

        void onConnectToWifiFail(int connectToWifiFailReason);

    }


}
