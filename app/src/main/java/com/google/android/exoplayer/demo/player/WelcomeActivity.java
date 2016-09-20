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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.Samples;
import com.google.android.exoplayer.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An activity for selecting from a number of samples.
 */
public class WelcomeActivity extends Activity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {
    public static final String TAG = "Welcome";
    String playAddress0 = "file://" + Environment.getExternalStorageDirectory() + File.separator + "test.aac";
    String playAddress = "file://" + Environment.getExternalStorageDirectory() + File.separator + "test.mp3";
    Queue<String> playAddresses = new LinkedList<>(Arrays.asList(new String[]{playAddress}));
    public static final String TYPE_MUZHI = "MUZHI";
    public static final String TYPE_QINGTING = "QINGQING";
    private String liveType;
    Timer timer;
    MyTimerTask myTimerTask;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_resume: {
                Intent intent = new Intent(WelcomeActivity.this, PlayService.class);
                intent.putExtra(PlayService.ACTION_PLAY, PlayService.KEY_LOAD);
                ArrayList<String> playingAddresses = new ArrayList<>(Arrays.asList(playAddress0, playAddress));
                intent.putExtra(PlayService.KEY_LIST, playingAddresses);
                intent.putExtra(PlayService.KEY_POS, 0);
                startService(intent);
                break;
            }
            case R.id.btn_pause: {
                Intent intent = new Intent(WelcomeActivity.this, PlayService.class);
                intent.putExtra(PlayService.ACTION_PLAY, PlayService.KEY_PLAY_PAUSE);
                startService(intent);
                break;
            }
        }
    }


    SeekBar seekBar;
    PlayStatusReceiver playStatusReceiver;
    TextView playingUrlView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout outLayout = new LinearLayout(this);
        outLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(outLayout);
        Button resumeButton = new Button(this);
        resumeButton.setText("Resume");
        resumeButton.setId(R.id.btn_resume);
        outLayout.addView(resumeButton);
        resumeButton.setOnClickListener(this);
        Button pauseButton = new Button(this);
        pauseButton.setText("Pause");
        outLayout.addView(pauseButton);
        pauseButton.setId(R.id.btn_pause);
        pauseButton.setOnClickListener(this);

        seekBar = new SeekBar(this);
        outLayout.addView(seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        playStatusView = new ImageView(this);
        playStatusView.setImageResource(R.mipmap.ic_launcher);
        outLayout.addView(playStatusView);
        playingUrlView = new TextView(this);
        outLayout.addView(playingUrlView);

        playStatusReceiver = new PlayStatusReceiver();
//        //TODO
//        SimplePlayer simplePlayer = SimplePlayer.getInstance();
//        Samples.Sample sample = new Samples.Sample("", "file:///storage/emulated/0/test.aac", Util.TYPE_OTHER);
//        simplePlayer.play(sample);

    }

    ImageView playStatusView;


    class PlayStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getBooleanExtra(SimplePlayer.KEY_STATUS, false)) {
                    playStatusView.setVisibility(View.VISIBLE);
                    Uri playUri = intent.getParcelableExtra(SimplePlayer.KEY_ADDRESS);
                    String playingAddress = playUri.toString();
                    playingUrlView.setText(playingAddress);

                } else {
                    playStatusView.setVisibility(View.INVISIBLE);
                    playingUrlView.setText("");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        myTimerTask = new MyTimerTask();
        //singleshot delay 1000 ms
        timer.schedule(myTimerTask, 1000, 1000);
        IntentFilter intentFilter = new IntentFilter(SimplePlayer.ACTION_PLAY_STUTUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(playStatusReceiver, intentFilter);
        boolean initPlay = SimplePlayer.getInstance().isPlaying();
        if (initPlay) {
            playStatusView.setVisibility(View.VISIBLE);
        } else {
            playStatusView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playStatusReceiver);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            SimplePlayer.getInstance().setProgress(progress * 1.0 / 100);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    boolean isPlaying = SimplePlayer.getInstance().isPlaying();
                    if (isPlaying) {
                        int progress = (int) (SimplePlayer.getInstance().getProgress() * 100.0);
                        seekBar.setProgress(progress);
                    }
                }
            });
        }

    }
}