package com.fuelspot;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class UserRewards extends AppCompatActivity {

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
        setContentView(R.layout.activity_user_rewards);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#0288D1"), Color.parseColor("#03A9F4"));

        //Comments
        requestQueue = Volley.newRequestQueue(UserRewards.this);
        mRecyclerView = findViewById(R.id.reportView);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //   fetchReports(); >>>  loadRewards();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //   fetchReports(); >>>  loadRewards();
    }


    //fetchRewards (banking-api)
   /* public void fetchReports() {
        userReportList.clear();
        mAdapter.notifyDataSetChanged();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REPORT_FETCH),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    ReportItem item = new ReportItem();
                                    item.setID(obj.getInt("id"));
                                    item.setUsername(obj.getString("username"));
                                    item.setStationID(obj.getInt("stationID"));
                                    item.setReportType(obj.getString("report"));
                                    item.setReportMessage(obj.getString("details"));
                                    item.setReportPhoto(obj.getString("photo"));
                                    item.setPrices(obj.getString("prices"));
                                    item.setIsReviewed(obj.getInt("status"));
                                    item.setReward((float) obj.getDouble("reward"));
                                    item.setReportTime(obj.getString("reportTime"));
                                    userReportList.add(item);
                                }

                                mLayoutManager = new GridLayoutManager(UserRewards.this, 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);

             mAdapter = new ReportAdapter(UserRewards.this, userReportList);
        mLayoutManager = new GridLayoutManager(UserRewards.this, 1);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        swipeContainer.setRefreshing(false);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
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
        RequestQueue requestQueue = Volley.newRequestQueue(UserRewards.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
*/

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
