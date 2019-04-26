package com.likang.easywifi.lib;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.likang.easywifi.lib.core.guid.UserActionBridgeActivity;
import com.likang.easywifi.lib.core.guid.UserActionGuideToast;
import com.likang.easywifi.lib.core.task.ConnectToWifiTask;
import com.likang.easywifi.lib.core.task.GetConnectionInfoTask;
import com.likang.easywifi.lib.core.task.ScanWifiTask;
import com.likang.easywifi.lib.core.task.SetWifiEnabledTask;
import com.likang.easywifi.lib.core.task.WifiTask;
import com.likang.easywifi.lib.util.ApplicationHolder;
import com.likang.easywifi.lib.util.IntentManager;
import com.likang.easywifi.lib.util.LocationUtils;
import com.likang.easywifi.lib.util.Logger;
import com.likang.easywifi.lib.util.WifiUtils;

import java.util.Collections;
import java.util.List;

/**
 * functions:
 * 1.Request all permissions wifi task need.
 * 2.Enable wifi;
 * 3.Disable wifi;
 * 4.Scan and get the wifi list in the environment;
 * 5.Connect to any wifi;
 * 6.Get current connected wifi info;
 * <p>
 *
 * @author likangren
 */
public final class EasyWifi {

    private static final String TAG = "EasyWifi";

    private static Application sApplication;
    private static WifiManager sWifiManager;
    private static boolean sIsInitialised = false;
    private static Handler sHandler;

    public static final int WIFI_TASK_TYPE_UNKNOWN = -1;
    public static final int WIFI_TASK_TYPE_SCAN_WIFI = 1;
    public static final int WIFI_TASK_TYPE_CONNECT_WIFI = 2;
    public static final int WIFI_TASK_TYPE_SET_WIFI_ENABLED = 3;
    public static final int WIFI_TASK_TYPE_GET_CONNECTION_INFO = 4;

    public static final int TASK_FAIL_REASON_LOCATION_MODULE_DISABLE = 1;
    public static final int TASK_FAIL_REASON_LOCATION_MODULE_NOT_EXIST = 2;
    public static final int TASK_FAIL_REASON_NOT_HAS_LOCATION_PERMISSIONS = 3;

    public static final int TASK_FAIL_REASON_WIFI_MODULE_NOT_EXIST = 4;
    public static final int TASK_FAIL_REASON_NOT_HAS_WIFI_PERMISSION = 5;
    public static final int TASK_FAIL_REASON_SET_WIFI_ENABLED_REQUEST_NOT_BE_SATISFIED = 6;
    public static final int TASK_FAIL_REASON_SET_WIFI_ENABLED_TIMEOUT = 7;

    public static final int TASK_FAIL_REASON_CONNECT_TO_WIFI_REQUEST_NOT_BE_SATISFIED = 8;
    public static final int TASK_FAIL_REASON_CONNECT_TO_WIFI_ERROR_AUTHENTICATING = 9;
    public static final int TASK_FAIL_REASON_CONNECT_TO_WIFI_NOT_OBTAINED_IP_ADDR = 10;
    public static final int TASK_FAIL_REASON_CONNECT_TO_WIFI_IS_POOR_LINK = 11;
    public static final int TASK_FAIL_REASON_CONNECT_TO_WIFI_TIMEOUT = 12;

    public static final int TASK_FAIL_REASON_SCAN_WIFI_REQUEST_NOT_BE_SATISFIED = 13;
    public static final int TASK_FAIL_REASON_SCAN_WIFI_TIMEOUT = 14;

    public static final int PREPARING_NEXT_STEP_CHECK_LOCATION_ENABLED = 1;
    public static final int PREPARING_NEXT_STEP_CHECK_LOCATION_PERMISSION = 2;
    public static final int PREPARING_NEXT_STEP_REQUEST_LOCATION_PERMISSION = 3;
    public static final int PREPARING_NEXT_STEP_LOCATION_ENABLE = 4;

    public static final int PREPARING_NEXT_STEP_CHECK_WIFI_ENABLED = 5;
    public static final int PREPARING_NEXT_STEP_CHECK_WIFI_PERMISSION = 6;
    public static final int PREPARING_NEXT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION = 7;
    public static final int PREPARING_NEXT_STEP_SET_WIFI_ENABLED = 8;

    public static final int CONNECTING_DETAIL_AUTHENTICATING = 1;
    public static final int CONNECTING_DETAIL_OBTAINING_IP_ADDR = 2;
    public static final int CONNECTING_DETAIL_VERIFYING_POOR_LINK = 3;
    public static final int CONNECTING_DETAIL_CAPTIVE_PORTAL_CHECK = 4;

