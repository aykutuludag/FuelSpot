package com.fuelspot.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.fuelspot.MainActivity.GeofenceScheduler;

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
}