/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.Locale;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class FileUtils {
    public static final String dir = "MediaDevice";
    private static final String[] UNIT_SIZE = {"B", "KB", "MB", "GB", "TB"};
    public static String rootName = Environment.getExternalStorageDirectory()
            .getPath();
    private static FileUtils instance;
    private Context context;

    private FileUtils(Context context) {
        this.context = context;
        setRootName(dir);
    }

    public static FileUtils getInstance(Context context) {
        if (instance == null)
            instance = new FileUtils(context);
        return instance;
    }

    public static void setRootName(String name) {
        if (!TextUtils.isEmpty(name))
            rootName = Environment.getExternalStorageDirectory().getPath()
                    + File.separator + name;
        else
            rootName = Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * @param path CloudPath
     */
    public static void createDir(String path) {
        if (TextUtils.isEmpty(path))
            return;

        if (path.startsWith(File.separator))
            path = path.substring(1);

        String dir_name_list[] = path.split(File.separator);

        String path_name = rootName;
        File file = new File(path_name);
        if (!file.exists()) {// 目录存在返回true
            file.mkdirs();// 创建一个目录
        }

        for (String dir_name : dir_name_list) {
            path_name += File.separator + dir_name;
            file = new File(path_name);
            if (!file.exists()) {
                Log.d("FileUtils", "file.getPath():" + file.getPath());
                Log.d("FileUtils", "file.mkdirs():" + file.mkdirs());
            }
        }
    }

    public static String getRealFilePath(String path) {
        if (path.startsWith(File.separator))
            return FileUtils.rootName + path;
        else
            return FileUtils.rootName + File.separator + path;
    }

    public static String getRealFilePath(String[] cacheName) {

        String pathename = rootName;
        if (cacheName != null) {
            int i, length = cacheName.length;
            for (i = 0; i < length; i++) {
                pathename += File.separator + cacheName[i];
            }
        }

        return pathename;
    }

    public static String getFormatSize(long size) {

        int decimal = 0;
        int unit = 0;
        while (size >= 1024 && unit < UNIT_SIZE.length - 1) {
            decimal = (int) (size % 1024);
            size = size / 1024;
            unit++;
        }
        return (unit == 0 ? (int) size + "" :
                String.format(Locale.US, "%.2f",
                        (double) size + (double) decimal / 1024.f)) + UNIT_SIZE[unit];
    }
}