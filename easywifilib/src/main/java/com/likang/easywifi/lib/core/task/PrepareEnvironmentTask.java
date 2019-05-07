package com.likang.easywifi.lib.core.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Parcel;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.core.guid.UserActionBridgeActivity;
import com.likang.easywifi.lib.util.IntentManager;
import com.likang.easywifi.lib.util.LocationUtils;
import com.likang.easywifi.lib.util.WifiUtils;

/**
 * @author likangren
 */
public class PrepareEnvironmentTask extends WifiTask {

    private boolean mIsNeedLocationPermission;
    private boolean mIsNeedEnableLocation;
    private boolean mIsNeedWifiPermission;
    private boolean mIsNeedSetWifiEnabled;
    private boolean mWifiEnabled;
    private long mSetWifiEnabledTimeout;
    private boolean mIsGuideUserGrantPermissionsTogether;

    public PrepareEnvironmentTask() {
    }


    public PrepareEnvironmentTask(boolean isNeedLocationPermission,
                                  boolean isNeedEnableLocation,
                                  boolean isNeedWifiPermission,
                                  boolean isNeedSetWifiEnabled,
                                  boolean wifiEnabled,
                                  long setWifiEnabledTimeout,
                                  boolean isGuideUserGrantPermissionsTogether,
                                  WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);


        if (isAboveLollipopMr1()) {
            mIsNeedLocationPermission = isNeedLocationPermission;
            mIsNeedEnableLocation = isNeedEnableLocation;
            mIsGuideUserGrantPermissionsTogether = isGuideUserGrantPermissionsTogether;
        }

