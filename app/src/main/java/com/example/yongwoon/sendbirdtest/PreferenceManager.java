package com.example.yongwoon.sendbirdtest;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.yongwoon.sendbirdtest.main.BaseApplication;

/**
 * Created by YongWoon on 2017-03-22.
 */

public class PreferenceManager {

    public static final String PREFERENCE_KEY_USER_ID = "userId";
    public static final String PREFERENCE_KEY_NICKNAME = "nickname";
    public static final String PREFERENCE_KEY_CONNECTED = "connected";

    private PreferenceManager() {}


    public static SharedPreferences getSharedPreferences() {
        return BaseApplication.getContext().getSharedPreferences("sendbird", Context.MODE_PRIVATE);
    }

    public static void setUserId(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_USER_ID, userId).apply();
    }

    public static String getUserId() {
        return getSharedPreferences().getString(PREFERENCE_KEY_USER_ID, "");
    }

    public static void setNickname(String nickname) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NICKNAME, nickname);
        editor.apply();
    }

    public static String getNickname() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NICKNAME, "");
    }

    public static void setConnected(boolean tf) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply();
    }

    public static boolean getConnected() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_CONNECTED, false);
    }

    public static void clearAll() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.clear().apply();
    }

}
