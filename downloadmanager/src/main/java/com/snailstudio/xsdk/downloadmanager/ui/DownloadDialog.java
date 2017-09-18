/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.snailstudio.xsdk.downloadmanager.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.snailstudio.xsdk.downloadmanager.R;
import com.snailstudio.xsdk.downloadmanager.core.OnDownloadListener;

import static com.snailstudio.xsdk.downloadmanager.utils.FileUtils.getFormatSize;
import static com.snailstudio.xsdk.downloadmanager.utils.NetSpeedUtils.getRestTime;
import static com.snailstudio.xsdk.downloadmanager.utils.NetSpeedUtils.getSpeed;

/**
 * Created by xuqiqiang on 2016/05/22.
 */
public class DownloadDialog extends Dialog implements OnDownloadListener {

    private Context context;
    private String name;
    private boolean cancelable;
    private boolean canPause;
    private boolean showInfo;
    private TextView tv_download;
    private TextView tv_info;
    private ProgressBar pb_download;
    private Button bt_pause;
    private Handler mHandler;
    private boolean isPaused;

    private OnDownloadDialogListener mOnDownloadDialogListener;

    private DownloadDialog(Context context, int theme) {
        super(context, theme);
    }

    public DownloadDialog(Context context,
                          String name,
                          boolean canPause,
                          boolean showInfo,
                          OnDownloadDialogListener onDownloadDialogListener) {
        this(context, R.style.CustomDialog);
        this.context = context;
        this.name = name;
        this.canPause = canPause;
        this.showInfo = showInfo;
        this.mOnDownloadDialogListener = onDownloadDialogListener;
        this.mHandler = new Handler(Looper.getMainLooper());
        if (TextUtils.isEmpty(name))
            this.name = context.getString(R.string.downloading);
        initView();
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        this.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    moveToBack();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void initView() {

        this.setCancelable(cancelable);
        this.setCanceledOnTouchOutside(false);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.download_dialog, null);
        ((TextView) layout.findViewById(R.id.title)).setText(name);

        Button bt_backstage = (Button) layout.findViewById(R.id.bt_backstage);
        bt_backstage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                moveToBack();
            }
        });

        bt_pause = (Button) layout.findViewById(R.id.bt_pause);
        if (canPause) {
            bt_pause.setVisibility(View.VISIBLE);
            bt_pause.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    pause();
                }
            });
        } else {
            bt_pause.setVisibility(View.GONE);
        }

        Button bt_stop = (Button) layout.findViewById(R.id.bt_stop);
        bt_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                stop();
            }
        });

        tv_download = (TextView) layout.findViewById(R.id.tv_download);
        tv_download.setText(context.getString(R.string.download_progress) + "0%");
        tv_info = (TextView) layout.findViewById(R.id.tv_info);
        if (showInfo) {
            tv_info.setVisibility(View.VISIBLE);
            tv_info.setText("0B      --:--:--      0KB/s");
        } else {
            tv_info.setVisibility(View.GONE);
        }

        pb_download = (ProgressBar) layout.findViewById(R.id.pb_download);

        pb_download.setVisibility(View.VISIBLE);
        pb_download.setIndeterminate(false);
        pb_download.setMax(100);
        pb_download.setProgress(0);
        this.setContentView(layout);
    }

    private void moveToBack() {
        if (this.mOnDownloadDialogListener.onMoveToBack())
            this.dismiss();
    }

    private void pause() {
        if (!isPaused)
            this.mOnDownloadDialogListener.onPause();
        else
            this.mOnDownloadDialogListener.onResume();
    }

    private void stop() {
        this.dismiss();
        this.mOnDownloadDialogListener.onStop();
    }

    @Override
    public void onStart(long fileSize) {
        isPaused = false;
        if (canPause)
            bt_pause.setText(R.string.download_pause);
    }

    @Override
    public void onError(String message) {
        if (!this.isShowing())
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                DownloadDialog.this.dismiss();
            }
        });
    }

    @Override
    public void onComplete(String downloadPath, long time, long downloadedSize) {
        if (!this.isShowing())
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                pb_download.setProgress(100);
                String message = context.getString(R.string.download_progress) + "100%";
                tv_download.setText(message);
                DownloadDialog.this.dismiss();
            }
        });

    }

    @Override
    public void onPaused(long fileSize, long downloadedSize) {
        isPaused = true;
        if (!this.isShowing())
            return;
        if (canPause)
            bt_pause.setText(R.string.download_resume);
        int progress = (Double.valueOf((downloadedSize * 100.0 / fileSize))).intValue();
        pb_download.setProgress(progress);
        tv_download.setText(context.getString(R.string.download_progress) + progress + "%");
        if (showInfo)
            tv_info.setText(getFormatSize(fileSize) + "      --:--:--      0KB/s");
    }

    @Override
    public void onProcess(final long fileSize, final long downloadedSize, final double speed) {
        if (!this.isShowing())
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int progress = (Double.valueOf((downloadedSize * 100.0 / fileSize))).intValue();
                pb_download.setProgress(progress);
                tv_download.setText(context.getString(R.string.download_progress) + progress + "%");
                if (showInfo)
                    tv_info.setText(getFormatSize(fileSize)
                            + "      " + getRestTime(fileSize - downloadedSize, speed)
                            + "      " + getSpeed(speed));

            }
        });

    }

    public interface OnDownloadDialogListener {
        boolean onMoveToBack();

        void onPause();

        void onResume();

        void onStop();
    }
}