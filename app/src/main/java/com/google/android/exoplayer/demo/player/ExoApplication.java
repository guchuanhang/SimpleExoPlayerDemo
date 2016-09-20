package com.google.android.exoplayer.demo.player;

import android.app.Application;

/**
 * Created by Chuanhang.Gu on 2016/9/18.
 */
public class ExoApplication extends Application {
    private static ExoApplication exoApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        this.exoApplication = this;
    }

    public static ExoApplication getApplication() {
        return exoApplication;
    }

}
