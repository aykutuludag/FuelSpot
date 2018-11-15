package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.userUnit;

public class PurchaseDetails extends AppCompatActivity {

    RequestOptions options;
    MapView mMapView;
    GoogleMap googleMap;
    FloatingActionButton fab;
    int purchaseID, fuelType1, fuelType2;
    String stationName, iconURL, stationLocation, billPhoto;
    String purchaseTime;
    float fuelPrice1, fuelLiter1, fuelTax1, fuelPrice2, fuelLiter2, fuelTax2, totalPrice;

    ImageView istasyonLogo, fatura, tur1, tur2;
    TextView fiyat1, litre1, fiyat2, litre2, birimFiyat1, birimFiyat2, vergi, toplamfiyat;
    RelativeTimeTextView tarih;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_details);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Satın alma detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        purchaseID = getIntent().getIntExtra("PURCHASE_ID", 0);
        stationName = getIntent().getStringExtra("STATION_NAME");
        iconURL = getIntent().getStringExtra("STATION_ICON");
        stationLocation = getIntent().getStringExtra("STATION_LOC");
        purchaseTime = getIntent().getStringExtra("PURCHASE_TIME");
        fuelType1 = getIntent().getIntExtra("FUEL_TYPE_1", -1);
        fuelType2 = getIntent().getIntExtra("FUEL_TYPE_2", -1);
        billPhoto = getIntent().getStringExtra("BILL_PHOTO");
        fuelPrice1 = getIntent().getFloatExtra("FUEL_PRICE_1", 0);
        fuelPrice2 = getIntent().getFloatExtra("FUEL_PRICE_2", 0);
        fuelLiter1 = getIntent().getFloatExtra("FUEL_LITER_1", 0);
        fuelLiter2 = getIntent().getFloatExtra("FUEL_LITER_2", 0);
        fuelTax1 = getIntent().getFloatExtra("FUEL_TAX_1", 0);
        fuelTax2 = getIntent().getFloatExtra("FUEL_TAX_2", 0);
        totalPrice = getIntent().getFloatExtra("TOTAL_PRICE", 0);

        istasyonLogo = findViewById(R.id.stationLogo);
        fatura = findViewById(R.id.billPhoto);
        tur1 = findViewById(R.id.type1);
        tur2 = findViewById(R.id.type2);
        fiyat1 = findViewById(R.id.price1);
        birimFiyat1 = findViewById(R.id.unitPrice1);
        birimFiyat2 = findViewById(R.id.unitPrice2);
        litre1 = findViewById(R.id.amount1);
        fiyat2 = findViewById(R.id.price2);
        litre2 = findViewById(R.id.amount2);
        vergi = findViewById(R.id.totalTax);
        toplamfiyat = findViewById(R.id.totalPrice);
        tarih = findViewById(R.id.purchaseTime);

        options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(this).load(billPhoto).apply(options).into(fatura);


        Glide.with(this).load(iconURL).into(istasyonLogo);
        switch (fuelType1) {
            case 0:
                Glide.with(this).load(R.drawable.gasoline).into(tur1);
                break;
            case 1:
                Glide.with(this).load(R.drawable.diesel).into(tur1);
                break;
            case 2:
                Glide.with(this).load(R.drawable.lpg).into(tur1);
                break;
            case 3:
                Glide.with(this).load(R.drawable.electricity).into(tur1);
                break;
        }
        birimFiyat1.setText(fuelPrice1 + " " + currencySymbol);
        litre1.setText(fuelLiter1 + " " + userUnit);
        int priceOne = (int) (fuelPrice1 * fuelLiter1);
        String priceHolder = priceOne + " " + currencySymbol;
        fiyat1.setText(priceHolder);

        if (fuelType2 != -1) {
            switch (fuelType2) {
                case 0:
                    Glide.with(this).load(R.drawable.gasoline).into(tur2);
                    break;
                case 1:
                    Glide.with(this).load(R.drawable.diesel).into(tur2);
                    break;
                case 2:
                    Glide.with(this).load(R.drawable.lpg).into(tur2);
                    break;
                case 3:
                    Glide.with(this).load(R.drawable.electricity).into(tur2);
                    break;
            }
            birimFiyat2.setText(fuelPrice2 + " " + currencySymbol);
            litre2.setText(fuelLiter2 + " " + userUnit);
            int priceTwo = (int) (fuelPrice2 * fuelLiter2);
            String priceHolder2 = priceTwo + " " + currencySymbol;
            fiyat2.setText(priceHolder2);
        } else {
            tur2.setVisibility(View.GONE);
            fiyat2.setVisibility(View.GONE);
            litre2.setVisibility(View.GONE);
        }

        float tax1 = fuelPrice1 * fuelLiter1 * fuelTax1;
        float tax2 = fuelPrice2 * fuelLiter2 * fuelTax2;
        String taxHolder = "VERGİ: " + String.format(Locale.getDefault(), "%.2f", tax1 + tax2) + " " + currencySymbol;
        vergi.setText(taxHolder);

        String totalHolder = "TOPLAM : " + String.format(Locale.getDefault(), "%.2f", totalPrice) + " " + currencySymbol;
        toplamfiyat.setText(totalHolder);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(purchaseTime);
            tarih.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Satın alma silinecek", Snackbar.LENGTH_LONG)
                        .setAction("SİL", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deletePurchase();
                            }
                        })
                        .show();
            }
        });

        checkLocationPermission();
    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(PurchaseDetails.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PurchaseDetails.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PurchaseDetails.this, new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
        } else {
            loadMap();
        }
    }

    void loadMap() {
        //Detect location and set on map
        MapsInitializer.initialize(this.getApplicationContext());
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                LatLng mStationLoc = new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1]));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(mStationLoc).zoom(16f).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition
                        (cameraPosition));
                googleMap.addMarker(new MarkerOptions().position(mStationLoc).title(stationName).snippet(tarih.getText().toString()));
            }
        });
    }

    private void deletePurchase() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.getString(R.string.API_DELETE_PURCHASE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(PurchaseDetails.this, response, Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("id", String.valueOf(purchaseID));

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (ActivityCompat.checkSelfPermission(PurchaseDetails.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        loadMap();
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