    public static final int SCAN_WIFI_WAY_INITIATIVE = 1;
    public static final int SCAN_WIFI_WAY_THROUGH_WIFI_SETTING = 2;

    public static final int TIME_OUT_SET_WIFI_ENABLED_5S = 5000;
    public static final int TIME_OUT_SET_WIFI_ENABLED_7S = 7000;
    public static final int TIME_OUT_SET_WIFI_ENABLED_10S = 10000;
    public static final int TIME_OUT_SET_WIFI_ENABLED_DEFAULT = TIME_OUT_SET_WIFI_ENABLED_5S;

    public static final int TIME_OUT_SCAN_WIFI_5S = 5000;
    public static final int TIME_OUT_SCAN_WIFI_10S = 10000;
    public static final int TIME_OUT_SCAN_WIFI_15S = 10000;
    public static final int TIME_OUT_SCAN_WIFI_DEFAULT = TIME_OUT_SCAN_WIFI_15S;

    public static final int TIME_OUT_CONNECT_TO_WIFI_5S = 5000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_10S = 10000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_15S = 15000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_20S = 20000;
    public static final int TIME_OUT_CONNECT_TO_WIFI_DEFAULT = TIME_OUT_CONNECT_TO_WIFI_20S;

    /*****open aip****/

    /**
     * must invoke in Application onCreate.
     *
     * @param application
     */

    public static void initCore(Application application) {
        ApplicationHolder.init(application);
        sApplication = application;
        sWifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
        sHandler = new Handler();
        sIsInitialised = true;
    }

