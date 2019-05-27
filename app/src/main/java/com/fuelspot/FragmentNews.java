package com.fuelspot;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.UniversalTimeFormat;
import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userUnit;

public class FragmentNews extends Fragment {

    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private List<NewsItem> feedsList = new ArrayList<>();
    private View rootView;
    private RequestQueue requestQueue;
    private NestedScrollView scrollView;
    private SimpleDateFormat sdf = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
    private SimpleDateFormat sdf2 = new SimpleDateFormat(UniversalTimeFormat, Locale.getDefault());

    // Price Index
    private LineChart chart, chart2, chart3, chart4;
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

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Haberler");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            AdView mAdView = rootView.findViewById(R.id.adView);
            if (!premium) {
                AdRequest adRequest = new AdRequest.Builder().addTestDevice("EEB32226D1D806C1259761D5FF4A8C41").build();
                mAdView.loadAd(adRequest);
            } else {
                mAdView.setVisibility(View.GONE);
            }

            requestQueue = Volley.newRequestQueue(getActivity());
            scrollView = rootView.findViewById(R.id.newsInfoFragment);
            mRecyclerView = rootView.findViewById(R.id.newsView);
            mRecyclerView.setNestedScrollingEnabled(true);

            chart = rootView.findViewById(R.id.chartAveragePriceGasoline);
            chart.setScaleEnabled(false);
            chart.setDragEnabled(false);
            chart.getXAxis().setAvoidFirstLastClipping(true);
            chart.getXAxis().setLabelCount(3, true);
            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getDescription().setText(currencySymbol + " / " + userUnit);
            chart.getDescription().setTextSize(13f);
            chart.getDescription().setTextColor(Color.BLACK);
            chart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                    Date date = new Date();
                    date.setTime((long) value);
                    return formatter.format(date);
                }
            });

            chart2 = rootView.findViewById(R.id.chartAveragePriceDiesel);
            chart2.setScaleEnabled(false);
            chart2.setDragEnabled(false);
            chart2.getXAxis().setAvoidFirstLastClipping(true);
            chart2.getXAxis().setLabelCount(3, true);
            chart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart2.getDescription().setText(currencySymbol + " / " + userUnit);
            chart2.getDescription().setTextSize(13f);
            chart2.getDescription().setTextColor(Color.BLACK);
            chart2.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                    Date date = new Date();
                    date.setTime((long) value);
                    return formatter.format(date);
                }
            });

            chart3 = rootView.findViewById(R.id.chartAveragePriceLPG);
            chart3.setScaleEnabled(false);
            chart3.setDragEnabled(false);
            chart3.getXAxis().setAvoidFirstLastClipping(true);
            chart3.getXAxis().setLabelCount(3, true);
            chart3.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart3.getDescription().setText(currencySymbol + " / " + userUnit);
            chart3.getDescription().setTextSize(12f);
            chart3.getDescription().setTextColor(Color.BLACK);
            chart3.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                    Date date = new Date();
                    date.setTime((long) value);
                    return formatter.format(date);
                }
            });

            chart4 = rootView.findViewById(R.id.chartAveragePriceElectricity);
            chart4.setScaleEnabled(false);
            chart4.setDragEnabled(false);
            chart4.getXAxis().setAvoidFirstLastClipping(true);
            chart4.getXAxis().setLabelCount(3, true);
            chart4.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart4.getDescription().setText(currencySymbol + " / " + userUnit);
            chart4.getDescription().setTextSize(12f);
            chart4.getDescription().setTextColor(Color.BLACK);
            chart4.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                    Date date = new Date();
                    date.setTime((long) value);
                    return formatter.format(date);
                }
            });

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
            fetchNews(userCountry);
            fetchCountryFinance(userCountry);
            parseCompanies();
        }
        return rootView;
    }

    private void fetchNews(final String tempCountryCode) {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_NEWS) + "?country=" + tempCountryCode + "&AUTH_KEY=" + getString(R.string.fuelspot_api_key),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        swipeContainer.setRefreshing(false);
                        if (response != null && response.length() > 0) {
                            try {
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
                                    feedsList.add(item);
                                }

                                mAdapter = new NewsAdapter(getActivity(), feedsList);
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
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchCountryFinance(final String tempCountryCode) {
        gasolinePriceHistory.clear();
        dieselPriceHistory.clear();
        lpgPriceHistory.clear();
        elecPriceHistory.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_COUNTRY_PRICES) + "?country=" + tempCountryCode + "&AUTH_KEY=" + getString(R.string.fuelspot_api_key),
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

                                if (gasolinePriceHistory.size() > 0) {
                                    LineDataSet dataSet = new LineDataSet(gasolinePriceHistory, getString(R.string.gasoline)); // add entries to dataset
                                    dataSet.setDrawValues(false);
                                    dataSet.setDrawCircles(false);
                                    dataSet.setColor(Color.BLACK);

                                    LineData lineData = new LineData(dataSet);
                                    chart.setData(lineData);
                                    chart.invalidate();
                                    dataSets.add(dataSet);
                                }

                                if (dieselPriceHistory.size() > 0) {
                                    LineDataSet dataSet2 = new LineDataSet(dieselPriceHistory, getString(R.string.diesel)); // add entries to dataset
                                    dataSet2.setDrawValues(false);
                                    dataSet2.setDrawCircles(false);
                                    dataSet2.setColor(Color.RED);

                                    LineData lineData = new LineData(dataSet2);
                                    chart2.setData(lineData);
                                    chart2.invalidate();
                                    dataSets.add(dataSet2);
                                }

                                if (lpgPriceHistory.size() > 0) {
                                    LineDataSet dataSet3 = new LineDataSet(lpgPriceHistory, getString(R.string.lpg)); // add entries to dataset
                                    dataSet3.setDrawValues(false);
                                    dataSet3.setDrawCircles(false);
                                    dataSet3.setColor(Color.BLUE);

                                    LineData lineData = new LineData(dataSet3);
                                    chart3.setData(lineData);
                                    chart3.invalidate();
                                    dataSets.add(dataSet3);
                                }

                                if (elecPriceHistory.size() > 0) {
                                    LineDataSet dataSet4 = new LineDataSet(elecPriceHistory, getString(R.string.electricity)); // add entries to dataset
                                    dataSet4.setDrawValues(false);
                                    dataSet4.setDrawCircles(false);
                                    dataSet4.setColor(Color.GREEN);

                                    LineData lineData = new LineData(dataSet4);
                                    chart4.setData(lineData);
                                    chart4.invalidate();
                                    dataSets.add(dataSet4);
                                }

                                GraphMarkerAdapter mv = new GraphMarkerAdapter(getActivity(), R.layout.popup_graph_marker, dataSets);
                                chart.setMarker(mv);
                                chart2.setMarker(mv);
                                chart3.setMarker(mv);
                                chart4.setMarker(mv);

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
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void parseCompanies() {
        if (companyList != null && companyList.size() > 0) {
            for (int i = 0; i < companyList.size(); i++) {
                totalStation += companyList.get(i).getNumOfStations();

                if (companyList.get(i).getNumOfStations() >= 350) {
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

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_COMPANY) + "?AUTH_KEY=" + getString(R.string.fuelspot_api_key),
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
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
}
