package com.fuelspot.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.fuelspot.AddFuel;
import com.fuelspot.R;
import com.fuelspot.StationDetails;
import com.google.android.gms.awareness.fence.FenceState;

import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isSuperUser;

public class FenceReceiver extends BroadcastReceiver {

    private Context mContext;
    private int currentStationID;

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

                    SharedPreferences prefs = mContext.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
                    getVariables(prefs);
                    if (!isSuperUser) {
                        sendNotification(fenceState.getFenceKey());
                    }
                }
            }
        }
    }

    private void sendNotification(String stationID) {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intentAddFuel = new Intent(mContext, AddFuel.class);
        intentAddFuel.putExtra("STATION_ID", Integer.parseInt(stationID));
        intentAddFuel.putExtra("FROM_NOTIFICATION", true);
        PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, 13200, intentAddFuel, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentReportPrice = new Intent(mContext, StationDetails.class);
        intentReportPrice.putExtra("STATION_ID", Integer.parseInt(stationID));
        intentReportPrice.putExtra("FROM_NOTIFICATION", true);
        PendingIntent mPendingIntent2 = PendingIntent.getActivity(mContext, 13200, intentReportPrice, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder;
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
                .addAction(R.drawable.ic_add_fuel, mContext.getString(R.string.add_fuel), mPendingIntent)
                .addAction(R.drawable.money, mContext.getString(R.string.fab_report_price), mPendingIntent2)
                .setLargeIcon(bm)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[]{500, 500, 500, 500});

        // Send notification
        Notification notification = builder.build();
        notificationManager.notify(63000, notification);
    }
}
