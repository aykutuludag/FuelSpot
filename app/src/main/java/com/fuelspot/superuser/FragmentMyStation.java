package com.fuelspot.superuser;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.fuelspot.Application;
import com.fuelspot.R;
import com.fuelspot.StationComments;
import com.fuelspot.adapter.MarkerAdapter;
import com.fuelspot.model.StationItem;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;
import static com.fuelspot.superuser.SuperMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.SuperMainActivity.superFacilities;
import static com.fuelspot.superuser.SuperMainActivity.superGoogleID;
import static com.fuelspot.superuser.SuperMainActivity.superLastUpdate;
import static com.fuelspot.superuser.SuperMainActivity.superLicenseNo;
import static com.fuelspot.superuser.SuperMainActivity.superStationAddress;
import static com.fuelspot.superuser.SuperMainActivity.superStationCountry;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;
import static com.fuelspot.superuser.SuperMainActivity.superStationLocation;
import static com.fuelspot.superuser.SuperMainActivity.superStationLogo;
import static com.fuelspot.superuser.SuperMainActivity.superStationName;

public class FragmentMyStation extends Fragment {

    private SharedPreferences prefs;
    private MapView mMapView;
    private RequestQueue queue;
    private TextView textName;
    private TextView textVicinity;
    private TextView textDistance;
    private TextView textGasoline;
    private TextView textDiesel;
    private TextView textLPG;
    private TextView textElectricity;
    private RelativeTimeTextView textLastUpdated;
    private ImageView stationIcon;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location locLastKnown = new Location("");
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private View rootView;
    private GoogleMap googleMap;
    NestedScrollView scrollView;
    float distanceInMeters;
    StationItem info = new StationItem();
    LatLng sydney;
    private SwipeRefreshLayout swipeContainer;

    public static FragmentMyStation newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "MyStation");

        FragmentMyStation fragment = new FragmentMyStation();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_mystation, container, false);

            // Keep screen on
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("MyStation");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            //Variables
            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            queue = Volley.newRequestQueue(getActivity());
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            scrollView = rootView.findViewById(R.id.myStationScroll);

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
                            if (locCurrent.getAccuracy() <= mapDefaultStationRange * 2) {
                                userlat = String.valueOf(locCurrent.getLatitude());
                                userlon = String.valueOf(locCurrent.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();

                                float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                if (distanceInMeter >= (mapDefaultRange / 2)) {
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
            Button editStation = rootView.findViewById(R.id.buttonEditStation);
            editStation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isStationVerified == 1) {
                        Intent i = new Intent(getActivity(), SuperUpdateStation.class);
                        startActivity(i);
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.pager), getString(R.string.station_waiting_approval), Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            Button openComments = rootView.findViewById(R.id.buttonComments);
            openComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isStationVerified == 1) {
                        Intent i = new Intent(getActivity(), StationComments.class);
                        i.putExtra("ISTASYON_ID", superStationID);
                        startActivity(i);
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.pager), getString(R.string.station_waiting_approval), Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            Button openCampaings = rootView.findViewById(R.id.buttonCampaings);
            openCampaings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isStationVerified == 1) {
                        Intent i = new Intent(getActivity(), SuperCampaings.class);
                        startActivity(i);
                    } else {
                        Snackbar.make(getActivity().findViewById(R.id.pager), getString(R.string.station_waiting_approval), Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            swipeContainer = rootView.findViewById(R.id.swipeContainer);
            // Setup refresh listener which triggers new data loading
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadMap();
                }
            });
            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            checkLocationPermission();
        }
        return rootView;
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            loadMap();
        }
    }

    private void loadMap() {
        //Detect location and set on map
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.setTrafficEnabled(true);
                googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            scrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                });
                MarkerAdapter customInfoWindow = new MarkerAdapter(getActivity());
                googleMap.setInfoWindowAdapter(customInfoWindow);
                loadStationDetails();
            }
        });
    }

    private void loadStationDetails() {
        googleMap.clear();

        //Station Icon
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.photo_placeholder)
                .error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(getActivity()).load(superStationLogo).apply(options).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                info.setStationLogoDrawable(resource);
                return false;
            }
        }).into(stationIcon);

        String[] locationHolder = superStationLocation.split(";");
        sydney = new LatLng(Double.parseDouble(locationHolder[0]), Double.parseDouble(locationHolder[1]));

        Location loc1 = new Location("");
        loc1.setLatitude(Double.parseDouble(userlat));
        loc1.setLongitude(Double.parseDouble(userlon));

        Location loc2 = new Location("");
        loc2.setLatitude(sydney.latitude);
        loc2.setLongitude(sydney.longitude);

        textName.setText(superStationName);
        textVicinity.setText(superStationAddress);
        distanceInMeters = loc1.distanceTo(loc2);
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
            SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
            Date date = format.parse(superLastUpdate);
            textLastUpdated.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        info.setID(superStationID);
        info.setStationName(superStationName);
        info.setVicinity(superStationAddress);
        info.setCountryCode(superStationCountry);
        info.setLocation(superStationLocation);
        info.setGoogleMapID(superGoogleID);
        info.setFacilities(superFacilities);
        info.setLicenseNo(superLicenseNo);
        info.setOwner(username);
        info.setPhotoURL(superStationLogo);
        info.setGasolinePrice(ownedGasolinePrice);
        info.setDieselPrice(ownedDieselPrice);
        info.setLpgPrice(ownedLPGPrice);
        info.setElectricityPrice(ownedElectricityPrice);
        info.setIsVerified(isStationVerified);
        info.setLastUpdated(superLastUpdate);
        info.setDistance((int) distanceInMeters);

        // Zoom-in camera
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(17f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.addCircle(new CircleOptions()
                .center(sydney)
                .radius(mapDefaultStationRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));

        // We are waiting for loading logos
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                addMarker();
            }
        }, 750);
        swipeContainer.setRefreshing(false);
    }

    private void addMarker() {
        Marker m;
        if (isStationVerified == 1) {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(superStationName).snippet(superStationAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
            m = googleMap.addMarker(mOptions);
            m.setTag(info);
        } else {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(superStationName).snippet(superStationAddress).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));
            m = googleMap.addMarker(mOptions);
            m.setTag(info);
        }
        m.showInfoWindow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (ActivityCompat.checkSelfPermission(getActivity(), PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                loadMap();
            } else {
                Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
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
            if (googleMap != null) {
                if (ActivityCompat.checkSelfPermission(getActivity(), PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        }

        if (mFusedLocationClient != null) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }

        if (!superStationName.equals(textName.getText().toString())) {
            loadMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
            if (googleMap != null) {
                if (ActivityCompat.checkSelfPermission(getActivity(), PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(false);
                }
            }
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
