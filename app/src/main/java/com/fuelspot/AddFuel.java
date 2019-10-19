package com.fuelspot;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fuelspot.adapter.VehicleAdapter;
import com.fuelspot.model.VehicleItem;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.AdMob;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.TAX_DIESEL;
import static com.fuelspot.MainActivity.TAX_ELECTRICITY;
import static com.fuelspot.MainActivity.TAX_GASOLINE;
import static com.fuelspot.MainActivity.TAX_LPG;
import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.getStringImage;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.resizeAndRotate;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.vehicleID;

public class AddFuel extends AppCompatActivity {

    ProgressDialog stationFetching;
    Button buttonAddBill;
    GridLayoutManager mLayoutManager;
    private Window window;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;
    private RequestOptions options;
    private int chosenStationID, tempKM;
    private String stationName, stationAddress, stationLoc, stationLogo;
    private float gasolinePrice, dieselPrice, LPGPrice, electricityPrice, selectedUnitPrice, buyedLiter, entryPrice, selectedTaxRate, selectedUnitPrice2, buyedLiter2, entryPrice2, selectedTaxRate2, totalPrice;
    /* LAYOUT 1 ÖĞELER */
    private CircleImageView istasyonLogoHolder;
    private RelativeLayout expandableLayoutYakit, expandableLayoutYakit2;
    private String fuelType, fuelType2;
    private ImageView fuelType1Icon, fuelType2Icon, photoHolder;
    private EditText enterKilometer, textViewLitreFiyati, textViewTotalFiyat, textViewLitreFiyati2, textViewTotalFiyat2;
    private Bitmap bitmap;
    private ScrollView scrollView;
    private RecyclerView mRecyclerView;
    private TextView istasyonNameHolder, istasyonAdresHolder, istasyonIDHolder, fuelType1Text, fuelType2Text, fuelGrandTotal, textViewLitre, textViewLitre2, textViewBonus;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fuel);

        //Window
        window = this.getWindow();

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);

        if (!premium) {
            AdMob(this);
        }

        //Creating a Request Queue
        requestQueue = Volley.newRequestQueue(AddFuel.this);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.icon_upload).error(R.drawable.icon_upload)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        // ProgressDialogs
        stationFetching = new ProgressDialog(AddFuel.this);
        stationFetching.setTitle(getString(R.string.station_info_loading));
        stationFetching.setMessage(getString(R.string.please_wait));
        stationFetching.setIndeterminate(true);
        stationFetching.setCancelable(false);
        stationFetching.show();

        scrollView = findViewById(R.id.addfuel_layout1);

        istasyonIDHolder = findViewById(R.id.stationIDHolder);
        istasyonLogoHolder = findViewById(R.id.stationIconHolder);
        istasyonNameHolder = findViewById(R.id.stationNameHolder);
        istasyonAdresHolder = findViewById(R.id.stationAddressHolder);

        expandableLayoutYakit = findViewById(R.id.division2);
        expandableLayoutYakit2 = findViewById(R.id.division3);

        enterKilometer = findViewById(R.id.editTextKilometer);

        TextView textViewUnitPrice = findViewById(R.id.textViewUnit);
        TextView textViewUnitPrice2 = findViewById(R.id.textViewUnit2);
        textViewUnitPrice.setText(currencySymbol + " / " + userUnit);
        textViewUnitPrice2.setText(currencySymbol + " / " + userUnit);

        TextView textViewCurrency = findViewById(R.id.textViewCurrency);
        TextView textViewCurrency2 = findViewById(R.id.textViewCurrency2);
        textViewCurrency.setText(currencySymbol);
        textViewCurrency2.setText(currencySymbol);

        TextView textViewUnit = findViewById(R.id.textViewLiter);
        TextView textViewUnit2 = findViewById(R.id.textViewLiter2);
        textViewUnit.setText(userUnit);
        textViewUnit2.setText(userUnit);

        fuelType1Icon = findViewById(R.id.fuelType1);
        fuelType1Text = findViewById(R.id.fuelType1Text);
        textViewLitreFiyati = findViewById(R.id.editTextPricePerLiter);
        textViewTotalFiyat = findViewById(R.id.editTextPrice);
        textViewLitre = findViewById(R.id.textViewHowManyLitre);

        fuelType2Icon = findViewById(R.id.fuelType2);
        fuelType2Text = findViewById(R.id.fuelType2Text);
        textViewLitreFiyati2 = findViewById(R.id.editTextPricePerLiter2);
        textViewLitre2 = findViewById(R.id.textViewHowManyLitre2);
        textViewTotalFiyat2 = findViewById(R.id.editTextPrice2);
        textViewBonus = findViewById(R.id.textViewBonus);
        fuelGrandTotal = findViewById(R.id.textViewGrandTotal);

        enterKilometer.setEnabled(false);
        textViewLitreFiyati.setEnabled(false);
        textViewTotalFiyat.setEnabled(false);
        textViewLitre.setEnabled(false);
        textViewLitreFiyati2.setEnabled(false);
        textViewTotalFiyat2.setEnabled(false);
        textViewLitre2.setEnabled(false);

        chosenStationID = getIntent().getIntExtra("STATION_ID", 0);
        if (chosenStationID != 0) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setLogo(R.drawable.brand_logo);

            fetchSingleStation();
        } else {
            // Check whether user is at station or not
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            searchStations();
        }

        // Get user automobiles
        mRecyclerView = findViewById(R.id.automobileView);
        mRecyclerView.setNestedScrollingEnabled(false);
        mLayoutManager = new GridLayoutManager(AddFuel.this, 1);

        if (userAutomobileList != null && userAutomobileList.size() > 0) {
            mAdapter = new VehicleAdapter(AddFuel.this, userAutomobileList);
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            fetchAutomobiles();
        }
    }

    private void searchStations() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_SEARCH_STATIONS) + "?location=" + userlat + ";" + userlon + "&radius=" + 50,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        stationFetching.dismiss();
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                chosenStationID = obj.getInt("id");
                                stationName = obj.getString("name");
                                stationAddress = obj.getString("vicinity");
                                stationLoc = obj.getString("location");
                                stationLogo = obj.getString("logoURL");
                                gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                dieselPrice = (float) obj.getDouble("dieselPrice");
                                LPGPrice = (float) obj.getDouble("lpgPrice");
                                electricityPrice = (float) obj.getDouble("electricityPrice");

                                scrollView.setAlpha(1.0f);
                                loadLayout();
                            } catch (JSONException e) {
                                Toast.makeText(AddFuel.this, e.toString(), Toast.LENGTH_LONG).show();
                                stationName = "";
                                stationAddress = "";
                                stationLoc = "";
                                stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                                gasolinePrice = 0.00f;
                                dieselPrice = 0.00f;
                                LPGPrice = 0.00f;
                                electricityPrice = 0.00f;
                            }
                        } else {
                            Toast.makeText(AddFuel.this, "Şu an istasyonda değilsiniz! Yakıt ekleyebilmek için istasyonda olmanız gerekiyor.", Toast.LENGTH_LONG).show();
                            stationName = "";
                            stationAddress = "";
                            stationLoc = "";
                            stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                            gasolinePrice = 0.00f;
                            dieselPrice = 0.00f;
                            LPGPrice = 0.00f;
                            electricityPrice = 0.00f;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(AddFuel.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        stationName = "";
                        stationAddress = "";
                        stationLoc = "";
                        stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                        gasolinePrice = 0.00f;
                        dieselPrice = 0.00f;
                        LPGPrice = 0.00f;
                        electricityPrice = 0.00f;
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

    private void fetchSingleStation() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_STATION) + "?stationID=" + chosenStationID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        stationFetching.dismiss();
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                stationName = obj.getString("name");
                                stationAddress = obj.getString("vicinity");
                                stationLoc = obj.getString("location");
                                stationLogo = obj.getString("logoURL");
                                gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                dieselPrice = (float) obj.getDouble("dieselPrice");
                                LPGPrice = (float) obj.getDouble("lpgPrice");
                                electricityPrice = (float) obj.getDouble("electricityPrice");

                                scrollView.setAlpha(1.0f);
                                loadLayout();
                            } catch (JSONException e) {
                                Toast.makeText(AddFuel.this, e.toString(), Toast.LENGTH_LONG).show();
                                stationName = "";
                                stationAddress = "";
                                stationLoc = "";
                                stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                                gasolinePrice = 0.00f;
                                dieselPrice = 0.00f;
                                LPGPrice = 0.00f;
                                electricityPrice = 0.00f;
                            }
                        } else {
                            Toast.makeText(AddFuel.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                            stationName = "";
                            stationAddress = "";
                            stationLoc = "";
                            stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                            gasolinePrice = 0.00f;
                            dieselPrice = 0.00f;
                            LPGPrice = 0.00f;
                            electricityPrice = 0.00f;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        stationFetching.dismiss();
                        Toast.makeText(AddFuel.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                        stationName = "";
                        stationAddress = "";
                        stationLoc = "";
                        stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                        gasolinePrice = 0.00f;
                        dieselPrice = 0.00f;
                        LPGPrice = 0.00f;
                        electricityPrice = 0.00f;
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

    private void fetchAutomobiles() {
        userAutomobileList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_USER_AUTOMOBILES) + "?username=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    VehicleItem item = new VehicleItem();
                                    item.setID(obj.getInt("id"));
                                    item.setVehicleBrand(obj.getString("car_brand"));
                                    item.setVehicleModel(obj.getString("car_model"));
                                    item.setVehicleFuelPri(obj.getInt("fuelPri"));
                                    item.setVehicleFuelSec(obj.getInt("fuelSec"));
                                    item.setVehicleKilometer(obj.getInt("kilometer"));
                                    item.setVehiclePhoto(obj.getString("carPhoto"));
                                    item.setVehiclePlateNo(obj.getString("plateNo"));
                                    item.setVehicleConsumption((float) obj.getDouble("avgConsumption"));
                                    item.setVehicleEmission(obj.getInt("carbonEmission"));
                                    userAutomobileList.add(item);

                                    // If there is any selected auto, choose first one.
                                    if (vehicleID == 0) {
                                        chooseVehicle(item);
                                    }
                                }

                                mAdapter = new VehicleAdapter(AddFuel.this, userAutomobileList);
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                mRecyclerView.setAdapter(mAdapter);
                            } catch (JSONException e) {
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void chooseVehicle(VehicleItem item) {
        vehicleID = item.getID();
        prefs.edit().putInt("vehicleID", vehicleID).apply();

        carBrand = item.getVehicleBrand();
        prefs.edit().putString("carBrand", carBrand).apply();

        carModel = item.getVehicleModel();
        prefs.edit().putString("carModel", carModel).apply();

        fuelPri = item.getVehicleFuelPri();
        prefs.edit().putInt("FuelPrimary", fuelPri).apply();

        fuelSec = item.getVehicleFuelSec();
        prefs.edit().putInt("FuelSecondary", fuelSec).apply();

        kilometer = item.getVehicleKilometer();
        prefs.edit().putInt("Kilometer", kilometer).apply();

        carPhoto = item.getVehiclePhoto();
        prefs.edit().putString("CarPhoto", carPhoto).apply();

        plateNo = item.getVehiclePlateNo();
        prefs.edit().putString("plateNo", plateNo).apply();

        averageCons = item.getVehicleConsumption();
        prefs.edit().putFloat("averageConsumption", averageCons).apply();

        carbonEmission = item.getVehicleEmission();
        prefs.edit().putInt("carbonEmission", carbonEmission).apply();
    }

    public void loadLayout() {
        enterKilometer.setEnabled(true);
        textViewLitreFiyati.setEnabled(true);
        textViewTotalFiyat.setEnabled(true);
        textViewLitre.setEnabled(true);
        textViewLitreFiyati2.setEnabled(true);
        textViewTotalFiyat2.setEnabled(true);
        textViewLitre2.setEnabled(true);

        istasyonIDHolder.setText("" + chosenStationID);
        istasyonNameHolder.setText(stationName);
        istasyonAdresHolder.setText(stationAddress);

        Glide.with(AddFuel.this).load(stationLogo).apply(options).into(istasyonLogoHolder);

        updateVehicleLayout();

        //1. YAKIT TİPİ
        switch (fuelPri) {
            case 0:
                selectedUnitPrice = gasolinePrice;
                selectedTaxRate = TAX_GASOLINE;
                fuelType = getString(R.string.gasoline);
                Glide.with(AddFuel.this).load(R.drawable.fuel_gasoline).apply(options).into(fuelType1Icon);

                expandableLayoutYakit.setVisibility(View.VISIBLE);
                break;
            case 1:
                selectedUnitPrice = dieselPrice;
                selectedTaxRate = TAX_DIESEL;
                fuelType = getString(R.string.diesel);
                Glide.with(AddFuel.this).load(R.drawable.fuel_diesel).apply(options).into(fuelType1Icon);

                expandableLayoutYakit.setVisibility(View.VISIBLE);
                break;
            case 2:
                selectedUnitPrice = LPGPrice;
                selectedTaxRate = TAX_LPG;
                fuelType = getString(R.string.lpg);
                Glide.with(AddFuel.this).load(R.drawable.fuel_lpg).apply(options).into(fuelType1Icon);

                expandableLayoutYakit.setVisibility(View.VISIBLE);
                break;
            case 3:
                selectedUnitPrice = electricityPrice;
                selectedTaxRate = TAX_ELECTRICITY;
                fuelType = getString(R.string.electricity);
                Glide.with(AddFuel.this).load(R.drawable.fuel_electricity).apply(options).into(fuelType1Icon);

                expandableLayoutYakit.setVisibility(View.VISIBLE);
                break;
            default:
                expandableLayoutYakit.setVisibility(View.GONE);
                break;
        }

        fuelType1Text.setText(fuelType);
        textViewLitreFiyati.setText(String.valueOf(selectedUnitPrice));

        //2. YAKIT TİPİ
        switch (fuelSec) {
            case 0:
                selectedUnitPrice2 = gasolinePrice;
                selectedTaxRate2 = TAX_GASOLINE;
                fuelType2 = getString(R.string.gasoline);
                Glide.with(AddFuel.this).load(R.drawable.fuel_gasoline).apply(options).into(fuelType2Icon);

                expandableLayoutYakit2.setVisibility(View.VISIBLE);
                break;
            case 1:
                selectedUnitPrice2 = dieselPrice;
                selectedTaxRate2 = TAX_DIESEL;
                fuelType2 = getString(R.string.diesel);
                Glide.with(AddFuel.this).load(R.drawable.fuel_diesel).apply(options).into(fuelType2Icon);

                expandableLayoutYakit2.setVisibility(View.VISIBLE);
                break;
            case 2:
                selectedUnitPrice2 = LPGPrice;
                selectedTaxRate2 = TAX_LPG;
                fuelType2 = getString(R.string.lpg);
                Glide.with(AddFuel.this).load(R.drawable.fuel_lpg).apply(options).into(fuelType2Icon);

                expandableLayoutYakit2.setVisibility(View.VISIBLE);
                break;
            case 3:
                selectedUnitPrice2 = electricityPrice;
                selectedTaxRate2 = TAX_ELECTRICITY;
                fuelType2 = getString(R.string.electricity);
                Glide.with(AddFuel.this).load(R.drawable.fuel_electricity).apply(options).into(fuelType2Icon);

                expandableLayoutYakit2.setVisibility(View.VISIBLE);
                break;
            default:
                expandableLayoutYakit2.setVisibility(View.GONE);
                break;
        }

        fuelType2Text.setText(fuelType2);
        textViewLitreFiyati2.setText(String.valueOf(selectedUnitPrice2));

        textViewLitreFiyati.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    selectedUnitPrice = Float.parseFloat(s.toString());
                    buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
                    String literText = String.format(Locale.getDefault(), "%.2f", buyedLiter);
                    textViewLitre.setText(literText);
                }
            }
        });

        textViewTotalFiyat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    entryPrice = Float.parseFloat(s.toString());
                    buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
                    String literText = String.format(Locale.getDefault(), "%.2f", buyedLiter);
                    textViewLitre.setText(literText);
                    totalPrice = entryPrice + entryPrice2;
                    updateTaxandGrandTotal();
                }
            }
        });

        textViewLitreFiyati2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    selectedUnitPrice2 = Float.parseFloat(s.toString());
                    buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
                    String literText2 = String.format(Locale.getDefault(), "%.2f", buyedLiter2);
                    textViewLitre2.setText(literText2);
                }
            }
        });

        textViewTotalFiyat2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    entryPrice2 = Float.parseFloat(s.toString());
                    buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
                    String literText2 = String.format(Locale.getDefault(), "%.2f", buyedLiter2);
                    textViewLitre2.setText(literText2);
                    totalPrice = entryPrice + entryPrice2;
                    updateTaxandGrandTotal();
                }
            }
        });

        photoHolder = findViewById(R.id.photoHolder);

        buttonAddBill = findViewById(R.id.button_add_bill);
        buttonAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyFilePickerPermission(AddFuel.this)) {
                    ImagePicker.cameraOnly().start(AddFuel.this);
                } else {
                    ActivityCompat.requestPermissions(AddFuel.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        Button sendVariables = findViewById(R.id.sendButton);
        sendVariables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected(AddFuel.this)) {
                    if (totalPrice > 0) {
                        if (tempKM != 0) {
                            if (tempKM > kilometer) {
                                if (selectedUnitPrice != 0 || selectedUnitPrice2 != 0) {
                                    addPurchase();
                                } else {
                                    Toast.makeText(AddFuel.this, "Lütfen yakıtın birim fiyatını giriniz.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(AddFuel.this, getString(R.string.low_kilometer), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AddFuel.this, getString(R.string.empty_kilometer), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AddFuel.this, getString(R.string.empty_total_price), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AddFuel.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateVehicleLayout() {
        tempKM = kilometer;
        enterKilometer.setText(String.valueOf(tempKM));
        enterKilometer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    tempKM = Integer.parseInt(s.toString());
                }
            }
        });
    }

    private float howManyLiter(float priceForUnit, float totalPrice) {
        if (priceForUnit == 0) {
            return 0f;
        } else {
            return totalPrice / priceForUnit;
        }
    }

    private void addPurchase() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(AddFuel.this, getString(R.string.adding_fuel), getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_PURCHASE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                Toast.makeText(AddFuel.this, getString(R.string.fuel_add_success), Toast.LENGTH_LONG).show();
                                kilometer = tempKM;
                                prefs.edit().putInt("Kilometer", kilometer).apply();


                                showAds(AddFuel.this, null);

                                AlertDialog alertDialog = new AlertDialog.Builder(AddFuel.this).create();
                                alertDialog.setTitle("Satınalma eklendi");
                                alertDialog.setMessage("Satınalmanız başarıyla eklendi! Otomobil sekmesinde satınalmanızı görebilirsiniz. Fiş fotoğrafı eklediyseniz veya 24 saat içerisinde eklerseniz bonus için değerlendirmeye alınacaktır.");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Tamam",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                bitmap = null;
                                                stationName = "";
                                                stationAddress = "";
                                                chosenStationID = 0;
                                                tempKM = 0;
                                                stationLoc = "";
                                                stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                                                gasolinePrice = 0.00f;
                                                dieselPrice = 0.00f;
                                                electricityPrice = 0.00f;
                                                LPGPrice = 0.00f;
                                                dialog.dismiss();
                                                finish();
                                            }
                                        });
                                alertDialog.setCancelable(false);
                                alertDialog.show();
                            } else {
                                Toast.makeText(AddFuel.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AddFuel.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }

                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        volleyError.printStackTrace();
                        Toast.makeText(AddFuel.this, getString(R.string.error), Toast.LENGTH_LONG).show();
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

                //Adding parameters
                params.put("username", username);
                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("plateNO", plateNo);
                params.put("kilometer", String.valueOf(tempKM));
                params.put("stationID", String.valueOf(chosenStationID));
                params.put("stationNAME", stationName);
                params.put("stationLOC", stationLoc);
                params.put("stationICON", stationLogo);
                if (entryPrice > 0) {
                    params.put("fuelType", String.valueOf(fuelPri));
                    params.put("fuelPrice", String.valueOf(selectedUnitPrice));
                    params.put("fuelLiter", String.valueOf(buyedLiter));
                    params.put("fuelTax", String.valueOf(selectedTaxRate));
                    params.put("subTotal", String.valueOf(entryPrice));
                } else {
                    params.put("fuelType", String.valueOf(-1));
                    params.put("fuelPrice", String.valueOf(0));
                    params.put("fuelLiter", String.valueOf(0));
                    params.put("fuelTax", String.valueOf(0));
                    params.put("subTotal", String.valueOf(0));
                }
                if (entryPrice2 > 0) {
                    params.put("fuelType2", String.valueOf(fuelSec));
                    params.put("fuelPrice2", String.valueOf(selectedUnitPrice2));
                    params.put("fuelLiter2", String.valueOf(buyedLiter2));
                    params.put("fuelTax2", String.valueOf(selectedTaxRate2));
                    params.put("subTotal2", String.valueOf(entryPrice2));
                } else {
                    params.put("fuelType2", String.valueOf(-1));
                    params.put("fuelPrice2", String.valueOf(0));
                    params.put("fuelLiter2", String.valueOf(0));
                    params.put("fuelTax2", String.valueOf(0));
                    params.put("subTotal2", String.valueOf(0));
                }
                params.put("totalPrice", String.valueOf(totalPrice));
                params.put("country", userCountry);
                params.put("unit", String.valueOf(userUnit));
                params.put("currency", String.valueOf(currencyCode));
                if (bitmap != null) {
                    params.put("billPhoto", getStringImage(bitmap));
                } else {
                    params.put("billPhoto", "");
                }
                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }

    private void updateTaxandGrandTotal() {
        totalPrice = entryPrice + entryPrice2;
        String totalHolder = getString(R.string.total) + ": " + String.format(Locale.getDefault(), "%.2f", totalPrice) + " " + currencyCode;
        fuelGrandTotal.setText(totalHolder);
        textViewBonus.setText("Fiş/Fatura fotoğrafı ekleyerek " + String.format(Locale.getDefault(), "%.2f", totalPrice / 100f) + " FP bonus kazanabilirsiniz!");
    }

    private void coloredBars(int color1, int color2) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color1);
            toolbar.setBackgroundColor(color2);
        } else {
            toolbar.setBackgroundColor(color2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (ActivityCompat.checkSelfPermission(AddFuel.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(AddFuel.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                ImagePicker.create(AddFuel.this).single().start();
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Imagepicker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                try {
                    bitmap = BitmapFactory.decodeFile(image.getPath());
                    ExifInterface ei = new ExifInterface(image.getPath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_NORMAL:
                            bitmap = resizeAndRotate(bitmap, 0);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap = resizeAndRotate(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = resizeAndRotate(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = resizeAndRotate(bitmap, 270);
                            break;
                    }
                    Glide.with(this).load(bitmap).apply(options).into(photoHolder);
                    photoHolder.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Remove variables
        bitmap = null;
        chosenStationID = 0;
        stationName = null;
        tempKM = 0;
        stationLoc = null;
        stationLogo = null;
        gasolinePrice = 0;
        dieselPrice = 0;
        electricityPrice = 0;
        LPGPrice = 0;
        finish();
    }
}
