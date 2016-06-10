package com.magomed.gamzatov.universalmarket;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static MyApplication sInstance;
    private static String login;
    private static String password;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static MyApplication getsInstance(){
        return sInstance;
    }

    public static Context getAppContext(){
        return sInstance.getApplicationContext();
    }

    public static String getLogin() {
        return login;
    }

    public static void setLogin(String login) {
        MyApplication.login = login;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        MyApplication.password = password;
    }
}
