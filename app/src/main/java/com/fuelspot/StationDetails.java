package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fuelspot.adapter.CampaignAdapter;
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.model.CampaignItem;
import com.fuelspot.model.CommentItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
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

import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.universalTimeFormat;
import static com.fuelspot.MainActivity.userFavorites;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.username;

public class StationDetails extends AppCompatActivity {

    public static int stars = 5;
    public static int stationDistance, choosenStationID, userCommentID, isStationVerified;
    public static float gasolinePrice, dieselPrice, lpgPrice, electricityPrice, numOfComments, sumOfPoints, stationScore;
    public static String lastUpdated, stationName, stationVicinity, stationLocation, iconURL, userComment;
    String facilitiesOfStation;
    public static boolean hasAlreadyCommented;
    public static List<CommentItem> stationCommentList = new ArrayList<>();
    public static List<CampaignItem> campaignList = new ArrayList<>();

    CircleImageView stationIcon;
    List<Entry> gasolinePriceHistory = new ArrayList<>();
    List<Entry> dieselPriceHistory = new ArrayList<>();
    List<Entry> lpgPriceHistory = new ArrayList<>();
    List<Entry> elecPriceHistory = new ArrayList<>();
    RelativeTimeTextView textLastUpdated;

    TextView noCampaignText, noCommentText, textStationID, textName, textVicinity, textGasoline, textDiesel, textLPG, textElectricity, literSectionTitle, textViewGasLt, textViewDieselLt, textViewLPGLt, textViewElectricityLt, textViewStationPoint;
    RecyclerView mRecyclerView, mRecyclerView2;
    RecyclerView.Adapter mAdapter, mAdapter2;
    Toolbar toolbar;
    Window window;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;
    PopupWindow mPopupWindow;
    RequestQueue requestQueue;
    NestedScrollView scrollView;
    ImageView errorStreetView, errorCampaign, errorComment;
    CircleImageView verifiedSection;
    CircleImageView imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic, imageViewRestaurant, imageViewParkSpot;
    float howMuchGas, howMuchDie, howMuchLPG, howMuchEle;
    RelativeLayout commentSection;
    Button seeAllComments;
    LineChart chart;
    Bitmap bitmap;
    ImageView reportStationPhoto;
    ImageView reportPricePhoto;
    RequestOptions options;
    WebView webview;
    SharedPreferences prefs;
    Drawable favorite;
    int reportRequest;
    String reportReason, reportDetails;
    SimpleDateFormat sdf;

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
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName("İstasyon detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        sdf = new SimpleDateFormat(universalTimeFormat, Locale.getDefault());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            favorite = ContextCompat.getDrawable(this, R.drawable.ic_fav);
        } else {
            favorite = getResources().getDrawable(R.drawable.ic_fav);
        }

