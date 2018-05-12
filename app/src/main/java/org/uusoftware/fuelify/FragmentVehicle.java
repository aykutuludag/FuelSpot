package org.uusoftware.fuelify;


import android.content.Intent;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uusoftware.fuelify.adapter.PurchaseAdapter;
import org.uusoftware.fuelify.model.PurchaseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.kilometer;
import static org.uusoftware.fuelify.MainActivity.purchaseKilometers;
import static org.uusoftware.fuelify.MainActivity.purchasePrices;
import static org.uusoftware.fuelify.MainActivity.purchaseTimes;
import static org.uusoftware.fuelify.MainActivity.username;

public class FragmentVehicle extends Fragment {

    CircleImageView carPhotoHolder;
    SwipeRefreshLayout swipeContainer;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<PurchaseItem> feedsList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("Vehicle Profile");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //SETTING HEADER VEHICLE VARIABLES
        View headerView = view.findViewById(R.id.header_vehicle);

        //ProfilePhoto
        carPhotoHolder = headerView.findViewById(R.id.car_picture);
        System.out.println(carPhoto);
        Picasso.with(getActivity()).load(Uri.parse(carPhoto)).error(R.drawable.empty).placeholder(R.drawable.empty)
                .into(carPhotoHolder);

        //Marka-model
        TextView fullname = headerView.findViewById(R.id.carFullname);
        String fullad = carBrand + " " + carModel;
        fullname.setText(fullad);

        //Kilometre
        TextView kilometerText = headerView.findViewById(R.id.car_kilometer);
        String kmHolder = kilometer + " " + "km";
        kilometerText.setText(kmHolder);

        //Yakıt tipi başlangıç
        TextView fuelType = headerView.findViewById(R.id.car_fuelType);
        String fuelText;
        switch (fuelPri) {
            case 0:
                fuelText = "Gasoline";
                break;
            case 1:
                fuelText = "Diesel";
                break;
            case 2:
                fuelText = "LPG";
                break;
            case 3:
                fuelText = "Electric";
                break;
            default:
                fuelText = "-";
                break;
        }

        switch (fuelSec) {
            case 0:
                fuelText = fuelText + ", Gasoline";
                break;
            case 1:
                fuelText = fuelText + ", Diesel";
                break;
            case 2:
                fuelText = fuelText + ", LPG";
                break;
            case 3:
                fuelText = fuelText + ", Electric";
                break;
        }
        fuelType.setText(fuelText);
        //Yakıt tipi bitiş

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

        fetchUserPurchases();

        return view;
    }

    private void fetchUserPurchases() {
        feedsList.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_USER_PURCHASE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            for (int i = 0; i < res.length(); i++) {
                                JSONObject obj = res.getJSONObject(i);

                                PurchaseItem item = new PurchaseItem();
                                item.setID(obj.getInt("id"));
                                item.setPurchaseTime(obj.getLong("time"));
                                item.setStationName(obj.getString("stationName"));
                                item.setFuelType(obj.getString("fuelType"));
                                item.setFuelPrice(obj.getDouble("fuelPrice"));
                                item.setFuelLiter(obj.getDouble("fuelLiter"));
                                item.setFuelType2(obj.getString("fuelType2"));
                                item.setFuelPrice2(obj.getDouble("fuelPrice2"));
                                item.setFuelLiter2(obj.getDouble("fuelLiter2"));
                                item.setTotalPrice(obj.getDouble("totalPrice"));
                                item.setBillPhoto(obj.getString("billPhoto"));
                                feedsList.add(item);

                                purchaseTimes.add(i, obj.getLong("time"));
                                purchasePrices.add(i, obj.getDouble("totalPrice"));
                                purchaseKilometers.add(i, obj.getInt("kilometer"));

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
                        } catch (JSONException e) {
                            e.printStackTrace();
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
}
