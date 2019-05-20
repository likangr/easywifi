package com.likangr.easywifi.lib;

import android.app.Application;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.likangr.easywifi.lib.core.task.WifiTask;
import com.likangr.easywifi.lib.util.ApplicationHolder;
import com.likangr.easywifi.lib.util.StringUtils;
import com.likangr.easywifi.lib.util.WifiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * functions:
 * 1.Request all permissions wifi task need.
 * 2.Enable wifi;
 * 3.Disable wifi;
 * 4.Scan and get the wifi list in the environment;
 * 5.Connect to any wifi;
 * 6.Get current connected wifi info;
 *
 * @author likangren
 */
public final class EasyWifi {

    private static final String TAG = "EasyWifi";

    private static WifiManager sWifiManager;
    private static boolean sIsInitialised = false;
    private static Handler sHandler;
    private static final Object sInitialiseLock = new Object();
    private static final Object sCurWifiTaskListLock = new Object();

    private static final ArrayList<WifiTask> CUR_WIFI_TASKS = new ArrayList<>();

    public static final int FAIL_REASON_SET_LOCATION_ENABLED_USER_REJECT = 1;
    public static final int FAIL_REASON_LOCATION_MODULE_NOT_EXIST = 2;
    public static final int FAIL_REASON_NOT_HAS_LOCATION_PERMISSION = 3;

    public static final int FAIL_REASON_WIFI_MODULE_NOT_EXIST = 4;
    public static final int FAIL_REASON_NOT_HAS_WIFI_PERMISSION = 5;
    public static final int FAIL_REASON_SET_WIFI_ENABLED_TIMEOUT = 7;

    public static final int FAIL_REASON_CONNECT_TO_WIFI_REQUEST_NOT_BE_SATISFIED = 8;
    public static final int FAIL_REASON_CONNECT_TO_WIFI_ERROR_AUTHENTICATING = 9;
    public static final int FAIL_REASON_CONNECT_TO_WIFI_NOT_OBTAINED_IP_ADDR = 10;
    public static final int FAIL_REASON_CONNECT_TO_WIFI_IS_POOR_LINK = 11;
    public static final int FAIL_REASON_CONNECT_TO_WIFI_TIMEOUT = 12;
    public static final int FAIL_REASON_CONNECT_TO_WIFI_UNKNOWN = 13;
    public static final int FAIL_REASON_CONNECT_TO_WIFI_ARGUMENTS_ERROR = 14;
    public static final int FAIL_REASON_CONNECT_TO_WIFI_MUST_THROUGH_SYSTEM_WIFI_SETTING = 15;

    public static final int FAIL_REASON_SCAN_WIFI_REQUEST_NOT_BE_SATISFIED = 16;
    public static final int FAIL_REASON_SCAN_WIFI_TIMEOUT = 17;
    public static final int FAIL_REASON_SCAN_WIFI_UNKNOWN = 18;

    public static final int FAIL_REASON_NOT_HAS_WIFI_AND_LOCATION_PERMISSION = 19;

    public static final int CURRENT_STEP_CHECK_LOCATION_MODULE_IS_EXIST = 1;
    public static final int CURRENT_STEP_CHECK_LOCATION_ENABLED = 2;
    public static final int CURRENT_STEP_SET_LOCATION_ENABLED = 3;
    public static final int CURRENT_STEP_CHECK_LOCATION_PERMISSION = 4;
    public static final int CURRENT_STEP_REQUEST_LOCATION_PERMISSION = 5;

    public static final int CURRENT_STEP_CHECK_WIFI_MODULE_IS_EXIST = 6;
    public static final int CURRENT_STEP_CHECK_WIFI_ENABLED = 7;
    public static final int CURRENT_STEP_CHECK_WIFI_PERMISSION = 8;
    public static final int CURRENT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION = 9;
    public static final int CURRENT_STEP_SET_WIFI_ENABLED = 10;

    public static final int CURRENT_STEP_CHECK_WIFI_AND_LOCATION_PERMISSION = 11;
    public static final int CURRENT_STEP_GUIDE_USER_GRANT_WIFI_AND_LOCATION_PERMISSION = 12;

    public static final int CURRENT_STEP_PREPARE_SUCCESS = 13;

    public static final int CURRENT_STEP_AUTHENTICATING = 14;
    public static final int CURRENT_STEP_OBTAINING_IP_ADDR = 15;
    public static final int CURRENT_STEP_VERIFYING_POOR_LINK = 16;
    public static final int CURRENT_STEP_CAPTIVE_PORTAL_CHECK = 17;

    public static final int SCAN_WIFI_WAY_INITIATIVE = 1;
    public static final int SCAN_WIFI_WAY_THROUGH_WIFI_SETTING = 2;

    public static final int TIME_OUT_SET_WIFI_ENABLED_5S = 5000;
    public static final int TIME_OUT_SET_WIFI_ENABLED_7S = 7000;
    public static final int TIME_OUT_SET_WIFI_ENABLED_10S = 10000;
    public static final int TIME_OUT_SET_WIFI_ENABLED_DEFAULT = TIME_OUT_SET_WIFI_ENABLED_5S;

