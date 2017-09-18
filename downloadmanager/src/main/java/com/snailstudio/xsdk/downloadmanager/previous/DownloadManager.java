/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.previous;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.snailstudio.xsdk.downloadmanager.R;
import com.snailstudio.xsdk.downloadmanager.core.DownloadUIManager;
import com.snailstudio.xsdk.downloadmanager.core.OnDownloadListener;
import com.snailstudio.xsdk.downloadmanager.ui.DownloadDialog;
import com.snailstudio.xsdk.downloadmanager.ui.DownloadNotification;
import com.snailstudio.xsdk.downloadmanager.utils.FileUtils;
import com.snailstudio.xsdk.downloadmanager.utils.Magnet;
import com.snailstudio.xsdk.downloadmanager.utils.NetSpeedUtils;
import com.snailstudio.xsdk.downloadmanager.utils.NotificationsUtils;

import java.io.File;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
@Deprecated
public class DownloadManager {
    public static final String DOWNLOAD_FILE_PATH = "download_cache";
    public static final String DOWNLOAD_FILE_NAME = "test";
    public static final int STATUS_IDLE = 0, STATUS_STARTING = 1, STATUS_COMPLETE = 2, STATUS_ERROR = 3;
    private static final String TAG = DownloadManager.class.getSimpleName();
    private static int mId = 10000;
    private long mFileSize;
    private long mDownloadedSize;
    private double mSpeed;
    private DownloadTask mDownloadTask;
    private int id = mId++;
    private Context context;
    private String url;
    private String mDirPath = DOWNLOAD_FILE_PATH;
    private String mFileName = DOWNLOAD_FILE_NAME;
    private String mFilePath;
    private int threadSum = 3;
    private long testTime;
    private long mTime;
    private int status = STATUS_IDLE;

    private OnDownloadListener mListener;

    private DownloadDialog mDownloadDialog;
    private DownloadNotification mDownloadNotification;

