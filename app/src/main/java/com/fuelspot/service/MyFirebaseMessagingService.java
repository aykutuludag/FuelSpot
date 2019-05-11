package com.fuelspot.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fuelspot.LoginActivity;
import com.fuelspot.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationCompat.Builder builder;
    private PendingIntent pIntent;
    String title, newsURL;
    Intent intent;

    @Override
    public void onNewToken(String token) {
        Log.d("Firebase", "Refreshed token: " + token);
        System.out.println("FirebaseToken: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
        } else {
            title = getString(R.string.app_name);
        }

        if (remoteMessage.getData() != null && remoteMessage.getData().size() > 0) {
            newsURL = remoteMessage.getData().get("URL");
        } else {
            newsURL = "https://fuelspot.com.tr";
        }

        sendNotification(title, newsURL);
    }

    private void sendNotification(String messageTitle, String newsLink) {
        intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (newsLink.length() > 0) {
            // This is news notification.
            intent.putExtra("URL", newsLink);
        }

        pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

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

        if (messageTitle != null && messageTitle.length() > 0) {
            notificationBuild(getString(R.string.app_name), messageTitle);
        } else {
            notificationBuild(getString(R.string.app_name), newsLink);
        }


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
