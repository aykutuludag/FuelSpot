package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.MarkerAdapter;
import com.fuelspot.adapter.StationAdapter;
import com.fuelspot.model.StationItem;
import com.fuelspot.receiver.AlarmReceiver;
import com.fuelspot.superuser.SuperMainActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.fuelspot.MainActivity.AlarmBuilder;
import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.adCount;
import static com.fuelspot.MainActivity.admobInterstitial;
import static com.fuelspot.MainActivity.fullStationList;
import static com.fuelspot.MainActivity.isGeofenceOpen;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class FragmentStations extends Fragment {

    int whichOrder;
    boolean isMapUpdating;
    NestedScrollView scrollView;
    boolean filterByWC, filterByMarket, filterByCarWash, filterByTireStore, filterByMechanic, filterByRestaurant, filterByParkSpot, filterByATM, filterByMotel;
    RelativeLayout stationLayout;
    // Station variables
    private List<StationItem> shortStationList = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();
    private boolean isAllStationsListed;
    private MapView mMapView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RequestQueue queue;
    private SharedPreferences prefs;
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
            t.setScreenName("Stations");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            // Clear objects
            shortStationList.clear();
            fullStationList.clear();
            markers.clear();

            // Objects
            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            queue = Volley.newRequestQueue(getActivity());
            scrollView = rootView.findViewById(R.id.nestedScrollView);

            // Activate map
            mMapView = rootView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            locLastKnown.setLatitude(Double.parseDouble(userlat));
            locLastKnown.setLongitude(Double.parseDouble(userlon));

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(7500);
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
            };

            stationLayout = rootView.findViewById(R.id.stationLayout);
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
            loadMap();
        }
    }

    void loadMap() {
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

                // Dokundu
                googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            scrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                });
                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });

                // Dokunma bitti
                googleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
                    @Override
                    public void onCameraMoveCanceled() {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                    }
                });
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                    }
                });

                // For zooming automatically to the location of the marker
                LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                MarkerAdapter customInfoWindow = new MarkerAdapter(getActivity());
                googleMap.setInfoWindowAdapter(customInfoWindow);
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        StationItem infoWindowData = (StationItem) marker.getTag();
                        openStation(infoWindowData);
                    }
                });
            }
        });
    }

    void updateMap() {
        if (googleMap != null) {
            isMapUpdating = true;

            // Clear filters
            filterByWC = false;
            filterByMarket = false;
            filterByCarWash = false;
            filterByTireStore = false;
            filterByMechanic = false;
            filterByRestaurant = false;
            filterByParkSpot = false;
            filterByATM = false;
            filterByMotel = false;

            isAllStationsListed = false;
            seeAllStations.setText(getString(R.string.see_all));
            seeAllStations.setVisibility(View.GONE);
            stationLayout.setVisibility(View.GONE);

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
                Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }
        } else {
            isMapUpdating = false;
            Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchStations() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_SEARCH_STATIONS) + "?location=" + userlat + ";" + userlon + "&radius=" + mapDefaultRange,
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

                                Toast.makeText(getActivity(), getString(R.string.station_found_pretext) + " " + fullStationList.size() + " " + getString(R.string.station_found_aftertext), Toast.LENGTH_LONG).show();
                                // Stations fetched. Visible recyclerview
                                stationLayout.setVisibility(View.VISIBLE);

                                if (fullStationList.size() > 5) {
                                    seeAllStations.setVisibility(View.VISIBLE);
                                } else {
                                    seeAllStations.setVisibility(View.GONE);
                                }

                                // Create a fence
                                if (!isSuperUser && isGeofenceOpen) {
                                    if (userAutomobileList != null && userAutomobileList.size() > 0) {
                                        AlarmBuilder(getActivity());
                                    }
                                } else {
                                    cancelGeofenceAlarm();
                                }

                                // Sort by distnce
                                whichOrder = 4;
                                sortBy(whichOrder);

                                if (isSuperUser) {
                                    ((SuperMainActivity) getActivity()).bottomNavigation.setNotification(fullStationList.size(), 0);
                                } else {
                                    ((MainActivity) getActivity()).bottomNavigation.setNotification(fullStationList.size(), 0);
                                }
                            } catch (JSONException e) {
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
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    private void sortBy(int position) {
        // User is at filter mode. just do nothing for now.
        if (filterByWC || filterByMarket || filterByTireStore || filterByMechanic || filterByRestaurant || filterByParkSpot || filterByATM || filterByMotel) {
            return;
        }

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
            default:
                whichOrder = 4;
                sortBy(whichOrder);
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

        // We are waiting for loading logos
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                addMarkers();
            }
        }, 750);
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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (markers != null && markers.size() > 0) {
                    markers.get(0).showInfoWindow();
                }
            }
        }, 250);
    }

    private void addMarker(final StationItem sItem) {
        // Add marker
        String[] stationKonum = sItem.getLocation().split(";");
        LatLng sydney = new LatLng(Double.parseDouble(stationKonum[0]), Double.parseDouble(stationKonum[1]));

        MarkerOptions mOptions;
        if (sItem.getIsVerified() == 1) {
            mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
        } else {
            mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));

        }
        Marker m = googleMap.addMarker(mOptions);
        m.setTag(sItem);
        markers.add(m);
    }

    public void filterPopup() {
        CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6, checkBox7, checkBox8, checkBox9;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_filter, null);
        final PopupWindow mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        checkBox1 = customView.findViewById(R.id.checkBox2);
        checkBox1.setChecked(filterByWC);
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByWC = isChecked;
            }
        });

        checkBox2 = customView.findViewById(R.id.checkBox3);
        checkBox2.setChecked(filterByMarket);
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByMarket = isChecked;
            }
        });

        checkBox3 = customView.findViewById(R.id.checkBox4);
        checkBox3.setChecked(filterByCarWash);
        checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByCarWash = isChecked;
            }
        });

        checkBox4 = customView.findViewById(R.id.checkBox5);
        checkBox4.setChecked(filterByTireStore);
        checkBox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByTireStore = isChecked;
            }
        });

        checkBox5 = customView.findViewById(R.id.checkBox6);
        checkBox5.setChecked(filterByMechanic);
        checkBox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByMechanic = isChecked;
            }
        });

        checkBox6 = customView.findViewById(R.id.checkBox7);
        checkBox6.setChecked(filterByRestaurant);
        checkBox6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByRestaurant = isChecked;
            }
        });

        checkBox7 = customView.findViewById(R.id.checkBox8);
        checkBox7.setChecked(filterByParkSpot);
        checkBox7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByParkSpot = isChecked;
            }
        });

        checkBox8 = customView.findViewById(R.id.checkBox9);
        checkBox8.setChecked(filterByATM);
        checkBox8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByATM = isChecked;
            }
        });

        checkBox9 = customView.findViewById(R.id.checkBox10);
        checkBox9.setChecked(filterByMotel);
        checkBox9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByMotel = isChecked;
            }
        });

        Button filterButton = customView.findViewById(R.id.button8);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterStations();
                mPopupWindow.dismiss();
            }
        });

        ImageView closeButton = customView.findViewById(R.id.imageViewClose);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        mPopupWindow.showAtLocation(customView, Gravity.CENTER, 0, 0);
    }

    void filterStations() {
        // If there is no filter, just order by distance
        if (!filterByWC && !filterByMarket && !filterByTireStore && !filterByMechanic && !filterByRestaurant && !filterByParkSpot && !filterByATM && !filterByMotel) {
            seeAllStations.setVisibility(View.VISIBLE);
            whichOrder = 4;
            sortBy(whichOrder);
            return;
        }

        // Clear variables
        googleMap.clear();
        markers.clear();
        shortStationList.clear();

        googleMap.addCircle(new CircleOptions().center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                .radius(mapDefaultRange)
                .fillColor(0x220000FF).strokeColor(Color.parseColor("#FF5635")));

        for (int i = 0; i < fullStationList.size(); i++) {
            try {
                JSONArray facilitiesRes = new JSONArray(fullStationList.get(i).getFacilities());
                JSONObject facilitiesObj = facilitiesRes.getJSONObject(0);

                if (filterByWC && facilitiesObj.getInt("WC") == 0) {
                    continue;
                } else if (filterByMarket && facilitiesObj.getInt("Market") == 0) {
                    continue;
                } else if (filterByCarWash && facilitiesObj.getInt("CarWash") == 0) {
                    continue;
                } else if (filterByTireStore && facilitiesObj.getInt("TireRepair") == 0) {
                    continue;
                } else if (filterByMechanic && facilitiesObj.getInt("Mechanic") == 0) {
                    continue;
                } else if (filterByRestaurant && facilitiesObj.getInt("Restaurant") == 0) {
                    continue;
                } else if (filterByParkSpot && facilitiesObj.getInt("ParkSpot") == 0) {
                    continue;
                } else if (filterByATM && facilitiesObj.getInt("ATM") == 0) {
                    continue;
                } else if (filterByMotel && facilitiesObj.getInt("Motel") == 0) {
                    continue;
                }
                shortStationList.add(fullStationList.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(getActivity(), getString(R.string.station_found_pretext) + " " + shortStationList.size() + " " + getString(R.string.station_found_aftertext), Toast.LENGTH_LONG).show();
        mAdapter = new StationAdapter(getActivity(), shortStationList, "NEARBY_STATIONS");
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);
        seeAllStations.setVisibility(View.GONE);

        for (int i = 0; i < shortStationList.size(); i++) {
            addMarker(shortStationList.get(i));
            markers.get(0).showInfoWindow();
        }
    }

    private void openStation(StationItem feedItemList) {
        Intent intent = new Intent(getActivity(), StationDetails.class);
        intent.putExtra("STATION_ID", feedItemList.getID());
        intent.putExtra("STATION_NAME", feedItemList.getStationName());
        intent.putExtra("STATION_VICINITY", feedItemList.getVicinity());
        intent.putExtra("STATION_LOCATION", feedItemList.getLocation());
        intent.putExtra("STATION_DISTANCE", feedItemList.getDistance());
        intent.putExtra("STATION_LASTUPDATED", feedItemList.getLastUpdated());
        intent.putExtra("STATION_GASOLINE", feedItemList.getGasolinePrice());
        intent.putExtra("STATION_DIESEL", feedItemList.getDieselPrice());
        intent.putExtra("STATION_LPG", feedItemList.getLpgPrice());
        intent.putExtra("STATION_ELECTRIC", feedItemList.getElectricityPrice());
        intent.putExtra("STATION_ICON", feedItemList.getPhotoURL());
        intent.putExtra("IS_VERIFIED", feedItemList.getIsVerified());
        intent.putExtra("STATION_FACILITIES", feedItemList.getFacilities());
        showAds(intent);
    }

    private void showAds(Intent intent) {
        if (admobInterstitial != null && admobInterstitial.isLoaded()) {
            //Facebook ads doesnt loaded he will see AdMob
            startActivity(intent);
            admobInterstitial.show();
            adCount++;
            admobInterstitial = null;
        } else {
            // Ads doesn't loaded.
            startActivity(intent);
        }

        if (adCount == 2) {
            Toast.makeText(getActivity(), getString(R.string.last_ads_info), Toast.LENGTH_SHORT).show();
            adCount++;
        }
    }

    private void cancelGeofenceAlarm() {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }
}