        mIsNeedWifiPermission = isNeedWifiPermission;
        mIsNeedSetWifiEnabled = isNeedSetWifiEnabled;
        mWifiEnabled = wifiEnabled;
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
    }


    private PrepareEnvironmentTask(Parcel in) {
        super(in);
        mIsNeedLocationPermission = in.readByte() == 1;
        mIsNeedEnableLocation = in.readByte() == 1;
        mIsNeedWifiPermission = in.readByte() == 1;
        mIsNeedSetWifiEnabled = in.readByte() == 1;
        mWifiEnabled = in.readByte() == 1;
        mSetWifiEnabledTimeout = in.readLong();
        mIsGuideUserGrantPermissionsTogether = in.readByte() == 1;
    }

    public boolean isNeedLocationPermission() {
        return mIsNeedLocationPermission;
    }

    public void setIsNeedLocationPermission(boolean isNeedLocationPermission) {
        if (isAboveLollipopMr1()) {
            mIsNeedLocationPermission = isNeedLocationPermission;
        }
    }

    public boolean isNeedEnableLocation() {
        return mIsNeedEnableLocation;
    }

    public void setIsNeedEnableLocation(boolean isNeedEnableLocation) {
        if (isAboveLollipopMr1()) {
            mIsNeedEnableLocation = isNeedEnableLocation;
        }
    }

    public boolean isNeedWifiPermission() {
        return mIsNeedWifiPermission;
    }

    public void setIsNeedWifiPermission(boolean isNeedWifiPermission) {

        mIsNeedWifiPermission = isNeedWifiPermission;
    }

    public boolean isNeedSetWifiEnabled() {
        return mIsNeedSetWifiEnabled;
    }

    public void setIsNeedSetWifiEnabled(boolean isNeedSetWifiEnabled) {
        mIsNeedSetWifiEnabled = isNeedSetWifiEnabled;
    }

    public boolean isWifiEnabled() {
        return mWifiEnabled;
    }

    public void setWifiEnabled(boolean mWifiEnabled) {
        this.mWifiEnabled = mWifiEnabled;
    }

    public long getSetWifiEnabledTimeout() {
        return mSetWifiEnabledTimeout;
    }

    public void setSetWifiEnabledTimeout(long setWifiEnabledTimeout) {
        mSetWifiEnabledTimeout = setWifiEnabledTimeout;
    }

    public boolean isGuideUserGrantPermissionsTogether() {
        return mIsGuideUserGrantPermissionsTogether;
    }

    public void setIsGuideUserGrantPermissionsTogether(boolean isGuideUserGrantPermissionsTogether) {
        if (isAboveLollipopMr1()) {
            mIsGuideUserGrantPermissionsTogether = isGuideUserGrantPermissionsTogether;
        }
    }

    @Override
    void checkParams() {
        if (mSetWifiEnabledTimeout < 0) {
            throw new IllegalArgumentException("SetWifiEnabledTimeout must more than 0!");
        }
    }

    @Override
    public synchronized void run() {
        super.run();
        if (!callOnTaskStartRun()) {
            return;
        }

        //The following three runnable tasks are in reverse order.5, 4,3,2,1;

        //step 5.
        final Runnable checkSetWifiEnabledIsSuccess = new Runnable() {
            @Override
            public void run() {
                final int expectedState = mWifiEnabled ?
                        WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                registerAutoReleaseReceiver(new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (expectedState == intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                                WifiManager.WIFI_STATE_UNKNOWN)) {
                            callOnTaskSuccess();
                        }
                    }
                }, intentFilter, mSetWifiEnabledTimeout, EasyWifi.FAIL_REASON_SET_WIFI_ENABLED_TIMEOUT);

            }
        };

        //step 4.
        final Runnable setWifiEnabledIfNeed = new Runnable() {
            @Override
            public void run() {
                if (mIsNeedSetWifiEnabled) {

                    if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CHECK_WIFI_MODULE_IS_EXIST)) {
                        return;
                    }
                    if (!WifiUtils.checkWifiModuleIsExist(EasyWifi.getWifiManager())) {
                        callOnTaskFail(EasyWifi.FAIL_REASON_WIFI_MODULE_NOT_EXIST);
                        return;
                    }

                    if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CHECK_WIFI_ENABLED)) {
                        return;
                    }

                    if (EasyWifi.isWifiEnabled() != mWifiEnabled) {

                        if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_SET_WIFI_ENABLED)) {
                            return;
                        }

                        if (EasyWifi.getWifiManager().setWifiEnabled(mWifiEnabled)) {
                            checkSetWifiEnabledIsSuccess.run();
                        } else {
                            IntentManager.gotoUserActionBridgeActivity(mWifiEnabled ? UserActionBridgeActivity.USER_ACTION_CODE_ENABLE_WIFI_MODULE :
                                            UserActionBridgeActivity.USER_ACTION_CODE_DISABLE_WIFI_MODULE,
                                    new UserActionBridgeActivity.OnUserActionDoneCallback() {
                                        @Override
                                        public void onUserActionDoneIsWeExcepted() {
                                            checkSetWifiEnabledIsSuccess.run();
                                        }

                                        @Override
                                        public void onUserActionDoneIsNotWeExcepted() {
                                            //It never gets executed here.
                                        }
                                    });
                        }
                    } else {
                        callOnTaskSuccess();
                    }
                } else {
                    callOnTaskSuccess();
                }
            }
        };


        //step 3.
        final Runnable checkIsNeedWifiPermissionAndNext = new Runnable() {
            @Override
            public void run() {
                if (mIsNeedWifiPermission) {

                    if (mIsGuideUserGrantPermissionsTogether) {
                        setWifiEnabledIfNeed.run();
                        return;
                    }

                    if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CHECK_WIFI_PERMISSION)) {
                        return;
                    }

                    if (WifiUtils.isUserForbidWifiPermission()) {
                        if (mIsResumeTask && mLastRunningCurrentStep == EasyWifi.CURRENT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION) {
                            callOnTaskFail(EasyWifi.FAIL_REASON_NOT_HAS_WIFI_PERMISSION);
                            return;
                        }
                        if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_GUIDE_USER_GRANT_WIFI_PERMISSION)) {
                            return;
                        }
                        IntentManager.gotoUserActionBridgeActivity(UserActionBridgeActivity.USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION,
                                new UserActionBridgeActivity.OnUserActionDoneCallback() {
                                    @Override
                                    public void onUserActionDoneIsWeExcepted() {
                                        setWifiEnabledIfNeed.run();
                                    }

                                    @Override
                                    public void onUserActionDoneIsNotWeExcepted() {
                                        callOnTaskFail(EasyWifi.FAIL_REASON_NOT_HAS_WIFI_PERMISSION);
                                    }
                                });
                    } else {
                        setWifiEnabledIfNeed.run();
                    }

                } else {
                    callOnTaskSuccess();
                }
            }
        };

        //step 2.
        final Runnable checkIsNeedEnableLocationAndNext = new Runnable() {
            @Override
            public void run() {
                if (mIsNeedEnableLocation) {

                    if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CHECK_LOCATION_MODULE_IS_EXIST)) {
                        return;
                    }
                    if (!LocationUtils.checkLocationModuleIsExist()) {
                        callOnTaskFail(EasyWifi.FAIL_REASON_LOCATION_MODULE_NOT_EXIST);
                        return;
                    }

                    if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CHECK_LOCATION_ENABLED)) {
                        return;
                    }
                    if (!LocationUtils.isLocationEnabled()) {
                        if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_SET_LOCATION_ENABLED)) {
                            return;
                        }
                        IntentManager.gotoUserActionBridgeActivity(UserActionBridgeActivity.USER_ACTION_CODE_ENABLE_LOCATION_MODULE,
                                new UserActionBridgeActivity.OnUserActionDoneCallback() {
                                    @Override
                                    public void onUserActionDoneIsWeExcepted() {
                                        checkIsNeedWifiPermissionAndNext.run();
                                    }

                                    @Override
                                    public void onUserActionDoneIsNotWeExcepted() {
                                        callOnTaskFail(EasyWifi.FAIL_REASON_SET_LOCATION_ENABLED_USER_REJECT);
                                    }
                                });
                    } else {
                        checkIsNeedWifiPermissionAndNext.run();
                    }

                } else {
                    checkIsNeedWifiPermissionAndNext.run();
                }
            }
        };

        //step 1.
        Runnable checkIsNeedLocationPermissionAndNext = new Runnable() {
            @Override
            public void run() {
                if (mIsNeedLocationPermission) {

                    if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CHECK_LOCATION_PERMISSION)) {
                        return;
                    }

                    if (!LocationUtils.checkHasLocationPermissions()) {
                        if (mIsResumeTask && mLastRunningCurrentStep == EasyWifi.CURRENT_STEP_REQUEST_LOCATION_PERMISSION) {
                            callOnTaskFail(EasyWifi.FAIL_REASON_NOT_HAS_LOCATION_PERMISSION);
                            return;
                        }
                        if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_REQUEST_LOCATION_PERMISSION)) {
                            return;
                        }
                        IntentManager.gotoUserActionBridgeActivity(
                                UserActionBridgeActivity.USER_ACTION_CODE_REQUEST_LOCATION_PERMISSION,
                                new UserActionBridgeActivity.OnUserActionDoneCallback() {
                                    @Override
                                    public void onUserActionDoneIsWeExcepted() {
                                        checkIsNeedEnableLocationAndNext.run();
                                    }

                                    @Override
                                    public void onUserActionDoneIsNotWeExcepted() {

                                        callOnTaskFail(EasyWifi.FAIL_REASON_NOT_HAS_LOCATION_PERMISSION);
                                    }
                                });
                    } else {
                        checkIsNeedEnableLocationAndNext.run();
                    }

                } else {
                    checkIsNeedWifiPermissionAndNext.run();
                }

            }
        };

        //step 1.
        Runnable checkIsHasAllPermissionAndNext = new Runnable() {
            @Override
            public void run() {

                if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_CHECK_WIFI_AND_LOCATION_PERMISSION)) {
                    return;
                }

                if (WifiUtils.isUserForbidWifiPermission() || !LocationUtils.checkHasLocationPermissions()) {
                    if (mIsResumeTask && mLastRunningCurrentStep == EasyWifi.CURRENT_STEP_GUIDE_USER_GRANT_WIFI_AND_LOCATION_PERMISSION) {
                        callOnTaskFail(EasyWifi.FAIL_REASON_NOT_HAS_WIFI_AND_LOCATION_PERMISSION);
                        return;
                    }
                    if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_GUIDE_USER_GRANT_WIFI_AND_LOCATION_PERMISSION)) {
                        return;
                    }
                    IntentManager.gotoUserActionBridgeActivity(
                            UserActionBridgeActivity.USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_AND_LOCATION_PERMISSION,
                            new UserActionBridgeActivity.OnUserActionDoneCallback() {
                                @Override
                                public void onUserActionDoneIsWeExcepted() {
                                    checkIsNeedEnableLocationAndNext.run();
                                }

                                @Override
                                public void onUserActionDoneIsNotWeExcepted() {
                                    callOnTaskFail(EasyWifi.FAIL_REASON_NOT_HAS_WIFI_AND_LOCATION_PERMISSION);
                                }
                            });
                } else {
                    checkIsNeedEnableLocationAndNext.run();
                }


            }
        };

        if (mIsGuideUserGrantPermissionsTogether) {
            checkIsHasAllPermissionAndNext.run();
        } else {
            checkIsNeedLocationPermissionAndNext.run();
        }
    }


    public static boolean isAboveLollipopMr1() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (mIsNeedLocationPermission ? 1 : 0));
        dest.writeByte((byte) (mIsNeedEnableLocation ? 1 : 0));
        dest.writeByte((byte) (mIsNeedWifiPermission ? 1 : 0));
        dest.writeByte((byte) (mIsNeedSetWifiEnabled ? 1 : 0));
        dest.writeByte((byte) (mWifiEnabled ? 1 : 0));
        dest.writeLong(mSetWifiEnabledTimeout);
        dest.writeByte((byte) (mIsGuideUserGrantPermissionsTogether ? 1 : 0));
    }

    public static final Creator<PrepareEnvironmentTask> CREATOR = new Creator<PrepareEnvironmentTask>() {
        @Override
        public PrepareEnvironmentTask createFromParcel(Parcel in) {
            return new PrepareEnvironmentTask(in);
        }

        @Override
        public PrepareEnvironmentTask[] newArray(int size) {
            return new PrepareEnvironmentTask[size];
        }
    };

    @Override
    public String toString() {
        return "PrepareEnvironmentTask{" +
                "mIsNeedLocationPermission=" + mIsNeedLocationPermission +
                ", mIsNeedEnableLocation=" + mIsNeedEnableLocation +
                ", mIsNeedWifiPermission=" + mIsNeedWifiPermission +
                ", mIsNeedSetWifiEnabled=" + mIsNeedSetWifiEnabled +
                ", mWifiEnabled=" + mWifiEnabled +
                ", mSetWifiEnabledTimeout=" + mSetWifiEnabledTimeout +
                ", mIsGuideUserGrantPermissionsTogether=" + mIsGuideUserGrantPermissionsTogether +
                ", mWifiTaskCallback=" + mWifiTaskCallback +
                ", mRunningCurrentStep=" + mRunningCurrentStep +
                ", mLastRunningCurrentStep=" + mLastRunningCurrentStep +
                ", mFailReason=" + mFailReason +
                ", mCurrentStatus=" + mCurrentStatus +
                ", mIsResumeTask=" + mIsResumeTask +
                '}';
    }
}
