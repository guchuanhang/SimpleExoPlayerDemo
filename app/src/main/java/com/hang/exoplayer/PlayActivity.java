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

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.exoplayer.demo.R;
import com.google.android.exoplayer.demo.player.SimplePlayer;
import com.hang.exoplayer.bean.QueryLiveProgramList;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends FragmentActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {
    private String nativeM3U8 = "file://" + Environment.getExternalStorageDirectory() + File.separator + "test.mp3";
    private String playAddress0 = "file://" + Environment.getExternalStorageDirectory() + File.separator + "test.aac";
//    private List<String> playingAddressList = Arrays.asList(new String[]{playAddress0, nativeM3U8, playAddress0});

    private static String muzhiLive = "http://living.muzhifm.com/muzhifm/jnxw1066.m3u8?auth_key=1480301650-0-0-f92ad1bc8b31d09ff37249205d7dbd59";
    private static final String URL_AUDIO = "http://vfile.dingdongfm.com/2015/1450/2617/4979/145026174979.ssm/145026174979.m3u8";

    private List<String> playingAddressList = Arrays.asList(new String[]{muzhiLive, URL_AUDIO, muzhiLive});


    private Timer timer;
    private MyTimerTask myTimerTask;

    private ImageButton mPlayControlView;
    private ImageButton mPreviousView;
    private ImageButton mNextView;
    private ImageView mPlayAnimView;
    private ImageButton mDownloadView;
    private ImageButton mPlayListView;
    private SeekBar seekBar;
    private PlayStatusReceiver playStatusReceiver;
    private PlayService mPlayService;
    private PlayServiceConnection mPlayServiceConnection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "play modify playing address!", Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_play);
        findViewById(R.id.ibtn_setting).setOnClickListener(this);
        mPlayControlView = (ImageButton) findViewById(R.id.ibtn_play_pause);
        mPlayControlView.setOnClickListener(this);
        mPlayAnimView = (ImageView) findViewById(R.id.iv_play_anim);
        mPreviousView = (ImageButton) findViewById(R.id.ibtn_previous);
        mNextView = (ImageButton) findViewById(R.id.ibtn_next);
        mPreviousView.setOnClickListener(this);
        mNextView.setOnClickListener(this);
        mDownloadView = (ImageButton) findViewById(R.id.ibtn_download);
        mPlayListView = (ImageButton) findViewById(R.id.ibtn_list);
        mDownloadView.setOnClickListener(this);
        mPlayListView.setOnClickListener(this);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        playStatusReceiver = new PlayStatusReceiver();
        Intent intent = new Intent(PlayActivity.this, PlayService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
        //for simper permission ,just apply it from start
        applyAccessPermission();
        //get internet play addresses
//        new Thread() {
//            @Override
//            public void run() {
//                QueryLiveProgramList.getLiveAddress(QueryLiveProgramList.TYPE_MUZHI);
//            }
//        }.start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_setting: {
                startActivity(new Intent(PlayActivity.this, SettingActivity.class));
                break;
            }
            case R.id.ibtn_play_pause: {
                if (!SimplePlayer.getInstance().isPlaying()) {
                    PlayService.loadMedia(this, playingAddressList, 0);
                    updatePlayView(true);
                } else {
                    mPlayService.pause();
                    updatePlayView(false);
                }
                break;
            }
            case R.id.ibtn_previous: {
                mPlayService.previous();
                break;
            }
            case R.id.ibtn_next: {
                mPlayService.next();
                break;
            }
            case R.id.ibtn_download: {
                Toast.makeText(this, "暂时没有添加相关逻辑哦~", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.ibtn_list: {
                Toast.makeText(this, "暂时没有添加相关逻辑哦~", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mPlayService = ((PlayService.Binder) iBinder).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPlayService = null;
        }
    }

    class PlayStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                updatePlayView(intent.getBooleanExtra(SimplePlayer.KEY_STATUS, false));
            }
        }
    }

    public void updatePlayView(boolean isPlaying) {
        if (isPlaying) {
            mPlayControlView.setImageResource(R.drawable.sel_radio_play);
            mPlayAnimView.setVisibility(View.VISIBLE);
            mPlayAnimView.startAnimation(AnimationUtils
                    .loadAnimation(PlayActivity.this, R.anim.ri_live_rotate));
        } else {
            mPlayControlView.setImageResource(R.drawable.sel_radio_pause);
            mPlayAnimView.clearAnimation();
            mPlayAnimView.setVisibility(View.INVISIBLE);
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
        updatePlayView(initPlay);
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
    protected void onDestroy() {
        if (mPlayServiceConnection != null) {
            unbindService(mPlayServiceConnection);
            mPlayServiceConnection = null;
        }
        super.onDestroy();
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

    public static final int REQUEST_PERMISSION_READ_EXTERNAL = 0x123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void applyAccessPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PlayActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_EXTERNAL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(PlayActivity.this,
                            "I sorry,lack permission i cann't play audio in external sd card~",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}