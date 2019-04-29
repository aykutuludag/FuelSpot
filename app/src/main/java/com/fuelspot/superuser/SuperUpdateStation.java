package com.fuelspot.superuser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
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
import com.fuelspot.R;
import com.fuelspot.adapter.CompanyAdapter;
import com.fuelspot.model.CompanyItem;
import com.fuelspot.model.StationItem;
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

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.companyList;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;
import static com.fuelspot.superuser.SuperMainActivity.listOfOwnedStations;
import static com.fuelspot.superuser.SuperMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.SuperMainActivity.superFacilities;
import static com.fuelspot.superuser.SuperMainActivity.superGoogleID;
import static com.fuelspot.superuser.SuperMainActivity.superLastUpdate;
import static com.fuelspot.superuser.SuperMainActivity.superLicenseNo;
import static com.fuelspot.superuser.SuperMainActivity.superStationAddress;
import static com.fuelspot.superuser.SuperMainActivity.superStationCountry;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;
import static com.fuelspot.superuser.SuperMainActivity.superStationLocation;
import static com.fuelspot.superuser.SuperMainActivity.superStationLogo;
import static com.fuelspot.superuser.SuperMainActivity.superStationName;

public class SuperUpdateStation extends AppCompatActivity {

    private EditText stationAddressHolder;
    private TextView stationLicenseHolder;
    private TextView textViewStationIDHolder;
    private EditText gasolineHolder;
    private EditText dieselHolder;
    private EditText electricityHolder;
    private EditText lpgHolder;
    private RequestQueue requestQueue;
    private RelativeTimeTextView lastUpdateTimeText;
    SharedPreferences prefs;
    private RelativeLayout verifiedLayout;
    private Spinner spinner;
    private Window window;
    private Toolbar toolbar;
    private CircleImageView imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic, imageViewRestaurant, imageViewParkSpot, imageViewATM;
    private JSONObject facilitiesObj;

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

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(SuperUpdateStation.this);

        spinner = findViewById(R.id.simpleSpinner);
        stationAddressHolder = findViewById(R.id.editTextStationAddress);
        stationLicenseHolder = findViewById(R.id.editTextStationLicense);
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
        imageViewATM = findViewById(R.id.ATM);

