package com.hang.exoplayer.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.google.android.exoplayer.demo.player.ExoApplication;
import com.hang.exoplayer.SettingActivity;

/**
 * Created by Chuanhang.Gu on 2016/9/27.
 */
public class Util {
    public static final String ALLOW_MOBILE_PLAY = "allow mobile play this time";

    public static void allowThis4GPlay() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ALLOW_MOBILE_PLAY, true);
        editor.commit();
    }

    public static void forbidNext4GPlay() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(ALLOW_MOBILE_PLAY);
        editor.commit();
    }

    private static boolean onlyWifi() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        return preferences.getBoolean(SettingActivity.KEY_ONLY_WIFI, true);
    }

    private static boolean isAllowThis4GPlay() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        return preferences.getBoolean(ALLOW_MOBILE_PLAY, false);
    }

    public static boolean canPlay() {
        return !onlyWifi() || isAllowThis4GPlay();
    }

    //ConnectivityManager.TYPE_MOBILE 进行对比处理
    //-1 not net available
    public static int getNetType() {
        ConnectivityManager manager = (ConnectivityManager) ExoApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.getType();
            }

        }
        return -1;
    }
}
