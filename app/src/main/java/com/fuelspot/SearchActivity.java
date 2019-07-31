package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.adapter.MarkerAdapter;
import com.fuelspot.adapter.StationAdapter;
import com.fuelspot.model.StationItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;

public class SearchActivity extends AppCompatActivity {

    SearchView searchView;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    Window window;
    Toolbar toolbar;
    List<StationItem> dummy = new ArrayList<>();
    LatLng sydney;
    RequestOptions options;
    private RequestQueue requestQueue;
    private GoogleMap googleMap;
    private MapView mMapView;

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

        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_station)
                .error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        requestQueue = Volley.newRequestQueue(this);
        mRecyclerView = findViewById(R.id.singleRecyclerView);

        mMapView = findViewById(R.id.googleMapSearch);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        // Analytics
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName("İstasyon ara");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        checkLocationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // adds item to action bar
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Get Search item from action bar and Get Search service
        MenuItem searchItem = menu.findItem(R.id.action_search_online);
        SearchManager searchManager = (SearchManager) SearchActivity.this.getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(SearchActivity.this.getComponentName()));
            searchView.setIconified(false);
            searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // Every time when you press search button on keypad an Activity is recreated which in turn calls this function
    @Override
    protected void onNewIntent(Intent intent) {
        // Get search query and create object of class AsyncFetch
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            int fuelspotID = Integer.parseInt(query);
            if (searchView != null) {
                searchView.clearFocus();
            }
            if (fuelspotID != 0) {
                fetchSingleStation(fuelspotID);
            } else {
                Toast.makeText(SearchActivity.this, "Lütfen 1 - 99.999 arası geçerli bir sayı giriniz.", Toast.LENGTH_LONG).show();
            }
        }
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
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);
                googleMap.setTrafficEnabled(true);

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

    private void fetchSingleStation(int fsID) {
        dummy.clear();
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_STATION) + "?stationID=" + fsID,
                new Response.Listener<String>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                final StationItem item = new StationItem();
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
                                item.setOtherFuels(obj.getString("otherFuels"));
                                item.setIsVerified(obj.getInt("isVerified"));
                                item.setLastUpdated(obj.getString("lastUpdated"));

                                // DISTANCE
                                String[] locationHolder = item.getLocation().split(";");
                                sydney = new LatLng(Double.parseDouble(locationHolder[0]), Double.parseDouble(locationHolder[1]));
                                Location loc1 = new Location("");
                                loc1.setLatitude(Double.parseDouble(userlat));
                                loc1.setLongitude(Double.parseDouble(userlon));
                                Location loc2 = new Location("");
                                loc2.setLatitude(sydney.latitude);
                                loc2.setLongitude(sydney.longitude);
                                item.setDistance((int) loc1.distanceTo(loc2));
                                // DISTANCE

                                dummy.add(item);

                                GridLayoutManager mLayoutManager = new GridLayoutManager(SearchActivity.this, 1);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                mAdapter = new StationAdapter(SearchActivity.this, dummy, "NEARBY_STATIONS");
                                mRecyclerView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();

                                // We are waiting for loading logos
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        addMarker();
                                    }
                                }, 1000);
                            } catch (JSONException e) {
                                Toast.makeText(SearchActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                mRecyclerView.removeAllViews();
                            }
                        } else {
                            Toast.makeText(SearchActivity.this, "İstasyon bulunamadı.", Toast.LENGTH_LONG).show();
                            mRecyclerView.removeAllViews();
                            googleMap.clear();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(SearchActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        mRecyclerView.removeAllViews();
                        googleMap.clear();
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

    private void addMarker() {
        Marker m;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(16f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.addCircle(new CircleOptions()
                .center(sydney)
                .radius(mapDefaultStationRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));
        StationItem info = dummy.get(0);

        if (isStationVerified == 1) {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(info.getStationName()).snippet(info.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
            m = googleMap.addMarker(mOptions);
            m.setTag(info);
        } else {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(info.getStationName()).snippet(info.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));
            m = googleMap.addMarker(mOptions);
            m.setTag(info);
        }
        m.showInfoWindow();
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
