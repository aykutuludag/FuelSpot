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
import static com.fuelspot.MainActivity.vehicleID;

public class FragmentAutomobile extends Fragment {

    public static ArrayList<String> purchaseTimes = new ArrayList<>();
    public static ArrayList<Double> purchaseUnitPrice = new ArrayList<>();
    public static ArrayList<Double> purchaseUnitPrice2 = new ArrayList<>();
    public static ArrayList<Double> purchasePrices = new ArrayList<>();
    public static ArrayList<Integer> purchaseKilometers = new ArrayList<>();
    public static ArrayList<Double> purchaseLiters = new ArrayList<>();
    CircleImageView carPhotoHolder;

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<PurchaseItem> feedsList = new ArrayList<>();
    SharedPreferences prefs;
    ImageView fuelTypeIndicator, fuelTypeIndicator2;
    TextView kilometerText, fullname, fuelType, fuelType2, avgText, avgPrice, emission;
    RelativeTimeTextView lastUpdated;
    RelativeLayout userNoPurchaseLayout;
    RequestQueue requestQueue;
    View headerView;

    View view;

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

            // Analytics
            Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Vehicle");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

            headerView = view.findViewById(R.id.header_vehicle);

            userNoPurchaseLayout = view.findViewById(R.id.noPurchaseLayout);
            requestQueue = Volley.newRequestQueue(getActivity());

            mRecyclerView = view.findViewById(R.id.feedView);

            fetchVehiclePurchases();
            loadVehicleProfile();
        }

        return view;
    }

    public void loadVehicleProfile() {
        //ProfilePhoto
        carPhotoHolder = headerView.findViewById(R.id.carPicture);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_automobile)
                .error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(getActivity()).load(carPhoto).apply(options).into(carPhotoHolder);

        kilometerText = headerView.findViewById(R.id.car_kilometer);
        String kmHolder = kilometer + " km";
        kilometerText.setText(kmHolder);

        TextView textViewPlaka = headerView.findViewById(R.id.car_plateNo);
        textViewPlaka.setText(plateNo);

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
        avgText = headerView.findViewById(R.id.report_sID);
        String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " lt/100km";
        avgText.setText(avgDummy);

        //Ortalama maliyet
        avgPrice = headerView.findViewById(R.id.report_reason);
        String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " TL/100km";
        avgPrice.setText(avgPriceDummy);

        //Ortalama emisyon
        emission = headerView.findViewById(R.id.report_reward);
        String emissionHolder = calculateCarbonEmission() + " g/100km";
        emission.setText(emissionHolder);

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
                Intent intent = new Intent(getActivity(), AutomobileEditActivity.class);
                startActivity(intent);
            }
        });
    }

    public void fetchVehiclePurchases() {
        feedsList.clear();
        purchaseTimes.clear();
        purchaseKilometers.clear();
        purchasePrices.clear();
        purchaseUnitPrice.clear();
        purchaseUnitPrice2.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_PURCHASES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            userNoPurchaseLayout.setVisibility(View.GONE);
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
                                    item.setFuelTax((float) obj.getDouble("fuelTax"));
                                    item.setFuelType2(obj.getInt("fuelType2"));
                                    item.setFuelPrice2((float) obj.getDouble("fuelPrice2"));
                                    item.setFuelLiter2((float) obj.getDouble("fuelLiter2"));
                                    item.setFuelTax2((float) obj.getDouble("fuelTax2"));
                                    item.setTotalPrice((float) obj.getDouble("totalPrice"));
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
                                }

                                //update kilometer
                                if (kilometerText != null) {
                                    String kmHolder = kilometer + " " + "km";
                                    kilometerText.setText(kmHolder);
                                }

                                if (purchaseTimes.size() > 0) {
                                    //BURADA TARİHE GÖRE GEÇMİŞTEN BUGÜNE SIRALIYORUZ
                                    Collections.reverse(purchaseTimes);
                                    Collections.reverse(purchasePrices);
                                    Collections.reverse(purchaseKilometers);

                                    //Ortalama maliyet
                                    if (avgPrice != null) {
                                        String avgPriceDummy = String.format(Locale.getDefault(), "%.2f", calculateAvgPrice()) + " TL/100km";
                                        avgPrice.setText(avgPriceDummy);
                                    }

                                    //Ortalama tüketim
                                    if (avgText != null) {
                                        String avgDummy = String.format(Locale.getDefault(), "%.2f", calculateAverageCons()) + " lt/100km";
                                        avgText.setText(avgDummy);
                                    }

                                    //Ortalama emisyon
                                    if (emission != null) {
                                        String emissionHolder = calculateCarbonEmission() + " g/100km";
                                        emission.setText(emissionHolder);
                                    }

                                    try {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                        Date date = format.parse(purchaseTimes.get(purchaseTimes.size() - 1));
                                        lastUpdated.setReferenceTime(date.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    //BURAYA BİR KONTROL MEKANİZMASI KOY BİLGİLER DEĞİŞTİĞİNDE UPDATE ETSİN SADECE
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            userNoPurchaseLayout.setVisibility(View.VISIBLE);
                            //   Snackbar.make(getActivity().findViewById(R.id.mainContainer), "Henüz hiç satın alma yapmamışsınız.", Snackbar.LENGTH_LONG).show();
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
                params.put("vehicleID", String.valueOf(vehicleID));

                //returning parameters
                return params;
            }
        };

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

    public int calculateCarbonEmission() {
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
                    carbonEmission = 0;
                    break;
                default:
                    carbonEmission = 0;
                    break;
            }

            carbonEmission = carbonEmission / 2;
            prefs.edit().putInt("carbonEmission", carbonEmission).apply();
        }
        return carbonEmission;
    }

   /* private void updateCarInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_AUTOMOBILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

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

                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("carBrand", carBrand);
                params.put("carModel", carModel);
                params.put("fuelPri", String.valueOf(fuelPri));
                params.put("fuelSec", String.valueOf(fuelSec));
                params.put("km", String.valueOf(kilometer));
                params.put("plate", plateNo);
                params.put("avgCons", String.valueOf(averageCons));
                params.put("carbonEmission", String.valueOf(carbonEmission));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
    */
}
