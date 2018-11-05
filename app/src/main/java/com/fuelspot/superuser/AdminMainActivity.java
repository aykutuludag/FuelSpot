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
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.FragmentNews;
import com.fuelspot.FragmentProfile;
import com.fuelspot.FragmentSettings;
import com.fuelspot.FragmentStations;
import com.fuelspot.R;
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

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.PURCHASE_ADMIN_PREMIUM;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.openCount;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class AdminMainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    // General variables for SuperUser
    public static boolean superPremium;

    public static int isStationVerified, isMobilePaymentAvailable, superStationID, isStationActive;
    public static float ownedGasolinePrice, ownedDieselPrice, ownedLPGPrice, ownedElectricityPrice;
    public static String userStations, superLicenseNo, superStationName, superStationAddress, superStationCountry, superStationLocation, superStationLogo, superGoogleID, superFacilities, superLastUpdate;

    // Multiple station
    public static List<StationItem> listOfStation = new ArrayList<>();
    ListPopupWindow popupWindow;

    boolean doubleBackToExitPressedOnce;
    RequestQueue queue;
    Window window;
    Toolbar toolbar;
    SharedPreferences prefs;
    IInAppBillingService mService;
    ServiceConnection mServiceConn;
    FragNavController mFragNavController;
    AHBottomNavigation bottomNavigation;
    List<Fragment> fragments = new ArrayList<>(5);
    Location locLastKnown;

    public static void getSuperVariables(SharedPreferences prefs) {
        // General information
        userStations = prefs.getString("userStations", "");
        superPremium = prefs.getBoolean("hasSuperPremium", false);

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
        isMobilePaymentAvailable = prefs.getInt("isMobilePaymentAvaiable", 0);
        isStationActive = prefs.getInt("isStationActive", 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

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
        popupWindow = new ListPopupWindow(this);

        // Last location
        locLastKnown = new Location("");
        locLastKnown.setLatitude(Double.parseDouble(userlat));
        locLastKnown.setLongitude(Double.parseDouble(userlon));

        // Activate map
        MapsInitializer.initialize(this.getApplicationContext());

        // Bottom navigation
        FragNavController.Builder builder = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.pager);
        fragments.add(FragmentMyStation.newInstance());
        fragments.add(FragmentStations.newInstance());
        fragments.add(FragmentProfile.newInstance());
        fragments.add(FragmentNews.newInstance());
        fragments.add(FragmentSettings.newInstance());
        builder.rootFragments(fragments);
        mFragNavController = builder.build();

        bottomNavigation = findViewById(R.id.bottom_navigation);
        //Add tabs
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_mystation, R.drawable.tab_mystation, R.color.colorPrimaryDark);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_stations, R.drawable.tab_stations, R.color.stationPage);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_profile, R.drawable.tab_profile, R.color.commentPage);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_news, R.drawable.tab_news, R.color.newsPage);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_settings, R.drawable.tab_settings, R.color.addOrEditPage);

        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.addItem(item5);

        // Bottombar Settings
        bottomNavigation.setColored(true);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        bottomNavigation.setOnTabSelectedListener(this);

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);

        //In-App Services
        buyPremiumPopup();
        InAppBilling();
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

    public void fetchUserStations() {
        listOfStation.clear();
        if (userStations != null && userStations.length() > 0) {
            String[] stationIDs = userStations.split(";");
            for (String stationID1 : stationIDs) {
                fetchSingleStation(Integer.parseInt(stationID1));
            }
            bottomNavigation.setNotification(stationIDs.length, 2);
        }
    }

    void fetchSingleStation(final int sID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                StationItem item = new StationItem();
                                item.setID(obj.getInt("id"));
                                item.setStationName(obj.getString("name"));
                                item.setVicinity(obj.getString("vicinity"));
                                item.setCountryCode(obj.getString("country"));
                                item.setLocation(obj.getString("location"));
                                item.setGoogleMapID(obj.getString("googleID"));
                                item.setLicenseNo(obj.getString("licenseNo"));
                                item.setOwner(obj.getString("owner"));
                                item.setPhotoURL(obj.getString("photoURL"));
                                item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                item.setIsVerified(obj.getInt("isVerified"));
                                item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
                                item.setIsActive(obj.getInt("isActive"));
                                item.setLastUpdated(obj.getString("lastUpdated"));

                                //DISTANCE START
                                Location loc = new Location("");
                                String[] stationKonum = item.getLocation().split(";");
                                loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                float uzaklik = locLastKnown.distanceTo(loc);
                                item.setDistance((int) uzaklik);
                                //DISTANCE END

                                listOfStation.add(item);
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
                params.put("stationID", String.valueOf(sID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    void openStationChoosePopup(View parent) {
        if (listOfStation != null && listOfStation.size() > 0) {
            if (listOfStation.get(listOfStation.size() - 1).getID() != -999) {
                StationItem item = new StationItem();
                item.setID(-999);
                item.setVicinity(getString(R.string.title_activity_add_new_station));
                listOfStation.add(item);
            }

            ListAdapter adapter = new StationChangerAdapter(AdminMainActivity.this, listOfStation);
            popupWindow.setAnchorView(parent);
            popupWindow.setAdapter(adapter);
            popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (listOfStation.get(i).getID() == -999) {
                        Intent intent = new Intent(AdminMainActivity.this, AddStation.class);
                        startActivity(intent);
                    } else {
                        changeStation(i);
                    }
                    popupWindow.dismiss();
                }
            });
            popupWindow.show();
        }
    }

    void changeStation(int position) {
        StationItem item = listOfStation.get(position);

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

        superLicenseNo = item.getLicenseNo();
        prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();

        isStationVerified = item.getIsVerified();
        prefs.edit().putInt("isStationVerified", isStationVerified).apply();

        isMobilePaymentAvailable = item.getHasSupportMobilePayment();
        prefs.edit().putInt("isMobilePaymentAvaiable", isMobilePaymentAvailable).apply();

        isStationActive = item.getIsActive();
        prefs.edit().putInt("isStationActive", isStationActive).apply();

        superLastUpdate = item.getLastUpdated();

        getSuperVariables(prefs);

        FragmentMyStation frag = (FragmentMyStation) fragments.get(0);
        if (frag != null) {
            frag.checkLocationPermission();
        }

        Snackbar.make(findViewById(R.id.pager), "İSTASYON SEÇİLDİ: " + superStationName, Snackbar.LENGTH_SHORT).show();
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
    public void onResume() {
        super.onResume();
        fetchUserStations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_showStations:
                openStationChoosePopup(bottomNavigation);
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

    public class StationChangerAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;
        private List<StationItem> mItemList;
        private Context mContext;

        StationChangerAdapter(Context context, List<StationItem> itemList) {
            mLayoutInflater = LayoutInflater.from(context);
            mContext = context;
            mItemList = itemList;
        }

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public StationItem getItem(int i) {
            return mItemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.card_station_mini, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String sName = getItem(position).getStationName();
            holder.textViewStationName.setText(sName);

            String sAddress = getItem(position).getVicinity();
            holder.textViewStationAddress.setText(sAddress);

            RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.default_station).error(R.drawable.default_station)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            Glide.with(mContext).load(getItem(position).getPhotoURL()).apply(options).into(holder.stationLogo);

            if (getItem(position).getID() == superStationID) {
                holder.stationIsSelected.setVisibility(View.VISIBLE);
            } else {
                holder.stationIsSelected.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            TextView textViewStationName, textViewStationAddress;
            CircleImageView stationLogo, stationIsSelected;

            ViewHolder(View view) {
                textViewStationName = view.findViewById(R.id.station_name);
                textViewStationAddress = view.findViewById(R.id.station_address);
                stationLogo = view.findViewById(R.id.station_photo);
                stationIsSelected = view.findViewById(R.id.stationIsSelected);
            }
        }
    }
}
