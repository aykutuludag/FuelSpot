package com.fuelspot.superuser;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.fuelspot.adapter.CompanyAdapter;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.StationItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.listOfOwnedStations;

public class AddStation extends AppCompatActivity {

    private TextView stationHint;
    private Spinner spinner;
    private EditText editTextStationAddress;
    private EditText editTextStationLicense;
    private RequestQueue requestQueue;
    private MapView mMapView;
    private Circle circle;
    private GoogleMap googleMap;
    private SharedPreferences prefs;
    private int stationID;
    private int doesStationVerified;
    private String googleID;
    private String stationName;
    private String stationAddress;
    private String stationCoordinates;
    private String stationCountry;
    private String licenseNo;
    private String stationLogo;
    private String stationFacilities;
    private float gasolinePrice;
    private float dieselPrice;
    private float lpgPrice;
    private float electricityPrice;
    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_station);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        // ProgressDialogs
        loading = new ProgressDialog(AddStation.this);
        loading.setTitle("Bilgileriniz kaydediliyor");
        loading.setMessage("Lütfen bekleyiniz...");
        loading.setIndeterminate(true);
        loading.setCancelable(false);

        // Initialize map
        MapsInitializer.initialize(this.getApplicationContext());

        stationHint = findViewById(R.id.stationHint);

        spinner = findViewById(R.id.simpleSpinner);

        editTextStationAddress = findViewById(R.id.superStationAddress);
        editTextStationAddress.setText(stationAddress);
        editTextStationAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    stationAddress = s.toString();
                }
            }
        });

        editTextStationLicense = findViewById(R.id.editTextLicense);
        editTextStationLicense.setText(licenseNo);
        editTextStationLicense.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    licenseNo = s.toString();
                }
            }
        });

        Button finishRegistration = findViewById(R.id.finishRegistration);
        finishRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stationName != null && stationName.length() > 0) {
                    if (licenseNo != null && licenseNo.length() > 0) {
                        if (doesStationVerified == 0) {
                            superUserRegistration();
                        } else {
                            Toast.makeText(AddStation.this, "Bu istasyon daha önce onaylanmış. Bir hata olduğunu düşünüyorsanız lütfen bizimle iletişime geçiniz.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AddStation.this, "Lütfen istasyon lisans numarasını giriniz)", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AddStation.this, "Kayıt işlemini tamamlayabilmek için istasyonunuzda olmanız gerekmektedir", Toast.LENGTH_LONG).show();
                }
            }
        });

        loadMap();
    }

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
                googleMap.setTrafficEnabled(true);
                updateMapObject();

                googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location arg0) {
                        Location loc1 = new Location("");
                        loc1.setLatitude(Double.parseDouble(MainActivity.userlat));
                        loc1.setLongitude(Double.parseDouble(MainActivity.userlon));

                        Location loc2 = new Location("");
                        loc2.setLatitude(arg0.getLatitude());
                        loc2.setLongitude(arg0.getLongitude());

                        float distanceInMeters = loc1.distanceTo(loc2);

                        if (distanceInMeters >= mapDefaultStationRange / 2) {
                            MainActivity.userlat = String.valueOf(arg0.getLatitude());
                            MainActivity.userlon = String.valueOf(arg0.getLongitude());
                            prefs.edit().putString("lat", MainActivity.userlat).apply();
                            prefs.edit().putString("lon", MainActivity.userlon).apply();
                            updateMapObject();
                        }
                    }
                });
            }
        });
    }

    private void updateMapObject() {
        if (circle != null) {
            circle.remove();
        }

        if (googleMap != null) {
            googleMap.clear();
            //Draw a circle with radius of 150m
            circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(Double.parseDouble(MainActivity.userlat), Double.parseDouble(MainActivity.userlon)))
                    .radius(mapDefaultStationRange)
                    .fillColor(0x220000FF)
                    .strokeColor(Color.RED));
        }

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(Double.parseDouble(MainActivity.userlat), Double.parseDouble(MainActivity.userlon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(17f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));


        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_SEARCH_STATIONS) + "?location=" + userlat + ";" + userlon + "&radius=" + mapDefaultRange,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                stationID = obj.getInt("id");
                                stationName = obj.getString("name");
                                stationAddress = obj.getString("vicinity");
                                stationCountry = obj.getString("country");
                                stationCoordinates = obj.getString("location");
                                googleID = obj.getString("googleID");
                                stationFacilities = obj.getString("facilities");
                                stationLogo = obj.getString("logoURL");
                                gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                dieselPrice = (float) obj.getDouble("dieselPrice");
                                lpgPrice = (float) obj.getDouble("lpgPrice");
                                electricityPrice = (float) obj.getDouble("electricityPrice");
                                licenseNo = obj.getString("licenseNo");
                                doesStationVerified = obj.getInt("isVerified");

                                if (doesStationVerified == 1) {
                                    stationHint.setTextColor(Color.parseColor("#ff0000"));
                                    stationHint.setText("Bu istasyon daha önce onaylanmış.");
                                } else {
                                    stationHint.setTextColor(Color.parseColor("#2DE778"));
                                }
                                loadStationDetails();
                            } catch (JSONException e) {
                                stationName = "";
                                stationAddress = "";
                                stationCoordinates = "";
                                stationCountry = "";
                                licenseNo = "";
                                stationLogo = "";
                                stationHint.setTextColor(Color.parseColor("#ff0000"));
                                loadStationDetails();
                                Toast.makeText(AddStation.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            stationName = "";
                            stationAddress = "";
                            stationCoordinates = "";
                            stationCountry = "";
                            licenseNo = "";
                            stationLogo = "";
                            stationHint.setTextColor(Color.parseColor("#ff0000"));
                            loadStationDetails();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        stationName = "";
                        stationAddress = "";
                        stationCoordinates = "";
                        stationCountry = "";
                        licenseNo = "";
                        stationLogo = "";
                        stationHint.setTextColor(Color.parseColor("#ff0000"));
                        loadStationDetails();

                        Toast.makeText(AddStation.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
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

    private void loadStationDetails() {
        if (companyList != null && companyList.size() > 0) {
            CompanyAdapter customAdapter = new CompanyAdapter(AddStation.this, companyList);
            spinner.setEnabled(false);
            spinner.setClickable(false);
            spinner.setAdapter(customAdapter);

            for (int i = 0; i < companyList.size(); i++) {
                if (companyList.get(i).getName().equals(stationName)) {
                    spinner.setSelection(i, true);
                    break;
                }
            }
        } else {
            // Somehow companList didn't fetch at SuperMainActivity. Fetch it.
            fetchCompanies();
        }

        editTextStationAddress.setText(stationAddress);
        editTextStationLicense.setText(licenseNo);
    }

    private void fetchCompanies() {
        companyList.clear();

        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_COMPANY),
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void superUserRegistration() {
        loading.show();
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_REGISTRATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (res != null && res.length() > 0) {
                            switch (res) {
                                case "Success":
                                    fetchOwnedStations();
                                    break;
                                case "Fail":
                                    loading.dismiss();
                                    Toast.makeText(AddStation.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    loading.dismiss();
                                    Toast.makeText(AddStation.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } else {
                            loading.dismiss();
                            Toast.makeText(AddStation.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(AddStation.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(stationID));
                params.put("licenseNo", licenseNo);
                params.put("owner", username);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchOwnedStations() {
        listOfOwnedStations.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_SUPERUSER_STATIONS) + "?superusername=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                                    listOfOwnedStations.add(item);
                                }

                                Toast.makeText(AddStation.this, "Bilgileriniz kaydedildi. Onay için sizinle en kısa sürede iletişime geçeceğiz.", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("superusername", username);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
