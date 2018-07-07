package com.fuelspot.superuser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
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
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import eu.amirs.JSON;

public class AdminRegister extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    SharedPreferences prefs;
    RequestQueue requestQueue;

    ScrollView welcome2;
    RelativeLayout promoLayout, registerLayout, welcome1;
    Button register, continueButton, finishRegistration;
    MapView mMapView;
    Circle circle;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    CallbackManager callbackManager;
    LoginButton loginButton;
    TextView textViewStationName, textViewFullName, stationHint, textViewAddress;
    EditText editTextEmail, editTextPhone, editTextBirthday;
    ImageView userPhoto, applicationForm;
    CheckBox termsAndConditions;
    Bitmap bitmap;
    RadioGroup editGender;
    RadioButton bMale, bFemale, bOther;
    int calendarYear, calendarMonth, calendarDay;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        AdminMainActivity.getSuperVariables(prefs);
        requestQueue = Volley.newRequestQueue(AdminRegister.this);

        promoLayout = findViewById(R.id.layout_promo);
        registerLayout = findViewById(R.id.registerLayout);
        welcome1 = findViewById(R.id.welcome1);
        welcome2 = findViewById(R.id.welcome2);

        /* LAYOUT 01 */
        register = findViewById(R.id.buttonRegister);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promoLayout.setVisibility(View.GONE);
                registerLayout.setVisibility(View.VISIBLE);
            }
        });
        /* LAYOUT 01 END */

        /* LAYOUT 02 */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, MainActivity.GOOGLE_LOGIN);
            }
        });

        facebookLogin();
        /* LAYOUT 02 END */

        /* LAYOUT 03 */
        continueButton = findViewById(R.id.button2);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(AdminRegister.this, new String[]
                        {MainActivity.PERMISSIONS_STORAGE[0], MainActivity.PERMISSIONS_STORAGE[1], MainActivity.PERMISSIONS_LOCATION}, MainActivity.UNIFIED_REQUEST);
            }
        });
        /* LAYOUT 03 END */

        /* LAYOUT 04 */
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        /* LAYOUT 04 END */
    }

    private void googleSignIn(Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                //NAME
                MainActivity.name = acct.getDisplayName();
                prefs.edit().putString("Name", MainActivity.name).apply();

                //USERNAME
                String tmpusername = Normalizer.normalize(MainActivity.name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "").replace(" ", "").toLowerCase();
                if (tmpusername.length() > 16) {
                    MainActivity.username = tmpusername.substring(0, 15);
                } else {
                    MainActivity.username = tmpusername;
                }
                prefs.edit().putString("UserName", MainActivity.username).apply();

                //EMAİL
                MainActivity.email = acct.getEmail();
                prefs.edit().putString("Email", MainActivity.email).apply();

                //PHOTO
                if (acct.getPhotoUrl() != null) {
                    MainActivity.photo = acct.getPhotoUrl().toString();
                    prefs.edit().putString("ProfilePhoto", MainActivity.photo).apply();
                }

                // G+
                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                if (person != null) {
                    //GENDER
                    if (person.hasGender()) {
                        if (person.getGender() == 0) {
                            MainActivity.gender = "male";
                        } else if (person.getGender() == 1) {
                            MainActivity.gender = "female";
                        } else {
                            MainActivity.gender = "transsexual";
                        }
                        prefs.edit().putString("Gender", MainActivity.gender).apply();
                    }

                    //BIRTHDAY
                    if (person.hasGender()) {
                        MainActivity.birthday = person.getBirthday();
                        prefs.edit().putString("Birthday", MainActivity.birthday).apply();
                    }

                    //LOCATION
                    if (person.hasCurrentLocation()) {
                        MainActivity.location = person.getCurrentLocation();
                        prefs.edit().putString("Location", MainActivity.location).apply();
                    }
                }
                saveUserInfo();
            } else {
                Toast.makeText(this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                prefs.edit().putBoolean("isSigned", false).apply();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("isSigned", false).apply();
        }
    }

    private void facebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    Toast.makeText(AdminRegister.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                } else {
                                    MainActivity.name = me.optString("first_name") + " " + me.optString("last_name");
                                    prefs.edit().putString("Name", MainActivity.name).apply();

                                    MainActivity.email = me.optString("email");
                                    prefs.edit().putString("Email", MainActivity.email).apply();

                                    MainActivity.photo = me.optString("profile_pic");
                                    prefs.edit().putString("ProfilePhoto", MainActivity.photo).apply();

                                    MainActivity.gender = me.optString("gender");
                                    prefs.edit().putString("Gender", MainActivity.gender).apply();

                                    MainActivity.location = me.optString("location");
                                    prefs.edit().putString("Location", MainActivity.location).apply();

                                    //USERNAME
                                    String tmpusername = Normalizer.normalize(MainActivity.name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "").replace(" ", "").toLowerCase();
                                    if (tmpusername.length() > 16) {
                                        MainActivity.username = tmpusername.substring(0, 15);
                                    } else {
                                        MainActivity.username = tmpusername;
                                    }
                                    prefs.edit().putString("UserName", MainActivity.username).apply();
                                    saveUserInfo();
                                }
                            }
                        }).executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(AdminRegister.this, getString(R.string.error_login_cancel), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(AdminRegister.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(AdminRegister.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_REGISTER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(AdminRegister.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                registerLayout.setVisibility(View.GONE);
                                welcome1.setVisibility(View.VISIBLE);
                                fetchSuperUserInfo();
                            }
                        }, 1500);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(AdminRegister.this, getString(R.string.error_login_fail), Toast.LENGTH_LONG).show();
                        prefs.edit().putBoolean("isSigned", false).apply();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", MainActivity.username);
                params.put("name", MainActivity.name);
                params.put("email", MainActivity.email);
                params.put("photo", MainActivity.photo);
                params.put("gender", MainActivity.gender);
                params.put("birthday", MainActivity.birthday);
                params.put("location", MainActivity.location);
                params.put("country", MainActivity.userCountry);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchSuperUserInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_FETCH_PROFILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            MainActivity.name = obj.getString("name");
                            prefs.edit().putString("Name", MainActivity.name).apply();

                            MainActivity.email = obj.getString("email");
                            prefs.edit().putString("Email", MainActivity.email).apply();

                            MainActivity.photo = obj.getString("photo");
                            prefs.edit().putString("ProfilePhoto", MainActivity.photo).apply();

                            MainActivity.gender = obj.getString("gender");
                            prefs.edit().putString("Gender", MainActivity.gender).apply();

                            MainActivity.birthday = obj.getString("birthday");
                            prefs.edit().putString("Birthday", MainActivity.birthday).apply();

                            AdminMainActivity.userPhoneNumber = obj.getString("userPhone");
                            prefs.edit().putString("userPhoneNumber", AdminMainActivity.userPhoneNumber).apply();

                            AdminMainActivity.superStationID = obj.getInt("stationID");
                            prefs.edit().putInt("SuperStationID", AdminMainActivity.superStationID).apply();

                            AdminMainActivity.superStationName = obj.getString("stationName");
                            prefs.edit().putString("SuperStationName", AdminMainActivity.superStationName).apply();

                            AdminMainActivity.superStationLocation = obj.getString("stationLocation");
                            prefs.edit().putString("SuperStationLocation", AdminMainActivity.superStationLocation).apply();

                            AdminMainActivity.superStationAddress = obj.getString("stationAddress");
                            prefs.edit().putString("SuperStationAddress", AdminMainActivity.superStationAddress).apply();

                            AdminMainActivity.superStationLogo = obj.getString("stationLogo");
                            prefs.edit().putString("SuperStationLogo", AdminMainActivity.superStationLogo).apply();

                            AdminMainActivity.contractPhoto = obj.getString("contractPhoto");
                            prefs.edit().putString("contractPhoto", AdminMainActivity.contractPhoto).apply();

                            AdminMainActivity.isSuperVerified = obj.getInt("isVerified");
                            prefs.edit().putInt("isSuperVerified", AdminMainActivity.isSuperVerified).apply();

                            MainActivity.getVariables(prefs);
                            AdminMainActivity.getSuperVariables(prefs);
                            continueButton.setAlpha(1.0f);
                            continueButton.setClickable(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                params.put("username", MainActivity.username);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void loadMap() {
        //Detect location and set on map
        MapsInitializer.initialize(this.getApplicationContext());
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
                        loc1.setLatitude(MainActivity.userlat);
                        loc1.setLongitude(MainActivity.userlon);

                        Location loc2 = new Location("");
                        loc2.setLatitude(arg0.getLatitude());
                        loc2.setLongitude(arg0.getLongitude());

                        float distanceInMeters = loc1.distanceTo(loc2);

                        if (distanceInMeters >= 10) {
                            MainActivity.userlat = (float) arg0.getLatitude();
                            MainActivity.userlon = (float) arg0.getLongitude();
                            prefs.edit().putFloat("lat", MainActivity.userlat).apply();
                            prefs.edit().putFloat("lon", MainActivity.userlon).apply();
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

        //Draw a circle with radius of 150m
        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(MainActivity.userlat, MainActivity.userlon))
                .radius(1500)
                .strokeColor(Color.RED));

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(MainActivity.userlat, MainActivity.userlon);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(12f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));

        //Search stations in a radius of 75m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + MainActivity.userlat + "," + MainActivity.userlon + "&radius=1000&type=gas_station&opennow=true&key=" + getString(R.string.google_api_key);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (json.key("results").count() > 0) {
                            // Yes! He is in station. Probably there is only one station in 75m  so get the first value
                            String placeID = json.key("results").index(0).key("place_id").stringValue();

                            AdminMainActivity.superStationName = json.key("results").index(0).key("name").stringValue();
                            textViewStationName.setText(AdminMainActivity.superStationName);

                            AdminMainActivity.superStationAddress = json.key("results").index(0).key("vicinity").stringValue();
                            textViewAddress.setText(AdminMainActivity.superStationAddress);

                            double lat = json.key("results").index(0).key("geometry").key("location").key("lat").doubleValue();
                            double lon = json.key("results").index(0).key("geometry").key("location").key("lng").doubleValue();
                            AdminMainActivity.superStationLocation = lat + ";" + lon;

                            LatLng sydney = new LatLng(lat, lon);
                            googleMap.addMarker(new MarkerOptions().position(sydney).title(AdminMainActivity.superStationName).snippet(AdminMainActivity.superStationAddress));

                            stationHint.setTextColor(Color.parseColor("#00801e"));

                            registerOwnedStation(AdminMainActivity.superStationName, AdminMainActivity.superStationAddress, AdminMainActivity.superStationLocation, placeID, MainActivity.stationPhotoChooser(AdminMainActivity.superStationName));
                            fetchOwnedStation(placeID);
                        } else {
                            AdminMainActivity.superStationName = "";
                            AdminMainActivity.superStationAddress = "";
                            AdminMainActivity.superStationLocation = "";
                            AdminMainActivity.superStationLogo = "";

                            textViewStationName.setText(AdminMainActivity.superStationName);
                            textViewAddress.setText(AdminMainActivity.superStationAddress);
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

    private void registerOwnedStation(final String name, final String vicinity, final String location, final String placeID, final String photoURL) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        // Do nothing
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
        requestQueue.add(stringRequest);
    }

    public void fetchOwnedStation(final String placeID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);
                            AdminMainActivity.superStationID = obj.getInt("id");
                            AdminMainActivity.superGoogleID = obj.getString("googleID");
                            AdminMainActivity.superStationName = obj.getString("name");
                            AdminMainActivity.superStationLocation = obj.getString("location");
                            AdminMainActivity.superStationLogo = obj.getString("photoURL");

                            prefs.edit().putInt("SuperStationID", AdminMainActivity.superStationID).apply();
                            prefs.edit().putString("SuperGoogleID", AdminMainActivity.superGoogleID).apply();
                            prefs.edit().putString("SuperStationName", AdminMainActivity.superStationName).apply();
                            prefs.edit().putString("SuperStationLocation", AdminMainActivity.superStationLocation).apply();
                            prefs.edit().putString("SuperStationLogo", AdminMainActivity.superStationLogo).apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                params.put("placeID", placeID);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void updateSuperUser() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_UPDATE_PROFILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        // Register process ended redirect user to AdminMainActivity to wait verification process.
                        MainActivity.isSigned = true;
                        prefs.edit().putBoolean("isSigned", MainActivity.isSigned).apply();
                        MainActivity.isSuperUser = true;
                        prefs.edit().putBoolean("isSuperUser", MainActivity.isSuperUser).apply();
                        Toast.makeText(AdminRegister.this, "Tüm bilgileriniz kaydedildi. Bayi paneline yönlendiriliyorsunuz.", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(AdminRegister.this, AdminMainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }, 1500);
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
                params.put("username", MainActivity.username);
                params.put("email", MainActivity.email);
                params.put("gender", MainActivity.gender);
                params.put("birthday", MainActivity.birthday);
                params.put("phoneNumber", AdminMainActivity.userPhoneNumber);
                params.put("stationID", String.valueOf(AdminMainActivity.superStationID));
                params.put("googleID", AdminMainActivity.superGoogleID);
                params.put("stationName", AdminMainActivity.superStationName);
                params.put("stationLocation", AdminMainActivity.superStationLocation);
                params.put("stationAddress", AdminMainActivity.superStationAddress);
                params.put("stationLogo", AdminMainActivity.superStationLogo);
                if (bitmap != null) {
                    params.put("contractPhoto", getStringImage(bitmap));
                } else {
                    if (AdminMainActivity.contractPhoto != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(AdminRegister.this.getContentResolver(), Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/FuelSpot/License.jpg"));
                            params.put("contractPhoto", getStringImage(bitmap));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void layout4() {
        /* LAYOUT 04 */
        stationHint = findViewById(R.id.stationHint);

        textViewStationName = findViewById(R.id.superStationName);
        textViewStationName.setText(AdminMainActivity.superStationName);

        textViewAddress = findViewById(R.id.superStationAddress);
        textViewAddress.setText(AdminMainActivity.superStationAddress);

        loadMap();

        userPhoto = findViewById(R.id.userPhoto);
        Glide.with(this).load(MainActivity.photo).into(userPhoto);

        textViewFullName = findViewById(R.id.editFullName);
        textViewFullName.setText(MainActivity.name);

        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextBirthday.setText(MainActivity.birthday);
        if (MainActivity.birthday.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY", Locale.getDefault());
            try {
                Date birthDateasDate = sdf.parse(MainActivity.birthday);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(birthDateasDate);
                calendarYear = calendar.get(Calendar.YEAR);
                calendarMonth = calendar.get(Calendar.MONTH) + 1;
                calendarDay = calendar.get(Calendar.DATE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        editTextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(AdminRegister.this, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        MainActivity.birthday = pad(dayOfMonth) + "/" + pad(monthOfYear + 1) + "/" + year;
                        editTextBirthday.setText(MainActivity.birthday);
                        prefs.edit().putString("Birthday", MainActivity.birthday).apply();
                    }
                }, calendarYear, calendarMonth, calendarDay);

                datePicker.setTitle("Bir tarih seçin");
                datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Set", datePicker);
                datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", datePicker);
                datePicker.show();
            }

            private String pad(int number) {
                String returnedValue;
                if (number < 10) {
                    returnedValue = "0" + number;
                } else {
                    returnedValue = String.valueOf(number);
                }
                return returnedValue;
            }
        });

        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPhone.setText(AdminMainActivity.userPhoneNumber);
        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    AdminMainActivity.userPhoneNumber = s.toString();
                    prefs.edit().putString("userPhoneNumber", AdminMainActivity.userPhoneNumber).apply();
                }
            }
        });

        editTextEmail = findViewById(R.id.editTextMail);
        editTextEmail.setText(MainActivity.email);
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    MainActivity.email = s.toString();
                    prefs.edit().putString("Email", MainActivity.email).apply();
                }
            }
        });

        //  Set gender and retrieve changes
        editGender = findViewById(R.id.radioGroupGender);
        bMale = findViewById(R.id.genderMale);
        bFemale = findViewById(R.id.genderFemale);
        bOther = findViewById(R.id.genderOther);
        switch (MainActivity.gender) {
            case "male":
                bMale.setChecked(true);
                break;
            case "female":
                bFemale.setChecked(true);
                break;
            default:
                bOther.setChecked(true);
                break;
        }
        editGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.genderMale) {
                    MainActivity.gender = "male";
                } else if (checkedId == R.id.genderFemale) {
                    MainActivity.gender = "female";
                } else {
                    MainActivity.gender = "transsexual";
                }
                prefs.edit().putString("gender", MainActivity.gender).apply();
            }
        });

        termsAndConditions = findViewById(R.id.checkBoxTerms);
        termsAndConditions.setText("");
        termsAndConditions.setText(Html.fromHtml(
                "<a href='http://fuel-spot.com/terms-and-conditions'>Şartlar ve koşullar</a>" + "ı okudum, kabul ediyorum."));
        termsAndConditions.setClickable(true);
        termsAndConditions.setMovementMethod(LinkMovementMethod.getInstance());

        applicationForm = findViewById(R.id.imageViewLicense);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.photo_placeholder)
                .error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(AdminRegister.this).load(AdminMainActivity.contractPhoto).apply(options).into(applicationForm);
        applicationForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyStoragePermissions(AdminRegister.this)) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(AdminRegister.this);
                } else {
                    ActivityCompat.requestPermissions(AdminRegister.this, MainActivity.PERMISSIONS_STORAGE, MainActivity.REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

        finishRegistration = findViewById(R.id.finishRegistration);
        finishRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AdminMainActivity.superStationName != null && AdminMainActivity.superStationName.length() > 0) {
                    if (AdminMainActivity.userPhoneNumber != null && AdminMainActivity.userPhoneNumber.length() > 0) {
                        if (MainActivity.email != null && MainActivity.email.length() > 0) {
                            if (AdminMainActivity.contractPhoto != null && AdminMainActivity.contractPhoto.length() > 0) {
                                if (termsAndConditions.isChecked()) {
                                    updateSuperUser();
                                } else {
                                    Toast.makeText(AdminRegister.this, "Lütfen şartlar ve koşulları onaylayınız.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(AdminRegister.this, "Lütfen doğrulamak için kullanabileceğimiz bir görsel yükleyiniz. (Örnek: İstayon lisansı, resmi evraklar...)", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AdminRegister.this, "Lütfen telefon numaranızı giriniz. Sizinle onay için iletişime geçeceğiz.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AdminRegister.this, "Lütfen telefon numaranızı giriniz. Sizinle onay için iletişime geçeceğiz.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminRegister.this, "Kayıt işlemini tamamlayabilmek için istasyonunuzda olmanız gerekmektedir", Toast.LENGTH_LONG).show();
                }
            }
        });
        /* LAYOUT 04 END */
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MainActivity.GOOGLE_LOGIN:
                googleSignIn(data);
                break;
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    MainActivity.carPhoto = aq.get(0);

                    System.out.println("file://" + MainActivity.carPhoto);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot");
                    folder.mkdirs();

                    AdminMainActivity.contractPhoto = "http://fuel-spot.com/FUELSPOTAPP/uploads/licenses/" + MainActivity.username + "-License.jpg";
                    UCrop.of(Uri.parse("file://" + MainActivity.carPhoto), Uri.fromFile(new File(folder, "License.jpg")))
                            .withAspectRatio(9, 16)
                            .withMaxResultSize(1080, 1920)
                            .start(AdminRegister.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        Glide.with(this).load(bitmap).into(applicationForm);
                        prefs.edit().putString("License", "file://" + Environment.getExternalStorageDirectory() + "/FuelSpot/License.jpg").apply();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(AdminRegister.this, cropError.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.UNIFIED_REQUEST: {
                if (ContextCompat.checkSelfPermission(AdminRegister.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Request location updates:
                    LocationManager locationManager = (LocationManager)
                            this.getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();

                    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                    if (location != null) {
                        MainActivity.userlat = (float) location.getLatitude();
                        MainActivity.userlon = (float) location.getLongitude();
                        prefs.edit().putFloat("lat", MainActivity.userlat).apply();
                        prefs.edit().putFloat("lon", MainActivity.userlon).apply();
                        MainActivity.getVariables(prefs);
                    }

                    if (AdminMainActivity.isSuperVerified == 1) {
                        Toast.makeText(AdminRegister.this, "Zaten daha önce hesabınız onaylanmış. FuelSpot Business'a tekrardan hoşgeldiniz!", Toast.LENGTH_SHORT).show();
                        updateSuperUser();
                    } else {
                        welcome1.setVisibility(View.GONE);
                        welcome2.setVisibility(View.VISIBLE);

                        layout4();
                    }
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }
}
