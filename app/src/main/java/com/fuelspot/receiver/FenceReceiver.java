package com.fuelspot.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.fuelspot.AddFuel;
import com.fuelspot.R;
import com.google.android.gms.awareness.fence.FenceState;

import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;

public class FenceReceiver extends BroadcastReceiver {

    Context mContext;
    NotificationCompat.Builder builder;
    NotificationManager notificationManager;
    PendingIntent mPendingIntent;
    int currentStationID;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        if (FENCE_RECEIVER_ACTION.equals(intent.getAction())) {
            // User entered the station
            FenceState fenceState = FenceState.extract(intent);
            if (fenceState.getCurrentState() == 2) {
                // 2 means user is at Station
                if (currentStationID != Integer.parseInt(fenceState.getFenceKey())) {
                    currentStationID = Integer.parseInt(fenceState.getFenceKey());
                    sendNotification(fenceState.getFenceKey());
                }
            }
        }
    }

    private void sendNotification(String stationID) {
        Intent intentLauncher = new Intent(mContext, AddFuel.class);
        intentLauncher.putExtra("STATION_ID", Integer.parseInt(stationID));
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mPendingIntent = PendingIntent.getActivity(mContext, 13200, intentLauncher, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = mContext.getString(R.string.add_fuel);
            String channelDesc = "Siz istasyona girdiğinizde tetiklenir. Bildirim aracılığıyla yakıt ekleyebilirsiniz.";
            NotificationChannel mChannel = new NotificationChannel(String.valueOf(0), channelName, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(channelDesc);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{500, 500, 500, 500});
            notificationManager.createNotificationChannel(mChannel);

            builder = new NotificationCompat.Builder(mContext, String.valueOf(0));
        } else {
            builder = new NotificationCompat.Builder(mContext);
        }

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        builder.setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.geofence_notification_title))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(mPendingIntent)
                .setLargeIcon(bm)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[]{500, 500, 500, 500});

        // Send notification
        Notification notification = builder.build();
        notificationManager.notify(0, notification);
    }
}
