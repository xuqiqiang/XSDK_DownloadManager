/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.core;

import android.util.Log;

import com.snailstudio.xsdk.downloadmanager.ui.DownloadDialog;
import com.snailstudio.xsdk.downloadmanager.ui.DownloadNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class DownloadUIManager {

    private static final String TAG = DownloadUIManager.class.getSimpleName();

    private static HashMap<Integer, OnDownloadListener> mOnDownloadListenerList;
    private static HashMap<Integer, DownloadDialog> mDownloadDialogList;
    private static HashMap<Integer, DownloadNotification> mDownloadNotificationList;

    private static void initOnDownloadListenerList() {
        if (mOnDownloadListenerList == null)
            mOnDownloadListenerList = new HashMap<>();
    }

    private static void initDownloadDialogList() {
        if (mDownloadDialogList == null)
            mDownloadDialogList = new HashMap<>();
    }

    private static void initDownloadNotificationList() {
        if (mDownloadNotificationList == null)
            mDownloadNotificationList = new HashMap<>();
    }

    public static void setOnDownloadListener(int id, OnDownloadListener listener) {
        initOnDownloadListenerList();
        mOnDownloadListenerList.put(id, listener);
    }

    public static OnDownloadListener getOnDownloadListener(int id) {
        initOnDownloadListenerList();
        return mOnDownloadListenerList.get(id);
    }

    public static DownloadDialog getDownloadDialog(int id) {
        initDownloadDialogList();
        return mDownloadDialogList.get(id);
    }

    public static DownloadNotification getDownloadNotification(int id) {
        initDownloadNotificationList();
        return mDownloadNotificationList.get(id);
    }

    public static List<OnDownloadListener> getListeners(int id) {
        Log.d(TAG, "getListeners");
        List<OnDownloadListener> listeners = new ArrayList<>();
        OnDownloadListener listener = getOnDownloadListener(id);
        if (listener != null)
            listeners.add(listener);
        DownloadDialog mDownloadDialog = getDownloadDialog(id);
        if (mDownloadDialog != null)
            listeners.add(mDownloadDialog);
        DownloadNotification mDownloadNotification = getDownloadNotification(id);
        if (mDownloadNotification != null)
            listeners.add(mDownloadNotification);
        return listeners;
    }

    public static DownloadDialog setDownloadDialog(int id, DownloadDialog downloadDialog) {
        initDownloadDialogList();
        mDownloadDialogList.put(id, downloadDialog);
        return downloadDialog;
    }

    public static DownloadNotification setDownloadNotification(int id, DownloadNotification downloadNotification) {
        initDownloadNotificationList();
        mDownloadNotificationList.put(id, downloadNotification);
        return downloadNotification;
    }
}
