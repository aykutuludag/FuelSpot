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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.R;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import static com.fuelspot.superuser.AdminMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.AdminMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.AdminMainActivity.superStationID;

public class SuperEditPrices extends AppCompatActivity {

    Long updateTime;
    EditText editTextGasoline, editTextDiesel, editTextLPG, editTextElectricity;
    Button sendPricesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_prices);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    ownedGasolinePrice = Double.parseDouble(s.toString());
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
                    ownedDieselPrice = Double.parseDouble(s.toString());
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
                    ownedLPGPrice = Double.parseDouble(s.toString());
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
                    ownedElectricityPrice = Double.parseDouble(s.toString());
                }
            }
        });

        sendPricesButton = findViewById(R.id.sendFiyat);
        sendPricesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                updateTime = calendar.getTimeInMillis();
                sendPrices();
            }
        });
    }

    public void sendPrices() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION_PRICES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(SuperEditPrices.this, s, Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(SuperEditPrices.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(superStationID));
                params.put("gasolinePrice", String.valueOf(ownedGasolinePrice));
                params.put("dieselPrice", String.valueOf(ownedDieselPrice));
                params.put("lpgPrice", String.valueOf(ownedLPGPrice));
                params.put("electricityPrice", String.valueOf(ownedElectricityPrice));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(SuperEditPrices.this);

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
