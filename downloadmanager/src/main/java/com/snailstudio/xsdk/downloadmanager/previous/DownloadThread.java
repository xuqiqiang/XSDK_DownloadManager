/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.previous;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class DownloadThread implements Runnable {
    private static final String TAG = "DownloadThread";
    private static final int BUFFER_SIZE = 4 * 1024;
    private URL url;
    private File file;
    private long startPosition;
    private long endPosition;
    private long curPosition;
    private boolean finished;
    private boolean error;
    private long downloadSize = 0;

    private BufferedInputStream bis;
    private RandomAccessFile fos;
    private String name;

    public DownloadThread(URL url, File file, long startPosition,
                          long endPosition) {
        this.url = url;
        this.file = file;
        this.startPosition = startPosition;
        this.curPosition = startPosition;
        this.endPosition = endPosition;
    }

    public void stopStream() {
        try {
            if (bis != null) {
                bis.close();
                bis = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    @Override
    public void run() {

        byte[] buf = new byte[BUFFER_SIZE];
        URLConnection con;
        try {

            con = url.openConnection();
            con.setAllowUserInteraction(true);
            con.setRequestProperty("Range", "bytes=" + startPosition + "-"
                    + endPosition);
            if (file != null) {
                fos = new RandomAccessFile(file, "rw");
                fos.seek(startPosition);
            }
            bis = new BufferedInputStream(con.getInputStream());
            while (curPosition < endPosition) {
                int len = bis.read(buf, 0, BUFFER_SIZE);
                if (len == -1) {
                    break;
                }
                if (fos != null) {
                    fos.write(buf, 0, len);
                }
                curPosition += len;
                if (curPosition > endPosition) {
                    downloadSize += len - (curPosition - endPosition) + 1;
                } else {
                    downloadSize += len;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, getName() + " Error:" + e.getMessage());
            error = true;
        } finally {
            this.finished = true;
            stopStream();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isError() {
        return error;
    }

    public long getDownloadSize() {
        return downloadSize;
    }
}
