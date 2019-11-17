package com.fuelspot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.CampaignAdapter;
import com.fuelspot.adapter.GraphMarkerAdapter;
import com.fuelspot.adapter.NewsAdapter;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.NewsItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.UniversalTimeFormat;
import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.globalCampaignList;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userUnit;

public class FragmentNews extends Fragment {

    private RecyclerView mRecyclerView, mRecyclerViewKampanya;
    private GridLayoutManager mLayoutManager;
    static List<NewsItem> newsFeed = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    public View rootView;
    private RequestQueue requestQueue;
    private NestedScrollView scrollView;
    private SimpleDateFormat sdf = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
    private SimpleDateFormat sdf2 = new SimpleDateFormat(UniversalTimeFormat, Locale.getDefault());
    private DecimalFormat df = new DecimalFormat("#.##");
    // Price Index
    private LineChart chart;
    private List<Entry> gasolinePriceHistory = new ArrayList<>();
    private List<Entry> dieselPriceHistory = new ArrayList<>();
    private List<Entry> lpgPriceHistory = new ArrayList<>();
    private List<Entry> elecPriceHistory = new ArrayList<>();
    private TextView lastUpdatedAvgPrice;

    // Companies
    private PieChart pieChart;
    private TextView textViewTotalNumber;
    private ArrayList<PieEntry> entries = new ArrayList<>();
    private int otherStations;
    private int totalStation;
    private SwipeRefreshLayout swipeContainer;
    String naMevcut = "- " + currencySymbol;

    List<Float> list = new ArrayList<>();
    List<Float> list2 = new ArrayList<>();
    List<Float> list3 = new ArrayList<>();
    List<Float> list4 = new ArrayList<>();

    private TextView gasolineMin, gasolineMax, dieselMin, dieselMax, lpgMin, lpgMax, electricityMin, electricityMax;

