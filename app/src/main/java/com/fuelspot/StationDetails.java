package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.fuelspot.adapter.CampaignAdapter;
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.model.CampaignItem;
import com.fuelspot.model.CommentItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.username;

public class StationDetails extends AppCompatActivity {

    int stationDistance, choosenStationID, userCommentID;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice;
    String lastUpdated;

    String stationName, stationVicinity, stationLocation, iconURL, userComment;

    int stars = 5;
    boolean hasAlreadyCommented;

    ImageView stationIcon;
    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;

    StreetViewPanoramaView mStreetViewPanoramaView;
    AppBarLayout appBarLayout;
    StreetViewPanorama mPanorama;
    RecyclerView mRecyclerView, mRecyclerView2;
    RecyclerView.Adapter mAdapter, mAdapter2;
    List<CommentItem> feedsList = new ArrayList<>();
    List<CampaignItem> feedsList2 = new ArrayList<>();
    Toolbar toolbar;
    Window window;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2;
    PopupWindow mPopupWindow;
    RequestQueue requestQueue;
    NestedScrollView scrollView;
    ImageView errorPhoto, errorStreetView;
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        //StatusBar
        window = this.getWindow();

        //Collapsing Toolbar
        collapsingToolbarLayout = findViewById(R.id.collapsing_header);
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Dynamic bar colors
        appBarLayout = findViewById(R.id.Appbar);
        coloredBars(Color.RED, Color.TRANSPARENT);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("İstasyon detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        errorPhoto = findViewById(R.id.errorPic);
        errorStreetView = findViewById(R.id.imageViewErrStreetView);
        scrollView = findViewById(R.id.scrollView);
        requestQueue = Volley.newRequestQueue(StationDetails.this);
        mStreetViewPanoramaView = findViewById(R.id.street_view_panorama);
        mStreetViewPanoramaView.onCreate(savedInstanceState);
        textName = findViewById(R.id.station_name);
        textVicinity = findViewById(R.id.station_vicinity);
        textDistance = findViewById(R.id.distance_ofStation);
        textGasoline = findViewById(R.id.taxGasoline);
        textDiesel = findViewById(R.id.taxDiesel);
        textLPG = findViewById(R.id.TaxLPG);
        textElectricity = findViewById(R.id.TaxElectricity);
        textLastUpdated = findViewById(R.id.lastUpdated);
        stationIcon = findViewById(R.id.station_photo);

        // Nerden gelirse gelsin stationID boş olamaz.
        choosenStationID = getIntent().getIntExtra("STATION_ID", 0);
        stationName = getIntent().getStringExtra("STATION_NAME");
        if (stationName != null && stationName.length() > 0) {
            collapsingToolbarLayout.setTitle(stationName);
            //Bilgiler intent ile geçilmiş. Yakın istasyonlar sayfasından geliyor olmalı.
            stationVicinity = getIntent().getStringExtra("STATION_VICINITY");
            stationLocation = getIntent().getStringExtra("STATION_LOCATION");
            stationDistance = getIntent().getIntExtra("STATION_DISTANCE", 0);
            gasolinePrice = getIntent().getFloatExtra("STATION_GASOLINE", 0f);
            dieselPrice = getIntent().getFloatExtra("STATION_DIESEL", 0f);
            lpgPrice = getIntent().getFloatExtra("STATION_LPG", 0f);
            electricityPrice = getIntent().getFloatExtra("STATION_ELECTRIC", 0f);
            lastUpdated = getIntent().getStringExtra("STATION_LASTUPDATED");
            iconURL = getIntent().getStringExtra("STATION_ICON");
            loadStationDetails();
        } else {
            //Bilgiler intent ile pass olmamış. Profil sayfasından geliyor olmalı. İnternetten çek verileri
            fetchStationByID(choosenStationID);
        }

        //Campaigns
        mRecyclerView2 = findViewById(R.id.campaignView);

        //Comments
        mRecyclerView = findViewById(R.id.commentView);

        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item2);