        Button streetViewApp = findViewById(R.id.buttonDownloadStreetView);
        streetViewApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.street"));
                startActivity(intent);
            }
        });

        Button buttonUpdateStation = findViewById(R.id.buttonUpdate);
        buttonUpdateStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStation();
            }
        });

        loadLayoutItems();
    }

    private void loadLayoutItems() {
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
        stationAddressHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    superStationAddress = s.toString();
                }
            }
        });

        stationLicenseHolder.setText(superLicenseNo);

        // if stationVerified == 1, this section shows up!
        if (isStationVerified == 1) {
            verifiedLayout.setVisibility(View.VISIBLE);
        } else {
            verifiedLayout.setVisibility(View.GONE);
        }

        if (superLastUpdate != null && superLastUpdate.length() > 0) {
            SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
            try {
                Date date = format.parse(superLastUpdate);
                lastUpdateTimeText.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                lastUpdateTimeText.setText(getString(R.string.last_update));
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
                imageViewWC.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Market") == 1) {
                imageViewMarket.setAlpha(1.0f);
            } else {
                imageViewMarket.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("CarWash") == 1) {
                imageViewCarWash.setAlpha(1.0f);
            } else {
                imageViewCarWash.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("TireRepair") == 1) {
                imageViewTireRepair.setAlpha(1.0f);
            } else {
                imageViewTireRepair.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Mechanic") == 1) {
                imageViewMechanic.setAlpha(1.0f);
            } else {
                imageViewMechanic.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Restaurant") == 1) {
                imageViewRestaurant.setAlpha(1.0f);
            } else {
                imageViewRestaurant.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("ParkSpot") == 1) {
                imageViewParkSpot.setAlpha(1.0f);
            } else {
                imageViewParkSpot.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("ATM") == 1) {
                imageViewATM.setAlpha(1.0f);
            } else {
                imageViewATM.setAlpha(0.25f);
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
                            imageViewWC.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("WC", "1");
                            imageViewWC.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.wc), Toast.LENGTH_SHORT).show();
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
                            imageViewMarket.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("Market", "1");
                            imageViewMarket.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.market), Toast.LENGTH_SHORT).show();
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
                            imageViewCarWash.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("CarWash", "1");
                            imageViewCarWash.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.carwash), Toast.LENGTH_SHORT).show();
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
                            imageViewTireRepair.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("TireRepair", "1");
                            imageViewTireRepair.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.tire_store), Toast.LENGTH_SHORT).show();
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
                            imageViewMechanic.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("Mechanic", "1");
                            imageViewMechanic.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.mechanic), Toast.LENGTH_SHORT).show();
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
                            imageViewRestaurant.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("Restaurant", "1");
                            imageViewRestaurant.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.restaurant), Toast.LENGTH_SHORT).show();
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
                            imageViewParkSpot.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("ParkSpot", "1");
                            imageViewParkSpot.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.parkspot), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageViewATM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (facilitiesObj.getInt("ATM") == 1) {
                            facilitiesObj.put("ATM", "0");
                            imageViewATM.setAlpha(0.25f);
                        } else {
                            facilitiesObj.put("ATM", "1");
                            imageViewATM.setAlpha(1.0f);
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.atm), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void fetchCompanies() {
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
        final ProgressDialog loading = ProgressDialog.show(SuperUpdateStation.this, getString(R.string.station_updating), getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    fetchOwnedStations();
                                    break;
                                case "Fail":
                                    Toast.makeText(SuperUpdateStation.this, getString(R.string.station_update_fail), Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(SuperUpdateStation.this, getString(R.string.station_update_fail), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(SuperUpdateStation.this, getString(R.string.station_update_fail), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(SuperUpdateStation.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
                params.put("address", superStationAddress);
                String dummy = "[" + facilitiesObj + "]";
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

    private void fetchOwnedStations() {
        listOfOwnedStations.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_SUPERUSER_STATIONS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    StationItem item = new StationItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationName(obj.getString("name"));
                                    item.setVicinity(obj.getString("vicinity"));
                                    item.setCountryCode(obj.getString("country"));
                                    item.setLocation(obj.getString("location"));
                                    item.setGoogleMapID(obj.getString("googleID"));
                                    item.setFacilities(obj.getString("facilities"));
                                    item.setLicenseNo(obj.getString("licenseNo"));
                                    item.setOwner(obj.getString("owner"));
                                    item.setPhotoURL(obj.getString("logoURL"));
                                    item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                    item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                    item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                    item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setLastUpdated(obj.getString("lastUpdated"));

                                    //DISTANCE START
                                    Location locLastKnow = new Location("");
                                    locLastKnow.setLatitude(Double.parseDouble(userlat));
                                    locLastKnow.setLongitude(Double.parseDouble(userlon));

                                    Location loc = new Location("");
                                    String[] stationKonum = item.getLocation().split(";");
                                    loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                    loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                    float uzaklik = locLastKnow.distanceTo(loc);
                                    item.setDistance((int) uzaklik);
                                    //DISTANCE END
                                    listOfOwnedStations.add(item);
                                }

                                for (int k = 0; k < listOfOwnedStations.size(); k++) {
                                    if (superStationID == listOfOwnedStations.get(k).getID()) {
                                        chooseStation(listOfOwnedStations.get(k));
                                        break;
                                    }
                                }
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
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("superusername", username);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void chooseStation(StationItem item) {
        superStationID = item.getID();
        prefs.edit().putInt("SuperStationID", superStationID).apply();

        superStationName = item.getStationName();
        prefs.edit().putString("SuperStationName", superStationName).apply();

        superStationAddress = item.getVicinity();
        prefs.edit().putString("SuperStationAddress", superStationAddress).apply();

        superStationCountry = item.getCountryCode();
        prefs.edit().putString("SuperStationCountry", superStationCountry).apply();

        superStationLocation = item.getLocation();
        prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

        superGoogleID = item.getGoogleMapID();
        prefs.edit().putString("SuperGoogleID", superGoogleID).apply();

        superFacilities = item.getFacilities();
        prefs.edit().putString("SuperStationFacilities", superFacilities).apply();

        superLicenseNo = item.getLicenseNo();
        prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();

        superStationLogo = item.getPhotoURL();
        prefs.edit().putString("SuperStationLogo", superStationLogo).apply();

        ownedGasolinePrice = item.getGasolinePrice();
        prefs.edit().putFloat("superGasolinePrice", ownedGasolinePrice).apply();

        ownedDieselPrice = item.getDieselPrice();
        prefs.edit().putFloat("superDieselPrice", ownedDieselPrice).apply();

        ownedLPGPrice = item.getLpgPrice();
        prefs.edit().putFloat("superLPGPrice", ownedLPGPrice).apply();

        ownedElectricityPrice = item.getElectricityPrice();
        prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();

        isStationVerified = item.getIsVerified();
        prefs.edit().putInt("isStationVerified", isStationVerified).apply();

        superLastUpdate = item.getLastUpdated();
        prefs.edit().putString("SuperLastUpdate", superLastUpdate).apply();

        // Station info update&sync finished
        Toast.makeText(SuperUpdateStation.this, getString(R.string.station_update_success), Toast.LENGTH_LONG).show();
        finish();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
