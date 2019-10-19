package com.fuelspot;

import android.content.Context;
import android.content.IntentFilter;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.fuelspot.receiver.FenceReceiver;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.libraries.places.api.Places;

import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;

public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // Register broadcast receiver for fences.
        registerReceiver(new FenceReceiver(), new IntentFilter(FENCE_RECEIVER_ACTION));
        MobileAds.initialize(this, getString(R.string.admobAppId));
        MapsInitializer.initialize(this);
        Places.initialize(getApplicationContext(), getString(R.string.google_key));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}