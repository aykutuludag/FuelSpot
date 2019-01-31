package com.fuelspot.superuser;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.fuelspot.R;
import com.fuelspot.adapter.CompanyAdapter;
import com.fuelspot.model.CompanyItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.FragmentSettings.companyList;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;
import static com.fuelspot.superuser.SuperMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.SuperMainActivity.superFacilities;
import static com.fuelspot.superuser.SuperMainActivity.superLastUpdate;
import static com.fuelspot.superuser.SuperMainActivity.superLicenseNo;
import static com.fuelspot.superuser.SuperMainActivity.superStationAddress;
import static com.fuelspot.superuser.SuperMainActivity.superStationCountry;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;
import static com.fuelspot.superuser.SuperMainActivity.superStationLogo;
import static com.fuelspot.superuser.SuperMainActivity.superStationName;

public class SuperUpdateStation extends AppCompatActivity {

    TextView stationAddressHolder, stationLicenseHolder, textViewOwnerHolder, textViewStationIDHolder;
    EditText gasolineHolder, dieselHolder, electricityHolder, lpgHolder;
    Button buttonUpdateStation;
    RequestQueue requestQueue;
    RelativeTimeTextView lastUpdateTimeText;
    CircleImageView stationLogoHolder, imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic, imageViewRestaurant, imageViewParkSpot;
    RelativeLayout verifiedLayout;
    RequestOptions options;
    Spinner spinner;
    Window window;
    Toolbar toolbar;
    JSONObject facilitiesObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_update_station);

        window = this.getWindow();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.BLACK, Color.parseColor("#212121"));

        requestQueue = Volley.newRequestQueue(SuperUpdateStation.this);
        options = new RequestOptions().centerCrop().error(R.drawable.default_station).error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        stationLogoHolder = findViewById(R.id.stationLogo);
        spinner = findViewById(R.id.simpleSpinner);
        stationAddressHolder = findViewById(R.id.editTextStationAddress);
        stationLicenseHolder = findViewById(R.id.editTextStationLicense);
        textViewOwnerHolder = findViewById(R.id.editTextOwner);
        textViewStationIDHolder = findViewById(R.id.textViewStationID);
        lastUpdateTimeText = findViewById(R.id.stationLastUpdate);
        verifiedLayout = findViewById(R.id.verifiedSection);
        gasolineHolder = findViewById(R.id.editTextGasoline);
        dieselHolder = findViewById(R.id.editTextDiesel);
        lpgHolder = findViewById(R.id.editTextLPG);
        electricityHolder = findViewById(R.id.editTextElectricity);
        imageViewWC = findViewById(R.id.WC);
        imageViewMarket = findViewById(R.id.Market);
        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewRestaurant = findViewById(R.id.Restaurant);
        imageViewParkSpot = findViewById(R.id.ParkSpot);
        buttonUpdateStation = findViewById(R.id.buttonUpdate);
        buttonUpdateStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStation();
            }
        });

        loadLayoutItems();
    }

    void loadLayoutItems() {
        if (companyList != null && companyList.size() > 0) {
            CompanyAdapter customAdapter = new CompanyAdapter(SuperUpdateStation.this, companyList);
            spinner.setEnabled(false);
            spinner.setClickable(false);
            spinner.setAdapter(customAdapter);

            for (int i = 0; i < companyList.size(); i++) {
                if (companyList.get(i).getName().equals(superStationName)) {
                    spinner.setSelection(i, true);
                    break;
                }
            }
        } else {
            // Somehow companList didn't fetch at SuperMainActivity. Fetch it.
            fetchCompanies();
        }

        String dummyId = "" + superStationID;
        textViewStationIDHolder.setText(dummyId);

        // Layout items
        stationAddressHolder.setText(superStationAddress);

        stationLicenseHolder.setText(superLicenseNo);

        textViewOwnerHolder.setText(name);

        Glide.with(this).load(superStationLogo).apply(options).into(stationLogoHolder);

        // if stationVerified == 1, this section shows up!
        if (isStationVerified == 1) {
            verifiedLayout.setVisibility(View.VISIBLE);
        } else {
            verifiedLayout.setVisibility(View.GONE);
        }

        if (superLastUpdate != null && superLastUpdate.length() > 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = format.parse(superLastUpdate);
                lastUpdateTimeText.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                lastUpdateTimeText.setText("Son güncelleme: ");
            }
        }

        String dummyG = "" + ownedGasolinePrice;
        gasolineHolder.setText(dummyG);
        gasolineHolder.addTextChangedListener(new TextWatcher() {
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

        String dummyD = "" + ownedDieselPrice;
        dieselHolder.setText(dummyD);
        dieselHolder.addTextChangedListener(new TextWatcher() {
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

        String dummyL = "" + ownedLPGPrice;
        lpgHolder.setText(dummyL);
        lpgHolder.addTextChangedListener(new TextWatcher() {
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

        String dummyE = "" + ownedElectricityPrice;
        electricityHolder.setText(dummyE);
        electricityHolder.addTextChangedListener(new TextWatcher() {
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
        try {
            JSONArray facilitiesRes = new JSONArray(superFacilities);
            facilitiesObj = facilitiesRes.getJSONObject(0);

            if (facilitiesObj.getInt("WC") == 1) {
                imageViewWC.setAlpha(1.0f);
            } else {
                imageViewWC.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Market") == 1) {
                imageViewMarket.setAlpha(1.0f);
            } else {
                imageViewMarket.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("CarWash") == 1) {
                imageViewCarWash.setAlpha(1.0f);
            } else {
                imageViewCarWash.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("TireRepair") == 1) {
                imageViewTireRepair.setAlpha(1.0f);
            } else {
                imageViewTireRepair.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Mechanic") == 1) {
                imageViewMechanic.setAlpha(1.0f);
            } else {
                imageViewMechanic.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Restaurant") == 1) {
                imageViewRestaurant.setAlpha(1.0f);
            } else {
                imageViewRestaurant.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("ParkSpot") == 1) {
                imageViewParkSpot.setAlpha(1.0f);
            } else {
                imageViewParkSpot.setAlpha(0.5f);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (facilitiesObj != null) {
            imageViewWC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("WC") == 1) {
                            facilitiesObj.put("WC", "0");
                            imageViewWC.setAlpha(0.5f);
                        } else {
                            facilitiesObj.put("WC", "1");
                            imageViewWC.setAlpha(1.0f);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            imageViewMarket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("Market") == 1) {
                            facilitiesObj.put("Market", "0");
                            imageViewMarket.setAlpha(0.5f);
                        } else {
                            facilitiesObj.put("Market", "1");
                            imageViewMarket.setAlpha(1.0f);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageViewCarWash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("CarWash") == 1) {
                            facilitiesObj.put("CarWash", "0");
                            imageViewCarWash.setAlpha(0.5f);
                        } else {
                            facilitiesObj.put("CarWash", "1");
                            imageViewCarWash.setAlpha(1.0f);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageViewTireRepair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("TireRepair") == 1) {
                            facilitiesObj.put("TireRepair", "0");
                            imageViewTireRepair.setAlpha(0.5f);
                        } else {
                            facilitiesObj.put("TireRepair", "1");
                            imageViewTireRepair.setAlpha(1.0f);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageViewMechanic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("Mechanic") == 1) {
                            facilitiesObj.put("Mechanic", "0");
                            imageViewMechanic.setAlpha(0.5f);
                        } else {
                            facilitiesObj.put("Mechanic", "1");
                            imageViewMechanic.setAlpha(1.0f);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageViewRestaurant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("Restaurant") == 1) {
                            facilitiesObj.put("Restaurant", "0");
                            imageViewRestaurant.setAlpha(0.5f);
                        } else {
                            facilitiesObj.put("Restaurant", "1");
                            imageViewRestaurant.setAlpha(1.0f);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageViewParkSpot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("ParkSpot") == 1) {
                            facilitiesObj.put("ParkSpot", "0");
                            imageViewParkSpot.setAlpha(0.5f);
                        } else {
                            facilitiesObj.put("ParkSpot", "1");
                            imageViewParkSpot.setAlpha(1.0f);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    void fetchCompanies() {
        companyList.clear();

        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_COMPANY),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CompanyItem item = new CompanyItem();
                                    item.setID(obj.getInt("id"));
                                    item.setName(obj.getString("companyName"));
                                    item.setLogo(obj.getString("companyLogo"));
                                    item.setWebsite(obj.getString("companyWebsite"));
                                    item.setPhone(obj.getString("companyPhone"));
                                    item.setAddress(obj.getString("companyAddress"));
                                    item.setNumOfVerifieds(obj.getInt("numOfVerifieds"));
                                    item.setNumOfStations(obj.getInt("numOfStations"));
                                    companyList.add(item);
                                }


                            } catch (JSONException e) {
                                Snackbar.make(findViewById(android.R.id.content), e.toString(), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Snackbar.make(findViewById(android.R.id.content), volleyError.toString(), Snackbar.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updateStation() {
        final ProgressDialog loading = ProgressDialog.show(SuperUpdateStation.this, "İstasyon güncelleniyor...", "Lütfen bekleyiniz...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            loading.dismiss();
                            switch (response) {
                                case "Success":
                                    Toast.makeText(SuperUpdateStation.this, getString(R.string.stationUpdated), Toast.LENGTH_LONG).show();
                                    break;
                                case "Fail":
                                    Toast.makeText(SuperUpdateStation.this, getString(R.string.stationUpdateFail), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(SuperUpdateStation.this, getString(R.string.stationUpdateFail), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(superStationID));
                params.put("stationName", superStationName);
                params.put("country", superStationCountry);
                String dummy = "[" + String.valueOf(facilitiesObj) + "]";
                params.put("facilities", dummy);
                params.put("gasolinePrice", String.valueOf(ownedGasolinePrice));
                params.put("dieselPrice", String.valueOf(ownedDieselPrice));
                params.put("lpgPrice", String.valueOf(ownedLPGPrice));
                params.put("electricityPrice", String.valueOf(ownedElectricityPrice));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void coloredBars(int color1, int color2) {
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
