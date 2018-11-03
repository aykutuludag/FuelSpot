package com.fuelspot.superuser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.fuelspot.R;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.superuser.AdminMainActivity.isStationActive;
import static com.fuelspot.superuser.AdminMainActivity.isStationVerified;
import static com.fuelspot.superuser.AdminMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.AdminMainActivity.superFacilities;
import static com.fuelspot.superuser.AdminMainActivity.superLastUpdate;
import static com.fuelspot.superuser.AdminMainActivity.superStationAddress;
import static com.fuelspot.superuser.AdminMainActivity.superStationID;
import static com.fuelspot.superuser.AdminMainActivity.superStationLogo;
import static com.fuelspot.superuser.AdminMainActivity.superStationName;

public class SuperUpdateStation extends AppCompatActivity {

    EditText editTextStationName, editTextStationAddress, editTextGasoline, editTextDiesel, editTextLPG, editTextElectricity;
    Button sendPricesButton;
    RequestQueue requestQueue;
    RelativeTimeTextView textViewLastUpdated;
    CircleImageView imageViewStationLogo, imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic;
    RelativeLayout verifiedLayout;
    RequestOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_update_station);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        requestQueue = Volley.newRequestQueue(SuperUpdateStation.this);
        options = new RequestOptions().centerCrop().error(R.drawable.default_station).error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        editTextStationName = findViewById(R.id.editTextStationName);
        editTextStationName.setText(superStationName);

        editTextStationAddress = findViewById(R.id.editTextStationAddress);
        editTextStationAddress.setText(superStationAddress);

        imageViewStationLogo = findViewById(R.id.stationLogo);
        Glide.with(this).load(superStationLogo).apply(options).into(imageViewStationLogo);

        //Last updated
        textViewLastUpdated = findViewById(R.id.stationLastUpdate);
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = format.parse(superLastUpdate);
            textViewLastUpdated.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // if stationVerified == 1, this section shows up!
        verifiedLayout = findViewById(R.id.verifiedSection);
        if (isStationVerified == 1) {
            verifiedLayout.setVisibility(View.VISIBLE);
        } else {
            verifiedLayout.setVisibility(View.GONE);
        }

        editTextGasoline = findViewById(R.id.editTextGasoline);
        editTextGasoline.setText(String.valueOf(ownedGasolinePrice));
        editTextGasoline.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    ownedGasolinePrice = Float.parseFloat(s.toString());
                }
            }
        });

        editTextDiesel = findViewById(R.id.editTextDiesel);
        editTextDiesel.setText(String.valueOf(ownedDieselPrice));
        editTextDiesel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    ownedDieselPrice = Float.parseFloat(s.toString());
                }
            }
        });

        editTextLPG = findViewById(R.id.editTextLPG);
        editTextLPG.setText(String.valueOf(ownedLPGPrice));
        editTextLPG.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    ownedLPGPrice = Float.parseFloat(s.toString());
                }
            }
        });

        editTextElectricity = findViewById(R.id.editTextElectricity);
        editTextElectricity.setText(String.valueOf(ownedElectricityPrice));
        editTextElectricity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    ownedElectricityPrice = Float.parseFloat(s.toString());
                }
            }
        });

        // Facilities
        imageViewWC = findViewById(R.id.WC);
        imageViewWC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewWC.getAlpha() == 1.0f) {
                    superFacilities = superFacilities.replace("WC;", "");
                    imageViewWC.setAlpha(0.5f);
                } else {
                    superFacilities = superFacilities + "WC;";
                    imageViewWC.setAlpha(1.0f);
                }
            }
        });

        imageViewMarket = findViewById(R.id.Market);
        imageViewMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewMarket.getAlpha() == 1.0f) {
                    superFacilities = superFacilities.replace("Market;", "");
                    imageViewMarket.setAlpha(0.5f);
                } else {
                    superFacilities = superFacilities + "Market;";
                    imageViewMarket.setAlpha(1.0f);
                }
            }
        });

        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewCarWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewCarWash.getAlpha() == 1.0f) {
                    superFacilities = superFacilities.replace("CarWash;", "");
                    imageViewCarWash.setAlpha(0.5f);
                } else {
                    superFacilities = superFacilities + "CarWash;";
                    imageViewCarWash.setAlpha(1.0f);
                }
            }
        });

        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewTireRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewTireRepair.getAlpha() == 1.0f) {
                    superFacilities = superFacilities.replace("TireRepair;", "");
                    imageViewTireRepair.setAlpha(0.5f);
                } else {
                    superFacilities = superFacilities + "TireRepair;";
                    imageViewTireRepair.setAlpha(1.0f);
                }
            }
        });

        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewMechanic.getAlpha() == 1.0f) {
                    superFacilities = superFacilities.replace("Mechanic;", "");
                    imageViewMechanic.setAlpha(0.5f);
                } else {
                    superFacilities = superFacilities + "Mechanic;";
                    imageViewMechanic.setAlpha(1.0f);
                }
            }
        });


        // Facilities
        if (superFacilities != null && superFacilities.length() > 0) {
            if (superFacilities.contains("WC")) {
                imageViewWC.setAlpha(1.0f);
            } else {
                imageViewWC.setAlpha(0.5f);
            }

            if (superFacilities.contains("Market")) {
                imageViewMarket.setAlpha(1.0f);
            } else {
                imageViewMarket.setAlpha(0.5f);
            }

            if (superFacilities.contains("CarWash")) {
                imageViewCarWash.setAlpha(1.0f);
            } else {
                imageViewCarWash.setAlpha(0.5f);
            }

            if (superFacilities.contains("TireRepair")) {
                imageViewTireRepair.setAlpha(1.0f);
            } else {
                imageViewTireRepair.setAlpha(0.5f);
            }

            if (superFacilities.contains("Mechanic")) {
                imageViewMechanic.setAlpha(1.0f);
            } else {
                imageViewMechanic.setAlpha(0.5f);
            }
        }

        sendPricesButton = findViewById(R.id.buttonUpdate);
        sendPricesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPrices();
            }
        });
    }

    public void sendPrices() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(SuperUpdateStation.this, s, Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(SuperUpdateStation.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(superStationID));
                params.put("stationName", superStationName);
                params.put("stationVicinity", superStationAddress);
                params.put("facilities", superFacilities);
                params.put("gasolinePrice", String.valueOf(ownedGasolinePrice));
                params.put("dieselPrice", String.valueOf(ownedDieselPrice));
                params.put("lpgPrice", String.valueOf(ownedLPGPrice));
                params.put("electricityPrice", String.valueOf(ownedElectricityPrice));
                params.put("isActive", String.valueOf(isStationActive));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
