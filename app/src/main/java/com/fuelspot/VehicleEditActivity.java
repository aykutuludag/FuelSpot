package com.fuelspot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.userVehicles;
import static com.fuelspot.MainActivity.vehicleID;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;


public class VehicleEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Bitmap bitmap;
    CircleImageView carPic;
    Spinner spinner, spinner2;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    RadioButton gasoline, diesel, lpg, elec, gasoline2, diesel2, lpg2, elec2;
    Window window;
    Toolbar toolbar;
    ArrayAdapter<CharSequence> adapter;
    ArrayAdapter<String> adapter2;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_edit);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Araç düzenle");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#000000"), Color.parseColor("#ffffff"));

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);
        editor = prefs.edit();
        MainActivity.getVariables(prefs);

        //CarPic
        carPic = findViewById(R.id.imageViewCar);
        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.default_automobile).error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);
        Glide.with(this).load(MainActivity.carPhoto).apply(options).into(carPic);
        carPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(VehicleEditActivity.this)) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(VehicleEditActivity.this);
                } else {
                    ActivityCompat.requestPermissions(VehicleEditActivity.this, MainActivity.PERMISSIONS_FILEPICKER, MainActivity.REQUEST_FILEPICKER);
                }
            }
        });

        //MARKA SEÇİMİ
        spinner = findViewById(R.id.spinner_brands);
        adapter = ArrayAdapter.createFromResource(this, R.array.CAR_PRODUCER, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
        String[] foo_array = this.getResources().getStringArray(R.array.CAR_PRODUCER);
        spinner.setSelection(MainActivity.getIndexOf(foo_array, MainActivity.carBrand), true);

        //MODEL SEÇİMİ
        spinner2 = findViewById(R.id.spinner_models);

        //Yakıt seçenekleri
        gasoline = findViewById(R.id.gasoline);
        diesel = findViewById(R.id.diesel);
        lpg = findViewById(R.id.lpg);
        elec = findViewById(R.id.electricity);
        gasoline2 = findViewById(R.id.gasoline2);
        diesel2 = findViewById(R.id.diesel2);
        lpg2 = findViewById(R.id.lpg2);
        elec2 = findViewById(R.id.electricity2);

        switch (MainActivity.fuelPri) {
            case 0:
                gasoline.setChecked(true);

                gasoline2.setEnabled(false);
                diesel2.setEnabled(false);
                lpg2.setEnabled(true);
                elec2.setEnabled(true);
                break;
            case 1:
                diesel.setChecked(true);

                gasoline2.setEnabled(false);
                diesel2.setEnabled(false);
                lpg2.setEnabled(false);
                elec2.setEnabled(true);
                break;
            case 2:
                lpg.setChecked(true);

                gasoline2.setEnabled(true);
                lpg2.setEnabled(false);
                diesel2.setEnabled(false);
                elec2.setEnabled(false);
                break;
            case 3:
                elec.setChecked(true);

                lpg2.setEnabled(false);
                elec2.setEnabled(false);
                gasoline2.setEnabled(true);
                diesel2.setEnabled(true);
                break;
            default:
                break;
        }

        switch (MainActivity.fuelSec) {
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
                break;
        }

        //1. yakıt
        RadioGroup radioGroup1 = findViewById(R.id.radioGroup_fuelPrimary);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.gasoline) {
                    MainActivity.fuelPri = 0;
                    gasoline2.setEnabled(false);
                    diesel2.setEnabled(false);
                    lpg2.setEnabled(true);
                    elec2.setEnabled(true);
                } else if (checkedId == R.id.diesel) {
                    MainActivity.fuelPri = 1;
                    gasoline2.setEnabled(false);
                    diesel2.setEnabled(false);
                    lpg2.setEnabled(false);
                    elec2.setEnabled(true);
                } else if (checkedId == R.id.lpg) {
                    MainActivity.fuelPri = 2;
                    gasoline2.setEnabled(true);
                    lpg2.setEnabled(false);
                    diesel2.setEnabled(false);
                    elec2.setEnabled(false);
                } else {
                    MainActivity.fuelPri = 3;
                    lpg2.setEnabled(false);
                    elec2.setEnabled(false);
                    gasoline2.setEnabled(true);
                    diesel2.setEnabled(true);
                }

                gasoline.setSelected(false);
                diesel2.setSelected(false);
                lpg2.setSelected(false);
                elec2.setSelected(false);
                MainActivity.fuelSec = -1;

                editor.putInt("FuelPrimary", MainActivity.fuelPri);
                editor.putInt("FuelSecondary", MainActivity.fuelSec);
            }
        });

        //2. yakıt
        RadioGroup radioGroup2 = findViewById(R.id.radioGroup_fuelSecondary);
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.gasoline2) {
                    MainActivity.fuelSec = 0;
                } else if (checkedId == R.id.diesel2) {
                    MainActivity.fuelSec = 1;
                } else if (checkedId == R.id.lpg2) {
                    MainActivity.fuelSec = 2;
                } else if (checkedId == R.id.electricity2) {
                    MainActivity.fuelSec = 3;
                }
                editor.putInt("FuelSecondary", MainActivity.fuelSec);
            }
        });

        //Kilometre
        EditText eText = findViewById(R.id.editText_km);
        eText.setText(String.valueOf(MainActivity.kilometer));
        eText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 1) {
                    MainActivity.kilometer = Integer.parseInt(s.toString());
                    editor.putInt("Kilometer", MainActivity.kilometer);
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

                    editor.putString("plateNo", plateNo);
                }
            }
        });

        final int vehicleNumber = userVehicles.split(";").length;
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (vehicleNumber > 1) {
                    Snackbar.make(view, "Araç silinecek?", Snackbar.LENGTH_LONG)
                            .setAction("SİL", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteVehicle();
                                }
                            }).show();
                } else {
                    Snackbar.make(view, "Araç silebilmek için en az 2 aracınız olmalıdır.", Snackbar.LENGTH_LONG).show();
                }


            }
        });
    }

    private void updateVehicle() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(VehicleEditActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_VEHICLE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(VehicleEditActivity.this, s, Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Toast.makeText(VehicleEditActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("carBrand", carBrand);
                params.put("carModel", carModel);
                params.put("fuelPri", String.valueOf(fuelPri));
                params.put("fuelSec", String.valueOf(fuelSec));
                params.put("km", String.valueOf(kilometer));
                if (bitmap != null) {
                    params.put("carPhoto", getStringImage(bitmap));
                }
                params.put("plate", plateNo);
                params.put("avgCons", String.valueOf(averageCons));
                params.put("carbonEmission", String.valueOf(carbonEmission));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void deleteVehicle() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.getString(R.string.API_DELETE_VEHICLE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userVehicles = userVehicles.replaceAll(vehicleID + ";", "");
                        prefs.edit().putString("userVehicles", userVehicles).apply();
                        Toast.makeText(VehicleEditActivity.this, response, Toast.LENGTH_LONG).show();
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
                params.put("vehicleID", String.valueOf(vehicleID));

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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {
            case R.id.spinner_brands:
                switch (position) {
                    case 0:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.acura_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, MainActivity.carModel), true);
                        break;
                    case 1:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.alfaRomeo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.alfaRomeo_models, MainActivity.carModel), true);
                        break;
                    case 2:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.anadol_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.anadol_models, MainActivity.carModel), true);
                        break;
                    case 3:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.astonMartin_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.astonMartin_models, MainActivity.carModel), true);
                        break;
                    case 4:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.audi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.audi_models, MainActivity.carModel), true);
                        break;
                    case 5:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.bentley_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.bentley_models, MainActivity.carModel), true);
                        break;
                    case 6:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.bmw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.bmw_models, MainActivity.carModel), true);
                        break;
                    case 7:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.bugatti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.bugatti_models, MainActivity.carModel), true);
                        break;
                    case 8:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.buick_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.buick_models, MainActivity.carModel), true);
                        break;
                    case 9:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.cadillac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.cadillac_models, MainActivity.carModel), true);
                        break;
                    case 10:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.cherry_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.cherry_models, MainActivity.carModel), true);
                        break;
                    case 11:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.chevrolet_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.chevrolet_models, MainActivity.carModel), true);
                        break;
                    case 12:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.chyrsler_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.chyrsler_models, MainActivity.carModel), true);
                        break;
                    case 13:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.citroen_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.citroen_models, MainActivity.carModel), true);
                        break;
                    case 14:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.dacia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.dacia_models, MainActivity.carModel), true);
                        break;
                    case 15:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.daeweo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.daeweo_models, MainActivity.carModel), true);
                        break;
                    case 16:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.daihatsu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.daihatsu_models, MainActivity.carModel), true);
                        break;
                    case 17:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.dodge_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.dodge_models, MainActivity.carModel), true);
                        break;
                    case 18:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ds_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ds_models, MainActivity.carModel), true);
                        break;
                    case 19:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.eagle_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.eagle_models, MainActivity.carModel), true);
                        break;
                    case 20:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ferrari_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ferrari_models, MainActivity.carModel), true);
                        break;
                    case 21:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.fiat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.fiat_models, MainActivity.carModel), true);
                        break;
                    case 22:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ford_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ford_models, MainActivity.carModel), true);
                        break;
                    case 23:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.gaz_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.gaz_models, MainActivity.carModel), true);
                        break;
                    case 24:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.geely_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, MainActivity.carModel), true);
                        break;
                    case 25:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.honda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.honda_models, MainActivity.carModel), true);
                        break;
                    case 26:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.hyundai_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.hyundai_models, MainActivity.carModel), true);
                        break;
                    case 27:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.ikco_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.ikco_models, MainActivity.carModel), true);
                        break;
                    case 28:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.infiniti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.infiniti_models, MainActivity.carModel), true);
                        break;
                    case 29:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.isuzu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.isuzu_models, MainActivity.carModel), true);
                        break;
                    case 30:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.jaguar_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.jaguar_models, MainActivity.carModel), true);
                        break;
                    case 31:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.kia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.kia_models, MainActivity.carModel), true);
                        break;
                    case 32:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.kral_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.kral_models, MainActivity.carModel), true);
                        break;
                    case 33:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lada_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lada_models, MainActivity.carModel), true);
                        break;
                    case 34:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lamborghini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lamborghini_models, MainActivity.carModel), true);
                        break;
                    case 35:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lancia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lancia_models, MainActivity.carModel), true);
                        break;
                    case 36:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lexus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lexus_models, MainActivity.carModel), true);
                        break;
                    case 37:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lincoln_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lincoln_models, MainActivity.carModel), true);
                        break;
                    case 38:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.lotus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.lotus_models, MainActivity.carModel), true);
                        break;
                    case 39:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.maserati_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.maserati_models, MainActivity.carModel), true);
                        break;
                    case 40:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.maybach_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.maybach_models, MainActivity.carModel), true);
                        break;
                    case 41:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mazda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, MainActivity.carModel), true);
                        break;
                    case 42:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mercedes_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mercedes_models, MainActivity.carModel), true);
                        break;
                    case 43:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mercury_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mercury_models, MainActivity.carModel), true);
                        break;
                    case 44:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mg_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mg_models, MainActivity.carModel), true);
                        break;
                    case 45:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.mini_models, MainActivity.carModel), true);
                        break;
                    case 46:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.mitsubishi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.acura_models, MainActivity.carModel), true);
                        break;
                    case 47:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.moskwitsch_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.moskwitsch_models, MainActivity.carModel), true);
                        break;
                    case 48:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.nissan_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.nissan_models, MainActivity.carModel), true);
                        break;
                    case 49:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.oldsmobile_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.oldsmobile_models, MainActivity.carModel), true);
                        break;
                    case 50:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.opel_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.opel_models, MainActivity.carModel), true);
                        break;
                    case 51:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.pagani_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.pagani_models, MainActivity.carModel), true);
                        break;
                    case 52:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.peugeot_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.peugeot_models, MainActivity.carModel), true);
                        break;
                    case 53:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.plymouth_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.plymouth_models, MainActivity.carModel), true);
                        break;
                    case 54:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.pontiac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.pontiac_models, MainActivity.carModel), true);
                        break;
                    case 55:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.porsche_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.porsche_models, MainActivity.carModel), true);
                        break;
                    case 56:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.proton_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.proton_models, MainActivity.carModel), true);
                        break;
                    case 57:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.renault_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.renault_models, MainActivity.carModel), true);
                        break;
                    case 58:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.rollsRoyce_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.rollsRoyce_models, MainActivity.carModel), true);
                        break;
                    case 59:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.rover_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.rover_models, MainActivity.carModel), true);
                        break;
                    case 60:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.saab_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.saab_models, MainActivity.carModel), true);
                        break;
                    case 61:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.seat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.seat_models, MainActivity.carModel), true);
                        break;
                    case 62:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.skoda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.skoda_models, MainActivity.carModel), true);
                        break;
                    case 63:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.smart_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.smart_models, MainActivity.carModel), true);
                        break;
                    case 64:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.subaru_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.subaru_models, MainActivity.carModel), true);
                        break;
                    case 65:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.suzuki_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.suzuki_models, MainActivity.carModel), true);
                        break;
                    case 66:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.tata_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.tata_models, MainActivity.carModel), true);
                        break;
                    case 67:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.tesla_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.tesla_models, MainActivity.carModel), true);
                        break;
                    case 68:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.tofas_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.tofas_models, MainActivity.carModel), true);
                        break;
                    case 69:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.toyota_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.toyota_models, MainActivity.carModel), true);
                        break;
                    case 70:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.vw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.vw_models, MainActivity.carModel), true);
                        break;
                    case 71:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.volvo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(MainActivity.volvo_models, MainActivity.carModel), true);
                        break;
                    default:
                        break;
                }
                spinner2.setOnItemSelectedListener(this);

                carBrand = spinner.getSelectedItem().toString();
                editor.putString("carBrand", carBrand);

                carModel = spinner2.getSelectedItem().toString();
                editor.putString("carModel", carModel);
                break;
            case R.id.spinner_models:
                carModel = spinner2.getSelectedItem().toString();
                editor.putString("carModel", carModel);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.navigation_save:
                editor.apply();
                if (isNetworkConnected(VehicleEditActivity.this)) {
                    updateVehicle();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Internet bağlantısında bir sorun var", Snackbar.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.REQUEST_FILEPICKER: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(VehicleEditActivity.this);
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

        CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
        String fileName = now + ".jpg";

        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    MainActivity.carPhoto = aq.get(0);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/CarPhotos");
                    folder.mkdirs();

                    UCrop.of(Uri.parse("file://" + MainActivity.carPhoto), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(VehicleEditActivity.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        carPic.setImageBitmap(bitmap);
                        editor.putString("CarPhoto", Environment.getExternalStorageDirectory() + "/FuelSpot/CarPhotos/" + now + ".jpg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(VehicleEditActivity.this, cropError.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}