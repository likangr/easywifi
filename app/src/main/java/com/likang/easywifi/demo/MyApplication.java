package com.likang.easywifi.demo;

import android.app.Application;

import com.likang.easywifi.lib.EasyWifi;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EasyWifi.initCore(this);
    }
}
