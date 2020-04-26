package com.fuelspot;

import android.content.Context;
import android.content.IntentFilter;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.fuelspot.receiver.FenceReceiver;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.util.Arrays;

import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;

public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // Register broadcast receiver for fences.
        registerReceiver(new FenceReceiver(), new IntentFilter(FENCE_RECEIVER_ACTION));

        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("196A02FCAD99E6C3DA35DB5C0702B4EE")).build();
        MobileAds.setRequestConfiguration(configuration);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        MapsInitializer.initialize(this);
        Places.initialize(getApplicationContext(), getString(R.string.google_key));

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:82431707983:android:bb97ba32596a54d8") // Required for Analytics.
                .setApiKey(getString(R.string.google_key)) // Required for Auth.
                .build();
        FirebaseApp.initializeApp(this, options, "FuelSpot");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}