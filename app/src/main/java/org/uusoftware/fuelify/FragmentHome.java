package org.uusoftware.fuelify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.volley.AuthFailureError;
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

import static org.uusoftware.fuelify.AnalyticsApplication.lat;
import static org.uusoftware.fuelify.AnalyticsApplication.lon;

public class FragmentHome extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    LatLng mCurrentLocation = new LatLng(0, 0);

    //Station variables
    String REGISTER_URL = "http://uusoftware.org/Fuelify/add-station.php";
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        mCurrentLocation = new LatLng(lat, lon);

        checkLocationPermission();

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplicationContext()).getDefaultTracker();
        t.setScreenName("Home");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        feedsList = new ArrayList<>();
        mRecyclerView = rootView.findViewById(R.id.feedView);

        queue = Volley.newRequestQueue(getActivity());

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
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

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

                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location arg0) {
                        mCurrentLocation = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    }
                });

                //Call once after map loaded then call every 10 seconds
                updateMapObject();

                final Handler ha = new Handler();
                ha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //call function
                        updateMapObject();
                        ha.postDelayed(this, 30000);
                    }
                }, 30000);
            }
        });
    }

    private void updateMapObject() {
        //Draw a circle with radius of 3000m
        final Circle circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude))
                .radius(3000)
                .strokeColor(Color.RED));

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(12.5f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));

        //Search stations in a radius of 3000m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + mCurrentLocation.latitude + "," + mCurrentLocation.longitude + "&radius=3000&type=gas_station&opennow=true&key=AIzaSyAOE5dwDvW_IOVmw-Plp9y5FLD9_1qb4vc";

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

                            photoURLs[i] = "https://maps.gstatic.com/mapfiles/place_api/icons/gas_station-71.png";

                            LatLng sydney = new LatLng(lat, lon);
                            googleMap.addMarker(new MarkerOptions().position(sydney).title(stationName[i]).snippet(placeID[i]));

                            fetchPrices(placeID[i]);
                            //registerStations(stationName[i], vicinity[i], location[i], placeID[i], "https://maps.gstatic.com/mapfiles/place_api/icons/gas_station-71.png");
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

        final Handler ha2 = new Handler();
        ha2.postDelayed(new Runnable() {
            @Override
            public void run() {
                circle.remove();
                ha2.postDelayed(this, 30000);
            }
        }, 30000);
    }

    private void registerStations(final String name, final String vicinity, final String location, final String placeID, final String photoURL) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
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
            protected Map<String, String> getParams() throws AuthFailureError {
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

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchPrices(final String placeID) {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://uusoftware.org/Fuelify/fetch-prices.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            for (int i = 0; i < res.length(); i++) {
                                JSONObject obj = res.getJSONObject(i);

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
                                item.setLastUpdated(obj.getLong("lastUpdated"));
                                feedsList.add(item);

                                mAdapter = new StationAdapter(getActivity(), feedsList);
                                mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                            }
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
            protected Map<String, String> getParams() throws AuthFailureError {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("placeID", placeID);

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
                        Toast.makeText(getActivity(), "İZİN VERİLDİ", Toast.LENGTH_LONG).show();
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