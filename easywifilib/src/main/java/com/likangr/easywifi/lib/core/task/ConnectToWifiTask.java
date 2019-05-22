package com.likangr.easywifi.lib.core.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.text.TextUtils;

import com.likangr.easywifi.lib.EasyWifi;
import com.likangr.easywifi.lib.util.Logger;
import com.likangr.easywifi.lib.util.WifiEncryptionScheme;
import com.likangr.easywifi.lib.util.WifiUtils;

/**
 * @author likangren
 */
public final class ConnectToWifiTask extends SpecificWifiTask {

    private String mSsid;
    private String mBssid;
    private String mPassword;
    private String mEncryptionScheme;
    private long mConnectToWifiTimeout;
    private WifiConfiguration mWifiConfiguration;
    private boolean mIsConnectToConfiguredWifi;
    private boolean mIsNeedUpdateWifiConfiguration;

    public ConnectToWifiTask() {
    }

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

    public boolean isNeedUpdateWifiConfiguration() {
        return mIsNeedUpdateWifiConfiguration;
    }

    public void setIsNeedUpdateWifiConfiguration(boolean isNeedUpdateWifiConfiguration) {
        mIsNeedUpdateWifiConfiguration = isNeedUpdateWifiConfiguration;
    }

    @Override
    protected void checkParams() {
        if (mConnectToWifiTimeout < 0) {
            throw new IllegalArgumentException("ConnectToWifiTimeout must more than 0!");
        }

        if (mIsConnectToConfiguredWifi) {

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
    protected void onEnvironmentPrepared() {

        if (mIsConnectToConfiguredWifi && !mIsNeedUpdateWifiConfiguration) {

            if (WifiUtils.isAlreadyConnected(mWifiConfiguration.SSID, mWifiConfiguration.BSSID, EasyWifi.getWifiManager())) {
                callOnTaskSuccess();
                return;
            }

        } else {

            if (mIsNeedUpdateWifiConfiguration) {
                boolean removeNetworkResult = EasyWifi.getWifiManager().removeNetwork(mWifiConfiguration.networkId);
                if (!removeNetworkResult) {
                    callOnTaskFail(EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_MUST_THROUGH_SYSTEM_WIFI_SETTING);
                    return;
                }
            }

            WifiConfiguration wifiConfiguration = WifiUtils.addNetWork(EasyWifi.getWifiManager(),
                    mSsid, mBssid,
                    mPassword, mEncryptionScheme);

            if (wifiConfiguration.networkId == -1) {
                callOnTaskFail(EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_ARGUMENTS_ERROR);
                return;
            }
            mWifiConfiguration = wifiConfiguration;
        }

        //fixme android p (nokia x6) onConnectToWifiStart lock.
        boolean requestConnectToWifiResult = WifiUtils.connectToConfiguredWifi(EasyWifi.getWifiManager(), mWifiConfiguration.networkId);

        if (requestConnectToWifiResult) {
            //note: connect to wifi is not timely.

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

            registerAutoReleaseReceiver(new BroadcastReceiver() {

                boolean authenticatingIsReceived = false;
                boolean obtainingIpAddrIsReceived = false;
                boolean verifyingPoorLinkIsReceived = false;
                boolean captivePortalCheckIsReceived = false;

                @Override
                public void onReceive(Context context, Intent intent) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    NetworkInfo.DetailedState detailedState = networkInfo.getDetailedState();
                    Logger.d(TAG, "detailedState=" + detailedState);

                    switch (detailedState) {
                        //DISCONNECTED
                        case IDLE:

                            break;
                        case SCANNING:
                            break;

                        //CONNECTING
                        case CONNECTING:
                            break;


                        case AUTHENTICATING:
                            if (!authenticatingIsReceived) {
                                authenticatingIsReceived = true;
                                callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_AUTHENTICATING);
                            }
                            break;
                        case OBTAINING_IPADDR:
                            if (!obtainingIpAddrIsReceived) {
                                obtainingIpAddrIsReceived = true;
                                callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_OBTAINING_IP_ADDR);
                            }
                            break;
                        case VERIFYING_POOR_LINK:
                            if (!verifyingPoorLinkIsReceived) {
                                verifyingPoorLinkIsReceived = true;
                                //Temporary shutdown (network down)
                                callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_VERIFYING_POOR_LINK);
                            }
                            break;
                        case CAPTIVE_PORTAL_CHECK:
                            if (!captivePortalCheckIsReceived) {
                                captivePortalCheckIsReceived = true;
                                //Determine whether a browser login is required
                                callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CAPTIVE_PORTAL_CHECK);
                            }
                            break;

                        //CONNECTED
                        case CONNECTED:
                            if (obtainingIpAddrIsReceived) {
                                if (WifiUtils.isAlreadyConnected(mWifiConfiguration.SSID,
                                        mWifiConfiguration.BSSID, EasyWifi.getWifiManager())) {
                                    callOnTaskSuccess();
                                } else {
                                    callOnTaskFail(EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_UNKNOWN);
                                }
                            }
                            break;

                        //SUSPENDED
                        case SUSPENDED:
                            break;

                        //DISCONNECTING
                        case DISCONNECTING:
                            break;

                        //DISCONNECTED
                        case DISCONNECTED:
                            if (verifyingPoorLinkIsReceived) {
                                callOnTaskFail(EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_IS_POOR_LINK);
                                break;
                            }
                            if (obtainingIpAddrIsReceived) {
                                callOnTaskFail(EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_NOT_OBTAINED_IP_ADDR);
                                break;
                            }
                            if (authenticatingIsReceived) {
                                callOnTaskFail(EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_ERROR_AUTHENTICATING);
                            }
                            break;
                        case FAILED:
                            break;
                        case BLOCKED:
                            break;
                        default:
                            break;
                    }

                }

            }, intentFilter, mConnectToWifiTimeout, EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_TIMEOUT);

        } else {
            callOnTaskFail(EasyWifi.FAIL_REASON_CONNECT_TO_WIFI_REQUEST_NOT_BE_SATISFIED);
        }

    }

    @Override
    protected void initPrepareEnvironment(PrepareEnvironmentTask prepareEnvironmentTask) {
        if (PrepareEnvironmentTask.isAboveLollipopMr1()) {
            prepareEnvironmentTask.setIsNeedLocationPermission(true);
            prepareEnvironmentTask.setIsNeedEnableLocation(true);
        }
        if (!EasyWifi.isWifiEnabled()) {
            prepareEnvironmentTask.setIsNeedWifiPermission(true);
            prepareEnvironmentTask.setIsNeedSetWifiEnabled(true);
            prepareEnvironmentTask.setWifiEnabled(true);
            prepareEnvironmentTask.setSetWifiEnabledTimeout(EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT);
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
                "mSsid='" + mSsid + '\'' +
                ", mBssid='" + mBssid + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mEncryptionScheme='" + mEncryptionScheme + '\'' +
                ", mConnectToWifiTimeout=" + mConnectToWifiTimeout +
                ", mWifiConfiguration=" + mWifiConfiguration +
                ", mIsConnectToConfiguredWifi=" + mIsConnectToConfiguredWifi +
                ", mIsNeedUpdateWifiConfiguration=" + mIsNeedUpdateWifiConfiguration +
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mLastRunningCurrentStep=" + mLastRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                ", mIsResumeTask=" + mIsResumeTask +
                '}';
    }
}
