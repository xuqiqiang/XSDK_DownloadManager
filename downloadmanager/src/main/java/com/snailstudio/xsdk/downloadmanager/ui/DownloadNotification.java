/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.snailstudio.xsdk.downloadmanager.R;
import com.snailstudio.xsdk.downloadmanager.core.OnDownloadListener;

import static com.snailstudio.xsdk.downloadmanager.utils.NetSpeedUtils.getRestTime;
import static com.snailstudio.xsdk.downloadmanager.utils.NetSpeedUtils.getSpeed;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class DownloadNotification extends Notification
        implements OnDownloadListener {

    public static final String DOWNLOAD_NOTIFICATION = "com.snailstudio.xsdk.DOWNLOAD_NOTIFICATION";
    private int id;
    private Context context;
    private boolean isShowing;
    private boolean showInfo;
    private boolean hasCompleted;
    private Handler mHandler;
    private OnDownloadNotificationListener mOnDownloadNotificationListener;

    public DownloadNotification(Context context,
                                int id,
                                String name,
                                boolean showInfo,
                                OnDownloadNotificationListener onDownloadNotificationListener) {
        this.context = context;
        this.id = id;
        this.showInfo = showInfo;
        this.mOnDownloadNotificationListener = onDownloadNotificationListener;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.icon = R.drawable.download_icon;
        this.tickerText = null;

        // 通知栏显示所用到的布局文件
        this.contentView = new RemoteViews(context.getPackageName(),
                R.layout.download_notification_view);
        if (TextUtils.isEmpty(name))
            name = context.getString(R.string.downloading);
        this.contentView.setTextViewText(R.id.tv_name, name);

        Intent notificationIntent = new Intent(DOWNLOAD_NOTIFICATION);
        notificationIntent.putExtra("id", id);

        this.contentIntent = PendingIntent.getBroadcast(
                context, id, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        this.flags |= Notification.FLAG_ONGOING_EVENT;
        this.flags |= Notification.FLAG_NO_CLEAR;

    }

    public static void close(Context context, int id) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(id);
        }
    }

    public void show() {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, this);
        this.isShowing = true;
    }

    private void setText(String str) {
        this.contentView.setTextViewText(R.id.tv_progress, str);
    }

    private void setProgress(int progress) {
        this.contentView.setProgressBar(R.id.download_bar, 100, progress, false);
    }

    public void close() {
        if (!this.isShowing)
            return;
        this.isShowing = false;
        close(context, id);
    }

    public boolean isCompleted() {
        return hasCompleted;
    }

    @Override
    public void onStart(long fileSize) {

    }

    @Override
    public void onError(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                hasCompleted = true;
                flags = Notification.FLAG_AUTO_CANCEL;
                setText(message);
                contentView.setTextColor(R.id.tv_progress, Color.RED);
                show();
            }
        });
    }

    @Override
    public void onComplete(String downloadPath, long time, long downloadedSize) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                hasCompleted = true;
                flags = Notification.FLAG_AUTO_CANCEL;
                setProgress(100);
                String message = context.getString(R.string.download_progress) + "100%";
                if (showInfo) {
                    message += "   00:00:00   0KB/s";
                }
                setText(message);
                show();
            }
        });
    }

    @Override
    public void onPaused(long fileSize, long downloadedSize) {
        if (!DownloadNotification.this.isShowing)
            return;
        int progress = (Double.valueOf((downloadedSize * 100.0 / fileSize))).intValue();
        setProgress(progress);
        String message = context.getString(R.string.download_progress) + progress + "%";
        if (showInfo) {
            message += "   --:--:--   0KB/s";
        }
        setText(message);
        show();
    }

    @Override
    public void onProcess(final long fileSize, final long downloadedSize, final double speed) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!DownloadNotification.this.isShowing)
                    return;
                int progress = (Double.valueOf((downloadedSize * 100.0 / fileSize))).intValue();
                setProgress(progress);
                String message = context.getString(R.string.download_progress) + progress + "%";
                if (showInfo) {
                    message += "   " + getRestTime(fileSize - downloadedSize, speed) + "   " + getSpeed(speed);
                }
                setText(message);
                show();
            }
        });
    }

    public void onClick() {
        if (mOnDownloadNotificationListener != null)
            mOnDownloadNotificationListener.onClick();
    }

    public interface OnDownloadNotificationListener {
        void onClick();
    }
}