    /**
     * @return
     */
    public static WifiManager getWifiManager() {
        return sWifiManager;
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
        String enclosedInDoubleQuotationMarksSsid = WifiUtils.enclosedInDoubleQuotationMarks(ssid);

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


    public static void cancelTask(WifiTask wifiTask) {
        checkIsInitialised();
        wifiTask.cancel();
    }

    /****internal****/

    /**
     *
     */
    public static void guideUserGrantPermission() {
//
//        if (!LocationUtils.checkLocationModuleIsExist()) {
//            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_LOCATION_MODULE_NOT_EXIST);
//        } else if (!LocationUtils.isLocationEnabled()) {
//            onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_LOCATION_ENABLE);
//            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
//                    UserActionBridgeActivity.STEP_CODE_ENABLE_LOCATION_MODULE,
//                    new UserActionBridgeActivity.OnUserDoneCallback() {
//                        @Override
//                        public void onUserDoneIsWeExcepted(int stepCode) {
//                            nextStep.run();
//                        }
//
//                        @Override
//                        public void onUserDoneIsNotWeExcepted(int stepCode) {
//                            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_LOCATION_MODULE_DISABLE);
//                        }
//                    });


//
//        boolean isHasLocationPermission = LocationUtils.checkHasLocationPermissions();
//
//        final Runnable nextStep = new Runnable() {
//            @Override
//            public void run() {
//                if (WIFI_TASK_TYPE_SCAN_WIFI == wifiTaskType || WIFI_TASK_TYPE_CONNECT_WIFI == wifiTaskType) {
//                    checkAndGuideUserGrantWifiPermissionAndNext(wifiTaskType, true, setWifiEnabledTimeout, onPrepareCallback);
//                } else if (WIFI_TASK_TYPE_GET_CONNECTION_INFO == wifiTaskType) {
//                    onPrepareCallback.onPrepareSuccess(wifiTaskType);
//                }
//            }
//        };
//
//        if (isHasLocationPermission) {
//            nextStep.run();
//        } else {
//
//            onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_REQUEST_LOCATION_PERMISSION);
//            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
//                    UserActionBridgeActivity.STEP_CODE_REQUEST_LOCATION_PERMISSION, new UserActionBridgeActivity.OnUserDoneCallback() {
//                        @Override
//                        public void onUserDoneIsWeExcepted(int stepCode) {
//                            nextStep.run();
//                        }
//
//                        @Override
//                        public void onUserDoneIsNotWeExcepted(int stepCode) {
//                            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_NOT_HAS_LOCATION_PERMISSIONS);
//                        }
//                    });
//        }


//        if (!WifiUtils.checkWifiModuleIsExist(sWifiManager)) {
//            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_WIFI_MODULE_NOT_EXIST);
//        } else if (!WifiUtils.checkHasChangeWifiStatePermission(sWifiManager)) {
//            onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION);
//            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
//                    UserActionBridgeActivity.STEP_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION,
//                    new UserActionBridgeActivity.OnUserDoneCallback() {
//                        @Override
//                        public void onUserDoneIsWeExcepted(int stepCode) {
//                            nextStep.run();
//                        }
//
//                        @Override
//                        public void onUserDoneIsNotWeExcepted(int stepCode) {
//                            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_NOT_HAS_WIFI_PERMISSION);
//                        }
//                    });
//        } else {


    }

    /**
     * @param wifiTask
     */
    public static void setWifiEnabled(final SetWifiEnabledTask wifiTask) {
        setWifiEnabledTaskPrepare(wifiTask.isEnabled(), wifiTask.getSetWifiEnabledTimeout(), new OnPrepareCallback() {
            @Override
            public void onPrepareStart(int wifiTaskType) {
                if (wifiTask.getWifiTaskCallback() != null) {
                    wifiTask.getWifiTaskCallback().onSetWifiEnabledPreparing(wifiTask.isEnabled());
                }
            }

            @Override
            public void onPreparingNextStep(int wifiTaskType, int nextStep) {
                if (wifiTask.getWifiTaskCallback() != null) {
                    if (nextStep == PREPARING_NEXT_STEP_SET_WIFI_ENABLED) {
                        wifiTask.getWifiTaskCallback().onSetWifiEnabledStart(wifiTask.isEnabled());
                    } else {
                        wifiTask.getWifiTaskCallback().onSetWifiEnabledPreparingNextStep(wifiTask.isEnabled(), nextStep);
                    }
                }
            }

            @Override
            public void onPrepareSuccess(int wifiTaskType) {
                if (wifiTask.getWifiTaskCallback() != null) {
                    wifiTask.getWifiTaskCallback().onSetWifiEnabledSuccess(wifiTask.isEnabled());
                }
            }

            @Override
            public void onPrepareFail(int wifiTaskType, int prepareTaskFailReason) {
                if (wifiTask.getWifiTaskCallback() != null) {
                    wifiTask.getWifiTaskCallback().onSetWifiEnabledFail(wifiTask.isEnabled(), prepareTaskFailReason);
                }
            }
        });
    }

    /**
     * @param scanWifiTask
     */
    public static void scanWifi(final ScanWifiTask scanWifiTask) {

        scanWifiPrepare(scanWifiTask.getSetWifiEnabledTimeout(), new OnPrepareCallback() {
            @Override
            public void onPrepareStart(int wifiTaskType) {
                if (scanWifiTask.getWifiTaskCallback() != null) {
                    scanWifiTask.getWifiTaskCallback().onScanWifiPreparing();
                }
            }

            @Override
            public void onPreparingNextStep(int wifiTaskType, int nextStep) {
                if (scanWifiTask.getWifiTaskCallback() != null) {
                    scanWifiTask.getWifiTaskCallback().onScanWifiPreparingNextStep(nextStep);
                }
            }

            @Override
            public void onPrepareSuccess(int wifiTaskType) {
                if (scanWifiTask.getWifiTaskCallback() != null) {
                    scanWifiTask.getWifiTaskCallback().onScanWifiStart();
                }

                boolean requestStartScanResult = false;
                boolean isNeedSwitchToThroughSystemWifi = false;

                if (SCAN_WIFI_WAY_INITIATIVE == scanWifiTask.getScanWifiWay()) {
                    //Take the initiative to call
                    requestStartScanResult = sWifiManager.startScan();
                    if (!requestStartScanResult && scanWifiTask.isIsAutoSwitchToThroughSystemWifi()) {
                        isNeedSwitchToThroughSystemWifi = true;
                    }
                }

                if (isNeedSwitchToThroughSystemWifi || SCAN_WIFI_WAY_THROUGH_WIFI_SETTING == scanWifiTask.getScanWifiWay()) {

                    requestStartScanResult = IntentManager.gotoWifiSettings(scanWifiTask.getSingleTaskActivity());
                    UserActionGuideToast.showGuideToast(sApplication, "正在扫描wifi",
                            "操作指南：\n如果很长时间没有跳回应用，需要您点击「返回」键", Toast.LENGTH_SHORT);
                    sApplication.registerActivityLifecycleCallbacks(
                            new Application.ActivityLifecycleCallbacks() {
                                @Override
                                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                                }

                                @Override
                                public void onActivityStarted(Activity activity) {
                                    Logger.d(TAG, "onActivityStarted=" + activity);
                                }

                                @Override
                                public void onActivityResumed(Activity activity) {
                                    Logger.d(TAG, "onActivityResumed=" + activity);
                                }

                                @Override
                                public void onActivityPaused(Activity activity) {
                                    Logger.d(TAG, "onActivityPaused=" + activity);
                                }

                                @Override
                                public void onActivityStopped(Activity activity) {
                                    Logger.d(TAG, "onActivityStopped=" + activity);
                                    if (scanWifiTask.getSingleTaskActivity() == activity) {
                                        scanWifiTask.getSingleTaskActivity().startActivity(
                                                new Intent(scanWifiTask.getSingleTaskActivity(),
                                                        scanWifiTask.getSingleTaskActivity().getClass()));

                                        //for compat:
                                        ActivityManager activityManager = (ActivityManager) sApplication.getSystemService(Context.ACTIVITY_SERVICE);
                                        activityManager.moveTaskToFront(scanWifiTask.getSingleTaskActivity().getTaskId(), 0);

                                        sApplication.unregisterActivityLifecycleCallbacks(this);
                                    }

                                }

                                @Override
                                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                                }

                                @Override
                                public void onActivityDestroyed(Activity activity) {

                                }
                            });
                }

                if (requestStartScanResult) {

                    final Runnable[] scanWifiTimeoutCallback = new Runnable[1];

                    final BroadcastReceiver scanResultsAvailableReceiver = new BroadcastReceiver() {

                        @Override
                        public void onReceive(Context context, Intent intent) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,
                                        false)) {
                                    return;
                                }
                            }

                            sApplication.unregisterReceiver(this);
                            sHandler.removeCallbacks(scanWifiTimeoutCallback[0]);
                            if (scanWifiTask.getWifiTaskCallback() != null) {
                                scanWifiTask.getWifiTaskCallback().onScanWifiSuccess();
                            }
                        }
                    };

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                    sApplication.registerReceiver(scanResultsAvailableReceiver, intentFilter);

