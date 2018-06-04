package org.uusoftware.fuelify;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uusoftware.fuelify.adapter.PurchaseAdapter;
import org.uusoftware.fuelify.model.PurchaseItem;

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

import static org.uusoftware.fuelify.MainActivity.averageCons;
import static org.uusoftware.fuelify.MainActivity.averagePrice;
import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.kilometer;
import static org.uusoftware.fuelify.MainActivity.purchaseKilometers;
import static org.uusoftware.fuelify.MainActivity.purchaseLiters;
import static org.uusoftware.fuelify.MainActivity.purchasePrices;
import static org.uusoftware.fuelify.MainActivity.purchaseTimes;
import static org.uusoftware.fuelify.MainActivity.purchaseUnitPrice;
import static org.uusoftware.fuelify.MainActivity.purchaseUnitPrice2;
import static org.uusoftware.fuelify.MainActivity.username;

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

        //SETTING HEADER VEHICLE VARIABLES
        View headerView = view.findViewById(R.id.header_vehicle);

        //ProfilePhoto
        carPhotoHolder = headerView.findViewById(R.id.car_picture);
        Glide.with(getActivity()).load(Uri.parse(carPhoto)).into(carPhotoHolder);

        //Marka-model
        fullname = headerView.findViewById(R.id.carFullname);
        String fullad = carBrand + " " + carModel;
        fullname.setText(fullad);

        //Yakıt tipi başlangıç
        fuelType = headerView.findViewById(R.id.car_fuelTypeText);
        fuelTypeIndicator = headerView.findViewById(R.id.car_fuelType);
        String fuelText;

        switch (fuelPri) {
            case 0:
                fuelText = "Gasoline";
                fuelTypeIndicator.setImageResource(R.drawable.gasoline);
                break;
            case 1:
                fuelText = "Diesel";
                fuelTypeIndicator.setImageResource(R.drawable.diesel);
                break;
            case 2:
                fuelText = "LPG";
                fuelTypeIndicator.setImageResource(R.drawable.lpg);
                break;
            case 3:
                fuelText = "Electric";
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
        switch (fuelSec) {
            case 0:
                fuelText2 = "Gasoline";
                fuelTypeIndicator2.setImageResource(R.drawable.gasoline);
                break;
            case 1:
                fuelText2 = "Diesel";
                fuelTypeIndicator2.setImageResource(R.drawable.diesel);
                break;
            case 2:
                fuelText2 = "LPG";
                fuelTypeIndicator2.setImageResource(R.drawable.lpg);
                break;
            case 3:
                fuelText2 = "Electric";
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
        String avgDummy = String.format(Locale.getDefault(), "%.2f", averageCons) + " lt/100km";
        avgText.setText(avgDummy);

        //Ortalama maliyet
        avgPrice = headerView.findViewById(R.id.car_avgPrice);
        String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", averagePrice) + " TL/100km";
        avgPrice.setText(avgPriceDummy);

        //Last updated
        lastUpdated = headerView.findViewById(R.id.car_lastUpdated);
        if (purchaseTimes.size() > 0) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(purchaseTimes.get(purchaseTimes.size() - 1));
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
        purchaseTimes.clear();
        purchaseKilometers.clear();
        purchasePrices.clear();
        purchaseUnitPrice.clear();
        purchaseUnitPrice2.clear();

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
                                    item.setFuelType(obj.getString("fuelType"));
                                    item.setFuelPrice(obj.getDouble("fuelPrice"));
                                    item.setFuelLiter(obj.getDouble("fuelLiter"));
                                    item.setFuelType2(obj.getString("fuelType2"));
                                    item.setFuelPrice2(obj.getDouble("fuelPrice2"));
                                    item.setFuelLiter2(obj.getDouble("fuelLiter2"));
                                    item.setTotalPrice(obj.getDouble("totalPrice"));
                                    item.setBillPhoto(obj.getString("billPhoto"));
                                    feedsList.add(item);

                                    purchaseTimes.add(i, obj.getString("time"));
                                    purchaseUnitPrice.add(i, obj.getDouble("fuelPrice"));
                                    purchaseUnitPrice2.add(i, obj.getDouble("fuelPrice2"));
                                    purchasePrices.add(i, obj.getDouble("totalPrice"));
                                    purchaseKilometers.add(i, obj.getInt("kilometer"));
                                    purchaseLiters.add(i, obj.getDouble("fuelLiter") + obj.getDouble("fuelLiter2"));

                                    mAdapter = new PurchaseAdapter(getActivity(), feedsList);
                                    mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                    mAdapter.notifyDataSetChanged();
                                    mRecyclerView.setAdapter(mAdapter);
                                    mRecyclerView.setLayoutManager(mLayoutManager);
                                    swipeContainer.setRefreshing(false);
                                }
                                //BURADA TARİHE GÖRE GEÇMİŞTEN BUGÜNE SIRALIYORUZ
                                Collections.reverse(purchaseTimes);
                                Collections.reverse(purchasePrices);
                                Collections.reverse(purchaseKilometers);

                                //Calculate avg fuel consumption and update
                                if (avgText != null) {
                                    String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " lt/100km";
                                    avgText.setText(avgDummy);
                                }

                                //update kilometer
                                if (kilometerText != null) {
                                    String kmHolder = kilometer + " " + "km";
                                    kilometerText.setText(kmHolder);
                                }

                                //update avg price
                                if (avgPrice != null) {
                                    String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " TL/100km";
                                    avgPrice.setText(avgPriceDummy);
                                }

                                if (purchaseTimes.size() > 0) {
                                    try {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                        Date date = format.parse(purchaseTimes.get(purchaseTimes.size() - 1));
                                        lastUpdated.setReferenceTime(date.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
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

    public float calculateAverageCons() {
        float totalLiter = 0;
        float kilometerDifference = 0;
        if (purchaseTimes.size() > 1) {
            for (int i = 0; i < purchaseTimes.size(); i++) {
                totalLiter += purchaseLiters.get(i);
                kilometerDifference = purchaseKilometers.get(purchaseKilometers.size() - 1) - purchaseKilometers.get(0);
            }
            averageCons = (totalLiter / kilometerDifference) * 100f;
            prefs.edit().putFloat("averageConsumption", averageCons).apply();
        } else {
            averageCons = 0;
        }
        return averageCons;
    }

    public float calculateAvgPrice() {
        float totalPrice = 0;
        float kilometerDifference = 0;
        if (purchaseTimes.size() > 1) {
            for (int i = 0; i < purchaseTimes.size(); i++) {
                totalPrice += purchasePrices.get(i);
                kilometerDifference = purchaseKilometers.get(purchaseKilometers.size() - 1) - purchaseKilometers.get(0);
            }
            averagePrice = (totalPrice / kilometerDifference) * 100f;
            prefs.edit().putFloat("averagePrice", averagePrice).apply();
        } else {
            averagePrice = 0;
        }
        return averagePrice;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRecyclerView != null) {
            fetchUserPurchases();
        }
    }
}
