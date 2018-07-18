package com.fuelspot;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.StationAdapter;
import com.fuelspot.model.StationItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import eu.amirs.JSON;

import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.stationPhotoChooser;

public class FragmentStations extends Fragment {

    MapView mMapView;

    List<StationItem> feedsList = new ArrayList<>();

    //Station variables
    List<String> stationName = new ArrayList<>();
    List<String> googleID = new ArrayList<>();
    List<String> vicinity = new ArrayList<>();
    List<String> location = new ArrayList<>();
    List<String> stationIcons = new ArrayList<>();

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    ArrayList<Marker> markers = new ArrayList<>();
    RequestQueue queue;
    SharedPreferences prefs;
    Circle circle;
    TabLayout tabLayout;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;
    ImageView noStationError;

    public static FragmentStations newInstance() {
        Bundle args = new Bundle();

        FragmentStations fragment = new FragmentStations();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stations, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("İstasyonlar");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        noStationError = rootView.findViewById(R.id.errorPicture);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        queue = Volley.newRequestQueue(getActivity());

        tabLayout = rootView.findViewById(R.id.sortBar);
        tabLayout.setSelectedTabIndicatorColor(Color.BLACK);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                sortBy(position);
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // DO NOTHING
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // DO NOTHING
            }
        });

        mRecyclerView = rootView.findViewById(R.id.feedView);
        mAdapter = new StationAdapter(getActivity(), feedsList);
        mLayoutManager = new GridLayoutManager(getActivity(), 1);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        checkLocationPermission();

        return rootView;
    }

    private void sortBy(int position) {
        switch (position) {
            case 0:
                Collections.sort(feedsList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getGasolinePrice() == 0 || obj2.getGasolinePrice() == 0) {
                            return Integer.MAX_VALUE;
                        } else {
                            return Double.compare(obj1.getGasolinePrice(), obj2.getGasolinePrice());
                        }
                    }
                });
                break;
            case 1:
                Collections.sort(feedsList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getDieselPrice() == 0 || obj2.getDieselPrice() == 0) {
                            return Integer.MAX_VALUE;
                        } else {
                            return Double.compare(obj1.getDieselPrice(), obj2.getDieselPrice());
                        }
                    }
                });
                break;
            case 2:
                Collections.sort(feedsList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getLpgPrice() == 0 || obj2.getLpgPrice() == 0) {
                            return Integer.MAX_VALUE;
                        } else {
                            return Double.compare(obj1.getLpgPrice(), obj2.getLpgPrice());
                        }
                    }
                });
                break;
            case 3:
                Collections.sort(feedsList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getElectricityPrice() == 0 || obj2.getElectricityPrice() == 0) {
                            return Integer.MAX_VALUE;
                        } else {
                            return Double.compare(obj1.getElectricityPrice(), obj2.getElectricityPrice());
                        }
                    }
                });
                break;
            case 4:
                Collections.sort(feedsList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        return Double.compare(obj1.getDistance(), obj2.getDistance());
                    }
                });
                break;
        }
        // Updating layout
        mAdapter.notifyDataSetChanged();
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.REQUEST_LOCATION);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                MainActivity.userlat = (float) location.getLatitude();
                                MainActivity.userlon = (float) location.getLongitude();
                                prefs.edit().putFloat("lat", MainActivity.userlat).apply();
                                prefs.edit().putFloat("lon", MainActivity.userlon).apply();
                                MainActivity.getVariables(prefs);
                                loadMap();
                            } else {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(60000);
                                mLocationRequest.setFastestInterval(5000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            }
                        }
                    });
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
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                updateMapObject();

                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {

                                    Location loc1 = new Location("");
                                    loc1.setLatitude(MainActivity.userlat);
                                    loc1.setLongitude(MainActivity.userlon);

                                    Location loc2 = new Location("");
                                    loc2.setLatitude(location.getLatitude());
                                    loc2.setLongitude(location.getLongitude());

                                    float distanceInMeters = loc1.distanceTo(loc2);

                                    if (distanceInMeters >= 100f) {
                                        MainActivity.userlat = (float) location.getLatitude();
                                        MainActivity.userlon = (float) location.getLongitude();
                                        prefs.edit().putFloat("lat", MainActivity.userlat).apply();
                                        prefs.edit().putFloat("lon", MainActivity.userlon).apply();
                                        MainActivity.getVariables(prefs);
                                        updateMapObject();
                                    }
                                } else {
                                    LocationRequest mLocationRequest = new LocationRequest();
                                    mLocationRequest.setInterval(60000);
                                    mLocationRequest.setFastestInterval(5000);
                                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                }
                            }
                        });
            }
        });
    }

    private void updateMapObject() {
        stationName.clear();
        vicinity.clear();
        googleID.clear();
        location.clear();
        stationIcons.clear();

        if (circle != null) {
            circle.remove();
        }

        if (googleMap != null) {
            googleMap.clear();
        }

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(MainActivity.userlat, MainActivity.userlon);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //Draw a circle with radius of 5000m
        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(MainActivity.userlat, MainActivity.userlon))
                .radius(mapDefaultRange)
                .strokeColor(Color.RED));

        //Search stations in a radius of 5000m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + MainActivity.userlat + "," + MainActivity.userlon + "&radius=" + mapDefaultRange + "&type=gas_station&opennow=true&key=" + getString(R.string.google_api_key);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (response != null && response.length() > 0) {
                            for (int i = 0; i < json.key("results").count(); i++) {
                                stationName.add(json.key("results").index(i).key("name").stringValue());
                                vicinity.add(json.key("results").index(i).key("vicinity").stringValue());
                                googleID.add(json.key("results").index(i).key("place_id").stringValue());

                                double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                                double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();
                                location.add(lat + ";" + lon);

                                stationIcons.add(stationPhotoChooser(stationName.get(i)));

                                LatLng sydney = new LatLng(lat, lon);
                                markers.add(googleMap.addMarker(new MarkerOptions().position(sydney).title(stationName.get(i)).snippet(vicinity.get(i))));

                                addStation(i);
                            }
                        } else {
                            noStationError.setVisibility(View.VISIBLE);
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Yakın çevrenizde istasyon bulunamadı.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                noStationError.setVisibility(View.VISIBLE);
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Yakın çevrenizde istasyon bulunamadı.", Snackbar.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /* This method add stations. If station exists in db, then update it (except prices). Returns stationInfos.
     * To update stationPrices, use API_UPDATE_STATION */
    private void addStation(final int index) {
        feedsList.clear();
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response != null && response.length() > 0) {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    StationItem item = new StationItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationName(obj.getString("name"));
                                    item.setVicinity(obj.getString("vicinity"));
                                    item.setLocation(obj.getString("location"));
                                    item.setGasolinePrice(obj.getDouble("gasolinePrice"));
                                    item.setDieselPrice(obj.getDouble("dieselPrice"));
                                    item.setLpgPrice(obj.getDouble("lpgPrice"));
                                    item.setElectricityPrice(obj.getDouble("electricityPrice"));
                                    item.setGoogleMapID(obj.getString("googleID"));
                                    item.setPhotoURL(obj.getString("photoURL"));

                                    //DISTANCE START
                                    Location loc1 = new Location("");
                                    loc1.setLatitude(MainActivity.userlat);
                                    loc1.setLongitude(MainActivity.userlon);
                                    Location loc2 = new Location("");
                                    loc2.setLatitude(Double.parseDouble(obj.getString("location").split(";")[0]));
                                    loc2.setLongitude(Double.parseDouble(obj.getString("location").split(";")[1]));
                                    float distanceInMeters = loc1.distanceTo(loc2);
                                    item.setDistance(distanceInMeters);
                                    //DISTANCE END

                                    //Lastupdated
                                    item.setLastUpdated(obj.getString("lastUpdated"));

                                    feedsList.add(item);
                                }

                                // Default - Sort by Distance
                                tabLayout.getTabAt(4).select();
                                sortBy(4);
                            }
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
                params.put("name", stationName.get(index));
                params.put("vicinity", vicinity.get(index));
                params.put("location", location.get(index));
                params.put("googleID", googleID.get(index));
                params.put("photoURL", stationIcons.get(index));

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
                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        // Got last known location. In some rare situations this can be null.
                                        if (location != null) {
                                            MainActivity.userlat = (float) location.getLatitude();
                                            MainActivity.userlon = (float) location.getLongitude();
                                            prefs.edit().putFloat("lat", MainActivity.userlat).apply();
                                            prefs.edit().putFloat("lon", MainActivity.userlon).apply();
                                            MainActivity.getVariables(prefs);
                                            loadMap();
                                        } else {
                                            LocationRequest mLocationRequest = new LocationRequest();
                                            mLocationRequest.setInterval(60000);
                                            mLocationRequest.setFastestInterval(15000);
                                            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                        }
                                    }
                                });
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }
}
