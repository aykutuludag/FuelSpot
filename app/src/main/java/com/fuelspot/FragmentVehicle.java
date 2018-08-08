package com.fuelspot;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.fuelspot.adapter.PurchaseAdapter;
import com.fuelspot.model.PurchaseItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class FragmentVehicle extends Fragment {

    CircleImageView carPhotoHolder;
    SwipeRefreshLayout swipeContainer;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<PurchaseItem> feedsList;
    SharedPreferences prefs;

    ImageView fuelTypeIndicator, fuelTypeIndicator2;
    TextView kilometerText, fullname, fuelType, fuelType2, avgText, avgPrice;
    RelativeTimeTextView lastUpdated;
    Snackbar snackBar;
    CircleImageView errorImage;

    public static FragmentVehicle newInstance() {

        Bundle args = new Bundle();

        FragmentVehicle fragment = new FragmentVehicle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("Araç");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        errorImage = view.findViewById(R.id.errorImage);

        snackBar = Snackbar.make(getActivity().findViewById(R.id.mainContainer), "Henüz hiç satın alma yapmamışsınız.", Snackbar.LENGTH_LONG);
        snackBar.setAction("Tamam", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });

        //SETTING HEADER VEHICLE VARIABLES
        View headerView = view.findViewById(R.id.header_vehicle);

        //ProfilePhoto
        carPhotoHolder = headerView.findViewById(R.id.car_picture);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_automobile)
                .error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(getActivity()).load(Uri.parse(MainActivity.carPhoto)).apply(options).into(carPhotoHolder);

        //Marka-model
        fullname = headerView.findViewById(R.id.carFullname);
        String fullad = MainActivity.carBrand + " " + MainActivity.carModel;
        fullname.setText(fullad);

        //Yakıt tipi başlangıç
        fuelType = headerView.findViewById(R.id.car_fuelTypeText);
        fuelTypeIndicator = headerView.findViewById(R.id.car_fuelType);
        String fuelText;

        switch (MainActivity.fuelPri) {
            case 0:
                fuelText = "gasoline";
                fuelTypeIndicator.setImageResource(R.drawable.gasoline);
                break;
            case 1:
                fuelText = "diesel";
                fuelTypeIndicator.setImageResource(R.drawable.diesel);
                break;
            case 2:
                fuelText = "lpg";
                fuelTypeIndicator.setImageResource(R.drawable.lpg);
                break;
            case 3:
                fuelText = "electric";
                fuelTypeIndicator.setImageResource(R.drawable.electricity);
                break;
            default:
                fuelText = "";
                fuelTypeIndicator.setImageDrawable(null);
                break;
        }
        fuelType.setText(fuelText);

        fuelType2 = headerView.findViewById(R.id.car_fuelTypeText2);
        fuelTypeIndicator2 = headerView.findViewById(R.id.car_fuelType2);
        String fuelText2;
        switch (MainActivity.fuelSec) {
            case 0:
                fuelText2 = "gasoline";
                fuelTypeIndicator2.setImageResource(R.drawable.gasoline);
                break;
            case 1:
                fuelText2 = "diesel";
                fuelTypeIndicator2.setImageResource(R.drawable.diesel);
                break;
            case 2:
                fuelText2 = "lpg";
                fuelTypeIndicator2.setImageResource(R.drawable.lpg);
                break;
            case 3:
                fuelText2 = "electric";
                fuelTypeIndicator2.setImageResource(R.drawable.electricity);
                break;
            default:
                fuelText2 = "";
                fuelTypeIndicator2.setImageDrawable(null);
        }
        fuelType2.setText(fuelText2);
        //Yakıt tipi bitiş

        //Ortalama tüketim
        avgText = headerView.findViewById(R.id.car_avgCons);
        String avgDummy = String.format(Locale.getDefault(), "%.2f", MainActivity.averageCons) + " lt/100km";
        avgText.setText(avgDummy);

        //Ortalama maliyet
        avgPrice = headerView.findViewById(R.id.car_avgPrice);
        String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", MainActivity.averagePrice) + " TL/100km";
        avgPrice.setText(avgPriceDummy);

        //Aylık maliyet

        //Last updated
        lastUpdated = headerView.findViewById(R.id.car_lastUpdated);
        if (MainActivity.purchaseTimes.size() > 0) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(MainActivity.purchaseTimes.get(MainActivity.purchaseTimes.size() - 1));
                lastUpdated.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //EditProfile
        ImageView updateCar = headerView.findViewById(R.id.updateCarInfo);
        updateCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VehicleEditActivity.class);
                startActivity(intent);
            }
        });

        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUserPurchases();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        feedsList = new ArrayList<>();
        mRecyclerView = view.findViewById(R.id.feedView);

        return view;
    }

    private void fetchUserPurchases() {
        feedsList.clear();
        MainActivity.purchaseTimes.clear();
        MainActivity.purchaseKilometers.clear();
        MainActivity.purchasePrices.clear();
        MainActivity.purchaseUnitPrice.clear();
        MainActivity.purchaseUnitPrice2.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_USER_PURCHASES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    PurchaseItem item = new PurchaseItem();
                                    item.setID(obj.getInt("id"));
                                    item.setPurchaseTime(obj.getString("time"));
                                    item.setStationName(obj.getString("stationName"));
                                    item.setStationIcon(obj.getString("stationIcon"));
                                    item.setStationLocation(obj.getString("stationLocation"));
                                    item.setFuelType(obj.getInt("fuelType"));
                                    item.setFuelPrice((float) obj.getDouble("fuelPrice"));
                                    item.setFuelLiter((float) obj.getDouble("fuelLiter"));
                                    item.setFuelType2(obj.getInt("fuelType2"));
                                    item.setFuelPrice2((float) obj.getDouble("fuelPrice2"));
                                    item.setFuelLiter2((float) obj.getDouble("fuelLiter2"));
                                    item.setTotalPrice((float) obj.getDouble("totalPrice"));
                                    item.setBillPhoto(obj.getString("billPhoto"));
                                    feedsList.add(item);

                                    MainActivity.purchaseTimes.add(i, obj.getString("time"));
                                    MainActivity.purchaseUnitPrice.add(i, obj.getDouble("fuelPrice"));
                                    MainActivity.purchaseUnitPrice2.add(i, obj.getDouble("fuelPrice2"));
                                    MainActivity.purchasePrices.add(i, obj.getDouble("totalPrice"));
                                    MainActivity.purchaseKilometers.add(i, obj.getInt("kilometer"));
                                    MainActivity.purchaseLiters.add(i, obj.getDouble("fuelLiter") + obj.getDouble("fuelLiter2"));

                                    mAdapter = new PurchaseAdapter(getActivity(), feedsList);
                                    mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                    mAdapter.notifyDataSetChanged();
                                    mRecyclerView.setAdapter(mAdapter);
                                    mRecyclerView.setLayoutManager(mLayoutManager);
                                    swipeContainer.setRefreshing(false);
                                }
                                //BURADA TARİHE GÖRE GEÇMİŞTEN BUGÜNE SIRALIYORUZ
                                Collections.reverse(MainActivity.purchaseTimes);
                                Collections.reverse(MainActivity.purchasePrices);
                                Collections.reverse(MainActivity.purchaseKilometers);

                                //Calculate avg fuel consumption and update
                                if (avgText != null) {
                                    String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " lt/100km";
                                    avgText.setText(avgDummy);
                                }

                                //update kilometer
                                if (kilometerText != null) {
                                    String kmHolder = MainActivity.kilometer + " " + "km";
                                    kilometerText.setText(kmHolder);
                                }

                                //update avg price
                                if (avgPrice != null) {
                                    String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " TL/100km";
                                    avgPrice.setText(avgPriceDummy);
                                }

                                if (MainActivity.purchaseTimes.size() > 0) {
                                    try {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                        Date date = format.parse(MainActivity.purchaseTimes.get(MainActivity.purchaseTimes.size() - 1));
                                        lastUpdated.setReferenceTime(date.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            errorImage.setVisibility(View.VISIBLE);
                            snackBar.show();
                            swipeContainer.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        swipeContainer.setRefreshing(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", MainActivity.username);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public float calculateAverageCons() {
        float totalLiter = 0;
        float kilometerDifference = 0;
        if (MainActivity.purchaseTimes.size() > 1) {
            for (int i = 0; i < MainActivity.purchaseTimes.size(); i++) {
                totalLiter += MainActivity.purchaseLiters.get(i);
                kilometerDifference = MainActivity.purchaseKilometers.get(MainActivity.purchaseKilometers.size() - 1) - MainActivity.purchaseKilometers.get(0);
            }
            MainActivity.averageCons = (totalLiter / kilometerDifference) * 100f;
            prefs.edit().putFloat("averageConsumption", MainActivity.averageCons).apply();
        } else {
            MainActivity.averageCons = 0;
        }
        return MainActivity.averageCons;
    }

    public float calculateAvgPrice() {
        float totalPrice = 0;
        float kilometerDifference = 0;
        if (MainActivity.purchaseTimes.size() > 1) {
            for (int i = 0; i < MainActivity.purchaseTimes.size(); i++) {
                totalPrice += MainActivity.purchasePrices.get(i);
                kilometerDifference = MainActivity.purchaseKilometers.get(MainActivity.purchaseKilometers.size() - 1) - MainActivity.purchaseKilometers.get(0);
            }
            MainActivity.averagePrice = (totalPrice / kilometerDifference) * 100f;
            prefs.edit().putFloat("averagePrice", MainActivity.averagePrice).apply();
        } else {
            MainActivity.averagePrice = 0;
        }
        return MainActivity.averagePrice;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRecyclerView != null) {
            fetchUserPurchases();
        }
    }
}
