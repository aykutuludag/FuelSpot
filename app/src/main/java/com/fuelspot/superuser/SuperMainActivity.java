package com.fuelspot.superuser;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
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
import com.fuelspot.model.CampaignItem;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.StationItem;
import com.google.android.material.snackbar.Snackbar;
import com.ncapdevi.fragnav.FragNavController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hotchemi.android.rate.AppRate;

import static com.fuelspot.MainActivity.adCount;
import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.globalCampaignList;
import static com.fuelspot.MainActivity.hasDoubleRange;
import static com.fuelspot.MainActivity.isGeofenceOpen;
import static com.fuelspot.MainActivity.isSigned;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.StoreActivity.doubleSku;
import static com.fuelspot.StoreActivity.premiumSku;
import static com.fuelspot.superuser.SuperStoreActivity.doubleSuperSku;
import static com.fuelspot.superuser.SuperStoreActivity.premiumSuperSku;

public class SuperMainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener, PurchasesUpdatedListener {

    // General variables for SuperUser
    public static List<StationItem> listOfOwnedStations = new ArrayList<>();

    public static int isStationVerified, superStationID;
    public static float ownedGasolinePrice, ownedDieselPrice, ownedLPGPrice, ownedElectricityPrice;
    public static String superLicenseNo, superStationName, superStationAddress, superStationCountry, superStationLocation, superStationLogo, ownedOtherFuels, superFacilities, superLastUpdate;
    public AHBottomNavigation bottomNavigation;
    CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
    private boolean doubleBackToExitPressedOnce;
    private RequestQueue queue;
    private Window window;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private FragNavController mFragNavController;
    public static List<Fragment> fragmentsSuperUser = new ArrayList<>(5);
    private BillingClient billingClient;

