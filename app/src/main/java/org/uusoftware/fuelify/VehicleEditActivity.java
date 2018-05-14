package org.uusoftware.fuelify;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.squareup.picasso.Picasso;
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

import static org.uusoftware.fuelify.MainActivity.PERMISSIONS_STORAGE;
import static org.uusoftware.fuelify.MainActivity.REQUEST_EXTERNAL_STORAGE;
import static org.uusoftware.fuelify.MainActivity.acura_models;
import static org.uusoftware.fuelify.MainActivity.alfaRomeo_models;
import static org.uusoftware.fuelify.MainActivity.anadol_models;
import static org.uusoftware.fuelify.MainActivity.astonMartin_models;
import static org.uusoftware.fuelify.MainActivity.audi_models;
import static org.uusoftware.fuelify.MainActivity.bentley_models;
import static org.uusoftware.fuelify.MainActivity.bmw_models;
import static org.uusoftware.fuelify.MainActivity.bugatti_models;
import static org.uusoftware.fuelify.MainActivity.buick_models;
import static org.uusoftware.fuelify.MainActivity.cadillac_models;
import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.cherry_models;
import static org.uusoftware.fuelify.MainActivity.chevrolet_models;
import static org.uusoftware.fuelify.MainActivity.chyrsler_models;
import static org.uusoftware.fuelify.MainActivity.citroen_models;
import static org.uusoftware.fuelify.MainActivity.dacia_models;
import static org.uusoftware.fuelify.MainActivity.daeweo_models;
import static org.uusoftware.fuelify.MainActivity.daihatsu_models;
import static org.uusoftware.fuelify.MainActivity.dodge_models;
import static org.uusoftware.fuelify.MainActivity.ds_models;
import static org.uusoftware.fuelify.MainActivity.eagle_models;
import static org.uusoftware.fuelify.MainActivity.ferrari_models;
import static org.uusoftware.fuelify.MainActivity.fiat_models;
import static org.uusoftware.fuelify.MainActivity.ford_models;
import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.gaz_models;
import static org.uusoftware.fuelify.MainActivity.geely_models;
import static org.uusoftware.fuelify.MainActivity.getVariables;
import static org.uusoftware.fuelify.MainActivity.honda_models;
import static org.uusoftware.fuelify.MainActivity.hyundai_models;
import static org.uusoftware.fuelify.MainActivity.ikco_models;
import static org.uusoftware.fuelify.MainActivity.infiniti_models;
import static org.uusoftware.fuelify.MainActivity.isNetworkConnected;
import static org.uusoftware.fuelify.MainActivity.isuzu_models;
import static org.uusoftware.fuelify.MainActivity.jaguar_models;
import static org.uusoftware.fuelify.MainActivity.kia_models;
import static org.uusoftware.fuelify.MainActivity.kilometer;
import static org.uusoftware.fuelify.MainActivity.kral_models;
import static org.uusoftware.fuelify.MainActivity.lada_models;
import static org.uusoftware.fuelify.MainActivity.lamborghini_models;
import static org.uusoftware.fuelify.MainActivity.lancia_models;
import static org.uusoftware.fuelify.MainActivity.lexus_models;
import static org.uusoftware.fuelify.MainActivity.lincoln_models;
import static org.uusoftware.fuelify.MainActivity.lotus_models;
import static org.uusoftware.fuelify.MainActivity.maserati_models;
import static org.uusoftware.fuelify.MainActivity.maybach_models;
import static org.uusoftware.fuelify.MainActivity.mazda_models;
import static org.uusoftware.fuelify.MainActivity.mercedes_models;
import static org.uusoftware.fuelify.MainActivity.mercury_models;
import static org.uusoftware.fuelify.MainActivity.mg_models;
import static org.uusoftware.fuelify.MainActivity.mini_models;
import static org.uusoftware.fuelify.MainActivity.mitsubishi_models;
import static org.uusoftware.fuelify.MainActivity.moskwitsch_models;
import static org.uusoftware.fuelify.MainActivity.nissan_models;
import static org.uusoftware.fuelify.MainActivity.oldsmobile_models;
import static org.uusoftware.fuelify.MainActivity.opel_models;
import static org.uusoftware.fuelify.MainActivity.pagani_models;
import static org.uusoftware.fuelify.MainActivity.peugeot_models;
import static org.uusoftware.fuelify.MainActivity.plymouth_models;
import static org.uusoftware.fuelify.MainActivity.pontiac_models;
import static org.uusoftware.fuelify.MainActivity.porsche_models;
import static org.uusoftware.fuelify.MainActivity.pos;
import static org.uusoftware.fuelify.MainActivity.pos2;
import static org.uusoftware.fuelify.MainActivity.proton_models;
import static org.uusoftware.fuelify.MainActivity.renault_models;
import static org.uusoftware.fuelify.MainActivity.rollsRoyce_models;
import static org.uusoftware.fuelify.MainActivity.rover_models;
import static org.uusoftware.fuelify.MainActivity.saab_models;
import static org.uusoftware.fuelify.MainActivity.seat_models;
import static org.uusoftware.fuelify.MainActivity.skoda_models;
import static org.uusoftware.fuelify.MainActivity.smart_models;
import static org.uusoftware.fuelify.MainActivity.subaru_models;
import static org.uusoftware.fuelify.MainActivity.suzuki_models;
import static org.uusoftware.fuelify.MainActivity.tata_models;
import static org.uusoftware.fuelify.MainActivity.tesla_models;
import static org.uusoftware.fuelify.MainActivity.tofas_models;
import static org.uusoftware.fuelify.MainActivity.toyota_models;
import static org.uusoftware.fuelify.MainActivity.username;
import static org.uusoftware.fuelify.MainActivity.volvo_models;
import static org.uusoftware.fuelify.MainActivity.vw_models;


