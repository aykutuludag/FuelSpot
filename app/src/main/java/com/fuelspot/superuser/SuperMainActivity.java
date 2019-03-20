package com.fuelspot.superuser;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
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
import com.fuelspot.FragmentNews;
import com.fuelspot.FragmentSettings;
import com.fuelspot.FragmentStations;
import com.fuelspot.LoginActivity;
import com.fuelspot.NewsDetail;
import com.fuelspot.R;
import com.fuelspot.StationDetails;
import com.fuelspot.WebViewActivity;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.StationItem;
import com.google.android.gms.maps.MapsInitializer;
import com.kobakei.ratethisapp.RateThisApp;
import com.ncapdevi.fragnav.FragNavController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.adCount;
import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.hasDoubleRange;
import static com.fuelspot.MainActivity.isGeofenceOpen;
import static com.fuelspot.MainActivity.isSigned;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;

public class SuperMainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    // General variables for SuperUser
    public static List<StationItem> listOfOwnedStations = new ArrayList<>();

    public static int isStationVerified, superStationID;
    public static float ownedGasolinePrice, ownedDieselPrice, ownedLPGPrice, ownedElectricityPrice;
    public static String superLicenseNo, superStationName, superStationAddress, superStationCountry, superStationLocation, superStationLogo, superGoogleID, superFacilities, superLastUpdate;

    private boolean doubleBackToExitPressedOnce;
    private RequestQueue queue;
    private Window window;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private FragNavController mFragNavController;
    private AHBottomNavigation bottomNavigation;
    private List<Fragment> fragments = new ArrayList<>(5);
    public MenuItem filterButton;

    public static void getSuperVariables(SharedPreferences prefs) {
        // Station-specific information
        superStationID = prefs.getInt("SuperStationID", 0);
        superStationName = prefs.getString("SuperStationName", "");
        superStationAddress = prefs.getString("SuperStationAddress", "");
        superStationCountry = prefs.getString("SuperStationCountry", "");
        superStationLocation = prefs.getString("SuperStationLocation", "");
        superGoogleID = prefs.getString("SuperGoogleID", "");
        superFacilities = prefs.getString("SuperStationFacilities", "");
        superStationLogo = prefs.getString("SuperStationLogo", "");
        ownedGasolinePrice = prefs.getFloat("superGasolinePrice", 0);
        ownedDieselPrice = prefs.getFloat("superDieselPrice", 0);
        ownedLPGPrice = prefs.getFloat("superLPGPrice", 0);
        ownedElectricityPrice = prefs.getFloat("superElectricityPrice", 0);
        superLicenseNo = prefs.getString("SuperLicenseNo", "");
        isStationVerified = prefs.getInt("isStationVerified", 0);
        superLastUpdate = prefs.getString("SuperLastUpdate", "");
        isGeofenceOpen = prefs.getBoolean("Geofence", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_main);

        //Window
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setLogo(R.drawable.brand_logo);
        }

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        //Some variables
        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);
        getSuperVariables(prefs);
        queue = Volley.newRequestQueue(this);

        // Activate map
        MapsInitializer.initialize(this.getApplicationContext());

        // Bottom navigation
        FragNavController.Builder builder = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.pager);
        fragments.add(FragmentMyStation.newInstance());
        fragments.add(FragmentNews.newInstance());
        fragments.add(FragmentStations.newInstance());
        fragments.add(FragmentSuperProfile.newInstance());
        fragments.add(FragmentSettings.newInstance());
        builder.rootFragments(fragments);
        mFragNavController = builder.build();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        //Add tabs
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_mystation, R.drawable.tab_mystation, R.color.colorPrimaryDark);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_news, R.drawable.tab_news, R.color.colorPrimaryDark);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_stations, R.drawable.tab_stations, R.color.colorPrimaryDark);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_profile, R.drawable.tab_profile, R.color.colorPrimaryDark);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_settings, R.drawable.tab_settings, R.color.colorPrimaryDark);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.addItem(item5);

        // Bottombar Settings
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        bottomNavigation.setOnTabSelectedListener(this);

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);

        //In-App Services
        InAppBilling();

        // Fetch stations once for each session
        fetchOwnedStations();

        // Fetch companies once for each session
        fetchCompanies();

        if (savedInstanceState == null) {
            if (isSigned) {
                mFragNavController.switchTab(FragNavController.TAB1);

                // AppDeepLinking
                String link = getIntent().getDataString();
                if (link != null && link.length() > 0) {
                    // Temporary only getting fuelspot.com
                    link = link.replace("fuelspot.com", "fuelspot.com.tr");

                    if (link.contains("fuelspot.com.tr/news")) {
                        Intent intent = new Intent(SuperMainActivity.this, NewsDetail.class);
                        intent.putExtra("URL", link);
                        startActivity(intent);
                    } else if (link.contains("fuelspot.com.tr/stations")) {
                        Intent intent2 = new Intent(SuperMainActivity.this, StationDetails.class);
                        intent2.putExtra("STATION_ID", Integer.parseInt(link.replace("https://fuelspot.com.tr/stations/", "")));
                        startActivity(intent2);
                    } else if (link.contains("https://fuelspot.com.tr/terms-and-conditions")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/terms-and-conditions");
                        startActivity(intent);
                    } else if (link.contains("https://fuelspot.com.tr/privacy")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/privacy");
                        startActivity(intent);
                    } else if (link.contains("https://fuelspot.com.tr/help")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/help");
                        startActivity(intent);
                    } else if (link.contains("https://fuelspot.com.tr/help-for-superuser")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/help-for-superuser");
                        startActivity(intent);
                    } else {
                        // Do nothing for now
                    }
                }

                // Firebase Cloud Messaging
                String link2 = getIntent().getExtras().getString("URL");
                if (link2 != null && link2.length() > 0) {
                    // Temporary only getting fuelspot.com
                    link2 = link2.replace("fuelspot.com", "fuelspot.com.tr");

                    if (link2.contains("fuelspot.com.tr/news")) {
                        Intent intent = new Intent(SuperMainActivity.this, NewsDetail.class);
                        intent.putExtra("URL", link2);
                        startActivity(intent);
                    } else if (link2.contains("fuelspot.com.tr/stations")) {
                        Intent intent2 = new Intent(SuperMainActivity.this, StationDetails.class);
                        intent2.putExtra("STATION_ID", Integer.parseInt(link2.replace("https://fuelspot.com.tr/stations/", "")));
                        startActivity(intent2);
                    } else if (link2.contains("https://fuelspot.com.tr/terms-and-conditions")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/terms-and-conditions");
                        startActivity(intent);
                    } else if (link2.contains("https://fuelspot.com.tr/privacy")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/privacy");
                        startActivity(intent);
                    } else if (link2.contains("https://fuelspot.com.tr/help")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/help");
                        startActivity(intent);
                    } else if (link2.contains("https://fuelspot.com.tr/help-for-superuser")) {
                        Intent intent = new Intent(SuperMainActivity.this, WebViewActivity.class);
                        intent.putExtra("URL", "https://fuelspot.com.tr/help-for-superuser");
                        startActivity(intent);
                    } else {
                        // Do nothing for now
                    }
                }
            } else {
                Intent intent = new Intent(SuperMainActivity.this, LoginActivity.class);
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

    private void fetchOwnedStations() {
        listOfOwnedStations.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_SUPERUSER_STATIONS),
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

                                    if (superStationID == 0) {
                                        chooseStation(item);
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
                params.put("superusername", username);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
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

        isStationVerified = item.getIsVerified();
        prefs.edit().putInt("isStationVerified", isStationVerified).apply();

        superLastUpdate = item.getLastUpdated();
        prefs.edit().putString("SuperLastUpdate", superLastUpdate).apply();

        getSuperVariables(prefs);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Thanks to this brief code, we can call onActivityResult in a fragment
        // Currently used in FragmentMyStation if user revoke location permission
        Fragment fragment = mFragNavController.getCurrentFrag();
        if (fragment != null && fragment.isVisible()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        filterButton = menu.findItem(R.id.filter_stations);
        filterButton.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_stations:
                FragmentStations fragment = (FragmentStations) fragments.get(2);
                fragment.filterPopup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        if (filterButton != null) {
            if (position == 2) {
                filterButton.setVisible(true);
            } else {
                filterButton.setVisible(false);
            }
        }
        mFragNavController.switchTab(position);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mServiceConn != null) {
            unbindService(mServiceConn);
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
}
