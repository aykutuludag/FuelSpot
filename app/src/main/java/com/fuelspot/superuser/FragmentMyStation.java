package com.fuelspot.superuser;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.fuelspot.Application;
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.fuelspot.StationComments;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.universalTimeFormat;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;
import static com.fuelspot.superuser.SuperMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.SuperMainActivity.superLastUpdate;
import static com.fuelspot.superuser.SuperMainActivity.superStationAddress;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;
import static com.fuelspot.superuser.SuperMainActivity.superStationLocation;
import static com.fuelspot.superuser.SuperMainActivity.superStationLogo;
import static com.fuelspot.superuser.SuperMainActivity.superStationName;

public class FragmentMyStation extends Fragment {

    SharedPreferences prefs;
    MapView mMapView;
    RequestQueue queue;
    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;
    ImageView stationIcon;
    Button editStation, openPurchases, openComments, openCampaings;
    FusedLocationProviderClient mFusedLocationClient;
    Location locLastKnown = new Location("");
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    View rootView;
    private GoogleMap googleMap;

    public static FragmentMyStation newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "OwnedStation");

        FragmentMyStation fragment = new FragmentMyStation();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_mystation, container, false);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("MyStation");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            //Variables
            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            queue = Volley.newRequestQueue(getActivity());
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            //Map
            locLastKnown.setLatitude(Double.parseDouble(userlat));
            locLastKnown.setLongitude(Double.parseDouble(userlon));

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    synchronized (this) {
                        super.onLocationResult(locationResult);
                        Location locCurrent = locationResult.getLastLocation();
                        if (locCurrent != null) {
                            if (locCurrent.getAccuracy() <= mapDefaultStationRange) {
                                userlat = String.valueOf(locCurrent.getLatitude());
                                userlon = String.valueOf(locCurrent.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();
                                MainActivity.getVariables(prefs);

                                float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                if (distanceInMeter >= mapDefaultStationRange) {
                                    locLastKnown.setLatitude(Double.parseDouble(userlat));
                                    locLastKnown.setLongitude(Double.parseDouble(userlon));
                                }
                            }
                        } else {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            };


            mMapView = rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();

            //Card
            textName = rootView.findViewById(R.id.ownedStationName);
            textVicinity = rootView.findViewById(R.id.ownedStationAddress);
            textDistance = rootView.findViewById(R.id.distanceBetweenOwner);
            textGasoline = rootView.findViewById(R.id.priceGasoline);
            textDiesel = rootView.findViewById(R.id.priceDiesel);
            textLPG = rootView.findViewById(R.id.priceLPG);
            textElectricity = rootView.findViewById(R.id.priceElectricity);
            textLastUpdated = rootView.findViewById(R.id.lastUpdateText);
            stationIcon = rootView.findViewById(R.id.imageViewStationLogo);

            //Buttons
            editStation = rootView.findViewById(R.id.buttonEditStation);
            editStation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isStationVerified == 1) {
                        Intent i = new Intent(getActivity(), SuperUpdateStation.class);
                        startActivity(i);
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            openPurchases = rootView.findViewById(R.id.buttonPurchases);
            openPurchases.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isStationVerified == 1) {
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
                    if (isStationVerified == 1) {
                        Intent i = new Intent(getActivity(), StationComments.class);
                        i.putExtra("ISTASYON_ID", superStationID);
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
                    if (isStationVerified == 1) {
                        Intent i = new Intent(getActivity(), SuperCampaings.class);
                        startActivity(i);
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
        return rootView;
    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
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
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setAllGesturesEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.setTrafficEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                loadStationDetails();
            }
        });
    }

    void loadStationDetails() {
        googleMap.clear();

        textName.setText(superStationName);
        textVicinity.setText(superStationAddress);

        Location loc1 = new Location("");
        loc1.setLatitude(Double.parseDouble(userlat));
        loc1.setLongitude(Double.parseDouble(userlon));
        Location loc2 = new Location("");
        loc2.setLatitude(Double.parseDouble(superStationLocation.split(";")[0]));
        loc2.setLongitude(Double.parseDouble(superStationLocation.split(";")[1]));
        float distanceInMeters = loc1.distanceTo(loc2);
        textDistance.setText((int) distanceInMeters + " m");

        prefs.edit().putFloat("superGasolinePrice", ownedGasolinePrice).apply();
        textGasoline.setText(ownedGasolinePrice + "TL");

        prefs.edit().putFloat("superDieselPrice", ownedDieselPrice).apply();
        textDiesel.setText(ownedDieselPrice + "TL");

        prefs.edit().putFloat("superLPGPrice", ownedLPGPrice).apply();
        textLPG.setText(ownedLPGPrice + "TL");

        prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();
        textElectricity.setText(ownedElectricityPrice + "TL");

        //Last updated
        try {
            SimpleDateFormat format = new SimpleDateFormat(universalTimeFormat, Locale.getDefault());
            Date date = format.parse(superLastUpdate);
            textLastUpdated.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Glide.with(getActivity()).load(superStationLogo).into(stationIcon);

        //Add marker to stationLoc
        String[] locationHolder = superStationLocation.split(";");
        LatLng sydney = new LatLng(Double.parseDouble(locationHolder[0]), Double.parseDouble(locationHolder[1]));
        googleMap.addMarker(new MarkerOptions().position(sydney).title(superStationName).snippet(superStationAddress));

        //Zoom-in camera
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(17f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));
        googleMap.addCircle(new CircleOptions()
                .center(sydney)
                .radius(mapDefaultStationRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (ActivityCompat.checkSelfPermission(getActivity(), PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    loadMap();
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }

        if (mFusedLocationClient != null) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }

        checkLocationPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mMapView != null) {
            mMapView.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMapView != null) {
            mMapView.onDestroy();
        }

        if (queue != null) {
            queue.cancelAll(getActivity());
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