                    //logic for scanWifi timeout;
                    scanWifiTimeoutCallback[0] = new Runnable() {
                        @Override
                        public void run() {
                            sApplication.unregisterReceiver(scanResultsAvailableReceiver);
                            if (scanWifiTask.getWifiTaskCallback() != null) {
                                scanWifiTask.getWifiTaskCallback().onScanWifiFail(TASK_FAIL_REASON_SCAN_WIFI_TIMEOUT);
                            }
                        }
                    };

                    sHandler.postDelayed(scanWifiTimeoutCallback[0], scanWifiTask.getScanWifiTimeout());

                } else {
                    if (scanWifiTask.getWifiTaskCallback() != null) {
                        scanWifiTask.getWifiTaskCallback().onScanWifiFail(TASK_FAIL_REASON_SCAN_WIFI_REQUEST_NOT_BE_SATISFIED);
                    }
                }
            }

            @Override
            public void onPrepareFail(int wifiTaskType, int prepareTaskFailReason) {
                if (scanWifiTask.getWifiTaskCallback() != null) {
                    scanWifiTask.getWifiTaskCallback().onScanWifiFail(prepareTaskFailReason);
                }
            }
        });
    }

    /**
     * @param connectToWifiTask
     */
    public static void connectToConfiguredWifi(final ConnectToWifiTask connectToWifiTask) {

        connectToWifiPrepare(connectToWifiTask.getSetWifiEnabledTimeout(), new OnPrepareCallback() {
            @Override
            public void onPrepareStart(int wifiTaskType) {
                if (connectToWifiTask.getWifiTaskCallback() != null) {
                    connectToWifiTask.getWifiTaskCallback().onConnectToWifiPreparing();
                }
            }

            @Override
            public void onPreparingNextStep(int wifiTaskType, int nextStep) {
                if (connectToWifiTask.getWifiTaskCallback() != null) {
                    connectToWifiTask.getWifiTaskCallback().onConnectToWifiPreparingNextStep(nextStep);
                }
            }

            @Override
            public void onPrepareSuccess(int wifiTaskType) {
                if (connectToWifiTask.getWifiTaskCallback() != null) {
                    connectToWifiTask.getWifiTaskCallback().onConnectToWifiStart();
                }

                if (WifiUtils.isAlreadyConnected(connectToWifiTask.getWifiConfiguration().SSID, connectToWifiTask.getWifiConfiguration().BSSID, sWifiManager)) {
                    if (connectToWifiTask.getWifiTaskCallback() != null) {
                        connectToWifiTask.getWifiTaskCallback().onConnectToWifiSuccess();
                    }
                    return;
                }

                //fixme android p (nokia x6) onConnectToWifiStart lock.
                boolean requestConnectToWifiResult = WifiUtils.connectToConfiguredWifi(sWifiManager, connectToWifiTask.getWifiConfiguration().networkId);

                if (requestConnectToWifiResult) {
                    //note: connect to wifi is not timely.
                    final Runnable[] connectToWifiTimeoutCallback = new Runnable[1];

                    final boolean[] authenticatingIsReceived = {false};
                    final boolean[] obtainingIpAddrIsReceived = {false};
                    final boolean[] verifyingPoorLinkIsReceived = {false};
                    final boolean[] captivePortalCheckIsReceived = {false};

                    final BroadcastReceiver networkStateChangedReceiver = new BroadcastReceiver() {
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
                                    if (!authenticatingIsReceived[0]) {
                                        authenticatingIsReceived[0] = true;
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiConnecting(CONNECTING_DETAIL_AUTHENTICATING);
                                        }
                                    }
                                    break;
                                case OBTAINING_IPADDR:
                                    if (!obtainingIpAddrIsReceived[0]) {
                                        obtainingIpAddrIsReceived[0] = true;
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiConnecting(CONNECTING_DETAIL_OBTAINING_IP_ADDR);
                                        }
                                    }
                                    break;
                                case VERIFYING_POOR_LINK:
                                    if (!verifyingPoorLinkIsReceived[0]) {
                                        verifyingPoorLinkIsReceived[0] = true;
                                        //Temporary shutdown (network down)
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiConnecting(CONNECTING_DETAIL_VERIFYING_POOR_LINK);
                                        }
                                    }
                                    break;
                                case CAPTIVE_PORTAL_CHECK:
                                    if (!captivePortalCheckIsReceived[0]) {
                                        captivePortalCheckIsReceived[0] = true;
                                        //Determine whether a browser login is required
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiConnecting(CONNECTING_DETAIL_CAPTIVE_PORTAL_CHECK);
                                        }
                                    }
                                    break;

                                //CONNECTED
                                case CONNECTED:
                                    if (authenticatingIsReceived[0]) {
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiSuccess();
                                        }
                                        sApplication.unregisterReceiver(this);
                                        sHandler.removeCallbacks(connectToWifiTimeoutCallback[0]);
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
                                    if (verifyingPoorLinkIsReceived[0]) {
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_IS_POOR_LINK);
                                        }
                                        sApplication.unregisterReceiver(this);
                                        sHandler.removeCallbacks(connectToWifiTimeoutCallback[0]);
                                        break;
                                    }
                                    if (obtainingIpAddrIsReceived[0]) {
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_NOT_OBTAINED_IP_ADDR);
                                        }
                                        sApplication.unregisterReceiver(this);
                                        sHandler.removeCallbacks(connectToWifiTimeoutCallback[0]);
                                        break;
                                    }
                                    if (authenticatingIsReceived[0]) {
                                        if (connectToWifiTask.getWifiTaskCallback() != null) {
                                            connectToWifiTask.getWifiTaskCallback().onConnectToWifiFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_ERROR_AUTHENTICATING);
                                        }
                                        sApplication.unregisterReceiver(this);
                                        sHandler.removeCallbacks(connectToWifiTimeoutCallback[0]);
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
                    };

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                    sApplication.registerReceiver(networkStateChangedReceiver, intentFilter);

                    //logic for connect to wifi timeout;
                    connectToWifiTimeoutCallback[0] = new Runnable() {
                        @Override
                        public void run() {

                            if (connectToWifiTask.getWifiTaskCallback() != null) {
                                connectToWifiTask.getWifiTaskCallback().onConnectToWifiFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_TIMEOUT);
                            }
                            sApplication.unregisterReceiver(networkStateChangedReceiver);

                        }
                    };

                    sHandler.postDelayed(connectToWifiTimeoutCallback[0], connectToWifiTask.getConnectToWifiTimeout());


                } else {
                    if (connectToWifiTask.getWifiTaskCallback() != null) {
                        connectToWifiTask.getWifiTaskCallback().onConnectToWifiFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_REQUEST_NOT_BE_SATISFIED);
                    }
                }
            }


            @Override
            public void onPrepareFail(int wifiTaskType, int prepareTaskFailReason) {
                if (connectToWifiTask.getWifiTaskCallback() != null) {
                    connectToWifiTask.getWifiTaskCallback().onConnectToWifiFail(prepareTaskFailReason);
                }
            }
        });


    }

    /**
     * @param connectToWifiTask
     */
    public static void connectToUnConfiguredWifi(ConnectToWifiTask connectToWifiTask) {

        //fixme if !bssidValid,  is hidden.

        WifiConfiguration configuration = WifiUtils.addNetWork(sWifiManager, connectToWifiTask.getSsid(), connectToWifiTask.getBssid(), connectToWifiTask.getPassword(), connectToWifiTask.getEncryptionScheme());
        connectToWifiTask.setWifiConfiguration(configuration);
        connectToConfiguredWifi(connectToWifiTask);

    }


    /**
     * @param getConnectionInfoTask
     */
    public static void getConnectionInfo(final GetConnectionInfoTask getConnectionInfoTask) {
        getConnectionInfoPrepare(new OnPrepareCallback() {
            @Override
            public void onPrepareStart(int wifiTaskType) {
                if (getConnectionInfoTask.getWifiTaskCallback() != null) {
                    getConnectionInfoTask.getWifiTaskCallback().onGetConnectionInfoPreparing();
                }
            }

            @Override
            public void onPreparingNextStep(int wifiTaskType, int nextStep) {
                if (getConnectionInfoTask.getWifiTaskCallback() != null) {
                    getConnectionInfoTask.getWifiTaskCallback().onGetConnectionInfoPreparingNextStep(nextStep);
                }
            }

            @Override
            public void onPrepareSuccess(int wifiTaskType) {
                WifiInfo connectionInfo = sWifiManager.getConnectionInfo();
                if (getConnectionInfoTask.getWifiTaskCallback() != null) {
                    getConnectionInfoTask.getWifiTaskCallback().onGetConnectionInfoSuccess(connectionInfo);
                }
            }

            @Override
            public void onPrepareFail(int wifiTaskType, int prepareTaskFailReason) {
                if (getConnectionInfoTask.getWifiTaskCallback() != null) {
                    getConnectionInfoTask.getWifiTaskCallback().onGetConnectionInfoFail(prepareTaskFailReason);
                }
            }
        });


    }

    private static void checkIsInitialised() {
        if (!sIsInitialised) {
            throw new IllegalStateException("you must invoke initCore method first of all.");
        }
    }

    /**
     * 1.Wifi module must be enabled.
     *
     * @param enabled
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void setWifiEnabledTaskPrepare(boolean enabled, long setWifiEnabledTimeout,
                                                  OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPrepareStart(WIFI_TASK_TYPE_SET_WIFI_ENABLED);

        checkAndGuideUserGrantWifiPermissionAndNext(WIFI_TASK_TYPE_SET_WIFI_ENABLED, enabled, setWifiEnabledTimeout, onPrepareCallback);
    }

    /**
     * 1.Request permission
     * need to request in runtime:
     * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" /> dangerous
     * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> dangerous
     * <p>
     * needn't to request in runtime：
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> normal
     * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" /> normal
     * <p>
     * 2.Location module must be enabled.
     * 3.Wifi module must be enabled.
     *
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */

    private static void scanWifiPrepare(long setWifiEnabledTimeout,
                                        OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPrepareStart(WIFI_TASK_TYPE_SCAN_WIFI);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkAndEnableLocationAndNext(WIFI_TASK_TYPE_SCAN_WIFI, setWifiEnabledTimeout, onPrepareCallback);
        } else {
            checkWifiEnabledAndNext(WIFI_TASK_TYPE_SCAN_WIFI, true, setWifiEnabledTimeout, onPrepareCallback);
        }
    }


    /**
     * 1.Wifi module must be enabled.
     *
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void connectToWifiPrepare(long setWifiEnabledTimeout,
                                             OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPrepareStart(WIFI_TASK_TYPE_CONNECT_WIFI);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkAndEnableLocationAndNext(WIFI_TASK_TYPE_CONNECT_WIFI, setWifiEnabledTimeout, onPrepareCallback);
        } else {
            checkAndGuideUserGrantWifiPermissionAndNext(WIFI_TASK_TYPE_CONNECT_WIFI, true, setWifiEnabledTimeout, onPrepareCallback);
        }
    }


    /**
     * 1.Location module must be enabled.
     *
     * @param onPrepareCallback
     */
    private static void getConnectionInfoPrepare(OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPrepareStart(WIFI_TASK_TYPE_GET_CONNECTION_INFO);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkAndEnableLocationAndNext(WIFI_TASK_TYPE_GET_CONNECTION_INFO, 0, onPrepareCallback);
        } else {
            onPrepareCallback.onPrepareSuccess(WIFI_TASK_TYPE_GET_CONNECTION_INFO);
        }
    }

    /**
     * 1.Check location module is exist.
     * 2.Check location is enable.
     * 3.Enable location.
     * 4.Request permission.
     *
     * @param wifiTaskType
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkAndEnableLocationAndNext(final int wifiTaskType, final long setWifiEnabledTimeout,
                                                      final OnPrepareCallback onPrepareCallback) {

        onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_CHECK_LOCATION_ENABLED);

        final Runnable nextStep = new Runnable() {
            @Override
            public void run() {
                checkAndRequestLocationPermissionAndNext(wifiTaskType, setWifiEnabledTimeout, onPrepareCallback);
            }
        };

        if (!LocationUtils.checkLocationModuleIsExist()) {
            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_LOCATION_MODULE_NOT_EXIST);
        } else if (!LocationUtils.isLocationEnabled()) {
            onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_LOCATION_ENABLE);
            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
                    UserActionBridgeActivity.STEP_CODE_ENABLE_LOCATION_MODULE,
                    new UserActionBridgeActivity.OnUserDoneCallback() {
                        @Override
                        public void onUserDoneIsWeExcepted(int stepCode) {
                            nextStep.run();
                        }

                        @Override
                        public void onUserDoneIsNotWeExcepted(int stepCode) {
                            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_LOCATION_MODULE_DISABLE);
                        }
                    });
        } else {
            nextStep.run();
        }
    }


    /**
     * 1.Check app is has permission about location operation.
     * 2.If app doesn't have permission, request.
     *
     * @param wifiTaskType
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkAndRequestLocationPermissionAndNext(final int wifiTaskType,
                                                                 final long setWifiEnabledTimeout,
                                                                 final OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_CHECK_LOCATION_PERMISSION);

        boolean isHasLocationPermission = LocationUtils.checkHasLocationPermissions();

        final Runnable nextStep = new Runnable() {
            @Override
            public void run() {
                if (WIFI_TASK_TYPE_SCAN_WIFI == wifiTaskType) {
                    checkWifiEnabledAndNext(wifiTaskType, true, setWifiEnabledTimeout, onPrepareCallback);
                } else if (WIFI_TASK_TYPE_GET_CONNECTION_INFO == wifiTaskType) {
                    onPrepareCallback.onPrepareSuccess(wifiTaskType);
                } else if (WIFI_TASK_TYPE_CONNECT_WIFI == wifiTaskType) {
                    checkAndGuideUserGrantWifiPermissionAndNext(wifiTaskType, true, setWifiEnabledTimeout, onPrepareCallback);
                }
            }
        };

        if (isHasLocationPermission) {
            nextStep.run();
        } else {

            onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_REQUEST_LOCATION_PERMISSION);
            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
                    UserActionBridgeActivity.STEP_CODE_REQUEST_LOCATION_PERMISSION, new UserActionBridgeActivity.OnUserDoneCallback() {
                        @Override
                        public void onUserDoneIsWeExcepted(int stepCode) {
                            nextStep.run();
                        }

                        @Override
                        public void onUserDoneIsNotWeExcepted(int stepCode) {
                            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_NOT_HAS_LOCATION_PERMISSIONS);
                        }
                    });
        }
    }

    /**
     * 1.check wifi module is enabled.
     * 2.if disable, invoke checkAndGuideUserGrantWifiPermissionAndNext;
     *
     * @param wifiTaskType
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkWifiEnabledAndNext(int wifiTaskType, boolean enabled, long setWifiEnabledTimeout, OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_CHECK_WIFI_ENABLED);
        if (isWifiEnabled() != enabled) {
            checkAndGuideUserGrantWifiPermissionAndNext(wifiTaskType, enabled, setWifiEnabledTimeout, onPrepareCallback);
        } else {
            onPrepareCallback.onPrepareSuccess(wifiTaskType);
        }
    }

    /**
     * 1.Check wifi module is exist.
     * 2.Request all permission about wifi operation.
     * <p>
     * (1).check app is has permission about wifi operation.
     * (2).If app doesn't have permission, guide the user to open the app permission setting page and grant wifi permission.
     * <p>
     * 3.Set wifi enabled.
     *
     * @param wifiTaskType
     * @param enabled
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkAndGuideUserGrantWifiPermissionAndNext(final int wifiTaskType,
                                                                    final boolean enabled,
                                                                    final long setWifiEnabledTimeout,
                                                                    final OnPrepareCallback onPrepareCallback) {

        onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_CHECK_WIFI_PERMISSION);

        final Runnable nextStep = new Runnable() {
            @Override
            public void run() {
                setWifiEnabled(enabled, wifiTaskType, setWifiEnabledTimeout, onPrepareCallback);
            }
        };

        if (!WifiUtils.checkWifiModuleIsExist(sWifiManager)) {
            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_WIFI_MODULE_NOT_EXIST);
        } else if (!WifiUtils.checkHasChangeWifiStatePermission(sWifiManager)) {
            onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION);
            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
                    UserActionBridgeActivity.STEP_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION,
                    new UserActionBridgeActivity.OnUserDoneCallback() {
                        @Override
                        public void onUserDoneIsWeExcepted(int stepCode) {
                            nextStep.run();
                        }

                        @Override
                        public void onUserDoneIsNotWeExcepted(int stepCode) {
                            onPrepareCallback.onPrepareFail(wifiTaskType, TASK_FAIL_REASON_NOT_HAS_WIFI_PERMISSION);
                        }
                    });
        } else {
            nextStep.run();
        }
    }

    /**
     * @param enabled
     * @param wifiTaskType
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void setWifiEnabled(boolean enabled,
                                       final int wifiTaskType,
                                       long setWifiEnabledTimeout,
                                       final OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPreparingNextStep(wifiTaskType, PREPARING_NEXT_STEP_SET_WIFI_ENABLED);
        setWifiEnabled(enabled, setWifiEnabledTimeout, new OnSetWifiEnabledInternalCallback() {

            @Override
            public void onSetWifiEnabledInternalSuccess() {
                onPrepareCallback.onPrepareSuccess(wifiTaskType);
            }

            @Override
            public void onSetWifiEnabledInternalFail(int setWifiEnabledFailReason) {
                onPrepareCallback.onPrepareFail(wifiTaskType, setWifiEnabledFailReason);
            }
        });
    }

    /**
     * 1.Check wifi is enable.
     * 2.Set wifi enabled.
     *
     * @param enabled
     * @param setWifiEnabledTimeout
     * @param onSetWifiEnabledInternalCallback
     */
    private static void setWifiEnabled(final boolean enabled,
                                       long setWifiEnabledTimeout,
                                       final OnSetWifiEnabledInternalCallback onSetWifiEnabledInternalCallback) {
        if (isWifiEnabled() != enabled) {

            boolean requestSetWifiEnabledResult = sWifiManager.setWifiEnabled(enabled);
            if (requestSetWifiEnabledResult) {

                final Runnable[] setWifiEnabledTimeoutCallback = new Runnable[1];
                //note: The operation of  wifi is not timely.
                final int expectedState = enabled ?
                        WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;

                final BroadcastReceiver wifiStateChangedReceiver = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (expectedState == intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                                WifiManager.WIFI_STATE_UNKNOWN)) {
                            onSetWifiEnabledInternalCallback.onSetWifiEnabledInternalSuccess();
                            sApplication.unregisterReceiver(this);
                            sHandler.removeCallbacks(setWifiEnabledTimeoutCallback[0]);

                        }
                    }
                };
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                sApplication.registerReceiver(wifiStateChangedReceiver, intentFilter);

                //logic for setWifiEnabledTimeout;
                setWifiEnabledTimeoutCallback[0] = new Runnable() {
                    @Override
                    public void run() {
                        onSetWifiEnabledInternalCallback.onSetWifiEnabledInternalFail(TASK_FAIL_REASON_SET_WIFI_ENABLED_TIMEOUT);
                        sApplication.unregisterReceiver(wifiStateChangedReceiver);
                    }
                };
                sHandler.postDelayed(setWifiEnabledTimeoutCallback[0], setWifiEnabledTimeout);

            } else {
                onSetWifiEnabledInternalCallback.onSetWifiEnabledInternalFail(TASK_FAIL_REASON_SET_WIFI_ENABLED_REQUEST_NOT_BE_SATISFIED);
            }
        } else {
            onSetWifiEnabledInternalCallback.onSetWifiEnabledInternalSuccess();
        }
    }

    private interface OnPrepareCallback {

        /**
         * @param wifiTaskType
         */
        void onPrepareStart(int wifiTaskType);

        /**
         * @param wifiTaskType
         * @param nextStep
         */
        void onPreparingNextStep(int wifiTaskType, int nextStep);

        /**
         * @param wifiTaskType
         */
        void onPrepareSuccess(int wifiTaskType);

        /**
         * @param wifiTaskType
         * @param prepareTaskFailReason
         */
        void onPrepareFail(int wifiTaskType, int prepareTaskFailReason);
    }

    private interface OnSetWifiEnabledInternalCallback {

        /**
         *
         */
        void onSetWifiEnabledInternalSuccess();

        /**
         * @param setWifiEnabledFailReason
         */
        void onSetWifiEnabledInternalFail(int setWifiEnabledFailReason);
    }
}