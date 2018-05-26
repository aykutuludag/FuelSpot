package org.uusoftware.fuelify;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.android.gms.maps.model.Marker;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import eu.amirs.JSON;

import static org.uusoftware.fuelify.AdminMainActivity.contractPhoto;
import static org.uusoftware.fuelify.AdminMainActivity.getSuperVariables;
import static org.uusoftware.fuelify.AdminMainActivity.isSuperVerified;
import static org.uusoftware.fuelify.AdminMainActivity.superStationID;
import static org.uusoftware.fuelify.AdminMainActivity.superStationLocation;
import static org.uusoftware.fuelify.AdminMainActivity.superStationLogo;
import static org.uusoftware.fuelify.AdminMainActivity.superStationName;
import static org.uusoftware.fuelify.AdminMainActivity.userPhoneNumber;
import static org.uusoftware.fuelify.MainActivity.GOOGLE_LOGIN;
import static org.uusoftware.fuelify.MainActivity.PERMISSIONS_LOCATION;
import static org.uusoftware.fuelify.MainActivity.PERMISSIONS_STORAGE;
import static org.uusoftware.fuelify.MainActivity.REQUEST_EXTERNAL_STORAGE;
import static org.uusoftware.fuelify.MainActivity.UNIFIED_REQUEST;
import static org.uusoftware.fuelify.MainActivity.birthday;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.email;
import static org.uusoftware.fuelify.MainActivity.gender;
import static org.uusoftware.fuelify.MainActivity.getVariables;
import static org.uusoftware.fuelify.MainActivity.isSigned;
import static org.uusoftware.fuelify.MainActivity.location;
import static org.uusoftware.fuelify.MainActivity.name;
import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.userCountry;
import static org.uusoftware.fuelify.MainActivity.userlat;
import static org.uusoftware.fuelify.MainActivity.userlon;
import static org.uusoftware.fuelify.MainActivity.username;
import static org.uusoftware.fuelify.MainActivity.verifyStoragePermissions;