    public DownloadManager(Context context, String url) {
        this.context = context;
        if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("The url is empty");
        }
        this.url = Magnet.parseUrl(url);
        if (Magnet.isMagnetLink(url)) {
            this.threadSum = 1;
        }
    }

    public DownloadManager(Context context, String url, String dirPath,
                           String fileName, int threadSum, long testTime) {
        this(context, url);
        this.mDirPath = dirPath;
        this.mFileName = fileName;
        this.threadSum = threadSum;
        this.testTime = testTime;
        if (Magnet.isMagnetLink(url)) {
            this.threadSum = 1;
        }
    }

    public void start(OnDownloadListener listener) {

        if (mDownloadTask != null && mDownloadTask.isAlive())
            return;

        if (TextUtils.isEmpty(mDirPath) ||
                TextUtils.isEmpty(mFileName))
            return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android
                    .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }

        FileUtils.createDir(mDirPath);

        showDownloadDialog();

        mListener = listener;
        mFilePath = FileUtils.getRealFilePath(mDirPath
                + File.separator + mFileName);
        mDownloadTask = new DownloadTask(url, mFilePath, threadSum, testTime,
                new OnDownloadListener() {

                    @Override
                    public void onStart(long fileSize) {
                        Log.d(TAG, "onStart url:" + url);
                        status = STATUS_STARTING;
                        mFileSize = fileSize;
                        if (mListener != null)
                            mListener.onStart(fileSize);
                        if (mDownloadDialog != null)
                            mDownloadDialog.onStart(fileSize);
                        if (mDownloadNotification != null)
                            mDownloadNotification.onStart(fileSize);
                    }

                    @Override
                    public void onError(String message) {
                        status = STATUS_ERROR;
                        mSpeed = 0;
                        if (mListener != null)
                            mListener.onError(message);
                        if (mDownloadDialog != null) {
                            mDownloadDialog.onError(message);
                        }
                        if (mDownloadNotification != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mDownloadNotification.show();
                                }
                            });
                            mDownloadNotification.onProcess(mFileSize, mDownloadedSize, mSpeed);
                            mDownloadNotification.onError(message);
                        }

                    }

                    @Override
                    public void onComplete(String downloadPath, long time, long downloadedSize) {
                        status = STATUS_COMPLETE;
                        mTime = time;
                        mDownloadedSize = downloadedSize;
                        mSpeed = 0;
                        if (mListener != null)
                            mListener.onComplete(downloadPath, time, downloadedSize);
                        if (mDownloadDialog != null)
                            mDownloadDialog.onComplete(downloadPath, time, downloadedSize);
                        if (mDownloadNotification != null)
                            mDownloadNotification.onComplete(downloadPath, time, downloadedSize);
                    }

                    @Override
                    public void onPaused(long fileSize, long downloadedSize) {

                    }

                    @Override
                    public void onProcess(long fileSize, long downloadedSize, double speed) {
                        Log.i(TAG, "onProcess fileSize:" + fileSize + ", downloadedSize:" + downloadedSize + ", speed:" + speed);
                        mDownloadedSize = downloadedSize;
                        mSpeed = speed;
                        if (mListener != null)
                            mListener.onProcess(fileSize, downloadedSize, speed);
                        if (mDownloadDialog != null)
                            mDownloadDialog.onProcess(fileSize, downloadedSize, speed);
                        if (mDownloadNotification != null)
                            mDownloadNotification.onProcess(fileSize, downloadedSize, speed);
                    }
                });
        mDownloadTask.start();
    }

    private void showDownloadDialog() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mDownloadDialog = DownloadUIManager.getDownloadDialog(id);

                if (mDownloadDialog == null) {
                    mDownloadDialog = new DownloadDialog(context,
                            null,
                            true,
                            true,
                            new DownloadDialog.OnDownloadDialogListener() {
                                @Override
                                public boolean onMoveToBack() {
                                    if (!NotificationsUtils.isNotificationEnabled(context)) {
                                        NotificationsUtils.getAppDetailSettingIntent(context);
                                        Toast.makeText(context, R.string.download_notification_permission, Toast.LENGTH_LONG).show();
                                        return false;
                                    }
                                    DownloadNotification mDownloadNotification = DownloadUIManager.getDownloadNotification(id);
                                    if (mDownloadNotification != null) {
                                        mDownloadNotification.show();
                                        mDownloadNotification.onProcess(mFileSize, mDownloadedSize, mSpeed);
                                    }
                                    return true;
                                }

                                @Override
                                public void onPause() {

                                }

                                @Override
                                public void onResume() {

                                }

                                @Override
                                public void onStop() {
                                    stop();
                                }
                            });

                    DownloadUIManager.setDownloadDialog(id, mDownloadDialog);
                }
                mDownloadDialog.show();
//                if (Build.VERSION.SDK_INT >= 23) {
//                    if(!Settings.canDrawOverlays(context)) {
//                        context.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
//                        return;
//                    } else {
//                        mDownloadDialog.show();
//                    }
//                } else {
//                    mDownloadDialog.show();
//                }

                mDownloadNotification = DownloadUIManager.getDownloadNotification(id);
                if (mDownloadNotification == null) {
                    mDownloadNotification = new DownloadNotification(context, id, null, true, new DownloadNotification.OnDownloadNotificationListener() {

                        @Override
                        public void onClick() {
                            if (status == STATUS_COMPLETE) {
                                if (mListener != null)
                                    mListener.onComplete(mFilePath, mTime, mDownloadedSize);
                            } else if (status == STATUS_ERROR) {

                            } else {
                                DownloadDialog mDownloadDialog = DownloadUIManager.getDownloadDialog(id);
                                if (mDownloadDialog != null) {
                                    mDownloadDialog.show();
                                    mDownloadDialog.onProcess(mFileSize, mDownloadedSize, mSpeed);
                                }
                            }
                        }
                    });

                    DownloadUIManager.setDownloadNotification(id, mDownloadNotification);
                }
                mDownloadNotification.close();
            }
        });


    }

    public void stop() {
        mDownloadTask.stopDownload();
    }

    public String getDownloadRate() {
        if (mFileSize == 0)
            return "---";
        else
            return (Double.valueOf((mDownloadedSize * 1.0
                    / mFileSize * 100))).intValue() + "%";
    }

    public String getSpeed() {
        return NetSpeedUtils.getSpeed(mSpeed);
    }

}