package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.MarkerAdapter;
import com.fuelspot.adapter.StationAdapter;
import com.fuelspot.model.MarkerItem;
import com.fuelspot.model.StationItem;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.AlarmBuilder;
import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isGeofenceOpen;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.userVehicles;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class FragmentStations extends Fragment {

    // Station variables
    public static List<StationItem> fullStationList = new ArrayList<>();
    static List<StationItem> shortStationList = new ArrayList<>();
    static ArrayList<Marker> markers = new ArrayList<>();

    boolean isAllStationsListed;

    MapView mMapView;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    RequestQueue queue;
    SharedPreferences prefs;
    Circle circle;
    ImageView noStationError;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    Location locLastKnown = new Location("");
    NestedScrollView nScrollView;
    Button seeAllStations;
    View rootView;
    RelativeLayout stationLayout, sortGasolineLayout, sortDieselLayout, sortLPGLayout, sortElectricityLayout, sortDistanceLayout;
    Window window;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;

    public static FragmentStations newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Stations");

        FragmentStations fragment = new FragmentStations();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_stations, container, false);

            // Analytics
            if (getActivity() != null) {
                Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
                t.setScreenName("İstasyonlar");
                t.enableAdvertisingIdCollection(true);
                t.send(new HitBuilders.ScreenViewBuilder().build());
            }

            // Objects
            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            queue = Volley.newRequestQueue(getActivity());
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            // Activate map
            mMapView = rootView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);

            locLastKnown.setLatitude(Double.parseDouble(userlat));
            locLastKnown.setLongitude(Double.parseDouble(userlon));

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (getActivity() != null && locationResult != null) {
                        synchronized (getActivity()) {
                            super.onLocationResult(locationResult);
                            Location locCurrent = locationResult.getLastLocation();
                            if (locCurrent != null) {
                                // <= 250m accuracy is sufficient for first load
                                if (locCurrent.getAccuracy() <= mapDefaultStationRange) {
                                    userlat = String.valueOf(locCurrent.getLatitude());
                                    userlon = String.valueOf(locCurrent.getLongitude());
                                    prefs.edit().putString("lat", userlat).apply();
                                    prefs.edit().putString("lon", userlon).apply();
                                    getVariables(prefs);

                                    float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                    if (distanceInMeter >= (mapDefaultRange / 2)) {
                                        // User's position has been changed. Load the new map
                                        locLastKnown.setLatitude(Double.parseDouble(userlat));
                                        locLastKnown.setLongitude(Double.parseDouble(userlon));
                                        updateMapObject();
                                    } else {
                                        // User position changed a little. Just update distances
                                        if (fullStationList != null && fullStationList.size() > 0) {
                                            for (int i = 0; i < fullStationList.size(); i++) {
                                                String[] stationLocation = fullStationList.get(i).getLocation().split(";");
                                                double stationLat = Double.parseDouble(stationLocation[0]);
                                                double stationLon = Double.parseDouble(stationLocation[1]);

                                                Location locStation = new Location("");
                                                locStation.setLatitude(stationLat);
                                                locStation.setLongitude(stationLon);

                                                float newDistance = locCurrent.distanceTo(locStation);
                                                fullStationList.get(i).setDistance((int) newDistance);
                                            }
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            } else {
                                Snackbar.make(getActivity().findViewById(R.id.mainContainer), getActivity().getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            };

            nScrollView = rootView.findViewById(R.id.nestedScrollView);
            noStationError = rootView.findViewById(R.id.errorPicture);
            stationLayout = rootView.findViewById(R.id.stationLayout);

            sortGasolineLayout = rootView.findViewById(R.id.sortGasoline);
            sortDieselLayout = rootView.findViewById(R.id.sortDiesel);
            sortLPGLayout = rootView.findViewById(R.id.sortLPG);
            sortElectricityLayout = rootView.findViewById(R.id.sortElectric);
            sortDistanceLayout = rootView.findViewById(R.id.sortDistance);

            sortGasolineLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sortBy(0);
                }
            });
            sortDieselLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sortBy(1);
                }
            });
            sortLPGLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sortBy(2);
                }
            });
            sortElectricityLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sortBy(3);
                }
            });
            sortDistanceLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sortBy(4);
                }
            });

            mRecyclerView = rootView.findViewById(R.id.stationView);
            mAdapter = new StationAdapter(getActivity(), shortStationList, "NEARBY_STATIONS");
            mLayoutManager = new GridLayoutManager(getActivity(), 1);

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setNestedScrollingEnabled(false);

            seeAllStations = rootView.findViewById(R.id.button_seeAllStations);
            seeAllStations.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter = new StationAdapter(getActivity(), fullStationList, "NEARBY_STATIONS");
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(mAdapter);
                    seeAllStations.setVisibility(View.GONE);
                    isAllStationsListed = true;

                    markers.clear();
                    if (googleMap != null) {
                        googleMap.clear();
                        //Draw a circle with radius of mapDefaultRange
                        circle = googleMap.addCircle(new CircleOptions()
                                .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                                .radius(mapDefaultRange)
                                .fillColor(0x220000FF)
                                .strokeColor(Color.parseColor("#FF5635")));
                    }

                    for (int i = 0; i < fullStationList.size(); i++) {
                        StationItem item = fullStationList.get(i);
                        addMarker(item);
                    }

                    MarkerAdapter customInfoWindow = new MarkerAdapter(getActivity());
                    googleMap.setInfoWindowAdapter(customInfoWindow);
                    markers.get(0).showInfoWindow();
                }
            });

            checkLocationPermission();
        }
        return rootView;
    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setCompassEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    googleMap.getUiSettings().setAllGesturesEnabled(false);
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                    googleMap.setTrafficEnabled(true);
                    updateMapObject();
                }
            });
        }
    }

    private void updateMapObject() {
        fullStationList.clear();
        mAdapter.notifyDataSetChanged();

        markers.clear();
        if (googleMap != null) {
            googleMap.clear();

            //Draw a circle with radius of mapDefaultRange
            circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                    .radius(mapDefaultRange)
                    .fillColor(0x220000FF)
                    .strokeColor(Color.parseColor("#FF5635")));
        }

        seeAllStations.setVisibility(View.GONE);
        noStationError.setVisibility(View.GONE);

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        fetchStations();
    }

    private void fetchStations() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SEARCH_STATION),
                new Response.Listener<String>() {
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

                                    if (fullStationList.size() <= 5) {
                                        shortStationList.add(item);
                                    }

                                    mAdapter.notifyDataSetChanged();

                                    // Add marker
                                    addMarker(item);
                                }

                                mRecyclerView.setAdapter(mAdapter);

                                if (fullStationList.size() > 5) {
                                    seeAllStations.setVisibility(View.VISIBLE);
                                } else {
                                    seeAllStations.setVisibility(View.GONE);
                                }

                                MarkerAdapter customInfoWindow = new MarkerAdapter(getActivity());
                                googleMap.setInfoWindowAdapter(customInfoWindow);
                                markers.get(0).showInfoWindow();
                                noStationError.setVisibility(View.GONE);

                                // Create a fence
                                if (!isSuperUser && isGeofenceOpen) {
                                    if (userVehicles != null && userVehicles.length() > 0) {
                                        AlarmBuilder(getActivity());
                                    }
                                }

                                // Restart map values if changed
                                mapDefaultRange = 2500;
                                mapDefaultZoom = 13f;
                            } catch (JSONException e) {
                                noStationError.setVisibility(View.VISIBLE);
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
                        noStationError.setVisibility(View.VISIBLE);
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                params.put("location", userlat + ";" + userlon);
                params.put("radius", String.valueOf(mapDefaultRange));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    void reTry() {
        // Maybe s/he is in the countryside. Increase mapDefaultRange, decrease mapDefaultZoom
        if (mapDefaultRange == 2500) {
            mapDefaultRange = 5000;
            mapDefaultZoom = 12f;
            Toast.makeText(getActivity(), "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            updateMapObject();
        } else if (mapDefaultRange == 5000) {
            mapDefaultRange = 10000;
            mapDefaultZoom = 11f;
            Toast.makeText(getActivity(), "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            updateMapObject();
        } else if (mapDefaultRange == 10000) {
            mapDefaultRange = 25000;
            mapDefaultZoom = 10f;
            Toast.makeText(getActivity(), "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            updateMapObject();
        } else if (mapDefaultRange == 25000) {
            mapDefaultRange = 50000;
            mapDefaultZoom = 8.75f;
            Toast.makeText(getActivity(), "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            updateMapObject();
        } else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "İstasyon bulunamadı...", Snackbar.LENGTH_LONG).show();
        }
    }

    private void sortBy(int position) {
        switch (position) {
            case 0:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getGasolinePrice() == 0 && obj2.getGasolinePrice() == 0) {
                            // 0TL, 0TL
                            return 1;
                        } else if (obj1.getGasolinePrice() == 0) {
                            // 0 TL, 5 TL
                            return 1;
                        } else if (obj2.getGasolinePrice() == 0) {
                            // 5TL, 0TL
                            return -1;
                        } else {
                            // 3.85 TL, 3.97 TL
                            return Float.compare(obj1.getGasolinePrice(), obj2.getGasolinePrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(1.0f);
                sortDieselLayout.setAlpha(0.5f);
                sortLPGLayout.setAlpha(0.5f);
                sortElectricityLayout.setAlpha(0.5f);
                sortDistanceLayout.setAlpha(0.5f);
                break;
            case 1:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getDieselPrice() == 0 && obj2.getDieselPrice() == 0) {
                            // 0TL, 0TL
                            return 1;
                        } else if (obj1.getDieselPrice() == 0) {
                            // 0 TL, 5 TL
                            return 1;
                        } else if (obj2.getDieselPrice() == 0) {
                            // 5TL, 0TL
                            return -1;
                        } else {
                            // 3.85 TL, 3.97 TL
                            return Float.compare(obj1.getDieselPrice(), obj2.getDieselPrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(0.5f);
                sortDieselLayout.setAlpha(1.0f);
                sortLPGLayout.setAlpha(0.5f);
                sortElectricityLayout.setAlpha(0.5f);
                sortDistanceLayout.setAlpha(0.5f);
                break;
            case 2:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getLpgPrice() == 0 && obj2.getLpgPrice() == 0) {
                            // 0TL, 0TL
                            return 1;
                        } else if (obj1.getLpgPrice() == 0) {
                            // 0 TL, 5 TL
                            return 1;
                        } else if (obj2.getLpgPrice() == 0) {
                            // 5TL, 0TL
                            return -1;
                        } else {
                            // 3.85 TL, 3.97 TL
                            return Float.compare(obj1.getLpgPrice(), obj2.getLpgPrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(0.5f);
                sortDieselLayout.setAlpha(0.5f);
                sortLPGLayout.setAlpha(1.0f);
                sortElectricityLayout.setAlpha(0.5f);
                sortDistanceLayout.setAlpha(0.5f);
                break;
            case 3:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getElectricityPrice() == 0 && obj2.getElectricityPrice() == 0) {
                            // 0TL, 0TL
                            return 1;
                        } else if (obj1.getElectricityPrice() == 0) {
                            // 0 TL, 5 TL
                            return 1;
                        } else if (obj2.getElectricityPrice() == 0) {
                            // 5TL, 0TL
                            return -1;
                        } else {
                            // 3.85 TL, 3.97 TL
                            return Float.compare(obj1.getElectricityPrice(), obj2.getElectricityPrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(0.5f);
                sortDieselLayout.setAlpha(0.5f);
                sortLPGLayout.setAlpha(0.5f);
                sortElectricityLayout.setAlpha(1.0f);
                sortDistanceLayout.setAlpha(0.5f);
                break;
            case 4:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        return Float.compare(obj1.getDistance(), obj2.getDistance());
                    }
                });

                sortGasolineLayout.setAlpha(0.5f);
                sortDieselLayout.setAlpha(0.5f);
                sortLPGLayout.setAlpha(0.5f);
                sortElectricityLayout.setAlpha(0.5f);
                sortDistanceLayout.setAlpha(1.0f);
                break;
        }

        if (!isAllStationsListed) {
            shortStationList.clear();
            for (int i = 0; i < 5; i++) {
                shortStationList.add(fullStationList.get(i));
            }
        }

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);

        markers.clear();
        if (googleMap != null) {
            googleMap.clear();
            //Draw a circle with radius of mapDefaultRange
            circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                    .radius(mapDefaultRange)
                    .fillColor(0x220000FF)
                    .strokeColor(Color.parseColor("#FF5635")));
        }

        int untilWhere;
        if (position == 4) {
            untilWhere = fullStationList.size();
        } else {
            untilWhere = 5;
        }

        for (int i = 0; i < untilWhere; i++) {
            StationItem sItem = fullStationList.get(i);
            addMarker(sItem);
        }

        MarkerAdapter customInfoWindow = new MarkerAdapter(getActivity());
        googleMap.setInfoWindowAdapter(customInfoWindow);
        markers.get(0).showInfoWindow();
    }

    private void addMarker(final StationItem sItem) {
        // Add marker
        MarkerItem info = new MarkerItem();
        info.setID(sItem.getID());
        info.setStationName(sItem.getStationName());
        info.setPhotoURL(sItem.getPhotoURL());
        info.setGasolinePrice(sItem.getGasolinePrice());
        info.setDieselPrice(sItem.getDieselPrice());
        info.setLpgPrice(sItem.getLpgPrice());

        String[] stationKonum = sItem.getLocation().split(";");
        LatLng sydney = new LatLng(Double.parseDouble(stationKonum[0]), Double.parseDouble(stationKonum[1]));

        if (sItem.getIsVerified() == 1) {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
            Marker m = googleMap.addMarker(mOptions);
            m.setTag(info);
            markers.add(m);
        } else {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));
            Marker m = googleMap.addMarker(mOptions);
            m.setTag(info);
            markers.add(m);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (ActivityCompat.checkSelfPermission(getActivity(), PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
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
                            updateMapObject();
                        }
                    });
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

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        if (queue != null) {
            queue.cancelAll(getActivity());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
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