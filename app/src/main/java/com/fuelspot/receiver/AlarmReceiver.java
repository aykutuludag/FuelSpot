package com.fuelspot.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
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
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.AddFuel;
import com.fuelspot.R;
import com.fuelspot.model.StationItem;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResult;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import static com.facebook.login.widget.ProfilePictureView.TAG;
import static com.fuelspot.FragmentStations.fullStationList;
import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class AlarmReceiver extends BroadcastReceiver {

    NotificationCompat.Builder builder;
    NotificationManager notificationManager;
    GoogleApiClient client;
    PendingIntent mPendingIntent;
    AwarenessFence locationFence, vehicleFence;
    Context mContext;
    LocationRequest mLocationRequest;
    SharedPreferences prefs;
    LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        prefs = mContext.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //If phone restarted, this section detects that and recreate geofences
            scheduleAlarm();
        } else {
            // Every XX mins, re-create fences.
            createFences();
        }
    }

    void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent myIntent = new Intent(mContext, AlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (alarmManager != null) {
            Calendar currentTime = Calendar.getInstance();
            alarmManager.setInexactRepeating(AlarmManager.RTC, currentTime.getTimeInMillis(), AlarmManager.INTERVAL_HALF_HOUR, mPendingIntent);
        }
    }

    @SuppressLint("MissingPermission")
    void createFences() {
        // Connect to Awareness API
        client = new GoogleApiClient.Builder(mContext)
                .addApi(Awareness.API)
                .build();
        client.connect();

        vehicleFence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE);

        if (fullStationList != null && fullStationList.size() > 0) {
            for (int i = 0; i < fullStationList.size(); i++) {
                double stationLat = Double.parseDouble(fullStationList.get(i).getLocation().split(";")[0]);
                double stationLon = Double.parseDouble(fullStationList.get(i).getLocation().split(";")[1]);
                locationFence = LocationFence.in(stationLat, stationLon, 50, 15000L);
                AwarenessFence userAtStation = AwarenessFence.and(vehicleFence, locationFence);
                registerFence(String.valueOf(fullStationList.get(i).getID()), locationFence);
            }
        } else {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (mContext != null && locationResult != null) {
                        synchronized (mContext) {
                            super.onLocationResult(locationResult);
                            Location locCurrent = locationResult.getLastLocation();
                            if (locCurrent != null) {
                                if (locCurrent.getAccuracy() <= mapDefaultStationRange * 5) {
                                    userlat = String.valueOf(locCurrent.getLatitude());
                                    userlon = String.valueOf(locCurrent.getLongitude());
                                    prefs.edit().putString("lat", userlat).apply();
                                    prefs.edit().putString("lon", userlon).apply();
                                    getVariables(prefs);
                                    fetchStations();
                                }
                            }
                        }
                    }
                }
            };
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void fetchStations() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.API_SEARCH_STATION),
                new Response.Listener<String>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    StationItem item = new StationItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationName(obj.getString("name"));
                                    item.setVicinity(obj.getString("vicinity"));
                                    item.setCountryCode(obj.getString("country"));
                                    item.setLocation(obj.getString("location"));
                                    item.setGoogleMapID(obj.getString("googleID"));
                                    item.setFacilities(obj.getString("facilities"));
                                    item.setLicenseNo(obj.getString("licenseNo"));
                                    item.setOwner(obj.getString("owner"));
                                    item.setPhotoURL(obj.getString("logoURL"));
                                    item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                    item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                    item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                    item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
                                    item.setIsActive(obj.getInt("isActive"));
                                    item.setLastUpdated(obj.getString("lastUpdated"));
                                    item.setDistance((int) obj.getDouble("distance"));
                                    fullStationList.add(item);

                                    // Add fence
                                    double lat = Double.parseDouble(item.getLocation().split(";")[0]);
                                    double lon = Double.parseDouble(item.getLocation().split(";")[1]);
                                    locationFence = LocationFence.in(lat, lon, 50, 15000L);
                                    AwarenessFence userAtStation = AwarenessFence.and(vehicleFence, locationFence);
                                    registerFence(String.valueOf(fullStationList.get(i).getID()), locationFence);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            reTry();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                params.put("location", userlat + ";" + userlon);
                params.put("radius", String.valueOf(mapDefaultRange));
                params.put("AUTH_KEY", mContext.getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }

    void reTry() {
        // Maybe s/he is in the countryside. Increase mapDefaultRange, decrease mapDefaultZoom
        if (mapDefaultRange == 2500) {
            mapDefaultRange = 5000;
            mapDefaultZoom = 12f;
            fetchStations();
        } else if (mapDefaultRange == 5000) {
            mapDefaultRange = 10000;
            mapDefaultZoom = 11f;
            fetchStations();
        } else if (mapDefaultRange == 10000) {
            mapDefaultRange = 25000;
            mapDefaultZoom = 10f;
            fetchStations();
        } else if (mapDefaultRange == 25000) {
            mapDefaultRange = 50000;
            mapDefaultZoom = 8.75f;
            fetchStations();
        }
    }

    protected void registerFence(final String fenceKey, final AwarenessFence fence) {
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Awareness.FenceApi.updateFences(client, new FenceUpdateRequest.Builder().addFence(fenceKey, fence, mPendingIntent).build()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Log.i(TAG, "Fence was successfully registered.");
                    queryFence(fenceKey);
                } else {
                    Log.e(TAG, "Fence could not be registered: " + status);
                }
            }
        });
    }

    protected void queryFence(final String fenceKey) {
        Awareness.FenceApi.queryFences(client,
                FenceQueryRequest.forFences(Collections.singletonList(fenceKey)))
                .setResultCallback(new ResultCallback<FenceQueryResult>() {
                    @Override
                    public void onResult(@NonNull FenceQueryResult fenceQueryResult) {
                        if (!fenceQueryResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not query fence: " + fenceKey);
                            return;
                        }
                        FenceStateMap map = fenceQueryResult.getFenceStateMap();
                        for (String fenceKey : map.getFenceKeys()) {
                            FenceState fenceState = map.getFenceState(fenceKey);
                            if (fenceState.getCurrentState() == 2) {
                                sendNotification(fenceState.getFenceKey());
                            }
                        }
                    }
                });
    }

    private void sendNotification(String stationID) {
        Intent intentLauncher = new Intent(mContext, AddFuel.class);
        intentLauncher.putExtra("STATION_ID", Integer.parseInt(stationID));
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mPendingIntent = PendingIntent.getActivity(mContext, 2, intentLauncher, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Yakıt ekleme";
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
                .setContentText("Yakıt mı alıyorsun? Eklemek için hemen tıkla!")
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