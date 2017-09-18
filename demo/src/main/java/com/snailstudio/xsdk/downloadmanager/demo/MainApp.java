package com.snailstudio.xsdk.downloadmanager.demo;

import android.app.Application;

import com.snailstudio.xsdk.downloadmanager.core.DownloadConfig;

import static com.snailstudio.xsdk.downloadmanager.core.DownloadConfig.newDownloadConfig;

public class MainApp extends Application {


    private static final String TAG = MainApp.class.getSimpleName();

    public void onCreate() {

        super.onCreate();

        DownloadConfig.init(this);

        newDownloadConfig()
                .showDialog(true)
                .showNotification(true)
                .canPause(true)
                .showInfo(true)
                .isWifiRequired(false)
                .callbackProgressCount(300)
                .minIntervalUpdateSpeed(400)
                .autoRetryTimes(3)
                .defaultConfig();

    }

}
