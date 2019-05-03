package com.fuelspot;

import android.graphics.Color;
import android.location.Location;
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
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;

public class UserFavorites extends AppCompatActivity {

    private RequestQueue requestQueue;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private List<StationItem> favoriteList = new ArrayList<>();
    private Toolbar toolbar;
    private Window window;

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

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        requestQueue = Volley.newRequestQueue(this);
        mRecyclerView = findViewById(R.id.feedView);
        GridLayoutManager mLayoutManager = new GridLayoutManager(UserFavorites.this, 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void parseUserFavorites() {
        favoriteList.clear();
        if (userFavorites != null && userFavorites.length() > 0) {
            String[] favStationss = userFavorites.split(";");

            int untilWhere;

            if (premium) {
                untilWhere = favStationss.length;
            } else {
                if (favStationss.length >= 3) {
                    untilWhere = 3;
                    Toast.makeText(UserFavorites.this, getString(R.string.less_than_3_favs), Toast.LENGTH_SHORT).show();
                } else {
                    untilWhere = favStationss.length;
                }
            }

            for (int i = 0; i < untilWhere; i++) {
                fetchStation(Integer.parseInt(favStationss[i]));
            }

            mAdapter = new StationAdapter(UserFavorites.this, favoriteList, "NEARBY_STATIONS");
            mRecyclerView.setAdapter(mAdapter);
        } else {
            Toast.makeText(UserFavorites.this, getString(R.string.no_favorite_station), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchStation(final int stationID) {
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
    public void onResume() {
        super.onResume();
        parseUserFavorites();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
