package org.uusoftware.fuelify;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsApplication extends Application {

    String trackingId = "UA-66337763-9";
    private Tracker mTracker;


    @Override
    public void onCreate() {
        super.onCreate();
        AppEventsLogger.activateApp(this);
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(trackingId);
        }
        return mTracker;
    }
}