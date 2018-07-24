package com.fuelspot;


import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.maps.MapsInitializer;
import com.kobakei.ratethisapp.RateThisApp;
import com.ncapdevi.fragnav.FragNavController;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Static values START
    public static final int REQUEST_EXTERNAL_STORAGE = 0;
    public static final int REQUEST_LOCATION = 1;
    public static final int UNIFIED_REQUEST = 99;
    public static final int GOOGLE_LOGIN = 100;
    public static final int GOOGLE_PLACE_AUTOCOMPLETE = 1320;
    public static final int PURCHASE_NORMAL_PREMIUM = 1000;
    public static final int PURCHASE_ADMIN_PREMIUM = 1001;

    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String PERMISSIONS_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    public static int adCount;

    public static float TAX_GASOLINE;
    public static float TAX_DIESEL;
    public static float TAX_LPG;
    public static float TAX_ELECTRICITY;

    public static boolean premium, isSigned, isSuperUser, isGlobalNews;
    public static float userlat, userlon, averageCons, averagePrice;
    public static String name, email, photo, carPhoto, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, username, carBrand, carModel, userUnit;
    public static int fuelPri, fuelSec, kilometer;
    public static float mapDefaultZoom;
    public static int mapDefaultRange;
    public static int openCount;

    public static String[] acura_models = {"RSX"};
    public static String[] alfaRomeo_models = {"33", "75", "145", "146", "147", "155", "156", "159", "164", "166", "Brera", "Giulia", "Giulietta", "GT", "MiTo", "Spider"};
    public static String[] anadol_models = {"A"};
    public static String[] astonMartin_models = {"DB7", "DB9", "DB11", "DBS", "Rapide", "Vanquish", "Vantage", "Virage"};
    public static String[] audi_models = {"A1", "A3", "A4", "A5", "A6", "A7", "A8", "R8", "RS", "TT", "80 Series", "90 Series", "100 Series", "200 Series"};
    public static String[] bentley_models = {"Arnage", "Bentayga", "Continental", "Flying Spur", "Mulsanne"};
    public static String[] bmw_models = {"1 Series", "2 Series", "3 Series", "4 Series", "5 Series", "6 Series", "7 Series", "8 Series", "i Series", "M Series", "Z Series"};
    public static String[] bugatti_models = {"Chiron"};
    public static String[] buick_models = {"Century", "Park Avenue", "Regal", "Roadmaster"};
    public static String[] cadillac_models = {"BLS", "Brougham", "CTS", "DeVille", "Fleetwood", "Seville", "STS"};
    public static String[] cherry_models = {"Alia", "Chance", "Kimo", "Niche"};
    public static String[] chevrolet_models = {"Aveo", "Camaro", "Caprice", "Celebrity", "Corvette", "Cruze", "Epica", "Evanda", "Geo Storm", "Impala", "Kalos", "Lacetti", "Rezzo", "Spark"};
    public static String[] chyrsler_models = {"300 C", "300 M", "Concorde", "Crossfire", "Le Baron", "LHS", "Neon", "PT Cruiser", "Sebring", "Stratus"};
    public static String[] citroen_models = {"BX", "C-Elysée", "C1", "C2", "C3", "C3 Picasso", "C4", "C4 Picasso", "C4 Grand Picasso", "C5", "C6", "C8", "Evasion", "Saxo", "Xantia", "XM", "Xsara", "ZX"};
    public static String[] dacia_models = {"1304", "1310", "Lodgy", "Logan", "Sandero", "Solenza"};
    public static String[] daeweo_models = {"Chairman", "Espero", "Lanos", "Leganza", "Matiz", "Nexia", "Nubira", "Racer", "Super Saloon", "Tico"};
    public static String[] daihatsu_models = {"Applause", "Charade", "Cuore", "Materia", "Move", "Sirion", "YRV"};
    public static String[] dodge_models = {"Avenger", "Challenger", "Charger", "Intrepid", "Magnum", "Viper"};
    public static String[] ds_models = {"DS3", "DS4", "DS4 Crossback", "DS5"};
    public static String[] eagle_models = {"Talon"};
    public static String[] ferrari_models = {"360", "430", "456", "488", "458", "512", "575", "599", "612", "California", "F355", "FF", "F Series"};
    public static String[] fiat_models = {"124 Spider", "126 Bis", "500 Family", "Albea", "Barchetta", "Brava", "Bravo", "Coupe", "Croma", "Egea", "Idea", "Linea", "Marea", "Mirafiori", "Palio", "Panda", "Punto", "Regata", "Sedici", "Seicento", "Siena", "Stilo", "Tempra", "Tipo", "Ulvsse"};
    public static String[] ford_models = {"B-Max", "C-Max", "Cougar", "Crown Victoria", "Escort", "Festiva", "Fiesta", "Focus", "Fusion", "Galaxy", "Granada", "Granada C-Max", "Ka", "Mondeo", "Mustang", "Probe", "Puma", "Scorpio", "Sierra", "Taunus", "Taurus", "Thunderbird"};
    public static String[] gaz_models = {"Volga"};
    public static String[] geely_models = {"Echo", "Emgrand", "Familia", "FC"};
    public static String[] honda_models = {"Accord", "City", "Civic", "CRX", "CR-Z", "Integra", "Jazz", "Legend", "Prelude", "S2000", "Shuttle", "S-MX", "Stream"};
    public static String[] hyundai_models = {"Accent", "Accent Blue", "Accent Era", "Atos", "Coupe", "Dynasty", "Elentra", "Excel", "Genesis", "Getz", "Grandeur", "Ioniq", "i10", "i20", "i20 Active", "i20 Troy", "i30", "i40", "iX200", "Matrix", "S-Coupe", "Sonata", "Trajet"};
    public static String[] ikco_models = {"Samand"};
    public static String[] infiniti_models = {"G", "I30", "Q30", "Q50"};
    public static String[] isuzu_models = {"Gemini"};
    public static String[] jaguar_models = {"Daimler", "F-Type", "Sovereign", "S-Type", "XE", "XF", "XJ", "XJR", "XJS", "XK8", "XKR", "X-Type"};
    public static String[] kia_models = {"Capital", "Carens", "Ceed", "Cerato", "Clarus", "Magentis", "Opirus", "Optima", "Picanto", "Pride", "Pro Ceed", "Rio", "Sephia", "Shuma", "Soul", "Venga"};
    public static String[] kral_models = {"Grande-5"};
    public static String[] lada_models = {"Kalina", "Nova", "Priora", "Samara", "Tavria", "Vaz", "Vega"};
    public static String[] lamborghini_models = {"Aventador", "Gallardo", "Huracan"};
    public static String[] lancia_models = {"Delta", "Thema", "Y (Ypsilon)"};
    public static String[] lexus_models = {"IS", "LS", "RC"};
    public static String[] lincoln_models = {"Continental", "LS", "MKS", "Town Car"};
    public static String[] lotus_models = {"Elisa", "Esprit"};
    public static String[] maserati_models = {"Series 4", "Cambiocorsa", "Ghibli", "GranCabrio", "GranTurismo", "GT", "Quattroporte", "Spyder"};
    public static String[] maybach_models = {"62"};
    public static String[] mazda_models = {"2", "3", "5", "6", "121", "323", "626", "929", "Lantis", "MX", "Premacy", "RX", "Xedos"};
    public static String[] mercedes_models = {"A", "AMG GT", "B", "C", "CL", "CLA", "CLC", "CLK", "CLS", "E", "Maybach S", "R", "S", "SL", "SLC", "SLK", "SLS AMG", "190", "200", "220", "230", "240", "250", "260", "280", "300", "400", "420", "500", "560"};
    public static String[] mercury_models = {"Sable"};
    public static String[] mg_models = {"F", "ZR"};
    public static String[] mini_models = {"Cooper", "Cooper Clubman", "Cooper S", "John Cooper", "One"};
    public static String[] mitsubishi_models = {"Attrage", "Carisma", "Colt", "Eclipse", "Galant", "Grandis", "Lancer", "Lancer Evolution", "Space Star", "Space Wagon"};
    public static String[] moskwitsch_models = {"1500 SL", "Aleko"};
    public static String[] nissan_models = {"200 SX", "350 Z", "Almera", "Bluebird", "GT-R", "Maxima", "Micra", "Note", "NX Coupe", "Primera", "Pulsar", "Sunny"};
    public static String[] oldsmobile_models = {"Cutlass Ciera"};
    public static String[] opel_models = {"Adam", "Agila", "Ampera", "Ascona", "Astra", "Calibra", "Cascada", "Corsa", "GT (Roadster)", "Insignia", "Insignia Grand Sport", "Insignia Sports Tourer", "Kadett", "Manta", "Meriva", "Omega", "Rekord", "Senator", "Signum", "Tigra", "Vectra", "Zafira"};
    public static String[] pagani_models = {"Huayra"};
    public static String[] peugeot_models = {"106", "107", "205", "206", "206+", "207", "208", "301", "305", "306", "307", "308", "309", "405", "406", "407", "508", "605", "607", "806", "807", "RCZ"};
    public static String[] plymouth_models = {"Laser"};
    public static String[] pontiac_models = {"Firebird", "Grand Am", "Sunbird"};
    public static String[] porsche_models = {"718", "911", "928", "944", "Boxster", "Cayman", "Panamera"};
    public static String[] proton_models = {"218", "315", "415", "416", "418", "420", "Gen-2", "Persona", "Saga", "Savvy", "Waja"};
    public static String[] renault_models = {"Clio", "Espace", "Fluence", "Fluence Z.E.", "Laguna", "Latitude", "Megane", "Modus", "Safrane", "Symbol", "Twizy", "ZOE", "Scenic", "Grand Scenic", "Talisman", "Twingo", "R 5", "R 9", "R 11", "R 12", "R 19", "R 21", "R 25"};
    public static String[] rollsRoyce_models = {"Ghost", "Park Ward", "Phantom", "Wraith"};
    public static String[] rover_models = {"25", "45", "75", "200", "214", "216", "218", "220", "400", "414", "416", "420", "620", "820", "Streetwise"};
    public static String[] saab_models = {"900", "9000", "9-3", "9-5"};
    public static String[] seat_models = {"Alhambra", "Altea", "Exeo", "Ibiza", "Leon", "Marbella", "Toledo"};
    public static String[] skoda_models = {"Citigo", "Fabia", "Favorit", "Felicia", "Forman", "Octavia", "Rapid", "Roomster", "Superb"};
    public static String[] smart_models = {"Forfour", "Fortwo", "Roadster"};
    public static String[] subaru_models = {"BRZ", "Impreza", "Justy", "Legacy", "Levorg", "SVX", "Vivio"};
    public static String[] suzuki_models = {"Alto", "Baleno", "Ignis", "Liana", "Maruti", "S-Cross", "Splash", "Swift", "SX4"};
    public static String[] tata_models = {"Indica", "Indigo", "Manza", "Marina", "Vista"};
    public static String[] tesla_models = {"Model S", "Model X"};
    public static String[] tofas_models = {"Doğan", "Kartal", "Murat", "Serçe", "Şahin"};
    public static String[] toyota_models = {"Auris", "Avensis", "Camry", "Carina", "Celica", "Corolla", "Corona", "Cressida", "Grown", "GT 86", "MR2", "Prius", "Starlet", "Supra", "Tercel", "Urban Cruiser", "Verso", "Yaris"};
    public static String[] vw_models = {"Arteon", "Bora", "EOS", "Golf", "Jetta", "Lupo", "New Beetle", "The Beetle", "Passat", "Passat Variant", "Phaeton", "Polo", "Santana", "Scirocco", "Sharan", "Touran", "Vento", "VW CC"};
    public static String[] volvo_models = {"C30", "C70", "S40", "S60", "S70", "S80", "S90", "V40", "V40 Cross Country", "V50", "V60", "V70", "V90 Cross Country", "240", "244", "440", "460", "480", "740", "850", "940", "960"};
    // Static values END

    public static ArrayList<String> purchaseTimes = new ArrayList<>();
    public static ArrayList<Double> purchaseUnitPrice = new ArrayList<>();
    public static ArrayList<Double> purchaseUnitPrice2 = new ArrayList<>();
    public static ArrayList<Double> purchasePrices = new ArrayList<>();
    public static ArrayList<Integer> purchaseKilometers = new ArrayList<>();
    public static ArrayList<Double> purchaseLiters = new ArrayList<>();

    static InterstitialAd facebookInterstitial;
    static com.google.android.gms.ads.InterstitialAd admobInterstitial;
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    Window window;
    Toolbar toolbar;
    boolean doubleBackToExitPressedOnce;
    SharedPreferences prefs;
    FragNavController mFragNavController;

    public static int getIndexOf(String[] strings, String item) {
        for (int i = 0; i < strings.length; i++) {
            if (item.equals(strings[i])) return i;
        }
        return -1;
    }

    public static void getVariables(SharedPreferences prefs) {
        name = prefs.getString("Name", "");
        email = prefs.getString("Email", "");
        photo = prefs.getString("ProfilePhoto", "http://fuel-spot.com/FUELSPOTAPP/default_icons/default_profile.pngfile.png");
        carPhoto = prefs.getString("CarPhoto", "http://fuel-spot.com/FUELSPOTAPP/default_icons/vehicle.png");
        gender = prefs.getString("Gender", "");
        birthday = prefs.getString("Birthday", "01/01/2000");
        location = prefs.getString("Location", "");
        username = prefs.getString("UserName", "");
        carBrand = prefs.getString("carBrand", "");
        carModel = prefs.getString("carModel", "");
        fuelPri = prefs.getInt("FuelPrimary", 0);
        fuelSec = prefs.getInt("FuelSecondary", -1);
        kilometer = prefs.getInt("Kilometer", 0);
        userlat = prefs.getFloat("lat", 39.925054f);
        userlon = prefs.getFloat("lon", 32.8347552f);
        premium = prefs.getBoolean("hasPremium", false);
        isSigned = prefs.getBoolean("isSigned", false);
        isSuperUser = prefs.getBoolean("isSuperUser", false);
        userCountry = prefs.getString("userCountry", "");
        userCountryName = prefs.getString("userCountryName", "");
        userDisplayLanguage = prefs.getString("userLanguage", "");
        userUnit = prefs.getString("userUnit", "ℓ");
        currencyCode = prefs.getString("userCurrency", "");
        averageCons = prefs.getFloat("averageConsumption", 0);
        averagePrice = prefs.getFloat("averagePrice", 0);
        TAX_GASOLINE = prefs.getFloat("taxGasoline", 0);
        TAX_DIESEL = prefs.getFloat("taxDiesel", 0);
        TAX_LPG = prefs.getFloat("taxLPG", 0);
        TAX_ELECTRICITY = prefs.getFloat("taxElectricity", 0);
        isGlobalNews = prefs.getBoolean("isGlobalNews", false);
        mapDefaultRange = prefs.getInt("RANGE", 5000);
        mapDefaultZoom = prefs.getFloat("ZOOM", 12.25f);
    }

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    public static boolean verifyStoragePermissions(Context context) {
        boolean hasStorage = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                hasStorage = true;
            }
        } else {
            hasStorage = true;
        }
        return hasStorage;
    }

    public static String stationPhotoChooser(String stationName) {
        String photoURL;
        if (stationName.contains("Shell")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/shell.png";
        } else if (stationName.contains("Opet")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/opet.jpg";
        } else if (stationName.contains("BP")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/bp.png";
        } else if (stationName.contains("Kadoil")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kadoil.jpg";
        } else if (stationName.contains("Petrol Ofisi")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/petrol-ofisi.png";
        } else if (stationName.contains("Lukoil")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/lukoil.jpg";
        } else if (stationName.contains("TP")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/turkiye-petrolleri.jpg";
        } else {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/unknown.png";
        }
        return photoURL;
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

    public static float taxCalculator(int fuelType, float price) {
        float tax;
        switch (fuelType) {
            case 0:
                tax = price * TAX_GASOLINE;
                break;
            case 1:
                tax = price * TAX_DIESEL;
                break;
            case 2:
                tax = price * TAX_LPG;
                break;
            default:
                tax = price * TAX_ELECTRICITY;
                break;
        }
        return tax;
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
        getSupportActionBar().setIcon(R.drawable.brand_logo);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#000000"), Color.parseColor("#ffffff"));

        // Activate map
        MapsInitializer.initialize(this.getApplicationContext());

        //Bottom navigation
        AHBottomNavigation bottomNavigation = findViewById(R.id.bottom_navigation);
        //Add tabs
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_stations, R.drawable.tab_stations, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_news, R.drawable.tab_news, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_vehicle, R.drawable.tab_vehicle, R.color.colorAccent);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_profile, R.drawable.tab_profile, R.color.colorAccent);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_settings, R.drawable.tab_settings, R.color.colorAccent);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.addItem(item5);

        // Bottombar Settings
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // BottomNavigationListener
        FragNavController.Builder builder = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.mainContainer);

        List<Fragment> fragments = new ArrayList<>(5);
        fragments.add(FragmentStations.newInstance());
        fragments.add(FragmentNews.newInstance());
        fragments.add(FragmentVehicle.newInstance());
        fragments.add(FragmentProfile.newInstance());
        fragments.add(FragmentSettings.newInstance());

        builder.rootFragments(fragments);
        mFragNavController = builder.build();

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                mFragNavController.switchTab(position);
                return true;
            }
        });

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);

        if (savedInstanceState == null) {
            mFragNavController.switchTab(0);
        }

        //In-App Services
        buyPremiumPopup();
        InAppBilling();

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);
    }

    private void buyPremiumPopup() {
        openCount = prefs.getInt("howMany", 0);
        openCount++;
        prefs.edit().putInt("howMany", openCount).apply();

        if (openCount >= 20 && !premium) {
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
                prefs.edit().putInt("RANGE", 50000).apply();
                prefs.edit().putFloat("ZOOM", 10f).apply();
            } else {
                premium = false;
                prefs.edit().putBoolean("hasPremium", premium).apply();
            }
        }
    }

    public void buyPremium() throws RemoteException, IntentSender.SendIntentException {
        Toast.makeText(MainActivity.this,
                "Premium sürüme geçerek uygulama içerisindeki tüm reklamları kaldırabilirsiniz. Ayrıca 50 km'ye kadar çevrenizdeki bütün istasyon fiyatlarını görebilirsiniz.", Toast.LENGTH_LONG)
                .show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "premium", "subs",
                "/tYMgwhg1DVikb4R4iLNAO5pNj/QWh19+vwajyUFbAyw93xVnDkeTZFdhdSdJ8M");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        switch (requestCode) {
            case PURCHASE_NORMAL_PREMIUM:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Satın alma başarılı. Premium sürüme geçiriliyorsunuz, teşekkürler!", Toast.LENGTH_LONG).show();
                    prefs.edit().putBoolean("hasPremium", true).apply();
                    prefs.edit().putInt("RANGE", 50000).apply();
                    prefs.edit().putFloat("ZOOM", 10f).apply();
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

        //Thanks to this brief code, we can call onActivityResult in a fragment
        Fragment fragment = mFragNavController.getCurrentFrag();
        if (fragment != null && fragment.isVisible()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConn != null) {
            unbindService(mServiceConn);
        }
        if (facebookInterstitial != null) {
            facebookInterstitial.destroy();
        }
    }
}