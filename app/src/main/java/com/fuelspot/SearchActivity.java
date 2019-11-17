package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.lastStationSearch;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.searchCount;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class SearchActivity extends AppCompatActivity {

    EditText editText;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    Window window;
    Toolbar toolbar;
    private RequestQueue requestQueue;
    private GoogleMap googleMap;
    private MapView mMapView;
    int whichOrder = 4;
    private double tempLat, tempLong;
    private ArrayList<Marker> tempMarkers = new ArrayList<>();
    private List<StationItem> tempStationList = new ArrayList<>();
    private RelativeLayout sortGasolineLayout;
    private RelativeLayout sortDieselLayout;
    private RelativeLayout sortLPGLayout;
    private RelativeLayout sortElectricityLayout;
    private RelativeLayout sortDistanceLayout;
    private SharedPreferences prefs;
    NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        requestQueue = Volley.newRequestQueue(this);
        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        if (!premium) {
            if (System.currentTimeMillis() - lastStationSearch >= (1000 * 60 * 60 * 24)) {
                searchCount = 3;
                prefs.edit().putInt("searchCount", searchCount).apply();

                lastStationSearch = System.currentTimeMillis();
                prefs.edit().putLong("lastStationSearch", lastStationSearch).apply();
            } else {
                searchCount = prefs.getInt("searchCount", 0);
            }
        }

        scrollView = findViewById(R.id.scrollViewSearch);
        editText = findViewById(R.id.editText);
        editText.setFocusable(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (premium) {
                    int AUTOCOMPLETE_REQUEST_CODE = 1;
                    List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).setCountry("TR").build(SearchActivity.this);
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                } else {
                    if (searchCount > 0) {
                        int AUTOCOMPLETE_REQUEST_CODE = 1;
                        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
                        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).setCountry("TR").build(SearchActivity.this);
                        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                    } else {
                        Toast.makeText(SearchActivity.this, getString(R.string.station_search_limit), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        mMapView = findViewById(R.id.googleMapSearch);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        sortGasolineLayout = findViewById(R.id.sortGasoline);
        sortDieselLayout = findViewById(R.id.sortDiesel);
        sortLPGLayout = findViewById(R.id.sortLPG);
        sortElectricityLayout = findViewById(R.id.sortElectric);
        sortDistanceLayout = findViewById(R.id.sortDistance);
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

        GridLayoutManager mLayoutManager = new GridLayoutManager(SearchActivity.this, 1);
        mAdapter = new StationAdapter(SearchActivity.this, tempStationList, "NEARBY_STATIONS");

        mRecyclerView = findViewById(R.id.searchRecyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.removeAllViews();

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SearchActivity.this, new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
        } else {
            loadMap();
        }
    }

    private void loadMap() {
        //Detect location and set on map
        MapsInitializer.initialize(this.getApplicationContext());
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
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

                MarkerAdapter customInfoWindow = new MarkerAdapter(SearchActivity.this);
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

    private void searchStations() {
        tempStationList.clear();
        final ProgressDialog stationFetching = ProgressDialog.show(SearchActivity.this, "İstasyonlar aranıyor", getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_SEARCH_STATIONS) + "?location=" + tempLat + ";" + tempLong + "&radius=" + mapDefaultRange,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        stationFetching.dismiss();
                        showAds(SearchActivity.this, null);

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
                                    //DISTANCE START
                                    Location locLastKnow = new Location("");
                                    locLastKnow.setLatitude(Double.parseDouble(userlat));
                                    locLastKnow.setLongitude(Double.parseDouble(userlon));

                                    Location loc = new Location("");
                                    String[] stationKonum = item.getLocation().split(";");
                                    loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                    loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                    float uzaklik = locLastKnow.distanceTo(loc);
                                    item.setDistance((int) uzaklik);
                                    //DISTANCE END
                                    tempStationList.add(item);
                                }

                                sortBy(whichOrder);

                                if (tempStationList.size() == 33) {
                                    Toast.makeText(SearchActivity.this, getString(R.string.limited_33), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SearchActivity.this, getString(R.string.station_found_pretext) + " " + tempStationList.size() + " " + getString(R.string.station_found_aftertext), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(SearchActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (mapDefaultRange == 3000) {
                                mapDefaultRange = 6000;
                                mapDefaultZoom = 12f;
                                Toast.makeText(SearchActivity.this, getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                searchStations();
                            } else if (mapDefaultRange == 6000) {
                                mapDefaultRange = 10000;
                                mapDefaultZoom = 11.5f;
                                Toast.makeText(SearchActivity.this, getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                searchStations();
                            } else if (mapDefaultRange == 10000) {
                                mapDefaultRange = 25000;
                                mapDefaultZoom = 10f;
                                Toast.makeText(SearchActivity.this, getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                searchStations();
                            } else if (mapDefaultRange == 25000) {
                                mapDefaultRange = 50000;
                                mapDefaultZoom = 9f;
                                Toast.makeText(SearchActivity.this, getString(R.string.station_not_found_retry) + " " + mapDefaultRange + getString(R.string.metre), Toast.LENGTH_SHORT).show();
                                searchStations();
                            } else {
                                // no station within 50km
                                Toast.makeText(SearchActivity.this, getString(R.string.no_station), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(SearchActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
        requestQueue.add(stringRequest);
    }

    private void sortBy(int position) {
        switch (position) {
            case 0:
                Collections.sort(tempStationList, new Comparator<StationItem>() {
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
                Collections.sort(tempStationList, new Comparator<StationItem>() {
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
                Collections.sort(tempStationList, new Comparator<StationItem>() {
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
                Collections.sort(tempStationList, new Comparator<StationItem>() {
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
                Collections.sort(tempStationList, new Comparator<StationItem>() {
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

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);

        // Clear variables
        googleMap.clear();
        tempMarkers.clear();

        //Draw a circle with radius of tempRange
        googleMap.addCircle(new CircleOptions()
                .center(new LatLng(tempLat, tempLong))
                .radius(mapDefaultRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));

        // We are waiting for loading logos
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                addMarkers();
            }
        }, 1000);
    }

    private void addMarkers() {
        for (int i = 0; i < tempStationList.size(); i++) {
            addMarker(tempStationList.get(i));
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (tempMarkers != null && tempMarkers.size() > 0) {
                    tempMarkers.get(0).showInfoWindow();
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
        tempMarkers.add(m);
    }

    private void openStation(StationItem feedItemList) {
        Intent intent = new Intent(SearchActivity.this, StationDetails.class);
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
        showAds(SearchActivity.this, intent);
    }


    public void coloredBars(int color1, int color2) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color1);
            toolbar.setBackgroundColor(color2);
        } else {
            toolbar.setBackgroundColor(color2);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                editText.setText(place.getName());

                tempLat = place.getLatLng().latitude;
                tempLong = place.getLatLng().longitude;

                if (googleMap != null) {
                    LatLng mCurrentLocation = new LatLng(tempLat, tempLong);
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

                searchCount--;
                prefs.edit().putInt("searchCount", searchCount).apply();

                searchStations();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("AutoCompleteError", status.getStatusMessage());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
            if (googleMap != null) {
                if (ActivityCompat.checkSelfPermission(SearchActivity.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
            if (googleMap != null) {
                if (ActivityCompat.checkSelfPermission(SearchActivity.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(false);
                }
            }
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

        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
