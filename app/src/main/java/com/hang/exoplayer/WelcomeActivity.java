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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.player.SimplePlayer;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An activity for selecting from a number of samples.
 */
public class WelcomeActivity extends FragmentActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {
    public static final String TAG = "Welcome";
    //not play
    public static String muzhiReview = "http://ts1.ijntv.cn/jnxwpl/sd/1474653600000,1800000.m3u8?_upt=1035157f1474862986";
    public static String muzhiLive = "http://living.muzhifm.com/muzhifm/jnxw1066.m3u8?auth_key=1480301650-0-0-f92ad1bc8b31d09ff37249205d7dbd59";
    public static final String URL_AUDIO = "http://vfile.dingdongfm.com/2015/1450/2617/4979/145026174979.ssm/145026174979.m3u8";
    //        public static final String URL_AUDIO = "http://vfile.dingdongfm.com/2016/1473/4999/6792/147349996792.ssm/147349996792.m3u8";
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
            case R.id.ibtn_setting: {
                startActivity(new Intent(WelcomeActivity.this, SettingActivity.class));
                break;
            }
            case R.id.btn_resume: {
//                String muzhiLive=
//                        "http://living.muzhifm.com/muzhifm/jnxw1066.m3u8?auth_key=1480301650-0-0-f92ad1bc8b31d09ff37249205d7dbd59";
                String nativeM3U8 = "file://" + Environment.getExternalStorageDirectory() + File.separator + "test.m3u8";
                List<String> playingAddressList = Arrays.asList(new String[]{muzhiLive});
                PlayService.loadMedia(this, playingAddressList, 0);
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
    ImageView playStatusView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.ibtn_setting).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        playStatusView = (ImageView) findViewById(R.id.iv_status);
        playStatusReceiver = new PlayStatusReceiver();

    }


    class PlayStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getBooleanExtra(SimplePlayer.KEY_STATUS, false)) {
                    playStatusView.setVisibility(View.VISIBLE);

                } else {
                    playStatusView.setVisibility(View.INVISIBLE);
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