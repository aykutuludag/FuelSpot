package com.fuelspot;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

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
import com.fuelspot.automobile.Brands;
import com.fuelspot.automobile.Models;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Currency;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_ALL;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.TAX_DIESEL;
import static com.fuelspot.MainActivity.TAX_ELECTRICITY;
import static com.fuelspot.MainActivity.TAX_GASOLINE;
import static com.fuelspot.MainActivity.TAX_LPG;
import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.isSigned;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.location;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;

public class WelcomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private RequestQueue requestQueue;
    private SharedPreferences prefs;
    private RelativeLayout layout1;
    private RelativeLayout layout2;
    private RelativeLayout layout4;
    private RelativeLayout layoutHow1;
    private RelativeLayout layoutHow2;
    private RelativeLayout layoutHow3;
    private ScrollView layout3;
    private Bitmap bitmap;
    private CircleImageView carPic;
    private Spinner spinner2;
    private boolean howto1;
    private boolean howto2;
    private RadioGroup radioGroup2;
    private RequestOptions options;
    private ProgressDialog loading;

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Analytics
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName("Welcome");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(WelcomeActivity.this);

        // ProgressDialogs
        loading = new ProgressDialog(WelcomeActivity.this);
        loading.setTitle(getString(R.string.registering_vehicle));
        loading.setMessage(getString(R.string.please_wait));
        loading.setIndeterminate(true);
        loading.setCancelable(false);

        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);
        layout4 = findViewById(R.id.layout4);

        layoutHow1 = findViewById(R.id.howto1);
        layoutHow2 = findViewById(R.id.howto2);
        layoutHow3 = findViewById(R.id.howto3);

        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_automobile).error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Button userHasCar = findViewById(R.id.button6);
        userHasCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCarSelection();
                layout3.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            }
        });
        Button userNoCar = findViewById(R.id.button7);
        userNoCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WelcomeActivity.this, getString(R.string.info_saved_welcome), Toast.LENGTH_LONG).show();
                layout4.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            }
        });

        Button continueButton = findViewById(R.id.buttonContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{PERMISSIONS_STORAGE[0], PERMISSIONS_STORAGE[1], PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_ALL);
                } else {
                    FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(WelcomeActivity.this);
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(WelcomeActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                userlat = String.valueOf(location.getLatitude());
                                userlon = String.valueOf(location.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();
                                Localization();
                            } else {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(5000);
                                mLocationRequest.setFastestInterval(1000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                Toast.makeText(WelcomeActivity.this, getString(R.string.location_fetching), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    fetchAutomobiles();
                }
            }
        });

        Button saveCarButton = findViewById(R.id.button4);
        saveCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plateNo != null && plateNo.length() > 0) {
                    addVehicle();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.enter_plate_no), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        Button finishButton = findViewById(R.id.button3);
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
                    isSigned = true;
                    prefs.edit().putBoolean("isSigned", isSigned).apply();
                    Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
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

                        Currency userParaSembolu = Currency.getInstance(currencyCode);
                        currencySymbol = userParaSembolu.getSymbol(userLocale);
                        prefs.edit().putString("userCurrencySymbol", currencySymbol).apply();

                        switch (userCountry) {
                            // US GALLON COUNTRIES
                            case "BZ":
                            case "CO":
                            case "DO":
                            case "EC":
                            case "GT":
                            case "HN":
                            case "HT":
                            case "LR":
                            case "MM":
                            case "NI":
                            case "PE":
                            case "US":
                            case "SV":
                                userUnit = getString(R.string.unitSystem2);
                                break;
                            // IMPERIAL GALLON COUNTRIES
                            case "AI":
                            case "AG":
                            case "BS":
                            case "DM":
                            case "GD":
                            case "KN":
                            case "KY":
                            case "LC":
                            case "MS":
                            case "VC":
                            case "VG":
                                userUnit = getString(R.string.unitSystem3);
                                break;
                            default:
                                // LITRE COUNTRIES. REST OF THE WORLD.
                                userUnit = getString(R.string.unitSystem1);
                                break;
                        }
                        prefs.edit().putString("userUnit", userUnit).apply();
                        updateUser();
                        fetchTaxRates();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                params.put("phoneNumber", userPhoneNumber);
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                } else {
                    params.put("photo", "");
                }
                params.put("gender", gender);
                params.put("birthday", birthday);
                params.put("location", location);
                params.put("country", userCountry);
                params.put("language", userDisplayLanguage);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchTaxRates() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_TAX) + "?country=" + userCountry,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                TAX_GASOLINE = (float) obj.getDouble("gasolineTax");
                                prefs.edit().putFloat("taxGasoline", TAX_GASOLINE).apply();

                                TAX_DIESEL = (float) obj.getDouble("dieselTax");
                                prefs.edit().putFloat("taxDiesel", TAX_DIESEL).apply();

                                TAX_LPG = (float) obj.getDouble("LPGTax");
                                prefs.edit().putFloat("taxLPG", TAX_LPG).apply();

                                TAX_ELECTRICITY = (float) obj.getDouble("electricityTax");
                                prefs.edit().putFloat("taxElectricity", TAX_ELECTRICITY).apply();
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

    private void fetchAutomobiles() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_USER_AUTOMOBILES) + "?username=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                if (res.length() > 0) {
                                    Toast.makeText(WelcomeActivity.this, getString(R.string.info_saved_welcome), Toast.LENGTH_LONG).show();
                                    layout4.setVisibility(View.VISIBLE);
                                    layout1.setVisibility(View.GONE);
                                } else {
                                    loadCarSelection();
                                    layout2.setVisibility(View.VISIBLE);
                                    layout1.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                loadCarSelection();
                                layout2.setVisibility(View.VISIBLE);
                                layout1.setVisibility(View.GONE);
                            }
                        } else {
                            loadCarSelection();
                            layout2.setVisibility(View.VISIBLE);
                            layout1.setVisibility(View.GONE);
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

    private void loadCarSelection() {
        //CarPic
        carPic = findViewById(R.id.imageViewCarHolder);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_automobile)
                .error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(this).load(carPhoto).apply(options).into(carPic);
        carPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyFilePickerPermission(WelcomeActivity.this)) {
                    ImagePicker.create(WelcomeActivity.this).single().start();
                } else {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        //MARKA SEÇİMİ
        Spinner spinner = findViewById(R.id.spinner_brands);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Brands.CAR_MANUFACTURERS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
        spinner.setSelection(MainActivity.getIndexOf(Brands.CAR_MANUFACTURERS, carBrand), true);

        //MODEL SEÇİMİ
        spinner2 = findViewById(R.id.spinner_models);

        //Yakıt seçenekleri
        RadioGroup radioGroup1 = findViewById(R.id.radioGroup_fuelPrimary);
        RadioButton gasoline = findViewById(R.id.gasoline);
        RadioButton diesel = findViewById(R.id.diesel);
        RadioButton lpg = findViewById(R.id.lpg);
        RadioButton elec = findViewById(R.id.electricity);
        radioGroup2 = findViewById(R.id.radioGroup_fuelSecondary);
        RadioButton gasoline2 = findViewById(R.id.gasoline2);
        RadioButton diesel2 = findViewById(R.id.diesel2);
        RadioButton lpg2 = findViewById(R.id.lpg2);
        RadioButton elec2 = findViewById(R.id.electricity2);

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
                if (s != null && s.length() > 0) {
                    kilometer = Integer.parseInt(s.toString());
                    prefs.edit().putInt("Kilometer", kilometer).apply();
                }
            }
        });

        //PlakaNO
        final EditText plateText = findViewById(R.id.editText_plate);
        plateText.setText(plateNo);
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    // Normalize
                    plateNo = s.toString().replaceAll(" ", "");
                    plateNo = plateNo.toUpperCase();
                    prefs.edit().putString("plateNo", plateNo).apply();
                }
            }
        };
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        plateText.setFilters(new InputFilter[]{filter});
        plateText.addTextChangedListener(mTextWatcher);
    }

    private void addVehicle() {
        loading.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_AUTOMOBILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "plateNo exist":
                                    Toast.makeText(WelcomeActivity.this, getString(R.string.plate_no_exist), Toast.LENGTH_LONG).show();
                                    break;
                                case "Success":
                                    layout4.setVisibility(View.VISIBLE);
                                    layout3.setVisibility(View.GONE);

                                    Toast.makeText(WelcomeActivity.this, getString(R.string.vehicle_added) + " " + plateNo, Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(WelcomeActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(WelcomeActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(WelcomeActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
                params.put("carBrand", carBrand);
                params.put("carModel", carModel);
                params.put("plateNo", plateNo);
                params.put("fuelPri", String.valueOf(fuelPri));
                params.put("kilometer", String.valueOf(kilometer));
                params.put("fuelSec", String.valueOf(fuelSec));
                if (bitmap != null) {
                    params.put("carPhoto", getStringImage(bitmap));
                } else {
                    params.put("carPhoto", "");
                }

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {
            case R.id.spinner_brands:
                switch (position) {
                    case 0:
                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.acura_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.acura_models, carModel), true);
                        break;
                    case 1:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.alfaRomeo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.alfaRomeo_models, carModel), true);
                        break;
                    case 2:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.anadol_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.anadol_models, carModel), true);
                        break;
                    case 3:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.astonMartin_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.astonMartin_models, carModel), true);
                        break;
                    case 4:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.audi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.audi_models, carModel), true);
                        break;
                    case 5:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.bentley_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.bentley_models, carModel), true);
                        break;
                    case 6:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.bmw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.bmw_models, carModel), true);
                        break;
                    case 7:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.bugatti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.bugatti_models, carModel), true);
                        break;
                    case 8:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.buick_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.buick_models, carModel), true);
                        break;
                    case 9:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.cadillac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.cadillac_models, carModel), true);
                        break;
                    case 10:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.cherry_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.cherry_models, carModel), true);
                        break;
                    case 11:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.chevrolet_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.chevrolet_models, carModel), true);
                        break;
                    case 12:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.chyrsler_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.chyrsler_models, carModel), true);
                        break;
                    case 13:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.citroen_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.citroen_models, carModel), true);
                        break;
                    case 14:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.dacia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.dacia_models, carModel), true);
                        break;
                    case 15:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.daeweo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.daeweo_models, carModel), true);
                        break;
                    case 16:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.daihatsu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.daihatsu_models, carModel), true);
                        break;
                    case 17:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.dodge_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.dodge_models, carModel), true);
                        break;
                    case 18:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ds_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ds_models, carModel), true);
                        break;
                    case 19:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.eagle_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.eagle_models, carModel), true);
                        break;
                    case 20:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ferrari_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ferrari_models, carModel), true);
                        break;
                    case 21:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.fiat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.fiat_models, carModel), true);
                        break;
                    case 22:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ford_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ford_models, carModel), true);
                        break;
                    case 23:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.gaz_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.gaz_models, carModel), true);
                        break;
                    case 24:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.geely_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.geely_models, carModel), true);
                        break;
                    case 25:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.honda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.honda_models, carModel), true);
                        break;
                    case 26:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.hyundai_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.hyundai_models, carModel), true);
                        break;
                    case 27:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ikco_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ikco_models, carModel), true);
                        break;
                    case 28:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.infiniti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.infiniti_models, carModel), true);
                        break;
                    case 29:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.isuzu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.isuzu_models, carModel), true);
                        break;
                    case 30:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.jaguar_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.jaguar_models, carModel), true);
                        break;
                    case 31:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.kia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.kia_models, carModel), true);
                        break;
                    case 32:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.kral_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.kral_models, carModel), true);
                        break;
                    case 33:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lada_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lada_models, carModel), true);
                        break;
                    case 34:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lamborghini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lamborghini_models, carModel), true);
                        break;
                    case 35:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lancia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lancia_models, carModel), true);
                        break;
                    case 36:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lexus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lexus_models, carModel), true);
                        break;
                    case 37:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lincoln_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lincoln_models, carModel), true);
                        break;
                    case 38:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lotus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lotus_models, carModel), true);
                        break;
                    case 39:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.maserati_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.maserati_models, carModel), true);
                        break;
                    case 40:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.maybach_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.maybach_models, carModel), true);
                        break;
                    case 41:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mazda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mazda_models, carModel), true);
                        break;
                    case 42:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mercedes_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mercedes_models, carModel), true);
                        break;
                    case 43:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mercury_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mercury_models, carModel), true);
                        break;
                    case 44:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mg_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mg_models, carModel), true);
                        break;
                    case 45:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mini_models, carModel), true);
                        break;
                    case 46:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mitsubishi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mitsubishi_models, carModel), true);
                        break;
                    case 47:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.moskwitsch_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.moskwitsch_models, carModel), true);
                        break;
                    case 48:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.nissan_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.nissan_models, carModel), true);
                        break;
                    case 49:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.oldsmobile_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.oldsmobile_models, carModel), true);
                        break;
                    case 50:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.opel_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.opel_models, carModel), true);
                        break;
                    case 51:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.pagani_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.pagani_models, carModel), true);
                        break;
                    case 52:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.peugeot_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.peugeot_models, carModel), true);
                        break;
                    case 53:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.plymouth_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.plymouth_models, carModel), true);
                        break;
                    case 54:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.pontiac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.pontiac_models, carModel), true);
                        break;
                    case 55:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.porsche_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.porsche_models, carModel), true);
                        break;
                    case 56:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.proton_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.proton_models, carModel), true);
                        break;
                    case 57:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.renault_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.renault_models, carModel), true);
                        break;
                    case 58:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.rollsRoyce_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.rollsRoyce_models, carModel), true);
                        break;
                    case 59:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.rover_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.rover_models, carModel), true);
                        break;
                    case 60:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.saab_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.saab_models, carModel), true);
                        break;
                    case 61:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.seat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.seat_models, carModel), true);
                        break;
                    case 62:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.skoda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.skoda_models, carModel), true);
                        break;
                    case 63:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.smart_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.smart_models, carModel), true);
                        break;
                    case 64:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.subaru_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.subaru_models, carModel), true);
                        break;
                    case 65:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.suzuki_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.suzuki_models, carModel), true);
                        break;
                    case 66:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.tata_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.tata_models, carModel), true);
                        break;
                    case 67:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.tesla_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.tesla_models, carModel), true);
                        break;
                    case 68:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.tofas_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.tofas_models, carModel), true);
                        break;
                    case 69:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.toyota_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.toyota_models, carModel), true);
                        break;
                    case 70:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.vw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.vw_models, carModel), true);
                        break;
                    case 71:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.volvo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.volvo_models, carModel), true);
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
        carBrand = "Acura";
        carModel = "RSX";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ALL: {
                if (ContextCompat.checkSelfPermission(WelcomeActivity.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(WelcomeActivity.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
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
                            } else {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(5000);
                                mLocationRequest.setFastestInterval(1000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                Toast.makeText(WelcomeActivity.this, getString(R.string.location_fetching), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    fetchAutomobiles();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_STORAGE: {
                if (ActivityCompat.checkSelfPermission(WelcomeActivity.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.create(WelcomeActivity.this).single().start();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
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
                    Glide.with(this).load(bitmap).apply(options).into(carPic);
                    carPhoto = "https://fuelspot.com.tr/uploads/automobiles/" + username + "-" + plateNo + ".jpg";
                    prefs.edit().putString("CarPhoto", carPhoto).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);

        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public Bitmap resizeAndRotate(Bitmap bmp, float degrees) {
        if (bmp.getWidth() > 1080 || bmp.getHeight() > 1920) {
            float aspectRatio = (float) bmp.getWidth() / bmp.getHeight();
            int width, height;

            if (aspectRatio < 1) {
                // Portrait
                width = (int) (aspectRatio * 1920);
                height = (int) (width * (1f / aspectRatio));
            } else {
                // Landscape
                width = (int) (aspectRatio * 1080);
                height = (int) (width * (1f / aspectRatio));
            }

            bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        if (degrees != 0) {
            return rotate(bmp, degrees);
        } else {
            return bmp;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
