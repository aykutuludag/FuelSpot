package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
import com.fuelspot.superuser.AdminRegister;
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
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONObject;

import java.text.Normalizer;
import java.util.Currency;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    RelativeLayout notLogged;
    VideoView background;

    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    int googleSign = 9001;

    CallbackManager callbackManager;
    LoginButton loginButton;

    SharedPreferences prefs;

    Handler handler;
    Intent intent;

    ImageView doUHaveStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Load background and login layout
        background = findViewById(R.id.videoViewBackground);
        notLogged = findViewById(R.id.notLoggedLayout);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplicationContext()).getDefaultTracker();
        t.setScreenName("Giriş");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        MainActivity.getVariables(prefs);

        handler = new Handler();
        intent = new Intent(LoginActivity.this, MainActivity.class);

        //Layout objects
        signInButton = findViewById(R.id.buttonGoogle);
        loginButton = findViewById(R.id.facebookButton);

        //Check the user trip another country and re-logged him
        if (!MainActivity.userCountry.equals(Locale.getDefault().getCountry())) {
            //User has changes his/her country. Fetch the tax rates/unit/currency.
            //Language will be automatically changed
            MainActivity.isSigned = false;
            prefs.edit().putBoolean("isSigned", false).apply();

            MainActivity.userCountry = Locale.getDefault().getCountry();
            prefs.edit().putString("userCountry", MainActivity.userCountry).apply();

            MainActivity.userCountryName = Locale.getDefault().getDisplayCountry();
            prefs.edit().putString("userCountryName", MainActivity.userCountryName).apply();

            MainActivity.userDisplayLanguage = Locale.getDefault().getDisplayLanguage();
            prefs.edit().putString("userLanguage", MainActivity.userDisplayLanguage).apply();

            MainActivity.currencyCode = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
            prefs.edit().putString("userCurrency", MainActivity.currencyCode).apply();

            if (MainActivity.userCountry.equals("US") || MainActivity.userCountry.equals("LR") || MainActivity.userCountry.equals("MM")) {
                MainActivity.userUnit = "US Customary";
            } else {
                MainActivity.userUnit = "Metric system";
            }
            prefs.edit().putString("userUnit", MainActivity.userUnit).apply();
        }

        //Check whether is logged or not
        arrangeLayouts();

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
                .addApi(Plus.API)
                .build();
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, googleSign);
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
                Intent i = new Intent(LoginActivity.this, AdminRegister.class);
                startActivity(i);
                finish();
            }
        });
    }

    void arrangeLayouts() {
        if (MainActivity.isSigned) {
            notLogged.setVisibility(View.GONE);
            //Check user is regular or superUser
            if (MainActivity.isSuperUser) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LoginActivity.this, AdminMainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, 3000);
            } else {
                // Check user has premium and connected to internet
                if (MainActivity.isNetworkConnected(LoginActivity.this) && !MainActivity.premium) {
                    // AudienceNetwork(LoginActivity.this);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (MainActivity.facebookInterstitial != null && MainActivity.facebookInterstitial.isAdLoaded()) {
                                //Facebook ads loaded he will see Facebook
                                startActivity(intent);
                                MainActivity.facebookInterstitial.show();
                                MainActivity.adCount++;
                                MainActivity.facebookInterstitial = null;
                            } else if (MainActivity.admobInterstitial != null && MainActivity.admobInterstitial.isLoaded()) {
                                //Facebook ads doesnt loaded he will see AdMob
                                startActivity(intent);
                                MainActivity.admobInterstitial.show();
                                MainActivity.adCount++;
                                MainActivity.admobInterstitial = null;
                            } else {
                                //Both ads doesn't loaded.
                                startActivity(intent);
                            }
                            finish();
                        }
                    }, 3000);
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }, 3000);
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
                if (tmpusername.length() > 21) {
                    MainActivity.username = tmpusername.substring(0, 20);
                } else {
                    MainActivity.username = tmpusername;
                }
                prefs.edit().putString("UserName", MainActivity.username).apply();

                //EMAİL
                MainActivity.email = acct.getEmail();
                prefs.edit().putString("Email", MainActivity.email).apply();

                //PHOTO
                if (acct.getPhotoUrl() != null && acct.getPhotoUrl().toString().length() > 0) {
                    MainActivity.photo = acct.getPhotoUrl().toString();
                    prefs.edit().putString("ProfilePhoto", MainActivity.photo).apply();
                }

                // G+
                if (mGoogleApiClient.isConnected()) {
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
        } else {
            Toast.makeText(this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("isSigned", false).apply();
        }
    }

    private void facebookLogin() {
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    Toast.makeText(LoginActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
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
                                    if (tmpusername.length() > 21) {
                                        MainActivity.username = tmpusername.substring(0, 20);
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
                Toast.makeText(LoginActivity.this, getString(R.string.error_login_cancel), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(LoginActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REGISTER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                        notLogged.setVisibility(View.GONE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
                                startActivity(i);
                            }
                        }, 2000);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(LoginActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_LONG).show();
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

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (background != null) {
            background.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background));
            background.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    background.start();
                }
            });
            background.start();
        }
        arrangeLayouts();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == googleSign) {
            googleSignIn(data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
    }
}

