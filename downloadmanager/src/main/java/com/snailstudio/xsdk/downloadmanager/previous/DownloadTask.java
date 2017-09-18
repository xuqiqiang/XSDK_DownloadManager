/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.previous;

import android.text.TextUtils;
import android.util.Log;

import com.snailstudio.xsdk.downloadmanager.core.OnDownloadListener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class DownloadTask extends Thread {
    private static final String TAG = "DownloadTask";
    private static final long DELAY = 200;
    private String mUrl;
    private String filePath;
    private boolean isRunning;
    private OnDownloadListener listener;
    private long testTime;
    private List<DownloadThread> fds;
    private int threadNum;
    private long fileSize;
    private long mDownloadedSize;
    private long startTime;
    private ExecutorService mExecutorService = Executors.newCachedThreadPool();

    public DownloadTask(String url, String filePath,
                        int threadNum, long testTime,
                        OnDownloadListener listener) {
        this.mUrl = url;
        this.filePath = filePath;
        this.threadNum = threadNum;
        this.testTime = testTime;
        this.listener = listener;
    }

    public void stopDownload() {
        isRunning = false;
        if (fds != null) {
            for (DownloadThread dt : fds) {
                dt.stopStream();
            }
        }
    }

    @Override
    public void run() {
        isRunning = true;
        URL url;
        try {
            url = new URL(mUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "Network disable");
            if (listener != null)
                listener.onError("Network disable");
            stopDownload();
            return;
        }

        fileSize = getFileSize(url);
        if (fileSize <= 0) {
            Log.e(TAG, "Network disable");
            if (listener != null)
                listener.onError("Network disable");
            stopDownload();
            return;
        }
        if (listener != null)
            listener.onStart(fileSize);
        File file = createFile();
        startDownloadThread(url, file, fileSize);
        checkComplete(file, fileSize);

    }

    private long getFileSize(URL url) {
        try {
            URLConnection conn = url.openConnection();
//            String fileName = conn.getHeaderField(6);
//            fileName = URLDecoder.decode(fileName.substring(fileName.indexOf("filename=")+9),"UTF-8");
//            Log.d("test", "fileName:" + fileName);
            return conn.getContentLength();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private File createFile() {
        File file = null;
        if (!TextUtils.isEmpty(filePath)) {
            file = new File(filePath);
            if (file.exists())
                file.delete();
        }
        return file;
    }

    private void startDownloadThread(URL url, File file, long fileSize) {
        long blockSize = fileSize / threadNum;
        fds = new ArrayList<>();
        for (int i = 0; i < threadNum; i++) {
            long endPosition = (i + 1) * blockSize - 1;
            if (i == threadNum - 1)
                endPosition = fileSize - 1;
            DownloadThread fdt = new DownloadThread(url, file, i
                    * blockSize, endPosition);
            fdt.setName("Thread" + i);
            mExecutorService.execute(fdt);
            fds.add(fdt);
        }
    }

    private void checkComplete(File file, long fileSize) {
        startTime = System.currentTimeMillis();

        try {
            boolean finished;
            while (true) {
                if (!isRunning)
                    return;
                long downloadedSize = 0;
                finished = true;
                for (DownloadThread dt : fds) {
                    downloadedSize += dt.getDownloadSize();
                    if (!dt.isFinished()) {
                        finished = false;
                    } else if (dt.isError()) {
                        if (listener != null)
                            listener.onError("Network disable");
                        stopDownload();
                        return;
                    }
                }
                Log.d(TAG, "progress:"
                        + (Double.valueOf((downloadedSize * 1.0
                        / fileSize * 100))).intValue() + "%"
                        + ", fileSize:" + fileSize
                        + ", downloadedSize:" + downloadedSize);
                long time = System.currentTimeMillis() - startTime;
                if (finished) {
                    if (listener != null)
                        listener.onComplete(
                                file.getPath(), time, fileSize);
                    stopDownload();
                    return;
                }

                if (listener != null) {
                    listener.onProcess(fileSize,
                            downloadedSize,
                            (double) (downloadedSize - mDownloadedSize) / (double) DELAY);
                }
                mDownloadedSize = downloadedSize;

                if (testTime > 0) {
                    if (time >= testTime) {
                        if (listener != null)
                            listener.onComplete(
                                    file.getPath(), time,
                                    downloadedSize);
                        stopDownload();
                        return;
                    }
                }
                try {
                    sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            mExecutorService.shutdown();
        }
    }
}