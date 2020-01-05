package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.fuelspot.model.CampaignItem;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.StationItem;
import com.fuelspot.model.VehicleItem;
import com.fuelspot.receiver.AlarmReceiver;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ncapdevi.fragnav.FragNavController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import hotchemi.android.rate.AppRate;

import static com.fuelspot.StoreActivity.doubleSku;
import static com.fuelspot.StoreActivity.premiumSku;
import static com.fuelspot.superuser.SuperStoreActivity.doubleSuperSku;
import static com.fuelspot.superuser.SuperStoreActivity.premiumSuperSku;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener, AHBottomNavigation.OnTabSelectedListener {

    public static final int REQUEST_STORAGE = 0;
    public static final int REQUEST_LOCATION = 1;
    public static final int GOOGLE_LOGIN = 100;

    public static List<VehicleItem> automobileModels = new ArrayList<>();
    public static List<StationItem> fullStationList = new ArrayList<>();
    public static List<VehicleItem> userAutomobileList = new ArrayList<>();
    public static List<CompanyItem> companyList = new ArrayList<>();
    public static List<CampaignItem> globalCampaignList = new ArrayList<>();

    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    public static String FENCE_RECEIVER_ACTION = "com.fuelspot.FENCE_RECEIVER_ACTION";
    public static int mapDefaultStationRange = 50;
    public static int streetViewCountForPremium;

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

    public static String token, firebaseToken, userPhoneNumber, plateNo, userlat, userlon, name, email, photo, carPhoto, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, currencySymbol, username, carBrand, carModel, userUnit, userFavorites;
    public static int adCount = 0;
    public static InterstitialAd admobInterstitial;
    public AHBottomNavigation bottomNavigation;
    CustomTabsIntent.Builder customTabBuilder = new CustomTabsIntent.Builder();
    public static List<Fragment> fragmentsUser = new ArrayList<>(5);
    private SharedPreferences prefs;
    private Window window;
    private Toolbar toolbar;
    private boolean doubleBackToExitPressedOnce;
    private FragNavController mFragNavController;
    private RequestQueue queue;
    public static long streetViewLastSeen, lastStationSearch;
    public static int searchCount;
    private BillingClient billingClient;
    ProgressDialog waitForLoad;

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
        fuelPri = prefs.getInt("FuelPrimary", 0);
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
        mapDefaultRange = prefs.getInt("RANGE", 3000);
        mapDefaultZoom = prefs.getFloat("ZOOM", 12.5f);
        isGeofenceOpen = prefs.getBoolean("Geofence", true);
        plateNo = prefs.getString("plateNo", "");
        vehicleID = prefs.getInt("vehicleID", 0);
        carbonEmission = prefs.getInt("carbonEmission", 0);
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
        currencySymbol = prefs.getString("userCurrencySymbol", "");
        userFavorites = prefs.getString("userFavorites", "");
        token = prefs.getString("token", "");
        firebaseToken = prefs.getString("firebaseToken", "");
        streetViewLastSeen = prefs.getLong("StreetViewLastSeen", 0);
        lastStationSearch = prefs.getLong("lastStationSearch", 0);
        searchCount = prefs.getInt("searchCount", 0);
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap resizeAndRotate(Bitmap bmp, float degrees) {
        if (bmp.getWidth() > 1080 || bmp.getHeight() > 1920) {
            float aspectRatio = (float) bmp.getWidth() / bmp.getHeight();
            int width, height;

            if (aspectRatio < 1) {
                // Portrait
                width = (int) (aspectRatio * 1920);
                height = (int) (width * (1f / aspectRatio));
            } else {
                // Landscape
                width = (int) (aspectRatio * 1080);
                height = (int) (width * (1f / aspectRatio));
            }

            bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        if (degrees != 0) {
            return rotate(bmp, degrees);
        } else {
            return bmp;
        }
    }

    public static String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);

        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    public static Boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
                return locationMode != Settings.Secure.LOCATION_MODE_OFF;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            //Lower than API 19
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return locationProviders.contains(LocationManager.GPS_PROVIDER) && locationProviders.contains(LocationManager.NETWORK_PROVIDER);
        }
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
        if (adCount < 2) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("EEB32226D1D806C1259761D5FF4A8C41").build();
            admobInterstitial = new com.google.android.gms.ads.InterstitialAd(mContext);
            admobInterstitial.setAdUnitId(mContext.getString(R.string.interstitial_admob));
            admobInterstitial.loadAd(adRequest);
        }
    }

    public static void showAds(final Context mContext, final Intent intent) {
        if (admobInterstitial != null && admobInterstitial.isLoaded()) {
            admobInterstitial.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    AdMob(mContext);
                    if (intent != null) {
                        mContext.startActivity(intent);
                    }
                }
            });

            admobInterstitial.show();
            adCount++;
        } else {
            // Ads doesn't loaded.
            if (intent != null) {
                mContext.startActivity(intent);
            }
        }

        if (adCount == 2) {
            Toast.makeText(mContext, mContext.getString(R.string.last_ads_info), Toast.LENGTH_SHORT).show();
            adCount++;
        }
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

        if (firebaseToken.length() == 0) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (task.getResult() != null && task.isSuccessful()) {
                        // Get new Instance ID token
                        firebaseToken = task.getResult().getToken();
                        prefs.edit().putString("firebaseToken", firebaseToken).apply();
                        registerToken();
                    }
                }
            });
        }

        // Custom Tab
        customTabBuilder.enableUrlBarHiding();
        customTabBuilder.setShowTitle(true);
        customTabBuilder.setToolbarColor(Color.parseColor("#FF7439"));

        // Bottom navigation
        FragNavController.Builder builder = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.mainContainer);
        fragmentsUser.add(FragmentStations.newInstance());
        fragmentsUser.add(FragmentNews.newInstance());
        fragmentsUser.add(FragmentAutomobile.newInstance());
        fragmentsUser.add(FragmentProfile.newInstance());
        fragmentsUser.add(FragmentSettings.newInstance());
        builder.rootFragments(fragmentsUser);
        mFragNavController = builder.build();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));
        bottomNavigation.setAccentColor(Color.parseColor("#FF4500"));
        bottomNavigation.setInactiveColor(Color.parseColor("#626262"));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setBehaviorTranslationEnabled(false);

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

        bottomNavigation.setOnTabSelectedListener(this);

        //In-App Services
        InAppBilling();

        // AppRater
        AppRate.with(this).setInstallDays(0).setLaunchTimes(3).setRemindInterval(3).monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        // Fetch user vehicles once for each session
        fetchAutomobiles();

        // Fetch globalCampaigns once for each session
        fetchGlobalCampaigns();

        if (savedInstanceState == null) {
            if (isSigned) {
                // AppDeepLinking
                final String link = getIntent().getDataString();
                if (link != null && link.length() > 0) {
                    waitForLoad = new ProgressDialog(MainActivity.this);
                    waitForLoad.setTitle(getString(R.string.data_loading));
                    waitForLoad.setMessage(getString(R.string.please_wait));
                    waitForLoad.setIndeterminate(true);
                    waitForLoad.setCancelable(false);
                    waitForLoad.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            urlStructure(link);
                        }
                    }, 3500);
                }

                // Firebase Cloud Messaging
                if (getIntent().getExtras() != null) {
                    final String link2 = getIntent().getExtras().getString("URL");
                    if (link2 != null && link2.length() > 0) {
                        waitForLoad = new ProgressDialog(MainActivity.this);
                        waitForLoad.setTitle(getString(R.string.data_loading));
                        waitForLoad.setMessage(getString(R.string.please_wait));
                        waitForLoad.setIndeterminate(true);
                        waitForLoad.setCancelable(false);
                        waitForLoad.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                urlStructure(link2);
                            }
                        }, 3500);
                    }
                }

                mFragNavController.switchTab(FragNavController.TAB1);
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    void urlStructure(String URL) {
        waitForLoad.dismiss();
        if (URL.contains("fuelspot.com.tr/news")) {
            Intent intent = new Intent(MainActivity.this, NewsDetail.class);
            intent.putExtra("URL", URL);
            showAds(MainActivity.this, intent);
        } else if (URL.contains("fuelspot.com.tr/stations")) {
            Intent intent2 = new Intent(MainActivity.this, StationDetails.class);
            intent2.putExtra("STATION_ID", Integer.parseInt(URL.replace("https://fuelspot.com.tr/stations/", "")));
            showAds(MainActivity.this, intent2);
        } else if (URL.contains("fuelspot.com.tr/terms-and-conditions")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/terms-and-conditions"));
        } else if (URL.contains("fuelspot.com.tr/privacy")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/privacy"));
        } else if (URL.contains("fuelspot.com.tr/help")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/help"));
        } else if (URL.contains("fuelspot.com.tr/help-for-superuser")) {
            CustomTabsIntent customTabsIntent = customTabBuilder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse("https://fuelspot.com.tr/help-for-superuser"));
        }
    }

    private void InAppBilling() {
        billingClient = BillingClient.newBuilder(MainActivity.this).setListener(MainActivity.this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if ((billingResult != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) && billingClient.isReady()) {
                    billingClient.isReady();
                    getSkus();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // DO NOTHING
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

                    checkSubscriptions();
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

    private void fetchAutomobiles() {
        if (userAutomobileList == null || userAutomobileList.size() == 0) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_USER_AUTOMOBILES) + "?username=" + username,
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
                                    }

                                    if (vehicleID == 0) {
                                        chooseVehicle(userAutomobileList.get(0));
                                    } else {
                                        // User already selected station.
                                        for (int k = 0; k < userAutomobileList.size(); k++) {
                                            if (vehicleID == userAutomobileList.get(k).getID()) {
                                                chooseVehicle(userAutomobileList.get(k));
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

    private void fetchGlobalCampaigns() {
        if (globalCampaignList == null || globalCampaignList.size() == 0) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_GLOBAL_CAMPAINGS),
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

                                    PackageManager packageManager = MainActivity.this.getPackageManager();
                                    Intent intent = packageManager.getLaunchIntentForPackage(MainActivity.this.getPackageName());
                                    ComponentName componentName = intent.getComponent();
                                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                                    MainActivity.this.startActivity(mainIntent);
                                    Runtime.getRuntime().exit(0);
                                } else {
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

    private void registerToken() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                Log.d("REGISTER_TOKEN", "SUCCESS");
                            } else {
                                Log.d("REGISTER_TOKEN", "FAIL");
                            }
                        } else {
                            Log.d("REGISTER_TOKEN", "FAIL");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("REGISTER_TOKEN", "FAIL");
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
                params.put("token", firebaseToken);
                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.75f;
        wm.updateViewLayout(container, p);
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
    public void onBackPressed() {
        // FragmentHome OnBackPressed
        if (fragmentsUser.get(0) != null) {
            if (fragmentsUser.get(0).isVisible()) {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    adCount = 0;

                    FragmentStations fs = (FragmentStations) fragmentsUser.get(0);
                    fs.rootView = null;

                    FragmentNews fn = (FragmentNews) fragmentsUser.get(1);
                    fn.rootView = null;

                    FragmentAutomobile fa = (FragmentAutomobile) fragmentsUser.get(2);
                    fa.view = null;

                    FragmentProfile fp = (FragmentProfile) fragmentsUser.get(3);
                    fp.rootView = null;

                    FragmentSettings fsettings = (FragmentSettings) fragmentsUser.get(4);
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

        if (fragmentsUser.get(1) != null) {
            if (fragmentsUser.get(1).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragmentsUser.get(2) != null) {
            if (fragmentsUser.get(2).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragmentsUser.get(3) != null) {
            if (fragmentsUser.get(3).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }

        if (fragmentsUser.get(4) != null) {
            if (fragmentsUser.get(4).isVisible()) {
                bottomNavigation.setCurrentItem(0);
                mFragNavController.switchTab(FragNavController.TAB1);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (queue != null) {
            queue.cancelAll(this);
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

    /**
     * Called when a tab has been selected (clicked)
     *
     * @param position    int: Position of the selected tab
     * @param wasSelected boolean: true if the tab was already selected
     * @return boolean: true for updating the tab UI, false otherwise
     */
    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        mFragNavController.switchTab(position);
        return true;
    }
}