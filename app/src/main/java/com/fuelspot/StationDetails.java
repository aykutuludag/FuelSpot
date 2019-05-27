package com.fuelspot;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fuelspot.adapter.CampaignAdapter;
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.adapter.GraphMarkerAdapter;
import com.fuelspot.model.CampaignItem;
import com.fuelspot.model.CommentItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.userFavorites;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;

public class StationDetails extends AppCompatActivity {

    public static List<CommentItem> stationCommentList = new ArrayList<>();
    private static List<CampaignItem> campaignList = new ArrayList<>();
    private List<Entry> gasolinePriceHistory = new ArrayList<>();
    private List<Entry> dieselPriceHistory = new ArrayList<>();
    private List<Entry> lpgPriceHistory = new ArrayList<>();
    private List<Entry> elecPriceHistory = new ArrayList<>();

    public static int stars = 5;
    public static int choosenStationID;
    public static int userCommentID;
    public static float numOfComments;
    public static float sumOfPoints;
    public static float stationScore;
    public static String userComment;
    public static boolean hasAlreadyCommented;
    private static int isStationVerified;
    private static float gasolinePrice;
    private static float dieselPrice;
    private static float lpgPrice;
    private static float electricityPrice;
    private static String lastUpdated;
    private static String stationName;
    private static String stationVicinity;
    private static String stationLocation;
    private static String iconURL;

    private String facilitiesOfStation;
    private CircleImageView stationIcon;

