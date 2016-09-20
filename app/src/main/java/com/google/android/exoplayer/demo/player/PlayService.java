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
package com.google.android.exoplayer.demo.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.Samples;
import com.google.android.exoplayer.util.Util;

import java.util.ArrayList;

/**
 * An activity for selecting from a number of samples.
 */
public class PlayService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static final String KEY_LIST = "play list";
    public static final String KEY_POS = "play postion";
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

    void showNotification() {
        rm = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notif_media_player);
        Intent pauseIntent = new Intent(getApplicationContext(), PlayService.class);
        pauseIntent.putExtra(ACTION_PLAY, KEY_PLAY_PAUSE);
        PendingIntent pause = PendingIntent.getService(getApplicationContext(), PAUSE, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rm.setOnClickPendingIntent(R.id.btn_play, pause);
        if (SimplePlayer.getInstance().isPlaying()) {
            rm.setImageViewResource(R.id.btn_play, R.mipmap.ic_launcher);
        } else {
            rm.setImageViewResource(R.id.btn_play, android.R.drawable.alert_dark_frame);
        }
        Notification notif = new NotificationCompat.Builder(getApplicationContext())
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(rm)
                .build();

        NotificationManager mgr = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(NOTIFICATION_ID, notif);
    }

    private void pause() {
        Message message = playHandler.obtainMessage();
        message.what = 2;
        message.sendToTarget();
    }

    private void play() {
        Message message = playHandler.obtainMessage();
        message.what = 1;
        Samples.Sample sample = new Samples.Sample("", playAddresses.get(mCurrentPosition), Util.TYPE_OTHER);
        message.obj = sample;
        message.sendToTarget();
        requestFocus();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO diff live program and audio;
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

    class PlayStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showNotification();
            if (intent != null) {
                if (intent.getBooleanExtra(SimplePlayer.KEY_SWITCH, false)) {
                    if (mCurrentPosition < playAddresses.size() - 1) {
                        String playString = playAddresses.get(++mCurrentPosition);
                        Message message = playHandler.obtainMessage();
                        message.what = 1;
                        //TODO maybe live program
                        Samples.Sample sample = new Samples.Sample("", playString, Util.TYPE_OTHER);
                        message.obj = sample;
                        message.sendToTarget();
                    }
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playStatusReceiver);
        super.onDestroy();
    }
}