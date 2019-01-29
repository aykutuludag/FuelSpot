package com.fuelspot.superuser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.R;
import com.fuelspot.model.StationItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.getSuperVariables;
import static com.fuelspot.superuser.SuperMainActivity.isMobilePaymentAvailable;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;
import static com.fuelspot.superuser.SuperMainActivity.listOfOwnedStations;
import static com.fuelspot.superuser.SuperMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.SuperMainActivity.superFacilities;
import static com.fuelspot.superuser.SuperMainActivity.superGoogleID;
import static com.fuelspot.superuser.SuperMainActivity.superLastUpdate;
import static com.fuelspot.superuser.SuperMainActivity.superLicenseNo;
import static com.fuelspot.superuser.SuperMainActivity.superStationAddress;
import static com.fuelspot.superuser.SuperMainActivity.superStationCountry;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;
import static com.fuelspot.superuser.SuperMainActivity.superStationLocation;
import static com.fuelspot.superuser.SuperMainActivity.superStationLogo;
import static com.fuelspot.superuser.SuperMainActivity.superStationName;

public class AddStation extends AppCompatActivity {

    Toolbar toolbar;
    TextView stationHint;
    EditText editTextStationName, editTextStationAddress, editTextStationLicense;
    RequestQueue requestQueue;
    MapView mMapView;
    Circle circle;
    GoogleMap googleMap;
    Button finishRegistration;
    SharedPreferences prefs;

