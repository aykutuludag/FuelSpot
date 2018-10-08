package com.fuelspot.superuser;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.fuelspot.FragmentProfile;
import com.fuelspot.FragmentSettings;
import com.fuelspot.FragmentStations;
import com.fuelspot.R;
import com.kobakei.ratethisapp.RateThisApp;
import com.ncapdevi.fragnav.FragNavController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.PURCHASE_ADMIN_PREMIUM;
import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.openCount;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.username;

public class AdminMainActivity extends AppCompatActivity {

    // General variables for SuperUser
    public static int isSuperVerified, superStationID;
    public static boolean superPremium;
    public static double ownedGasolinePrice, ownedDieselPrice, ownedLPGPrice, ownedElectricityPrice;
    public static String superStationName, superStationLocation, superStationLogo, superStationAddress, contractPhoto, superGoogleID;

    boolean doubleBackToExitPressedOnce;
    RequestQueue queue;
    Window window;
    Toolbar toolbar;
    SharedPreferences prefs;
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    FragNavController mFragNavController;

    public static void getSuperVariables(SharedPreferences prefs) {
        superStationID = prefs.getInt("SuperStationID", 0);
        superGoogleID = prefs.getString("SuperGoogleID", "");
        superStationName = prefs.getString("SuperStationName", "");
        superStationLocation = prefs.getString("SuperStationLocation", "");
        superStationAddress = prefs.getString("SuperStationAddress", "");
        superStationLogo = prefs.getString("SuperStationLogo", "");
        contractPhoto = prefs.getString("contractPhoto", "");

        isSuperVerified = prefs.getInt("isSuperVerified", 0);
        superPremium = prefs.getBoolean("hasSuperPremium", false);
        ownedGasolinePrice = prefs.getFloat("superGasolinePrice", 0);
        ownedDieselPrice = prefs.getFloat("superDieselPrice", 0);
        ownedLPGPrice = prefs.getFloat("superLPGPrice", 0);
        ownedElectricityPrice = prefs.getFloat("superElectricityPrice", 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.brand_logo);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#000000"), Color.parseColor("#ffffff"));

        //Bottom navigation
        AHBottomNavigation bottomNavigation = findViewById(R.id.bottom_navigation);
        //Add tabs
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_mystation, R.drawable.tab_mystation, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_stations, R.drawable.tab_stations, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_news, R.drawable.tab_news, R.color.colorAccent);
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
        FragNavController.Builder builder = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.pager);

        List<Fragment> fragments = new ArrayList<>(5);
        fragments.add(FragmentOwnedStation.newInstance());
        fragments.add(FragmentStations.newInstance());
        fragments.add(FragmentNews.newInstance());
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
        queue = Volley.newRequestQueue(this);
        getVariables(prefs);
        getSuperVariables(prefs);

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);

        if (savedInstanceState == null) {
            mFragNavController.switchTab(FragNavController.TAB1);
        }

        fetchSuperUser();

        //In-App Services
        buyPremiumPopup();
        InAppBilling();
    }

    void fetchSuperUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_FETCH),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                name = obj.getString("name");
                                prefs.edit().putString("Name", name).apply();

                                email = obj.getString("email");
                                prefs.edit().putString("Email", email).apply();

                                photo = obj.getString("photo");
                                prefs.edit().putString("ProfilePhoto", photo).apply();

                                gender = obj.getString("gender");
                                prefs.edit().putString("Gender", gender).apply();

                                birthday = obj.getString("birthday");
                                prefs.edit().putString("Birthday", birthday).apply();

                                userPhoneNumber = obj.getString("userPhone");
                                prefs.edit().putString("userPhoneNumber", userPhoneNumber).apply();

                                superStationID = obj.getInt("stationID");
                                prefs.edit().putInt("SuperStationID", superStationID).apply();

                                superGoogleID = obj.getString("googleID");
                                prefs.edit().putString("SuperGoogleID", superGoogleID).apply();

                                superStationName = obj.getString("stationName");
                                prefs.edit().putString("SuperStationName", superStationName).apply();

                                superStationLocation = obj.getString("stationLocation");
                                prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

                                superStationAddress = obj.getString("stationAddress");
                                prefs.edit().putString("SuperStationAddress", superStationAddress).apply();

                                superStationLogo = obj.getString("stationLogo");
                                prefs.edit().putString("SuperStationLogo", superStationLogo).apply();

                                contractPhoto = obj.getString("contractPhoto");
                                prefs.edit().putString("contractPhoto", contractPhoto).apply();

                                isSuperVerified = obj.getInt("isVerified");
                                prefs.edit().putInt("isSuperVerified", isSuperVerified).apply();

                                getVariables(prefs);
                                getSuperVariables(prefs);

                                if (isSuperVerified == 0) {
                                    Snackbar.make(findViewById(R.id.pager), "Hesabınız onay sürecindedir. En kısa zamanda sizinle iletişime geçeceğiz.", Snackbar.LENGTH_LONG).show();
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
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    private void buyPremiumPopup() {
        openCount = prefs.getInt("howMany", 0);
        openCount++;
        prefs.edit().putInt("howMany", openCount).apply();

        if (openCount >= 15 && !premium) {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle(R.string.buy_premium_ads_title)
                    .setMessage(R.string.buy_premium_ads_content)
                    .setPositiveButton(R.string.buy_premium_ads_okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                buyAdminPremium();
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
        Bundle ownedItems = mService.getPurchases(3, getPackageName(), "subs", null);
        if (ownedItems.getInt("RESPONSE_CODE") == 0) {
            ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            assert ownedSkus != null;
            if (ownedSkus.contains("premium_admin")) {
                premium = true;
                prefs.edit().putBoolean("hasPremium", premium).apply();
            } else {
                premium = false;
                prefs.edit().putBoolean("hasPremium", premium).apply();
            }
        }
    }

    public void buyAdminPremium() throws RemoteException, IntentSender.SendIntentException {
        Toast.makeText(AdminMainActivity.this,
                "Premium sürüme geçerek uygulama içerisindeki tüm reklamları kaldırabilirsiniz. Ayrıca 50 km'ye kadar çevrenizdeki bütün istasyon fiyatlarını görebilirsiniz.", Toast.LENGTH_LONG)
                .show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "premium_admin", "subs",
                "/tYMgwhg1DVikb4R4iLNAO5pNj/QWh19+vwajyUFbAyw93xVnDkeTZFdhdSdJ8M");
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        assert pendingIntent != null;
        startIntentSenderForResult(pendingIntent.getIntentSender(), PURCHASE_ADMIN_PREMIUM, new Intent(), 0,
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
        getMenuInflater().inflate(R.menu.menu_admin_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editPrices:
                if (isSuperVerified == 1) {
                    Intent intent = new Intent(AdminMainActivity.this, SuperEditPrices.class);
                    startActivity(intent);
                } else {
                    Snackbar.make(findViewById(R.id.pager), getString(R.string.pending_approval), Snackbar.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PURCHASE_ADMIN_PREMIUM:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(AdminMainActivity.this, "Satın alma başarılı. Premium sürüme geçiriliyorsunuz, teşekkürler!", Toast.LENGTH_LONG).show();
                    prefs.edit().putBoolean("hasPremium", true).apply();
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    if (i != null) {
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                } else {
                    Toast.makeText(AdminMainActivity.this, "Satın alma başarısız. Lütfen daha sonra tekrar deneyiniz.",
                            Toast.LENGTH_LONG).show();
                    prefs.edit().putBoolean("hasPremium", false).apply();
                }
                break;

        }

        //Irrelevant
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("Stations");
        if (fragment != null && fragment.isVisible()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
