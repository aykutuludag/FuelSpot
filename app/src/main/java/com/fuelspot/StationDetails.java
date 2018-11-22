package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.widget.RelativeLayout;
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.username;

public class StationDetails extends AppCompatActivity {

    public static int stars = 5;
    public static int stationDistance, choosenStationID, userCommentID, isStationVerified;
    public static float gasolinePrice, dieselPrice, lpgPrice, electricityPrice, numOfComments, sumOfPoints, stationScore;
    public static String lastUpdated, facilitiesOfStation, stationName, stationVicinity, stationLocation, iconURL, userComment;
    public static boolean hasAlreadyCommented;

    ImageView stationIcon;
    public static List<CommentItem> stationCommentList = new ArrayList<>();
    RelativeTimeTextView textLastUpdated;

    StreetViewPanoramaView mStreetViewPanoramaView;
    StreetViewPanorama mPanorama;
    TextView noCampaignText, textStationID, textName, textVicinity, textGasoline, textDiesel, textLPG, textElectricity, literSectionTitle, textViewGasLt, textViewDieselLt, textViewLPGLt, textViewElectricityLt, textViewStationPoint;
    RecyclerView mRecyclerView, mRecyclerView2;
    RecyclerView.Adapter mAdapter, mAdapter2;
    List<CampaignItem> campaignList = new ArrayList<>();
    Toolbar toolbar;
    Window window;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2;
    PopupWindow mPopupWindow;
    RequestQueue requestQueue;
    NestedScrollView scrollView;
    ImageView errorStreetView, errorCampaign;
    CircleImageView verifiedSection;
    CircleImageView imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic, imageViewRestaurant;
    float howMuchGas, howMuchDie, howMuchLPG, howMuchEle;
    RelativeLayout commentSection;
    Button seeAllComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        //StatusBar
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Dynamic bar colors
        coloredBars(Color.RED, Color.TRANSPARENT);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("İstasyon detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        errorCampaign = findViewById(R.id.errorNoCampaign);
        noCampaignText = findViewById(R.id.noCampaignText);
        errorStreetView = findViewById(R.id.imageViewErrStreetView);
        scrollView = findViewById(R.id.scrollView);
        requestQueue = Volley.newRequestQueue(StationDetails.this);
        mStreetViewPanoramaView = findViewById(R.id.street_view_panorama);
        mStreetViewPanoramaView.onCreate(savedInstanceState);
        textName = findViewById(R.id.station_name);
        textStationID = findViewById(R.id.station_ID);
        textVicinity = findViewById(R.id.station_vicinity);
        textGasoline = findViewById(R.id.priceGasoline);
        textDiesel = findViewById(R.id.priceDiesel);
        textLPG = findViewById(R.id.priceLPG);
        textElectricity = findViewById(R.id.priceElectricity);
        textLastUpdated = findViewById(R.id.stationLastUpdate);
        stationIcon = findViewById(R.id.station_photo);
        literSectionTitle = findViewById(R.id.textViewUnitPrice);
        textViewGasLt = findViewById(R.id.howMuchGasoline);
        textViewDieselLt = findViewById(R.id.howMuchDiesel);
        textViewLPGLt = findViewById(R.id.howMuchLPG);
        textViewElectricityLt = findViewById(R.id.howMuchElectricity);
        imageViewWC = findViewById(R.id.WC);
        imageViewMarket = findViewById(R.id.Market);
        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewRestaurant = findViewById(R.id.Restaurant);
        commentSection = findViewById(R.id.section_comment);
        commentSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StationDetails.this, StationComments.class);
                startActivity(intent);
            }
        });
        textViewStationPoint = findViewById(R.id.stationPoint);
        textViewStationPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StationDetails.this, StationComments.class);
                startActivity(intent);
            }
        });
        seeAllComments = findViewById(R.id.button_seeAllComments);
        seeAllComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StationDetails.this, StationComments.class);
                startActivity(intent);
            }
        });

        // if stationVerified == 1, this section shows up!
        verifiedSection = findViewById(R.id.verifiedStation);

        // Nerden gelirse gelsin stationID boş olamaz.
        choosenStationID = getIntent().getIntExtra("STATION_ID", 0);
        stationName = getIntent().getStringExtra("STATION_NAME");
        if (stationName != null && stationName.length() > 0) {
            getSupportActionBar().setTitle(stationName);
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
            isStationVerified = getIntent().getIntExtra("IS_VERIFIED", 0);
            facilitiesOfStation = getIntent().getStringExtra("STATION_FACILITIES");
            loadStationDetails();
        } else {
            //Bilgiler intent ile pass olmamış. Profil sayfasından geliyor olmalı. İnternetten çek verileri
            fetchStation(choosenStationID);
        }

        // Campaigns
        mRecyclerView = findViewById(R.id.campaignView);

        // Comments
        mRecyclerView2 = findViewById(R.id.commentView);

        // FABs
        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);

        floatingActionButton1 = findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportPrices(v);
            }
        });

        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item3);
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDesignFAM.close(true);
                reportStation(v);
            }
        });
    }

    void fetchStation(final int stationID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            stationName = obj.getString("name");
                            getSupportActionBar().setTitle(stationName);
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

                            facilitiesOfStation = obj.getString("facilities");
                            gasolinePrice = (float) obj.getDouble("gasolinePrice");
                            dieselPrice = (float) obj.getDouble("dieselPrice");
                            lpgPrice = (float) obj.getDouble("lpgPrice");
                            electricityPrice = (float) obj.getDouble("electricityPrice");
                            lastUpdated = obj.getString("lastUpdated");
                            iconURL = obj.getString("photoURL");
                            isStationVerified = obj.getInt("isVerified");

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

            }
        });

        //SingleStation
        textName.setText(stationName);
        textVicinity.setText(stationVicinity);
        textStationID.setText("" + choosenStationID);

        if (gasolinePrice == 0) {
            textGasoline.setText("-");
        } else {
            textGasoline.setText(gasolinePrice + " " + currencySymbol);
        }

        if (dieselPrice == 0) {
            textDiesel.setText("-");
        } else {
            textDiesel.setText(dieselPrice + " " + currencySymbol);
        }

        if (lpgPrice == 0) {
            textLPG.setText("-");
        } else {
            textLPG.setText(lpgPrice + " " + currencySymbol);
        }

        if (electricityPrice == 0) {
            textElectricity.setText("-");
        } else {
            textElectricity.setText(electricityPrice + " " + currencySymbol);
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = format.parse(lastUpdated);
            textLastUpdated.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Glide.with(StationDetails.this).load(Uri.parse(iconURL)).into(stationIcon);

        if (isStationVerified == 1) {
            verifiedSection.setVisibility(View.VISIBLE);
        } else {
            verifiedSection.setVisibility(View.GONE);
        }

        literCalculator();

        String titleHolder = "100 " + currencySymbol + " ile şunları satın alabilirsiniz:";
        literSectionTitle.setText(titleHolder);

        if (howMuchGas == 0) {
            textViewGasLt.setText("-");
        } else {
            String gasolineHolder = String.format(Locale.getDefault(), "%.1f", howMuchGas) + " " + userUnit;
            textViewGasLt.setText(gasolineHolder);
        }

        if (howMuchDie == 0) {
            textViewDieselLt.setText("-");
        } else {
            String dieselHolder = String.format(Locale.getDefault(), "%.1f", howMuchDie) + " " + userUnit;
            textViewDieselLt.setText(dieselHolder);

        }

        if (howMuchLPG == 0) {
            textViewLPGLt.setText("-");
        } else {
            String lpgHolder = String.format(Locale.getDefault(), "%.1f", howMuchLPG) + " " + userUnit;
            textViewLPGLt.setText(lpgHolder);
        }


        if (howMuchEle == 0) {
            textViewElectricityLt.setText("-");
        } else {
            String electricityHolder = String.format(Locale.getDefault(), "%.1f", howMuchEle) + " " + getString(R.string.electricity_unit);
            textViewElectricityLt.setText(electricityHolder);
        }


        // Facilities
        if (facilitiesOfStation.contains("WC")) {
            imageViewWC.setVisibility(View.VISIBLE);
        } else {
            imageViewWC.setVisibility(View.GONE);
        }

        if (facilitiesOfStation.contains("Market")) {
            imageViewMarket.setVisibility(View.VISIBLE);
        } else {
            imageViewMarket.setVisibility(View.GONE);
        }

        if (facilitiesOfStation.contains("CarWash")) {
            imageViewCarWash.setVisibility(View.VISIBLE);
        } else {
            imageViewCarWash.setVisibility(View.GONE);
        }

        if (facilitiesOfStation.contains("TireRepair")) {
            imageViewTireRepair.setVisibility(View.VISIBLE);
        } else {
            imageViewTireRepair.setVisibility(View.GONE);
        }

        if (facilitiesOfStation.contains("Mechanic")) {
            imageViewMechanic.setVisibility(View.VISIBLE);
        } else {
            imageViewMechanic.setVisibility(View.GONE);
        }

        if (facilitiesOfStation.contains("Restaurant")) {
            imageViewRestaurant.setVisibility(View.VISIBLE);
        } else {
            imageViewRestaurant.setVisibility(View.GONE);
        }

        fetchCampaigns();
        fetchStationComments();
    }

    void fetchCampaigns() {
        campaignList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_CAMPAINGS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                                    campaignList.add(item);
                                }

                                mAdapter = new CampaignAdapter(StationDetails.this, campaignList);
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(StationDetails.this, LinearLayoutManager.HORIZONTAL, false));

                                mRecyclerView.setVisibility(View.VISIBLE);
                                errorCampaign.setVisibility(View.GONE);
                                noCampaignText.setVisibility(View.GONE);
                            } catch (JSONException e) {
                                mRecyclerView.setVisibility(View.GONE);
                                errorCampaign.setVisibility(View.VISIBLE);
                                noCampaignText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            mRecyclerView.setVisibility(View.GONE);
                            errorCampaign.setVisibility(View.VISIBLE);
                            noCampaignText.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRecyclerView.setVisibility(View.GONE);
                        errorCampaign.setVisibility(View.VISIBLE);
                        noCampaignText.setVisibility(View.VISIBLE);
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

    public void fetchStationComments() {
        sumOfPoints = 0;
        numOfComments = 0;
        stationCommentList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION_COMMENTS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            List<CommentItem> dummyList = new ArrayList<>();
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
                                    stationCommentList.add(item);

                                    if (i < 3) {
                                        dummyList.add(item);
                                    }

                                    if (obj.getString("username").equals(username)) {
                                        hasAlreadyCommented = true;
                                        userCommentID = obj.getInt("id");
                                        userComment = obj.getString("comment");
                                        stars = obj.getInt("stars");
                                    }

                                    sumOfPoints += obj.getInt("stars");
                                    numOfComments++;

                                }

                                // Calculate station score
                                DecimalFormat df = new DecimalFormat("#.##");
                                stationScore = sumOfPoints / numOfComments;
                                textViewStationPoint.setText((int) numOfComments + " yorum" + " - " + df.format(stationScore));

                                // Display first three comments
                                mAdapter2 = new CommentAdapter(StationDetails.this, dummyList, "STATION_COMMENTS");
                                GridLayoutManager mLayoutManager = new GridLayoutManager(StationDetails.this, 1);

                                mAdapter2.notifyDataSetChanged();
                                mRecyclerView2.setAdapter(mAdapter2);
                                mRecyclerView2.setLayoutManager(mLayoutManager);
                            } catch (JSONException e) {
                                hasAlreadyCommented = false;
                                textViewStationPoint.setText(0 + " yorum" + " - " + 0.0);
                            }
                        } else {
                            hasAlreadyCommented = false;
                            textViewStationPoint.setText(0 + " yorum" + " - " + 0.0);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hasAlreadyCommented = false;
                        textViewStationPoint.setText(0 + " yorum" + " - " + 0.0);
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

    void reportStation(final View view) {
        final String[] reportReason = new String[1];
        final String[] reportDetails = new String[1];

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_report, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        final Spinner spinner = customView.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view2, int position, long id) {
                if (position == 5) {
                    mPopupWindow.dismiss();
                    reportPrices(view);
                } else {
                    reportReason[0] = position + " - " + spinner.getItemAtPosition(position);
                }
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
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REPORT_ADD),
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

    public void reportPrices(View view) {
        final float[] benzinFiyat = new float[1];
        final float[] dizelFiyat = new float[1];
        final float[] LPGFiyat = new float[1];
        final float[] ElektrikFiyat = new float[1];
        final String[] pricesArray = new String[1];

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_report_prices, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        EditText editText = customView.findViewById(R.id.editTextGasoline);
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
                    benzinFiyat[0] = Float.parseFloat(s.toString());
                }
            }
        });

        EditText editText2 = customView.findViewById(R.id.editTextDiesel);
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    dizelFiyat[0] = Float.parseFloat(s.toString());
                }
            }
        });

        EditText editText3 = customView.findViewById(R.id.editTextLPG);
        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    LPGFiyat[0] = Float.parseFloat(s.toString());
                }
            }
        });

        EditText editText4 = customView.findViewById(R.id.editTextElectricity);
        editText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    ElektrikFiyat[0] = Float.parseFloat(s.toString());
                }
            }
        });

        Button sendReport = customView.findViewById(R.id.sendFiyat);
        sendReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pricesArray[0] = "{ gasoline = " + benzinFiyat[0] + " diesel = " + dizelFiyat[0] + " lpg = " + LPGFiyat[0] + " electricity = " + ElektrikFiyat[0] + " }";
                sendReporttoServer();
            }

            private void sendReporttoServer() {
                final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Sending report...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REPORT_ADD),
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
                        params.put("prices", pricesArray[0]);

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

    void literCalculator() {
        if (gasolinePrice > 0) {
            howMuchGas = 100 / gasolinePrice;
        }

        if (dieselPrice > 0) {
            howMuchDie = 100 / dieselPrice;
        }

        if (lpgPrice > 0) {
            howMuchLPG = 100 / lpgPrice;
        }

        if (electricityPrice > 0) {
            howMuchEle = 100 / electricityPrice;
        }
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
            case R.id.menu_fav:
                Toast.makeText(StationDetails.this, "Coming soon:", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, stationName + " on FuelSpot: " + "http://fuel-spot.com/stations?id=" + choosenStationID);
                startActivity(Intent.createChooser(intent, getString(R.string.menu_share)));
                return true;
            case R.id.menu_go:
                Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + stationLocation.split(";")[0] + "," + stationLocation.split(";")[1]));
                startActivity(intent2);
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