        if (isSuperUser) {
            floatingActionButton1.setVisibility(View.GONE);
        } else {
            floatingActionButton1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    materialDesignFAM.close(true);
                    addUpdateCommentPopup(v);
                }
            });
        }
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDesignFAM.close(true);
                reportStation(v);
            }
        });
    }

    void fetchStationByID(final int stationID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            stationName = obj.getString("name");
                            collapsingToolbarLayout.setTitle(stationName);
                            stationVicinity = obj.getString("vicinity");
                            stationLocation = obj.getString("location");
                            //DISTANCE START
                            Location loc1 = new Location("");
                            loc1.setLatitude(Double.parseDouble(MainActivity.userlat));
                            loc1.setLongitude(Double.parseDouble(MainActivity.userlon));
                            Location loc2 = new Location("");
                            String[] stationPoint = stationLocation.split(";");
                            loc2.setLatitude(Double.parseDouble(stationPoint[0]));
                            loc2.setLongitude(Double.parseDouble(stationPoint[1]));
                            stationDistance = (int) loc1.distanceTo(loc2);
                            //DISTANCE END

                            gasolinePrice = (float) obj.getDouble("gasolinePrice");
                            dieselPrice = (float) obj.getDouble("dieselPrice");
                            lpgPrice = (float) obj.getDouble("lpgPrice");
                            electricityPrice = (float) obj.getDouble("electricityPrice");
                            lastUpdated = obj.getString("lastUpdated");
                            iconURL = obj.getString("photoURL");
                            loadStationDetails();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(stationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void loadStationDetails() {
        //Panorama
        mStreetViewPanoramaView.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
            @Override
            public void onStreetViewPanoramaReady(final StreetViewPanorama panorama) {
                panorama.setStreetNamesEnabled(true);
                panorama.setPosition(new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1])));
                panorama.setOnStreetViewPanoramaChangeListener(new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
                    @Override
                    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                        if (streetViewPanoramaLocation != null && streetViewPanoramaLocation.links != null) {
                            mPanorama = panorama;
                        } else {
                            errorStreetView.setVisibility(View.VISIBLE);
                            Snackbar.make(findViewById(android.R.id.content), "Sokak görünümü bulunamadı.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });

                //SingleStation
                textName.setText(stationName);
                textVicinity.setText(stationVicinity);
                textDistance.setText(stationDistance + " m");

                if (gasolinePrice == 0) {
                    textGasoline.setText("-");
                } else {
                    textGasoline.setText(String.valueOf(gasolinePrice));
                }

                if (dieselPrice == 0) {
                    textDiesel.setText("-");
                } else {
                    textDiesel.setText(String.valueOf(dieselPrice));
                }

                if (lpgPrice == 0) {
                    textLPG.setText("-");
                } else {
                    textLPG.setText(String.valueOf(lpgPrice));
                }

                if (electricityPrice == 0) {
                    textElectricity.setText("-");
                } else {
                    textElectricity.setText(String.valueOf(electricityPrice));
                }

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = format.parse(lastUpdated);
                    textLastUpdated.setReferenceTime(date.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Glide.with(StationDetails.this).load(Uri.parse(iconURL)).into(stationIcon);

                fetchComments();
                fetchCampaigns();
            }
        });
    }

    void fetchCampaigns() {
        feedsList2.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_CAMPAINGS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("AQQ: " + response);
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CampaignItem item = new CampaignItem();
                                    item.setCampaignName(obj.getString("campaignName"));
                                    item.setCampaignDesc(obj.getString("campaignDesc"));
                                    item.setCampaignPhoto(obj.getString("campaignPhoto"));
                                    item.setCampaignStart(obj.getString("campaignStart"));
                                    item.setCampaignEnd(obj.getString("campaignEnd"));
                                    feedsList2.add(item);
                                }

                                mAdapter2 = new CampaignAdapter(StationDetails.this, feedsList2);

                                mAdapter2.notifyDataSetChanged();
                                mRecyclerView2.setAdapter(mAdapter2);
                                mRecyclerView2.setLayoutManager(new LinearLayoutManager(StationDetails.this, LinearLayoutManager.HORIZONTAL, false));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
                params.put("id", String.valueOf(choosenStationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchComments() {
        feedsList.clear();
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
                                    item.setStationID(obj.getInt("station_id"));
                                    item.setProfile_pic(obj.getString("user_photo"));
                                    item.setUsername(obj.getString("username"));
                                    item.setRating(obj.getInt("stars"));
                                    item.setAnswer(obj.getString("answer"));
                                    item.setReplyTime(obj.getString("replyTime"));
                                    item.setLogo(obj.getString("logo"));
                                    feedsList.add(item);

                                    if (obj.getString("username").equals(username)) {
                                        hasAlreadyCommented = true;
                                        userCommentID = obj.getInt("id");
                                        userComment = obj.getString("comment");
                                        stars = obj.getInt("stars");
                                        floatingActionButton1.setImageDrawable(ContextCompat.getDrawable(StationDetails.this, R.drawable.edit));
                                        floatingActionButton1.setLabelText("Edit comment");
                                    }
                                }

                                mRecyclerView.setVisibility(View.VISIBLE);
                                errorPhoto.setVisibility(View.GONE);
                                mAdapter = new CommentAdapter(StationDetails.this, feedsList);
                                GridLayoutManager mLayoutManager = new GridLayoutManager(StationDetails.this, 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                            } catch (JSONException e) {
                                mRecyclerView.setVisibility(View.GONE);
                                errorPhoto.setVisibility(View.VISIBLE);
                                hasAlreadyCommented = false;
                                floatingActionButton1.setImageDrawable(ContextCompat.getDrawable(StationDetails.this, R.drawable.fab_comment));
                            }
                        } else {
                            mRecyclerView.setVisibility(View.GONE);
                            errorPhoto.setVisibility(View.VISIBLE);
                            hasAlreadyCommented = false;
                            floatingActionButton1.setImageDrawable(ContextCompat.getDrawable(StationDetails.this, R.drawable.fab_comment));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRecyclerView.setVisibility(View.GONE);
                        errorPhoto.setVisibility(View.VISIBLE);
                        hasAlreadyCommented = false;
                        floatingActionButton1.setImageDrawable(ContextCompat.getDrawable(StationDetails.this, R.drawable.fab_comment));
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("id", String.valueOf(choosenStationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void addUpdateCommentPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) StationDetails.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_comment, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        TextView titlePopup = customView.findViewById(R.id.title);
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
                        sendComment();
                    }
                } else {
                    Toast.makeText(StationDetails.this, "Lütfen yorum ekleyiniz", Toast.LENGTH_SHORT).show();
                }
            }

            private void sendComment() {
                final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Adding comment...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_COMMENT),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                loading.dismiss();
                                Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
                                fetchComments();
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
                        params.put("station_id", String.valueOf(choosenStationID));
                        params.put("username", username);
                        params.put("user_photo", MainActivity.photo);
                        params.put("stars", String.valueOf(stars));

                        //returning parameters
                        return params;
                    }
                };

                //Adding request to the queue
                requestQueue.add(stringRequest);
            }

            private void updateComment() {
                final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Updating comment...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_COMMENT),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                loading.dismiss();
                                Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
                                fetchComments();
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
                        params.put("station_id", String.valueOf(choosenStationID));
                        params.put("username", username);
                        params.put("user_photo", MainActivity.photo);
                        params.put("stars", String.valueOf(stars));

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

    void reportStation(View view) {
        final String[] reportReason = new String[1];
        final String[] reportDetails = new String[1];

        LayoutInflater inflater = (LayoutInflater) StationDetails.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_report, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        final Spinner spinner = customView.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reportReason[0] = position + " - " + spinner.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                reportReason[0] = 0 + " - " + spinner.getItemAtPosition(0);
            }
        });

        EditText editText = customView.findViewById(R.id.editTextReport);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    reportDetails[0] = s.toString();
                }
            }
        });

        Button sendReport = customView.findViewById(R.id.sendReport);
        sendReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReporttoServer();
            }

            private void sendReporttoServer() {
                final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Sending report...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REPORT),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                loading.dismiss();
                                Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
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
                        params.put("username", username);
                        params.put("stationID", String.valueOf(choosenStationID));
                        params.put("report", reportReason[0]);
                        params.put("details", reportDetails[0]);

                        //returning parameters
                        return params;
                    }
                };

                //Adding request to the queue
                requestQueue.add(stringRequest);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_station, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.navigation_go:
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + stationLocation.split(";")[0] + "," + stationLocation.split(";")[1]));
                startActivity(intent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStreetViewPanoramaView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStreetViewPanoramaView.onResume();
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
