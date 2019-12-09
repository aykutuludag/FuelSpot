package com.fuelspot;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
import com.fuelspot.adapter.InboxAdapter;
import com.fuelspot.model.MessageItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.fuelspot.FragmentProfile.conversationIDs;
import static com.fuelspot.FragmentProfile.lastMessages;
import static com.fuelspot.FragmentProfile.userInbox;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.username;

public class UserInbox extends AppCompatActivity {

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
        setContentView(R.layout.activity_user_inbox);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        //Comments
        requestQueue = Volley.newRequestQueue(UserInbox.this);
        mRecyclerView = findViewById(R.id.inboxView);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchInbox();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        loadInbox();
    }

    private void loadInbox() {
        mAdapter = new InboxAdapter(UserInbox.this, lastMessages, "INBOX");
        mLayoutManager = new GridLayoutManager(UserInbox.this, 1);

        mAdapter.notifyDataSetChanged();
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        swipeContainer.setRefreshing(false);
    }

    // Depends on user, it changes with user comments or station comments
    private void fetchInbox() {
        userInbox.clear();
        lastMessages.clear();
        conversationIDs.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_INBOX) + "?username=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        swipeContainer.setRefreshing(false);
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    MessageItem item = new MessageItem();
                                    item.setID(obj.getInt("id"));
                                    item.setConversationID(obj.getInt("conversationID"));
                                    item.setSender(obj.getString("sender"));
                                    item.setSenderPhoto(obj.getString("senderPhoto"));
                                    item.setReceiver(obj.getString("receiver"));
                                    item.setReceiverPhoto(obj.getString("receiverPhoto"));
                                    item.setTopic(obj.getString("topic"));
                                    item.setMessage(obj.getString("message"));
                                    item.setIsOpen(obj.getInt("isOpen"));
                                    item.setTime(obj.getString("time"));
                                    userInbox.add(item);

                                    if (conversationIDs != null && !conversationIDs.contains(item.getConversationID())) {
                                        conversationIDs.add(item.getConversationID());
                                        lastMessages.add(item);
                                    }
                                }

                                mAdapter = new InboxAdapter(UserInbox.this, lastMessages, "INBOX");
                                mLayoutManager = new GridLayoutManager(UserInbox.this, 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setVisibility(View.VISIBLE);
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
                    public void onErrorResponse(VolleyError volleyError) {
                        swipeContainer.setRefreshing(false);
                        volleyError.printStackTrace();
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
