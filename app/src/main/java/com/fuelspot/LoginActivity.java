package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.fuelspot.superuser.SuperMainActivity;
import com.fuelspot.superuser.SuperWelcomeActivity;
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
import com.yqritc.scalablevideoview.ScalableVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ScalableVideoView background;
    private RelativeLayout notLogged;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private SharedPreferences prefs;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Load background and login layout
        background = findViewById(R.id.animatedBackground);
        notLogged = findViewById(R.id.notLoggedLayout);
        try {
            background.setRawData(R.raw.fuelspot);
            background.setVolume(0f, 0f);
            background.prepareAsync(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    background.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Analytics
        Tracker t = ((Application) this.getApplicationContext()).getDefaultTracker();
        t.setScreenName("LoginActivity");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);

        //Layout objects
        SignInButton signInButton = findViewById(R.id.buttonGoogle);
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
        ImageView doUHaveStation = findViewById(R.id.imageViewSuperUser);
        doUHaveStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SuperWelcomeActivity.class);
                startActivity(i);
                finish();
            }
        });

        arrangeLayouts();
    }

    private void Localization() {
        Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(Double.parseDouble(userlat), Double.parseDouble(userlon), 1);
            if (addresses.size() > 0) {
                //Check the user if s/he trip another country and if s/he is, re-logged him
                if (!userCountry.equals(addresses.get(0).getCountryCode())) {
                    // User has changes his/her country. Fetch the tax rates/unit/currency. Need to re-login
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

    private void arrangeLayouts() {
        if (isSigned) {
            notLogged.setVisibility(View.GONE);

            // Maybe she came from firebase notification. Redirect with url
            if (getIntent().getExtras() != null) {
                String link2 = getIntent().getExtras().getString("URL");
                if (link2 != null && link2.length() > 0) {
                    // Temporary only getting fuelspot.com
                    link2 = link2.replace("fuelspot.com", "fuelspot.com.tr");

                    final Intent intent;
                    if (isSuperUser) {
                        intent = new Intent(LoginActivity.this, SuperMainActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    }

                    intent.putExtra("URL", link2);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(intent);
                            finish();
                        }
                    }, 1250);
                    return;
                }
            }

            //Check user is regular or superUser
            if (isSuperUser) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LoginActivity.this, SuperMainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, 1250);
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
                    }, 1250);
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(intent);
                            finish();
                        }
                    }, 1250);
                }
            }
        }
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

                if (isNetworkConnected(LoginActivity.this)) {
                    register();
                } else {
                    Snackbar.make(background, getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show();
                    prefs.edit().putBoolean("isSigned", false).apply();
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
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        loginButton.setReadPermissions();
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

                            if (isNetworkConnected(LoginActivity.this)) {
                                register();
                            } else {
                                Snackbar.make(background, getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                            }
                        } catch (JSONException e) {
                            Snackbar.make(background, e.toString(), Snackbar.LENGTH_SHORT).show();
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
                Snackbar.make(background, getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
                prefs.edit().putBoolean("isSigned", false).apply();
            }
        });
    }

    private void register() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(LoginActivity.this, getString(R.string.logging_in), getString(R.string.please_wait), false, true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_CREATE_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
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

                                location = obj.getString("location");
                                prefs.edit().putString("Location", location).apply();

                                userCountry = obj.getString("country");
                                prefs.edit().putString("userCountry", userCountry).apply();

                                userDisplayLanguage = obj.getString("language");
                                prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

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
                                Snackbar.make(background, e.toString(), Snackbar.LENGTH_SHORT).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                            }
                        } else {
                            Snackbar.make(background, getString(R.string.error_login_fail), Snackbar.LENGTH_SHORT).show();
                            prefs.edit().putBoolean("isSigned", false).apply();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Snackbar.make(background, volleyError.toString(), Snackbar.LENGTH_SHORT).show();
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