        webview = findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);

        errorCampaign = findViewById(R.id.errorNoCampaign);
        noCampaignText = findViewById(R.id.noCampaignText);
        errorStreetView = findViewById(R.id.imageViewErrStreetView);
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
        imageViewWC = findViewById(R.id.WC);
        imageViewMarket = findViewById(R.id.Market);
        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewRestaurant = findViewById(R.id.Restaurant);
        imageViewParkSpot = findViewById(R.id.ParkSpot);
        commentSection = findViewById(R.id.section_comment);
        errorComment = findViewById(R.id.errorNoComment);
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
                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);

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

        // check the station added favorites
        if (userFavorites.contains(choosenStationID + ";")) {
            favorite.setColorFilter(Color.argb(255, 255, 127, 80), PorterDuff.Mode.SRC_IN);
        }

        // Campaigns
        mRecyclerView = findViewById(R.id.campaignView);

        // Comments
        mRecyclerView2 = findViewById(R.id.commentView);

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

        floatingActionButton2 = findViewById(R.id.fab2);
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDesignFAM.close(true);
                reportPrices(v);
            }
        });

        floatingActionButton3 = findViewById(R.id.fab3);
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
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
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(stationID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void fetchStationFinance() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION_PRICES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = res.length() - 1; i >= 0; i--) {
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
                                    LineDataSet dataSet = new LineDataSet(gasolinePriceHistory, getString(R.string.gasoline)); // add entries to dataset
                                    dataSet.setDrawValues(false);
                                    dataSet.setColor(Color.BLACK);
                                    dataSet.setDrawCircles(false);
                                    dataSet.setDrawFilled(true);
                                    dataSet.setFillColor(Color.parseColor("#90000000"));
                                    dataSets.add(dataSet);
                                }

                                if (dieselPriceHistory.size() > 0) {
                                    LineDataSet dataSet2 = new LineDataSet(dieselPriceHistory, getString(R.string.diesel)); // add entries to dataset
                                    dataSet2.setDrawValues(false);
                                    dataSet2.setColor(Color.RED);
                                    dataSet2.setDrawCircles(false);
                                    dataSet2.setDrawFilled(true);
                                    dataSet2.setFillColor(Color.parseColor("#90FF0000"));
                                    dataSets.add(dataSet2);
                                }

                                if (lpgPriceHistory.size() > 0) {
                                    LineDataSet dataSet3 = new LineDataSet(lpgPriceHistory, getString(R.string.lpg)); // add entries to dataset
                                    dataSet3.setDrawValues(false);
                                    dataSet3.setColor(Color.BLUE);
                                    dataSet3.setDrawCircles(false);
                                    dataSet3.setDrawFilled(true);
                                    dataSet3.setFillColor(Color.parseColor("#900000FF"));
                                    dataSets.add(dataSet3);
                                }

                                if (elecPriceHistory.size() > 0) {
                                    LineDataSet dataSet4 = new LineDataSet(elecPriceHistory, getString(R.string.electricity)); // add entries to dataset
                                    dataSet4.setDrawValues(false);
                                    dataSet4.setColor(Color.GREEN);
                                    dataSet4.setDrawCircles(false);
                                    dataSet4.setDrawFilled(true);
                                    dataSet4.setFillColor(Color.parseColor("#9000FF00"));
                                    dataSets.add(dataSet4);
                                }

                                LineData lineData = new LineData(dataSets);
                                chart.setData(lineData);
                                chart.getXAxis().setAvoidFirstLastClipping(true);
                                chart.getXAxis().setLabelCount(3, true);
                                chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                                chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, AxisBase axis) {
                                        DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                                        Date date = new Date();
                                        date.setTime((long) value);
                                        return formatter.format(date);
                                    }
                                });
                                chart.getDescription().setText(currencySymbol + " / " + userUnit);
                                chart.getDescription().setTextSize(12f);
                                chart.getDescription().setTextColor(Color.WHITE);
                                chart.setExtraRightOffset(10f);
                                chart.animateX(1500, Easing.EasingOption.EaseInSine);
                                chart.invalidate(); // refresh
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
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(choosenStationID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void loadStationDetails() {
        webview.loadUrl("https://www.google.com/maps?cbll=" + stationLocation.split(";")[0] + "," + stationLocation.split(";")[1] + "&layer=c");
        webview.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // Do something
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

        chart = findViewById(R.id.chart);

        // Facilities
        // Facilities
        try {
            JSONArray facilitiesRes = new JSONArray(facilitiesOfStation);
            JSONObject facilitiesObj = facilitiesRes.getJSONObject(0);

            if (facilitiesObj.getInt("WC") == 1) {
                imageViewWC.setAlpha(1.0f);
            } else {
                imageViewWC.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Market") == 1) {
                imageViewMarket.setAlpha(1.0f);
            } else {
                imageViewMarket.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("CarWash") == 1) {
                imageViewCarWash.setAlpha(1.0f);
            } else {
                imageViewCarWash.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("TireRepair") == 1) {
                imageViewTireRepair.setAlpha(1.0f);
            } else {
                imageViewTireRepair.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Mechanic") == 1) {
                imageViewMechanic.setAlpha(1.0f);
            } else {
                imageViewMechanic.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Restaurant") == 1) {
                imageViewRestaurant.setAlpha(1.0f);
            } else {
                imageViewRestaurant.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("ParkSpot") == 1) {
                imageViewParkSpot.setAlpha(1.0f);
            } else {
                imageViewParkSpot.setAlpha(0.5f);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        fetchStationFinance();
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
                params.put("stationID", String.valueOf(choosenStationID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

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
                                    floatingActionButton1.setLabelText("Yorumu güncelle");
                                } else {
                                    floatingActionButton1.setLabelText("Yorum yaz");
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
                                errorComment.setVisibility(View.VISIBLE);
                                noCommentText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            hasAlreadyCommented = false;
                            textViewStationPoint.setText(0 + " yorum" + " - " + 0.0);
                            errorComment.setVisibility(View.VISIBLE);
                            noCommentText.setVisibility(View.VISIBLE);
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
                params.put("stationID", String.valueOf(choosenStationID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

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

        TextView titlePopup = customView.findViewById(R.id.campaignPhoto);
        if (hasAlreadyCommented) {
            titlePopup.setText("Yorumunu güncelle");
        } else {
            titlePopup.setText("Yorum yaz");
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
                    Toast.makeText(StationDetails.this, "Lütfen yorumunuzu yazınız", Toast.LENGTH_SHORT).show();
                }
            }

            private void addComment() {
                final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Yorumunuz ekleniyor", "Lütfen bekleyiniz...", true, false);
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
                final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Yorumunuz güncelleniyor...", "Lütfen bekleyiniz...", true, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_COMMENT),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                loading.dismiss();
                                Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
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
        });
    }

    void reportStation(final View view) {
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
                    reportReason = position + " - " + spinner.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                reportReason = 0 + " - " + spinner.getItemAtPosition(0);
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
                    reportDetails = s.toString();
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
                sendReporttoServer(username, choosenStationID, reportReason, reportDetails, null, bitmap);
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
                pricesArray[0] = "REPORT: { gasoline = " + benzinFiyat[0] + " diesel = " + dizelFiyat[0] + " lpg = " + LPGFiyat[0] + " electricity = " + ElektrikFiyat[0] + " }";
                sendReporttoServer(username, choosenStationID, getApplicationContext().getResources().getStringArray(R.array.report_reasons)[5], null, pricesArray[0], bitmap);
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
        final ProgressDialog loading = ProgressDialog.show(StationDetails.this, "Sending report...", "Please wait...", false, true);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_REPORT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(StationDetails.this, response, Toast.LENGTH_LONG).show();
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    loading.dismiss();
                                    Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
                                    mPopupWindow.dismiss();
                                    break;
                                case "Fail":
                                    loading.dismiss();
                                    Toast.makeText(StationDetails.this, response, Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } else {
                            loading.dismiss();
                            Toast.makeText(StationDetails.this, "Error", Toast.LENGTH_SHORT).show();
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
                if (raporDetayi != null && raporDetayi.length() > 0) {
                    params.put("details", raporDetayi);
                }
                if (fiyatlar != null && fiyatlar.length() > 0) {
                    params.put("prices", fiyatlar);
                }
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                }
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bmp) {
        Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp2.compress(Bitmap.CompressFormat.JPEG, 65, baos);

        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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

    void clearVariables() {
        stationDistance = 0;
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
        favorite.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
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
                    favorite.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
                    Toast.makeText(StationDetails.this, "Favorilerinizden çıkartıldı.", Toast.LENGTH_SHORT).show();
                } else {
                    userFavorites += choosenStationID + ";";
                    favorite.setColorFilter(Color.argb(255, 255, 127, 80), PorterDuff.Mode.SRC_IN);
                    Toast.makeText(StationDetails.this, "Favorilerinize eklendi.", Toast.LENGTH_SHORT).show();
                }
                prefs.edit().putString("userFavorites", userFavorites).apply();
                return true;
            case R.id.menu_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, stationName + " on FuelSpot: " + "https://fuel-spot.com/stations?id=" + choosenStationID);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (ActivityCompat.checkSelfPermission(StationDetails.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.create(StationDetails.this).single().start(reportRequest);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
                }
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
                bitmap = BitmapFactory.decodeFile(image.getPath());
                switch (reportRequest) {
                    case 0:
                        Glide.with(StationDetails.this).load(image.getPath()).apply(options).into(reportStationPhoto);
                        break;
                    case 1:
                        Glide.with(StationDetails.this).load(image.getPath()).apply(options).into(reportPricePhoto);
                        break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearVariables();
        finish();
    }
}