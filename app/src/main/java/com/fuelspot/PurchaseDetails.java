package com.fuelspot;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

public class PurchaseDetails extends AppCompatActivity {

    MapView mMapView;
    GoogleMap googleMap;
    FloatingActionButton fab;
    int purchaseID;
    String stationName, iconURL, stationLocation, fuelType1, fuelType2, billPhoto;
    long purchaseTime;
    double fuelPrice1, fuelLiter1, fuelPrice2, fuelLiter2, totalPrice;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        purchaseID = getIntent().getIntExtra("PURCHASE_ID", 0);
        stationName = getIntent().getStringExtra("STATION_NAME");
        iconURL = getIntent().getStringExtra("STATION_ICON");
        stationLocation = getIntent().getStringExtra("STATION_LOC");
        purchaseTime = getIntent().getLongExtra("PURCHASE_TIME", 0);
        fuelType1 = getIntent().getStringExtra("FUEL_TYPE_1");
        fuelType2 = getIntent().getStringExtra("FUEL_TYPE_2");
        billPhoto = getIntent().getStringExtra("BILL_PHOTO");
        fuelPrice1 = getIntent().getDoubleExtra("FUEL_PRICE_1", 0);
        fuelPrice2 = getIntent().getDoubleExtra("FUEL_PRICE_2", 0);
        fuelLiter1 = getIntent().getDoubleExtra("FUEL_LITER_1", 0);
        fuelLiter2 = getIntent().getDoubleExtra("FUEL_LITER_2", 0);
        totalPrice = getIntent().getDoubleExtra("TOTAL_PRICE", 0);

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

        Glide.with(this).load(iconURL).into(istasyonLogo);
        Glide.with(this).load(billPhoto).into(fatura);
        switch (fuelType1) {
            case "gasoline":
                Glide.with(this).load(R.drawable.gasoline).into(tur1);
                break;
            case "diesel":
                Glide.with(this).load(R.drawable.diesel).into(tur1);
                break;
            case "lpg":
                Glide.with(this).load(R.drawable.lpg).into(tur1);
                break;
            case "electric":
                Glide.with(this).load(R.drawable.electricity).into(tur1);
                break;
        }
        birimFiyat1.setText(fuelPrice1 + "TL");
        litre1.setText(fuelLiter1 + "LT");
        int priceOne = (int) (fuelPrice1 * fuelLiter1);
        String priceHolder = priceOne + " TL";
        fiyat1.setText(priceHolder);

        if (fuelType2 != null && fuelType2.length() > 0) {
            switch (fuelType2) {
                case "gasoline":
                    Glide.with(this).load(R.drawable.gasoline).into(tur2);
                    break;
                case "diesel":
                    Glide.with(this).load(R.drawable.diesel).into(tur2);
                    break;
                case "lpg":
                    Glide.with(this).load(R.drawable.lpg).into(tur2);
                    break;
                case "electric":
                    Glide.with(this).load(R.drawable.electricity).into(tur2);
                    break;
            }
            birimFiyat2.setText(fuelPrice2 + "TL");
            litre2.setText(fuelLiter2 + "LT");
            int priceTwo = (int) (fuelPrice2 * fuelLiter2);
            String priceHolder2 = priceTwo + " TL";
            fiyat2.setText(priceHolder2);
        } else {
            tur2.setVisibility(View.GONE);
            fiyat2.setVisibility(View.GONE);
            litre2.setVisibility(View.GONE);
        }


        float tax1 = MainActivity.taxCalculator(fuelType1, (float) (fuelPrice1 * fuelLiter1));
        float tax2 = MainActivity.taxCalculator(fuelType2, (float) (fuelPrice2 * fuelLiter2));
        String taxHolder = "VERGİ: " + String.format(Locale.getDefault(), "%.2f", tax1 + tax2) + " TL";
        vergi.setText(taxHolder);
        toplamfiyat.setText(totalPrice + "TL");
        tarih.setReferenceTime(purchaseTime);

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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Konum izni gerekiyor")
                        .setMessage("Size en yakın benzinlikleri ve fiyatlarını gösterebilmemiz için konum iznine ihtiyaç duyuyoruz")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(PurchaseDetails.this, new String[]
                                        {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
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
                CameraPosition cameraPosition = new CameraPosition.Builder().target(mStationLoc).zoom(13f).build();
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
