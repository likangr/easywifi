package com.likang.easywifi.lib.core.task;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Parcel;
import android.text.TextUtils;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.util.WifiEncryptionScheme;

/**
 * @author likangren
 */
public class ConnectToWifiTask extends WifiTask {

    private long mSetWifiEnabledTimeout;
    private String mSsid;
    private String mBssid;
    private String mPassword;
    private String mEncryptionScheme;
    private long mConnectToWifiTimeout;
    private WifiConfiguration mWifiConfiguration;
    private boolean mIsConnectToConfiguredWifi;


    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             String ssid,
                             String bssid,
                             String password,
                             String encryptionScheme,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
        mIsConnectToConfiguredWifi = false;
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        mEncryptionScheme = encryptionScheme;
        mConnectToWifiTimeout = connectToWifiTimeout;
    }

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             String ssid,
                             String bssid,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {
        this(setWifiEnabledTimeout,
                ssid,
                bssid,
                null,
                WifiEncryptionScheme.ENCRYPTION_SCHEME_NONE,
                connectToWifiTimeout,
                wifiTaskCallback);
    }

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             ScanResult scanResult,
                             String password,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {
        this(setWifiEnabledTimeout,
                scanResult.SSID,
                scanResult.BSSID,
                password,
                WifiEncryptionScheme.getEncryptionSchemeByScanResult(scanResult),
                connectToWifiTimeout,
                wifiTaskCallback);
    }

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             ScanResult scanResult,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {
        this(setWifiEnabledTimeout,
                scanResult,
                null,
                connectToWifiTimeout,
                wifiTaskCallback);
    }

    public ConnectToWifiTask(long setWifiEnabledTimeout,
                             WifiConfiguration wifiConfiguration,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {

        super(wifiTaskCallback);
        mIsConnectToConfiguredWifi = true;
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
        mSsid = wifiConfiguration.SSID;
        mBssid = wifiConfiguration.BSSID;
        mConnectToWifiTimeout = connectToWifiTimeout;
        mWifiConfiguration = wifiConfiguration;
    }


    protected ConnectToWifiTask(Parcel in) {
        super(in);
        mSetWifiEnabledTimeout = in.readLong();
        mSsid = in.readString();
        mBssid = in.readString();
        mPassword = in.readString();
        mEncryptionScheme = in.readString();
        mConnectToWifiTimeout = in.readLong();
        mWifiConfiguration = in.readParcelable(WifiConfiguration.class.getClassLoader());
        mIsConnectToConfiguredWifi = in.readByte() == 1;
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


    @Override
    void checkParams() {

        if (mSetWifiEnabledTimeout < 0) {
            throw new IllegalArgumentException("SetWifiEnabledTimeout must more than 0!");
        }
        if (mConnectToWifiTimeout < 0) {
            throw new IllegalArgumentException("ConnectToWifiTimeout must more than 0!");
        }

        if (mIsConnectToConfiguredWifi) {
            if (mWifiConfiguration == null) {
                throw new IllegalArgumentException("WifiConfiguration can not be null!");
            }

            if (mWifiConfiguration.networkId == -1) {
                throw new IllegalArgumentException("WifiConfiguration is have not been configured!");
            }

        } else {
            if (TextUtils.isEmpty(mSsid)) {
                throw new IllegalArgumentException("Ssid can not be empty!");
            }

            if (!WifiEncryptionScheme.ENCRYPTION_SCHEME_NONE.equals(mEncryptionScheme) && TextUtils.isEmpty(mPassword)) {
                throw new IllegalArgumentException("Password can not be empty!");
            }

            if (TextUtils.isEmpty(mEncryptionScheme)) {
                throw new IllegalArgumentException("mEncryptionScheme can not be empty!");
            }
        }
    }

    @Override
    public void run() {
        super.run();
        if (mIsConnectToConfiguredWifi) {
            EasyWifi.connectToConfiguredWifi(this);
        } else {
            EasyWifi.connectToUnConfiguredWifi(this);
        }
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
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(mSetWifiEnabledTimeout);
        dest.writeString(mSsid);
        dest.writeString(mBssid);
        dest.writeString(mPassword);
        dest.writeString(mEncryptionScheme);
        dest.writeLong(mConnectToWifiTimeout);
        dest.writeParcelable(mWifiConfiguration, flags);
        dest.writeByte((byte) (mIsConnectToConfiguredWifi ? 1 : 0));
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
                ", mIsConnectToConfiguredWifi=" + mIsConnectToConfiguredWifi +
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                '}';
    }

}
