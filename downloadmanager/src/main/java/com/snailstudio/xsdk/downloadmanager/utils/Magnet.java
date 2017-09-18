/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.utils;

import android.text.TextUtils;

import com.snailstudio.xsdk.utils.Base32;
import com.snailstudio.xsdk.utils.Hex;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class Magnet {
    private static final String MAGNET_LINK_PREFIX = "magnet:?xt=urn:btih:";

    // http://magnet2torrent.com/
    public static boolean isMagnetLink(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith(MAGNET_LINK_PREFIX);
    }

    public static String parseUrl(String url) {
        if (url.startsWith(MAGNET_LINK_PREFIX)) {
            String infoHash = url.substring(MAGNET_LINK_PREFIX.length());
            return getFromVuze(infoHash);
        }
        return url;
    }

    private static String getFromVuze(String infoHash) {
        return "http://magnet.vuze.com/magnetLookup?hash="
                + Base32.encode(Hex.decodeHex(infoHash.toCharArray()));
    }

    private static String getFromThunder(String infoHash) {
        return "http://bt.box.n0808.com/"
                + infoHash.substring(0, 2) + "/"
                + infoHash.substring(infoHash.length() - 2, infoHash.length()) + "/"
                + infoHash;
    }

    private static String getFromTorrage(String infoHash) {
        return "http://torrage.com/torrent/"
                + infoHash + ".torrent";
    }

    private static String getFromTorcache(String infoHash) {
        return "http://torcache.nei/torrent/"
                + infoHash + ".torrent";
    }

    private static String getFromZoink(String infoHash) {
        return "http://zoink.it/torrent/"
                + infoHash + ".torrent";
    }

    private static String getFromMag2tor(String infoHash) {
        return "http://mag2tor.com/static/torrents/"
                + infoHash.substring(0, 2) + "/"
                + infoHash.substring(2, 4) + "/"
                + infoHash.substring(4, 6) + "/"
                + infoHash + ".torrent";
    }

    private static String getFrom178(String infoHash) {
        return "http://178.73.198.210/torrent/"
                + infoHash + ".torrent";
    }

    private static String getFromMt520(String infoHash) {
        return "http://mt520.xyz:8080/CPServer/cloudplayer/getlistopen?hash="
                + infoHash;
    }
}
