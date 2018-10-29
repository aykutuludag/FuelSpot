package com.fuelspot.superuser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.amirs.JSON;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_ALL;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isSigned;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.stationPhotoChooser;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.AdminMainActivity.getSuperVariables;
import static com.fuelspot.superuser.AdminMainActivity.isMobilePaymentAvailable;
import static com.fuelspot.superuser.AdminMainActivity.isStationVerified;
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

public class AdminWelcome extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    SharedPreferences prefs;
    RequestQueue requestQueue;

    ScrollView welcome2;
    RelativeLayout promoLayout, registerLayout, welcome1, welcome3;
    Button register, continueButton, finishRegistration, finishHowTo;
    MapView mMapView;
    Circle circle;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    CallbackManager callbackManager;
    LoginButton loginButton;
    TextView stationHint;
    EditText editTextStationName, editTextStationAddress, editTextStationLicense, editTextFullName, editTextEmail, editTextPhone, editTextBirthday;
    CircleImageView userPhoto;
    CheckBox termsAndConditions;
    RadioGroup editGender;
    RadioButton bMale, bFemale, bOther;
    int calendarYear, calendarMonth, calendarDay;
    VideoView background;
    FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap googleMap;
    Bitmap bitmap;
    RequestOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_welcome);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getSuperVariables(prefs);
        requestQueue = Volley.newRequestQueue(AdminWelcome.this);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);

        background = findViewById(R.id.animatedAdminBackground);
        promoLayout = findViewById(R.id.layout_promo);
        registerLayout = findViewById(R.id.registerLayout);
        welcome1 = findViewById(R.id.welcome1);
        welcome2 = findViewById(R.id.welcome2);
        welcome3 = findViewById(R.id.welcome3);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
                .build();

        signInButton = findViewById(R.id.googleButton);
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
                ActivityCompat.requestPermissions(AdminWelcome.this, new String[]
                        {PERMISSIONS_STORAGE[0], PERMISSIONS_STORAGE[1], PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[0]}, REQUEST_ALL);
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
                if (tmpusername.length() > 21) {
                    username = tmpusername.substring(0, 20);
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
        loginButton = findViewById(R.id.facebookButton);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    Toast.makeText(AdminWelcome.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                } else {
                                    MainActivity.name = me.optString("first_name") + " " + me.optString("last_name");
                                    prefs.edit().putString("Name", MainActivity.name).apply();

                                    email = me.optString("email");
                                    prefs.edit().putString("Email", email).apply();

                                    photo = me.optString("profile_pic");
                                    prefs.edit().putString("ProfilePhoto", photo).apply();

                                    //USERNAME
                                    String tmpusername = Normalizer.normalize(MainActivity.name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "").replace(" ", "").toLowerCase();
                                    if (tmpusername.length() > 21) {
                                        username = tmpusername.substring(0, 20);
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
                Toast.makeText(AdminWelcome.this, getString(R.string.error_login_cancel), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(AdminWelcome.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(AdminWelcome.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_CREATE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        fetchSuperUserInfo();
                        loading.dismiss();
                        Toast.makeText(AdminWelcome.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                registerLayout.setVisibility(View.GONE);
                                welcome1.setVisibility(View.VISIBLE);
                                background.setVisibility(View.INVISIBLE);
                            }
                        }, 1500);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(AdminWelcome.this, getString(R.string.error_login_fail), Toast.LENGTH_LONG).show();
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

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchSuperUserInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_FETCH),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
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

                                userPhoneNumber = obj.getString("phoneNumber");
                                prefs.edit().putString("userPhoneNumber", userPhoneNumber).apply();

                                userCountry = obj.getString("country");
                                prefs.edit().putString("userCountry", userCountry).apply();

                                userDisplayLanguage = obj.getString("language");
                                prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                                userStations = obj.getString("stations");
                                prefs.edit().putString("userStations", userStations).apply();

                                getVariables(prefs);
                                getSuperVariables(prefs);
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
                params.put("username", username);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchTaxRates() {
        final ProgressDialog loading = ProgressDialog.show(AdminWelcome.this, "Vergi oranları çekiliyor", "Lütfen bekleyiniz...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_TAX),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                MainActivity.TAX_GASOLINE = (float) obj.getDouble("gasolineTax");
                                prefs.edit().putFloat("taxGasoline", MainActivity.TAX_GASOLINE).apply();

                                MainActivity.TAX_DIESEL = (float) obj.getDouble("dieselTax");
                                prefs.edit().putFloat("taxDiesel", MainActivity.TAX_DIESEL).apply();

                                MainActivity.TAX_LPG = (float) obj.getDouble("LPGTax");
                                prefs.edit().putFloat("taxLPG", MainActivity.TAX_LPG).apply();

                                MainActivity.TAX_ELECTRICITY = (float) obj.getDouble("electricityTax");
                                prefs.edit().putFloat("taxElectricity", MainActivity.TAX_ELECTRICITY).apply();

                                getVariables(prefs);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        continueButton.setAlpha(1.0f);
                        continueButton.setClickable(true);
                        loading.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("country", MainActivity.userCountry);

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
                                prefs.edit().putInt("SuperStationID", superStationID).apply();

                                userStations += superStationID + ";";
                                prefs.edit().putString("userStations", userStations).apply();

                                superStationName = obj.getString("name");
                                prefs.edit().putString("SuperStationName", superStationName).apply();

                                superStationCountry = obj.getString("country");
                                prefs.edit().putString("SuperStationCountry", superStationCountry).apply();

                                superStationLocation = obj.getString("location");
                                prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

                                superGoogleID = obj.getString("googleID");
                                prefs.edit().putString("SuperGoogleID", superGoogleID).apply();

                                superLicenseNo = obj.getString("licenseNo");
                                prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();
                                editTextStationLicense.setText(superLicenseNo);

                                superStationLogo = obj.getString("photoURL");
                                prefs.edit().putString("SuperStationLogo", superStationLogo).apply();

                                ownedGasolinePrice = obj.getDouble("gasolinePrice");
                                prefs.edit().putFloat("superGasolinePrice", (float) ownedGasolinePrice).apply();

                                ownedDieselPrice = obj.getDouble("dieselPrice");
                                prefs.edit().putFloat("superDieselPrice", (float) ownedDieselPrice).apply();

                                ownedLPGPrice = obj.getDouble("lpgPrice");
                                prefs.edit().putFloat("superLPGPrice", (float) ownedLPGPrice).apply();

                                ownedElectricityPrice = obj.getDouble("electricityPrice");
                                prefs.edit().putFloat("superElectricityPrice", (float) ownedElectricityPrice).apply();
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
                        Toast.makeText(AdminWelcome.this, res, Toast.LENGTH_LONG).show();
                        if (res != null && res.length() > 0) {
                            switch (res) {
                                case "Success":
                                    // Register process ended redirect user to AdminMainActivity to wait verification process.
                                    Toast.makeText(AdminWelcome.this, "Tüm bilgileriniz kaydedildi.", Toast.LENGTH_SHORT).show();
                                    welcome2.setVisibility(View.GONE);
                                    welcome3.setVisibility(View.VISIBLE);
                                    layout5();
                                    break;
                                case "Fail":
                                    Toast.makeText(AdminWelcome.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(AdminWelcome.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
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
                params.put("isActive", String.valueOf(1));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void updateSuperUser() {
        final ProgressDialog loading = ProgressDialog.show(AdminWelcome.this, "Loading...", "Please wait...", false, false);
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_UPDATE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Toast.makeText(AdminWelcome.this, res, Toast.LENGTH_LONG).show();
                        if (res != null && res.length() > 0) {
                            System.out.println("UPDATESUPERUSER: " + res);
                            switch (res) {
                                case "Success":
                                    updateStation();
                                    break;
                                case "Fail":
                                    Toast.makeText(AdminWelcome.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
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
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                }
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

    private void layout4() {
        /* LAYOUT 04 */
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
                    prefs.edit().putString("SuperStationName", superStationName).apply();
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
                    prefs.edit().putString("SuperStationAddress", superStationAddress).apply();
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
                    prefs.edit().putString("licenseNumbers", superLicenseNo).apply();
                }
            }
        });

        userPhoto = findViewById(R.id.userPhoto);
        Glide.with(this).load(photo).apply(options).into(userPhoto);
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyFilePickerPermission(AdminWelcome.this)) {
                  /*  FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(AdminWelcome.this);
                            */
                    Toast.makeText(AdminWelcome.this, "Geçici olarak deactive edildi.", Toast.LENGTH_LONG).show();
                } else {
                    ActivityCompat.requestPermissions(AdminWelcome.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        editTextFullName = findViewById(R.id.editFullName);
        editTextFullName.setText(name);
        editTextFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    name = s.toString();
                    prefs.edit().putString("Name", name).apply();
                }
            }
        });

        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextBirthday.setText(birthday);
        if (birthday.length() > 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date birthDateasDate = sdf.parse(birthday);
                calendarYear = birthDateasDate.getYear() + 1900;
                calendarMonth = birthDateasDate.getMonth() + 1;
                calendarDay = birthDateasDate.getDate();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Date birthDateasDate = new Date();
            calendarYear = birthDateasDate.getYear() + 1900;
            calendarMonth = birthDateasDate.getMonth() + 1;
            calendarDay = birthDateasDate.getDate();
        }
        editTextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(AdminWelcome.this, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        birthday = pad(dayOfMonth) + "/" + pad(monthOfYear + 1) + "/" + year;
                        editTextBirthday.setText(birthday);
                        prefs.edit().putString("Birthday", birthday).apply();
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

        //  Set gender and retrieve changes
        editGender = findViewById(R.id.radioGroupGender);
        bMale = findViewById(R.id.genderMale);
        bFemale = findViewById(R.id.genderFemale);
        bOther = findViewById(R.id.genderOther);
        switch (gender) {
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
                    gender = "male";
                } else if (checkedId == R.id.genderFemale) {
                    gender = "female";
                } else {
                    gender = "transsexual";
                }
                prefs.edit().putString("gender", gender).apply();
            }
        });

        termsAndConditions = findViewById(R.id.checkBoxTerms);
        termsAndConditions.setText("");
        termsAndConditions.setText(Html.fromHtml(
                "Şartlar ve koşulları okudum, <a href='http://fuel-spot.com/terms-and-conditions'>kabul ediyorum.</a>"));
        termsAndConditions.setClickable(true);
        termsAndConditions.setMovementMethod(LinkMovementMethod.getInstance());


        finishRegistration = findViewById(R.id.finishRegistration);
        finishRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (superStationName != null && superStationName.length() > 0) {
                    if (userPhoneNumber != null && userPhoneNumber.length() > 0) {
                        if (email != null && email.length() > 0) {
                            if (superLicenseNo != null && superLicenseNo.length() > 0) {
                                if (termsAndConditions.isChecked()) {
                                    updateSuperUser();
                                } else {
                                    Toast.makeText(AdminWelcome.this, "Lütfen şartlar ve koşulları onaylayınız.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(AdminWelcome.this, "Lütfen istasyon lisans numarasını giriniz)", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AdminWelcome.this, "Lütfen telefon numaranızı giriniz. Sizinle onay için iletişime geçeceğiz.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AdminWelcome.this, "Lütfen telefon numaranızı giriniz. Sizinle onay için iletişime geçeceğiz.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminWelcome.this, "Kayıt işlemini tamamlayabilmek için istasyonunuzda olmanız gerekmektedir", Toast.LENGTH_LONG).show();
                }
            }
        });
        /* LAYOUT 04 END */
    }

    private void Localization() {
        if (userlat != null && userlon != null) {
            if (userlat.length() > 0 && userlon.length() > 0) {
                Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geo.getFromLocation(Double.parseDouble(userlat), Double.parseDouble(userlon), 1);
                    if (addresses.size() > 0) {
                        userCountry = addresses.get(0).getCountryCode();
                        prefs.edit().putString("userCountry", userCountry).apply();

                        userCountryName = addresses.get(0).getCountryName();
                        prefs.edit().putString("userCountryName", userCountryName).apply();

                        userDisplayLanguage = Locale.getDefault().getDisplayLanguage();
                        prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                        Locale userLocale = new Locale(Locale.getDefault().getISO3Language(), addresses.get(0).getCountryCode());
                        currencyCode = Currency.getInstance(userLocale).getCurrencyCode();
                        prefs.edit().putString("userCurrency", currencyCode).apply();

                        Currency userParaSembolu = Currency.getInstance(currencyCode);
                        currencySymbol = userParaSembolu.getSymbol(userLocale);
                        prefs.edit().putString("userCurrencySymbol", currencySymbol).apply();

                        switch (userCountry) {
                            // US GALLON COUNTRIES
                            case "BZ":
                            case "CO":
                            case "DO":
                            case "EC":
                            case "GT":
                            case "HN":
                            case "HT":
                            case "LR":
                            case "MM":
                            case "NI":
                            case "PE":
                            case "US":
                            case "SV":
                                userUnit = getString(R.string.unitSystem2);
                                break;
                            // IMPERIAL GALLON COUNTRIES
                            case "AI":
                            case "AG":
                            case "BS":
                            case "DM":
                            case "GD":
                            case "KN":
                            case "KY":
                            case "LC":
                            case "MS":
                            case "VC":
                            case "VG":
                                userUnit = getString(R.string.unitSystem3);
                                break;
                            default:
                                // LITRE COUNTRIES. REST OF THE WORLD.
                                userUnit = getString(R.string.unitSystem1);
                                break;
                        }
                        prefs.edit().putString("userUnit", userUnit).apply();
                        fetchTaxRates();
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

    void layout5() {
        isSigned = true;
        prefs.edit().putBoolean("isSigned", isSigned).apply();

        isSuperUser = true;
        prefs.edit().putBoolean("isSuperUser", isSuperUser).apply();

        finishHowTo = findViewById(R.id.continueToMainMenu);
        finishHowTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(AdminWelcome.this, AdminMainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, 1500);
            }
        });
    }

    void fetchStation(final int stationID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            superStationID = obj.getInt("id");
                            prefs.edit().putInt("SuperStationID", superStationID).apply();

                            superStationName = obj.getString("name");
                            prefs.edit().putString("SuperStationName", superStationName).apply();

                            superStationCountry = obj.getString("country");
                            prefs.edit().putString("SuperStationCountry", superStationCountry).apply();

                            superStationLocation = obj.getString("location");
                            prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

                            superGoogleID = obj.getString("googleID");
                            prefs.edit().putString("SuperGoogleID", superGoogleID).apply();

                            superLicenseNo = obj.getString("licenseNo");
                            prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();

                            superStationLogo = obj.getString("photoURL");
                            prefs.edit().putString("SuperStationLogo", superStationLogo).apply();

                            ownedGasolinePrice = obj.getDouble("gasolinePrice");
                            prefs.edit().putFloat("superGasolinePrice", (float) ownedGasolinePrice).apply();

                            ownedDieselPrice = obj.getDouble("dieselPrice");
                            prefs.edit().putFloat("superDieselPrice", (float) ownedDieselPrice).apply();

                            ownedLPGPrice = obj.getDouble("lpgPrice");
                            prefs.edit().putFloat("superLPGPrice", (float) ownedLPGPrice).apply();

                            ownedElectricityPrice = obj.getDouble("electricityPrice");
                            prefs.edit().putFloat("superElectricityPrice", (float) ownedElectricityPrice).apply();

                            isStationVerified = obj.getInt("isVerified");
                            prefs.edit().putInt("isStationVerified", isStationVerified).apply();

                            isMobilePaymentAvailable = obj.getInt("isMobilePaymentAvailable");
                            prefs.edit().putInt("isMobilePaymentAvailable", isMobilePaymentAvailable).apply();

                            isSigned = true;
                            prefs.edit().putBoolean("isSigned", isSigned).apply();
                            isSuperUser = true;
                            prefs.edit().putBoolean("isSuperUser", isSuperUser).apply();

                            getVariables(prefs);
                            getSuperVariables(prefs);

                            Toast.makeText(AdminWelcome.this, "Zaten daha önce hesabınız onaylanmış. FuelSpot Business'a tekrardan hoşgeldiniz!", Toast.LENGTH_LONG).show();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(AdminWelcome.this, AdminMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1500);
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
                params.put("stationID", String.valueOf(stationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
        String fileName = now + ".jpg";

        switch (requestCode) {
            case MainActivity.GOOGLE_LOGIN:
                googleSignIn(data);
                break;
       /*     case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    photo = aq.get(0);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/UserPhotos");
                    folder.mkdirs();

                    UCrop.of(Uri.parse("file://" + photo), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(AdminWelcome.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        Glide.with(this).load(bitmap).apply(options).into(userPhoto);
                        prefs.edit().putString("ProfilePhoto", Environment.getExternalStorageDirectory() + "/FuelSpot/UserPhotos/" + fileName).apply();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(AdminWelcome.this, cropError.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
                */
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ALL: {
                if (ContextCompat.checkSelfPermission(AdminWelcome.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(AdminWelcome.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                    //Request location updates:
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(AdminWelcome.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                MainActivity.userlat = String.valueOf(location.getLatitude());
                                MainActivity.userlon = String.valueOf(location.getLongitude());
                                prefs.edit().putString("lat", MainActivity.userlat).apply();
                                prefs.edit().putString("lon", MainActivity.userlon).apply();

                                Localization();

                                if (userStations != null && userStations.length() > 0) {
                                    // S/he already verified by us. Fetch station and redirect to MainActivity
                                    superStationID = Integer.parseInt(userStations.split(";")[0]);
                                    fetchStation(superStationID);
                                } else {
                                    welcome2.setVisibility(View.VISIBLE);
                                    welcome1.setVisibility(View.GONE);
                                    layout4();
                                }
                            } else {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(5000);
                                mLocationRequest.setFastestInterval(1000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            }
                        }
                    });
                }
                break;
            }
            case REQUEST_STORAGE: {
                if (ActivityCompat.checkSelfPermission(AdminWelcome.this, PERMISSIONS_STORAGE[0]) == PackageManager.PERMISSION_GRANTED) {
              /*    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(AdminWelcome.this); */
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
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

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
        if (background != null) {
            String uriPath = "android.resource://" + getPackageName() + "/" + R.raw.background_login;
            Uri uri = Uri.parse(uriPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                background.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
            }
            background.setVideoURI(uri);
            background.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    background.start();
                }
            });
            background.start();
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
