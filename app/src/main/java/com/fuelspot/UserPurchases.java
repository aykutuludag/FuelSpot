package com.fuelspot;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.PurchaseAdapter;
import com.fuelspot.model.PurchaseItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.fuelspot.FragmentAutomobile.vehiclePurchaseList;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.token;

public class UserPurchases extends AppCompatActivity {

    private Window window;
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private SwipeRefreshLayout swipeContainer;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_purchases);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        //Comments
        requestQueue = Volley.newRequestQueue(UserPurchases.this);
        mRecyclerView = findViewById(R.id.purchaseView);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPurchases();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void loadPurchases() {
        mAdapter = new PurchaseAdapter(UserPurchases.this, vehiclePurchaseList);
        mLayoutManager = new GridLayoutManager(UserPurchases.this, 1);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        swipeContainer.setRefreshing(false);
    }

    private void fetchPurchases() {
        vehiclePurchaseList.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_AUTOMOBILE_PURCHASES) + "?plateNo=" + plateNo,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    PurchaseItem item = new PurchaseItem();
                                    item.setID(obj.getInt("id"));
                                    item.setPurchaseTime(obj.getString("time"));
                                    item.setStationName(obj.getString("stationName"));
                                    item.setStationIcon(obj.getString("stationIcon"));
                                    item.setStationLocation(obj.getString("stationLocation"));
                                    item.setFuelType(obj.getInt("fuelType"));
                                    item.setFuelPrice((float) obj.getDouble("fuelPrice"));
                                    item.setFuelLiter((float) obj.getDouble("fuelLiter"));
                                    item.setFuelTax((float) obj.getDouble("fuelTax"));
                                    item.setFuelType2(obj.getInt("fuelType2"));
                                    item.setFuelPrice2((float) obj.getDouble("fuelPrice2"));
                                    item.setFuelLiter2((float) obj.getDouble("fuelLiter2"));
                                    item.setFuelTax2((float) obj.getDouble("fuelTax2"));
                                    item.setTotalPrice((float) obj.getDouble("totalPrice"));
                                    item.setBillPhoto(obj.getString("billPhoto"));
                                    item.setKilometer(obj.getInt("kilometer"));
                                    vehiclePurchaseList.add(item);

                                    mAdapter = new PurchaseAdapter(UserPurchases.this, vehiclePurchaseList);
                                    mLayoutManager = new GridLayoutManager(UserPurchases.this, 1);

                                    mAdapter.notifyDataSetChanged();
                                    mRecyclerView.setAdapter(mAdapter);
                                    mRecyclerView.setLayoutManager(mLayoutManager);
                                    swipeContainer.setRefreshing(false);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                swipeContainer.setRefreshing(false);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(UserPurchases.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        swipeContainer.setRefreshing(false);
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
        loadPurchases();
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