package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.CompanyAdapter;
import com.fuelspot.model.CompanyItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;

public class ReportMissingStation extends AppCompatActivity {

    ScrollView scrollView;
    Spinner spinner;
    String dummyBrandName;
    String dummyLocation;
    String report, reportDetails;
    private MapView mMapView;
    private GoogleMap googleMap;
    private Window window;
    private Toolbar toolbar;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_missing_station);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#0288D1"), Color.parseColor("#03A9F4"));

        requestQueue = Volley.newRequestQueue(ReportMissingStation.this);
        scrollView = findViewById(R.id.missing_scroll);

        // Activate map
        mMapView = findViewById(R.id.markerMap);
        mMapView.onCreate(savedInstanceState);

        checkLocationPermission();

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dummyBrandName = companyList.get(position).getName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                dummyBrandName = null;
            }
        });

        if (companyList != null && companyList.size() > 0) {
            CompanyAdapter customAdapter = new CompanyAdapter(ReportMissingStation.this, companyList);
            spinner.setAdapter(customAdapter);
        } else {
            // Somehow companyList didn't fetch at SuperMainActivity. Fetch it.
            fetchCompanies();
        }

        Button button = findViewById(R.id.buttonSend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dummyLocation != null && dummyLocation.length() > 0) {
                    if (dummyBrandName != null && dummyBrandName.length() > 0) {
                        report = "Eksik istasyon";
                        reportDetails = "REPORT: { konum = " + dummyLocation + " marka = " + dummyBrandName + " }";
                        sendReporttoServer();
                    } else {
                        Toast.makeText(ReportMissingStation.this, "Lütfen istasyonun markasını seçiniz...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReportMissingStation.this, "Lütfen istasyonun konumunu seçiniz...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendReporttoServer() {
        final ProgressDialog loading = ProgressDialog.show(ReportMissingStation.this, getString(R.string.sending_report), getString(R.string.sending_report), false, true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REPORT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                Toast.makeText(ReportMissingStation.this, getString(R.string.report_send_success), Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ReportMissingStation.this, getString(R.string.error), Toast.LENGTH_SHORT).show();

                            }
                        } else {
                            Toast.makeText(ReportMissingStation.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(ReportMissingStation.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("stationID", String.valueOf(-1));
                params.put("report", report);
                params.put("details", reportDetails);
                params.put("photo", "");
                params.put("prices", "");
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(ReportMissingStation.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ReportMissingStation.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReportMissingStation.this, new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
        } else {
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
                googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            scrollView.requestDisallowInterceptTouchEvent(true);
                        }
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

                LatLng dmmy = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
                dummyLocation = String.format(Locale.getDefault(), "%.5f", dmmy.latitude) + ";" + String.format(Locale.getDefault(), "%.5f", dmmy.longitude);
                MarkerOptions mOptions = new MarkerOptions().position(dmmy).title("İstasyon").snippet(dummyLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));
                Marker m = googleMap.addMarker(mOptions);
                m.showInfoWindow();

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        googleMap.clear();
                        dummyLocation = String.format(Locale.getDefault(), "%.5f", latLng.latitude) + ";" + String.format(Locale.getDefault(), "%.5f", latLng.longitude);
                        MarkerOptions mOptions = new MarkerOptions().position(latLng).title("İstasyon").snippet(dummyLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));
                        Marker m = googleMap.addMarker(mOptions);
                        m.showInfoWindow();
                    }
                });
            }
        });
    }

    private void fetchCompanies() {
        companyList.clear();

        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_COMPANY),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CompanyItem item = new CompanyItem();
                                    item.setID(obj.getInt("id"));
                                    item.setName(obj.getString("companyName"));
                                    item.setLogo(obj.getString("companyLogo"));
                                    item.setWebsite(obj.getString("companyWebsite"));
                                    item.setPhone(obj.getString("companyPhone"));
                                    item.setAddress(obj.getString("companyAddress"));
                                    item.setNumOfVerifieds(obj.getInt("numOfVerifieds"));
                                    item.setNumOfStations(obj.getInt("numOfStations"));
                                    companyList.add(item);
                                }

                                CompanyAdapter customAdapter = new CompanyAdapter(ReportMissingStation.this, companyList);
                                spinner.setEnabled(false);
                                spinner.setClickable(false);
                                spinner.setAdapter(customAdapter);
                            } catch (JSONException e) {
                                Snackbar.make(findViewById(android.R.id.content), e.toString(), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Snackbar.make(findViewById(android.R.id.content), volleyError.toString(), Snackbar.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void coloredBars(int color1, int color2) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (ActivityCompat.checkSelfPermission(ReportMissingStation.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                loadMap();
            } else {
                Snackbar.make(findViewById(R.id.mainContainer), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
