package org.uusoftware.fuelify;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uusoftware.fuelify.adapter.CommentAdapter;
import org.uusoftware.fuelify.model.CommentItem;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.username;

public class StationDetails extends AppCompatActivity {

    int stationID;
    String stationName, stationVicinity, stationLocation, iconURL;
    ImageView stationIcon;
    float stationDistance;
    double gasolinePrice, dieselPrice, lpgPrice, electricityPrice;
    long lastUpdated;

    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;

    StreetViewPanoramaView mStreetViewPanoramaView;
    StreetViewPanorama mPanorama;
    EditText commentHolder;
    ImageView sendComment;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<CommentItem> feedsList;
    SwipeRefreshLayout swipeContainer;
    String comment;
    Toolbar toolbar;
    Window window;
    private String FETCH_COMMENT = "http://uusoftware.org/Fuelspot/api/fetch-comments.php";
    private String SEND_COMMENT = "http://uusoftware.org/Fuelspot/api/add-comment.php";

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
                        if (streetViewPanoramaLocation != null && streetViewPanoramaLocation.links != null) {
                            Toast.makeText(StationDetails.this, "Sokak görünümü mevcut", Toast.LENGTH_LONG).show();
                        } else {
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
        Picasso.with(this).load(iconURL).error(R.drawable.unknown).placeholder(R.drawable.unknown)
                .into(stationIcon);

        //Comments
        feedsList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.commentView);

        commentHolder = findViewById(R.id.editText);
        commentHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    comment = editable.toString();
                }
            }
        });

        sendComment = findViewById(R.id.imageView4);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });

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


       /* FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + stationLocation.split(";")[0] + "," + stationLocation.split(";")[1]));
                startActivity(intent);
            }
        });*/
    }

    public void fetchComments() {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, FETCH_COMMENT,
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SEND_COMMENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
                        commentHolder.setText("");
                        fetchComments();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        loading.dismiss();
                        commentHolder.setText("");
                        Toast.makeText(StationDetails.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("comment", comment);
                params.put("station_id", String.valueOf(stationID));
                params.put("username", username);
                params.put("user_photo", photo);

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
