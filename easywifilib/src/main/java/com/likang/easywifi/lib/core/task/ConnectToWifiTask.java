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

    private String mSsid;
    private String mBssid;
    private String mPassword;
    private String mEncryptionScheme;
    private long mConnectToWifiTimeout;
    private WifiConfiguration mWifiConfiguration;
    private boolean mIsConnectToConfiguredWifi;
    private boolean mIsNeedUpdateWifiConfiguration;


    public ConnectToWifiTask(
            String ssid,
            String bssid,
            String password,
            String encryptionScheme,
            long connectToWifiTimeout,
            WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
        mIsConnectToConfiguredWifi = false;
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        mEncryptionScheme = encryptionScheme;
        mConnectToWifiTimeout = connectToWifiTimeout;
    }

    public ConnectToWifiTask(ScanResult scanResult,
                             String password,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {
        this(scanResult.SSID,
                scanResult.BSSID,
                password,
                WifiEncryptionScheme.getEncryptionSchemeByScanResult(scanResult),
                connectToWifiTimeout,
                wifiTaskCallback);
    }

    public ConnectToWifiTask(String ssid,
                             String bssid,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {
        this(ssid,
                bssid,
                null,
                WifiEncryptionScheme.ENCRYPTION_SCHEME_NONE,
                connectToWifiTimeout,
                wifiTaskCallback);
    }

    public ConnectToWifiTask(ScanResult scanResult,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {
        this(scanResult,
                null,
                connectToWifiTimeout,
                wifiTaskCallback);
    }

    public ConnectToWifiTask(WifiConfiguration wifiConfiguration,
                             boolean isNeedUpdateWifiConfiguration,
                             String password,
                             String encryptionScheme,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {

        super(wifiTaskCallback);
        mIsConnectToConfiguredWifi = true;
        mSsid = wifiConfiguration.SSID;
        mBssid = wifiConfiguration.BSSID;
        mIsNeedUpdateWifiConfiguration = isNeedUpdateWifiConfiguration;
        mPassword = password;
        mEncryptionScheme = encryptionScheme;
        mConnectToWifiTimeout = connectToWifiTimeout;
        mWifiConfiguration = wifiConfiguration;
    }

    public ConnectToWifiTask(WifiConfiguration wifiConfiguration,
                             long connectToWifiTimeout,
                             WifiTaskCallback wifiTaskCallback) {

        this(wifiConfiguration, false, null, null, connectToWifiTimeout, wifiTaskCallback);
    }


    private ConnectToWifiTask(Parcel in) {
        super(in);
        mSsid = in.readString();
        mBssid = in.readString();
        mPassword = in.readString();
        mEncryptionScheme = in.readString();
        mConnectToWifiTimeout = in.readLong();
        mWifiConfiguration = in.readParcelable(WifiConfiguration.class.getClassLoader());
        mIsConnectToConfiguredWifi = in.readByte() == 1;
        mIsNeedUpdateWifiConfiguration = in.readByte() == 1;
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

    public boolean isConnectToConfiguredWifi() {
        return mIsConnectToConfiguredWifi;
    }

    public void setIsConnectToConfiguredWifi(boolean isConnectToConfiguredWifi) {
        mIsConnectToConfiguredWifi = isConnectToConfiguredWifi;
    }

    public boolean isNeedUpdateWifiConfiguration() {
        return mIsNeedUpdateWifiConfiguration;
    }

    public void setIsNeedUpdateWifiConfiguration(boolean isNeedUpdateWifiConfiguration) {
        mIsNeedUpdateWifiConfiguration = isNeedUpdateWifiConfiguration;
    }

    @Override
    void checkParams() {
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

        }

        if (!mIsConnectToConfiguredWifi || mIsNeedUpdateWifiConfiguration) {

            if (TextUtils.isEmpty(mSsid)) {
                throw new IllegalArgumentException("Ssid can not be empty!");
            }

            if (TextUtils.isEmpty(mEncryptionScheme)) {
                throw new IllegalArgumentException("EncryptionScheme can not be empty!");
            }

            if (!WifiEncryptionScheme.ENCRYPTION_SCHEME_NONE.equals(mEncryptionScheme) && TextUtils.isEmpty(mPassword)) {
                throw new IllegalArgumentException("Password can not be empty!");
            }
        }
    }

    @Override
    public void run() {
        super.run();
        EasyWifi.connectToWifi(this);
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
        dest.writeString(mSsid);
        dest.writeString(mBssid);
        dest.writeString(mPassword);
        dest.writeString(mEncryptionScheme);
        dest.writeLong(mConnectToWifiTimeout);
        dest.writeParcelable(mWifiConfiguration, flags);
        dest.writeByte((byte) (mIsConnectToConfiguredWifi ? 1 : 0));
        dest.writeByte((byte) (mIsNeedUpdateWifiConfiguration ? 1 : 0));
    }

    @Override
    public String toString() {
        return "ConnectToWifiTask{" +
                ", mSsid='" + mSsid + '\'' +
                ", mBssid='" + mBssid + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mEncryptionScheme='" + mEncryptionScheme + '\'' +
                ", mConnectToWifiTimeout=" + mConnectToWifiTimeout +
                ", mWifiConfiguration=" + mWifiConfiguration +
                ", mIsConnectToConfiguredWifi=" + mIsConnectToConfiguredWifi +
                ", mIsNeedUpdateWifiConfiguration=" + mIsNeedUpdateWifiConfiguration +
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                '}';
    }

}