    int stationID, doesStationVerified;
    String googleID, stationName, stationAddress, stationCoordinates, stationCountry, licenseNo, stationLogo;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_station);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getSuperVariables(prefs);
        requestQueue = Volley.newRequestQueue(this);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        loadMap();

        stationHint = findViewById(R.id.stationHint);

        editTextStationName = findViewById(R.id.superStationName);
        editTextStationName.setText(stationName);
        editTextStationName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    stationName = s.toString();
                }
            }
        });

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

        finishRegistration = findViewById(R.id.finishRegistration);
        finishRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stationName != null && stationName.length() > 0) {
                    if (licenseNo != null && licenseNo.length() > 0) {
                        if (doesStationVerified == 0) {
                            updateStation();
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
    }

    void loadMap() {
        //Detect location and set on map
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
                        loc1.setLatitude(Double.parseDouble(userlat));
                        loc1.setLongitude(Double.parseDouble(userlon));

                        Location loc2 = new Location("");
                        loc2.setLatitude(arg0.getLatitude());
                        loc2.setLongitude(arg0.getLongitude());

                        float distanceInMeters = loc1.distanceTo(loc2);

                        if (distanceInMeters >= mapDefaultStationRange / 2) {
                            userlat = String.valueOf(arg0.getLatitude());
                            userlon = String.valueOf(arg0.getLongitude());
                            prefs.edit().putString("lat", userlat).apply();
                            prefs.edit().putString("lon", userlon).apply();
                            getVariables(prefs);
                            // updateMapObject();
                        }
                    }
                });
                // updateMapObject();
            }
        });
    }

   /* private void updateMapObject() {
        if (circle != null) {
            circle.remove();
        }

        if (googleMap != null) {
            googleMap.clear();

            //Draw a circle with radius of 150m
            circle = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                    .radius(mapDefaultStationRange)
                    .strokeColor(Color.RED));
        }

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(17f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));

        //Search stations in a radius of 50m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=" + mapDefaultStationRange + "&type=gas_station&key=" + getString(R.string.google_api_key);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (json.key("results").count() > 0) {
                            for (int i = 0; i < json.key("results").count(); i++) {
                                googleID = json.key("results").index(i).key("place_id").stringValue();
                                stationName = json.key("results").index(i).key("name").stringValue();
                                stationAddress = json.key("results").index(i).key("vicinity").stringValue();
                                double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                                double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();
                                stationCoordinates = lat + ";" + lon;

                                Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                                try {
                                    List<Address> addresses = geo.getFromLocation(lat, lon, 1);
                                    if (addresses.size() > 0) {
                                        stationCountry = addresses.get(0).getCountryCode();
                                    } else {
                                        stationCountry = "";
                                    }
                                } catch (Exception e) {
                                    stationCountry = "";
                                }

                                stationLogo = stationPhotoChooser(stationName);
                                addStation();
                            }
                        } else {
                            stationName = "";
                            stationAddress = "";
                            stationCoordinates = "";
                            stationCountry = "";
                            licenseNo = "";
                            stationLogo = "";

                            editTextStationName.setText(stationName);
                            editTextStationAddress.setText(stationAddress);
                            editTextStationLicense.setText(licenseNo);
                            stationHint.setTextColor(Color.parseColor("#ff0000"));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }*/

    private void addStation() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SEARCH_STATIONS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);
                                if (obj.getInt("isActive") == 1) {
                                    stationID = obj.getInt("id");
                                    stationName = obj.getString("name");
                                    stationAddress = obj.getString("vicinity");
                                    stationCoordinates = obj.getString("location");
                                    googleID = obj.getString("googleID");
                                    licenseNo = obj.getString("licenseNo");
                                    stationLogo = obj.getString("photoURL");
                                    gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                    dieselPrice = (float) obj.getDouble("dieselPrice");
                                    lpgPrice = (float) obj.getDouble("lpgPrice");
                                    electricityPrice = (float) obj.getDouble("electricityPrice");
                                    doesStationVerified = obj.getInt("isVerified");

                                    if (doesStationVerified == 1) {
                                        stationHint.setTextColor(Color.parseColor("#ff0000"));
                                        stationHint.setText("Bu istasyon daha önce onaylanmış. Bir hata olduğunu düşünüyorsanız lütfen bizimle iletişime geçiniz.");
                                    } else {
                                        stationHint.setTextColor(Color.parseColor("#00801e"));
                                    }

                                    Double lat = Double.parseDouble(stationCoordinates.split(";")[0]);
                                    Double lon = Double.parseDouble(stationCoordinates.split(";")[1]);

                                    LatLng sydney = new LatLng(lat, lon);
                                    googleMap.addMarker(new MarkerOptions().position(sydney).title(stationName).snippet(stationAddress));

                                    editTextStationName.setText(stationName);
                                    editTextStationAddress.setText(stationAddress);
                                    editTextStationLicense.setText(licenseNo);
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
                params.put("name", stationName);
                params.put("vicinity", stationAddress);
                params.put("country", stationCountry);
                params.put("location", stationCoordinates);
                params.put("googleID", googleID);
                params.put("photoURL", stationLogo);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void updateStation() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (res != null && res.length() > 0) {
                            switch (res) {
                                case "Success":
                                    fetchOwnedStations();
                                    break;
                                case "Fail":
                                    Toast.makeText(AddStation.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(AddStation.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(AddStation.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(stationID));
                params.put("stationName", stationName);
                params.put("stationVicinity", stationAddress);
                params.put("facilities", "WC;Market;CarWash");
                params.put("stationLogo", stationLogo);
                params.put("licenseNo", licenseNo);
                params.put("owner", username);
                params.put("gasolinePrice", String.valueOf(gasolinePrice));
                params.put("dieselPrice", String.valueOf(dieselPrice));
                params.put("lpgPrice", String.valueOf(lpgPrice));
                params.put("electricityPrice", String.valueOf(electricityPrice));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void fetchOwnedStations() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
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
                                    item.setLicenseNo(obj.getString("licenseNo"));
                                    item.setOwner(obj.getString("owner"));
                                    item.setPhotoURL(obj.getString("logoURL"));
                                    item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                    item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                    item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                    item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
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

                                    if (superStationID == 0) {
                                        // This is for the first open
                                        chooseStation(item);
                                    }
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
                params.put("superusername", username);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void chooseStation(StationItem item) {
        superStationID = item.getID();
        prefs.edit().putInt("SuperStationID", superStationID).apply();

        superStationName = item.getStationName();
        prefs.edit().putString("SuperStationName", superStationName).apply();

        superStationAddress = item.getVicinity();
        prefs.edit().putString("SuperStationAddress", superStationAddress).apply();

        superStationCountry = item.getCountryCode();
        prefs.edit().putString("SuperStationCountry", superStationCountry).apply();

        superStationLocation = item.getLocation();
        prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

        superGoogleID = item.getGoogleMapID();
        prefs.edit().putString("SuperGoogleID", superGoogleID).apply();

        superFacilities = item.getFacilities();
        prefs.edit().putString("SuperStationFacilities", superFacilities).apply();

        superStationLogo = item.getPhotoURL();
        prefs.edit().putString("SuperStationLogo", superStationLogo).apply();

        ownedGasolinePrice = item.getGasolinePrice();
        prefs.edit().putFloat("superGasolinePrice", ownedGasolinePrice).apply();

        ownedDieselPrice = item.getDieselPrice();
        prefs.edit().putFloat("superDieselPrice", ownedDieselPrice).apply();

        ownedLPGPrice = item.getLpgPrice();
        prefs.edit().putFloat("superLPGPrice", ownedLPGPrice).apply();

        ownedElectricityPrice = item.getElectricityPrice();
        prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();

        superLicenseNo = item.getLicenseNo();
        prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();

        isStationVerified = item.getIsVerified();
        prefs.edit().putInt("isStationVerified", isStationVerified).apply();

        isMobilePaymentAvailable = item.getHasSupportMobilePayment();
        prefs.edit().putInt("isMobilePaymentAvaiable", isMobilePaymentAvailable).apply();

        superLastUpdate = item.getLastUpdated();
        prefs.edit().putString("SuperLastUpdate", superLastUpdate).apply();

        getSuperVariables(prefs);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
