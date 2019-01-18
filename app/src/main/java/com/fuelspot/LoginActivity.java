package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.fuelspot.superuser.AdminMainActivity;
import com.fuelspot.superuser.AdminWelcome;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Currency;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.GOOGLE_LOGIN;
import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.isSigned;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.location;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userVehicles;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    VideoView background;
    RelativeLayout notLogged;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    CallbackManager callbackManager;
    LoginButton loginButton;
    SharedPreferences prefs;
    Handler handler = new Handler();
    ImageView doUHaveStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Load background and login layout
        background = findViewById(R.id.animatedBackground);
        notLogged = findViewById(R.id.notLoggedLayout);

        // Analytics
        Tracker t = ((Application) this.getApplicationContext()).getDefaultTracker();
        t.setScreenName("LoginActivity");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        MainActivity.getVariables(prefs);

        //Layout objects
        signInButton = findViewById(R.id.buttonGoogle);
        loginButton = findViewById(R.id.facebookButton);

        if (isSigned) {
            if (userlat != null && userlon != null) {
                if (userlat.length() > 0 && userlon.length() > 0) {
                    Localization();
                }
            }
        }

        /* Google Sign-In */
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
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, GOOGLE_LOGIN);
            }
        });
        //END

        /* Facebook Login */
        facebookLogin();
        //END

        /*StationOwnerRegister */
        doUHaveStation = findViewById(R.id.imageViewSuperUser);
        doUHaveStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, AdminWelcome.class);
                startActivity(i);
            }
        });

    }

    void Localization() {
        Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(Double.parseDouble(userlat), Double.parseDouble(userlon), 1);
            if (addresses.size() > 0) {
                //Check the user if s/he trip another country and if s/he is, re-logged him
                if (!userCountry.equals(addresses.get(0).getCountryCode())) {
                    //User has changes his/her country. Fetch the tax rates/unit/currency.
                    //Language will be automatically changed
                    isSigned = false;
                    prefs.edit().putBoolean("isSigned", false).apply();

                    userCountry = addresses.get(0).getCountryCode();
                    prefs.edit().putString("userCountry", userCountry).apply();

                    userCountryName = addresses.get(0).getCountryName();
                    prefs.edit().putString("userCountryName", userCountryName).apply();

                    userDisplayLanguage = Locale.getDefault().getDisplayLanguage();
                    prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                    Locale userLocale = new Locale(Locale.getDefault().getISO3Language(), addresses.get(0).getCountryCode());
                    currencyCode = Currency.getInstance(userLocale).getCurrencyCode();
                    prefs.edit().putString("userCurrency", currencyCode).apply();

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
                }
            }
        } catch (Exception e) {
            // Do nothing
        }
    }

    void arrangeLayouts() {
        if (isSigned) {
            notLogged.setVisibility(View.GONE);

            //Check user is regular or superUser
            if (isSuperUser) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LoginActivity.this, AdminMainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, 2000);
            } else {
                final Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                // Check user has premium and connected to internet
                if (isNetworkConnected(LoginActivity.this) && !premium) {
                    // AdMob(LoginActivity.this);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(intent);
                            finish();
                        }
                    }, 2000);
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(intent);
                            finish();
                        }
                    }, 2000);
                }
            }
        }
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
                if (tmpusername.length() > 30) {
                    username = tmpusername.substring(0, 30);
                } else {
                    username = tmpusername;
                }
                prefs.edit().putString("UserName", username).apply();

                //EMAİL
                email = acct.getEmail();
                prefs.edit().putString("Email", email).apply();

                //PHOTO
                if (acct.getPhotoUrl() != null && acct.getPhotoUrl().toString().length() > 0) {
                    photo = acct.getPhotoUrl().toString();
                    prefs.edit().putString("ProfilePhoto", photo).apply();
                }

                if (isNetworkConnected(LoginActivity.this)) {
                    register();
                } else {
                    Snackbar.make(background, "İnternet bağlantınızda bir sorun bulunmakta.", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(background, getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
                prefs.edit().putBoolean("isSigned", false).apply();
            }
        } else {
            Snackbar.make(background, getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
            prefs.edit().putBoolean("isSigned", false).apply();
        }
    }

    private void facebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.setReadPermissions();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        System.out.println(response.toString());
                        name = me.optString("name");
                        prefs.edit().putString("Name", name).apply();

                        email = me.optString("email");
                        prefs.edit().putString("Email", email).apply();

                        photo = me.optString("picture");

                                /*if (me.has("picture")) {
                                    photo = me.getJSONObject("picture").getJSONObject("data").getString("url");
                                }*/
                        prefs.edit().putString("ProfilePhoto", photo).apply();

                        //USERNAME
                        String tmpusername = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "").replace(" ", "").toLowerCase();
                        if (tmpusername.length() > 30) {
                            username = tmpusername.substring(0, 30);
                        } else {
                            username = tmpusername;
                        }
                        prefs.edit().putString("UserName", username).apply();

                        if (isNetworkConnected(LoginActivity.this)) {
                            register();
                        } else {
                            Snackbar.make(background, "İnternet bağlantınızda bir sorun bulunmakta.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }).executeAsync();
            }

            @Override
            public void onCancel() {
                // Do nothing
            }

            @Override
            public void onError(FacebookException error) {
                Snackbar.make(background, getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void register() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(LoginActivity.this, "Giriş yapılıyor", "Lütfen bekleyiniz...", false, true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_CREATE_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                loading.dismiss();
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

                                location = obj.getString("location");
                                prefs.edit().putString("Location", location).apply();

                                userCountry = obj.getString("country");
                                prefs.edit().putString("userCountry", userCountry).apply();

                                userDisplayLanguage = obj.getString("language");
                                prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                                userVehicles = obj.getString("vehicles");
                                prefs.edit().putString("userVehicles", userVehicles).apply();

                                getVariables(prefs);

                                loading.dismiss();
                                Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                                notLogged.setVisibility(View.GONE);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }, 2000);
                            } catch (JSONException e) {
                                //Dismissing the progress dialog
                                loading.dismiss();
                                Snackbar.make(background, e.toString(), Snackbar.LENGTH_SHORT).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Snackbar.make(background, getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
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
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (background != null) {
            String uriPath = "android.resource://" + getPackageName() + "/" + R.raw.background_login;
            Uri uri = Uri.parse(uriPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                background.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
            }
            background.setVideoURI(uri);
            background.start();
        }
        arrangeLayouts();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // FACEBOOK
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // GOOGLE
        if (requestCode == GOOGLE_LOGIN) {
            googleSignIn(data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(background, getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
    }
}

