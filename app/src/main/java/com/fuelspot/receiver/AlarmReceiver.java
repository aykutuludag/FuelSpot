package com.fuelspot.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.R;
import com.fuelspot.model.StationItem;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
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

import static com.fuelspot.MainActivity.FENCE_RECEIVER_ACTION;
import static com.fuelspot.MainActivity.fullStationList;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class AlarmReceiver extends BroadcastReceiver {

    private GoogleApiClient client;
    private PendingIntent mPendingIntent;
    private AwarenessFence locationFence;
    private AwarenessFence vehicleFence;
    private Context mContext;
    private SharedPreferences prefs;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;
    boolean doesLocationWorking;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        prefs = mContext.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);

        if (!isSuperUser) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                // If phone restarted, this section detects that and reschedule alarm
                scheduleAlarm();
            } else {
                // Every 30 mins, re-create fences.
                createFences();
            }
        }
    }

    private void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent myIntent = new Intent(mContext, AlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            Calendar currentTime = Calendar.getInstance();
            alarmManager.setInexactRepeating(AlarmManager.RTC, currentTime.getTimeInMillis(), AlarmManager.INTERVAL_HALF_HOUR, mPendingIntent);
        }
    }

    @SuppressLint("MissingPermission")
    private void createFences() {
        doesLocationWorking = true;

        // Connect to Awareness API
        client = new GoogleApiClient.Builder(mContext)
                .addApi(Awareness.API)
                .build();
        client.connect();

        vehicleFence = DetectedActivityFence.during(DetectedActivityFence.IN_VEHICLE);

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15 * 60 * 1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mContext != null && locationResult != null) {
                    synchronized (this) {
                        super.onLocationResult(locationResult);
                        Location locCurrent = locationResult.getLastLocation();
                        if (locCurrent != null) {
                            if (locCurrent.getAccuracy() <= mapDefaultStationRange * 2) {
                                Location locLastKnown = new Location("");
                                locLastKnown.setLatitude(Double.parseDouble(userlat));
                                locLastKnown.setLongitude(Double.parseDouble(userlon));

                                userlat = String.valueOf(locCurrent.getLatitude());
                                userlon = String.valueOf(locCurrent.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();

                                float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                if (fullStationList != null) {
                                    if (fullStationList.size() == 0 || (distanceInMeter >= (mapDefaultRange / 2))) {
                                        // User's position has been changed. Load new stations
                                        fetchStations();
                                    } else {
                                        for (int i = 0; i < fullStationList.size(); i++) {
                                            double stationLat = Double.parseDouble(fullStationList.get(i).getLocation().split(";")[0]);
                                            double stationLon = Double.parseDouble(fullStationList.get(i).getLocation().split(";")[1]);
                                            locationFence = LocationFence.in(stationLat, stationLon, 50, 10000L);
                                            AwarenessFence userAtStation = AwarenessFence.and(locationFence);
                                            registerFence(String.valueOf(fullStationList.get(i).getID()), userAtStation);
                                        }
                                    }
                                } else {
                                    fetchStations();
                                }
                            }
                        }
                    }
                }
            }
        };
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void fetchStations() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mContext.getString(R.string.API_SEARCH_STATIONS) + "?location=" + userlat + ";" + userlon + "&radius=" + mapDefaultRange + "&AUTH_KEY=" + mContext.getString(R.string.fuelspot_api_key),
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
                                    item.setLastUpdated(obj.getString("lastUpdated"));
                                    item.setDistance((int) obj.getDouble("distance"));

                                    // Add fence
                                    double lat = Double.parseDouble(item.getLocation().split(";")[0]);
                                    double lon = Double.parseDouble(item.getLocation().split(";")[1]);
                                    locationFence = LocationFence.in(lat, lon, 50, 10000L);
                                    AwarenessFence userAtStation = AwarenessFence.and(locationFence);
                                    registerFence(String.valueOf(item.getID()), userAtStation);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (mapDefaultRange == 2500) {
                                mapDefaultRange = 5000;
                                fetchStations();
                            } else if (mapDefaultRange == 5000) {
                                mapDefaultRange = 10000;
                                fetchStations();
                            } else if (mapDefaultRange == 10000) {
                                mapDefaultRange = 25000;
                                fetchStations();
                            } else if (mapDefaultRange == 25000) {
                                mapDefaultRange = 50000;
                                fetchStations();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
        };

        //Adding request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(stringRequest);
    }

    private void registerFence(final String fenceKey, final AwarenessFence fence) {
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 13200, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Awareness.FenceApi.updateFences(client, new FenceUpdateRequest.Builder().addFence(fenceKey, fence, mPendingIntent).build()).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    System.out.println("Fence was successfully registered.");
                } else {
                    System.out.println("Fence could not be registered: " + status);
                }
            }
        });

        if (doesLocationWorking) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            doesLocationWorking = false;
        }
    }
}