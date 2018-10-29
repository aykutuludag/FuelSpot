package com.fuelspot;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
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
import com.github.ybq.android.spinkit.SpinKitView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import java.util.Locale;
import java.util.Map;

import eu.amirs.JSON;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.stationPhotoChooser;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class FragmentStations extends Fragment {

    MapView mMapView;
    SpinKitView proggressBar;

    List<StationItem> feedsList = new ArrayList<>();

    //Station variables
    List<String> stationName = new ArrayList<>();
    List<String> googleID = new ArrayList<>();
    List<String> vicinity = new ArrayList<>();
    List<String> location = new ArrayList<>();
    List<String> stationIcon = new ArrayList<>();
    List<Integer> distanceInMeters = new ArrayList<>();
    List<String> stationCountry = new ArrayList<>();
    ArrayList<Marker> markers = new ArrayList<>();

    Context mContext;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    RequestQueue queue;
    SharedPreferences prefs;
    Circle circle;
    TabLayout tabLayout;
    ImageView noStationError;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    Location locLastKnown = new Location("");
    BitmapDescriptor verifiedIcon;
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;
    NestedScrollView nScrollView;

    public static FragmentStations newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Stations");

        FragmentStations fragment = new FragmentStations();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stations, container, false);

        // Analytics
        if (getActivity() != null) {
            Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("İstasyonlar");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());
            mContext = getActivity();
        }

        //Variables
        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        nScrollView = rootView.findViewById(R.id.nestedScrollView);
        noStationError = rootView.findViewById(R.id.errorPicture);
        verifiedIcon = BitmapDescriptorFactory.fromResource(R.drawable.verified_station);

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

        proggressBar = rootView.findViewById(R.id.spin_kit);
        proggressBar.setColor(Color.WHITE);

        mRecyclerView = rootView.findViewById(R.id.feedView);
        mAdapter = new StationAdapter(getActivity(), feedsList);
        mLayoutManager = new GridLayoutManager(getActivity(), 1);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        locLastKnown.setLatitude(Double.parseDouble(userlat));
        locLastKnown.setLongitude(Double.parseDouble(userlon));

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
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
                            if (locCurrent.getAccuracy() <= mapDefaultStationRange) {
                                userlat = String.valueOf(locCurrent.getLatitude());
                                userlon = String.valueOf(locCurrent.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();
                                MainActivity.getVariables(prefs);

                                float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                if (distanceInMeter >= (mapDefaultRange / 5)) {
                                    locLastKnown.setLatitude(Double.parseDouble(userlat));
                                    locLastKnown.setLongitude(Double.parseDouble(userlon));
                                    updateMapObject();
                                } else {
                                    if (feedsList != null && feedsList.size() > 0) {
                                        for (int i = 0; i < feedsList.size(); i++) {
                                            String[] stationLocation = feedsList.get(i).getLocation().split(";");
                                            double stationLat = Double.parseDouble(stationLocation[0]);
                                            double stationLon = Double.parseDouble(stationLocation[1]);

                                            Location locStation = new Location("");
                                            locStation.setLatitude(stationLat);
                                            locStation.setLongitude(stationLon);

                                            float newDistance = locCurrent.distanceTo(locStation);
                                            distanceInMeters.set(i, (int) newDistance);
                                            feedsList.get(i).setDistance((int) newDistance);
                                        }
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        } else {
                            Snackbar.make(getActivity().findViewById(R.id.mainContainer), getActivity().getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.mainContainer), getActivity().getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                }
            }
        };

        checkLocationPermission();

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
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
            /*    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int dontHide;
                        for (int i = 0; i < markers.size(); i++) {
                            if (marker.equals(markers.get(i))) {
                                RecyclerView.ViewHolder holder;
                                dontHide = i;

                                for (int j = 0; j < stationName.size(); j++) {
                                    holder = mRecyclerView.findViewHolderForAdapterPosition(j);
                                    if (holder != null) {
                                        holder.itemView.setVisibility(View.GONE);
                                    }
                                }

                                holder = mRecyclerView.findViewHolderForAdapterPosition(dontHide);
                                if (holder != null) {
                                    holder.itemView.setVisibility(View.VISIBLE);
                                }
                                mAdapter.notifyDataSetChanged();
                                return true;
                            }
                        }
                        return false;
                    }
                });*/
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
                updateMapObject();
            }
        });
    }

    private void updateMapObject() {
        stationName.clear();
        vicinity.clear();
        stationCountry.clear();
        markers.clear();
        googleID.clear();
        location.clear();
        stationIcon.clear();
        distanceInMeters.clear();
        feedsList.clear();

        if (circle != null) {
            circle.remove();
        }

        if (googleMap != null) {
            googleMap.clear();
        }

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //Draw a circle with radius of mapDefaultRange
        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                .radius(mapDefaultRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));

        searchStations("");
    }

    void searchStations(String token) {
        String url;

        if (token != null && token.length() > 0) {
            // For getting next 20 stations
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=" + mapDefaultRange + "&type=gas_station&pagetoken=" + token + "&key=" + getString(R.string.g_api_key);
        } else {
            // For getting first 20 stations
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=" + mapDefaultRange + "&type=gas_station" + "&key=" + getString(R.string.g_api_key);
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (response != null && response.length() > 0) {
                            if (json.key("results").count() > 0) {
                                for (int i = 0; i < json.key("results").count(); i++) {
                                    stationName.add(json.key("results").index(i).key("name").stringValue());
                                    vicinity.add(json.key("results").index(i).key("vicinity").stringValue());
                                    googleID.add(json.key("results").index(i).key("place_id").stringValue());

                                    double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                                    double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();
                                    location.add(lat + ";" + lon);

                                    stationIcon.add(stationPhotoChooser(stationName.get(i)));

                                    Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                                    try {
                                        List<Address> addresses = geo.getFromLocation(lat, lon, 1);
                                        if (addresses.size() > 0) {
                                            stationCountry.add(i, addresses.get(0).getCountryCode());
                                        } else {
                                            stationCountry.add(i, "");
                                        }
                                    } catch (Exception e) {
                                        stationCountry.add(i, "");
                                    }
                                }

                                if (json.key("next_page_token").stringValue() != null && json.key("next_page_token").stringValue().length() > 0) {
                                    searchStations(json.key("next_page_token").stringValue());
                                } else {
                                    for (int i = 0; i < stationName.size(); i++) {
                                        addStation(i);
                                    }
                                    proggressBar.setVisibility(View.GONE);
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                    tabLayout.getTabAt(4).select();
                                }
                            } else {
                                // Maybe s/he is in the countryside. Increase mapDefaultRange, decrease mapDefaultZoom
                                if (mapDefaultRange == 2500) {
                                    mapDefaultRange = 5000;
                                    mapDefaultZoom = 12f;
                                    Toast.makeText(getActivity(), "2500 metre içerisinde istasyon bulunamadı. YENİ MENZİL DENENİYOR: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    updateMapObject();
                                } else if (mapDefaultRange == 5000) {
                                    mapDefaultRange = 10000;
                                    mapDefaultZoom = 11f;
                                    Toast.makeText(getActivity(), "5000 metre içerisinde istasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    updateMapObject();
                                } else if (mapDefaultRange == 10000) {
                                    mapDefaultRange = 20000;
                                    mapDefaultZoom = 10f;
                                    Toast.makeText(getActivity(), "10000 metre içerisinde istasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    updateMapObject();
                                } else if (mapDefaultRange == 20000) {
                                    mapDefaultRange = 50000;
                                    mapDefaultZoom = 8.75f;
                                    Toast.makeText(getActivity(), "20000 metre içerisinde istasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    updateMapObject();
                                } else {
                                    noStationError.setVisibility(View.VISIBLE);
                                    Snackbar.make(getActivity().findViewById(R.id.mainContainer), "Yakın çevrenizde istasyon bulunamadı.", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            noStationError.setVisibility(View.VISIBLE);
                            Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener()

        {
            @Override
            public void onErrorResponse(VolleyError error) {
                noStationError.setVisibility(View.VISIBLE);
                Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /* This method add_fuel stations. If station exists in db, then update it (except prices). Returns stationInfos.
     * To update stationPrices, use API_UPDATE_STATION */
    private void addStation(final int index) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);
                                if (obj.getInt("isActive") == 1) {
                                    StationItem item = new StationItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationName(obj.getString("name"));
                                    item.setVicinity(obj.getString("vicinity"));
                                    item.setCountryCode(obj.getString("country"));
                                    item.setLocation(obj.getString("location"));
                                    item.setGoogleMapID(obj.getString("googleID"));
                                    item.setLicenseNo(obj.getString("licenseNo"));
                                    item.setOwner(obj.getString("owner"));
                                    item.setPhotoURL(obj.getString("photoURL"));
                                    item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                    item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                    item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                    item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
                                    item.setIsActive(obj.getInt("isActive"));
                                    item.setLastUpdated(obj.getString("lastUpdated"));

                                    //DISTANCE START
                                    Location loc = new Location("");
                                    String[] stationKonum = item.getLocation().split(";");
                                    loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                    loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                    float uzaklik = locLastKnown.distanceTo(loc);
                                    distanceInMeters.add((int) uzaklik);
                                    item.setDistance((int) uzaklik);
                                    //DISTANCE END

                                    feedsList.add(item);
                                    mAdapter.notifyDataSetChanged();

                                    //Add marker
                                    LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());
                                    if (item.getIsVerified() == 1) {
                                        markers.add(googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")).icon(verifiedIcon)));
                                    } else {
                                        markers.add(googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity"))));
                                    }

                                    sortBy(4);
                                }
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
                params.put("name", stationName.get(index));
                params.put("vicinity", vicinity.get(index));
                params.put("country", stationCountry.get(index));
                params.put("location", location.get(index));
                params.put("googleID", googleID.get(index));
                params.put("photoURL", stationIcon.get(index));

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
                Collections.sort(feedsList, new Comparator<StationItem>() {
                    public int compare(StationItem obj1, StationItem obj2) {
                        if (obj1.getGasolinePrice() == 0 || obj2.getGasolinePrice() == 0) {
                            return Integer.MAX_VALUE;
                        } else if (obj1.getGasolinePrice() == obj2.getGasolinePrice()) {
                            return Double.compare(obj1.getDistance(), obj2.getDistance());
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
                        } else if (obj1.getDieselPrice() == obj2.getDieselPrice()) {
                            return Double.compare(obj1.getDistance(), obj2.getDistance());
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
                        } else if (obj1.getLpgPrice() == obj2.getLpgPrice()) {
                            return Double.compare(obj1.getDistance(), obj2.getDistance());
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
                        } else if (obj1.getElectricityPrice() == obj2.getElectricityPrice()) {
                            return Double.compare(obj1.getDistance(), obj2.getDistance());
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