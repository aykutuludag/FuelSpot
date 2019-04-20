package com.fuelspot;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.StationItem;
import com.fuelspot.model.VehicleItem;
import com.fuelspot.receiver.AlarmReceiver;
import com.fuelspot.superuser.SuperMainActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.MapsInitializer;
import com.kobakei.ratethisapp.RateThisApp;
import com.ncapdevi.fragnav.FragNavController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    public static final int REQUEST_STORAGE = 0;
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_ALL = 2;
    public static final int GOOGLE_LOGIN = 100;

    public static List<StationItem> fullStationList = new ArrayList<>();
    public static List<VehicleItem> userAutomobileList = new ArrayList<>();
    public static List<CompanyItem> companyList = new ArrayList<>();

    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    public static String FENCE_RECEIVER_ACTION = "com.fuelspot.FENCE_RECEIVER_ACTION";
    public static int mapDefaultStationRange = 50;

    public static String USTimeFormat = "yyyy-MM-dd HH:mm:ss";
    public static String UniversalTimeFormat = "dd-MM-yyyy HH:mm:ss";
    public static String shortTimeFormat = "dd-MMM HH:mm";

    public static boolean premium, hasDoubleRange, isSigned, isSuperUser, isGeofenceOpen;
    public static float averageCons, userFSMoney, averagePrice, mapDefaultZoom, TAX_GASOLINE, TAX_DIESEL, TAX_LPG, TAX_ELECTRICITY;
    public static int carbonEmission;
    public static int vehicleID;
    public static int fuelPri;
    public static int fuelSec;
    public static int kilometer;
    public static int mapDefaultRange;
    public static String userPhoneNumber, plateNo, userlat, userlon, name, email, photo, carPhoto, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, currencySymbol, username, carBrand, carModel, userUnit, userFavorites;
    public static int adCount;

    private List<Fragment> fragments = new ArrayList<>(5);
    private SharedPreferences prefs;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private Window window;
    private Toolbar toolbar;
    private boolean doubleBackToExitPressedOnce;
    private FragNavController mFragNavController;
    private AHBottomNavigation bottomNavigation;
    private RequestQueue queue;
    public static InterstitialAd admobInterstitial;
    public MenuItem filterButton;
    CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();

    public static int getIndexOf(String[] strings, String item) {
        for (int i = 0; i < strings.length; i++) {
            if (item.equals(strings[i])) return i;
        }
        return -1;
    }

    public static void getVariables(SharedPreferences prefs) {
        name = prefs.getString("Name", "");
        email = prefs.getString("Email", "");
        photo = prefs.getString("ProfilePhoto", "");
        carPhoto = prefs.getString("CarPhoto", "");
        gender = prefs.getString("Gender", "");
        birthday = prefs.getString("Birthday", "");
        location = prefs.getString("Location", "");
        username = prefs.getString("UserName", "");
        carBrand = prefs.getString("carBrand", "Acura");
        carModel = prefs.getString("carModel", "RSX");
        fuelPri = prefs.getInt("FuelPrimary", -1);
        fuelSec = prefs.getInt("FuelSecondary", -1);
        kilometer = prefs.getInt("Kilometer", 0);
        userlat = prefs.getString("lat", "39.92505");
        userlon = prefs.getString("lon", "32.83476");
        premium = prefs.getBoolean("hasPremium", false);
        hasDoubleRange = prefs.getBoolean("hasDoubleRange", false);
        isSigned = prefs.getBoolean("isSigned", false);
        isSuperUser = prefs.getBoolean("isSuperUser", false);
        userCountry = prefs.getString("userCountry", "");
        userCountryName = prefs.getString("userCountryName", "");
        userDisplayLanguage = prefs.getString("userLanguage", "");
        userUnit = prefs.getString("userUnit", "");
        currencyCode = prefs.getString("userCurrency", "");
        averageCons = prefs.getFloat("averageConsumption", 0);
        averagePrice = prefs.getFloat("averagePrice", 0);
        TAX_GASOLINE = prefs.getFloat("taxGasoline", 0);
        TAX_DIESEL = prefs.getFloat("taxDiesel", 0);
        TAX_LPG = prefs.getFloat("taxLPG", 0);
        TAX_ELECTRICITY = prefs.getFloat("taxElectricity", 0);
        mapDefaultRange = prefs.getInt("RANGE", 2500);
        mapDefaultZoom = prefs.getFloat("ZOOM", 12.75f);
        isGeofenceOpen = prefs.getBoolean("Geofence", true);
        plateNo = prefs.getString("plateNo", "");
        vehicleID = prefs.getInt("vehicleID", 0);
        carbonEmission = prefs.getInt("carbonEmission", 0);
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
        currencySymbol = prefs.getString("userCurrencySymbol", "");
        userFavorites = prefs.getString("userFavorites", "");
    }

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    public static boolean verifyFilePickerPermission(Context context) {
        boolean hasStorage = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                hasStorage = true;
            }
        } else {
            hasStorage = true;
        }
        return hasStorage;
    }

    public static void AdMob(final Context mContext) {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("EEB32226D1D806C1259761D5FF4A8C41").build();
        admobInterstitial = new com.google.android.gms.ads.InterstitialAd(mContext);
        admobInterstitial.setAdUnitId(mContext.getString(R.string.interstitial_admob));
        admobInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (adCount < 2) {
                    AdMob(mContext);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                if (adCount < 2) {
                    AdMob(mContext);
                }
            }
        });
        admobInterstitial.loadAd(adRequest);
    }

    public static void AlarmBuilder(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        Intent myIntent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (alarmManager != null) {
            Calendar currentTime = Calendar.getInstance();
            alarmManager.setInexactRepeating(AlarmManager.RTC, currentTime.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Window
        window = this.getWindow();

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setLogo(R.drawable.brand_logo);
        }

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        // Some variables
        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);
        queue = Volley.newRequestQueue(this);

        // Custom Tab
        customTabBuilder.enableUrlBarHiding();
        customTabBuilder.setShowTitle(true);
        customTabBuilder.setToolbarColor(Color.parseColor("#FF7439"));

        // Initializing GoogleMaps
        MapsInitializer.initialize(this);

        // Bottom navigation
        FragNavController.Builder builder = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.mainContainer);
        fragments.add(FragmentStations.newInstance());
        fragments.add(FragmentNews.newInstance());
        fragments.add(FragmentAutomobile.newInstance());
        fragments.add(FragmentProfile.newInstance());
        fragments.add(FragmentSettings.newInstance());
        builder.rootFragments(fragments);
        mFragNavController = builder.build();

        bottomNavigation = findViewById(R.id.bottom_navigation);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_stations, R.drawable.tab_stations, R.color.colorPrimaryDark);
        bottomNavigation.addItem(item1);

        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_news, R.drawable.tab_news, R.color.colorPrimaryDark);
        bottomNavigation.addItem(item2);

        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_vehicle, R.drawable.tab_vehicle, R.color.colorPrimaryDark);
        bottomNavigation.addItem(item3);

        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_profile, R.drawable.tab_profile, R.color.colorPrimaryDark);
        bottomNavigation.addItem(item4);

        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_settings, R.drawable.tab_settings, R.color.colorPrimaryDark);
        bottomNavigation.addItem(item5);

        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        bottomNavigation.setOnTabSelectedListener(this);

        //In-App Services
        InAppBilling();

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.Config config = new RateThisApp.Config(3, 5);
        RateThisApp.init(config);

        // Fetch user vehicles once for each session
        fetchAutomobiles();

        // Fetch companies once for each session
        fetchCompanies();

        if (savedInstanceState == null) {
            if (isSigned) {
                // For superusuers
                if (isSuperUser) {
                    Intent intent = new Intent(MainActivity.this, SuperMainActivity.class);
                    startActivity(intent);
                }

                mFragNavController.switchTab(FragNavController.TAB1);

                // AppDeepLinking
                String link = getIntent().getDataString();
                if (link != null && link.length() > 0) {
                    if (link.contains("fuelspot.com.tr/news")) {
                        Intent intent = new Intent(MainActivity.this, NewsDetail.class);
                        intent.putExtra("URL", link);
                        startActivity(intent);
                    } else if (link.contains("fuelspot.com.tr/stations")) {
                        Intent intent2 = new Intent(MainActivity.this, StationDetails.class);
                        intent2.putExtra("STATION_ID", Integer.parseInt(link.replace("https://fuelspot.com.tr/stations/", "")));
                        startActivity(intent2);
                    } else if (link.contains("fuelspot.com.tr/terms-and-conditions")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/terms-and-conditions"));
                    } else if (link.contains("fuelspot.com.tr/privacy")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/privacy"));
                    } else if (link.contains("fuelspot.com.tr/help")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/help"));
                    } else if (link.contains("fuelspot.com.tr/help-for-superuser")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/help-for-superuser"));
                    }
                }

                // Firebase Cloud Messaging
                String link2 = getIntent().getExtras().getString("URL");
                if (link2 != null && link2.length() > 0) {
                    if (link2.contains("fuelspot.com.tr/news")) {
                        Intent intent = new Intent(MainActivity.this, NewsDetail.class);
                        intent.putExtra("URL", link2);
                        startActivity(intent);
                    } else if (link2.contains("fuelspot.com.tr/stations")) {
                        Intent intent2 = new Intent(MainActivity.this, StationDetails.class);
                        intent2.putExtra("STATION_ID", Integer.parseInt(link2.replace("https://fuelspot.com.tr/stations/", "")));
                        startActivity(intent2);
                    } else if (link2.contains("fuelspot.com.tr/terms-and-conditions")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/terms-and-conditions"));
                    } else if (link2.contains("fuelspot.com.tr/privacy")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/privacy"));
                    } else if (link2.contains("fuelspot.com.tr/help")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/help"));
                    } else if (link2.contains("fuelspot.com.tr/help-for-superuser")) {
                        CustomTabsIntent customTabsIntent = customTabBuilder.build();
                        customTabsIntent.intent.setPackage("com.android.chrome");
                        customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/help-for-superuser"));
                    }
                }

                // For superusuers
                if (isSuperUser) {
                    finish();
                }
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void InAppBilling() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
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

    private void checkPremium() throws RemoteException {
        Bundle ownedItems = mService.getPurchases(3, getPackageName(), "subs", null);
        if (ownedItems.getInt("RESPONSE_CODE") == 0) {
            ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            assert ownedSkus != null;

            if (ownedSkus.contains("premium") || ownedSkus.contains("premium_super")) {
                premium = true;
                prefs.edit().putInt("RANGE", 5000).apply();
                prefs.edit().putFloat("ZOOM", 12f).apply();
            } else {
                premium = false;
                prefs.edit().putInt("RANGE", 2500).apply();
                prefs.edit().putFloat("ZOOM", 13f).apply();
            }
            prefs.edit().putBoolean("hasPremium", premium).apply();

            if (ownedSkus.contains("2x_range") || ownedSkus.contains("2x_range_super")) {
                hasDoubleRange = true;
                prefs.edit().putInt("RANGE", 5000).apply();
                prefs.edit().putFloat("ZOOM", 12f).apply();

            } else {
                hasDoubleRange = false;
                prefs.edit().putInt("RANGE", 2500).apply();
                prefs.edit().putFloat("ZOOM", 13f).apply();
            }
            prefs.edit().putBoolean("hasDoubleRange", hasDoubleRange).apply();
        }
    }

    private void fetchAutomobiles() {
        userAutomobileList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_USER_AUTOMOBILES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    VehicleItem item = new VehicleItem();
                                    item.setID(obj.getInt("id"));
                                    item.setVehicleBrand(obj.getString("car_brand"));
                                    item.setVehicleModel(obj.getString("car_model"));
                                    item.setVehicleFuelPri(obj.getInt("fuelPri"));
                                    item.setVehicleFuelSec(obj.getInt("fuelSec"));
                                    item.setVehicleKilometer(obj.getInt("kilometer"));
                                    item.setVehiclePhoto(obj.getString("carPhoto"));
                                    item.setVehiclePlateNo(obj.getString("plateNo"));
                                    item.setVehicleConsumption((float) obj.getDouble("avgConsumption"));
                                    item.setVehicleEmission(obj.getInt("carbonEmission"));
                                    userAutomobileList.add(item);

                                    // If there is any selected auto, choose first one.
                                    if (vehicleID == 0) {
                                        chooseVehicle(item);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    private void chooseVehicle(VehicleItem item) {
        vehicleID = item.getID();
        prefs.edit().putInt("vehicleID", vehicleID).apply();

        carBrand = item.getVehicleBrand();
        prefs.edit().putString("carBrand", carBrand).apply();

        carModel = item.getVehicleModel();
        prefs.edit().putString("carModel", carModel).apply();

        fuelPri = item.getVehicleFuelPri();
        prefs.edit().putInt("FuelPrimary", fuelPri).apply();

        fuelSec = item.getVehicleFuelSec();
        prefs.edit().putInt("FuelSecondary", fuelSec).apply();

        kilometer = item.getVehicleKilometer();
        prefs.edit().putInt("Kilometer", kilometer).apply();

        carPhoto = item.getVehiclePhoto();
        prefs.edit().putString("CarPhoto", carPhoto).apply();

        plateNo = item.getVehiclePlateNo();
        prefs.edit().putString("plateNo", plateNo).apply();

        averageCons = item.getVehicleConsumption();
        prefs.edit().putFloat("averageConsumption", averageCons).apply();

        carbonEmission = item.getVehicleEmission();
        prefs.edit().putInt("carbonEmission", carbonEmission).apply();
    }

    private void fetchCompanies() {
        companyList.clear();

        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_COMPANY),
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    private void coloredBars(int color1, int color2) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Thanks to this brief code, we can call onActivityResult in a fragment
        // Currently used in FragmentStations if user revoke location permission
        Fragment fragment = mFragNavController.getCurrentFrag();
        if (fragment != null && fragment.isVisible()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        filterButton = menu.findItem(R.id.filter_stations);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_stations:
                FragmentStations fragment = (FragmentStations) fragments.get(0);
                fragment.filterPopup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }

        if (queue != null) {
            queue.cancelAll(this);
        }
    }

    @Override
    public void onBackPressed() {
        // FragmentHome OnBackPressed
        if (fragments.get(0) != null) {
            if (fragments.get(0).isVisible()) {
                if (doubleBackToExitPressedOnce) {
                    adCount = 0;
                    mFragNavController.clearStack();
                    super.onBackPressed();
                    finish();
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

        if (fragments.get(1) != null) {
            if (fragments.get(1).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragments.get(2) != null) {
            if (fragments.get(2).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragments.get(3) != null) {
            if (fragments.get(3).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragments.get(4) != null) {
            if (fragments.get(4).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        if (filterButton != null) {
            if (position == 0) {
                filterButton.setVisible(true);
            } else {
                filterButton.setVisible(false);
            }
        }
        mFragNavController.switchTab(position);
        return true;
    }
}