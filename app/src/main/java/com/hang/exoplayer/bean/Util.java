package com.hang.exoplayer.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.android.exoplayer.demo.player.ExoApplication;
import com.hang.exoplayer.SettingActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Chuanhang.Gu on 2016/9/27.
 */
public class Util {
    public static final String ALLOW_MOBILE_PLAY = "allow mobile play this time";
    public static final String STOP_BY_LOSS_FOCUS = "stop play by loss audio foucs";
    public static final String STOP_BY_LEAVE_NET = "stop play by user leave net";

    public static void allowThis4GPlay() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ALLOW_MOBILE_PLAY, true);
        editor.commit();
    }

    public static void resetInit() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(ALLOW_MOBILE_PLAY);
        editor.remove(STOP_BY_LOSS_FOCUS);
        editor.remove(STOP_BY_LEAVE_NET);
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
        boolean canPlay =
                (com.hang.exoplayer.bean.Util.getNetType() == ConnectivityManager.TYPE_WIFI);
        if (!canPlay) {
            canPlay = (!onlyWifi() || isAllowThis4GPlay()) &&
                    com.hang.exoplayer.bean.Util.getNetType() == ConnectivityManager.TYPE_MOBILE;
        }
        return canPlay;
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

    public static void saveStopPlayByLossFocus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STOP_BY_LOSS_FOCUS, true);
        editor.apply();
    }

    public static boolean clearStopPlayByLossFocusLastTime() {
        boolean isStopByLossFocus = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        isStopByLossFocus = preferences.getBoolean(STOP_BY_LOSS_FOCUS, false);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STOP_BY_LOSS_FOCUS, false);
        editor.apply();
        return isStopByLossFocus;
    }

    public static void saveStopPlayByLeaveNet() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STOP_BY_LEAVE_NET, true);
        editor.apply();
    }

    public static boolean clearStopPlayByLeaveNetLastTime() {
        boolean isStopByLeaveNet = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ExoApplication.getApplication());
        isStopByLeaveNet = preferences.getBoolean(STOP_BY_LEAVE_NET, false);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(STOP_BY_LEAVE_NET, false);
        editor.apply();
        return isStopByLeaveNet;
    }

    public static void appendLog(String text) {
        File logFile = new File(Environment.getExternalStorageDirectory() + File.separator + ".0log.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
