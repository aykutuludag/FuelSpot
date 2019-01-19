package com.fuelspot;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

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

import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.FragmentAutomobile.purchaseKilometers;
import static com.fuelspot.FragmentAutomobile.purchaseLiters;
import static com.fuelspot.FragmentAutomobile.purchasePrices;
import static com.fuelspot.FragmentAutomobile.purchaseTimes;
import static com.fuelspot.FragmentAutomobile.purchaseUnitPrice;
import static com.fuelspot.FragmentAutomobile.purchaseUnitPrice2;
import static com.fuelspot.FragmentAutomobile.vehiclePurchaseList;
import static com.fuelspot.MainActivity.vehicleID;

public class UserPurchases extends AppCompatActivity {

    Window window;
    Toolbar toolbar;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    SwipeRefreshLayout swipeContainer;
    RequestQueue requestQueue;


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

        coloredBars(Color.parseColor("#0288D1"), Color.parseColor("#03A9F4"));

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

    public void loadPurchases() {
        mAdapter = new PurchaseAdapter(UserPurchases.this, vehiclePurchaseList);
        mLayoutManager = new GridLayoutManager(UserPurchases.this, 1);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        swipeContainer.setRefreshing(false);
    }

    public void fetchPurchases() {
        vehiclePurchaseList.clear();
        purchaseTimes.clear();
        purchaseKilometers.clear();
        purchasePrices.clear();
        purchaseUnitPrice.clear();
        purchaseUnitPrice2.clear();
        purchaseLiters.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_PURCHASES),
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
                                    vehiclePurchaseList.add(item);

                                    purchaseTimes.add(i, obj.getString("time"));
                                    purchaseUnitPrice.add(i, obj.getDouble("fuelPrice"));
                                    purchaseUnitPrice2.add(i, obj.getDouble("fuelPrice2"));
                                    purchasePrices.add(i, obj.getDouble("totalPrice"));
                                    purchaseKilometers.add(i, obj.getInt("kilometer"));
                                    purchaseLiters.add(i, obj.getDouble("fuelLiter") + obj.getDouble("fuelLiter2"));

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
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("vehicleID", String.valueOf(vehicleID));
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
    public void onResume() {
        super.onResume();
        loadPurchases();
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