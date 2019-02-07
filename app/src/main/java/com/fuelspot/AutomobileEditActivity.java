package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
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
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fuelspot.automobile.Brands;
import com.fuelspot.automobile.Models;
import com.fuelspot.model.VehicleItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.vehicleID;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;


public class AutomobileEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Bitmap bitmap;
    private CircleImageView carPic;
    private Spinner spinner;
    private Spinner spinner2;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private RadioButton gasoline;
    private RadioButton diesel;
    private RadioButton lpg;
    private RadioButton elec;
    private RadioButton gasoline2;
    private RadioButton diesel2;
    private RadioButton lpg2;
    private RadioButton elec2;
    private Window window;
    private Toolbar toolbar;
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapter2;
    private RequestQueue requestQueue;
    private RequestOptions options;
    private TextWatcher mTextWatcher;
    private ProgressDialog loadingUpdate;
    private ProgressDialog loadingDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automobile_edit);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Analytics
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName("Araç düzenle");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#000000"), Color.parseColor("#ffffff"));

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);
        editor = prefs.edit();
        getVariables(prefs);

        // ProgressDialogs
        loadingUpdate = new ProgressDialog(AutomobileEditActivity.this);
        loadingUpdate.setTitle(getString(R.string.vehicle_updating));
        loadingUpdate.setMessage(getString(R.string.please_wait));
        loadingUpdate.setIndeterminate(true);
        loadingUpdate.setCancelable(false);

        loadingDelete = new ProgressDialog(AutomobileEditActivity.this);
        loadingDelete.setTitle(getString(R.string.vehicle_deleting));
        loadingDelete.setMessage(getString(R.string.please_wait));
        loadingDelete.setIndeterminate(true);
        loadingDelete.setCancelable(false);

        //CarPic
        carPic = findViewById(R.id.imageViewCar);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_automobile).error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);
        Glide.with(this).load(carPhoto).apply(options).into(carPic);
        carPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(AutomobileEditActivity.this)) {
                    ImagePicker.create(AutomobileEditActivity.this).single().start();
                } else {
                    ActivityCompat.requestPermissions(AutomobileEditActivity.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        //MARKA SEÇİMİ
        spinner = findViewById(R.id.spinner_brands);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Brands.CAR_MANUFACTURERS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
        spinner.setSelection(MainActivity.getIndexOf(Brands.CAR_MANUFACTURERS, MainActivity.carBrand), true);

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
        RadioGroup radioGroup1 = findViewById(R.id.radioGroup_fuelPrimary);
        final RadioGroup radioGroup2 = findViewById(R.id.radioGroup_fuelSecondary);

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

                editor.putInt("FuelPrimary", fuelPri);
                editor.putInt("FuelSecondary", fuelSec);
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

                editor.putInt("FuelSecondary", fuelSec);
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
        mTextWatcher = new TextWatcher() {
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

        final int vehicleNumber = userAutomobileList.size();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (vehicleNumber > 1) {
                    Snackbar.make(view, getString(R.string.remove_vehicle), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.delete), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteVehicle();
                                }
                            }).show();
                } else {
                    Snackbar.make(view, getString(R.string.at_least_2_vehicle), Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }

    private void updateVehicle() {
        loadingUpdate.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_AUTOMOBILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingUpdate.dismiss();

                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    Toast.makeText(AutomobileEditActivity.this, getString(R.string.vehicle_update_success), Toast.LENGTH_LONG).show();
                                    finish();
                                    break;
                                case "Fail":
                                    Toast.makeText(AutomobileEditActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(AutomobileEditActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loadingUpdate.dismiss();
                        Toast.makeText(AutomobileEditActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
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
                params.put("kilometer", String.valueOf(kilometer));
                if (bitmap != null) {
                    params.put("carPhoto", getStringImage(bitmap));
                }
                params.put("plate", plateNo);
                params.put("avgCons", String.valueOf(averageCons));
                params.put("carbonEmission", String.valueOf(carbonEmission));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void deleteVehicle() {
        loadingDelete.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.getString(R.string.API_DELETE_AUTOMOBILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDelete.dismiss();
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    //set VehicleID = 0 because user delete this car. Choose another one.
                                    vehicleID = 0;
                                    Toast.makeText(AutomobileEditActivity.this, getString(R.string.vehicle_delete_success), Toast.LENGTH_LONG).show();
                                    fetchAutomobiles();
                                    break;
                                default:
                                    Toast.makeText(AutomobileEditActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(AutomobileEditActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDelete.dismiss();
                        Toast.makeText(AutomobileEditActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchAutomobiles() {
        userAutomobileList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_USER_AUTOMOBILES),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    VehicleItem item = new VehicleItem();
                                    item.setID(obj.getInt("id"));
                                    item.setVehicleBrand(obj.getString("car_brand"));
                                    item.setVehicleModel(obj.getString("car_model"));
                                    item.setVehicleFuelPri(obj.getInt("fuelPri"));
                                    item.setVehicleFuelSec(obj.getInt("fuelSec"));
                                    item.setVehicleKilometer(obj.getInt("kilometer"));
                                    item.setVehiclePhoto(obj.getString("carPhoto"));
                                    item.setVehiclePlateNo(obj.getString("plateNo"));
                                    item.setVehicleConsumption((float) obj.getDouble("avgConsumption"));
                                    item.setVehicleEmission(obj.getInt("carbonEmission"));
                                    userAutomobileList.add(item);

                                    // If there is any selected auto, choose first one.
                                    if (vehicleID == 0) {
                                        chooseVehicle(item);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        finish();
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
                params.put("username", username);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void chooseVehicle(VehicleItem item) {
        vehicleID = item.getID();
        prefs.edit().putInt("vehicleID", vehicleID).apply();

        carBrand = item.getVehicleBrand();
        prefs.edit().putString("carBrand", carBrand).apply();

        carModel = item.getVehicleModel();
        prefs.edit().putString("carModel", carModel).apply();

        fuelPri = item.getVehicleFuelPri();
        prefs.edit().putInt("FuelPrimary", fuelPri).apply();

        fuelSec = item.getVehicleFuelSec();
        prefs.edit().putInt("FuelSecondary", fuelSec).apply();

        kilometer = item.getVehicleKilometer();
        prefs.edit().putInt("Kilometer", kilometer).apply();

        carPhoto = item.getVehiclePhoto();
        prefs.edit().putString("CarPhoto", carPhoto).apply();

        plateNo = item.getVehiclePlateNo();
        prefs.edit().putString("plateNo", plateNo).apply();

        averageCons = item.getVehicleConsumption();
        prefs.edit().putFloat("averageConsumption", averageCons).apply();

        carbonEmission = item.getVehicleEmission();
        prefs.edit().putInt("carbonEmission", carbonEmission).apply();

        getVariables(prefs);
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {
            case R.id.spinner_brands:
                switch (position) {
                    case 0:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.acura_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.acura_models, MainActivity.carModel), true);
                        break;
                    case 1:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.alfaRomeo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.alfaRomeo_models, MainActivity.carModel), true);
                        break;
                    case 2:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.anadol_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.anadol_models, MainActivity.carModel), true);
                        break;
                    case 3:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.astonMartin_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.astonMartin_models, MainActivity.carModel), true);
                        break;
                    case 4:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.audi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.audi_models, MainActivity.carModel), true);
                        break;
                    case 5:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.bentley_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.bentley_models, MainActivity.carModel), true);
                        break;
                    case 6:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.bmw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.bmw_models, MainActivity.carModel), true);
                        break;
                    case 7:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.bugatti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.bugatti_models, MainActivity.carModel), true);
                        break;
                    case 8:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.buick_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.buick_models, MainActivity.carModel), true);
                        break;
                    case 9:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.cadillac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.cadillac_models, MainActivity.carModel), true);
                        break;
                    case 10:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.cherry_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.cherry_models, MainActivity.carModel), true);
                        break;
                    case 11:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.chevrolet_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.chevrolet_models, MainActivity.carModel), true);
                        break;
                    case 12:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.chyrsler_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.chyrsler_models, MainActivity.carModel), true);
                        break;
                    case 13:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.citroen_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.citroen_models, MainActivity.carModel), true);
                        break;
                    case 14:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.dacia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.dacia_models, MainActivity.carModel), true);
                        break;
                    case 15:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.daeweo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.daeweo_models, MainActivity.carModel), true);
                        break;
                    case 16:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.daihatsu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.daihatsu_models, MainActivity.carModel), true);
                        break;
                    case 17:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.dodge_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.dodge_models, MainActivity.carModel), true);
                        break;
                    case 18:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ds_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ds_models, MainActivity.carModel), true);
                        break;
                    case 19:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.eagle_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.eagle_models, MainActivity.carModel), true);
                        break;
                    case 20:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ferrari_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ferrari_models, MainActivity.carModel), true);
                        break;
                    case 21:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.fiat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.fiat_models, MainActivity.carModel), true);
                        break;
                    case 22:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ford_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ford_models, MainActivity.carModel), true);
                        break;
                    case 23:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.gaz_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.gaz_models, MainActivity.carModel), true);
                        break;
                    case 24:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.geely_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.geely_models, MainActivity.carModel), true);
                        break;
                    case 25:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.honda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.honda_models, MainActivity.carModel), true);
                        break;
                    case 26:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.hyundai_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.hyundai_models, MainActivity.carModel), true);
                        break;
                    case 27:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.ikco_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.ikco_models, MainActivity.carModel), true);
                        break;
                    case 28:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.infiniti_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.infiniti_models, MainActivity.carModel), true);
                        break;
                    case 29:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.isuzu_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.isuzu_models, MainActivity.carModel), true);
                        break;
                    case 30:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.jaguar_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.jaguar_models, MainActivity.carModel), true);
                        break;
                    case 31:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.kia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.kia_models, MainActivity.carModel), true);
                        break;
                    case 32:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.kral_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.kral_models, MainActivity.carModel), true);
                        break;
                    case 33:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lada_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lada_models, MainActivity.carModel), true);
                        break;
                    case 34:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lamborghini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lamborghini_models, MainActivity.carModel), true);
                        break;
                    case 35:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lancia_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lancia_models, MainActivity.carModel), true);
                        break;
                    case 36:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lexus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lexus_models, MainActivity.carModel), true);
                        break;
                    case 37:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lincoln_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lincoln_models, MainActivity.carModel), true);
                        break;
                    case 38:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.lotus_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.lotus_models, MainActivity.carModel), true);
                        break;
                    case 39:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.maserati_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.maserati_models, MainActivity.carModel), true);
                        break;
                    case 40:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.maybach_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.maybach_models, MainActivity.carModel), true);
                        break;
                    case 41:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mazda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mazda_models, MainActivity.carModel), true);
                        break;
                    case 42:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mercedes_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mercedes_models, MainActivity.carModel), true);
                        break;
                    case 43:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mercury_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mercury_models, MainActivity.carModel), true);
                        break;
                    case 44:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mg_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mg_models, MainActivity.carModel), true);
                        break;
                    case 45:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mini_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mini_models, MainActivity.carModel), true);
                        break;
                    case 46:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.mitsubishi_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.mitsubishi_models, MainActivity.carModel), true);
                        break;
                    case 47:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.moskwitsch_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.moskwitsch_models, MainActivity.carModel), true);
                        break;
                    case 48:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.nissan_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.nissan_models, MainActivity.carModel), true);
                        break;
                    case 49:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.oldsmobile_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.oldsmobile_models, MainActivity.carModel), true);
                        break;
                    case 50:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.opel_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.opel_models, MainActivity.carModel), true);
                        break;
                    case 51:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.pagani_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.pagani_models, MainActivity.carModel), true);
                        break;
                    case 52:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.peugeot_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.peugeot_models, MainActivity.carModel), true);
                        break;
                    case 53:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.plymouth_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.plymouth_models, MainActivity.carModel), true);
                        break;
                    case 54:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.pontiac_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.pontiac_models, MainActivity.carModel), true);
                        break;
                    case 55:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.porsche_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.porsche_models, MainActivity.carModel), true);
                        break;
                    case 56:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.proton_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.proton_models, MainActivity.carModel), true);
                        break;
                    case 57:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.renault_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.renault_models, MainActivity.carModel), true);
                        break;
                    case 58:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.rollsRoyce_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.rollsRoyce_models, MainActivity.carModel), true);
                        break;
                    case 59:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.rover_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.rover_models, MainActivity.carModel), true);
                        break;
                    case 60:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.saab_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.saab_models, MainActivity.carModel), true);
                        break;
                    case 61:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.seat_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.seat_models, MainActivity.carModel), true);
                        break;
                    case 62:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.skoda_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.skoda_models, MainActivity.carModel), true);
                        break;
                    case 63:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.smart_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.smart_models, MainActivity.carModel), true);
                        break;
                    case 64:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.subaru_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.subaru_models, MainActivity.carModel), true);
                        break;
                    case 65:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.suzuki_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.suzuki_models, MainActivity.carModel), true);
                        break;
                    case 66:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.tata_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.tata_models, MainActivity.carModel), true);
                        break;
                    case 67:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.tesla_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.tesla_models, MainActivity.carModel), true);
                        break;
                    case 68:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.tofas_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.tofas_models, MainActivity.carModel), true);
                        break;
                    case 69:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.toyota_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.toyota_models, MainActivity.carModel), true);
                        break;
                    case 70:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.vw_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.vw_models, MainActivity.carModel), true);
                        break;
                    case 71:
                        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Models.volvo_models);
                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner2.setAdapter(adapter2);
                        spinner2.setSelection(MainActivity.getIndexOf(Models.volvo_models, MainActivity.carModel), true);
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
                if (isNetworkConnected(AutomobileEditActivity.this)) {
                    updateVehicle();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (ActivityCompat.checkSelfPermission(AutomobileEditActivity.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.create(AutomobileEditActivity.this).single().start();
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
                bitmap = BitmapFactory.decodeFile(image.getPath());
                Glide.with(this).load(bitmap).apply(options).into(carPic);
                carPhoto = "https://fuel-spot.com/uploads/automobiles/" + username + "-" + plateNo + ".jpg";
                prefs.edit().putString("CarPhoto", carPhoto).apply();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}