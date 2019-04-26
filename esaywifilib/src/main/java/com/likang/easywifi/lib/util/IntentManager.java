package com.likang.easywifi.lib.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.likang.easywifi.lib.core.guid.UserActionBridgeActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author likangren
 */
public class IntentManager {

    private static final String TAG = "IntentManager";


    public static boolean gotoWifiSettings(Context context) {
        Intent intent = new Intent();
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            intent.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            intent.setAction(Settings.ACTION_WIFI_SETTINGS);
            context.startActivity(intent);
            return true;
        }
    }

    public static void gotoLocationSettings(Context context) {
        Intent intent = new Intent();
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    public static void gotoUserActionBridgeActivity(int stepCode, UserActionBridgeActivity.OnUserDoneCallback onUserDoneCallback) {
        Application application = ApplicationHolder.getApplication();
        UserActionBridgeActivity.setOnUserDoneCallback(onUserDoneCallback);
        Intent intent = new Intent(application, UserActionBridgeActivity.class);
        intent.putExtra(UserActionBridgeActivity.CALLBACK_ID, onUserDoneCallback.hashCode());
        intent.putExtra(UserActionBridgeActivity.STEP_CODE, stepCode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }


    public static void gotoSelfPermissionSetting(Context context) {

        Intent intent = new Intent();
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            switch (Build.MANUFACTURER) {
                case PermissionSettingIntent.MANUFACTURER_HUAWEI:
                    PermissionSettingIntent.setHUAWEIIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_VIVO:
                    PermissionSettingIntent.setVIVOIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_OPPO:
                    PermissionSettingIntent.setOPPOIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_MEIZU:
                    PermissionSettingIntent.setMEIZUIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_XIAOMI:
                    PermissionSettingIntent.setXIAOMIIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_COOLPAD:
                    PermissionSettingIntent.setCOOLPADIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_SONY:
                    PermissionSettingIntent.setSONYIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_LG:
                    PermissionSettingIntent.setLGIntentInfo(context, intent);
                    break;
                case PermissionSettingIntent.MANUFACTURER_LETV:
                    PermissionSettingIntent.setLETVIntentInfo(context, intent);
                    break;
                default:
                    PermissionSettingIntent.setSettingIntentInfo(context, intent);
                    break;
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            PermissionSettingIntent.setSettingIntentInfo(context, intent);
            context.startActivity(intent);
        }
    }

    private static class PermissionSettingIntent {

        /**
         * Build.MANUFACTURER
         */
        private static final String MANUFACTURER_HUAWEI = "Huawei";
        private static final String MANUFACTURER_VIVO = "vivo";
        private static final String MANUFACTURER_OPPO = "OPPO";
        private static final String MANUFACTURER_MEIZU = "Meizu";
        private static final String MANUFACTURER_XIAOMI = "Xiaomi";
        private static final String MANUFACTURER_COOLPAD = "Coolpad";
        private static final String MANUFACTURER_SONY = "Sony";
        private static final String MANUFACTURER_LG = "LG";
        private static final String MANUFACTURER_LETV = "Letv";
        private static final String MANUFACTURER_SAMSUNG = "samsung";
        private static final String MANUFACTURER_ZTE = "ZTE";
        private static final String MANUFACTURER_YULONG = "YuLong";
        private static final String MANUFACTURER_LENOVO = "LENOVO";

        private static void setHUAWEIIntentInfo(Context context, Intent intent) {
            setIntentInfoByComponentName(context, intent, "com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
        }

        private static void setVIVOIntentInfo(Context context, Intent intent) throws Exception {
            setIntentInfoByPackageName(context, intent, "com.bairenkeji.icaller");
        }

        private static void setLGIntentInfo(Context context, Intent intent) {
            setIntentInfoByComponentName(context, intent, "com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
        }

        private static void setSONYIntentInfo(Context context, Intent intent) {
            setIntentInfoByComponentName(context, intent, "com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
        }


        private static String getMIUIVersion() {
            String propName = "ro.miui.ui.version.name";
            String line;
            BufferedReader input = null;
            try {
                Process p = Runtime.getRuntime().exec("getprop " + propName);
                input = new BufferedReader(
                        new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                input.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return line;
        }

        private static void setXIAOMIIntentInfo(Context context, Intent intent) {
            String romVersion = getMIUIVersion();
            String packageName = context.getPackageName();
            if ("V6".equals(romVersion) || "V7".equals(romVersion)) {
                intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                intent.putExtra("extra_pkgname", packageName);
            } else {
                intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", packageName);
            }
        }

        private static void setMEIZUIntentInfo(Context context, Intent intent) {
            intent.setAction("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", context.getPackageName());
        }


        private static void setLETVIntentInfo(Context context, Intent intent) {
            setIntentInfoByComponentName(context, intent, "com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
            intent.putExtra("packageName", context.getPackageName());
        }

        private static void setOPPOIntentInfo(Context context, Intent intent) throws Exception {
            setIntentInfoByPackageName(context, intent, "com.coloros.safecenter");
        }


        private static void setCOOLPADIntentInfo(Context context, Intent intent) throws Exception {
            setIntentInfoByPackageName(context, intent, "com.yulong.android.security:remote");
        }

        private static void setIntentInfoByComponentName(Context context, Intent intent, String pkg, String cls) {
            intent.setAction(context.getPackageName());
            ComponentName componentName = new ComponentName(pkg, cls);
            intent.setComponent(componentName);
        }

        private static void setIntentInfoByPackageName(Context context, Intent intent, String permissionSettingPackageName) throws Exception {
            Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(permissionSettingPackageName);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(launchIntentForPackage.getComponent());
        }

        private static void setSettingIntentInfo(Context context, Intent intent) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
    }

}
