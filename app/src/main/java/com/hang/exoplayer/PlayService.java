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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    public static final String ACTION_PLAY_PAUSE = "action play pause";
    public static final String ACTION_LOAD = "action load";
    public static final String ACTION_EXIT = "action exit";
    public static final String ACTION_PREVIOUS = "action previous";
    public static final String ACTION_NEXT = "action next";
    public static final String KEY_POS = "audio position";
    private AudioManager mAm;

    private ArrayList<String> playAddresses = new ArrayList<>();
    private int mCurrentPosition;
    public static final String TAG = "PlayService";

    PlayHandler playHandler;
    PlayStatusReceiver playStatusReceiver;
    NetworkStateReceiver networkStateReceiver;
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
        intent.putExtra(PlayService.ACTION_LOAD, true);
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
        networkStateReceiver = new NetworkStateReceiver();
        IntentFilter netFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkStateReceiver, netFilter);
        IntentFilter intentFilter = new IntentFilter(SimplePlayer.ACTION_PLAY_STUTUS);
        LocalBroadcastManager.getInstance(this).
                registerReceiver(playStatusReceiver, intentFilter);
        HandlerThread playThread = new HandlerThread("exoplayer");
        playThread.start();
        playHandler = new PlayHandler(playThread.getLooper());
    }

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (com.hang.exoplayer.bean.Util.clearStopPlayByLossFocusLastTime()) {
                        play();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    if (com.hang.exoplayer.bean.Util.clearStopPlayByLossFocusLastTime()) {
                        play();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    if (com.hang.exoplayer.bean.Util.clearStopPlayByLossFocusLastTime()) {
                        play();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (SimplePlayer.getInstance().isPlaying()) {
                        pause();
                        com.hang.exoplayer.bean.Util.saveStopPlayByLossFocus();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (SimplePlayer.getInstance().isPlaying()) {
                        pause();
                        com.hang.exoplayer.bean.Util.saveStopPlayByLossFocus();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    // Lower the volume
                    break;
            }
        }
    };

    private boolean requestFocus() {
        // Request audio focus for playback
        int result = mAm.requestAudioFocus(mOnAudioFocusChangeListener,
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
            boolean playOrPauseAction = intent.getBooleanExtra(ACTION_PLAY_PAUSE, false);
            boolean loadAction = intent.getBooleanExtra(ACTION_LOAD, false);
            boolean exitAction = intent.getBooleanExtra(ACTION_EXIT, false);
            boolean previousAction = intent.getBooleanExtra(ACTION_PREVIOUS, false);
            boolean nextAction = intent.getBooleanExtra(ACTION_NEXT, false);
            if (playOrPauseAction) {
                boolean isPlaying = SimplePlayer.getInstance().isPlaying();
                if (isPlaying) {
                    pause();
                } else {
                    play();
                }
            } else if (loadAction) {
                ArrayList<String> audioList = intent.getStringArrayListExtra(KEY_LIST);
                int currentPosition = intent.getIntExtra(KEY_POS, 0);
                if (playAddresses.containsAll(audioList) && mCurrentPosition == currentPosition) {
                } else {
                    mCurrentPosition = currentPosition;
                    playAddresses.clear();
                    playAddresses.addAll(audioList);
                }
                play();
            } else if (exitAction) {
                pause();
                SimplePlayer.getInstance().sendExitBroadcast();
                if (playStatusReceiver != null) {
                    LocalBroadcastManager.getInstance(this).unregisterReceiver(playStatusReceiver);
                }
                stopSelf();
            } else if (previousAction) {
                if (mCurrentPosition > 0) {
                    --mCurrentPosition;
                    play();
                } else {
                    Toast.makeText(this, "已经是第一首啦~", Toast.LENGTH_SHORT).show();
                }
            } else if (nextAction) {
                if (mCurrentPosition < playAddresses.size() - 1) {
                    ++mCurrentPosition;
                    play();
                } else {
                    Toast.makeText(this, "已经是最后一首啦~", Toast.LENGTH_SHORT).show();
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
                if (intent.getBooleanExtra(SimplePlayer.ACTION_SERVICE_EXISTS, false)) {
                    dismissNotification();
                } else {
                    showNotification();
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (networkStateReceiver != null) {
            unregisterReceiver(networkStateReceiver);
        }
        if (playStatusReceiver != null) {
            unregisterReceiver(playStatusReceiver);
        }
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
        PendingIntent pendingIntent = PendingIntent.getActivity(PlayService.this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Intent playPauseIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        playPauseIntent.putExtra(ACTION_PLAY_PAUSE, true);
        Intent previousIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        previousIntent.putExtra(ACTION_PREVIOUS, true);
        Intent nextIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        nextIntent.putExtra(ACTION_NEXT, true);

        Intent dismissIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
        dismissIntent.putExtra(ACTION_EXIT, true);

        PendingIntent playPendingIntent = PendingIntent.getService(PlayService.this, 1,
                playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent previousPendingIntent = PendingIntent.getService(PlayService.this, 2,
                previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextPendingIntent = PendingIntent.getService(PlayService.this, 3,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent dismissPendingIntent = PendingIntent.getService(PlayService.this, 4,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(getPackageName(),
                R.layout.notification_high_version_no_live);
        contentView.setTextViewText(R.id.songName, title);
        contentView.setTextViewText(R.id.artist, description);
        boolean isPlaying = SimplePlayer.getInstance().isPlaying();
//        Log.d(TAG, "Notification is Playing?" + isPlaying);
        contentView.setImageViewResource(R.id.play_pause,
                isPlaying ? R.drawable.notification_play_normal
                        : R.drawable.notification_pause_normal);
        contentView.setOnClickPendingIntent(R.id.play_pause, playPendingIntent);
        contentView.setOnClickPendingIntent(R.id.play_pre, previousPendingIntent);
        contentView.setOnClickPendingIntent(R.id.forward, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.stop, dismissPendingIntent);
        builder.setCustomContentView(contentView);
        Notification notification = builder.setSmallIcon(R.mipmap.ic_launcher).build();
        startForeground(ID_NOTIFICATION, notification);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(ID_NOTIFICATION, notification);
    }

    public class NetworkStateReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo == null || com.hang.exoplayer.bean.Util.getNetType() == -1) {
                if (SimplePlayer.getInstance().isPlaying()) {
                    com.hang.exoplayer.bean.Util.saveStopPlayByLeaveNet();
                    Toast.makeText(ExoApplication.getApplication(), "save", Toast.LENGTH_SHORT).show();
                    com.hang.exoplayer.bean.Util.appendLog("saved");
                    return;
                }
            }
            if (com.hang.exoplayer.bean.Util.canPlay() && com.hang.exoplayer.bean.Util.clearStopPlayByLeaveNetLastTime()) {
                play();
                com.hang.exoplayer.bean.Util.appendLog("play");
                Toast.makeText(ExoApplication.getApplication(), "play", Toast.LENGTH_LONG).show();
            }
        }
    }
}