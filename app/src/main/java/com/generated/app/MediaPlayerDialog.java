package com.generated.app;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.Locale;

public class MediaPlayerDialog extends Dialog {

    private final File mediaFile;
    private final boolean isVideo;
    private MediaPlayer mediaPlayer;
    private VideoView videoView;
    private Button btnPlayPause;
    private SeekBar seekBar;
    private TextView tvTime;
    private final Handler handler = new Handler();

    public MediaPlayerDialog(Context context, File mediaFile, boolean isVideo) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.mediaFile = mediaFile;
        this.isVideo = isVideo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(getContext());
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF111827);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(mediaFile.getName());
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTextSize(16);
        tvTitle.setPadding(24, 24, 24, 24);
        mainLayout.addView(tvTitle);

        if (isVideo) {
            videoView = new VideoView(getContext());
            android.widget.LinearLayout.LayoutParams videoParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            videoView.setLayoutParams(videoParams);
            mainLayout.addView(videoView);
            videoView.setVideoPath(mediaFile.getAbsolutePath());
            videoView.start();
        } else {
            TextView tvAudioIcon = new TextView(getContext());
            tvAudioIcon.setText("🎵");
            tvAudioIcon.setTextSize(72);
            tvAudioIcon.setGravity(android.view.Gravity.CENTER);
            android.widget.LinearLayout.LayoutParams iconParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            tvAudioIcon.setLayoutParams(iconParams);
            mainLayout.addView(tvAudioIcon);

            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(mediaFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        seekBar = new SeekBar(getContext());
        mainLayout.addView(seekBar);

        tvTime = new TextView(getContext());
        tvTime.setTextColor(0xFFCCCCCC);
        tvTime.setGravity(android.view.Gravity.CENTER);
        mainLayout.addView(tvTime);

        android.widget.LinearLayout ctrlBar = new android.widget.LinearLayout(getContext());
        ctrlBar.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        ctrlBar.setGravity(android.view.Gravity.CENTER);
        ctrlBar.setPadding(16, 16, 16, 16);

        btnPlayPause = new Button(getContext());
        btnPlayPause.setText("השהה");
        btnPlayPause.setOnClickListener(v -> {
            if (isVideo) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    btnPlayPause.setText("נגן");
                } else {
                    videoView.start();
                    btnPlayPause.setText("השהה");
                }
            } else if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlayPause.setText("נגן");
                } else {
                    mediaPlayer.start();
                    btnPlayPause.setText("השהה");
                }
            }
        });
        ctrlBar.addView(btnPlayPause);

        Button btnClose = new Button(getContext());
        btnClose.setText("סגור");
        btnClose.setOnClickListener(v -> dismiss());
        ctrlBar.addView(btnClose);

        mainLayout.addView(ctrlBar);
        setContentView(mainLayout);

        updateProgressRunnable.run();
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            int current = 0;
            int total = 0;
            if (isVideo && videoView != null) {
                current = videoView.getCurrentPosition();
                total = videoView.getDuration();
            } else if (mediaPlayer != null) {
                current = mediaPlayer.getCurrentPosition();
                total = mediaPlayer.getDuration();
            }
            if (total > 0) {
                seekBar.setMax(total);
                seekBar.setProgress(current);
                tvTime.setText(formatMs(current) + " / " + formatMs(total));
            }
            handler.postDelayed(this, 1000);
        }
    };

    private String formatMs(int ms) {
        int seconds = (ms / 1000) % 60;
        int minutes = (ms / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(updateProgressRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}
