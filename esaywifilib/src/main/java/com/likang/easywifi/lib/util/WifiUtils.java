package com.likang.easywifi.lib.util;

import android.app.Application;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author likangren
 */
public class WifiUtils {

    private static String TAG = "WifiUtils";


    public static boolean checkWifiModuleIsExist(WifiManager wifiManager) {
        if (wifiManager == null) {
            return false;
        }
        Application application = ApplicationHolder.getApplication();
        PackageManager packageManager = application.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    public static boolean checkHasChangeWifiStatePermission(WifiManager wifiManager) {
        return wifiManager.setWifiEnabled(wifiManager.isWifiEnabled());
    }


    public static boolean isAlreadyConnected(String ssid, String bssid, WifiManager wifiManager) {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo == null) {
            return false;
        }

        boolean ipAddrValid = connectionInfo.getIpAddress() != 0;

        if (!ipAddrValid) {
            return false;
        }

        if (!enclosedInDoubleQuotationMarks(ssid).equals(connectionInfo.getSSID())) {
            return false;
        }

        //fixme xiaomi bssid is 'any'
//        if (!TextUtils.isEmpty(bssid)) {
//            return bssid.equals(connectionInfo.getBSSID());
//        }

        return true;
    }


    public static boolean connectToConfiguredWifi(WifiManager wifiManager, int networkId) {
        return wifiManager.enableNetwork(networkId, true);
    }


    public static WifiConfiguration addNetWork(WifiManager wifiManager, String ssid, String bssid, String password, String encryptionScheme) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = enclosedInDoubleQuotationMarks(ssid);
        if (!TextUtils.isEmpty(bssid)) {
            wifiConfiguration.BSSID = bssid;
        }
        WifiEncryptionScheme.configEncryptionScheme(wifiConfiguration, encryptionScheme, password);
        wifiConfiguration.networkId = wifiManager.addNetwork(wifiConfiguration);
        return wifiConfiguration;
    }


    public static String enclosedInDoubleQuotationMarks(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }

        int lastPos = string.length() - 1;
        if ((string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
            return string;
        }

        return "\"" + string + "\"";
    }


    public static void filterScanResult(List<ScanResult> scanResults) {
        LinkedHashMap<String, ScanResult> linkedMap = new LinkedHashMap<>(scanResults.size());
        for (ScanResult scanResult : scanResults) {
            if (linkedMap.containsKey(scanResult.SSID)) {
                if (scanResult.level > linkedMap.get(scanResult.SSID).level) {
                    linkedMap.put(scanResult.SSID, scanResult);
                }
                continue;
            }
            if (!TextUtils.isEmpty(scanResult.SSID)) {
                linkedMap.put(scanResult.SSID, scanResult);
            }
        }
        scanResults.clear();
        scanResults.addAll(linkedMap.values());
    }
}
