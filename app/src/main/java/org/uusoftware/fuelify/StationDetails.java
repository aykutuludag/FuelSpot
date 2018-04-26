package org.uusoftware.fuelify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.squareup.picasso.Picasso;

public class StationDetails extends AppCompatActivity {

    String stationName, stationVicinity, stationLocation, iconURL;
    ImageView stationIcon;
    float stationDistance;
    double gasolinePrice, dieselPrice, lpgPrice, electricityPrice;
    long lastUpdated;

    TextView textName, textVicinity, textDistance, textGasoline, textDiesel, textLPG, textElectricity;
    RelativeTimeTextView textLastUpdated;

    StreetViewPanoramaView mStreetViewPanoramaView;
    StreetViewPanorama mPanorama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stationName = getIntent().getStringExtra("STATION_NAME");
        stationVicinity = getIntent().getStringExtra("STATION_VICINITY");
        stationLocation = getIntent().getStringExtra("STATION_LOCATION");
        stationDistance = getIntent().getFloatExtra("STATION_DISTANCE", 0.00f);
        gasolinePrice = getIntent().getDoubleExtra("STATION_GASOLINE", 0.00f);
        dieselPrice = getIntent().getDoubleExtra("STATION_DIESEL", 0.00f);
        lpgPrice = getIntent().getDoubleExtra("STATION_LPG", 0.00f);
        electricityPrice = getIntent().getDoubleExtra("STATION_ELECTRIC", 0.00f);
        lastUpdated = getIntent().getLongExtra("STATION_LASTUPDATED", 0);
        iconURL = getIntent().getStringExtra("STATION_ICON");

        textName = findViewById(R.id.station_name);
        textName.setText(stationName);

        textVicinity = findViewById(R.id.station_vicinity);
        textVicinity.setText(stationVicinity);

        textDistance = findViewById(R.id.distance_ofStation);
        textDistance.setText((int) stationDistance + " m");

        textGasoline = findViewById(R.id.gasoline_price);
        textGasoline.setText(String.valueOf(gasolinePrice));

        textDiesel = findViewById(R.id.diesel_price);
        textDiesel.setText(String.valueOf(dieselPrice));

        textLPG = findViewById(R.id.lpg_price);
        textLPG.setText(String.valueOf(lpgPrice));

        textElectricity = findViewById(R.id.electricity_price);
        textElectricity.setText(String.valueOf(electricityPrice));

        textLastUpdated = findViewById(R.id.lastUpdated);
        textLastUpdated.setReferenceTime(lastUpdated);

        //Station Icon
        stationIcon = findViewById(R.id.station_photo);
        Picasso.with(this).load(iconURL).error(R.drawable.unknown).placeholder(R.drawable.unknown)
                .into(stationIcon);

        mStreetViewPanoramaView = findViewById(R.id.street_view_panorama);
        mStreetViewPanoramaView.onCreate(savedInstanceState);
        mStreetViewPanoramaView.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
            @Override
            public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                panorama.setPosition(new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1])));
                mPanorama = panorama;
                mPanorama.setOnStreetViewPanoramaChangeListener(new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
                    @Override
                    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                        if (streetViewPanoramaLocation != null && streetViewPanoramaLocation.links != null) {
                            Toast.makeText(StationDetails.this, "Sokak görünümü mevcut", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(StationDetails.this, "Sokak görünümü yok", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + stationLocation.split(";")[0] + "," + stationLocation.split(";")[1]));
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStreetViewPanoramaView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStreetViewPanoramaView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStreetViewPanoramaView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mStreetViewPanoramaView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mStreetViewPanoramaView.onSaveInstanceState(outState);
    }
}
