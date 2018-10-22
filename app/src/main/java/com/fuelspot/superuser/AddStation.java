package com.fuelspot.superuser;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
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
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.amirs.JSON;

import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.stationPhotoChooser;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.AdminMainActivity.getSuperVariables;
import static com.fuelspot.superuser.AdminMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.AdminMainActivity.superGoogleID;
import static com.fuelspot.superuser.AdminMainActivity.superLicenseNo;
import static com.fuelspot.superuser.AdminMainActivity.superStationAddress;
import static com.fuelspot.superuser.AdminMainActivity.superStationCountry;
import static com.fuelspot.superuser.AdminMainActivity.superStationID;
import static com.fuelspot.superuser.AdminMainActivity.superStationLocation;
import static com.fuelspot.superuser.AdminMainActivity.superStationLogo;
import static com.fuelspot.superuser.AdminMainActivity.superStationName;
import static com.fuelspot.superuser.AdminMainActivity.userStations;

public class AddStation extends AppCompatActivity {

    Window window;
    Toolbar toolbar;
    TextView stationHint;
    EditText editTextStationName, editTextStationAddress, editTextStationLicense;
    RequestQueue requestQueue;
    MapView mMapView;
    Circle circle;
    GoogleMap googleMap;
    Button finishRegistration;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_station);

        window = this.window;

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
        editTextStationName.setText(superStationName);
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
                    superStationName = s.toString();
                }
            }
        });

        editTextStationAddress = findViewById(R.id.superStationAddress);
        editTextStationAddress.setText(superStationAddress);
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
                    superStationAddress = s.toString();
                }
            }
        });

        editTextStationLicense = findViewById(R.id.editTextLicense);
        editTextStationLicense.setText(superLicenseNo);
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
                    superLicenseNo = s.toString();
                }
            }
        });

        finishRegistration = findViewById(R.id.finishRegistration);
        finishRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (superStationName != null && superStationName.length() > 0) {
                    if (superLicenseNo != null && superLicenseNo.length() > 0) {
                        updateSuperUser();
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
                            getVariables(prefs);
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

        //Draw a circle with radius of 150m
        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(MainActivity.userlat), Double.parseDouble(MainActivity.userlon)))
                .radius(mapDefaultStationRange)
                .strokeColor(Color.RED));

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(Double.parseDouble(MainActivity.userlat), Double.parseDouble(MainActivity.userlon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(17f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));

        //Search stations in a radius of 50m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + MainActivity.userlat + "," + MainActivity.userlon + "&radius=" + mapDefaultStationRange + "&type=gas_station&key=" + getString(R.string.google_api_key);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (json.key("results").count() > 0) {
                            // Yes! He is in station. Probably there is only one station in 50m  so get the first value
                            superGoogleID = json.key("results").index(0).key("place_id").stringValue();

                            superStationName = json.key("results").index(0).key("name").stringValue();
                            editTextStationName.setText(superStationName);

                            superStationAddress = json.key("results").index(0).key("vicinity").stringValue();
                            editTextStationAddress.setText(superStationAddress);

                            double lat = json.key("results").index(0).key("geometry").key("location").key("lat").doubleValue();
                            double lon = json.key("results").index(0).key("geometry").key("location").key("lng").doubleValue();
                            superStationLocation = lat + ";" + lon;

                            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> addresses = geo.getFromLocation(lat, lon, 1);
                                if (addresses.size() > 0) {
                                    superStationCountry = addresses.get(0).getCountryCode();
                                } else {
                                    superStationCountry = "";
                                }
                            } catch (Exception e) {
                                superStationCountry = "";
                            }

                            LatLng sydney = new LatLng(lat, lon);
                            googleMap.addMarker(new MarkerOptions().position(sydney).title(superStationName).snippet(superStationAddress));

                            stationHint.setTextColor(Color.parseColor("#00801e"));

                            addStation();
                        } else {
                            superStationName = "";
                            superStationAddress = "";
                            superStationLocation = "";
                            superStationCountry = "";
                            superLicenseNo = "";
                            superStationLogo = "";

                            editTextStationName.setText(superStationName);
                            editTextStationAddress.setText(superStationAddress);
                            editTextStationLicense.setText(superLicenseNo);
                            stationHint.setTextColor(Color.parseColor("#ff0000"));
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    private void addStation() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s != null && s.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(s);
                                JSONObject obj = res.getJSONObject(0);

                                superStationID = obj.getInt("id");

                                userStations += superStationID + ";";

                                superStationName = obj.getString("name");

                                superStationCountry = obj.getString("country");

                                superStationLocation = obj.getString("location");

                                superGoogleID = obj.getString("googleID");

                                superLicenseNo = obj.getString("licenseNo");
                                editTextStationLicense.setText(superLicenseNo);

                                superStationLogo = obj.getString("photoURL");

                                ownedGasolinePrice = obj.getDouble("gasolinePrice");

                                ownedDieselPrice = obj.getDouble("dieselPrice");

                                ownedLPGPrice = obj.getDouble("lpgPrice");

                                ownedElectricityPrice = obj.getDouble("electricityPrice");
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
                params.put("name", superStationName);
                params.put("vicinity", superStationAddress);
                params.put("country", superStationCountry);
                params.put("location", superStationLocation);
                params.put("googleID", superGoogleID);
                params.put("photoURL", stationPhotoChooser(superStationName));

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
                                    // Register process ended redirect user to AdminMainActivity to wait verification process.
                                    Toast.makeText(AddStation.this, "Tüm bilgileriniz kaydedildi.", Toast.LENGTH_SHORT).show();
                                    finish();
                                    break;
                                case "Fail":
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
                params.put("stationID", String.valueOf(superStationID));
                params.put("stationName", superStationName);
                params.put("stationVicinity", superStationAddress);
                params.put("licenseNo", superLicenseNo);
                params.put("owner", username);
                params.put("gasolinePrice", String.valueOf(ownedGasolinePrice));
                params.put("dieselPrice", String.valueOf(ownedDieselPrice));
                params.put("lpgPrice", String.valueOf(ownedLPGPrice));
                params.put("electricityPrice", String.valueOf(ownedElectricityPrice));


                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void updateSuperUser() {
        final ProgressDialog loading = ProgressDialog.show(AddStation.this, "Loading...", "Please wait...", false, false);
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_UPDATE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (res != null && res.length() > 0) {
                            switch (res) {
                                case "Success":
                                    updateStation();
                                    break;
                                case "Fail":
                                    Toast.makeText(AddStation.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            loading.dismiss();
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
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                params.put("gender", gender);
                params.put("birthday", birthday);
                params.put("phoneNumber", userPhoneNumber);
                params.put("country", userCountry);
                params.put("language", userDisplayLanguage);
                params.put("stationIDs", userStations);

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
