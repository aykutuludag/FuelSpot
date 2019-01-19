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
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.FragmentNews;
import com.fuelspot.FragmentSettings;
import com.fuelspot.FragmentStations;
import com.fuelspot.R;
import com.fuelspot.model.StationItem;
import com.google.android.gms.maps.MapsInitializer;
import com.kobakei.ratethisapp.RateThisApp;
import com.ncapdevi.fragnav.FragNavController;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.hasDoubleRange;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class AdminMainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    // General variables for SuperUser
    public static int isStationVerified, isMobilePaymentAvailable, isDeliveryAvailable, superStationID;
    public static float ownedGasolinePrice, ownedDieselPrice, ownedLPGPrice, ownedElectricityPrice;
    public static String userStations, superLicenseNo, superStationName, superStationAddress, superStationCountry, superStationLocation, superStationLogo, superGoogleID, superFacilities, superLastUpdate;

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
        isDeliveryAvailable = prefs.getInt("isDeliveryAvailable", 0);
        superLastUpdate = prefs.getString("SuperLastUpdate", "");
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

        // Last location
        locLastKnown = new Location("");
        locLastKnown.setLatitude(Double.parseDouble(userlat));
        locLastKnown.setLongitude(Double.parseDouble(userlon));

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
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_news, R.drawable.tab_news, R.color.newsPage);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_stations, R.drawable.tab_stations, R.color.stationPage);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_profile, R.drawable.tab_profile, R.color.commentPage);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_settings, R.drawable.tab_settings, R.color.addOrEditPage);

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

            premium = ownedSkus.contains("premium");
            prefs.edit().putBoolean("hasPremium", premium).apply();

            hasDoubleRange = ownedSkus.contains("2x_range");
            prefs.edit().putBoolean("hasDoubleRange", premium).apply();
        }
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

    /*void openStationChoosePopup(View parent) {
        if (listOfStation != null && listOfStation.size() > 0) {
            if (listOfStation.get(listOfStation.size() - 1).getID() != -999) {
                StationItem item = new StationItem();
                item.setID(-999);
                item.setVicinity(getString(R.string.add_station));
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
    }*/

    /*void changeStation(int position) {
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

        superLastUpdate = item.getLastUpdated();

        getSuperVariables(prefs);

        FragmentMyStation frag = (FragmentMyStation) fragments.get(0);
        if (frag != null) {
            frag.checkLocationPermission();
        }

        Snackbar.make(findViewById(R.id.pager), "İSTASYON SEÇİLDİ: " + superStationName, Snackbar.LENGTH_SHORT).show();
    }*/

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
    public void onResume() {
        super.onResume();
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