    private RelativeTimeTextView textLastUpdated;
    MenuItem settingsItem;
    private TextView noCampaignText;
    private TextView noCommentText;
    private TextView textStationID;
    private TextView textName;
    private TextView textVicinity;
    private TextView textGasoline;
    private TextView textDiesel;
    private TextView textLPG;
    private TextView textElectricity;
    private TextView literSectionTitle;
    private TextView textViewGasLt;
    private TextView textViewDieselLt;
    private TextView textViewLPGLt;
    private TextView textViewElectricityLt;
    private TextView textViewStationPoint;
    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView2;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mAdapter2;
    private Toolbar toolbar;
    private Window window;
    private FloatingActionMenu materialDesignFAM;
    private FloatingActionButton floatingActionButton1;
    private PopupWindow mPopupWindow;
    private RequestQueue requestQueue;
    private NestedScrollView scrollView;
    private CircleImageView verifiedSection;
    private float howMuchGas;
    private float howMuchDie;
    private float howMuchLPG;
    private float howMuchEle;
    private Button seeAllComments;
    private LineChart chart;
    private Bitmap bitmap;
    private ImageView reportStationPhoto;
    private ImageView reportPricePhoto;
    private RequestOptions options;
    private SharedPreferences prefs;
    private int reportRequest;
    private SimpleDateFormat sdf;
    private CircleImageView imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic, imageViewRestaurant, imageViewParkSpot, imageViewATM, imageViewMotel;

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
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
        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        // Analytics
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName("İstasyon detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        sdf = new SimpleDateFormat(USTimeFormat, Locale.getDefault());

        AdView mAdView = findViewById(R.id.adView);
        if (!premium) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("EEB32226D1D806C1259761D5FF4A8C41").build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        noCampaignText = findViewById(R.id.noCampaignText);
        scrollView = findViewById(R.id.scrollView);

        requestQueue = Volley.newRequestQueue(StationDetails.this);
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
        chart = findViewById(R.id.chart);
        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        chart.getXAxis().setAvoidFirstLastClipping(true);
        chart.getXAxis().setLabelCount(3, true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                Date date = new Date();
                date.setTime((long) value);
                return formatter.format(date);
            }
        });
        chart.getDescription().setText(currencySymbol + " / " + userUnit);
        chart.getDescription().setTextSize(13f);
        chart.getDescription().setTextColor(Color.BLUE);
        chart.setExtraRightOffset(10f);
        imageViewWC = findViewById(R.id.WC);
        imageViewWC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.wc), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewMarket = findViewById(R.id.Market);
        imageViewMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.market), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewCarWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.carwash), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewTireRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.tire_store), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.mechanic), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewRestaurant = findViewById(R.id.Restaurant);
        imageViewRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.restaurant), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewParkSpot = findViewById(R.id.ParkSpot);
        imageViewParkSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.parkspot), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewATM = findViewById(R.id.ATM);
        imageViewATM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.atm), Toast.LENGTH_SHORT).show();
            }
        });
        imageViewMotel = findViewById(R.id.Motel);
        imageViewMotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StationDetails.this, getString(R.string.motel), Toast.LENGTH_SHORT).show();
            }
        });
        RelativeLayout commentSection = findViewById(R.id.section_comment);
        noCommentText = findViewById(R.id.noCommentText);
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
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_station).error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        // Nerden gelirse gelsin stationID boş olamaz.
        choosenStationID = getIntent().getIntExtra("STATION_ID", 0);
        stationName = getIntent().getStringExtra("STATION_NAME");
        if (stationName != null && stationName.length() > 0) {
            getSupportActionBar().setTitle(stationName);
            //Bilgiler intent ile geçilmiş. Yakın istasyonlar sayfasından geliyor olmalı.
            stationVicinity = getIntent().getStringExtra("STATION_VICINITY");
            stationLocation = getIntent().getStringExtra("STATION_LOCATION");
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
        mRecyclerView.setNestedScrollingEnabled(false);

        // Comments
        mRecyclerView2 = findViewById(R.id.commentView);
        mRecyclerView2.setNestedScrollingEnabled(false);

        // FABs
        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);

        floatingActionButton1 = findViewById(R.id.fab1);
        if (isSuperUser) {
            floatingActionButton1.setVisibility(View.GONE);
        } else {
            if (hasAlreadyCommented) {
                floatingActionButton1.setLabelText("Yorumu güncelle");
            } else {
                floatingActionButton1.setLabelText("Yorum yaz");
            }

            floatingActionButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    materialDesignFAM.close(true);
                    addUpdateCommentPopup(v);
                }
            });
        }

        FloatingActionButton floatingActionButton2 = findViewById(R.id.fab2);
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDesignFAM.close(true);
                reportPrices(v);
            }
        });

        FloatingActionButton floatingActionButton3 = findViewById(R.id.fab3);
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDesignFAM.close(true);
                reportStation(v);
            }
        });
    }

    private void fetchStation(final int stationID) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_STATION) + "?stationID=" + choosenStationID + "&AUTH_KEY=" + getString(R.string.fuelspot_api_key),
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

                            facilitiesOfStation = obj.getString("facilities");
                            gasolinePrice = (float) obj.getDouble("gasolinePrice");
                            dieselPrice = (float) obj.getDouble("dieselPrice");
                            lpgPrice = (float) obj.getDouble("lpgPrice");
                            electricityPrice = (float) obj.getDouble("electricityPrice");
                            lastUpdated = obj.getString("lastUpdated");
                            iconURL = obj.getString("logoURL");
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
                        Toast.makeText(StationDetails.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchStationFinance() {
        gasolinePriceHistory.clear();
        dieselPriceHistory.clear();
        lpgPriceHistory.clear();
        elecPriceHistory.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_STATION_PRICES) + "?stationID=" + choosenStationID + "&AUTH_KEY=" + getString(R.string.fuelspot_api_key),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    if (obj.getDouble("gasolinePrice") != 0) {
                                        gasolinePriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("gasolinePrice")));
                                    }

                                    if (obj.getDouble("dieselPrice") != 0) {
                                        dieselPriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("dieselPrice")));
                                    }

                                    if (obj.getDouble("lpgPrice") != 0) {
                                        lpgPriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("lpgPrice")));
                                    }

                                    if (obj.getDouble("electricityPrice") != 0) {
                                        elecPriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("electricityPrice")));
                                    }
                                }

                                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                if (gasolinePriceHistory.size() > 0) {
                                    Collections.reverse(gasolinePriceHistory);
                                    LineDataSet dataSet = new LineDataSet(gasolinePriceHistory, getString(R.string.gasoline)); // add entries to dataset
                                    dataSet.setDrawValues(false);
                                    dataSet.setColor(Color.BLACK);
                                    dataSet.setDrawCircles(false);
                                    dataSets.add(dataSet);
                                }

                                if (dieselPriceHistory.size() > 0) {
                                    Collections.reverse(dieselPriceHistory);
                                    LineDataSet dataSet2 = new LineDataSet(dieselPriceHistory, getString(R.string.diesel)); // add entries to dataset
                                    dataSet2.setDrawValues(false);
                                    dataSet2.setColor(Color.RED);
                                    dataSet2.setDrawCircles(false);
                                    dataSets.add(dataSet2);
                                }

                                if (lpgPriceHistory.size() > 0) {
                                    Collections.reverse(lpgPriceHistory);
                                    LineDataSet dataSet3 = new LineDataSet(lpgPriceHistory, getString(R.string.lpg)); // add entries to dataset
                                    dataSet3.setDrawValues(false);
                                    dataSet3.setColor(Color.BLUE);
                                    dataSet3.setDrawCircles(false);
                                    dataSets.add(dataSet3);
                                }

                                if (elecPriceHistory.size() > 0) {
                                    Collections.reverse(elecPriceHistory);
                                    LineDataSet dataSet4 = new LineDataSet(elecPriceHistory, getString(R.string.electricity)); // add entries to dataset
                                    dataSet4.setDrawValues(false);
                                    dataSet4.setColor(Color.GREEN);
                                    dataSet4.setDrawCircles(false);
                                    dataSets.add(dataSet4);
                                }

                                if (dataSets.size() > 0) {
                                    LineData lineData = new LineData(dataSets);
                                    chart.setData(lineData);
                                    chart.invalidate(); // refresh
                                    GraphMarkerAdapter mv = new GraphMarkerAdapter(StationDetails.this, R.layout.popup_graph_marker, dataSets);
                                    chart.setMarker(mv);
                                }
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(StationDetails.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void loadStationDetails() {
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
            SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
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
        try {
            JSONArray facilitiesRes = new JSONArray(facilitiesOfStation);
            JSONObject facilitiesObj = facilitiesRes.getJSONObject(0);

            if (facilitiesObj.getInt("WC") == 1) {
                imageViewWC.setAlpha(1.0f);
            } else {
                imageViewWC.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Market") == 1) {
                imageViewMarket.setAlpha(1.0f);
            } else {
                imageViewMarket.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("CarWash") == 1) {
                imageViewCarWash.setAlpha(1.0f);
            } else {
                imageViewCarWash.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("TireRepair") == 1) {
                imageViewTireRepair.setAlpha(1.0f);
            } else {
                imageViewTireRepair.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Mechanic") == 1) {
                imageViewMechanic.setAlpha(1.0f);
            } else {
                imageViewMechanic.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Restaurant") == 1) {
                imageViewRestaurant.setAlpha(1.0f);
            } else {
                imageViewRestaurant.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("ParkSpot") == 1) {
                imageViewParkSpot.setAlpha(1.0f);
            } else {
                imageViewParkSpot.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("ATM") == 1) {
                imageViewATM.setAlpha(1.0f);
            } else {
                imageViewATM.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Motel") == 1) {
                imageViewMotel.setAlpha(1.0f);
            } else {
                imageViewMotel.setAlpha(0.25f);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        fetchStationFinance();
        fetchCampaigns();
        fetchStationComments();
    }

    private void fetchCampaigns() {
        campaignList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_CAMPAINGS) + "?stationID=" + choosenStationID + "&AUTH_KEY=" + getString(R.string.fuelspot_api_key),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CampaignItem item = new CampaignItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationID(obj.getInt("stationID"));
                                    item.setCampaignName(obj.getString("campaignName"));
                                    item.setCampaignDesc(obj.getString("campaignDesc"));
                                    item.setCampaignPhoto(obj.getString("campaignPhoto"));
                                    item.setCampaignStart(obj.getString("campaignStart"));
                                    item.setCampaignEnd(obj.getString("campaignEnd"));
                                    item.setIsGlobal(obj.getInt("isGlobal"));
                                    campaignList.add(item);
                                }

                                mAdapter = new CampaignAdapter(StationDetails.this, campaignList, "USER");
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(StationDetails.this, LinearLayoutManager.HORIZONTAL, false));

                                mRecyclerView.setVisibility(View.VISIBLE);
                                noCampaignText.setVisibility(View.GONE);
                            } catch (JSONException e) {
                                mRecyclerView.setVisibility(View.GONE);
                                noCampaignText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            mRecyclerView.setVisibility(View.GONE);
                            noCampaignText.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRecyclerView.setVisibility(View.GONE);
                        noCampaignText.setVisibility(View.VISIBLE);
                    }
                }) {
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchStationComments() {
        sumOfPoints = 0;
        numOfComments = 0;
        stationCommentList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_STATION_COMMENTS) + "?stationID=" + choosenStationID + "&AUTH_KEY=" + getString(R.string.fuelspot_api_key),
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
                                    } else {
                                        seeAllComments.setVisibility(View.VISIBLE);
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

                                if (hasAlreadyCommented) {
                                    floatingActionButton1.setLabelText(getString(R.string.update_comment));
                                } else {
                                    floatingActionButton1.setLabelText(getString(R.string.add_comment));
                                }

                                noCommentText.setVisibility(View.GONE);

                                // Calculate station score
                                DecimalFormat df = new DecimalFormat("#.##");
                                stationScore = sumOfPoints / numOfComments;
                                textViewStationPoint.setText((int) numOfComments + " " + getString(R.string.comments) + " - " + df.format(stationScore));

                                // Display first three comments
                                mAdapter2 = new CommentAdapter(StationDetails.this, dummyList, "STATION_COMMENTS");
                                GridLayoutManager mLayoutManager = new GridLayoutManager(StationDetails.this, 1);

                                mAdapter2.notifyDataSetChanged();
                                mRecyclerView2.setAdapter(mAdapter2);
                                mRecyclerView2.setLayoutManager(mLayoutManager);
                            } catch (JSONException e) {
                                hasAlreadyCommented = false;
                                textViewStationPoint.setText(0 + " " + getString(R.string.comments) + " - " + 0.0);
                                noCommentText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            hasAlreadyCommented = false;
                            textViewStationPoint.setText(0 + " " + getString(R.string.comments) + " - " + 0.0);
                            noCommentText.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hasAlreadyCommented = false;
                        textViewStationPoint.setText(0 + " " + getString(R.string.comments) + " - " + 0.0);
                        noCommentText.setVisibility(View.VISIBLE);
                    }
                }) {
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void addUpdateCommentPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) StationDetails.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_comment, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        TextView titlePopup = customView.findViewById(R.id.popup_comment_title);
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
                    Toast.makeText(StationDetails.this, getString(R.string.empty_comment), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (hasAlreadyCommented) {
            titlePopup.setText(getString(R.string.update_comment));
            sendAnswer.setText(getString(R.string.update_comment));
        } else {
            titlePopup.setText(getString(R.string.add_comment));
            sendAnswer.setText(getString(R.string.add_comment));
        }

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

    private void addComment() {
        final ProgressDialog loading = ProgressDialog.show(StationDetails.this, getString(R.string.comment_adding), getString(R.string.please_wait), true, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
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

    private void updateComment() {
        final ProgressDialog loading = ProgressDialog.show(StationDetails.this, getString(R.string.comment_updating), getString(R.string.please_wait), true, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();

                        if (response != null && response.length() > 0) {
                            if ("Success".equals(response)) {
                                Toast.makeText(StationDetails.this, getString(R.string.comment_update_success), Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
                                fetchStationComments();
                            } else {
                                Toast.makeText(StationDetails.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(StationDetails.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(StationDetails.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
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

    private void reportStation(final View view) {
        // Clear image
        bitmap = null;

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
                    reportReason[0] = (String) spinner.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                reportReason[0] = (String) spinner.getItemAtPosition(0);
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

        reportStationPhoto = customView.findViewById(R.id.imageViewReport);
        reportStationPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRequest = 0;
                if (MainActivity.verifyFilePickerPermission(StationDetails.this)) {
                    ImagePicker.create(StationDetails.this).single().start();
                } else {
                    ActivityCompat.requestPermissions(StationDetails.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        Button sendReport = customView.findViewById(R.id.sendReport);
        sendReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reportDetails[0] != null && reportDetails[0].length() > 0) {
                    sendReporttoServer(username, choosenStationID, reportReason[0], reportDetails[0], "", bitmap);
                } else {
                    Toast.makeText(StationDetails.this, "Lütfen mesajınızı giriniz", Toast.LENGTH_LONG).show();
                }
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

    private void reportPrices(View view) {
        // Clear image
        bitmap = null;

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

        reportPricePhoto = customView.findViewById(R.id.imageViewPricesPhoto);
        reportPricePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRequest = 1;
                if (MainActivity.verifyFilePickerPermission(StationDetails.this)) {
                    ImagePicker.create(StationDetails.this).single().start();
                } else {
                    ActivityCompat.requestPermissions(StationDetails.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        Button sendReport = customView.findViewById(R.id.sendFiyat);
        sendReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (benzinFiyat[0] != 0 || dizelFiyat[0] != 0 || LPGFiyat[0] != 0 || ElektrikFiyat[0] != 0) {
                    pricesArray[0] = "PRICES: { fuel_gasoline= " + benzinFiyat[0] + ", fuel_diesel= " + dizelFiyat[0] + ", fuel_lpg= " + LPGFiyat[0] + ", fuel_electricity= " + ElektrikFiyat[0] + " }";
                    if (bitmap != null) {
                        sendReporttoServer(username, choosenStationID, getApplicationContext().getResources().getStringArray(R.array.report_reasons)[5], "", pricesArray[0], bitmap);
                    } else {
                        Toast.makeText(StationDetails.this, "Raporunuzun değerlendirmeye alınabilmesi için lütfen fiyat tabelası/fiş-fatura vs gibi kanıtlayıcı bir görsel ekleyiniz.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(StationDetails.this, getString(R.string.enter_prices), Toast.LENGTH_LONG).show();
                }
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

    private void sendReporttoServer(final String kullaniciAdi, final int istasyonID, final String raporSebebi, final String raporDetayi, final String fiyatlar, final Bitmap bitmap) {
        final ProgressDialog loading = ProgressDialog.show(StationDetails.this, getString(R.string.sending_report), getString(R.string.sending_report), false, true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REPORT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            if ("Success".equals(response)) {
                                Toast.makeText(StationDetails.this, getString(R.string.report_send_success), Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
                            } else {
                                Toast.makeText(StationDetails.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(StationDetails.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(StationDetails.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", kullaniciAdi);
                params.put("stationID", String.valueOf(istasyonID));
                params.put("report", raporSebebi);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));
                if (raporDetayi != null && raporDetayi.length() > 0) {
                    params.put("details", raporDetayi);
                } else {
                    params.put("details", "");
                }
                if (fiyatlar != null && fiyatlar.length() > 0) {
                    params.put("prices", fiyatlar);
                } else {
                    params.put("prices", "");
                }
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                } else {
                    params.put("photo", "");
                }

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);

        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public Bitmap resizeAndRotate(Bitmap bmp, float degrees) {
        if (bmp.getWidth() > 1080 || bmp.getHeight() > 1920) {
            float aspectRatio = (float) bmp.getWidth() / bmp.getHeight();
            int width, height;

            if (aspectRatio < 1) {
                // Portrait
                width = (int) (aspectRatio * 1920);
                height = (int) (width * (1f / aspectRatio));
            } else {
                // Landscape
                width = (int) (aspectRatio * 1080);
                height = (int) (width * (1f / aspectRatio));
            }

            bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        if (degrees != 0) {
            return rotate(bmp, degrees);
        } else {
            return bmp;
        }
    }

    private void literCalculator() {
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

    private void clearVariables() {
        choosenStationID = 0;
        userCommentID = 0;
        isStationVerified = 0;
        gasolinePrice = 0;
        dieselPrice = 0;
        lpgPrice = 0;
        electricityPrice = 0;
        numOfComments = 0;
        sumOfPoints = 0;
        stationScore = 0;
        lastUpdated = "";
        facilitiesOfStation = "";
        stationName = "";
        stationVicinity = "";
        stationLocation = "";
        iconURL = "";
        userComment = "";
        hasAlreadyCommented = false;
        stationCommentList.clear();
        campaignList.clear();
        gasolinePriceHistory.clear();
        dieselPriceHistory.clear();
        lpgPriceHistory.clear();
        elecPriceHistory.clear();
        settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_fav));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        settingsItem = menu.findItem(R.id.menu_fav);
        if (userFavorites.contains(choosenStationID + ";")) {
            settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_fav_orange));
        } else {
            settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_fav));
        }
        return super.onPrepareOptionsMenu(menu);
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
                clearVariables();
                finish();
                return true;
            case R.id.menu_fav:
                if (userFavorites.contains(choosenStationID + ";")) {
                    userFavorites = userFavorites.replace(choosenStationID + ";", "");
                    settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_fav_orange));
                    Toast.makeText(StationDetails.this, getString(R.string.removed_from_favs), Toast.LENGTH_SHORT).show();
                } else {
                    userFavorites += choosenStationID + ";";
                    settingsItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_fav));
                    Toast.makeText(StationDetails.this, getString(R.string.added_to_favs), Toast.LENGTH_SHORT).show();
                }
                invalidateOptionsMenu();
                prefs.edit().putString("userFavorites", userFavorites).apply();
                return true;
            case R.id.menu_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, stationName + " on FuelSpot: " + "https://fuelspot.com.tr/stations/" + choosenStationID);
                startActivity(Intent.createChooser(intent, getString(R.string.menu_share)));
                return true;
            case R.id.menu_go:
                String uri = "https://www.google.com/maps/dir/?api=1&origin=" + userlat + "," + userlon + "&destination=" +
                        stationLocation.split(";")[0] + "," + stationLocation.split(";")[1] + "&travelmode=driving";
                Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_STORAGE) {
            if (ActivityCompat.checkSelfPermission(StationDetails.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.create(StationDetails.this).single().start(reportRequest);
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Imagepicker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                try {
                    bitmap = BitmapFactory.decodeFile(image.getPath());
                    ExifInterface ei = new ExifInterface(image.getPath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_NORMAL:
                            bitmap = resizeAndRotate(bitmap, 0);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap = resizeAndRotate(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = resizeAndRotate(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = resizeAndRotate(bitmap, 270);
                            break;
                    }

                    switch (reportRequest) {
                        case 0:
                            Glide.with(StationDetails.this).load(bitmap).apply(options).into(reportStationPhoto);
                            break;
                        case 1:
                            Glide.with(StationDetails.this).load(bitmap).apply(options).into(reportPricePhoto);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        requestQueue.cancelAll(this);
        clearVariables();
        finish();
    }
}