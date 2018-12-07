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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    //Station variables
    static List<String> stationName = new ArrayList<>();
    List<StationItem> dummyList = new ArrayList<>();
    MapView mMapView;
    SpinKitView proggressBar;

    public List<StationItem> feedsList = new ArrayList<>();
    static List<String> googleID = new ArrayList<>();
    static List<String> vicinity = new ArrayList<>();
    static List<String> location = new ArrayList<>();
    static List<String> stationIcon = new ArrayList<>();
    static List<String> stationCountry = new ArrayList<>();
    List<Integer> distanceInMeters = new ArrayList<>();
    ArrayList<Marker> markers = new ArrayList<>();

    Context mContext;
    RelativeLayout stationLayout;
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
    private GoogleMap googleMap;
    private FusedLocationProviderClient mFusedLocationClient;
    NestedScrollView nScrollView;
    Button seeAllStations;
    View rootView;
    boolean isAllStationsListed;
    RelativeLayout sortGasolineLayout, sortDieselLayout, sortLPGLayout, sortElectricityLayout, sortDistanceLayout;

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
                Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
                t.setScreenName("İstasyonlar");
                t.enableAdvertisingIdCollection(true);
                t.send(new HitBuilders.ScreenViewBuilder().build());
                mContext = getActivity();
            }

            //Variables
            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            queue = Volley.newRequestQueue(getActivity());

            nScrollView = rootView.findViewById(R.id.nestedScrollView);
            noStationError = rootView.findViewById(R.id.errorPicture);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            stationLayout = rootView.findViewById(R.id.stationLayout);
            proggressBar = rootView.findViewById(R.id.spin_kit);

            /*tabLayout = rootView.findViewById(R.id.sortBar);
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
            });*/
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
            mAdapter = new StationAdapter(getActivity(), dummyList);
            mLayoutManager = new GridLayoutManager(getActivity(), 1);

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setNestedScrollingEnabled(false);

            seeAllStations = rootView.findViewById(R.id.button_seeAllStations);
            seeAllStations.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter = new StationAdapter(getActivity(), feedsList);
                    mAdapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(mAdapter);
                    seeAllStations.setVisibility(View.GONE);
                    isAllStationsListed = true;
                }
            });

            mMapView = rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);

            locLastKnown.setLatitude(Double.parseDouble(userlat));
            locLastKnown.setLongitude(Double.parseDouble(userlon));

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000);
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
                    }
                }
            };

            checkLocationPermission();
        }
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
                googleMap.getUiSettings().setAllGesturesEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
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
        dummyList.clear();
        mAdapter.notifyDataSetChanged();

        stationLayout.setVisibility(View.GONE);
        proggressBar.setVisibility(View.VISIBLE);
        seeAllStations.setVisibility(View.GONE);

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
                                    googleID.add(json.key("results").index(i).key("place_id").stringValue());
                                    stationName.add(json.key("results").index(i).key("name").stringValue());
                                    vicinity.add(json.key("results").index(i).key("vicinity").stringValue());

                                    double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                                    double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();
                                    location.add(String.format(Locale.US, "%.5f", lat) + ";" + String.format(Locale.US, "%.5f", lon));

                                    stationIcon.add(stationPhotoChooser(json.key("results").index(i).key("name").stringValue()));
                                    stationCountry.add(countryFinder(lat, lon));
                                }

                                if (!json.key("next_page_token").isNull() && json.key("next_page_token").stringValue().length() > 0) {
                                    searchStations(json.key("next_page_token").stringValue());
                                } else {
                                    for (int i = 0; i < googleID.size(); i++) {
                                        addStations(i);
                                    }
                                }
                            } else {
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
                                    mapDefaultRange = 20000;
                                    mapDefaultZoom = 10f;
                                    Toast.makeText(getActivity(), "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    updateMapObject();
                                } else if (mapDefaultRange == 20000) {
                                    mapDefaultRange = 50000;
                                    mapDefaultZoom = 8.75f;
                                    Toast.makeText(getActivity(), "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    updateMapObject();
                                } else {
                                    noStationError.setVisibility(View.VISIBLE);
                                    Snackbar.make(getActivity().findViewById(android.R.id.content), "İstasyon bulunamadı...", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            noStationError.setVisibility(View.VISIBLE);
                            Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    private String countryFinder(double lat, double lon) {
                        Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geo.getFromLocation(lat, lon, 1);
                            if (addresses.size() > 0) {
                                return addresses.get(0).getCountryCode();
                            } else {
                                return "";
                            }
                        } catch (Exception e) {
                            return "";
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                noStationError.setVisibility(View.VISIBLE);
                Snackbar.make(getActivity().findViewById(android.R.id.content), error.toString(), Snackbar.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /* This method add_fuel stations. If station exists in db, then update it (except prices). Returns stationInfos.
     * To update stationPrices, use API_UPDATE_STATION */
    private void addStations(final int index) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                JSONObject obj = res.getJSONObject(0);

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

                                if (feedsList.size() <= 10) {
                                    dummyList.add(item);
                                } else {
                                    seeAllStations.setVisibility(View.VISIBLE);
                                }

                                //Add marker
                                LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());
                                markers.add(googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance))));

                                sortBy(4);
                                noStationError.setVisibility(View.GONE);
                                stationLayout.setVisibility(View.VISIBLE);
                                proggressBar.setVisibility(View.GONE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                params.put("name", stationName.get(index));
                params.put("vicinity", vicinity.get(index));
                params.put("country", stationCountry.get(index));
                params.put("location", location.get(index));
                params.put("googleID", googleID.get(index));
                params.put("logoURL", stationPhotoChooser(stationName.get(index)));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    private void sortBy(int position) {
        List<StationItem> tempList;

        if (isAllStationsListed) {
            tempList = feedsList;
        } else {
            tempList = dummyList;
        }

        switch (position) {
            case 0:
                Collections.sort(tempList, new Comparator<StationItem>() {
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
                Collections.sort(tempList, new Comparator<StationItem>() {
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
                Collections.sort(tempList, new Comparator<StationItem>() {
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
                Collections.sort(tempList, new Comparator<StationItem>() {
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
                Collections.sort(tempList, new Comparator<StationItem>() {
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