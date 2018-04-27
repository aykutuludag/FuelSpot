package org.uusoftware.fuelify;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static android.app.Activity.RESULT_OK;
import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.kilometer;
import static org.uusoftware.fuelify.MainActivity.username;

public class FragmentVehicle extends Fragment {

    public static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
    ImageView carPhotoHolder;
    SharedPreferences prefs;

    String FETCH_URL = "http://uusoftware.org/Fuelspot/api/fetch-purchases.php";

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

        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        //SETTING HEADER VEHICLE VARIABLES
        View headerView = view.findViewById(R.id.header_vehicle);

        carPhotoHolder = headerView.findViewById(R.id.car_picture);
        Picasso.with(getActivity()).load(Uri.parse(carPhoto)).error(R.drawable.empty).placeholder(R.drawable.empty)
                .into(carPhotoHolder);
        carPhotoHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyStoragePermissions()) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(getActivity());
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

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
                fuelText += ", Gasoline";
                break;
            case 1:
                fuelText = ", Diesel";
                break;
            case 2:
                fuelText = ", LPG";
                break;
            case 3:
                fuelText = ", Electric";
                break;
            default:
                fuelText = "-";
                break;
        }
        fuelType.setText(fuelText);
        //Yakıt tipi bitiş

        ImageView updateCar = headerView.findViewById(R.id.updateCarInfo);
        updateCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VehicleSelection.class);
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, FETCH_URL,
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

                                mAdapter = new PurchaseAdapter(getActivity(), feedsList);
                                mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                swipeContainer.setRefreshing(false);
                            }
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

    public boolean verifyStoragePermissions() {
        boolean hasStorage;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int permission = ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            hasStorage = permission == PackageManager.PERMISSION_GRANTED;
        } else {
            hasStorage = true;
        }
        return hasStorage;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Settings saved...", Toast.LENGTH_SHORT).show();
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(getActivity());
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                String fileURI = aq.get(0);
                if (!fileURI.contains("file://")) {
                    fileURI = "file://" + fileURI;
                }
                carPhoto = fileURI;
                prefs.edit().putString("CarPhoto", carPhoto).apply();
                Picasso.with(getActivity()).load(Uri.parse(fileURI)).error(R.drawable.empty).placeholder(R.drawable.empty)
                        .into(carPhotoHolder);
            }
        }
    }
}
