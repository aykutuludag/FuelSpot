package com.fuelspot;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.fuelspot.adapter.StationAdapter;
import com.fuelspot.model.StationItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.userFavorites;

public class UserFavorites extends AppCompatActivity {

    RequestQueue requestQueue;
    GridLayoutManager mLayoutManager;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    List<StationItem> favoriteList = new ArrayList<>();
    Toolbar toolbar;
    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favorites);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#388E3C"), Color.parseColor("#4CAF50"));

        requestQueue = Volley.newRequestQueue(this);
        mRecyclerView = findViewById(R.id.feedView);
        mLayoutManager = new GridLayoutManager(UserFavorites.this, 1);
        mAdapter = new StationAdapter(UserFavorites.this, favoriteList, "NEARBY_STATIONS");

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        parseUserFavorites();
    }

    void parseUserFavorites() {
        if (userFavorites != null && userFavorites.length() > 0) {
            String[] favStationss = userFavorites.split(";");

            int untilWhere;

            if (premium) {
                untilWhere = favStationss.length;
            } else {
                if (favStationss.length >= 5) {
                    untilWhere = 5;
                    Toast.makeText(UserFavorites.this, "5'ten fazla favori istasyon izleyebilmek için premium sürüme geçebilirsiniz", Toast.LENGTH_SHORT).show();
                } else {
                    untilWhere = favStationss.length;
                }
            }

            for (int i = 0; i < untilWhere; i++) {
                fetchStation(Integer.parseInt(favStationss[i]));
            }
        } else {
            Toast.makeText(UserFavorites.this, "Henüz favorilerinize hiç istasyon eklememişsiniz.", Toast.LENGTH_SHORT).show();
        }
    }

    void fetchStation(final int stationID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                            item.setFacilities(obj.getString("facilities"));
                            item.setLicenseNo(obj.getString("licenseNo"));
                            item.setOwner(obj.getString("owner"));
                            item.setPhotoURL(obj.getString("logoURL"));
                            item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                            item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                            item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                            item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                            item.setIsVerified(obj.getInt("isVerified"));
                            item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
                            item.setIsActive(obj.getInt("isActive"));
                            item.setLastUpdated(obj.getString("lastUpdated"));

                            favoriteList.add(item);
                            mAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
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
                params.put("stationID", String.valueOf(stationID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
