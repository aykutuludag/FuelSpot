package com.fuelspot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.fuelspot.MainActivity.REQUEST_FILEPICKER;
import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.location;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userVehicles;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.vehicleID;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;

public class WelcomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    RequestQueue requestQueue;
    SharedPreferences prefs;
    Button continueButton, saveCarButton, finishButton;
    RelativeLayout layout1, layout3, layoutHow1, layoutHow2, layoutHow3;
    ScrollView layout2;
    Bitmap bitmap;
    CircleImageView carPic;
    Spinner spinner, spinner2;
    RadioButton gasoline, diesel, lpg, elec, gasoline2, diesel2, lpg2, elec2;
    ArrayAdapter<CharSequence> adapter;
    ArrayAdapter<String> adapter2;
    boolean howto1, howto2, howto3;
    RadioGroup radioGroup1, radioGroup2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Hoşgeldin");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(WelcomeActivity.this);

        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);

        layoutHow1 = findViewById(R.id.howto1);
        layoutHow2 = findViewById(R.id.howto2);
        layoutHow3 = findViewById(R.id.howto3);

        continueButton = findViewById(R.id.button2);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]
                        {MainActivity.PERMISSIONS_FILEPICKER[0], MainActivity.PERMISSIONS_FILEPICKER[1], MainActivity.PERMISSIONS_FILEPICKER[2], MainActivity.PERMISSIONS_LOCATION}, REQUEST_FILEPICKER);
            }
        });

        saveCarButton = findViewById(R.id.button4);
        saveCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addVehicle();
            }
        });

        finishButton = findViewById(R.id.button3);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!howto1) {
                    howto1 = true;
                    layoutHow2.setVisibility(View.VISIBLE);
                    layoutHow1.setVisibility(View.INVISIBLE);
                } else if (!howto2) {
                    howto2 = true;
                    layoutHow3.setVisibility(View.VISIBLE);
                    layoutHow2.setVisibility(View.INVISIBLE);
                } else {
                    //Registration finished
                    howto3 = true;
                    MainActivity.isSigned = true;
                    prefs.edit().putBoolean("isSigned", MainActivity.isSigned).apply();
                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });

        fetchUserInfo();
    }

    public void fetchUserInfo() {
        final ProgressDialog loading = ProgressDialog.show(WelcomeActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            loading.dismiss();
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            name = obj.getString("name");
                            prefs.edit().putString("Name", name).apply();

                            email = obj.getString("email");
                            prefs.edit().putString("Email", email).apply();

                            photo = obj.getString("photo");
                            prefs.edit().putString("ProfilePhoto", photo).apply();

                            gender = obj.getString("gender");
                            prefs.edit().putString("Gender", gender).apply();

                            birthday = obj.getString("birthday");
                            prefs.edit().putString("Birthday", birthday).apply();

                            location = obj.getString("location");
                            prefs.edit().putString("Location", location).apply();

                            userCountry = obj.getString("country");
                            prefs.edit().putString("userCountry", userCountry).apply();

                            userDisplayLanguage = obj.getString("language");
                            prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                            userVehicles = obj.getString("vehicles");
                            prefs.edit().putString("userVehicles", userVehicles).apply();

                            MainActivity.getVariables(prefs);
                            continueButton.setAlpha(1.0f);
                            continueButton.setClickable(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
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

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void Localization() {
        if (userlat != null && userlon != null) {
            if (userlat.length() > 0 && userlon.length() > 0) {
                Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geo.getFromLocation(Double.parseDouble(userlat), Double.parseDouble(userlon), 1);
                    if (addresses.size() > 0) {
                        userCountry = addresses.get(0).getCountryCode();
                        prefs.edit().putString("userCountry", userCountry).apply();

                        userCountryName = addresses.get(0).getCountryName();
                        prefs.edit().putString("userCountryName", userCountryName).apply();

                        userDisplayLanguage = Locale.getDefault().getDisplayLanguage();
                        prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                        Locale userLocale = new Locale(Locale.getDefault().getISO3Language(), addresses.get(0).getCountryCode());
                        currencyCode = Currency.getInstance(userLocale).getCurrencyCode();
                        prefs.edit().putString("userCurrency", currencyCode).apply();

                        if (userCountry.equals("US") || userCountry.equals("LR") || userCountry.equals("MM")) {
                            userUnit = getString(R.string.unitSystem2);
                        } else {
                            userUnit = getString(R.string.unitSystem1);
                        }
                        prefs.edit().putString("userUnit", userUnit).apply();
                        fetchTaxRates();
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

    public void fetchTaxRates() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_TAX),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                MainActivity.TAX_GASOLINE = (float) obj.getDouble("gasolineTax");
                                prefs.edit().putFloat("taxGasoline", MainActivity.TAX_GASOLINE).apply();

                                MainActivity.TAX_DIESEL = (float) obj.getDouble("dieselTax");
                                prefs.edit().putFloat("taxDiesel", MainActivity.TAX_DIESEL).apply();

                                MainActivity.TAX_LPG = (float) obj.getDouble("LPGTax");
                                prefs.edit().putFloat("taxLPG", MainActivity.TAX_LPG).apply();

                                MainActivity.TAX_ELECTRICITY = (float) obj.getDouble("electricityTax");
                                prefs.edit().putFloat("taxElectricity", MainActivity.TAX_ELECTRICITY).apply();

                                MainActivity.getVariables(prefs);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
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

                //Adding parameters
                params.put("country", MainActivity.userCountry);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

   /* public void fetchVehicle() {
        final ProgressDialog loading = ProgressDialog.show(WelcomeActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_VEHICLE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            carBrand = obj.getString("car_brand");
                            prefs.edit().putString("carBrand", carBrand).apply();

                            carModel = obj.getString("car_model");
                            prefs.edit().putString("carModel", carModel).apply();

                            MainActivity.fuelPri = obj.getInt("fuelPri");
                            prefs.edit().putInt("FuelPrimary", MainActivity.fuelPri).apply();

                            MainActivity.fuelSec = obj.getInt("fuelSec");
                            prefs.edit().putInt("FuelSecondary", MainActivity.fuelSec).apply();

                            kilometer = obj.getInt("kilometer");
                            prefs.edit().putInt("Kilometer", kilometer).apply();

                            MainActivity.carPhoto = obj.getString("carPhoto");
                            prefs.edit().putString("CarPhoto", MainActivity.carPhoto).apply();

                            loading.dismiss();
                            MainActivity.getVariables(prefs);
                            continueButton.setAlpha(1.0f);
                            continueButton.setClickable(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
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

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }*/

    public void loadCarSelection() {
        //CarPic
        carPic = findViewById(R.id.imageViewCar);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_automobile)
                .error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(this).load(MainActivity.carPhoto).apply(options).into(carPic);
        carPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(WelcomeActivity.this)) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(WelcomeActivity.this);
                } else {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, MainActivity.PERMISSIONS_FILEPICKER, MainActivity.REQUEST_FILEPICKER);
                }
            }
        });

        //MARKA SEÇİMİ
        spinner = findViewById(R.id.spinner_brands);
        adapter = ArrayAdapter.createFromResource(this, R.array.CAR_PRODUCER, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
        String[] foo_array = getResources().getStringArray(R.array.CAR_PRODUCER);
        spinner.setSelection(MainActivity.getIndexOf(foo_array, MainActivity.carBrand), true);

        //MODEL SEÇİMİ
        spinner2 = findViewById(R.id.spinner_models);

        //Yakıt seçenekleri
        radioGroup1 = findViewById(R.id.radioGroup_fuelPrimary);
        gasoline = findViewById(R.id.gasoline);
        diesel = findViewById(R.id.diesel);
        lpg = findViewById(R.id.lpg);
        elec = findViewById(R.id.electricity);
        radioGroup2 = findViewById(R.id.radioGroup_fuelSecondary);
        gasoline2 = findViewById(R.id.gasoline2);
        diesel2 = findViewById(R.id.diesel2);
        lpg2 = findViewById(R.id.lpg2);
        elec2 = findViewById(R.id.electricity2);

        switch (fuelPri) {
            case 0:
                gasoline.setChecked(true);
                break;
            case 1:
                diesel.setChecked(true);
                break;
            case 2:
                lpg.setChecked(true);
                break;
            case 3:
                elec.setChecked(true);
                break;
            default:
                fuelPri = -1;
                radioGroup1.clearCheck();
                break;
        }

        switch (fuelSec) {
            case 0:
                gasoline2.setChecked(true);
                break;
            case 1:
                diesel2.setChecked(true);
                break;
            case 2:
                lpg2.setChecked(true);
                break;
            case 3:
                elec2.setChecked(true);
                break;
            default:
                fuelSec = -1;
                radioGroup2.clearCheck();
                break;
        }

        //1. yakıt
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.gasoline) {
                    fuelPri = 0;
                } else if (checkedId == R.id.diesel) {
                    fuelPri = 1;
                } else if (checkedId == R.id.lpg) {
                    fuelPri = 2;
                } else {
                    fuelPri = 3;
                }

                fuelSec = -1;
                radioGroup2.clearCheck();

                prefs.edit().putInt("FuelPrimary", fuelPri).apply();
                prefs.edit().putInt("FuelSecondary", fuelSec).apply();
            }
        });

        //2. yakıt
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.gasoline2) {
                    fuelSec = 0;
                } else if (checkedId == R.id.diesel2) {
                    fuelSec = 1;
                } else if (checkedId == R.id.lpg2) {
                    fuelSec = 2;
                } else if (checkedId == R.id.electricity2) {
                    fuelSec = 3;
                }

                prefs.edit().putInt("FuelSecondary", fuelSec).apply();
            }
        });

        //Kilometre
        EditText eText = findViewById(R.id.editText_km);
        eText.setText(String.valueOf(kilometer));
        eText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() >= 1) {
                    kilometer = Integer.parseInt(s.toString());
                    prefs.edit().putInt("Kilometer", kilometer).apply();
                }
            }
        });

        //PlakaNO
        final EditText plateText = findViewById(R.id.editText_plate);
        plateText.setText(plateNo);
        plateText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    if (s.toString().contains(" ")) {
                        plateNo = s.toString().replaceAll(" ", "");
                    } else {
                        plateNo = s.toString();
                    }

                    //All uppercase
                    plateNo = plateNo.toUpperCase();

                    prefs.edit().putString("plateNo", plateNo).apply();
                }
            }
        });
    }

    private void addVehicle() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(WelcomeActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_VEHICLE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                vehicleID = obj.getInt("id");
                                prefs.edit().putInt("vehicleID", vehicleID).apply();

                                plateNo = obj.getString("plateNo");
                                prefs.edit().putString("plateNo", plateNo).apply();

                                userVehicles += vehicleID + "-" + plateNo + ";";
                                prefs.edit().putString("userVehicles", userVehicles).apply();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            MainActivity.getVariables(prefs);
                            updateUserInfo();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("country", userCountry);
                params.put("carBrand", carBrand);
                params.put("carModel", carModel);
                params.put("fuelPri", String.valueOf(fuelPri));
                params.put("fuelSec", String.valueOf(fuelSec));
                params.put("km", String.valueOf(kilometer));
                if (bitmap != null) {
                    params.put("carPhoto", getStringImage(bitmap));
                }
                params.put("plate", plateNo);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updateUserInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(WelcomeActivity.this, response, Toast.LENGTH_LONG).show();
                        layout3.setVisibility(View.VISIBLE);
                        layout2.setVisibility(View.GONE);
                        System.out.println(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(WelcomeActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        System.out.println(volleyError);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", MainActivity.username);
                params.put("email", MainActivity.email);
                params.put("gender", MainActivity.gender);
                params.put("birthday", MainActivity.birthday);
                params.put("location", MainActivity.location);
                params.put("country", MainActivity.userCountry);
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                }
                params.put("language", userDisplayLanguage);
                params.put("vehicles", userVehicles);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {
            case R.id.spinner_brands:
                switch (position) {
                    case 0:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.acura_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, carModel), true);
                        break;
                    case 1:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.alfaRomeo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.alfaRomeo_models, carModel), true);
                        break;
                    case 2:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.anadol_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.anadol_models, carModel), true);
                        break;
                    case 3:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.astonMartin_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.astonMartin_models, carModel), true);
                        break;
                    case 4:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.audi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.audi_models, carModel), true);
                        break;
                    case 5:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.bentley_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.bentley_models, carModel), true);
                        break;
                    case 6:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.bmw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.bmw_models, carModel), true);
                        break;
                    case 7:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.bugatti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.bugatti_models, carModel), true);
                        break;
                    case 8:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.buick_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.buick_models, carModel), true);
                        break;
                    case 9:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.cadillac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.cadillac_models, carModel), true);
                        break;
                    case 10:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.cherry_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.cherry_models, carModel), true);
                        break;
                    case 11:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.chevrolet_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.chevrolet_models, carModel), true);
                        break;
                    case 12:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.chyrsler_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.chyrsler_models, carModel), true);
                        break;
                    case 13:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.citroen_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.citroen_models, carModel), true);
                        break;
                    case 14:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.dacia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.dacia_models, carModel), true);
                        break;
                    case 15:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.daeweo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.daeweo_models, carModel), true);
                        break;
                    case 16:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.daihatsu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.daihatsu_models, carModel), true);
                        break;
                    case 17:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.dodge_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.dodge_models, carModel), true);
                        break;
                    case 18:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ds_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ds_models, carModel), true);
                        break;
                    case 19:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.eagle_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.eagle_models, carModel), true);
                        break;
                    case 20:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ferrari_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ferrari_models, carModel), true);
                        break;
                    case 21:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.fiat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.fiat_models, carModel), true);
                        break;
                    case 22:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ford_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ford_models, carModel), true);
                        break;
                    case 23:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.gaz_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.gaz_models, carModel), true);
                        break;
                    case 24:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.geely_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, carModel), true);
                        break;
                    case 25:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.honda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.honda_models, carModel), true);
                        break;
                    case 26:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.hyundai_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.hyundai_models, carModel), true);
                        break;
                    case 27:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ikco_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ikco_models, carModel), true);
                        break;
                    case 28:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.infiniti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.infiniti_models, carModel), true);
                        break;
                    case 29:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.isuzu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.isuzu_models, carModel), true);
                        break;
                    case 30:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.jaguar_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.jaguar_models, carModel), true);
                        break;
                    case 31:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.kia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.kia_models, carModel), true);
                        break;
                    case 32:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.kral_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.kral_models, carModel), true);
                        break;
                    case 33:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lada_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lada_models, carModel), true);
                        break;
                    case 34:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lamborghini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lamborghini_models, carModel), true);
                        break;
                    case 35:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lancia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lancia_models, carModel), true);
                        break;
                    case 36:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lexus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lexus_models, carModel), true);
                        break;
                    case 37:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lincoln_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lincoln_models, carModel), true);
                        break;
                    case 38:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lotus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lotus_models, carModel), true);
                        break;
                    case 39:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.maserati_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.maserati_models, carModel), true);
                        break;
                    case 40:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.maybach_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.maybach_models, carModel), true);
                        break;
                    case 41:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mazda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, carModel), true);
                        break;
                    case 42:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mercedes_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mercedes_models, carModel), true);
                        break;
                    case 43:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mercury_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mercury_models, carModel), true);
                        break;
                    case 44:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mg_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mg_models, carModel), true);
                        break;
                    case 45:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mini_models, carModel), true);
                        break;
                    case 46:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mitsubishi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, carModel), true);
                        break;
                    case 47:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.moskwitsch_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.moskwitsch_models, carModel), true);
                        break;
                    case 48:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.nissan_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.nissan_models, carModel), true);
                        break;
                    case 49:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.oldsmobile_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.oldsmobile_models, carModel), true);
                        break;
                    case 50:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.opel_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.opel_models, carModel), true);
                        break;
                    case 51:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.pagani_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.pagani_models, carModel), true);
                        break;
                    case 52:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.peugeot_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.peugeot_models, carModel), true);
                        break;
                    case 53:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.plymouth_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.plymouth_models, carModel), true);
                        break;
                    case 54:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.pontiac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.pontiac_models, carModel), true);
                        break;
                    case 55:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.porsche_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.porsche_models, carModel), true);
                        break;
                    case 56:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.proton_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.proton_models, carModel), true);
                        break;
                    case 57:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.renault_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.renault_models, carModel), true);
                        break;
                    case 58:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.rollsRoyce_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.rollsRoyce_models, carModel), true);
                        break;
                    case 59:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.rover_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.rover_models, carModel), true);
                        break;
                    case 60:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.saab_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.saab_models, carModel), true);
                        break;
                    case 61:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.seat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.seat_models, carModel), true);
                        break;
                    case 62:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.skoda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.skoda_models, carModel), true);
                        break;
                    case 63:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.smart_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.smart_models, carModel), true);
                        break;
                    case 64:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.subaru_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.subaru_models, carModel), true);
                        break;
                    case 65:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.suzuki_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.suzuki_models, carModel), true);
                        break;
                    case 66:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.tata_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.tata_models, carModel), true);
                        break;
                    case 67:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.tesla_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.tesla_models, carModel), true);
                        break;
                    case 68:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.tofas_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.tofas_models, carModel), true);
                        break;
                    case 69:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.toyota_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.toyota_models, carModel), true);
                        break;
                    case 70:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.vw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.vw_models, carModel), true);
                        break;
                    case 71:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.volvo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.volvo_models, carModel), true);
                        break;
                    default:
                        break;
                }
                spinner2.setOnItemSelectedListener(this);

                carBrand = spinner.getSelectedItem().toString();
                prefs.edit().putString("carBrand", carBrand).apply();

                carModel = spinner2.getSelectedItem().toString();
                prefs.edit().putString("carModel", carModel).apply();
                break;
            case R.id.spinner_models:
                carModel = spinner2.getSelectedItem().toString();
                prefs.edit().putString("carModel", carModel).apply();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //DoNothing
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FILEPICKER: {
                if (ContextCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                userlat = String.valueOf(location.getLatitude());
                                userlon = String.valueOf(location.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();
                                Localization();
                                MainActivity.getVariables(prefs);
                            } else {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(60000);
                                mLocationRequest.setFastestInterval(5000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            }
                        }
                    });

                    if (userVehicles != null && userVehicles.length() > 0) {
                        //User re-looged in
                        Toast.makeText(WelcomeActivity.this, "Bilgileriniz kaydedildi. FuelSpot'a tekrardan hoşgeldiniz!", Toast.LENGTH_LONG).show();
                        howto3 = true;
                        MainActivity.isSigned = true;
                        prefs.edit().putBoolean("isSigned", MainActivity.isSigned).apply();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }, 2000);
                    } else {
                        loadCarSelection();
                        layout2.setVisibility(View.VISIBLE);
                        layout1.setVisibility(View.GONE);
                    }
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

        String fileName = username + "-CARPHOTO" + ".jpg";

        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    MainActivity.carPhoto = aq.get(0);

                    System.out.println("file://" + MainActivity.carPhoto);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/CarPhotos");
                    folder.mkdirs();

                    UCrop.of(Uri.parse("file://" + MainActivity.carPhoto), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(WelcomeActivity.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        Glide.with(this).load(bitmap).into(carPic);
                        prefs.edit().putString("CarPhoto", "file://" + Environment.getExternalStorageDirectory() + "/FuelSpot/CarPhotos/" + fileName).apply();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(WelcomeActivity.this, cropError.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
