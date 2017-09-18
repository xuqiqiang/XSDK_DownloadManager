/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.utils;

import android.text.TextUtils;
import android.util.Log;

import com.snailstudio.xsdk.downloadmanager.core.OnDownloadListener;
import com.snailstudio.xsdk.downloadmanager.previous.DownloadTask;

import java.util.Locale;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class NetSpeedUtils {
    private static final String TAG = "NetSpeed";

    private static final long TEST_TIME = 5000L;
    private static final String TEST_URL = "http://gdown.baidu.com/data/wisegame/81cce5bbe49b4301/QQ_422.apk";
    private static String lastSpeed;

    public static String getNetSpeed() {
        return lastSpeed;
    }

    public static void testNetSpeed(final OnTestListener listener) {

        if (TextUtils.isEmpty(lastSpeed)) {
            DownloadTask mDownloadTask = new DownloadTask(TEST_URL, null, 3, TEST_TIME,
                    new OnDownloadListener() {

                        @Override
                        public void onStart(long fileSize) {

                        }

                        @Override
                        public void onError(String message) {

                        }

                        @Override
                        public void onComplete(String downloadPath,
                                               long time, long downloadedSize) {
                            Log.d(TAG, "time:" + time);
                            Log.d(TAG, "downloadedSize:" + downloadedSize);
                            Log.d(TAG,
                                    "speed:"
                                            + getSpeed(time, downloadedSize));
                            if (downloadedSize > 0)
                                lastSpeed = getSpeed(time, downloadedSize);
                            listener.OnTestComplete(time, downloadedSize, lastSpeed);
                        }

                        @Override
                        public void onPaused(long fileSize, long downloadedSize) {

                        }

                        @Override
                        public void onProcess(long fileSize, long downloadedSize, double speed) {

                        }

                    });
            mDownloadTask.start();
        } else {
            listener.OnTestComplete(0, 0, lastSpeed);
        }


    }

    public static String getSpeed(long time, long size) {
        String speed = "---";
        if (time > 0) {
            speed = getSpeed((double) size / (double) time);
        }
        return speed;
    }

    // B/ms
    public static String getSpeed(double s) {
        return FileUtils.getFormatSize((long) (s * 1000f)) + "/s";
    }

    // B/ms
    public static String getRestTime(long restSize, double speed) {
        if (speed >= -0.000001 && speed <= 0.000001)
            return "--:--:--";
        int restTime = (int) (restSize / 1000f / speed);
        int hour = restTime / 3600;
        int minute = restTime / 60 % 60;
        int second = restTime % 60;
        return String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second);
    }

    public interface OnTestListener {
        void OnTestComplete(long time,
                            long downloadedSize, String speed);
    }
}