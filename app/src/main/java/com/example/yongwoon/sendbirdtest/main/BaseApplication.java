package com.example.yongwoon.sendbirdtest.main;

import android.app.Application;
import android.content.Context;

import com.sendbird.android.SendBird;

/**
 * Created by YongWoon on 2017-03-22.
 */

public class BaseApplication extends Application {

    private static final String APP_ID = "64C046FB-83B8-4F06-97CD-52F14FE57EA7";

    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        SendBird.init(APP_ID, this);
        this.context = this;
    }

    public static Context getContext() {
        return context;
    }
}
