package com.fuelspot;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.model.CommentItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.MainActivity.dimBehind;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.token;
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

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeContainer;
    private RequestQueue requestQueue;
    private PopupWindow mPopupWindow;
    private Snackbar snackbar;
    private Window window;
    private Toolbar toolbar;
    private FloatingActionMenu materialDesignFAM;
    private int istasyonID;

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

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

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

        snackbar = Snackbar.make(swipeContainer, getString(R.string.no_comment_text), Snackbar.LENGTH_LONG);

        // FABs
        materialDesignFAM = findViewById(R.id.fab_menu);

        FloatingActionButton floatingActionButton1 = findViewById(R.id.fab1);
        if (isSuperUser) {
            materialDesignFAM.setVisibility(View.GONE);
        } else {
            if (hasAlreadyCommented) {
                floatingActionButton1.setLabelText(getString(R.string.update_comment));
            } else {
                floatingActionButton1.setLabelText(getString(R.string.add_comment));
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

    private void loadComments() {
        if (stationCommentList != null && stationCommentList.size() > 0) {
            RecyclerView.Adapter mAdapter = new CommentAdapter(StationComments.this, stationCommentList, "STATION_COMMENTS");
            GridLayoutManager mLayoutManager = new GridLayoutManager(StationComments.this, 1);

            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
            swipeContainer.setRefreshing(false);
            mAdapter.notifyDataSetChanged();
        } else {
            snackbar.show();
        }
    }

    private void addUpdateCommentPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) StationComments.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_comment, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        TextView titlePopup = customView.findViewById(R.id.popup_comment_title);
        Button sendAnswer = customView.findViewById(R.id.buttonSendComment);
        if (hasAlreadyCommented) {
            titlePopup.setText(getString(R.string.update_comment));
            sendAnswer.setText(getString(R.string.update_comment));
        } else {
            titlePopup.setText(getString(R.string.add_comment));
            sendAnswer.setText(getString(R.string.add_comment));
        }
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
                    Toast.makeText(StationComments.this, getString(R.string.empty_comment), Toast.LENGTH_SHORT).show();
                }
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

        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBehind(mPopupWindow);
    }

    private void updateComment() {
        final ProgressDialog loading = ProgressDialog.show(StationComments.this, getString(R.string.comment_updating), getString(R.string.please_wait), false, false);
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("commentID", String.valueOf(userCommentID));
                params.put("comment", userComment);
                params.put("stars", String.valueOf(stars));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void addComment() {
        final ProgressDialog loading = ProgressDialog.show(StationComments.this, getString(R.string.comment_adding), getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        mPopupWindow.dismiss();

                        if (response != null && response.length() > 0) {
                            if ("Success".equals(response)) {
                                Toast.makeText(StationComments.this, getString(R.string.add_comment_success), Toast.LENGTH_SHORT).show();
                                hasAlreadyCommented = true;
                                fetchStationComments();
                            } else {
                                Toast.makeText(StationComments.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                hasAlreadyCommented = false;
                            }
                        } else {
                            Toast.makeText(StationComments.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            hasAlreadyCommented = false;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        loading.dismiss();
                        Toast.makeText(StationComments.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

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

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchStationComments() {
        final ProgressDialog loading = ProgressDialog.show(StationComments.this, getString(R.string.loading_comments), getString(R.string.please_wait), false, false);
        stationCommentList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_STATION_COMMENTS) + "?stationID=" + istasyonID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        swipeContainer.setRefreshing(false);
                        loading.dismiss();
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
                                    item.setProfile_pic(obj.getString("user_photo"));
                                    item.setUsername(obj.getString("username"));
                                    item.setRating(obj.getInt("stars"));
                                    item.setCommentPhoto(obj.getString("comment_photo"));
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
                        loading.dismiss();
                        mRecyclerView.setVisibility(View.GONE);
                        swipeContainer.setRefreshing(false);
                        snackbar.show();
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
