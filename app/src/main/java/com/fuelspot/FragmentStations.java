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
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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
import static com.fuelspot.FragmentFilterDistributors.filterStation1;
import static com.fuelspot.FragmentFilterDistributors.filterStation10;
import static com.fuelspot.FragmentFilterDistributors.filterStation11;
import static com.fuelspot.FragmentFilterDistributors.filterStation12;
import static com.fuelspot.FragmentFilterDistributors.filterStation2;
import static com.fuelspot.FragmentFilterDistributors.filterStation3;
import static com.fuelspot.FragmentFilterDistributors.filterStation4;
import static com.fuelspot.FragmentFilterDistributors.filterStation5;
import static com.fuelspot.FragmentFilterDistributors.filterStation6;
import static com.fuelspot.FragmentFilterDistributors.filterStation7;
import static com.fuelspot.FragmentFilterDistributors.filterStation8;
import static com.fuelspot.FragmentFilterDistributors.filterStation9;
import static com.fuelspot.FragmentFilterDistributors.markalar;
import static com.fuelspot.FragmentFilterFacilities.filterByATM;
import static com.fuelspot.FragmentFilterFacilities.filterByCarWash;
import static com.fuelspot.FragmentFilterFacilities.filterByCoffeeShop;
import static com.fuelspot.FragmentFilterFacilities.filterByMarket;
import static com.fuelspot.FragmentFilterFacilities.filterByMechanic;
import static com.fuelspot.FragmentFilterFacilities.filterByMosque;
import static com.fuelspot.FragmentFilterFacilities.filterByMotel;
import static com.fuelspot.FragmentFilterFacilities.filterByParkSpot;
import static com.fuelspot.FragmentFilterFacilities.filterByRestaurant;
import static com.fuelspot.FragmentFilterFacilities.filterByTireStore;
import static com.fuelspot.FragmentFilterFacilities.filterByWC;
import static com.fuelspot.MainActivity.AlarmBuilder;
import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fullStationList;
import static com.fuelspot.MainActivity.isLocationEnabled;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class FragmentStations extends Fragment {

    public View rootView;
    private int whichOrder;
    private boolean isMapUpdating;
    private NestedScrollView scrollView;
    // Station variables
    private List<StationItem> shortStationList = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();
    private boolean isAllStationsListed = true;
    private MapView mMapView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RequestQueue queue;
    private SharedPreferences prefs;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location locLastKnown = new Location("");
    private Button seeAllStations;
    private RelativeLayout stationLayout;
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
            setHasOptionsMenu(true);

            // Keep screen on
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Clear objects
            shortStationList.clear();
            fullStationList.clear();
            markers.clear();

            // Objects
            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            queue = Volley.newRequestQueue(getActivity());
            scrollView = rootView.findViewById(R.id.nestedScrollView);

            mRecyclerView = rootView.findViewById(R.id.stationView);
            GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setNestedScrollingEnabled(false);
            mRecyclerView.removeAllViews();

            // Activate map
            mMapView = rootView.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            locLastKnown.setLatitude(Double.parseDouble(userlat));
            locLastKnown.setLongitude(Double.parseDouble(userlon));

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (getActivity() != null) {
                        synchronized (getActivity()) {
                            super.onLocationResult(locationResult);
                            Location locCurrent = locationResult.getLastLocation();
                            if (locCurrent != null) {
                                if (locCurrent.getAccuracy() <= mapDefaultStationRange * 10) {
                                    userlat = String.valueOf(locCurrent.getLatitude());
                                    userlon = String.valueOf(locCurrent.getLongitude());
                                    prefs.edit().putString("lat", userlat).apply();
                                    prefs.edit().putString("lon", userlon).apply();

                                    float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                    if (fullStationList.size() == 0 || (distanceInMeter >= (mapDefaultRange / 2f))) {
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

                                        if (mAdapter != null) {
                                            mAdapter.notifyDataSetChanged();
                                        }
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

        if (!isLocationEnabled(getActivity())) {
            Toast.makeText(getActivity(), getString(R.string.location_services_off), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadMap() {
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
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);
                googleMap.setTrafficEnabled(true);

                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });

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

    private void updateMap() {
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
            filterByCoffeeShop = false;
            filterByMosque = false;

            filterStation1 = false;
            filterStation2 = false;
            filterStation3 = false;
            filterStation4 = false;
            filterStation5 = false;
            filterStation6 = false;
            filterStation7 = false;
            filterStation8 = false;
            filterStation9 = false;
            filterStation10 = false;
            filterStation11 = false;
            filterStation12 = false;

            isAllStationsListed = true;
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
                                    item.setFacilities(obj.getString("facilities"));
                                    item.setLicenseNo(obj.getString("licenseNo"));
                                    item.setOwner(obj.getString("owner"));
                                    item.setPhotoURL(obj.getString("logoURL"));
                                    item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                    item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                    item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                    item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                    item.setOtherFuels(obj.getString("otherFuels"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setLastUpdated(obj.getString("lastUpdated"));
                                    item.setDistance((int) obj.getDouble("distance"));
                                    fullStationList.add(item);
                                }

                                // Stations fetched. Visible recyclerview
                                stationLayout.setVisibility(View.VISIBLE);

                                if (fullStationList.size() > 5) {
                                    seeAllStations.setVisibility(View.VISIBLE);
                                } else {
                                    seeAllStations.setVisibility(View.GONE);
                                }

                                if (isSuperUser) {
                                    cancelGeofenceAlarm();

                                    // Sort by distance
                                    whichOrder = 4;

                                    ((SuperMainActivity) getActivity()).bottomNavigation.setNotification(fullStationList.size(), 2);
                                } else {
                                    if (fuelPri == -1) {
                                        // Sort by distance
                                        whichOrder = 4;
                                    } else {
                                        AlarmBuilder(getActivity());

                                        // Sort by fuelType
                                        whichOrder = fuelPri;
                                    }

                                    ((MainActivity) getActivity()).bottomNavigation.setNotification(fullStationList.size(), 0);
                                }

                                sortBy(whichOrder);

                                if (fullStationList.size() == 33) {
                                    Toast.makeText(getActivity(), getString(R.string.limited_33), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.station_found_pretext) + " " + fullStationList.size() + " " + getString(R.string.station_found_aftertext), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (mapDefaultRange == 3000) {
                                mapDefaultRange = 6000;
                                mapDefaultZoom = 12f;
                                Toast.makeText(getActivity(), getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                updateMap();
                            } else if (mapDefaultRange == 6000) {
                                mapDefaultRange = 10000;
                                mapDefaultZoom = 11.5f;
                                Toast.makeText(getActivity(), getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                updateMap();
                            } else if (mapDefaultRange == 10000) {
                                mapDefaultRange = 25000;
                                mapDefaultZoom = 10f;
                                Toast.makeText(getActivity(), getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                updateMap();
                            } else if (mapDefaultRange == 25000) {
                                mapDefaultRange = 50000;
                                mapDefaultZoom = 9f;
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
        if (filterByWC || filterByMarket || filterByTireStore || filterByMechanic || filterByRestaurant || filterByParkSpot || filterByATM || filterByMotel || filterByCoffeeShop || filterByMosque) {
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

    void filterStationsByFacilities() {
        // If there is no filter, just order by distance
        if (!filterByWC && !filterByMarket && !filterByTireStore && !filterByMechanic && !filterByRestaurant && !filterByParkSpot && !filterByATM && !filterByMotel && !filterByCoffeeShop && !filterByMosque) {
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
                } else if (filterByCoffeeShop && facilitiesObj.getInt("CoffeeShop") == 0) {
                    continue;
                } else if (filterByMosque && facilitiesObj.getInt("Mosque") == 0) {
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

    void filterStationsByDistributors() {
        // If there is no filter, just order by distance
        if (!filterStation1 && !filterStation2 && !filterStation3 && !filterStation4 && !filterStation5 && !filterStation6 && !filterStation7 && !filterStation8 && !filterStation9 && !filterStation10
                && !filterStation11 && !filterStation12) {
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

        List<String> dummySelectedBrand = new ArrayList<>();

        if (filterStation1) {
            dummySelectedBrand.add(markalar.get(0));
        }

        if (filterStation2) {
            dummySelectedBrand.add(markalar.get(1));
        }

        if (filterStation3) {
            dummySelectedBrand.add(markalar.get(2));
        }

        if (filterStation4) {
            dummySelectedBrand.add(markalar.get(3));
        }

        if (filterStation5) {
            dummySelectedBrand.add(markalar.get(4));
        }

        if (filterStation6) {
            dummySelectedBrand.add(markalar.get(5));
        }

        if (filterStation7) {
            dummySelectedBrand.add(markalar.get(6));
        }

        if (filterStation8) {
            dummySelectedBrand.add(markalar.get(7));
        }

        if (filterStation9) {
            dummySelectedBrand.add(markalar.get(8));
        }

        if (filterStation10) {
            dummySelectedBrand.add(markalar.get(9));
        }

        if (filterStation11) {
            dummySelectedBrand.add(markalar.get(10));
        }

        if (filterStation12) {
            dummySelectedBrand.add(markalar.get(11));
        }

        for (int i = 0; i < fullStationList.size(); i++) {
            if (dummySelectedBrand.contains(fullStationList.get(i).getStationName())) {
                shortStationList.add(fullStationList.get(i));
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
        intent.putExtra("STATION_OTHER_FUELS", feedItemList.getOtherFuels());
        intent.putExtra("STATION_ICON", feedItemList.getPhotoURL());
        intent.putExtra("IS_VERIFIED", feedItemList.getIsVerified());
        intent.putExtra("STATION_FACILITIES", feedItemList.getFacilities());
        showAds(getActivity(), intent);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stations, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.filter_stations) {
            new FragmentFilter().show(getActivity().getSupportFragmentManager(), "FragmentFilter");
        } else if (item.getItemId() == R.id.favorite_stations) {
            Intent intent = new Intent(getActivity(), UserFavorites.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.search_station) {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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
    public void onStart() {
        super.onStart();
        if (mMapView != null) {
            mMapView.onStart();
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