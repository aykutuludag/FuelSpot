package com.fuelspot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.adapter.NewsAdapter;
import com.fuelspot.model.NewsItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static com.fuelspot.MainActivity.isGlobalNews;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.username;

public class FragmentNews extends Fragment {

    SwipeRefreshLayout swipeContainer;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<NewsItem> feedsList;
    String feedURL;
    ImageView errorPhoto;
    SharedPreferences prefs;

    public static FragmentNews newInstance() {

        Bundle args = new Bundle();

        FragmentNews fragment = new FragmentNews();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("Haberler");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        swipeContainer = rootView.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (feedURL != null && feedURL.length() > 0) {
                    fetchNews(feedURL);
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        feedsList = new ArrayList<>();
        mRecyclerView = rootView.findViewById(R.id.feedView);

        errorPhoto = rootView.findViewById(R.id.errorPhoto);

        contentChooserByCountry(userCountry);

        return rootView;
    }

    private void contentChooserByCountry(String country) {
        if (isGlobalNews) {
            feedURL = getString(R.string.API_FETCH_NEWS);
        } else {
            switch (country) {
                case "AZ":
                    feedURL = "http://fuel-spot.com/category/countries/azerbaijan/feed/json";
                    break;
                case "DE":
                    feedURL = "http://fuel-spot.com/category/countries/germany/feed/json";
                    break;
                case "TR":
                    feedURL = "http://fuel-spot.com/category/countries/turkey/feed/json";
                    break;
                case "US":
                    feedURL = "http://fuel-spot.com/category/countries/united-states/feed/json";
                    break;
                default:
                    feedURL = getString(R.string.API_FETCH_NEWS);
                    break;

            }
        }
        fetchNews(feedURL);
    }

    private void fetchNews(String url) {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONObject jsonObj = new JSONObject(response);
                                JSONArray res = jsonObj.getJSONArray("items");
                                if (res.length() > 0) {
                                    for (int i = 0; i < res.length(); i++) {
                                        JSONObject obj = res.getJSONObject(i);

                                        NewsItem item = new NewsItem();
                                        item.setLink(obj.getString("url"));
                                        item.setTitle(String.valueOf(Html.fromHtml(obj.getString("title"))));
                                        String[] thumbNailHolder = obj.getString("content_html").split("\"");
                                        item.setThumbnail(thumbNailHolder[5]);
                                        item.setPublishDate(obj.getString("date_published"));
                                        feedsList.add(item);

                                        mAdapter = new NewsAdapter(getActivity(), feedsList);
                                        mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                        mAdapter.notifyDataSetChanged();
                                        mRecyclerView.setAdapter(mAdapter);
                                        mRecyclerView.setLayoutManager(mLayoutManager);
                                        swipeContainer.setRefreshing(false);
                                        errorPhoto.setVisibility(View.GONE);
                                    }
                                } else {
                                    errorLayout();
                                }
                            } catch (JSONException e) {
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
                        errorLayout();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void errorLayout() {
        swipeContainer.setRefreshing(false);
        errorPhoto.setVisibility(View.VISIBLE);
        Snackbar.make(getActivity().findViewById(android.R.id.content), "Ülkenizle alakalı bir haber bulunamadı. Tüm haberleri göster?", Snackbar.LENGTH_LONG)
                .setAction("Tamam", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fetchNews(getString(R.string.API_FETCH_NEWS));
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Tüm haberler gösteriliyor. Ayarlardan açıp/kapatabilirsiniz.", Snackbar.LENGTH_LONG)
                                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                                .show();
                        isGlobalNews = true;
                        prefs.edit().putBoolean("isGlobalNews", isGlobalNews).apply();

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }
}
