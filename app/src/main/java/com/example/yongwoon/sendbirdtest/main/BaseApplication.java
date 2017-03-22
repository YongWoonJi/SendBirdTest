package com.example.yongwoon.sendbirdtest.main;

import android.app.Application;
import android.content.Context;

import com.sendbird.android.SendBird;

/**
 * Created by YongWoon on 2017-03-22.
 */

public class BaseApplication extends Application {

    private static final String APP_ID = "5087A865-57CF-4AB9-AA3B-2C7E71B50071";
    public static final String VERSION = "1.0";

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
