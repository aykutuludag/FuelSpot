package com.fuelspot;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.averagePrice;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.universalTimeFormat;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.vehicleID;

public class FragmentAutomobile extends Fragment {

    public static List<PurchaseItem> vehiclePurchaseList = new ArrayList<>();

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
    private View headerView;
    private Button buttonSeeAllPurchases;
    private View view;
    private RelativeLayout regularLayout;
    private RelativeLayout noAracLayout;

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

            // Keep screen off
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Vehicle");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

            headerView = view.findViewById(R.id.header_vehicle);

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
        }

        return view;
    }

    private void loadVehicleProfile() {
        //ProfilePhoto
        CircleImageView carPhotoHolder = headerView.findViewById(R.id.carPicture);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_automobile)
                .error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
        Glide.with(getActivity()).load(carPhoto).apply(options).into(carPhotoHolder);

        kilometerText = headerView.findViewById(R.id.automobile_kilometer);
        String kmHolder = kilometer + " " + getString(R.string.kilometer);
        kilometerText.setText(kmHolder);

        TextView textViewPlaka = headerView.findViewById(R.id.automobile_plateNo);
        textViewPlaka.setText(plateNo);

        //Marka-model
        TextView fullname = headerView.findViewById(R.id.carFullname);
        String fullad = carBrand + " " + carModel;
        fullname.setText(fullad);

        //Yakıt tipi başlangıç
        TextView fuelType = headerView.findViewById(R.id.car_fuelTypeText);
        ImageView fuelTypeIndicator = headerView.findViewById(R.id.car_fuelType);
        String fuelText;

        switch (fuelPri) {
            case 0:
                fuelText = getString(R.string.gasoline);
                fuelTypeIndicator.setImageResource(R.drawable.gasoline);
                break;
            case 1:
                fuelText = getString(R.string.diesel);
                fuelTypeIndicator.setImageResource(R.drawable.diesel);
                break;
            case 2:
                fuelText = getString(R.string.lpg);
                fuelTypeIndicator.setImageResource(R.drawable.lpg);
                break;
            case 3:
                fuelText = getString(R.string.electricity);
                fuelTypeIndicator.setImageResource(R.drawable.electricity);
                break;
            default:
                fuelText = "";
                fuelTypeIndicator.setImageDrawable(null);
                break;
        }
        fuelType.setText(fuelText);

        TextView fuelType2 = headerView.findViewById(R.id.car_fuelTypeText2);
        ImageView fuelTypeIndicator2 = headerView.findViewById(R.id.car_fuelType2);
        String fuelText2;
        switch (MainActivity.fuelSec) {
            case 0:
                fuelText2 = getString(R.string.gasoline);
                fuelTypeIndicator2.setImageResource(R.drawable.gasoline);

                fuelType2.setVisibility(View.VISIBLE);
                fuelTypeIndicator2.setVisibility(View.VISIBLE);
                break;
            case 1:
                fuelText2 = getString(R.string.diesel);
                fuelTypeIndicator2.setImageResource(R.drawable.diesel);

                fuelType2.setVisibility(View.VISIBLE);
                fuelTypeIndicator2.setVisibility(View.VISIBLE);
                break;
            case 2:
                fuelText2 = getString(R.string.lpg);
                fuelTypeIndicator2.setImageResource(R.drawable.lpg);

                fuelType2.setVisibility(View.VISIBLE);
                fuelTypeIndicator2.setVisibility(View.VISIBLE);
                break;
            case 3:
                fuelText2 = getString(R.string.electricity);
                fuelTypeIndicator2.setImageResource(R.drawable.electricity);

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
        avgText = headerView.findViewById(R.id.automobile_consumption);
        String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " lt/100km";
        avgText.setText(avgDummy);

        //Ortalama maliyet
        avgPrice = headerView.findViewById(R.id.automobile_priceCons);
        String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " TL/100km";
        avgPrice.setText(avgPriceDummy);

        //Ortalama emisyon
        emission = headerView.findViewById(R.id.automobile_emission);
        String emissionHolder = calculateCarbonEmission() + " g/100km";
        emission.setText(emissionHolder);

        //Last updated
        lastUpdated = headerView.findViewById(R.id.car_lastUpdated);
        if (vehiclePurchaseList.size() > 0) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(vehiclePurchaseList.get(vehiclePurchaseList.size() - 1).getPurchaseTime());
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
                Intent intent = new Intent(getActivity(), AutomobileEditActivity.class);
                startActivity(intent);
            }
        });

        fetchVehiclePurchases();
    }

    private void fetchVehiclePurchases() {
        vehiclePurchaseList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_AUTOMOBILE_PURCHASES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            mRecyclerView.setVisibility(View.VISIBLE);
                            userNoPurchaseLayout.setVisibility(View.GONE);

                            List<PurchaseItem> dummyList = new ArrayList<>();
                            mAdapter = new PurchaseAdapter(getActivity(), dummyList);
                            mLayoutManager = new GridLayoutManager(getActivity(), 1);

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
                                        dummyList.add(item);
                                    } else {
                                        buttonSeeAllPurchases.setVisibility(View.VISIBLE);
                                    }
                                    mAdapter.notifyDataSetChanged();
                                }

                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);

                                //update kilometer
                                if (kilometerText != null) {
                                    String kmHolder = kilometer + " " + getString(R.string.kilometer);
                                    kilometerText.setText(kmHolder);
                                }

                                //Ortalama tüketim
                                if (avgText != null) {
                                    String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " lt" + getString(R.string.per_100km);
                                    avgText.setText(avgDummy);
                                }

                                // We're sync consumption values in here. Passive update mechanism
                                if (averageCons != calculateAvgPrice()) {
                                    updateVehicle();
                                }

                                //Ortalama maliyet
                                if (avgPrice != null) {
                                    String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " " + currencySymbol + getString(R.string.per_100km);
                                    avgPrice.setText(avgPriceDummy);
                                }

                                //Ortalama emisyon
                                if (emission != null) {
                                    String emissionHolder = calculateCarbonEmission() + " g" + getString(R.string.per_100km);
                                    emission.setText(emissionHolder);
                                }

                                try {
                                    SimpleDateFormat format = new SimpleDateFormat(universalTimeFormat, Locale.getDefault());
                                    Date date = format.parse(vehiclePurchaseList.get(vehiclePurchaseList.size() - 1).getPurchaseTime());
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
                        //Showing toast
                        Toast.makeText(getActivity(), volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("plateNo", plateNo);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
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
                case 3:
                    carbonEmission = 0;
                    break;
                default:
                    carbonEmission = 0;
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
                case 3:
                    carbonEmission += 0;
                    break;
                default:
                    carbonEmission += 0;
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
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("avgCons", String.valueOf(averageCons));
                params.put("carbonEmission", String.valueOf(carbonEmission));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (headerView != null) {
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
    }
}
