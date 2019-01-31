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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.model.CommentItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.FragmentProfile.userCommentList;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;

public class UserComments extends AppCompatActivity {

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
        setContentView(R.layout.activity_user_comments);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#0288D1"), Color.parseColor("#03A9F4"));

        //Comments
        requestQueue = Volley.newRequestQueue(UserComments.this);
        mRecyclerView = findViewById(R.id.commentView);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchComments();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        loadComments();
    }

    public void loadComments() {
        mAdapter = new CommentAdapter(UserComments.this, userCommentList, "USER_COMMENTS");
        mLayoutManager = new GridLayoutManager(UserComments.this, 1);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        swipeContainer.setRefreshing(false);
    }

    // Depends on user, it changes with user comments or station comments
    public void fetchComments() {
        userCommentList.clear();
        mAdapter.notifyDataSetChanged();

        final String whichApi, whichParamater, whichValue;
        if (isSuperUser) {
            whichApi = getString(R.string.API_FETCH_STATION_COMMENTS);
            whichParamater = "stationID";
            whichValue = String.valueOf(superStationID);
        } else {
            whichApi = getString(R.string.API_FETCH_USER_COMMENTS);
            whichParamater = "username";
            whichValue = username;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, whichApi,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CommentItem item = new CommentItem();
                                    item.setID(obj.getInt("id"));
                                    item.setComment(obj.getString("comment"));
                                    item.setTime(obj.getString("time"));
                                    item.setStationID(obj.getInt("station_id"));
                                    item.setUsername(obj.getString("username"));
                                    item.setProfile_pic(obj.getString("user_photo"));
                                    item.setRating(obj.getInt("stars"));
                                    item.setAnswer(obj.getString("answer"));
                                    item.setReplyTime(obj.getString("replyTime"));
                                    item.setLogo(obj.getString("logo"));
                                    userCommentList.add(item);
                                }
                                mAdapter = new CommentAdapter(UserComments.this, userCommentList, "USER_COMMENTS");
                                mLayoutManager = new GridLayoutManager(UserComments.this, 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
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
                params.put(whichParamater, whichValue);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(UserComments.this);

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
        loadComments();
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
