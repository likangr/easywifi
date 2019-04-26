package com.likang.easywifi.lib.core.guid;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.util.IntentManager;
import com.likang.easywifi.lib.util.LocationUtils;
import com.likang.easywifi.lib.util.PermissionsManager;
import com.likang.easywifi.lib.util.WifiUtils;

import java.util.HashMap;

/**
 * @author likangren
 */
public class UserActionBridgeActivity extends AppCompatActivity implements PermissionsManager.IReqListener1 {

    private static final String TAG = "UserActionBridgeActivity";

    public static final int STEP_CODE_ENABLE_LOCATION_MODULE = 1;
    public static final int STEP_CODE_REQUEST_LOCATION_PERMISSION = 2;
    public static final int STEP_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION = 3;

    public static final String STEP_CODE = "step_code";
    public static final String CALLBACK_ID = "callback_id";

    private static HashMap<Integer, OnUserDoneCallback> sOnUserDoneCallbacks;
    private int mCallbackId;
    private int mStepCode;
    private boolean mIsFirstOnResume = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }

        Intent intent = getIntent();
        mCallbackId = intent.getIntExtra(CALLBACK_ID, 0);
        mStepCode = intent.getIntExtra(STEP_CODE, 0);

        switch (mStepCode) {
            case STEP_CODE_ENABLE_LOCATION_MODULE:
                IntentManager.gotoLocationSettings(this);
                UserActionGuideToast.showGuideToast(this, "需要打开位置信息服务",
                        "操作指南：\n找到「位置信息/定位」相关按钮并打开对应开关", Toast.LENGTH_LONG);
                break;
            case STEP_CODE_REQUEST_LOCATION_PERMISSION:
                if (LocationUtils.userRejectedLocationPermissionsAndCheckedNoLongerAskOption(this)) {
                    IntentManager.gotoSelfPermissionSetting(this);
                    UserActionGuideToast.showGuideToast(this, "需要获取位置权限",
                            "操作指南：\n进入「权限」设置，找到「位置信息/定位」相关按钮并打开对应开关", Toast.LENGTH_LONG);
                } else {
                    PermissionsManager.request(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION}, STEP_CODE_REQUEST_LOCATION_PERMISSION,
                            this, this);
                }
                break;
            case STEP_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION:
                IntentManager.gotoSelfPermissionSetting(this);
                UserActionGuideToast.showGuideToast(this, "需要获取WIFI操作权限",
                        "操作指南：\n进入「权限」设置，找到「连接WLAN网络和断开连接/开启关闭WIFI」相关按钮并打开对应开关", Toast.LENGTH_LONG);
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
        if (mStepCode == STEP_CODE_GUIDE_USER_GRANT_WIFI_PERMISSION) {
            userDoneIsWeExcepted = WifiUtils.checkHasChangeWifiStatePermission(EasyWifi.getWifiManager());
        } else if (mStepCode == STEP_CODE_REQUEST_LOCATION_PERMISSION) {
            userDoneIsWeExcepted = LocationUtils.checkHasLocationPermissions();
        } else if (mStepCode == STEP_CODE_ENABLE_LOCATION_MODULE) {
            userDoneIsWeExcepted = LocationUtils.isLocationEnabled();
        }
        return userDoneIsWeExcepted;
    }


    public static void setOnUserDoneCallback(OnUserDoneCallback onUserDoneCallback) {
        if (sOnUserDoneCallbacks == null) {
            sOnUserDoneCallbacks = new HashMap<>(2);
        }
        sOnUserDoneCallbacks.put(onUserDoneCallback.hashCode(), onUserDoneCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResult(int reqCode, String[] permissions, boolean result, int[] grantResults) {

        if (reqCode == STEP_CODE_REQUEST_LOCATION_PERMISSION) {
            invokeCallback(result);
        }
    }

    private void invokeCallback(boolean userDoneIsWeExcepted) {
        finish();
        try {
            OnUserDoneCallback onUserDoneCallback = sOnUserDoneCallbacks.get(mCallbackId);
            if (userDoneIsWeExcepted) {
                onUserDoneCallback.onUserDoneIsWeExcepted(mStepCode);
            } else {
                onUserDoneCallback.onUserDoneIsNotWeExcepted(mStepCode);
            }
        } finally {
            sOnUserDoneCallbacks.remove(mCallbackId);
        }
    }

    public interface OnUserDoneCallback {

        void onUserDoneIsWeExcepted(int stepCode);

        void onUserDoneIsNotWeExcepted(int stepCode);
    }

}
