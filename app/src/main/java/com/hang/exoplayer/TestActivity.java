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

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.player.DemoPlayer;

/**
 * An activity that plays media using {@link DemoPlayer}.
 */
public class TestActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showNotification();
//        simpleNotification();
    }

    public void simpleNotification() {
        // 1.展示的内容
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this)
                .setContentTitle("title title title ")
                .setContentText("text text text")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                        BitmapFactory.decodeResource(getResources(),
                                R.mipmap.ic_launcher));
        // 2.点击通知栏的跳转
        Intent intent = new Intent(TestActivity.this, WelcomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        // 3.发出通知
        Notification notification = null;
        notification = notificationBuilder.build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    public void showNotification() {
        String title = "XXXXXXXX";
        String description = "YYYYYYYYYYYYY";
        Notification notification = null;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                TestActivity.this)
                .setAutoCancel(false).setOngoing(true);
        Intent notificationIntent = new Intent(TestActivity.this, WelcomeActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(TestActivity.this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
//        Intent playPauseIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
//        playPauseIntent.putExtra(PlayService.ACTION_PLAY, PlayService.KEY_PLAY_PAUSE);
//        Intent previousIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
//        previousIntent.putExtra(PlayService.ACTION_PLAY, PlayService.KEY_PREVIOUS);
//        Intent nextIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
//        nextIntent.putExtra(PlayService.ACTION_PLAY, PlayService.KEY_NEXT);
//
//        Intent dismissIntent = new Intent(ExoApplication.getApplication(), PlayService.class);
//        dismissIntent.putExtra(PlayService.ACTION_PLAY, PlayService.KEY_RELEASE);
//
//        PendingIntent playPendingIntent = PendingIntent.getService(SimpleActivity.this, 0,
//                playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        PendingIntent previousPendingIntent = PendingIntent.getService(SimpleActivity.this, 0,
//                previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent nextPendingIntent = PendingIntent.getService(SimpleActivity.this, 0,
//                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent dismissPendingIntent = PendingIntent.getService(SimpleActivity.this, 0,
//                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = null;
//            RemoteViews bigContentView = null;
        contentView = new RemoteViews(getPackageName(),
                R.layout.notification_low_version_no_live);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.notification_high_version_no_live);
//        contentView.setTextViewText(R.id.songName, title);
//        contentView.setTextViewText(R.id.artist, description);
//        contentView.setImageViewResource(R.id.play_pause,
//                SimplePlayer.getInstance().isPlaying() ? R.drawable.notification_play_normal
//                        : R.drawable.notification_pause_normal);
//        contentView.setOnClickPendingIntent(R.id.play_pause, playPendingIntent);
//        contentView.setOnClickPendingIntent(R.id.play_pre, previousPendingIntent);
//        contentView.setOnClickPendingIntent(R.id.forward, nextPendingIntent);
//        contentView.setOnClickPendingIntent(R.id.stop, dismissPendingIntent);
//        builder.setCustomContentView(contentView);
        builder.setContent(bigViews);
        notification = builder.setSmallIcon(R.mipmap.ic_launcher).build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(PlayService.ID_NOTIFICATION, notification);

    }
}