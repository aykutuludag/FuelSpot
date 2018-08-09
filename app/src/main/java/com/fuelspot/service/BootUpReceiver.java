package com.fuelspot.service;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;
import static com.fuelspot.MainActivity.isGeofenceOpen;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //If phone restarted, this section detects that and recreate alarm
            GeofenceScheduler(context);
        } else {
            // GeofenceAlarm triggered. Call the service.
            context.startService(new Intent(context, GeofenceService.class));
        }
    }

    public void GeofenceScheduler(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        Intent myIntent = new Intent(mContext, GeofenceService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            if (isGeofenceOpen && !isGeoServiceRunning(mContext)) {
                // Start the service
                mContext.startService(new Intent(mContext, GeofenceService.class));

                // and set alarm for every hour
                Calendar currentTime = Calendar.getInstance();
                alarmManager.setInexactRepeating(AlarmManager.RTC, currentTime.getTimeInMillis() + 60 * 60 * 1000, AlarmManager.INTERVAL_HOUR, pendingIntent);
            } else {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    private boolean isGeoServiceRunning(Context c) {
        ActivityManager manager = (ActivityManager) c.getSystemService(ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.fuelspot.service.GeofenceService".equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }
}