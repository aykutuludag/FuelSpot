package com.fuelspot;

import android.content.Context;
import android.content.IntentFilter;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.fuelspot.receiver.FenceReceiver;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.MapsInitializer;

import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;

public class Application extends MultiDexApplication {

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        // Register broadcast receiver for fences.
        registerReceiver(new FenceReceiver(), new IntentFilter(FENCE_RECEIVER_ACTION));
        MobileAds.initialize(this, getString(R.string.admobAppId));
        MapsInitializer.initialize(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker("UA-120925005-4");
        }
        return mTracker;
    }
}