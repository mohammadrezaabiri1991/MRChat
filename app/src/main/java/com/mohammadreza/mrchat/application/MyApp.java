package com.mohammadreza.mrchat.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import androidx.multidex.MultiDex;

import com.google.firebase.database.FirebaseDatabase;
import com.mohammadreza.mrchat.R;


public class MyApp extends Application {
    public static Typeface TYPE_FACE;
    public static Context appContext;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this;
        TYPE_FACE = Typeface.createFromAsset(getAssets(), getString(R.string.iran_sans));
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//            FirebaseDatabase.getInstance().setPersistenceCacheSizeBytes(3000);
        } catch (Exception e) {
        }
    }


}
