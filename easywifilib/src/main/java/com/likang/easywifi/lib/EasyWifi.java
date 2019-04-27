package com.likang.easywifi.lib;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.likang.easywifi.lib.core.guid.UserActionBridgeActivity;
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
 * <p>
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
    private static final Object sLock = new Object();

    private static final ArrayList<WifiTask> CUR_WIFI_TASKS = new ArrayList<>();

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
    public static final int TASK_FAIL_REASON_SCAN_WIFI_UNKNOWN = 15;

    public static final int PREPARING_CURRENT_STEP_CHECK_LOCATION_ENABLED = 1;
    public static final int PREPARING_CURRENT_STEP_SET_LOCATION_ENABLED = 2;
    public static final int PREPARING_CURRENT_STEP_CHECK_LOCATION_PERMISSION = 3;
    public static final int PREPARING_CURRENT_STEP_REQUEST_LOCATION_PERMISSION = 4;

    public static final int PREPARING_CURRENT_STEP_CHECK_WIFI_ENABLED = 5;
    public static final int PREPARING_CURRENT_STEP_CHECK_WIFI_PERMISSION = 6;
    public static final int PREPARING_CURRENT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION = 7;
    public static final int PREPARING_CURRENT_STEP_SET_WIFI_ENABLED = 8;

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
     * @param application
     */

    public static void initCore(Application application) {
        ApplicationHolder.init(application);
        sApplication = application;
        sWifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
        sHandler = new Handler(Looper.getMainLooper());
        synchronized (sLock) {
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

    public static ArrayList<WifiTask> getCurrentTasks() {
        checkIsInitialised();
        return CUR_WIFI_TASKS;
    }


    public static void cancelAllTasks() {
        Iterator<WifiTask> iterator = CUR_WIFI_TASKS.iterator();
        while (iterator.hasNext()) {
            iterator.next().cancel();
        }
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
//            onPrepareCallback.onPreparingCurrentStep(wifiTaskType, PREPARING_CURRENT_STEP_SET_LOCATION_ENABLED);
//            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
//                    UserActionBridgeActivity.STEP_CODE_ENABLE_LOCATION_MODULE,
//                    new UserActionBridgeActivity.OnUserDoneCallback() {
//                        @Override
//                        public void onUserDoneIsWeExcepted(int stepCode) {
//                            currentStep.run();
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
//        final Runnable currentStep = new Runnable() {
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
//            currentStep.run();
//        } else {
//
//            onPrepareCallback.onPreparingCurrentStep(wifiTaskType, PREPARING_CURRENT_STEP_REQUEST_LOCATION_PERMISSION);
//            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
//                    UserActionBridgeActivity.STEP_CODE_REQUEST_LOCATION_PERMISSION, new UserActionBridgeActivity.OnUserDoneCallback() {
//                        @Override
//                        public void onUserDoneIsWeExcepted(int stepCode) {
//                            currentStep.run();
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
//            onPrepareCallback.onPreparingCurrentStep(wifiTaskType, PREPARING_CURRENT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION);
//            IntentManager.gotoUserActionBridgeActivity(wifiTaskType,
//                    UserActionBridgeActivity.STEP_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION,
//                    new UserActionBridgeActivity.OnUserDoneCallback() {
//                        @Override
//                        public void onUserDoneIsWeExcepted(int stepCode) {
//                            currentStep.run();
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
     * @param setWifiEnabledTask
     */
    public static void setWifiEnabled(final SetWifiEnabledTask setWifiEnabledTask) {
        setWifiEnabledTask.callOnTaskStartRun();

        setWifiEnabledTaskPrepare(setWifiEnabledTask, new OnPrepareCallback() {

            @Override
            public void onPreparingCurrentStep(int currentStep) {
                setWifiEnabledTask.callOnTaskRunningCurrentStep(currentStep);
            }

            @Override
            public void onPrepareSuccess() {
                setWifiEnabledTask.callOnTaskSuccess();
            }

            @Override
            public void onPrepareFail(int prepareTaskFailReason) {
                setWifiEnabledTask.callOnTaskFail(prepareTaskFailReason);
            }
        });
    }

    /**
     * @param scanWifiTask
     */
    public static void scanWifi(final ScanWifiTask scanWifiTask) {

        scanWifiTask.callOnTaskStartRun();
        scanWifiPrepare(scanWifiTask, new OnPrepareCallback() {

            @Override
            public void onPreparingCurrentStep(int currentStep) {
                scanWifiTask.callOnTaskRunningCurrentStep(currentStep);
            }

            @Override
            public void onPrepareSuccess() {

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

                    requestStartScanResult = true;
                    IntentManager.gotoRequestSystemWifiScanActivity();
                }

                if (requestStartScanResult) {

                    scanWifiTask.setBroadcastReceiver(new BroadcastReceiver() {

                        @Override
                        public void onReceive(Context context, Intent intent) {

                            boolean isSuccess = true;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,
                                        false)) {
                                    isSuccess = false;
                                }
                            }

                            sApplication.unregisterReceiver(this);
                            scanWifiTask.setBroadcastReceiver(null);
                            sHandler.removeCallbacks(scanWifiTask.getPostDelayRunnable());
                            scanWifiTask.setPostDelayRunnable(null);
                            if (isSuccess) {
                                scanWifiTask.callOnTaskSuccess();
                            } else {
                                scanWifiTask.callOnTaskFail(TASK_FAIL_REASON_SCAN_WIFI_UNKNOWN);
                            }
                        }
                    });

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                    sApplication.registerReceiver(scanWifiTask.getBroadcastReceiver(), intentFilter);

                    //logic for scanWifi timeout;
                    scanWifiTask.setPostDelayRunnable(new Runnable() {
                        @Override
                        public void run() {
                            scanWifiTask.setPostDelayRunnable(null);
                            sApplication.unregisterReceiver(scanWifiTask.getBroadcastReceiver());
                            scanWifiTask.setBroadcastReceiver(null);
                            scanWifiTask.callOnTaskFail(TASK_FAIL_REASON_SCAN_WIFI_TIMEOUT);
                        }
                    });

                    sHandler.postDelayed(scanWifiTask.getPostDelayRunnable(), scanWifiTask.getScanWifiTimeout());

                } else {
                    scanWifiTask.callOnTaskFail(TASK_FAIL_REASON_SCAN_WIFI_REQUEST_NOT_BE_SATISFIED);
                }
            }

            @Override
            public void onPrepareFail(int prepareTaskFailReason) {
                scanWifiTask.callOnTaskFail(prepareTaskFailReason);
            }
        });
    }

    /**
     * @param connectToWifiTask
     */
    public static void connectToConfiguredWifi(final ConnectToWifiTask connectToWifiTask) {

        connectToWifiTask.callOnTaskStartRun();

        connectToWifiPrepare(connectToWifiTask, new OnPrepareCallback() {

            @Override
            public void onPreparingCurrentStep(int currentStep) {
                connectToWifiTask.callOnTaskRunningCurrentStep(currentStep);
            }

            @Override
            public void onPrepareSuccess() {

                if (WifiUtils.isAlreadyConnected(connectToWifiTask.getWifiConfiguration().SSID, connectToWifiTask.getWifiConfiguration().BSSID, sWifiManager)) {
                    connectToWifiTask.callOnTaskSuccess();
                    return;
                }

                //fixme android p (nokia x6) onConnectToWifiStart lock.
                boolean requestConnectToWifiResult = WifiUtils.connectToConfiguredWifi(sWifiManager, connectToWifiTask.getWifiConfiguration().networkId);

                if (requestConnectToWifiResult) {
                    //note: connect to wifi is not timely.

                    final boolean[] authenticatingIsReceived = {false};
                    final boolean[] obtainingIpAddrIsReceived = {false};
                    final boolean[] verifyingPoorLinkIsReceived = {false};
                    final boolean[] captivePortalCheckIsReceived = {false};

                    connectToWifiTask.setBroadcastReceiver(new BroadcastReceiver() {
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
                                        connectToWifiTask.callOnTaskRunningCurrentStep(CONNECTING_DETAIL_AUTHENTICATING);
                                    }
                                    break;
                                case OBTAINING_IPADDR:
                                    if (!obtainingIpAddrIsReceived[0]) {
                                        obtainingIpAddrIsReceived[0] = true;
                                        connectToWifiTask.callOnTaskRunningCurrentStep(CONNECTING_DETAIL_OBTAINING_IP_ADDR);
                                    }
                                    break;
                                case VERIFYING_POOR_LINK:
                                    if (!verifyingPoorLinkIsReceived[0]) {
                                        verifyingPoorLinkIsReceived[0] = true;
                                        //Temporary shutdown (network down)
                                        connectToWifiTask.callOnTaskRunningCurrentStep(CONNECTING_DETAIL_VERIFYING_POOR_LINK);
                                    }
                                    break;
                                case CAPTIVE_PORTAL_CHECK:
                                    if (!captivePortalCheckIsReceived[0]) {
                                        captivePortalCheckIsReceived[0] = true;
                                        //Determine whether a browser login is required
                                        connectToWifiTask.callOnTaskRunningCurrentStep(CONNECTING_DETAIL_CAPTIVE_PORTAL_CHECK);
                                    }
                                    break;

                                //CONNECTED
                                case CONNECTED:
                                    if (authenticatingIsReceived[0]) {
                                        unregisterReceiver();
                                        connectToWifiTask.callOnTaskSuccess();
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
                                        unregisterReceiver();
                                        connectToWifiTask.callOnTaskFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_IS_POOR_LINK);
                                        break;
                                    }
                                    if (obtainingIpAddrIsReceived[0]) {
                                        unregisterReceiver();
                                        connectToWifiTask.callOnTaskFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_NOT_OBTAINED_IP_ADDR);
                                        break;
                                    }
                                    if (authenticatingIsReceived[0]) {
                                        unregisterReceiver();
                                        connectToWifiTask.callOnTaskFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_ERROR_AUTHENTICATING);
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

                        private void unregisterReceiver() {
                            sApplication.unregisterReceiver(this);
                            connectToWifiTask.setBroadcastReceiver(null);
                            sHandler.removeCallbacks(connectToWifiTask.getPostDelayRunnable());
                            connectToWifiTask.setPostDelayRunnable(null);
                        }
                    });

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                    sApplication.registerReceiver(connectToWifiTask.getBroadcastReceiver(), intentFilter);

                    //logic for connect to wifi timeout;
                    connectToWifiTask.setPostDelayRunnable(new Runnable() {
                        @Override
                        public void run() {
                            connectToWifiTask.setPostDelayRunnable(null);
                            sApplication.unregisterReceiver(connectToWifiTask.getBroadcastReceiver());
                            connectToWifiTask.setBroadcastReceiver(null);
                            connectToWifiTask.callOnTaskFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_TIMEOUT);

                        }
                    });

                    sHandler.postDelayed(connectToWifiTask.getPostDelayRunnable(), connectToWifiTask.getConnectToWifiTimeout());


                } else {
                    connectToWifiTask.callOnTaskFail(TASK_FAIL_REASON_CONNECT_TO_WIFI_REQUEST_NOT_BE_SATISFIED);
                }
            }


            @Override
            public void onPrepareFail(int prepareTaskFailReason) {
                connectToWifiTask.callOnTaskFail(prepareTaskFailReason);
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

        getConnectionInfoTask.callOnTaskStartRun();

        getConnectionInfoPrepare(getConnectionInfoTask, new OnPrepareCallback() {

            @Override
            public void onPreparingCurrentStep(int currentStep) {
                getConnectionInfoTask.callOnTaskRunningCurrentStep(currentStep);
            }

            @Override
            public void onPrepareSuccess() {
                getConnectionInfoTask.callOnTaskSuccess();
            }

            @Override
            public void onPrepareFail(int prepareTaskFailReason) {
                getConnectionInfoTask.callOnTaskFail(prepareTaskFailReason);
            }
        });


    }

    private static void checkIsInitialised() {
        synchronized (sLock) {
            if (!sIsInitialised) {
                throw new IllegalStateException("you must invoke initCore method first of all.");
            }
        }
    }

    /**
     * 1.Wifi module must be enabled.
     *
     * @param setWifiEnabledTask
     * @param onPrepareCallback
     */
    private static void setWifiEnabledTaskPrepare(SetWifiEnabledTask setWifiEnabledTask,
                                                  OnPrepareCallback onPrepareCallback) {

        if (isWifiEnabled() == setWifiEnabledTask.isEnabled()) {
            onPrepareCallback.onPrepareSuccess();
        } else {
            checkAndGuideUserGrantWifiPermissionAndNext(setWifiEnabledTask,
                    setWifiEnabledTask.isEnabled(),
                    setWifiEnabledTask.getSetWifiEnabledTimeout(),
                    onPrepareCallback);
        }

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
     * @param scanWifiTask
     * @param onPrepareCallback
     */

    private static void scanWifiPrepare(ScanWifiTask scanWifiTask,
                                        OnPrepareCallback onPrepareCallback) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkAndEnableLocationAndNext(scanWifiTask, scanWifiTask.getSetWifiEnabledTimeout(), onPrepareCallback);
        } else {
            checkWifiEnabledAndNext(scanWifiTask, true, scanWifiTask.getSetWifiEnabledTimeout(), onPrepareCallback);
        }
    }


    /**
     * 1.Wifi module must be enabled.
     *
     * @param connectToWifiTask
     * @param onPrepareCallback
     */
    private static void connectToWifiPrepare(ConnectToWifiTask connectToWifiTask,
                                             OnPrepareCallback onPrepareCallback) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkAndEnableLocationAndNext(connectToWifiTask, connectToWifiTask.getSetWifiEnabledTimeout(), onPrepareCallback);
        } else {
            checkAndGuideUserGrantWifiPermissionAndNext(connectToWifiTask, true, connectToWifiTask.getSetWifiEnabledTimeout(), onPrepareCallback);
        }
    }


    /**
     * 1.Location module must be enabled.
     *
     * @param onPrepareCallback
     */
    private static void getConnectionInfoPrepare(GetConnectionInfoTask getConnectionInfoTask, OnPrepareCallback onPrepareCallback) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            checkAndEnableLocationAndNext(getConnectionInfoTask, 0, onPrepareCallback);
        } else {
            onPrepareCallback.onPrepareSuccess();
        }
    }

    /**
     * 1.Check location module is exist.
     * 2.Check location is enable.
     * 3.Enable location.
     * 4.Request permission.
     *
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkAndEnableLocationAndNext(final WifiTask wifiTask,
                                                      final long setWifiEnabledTimeout,
                                                      final OnPrepareCallback onPrepareCallback) {

        onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_CHECK_LOCATION_ENABLED);

        final Runnable currentStep = new Runnable() {
            @Override
            public void run() {
                checkAndRequestLocationPermissionAndNext(wifiTask, setWifiEnabledTimeout, onPrepareCallback);
            }
        };

        if (!LocationUtils.checkLocationModuleIsExist()) {
            onPrepareCallback.onPrepareFail(TASK_FAIL_REASON_LOCATION_MODULE_NOT_EXIST);
        } else if (!LocationUtils.isLocationEnabled()) {
            onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_SET_LOCATION_ENABLED);
            IntentManager.gotoUserActionBridgeActivity(UserActionBridgeActivity.STEP_CODE_ENABLE_LOCATION_MODULE,
                    new UserActionBridgeActivity.OnUserDoneCallback() {
                        @Override
                        public void onUserDoneIsWeExcepted(int stepCode) {
                            currentStep.run();
                        }

                        @Override
                        public void onUserDoneIsNotWeExcepted(int stepCode) {
                            onPrepareCallback.onPrepareFail(TASK_FAIL_REASON_LOCATION_MODULE_DISABLE);
                        }
                    });
        } else {
            currentStep.run();
        }
    }


    /**
     * 1.Check app is has permission about location operation.
     * 2.If app doesn't have permission, request.
     *
     * @param wifiTask
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkAndRequestLocationPermissionAndNext(final WifiTask wifiTask,
                                                                 final long setWifiEnabledTimeout,
                                                                 final OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_CHECK_LOCATION_PERMISSION);

        boolean isHasLocationPermission = LocationUtils.checkHasLocationPermissions();

        final Runnable currentStep = new Runnable() {
            @Override
            public void run() {
                if (wifiTask instanceof ScanWifiTask) {
                    checkWifiEnabledAndNext(wifiTask, true, setWifiEnabledTimeout, onPrepareCallback);
                } else if (wifiTask instanceof GetConnectionInfoTask) {
                    onPrepareCallback.onPrepareSuccess();
                } else if (wifiTask instanceof ConnectToWifiTask) {
                    checkAndGuideUserGrantWifiPermissionAndNext(wifiTask, true, setWifiEnabledTimeout, onPrepareCallback);
                }

            }
        };

        if (isHasLocationPermission) {
            currentStep.run();
        } else {

            onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_REQUEST_LOCATION_PERMISSION);
            IntentManager.gotoUserActionBridgeActivity(
                    UserActionBridgeActivity.STEP_CODE_REQUEST_LOCATION_PERMISSION, new UserActionBridgeActivity.OnUserDoneCallback() {
                        @Override
                        public void onUserDoneIsWeExcepted(int stepCode) {
                            currentStep.run();
                        }

                        @Override
                        public void onUserDoneIsNotWeExcepted(int stepCode) {
                            onPrepareCallback.onPrepareFail(TASK_FAIL_REASON_NOT_HAS_LOCATION_PERMISSIONS);
                        }
                    });
        }
    }

    /**
     * 1.check wifi module is enabled.
     * 2.if disable, invoke checkAndGuideUserGrantWifiPermissionAndNext;
     *
     * @param wifiTask
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkWifiEnabledAndNext(WifiTask wifiTask, boolean enabled, long setWifiEnabledTimeout, OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_CHECK_WIFI_ENABLED);
        if (isWifiEnabled() != enabled) {
            checkAndGuideUserGrantWifiPermissionAndNext(wifiTask, enabled, setWifiEnabledTimeout, onPrepareCallback);
        } else {
            onPrepareCallback.onPrepareSuccess();
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
     * @param wifiTask
     * @param enabled
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void checkAndGuideUserGrantWifiPermissionAndNext(final WifiTask wifiTask,
                                                                    final boolean enabled,
                                                                    final long setWifiEnabledTimeout,
                                                                    final OnPrepareCallback onPrepareCallback) {

        onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_CHECK_WIFI_PERMISSION);

        final Runnable currentStep = new Runnable() {
            @Override
            public void run() {
                if (isWifiEnabled() != enabled) {
                    setWifiEnabledInternal(wifiTask, enabled, setWifiEnabledTimeout, onPrepareCallback);
                } else {
                    onPrepareCallback.onPrepareSuccess();
                }
            }
        };

        if (!WifiUtils.checkWifiModuleIsExist(sWifiManager)) {
            onPrepareCallback.onPrepareFail(TASK_FAIL_REASON_WIFI_MODULE_NOT_EXIST);
        } else if (!WifiUtils.checkHasChangeWifiStatePermission(sWifiManager)) {//fixme vivo reject crash.
            onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION);
            IntentManager.gotoUserActionBridgeActivity(UserActionBridgeActivity.STEP_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION,
                    new UserActionBridgeActivity.OnUserDoneCallback() {
                        @Override
                        public void onUserDoneIsWeExcepted(int stepCode) {
                            currentStep.run();
                        }

                        @Override
                        public void onUserDoneIsNotWeExcepted(int stepCode) {
                            onPrepareCallback.onPrepareFail(TASK_FAIL_REASON_NOT_HAS_WIFI_PERMISSION);
                        }
                    });
        } else {
            currentStep.run();
        }
    }

    /**
     * 1.Check wifi is enable.
     * 2.Set wifi enabled.
     *
     * @param wifiTask
     * @param enabled
     * @param setWifiEnabledTimeout
     * @param onPrepareCallback
     */
    private static void setWifiEnabledInternal(final WifiTask wifiTask,
                                               boolean enabled,
                                               long setWifiEnabledTimeout,
                                               final OnPrepareCallback onPrepareCallback) {
        onPrepareCallback.onPreparingCurrentStep(PREPARING_CURRENT_STEP_SET_WIFI_ENABLED);


        boolean requestSetWifiEnabledResult = sWifiManager.setWifiEnabled(enabled);
        if (requestSetWifiEnabledResult) {

            //note: The operation of  wifi is not timely.
            final int expectedState = enabled ?
                    WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;

            wifiTask.setBroadcastReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (expectedState == intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN)) {
                        sApplication.unregisterReceiver(this);
                        wifiTask.setBroadcastReceiver(null);
                        sHandler.removeCallbacks(wifiTask.getPostDelayRunnable());
                        wifiTask.setPostDelayRunnable(null);
                        onPrepareCallback.onPrepareSuccess();
                    }
                }
            });
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            sApplication.registerReceiver(wifiTask.getBroadcastReceiver(), intentFilter);

            //logic for setWifiEnabledTimeout;
            wifiTask.setPostDelayRunnable(new Runnable() {
                @Override
                public void run() {
                    wifiTask.setPostDelayRunnable(null);
                    sApplication.unregisterReceiver(wifiTask.getBroadcastReceiver());
                    wifiTask.setBroadcastReceiver(null);
                    onPrepareCallback.onPrepareFail(TASK_FAIL_REASON_SET_WIFI_ENABLED_TIMEOUT);
                }
            });
            sHandler.postDelayed(wifiTask.getPostDelayRunnable(), setWifiEnabledTimeout);

        } else {
            onPrepareCallback.onPrepareFail(TASK_FAIL_REASON_SET_WIFI_ENABLED_REQUEST_NOT_BE_SATISFIED);
        }
    }

    private interface OnPrepareCallback {

        /**
         * @param currentStep
         */
        void onPreparingCurrentStep(int currentStep);

        /**
         *
         */
        void onPrepareSuccess();

        /**
         * @param prepareTaskFailReason
         */
        void onPrepareFail(int prepareTaskFailReason);
    }

}