package org.uusoftware.fuelify;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uusoftware.fuelify.adapter.StationAdapter;
import org.uusoftware.fuelify.model.StationItem;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import eu.amirs.JSON;

import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.userlat;
import static org.uusoftware.fuelify.MainActivity.userlon;

public class FragmentStations extends Fragment {

    MapView mMapView;

    //Station variables
    String[] stationName = new String[99];
    String[] placeID = new String[99];
    String[] vicinity = new String[99];
    String[] location = new String[99];
    String[] photoURLs = new String[99];
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<StationItem> feedsList;
    RequestQueue queue;
    private GoogleMap googleMap;
    SharedPreferences prefs;
    Circle circle;
    Marker[] markes = new Marker[99];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stations, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplicationContext()).getDefaultTracker();
        t.setScreenName("Home");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        feedsList = new ArrayList<>();
        mRecyclerView = rootView.findViewById(R.id.feedView);
        queue = Volley.newRequestQueue(getActivity());
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        checkLocationPermission();

        return rootView;
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Konum izni gerekiyor")
                        .setMessage("Size en yakın benzinlikleri ve fiyatlarını gösterebilmemiz için konum iznine ihtiyaç duyuyoruz")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(), new String[]
                                        {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            //Request location updates:
            LocationManager locationManager = (LocationManager)
                    getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

            if (location != null) {
                userlat = location.getLatitude();
                userlon = location.getLongitude();
                prefs.edit().putString("lat", String.valueOf(userlat)).apply();
                prefs.edit().putString("lon", String.valueOf(userlon)).apply();
                MainActivity.getVariables(prefs);
            }

            loadMap();
        }
    }

    void loadMap() {
        //Detect location and set on map
        MapsInitializer.initialize(getActivity().getApplicationContext());
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location arg0) {
                        Location loc1 = new Location("");
                        loc1.setLatitude(userlat);
                        loc1.setLongitude(userlon);

                        Location loc2 = new Location("");
                        loc2.setLatitude(arg0.getLatitude());
                        loc2.setLongitude(arg0.getLongitude());

                        float distanceInMeters = loc1.distanceTo(loc2);

                        if (distanceInMeters >= 10) {
                            userlat = arg0.getLatitude();
                            userlon = arg0.getLongitude();
                            prefs.edit().putString("lat", String.valueOf(userlat)).apply();
                            prefs.edit().putString("lon", String.valueOf(userlon)).apply();
                            MainActivity.getVariables(prefs);

                            updateMapObject();
                        }
                    }
                });
                updateMapObject();
            }
        });
    }

    private void updateMapObject() {
        if (circle != null) {
            circle.remove();
        }

        if (googleMap != null) {
            googleMap.clear();
        }

        //Draw a circle with radius of 5000m
        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(userlat, userlon))
                .radius(3500)
                .strokeColor(Color.RED));

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(userlat, userlon);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(12f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));

        //Search stations in a radius of 3500m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=3500&type=gas_station&opennow=true&key=AIzaSyAOE5dwDvW_IOVmw-Plp9y5FLD9_1qb4vc";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //System.out.println("Response is: " + response);
                        JSON json = new JSON(response);

                        for (int i = 0; i < json.key("results").count(); i++) {
                            stationName[i] = json.key("results").index(i).key("name").stringValue();
                            vicinity[i] = json.key("results").index(i).key("vicinity").stringValue();
                            placeID[i] = json.key("results").index(i).key("place_id").stringValue();

                            double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                            double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();
                            location[i] = lat + ";" + lon;

                            stationPhotoChooser(i);

                            LatLng sydney = new LatLng(lat, lon);
                            markes[i] = googleMap.addMarker(new MarkerOptions().position(sydney).title(stationName[i]).snippet(vicinity[i]));

                            registerStations(stationName[i], vicinity[i], location[i], placeID[i], photoURLs[i]);
                            fetchPrices(placeID[i]);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void stationPhotoChooser(int i) {
        if (stationName[i].contains("Shell")) {
            photoURLs[i] = "http://fuel-spot.com/FUELSPOTAPP/station_icons/shell.png";
        } else if (stationName[i].contains("Opet")) {
            photoURLs[i] = "http://fuel-spot.com/FUELSPOTAPP/station_icons/opet.jpg";
        } else if (stationName[i].contains("BP")) {
            photoURLs[i] = "http://fuel-spot.com/FUELSPOTAPP/station_icons/bp.png";
        } else if (stationName[i].contains("Kadoil")) {
            photoURLs[i] = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kadoil.jpg";
        } else if (stationName[i].contains("Petrol Ofisi")) {
            photoURLs[i] = "http://fuel-spot.com/FUELSPOTAPP/station_icons/petrol-ofisi.png";
        } else if (stationName[i].contains("Lukoil")) {
            photoURLs[i] = "http://fuel-spot.com/FUELSPOTAPP/station_icons/lukoil.jpg";
        } else {
            photoURLs[i] = "http://fuel-spot.com/FUELSPOTAPP/station_icons/unknown.png";
        }
    }

    private void registerStations(final String name, final String vicinity, final String location, final String placeID, final String photoURL) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REGISTER_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //   Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("name", name);
                params.put("vicinity", vicinity);
                params.put("location", location);
                params.put("googleID", placeID);
                params.put("photoURL", photoURL);
                params.put("timeStamp", String.valueOf(System.currentTimeMillis()));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    public void fetchPrices(final String placeID) {
        feedsList.clear();
        mAdapter = null;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION_PRICES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            System.out.println(response);

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
                            loc1.setLatitude(userlat);
                            loc1.setLongitude(userlon);
                            Location loc2 = new Location("");
                            loc2.setLatitude(Double.parseDouble(obj.getString("location").split(";")[0]));
                            loc2.setLongitude(Double.parseDouble(obj.getString("location").split(";")[1]));
                            float distanceInMeters = loc1.distanceTo(loc2);
                            item.setDistance(distanceInMeters);
                            //DISTANCE END
                            item.setLastUpdated(obj.getLong("lastUpdated"));
                            feedsList.add(item);

                            mAdapter = new StationAdapter(getActivity(), feedsList);
                            mLayoutManager = new GridLayoutManager(getActivity(), 1);

                            mAdapter.notifyDataSetChanged();
                            mRecyclerView.setAdapter(mAdapter);
                            mRecyclerView.setLayoutManager(mLayoutManager);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("placeID", placeID);

                if (fuelPri == 0) {
                    params.put("orderBy", "gasolinePrice");
                } else if (fuelPri == 1) {
                    params.put("orderBy", "dieselPrice");
                } else if (fuelPri == 2) {
                    params.put("orderBy", "lpgPrice");
                } else {
                    params.put("orderBy", "electricityPrice");
                }

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
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        LocationManager locationManager = (LocationManager)
                                getActivity().getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();

                        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                        if (location != null) {
                            userlat = location.getLatitude();
                            userlon = location.getLongitude();
                            prefs.edit().putString("lat", String.valueOf(userlat)).apply();
                            prefs.edit().putString("lon", String.valueOf(userlon)).apply();
                            MainActivity.getVariables(prefs);
                        }

                        loadMap();
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
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
