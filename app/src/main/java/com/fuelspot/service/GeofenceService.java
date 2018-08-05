package com.fuelspot.service;


import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.LoginActivity;
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import eu.amirs.JSON;

import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;


public class GeofenceService extends IntentService {

    PendingIntent mGeofencePendingIntent;
    SharedPreferences prefs;
    RequestQueue queue;
    Context mContext;
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList = new ArrayList<>();

    public GeofenceService() {
        super("GeofenceService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        queue = Volley.newRequestQueue(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener((Activity) mContext, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                MainActivity.userlat = String.valueOf(location.getLatitude());
                                MainActivity.userlon = String.valueOf(location.getLongitude());
                                prefs.edit().putString("lat", MainActivity.userlat).apply();
                                prefs.edit().putString("lon", MainActivity.userlon).apply();
                                MainActivity.getVariables(prefs);
                                mGeofenceList.add(new Geofence.Builder().setRequestId("ev")
                                        .setCircularRegion(Double.parseDouble(MainActivity.userlat), Double.parseDouble(MainActivity.userlon), mapDefaultStationRange)
                                        .setExpirationDuration(45 * 60 * 1000)
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                                        .build());
                                addGeofence();
                                fetchStation();
                            } else {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(60000);
                                mLocationRequest.setFastestInterval(5000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            }
                        }
                    });
        }

    }

    void fetchStation() {
        //Search stations in a radius of 5000m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + MainActivity.userlat + "," + MainActivity.userlon + "&radius=" + mapDefaultRange + "&type=gas_station&opennow=true&key=" + getString(R.string.google_api_key);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (response != null && response.length() > 0) {
                            for (int i = 0; i < json.key("results").count(); i++) {
                                double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                                double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();

                                //Add them to the geofence list
                                mGeofenceList.add(new Geofence.Builder().setRequestId(json.key("results").index(i).key("place_id").stringValue())
                                        .setCircularRegion(lat, lon, mapDefaultStationRange)
                                        .setExpirationDuration(60 * 60 * 1000)
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                                        .build());
                                addGeofence();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Do nothing
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    void addGeofence() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
            System.out.println("GEOFENCE EKLENDİ:");
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent != null && !geofencingEvent.hasError()) {
            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                String geofenceTransitionDetails = "Yakıt mı alıyorsunuz? Eklemek için tıklayın!";

                // Send notification and log the transition details.
                sendNotification("FuelSpot", geofenceTransitionDetails);
                Log.i("Error", geofenceTransitionDetails);
            }
        }
    }

    private void sendNotification(String messageTitle, String messageBody) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bm)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}