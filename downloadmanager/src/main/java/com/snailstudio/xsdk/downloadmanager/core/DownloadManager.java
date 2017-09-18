/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.core;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.snailstudio.xsdk.downloadmanager.R;
import com.snailstudio.xsdk.downloadmanager.ui.DownloadDialog;
import com.snailstudio.xsdk.downloadmanager.ui.DownloadNotification;
import com.snailstudio.xsdk.downloadmanager.utils.Magnet;
import com.snailstudio.xsdk.downloadmanager.utils.NetSpeedUtils;
import com.snailstudio.xsdk.downloadmanager.utils.NotificationsUtils;

import java.io.File;

import static com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager.getDownloadDialog;
import static com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager.getDownloadNotification;
import static com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager.getListeners;
import static com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager.getOnDownloadListener;
import static com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager.setDownloadDialog;
import static com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager.setDownloadNotification;
import static com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager.setOnDownloadListener;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class DownloadManager {

    private static final int STATUS_IDLE = 0, STATUS_STARTING = 1, STATUS_COMPLETE = 2, STATUS_ERROR = 3;
    private static final String TAG = DownloadManager.class.getSimpleName();
    private static int mId = 10000;

    private BaseDownloadTask mDownloadTask;
    private long mFileSize;
    private long mDownloadedSize;
    private int id = mId++;

    private Context context;
    private DownloadConfig config;

    private long mStartTime;
    private long mDownloadTime;
    private int status = STATUS_IDLE;

    private String mErrorMessage;

    DownloadManager(DownloadConfig config) {
        this.config = config;
        this.context = config.context;

        mDownloadTask = FileDownloader.getImpl().create(Magnet.parseUrl(config.url))
                .setPath(config.path, config.pathAsDirectory)
                .setCallbackProgressTimes(config.callbackProgressTime)
                .setMinIntervalUpdateSpeed(config.minIntervalUpdateSpeed)
                .setAutoRetryTimes(config.autoRetryTimes)
                .setWifiRequired(config.isWifiRequired)
                .setListener(new FileDownloadLargeFileListener() {

                    /**
                     * Entry queue, and pending
                     *
                     * @param task       Current task
                     * @param soFarBytes Already downloaded bytes stored in the db
                     * @param totalBytes Total bytes stored in the db
                     */
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.i(TAG, "pending soFarBytes:" + soFarBytes + ", totalBytes:" + totalBytes);
                    }

                    /**
                     * Connected
                     *
                     * @param task       Current task
                     * @param etag       ETag
                     * @param isContinue Is resume from breakpoint
                     * @param soFarBytes Number of bytes download so far
                     * @param totalBytes Total size of the download in bytes
                     */
                    protected void connected(final BaseDownloadTask task, final String etag,
                                             final boolean isContinue,
                                             final long soFarBytes, final long totalBytes) {
                        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                        Log.i(TAG, "connected etag:" + etag + ", isContinue:"
                                + isContinue + ", soFarBytes:" + soFarBytes + ", totalBytes:" + totalBytes);

                        status = STATUS_STARTING;
                        mFileSize = totalBytes;
                        for (OnDownloadListener listener : getListeners(id)) {
                            listener.onStart(mFileSize);
                        }
                    }

                    /**
                     * @param task       Current task
                     * @param soFarBytes Number of bytes download so far
                     * @param totalBytes Total size of the download in bytes
                     */
                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.i(TAG, "progress soFarBytes:" + soFarBytes + ", totalBytes:" + totalBytes
                                + ", getSpeed:" + task.getSpeed());

                        mDownloadedSize = soFarBytes;
                        long speed = task.getSpeed();
                        for (OnDownloadListener listener : getListeners(id)) {
                            listener.onProcess(totalBytes, soFarBytes, speed);
                        }
                    }

                    /**
                     * Download paused
                     *
                     * @param task       Current task
                     * @param soFarBytes Number of bytes download so far
                     * @param totalBytes Total size of the download in bytes
                     */
                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.i(TAG, "paused soFarBytes:" + soFarBytes + ", totalBytes:" + totalBytes);
                        mDownloadedSize = soFarBytes;
                        for (OnDownloadListener listener : getListeners(id)) {
                            listener.onPaused(totalBytes, soFarBytes);
                        }
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.i(TAG, "completed");
                        status = STATUS_COMPLETE;
                        mDownloadedSize = mFileSize;
                        mDownloadTime += System.currentTimeMillis() - mStartTime;
                        for (OnDownloadListener listener : getListeners(id)) {
                            listener.onComplete(task.getTargetFilePath(), mDownloadTime, mDownloadedSize);
                        }
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.i(TAG, "error:" + e.getMessage());
                        e.printStackTrace();
                        mErrorMessage = e.getMessage();
                        if (mErrorMessage != null && mErrorMessage.startsWith("unexpected end of stream")) {
                            File targetFile = new File(task.getTargetFilePath());
                            if (targetFile.exists())
                                targetFile.delete();
                            File tempFile = new File(task.getTargetFilePath() + ".temp");
                            if (tempFile.exists())
                                tempFile.renameTo(targetFile);
                            completed(task);
                            return;
                        }
                        status = STATUS_ERROR;
                        DownloadNotification downloadNotification = getDownloadNotification(id);
                        if (downloadNotification != null) {
                            downloadNotification.show();
                            downloadNotification.onProcess(mFileSize, mDownloadedSize, task.getSpeed());
                        }
                        for (OnDownloadListener listener : getListeners(id)) {
                            listener.onError(mErrorMessage);
                        }
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.i(TAG, "warn");
                        error(task, new RuntimeException("Some same task is running"));
                    }

                });
    }

    public void start(OnDownloadListener listener) {

        if (mDownloadTask != null && mDownloadTask.isRunning())
            return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }

        setOnDownloadListener(id, listener);

        showDownloadUI();
        try {
            mDownloadTask.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mStartTime = System.currentTimeMillis();
        mDownloadTime = 0;
    }

    public void pause() {
        if (!config.canPause)
            return;
        if (mDownloadTask != null)
            mDownloadTask.pause();
        mDownloadTime += System.currentTimeMillis() - mStartTime;
    }

    public void resume() {
        if (mDownloadTask != null) {
            mDownloadTask.reuse();
            try {
                mDownloadTask.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mStartTime = System.currentTimeMillis();
        }
    }

    private void showDownloadUI() {

        if (!config.showDialog && !config.showNotification)
            return;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (config.showDialog) {
                    DownloadDialog downloadDialog = createDownloadDialog();
                    try {
                        downloadDialog.show();
                    } catch (WindowManager.BadTokenException e) {
                        setDownloadDialog(id, null);
                    }
                }

                if (config.showNotification) {
                    DownloadNotification downloadNotification = createDownloadNotification();
                    if (config.showDialog && getDownloadDialog(id) != null) {
                        downloadNotification.close();
                    } else {
                        if (!NotificationsUtils.isNotificationEnabled(context)) {
                            NotificationsUtils.getAppDetailSettingIntent(context);
                            Toast.makeText(context, R.string.download_notification_permission, Toast.LENGTH_LONG).show();
                            return;
                        }
                        downloadNotification.show();
                    }
                }
            }
        });

    }

    private DownloadDialog createDownloadDialog() {
        Log.d(TAG, "canPause:" + config.canPause);
        DownloadDialog mDownloadDialog = new DownloadDialog(context,
                config.name,
                config.canPause,
                config.showInfo,
                new DownloadDialog.OnDownloadDialogListener() {
                    @Override
                    public boolean onMoveToBack() {
                        if (config.showNotification) {
                            if (!NotificationsUtils.isNotificationEnabled(context)) {
                                NotificationsUtils.getAppDetailSettingIntent(context);
                                Toast.makeText(context, R.string.download_notification_permission, Toast.LENGTH_LONG).show();
                                return false;
                            }
                            DownloadNotification mDownloadNotification = getDownloadNotification(id);
                            if (mDownloadNotification != null) {
                                mDownloadNotification.show();
                                if (mDownloadTask != null) {
                                    if (mDownloadTask.getStatus() == FileDownloadStatus.paused) {
                                        mDownloadNotification.onPaused(mFileSize, mDownloadedSize);
                                    } else {
                                        mDownloadNotification.onProcess(mFileSize, mDownloadedSize, mDownloadTask.getSpeed());
                                    }
                                }
                            }
                        }
                        return true;
                    }

                    @Override
                    public void onPause() {
                        pause();
                    }

                    @Override
                    public void onResume() {
                        resume();
                    }

                    @Override
                    public void onStop() {
                        stop();
                    }
                });
        return setDownloadDialog(id, mDownloadDialog);
    }

    private DownloadNotification createDownloadNotification() {
        DownloadNotification downloadNotification = new DownloadNotification(
                context, id, config.name, config.showInfo,
                new DownloadNotification.OnDownloadNotificationListener() {

                    @Override
                    public void onClick() {
                        OnDownloadListener listener = getOnDownloadListener(id);
                        if (status == STATUS_COMPLETE) {
                            if (listener != null)
                                listener.onComplete(mDownloadTask.getTargetFilePath(), mDownloadTime, mDownloadedSize);
                        } else if (status == STATUS_ERROR) {
                            if (listener != null)
                                listener.onError(mErrorMessage);
                        } else {
                            if (config.showDialog) {
                                DownloadDialog downloadDialog = getDownloadDialog(id);
                                if (downloadDialog != null) {
                                    try {
                                        downloadDialog.show();
                                        if (mDownloadTask.getStatus() == FileDownloadStatus.paused) {
                                            downloadDialog.onPaused(mFileSize, mDownloadedSize);
                                        } else {
                                            downloadDialog.onProcess(mFileSize, mDownloadedSize, mDownloadTask.getSpeed());
                                        }
                                    } catch (WindowManager.BadTokenException e) {
                                        setDownloadDialog(id, null);
                                        if (mDownloadTask != null
                                                && mDownloadTask.getStatus() == FileDownloadStatus.paused) {
                                            resume();
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

        return setDownloadNotification(id, downloadNotification);
    }

    public void stop() {
        if (mDownloadTask != null)
            mDownloadTask.pause();
    }

    public String getDownloadRate() {
        if (mFileSize == 0)
            return "---";
        else
            return (Double.valueOf((mDownloadedSize * 1.0
                    / mFileSize * 100))).intValue() + "%";
    }

    public String getSpeed() {
        return NetSpeedUtils.getSpeed(mDownloadTask.getSpeed());
    }
}