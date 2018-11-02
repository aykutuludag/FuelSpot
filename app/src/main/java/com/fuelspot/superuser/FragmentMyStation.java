package com.fuelspot.superuser;


import android.Manifest;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.superuser.AdminMainActivity.isMobilePaymentAvailable;
import static com.fuelspot.superuser.AdminMainActivity.isStationVerified;
import static com.fuelspot.superuser.AdminMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.AdminMainActivity.superGoogleID;
import static com.fuelspot.superuser.AdminMainActivity.superLicenseNo;
import static com.fuelspot.superuser.AdminMainActivity.superStationAddress;
import static com.fuelspot.superuser.AdminMainActivity.superStationCountry;
import static com.fuelspot.superuser.AdminMainActivity.superStationID;
import static com.fuelspot.superuser.AdminMainActivity.superStationLocation;
import static com.fuelspot.superuser.AdminMainActivity.superStationLogo;
import static com.fuelspot.superuser.AdminMainActivity.superStationName;

public class FragmentMyStation extends Fragment {

    SharedPreferences prefs;
    MapView mMapView;
    RequestQueue queue;
    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;
    ImageView stationIcon;
    Button editStation, openPurchases, openComments, openCampaings, openPosts;
    FusedLocationProviderClient mFusedLocationClient;
    Circle circle;
    private GoogleMap googleMap;
    Location locLastKnown = new Location("");
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;

    public static FragmentMyStation newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "OwnedStation");

        FragmentMyStation fragment = new FragmentMyStation();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mystation, container, false);

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
        locLastKnown.setLatitude(Double.parseDouble(userlat));
        locLastKnown.setLongitude(Double.parseDouble(userlon));

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
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
        textLastUpdated = rootView.findViewById(R.id.lastUpdateTime);
        stationIcon = rootView.findViewById(R.id.stationLogo);

        //Buttons
        editStation = rootView.findViewById(R.id.buttonEditStation);
        editStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStationVerified == 1) {
                    Intent i = new Intent(getActivity(), SuperEditPrices.class);
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
                if (isStationVerified == 1) {
                    Intent i = new Intent(getActivity(), SuperCampaings.class);
                    startActivity(i);
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        openPosts = rootView.findViewById(R.id.buttonPromo);
        openPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStationVerified == 1) {
                    Toast.makeText(getActivity(), "Coming soon...", Toast.LENGTH_LONG).show();
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir.", Snackbar.LENGTH_LONG).show();
                }
            }
        });


        checkLocationPermission();

        return rootView;
    }

    void checkLocationPermission() {
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
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        //Scroll iptal
                    }
                });
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        //Scroll enable
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
                        System.out.println("İSTASYON DETAYI:" + response);
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                superStationID = obj.getInt("id");
                                prefs.edit().putInt("SuperStationID", superStationID).apply();

                                superStationName = obj.getString("name");
                                prefs.edit().putString("SuperStationName", superStationName).apply();

                                superStationCountry = obj.getString("country");
                                prefs.edit().putString("SuperStationCountry", superStationCountry).apply();

                                superStationLocation = obj.getString("location");
                                prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

                                superGoogleID = obj.getString("googleID");
                                prefs.edit().putString("SuperGoogleID", superGoogleID).apply();

                                superLicenseNo = obj.getString("licenseNo");
                                prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();

                                superStationLogo = obj.getString("photoURL");
                                prefs.edit().putString("SuperStationLogo", superStationLogo).apply();

                                ownedGasolinePrice = (float) obj.getDouble("gasolinePrice");
                                prefs.edit().putFloat("superGasolinePrice", ownedGasolinePrice).apply();

                                ownedDieselPrice = (float) obj.getDouble("dieselPrice");
                                prefs.edit().putFloat("superDieselPrice", ownedDieselPrice).apply();

                                ownedLPGPrice = (float) obj.getDouble("lpgPrice");
                                prefs.edit().putFloat("superLPGPrice", ownedLPGPrice).apply();

                                ownedElectricityPrice = (float) obj.getDouble("electricityPrice");
                                prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();

                                isStationVerified = obj.getInt("isVerified");
                                prefs.edit().putInt("isStationVerified", isStationVerified).apply();

                                isMobilePaymentAvailable = obj.getInt("isMobilePaymentAvailable");
                                prefs.edit().putInt("isMobilePaymentAvailable", isMobilePaymentAvailable).apply();

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

                                ownedGasolinePrice = (float) obj.getDouble("gasolinePrice");
                                prefs.edit().putFloat("superGasolinePrice", ownedGasolinePrice).apply();
                                textGasoline.setText(ownedGasolinePrice + "TL");

                                ownedDieselPrice = (float) obj.getDouble("dieselPrice");
                                prefs.edit().putFloat("superDieselPrice", ownedDieselPrice).apply();
                                textDiesel.setText(ownedDieselPrice + "TL");

                                ownedLPGPrice = (float) obj.getDouble("lpgPrice");
                                prefs.edit().putFloat("superLPGPrice", ownedLPGPrice).apply();
                                textLPG.setText(ownedLPGPrice + "TL");

                                ownedElectricityPrice = (float) obj.getDouble("electricityPrice");
                                prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();
                                textElectricity.setText(ownedElectricityPrice + "TL");

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
                                String[] locationHolder = superStationLocation.split(";");
                                LatLng sydney = new LatLng(Double.parseDouble(locationHolder[0]), Double.parseDouble(locationHolder[1]));
                                googleMap.addMarker(new MarkerOptions().position(sydney).title(superStationName).snippet(superStationAddress));

                                //Zoom-in camera
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(17f).build();
                                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition
                                        (cameraPosition));

                                circle = googleMap.addCircle(new CircleOptions()
                                        .center(sydney)
                                        .radius(mapDefaultStationRange)
                                        .strokeColor(Color.RED));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
                params.put("stationID", String.valueOf(superStationID));

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
