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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.fuelspot.adapter.CommentAdapter;
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

public class StationDetails extends AppCompatActivity {

    int stationDistance;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice;
    String lastUpdated;

    int stationID, userCommentID;
    String stationName, stationVicinity, stationLocation, iconURL, userComment;

    int stars = 5;
    boolean hasAlreadyCommented;

    ImageView stationIcon;
    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;

    StreetViewPanoramaView mStreetViewPanoramaView;
    AppBarLayout appBarLayout;
    StreetViewPanorama mPanorama;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<CommentItem> feedsList;
    Toolbar toolbar;
    Window window;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2;
    PopupWindow mPopupWindow;
    RequestQueue requestQueue;

    ImageView errorPhoto, errorStreetView;
    CollapsingToolbarLayout collapsingToolbarLayout;

    ImageView campaign1, campaign2, campaign3;
    RelativeLayout campaignSection;

    ArrayList<String> campaignName = new ArrayList<>();
    ArrayList<String> campaignDesc = new ArrayList<>();
    ArrayList<String> campaignPhoto = new ArrayList<>();
    ArrayList<String> campaignStart = new ArrayList<>();
    ArrayList<String> campaignEnd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        //StatusBar
        window = this.getWindow();

        //Collapsing Toolbar
        collapsingToolbarLayout = findViewById(R.id.collapsing_header);
        collapsingToolbarLayout.setTitle("İstasyon detayı");
        collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Dynamic bar colors
        appBarLayout = findViewById(R.id.Appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    coloredBars(ContextCompat.getColor(StationDetails.this, R.color.colorPrimaryDark), ContextCompat.getColor(StationDetails.this, R.color.colorPrimary));
                } else if (verticalOffset == 0) {
                    coloredBars(Color.TRANSPARENT, Color.TRANSPARENT);
                } else {
                    coloredBars(Color.argb(255 - verticalOffset / 2, 230, 74, 25), Color.argb(255 - verticalOffset / 2, 255, 87, 34));
                }
            }
        });

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("İstasyon detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        errorPhoto = findViewById(R.id.errorPic);
        errorStreetView = findViewById(R.id.imageViewErrStreetView);

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
        stationID = getIntent().getIntExtra("STATION_ID", 0);
        stationName = getIntent().getStringExtra("STATION_NAME");
        if (stationName != null && stationName.length() > 0) {
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
            fetchStationByID(stationID);
        }

        //Campaigns
        campaignSection = findViewById(R.id.campaignSection);
        campaign1 = findViewById(R.id.campaign1);
        campaign1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campaignName != null && campaignName.size() > 0) {
                    try {
                        campaignPopup(0, v);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        campaign2 = findViewById(R.id.campaign2);
        campaign2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campaignName != null && campaignName.size() > 1) {
                    try {
                        campaignPopup(1, v);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        campaign3 = findViewById(R.id.campaign3);
        campaign3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campaignName != null && campaignName.size() > 2) {
                    try {
                        campaignPopup(2, v);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //Comments
        feedsList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.commentView);

        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item2);

        if (MainActivity.isSuperUser) {
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
            public void onClick(View v) {
                materialDesignFAM.close(true);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + stationLocation.split(";")[0] + "," + stationLocation.split(";")[1]));
                startActivity(intent);
            }
        });
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
            }
        });
    }

    void fetchStationByID(final int stationID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION_BY_ID),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            stationName = obj.getString("name");
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

    void fetchCampaigns() {
        campaignName.clear();
        campaignDesc.clear();
        campaignPhoto.clear();
        campaignStart.clear();
        campaignEnd.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_CAMPAINGS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("AMK:" + response);
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    campaignName.add(i, obj.getString("campaignName"));
                                    campaignDesc.add(i, obj.getString("campaignDesc"));
                                    campaignPhoto.add(i, obj.getString("campaignPhoto"));
                                    campaignStart.add(i, obj.getString("campaignStart"));
                                    campaignEnd.add(i, obj.getString("campaignEnd"));

                                    System.out.println("AMK:" + campaignName.get(i));

                                    if (i == 0) {
                                        Glide.with(StationDetails.this).load(Uri.parse(campaignPhoto.get(0))).into(campaign1);
                                    } else if (i == 1) {
                                        Glide.with(StationDetails.this).load(Uri.parse(campaignPhoto.get(1))).into(campaign2);
                                    } else {
                                        Glide.with(StationDetails.this).load(Uri.parse(campaignPhoto.get(2))).into(campaign3);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        campaignSection.setVisibility(View.GONE);
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

    void campaignPopup(int campaignID, View view) throws ParseException {
        LayoutInflater inflater = (LayoutInflater) StationDetails.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_campaign, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

        ImageView imgPopup = customView.findViewById(R.id.campaignPhoto);
        Glide.with(StationDetails.this).load(Uri.parse(campaignPhoto.get(campaignID))).into(imgPopup);

        TextView titlePopup = customView.findViewById(R.id.campaignTitle);
        titlePopup.setText(campaignName.get(campaignID));

        TextView descPopup = customView.findViewById(R.id.campaignDesc);
        descPopup.setText(campaignDesc.get(campaignID));

        RelativeTimeTextView startTime = customView.findViewById(R.id.startTime);
        Date date = format.parse(campaignStart.get(campaignID));
        startTime.setReferenceTime(date.getTime());

        RelativeTimeTextView endTime = customView.findViewById(R.id.endTime);
        Date date2 = format.parse(campaignEnd.get(campaignID));
        endTime.setReferenceTime(date2.getTime());

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

                                    if (obj.getString("username").equals(MainActivity.username)) {
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
                                mLayoutManager = new GridLayoutManager(StationDetails.this, 1);

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
                params.put("id", String.valueOf(stationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
                params.put("station_id", String.valueOf(stationID));
                params.put("username", MainActivity.username);
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
                params.put("station_id", String.valueOf(stationID));
                params.put("username", MainActivity.username);
                params.put("user_photo", MainActivity.photo);
                params.put("stars", String.valueOf(stars));

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
        fetchCampaigns();
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
