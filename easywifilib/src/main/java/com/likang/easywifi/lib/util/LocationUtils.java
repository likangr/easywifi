package com.likang.easywifi.lib.util;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import java.util.List;


/**
 * @author likangren
 */
public class LocationUtils {

    private static final String TAG = "LocationUtils";

    public static boolean checkLocationModuleIsExist() {
        Application application = ApplicationHolder.getApplication();
        PackageManager packageManager = application.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
            return false;
        }
        LocationManager locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        List<String> providers = locationManager.getAllProviders();
        return providers.contains(LocationManager.GPS_PROVIDER) || providers.contains(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) ApplicationHolder.getApplication().getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }


    public static boolean checkHasLocationPermissions() {
        return PermissionsManager.check(
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION});
    }

    public static boolean isUserForbidLocationPermissions(Activity activity) {

        boolean isUserForbidLocationPermissions = !PermissionsManager.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION, activity)
                & !PermissionsManager.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION, activity);
        Logger.d(TAG, "isUserForbidLocationPermissions=" + isUserForbidLocationPermissions);
        return isUserForbidLocationPermissions;
    }

}
