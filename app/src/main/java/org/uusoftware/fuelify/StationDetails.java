package org.uusoftware.fuelify;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uusoftware.fuelify.adapter.CommentAdapter;
import org.uusoftware.fuelify.model.CommentItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.username;

public class StationDetails extends AppCompatActivity implements RatingDialogListener {

    int stationID;
    int stars;
    String stationName, stationVicinity, stationLocation, iconURL, userComment;
    ImageView stationIcon;
    float stationDistance;
    double gasolinePrice, dieselPrice, lpgPrice, electricityPrice;
    long lastUpdated;

    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;

    StreetViewPanoramaView mStreetViewPanoramaView;
    StreetViewPanorama mPanorama;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<CommentItem> feedsList;
    SwipeRefreshLayout swipeContainer;
    Toolbar toolbar;
    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.brand_logo);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("İstasyon detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        stationName = getIntent().getStringExtra("STATION_NAME");
        stationVicinity = getIntent().getStringExtra("STATION_VICINITY");
        stationLocation = getIntent().getStringExtra("STATION_LOCATION");
        stationDistance = getIntent().getFloatExtra("STATION_DISTANCE", 0.00f);
        gasolinePrice = getIntent().getDoubleExtra("STATION_GASOLINE", 0.00f);
        dieselPrice = getIntent().getDoubleExtra("STATION_DIESEL", 0.00f);
        lpgPrice = getIntent().getDoubleExtra("STATION_LPG", 0.00f);
        electricityPrice = getIntent().getDoubleExtra("STATION_ELECTRIC", 0.00f);
        lastUpdated = getIntent().getLongExtra("STATION_LASTUPDATED", 0);
        iconURL = getIntent().getStringExtra("STATION_ICON");
        stationID = getIntent().getIntExtra("STATION_ID", 0);

        //Panorama
        mStreetViewPanoramaView = findViewById(R.id.street_view_panorama);
        mStreetViewPanoramaView.onCreate(savedInstanceState);
        mStreetViewPanoramaView.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
            @Override
            public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                panorama.setPosition(new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1])));
                mPanorama = panorama;
                mPanorama.setOnStreetViewPanoramaChangeListener(new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
                    @Override
                    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                        if (streetViewPanoramaLocation == null) {
                            Toast.makeText(StationDetails.this, "Sokak görünümü yok", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        //SingleStation
        textName = findViewById(R.id.station_name);
        textName.setText(stationName);

        textVicinity = findViewById(R.id.station_vicinity);
        textVicinity.setText(stationVicinity);

        textDistance = findViewById(R.id.distance_ofStation);
        textDistance.setText((int) stationDistance + " m");

        textGasoline = findViewById(R.id.gasoline_price);
        textGasoline.setText(String.valueOf(gasolinePrice));

        textDiesel = findViewById(R.id.diesel_price);
        textDiesel.setText(String.valueOf(dieselPrice));

        textLPG = findViewById(R.id.lpg_price);
        textLPG.setText(String.valueOf(lpgPrice));

        textElectricity = findViewById(R.id.electricity_price);
        textElectricity.setText(String.valueOf(electricityPrice));

        textLastUpdated = findViewById(R.id.lastUpdated);
        textLastUpdated.setReferenceTime(lastUpdated);

        //Station Icon
        stationIcon = findViewById(R.id.station_photo);
        Glide.with(this).load(Uri.parse(iconURL)).into(stationIcon);

        //Comments
        feedsList = new ArrayList<>();
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AppRatingDialog.Builder()
                        .setPositiveButtonText("Gönder")
                        .setNegativeButtonText("İptal")
                        .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                        .setDefaultRating(5)
                        .setTitle("Bu istasyonu puanlayın")
                        .setDescription("Please select some stars and give your feedback")
                        .setDefaultComment("Bu istasyonda en uygun fiyatlar var!")
                        .setHint("Please write your comment here ...")
                        .create(StationDetails.this)
                        .show();


                /*Snackbar.make(view, "Yorum ekle?", Snackbar.LENGTH_LONG)
                        .setAction("Ekle", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        })
                        .show();*/

                /*Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + stationLocation.split(";")[0] + "," + stationLocation.split(";")[1]));
                startActivity(intent);*/
            }
        });
    }

    public void fetchComments() {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_COMMENTS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                                feedsList.add(item);

                                mAdapter = new CommentAdapter(StationDetails.this, feedsList);
                                mLayoutManager = new GridLayoutManager(StationDetails.this, 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("id", String.valueOf(stationID));

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(StationDetails.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void sendComment() {
        final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
                        fetchComments();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        loading.dismiss();
                        Toast.makeText(StationDetails.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("comment", userComment);
                params.put("station_id", String.valueOf(stationID));
                params.put("username", username);
                params.put("user_photo", photo);
                params.put("stars", String.valueOf(stars));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(StationDetails.this);

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
    public void onPositiveButtonClicked(int rate, String comment) {
        // interpret results, send it to analytics etc...
        stars = rate;
        userComment = comment;
        sendComment();
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStreetViewPanoramaView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStreetViewPanoramaView.onResume();
        fetchComments();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStreetViewPanoramaView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mStreetViewPanoramaView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mStreetViewPanoramaView.onSaveInstanceState(outState);
    }
}
