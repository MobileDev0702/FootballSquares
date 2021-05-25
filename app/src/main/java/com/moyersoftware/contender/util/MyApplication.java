package com.moyersoftware.contender.util;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.google.firebase.database.FirebaseDatabase;
import com.moyersoftware.contender.R;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class MyApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        // Init Facebook SDK
        FacebookSdk.sdkInitialize(this);

        // Init Firebase SDK
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        // Init calligraphy library
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    public static Context getContext() {
        return sContext;
    }
}
