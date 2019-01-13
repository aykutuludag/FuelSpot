package com.fuelspot;

import android.content.Context;
import android.content.IntentFilter;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.fuelspot.receiver.FenceReceiver;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;

public class Application extends MultiDexApplication {

    String trackingId = "UA-120925005-4";
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        // Register broadcast receiver for fences.
        registerReceiver(new FenceReceiver(), new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(trackingId);
        }
        return mTracker;
    }
}