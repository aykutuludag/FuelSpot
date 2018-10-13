package com.fuelspot.superuser;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.fuelspot.AnalyticsApplication;
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.superuser.AdminMainActivity.isSuperVerified;

public class FragmentOwnedStation extends Fragment {

    SharedPreferences prefs;
    MapView mMapView;
    RequestQueue queue;
    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;
    ImageView stationIcon;
    Button openPurchases, openComments, openCampaings, openPosts;
    FusedLocationProviderClient mFusedLocationClient;
    Circle circle;
    private GoogleMap googleMap;

    public static FragmentOwnedStation newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "OwnedStation");

        FragmentOwnedStation fragment = new FragmentOwnedStation();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_owned_station, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("MyStation");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(getActivity());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

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
                if (isSuperVerified == 1) {
                    Intent i = new Intent(getActivity(), SuperPurchases.class);
                    startActivity(i);
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        openComments = rootView.findViewById(R.id.buttonComments);
        openComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSuperVerified == 1) {
                    Intent i = new Intent(getActivity(), SuperComments.class);
                    startActivity(i);
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        openCampaings = rootView.findViewById(R.id.buttonCampaings);
        openCampaings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSuperVerified == 1) {
                    Intent i = new Intent(getActivity(), SuperCampaings.class);
                    startActivity(i);
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        openPosts = rootView.findViewById(R.id.buttonPosts);
        openPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSuperVerified == 1) {
                    Toast.makeText(getActivity(), "Coming soon...", Toast.LENGTH_LONG).show();
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_LOCATION, REQUEST_LOCATION);
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
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(true);

                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {

                                    Location loc1 = new Location("");
                                    loc1.setLatitude(Double.parseDouble(userlat));
                                    loc1.setLongitude(Double.parseDouble(userlon));

                                    Location loc2 = new Location("");
                                    loc2.setLatitude(location.getLatitude());
                                    loc2.setLongitude(location.getLongitude());

                                    float distanceInMeters = loc1.distanceTo(loc2);

                                    if (distanceInMeters >= 50f) {
                                        userlat = String.valueOf(location.getLatitude());
                                        userlon = String.valueOf(location.getLongitude());
                                        prefs.edit().putString("lat", userlat).apply();
                                        prefs.edit().putString("lon", userlon).apply();
                                        MainActivity.getVariables(prefs);
                                    }
                                } else {
                                    LocationRequest mLocationRequest = new LocationRequest();
                                    mLocationRequest.setInterval(60000);
                                    mLocationRequest.setFastestInterval(5000);
                                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                }
                            }
                        });

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
                            loc1.setLatitude(Double.parseDouble(userlat));
                            loc1.setLongitude(Double.parseDouble(userlon));
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
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(16.75f).build();
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition
                                    (cameraPosition));

                            circle = googleMap.addCircle(new CircleOptions()
                                    .center(sydney)
                                    .radius(50)
                                    .strokeColor(Color.RED));
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
                params.put("stationID", String.valueOf(AdminMainActivity.superStationID));

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
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are car_placeholder.
                if (ActivityCompat.checkSelfPermission(getActivity(), PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    //Request location updates:
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        userlat = String.valueOf(location.getLatitude());
                                        userlon = String.valueOf(location.getLongitude());
                                        prefs.edit().putString("lat", userlat).apply();
                                        prefs.edit().putString("lon", userlon).apply();
                                        MainActivity.getVariables(prefs);
                                        loadMap();
                                    } else {
                                        LocationRequest mLocationRequest = new LocationRequest();
                                        mLocationRequest.setInterval(15000);
                                        mLocationRequest.setFastestInterval(1000);
                                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    }
                                }
                            });
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
            checkLocationPermission();
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
