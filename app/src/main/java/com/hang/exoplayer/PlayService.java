/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hang.exoplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.Samples;
import com.google.android.exoplayer.demo.player.ExoApplication;
import com.google.android.exoplayer.demo.player.SimplePlayer;
import com.google.android.exoplayer.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity for selecting from a number of samples.
 */
public class PlayService extends Service {
    public static final String KEY_LIST = "play list";
    public static final String KEY_POS = "play position";
    public static final String ACTION_PLAY = "action type";
    public static final int KEY_PLAY_PAUSE = 0;
    public static final int KEY_LOAD = 1;
    public static final int KEY_RELEASE = 2;
    public static final int KEY_PREVIOUS = 3;
    public static final int KEY_NEXT = 4;
    private AudioManager mAm;

    private ArrayList<String> playAddresses = new ArrayList<>();
    private int mCurrentPosition;
    public static final String TAG = "PlayService";

    PlayHandler playHandler;
    PlayStatusReceiver playStatusReceiver;
    RemoteViews rm;
    public static final String REQUEST_CODE = "1111";
    public static final int PAUSE = 2;
    public static final int NOTIFICATION_ID = 123;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void loadMedia(Context packageContext, List<String> playingAddress, int position) {
        boolean isNativeAudio = (playingAddress != null && playingAddress.size() > 0) ? playingAddress.get(0).startsWith("file:") : false;
        Intent intent = new Intent(packageContext, PlayService.class);
        intent.putExtra(PlayService.ACTION_PLAY, PlayService.KEY_LOAD);
        ArrayList<String> playingAddresses = new ArrayList<>(playingAddress);
        intent.putExtra(PlayService.KEY_LIST, playingAddresses);
        intent.putExtra(PlayService.KEY_POS, position);
        if (isNativeAudio || com.hang.exoplayer.bean.Util.canPlay()) {
            packageContext.startService(intent);
        } else if (com.hang.exoplayer.bean.Util.getNetType() > -1) {
            Bundle bundle = intent.getExtras();
            if (packageContext instanceof FragmentActivity) {
                NetInfoFragment netInfoFragment = NetInfoFragment.newInstance(bundle);
                netInfoFragment.show(((FragmentActivity) packageContext).getSupportFragmentManager(),
                        "netInfo");
            } else {
                throw new RuntimeException("loadMedia Activity must be FragmentActivity");
            }
        } else {
            Toast.makeText(ExoApplication.getApplication(), "请检查网络设置，稍后重试~", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAm = (AudioManager) getSystemService(AUDIO_SERVICE);
        playStatusReceiver = new PlayStatusReceiver();
        IntentFilter intentFilter = new IntentFilter(SimplePlayer.ACTION_PLAY_STUTUS);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(playStatusReceiver, intentFilter);
        HandlerThread playThread = new HandlerThread("exoplayer");
        playThread.start();
        playHandler = new PlayHandler(playThread.getLooper());
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                pause();
                mAm.abandonAudioFocus(afChangeListener);
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
                Log.d(TAG, "AUDIOFOCUS_GAIN");

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mAm.abandonAudioFocus(afChangeListener);
                // Stop playback
                Log.d(TAG, "AUDIOFOCUS_LOSS");
            }
        }
    };