public class AdminRegister extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    SharedPreferences prefs;
    RequestQueue requestQueue;

    VideoView background;
    ScrollView welcome2;
    RelativeLayout promoLayout, registerLayout, welcome1;
    Button register, continueButton, finishRegistration;
    MapView mMapView;
    Circle circle;
    Marker marker;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    CallbackManager callbackManager;
    LoginButton loginButton;
    TextView textViewStationName, textViewFullName;
    EditText editTextEmail, editTextPhone;
    ImageView applicationForm;
    CheckBox termsAndConditions;
    Bitmap bitmap;
    String licenseURI;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getSuperVariables(prefs);
        requestQueue = Volley.newRequestQueue(AdminRegister.this);

        //Load background and login layout
        background = findViewById(R.id.videoViewBackground);
        background.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background));
        background.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                background.start();
            }
        });
        background.start();

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
                startActivityForResult(signInIntent, GOOGLE_LOGIN);
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
                        {PERMISSIONS_STORAGE[0], PERMISSIONS_STORAGE[1], PERMISSIONS_LOCATION}, UNIFIED_REQUEST);
            }
        });
        /* LAYOUT 03 END */

        /* LAYOUT 04 */
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        textViewStationName = findViewById(R.id.superStationName);
        textViewStationName.setText(superStationName);

        textViewFullName = findViewById(R.id.superFullName);
        textViewFullName.setText(name);

        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPhone.setText(userPhoneNumber);
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
                    userPhoneNumber = s.toString();
                    prefs.edit().putString("userPhoneNumber", userPhoneNumber).apply();
                }
            }
        });

        editTextEmail = findViewById(R.id.editTextMail);
        editTextEmail.setText(email);
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
                    email = s.toString();
                    prefs.edit().putString("Email", email).apply();
                }
            }
        });

        termsAndConditions = findViewById(R.id.checkBoxTerms);
        termsAndConditions.setText("");
        termsAndConditions.setText(Html.fromHtml("I have read and agree to the " +
                "<a href='http://fuel-spot.com/terms-and-conditions'>TERMS AND CONDITIONS</a>"));
        termsAndConditions.setClickable(true);
        termsAndConditions.setMovementMethod(LinkMovementMethod.getInstance());

        applicationForm = findViewById(R.id.imageViewLicense);
        applicationForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyStoragePermissions(AdminRegister.this)) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(AdminRegister.this);
                } else {
                    ActivityCompat.requestPermissions(AdminRegister.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

        finishRegistration = findViewById(R.id.finishRegistration);
        finishRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (superStationName != null && superStationName.length() > 0) {
                    if (userPhoneNumber != null && userPhoneNumber.length() > 0) {
                        if (email != null && email.length() > 0) {
                            if (licenseURI != null && licenseURI.length() > 0) {
                                updateSuperUser();
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
                    Toast.makeText(AdminRegister.this, "Kayıt işlemini tamamlamak için lütfen istasyonunuza gidiniz.", Toast.LENGTH_LONG).show();
                }
            }
        });
        /* LAYOUT 04 END */
    }

    private void googleSignIn(Intent data) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                //NAME
                name = acct.getDisplayName();
                prefs.edit().putString("Name", name).apply();

                //USERNAME
                String tmpusername = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "").replace(" ", "").toLowerCase();
                if (tmpusername.length() > 16) {
                    username = tmpusername.substring(0, 15);
                } else {
                    username = tmpusername;
                }
                prefs.edit().putString("UserName", username).apply();

                //EMAİL
                email = acct.getEmail();
                prefs.edit().putString("Email", email).apply();

                //PHOTO
                if (acct.getPhotoUrl() != null) {
                    photo = acct.getPhotoUrl().toString();
                    prefs.edit().putString("ProfilePhoto", photo).apply();
                }

                // G+
                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                if (person != null) {
                    //GENDER
                    if (person.hasGender()) {
                        if (person.getGender() == 0) {
                            gender = "male";
                        } else if (person.getGender() == 1) {
                            gender = "female";
                        } else {
                            gender = "transsexual";
                        }
                        prefs.edit().putString("Gender", gender).apply();
                    }

                    //BIRTHDAY
                    if (person.hasGender()) {
                        birthday = person.getBirthday();
                        prefs.edit().putString("Birthday", birthday).apply();
                    }

                    //LOCATION
                    if (person.hasCurrentLocation()) {
                        location = person.getCurrentLocation();
                        prefs.edit().putString("Location", location).apply();
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
                                    name = me.optString("first_name") + " " + me.optString("last_name");
                                    prefs.edit().putString("Name", name).apply();

                                    email = me.optString("email");
                                    prefs.edit().putString("Email", email).apply();

                                    photo = me.optString("profile_pic");
                                    prefs.edit().putString("ProfilePhoto", photo).apply();

                                    gender = me.optString("gender");
                                    prefs.edit().putString("Gender", gender).apply();

                                    location = me.optString("location");
                                    prefs.edit().putString("Location", location).apply();

                                    //USERNAME
                                    String tmpusername = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "").replace(" ", "").toLowerCase();
                                    if (tmpusername.length() > 16) {
                                        username = tmpusername.substring(0, 15);
                                    } else {
                                        username = tmpusername;
                                    }
                                    prefs.edit().putString("UserName", username).apply();
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_CREATE_NEW_SUPERUSER),
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
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                params.put("photo", photo);
                params.put("gender", gender);
                params.put("birthday", birthday);
                params.put("location", location);
                params.put("country", userCountry);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchSuperUserInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_SUPERUSER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            name = obj.getString("name");
                            prefs.edit().putString("Name", name).apply();

                            email = obj.getString("email");
                            prefs.edit().putString("Email", email).apply();

                            photo = obj.getString("photo");
                            prefs.edit().putString("ProfilePhoto", photo).apply();

                            gender = obj.getString("gender");
                            prefs.edit().putString("Gender", gender).apply();

                            birthday = obj.getString("birthday");
                            prefs.edit().putString("Birthday", birthday).apply();

                            location = obj.getString("location");
                            prefs.edit().putString("Location", location).apply();

                            superStationID = obj.getInt("stationID");
                            prefs.edit().putInt("SuperStationID", superStationID).apply();

                            superStationName = obj.getString("stationName");
                            prefs.edit().putString("SuperStationName", superStationName).apply();

                            superStationLocation = obj.getString("stationLocation");
                            prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

                            userPhoneNumber = obj.getString("userPhone");
                            prefs.edit().putString("userPhone", userPhoneNumber).apply();

                            isSuperVerified = obj.getBoolean("isVerified");
                            prefs.edit().putBoolean("isSuperVerified", isSuperVerified).apply();

                            contractPhoto = obj.getString("contractPhoto");
                            prefs.edit().putString("contractPhoto", contractPhoto).apply();

                            prefs.edit().putInt("SuperStationID", superStationID).apply();
                            prefs.edit().putString("SuperStationName", superStationName).apply();
                            prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

                            getSuperVariables(prefs);
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
                params.put("username", username);

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
                        loc1.setLatitude(userlat);
                        loc1.setLongitude(userlon);

                        Location loc2 = new Location("");
                        loc2.setLatitude(arg0.getLatitude());
                        loc2.setLongitude(arg0.getLongitude());

                        float distanceInMeters = loc1.distanceTo(loc2);

                        if (distanceInMeters >= 100) {
                            userlat = (float) arg0.getLatitude();
                            userlon = (float) arg0.getLongitude();
                            prefs.edit().putFloat("lat", userlat).apply();
                            prefs.edit().putFloat("lon", userlon).apply();
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
                .center(new LatLng(userlat, userlon))
                .radius(150)
                .strokeColor(Color.RED));

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(userlat, userlon);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(15f).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
                (cameraPosition));

        //Search stations in a radius of 3500m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=150&type=gas_station&opennow=true&key=AIzaSyAOE5dwDvW_IOVmw-Plp9y5FLD9_1qb4vc";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);

                        superStationName = json.key("results").index(0).key("name").stringValue();
                        String vicinity = json.key("results").index(0).key("vicinity").stringValue();
                        String placeID = json.key("results").index(0).key("place_id").stringValue();

                        double lat = json.key("results").index(0).key("geometry").key("location").key("lat").doubleValue();
                        double lon = json.key("results").index(0).key("geometry").key("location").key("lng").doubleValue();
                        superStationLocation = lat + ";" + lon;

                        LatLng sydney = new LatLng(lat, lon);
                        marker = googleMap.addMarker(new MarkerOptions().position(sydney).title(superStationName).snippet(vicinity));

                        registerOwnedStation(superStationName, vicinity, superStationLocation, placeID, stationPhotoChooser(superStationName));
                        fetchOwnedStation(placeID);

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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REGISTER_STATION),
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION_PRICES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);
                            superStationID = obj.getInt("id");
                            superStationName = obj.getString("name");
                            superStationLocation = obj.getString("location");
                            superStationLogo = obj.getString("photoURL");

                            prefs.edit().putInt("SuperStationID", superStationID).apply();
                            prefs.edit().putString("SuperStationName", superStationName).apply();
                            prefs.edit().putString("SuperStationLocation", superStationLocation).apply();
                            prefs.edit().putString("SuperStationLogo", superStationLogo).apply();
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_SUPERUSER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        // Register process ended redirect user to AdminMainActivity to wait verification process.
                        isSigned = true;
                        prefs.edit().putBoolean("isSigned", isSigned).apply();
                        Intent i = new Intent(AdminRegister.this, AdminMainActivity.class);
                        startActivity(i);
                        finish();
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
                params.put("email", email);
                params.put("photo", photo);
                params.put("birthday", birthday);
                params.put("stationID", String.valueOf(superStationID));
                params.put("stationName", superStationName);
                params.put("stationLocation", superStationLocation);
                params.put("stationLogo", superStationLogo);
                params.put("phoneNumber", userPhoneNumber);
                params.put("contractPhoto", getStringImage(bitmap));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private String stationPhotoChooser(String stationName) {
        String photoURL;
        if (stationName.contains("Shell")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/shell.png";
        } else if (stationName.contains("Opet")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/opet.jpg";
        } else if (stationName.contains("BP")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/bp.png";
        } else if (stationName.contains("Kadoil")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kadoil.jpg";
        } else if (stationName.contains("Petrol Ofisi")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/petrol-ofisi.png";
        } else if (stationName.contains("Lukoil")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/lukoil.jpg";
        } else {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/unknown.png";
        }
        return photoURL;
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

        CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
        String fileName = now + ".jpg";

        switch (requestCode) {
            case GOOGLE_LOGIN:
                googleSignIn(data);
                break;
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    carPhoto = aq.get(0);

                    System.out.println("file://" + carPhoto);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot");
                    folder.mkdirs();

                    UCrop.of(Uri.parse("file://" + carPhoto), Uri.fromFile(new File(folder, fileName)))
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
                        prefs.edit().putString("CarPhoto", "file://" + Environment.getExternalStorageDirectory() + "/FuelSpot" + fileName).apply();
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
            case UNIFIED_REQUEST: {
                if (ContextCompat.checkSelfPermission(AdminRegister.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Request location updates:
                    LocationManager locationManager = (LocationManager)
                            this.getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();

                    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                    if (location != null) {
                        userlat = (float) location.getLatitude();
                        userlon = (float) location.getLongitude();
                        prefs.edit().putFloat("lat", userlat).apply();
                        prefs.edit().putFloat("lon", userlon).apply();
                        getVariables(prefs);
                    }

                    welcome1.setVisibility(View.GONE);
                    welcome2.setVisibility(View.VISIBLE);
                    loadMap();
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
