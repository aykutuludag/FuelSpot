package org.uusoftware.fuelify;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.kobakei.ratethisapp.RateThisApp;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    Window window;
    Toolbar toolbar;
    boolean doubleBackToExitPressedOnce;
    SharedPreferences prefs;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    public static int adCount;
    static InterstitialAd facebookInterstitial;
    static com.google.android.gms.ads.InterstitialAd admobInterstitial;
    int openCount;

    //User values
    public static boolean premium;
    public static double userlat, userlon;
    public static String name, email, photo, carPhoto, gender, birthday, location, username, carBrand, carModel;
    public static int fuelPri, fuelSec, kilometer;

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

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        InAppBilling();
        getVariables(prefs);
        buyPremiumPopup();

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

        ImageView userPhotoHolder = headerView.findViewById(R.id.profile_image);
        Picasso.with(this).load(Uri.parse(photo)).error(R.drawable.empty).placeholder(R.drawable.empty)
                .into(userPhotoHolder);

        TextView userName = headerView.findViewById(R.id.textViewName);
        userName.setText(name);

        TextView carName = headerView.findViewById(R.id.textViewCarName);
        String carText = carBrand + " " + carModel;
        carName.setText(carText);

        ImageView purchases = headerView.findViewById(R.id.imageViewPurchases);
        purchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PurchasesActivity.class);
                startActivity(intent);
            }
        });

        ImageView settings = headerView.findViewById(R.id.imageViewSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        ImageView help = headerView.findViewById(R.id.imageViewHelp);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                navigationView.setCheckedItem(R.id.nav_profile);
                Fragment fragment3 = new FragmentProfile();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment3, "Profile").commit();
            }
        });

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();
                menuItem.setChecked(true);
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        Fragment fragment = new FragmentHome();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
                        return true;
                    case R.id.nav_vehicle:
                        Fragment fragmentVehicle = new FragmentVehicle();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragmentVehicle, "Vehicle").commit();
                        return true;
                    case R.id.nav_stations:
                        Fragment fragment2 = new FragmentStations();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment2, "Stations").commit();
                        return true;
                    case R.id.nav_profile:
                        Fragment fragment3 = new FragmentProfile();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment3, "Profile").commit();
                        return true;
                    case R.id.nav_news:
                        Fragment fragment4 = new FragmentNews();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment4, "News").commit();
                        return true;
                    case R.id.nav_premium:
                        if (!premium) {
                            try {
                                buyPremium();
                            } catch (RemoteException | IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        } else {
                            menuItem.setVisible(false);
                            Toast.makeText(getApplicationContext(), "Zaten daha önce premium sürüme geçmiş yapmışsınız...", Toast.LENGTH_LONG).show();
                        }
                        return true;
                    case R.id.nav_puanla:
                        //PUANLA
                        Intent intent4 = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.uusoftware.fuelify"));
                        startActivity(intent4);
                        return true;
                    case R.id.nav_support:
                        //PAYLAŞ
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Günlük Burçlar: Astroloji, burç uyumu, yükselen burç ve daha fazlası! https://play.google.com/store/apps/details?id=org.uusoftware.fuelify");
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

    public static void getVariables(SharedPreferences prefs) {
        name = prefs.getString("Name", "-");
        email = prefs.getString("Email", "-");
        photo = prefs.getString("ProfilePhoto", "http://uusoftware.org/Fuelify/profile.png");
        carPhoto = prefs.getString("CarPhoto", "http://uusoftware.org/Fuelify/profile.png");
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
        premium = prefs.getBoolean("hasPremium", false);
    }

    private void buyPremiumPopup() {
        openCount = prefs.getInt("howMany", 0);
        openCount++;
        prefs.edit().putInt("howMany", openCount).apply();

        if (openCount >= 15 && !premium) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.buy_premium_ads_title)
                    .setMessage(R.string.buy_premium_ads_content)
                    .setPositiveButton(R.string.buy_premium_ads_okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                buyPremium();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.buy_premium_ads_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openCount = 0;
                            prefs.edit().putInt("howMany", openCount).apply();
                        }
                    })
                    .show();
        }
    }

    private void InAppBilling() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                //  loadBanner();
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                try {
                    checkPremium();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    public void checkPremium() throws RemoteException {
        Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
        if (ownedItems.getInt("RESPONSE_CODE") == 0) {
            ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            assert ownedSkus != null;
            if (ownedSkus.contains("premium")) {
                premium = true;
                prefs.edit().putBoolean("hasPremium", premium).apply();
                // loadBanner();
            } else {
                premium = false;
                prefs.edit().putBoolean("hasPremium", premium).apply();
                //  loadBanner();
            }
        } else {
            // loadBanner();
        }
    }

    public void buyPremium() throws RemoteException, IntentSender.SendIntentException {
        Toast.makeText(MainActivity.this,
                "Premium sürüme geçerek uygulama içerisindeki tüm reklamları kaldırabilirsiniz.", Toast.LENGTH_LONG)
                .show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "premium", "subs",
                "/tYMgwhg1DVikb4R4iLNAO5pNj/QWh19+vwajyUFbAyw93xVnDkeTZFdhdSdJ8M");
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        assert pendingIntent != null;
        startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0,
                0, 0);
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

   /* public void loadBanner() {
        bannerLayout = findViewById(R.id.bannerLayout);
        adViewContainer = findViewById(R.id.adFacebook);
        bannerAdmob = findViewById(R.id.adView);

        if (premium) {
            bannerLayout.setVisibility(View.GONE);
            adViewContainer.setVisibility(View.GONE);
            bannerAdmob.setVisibility(View.GONE);
            FrameLayout layout = findViewById(R.id.frame_container);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layout.getLayoutParams();
            params.bottomMargin = 0;
            layout.setLayoutParams(params);
        } else {
            bannerFacebook = new AdView(MainActivity.this, getString(R.string.banner_facebook), AdSize.BANNER_HEIGHT_50);
            adViewContainer.addView(bannerFacebook);
            bannerFacebook.setAdListener(new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    // Ad error callback
                    adViewContainer.setVisibility(View.GONE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    bannerAdmob.loadAd(adRequest);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    // Ad loaded callback
                    bannerAdmob.setVisibility(View.GONE);
                }

                @Override
                public void onAdClicked(Ad ad) {
                    // Ad clicked callback
                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            bannerFacebook.loadAd();
        }
    }*/

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Satın alma başarılı. Premium sürüme geçiriliyorsunuz, teşekkürler!", Toast.LENGTH_LONG).show();
                prefs.edit().putBoolean("hasPremium", true).apply();

                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            } else {
                Toast.makeText(MainActivity.this, "Satın alma başarısız. Lütfen daha sonra tekrar deneyiniz.",
                        Toast.LENGTH_LONG).show();
                prefs.edit().putBoolean("hasPremium", false).apply();
            }
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("Vehicle");
        if (fragment != null && fragment.isVisible()){
            fragment.onActivityResult(requestCode, resultCode, data);
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
            FragmentVehicle fragment1 = (FragmentVehicle) getSupportFragmentManager().findFragmentByTag("Vehicle");
            FragmentStations fragment2 = (FragmentStations) getSupportFragmentManager().findFragmentByTag("Stations");
            FragmentProfile fragment3 = (FragmentProfile) getSupportFragmentManager().findFragmentByTag("Profile");
            FragmentNews fragment4 = (FragmentNews) getSupportFragmentManager().findFragmentByTag("News");

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

            // FragmentVehicle OnBackPressed
            if (fragment1 != null && fragment1.isVisible()) {
                Fragment fragment = new FragmentHome();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
            }

            // FragmentStations OnBackPressed
            if (fragment2 != null && fragment2.isVisible()) {
                Fragment fragment = new FragmentHome();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
            }

            // FragmentProfile OnBackPressed
            if (fragment3 != null && fragment3.isVisible()) {
                Fragment fragment = new FragmentHome();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
            }

            // FragmentNews OnBackPressed
            if (fragment4 != null && fragment4.isVisible()) {
                Fragment fragment = new FragmentHome();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Home").commit();
            }
        }
    }
}