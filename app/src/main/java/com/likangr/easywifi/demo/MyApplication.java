package com.likangr.easywifi.demo;

import android.app.Application;

import com.likangr.easywifi.lib.EasyWifi;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EasyWifi.initCore(this);
    }
}