public class VehicleEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Bitmap bitmap;
    CircleImageView carPic;
    Spinner spinner, spinner2;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    RadioButton gasoline, diesel, lpg, elec, gasoline2, diesel2, lpg2, elec2;
    Window window;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_selection);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.brand_logo);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#000000"), Color.parseColor("#ffffff"));

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        editor = prefs.edit();
        getVariables(prefs);

        //CarPic
        carPic = findViewById(R.id.imageViewCar);
        Picasso.with(VehicleEditActivity.this).load(Uri.parse(carPhoto)).error(R.drawable.empty).placeholder(R.drawable.empty)
                .into(carPic);
        carPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyStoragePermissions()) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(VehicleEditActivity.this);
                    Toast.makeText(VehicleEditActivity.this, "izin var", Toast.LENGTH_LONG).show();
                } else {
                    ActivityCompat.requestPermissions(VehicleEditActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                    Toast.makeText(VehicleEditActivity.this, "izin yok", Toast.LENGTH_LONG).show();
                }
            }
        });

        //MARKA SEÇİMİ
        spinner = findViewById(R.id.spinner_brands);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.car_makers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(pos, true);

        //MODEL
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

        //1. yakıt
        RadioGroup radioGroup1 = findViewById(R.id.radioGroup_fuelPrimary);
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.gasoline) {
                    fuelPri = 0;
                    gasoline2.setEnabled(false);
                    diesel2.setEnabled(false);
                    lpg2.setEnabled(true);
                    elec2.setEnabled(true);
                } else if (checkedId == R.id.diesel) {
                    fuelPri = 1;
                    gasoline2.setEnabled(false);
                    diesel2.setEnabled(false);
                    lpg2.setEnabled(false);
                    elec2.setEnabled(true);
                } else if (checkedId == R.id.lpg) {
                    fuelPri = 2;
                    gasoline2.setEnabled(true);
                    lpg2.setEnabled(false);
                    diesel2.setEnabled(false);
                    elec2.setEnabled(false);
                } else {
                    fuelPri = 3;
                    lpg2.setEnabled(false);
                    elec2.setEnabled(false);
                    gasoline2.setEnabled(true);
                    diesel2.setEnabled(true);
                }

                gasoline.setSelected(false);
                diesel2.setSelected(false);
                lpg2.setSelected(false);
                elec2.setSelected(false);
                fuelSec = -1;

                prefs.edit().putInt("FuelPrimary", fuelPri).apply();
                prefs.edit().putInt("FuelSecondary", fuelSec).apply();
            }
        });

        //2. yakıt
        RadioGroup radioGroup2 = findViewById(R.id.radioGroup_fuelSecondary);
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

        switch (fuelPri) {
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
                break;
        }

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
                if (s.length() >= 1) {
                    kilometer = Integer.parseInt(s.toString());
                    editor.putInt("Kilometer", kilometer);
                }
            }
        });
    }

    private void saveUserInfo() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(VehicleEditActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_CAR),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(VehicleEditActivity.this, s, Toast.LENGTH_LONG).show();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
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
                params.put("username", username);
                params.put("carBrand", carBrand);
                params.put("carModel", carModel);
                params.put("fuelPri", String.valueOf(fuelPri));
                params.put("fuelSec", String.valueOf(fuelSec));
                params.put("km", String.valueOf(kilometer));
                if (bitmap != null) {
                    params.put("carPhoto", getStringImage(bitmap));
                } else {
                    params.put("carPhoto", "http://fuel-spot.com/FUELSPOTAPP/uploads/" + username + "-CARPHOTO.jpeg");
                }
                params.put("posIn1", String.valueOf(pos));
                params.put("posIn2", String.valueOf(pos2));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(VehicleEditActivity.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public boolean verifyStoragePermissions() {
        boolean hasStorage = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                hasStorage = true;
            }
        } else {
            hasStorage = true;
        }
        return hasStorage;
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
                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, acura_models);
                switch (position) {
                    case 0:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, acura_models);
                        break;
                    case 1:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, alfaRomeo_models);
                        break;
                    case 2:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, anadol_models);
                        break;
                    case 3:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, astonMartin_models);
                        break;
                    case 4:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, audi_models);
                        break;
                    case 5:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bentley_models);
                        break;
                    case 6:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bmw_models);
                        break;
                    case 7:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bugatti_models);
                        break;
                    case 8:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, buick_models);
                        break;
                    case 9:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cadillac_models);
                        break;
                    case 10:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cherry_models);
                        break;
                    case 11:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chevrolet_models);
                        break;
                    case 12:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chyrsler_models);
                        break;
                    case 13:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citroen_models);
                        break;
                    case 14:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dacia_models);
                        break;
                    case 15:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daeweo_models);
                        break;
                    case 16:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daihatsu_models);
                        break;
                    case 17:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dodge_models);
                        break;
                    case 18:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ds_models);
                        break;
                    case 19:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eagle_models);
                        break;
                    case 20:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ferrari_models);
                        break;
                    case 21:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fiat_models);
                        break;
                    case 22:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ford_models);
                        break;
                    case 23:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gaz_models);
                        break;
                    case 24:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, geely_models);
                        break;
                    case 25:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, honda_models);
                        break;
                    case 26:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hyundai_models);
                        break;
                    case 27:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ikco_models);
                        break;
                    case 28:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, infiniti_models);
                        break;
                    case 29:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, isuzu_models);
                        break;
                    case 30:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jaguar_models);
                        break;
                    case 31:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kia_models);
                        break;
                    case 32:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kral_models);
                        break;
                    case 33:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lada_models);
                        break;
                    case 34:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lamborghini_models);
                        break;
                    case 35:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lancia_models);
                        break;
                    case 36:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lexus_models);
                        break;
                    case 37:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lincoln_models);
                        break;
                    case 38:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lotus_models);
                        break;
                    case 39:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, maserati_models);
                        break;
                    case 40:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, maybach_models);
                        break;
                    case 41:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mazda_models);
                        break;
                    case 42:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mercedes_models);
                        break;
                    case 43:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mercury_models);
                        break;
                    case 44:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mg_models);
                        break;
                    case 45:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mini_models);
                        break;
                    case 46:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mitsubishi_models);
                        break;
                    case 47:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moskwitsch_models);
                        break;
                    case 48:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nissan_models);
                        break;
                    case 49:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, oldsmobile_models);
                        break;
                    case 50:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opel_models);
                        break;
                    case 51:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pagani_models);
                        break;
                    case 52:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, peugeot_models);
                        break;
                    case 53:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plymouth_models);
                        break;
                    case 54:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pontiac_models);
                        break;
                    case 55:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, porsche_models);
                        break;
                    case 56:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, proton_models);
                        break;
                    case 57:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, renault_models);
                        break;
                    case 58:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rollsRoyce_models);
                        break;
                    case 59:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rover_models);
                        break;
                    case 60:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, saab_models);
                        break;
                    case 61:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seat_models);
                        break;
                    case 62:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, skoda_models);
                        break;
                    case 63:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, smart_models);
                        break;
                    case 64:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subaru_models);
                        break;
                    case 65:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, suzuki_models);
                        break;
                    case 66:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tata_models);
                        break;
                    case 67:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tesla_models);
                        break;
                    case 68:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tofas_models);
                        break;
                    case 69:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, toyota_models);
                        break;
                    case 70:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vw_models);
                        break;
                    case 71:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, volvo_models);
                        break;
                    default:
                        break;
                }

                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(adapter2);
                spinner2.setOnItemSelectedListener(this);
                spinner2.setSelection(pos2, true);

                carBrand = spinner.getSelectedItem().toString();
                pos = spinner.getSelectedItemPosition();
                prefs.edit().putString("carBrand", carBrand).apply();
                prefs.edit().putInt("carPos", pos).apply();
                break;
            case R.id.spinner_models:
                carModel = spinner2.getSelectedItem().toString();
                pos2 = spinner2.getSelectedItemPosition();
                prefs.edit().putString("carModel", carModel).apply();
                prefs.edit().putInt("carPos2", pos2).apply();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do nothing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_edit, menu);
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
                    saveUserInfo();
                } else {
                    Toast.makeText(VehicleEditActivity.this, "Internet bağlantısında bir sorun var", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(VehicleEditActivity.this, "Settings saved...", Toast.LENGTH_SHORT).show();
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
                    carPhoto = aq.get(0);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/CarPhotos");
                    folder.mkdirs();

                    UCrop.of(Uri.parse("file://" + carPhoto), Uri.fromFile(new File(folder, fileName)))
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
                        editor.putString("CarPhoto", "file://" + Environment.getExternalStorageDirectory() + "/FuelSpot/CarPhotos/" + fileName);
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