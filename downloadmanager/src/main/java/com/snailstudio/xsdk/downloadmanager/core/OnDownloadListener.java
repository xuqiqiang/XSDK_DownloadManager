/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.core;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public interface OnDownloadListener {
    void onStart(long fileSize);

    void onError(String message);

    void onComplete(String downloadPath, long time,
                    long downloadedSize);

    void onPaused(long fileSize, long downloadedSize);

    void onProcess(long fileSize, long downloadedSize, double speed);
}
