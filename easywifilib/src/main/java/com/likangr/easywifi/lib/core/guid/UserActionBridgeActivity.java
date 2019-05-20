package com.likangr.easywifi.lib.core.guid;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.likangr.easywifi.lib.util.IntentManager;
import com.likangr.easywifi.lib.util.LocationUtils;
import com.likangr.easywifi.lib.util.PermissionsManager;
import com.likangr.easywifi.lib.util.WifiUtils;

import java.util.HashMap;

/**
 * @author likangren
 */
public class UserActionBridgeActivity extends AppCompatActivity implements PermissionsManager.IReqListener1 {

    private static final String TAG = "UserActionBridgeActivity";

    public static final int USER_ACTION_CODE_ENABLE_LOCATION_MODULE = 1;
    public static final int USER_ACTION_CODE_REQUEST_LOCATION_PERMISSION = 2;
    public static final int USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION = 3;
    public static final int USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_AND_LOCATION_PERMISSION = 4;
    public static final int USER_ACTION_CODE_ENABLE_WIFI_MODULE = 5;
    public static final int USER_ACTION_CODE_DISABLE_WIFI_MODULE = 6;

    public static final String INTENT_EXTRA_KEY_USER_ACTION_CODE = "user_action_code";
    public static final String INTENT_EXTRA_KEY_USER_ACTION_DONE_CALLBACK_ID = "user_action_done_callback_id";

    private static HashMap<Integer, OnUserActionDoneCallback> sOnUserActionDoneCallbacks;
    private int mUserActionDoneCallbackId;
    private int mUserActionCode;
    private boolean mIsFirstOnResume = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }

        Intent intent = getIntent();
        mUserActionDoneCallbackId = intent.getIntExtra(INTENT_EXTRA_KEY_USER_ACTION_DONE_CALLBACK_ID, 0);
        mUserActionCode = intent.getIntExtra(INTENT_EXTRA_KEY_USER_ACTION_CODE, 0);

        switch (mUserActionCode) {
            case USER_ACTION_CODE_ENABLE_LOCATION_MODULE:
                IntentManager.gotoLocationSettings(this);
                UserActionGuideToast.showGuideToast(this, "需要打开「位置信息服务」",
                        "操作指南：\n1.找到「位置信息/定位」相关按钮并打开对应开关\n2.操作完成后点击返回键返回应用", Toast.LENGTH_LONG);
                break;
            case USER_ACTION_CODE_REQUEST_LOCATION_PERMISSION:
                if (LocationUtils.isUserForbidLocationPermissions(this)) {
                    IntentManager.gotoSelfPermissionSetting(this);
                    UserActionGuideToast.showGuideToast(this, "需要「位置信息权限」",
                            "操作指南：\n1.进入「权限」设置，找到「位置信息/定位」相关按钮并允许权限\n2.操作完成后点击返回键返回应用", Toast.LENGTH_LONG);
                } else {
                    PermissionsManager.request(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION}, USER_ACTION_CODE_REQUEST_LOCATION_PERMISSION,
                            this, this);
                }
                break;
            case USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION:
                IntentManager.gotoSelfPermissionSetting(this);
                UserActionGuideToast.showGuideToast(this, "需要「WIFI操作权限」",
                        "操作指南：\n1.进入「权限」设置，找到「连接WLAN网络和断开连接/开启关闭WIFI」相关按钮并允许权限\n2.操作完成后点击返回键返回应用", Toast.LENGTH_LONG);
                break;
            case USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_AND_LOCATION_PERMISSION:
                IntentManager.gotoSelfPermissionSetting(this);
                UserActionGuideToast.showGuideToast(this, "需要「WIFI操作权限」和「位置信息权限」",
                        "操作指南：\n1.进入「权限」设置，找到「连接WLAN网络和断开连接/开启关闭WIFI」及「位置信息/定位」相关按钮并允许权限\n2.操作完成后点击返回键返回应用", Toast.LENGTH_LONG);
                break;
            case USER_ACTION_CODE_ENABLE_WIFI_MODULE:
                IntentManager.gotoWifiSettings(this);
                UserActionGuideToast.showGuideToast(this, "需要打开「WLAN/WIFI」",
                        "操作指南：\n1.找到「打开WLAN/WIFI」相关按钮并打开对应开关\n2.操作完成后点击返回键返回应用", Toast.LENGTH_LONG);
                break;
            case USER_ACTION_CODE_DISABLE_WIFI_MODULE:
                IntentManager.gotoWifiSettings(this);
                UserActionGuideToast.showGuideToast(this, "需要关闭「WLAN/WIFI」",
                        "操作指南：\n1.找到「打开WLAN/WIFI」相关按钮并关闭对应开关\n2.操作完成后点击返回键返回应用", Toast.LENGTH_LONG);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsFirstOnResume) {
            mIsFirstOnResume = false;
        } else {
            invokeCallback(checkUserDoneIsWeExcepted());
        }

    }

    private boolean checkUserDoneIsWeExcepted() {
        boolean userDoneIsWeExcepted = false;
        if (mUserActionCode == USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION) {
            userDoneIsWeExcepted = !WifiUtils.isUserForbidWifiPermission();
        } else if (mUserActionCode == USER_ACTION_CODE_REQUEST_LOCATION_PERMISSION) {
            userDoneIsWeExcepted = LocationUtils.checkHasLocationPermissions();
        } else if (mUserActionCode == USER_ACTION_CODE_ENABLE_LOCATION_MODULE) {
            userDoneIsWeExcepted = LocationUtils.isLocationEnabled();
        } else if (mUserActionCode == USER_ACTION_CODE_GUIDE_USER_GRANT_WIFI_AND_LOCATION_PERMISSION) {
            userDoneIsWeExcepted = !WifiUtils.isUserForbidWifiPermission() && LocationUtils.checkHasLocationPermissions();
        } else if (mUserActionCode == USER_ACTION_CODE_ENABLE_WIFI_MODULE) {
            //because set wifi enabled is async ，so force true.
            userDoneIsWeExcepted = true;
        } else if (mUserActionCode == USER_ACTION_CODE_DISABLE_WIFI_MODULE) {
            //because set wifi enabled is async ，so force true.
            userDoneIsWeExcepted = true;
        }
        return userDoneIsWeExcepted;
    }


    public static void setOnUserActionDoneCallback(OnUserActionDoneCallback onUserActionDoneCallback) {
        if (sOnUserActionDoneCallbacks == null) {
            sOnUserActionDoneCallbacks = new HashMap<>(5);
        }
        sOnUserActionDoneCallbacks.put(onUserActionDoneCallback.hashCode(), onUserActionDoneCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResult(int reqCode, String[] permissions, boolean result, int[] grantResults) {

        if (reqCode == USER_ACTION_CODE_REQUEST_LOCATION_PERMISSION) {
            invokeCallback(result);
        }
    }

    private void invokeCallback(boolean userDoneIsWeExcepted) {
        finish();
        try {
            OnUserActionDoneCallback onUserDoneCallback = sOnUserActionDoneCallbacks.get(mUserActionDoneCallbackId);
            if (userDoneIsWeExcepted) {
                onUserDoneCallback.onUserActionDoneIsWeExcepted();
            } else {
                onUserDoneCallback.onUserActionDoneIsNotWeExcepted();
            }
        } finally {
            sOnUserActionDoneCallbacks.remove(mUserActionDoneCallbackId);
        }
    }

    public interface OnUserActionDoneCallback {

        void onUserActionDoneIsWeExcepted();

        void onUserActionDoneIsNotWeExcepted();
    }

}
