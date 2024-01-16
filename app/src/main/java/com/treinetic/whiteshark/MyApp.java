package com.treinetic.whiteshark;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.androidnetworking.AndroidNetworking;
//import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.treinetic.whiteshark.util.Connections;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class MyApp extends MultiDexApplication {

    public static Context context;
    public static MyApp INSTANCE;
    public static FirebaseAnalytics analytics;
    public static FirebaseCrashlytics crashlytics;

    @Override
    public void onCreate() {
        super.onCreate();
//        MultiDex.install(this);
        INSTANCE = this;
        MyApp.context = getApplicationContext();
        FirebaseApp.initializeApp(this);
        analytics = FirebaseAnalytics.getInstance(this);
        crashlytics = FirebaseCrashlytics.getInstance();
        Logger.addLogAdapter(new AndroidLogAdapter());
        configureNetworkLib();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
        Connections.Companion.getInstance().listenToNetwork();
    }

    public void configureNetworkLib() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();
        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);
    }

    public static Context getAppContext() {
        return MyApp.context;
    }

    public static boolean isDebugMode() {
        return BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug");
    }
}
