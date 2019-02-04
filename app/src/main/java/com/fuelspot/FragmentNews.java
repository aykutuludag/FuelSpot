package com.fuelspot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.NewsAdapter;
import com.fuelspot.model.NewsItem;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.ybq.android.spinkit.SpinKitView;
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

import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.universalTimeFormat;
import static com.fuelspot.MainActivity.userUnit;

public class FragmentNews extends Fragment {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<NewsItem> feedsList = new ArrayList<>();
    RelativeLayout errorLayout;
    SharedPreferences prefs;
    SpinKitView proggressBar;
    ArrayAdapter adapter;
    View rootView;
    RequestQueue requestQueue;
    NestedScrollView scrollView;

    LineChart chart;
    List<Entry> gasolinePriceHistory = new ArrayList<>();
    List<Entry> dieselPriceHistory = new ArrayList<>();
    List<Entry> lpgPriceHistory = new ArrayList<>();
    List<Entry> elecPriceHistory = new ArrayList<>();
    TextView lastUpdatedAvgPrice;

    LineChart chart2;
    List<Entry> purchaseHistoryOf = new ArrayList<>();
    TextView lastUpdatedVolume;
    SimpleDateFormat sdf;

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

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Haberler");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            requestQueue = Volley.newRequestQueue(getActivity());
            scrollView = rootView.findViewById(R.id.newsInfoFragment);
            proggressBar = rootView.findViewById(R.id.spin_kit);
            proggressBar.setColor(Color.BLUE);
            mRecyclerView = rootView.findViewById(R.id.newsView);
            mRecyclerView.setNestedScrollingEnabled(false);
            errorLayout = rootView.findViewById(R.id.newsErrorLayout);
            chart = rootView.findViewById(R.id.chartAveragePrice);
            chart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            lastUpdatedAvgPrice = rootView.findViewById(R.id.dummy000);
            chart2 = rootView.findViewById(R.id.chartVolume);
            chart2.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            lastUpdatedVolume = rootView.findViewById(R.id.dummy001);
            sdf = new SimpleDateFormat(universalTimeFormat, Locale.getDefault());
            // ÜLKE SEÇİMİ
            fetchNews("TR");
            fetchCountryFinance("TR");
            fetchVolume("TR");
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
                                Toast.makeText(getActivity(), "Eksepsion: " + e.toString(), Toast.LENGTH_SHORT).show();
                                errorLayout();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Empty response", Toast.LENGTH_SHORT).show();
                            errorLayout();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), "Volley error: " + volleyError.toString(), Toast.LENGTH_SHORT).show();
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

                                String dummyLastText = "Son güncelleme: " + res.getJSONObject(0).getString("date");
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

    private void fetchVolume(final String tempCountryCode) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_COUNTRY_VOLUME),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = res.length() - 1; i >= 0; i--) {
                                    JSONObject obj = res.getJSONObject(i);
                                    if (obj.getDouble("totalPrice") != 0) {
                                        purchaseHistoryOf.add(new Entry((float) sdf.parse(obj.getString("time")).getTime(), (float) obj.getDouble("totalPrice")));
                                    }
                                }

                                String dummyLastText = "Son güncelleme: " + res.getJSONObject(0).getString("time");
                                lastUpdatedVolume.setText(dummyLastText);

                                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                if (purchaseHistoryOf.size() > 0) {
                                    LineDataSet dataSet = new LineDataSet(purchaseHistoryOf, "Hacim: " + "(" + currencySymbol + ")"); // add entries to dataset
                                    dataSet.setColor(Color.BLACK);
                                    dataSet.setDrawValues(false);
                                    dataSet.setDrawCircles(false);
                                    dataSet.setFillColor(Color.parseColor("#90000000"));
                                    dataSet.setDrawFilled(true);
                                    dataSets.add(dataSet);
                                }

                                LineData lineData = new LineData(dataSets);
                                chart2.setData(lineData);
                                chart2.getXAxis().setAvoidFirstLastClipping(true);
                                chart2.getXAxis().setLabelCount(3, true);
                                chart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                                chart2.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                                    @Override
                                    public String getFormattedValue(float value, AxisBase axis) {
                                        DateFormat formatter = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
                                        Date date = new Date();
                                        date.setTime((long) value);
                                        return formatter.format(date);
                                    }
                                });
                                chart2.getDescription().setText(currencySymbol);
                                chart2.getDescription().setTextSize(12f);
                                chart2.getDescription().setTextColor(Color.WHITE);
                                chart2.setExtraRightOffset(10f);
                                chart2.animateX(1500, Easing.EasingOption.EaseInSine);
                                chart2.invalidate(); // refresh
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
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

    void errorLayout() {
        errorLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        Snackbar.make(getActivity().findViewById(android.R.id.content), "Ülkenizle alakalı bir haber bulunamadı.", Snackbar.LENGTH_LONG).show();
    }
}
