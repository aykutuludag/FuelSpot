package com.fuelspot.superuser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.FragmentNews;
import com.fuelspot.FragmentProfile;
import com.fuelspot.FragmentStations;
import com.fuelspot.R;
import com.kobakei.ratethisapp.RateThisApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.username;

public class AdminMainActivity extends AppCompatActivity {

    // General variables for SuperUser
    public static int isSuperVerified, superStationID;
    public static boolean superPremium;
    public static double ownedGasolinePrice, ownedDieselPrice, ownedLPGPrice, ownedElectricityPrice;
    public static String superStationName, superStationLocation, superStationLogo, superStationAddress, userPhoneNumber, contractPhoto, superGoogleID;

    boolean doubleBackToExitPressedOnce;
    RequestQueue queue;
    MyPagerAdapter mSectionsPagerAdapter;
    PagerTitleStrip pagertabstrip;
    ViewPager mViewPager;
    Window window;
    Toolbar toolbar;
    SharedPreferences prefs;

    public static void getSuperVariables(SharedPreferences prefs) {
        superStationID = prefs.getInt("SuperStationID", 0);
        superGoogleID = prefs.getString("SuperGoogleID", "");
        superStationName = prefs.getString("SuperStationName", "");
        superStationLocation = prefs.getString("SuperStationLocation", "");
        superStationAddress = prefs.getString("SuperStationAddress", "");
        superStationLogo = prefs.getString("SuperStationLogo", "");
        contractPhoto = prefs.getString("contractPhoto", "");
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
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
        mSectionsPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), AdminMainActivity.this);
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        pagertabstrip = findViewById(R.id.pager_title_strip);
        pagertabstrip.setBackgroundColor(Color.TRANSPARENT);

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        queue = Volley.newRequestQueue(this);
        getVariables(prefs);
        getSuperVariables(prefs);

        // AppRater
        RateThisApp.onCreate(this);
        RateThisApp.showRateDialogIfNeeded(this);

        if (savedInstanceState == null) {
            Fragment fragment = new FragmentOwnedStation();
            getSupportFragmentManager().beginTransaction().replace(R.id.pager, fragment, "OwnedStation").commit();
        }

        createLocalDatabase();
        fetchSuperUser();
    }

    void fetchSuperUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_FETCH_PROFILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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

                            checkVerifyStatus();
                        } catch (JSONException e) {
                            e.printStackTrace();
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

    private void checkVerifyStatus() {
        if (isSuperVerified == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminMainActivity.this);
            builder.setMessage("Hesabınız onay sürecindedir. En kısa zamanda bir temsilcimiz sizinle iletişime geçecektir. Teşekkürler...");
            builder.setCancelable(false);
            builder.setIcon(R.drawable.onaybekleniyor);
            builder.setNeutralButton("Tamam", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AdminMainActivity.this.finish();
                }
            });
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                        AdminMainActivity.this.finish();
                    return false;
                }
            });
            builder.create();
            builder.show();
        }
    }

    public void createLocalDatabase() {
        //Create databese
        SQLiteDatabase mobiledatabase = openOrCreateDatabase("fuelspot_local", MODE_PRIVATE, null);
        mobiledatabase.execSQL("CREATE TABLE IF NOT EXISTS fuelspot_local(Title TEXT,Thumbnail VARCHAR, Link VARCHAR, Date TEXT);");
        SQLiteDatabase mobiledatabase2 = openOrCreateDatabase("fuelspot_local2", MODE_PRIVATE, null);
        mobiledatabase2.execSQL("CREATE TABLE IF NOT EXISTS fuelspot_local2(Title TEXT,Thumbnail VARCHAR, Link VARCHAR, Date TEXT);");
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
                }
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
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(AdminMainActivity.this, "Premium üyeliğiniz başlamıştır. Aktif ediliyor...", Toast.LENGTH_LONG).show();
                prefs.edit().putBoolean("hasPremium", true).apply();
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    this.finish();
                }
            } else {
                Toast.makeText(AdminMainActivity.this, "Satın alma başarısız. Lütfen daha sonra tekrar deneyiniz.",
                        Toast.LENGTH_LONG).show();
                prefs.edit().putBoolean("hasPremium", false).apply();
            }
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private Fragment fragment = null;
        private Context mContext;

        MyPagerAdapter(FragmentManager fm, Context c) {
            super(fm);
            mContext = c;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    fragment = new FragmentOwnedStation();
                    break;
                case 1:
                    fragment = new FragmentStations();
                    break;
                case 2:
                    fragment = new FragmentNews();
                    break;
                case 3:
                    fragment = new FragmentProfile();
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mContext.getString(R.string.title_station);
                case 1:
                    return mContext.getString(R.string.title_nearStations);
                case 2:
                    return mContext.getString(R.string.title_news);
                case 3:
                    return mContext.getString(R.string.title_profile);
            }
            return null;
        }
    }
}