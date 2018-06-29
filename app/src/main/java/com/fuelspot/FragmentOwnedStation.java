package com.fuelspot;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

public class FragmentOwnedStation extends Fragment {

    SharedPreferences prefs;
    MapView mMapView;
    RequestQueue queue;
    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;
    ImageView stationIcon;
    Button openPurchases, openComments, openCampaings, openPosts;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_owned_station, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("İstasyonlar");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(getActivity());

        //Map
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        MapsInitializer.initialize(getActivity().getApplicationContext());

        //Card
        textName = rootView.findViewById(R.id.ownedStationName);
        textVicinity = rootView.findViewById(R.id.ownedStationAddress);
        textDistance = rootView.findViewById(R.id.distanceBetweenOwner);
        textGasoline = rootView.findViewById(R.id.priceGasoline);
        textDiesel = rootView.findViewById(R.id.priceDiesel);
        textLPG = rootView.findViewById(R.id.priceLPG);
        textElectricity = rootView.findViewById(R.id.priceElectricity);
        textLastUpdated = rootView.findViewById(R.id.lastUpdateTime);
        stationIcon = rootView.findViewById(R.id.stationLogo);

        //Buttons
        openPurchases = rootView.findViewById(R.id.buttonPurchases);
        openPurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SuperPurchases.class);
                startActivity(i);
            }
        });

        openComments = rootView.findViewById(R.id.buttonComments);
        openComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SuperComments.class);
                startActivity(i);
            }
        });

        openCampaings = rootView.findViewById(R.id.buttonCampaings);
        openCampaings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SuperCampaings.class);
                startActivity(i);
            }
        });

        openPosts = rootView.findViewById(R.id.buttonPosts);
        openPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Coming soon...", Toast.LENGTH_LONG).show();
            }
        });

        checkLocationPermission();

        return rootView;
    }

    void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{MainActivity.PERMISSIONS_LOCATION}, MainActivity.REQUEST_LOCATION);
        } else {
            loadMap();
        }
    }

    void loadMap() {
        //Detect location and set on map
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                googleMap.getUiSettings().setScrollGesturesEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(true);

                loadStationDetails();
            }
        });
    }

    void loadStationDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);
                            textName.setText(obj.getString("name"));
                            textVicinity.setText(obj.getString("vicinity"));
                            Location loc1 = new Location("");
                            loc1.setLatitude(MainActivity.userlat);
                            loc1.setLongitude(MainActivity.userlon);
                            Location loc2 = new Location("");
                            loc2.setLatitude(Double.parseDouble(obj.getString("location").split(";")[0]));
                            loc2.setLongitude(Double.parseDouble(obj.getString("location").split(";")[1]));
                            float distanceInMeters = loc1.distanceTo(loc2);
                            textDistance.setText((int) distanceInMeters + " m");

                            AdminMainActivity.ownedGasolinePrice = obj.getDouble("gasolinePrice");
                            prefs.edit().putFloat("superGasolinePrice", (float) AdminMainActivity.ownedGasolinePrice).apply();
                            textGasoline.setText(AdminMainActivity.ownedGasolinePrice + "TL");

                            AdminMainActivity.ownedDieselPrice = obj.getDouble("dieselPrice");
                            prefs.edit().putFloat("superDieselPrice", (float) AdminMainActivity.ownedDieselPrice).apply();
                            textDiesel.setText(AdminMainActivity.ownedDieselPrice + "TL");

                            AdminMainActivity.ownedLPGPrice = obj.getDouble("lpgPrice");
                            prefs.edit().putFloat("superLPGPrice", (float) AdminMainActivity.ownedLPGPrice).apply();
                            textLPG.setText(AdminMainActivity.ownedLPGPrice + "TL");

                            AdminMainActivity.ownedElectricityPrice = obj.getDouble("electricityPrice");
                            prefs.edit().putFloat("superElectricityPrice", (float) AdminMainActivity.ownedElectricityPrice).apply();
                            textElectricity.setText(AdminMainActivity.ownedElectricityPrice + "TL");

                            //Last updated
                            try {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                Date date = format.parse(obj.getString("lastUpdated"));
                                textLastUpdated.setReferenceTime(date.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Glide.with(getActivity()).load(Uri.parse(obj.getString("photoURL"))).into(stationIcon);

                            //Add marker to stationLoc
                            String[] locationHolder = AdminMainActivity.superStationLocation.split(";");
                            LatLng sydney = new LatLng(Double.parseDouble(locationHolder[0]), Double.parseDouble(locationHolder[1]));
                            googleMap.addMarker(new MarkerOptions().position(sydney).title(AdminMainActivity.superStationName).snippet(AdminMainActivity.superStationAddress));

                            //Zoom-in camera
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(16f).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
                                    (cameraPosition));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("placeID", String.valueOf(AdminMainActivity.superGoogleID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are car_placeholder.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        LocationManager locationManager = (LocationManager)
                                getActivity().getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();

                        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                        if (location != null) {
                            MainActivity.userlat = (float) location.getLatitude();
                            MainActivity.userlon = (float) location.getLongitude();
                            prefs.edit().putFloat("lat", MainActivity.userlat).apply();
                            prefs.edit().putFloat("lon", MainActivity.userlon).apply();
                            MainActivity.getVariables(prefs);
                        }

                        loadMap();
                    }
                } else {
                    Toast.makeText(getActivity(), "İZİN VERİLMEDİ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }
}
