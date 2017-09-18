/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.torrent;

import java.util.List;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class Files {
    private long length;
    private String md5sum;
    private List<String> path;

    public Files() {
    }

    public Files(long length, String md5sum, List<String> path) {
        super();
        this.length = length;
        this.md5sum = md5sum;
        this.path = path;
    }

    //getter and setter and tostring
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }
}

