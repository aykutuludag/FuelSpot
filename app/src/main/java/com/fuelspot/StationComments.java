package com.fuelspot;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.model.CommentItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.StationDetails.choosenStationID;
import static com.fuelspot.StationDetails.hasAlreadyCommented;
import static com.fuelspot.StationDetails.numOfComments;
import static com.fuelspot.StationDetails.stars;
import static com.fuelspot.StationDetails.stationCommentList;
import static com.fuelspot.StationDetails.stationScore;
import static com.fuelspot.StationDetails.sumOfPoints;
import static com.fuelspot.StationDetails.userComment;
import static com.fuelspot.StationDetails.userCommentID;

public class StationComments extends AppCompatActivity {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    SwipeRefreshLayout swipeContainer;
    RequestQueue requestQueue;
    PopupWindow mPopupWindow;
    Snackbar snackbar;
    Window window;
    Toolbar toolbar;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1;
    int istasyonID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_comments);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#0288D1"), Color.parseColor("#03A9F4"));

        //Comments
        requestQueue = Volley.newRequestQueue(StationComments.this);
        mRecyclerView = findViewById(R.id.commentView);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchStationComments();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        snackbar = Snackbar.make(swipeContainer, "Henüz hiç yorum yazılmamış.", Snackbar.LENGTH_LONG);

        // FABs
        materialDesignFAM = findViewById(R.id.fab_menu);

        floatingActionButton1 = findViewById(R.id.fab1);
        if (isSuperUser) {
            materialDesignFAM.setVisibility(View.GONE);
        } else {
            if (hasAlreadyCommented) {
                floatingActionButton1.setLabelText("Yorumu güncelle");
            } else {
                floatingActionButton1.setLabelText("Yorum yaz");
            }
            floatingActionButton1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    materialDesignFAM.close(true);
                    addUpdateCommentPopup(v);
                }
            });
        }

        istasyonID = getIntent().getIntExtra("ISTASYON_ID", 0);
        if (istasyonID != 0) {
            // This means superuser came for FragmentMyStation
            fetchStationComments();
        } else {
            // User came from StationDetails. Comments already fetched.
            istasyonID = choosenStationID;
            loadComments();
        }
    }

    public void loadComments() {
        if (stationCommentList != null && stationCommentList.size() > 0) {
            mAdapter = new CommentAdapter(StationComments.this, stationCommentList, "STATION_COMMENTS");
            mLayoutManager = new GridLayoutManager(StationComments.this, 1);

            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
            swipeContainer.setRefreshing(false);
        } else {
            snackbar.show();
        }
    }

    void addUpdateCommentPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) StationComments.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_comment, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        TextView titlePopup = customView.findViewById(R.id.campaignPhoto);
        if (hasAlreadyCommented) {
            titlePopup.setText("Yorumu güncelle");
        } else {
            titlePopup.setText("Yorum yaz");
        }

        Button sendAnswer = customView.findViewById(R.id.buttonSendComment);
        sendAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userComment != null && userComment.length() > 0) {
                    if (hasAlreadyCommented) {
                        updateComment();
                    } else {
                        addComment();
                    }
                } else {
                    Toast.makeText(StationComments.this, "Lütfen yorumunuzu yazınız", Toast.LENGTH_SHORT).show();
                }
            }

            private void updateComment() {
                final ProgressDialog loading = ProgressDialog.show(StationComments.this, "Updating comment...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_COMMENT),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                loading.dismiss();
                                Toast.makeText(StationComments.this, response, Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
                                fetchStationComments();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                //Showing toast
                                loading.dismiss();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        //Creating parameters
                        Map<String, String> params = new Hashtable<>();

                        //Adding parameters
                        params.put("commentID", String.valueOf(userCommentID));
                        params.put("comment", userComment);
                        params.put("stars", String.valueOf(stars));
                        params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                        //returning parameters
                        return params;
                    }
                };

                //Adding request to the queue
                requestQueue.add(stringRequest);
            }

            private void addComment() {
                final ProgressDialog loading = ProgressDialog.show(StationComments.this, "Adding comment...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_COMMENT),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                loading.dismiss();
                                Toast.makeText(StationComments.this, response, Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
                                hasAlreadyCommented = true;
                                fetchStationComments();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                //Showing toast
                                loading.dismiss();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        //Creating parameters
                        Map<String, String> params = new Hashtable<>();

                        //Adding parameters
                        params.put("comment", userComment);
                        params.put("stationID", String.valueOf(choosenStationID));
                        params.put("username", username);
                        params.put("stars", String.valueOf(stars));
                        params.put("user_photo", photo);
                        params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                        //returning parameters
                        return params;
                    }
                };

                //Adding request to the queue
                requestQueue.add(stringRequest);
            }
        });

        EditText getComment = customView.findViewById(R.id.editTextComment);
        if (userComment != null && userComment.length() > 0) {
            getComment.setText(userComment);
        }
        getComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    userComment = s.toString();
                }
            }
        });

        final RatingBar ratingBar = customView.findViewById(R.id.ratingBar);
        ratingBar.setRating(stars);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                stars = (int) rating;
            }
        });

        ImageView closeButton = customView.findViewById(R.id.imageViewClose);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    public void fetchStationComments() {
        stationCommentList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION_COMMENTS),
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
                                    item.setProfile_pic(obj.getString("user_photo"));
                                    item.setUsername(obj.getString("username"));
                                    item.setRating(obj.getInt("stars"));
                                    item.setAnswer(obj.getString("answer"));
                                    item.setReplyTime(obj.getString("replyTime"));
                                    item.setLogo(obj.getString("logo"));
                                    stationCommentList.add(item);

                                    sumOfPoints += obj.getInt("stars");
                                    numOfComments++;

                                    if (obj.getString("username").equals(username)) {
                                        hasAlreadyCommented = true;
                                        userCommentID = obj.getInt("id");
                                        userComment = obj.getString("comment");
                                        stars = obj.getInt("stars");
                                    }
                                }

                                stationScore = sumOfPoints / numOfComments;
                                loadComments();
                            } catch (JSONException e) {
                                mRecyclerView.setVisibility(View.GONE);
                                swipeContainer.setRefreshing(false);
                                snackbar.show();
                            }
                        } else {
                            mRecyclerView.setVisibility(View.GONE);
                            swipeContainer.setRefreshing(false);
                            snackbar.show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRecyclerView.setVisibility(View.GONE);
                        swipeContainer.setRefreshing(false);
                        snackbar.show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(istasyonID));
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
