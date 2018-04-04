package org.uusoftware.fuelify;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.maps.model.LatLng;
import com.kobakei.ratethisapp.RateThisApp;


public class MainActivity extends AppCompatActivity {

    FragmentTransaction transaction;
    Window window;
    Toolbar toolbar;
    boolean doubleBackToExitPressedOnce;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    public static int adCount;
    static InterstitialAd facebookInterstitial;
    static com.google.android.gms.ads.InterstitialAd admobInterstitial;

    //User values
    public static double userlat, userlon;
    public static LatLng mCurrentLocation = new LatLng(userlat, userlon);
    public static String name, email, photo, gender, birthday, location, username, carBrand, carModel;
    public static int fuelPri, fuelSec, kilometer;


    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    //First try to load Audience Network, fails load AdMob
    public static void AudienceNetwork(final Context mContext) {
        if (adCount < 2) {
            facebookInterstitial = new InterstitialAd(mContext, mContext.getString(R.string.interstitial_facebook));
            facebookInterstitial.setAdListener(new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                    // Interstitial displayed callback
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    // Interstitial dismissed callback
                    AudienceNetwork(mContext);
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    // Ad error callback
                    AdMob(mContext);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    // Show the ad when it's done loading.
                }

                @Override
                public void onAdClicked(Ad ad) {
                    // Ad clicked callback
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            facebookInterstitial.loadAd();
        }
    }

    public static void AdMob(final Context mContext) {
        AdRequest adRequest = new AdRequest.Builder().build();
        admobInterstitial = new com.google.android.gms.ads.InterstitialAd(mContext);
        admobInterstitial.setAdUnitId(mContext.getString(R.string.interstitial_admob));
        admobInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AudienceNetwork(mContext);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                AudienceNetwork(mContext);
            }
        });
        admobInterstitial.loadAd(adRequest);
    }

    public static void getVariables(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        name = prefs.getString("Name", "-");
        email = prefs.getString("Email", "-");
        photo = prefs.getString("ProfilePhoto", "http://uusoftware.org/Fuelify/profile.png");
        gender = prefs.getString("Gender", "-");
        birthday = prefs.getString("Birthday", "-");
        location = prefs.getString("Location", "-");
        username = prefs.getString("UserName", "-");
        carBrand = prefs.getString("carBrand", "Acura");
        carModel = prefs.getString("carModel", "RSX");
        fuelPri = prefs.getInt("FuelPrimary", 0);
        fuelSec = prefs.getInt("FuelSecondary", -1);
        kilometer = prefs.getInt("Kilometer", 0);
        userlat = Double.parseDouble(prefs.getString("lat", "0"));
        userlon = Double.parseDouble(prefs.getString("lon", "0"));
        mCurrentLocation = new LatLng(userlat, userlon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addFuel:
                Intent intent = new Intent(MainActivity.this, AddFuel.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    public void coloredBars(int color1, int color2) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color1);
            toolbar.setBackgroundColor(color2);
        } else {
            toolbar.setBackgroundColor(color2);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            navigationView.setCheckedItem(R.id.nav_home);

            // Fragments
            FragmentHome fragment0 = (FragmentHome) getSupportFragmentManager().findFragmentByTag("Home");
            FragmentStations fragment1 = (FragmentStations) getSupportFragmentManager().findFragmentByTag("Stations");
            FragmentProfile fragment2 = (FragmentProfile) getSupportFragmentManager().findFragmentByTag("Profile");
            FragmentNews fragment3 = (FragmentNews) getSupportFragmentManager().findFragmentByTag("News");

            // FragmentHome OnBackPressed
            if (fragment0 != null) {
                if (fragment0.isVisible()) {
                    if (doubleBackToExitPressedOnce) {
                        super.onBackPressed();
                        return;
                    }

                    this.doubleBackToExitPressedOnce = true;
                    Toast.makeText(this, getString(R.string.exit), Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 2000);
                }
            }

            // FragmentSecond OnBackPressed
            if (fragment1 != null) {
                if (fragment1.isVisible()) {
                    Fragment fragment = new FragmentHome();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
                }
            }

            // FragmentThird OnBackPressed
            if (fragment2 != null) {
                if (fragment2.isVisible()) {
                    Fragment fragment = new FragmentHome();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
                }
            }

            // FragmentFourth OnBackPressed
            if (fragment3 != null) {
                if (fragment3.isVisible()) {
                    Fragment fragment = new FragmentHome();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.sorbie);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        getVariables(this);

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer openes as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        //Initializing NavigationView
        navigationView = findViewById(R.id.nav_view);

        //Add Navigation header and its ClickListeners
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_container, new FragmentProfile());
                transaction.commit();
            }
        });

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        menuItem.setChecked(true);
                        Fragment fragment = new FragmentHome();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
                        return true;
                    case R.id.nav_stations:
                        menuItem.setChecked(true);
                        Fragment fragment2 = new FragmentStations();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment2, "Stations").commit();
                        return true;
                    case R.id.nav_profile:
                        menuItem.setChecked(true);
                        Fragment fragment3 = new FragmentProfile();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment3, "Profile").commit();
                        return true;
                    case R.id.nav_news:
                        menuItem.setChecked(true);
                        Fragment fragment4 = new FragmentProfile();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment4, "Profile").commit();
                        return true;
                    case R.id.nav_premium:
                        /*if (!premium) {
                            try {
                                buyPremium();
                            } catch (RemoteException | IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            menuItem.setVisible(false);
                            Toast.makeText(getApplicationContext(), "Zaten daha önce premium sürüme geçmiş yapmışsınız...", Toast.LENGTH_LONG).show();
                        }*/
                        return true;
                    case R.id.nav_puanla:
                        //PUANLA
                        Intent intent4 = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.uusoftware.burclar"));
                        startActivity(intent4);
                        return true;
                    case R.id.nav_support:
                        //PAYLAŞ
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Günlük Burçlar: Astroloji, burç uyumu, yükselen burç ve daha fazlası! https://play.google.com/store/apps/details?id=org.uusoftware.burclar");
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Uygulamayı paylaş"));
                        return true;
                    case R.id.nav_beta:
                        //Beta
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        builder.enableUrlBarHiding();
                        builder.setShowTitle(true);
                        builder.setToolbarColor(Color.parseColor("#212121"));
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://play.google.com/apps/testing/org.uusoftware.burclar"));
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Bir hata oluştu! Lütfen daha sonra tekrar deneyiniz...", Toast.LENGTH_LONG).show();
                        return true;
                }
            }
        });

        if (savedInstanceState == null) {
            Fragment fragment = new FragmentHome();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
    }
}