    private boolean requestFocus() {
        // Request audio focus for playback
        int result = mAm.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void pause() {
        Message message = playHandler.obtainMessage();
        message.what = 2;
        message.sendToTarget();
    }

    private void play() {
        Message message = playHandler.obtainMessage();
        message.what = 1;
        Samples.Sample sample = new Samples.Sample("", playAddresses.get(mCurrentPosition), Util.TYPE_HLS);

        message.obj = sample;
        message.sendToTarget();
        requestFocus();
    }

    public boolean hasNext() {
        if (mCurrentPosition < playAddresses.size()) {
            return true;
        }
        return false;
    }

    public boolean hasPrevious() {
        return mCurrentPosition > 0;
    }

    public void next() {
        if (mCurrentPosition < playAddresses.size() - 1) {
            SimplePlayer.getInstance().sendPlayStatusBroadcast(false, true, true, false);
        }
    }

    public void previous() {
        if (mCurrentPosition > 0) {
            SimplePlayer.getInstance().sendPlayStatusBroadcast(false, true, false, false);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int actionType = intent.getIntExtra(ACTION_PLAY, 0);
            switch (actionType) {
                case KEY_PLAY_PAUSE: {
                    boolean isPlaying = SimplePlayer.getInstance().isPlaying();
                    if (isPlaying) {
                        pause();
                    } else {
                        play();
                    }
                    break;
                }

                case KEY_LOAD: {
                    ArrayList<String> audioList = intent.getStringArrayListExtra(KEY_LIST);
                    int currentPosition = intent.getIntExtra(KEY_POS, 0);
                    if (playAddresses.containsAll(audioList) && mCurrentPosition == currentPosition) {
                    } else {
                        mCurrentPosition = currentPosition;
                        playAddresses.clear();
                        playAddresses.addAll(audioList);
                    }
                    play();
                    break;
                }
                case KEY_RELEASE: {
                    pause();
                    stopSelf();
                    dismissNotification();
                    break;
                }
                case KEY_PREVIOUS: {
                    if (mCurrentPosition > 0) {
                        --mCurrentPosition;
                        play();
                    } else {
                        Toast.makeText(this, "已经是第一首啦~", Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
                case KEY_NEXT: {
                    if (mCurrentPosition < playAddresses.size() - 1) {
                        ++mCurrentPosition;
                        play();
                    } else {
                        Toast.makeText(this, "已经是最后一首啦~", Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);

    }


    class PlayHandler extends Handler {
        SimplePlayer playService;

        public PlayHandler(Looper looper) {
            super(looper);
            this.playService = SimplePlayer.getInstance();
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1: {
                    Samples.Sample sample = (Samples.Sample) msg.obj;
                    playService.play(sample);
                    break;
                }
                case 2: {
                    playService.release();
                    break;
                }

            }
        }
    }

    public static final int ID_NOTIFICATION = 123;

    class PlayStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getBooleanExtra(SimplePlayer.KEY_SWITCH, false)) {
                    if (intent.getBooleanExtra(SimplePlayer.KEY_IS_NEXT, true)) {
                        if (mCurrentPosition < playAddresses.size() - 1) {
                            ++mCurrentPosition;
                            play();
                        }
                    } else {
                        if (mCurrentPosition > 0) {
                            --mCurrentPosition;
                            play();
                        }
                    }
                }
                if (intent.getBooleanExtra(SimplePlayer.KEY_START_PLAY, false)) {
                    showNotification();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playStatusReceiver);
        super.onDestroy();
    }

    private void dismissNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(ID_NOTIFICATION);
    }

    public void showNotification() {
        String title = "XXXXXXXX";
        String description = "YYYYYYYYYYYYY";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                PlayService.this)
                .setAutoCancel(false).setOngoing(true);
        Class<?> targetClass = WelcomeActivity.class;
        Intent notificationIntent = new Intent(PlayService.this, targetClass);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(PlayService.this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Intent playPauseIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        playPauseIntent.putExtra(ACTION_PLAY, KEY_PLAY_PAUSE);
        Intent previousIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        previousIntent.putExtra(ACTION_PLAY, KEY_PREVIOUS);
        Intent nextIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        nextIntent.putExtra(ACTION_PLAY, KEY_NEXT);

        Intent dismissIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        dismissIntent.putExtra(ACTION_PLAY, KEY_RELEASE);

        PendingIntent playPendingIntent = PendingIntent.getService(PlayService.this, 0,
                playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent previousPendingIntent = PendingIntent.getService(PlayService.this, 0,
                previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextPendingIntent = PendingIntent.getService(PlayService.this, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent dismissPendingIntent = PendingIntent.getService(PlayService.this, 0,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(getPackageName(),
                R.layout.notification_high_version_no_live);
        contentView.setTextViewText(R.id.songName, title);
        contentView.setTextViewText(R.id.artist, description);
        contentView.setImageViewResource(R.id.play_pause,
                SimplePlayer.getInstance().isPlaying() ? R.drawable.notification_play_normal
                        : R.drawable.notification_pause_normal);
        contentView.setOnClickPendingIntent(R.id.play_pause, playPendingIntent);
        contentView.setOnClickPendingIntent(R.id.play_pre, previousPendingIntent);
        contentView.setOnClickPendingIntent(R.id.forward, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.stop, dismissPendingIntent);
        Notification notification = builder.setContent(contentView).setSmallIcon(R.mipmap.ic_launcher).build();
        startForeground(ID_NOTIFICATION, notification);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(ID_NOTIFICATION, notification);
    }
}