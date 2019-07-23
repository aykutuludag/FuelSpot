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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.exifinterface.media.ExifInterface;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fuelspot.LoginActivity;
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.fuelspot.adapter.CompanyAdapter;
import com.fuelspot.adapter.MarkerAdapter;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.StationItem;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.GOOGLE_LOGIN;
import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.getStringImage;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isLocationEnabled;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.isSigned;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.resizeAndRotate;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.getSuperVariables;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;
import static com.fuelspot.superuser.SuperMainActivity.listOfOwnedStations;
import static com.fuelspot.superuser.SuperMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedOtherFuels;
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

public class SuperWelcomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private SharedPreferences prefs;
    private RequestQueue requestQueue;

    private NestedScrollView promoLayout, welcome2;
    private RelativeLayout welcome1;
    private Button continueButton;
    private MapView mMapView;
    private Circle circle;
    private GoogleApiClient mGoogleApiClient;
    private TextView stationHint;
    private Spinner spinner;
    private TextView textViewStationAddress;
    private EditText editTextStationLicense;
    private EditText editTextBirthday;
    private CircleImageView userPhoto;
    private CheckBox termsAndConditions;
    private int calendarYear;
    private int calendarMonth;
    private int calendarDay;
    private FusedLocationProviderClient mFusedLocationClient;
    private Bitmap bitmap;
    private RequestOptions options;
    private ProgressDialog loading0;
    private ProgressDialog loading;
    private GoogleMap googleMap;
    private CallbackManager callbackManager;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    StationItem superStationITEM = new StationItem();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_welcome);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);
        getSuperVariables(prefs);

        requestQueue = Volley.newRequestQueue(SuperWelcomeActivity.this);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        // Initialize map
        MapsInitializer.initialize(this.getApplicationContext());

        promoLayout = findViewById(R.id.layout_promo);
        welcome1 = findViewById(R.id.welcome1);
        welcome2 = findViewById(R.id.welcome2);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                synchronized (this) {
                    super.onLocationResult(locationResult);
                    Location locCurrent = locationResult.getLastLocation();
                    if (locCurrent != null) {
                        Location loc1 = new Location("");
                        loc1.setLatitude(Double.parseDouble(MainActivity.userlat));
                        loc1.setLongitude(Double.parseDouble(MainActivity.userlon));

                        Location loc2 = new Location("");
                        loc2.setLatitude(locCurrent.getLatitude());
                        loc2.setLongitude(locCurrent.getLongitude());

                        float distanceInMeters = loc1.distanceTo(loc2);

                        if (distanceInMeters >= mapDefaultStationRange / 2) {
                            MainActivity.userlat = String.valueOf(locCurrent.getLatitude());
                            MainActivity.userlon = String.valueOf(locCurrent.getLongitude());
                            prefs.edit().putString("lat", MainActivity.userlat).apply();
                            prefs.edit().putString("lon", MainActivity.userlon).apply();
                            updateMapObject();
                        }
                    }
                }
            }
        };

        // ProgressDialogs
        loading0 = new ProgressDialog(SuperWelcomeActivity.this);
        loading0.setTitle(getString(R.string.logging_in));
        loading0.setMessage(getString(R.string.please_wait));
        loading0.setIndeterminate(true);
        loading0.setCancelable(false);

        loading = new ProgressDialog(SuperWelcomeActivity.this);
        loading.setTitle(getString(R.string.profile_updating));
        loading.setMessage(getString(R.string.please_wait));
        loading.setIndeterminate(true);
        loading.setCancelable(false);

        /* LAYOUT 01 */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = findViewById(R.id.googleButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, MainActivity.GOOGLE_LOGIN);
            }
        });

        callbackManager = CallbackManager.Factory.create();
        facebookLogin();
        /* LAYOUT 01 END */

        /* LAYOUT 03 */
        continueButton = findViewById(R.id.button2);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(SuperWelcomeActivity.this, new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
                } else {
                    if (isLocationEnabled(SuperWelcomeActivity.this)) {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(SuperWelcomeActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    MainActivity.userlat = String.valueOf(location.getLatitude());
                                    MainActivity.userlon = String.valueOf(location.getLongitude());
                                    prefs.edit().putString("lat", MainActivity.userlat).apply();
                                    prefs.edit().putString("lon", MainActivity.userlon).apply();
                                    Localization();
                                } else {
                                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                                    Toast.makeText(SuperWelcomeActivity.this, getString(R.string.location_fetching), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SuperWelcomeActivity.this, getString(R.string.location_services_off), Toast.LENGTH_LONG).show();
                    }
                }
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
                //EMAİL
                email = acct.getEmail();
                prefs.edit().putString("Email", email).apply();

                //NAME
                name = acct.getDisplayName();
                if (name != null) {
                    if (name.contains("@")) {
                        name = name.split("@")[0];
                    }
                    prefs.edit().putString("Name", name).apply();

                    //USERNAME
                    String tmp0 = name.toLowerCase();
                    String tmp1 = tmp0.replace("ç", "c").replace("ğ", "g").replace("ı", "i")
                            .replace("ö", "o").replace("ş", "s").replace("ü", "u").replace(" ", "");
                    String tmpusername = tmp1.replaceAll("[^a-zA-Z0-9]", "");
                    if (tmpusername.length() > 30) {
                        username = tmpusername.substring(0, 30);
                    } else {
                        username = tmpusername;
                    }
                    prefs.edit().putString("UserName", username).apply();
                } else {
                    name = email.split("@")[0];
                    username = name;

                    prefs.edit().putString("Name", name).apply();
                    prefs.edit().putString("UserName", username).apply();
                }

                //PHOTO
                if (acct.getPhotoUrl() != null && acct.getPhotoUrl().toString().length() > 0) {
                    photo = acct.getPhotoUrl().toString();
                    prefs.edit().putString("ProfilePhoto", photo).apply();
                }

                if (isNetworkConnected(SuperWelcomeActivity.this)) {
                    saveUserInfo();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show();
                    prefs.edit().putBoolean("isSigned", false).apply();
                }
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
        LoginButton loginButton = findViewById(R.id.facebookButton);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        try {
                            email = me.getString("email");
                            prefs.edit().putString("Email", email).apply();

                            name = me.optString("name");
                            if (name != null) {
                                prefs.edit().putString("Name", name).apply();

                                //USERNAME
                                String tmp0 = name.toLowerCase();
                                String tmp1 = tmp0.replace("ç", "c").replace("ğ", "g").replace("ı", "i")
                                        .replace("ö", "o").replace("ş", "s").replace("ü", "u").replace(" ", "");
                                String tmpusername = tmp1.replaceAll("[^a-zA-Z0-9]", "");
                                if (tmpusername.length() > 30) {
                                    username = tmpusername.substring(0, 30);
                                } else {
                                    username = tmpusername;
                                }
                                prefs.edit().putString("UserName", username).apply();
                                //USERNAME
                            } else {
                                name = email.split("@")[0];
                                username = name;

                                prefs.edit().putString("Name", name).apply();
                                prefs.edit().putString("UserName", username).apply();
                            }

                            String id = me.getString("id");
                            photo = "https://graph.facebook.com/" + id + "/picture?type=normal";
                            prefs.edit().putString("ProfilePhoto", photo).apply();

                            if (isNetworkConnected(SuperWelcomeActivity.this)) {
                                saveUserInfo();
                            } else {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                            }
                        } catch (JSONException e) {
                            Snackbar.make(findViewById(android.R.id.content), e.toString(), Snackbar.LENGTH_SHORT).show();
                            prefs.edit().putBoolean("isSigned", false).apply();
                        }
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // Do nothing
            }

            @Override
            public void onError(FacebookException error) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
                prefs.edit().putBoolean("isSigned", false).apply();
            }
        });
    }

    private void saveUserInfo() {
        loading0.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_CREATE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s != null && s.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(s);
                                JSONObject obj = res.getJSONObject(0);

                                name = obj.getString("name");
                                prefs.edit().putString("Name", name).apply();

                                username = obj.getString("username");
                                prefs.edit().putString("UserName", username).apply();

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

                                token = obj.getString("token");
                                prefs.edit().putString("token", token).apply();

                                Toast.makeText(SuperWelcomeActivity.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                                promoLayout.setVisibility(View.GONE);
                                welcome1.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                Toast.makeText(SuperWelcomeActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                            }
                        } else {
                            Toast.makeText(SuperWelcomeActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_LONG).show();
                            prefs.edit().putBoolean("isSigned", false).apply();
                        }
                        loading0.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading0.dismiss();
                        Toast.makeText(SuperWelcomeActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        prefs.edit().putBoolean("isSigned", false).apply();
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
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                params.put("photo", photo);
                params.put("deviceType", "android");

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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

                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        fetchTaxRates();
                        fetchOwnedStations();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fetchTaxRates() {
        final ProgressDialog loading = ProgressDialog.show(SuperWelcomeActivity.this, getString(R.string.updating_tax), getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_TAX) + "?country=" + userCountry,
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
                        volleyError.toString();
                        continueButton.setAlpha(1.0f);
                        continueButton.setClickable(true);
                        loading.dismiss();
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

    private void fetchOwnedStations() {
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
                                    listOfOwnedStations.add(item);
                                }

                                if (superStationID == 0) {
                                    chooseStation(listOfOwnedStations.get(0));
                                } else {
                                    // User already selected station.
                                    for (int k = 0; k < listOfOwnedStations.size(); k++) {
                                        if (superStationID == listOfOwnedStations.get(k).getID()) {
                                            chooseStation(listOfOwnedStations.get(k));
                                            break;
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                welcome2.setVisibility(View.VISIBLE);
                                welcome1.setVisibility(View.GONE);
                                layout4();
                            }
                        } else {
                            welcome2.setVisibility(View.VISIBLE);
                            welcome1.setVisibility(View.GONE);
                            layout4();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                        welcome2.setVisibility(View.VISIBLE);
                        welcome1.setVisibility(View.GONE);
                        layout4();
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

    private void chooseStation(StationItem item) {
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

        superLicenseNo = item.getLicenseNo();
        prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();

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

        ownedOtherFuels = item.getOtherFuels();
        prefs.edit().putString("superOtherFuels", ownedOtherFuels).apply();

        isStationVerified = item.getIsVerified();
        prefs.edit().putInt("isStationVerified", isStationVerified).apply();

        superLastUpdate = item.getLastUpdated();
        prefs.edit().putString("SuperLastUpdate", superLastUpdate).apply();

        isSigned = true;
        prefs.edit().putBoolean("isSigned", isSigned).apply();

        isSuperUser = true;
        prefs.edit().putBoolean("isSuperUser", isSuperUser).apply();

        Toast.makeText(SuperWelcomeActivity.this, getString(R.string.info_saved_welcome), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(SuperWelcomeActivity.this, SuperMainActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("MissingPermission")
    private void layout4() {
        /* LAYOUT 04 */
        loadMap();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);

        stationHint = findViewById(R.id.stationHint);

        spinner = findViewById(R.id.simpleSpinner);

        textViewStationAddress = findViewById(R.id.superStationAddress);
        textViewStationAddress.setText(superStationAddress);

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

        userPhoto = findViewById(R.id.receiverPhoto);
        Glide.with(this).load(photo).apply(options).into(userPhoto);
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyFilePickerPermission(SuperWelcomeActivity.this)) {
                    ImagePicker.create(SuperWelcomeActivity.this).single().start();
                } else {
                    ActivityCompat.requestPermissions(SuperWelcomeActivity.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        EditText editTextFullName = findViewById(R.id.editFullName);
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
                Calendar cal = Calendar.getInstance();
                cal.setTime(birthDateasDate);
                calendarYear = cal.get(Calendar.YEAR);
                calendarMonth = cal.get(Calendar.MONTH);
                calendarDay = cal.get(Calendar.DAY_OF_WEEK);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Calendar cal = Calendar.getInstance();
            calendarYear = cal.get(Calendar.YEAR);
            calendarMonth = cal.get(Calendar.MONTH);
            calendarDay = cal.get(Calendar.DAY_OF_WEEK);
        }
        editTextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(SuperWelcomeActivity.this, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        birthday = pad(dayOfMonth) + "/" + pad(monthOfYear + 1) + "/" + year;
                        editTextBirthday.setText(birthday);
                        prefs.edit().putString("Birthday", birthday).apply();
                    }
                }, calendarYear, calendarMonth, calendarDay);

                datePicker.setTitle(getString(R.string.pick_your_birthday));
                datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.set), datePicker);
                datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), datePicker);
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

        EditText editTextPhone = findViewById(R.id.editTextPhone);
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

        EditText editTextEmail = findViewById(R.id.editTextMail);
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
        RadioGroup editGender = findViewById(R.id.radioGroupGender);
        RadioButton bMale = findViewById(R.id.genderMale);
        RadioButton bFemale = findViewById(R.id.genderFemale);
        RadioButton bOther = findViewById(R.id.genderOther);
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
        termsAndConditions.setMovementMethod(LinkMovementMethod.getInstance());
        termsAndConditions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = customTabBuilder.build();
                    customTabsIntent.intent.setPackage("com.android.chrome");
                    customTabsIntent.launchUrl(SuperWelcomeActivity.this, Uri.parse("https://fuelspot.com.tr/terms-and-conditions"));
                }
            }
        });

        Button finishRegistration = findViewById(R.id.finishRegistration);
        finishRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (superStationName != null && superStationName.length() > 0) {
                    if (userPhoneNumber != null && userPhoneNumber.length() > 0) {
                        if (email != null && email.length() > 0) {
                            if (superLicenseNo != null && superLicenseNo.length() > 0) {
                                if (termsAndConditions.isChecked()) {
                                    if (isStationVerified == 0) {
                                        superUserRegistration();
                                    } else {
                                        Toast.makeText(SuperWelcomeActivity.this, "Bu istasyon daha önce onaylanmış. Bir hata olduğunu düşünüyorsanız lütfen bizimle iletişime geçiniz.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(SuperWelcomeActivity.this, "Lütfen şartlar ve koşulları onaylayınız.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(SuperWelcomeActivity.this, "Lütfen istasyon lisans numarasını giriniz", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SuperWelcomeActivity.this, "Lütfen telefon numaranızı giriniz. Sizinle onay için iletişime geçeceğiz.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(SuperWelcomeActivity.this, "Lütfen telefon numaranızı giriniz. Sizinle onay için iletişime geçeceğiz.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SuperWelcomeActivity.this, "Kayıt işlemini tamamlayabilmek için istasyonunuzda olmanız gerekmektedir", Toast.LENGTH_LONG).show();
                }
            }
        });
        /* LAYOUT 04 END */
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
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);
                googleMap.setTrafficEnabled(true);

                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        welcome2.requestDisallowInterceptTouchEvent(true);
                    }
                });

                googleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
                    @Override
                    public void onCameraMoveCanceled() {
                        welcome2.requestDisallowInterceptTouchEvent(false);
                    }
                });

                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        welcome2.requestDisallowInterceptTouchEvent(false);
                    }
                });

                MarkerAdapter customInfoWindow = new MarkerAdapter(SuperWelcomeActivity.this);
                googleMap.setInfoWindowAdapter(customInfoWindow);

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
            //Draw a circle with radius of 50m
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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_SEARCH_STATIONS) + "?location=" + userlat + ";" + userlon + "&radius=" + mapDefaultStationRange,
                new Response.Listener<String>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                superStationID = obj.getInt("id");
                                superStationName = obj.getString("name");
                                superStationAddress = obj.getString("vicinity");
                                superStationCountry = obj.getString("country");
                                superStationLocation = obj.getString("location");
                                superGoogleID = obj.getString("googleID");
                                superFacilities = obj.getString("facilities");
                                superStationLogo = obj.getString("logoURL");
                                ownedGasolinePrice = (float) obj.getDouble("gasolinePrice");
                                ownedDieselPrice = (float) obj.getDouble("dieselPrice");
                                ownedLPGPrice = (float) obj.getDouble("lpgPrice");
                                ownedElectricityPrice = (float) obj.getDouble("electricityPrice");
                                ownedOtherFuels = obj.getString("otherFuels");
                                superLicenseNo = obj.getString("licenseNo");
                                isStationVerified = obj.getInt("isVerified");

                                editTextStationLicense.setText(superLicenseNo);

                                superStationITEM.setID(superStationID);
                                superStationITEM.setStationName(superStationName);
                                superStationITEM.setVicinity(superStationAddress);
                                superStationITEM.setCountryCode(superStationCountry);
                                superStationITEM.setLocation(superStationLocation);
                                superStationITEM.setGoogleMapID(superGoogleID);
                                superStationITEM.setFacilities(superFacilities);
                                superStationITEM.setLicenseNo(superLicenseNo);
                                superStationITEM.setPhotoURL(superStationLogo);
                                superStationITEM.setGasolinePrice(ownedGasolinePrice);
                                superStationITEM.setDieselPrice(ownedDieselPrice);
                                superStationITEM.setLpgPrice(ownedLPGPrice);
                                superStationITEM.setElectricityPrice(ownedElectricityPrice);
                                superStationITEM.setOtherFuels(ownedOtherFuels);
                                superStationITEM.setDistance((int) obj.getDouble("distance"));

                                //Station Icon
                                Glide.with(SuperWelcomeActivity.this).load(superStationITEM.getPhotoURL()).listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        superStationITEM.setStationLogoDrawable(resource);
                                        return false;
                                    }
                                });

                                loadStationDetails();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        addMarker();
                                    }
                                }, 1000);
                            } catch (JSONException e) {
                                superStationName = "";
                                superStationAddress = "";
                                superStationLocation = "";
                                superStationCountry = "";
                                superLicenseNo = "";
                                superStationLogo = "";
                                stationHint.setText(getString(R.string.go_to_station_text));
                                stationHint.setTextColor(Color.parseColor("#ff0000"));
                                loadStationDetails();
                                Toast.makeText(SuperWelcomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            superStationName = "";
                            superStationAddress = "";
                            superStationLocation = "";
                            superStationCountry = "";
                            superLicenseNo = "";
                            superStationLogo = "";
                            stationHint.setText(getString(R.string.go_to_station_text));
                            stationHint.setTextColor(Color.parseColor("#ff0000"));
                            loadStationDetails();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        superStationName = "";
                        superStationAddress = "";
                        superStationLocation = "";
                        superStationCountry = "";
                        superLicenseNo = "";
                        superStationLogo = "";
                        stationHint.setText(getString(R.string.go_to_station_text));
                        stationHint.setTextColor(Color.parseColor("#ff0000"));
                        loadStationDetails();

                        Toast.makeText(SuperWelcomeActivity.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
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
        // Add marker
        String[] stationKonum = superStationLocation.split(";");
        LatLng sydney = new LatLng(Double.parseDouble(stationKonum[0]), Double.parseDouble(stationKonum[1]));

        MarkerOptions mOptions;
        if (isStationVerified == 1) {
            mOptions = new MarkerOptions().position(sydney).title(superStationITEM.getStationName()).snippet(superStationITEM.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
        } else {
            mOptions = new MarkerOptions().position(sydney).title(superStationITEM.getStationName()).snippet(superStationITEM.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));
        }
        Marker m = googleMap.addMarker(mOptions);
        m.setTag(superStationITEM);
        m.showInfoWindow();
    }

    private void loadStationDetails() {
        if (isStationVerified == 1) {
            stationHint.setTextColor(Color.parseColor("#ff0000"));
            stationHint.setText("Bu istasyon daha önce onaylanmış. Bir hata olduğunu düşünüyorsanız lütfen bizimle iletişime geçiniz.");
        } else {
            stationHint.setText("Şu anda istasyondasınız!");
            stationHint.setTextColor(Color.parseColor("#2DE778"));
        }

        if (companyList != null && companyList.size() > 0) {
            CompanyAdapter customAdapter = new CompanyAdapter(SuperWelcomeActivity.this, companyList);
            spinner.setEnabled(false);
            spinner.setClickable(false);
            spinner.setAdapter(customAdapter);

            for (int i = 0; i < companyList.size(); i++) {
                if (companyList.get(i).getName().equals(superStationName)) {
                    spinner.setSelection(i, true);
                    break;
                }
            }
        } else {
            // Somehow companList didn't fetch at SuperMainActivity. Fetch it.
            fetchCompanies();
        }


        textViewStationAddress.setText(superStationAddress);
        editTextStationLicense.setFocusable(true);
        editTextStationLicense.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        editTextStationLicense.setText(superLicenseNo);
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

                                CompanyAdapter customAdapter = new CompanyAdapter(SuperWelcomeActivity.this, companyList);
                                spinner.setEnabled(false);
                                spinner.setClickable(false);
                                spinner.setAdapter(customAdapter);

                                for (int i = 0; i < companyList.size(); i++) {
                                    if (companyList.get(i).getName().equals(superStationName)) {
                                        spinner.setSelection(i, true);
                                        break;
                                    }
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
                            if (res.equals("Success")) {
                                prefs.edit().putInt("SuperStationID", superStationID).apply();
                                prefs.edit().putString("SuperStationName", superStationName).apply();
                                prefs.edit().putString("SuperStationAddress", superStationAddress).apply();
                                prefs.edit().putString("SuperStationCountry", superStationCountry).apply();
                                prefs.edit().putString("SuperStationLocation", superStationLocation).apply();
                                prefs.edit().putString("SuperGoogleID", superGoogleID).apply();
                                prefs.edit().putString("SuperStationFacilities", superFacilities).apply();
                                prefs.edit().putString("SuperStationLogo", superStationLogo).apply();
                                prefs.edit().putFloat("superGasolinePrice", ownedGasolinePrice).apply();
                                prefs.edit().putFloat("superDieselPrice", ownedDieselPrice).apply();
                                prefs.edit().putFloat("superLPGPrice", ownedLPGPrice).apply();
                                prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();
                                prefs.edit().putString("superOtherFuels", ownedOtherFuels).apply();
                                prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();
                                prefs.edit().putInt("isStationVerified", isStationVerified).apply();
                                updateSuperUser();
                            } else {
                                loading.dismiss();
                                Toast.makeText(SuperWelcomeActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loading.dismiss();
                            Toast.makeText(SuperWelcomeActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(SuperWelcomeActivity.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
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
                params.put("stationID", String.valueOf(superStationID));
                params.put("licenseNo", superLicenseNo);
                params.put("address", superStationAddress);
                params.put("owner", username);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updateSuperUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_UPDATE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        loading.dismiss();
                        if (res != null && res.length() > 0) {
                            if (res.equals("Success")) {
                                Toast.makeText(SuperWelcomeActivity.this, getString(R.string.info_saved_welcome), Toast.LENGTH_SHORT).show();
                                isSigned = true;
                                prefs.edit().putBoolean("isSigned", isSigned).apply();
                                isSuperUser = true;
                                prefs.edit().putBoolean("isSuperUser", isSuperUser).apply();

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent i = new Intent(SuperWelcomeActivity.this, SuperMainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }, 1250);

                            } else {
                                Toast.makeText(SuperWelcomeActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SuperWelcomeActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(SuperWelcomeActivity.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
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
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                } else {
                    params.put("photo", "");
                }
                params.put("gender", gender);
                params.put("birthday", birthday);
                params.put("phoneNumber", userPhoneNumber);
                params.put("country", userCountry);
                params.put("language", userDisplayLanguage);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (ContextCompat.checkSelfPermission(SuperWelcomeActivity.this, PERMISSIONS_LOCATION[0]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SuperWelcomeActivity.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    if (isLocationEnabled(SuperWelcomeActivity.this)) {
                        mFusedLocationClient.getLastLocation().addOnSuccessListener(SuperWelcomeActivity.this, new OnSuccessListener<Location>() {
                            @SuppressLint("MissingPermission")
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    userlat = String.valueOf(location.getLatitude());
                                    userlon = String.valueOf(location.getLongitude());
                                    prefs.edit().putString("lat", userlat).apply();
                                    prefs.edit().putString("lon", userlon).apply();
                                    Localization();
                                } else {
                                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                                    Toast.makeText(SuperWelcomeActivity.this, getString(R.string.location_fetching), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SuperWelcomeActivity.this, getString(R.string.location_services_off), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_STORAGE: {
                if (ActivityCompat.checkSelfPermission(SuperWelcomeActivity.this, PERMISSIONS_STORAGE[0]) == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.create(SuperWelcomeActivity.this).single().start();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // FACEBOOK
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_LOGIN) {
            googleSignIn(data);
        }

        // Imagepicker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                try {
                    bitmap = BitmapFactory.decodeFile(image.getPath());
                    ExifInterface ei = new ExifInterface(image.getPath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_NORMAL:
                            bitmap = resizeAndRotate(bitmap, 0);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap = resizeAndRotate(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = resizeAndRotate(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = resizeAndRotate(bitmap, 270);
                            break;
                    }
                    Glide.with(this).load(bitmap).apply(options).into(userPhoto);
                    photo = "https://fuelspot.com.tr/uploads/superusers/" + username + ".jpg";
                    prefs.edit().putString("ProfilePhoto", photo).apply();
                } catch (IOException e) {
                    e.printStackTrace();
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
        Intent i = new Intent(SuperWelcomeActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