    public static final int TIME_OUT_SCAN_WIFI_5S = 5000;
    public static final int TIME_OUT_SCAN_WIFI_10S = 10000;
    public static final int TIME_OUT_SCAN_WIFI_15S = 15000;
    public static final int TIME_OUT_SCAN_WIFI_DEFAULT = TIME_OUT_SCAN_WIFI_15S;

    public static final int TIME_OUT_CONNECT_TO_WIFI_5S = 5000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_10S = 10000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_15S = 15000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_20S = 20000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_DEFAULT = TIME_OUT_CONNECT_TO_WIFI_20S;

    /*****open aip****/

    /**
     * @param application
     */
    public static void initCore(Application application) {
        synchronized (sInitialiseLock) {
            if (sIsInitialised) {
                return;
            }
            ApplicationHolder.init(application);
            sWifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
            sHandler = new Handler(Looper.getMainLooper());
            sIsInitialised = true;
        }
    }

    /**
     * @return
     */
    public static WifiManager getWifiManager() {
        checkIsInitialised();
        return sWifiManager;
    }

    /**
     * @return
     */
    public static Handler getHandler() {
        checkIsInitialised();
        return sHandler;
    }

    /**
     * @return
     */
    public static List<ScanResult> getScanResults() {
        checkIsInitialised();
        if (sWifiManager == null) {
            return Collections.emptyList();
        }

        List<ScanResult> scanResults = sWifiManager.getScanResults();
        WifiUtils.filterScanResult(scanResults);
        return scanResults;
    }

    /**
     * @return
     */
    public static List<WifiConfiguration> getConfiguredNetworks() {
        checkIsInitialised();
        if (sWifiManager == null) {
            return Collections.emptyList();
        }
        return sWifiManager.getConfiguredNetworks();
    }

    /**
     * @return
     */
    public static boolean isWifiEnabled() {
        checkIsInitialised();
        if (sWifiManager == null) {
            return false;
        }
        return sWifiManager.isWifiEnabled();
    }

    /**
     * @param scanResult
     * @return
     */
    public static WifiConfiguration getConfiguredWifiConfiguration(ScanResult scanResult) {
        return getConfiguredWifiConfiguration(scanResult.SSID, scanResult.BSSID);
    }

    /**
     * @param wifiInfo
     * @return
     */
    public static WifiConfiguration getConfiguredWifiConfiguration(WifiInfo wifiInfo) {
        return getConfiguredWifiConfiguration(wifiInfo.getSSID(), wifiInfo.getBSSID());
    }


    /**
     * @param ssid
     * @param bssid
     * @return
     */
    public static WifiConfiguration getConfiguredWifiConfiguration(String ssid, String bssid) {
        checkIsInitialised();
        if (sWifiManager == null) {
            return null;
        }

        boolean bssidValid = !TextUtils.isEmpty(bssid);
        String enclosedInDoubleQuotationMarksSsid = StringUtils.enclosedInDoubleQuotationMarks(ssid);

        WifiConfiguration configuredWifiConfiguration = null;
        List<WifiConfiguration> wifiConfigurations = getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : wifiConfigurations) {
            if (wifiConfiguration.SSID.equals(enclosedInDoubleQuotationMarksSsid)) {
                //fixme xiaomi bssid is "any"
//                if (bssidValid && !TextUtils.isEmpty(wifiConfiguration.BSSID) &&
//                        !wifiConfiguration.BSSID.equals(bssid)) {
//                    continue;
//                }
                configuredWifiConfiguration = wifiConfiguration;
            }
        }
        return configuredWifiConfiguration;
    }


    /**
     * @param wifiTask
     */
    public static void executeTask(WifiTask wifiTask) {
        checkIsInitialised();
        wifiTask.run();
    }

    /**
     * @param wifiTask
     */
    public static void cancelTask(WifiTask wifiTask) {
        checkIsInitialised();
        wifiTask.cancel();
    }


    /**
     *
     */
    public static void cancelAllTasks() {
        synchronized (sCurWifiTaskListLock) {
            Iterator<WifiTask> iterator = getUnmodifiableCurrentTasks().iterator();
            while (iterator.hasNext()) {
                iterator.next().cancel();
            }
        }
    }

    /**
     * @return
     */
    public static ArrayList<WifiTask> getUnmodifiableCurrentTasks() {
        synchronized (sCurWifiTaskListLock) {
            checkIsInitialised();
            return new ArrayList<>(CUR_WIFI_TASKS);
        }
    }


    /****internal****/
    /**
     * @param wifiTask
     */
    public static void removeTask(WifiTask wifiTask) {
        synchronized (sCurWifiTaskListLock) {
            CUR_WIFI_TASKS.remove(wifiTask);
        }
    }

    /**
     * @param wifiTask
     */
    public static void addTask(WifiTask wifiTask) {
        synchronized (sCurWifiTaskListLock) {
            CUR_WIFI_TASKS.add(wifiTask);
        }
    }

    /**
     *
     */
    private static void checkIsInitialised() {
        synchronized (sInitialiseLock) {
            if (!sIsInitialised) {
                throw new IllegalStateException("You must invoke initCore method first of all.");
            }
        }
    }

}