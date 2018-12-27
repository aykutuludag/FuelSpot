package com.fuelspot;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.fuelspot.adapter.NewsAdapter;
import com.fuelspot.model.NewsItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.getIndexOf;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userUnit;

public class FragmentNews extends Fragment {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<NewsItem> feedsList = new ArrayList<>();
    RelativeLayout errorLayout;
    SharedPreferences prefs;
    SpinKitView proggressBar;
    Spinner spinner;
    ArrayAdapter adapter;
    View rootView;
    RequestQueue requestQueue;

    LineChart chart;
    List<Entry> gasolinePriceHistory = new ArrayList<>();
    List<Entry> dieselPriceHistory = new ArrayList<>();
    List<Entry> lpgPriceHistory = new ArrayList<>();
    List<Entry> elecPriceHistory = new ArrayList<>();
    TextView lastUpdatedAvgPrice;

    LineChart chart2;
    List<Entry> purchaseHistoryOf = new ArrayList<>();
    TextView lastUpdatedVolume;

    public static FragmentNews newInstance() {

        Bundle args = new Bundle();

        FragmentNews fragment = new FragmentNews();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_news, container, false);

            // Analytics
            Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Haberler");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            requestQueue = Volley.newRequestQueue(getActivity());
            proggressBar = rootView.findViewById(R.id.spin_kit);
            proggressBar.setColor(Color.BLUE);
            mRecyclerView = rootView.findViewById(R.id.newsView);
            errorLayout = rootView.findViewById(R.id.newsErrorLayout);
            chart = rootView.findViewById(R.id.chartAveragePrice);
            lastUpdatedAvgPrice = rootView.findViewById(R.id.dummy000);
            chart2 = rootView.findViewById(R.id.chartVolume);
            lastUpdatedVolume = rootView.findViewById(R.id.dummy001);

            // ÜLKE SEÇİMİ
            spinner = rootView.findViewById(R.id.spinner_countries);
            adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.country_codes));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String tempCountry = spinner.getSelectedItem().toString();
                    fetchNews(tempCountry);
                    fetchCountryFinance(tempCountry);
                    fetchVolume(tempCountry);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
            spinner.setAdapter(adapter);

            // Select spinner and fetch news based on user country
            int index = getIndexOf(getResources().getStringArray(R.array.country_codes), userCountry);
            spinner.setSelection(index);
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

                                String dummyLastText = "Son güncelleme: " + res.getJSONObject(res.length() - 1).getString("date");
                                lastUpdatedAvgPrice.setText(dummyLastText);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    if (obj.getDouble("gasolinePrice") != 0) {
                                        gasolinePriceHistory.add(new Entry(i, (float) obj.getDouble("gasolinePrice")));
                                    }

                                    if (obj.getDouble("dieselPrice") != 0) {
                                        dieselPriceHistory.add(new Entry(i, (float) obj.getDouble("dieselPrice")));
                                    }

                                    if (obj.getDouble("lpgPrice") != 0) {
                                        lpgPriceHistory.add(new Entry(i, (float) obj.getDouble("lpgPrice")));
                                    }

                                    if (obj.getDouble("electricityPrice") != 0) {
                                        elecPriceHistory.add(new Entry(i, (float) obj.getDouble("electricityPrice")));
                                    }
                                }

                                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                if (gasolinePriceHistory.size() > 0) {
                                    LineDataSet dataSet = new LineDataSet(gasolinePriceHistory, getString(R.string.gasoline)); // add entries to dataset
                                    dataSet.setColor(Color.BLACK);
                                    dataSet.setDrawCircles(false);
                                    dataSets.add(dataSet);
                                }

                                if (dieselPriceHistory.size() > 0) {
                                    LineDataSet dataSet2 = new LineDataSet(dieselPriceHistory, getString(R.string.diesel)); // add entries to dataset
                                    dataSet2.setColor(Color.RED);
                                    dataSet2.setDrawCircles(false);
                                    dataSets.add(dataSet2);
                                }

                                if (lpgPriceHistory.size() > 0) {
                                    LineDataSet dataSet3 = new LineDataSet(lpgPriceHistory, getString(R.string.lpg)); // add entries to dataset
                                    dataSet3.setColor(Color.BLUE);
                                    dataSet3.setDrawCircles(false);
                                    dataSets.add(dataSet3);
                                }

                                if (elecPriceHistory.size() > 0) {
                                    LineDataSet dataSet4 = new LineDataSet(elecPriceHistory, getString(R.string.electricity)); // add entries to dataset
                                    dataSet4.setColor(Color.GREEN);
                                    dataSet4.setDrawCircles(false);
                                    dataSets.add(dataSet4);
                                }

                                LineData lineData = new LineData(dataSets);
                                chart.setData(lineData);
                                chart.getAxisRight().setEnabled(false);
                                chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                                chart.getDescription().setText(currencySymbol + " / " + userUnit);
                                chart.invalidate(); // refresh
                            } catch (JSONException e) {
                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
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

                                String dummyLastText = "Son güncelleme: " + res.getJSONObject(res.length() - 1).getString("time");
                                lastUpdatedVolume.setText(dummyLastText);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    if (obj.getDouble("totalPrice") != 0) {
                                        purchaseHistoryOf.add(new Entry(i, (float) obj.getDouble("totalPrice")));
                                    }
                                }

                                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                if (purchaseHistoryOf.size() > 0) {
                                    LineDataSet dataSet = new LineDataSet(purchaseHistoryOf, "Hacim: " + "(" + currencySymbol + ")"); // add entries to dataset
                                    dataSet.setColor(Color.BLACK);
                                    dataSet.setDrawCircles(false);
                                    dataSets.add(dataSet);
                                }

                                LineData lineData = new LineData(dataSets);
                                chart2.setData(lineData);
                                chart2.getAxisRight().setEnabled(false);
                                chart2.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                                chart2.getDescription().setText(currencySymbol);
                                chart2.invalidate(); // refresh
                            } catch (JSONException e) {
                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
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

    void errorLayout() {
        errorLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        Snackbar.make(getActivity().findViewById(android.R.id.content), "Ülkenizle alakalı bir haber bulunamadı.", Snackbar.LENGTH_LONG).show();
    }
}
