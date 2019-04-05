package com.fuelspot;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fuelspot.adapter.GraphMarkerAdapter;
import com.fuelspot.adapter.NewsAdapter;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.NewsItem;
import com.github.mikephil.charting.animation.Easing;
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
import com.github.ybq.android.spinkit.SpinKitView;
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

import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.universalTimeFormat;
import static com.fuelspot.MainActivity.userUnit;

public class FragmentNews extends Fragment {

    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private List<NewsItem> feedsList = new ArrayList<>();
    private RelativeLayout errorLayout;
    private SpinKitView proggressBar;
    private View rootView;
    private RequestQueue requestQueue;
    private NestedScrollView scrollView;
    private SimpleDateFormat sdf;

    // Price Index
    private LineChart chart;
    private List<Entry> gasolinePriceHistory = new ArrayList<>();
    private List<Entry> dieselPriceHistory = new ArrayList<>();
    private List<Entry> lpgPriceHistory = new ArrayList<>();
    private List<Entry> elecPriceHistory = new ArrayList<>();
    private TextView lastUpdatedAvgPrice;

    // Companies
    private PieChart chart3;
    private TextView textViewTotalNumber;
    private ArrayList<PieEntry> entries = new ArrayList<>();
    private int otherStations;
    private int totalVerified;
    private int totalStation;

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
               /* AdRequest adRequest = new AdRequest.Builder().addTestDevice("EEB32226D1D806C1259761D5FF4A8C41").build();
                mAdView.loadAd(adRequest);*/
            } else {
                mAdView.setVisibility(View.GONE);
            }

            requestQueue = Volley.newRequestQueue(getActivity());
            sdf = new SimpleDateFormat(universalTimeFormat, Locale.getDefault());
            scrollView = rootView.findViewById(R.id.newsInfoFragment);
            proggressBar = rootView.findViewById(R.id.spin_kit);
            proggressBar.setColor(Color.BLUE);
            mRecyclerView = rootView.findViewById(R.id.newsView);
            mRecyclerView.setNestedScrollingEnabled(false);
            errorLayout = rootView.findViewById(R.id.newsErrorLayout);

            chart = rootView.findViewById(R.id.chartAveragePrice);
            chart.getXAxis().setAvoidFirstLastClipping(true);
            chart.getXAxis().setLabelCount(3, true);
            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getDescription().setText(currencySymbol + " / " + userUnit);
            chart.getDescription().setTextSize(12f);
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
            chart.animateX(1000, Easing.EaseInElastic);
            chart.setExtraRightOffset(16f);
            chart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            lastUpdatedAvgPrice = rootView.findViewById(R.id.dummy000);

            chart3 = rootView.findViewById(R.id.chart3);
            chart3.getDescription().setEnabled(false);
            chart3.setDragDecelerationFrictionCoef(0.95f);
            chart3.setDrawHoleEnabled(false);
            chart3.getLegend().setEnabled(false);
            chart3.setTransparentCircleColor(Color.BLACK);
            chart3.setTransparentCircleAlpha(120);
            chart3.setTransparentCircleRadius(60f);
            chart3.setUsePercentValues(false);
            chart3.setRotationEnabled(true);
            chart3.setHighlightPerTapEnabled(true);
            chart3.setEntryLabelColor(Color.BLACK);
            chart3.setEntryLabelTextSize(12f);
            chart3.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            textViewTotalNumber = rootView.findViewById(R.id.textViewtoplamSayi);

            // ÜLKE SEÇİMİ
            fetchNews("TR");
            fetchCountryFinance("TR");
            parseCompanies();
        }
        return rootView;
    }

    private void fetchNews(final String tempCountryCode) {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_NEWS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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
                                errorLayout.setVisibility(View.GONE);
                                proggressBar.setVisibility(View.GONE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                errorLayout();
                            }
                        } else {
                            errorLayout();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                        errorLayout();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("country", tempCountryCode);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchCountryFinance(final String tempCountryCode) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_COUNTRY_PRICES),
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

                                String dummyLastText = getString(R.string.last_update) + " " + res.getJSONObject(0).getString("date");
                                lastUpdatedAvgPrice.setText(dummyLastText);

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
                                    dataSet4.setFillColor(Color.parseColor("#9000FF00"));
                                    dataSet4.setDrawFilled(true);
                                    dataSets.add(dataSet4);
                                }

                                LineData lineData = new LineData(dataSets);
                                chart.setData(lineData);
                                GraphMarkerAdapter mv = new GraphMarkerAdapter(getActivity(), R.layout.popup_graph_marker, dataSets);
                                chart.setMarker(mv);
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
                        Toast.makeText(getActivity(), volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("country", tempCountryCode);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void parseCompanies() {
        if (companyList != null && companyList.size() > 0) {
            for (int i = 0; i < companyList.size(); i++) {
                totalVerified += companyList.get(i).getNumOfVerifieds();
                totalStation += companyList.get(i).getNumOfStations();

                if (companyList.get(i).getNumOfStations() >= 300) {
                    entries.add(new PieEntry((float) companyList.get(i).getNumOfStations(), companyList.get(i).getName()));
                } else {
                    otherStations += companyList.get(i).getNumOfStations();
                }
            }

            entries.add(new PieEntry((float) otherStations, getString(R.string.other)));

            PieDataSet dataSet = new PieDataSet(entries, getString(R.string.fuel_dist_comp));

            // add a lot of colors
            ArrayList<Integer> colors = new ArrayList<>();

            for (int c : ColorTemplate.VORDIPLOM_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.JOYFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.LIBERTY_COLORS)
                colors.add(c);

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());

            dataSet.setColors(colors);
            //dataSet.setSelectionShift(0f);

            PieData data = new PieData(dataSet);
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.BLACK);
            chart3.setData(data);
            chart3.highlightValues(null);
            chart3.invalidate();

            String dummy = getString(R.string.registered_station_number) + ": " + totalStation;
            textViewTotalNumber.setText(dummy);
        } else {
            // Somehow companList didn't fetch at MainActivity or SuperMainActivity. Fetch it.
            fetchCompanies();
        }
    }

    public Drawable getDrawableOfLogo(CompanyItem item) {
        final Drawable[] drawable = new Drawable[1];

        Glide.with(getActivity())
                .asBitmap()
                .load(item.getLogo())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        drawable[0] = new BitmapDrawable(getActivity().getResources(), resource);

                    }
                });

        return drawable[0];
    }

    private void fetchCompanies() {
        companyList.clear();
        totalStation = 0;
        totalVerified = 0;
        otherStations = 0;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_COMPANY),
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

    private void errorLayout() {
        errorLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.no_news), Snackbar.LENGTH_LONG).show();
    }
}
