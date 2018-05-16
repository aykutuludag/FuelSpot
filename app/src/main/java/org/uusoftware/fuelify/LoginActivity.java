package org.uusoftware.fuelify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
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
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import static org.uusoftware.fuelify.MainActivity.birthday;
import static org.uusoftware.fuelify.MainActivity.email;
import static org.uusoftware.fuelify.MainActivity.gender;
import static org.uusoftware.fuelify.MainActivity.getVariables;
import static org.uusoftware.fuelify.MainActivity.isNetworkConnected;
import static org.uusoftware.fuelify.MainActivity.location;
import static org.uusoftware.fuelify.MainActivity.name;
import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.userCountry;
import static org.uusoftware.fuelify.MainActivity.username;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    SharedPreferences prefs;
    boolean isSigned;
    CallbackManager callbackManager;
    LoginButton loginButton;
    int googleSign = 9001;
    ProgressBar pb;
    boolean premium;
    Handler handler;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplicationContext()).getDefaultTracker();
        t.setScreenName("Login");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        isSigned = prefs.getBoolean("isSigned", false);
        getVariables(prefs);

        handler = new Handler();
        intent = new Intent(LoginActivity.this, MainActivity.class);

        //COUNTRY FOR GETTING TAX RATES
        userCountry = Locale.getDefault().getCountry();
        prefs.edit().putString("userCountry", userCountry).apply();

        //Layout objects
        pb = findViewById(R.id.progressBar);
        signInButton = findViewById(R.id.sign_in_button);
        loginButton = findViewById(R.id.login_button);

        //Check whether is logged or not
        if (isSigned) {
            signInButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            pb.setVisibility(View.VISIBLE);

            if (isNetworkConnected(LoginActivity.this) && !premium) {
                //  AudienceNetwork(LoginActivity.this);
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
                }, 2500);
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, 1250);
            }
        } else {
            signInButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }

        /* Google Sign-In */
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

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
        //END

        /* Facebook Login */
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
                                    name = me.optString("first_name");
                                    name += " " + me.optString("last_name");
                                    email = me.optString("email");
                                    photo = me.optString("profile_pic");
                                    gender = me.optString("gender");
                                    location = me.optString("location");

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
                Toast.makeText(LoginActivity.this, getString(R.string.error_login_cancel), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
            }
        });
        //END
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, googleSign);
    }

    private void saveUserInfo() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(LoginActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_CREATE_NEW_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        signInButton.setVisibility(View.INVISIBLE);
                        prefs.edit().putBoolean("isSigned", true).apply();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }, 1500);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(LoginActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == googleSign) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    //GOOGLE ID
                    String googleID = acct.getId();
                    prefs.edit().putString("GoogleAccountID", googleID).apply();

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
                    } else {
                        photo = "";
                        prefs.edit().putString("ProfilePhoto", photo).apply();
                    }

                    // G+
                    Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                    if (person != null) {
                        //GENDER
                        if (person.hasGender()) {
                            if (person.getGender() == 0) {
                                gender = "male";
                                prefs.edit().putString("Gender", gender).apply();
                            } else if (person.getGender() == 1) {
                                gender = "female";
                                prefs.edit().putString("Gender", gender).apply();
                            } else {
                                gender = "transsexual";
                                prefs.edit().putString("Gender", gender).apply();
                            }
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
                    } else {
                        //Default values
                        gender = "";
                        birthday = "";
                        location = "";
                        prefs.edit().putString("Gender", gender).apply();
                        prefs.edit().putString("Birthday", null).apply();
                        prefs.edit().putString("Location", null).apply();
                    }
                    saveUserInfo();
                } else {
                    Toast.makeText(this, getString(R.string.error_login_no_account), Toast.LENGTH_SHORT).show();
                    prefs.edit().putBoolean("isSigned", false).apply();
                }
            } else {
                Toast.makeText(this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
                prefs.edit().putBoolean("isSigned", false).apply();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.error_login_fail), Toast.LENGTH_SHORT).show();
    }
}

