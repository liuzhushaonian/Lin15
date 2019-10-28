package com.xp.legend.lin17.utils;

import android.app.Application;
import android.content.Context;

public class LinApp extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }

    public static Context getContext(){

        return context;

    }

}
