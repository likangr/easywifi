package com.likang.easywifi.lib.util;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;


public class WifiEncryptionScheme {

    private static String TAG = "WifiEncryptionScheme";

    public static final String ENCRYPTION_SCHEME_NONE = "NONE";
    public static final String ENCRYPTION_SCHEME_WEP = "WEP";
    public static final String ENCRYPTION_SCHEME_PSK = "PSK";
    public static final String ENCRYPTION_SCHEME_EAP = "EAP";


    public static void configEncryptionScheme(WifiConfiguration wifiConfiguration, String encryptionScheme, String password) {
        wifiConfiguration.allowedAuthAlgorithms.clear();
        wifiConfiguration.allowedGroupCiphers.clear();
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedPairwiseCiphers.clear();
        wifiConfiguration.allowedProtocols.clear();

        switch (encryptionScheme) {
            case ENCRYPTION_SCHEME_NONE:
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                break;
            case ENCRYPTION_SCHEME_WEP:
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                if (isHexWepKey(password)) {
                    wifiConfiguration.wepKeys[0] = password;
                } else {
                    wifiConfiguration.wepKeys[0] = WifiUtils.enclosedInDoubleQuotationMarks(password);
                }
                break;
            case ENCRYPTION_SCHEME_PSK:
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    wifiConfiguration.preSharedKey = password;
                } else {
                    wifiConfiguration.preSharedKey = WifiUtils.enclosedInDoubleQuotationMarks(password);
                }
                break;
            case ENCRYPTION_SCHEME_EAP:
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                wifiConfiguration.preSharedKey = WifiUtils.enclosedInDoubleQuotationMarks(password);
                break;

            default:
                break;
        }
    }


    public static boolean isHexWepKey(String wepKey) {
        int passwordLen = wepKey == null ? 0 : wepKey.length();
        return passwordLen != 0 && (passwordLen == 10 || passwordLen == 26 || passwordLen == 58) && wepKey.matches("[0-9A-Fa-f]*");
    }

    public static String getEncryptionSchemeByScanResult(ScanResult result) {
        String encryptionScheme = ENCRYPTION_SCHEME_NONE;

        if (result.capabilities.contains(ENCRYPTION_SCHEME_WEP)) {
            encryptionScheme = ENCRYPTION_SCHEME_WEP;
        }
        if (result.capabilities.contains(ENCRYPTION_SCHEME_PSK)) {
            encryptionScheme = ENCRYPTION_SCHEME_PSK;
        }
        if (result.capabilities.contains(ENCRYPTION_SCHEME_EAP)) {
            encryptionScheme = ENCRYPTION_SCHEME_EAP;
        }

        return encryptionScheme;
    }
}
