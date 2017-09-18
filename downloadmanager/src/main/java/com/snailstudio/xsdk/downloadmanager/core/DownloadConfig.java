/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.snailstudio.xsdk.downloadmanager.utils.FileUtils;

import java.io.File;
import java.net.Proxy;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class DownloadConfig {

    public static final String DOWNLOAD_FILE_PATH = "download_cache";
    public static final String DOWNLOAD_FILE_NAME = "test";
    private static DownloadConfig mDefaultConfig;
    Context context;
    String url;
    String path;
    String name;
    boolean pathAsDirectory;
    boolean showDialog;
    boolean showNotification;
    boolean canPause;
    boolean showInfo;
    boolean isWifiRequired;
    int callbackProgressTime = 300;
    int minIntervalUpdateSpeed = 400;
    int autoRetryTimes = 3;

    public DownloadConfig() {
        this.path = getDefaultPath();
    }

    public static DownloadConfig newDownloadConfig() {
        if (mDefaultConfig != null) {
            return mDefaultConfig.clone();
        }
        return new DownloadConfig();
    }

    public static void init(Application application) {
        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader#init(Context)
         */
        FileDownloader.setupOnApplicationOnCreate(application)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                        .proxy(Proxy.NO_PROXY) // set proxy
                ))
                .commit();
    }

    private static String getDefaultPath() {

        FileUtils.createDir(DOWNLOAD_FILE_PATH);

        String filePath = FileUtils.getRealFilePath(DOWNLOAD_FILE_PATH
                + File.separator + DOWNLOAD_FILE_NAME);

        File file = new File(filePath);
        if (file.exists())
            file.delete();
        return filePath;
    }

    @Override
    protected DownloadConfig clone() {
        DownloadConfig config = new DownloadConfig();
        config.context = context;
        config.url = url;
        config.path = path;
        config.pathAsDirectory = pathAsDirectory;
        config.showDialog = showDialog;
        config.showNotification = showNotification;
        config.canPause = canPause;
        config.showInfo = showInfo;
        config.isWifiRequired = isWifiRequired;
        config.callbackProgressTime = callbackProgressTime;
        config.minIntervalUpdateSpeed = minIntervalUpdateSpeed;
        config.autoRetryTimes = autoRetryTimes;
        return config;
    }

    public DownloadConfig defaultConfig() {
        mDefaultConfig = this;
        // Avoid memory leak
        mDefaultConfig.context(null);
        return this;
    }

    public DownloadConfig context(Context context) {
        this.context = context;
        return this;
    }

    public DownloadConfig url(String url) {
        this.url = url;
        return this;
    }

    public DownloadConfig path(String path, boolean pathAsDirectory) {
        this.path = path;
        this.pathAsDirectory = pathAsDirectory;
        return this;
    }

    public DownloadConfig name(String name) {
        this.name = name;
        return this;
    }

    public DownloadConfig canPause(boolean canPause) {
        this.canPause = canPause;
        return this;
    }

    public DownloadConfig showInfo(boolean showInfo) {
        this.showInfo = showInfo;
        return this;
    }

    public DownloadConfig isWifiRequired(boolean isWifiRequired) {
        this.isWifiRequired = isWifiRequired;
        return this;
    }

    public DownloadConfig callbackProgressCount(int callbackProgressTime) {
        this.callbackProgressTime = callbackProgressTime;
        return this;
    }

    public DownloadConfig minIntervalUpdateSpeed(int minIntervalUpdateSpeed) {
        this.minIntervalUpdateSpeed = minIntervalUpdateSpeed;
        return this;
    }

    public DownloadConfig autoRetryTimes(int autoRetryTimes) {
        this.autoRetryTimes = autoRetryTimes;
        return this;
    }

    public DownloadConfig showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    public DownloadConfig showDialog(boolean showDialog) {
        this.showDialog = showDialog;
        return this;
    }

    public DownloadManager create() {
        if (context == null) {
            throw new RuntimeException("The context is null");
        }
        if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("The url is empty");
        }
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("The path is empty");
        }
        if (showDialog && !(context instanceof Activity)) {
            throw new RuntimeException("The context should be a activity");
        }
        return new DownloadManager(this);
    }

}
