package com.fuelspot.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fuelspot.LoginActivity;
import com.fuelspot.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    SharedPreferences prefs;
    boolean alarm;
    NotificationCompat.Builder builder;
    NotificationManager notificationManager;
    PendingIntent pIntent;

    @Override
    public void onNewToken(String token) {
        Log.d("Firebase", "Refreshed token: " + token);
        System.out.println("FirebaseToken: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        prefs = getSharedPreferences("Preferences", MODE_PRIVATE);
        alarm = prefs.getBoolean("Alarm", true);
        if (alarm) {
            if (remoteMessage.getNotification() != null) {
                // Consoledan mesaj gönderildiğinde burası tetiklenecektir
                String title = remoteMessage.getNotification().getTitle();
                sendNotification(title);
            }
        }
    }

    private void sendNotification(String messageTitle) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Other notifications";
            String channelDesc = "News, app updates, important events, campaigns...";
            NotificationChannel mChannel = new NotificationChannel(String.valueOf(0), channelName, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(channelDesc);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{500, 500, 500, 500});
            notificationManager.createNotificationChannel(mChannel);

            builder = new NotificationCompat.Builder(this, String.valueOf(0));
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        notificationBuild(getString(R.string.app_name), messageTitle);

        //Send notification
        Notification notification = builder.build();
        notificationManager.notify(0, notification);
    }

    private void notificationBuild(String messageTitle, String messageBody) {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        builder.setContentTitle(messageTitle)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bm)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setVibrate(new long[]{500, 500, 500, 500})
                .setContentIntent(pIntent);
    }
}
