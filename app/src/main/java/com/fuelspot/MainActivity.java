package com.fuelspot;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.fuelspot.receiver.AlarmReceiver;
import com.google.android.gms.maps.MapsInitializer;
import com.kobakei.ratethisapp.RateThisApp;
import com.ncapdevi.fragnav.FragNavController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    public static final int REQUEST_STORAGE = 0;
    public static final int REQUEST_LOCATION = 1;
    public static final int REQUEST_ALL = 2;

    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static String FENCE_RECEIVER_ACTION = "com.fuelspotFENCE_RECEIVER_ACTION";

    public static final int GOOGLE_LOGIN = 100;
    public static final int GOOGLE_PLACE_AUTOCOMPLETE = 1320;
    public static final int PURCHASE_NORMAL_PREMIUM = 1000;
    public static final int PURCHASE_ADMIN_PREMIUM = 1001;

    // Diameter of 50m circle
    public static int mapDefaultStationRange = 50;

    public static String universalTimeFormat = "yyyy-MM-dd HH:mm:ss";
    public static String shortTimeFormat = "dd-MMM HH:mm";
    public static boolean premium, isSigned, isSuperUser, isGeofenceOpen;
    public static float averageCons, userFSMoney, averagePrice, mapDefaultZoom, TAX_GASOLINE, TAX_DIESEL, TAX_LPG, TAX_ELECTRICITY;
    public static int carbonEmission, vehicleID, fuelPri, fuelSec, kilometer, openCount, mapDefaultRange, adCount;
    public static String userVehicles, userPhoneNumber, plateNo, userlat, userlon, name, email, photo, carPhoto, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, currencySymbol, username, carBrand, carModel, userUnit, userFavorites;

    static SharedPreferences prefs;
    //In-App Billings
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    Window window;
    Toolbar toolbar;
    boolean doubleBackToExitPressedOnce;
    FragNavController mFragNavController;
    public static List<Fragment> fragments = new ArrayList<>(5);
    AHBottomNavigation bottomNavigation;
    ListPopupWindow popupWindow;
    // Static values END

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
        userlat = prefs.getString("lat", "39.925054");
        userlon = prefs.getString("lon", "32.8347552");
        premium = prefs.getBoolean("hasPremium", false);
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
        userVehicles = prefs.getString("userVehicles", "");
        plateNo = prefs.getString("plateNo", "");
        vehicleID = prefs.getInt("vehicleID", 0);
        carbonEmission = prefs.getInt("carbonEmission", 0);
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
        currencySymbol = prefs.getString("userCurrencySymbol", "");
        userFSMoney = prefs.getFloat("userFSMoney", 0);
        userFavorites = prefs.getString("userFavorites", "");
    }

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    public static boolean verifyFilePickerPermission(Context context) {
        boolean hasStorage = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                hasStorage = true;
            }
        } else {
            hasStorage = true;
        }
        return hasStorage;
    }

    // Updated on Dec 22, 2018
    public static String stationPhotoChooser(String stationName) {
        String photoURL;
        switch (stationName) {
            case "Akçagaz":
                photoURL = "https://fuel-spot.com/station_icons/akcagaz.jpg";
                break;
            case "Akpet":
                photoURL = "https://fuel-spot.com/station_icons/akpet.jpg";
                break;
            case "Alpet":
                photoURL = "https://fuel-spot.com/station_icons/alpet.jpg";
                break;
            case "Anadolugaz":
                photoURL = "https://fuel-spot.com/station_icons/anadolugaz.jpg";
                break;
            case "Antoil":
                photoURL = "https://fuel-spot.com/station_icons/antoil.jpg";
                break;
            case "Aygaz":
                photoURL = "https://fuel-spot.com/station_icons/aygaz.jpg";
                break;
            case "Aytemiz":
                photoURL = "https://fuel-spot.com/station_icons/aytemiz.jpg";
                break;
            case "Best":
                photoURL = "https://fuel-spot.com/station_icons/best.jpg";
                break;
            case "BP":
                photoURL = "https://fuel-spot.com/station_icons/bp.jpg";
                break;
            case "Bpet":
                photoURL = "https://fuel-spot.com/station_icons/bpet.jpg";
                break;
            case "Çekoil":
                photoURL = "https://fuel-spot.com/station_icons/cekoil.jpg";
                break;
            case "Class":
                photoURL = "https://fuel-spot.com/station_icons/class.jpg";
                break;
            case "Damla Petrol":
                photoURL = "https://fuel-spot.com/station_icons/damla-petrol.jpg";
                break;
            case "Ecogaz":
                photoURL = "https://fuel-spot.com/station_icons/ecogaz.jpg";
                break;
            case "Energy":
                photoURL = "https://fuel-spot.com/station_icons/energy.jpg";
                break;
            case "Erk":
                photoURL = "https://fuel-spot.com/station_icons/erk.jpg";
                break;
            case "Euroil":
                photoURL = "https://fuel-spot.com/station_icons/euroil.jpg";
                break;
            case "Exengaz":
                photoURL = "https://fuel-spot.com/station_icons/exengaz.jpg";
                break;
            case "GO":
                photoURL = "https://fuel-spot.com/station_icons/go.jpg";
                break;
            case "Gulf":
                photoURL = "https://fuel-spot.com/station_icons/gulf.jpg";
                break;
            case "Güneygaz":
                photoURL = "https://fuel-spot.com/station_icons/guneygaz.jpg";
                break;
            case "Güvenal Gaz":
                photoURL = "https://fuel-spot.com/station_icons/guvenalgaz.jpg";
                break;
            case "Habaş":
                photoURL = "https://fuel-spot.com/station_icons/habas.jpg";
                break;
            case "Hisarpet":
                photoURL = "https://fuel-spot.com/station_icons/hisarpet.jpg";
                break;
            case "İpragaz":
            case "Ipragaz":
                photoURL = "https://fuel-spot.com/station_icons/ipragaz.jpg";
                break;
            case "Jetpet":
                photoURL = "https://fuel-spot.com/station_icons/jetpet.jpg";
                break;
            case "Kadoil":
                photoURL = "https://fuel-spot.com/station_icons/kadoil.jpg";
                break;
            case "Kalegaz":
                photoURL = "https://fuel-spot.com/station_icons/kalegaz.jpg";
                break;
            case "Kalepet":
                photoURL = "https://fuel-spot.com/station_icons/kalepet.jpg";
                break;
            case "K-pet":
            case "Kpet":
                photoURL = "https://fuel-spot.com/station_icons/kpet.jpg";
                break;
            case "Ligoil":
                photoURL = "https://fuel-spot.com/station_icons/ligoil.jpg";
                break;
            case "Lipetgaz":
                photoURL = "https://fuel-spot.com/station_icons/lipetgaz.jpg";
                break;
            case "Lukoil":
                photoURL = "https://fuel-spot.com/station_icons/lukoil.jpg";
                break;
            case "Memoil":
                photoURL = "https://fuel-spot.com/station_icons/memoil.jpg";
                break;
            case "Milangaz":
                photoURL = "https://fuel-spot.com/station_icons/milangaz.jpg";
                break;
            case "Mogaz":
                photoURL = "https://fuel-spot.com/station_icons/mogaz.jpg";
                break;
            case "Moil":
                photoURL = "https://fuel-spot.com/station_icons/moil.jpg";
                break;
            case "Mola":
                photoURL = "https://fuel-spot.com/station_icons/mola.jpg";
                break;
            case "Onex":
                photoURL = "https://fuel-spot.com/station_icons/opet.jpg";
                break;
            case "Opet":
                photoURL = "https://fuel-spot.com/station_icons/opet.jpg";
                break;
            case "Pacific":
                photoURL = "https://fuel-spot.com/station_icons/pacific.jpg";
                break;
            case "Parkoil":
                photoURL = "https://fuel-spot.com/station_icons/parkoil.jpg";
                break;
            case "Petline":
                photoURL = "https://fuel-spot.com/station_icons/petline.jpg";
                break;
            case "Petrol Ofisi":
                photoURL = "https://fuel-spot.com/station_icons/petrol-ofisi.jpg";
                break;
            case "Petrotürk":
                photoURL = "https://fuel-spot.com/station_icons/petroturk.jpg";
                break;
            case "Powerwax":
                photoURL = "https://fuel-spot.com/station_icons/powerwax.jpg";
                break;
            case "Qplus":
                photoURL = "https://fuel-spot.com/station_icons/qplus.jpg";
                break;
            case "Remoil":
                photoURL = "https://fuel-spot.com/station_icons/remoil.jpg";
                break;
            case "Sanoil":
                photoURL = "https://fuel-spot.com/station_icons/sanoil.jpg";
                break;
            case "Shell":
                photoURL = "https://fuel-spot.com/station_icons/shell.jpg";
                break;
            case "S-Oil":
                photoURL = "https://fuel-spot.com/station_icons/s-oil.jpg";
                break;
            case "Starpet":
                photoURL = "https://fuel-spot.com/station_icons/starpet.jpg";
                break;
            case "Sunpet":
                photoURL = "https://fuel-spot.com/station_icons/sunpet.jpg";
                break;
            case "Teco":
                photoURL = "https://fuel-spot.com/station_icons/teco.jpg";
                break;
            case "Termo":
                photoURL = "https://fuel-spot.com/station_icons/termo.jpg";
                break;
            case "Total":
                photoURL = "https://fuel-spot.com/station_icons/total.jpg";
                break;
            case "Turkuaz":
                photoURL = "https://fuel-spot.com/station_icons/turkuaz.jpg";
                break;
            case "Türkiş":
                photoURL = "https://fuel-spot.com/station_icons/turkis.jpg";
                break;
            case "Türkiye Petrolleri":
                photoURL = "https://fuel-spot.com/station_icons/turkiye-petrolleri.jpg";
                break;
            case "Türkoil":
                photoURL = "https://fuel-spot.com/station_icons/turkoil.jpg";
                break;
            case "Türkpetrol":
                photoURL = "https://fuel-spot.com/station_icons/turkpetrol.jpg";
                break;
            case "United":
                photoURL = "https://fuel-spot.com/station_icons/united.jpg";
                break;
            case "Uspet":
                photoURL = "https://fuel-spot.com/station_icons/uspet.jpg";
                break;
            default:
                photoURL = "https://fuel-spot.com/default_icons/station.jpg";
                break;
        }
        return photoURL;
    }

  /*  public static void AdMob(final Context mContext) {
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
    }*/

    public static void AlarmBuilder(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        Intent myIntent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        popupWindow = new ListPopupWindow(MainActivity.this);
        MapsInitializer.initialize(this);

        // Bottom navigation
        if (savedInstanceState == null) {
            FragNavController.Builder builder = FragNavController.newBuilder(null, getSupportFragmentManager(), R.id.mainContainer);
            fragments.add(FragmentStations.newInstance());
            fragments.add(FragmentNews.newInstance());
            fragments.add(FragmentAutomobile.newInstance());
            fragments.add(FragmentProfile.newInstance());
            fragments.add(FragmentSettings.newInstance());
            builder.rootFragments(fragments);
            mFragNavController = builder.build();

            bottomNavigation = findViewById(R.id.bottom_navigation);
            AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_stations, R.drawable.tab_stations, R.color.colorPrimaryDark);
            AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_news, R.drawable.tab_news, R.color.newsPage);
            AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_vehicle, R.drawable.tab_vehicle, R.color.purchasePage);
            AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_profile, R.drawable.tab_profile, R.color.commentPage);
            AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_settings, R.drawable.tab_settings, R.color.addOrEditPage);

            bottomNavigation.addItem(item1);
            bottomNavigation.addItem(item2);
            bottomNavigation.addItem(item3);
            bottomNavigation.addItem(item4);
            bottomNavigation.addItem(item5);

            bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
            bottomNavigation.setOnTabSelectedListener(this);
        }

        //In-App Services
        InAppBilling();

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
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

    public void checkPremium() throws RemoteException {
        Bundle ownedItems = mService.getPurchases(3, getPackageName(), "subs", null);
        if (ownedItems.getInt("RESPONSE_CODE") == 0) {
            ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            assert ownedSkus != null;
            if (ownedSkus.contains("premium")) {
                premium = true;
                prefs.edit().putBoolean("hasPremium", premium).apply();
                prefs.edit().putInt("RANGE", 5000).apply();
                prefs.edit().putFloat("ZOOM", 12f).apply();
            } else {
                premium = false;
                prefs.edit().putBoolean("hasPremium", premium).apply();
            }
        }
    }

    public void buyPremium() throws RemoteException, IntentSender.SendIntentException {
        Toast.makeText(MainActivity.this,
                "Premium sürüm reklamları kaldırır ve menzilinizi 2 katına çıkarır.", Toast.LENGTH_LONG)
                .show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "premium", "subs",
                "dfgfddfgdfgasd/sdfsffgdgfgjkjk/ajyUFbAyw93xVnDkeTZFdhdSdJ8M");
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        assert pendingIntent != null;
        startIntentSenderForResult(pendingIntent.getIntentSender(), PURCHASE_NORMAL_PREMIUM, new Intent(), 0,
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PURCHASE_NORMAL_PREMIUM:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Satın alma başarılı. Premium sürüme geçiriliyorsunuz, teşekkürler!", Toast.LENGTH_LONG).show();
                    prefs.edit().putBoolean("hasPremum", true).apply();
                    prefs.edit().putInt("RANGE", 5000).apply();
                    prefs.edit().putFloat("ZOOM", 12f).apply();
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
                break;
        }

        // Thanks to this brief code, we can call onActivityResult in a fragment
        // Currently used in FragmentStations if user revoke location permission
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