    public static void getSuperVariables(SharedPreferences prefs) {
        // Station-specific information
        superStationID = prefs.getInt("SuperStationID", 0);
        superStationName = prefs.getString("SuperStationName", "");
        superStationAddress = prefs.getString("SuperStationAddress", "");
        superStationCountry = prefs.getString("SuperStationCountry", "");
        superStationLocation = prefs.getString("SuperStationLocation", "");
        superFacilities = prefs.getString("SuperStationFacilities", "");
        superStationLogo = prefs.getString("SuperStationLogo", "");
        ownedGasolinePrice = prefs.getFloat("superGasolinePrice", 0);
        ownedDieselPrice = prefs.getFloat("superDieselPrice", 0);
        ownedLPGPrice = prefs.getFloat("superLPGPrice", 0);
        ownedElectricityPrice = prefs.getFloat("superElectricityPrice", 0);
        ownedOtherFuels = prefs.getString("superOtherFuels", "");
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

        // Bottom navigation
        FragNavController.Builder builder = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.pager);
        fragmentsSuperUser.add(FragmentMyStation.newInstance());
        fragmentsSuperUser.add(FragmentNews.newInstance());
        fragmentsSuperUser.add(FragmentStations.newInstance());
        fragmentsSuperUser.add(FragmentSuperProfile.newInstance());
        fragmentsSuperUser.add(FragmentSettings.newInstance());
        builder.rootFragments(fragmentsSuperUser);
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
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));
        bottomNavigation.setAccentColor(Color.parseColor("#FF4500"));
        bottomNavigation.setInactiveColor(Color.parseColor("#626262"));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setOnTabSelectedListener(this);

        // AppRater
        AppRate.with(this).setInstallDays(0).setLaunchTimes(5).setRemindInterval(3).monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        //In-App Services
        InAppBilling();

        // Fetch stations once for each session
        fetchOwnedStations();

        // Fetch companies once for each session
        fetchCompanies();

        // Fetch globalCampaigns once for each session
        fetchGlobalCampaigns();

        if (savedInstanceState == null) {
            if (isSigned) {
                mFragNavController.switchTab(FragNavController.TAB1);

                // AppDeepLinking
                String link = getIntent().getDataString();
                if (link != null && link.length() > 0) {
                    urlStructure(link);
                }

                // Firebase Cloud Messaging
                if (getIntent().getExtras() != null) {
                    String link2 = getIntent().getExtras().getString("URL");
                    if (link2 != null && link2.length() > 0) {
                        urlStructure(link2);
                    }
                }
            } else {
                Intent intent = new Intent(SuperMainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    void urlStructure(String URL) {
        if (URL.contains("fuelspot.com.tr/news")) {
            Intent intent = new Intent(SuperMainActivity.this, NewsDetail.class);
            intent.putExtra("URL", URL);
            showAds(this, intent);
        } else if (URL.contains("fuelspot.com.tr/stations")) {
            Intent intent2 = new Intent(SuperMainActivity.this, StationDetails.class);
            intent2.putExtra("STATION_ID", Integer.parseInt(URL.replace("https://fuelspot.com.tr/stations/", "")));
            showAds(this, intent2);
        } else if (URL.contains("fuelspot.com.tr/terms-and-conditions")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(SuperMainActivity.this, Uri.parse("https://fuelspot.com.tr/terms-and-conditions"));
        } else if (URL.contains("fuelspot.com.tr/privacy")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(SuperMainActivity.this, Uri.parse("https://fuelspot.com.tr/privacy"));
        } else if (URL.contains("fuelspot.com.tr/help")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(SuperMainActivity.this, Uri.parse("https://fuelspot.com.tr/help"));
        } else if (URL.contains("fuelspot.com.tr/help-for-superuser")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(SuperMainActivity.this, Uri.parse("https://fuelspot.com.tr/help-for-superuser"));
        }
    }

    private void InAppBilling() {
        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    getSkus();
                    checkSubscriptions();
                } else {
                    billingClient = null;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingClient = null;
            }
        });
    }

    private void getSkus() {
        List<String> skuList = new ArrayList<>();
        skuList.add("premium");
        skuList.add("premium_super");
        skuList.add("2x_range");
        skuList.add("2x_range_super");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    for (int i = 0; i < skuDetailsList.size(); i++) {
                        if (skuDetailsList.get(i).getSku().equals("premium")) {
                            premiumSku = skuDetailsList.get(i);
                        }

                        if (skuDetailsList.get(i).getSku().equals("premium_super")) {
                            premiumSuperSku = skuDetailsList.get(i);
                        }

                        if (skuDetailsList.get(i).getSku().equals("2x_range")) {
                            doubleSku = skuDetailsList.get(i);
                        }

                        if (skuDetailsList.get(i).getSku().equals("2x_range_super")) {
                            doubleSuperSku = skuDetailsList.get(i);
                        }
                    }
                }
            }
        });
    }

    private void checkSubscriptions() {
        Purchase.PurchasesResult billingResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && billingResult.getPurchasesList() != null) {
            ArrayList<String> ownedSkus = new ArrayList<>();
            for (int i = 0; i < billingResult.getPurchasesList().size(); i++) {
                ownedSkus.add(billingResult.getPurchasesList().get(i).getSku());
                System.out.println(billingResult.getPurchasesList().get(i));
            }

            if (ownedSkus.contains("premium") || ownedSkus.contains("premium_super")) {
                premium = true;
                mapDefaultRange = 6000;
                mapDefaultZoom = 12f;
            } else if (ownedSkus.contains("2x_range") || ownedSkus.contains("2x_range_super")) {
                hasDoubleRange = true;
                mapDefaultRange = 6000;
                mapDefaultZoom = 12f;
            } else {
                hasDoubleRange = false;
                premium = false;
                mapDefaultRange = 3000;
                mapDefaultZoom = 12.5f;
            }

            prefs.edit().putBoolean("hasDoubleRange", hasDoubleRange).apply();
            prefs.edit().putBoolean("hasPremium", premium).apply();
            prefs.edit().putInt("RANGE", mapDefaultRange).apply();
            prefs.edit().putFloat("ZOOM", mapDefaultZoom).apply();
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
        if (listOfOwnedStations == null || listOfOwnedStations.size() == 0) {
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
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            //Adding request to the queue
            queue.add(stringRequest);
        }
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

        ownedOtherFuels = item.getOtherFuels();
        prefs.edit().putString("superOtherFuels", ownedOtherFuels).apply();

        ownedElectricityPrice = item.getElectricityPrice();
        prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();

        isStationVerified = item.getIsVerified();
        prefs.edit().putInt("isStationVerified", isStationVerified).apply();

        superLastUpdate = item.getLastUpdated();
        prefs.edit().putString("SuperLastUpdate", superLastUpdate).apply();
    }

    private void fetchCompanies() {
        if (companyList == null || companyList.size() == 0) {
            //Showing the progress dialog
            StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_COMPANY),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null && response.length() > 0) {
                                if (response.equals("AuthError")) {
                                    //We're just checking here for any authentication error. If it is, log out.

                                    // Do logout
                                    @SuppressLint("SdCardPath")
                                    File sharedPreferenceFile = new File("/data/data/" + getPackageName() + "/shared_prefs/");
                                    File[] listFiles = sharedPreferenceFile.listFiles();
                                    for (File file : listFiles) {
                                        file.delete();
                                    }

                                    PackageManager packageManager = SuperMainActivity.this.getPackageManager();
                                    Intent intent = packageManager.getLaunchIntentForPackage(SuperMainActivity.this.getPackageName());
                                    ComponentName componentName = intent.getComponent();
                                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                                    SuperMainActivity.this.startActivity(mainIntent);
                                    Runtime.getRuntime().exit(0);
                                } else {
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
            queue.add(stringRequest);
        }
    }

    private void fetchGlobalCampaigns() {
        if (globalCampaignList == null || globalCampaignList.size() == 0) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_GLOBAL_CAMPAINGS),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null && response.length() > 0) {
                                try {
                                    JSONArray res = new JSONArray(response);
                                    for (int i = 0; i < res.length(); i++) {
                                        JSONObject obj = res.getJSONObject(i);

                                        CampaignItem item = new CampaignItem();
                                        item.setID(obj.getInt("id"));
                                        item.setStationID(-1);
                                        item.setCompanyName(obj.getString("companyName"));
                                        item.setCampaignName(obj.getString("campaignName"));
                                        item.setCampaignDesc(obj.getString("campaignDesc"));
                                        item.setCampaignPhoto(obj.getString("campaignPhoto"));
                                        item.setCampaignStart(obj.getString("campaignStart"));
                                        item.setCampaignEnd(obj.getString("campaignEnd"));
                                        globalCampaignList.add(item);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
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
            queue.add(stringRequest);
        }
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
    public boolean onTabSelected(int position, boolean wasSelected) {
        mFragNavController.switchTab(position);
        return true;
    }

    @Override
    public void onBackPressed() {
        // FragmentHome OnBackPressed
        if (fragmentsSuperUser.get(0) != null) {
            if (fragmentsSuperUser.get(0).isVisible()) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    adCount = 0;

                    FragmentMyStation fMy = (FragmentMyStation) fragmentsSuperUser.get(0);
                    fMy.rootView = null;

                    FragmentNews fn = (FragmentNews) fragmentsSuperUser.get(1);
                    fn.rootView = null;

                    FragmentStations fs = (FragmentStations) fragmentsSuperUser.get(2);
                    fs.rootView = null;

                    FragmentSuperProfile fp = (FragmentSuperProfile) fragmentsSuperUser.get(3);
                    fp.rootView = null;

                    FragmentSettings fsettings = (FragmentSettings) fragmentsSuperUser.get(4);
                    fsettings.rootView = null;

                    finish();
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

        if (fragmentsSuperUser.get(1) != null) {
            if (fragmentsSuperUser.get(1).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragmentsSuperUser.get(2) != null) {
            if (fragmentsSuperUser.get(2).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragmentsSuperUser.get(3) != null) {
            if (fragmentsSuperUser.get(3).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragmentsSuperUser.get(4) != null) {
            if (fragmentsSuperUser.get(4).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }
    }

    /**
     * Implement this method to get notifications for purchases updates. Both purchases initiated by
     * your app and the ones initiated by Play Store will be reported here.
     *
     * @param billingResult BillingResult of the update.
     * @param purchases     List of updated purchases if present.
     */
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        // DO NOTHING. WE DO NOT PURCHASE ANYTHING HERE
    }
}
