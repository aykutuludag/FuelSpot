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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.LoginActivity;
import com.fuelspot.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.MainActivity.firebaseToken;
import static com.fuelspot.MainActivity.isSigned;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.username;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String title, newsURL;
    Intent intent;
    private NotificationCompat.Builder builder;
    private PendingIntent pIntent;

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("Firebase", "Refreshed token: " + token);
        firebaseToken = token;
        SharedPreferences prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        prefs.edit().putString("firebaseToken", firebaseToken).apply();

        if (isSigned) {
            registerToken();
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
        } else {
            title = getString(R.string.app_name);
        }

        if (remoteMessage.getData().size() > 0) {
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

    private void registerToken() {
        String url;
        if (isSuperUser) {
            url = getString(R.string.API_SUPERUSER_UPDATE);
        } else {
            url = getString(R.string.API_UPDATE_USER);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                Log.d("REGISTER_TOKEN", "SUCCESS");
                            } else {
                                Log.d("REGISTER_TOKEN", "FAIL");
                            }
                        } else {
                            Log.d("REGISTER_TOKEN", "FAIL");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("REGISTER_TOKEN", "FAIL");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("token", firebaseToken);
                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
