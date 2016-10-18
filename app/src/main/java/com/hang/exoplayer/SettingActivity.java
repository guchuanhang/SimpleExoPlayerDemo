package com.hang.exoplayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.exoplayer.demo.R;

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    public static final String KEY_ONLY_WIFI = "only wifi play";
    private ToggleButton onlyWifiPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        onlyWifiPlayView = (ToggleButton) findViewById(R.id.tgbtn_wifi);
        onlyWifiPlayView.setOnCheckedChangeListener(this);
        initData();
        findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);

            }
        });
    }

    private void initData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean onlyWifi = preferences.getBoolean(KEY_ONLY_WIFI, true);
        onlyWifiPlayView.setChecked(onlyWifi);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_ONLY_WIFI, b);
        editor.apply();
    }
}
