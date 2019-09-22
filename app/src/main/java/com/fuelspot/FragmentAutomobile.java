package com.fuelspot;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.averagePrice;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.vehicleID;

public class FragmentAutomobile extends Fragment {

    public static List<PurchaseItem> vehiclePurchaseList = new ArrayList<>();
    public static List<PurchaseItem> dummyPurchaseList = new ArrayList<>();
    TextView textViewPlaka;
    Button addAutomobileButton;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private SharedPreferences prefs;
    private TextView kilometerText;
    private TextView avgText;
    private TextView avgPrice;
    private TextView emission;
    private RelativeTimeTextView lastUpdated;
    private RelativeLayout userNoPurchaseLayout;
    private RequestQueue requestQueue;
    private Button buttonSeeAllPurchases;
    public View view;
    private RelativeLayout regularLayout;
    private RelativeLayout noAracLayout;
    private SwipeRefreshLayout swipeContainer;

    public static FragmentAutomobile newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Automobile");

        FragmentAutomobile fragment = new FragmentAutomobile();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_automobile, container, false);
            setHasOptionsMenu(true);

            // Keep screen off
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Otomobil");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

            regularLayout = view.findViewById(R.id.userRegularLayout);
            noAracLayout = view.findViewById(R.id.userNoCarLayout);

            userNoPurchaseLayout = view.findViewById(R.id.noPurchaseLayout);
            requestQueue = Volley.newRequestQueue(getActivity());

            mRecyclerView = view.findViewById(R.id.purchaseView);

            buttonSeeAllPurchases = view.findViewById(R.id.button_seeAllPurchases);
            buttonSeeAllPurchases.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserPurchases.class);
                    startActivity(intent);
                }
            });

            swipeContainer = view.findViewById(R.id.swipeContainer);
            // Setup refresh listener which triggers new data loading
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (userAutomobileList != null && userAutomobileList.size() > 0) {
                        noAracLayout.setVisibility(View.GONE);
                        regularLayout.setVisibility(View.VISIBLE);
                        loadVehicleProfile();
                    } else {
                        noAracLayout.setVisibility(View.VISIBLE);
                        noAracLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), AddAutomobile.class);
                                startActivity(intent);
                            }
                        });
                        regularLayout.setVisibility(View.GONE);
                    }
                }
            });
            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            if (userAutomobileList != null && userAutomobileList.size() > 0) {
                noAracLayout.setVisibility(View.GONE);
                regularLayout.setVisibility(View.VISIBLE);

                loadVehicleProfile();
            } else {
                noAracLayout.setVisibility(View.VISIBLE);
                regularLayout.setVisibility(View.GONE);

                addAutomobileButton = view.findViewById(R.id.buttonAddCar);
                addAutomobileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), AddAutomobile.class);
                        startActivity(intent);
                    }
                });
            }
        }
        return view;
    }

    private void loadVehicleProfile() {
        //ProfilePhoto
        CircleImageView carPhotoHolder = view.findViewById(R.id.carPicture);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_automobile)
                .error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
        Glide.with(getActivity()).load(carPhoto).apply(options).into(carPhotoHolder);

        kilometerText = view.findViewById(R.id.automobile_kilometer);
        String kmHolder = kilometer + " KM";
        kilometerText.setText(kmHolder);

        textViewPlaka = view.findViewById(R.id.automobile_plateNo);
        textViewPlaka.setText(plateNo);

        //Marka-model
        TextView fullname = view.findViewById(R.id.carFullname);
        String fullad = carBrand + " " + carModel;
        fullname.setText(fullad);

        //Yakıt tipi başlangıç
        TextView fuelType = view.findViewById(R.id.car_fuelTypeText);
        ImageView fuelTypeIndicator = view.findViewById(R.id.car_fuelType);
        String fuelText;

        switch (fuelPri) {
            case 0:
                fuelText = getString(R.string.gasoline);
                fuelTypeIndicator.setImageResource(R.drawable.fuel_gasoline);
                break;
            case 1:
                fuelText = getString(R.string.diesel);
                fuelTypeIndicator.setImageResource(R.drawable.fuel_diesel);
                break;
            case 2:
                fuelText = getString(R.string.lpg);
                fuelTypeIndicator.setImageResource(R.drawable.fuel_lpg);
                break;
            case 3:
                fuelText = getString(R.string.electricity);
                fuelTypeIndicator.setImageResource(R.drawable.fuel_electricity);
                break;
            default:
                fuelText = "";
                fuelTypeIndicator.setImageDrawable(null);
                break;
        }
        fuelType.setText(fuelText);

        TextView fuelType2 = view.findViewById(R.id.car_fuelTypeText2);
        ImageView fuelTypeIndicator2 = view.findViewById(R.id.car_fuelType2);
        String fuelText2;
        switch (MainActivity.fuelSec) {
            case 0:
                fuelText2 = getString(R.string.gasoline);
                fuelTypeIndicator2.setImageResource(R.drawable.fuel_gasoline);

                fuelType2.setVisibility(View.VISIBLE);
                fuelTypeIndicator2.setVisibility(View.VISIBLE);
                break;
            case 1:
                fuelText2 = getString(R.string.diesel);
                fuelTypeIndicator2.setImageResource(R.drawable.fuel_diesel);

                fuelType2.setVisibility(View.VISIBLE);
                fuelTypeIndicator2.setVisibility(View.VISIBLE);
                break;
            case 2:
                fuelText2 = getString(R.string.lpg);
                fuelTypeIndicator2.setImageResource(R.drawable.fuel_lpg);

                fuelType2.setVisibility(View.VISIBLE);
                fuelTypeIndicator2.setVisibility(View.VISIBLE);
                break;
            case 3:
                fuelText2 = getString(R.string.electricity);
                fuelTypeIndicator2.setImageResource(R.drawable.fuel_electricity);

                fuelType2.setVisibility(View.VISIBLE);
                fuelTypeIndicator2.setVisibility(View.VISIBLE);
                break;
            default:
                fuelText2 = "";
                fuelTypeIndicator2.setImageDrawable(null);

                fuelType2.setVisibility(View.GONE);
                fuelTypeIndicator2.setVisibility(View.GONE);
        }
        fuelType2.setText(fuelText2);
        //Yakıt tipi bitiş

        //Ortalama tüketim
        avgText = view.findViewById(R.id.automobile_consumption);
        String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " LT/100km";
        avgText.setText(avgDummy);

        //Ortalama maliyet
        avgPrice = view.findViewById(R.id.automobile_priceCons);
        String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " TL/100km";
        avgPrice.setText(avgPriceDummy);

        //Ortalama emisyon
        emission = view.findViewById(R.id.automobile_emission);
        String emissionHolder = calculateCarbonEmission() + " GR/100km";
        emission.setText(emissionHolder);

        //Last updated
        lastUpdated = view.findViewById(R.id.car_lastUpdated);
        if (vehiclePurchaseList.size() > 0) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
                Date date = format.parse(vehiclePurchaseList.get(0).getPurchaseTime());
                lastUpdated.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //EditProfile
        ImageView updateCar = view.findViewById(R.id.updateCarInfo);
        updateCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AutomobileEditActivity.class);
                startActivity(intent);
            }
        });

        fetchVehiclePurchases();
    }

    private void fetchVehiclePurchases() {
        dummyPurchaseList.clear();
        vehiclePurchaseList.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_AUTOMOBILE_PURCHASES) + "?plateNo=" + plateNo,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        swipeContainer.setRefreshing(false);
                        if (response != null && response.length() > 0) {
                            try {
                                mRecyclerView.setVisibility(View.VISIBLE);
                                userNoPurchaseLayout.setVisibility(View.GONE);

                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    PurchaseItem item = new PurchaseItem();
                                    item.setID(obj.getInt("id"));
                                    item.setPurchaseTime(obj.getString("time"));
                                    item.setStationID(obj.getInt("stationID"));
                                    item.setStationName(obj.getString("stationName"));
                                    item.setStationIcon(obj.getString("stationIcon"));
                                    item.setStationLocation(obj.getString("stationLocation"));
                                    item.setPlateNo(obj.getString("plateNo"));
                                    item.setFuelType(obj.getInt("fuelType"));
                                    item.setFuelPrice((float) obj.getDouble("fuelPrice"));
                                    item.setFuelLiter((float) obj.getDouble("fuelLiter"));
                                    item.setFuelTax((float) obj.getDouble("fuelTax"));
                                    item.setSubTotal((float) obj.getDouble("subTotal"));
                                    item.setFuelType2(obj.getInt("fuelType2"));
                                    item.setFuelPrice2((float) obj.getDouble("fuelPrice2"));
                                    item.setFuelLiter2((float) obj.getDouble("fuelLiter2"));
                                    item.setFuelTax2((float) obj.getDouble("fuelTax2"));
                                    item.setSubTotal2((float) obj.getDouble("subTotal2"));
                                    item.setBonus((float) obj.getDouble("bonus"));
                                    item.setTotalPrice((float) obj.getDouble("totalPrice"));
                                    item.setBillPhoto(obj.getString("billPhoto"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setKilometer(obj.getInt("kilometer"));
                                    vehiclePurchaseList.add(item);

                                    if (i < 3) {
                                        dummyPurchaseList.add(item);
                                        buttonSeeAllPurchases.setVisibility(View.GONE);
                                    } else {
                                        buttonSeeAllPurchases.setVisibility(View.VISIBLE);
                                    }
                                }

                                mAdapter = new PurchaseAdapter(getActivity(), dummyPurchaseList);
                                mLayoutManager = new GridLayoutManager(getActivity(), 1);
                                mRecyclerView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setLayoutManager(mLayoutManager);

                                //update kilometer
                                if (kilometerText != null) {
                                    String kmHolder = kilometer + " KM";
                                    kilometerText.setText(kmHolder);
                                }

                                //Ortalama tüketim
                                if (avgText != null) {
                                    String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " LT/100km";
                                    avgText.setText(avgDummy);
                                }

                                // We're sync consumption values in here. Passive update mechanism
                                if (averageCons != 0) {
                                    updateVehicle();
                                }

                                //Ortalama maliyet
                                if (avgPrice != null) {
                                    String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " TL/100km";
                                    avgPrice.setText(avgPriceDummy);
                                }

                                //Ortalama emisyon
                                if (emission != null) {
                                    String emissionHolder = calculateCarbonEmission() + " GR/100km";
                                    emission.setText(emissionHolder);
                                }
                                try {
                                    SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
                                    Date date = format.parse(vehiclePurchaseList.get(0).getPurchaseTime());
                                    lastUpdated.setReferenceTime(date.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } catch (JSONException e) {
                                userNoPurchaseLayout.setVisibility(View.VISIBLE);
                                buttonSeeAllPurchases.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.GONE);
                                e.printStackTrace();
                            }
                        } else {
                            userNoPurchaseLayout.setVisibility(View.VISIBLE);
                            buttonSeeAllPurchases.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        swipeContainer.setRefreshing(false);
                        Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
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

    private float calculateAverageCons() {
        float totalLiter = 0;
        float kilometerDifference = 0;
        if (vehiclePurchaseList.size() > 1) {
            for (int i = 0; i < vehiclePurchaseList.size(); i++) {
                totalLiter += vehiclePurchaseList.get(i).getFuelLiter() + vehiclePurchaseList.get(i).getFuelLiter2();
                kilometerDifference = vehiclePurchaseList.get(0).getKilometer() - vehiclePurchaseList.get(vehiclePurchaseList.size() - 1).getKilometer();
            }
            averageCons = (totalLiter / kilometerDifference) * 100f;
            prefs.edit().putFloat("averageConsumption", averageCons).apply();
        } else {
            averageCons = 0;
        }
        return averageCons;
    }

    private float calculateAvgPrice() {
        float totalPrice = 0;
        float kilometerDifference = 0;
        if (vehiclePurchaseList.size() > 1) {
            for (int i = 0; i < vehiclePurchaseList.size(); i++) {
                totalPrice += vehiclePurchaseList.get(i).getTotalPrice();
                kilometerDifference = vehiclePurchaseList.get(0).getKilometer() - vehiclePurchaseList.get(vehiclePurchaseList.size() - 1).getKilometer();
            }
            averagePrice = (totalPrice / kilometerDifference) * 100f;
            prefs.edit().putFloat("averagePrice", averagePrice).apply();
        } else {
            averagePrice = 0;
        }
        return averagePrice;
    }

    private int calculateCarbonEmission() {
        int emissionGasoline = 2392;
        int emissionDiesel = 2640;
        int emissionlpg = 1665;

        if (fuelSec == -1) {
            switch (fuelPri) {
                case 0:
                    carbonEmission = (int) (emissionGasoline * averageCons);
                    break;
                case 1:
                    carbonEmission = (int) (emissionDiesel * averageCons);
                    break;
                case 2:
                    carbonEmission = (int) (emissionlpg * averageCons);
                    break;
                case 3:
                    carbonEmission = 0;
                    break;
                default:
                    carbonEmission = 0;
                    break;
            }
            prefs.edit().putInt("carbonEmission", carbonEmission).apply();
        } else {
            switch (fuelPri) {
                case 0:
                    carbonEmission = (int) (emissionGasoline * averageCons);
                    break;
                case 1:
                    carbonEmission = (int) (emissionDiesel * averageCons);
                    break;
                case 2:
                    carbonEmission = (int) (emissionlpg * averageCons);
                    break;
            }
            switch (fuelSec) {
                case 0:
                    carbonEmission += (int) (emissionGasoline * averageCons);
                    break;
                case 1:
                    carbonEmission += (int) (emissionDiesel * averageCons);
                    break;
                case 2:
                    carbonEmission += (int) (emissionlpg * averageCons);
                    break;
            }

            carbonEmission = carbonEmission / 2;
            prefs.edit().putInt("carbonEmission", carbonEmission).apply();
        }

        return carbonEmission;
    }

    private void updateVehicle() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_AUTOMOBILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do nothing
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("avgCons", String.valueOf(averageCons));
                params.put("carbonEmission", String.valueOf(carbonEmission));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_automobile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_fuel) {
            Intent intent = new Intent(getActivity(), AddManuelFuel.class);
            showAds(getActivity(), intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (textViewPlaka != null) {
            if (!plateNo.equals(textViewPlaka.getText().toString())) {
                loadVehicleProfile();
            }
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