    public static FragmentNews newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "News");

        FragmentNews fragment = new FragmentNews();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_news, container, false);

            // Keep screen off
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Find the Ad Container
            final AdView mAdView = rootView.findViewById(R.id.nativeAdView);
            if (premium) {
                mAdView.setVisibility(View.GONE);
            } else {
                AdRequest request = new AdRequest.Builder().build();
                mAdView.loadAd(request);
            }

            requestQueue = Volley.newRequestQueue(getActivity());
            scrollView = rootView.findViewById(R.id.newsInfoFragment);
            mRecyclerView = rootView.findViewById(R.id.newsView);
            mRecyclerView.setNestedScrollingEnabled(false);
            mRecyclerViewKampanya = rootView.findViewById(R.id.kampanyaView);
            mRecyclerViewKampanya.setNestedScrollingEnabled(false);

            chart = rootView.findViewById(R.id.chartPriceIndex);
            chart.setScaleEnabled(false);
            chart.setDragEnabled(false);
            chart.getXAxis().setAvoidFirstLastClipping(true);
            chart.getXAxis().setLabelCount(3, true);
            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getDescription().setText(currencySymbol + " / " + userUnit);
            chart.getDescription().setTextColor(Color.WHITE);
            chart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                    Date date = new Date();
                    date.setTime((long) value);
                    return formatter.format(date);
                }
            });

            gasolineMax = rootView.findViewById(R.id.gasolinePriceMax);
            gasolineMin = rootView.findViewById(R.id.gasolinePriceMin);

            dieselMax = rootView.findViewById(R.id.dieselPriceMax);
            dieselMin = rootView.findViewById(R.id.dieselPriceMin);

            lpgMax = rootView.findViewById(R.id.LPGPriceMax);
            lpgMin = rootView.findViewById(R.id.LPGPriceMin);

            electricityMax = rootView.findViewById(R.id.ElectricPriceMax);
            electricityMin = rootView.findViewById(R.id.ElectricPriceMin);

            lastUpdatedAvgPrice = rootView.findViewById(R.id.dummy000);

            pieChart = rootView.findViewById(R.id.chart3);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(false);
            pieChart.getLegend().setEnabled(false);
            pieChart.setDragDecelerationFrictionCoef(0.95f);
            pieChart.setTransparentCircleColor(Color.BLACK);
            pieChart.setTransparentCircleAlpha(120);
            pieChart.setTransparentCircleRadius(60f);
            pieChart.setUsePercentValues(false);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(false);
            pieChart.setEntryLabelColor(Color.BLACK);
            pieChart.setEntryLabelTextSize(12f);
            pieChart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            textViewTotalNumber = rootView.findViewById(R.id.textViewtoplamSayi);

            swipeContainer = rootView.findViewById(R.id.swipeContainer);
            // Setup refresh listener which triggers new data loading
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // ÜLKE SEÇİMİ
                    fetchNews("TR");
                    fetchCountryFinance("TR");
                }
            });
            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            // ÜLKE SEÇİMİ
            if (isNetworkConnected(getActivity())) {
                fetchNews(userCountry);
                loadGlobalCampaigns();
                fetchCountryFinance(userCountry);
                parseCompanies();
            } else {
                Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            }

            Button seeNewsButton = rootView.findViewById(R.id.button_seeAllNews);
            seeNewsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AllNews.class);
                    startActivity(intent);
                }
            });
        }
        return rootView;
    }

    private void loadGlobalCampaigns() {
        if (globalCampaignList != null && globalCampaignList.size() > 0) {
            RecyclerView.Adapter mAdapterKampanya = new CampaignAdapter(getActivity(), globalCampaignList, "USER");
            mRecyclerViewKampanya.setAdapter(mAdapterKampanya);
            mRecyclerViewKampanya.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            mAdapterKampanya.notifyDataSetChanged();
        }
    }

    private void fetchNews(final String tempCountryCode) {
        newsFeed.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_NEWS) + "?country=" + tempCountryCode,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        swipeContainer.setRefreshing(false);
                        if (response != null && response.length() > 0) {
                            try {
                                List<NewsItem> dummyList = new ArrayList<>();

                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    NewsItem item = new NewsItem();
                                    item.setID(obj.getInt("id"));
                                    item.setTitle(obj.getString("title"));
                                    item.setContent(obj.getString("content"));
                                    item.setPhoto(obj.getString("photo"));
                                    item.setCountry(obj.getString("country"));
                                    item.setTags(obj.getString("tags"));
                                    item.setURL(obj.getString("url"));
                                    item.setSourceURL(obj.getString("sourceURL"));
                                    item.setPublishDate(obj.getString("releaseDate"));
                                    newsFeed.add(item);

                                    if (i < 3) {
                                        dummyList.add(item);
                                    }
                                }

                                mAdapter = new NewsAdapter(getActivity(), dummyList);
                                mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                mAdapter.notifyDataSetChanged();
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

    private void fetchCountryFinance(final String tempCountryCode) {
        gasolinePriceHistory.clear();
        dieselPriceHistory.clear();
        lpgPriceHistory.clear();
        elecPriceHistory.clear();

        list.clear();
        list2.clear();
        list3.clear();
        list4.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_COUNTRY_PRICES) + "?country=" + tempCountryCode,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                JSONArray res = new JSONArray(response);


                                for (int i = res.length() - 1; i >= 0; i--) {
                                    JSONObject obj = res.getJSONObject(i);

                                    if (obj.getDouble("gasolinePrice") != 0) {
                                        if (obj.getDouble("gasolinePrice2") != 0) {
                                            float avgPrice = (float) (obj.getDouble("gasolinePrice") + obj.getDouble("gasolinePrice2")) / 2;
                                            gasolinePriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), avgPrice));
                                            list.add(avgPrice);
                                        } else {
                                            gasolinePriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("gasolinePrice")));
                                            list.add((float) obj.getDouble("gasolinePrice"));
                                        }
                                    }

                                    if (obj.getDouble("dieselPrice") != 0) {
                                        if (obj.getDouble("dieselPrice2") != 0) {
                                            float avgPrice = (float) (obj.getDouble("dieselPrice") + obj.getDouble("dieselPrice2")) / 2;
                                            dieselPriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), avgPrice));
                                            list2.add(avgPrice);
                                        } else {
                                            dieselPriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("dieselPrice")));
                                            list2.add((float) obj.getDouble("dieselPrice"));
                                        }
                                    }

                                    if (obj.getDouble("lpgPrice") != 0) {
                                        lpgPriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("lpgPrice")));
                                        list3.add((float) obj.getDouble("lpgPrice"));
                                    }

                                    if (obj.getDouble("electricityPrice") != 0) {
                                        elecPriceHistory.add(new Entry((float) sdf.parse(obj.getString("date")).getTime(), (float) obj.getDouble("electricityPrice")));
                                        list4.add((float) obj.getDouble("electricityPrice"));
                                    }
                                }

                                if (gasolinePriceHistory.size() > 0) {
                                    LineDataSet dataSet = new LineDataSet(gasolinePriceHistory, getString(R.string.gasoline)); // add entries to dataset
                                    dataSet.setDrawValues(false);
                                    dataSet.setDrawCircles(false);
                                    dataSet.setColor(Color.BLACK);
                                    dataSet.setDrawFilled(true);
                                    dataSet.setFillColor(Color.parseColor("#90000000"));
                                    dataSets.add(dataSet);

                                    float min = Collections.min(list);
                                    float max = Collections.max(list);


                                    String benzinMin = df.format(min) + " " + currencySymbol;
                                    gasolineMin.setText(benzinMin);

                                    String benzinMax = df.format(max) + " " + currencySymbol;
                                    gasolineMax.setText(benzinMax);
                                } else {
                                    gasolineMin.setText(naMevcut);
                                    gasolineMax.setText(naMevcut);
                                }

                                if (dieselPriceHistory.size() > 0) {
                                    LineDataSet dataSet2 = new LineDataSet(dieselPriceHistory, getString(R.string.diesel)); // add entries to dataset
                                    dataSet2.setDrawValues(false);
                                    dataSet2.setDrawCircles(false);
                                    dataSet2.setColor(Color.RED);
                                    dataSet2.setDrawFilled(true);
                                    dataSet2.setFillColor(Color.parseColor("#90FF0000"));
                                    dataSets.add(dataSet2);

                                    float min = Collections.min(list2);
                                    float max = Collections.max(list2);

                                    String dizelMin = df.format(min) + " " + currencySymbol;
                                    dieselMin.setText(dizelMin);

                                    String dizelMax = df.format(max) + " " + currencySymbol;
                                    dieselMax.setText(dizelMax);
                                } else {
                                    dieselMin.setText(naMevcut);
                                    dieselMax.setText(naMevcut);
                                }

                                if (lpgPriceHistory.size() > 0) {
                                    LineDataSet dataSet3 = new LineDataSet(lpgPriceHistory, getString(R.string.lpg)); // add entries to dataset
                                    dataSet3.setDrawValues(false);
                                    dataSet3.setDrawCircles(false);
                                    dataSet3.setColor(Color.BLUE);
                                    dataSet3.setDrawFilled(true);
                                    dataSet3.setFillColor(Color.parseColor("#900000FF"));
                                    dataSets.add(dataSet3);

                                    float min = Collections.min(list3);
                                    float max = Collections.max(list3);

                                    String otogazMin = df.format(min) + " " + currencySymbol;
                                    lpgMin.setText(otogazMin);

                                    String otogazMax = df.format(max) + " " + currencySymbol;
                                    lpgMax.setText(otogazMax);
                                } else {
                                    lpgMin.setText(naMevcut);
                                    lpgMax.setText(naMevcut);
                                }

                                if (elecPriceHistory.size() > 0) {
                                    LineDataSet dataSet4 = new LineDataSet(elecPriceHistory, getString(R.string.electricity)); // add entries to dataset
                                    dataSet4.setDrawValues(false);
                                    dataSet4.setDrawCircles(false);
                                    dataSet4.setColor(Color.GREEN);
                                    dataSet4.setFillColor(Color.parseColor("#9000FF00"));
                                    dataSet4.setDrawFilled(true);
                                    dataSets.add(dataSet4);

                                    float min = Collections.min(list4);
                                    float max = Collections.max(list4);

                                    String elecMin = df.format(min) + " " + currencySymbol;
                                    electricityMin.setText(elecMin);

                                    String elecMax = df.format(max) + " " + currencySymbol;
                                    electricityMax.setText(elecMax);
                                } else {
                                    electricityMin.setText(naMevcut);
                                    electricityMax.setText(naMevcut);
                                }

                                LineData lineData = new LineData(dataSets);
                                chart.setData(lineData);
                                GraphMarkerAdapter mv = new GraphMarkerAdapter(getActivity(), R.layout.popup_graph_marker, dataSets);
                                chart.setMarker(mv);
                                chart.invalidate(); // refresh

                                String dummyLastText = getString(R.string.last_update) + " " + sdf2.format(sdf.parse(res.getJSONObject(0).getString("date")));
                                lastUpdatedAvgPrice.setText(dummyLastText);
                            } catch (JSONException | ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), volleyError.toString(), Toast.LENGTH_SHORT).show();
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

    private void parseCompanies() {
        if (companyList != null && companyList.size() > 0) {
            for (int i = 0; i < companyList.size(); i++) {
                totalStation += companyList.get(i).getNumOfStations();

                if (companyList.get(i).getNumOfStations() >= 450) {
                    entries.add(new PieEntry((float) companyList.get(i).getNumOfStations(), companyList.get(i).getName()));
                } else {
                    otherStations += companyList.get(i).getNumOfStations();
                }
            }

            entries.add(new PieEntry((float) otherStations, getString(R.string.other)));

            PieDataSet dataSet = new PieDataSet(entries, getString(R.string.fuel_dist_comp));

            // add a lot of colors
            ArrayList<Integer> colors = new ArrayList<>();

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.VORDIPLOM_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.JOYFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());
            dataSet.setColors(colors);

            PieData data = new PieData(dataSet);
            data.setValueTextSize(12f);
            data.setValueTextColor(Color.BLACK);
            pieChart.setData(data);
            pieChart.invalidate();

            String dummy = getString(R.string.registered_station_number) + " " + totalStation;
            textViewTotalNumber.setText(dummy);
        } else {
            // Somehow companList didn't fetch at MainActivity or SuperMainActivity. Fetch it.
            fetchCompanies();
        }
    }

    private void fetchCompanies() {
        companyList.clear();
        totalStation = 0;
        otherStations = 0;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_COMPANY),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CompanyItem item = new CompanyItem();
                                    item.setID(obj.getInt("id"));
                                    item.setName(obj.getString("companyName"));
                                    item.setLogo(obj.getString("companyLogo"));
                                    item.setWebsite(obj.getString("companyWebsite"));
                                    item.setPhone(obj.getString("companyPhone"));
                                    item.setAddress(obj.getString("companyAddress"));
                                    item.setNumOfVerifieds(obj.getInt("numOfVerifieds"));
                                    item.setNumOfStations(obj.getInt("numOfStations"));
                                    companyList.add(item);
                                }
                                parseCompanies();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
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
}
