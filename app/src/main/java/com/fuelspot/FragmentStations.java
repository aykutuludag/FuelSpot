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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fullStationList;
import static com.fuelspot.MainActivity.isGeofenceOpen;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class FragmentStations extends Fragment {

    // Station variables
    private List<StationItem> shortStationList = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();
    private boolean isAllStationsListed;
    private MapView mMapView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RequestQueue queue;
    private SharedPreferences prefs;
    private ImageView noStationError;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location locLastKnown = new Location("");
    private Button seeAllStations;
    private View rootView;
    private RelativeLayout sortGasolineLayout;
    private RelativeLayout sortDieselLayout;
    private RelativeLayout sortLPGLayout;
    private RelativeLayout sortElectricityLayout;
    private RelativeLayout sortDistanceLayout;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;
    int whichOrder;
    boolean isMapUpdating;

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

            // Keep screen on
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("İstasyonlar");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            // Clear objects
            shortStationList.clear();
            fullStationList.clear();
            markers.clear();

            // Objects
            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            queue = Volley.newRequestQueue(getActivity());

            // Activate map
            mMapView = rootView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            locLastKnown.setLatitude(Double.parseDouble(userlat));
            locLastKnown.setLongitude(Double.parseDouble(userlon));

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (getActivity() != null && locationResult != null) {
                        synchronized (getActivity()) {
                            super.onLocationResult(locationResult);
                            Location locCurrent = locationResult.getLastLocation();
                            if (locCurrent != null) {
                                if (locCurrent.getAccuracy() <= mapDefaultStationRange * 2) {
                                    userlat = String.valueOf(locCurrent.getLatitude());
                                    userlon = String.valueOf(locCurrent.getLongitude());
                                    prefs.edit().putString("lat", userlat).apply();
                                    prefs.edit().putString("lon", userlon).apply();

                                    float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                    if (fullStationList.size() == 0 || (distanceInMeter >= (mapDefaultRange / 2))) {
                                        // User's position has been changed. Load the new map
                                        locLastKnown.setLatitude(Double.parseDouble(userlat));
                                        locLastKnown.setLongitude(Double.parseDouble(userlon));
                                        if (!isMapUpdating) {
                                            updateMap();
                                        }
                                    } else {
                                        // User position changed a little. Just update distances
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
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.location_fetching), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            };

            noStationError = rootView.findViewById(R.id.errorPicture);
            sortGasolineLayout = rootView.findViewById(R.id.sortGasoline);
            sortDieselLayout = rootView.findViewById(R.id.sortDiesel);
            sortLPGLayout = rootView.findViewById(R.id.sortLPG);
            sortElectricityLayout = rootView.findViewById(R.id.sortElectric);
            sortDistanceLayout = rootView.findViewById(R.id.sortDistance);
            sortGasolineLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    whichOrder = 0;
                    sortBy(whichOrder);
                }
            });
            sortDieselLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    whichOrder = 1;
                    sortBy(whichOrder);
                }
            });
            sortLPGLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    whichOrder = 2;
                    sortBy(whichOrder);
                }
            });
            sortElectricityLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    whichOrder = 3;
                    sortBy(whichOrder);
                }
            });
            sortDistanceLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    whichOrder = 4;
                    sortBy(whichOrder);
                }
            });

            mRecyclerView = rootView.findViewById(R.id.stationView);
            GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setNestedScrollingEnabled(false);
            mRecyclerView.removeAllViews();

            seeAllStations = rootView.findViewById(R.id.button_seeAllStations);
            seeAllStations.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isAllStationsListed = !isAllStationsListed;
                    sortBy(whichOrder);
                }
            });

            // Start the load map
            checkLocationPermission();
        }

        return rootView;
    }

    private void checkLocationPermission() {
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
                    googleMap.setTrafficEnabled(true);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);

                    MarkerAdapter customInfoWindow = new MarkerAdapter(getActivity());
                    googleMap.setInfoWindowAdapter(customInfoWindow);
                }
            });
        }
    }

    void updateMap() {
        if (googleMap != null) {
            isMapUpdating = true;

            isAllStationsListed = false;
            seeAllStations.setText(getString(R.string.see_all));
            seeAllStations.setVisibility(View.GONE);
            noStationError.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);

            // Clear objects
            shortStationList.clear();
            fullStationList.clear();
            markers.clear();
            googleMap.clear();

            //Draw a circle with radius of mapDefaultRange
            googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                    .radius(mapDefaultRange)
                    .fillColor(0x220000FF)
                    .strokeColor(Color.parseColor("#FF5635")));

            // For zooming automatically to the location of the marker
            LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (isNetworkConnected(getActivity())) {
                searchStations();
            } else {
                isMapUpdating = false;
                Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
            }
        } else {
            isMapUpdating = false;
            Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        }
    }

    private void searchStations() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SEARCH_STATIONS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isMapUpdating = false;
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
                                    fullStationList.add(item);
                                }

                                if (fullStationList.size() > 0) {
                                    Toast.makeText(getActivity(), getString(R.string.station_found_pretext) + " " + fullStationList.size() + " " + getString(R.string.station_found_aftertext), Toast.LENGTH_LONG).show();
                                    // Stations fetched. Visible recyclerview
                                    mRecyclerView.setVisibility(View.VISIBLE);

                                    if (fullStationList.size() > 5) {
                                        seeAllStations.setVisibility(View.VISIBLE);
                                    } else {
                                        seeAllStations.setVisibility(View.GONE);
                                    }

                                    // Sort by primary fuel
                                    if (!isSuperUser) {
                                        whichOrder = fuelPri;
                                        sortBy(whichOrder);
                                    } else {
                                        whichOrder = 4;
                                        sortBy(whichOrder);
                                    }

                                    // Create a fence
                                    if (!isSuperUser && isGeofenceOpen) {
                                        if (userAutomobileList != null && userAutomobileList.size() > 0) {
                                            AlarmBuilder(getActivity());
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                noStationError.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (mapDefaultRange == 2500) {
                                mapDefaultRange = 5000;
                                mapDefaultZoom = 12f;
                                Toast.makeText(getActivity(), getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                updateMap();
                            } else if (mapDefaultRange == 5000) {
                                mapDefaultRange = 10000;
                                mapDefaultZoom = 11.25f;
                                Toast.makeText(getActivity(), getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                updateMap();
                            } else if (mapDefaultRange == 10000) {
                                mapDefaultRange = 25000;
                                mapDefaultZoom = 10f;
                                Toast.makeText(getActivity(), getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                updateMap();
                            } else if (mapDefaultRange == 25000) {
                                mapDefaultRange = 50000;
                                mapDefaultZoom = 8.75f;
                                Toast.makeText(getActivity(), getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                updateMap();
                            } else {
                                // no station within 50km
                                Toast.makeText(getActivity(), getString(R.string.no_station), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        isMapUpdating = false;
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

    private void sortBy(int position) {
        switch (position) {
            case 0:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getGasolinePrice() == 0 && obj2.getGasolinePrice() == 0) {
                            return 0;
                        } else if (obj1.getGasolinePrice() == 0) {
                            return 1;
                        } else if (obj2.getGasolinePrice() == 0) {
                            return -1;
                        } else {
                            return Float.compare(obj1.getGasolinePrice(), obj2.getGasolinePrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(1.0f);
                sortDieselLayout.setAlpha(0.33f);
                sortLPGLayout.setAlpha(0.33f);
                sortElectricityLayout.setAlpha(0.33f);
                sortDistanceLayout.setAlpha(0.33f);
                break;
            case 1:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getDieselPrice() == 0 && obj2.getDieselPrice() == 0) {
                            return 0;
                        } else if (obj1.getDieselPrice() == 0) {
                            return 1;
                        } else if (obj2.getDieselPrice() == 0) {
                            return -1;
                        } else {
                            return Float.compare(obj1.getDieselPrice(), obj2.getDieselPrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(0.33f);
                sortDieselLayout.setAlpha(1.0f);
                sortLPGLayout.setAlpha(0.33f);
                sortElectricityLayout.setAlpha(0.33f);
                sortDistanceLayout.setAlpha(0.33f);
                break;
            case 2:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getLpgPrice() == 0 && obj2.getLpgPrice() == 0) {
                            return 0;
                        } else if (obj1.getLpgPrice() == 0) {
                            return 1;
                        } else if (obj2.getLpgPrice() == 0) {
                            return -1;
                        } else {
                            return Float.compare(obj1.getLpgPrice(), obj2.getLpgPrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(0.33f);
                sortDieselLayout.setAlpha(0.33f);
                sortLPGLayout.setAlpha(1.0f);
                sortElectricityLayout.setAlpha(0.33f);
                sortDistanceLayout.setAlpha(0.33f);
                break;
            case 3:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getElectricityPrice() == 0 && obj2.getElectricityPrice() == 0) {
                            return 0;
                        } else if (obj1.getElectricityPrice() == 0) {
                            return 1;
                        } else if (obj2.getElectricityPrice() == 0) {
                            return -1;
                        } else {
                            return Float.compare(obj1.getElectricityPrice(), obj2.getElectricityPrice());
                        }
                    }
                });

                sortGasolineLayout.setAlpha(0.33f);
                sortDieselLayout.setAlpha(0.33f);
                sortLPGLayout.setAlpha(0.33f);
                sortElectricityLayout.setAlpha(1.0f);
                sortDistanceLayout.setAlpha(0.33f);
                break;
            case 4:
                Collections.sort(fullStationList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        return Float.compare(obj1.getDistance(), obj2.getDistance());
                    }
                });

                sortGasolineLayout.setAlpha(0.33f);
                sortDieselLayout.setAlpha(0.33f);
                sortLPGLayout.setAlpha(0.33f);
                sortElectricityLayout.setAlpha(0.33f);
                sortDistanceLayout.setAlpha(1.0f);
                break;
        }

        // Clear variables
        googleMap.clear();
        markers.clear();
        shortStationList.clear();

        googleMap.addCircle(new CircleOptions().center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                .radius(mapDefaultRange)
                .fillColor(0x220000FF).strokeColor(Color.parseColor("#FF5635")));

        if (!isAllStationsListed) {
            int untilWhere;
            if (fullStationList.size() < 5) {
                untilWhere = fullStationList.size();
            } else {
                untilWhere = 5;
            }

            for (int i = 0; i < untilWhere; i++) {
                shortStationList.add(fullStationList.get(i));
            }
            mAdapter = new StationAdapter(getActivity(), shortStationList, "NEARBY_STATIONS");
            seeAllStations.setText(getString(R.string.see_all));
        } else {
            mAdapter = new StationAdapter(getActivity(), fullStationList, "NEARBY_STATIONS");
            seeAllStations.setText(getString(R.string.show_less));
        }

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);
        addMarkers();
    }

    private void addMarkers() {
        if (isAllStationsListed) {
            for (int i = 0; i < fullStationList.size(); i++) {
                addMarker(fullStationList.get(i));
            }
        } else {
            int untilWhere;
            if (fullStationList.size() < 5) {
                untilWhere = fullStationList.size();
            } else {
                untilWhere = 5;
            }

            for (int i = 0; i < untilWhere; i++) {
                addMarker(fullStationList.get(i));
            }
        }
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

        MarkerOptions mOptions;
        if (sItem.getIsVerified() == 1) {
            mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
        } else {
            mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));

        }
        Marker m = googleMap.addMarker(mOptions);
        m.setTag(info);
        markers.add(m);
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
                            googleMap.getUiSettings().setAllGesturesEnabled(false);
                            googleMap.getUiSettings().setMapToolbarEnabled(false);
                            googleMap.setTrafficEnabled(true);
                            googleMap.getUiSettings().setZoomControlsEnabled(true);
                        }
                    });
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }
}