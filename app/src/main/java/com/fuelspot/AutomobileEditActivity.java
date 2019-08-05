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
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import com.bumptech.glide.signature.ObjectKey;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fuelspot.model.VehicleItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.FragmentAutomobile.vehiclePurchaseList;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.automobileModels;
import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.getStringImage;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.resizeAndRotate;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.vehicleID;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;

public class AutomobileEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Bitmap bitmap;
    private CircleImageView carPic;
    private Spinner spinner2;
    private SharedPreferences.Editor editor;
    private Window window;
    private Toolbar toolbar;
    private RequestQueue requestQueue;
    private RequestOptions options;
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
        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        SharedPreferences prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        editor = prefs.edit();
        requestQueue = Volley.newRequestQueue(this);

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
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
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
        String[] carManufactures = new String[automobileModels.size()];

        for (int i = 0; i < automobileModels.size(); i++) {
            carManufactures[i] = automobileModels.get(i).getVehicleBrand();
        }

        Spinner spinner = findViewById(R.id.spinner_brands);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carManufactures);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(adapter);
        spinner.setSelection(MainActivity.getIndexOf(carManufactures, MainActivity.carBrand), true);

        //MODEL SEÇİMİ
        spinner2 = findViewById(R.id.spinner_models);

        //Yakıt seçenekleri
        RadioButton gasoline = findViewById(R.id.gasoline);
        RadioButton diesel = findViewById(R.id.diesel);
        RadioButton lpg = findViewById(R.id.lpg);
        RadioButton elec = findViewById(R.id.electricity);

        RadioButton notExist = findViewById(R.id.notExist);
        RadioButton gasoline2 = findViewById(R.id.gasoline2);
        RadioButton diesel2 = findViewById(R.id.diesel2);
        RadioButton lpg2 = findViewById(R.id.lpg2);
        RadioButton elec2 = findViewById(R.id.electricity2);

        RadioGroup radioGroup1 = findViewById(R.id.radioGroup_fuelPrimary);
        RadioGroup radioGroup2 = findViewById(R.id.radioGroup_fuelSecondary);

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
            case -1:
                notExist.setChecked(true);
                break;
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
                } else if (checkedId == R.id.electricity) {
                    fuelPri = 3;
                } else {
                    fuelPri = -1;
                }

                editor.putInt("FuelPrimary", fuelPri);
            }
        });

        //2. yakıt
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.notExist) {
                    fuelSec = -1;
                } else if (checkedId == R.id.gasoline2) {
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
                if (s != null && s.length() >= 1) {
                    MainActivity.kilometer = Integer.parseInt(s.toString());
                    editor.putInt("Kilometer", MainActivity.kilometer);
                }
            }
        });

        //PlakaNO
        final EditText plateText = findViewById(R.id.editText_plate);
        plateText.setText(plateNo);
        if (vehiclePurchaseList.size() > 0) {
            plateText.setEnabled(false);
        } else {
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
                        editor.putString("plateNo", plateNo);
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

        final int vehicleNumber = userAutomobileList.size();
        FloatingActionButton fab = findViewById(R.id.fab);
        if (vehiclePurchaseList.size() > 0) {
            fab.hide();
        }
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
                            if (response.equals("Success")) {
                                editor.apply();
                                Toast.makeText(AutomobileEditActivity.this, getString(R.string.vehicle_update_success), Toast.LENGTH_LONG).show();
                                finish();
                            } else if (response.equals("plateNo exists")) {
                                Toast.makeText(AutomobileEditActivity.this, "Bu plaka daha önce eklenmiş. Bir hata olduğunu düşünüyorsanız bizimle iletişime geçiniz.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(AutomobileEditActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
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
                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("username", username);
                params.put("carBrand", carBrand);
                params.put("carModel", carModel);
                params.put("fuelPri", String.valueOf(fuelPri));
                params.put("fuelSec", String.valueOf(fuelSec));
                params.put("kilometer", String.valueOf(kilometer));
                if (bitmap != null) {
                    params.put("carPhoto", getStringImage(bitmap));
                } else {
                    params.put("carPhoto", "");
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

    private void deleteVehicle() {
        loadingDelete.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.getString(R.string.API_DELETE_AUTOMOBILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDelete.dismiss();
                        if (response != null && response.length() > 0) {
                            if ("Success".equals(response)) {//set VehicleID = 0 because user delete this car. Choose another one.
                                vehicleID = 0;
                                Toast.makeText(AutomobileEditActivity.this, getString(R.string.vehicle_delete_success), Toast.LENGTH_LONG).show();
                                fetchAutomobiles();
                            } else {
                                Toast.makeText(AutomobileEditActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
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
                params.put("vehicleID", String.valueOf(vehicleID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchAutomobiles() {
        userAutomobileList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_USER_AUTOMOBILES) + "?username=" + username,
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
                                }

                                if (vehicleID == 0) {
                                    chooseVehicle(userAutomobileList.get(0));
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

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void chooseVehicle(VehicleItem item) {
        vehicleID = item.getID();
        editor.putInt("vehicleID", vehicleID);

        carBrand = item.getVehicleBrand();
        editor.putString("carBrand", carBrand);

        carModel = item.getVehicleModel();
        editor.putString("carModel", carModel);

        fuelPri = item.getVehicleFuelPri();
        editor.putInt("FuelPrimary", fuelPri);

        fuelSec = item.getVehicleFuelSec();
        editor.putInt("FuelSecondary", fuelSec);

        kilometer = item.getVehicleKilometer();
        editor.putInt("Kilometer", kilometer);

        carPhoto = item.getVehiclePhoto();
        editor.putString("CarPhoto", carPhoto);

        plateNo = item.getVehiclePlateNo();
        editor.putString("plateNo", plateNo);

        averageCons = item.getVehicleConsumption();
        editor.putFloat("averageConsumption", averageCons);

        carbonEmission = item.getVehicleEmission();
        editor.putInt("carbonEmission", carbonEmission);

        editor.apply();
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {
            case R.id.spinner_brands:
                try {
                    String models = automobileModels.get(position).getVehicleModel();
                    JSONArray res = new JSONArray(models);
                    String[] spinnerArray = new String[res.length()];

                    for (int i = 0; i < res.length(); i++) {
                        spinnerArray[i] = res.getString(i);
                    }

                    ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArray);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner2.setAdapter(adapter2);
                    spinner2.setSelection(MainActivity.getIndexOf(spinnerArray, carModel), true);
                    spinner2.setOnItemSelectedListener(this);

                    carBrand = spinner.getSelectedItem().toString();
                    editor.putString("carBrand", carBrand);

                    carModel = spinner2.getSelectedItem().toString();
                    editor.putString("carModel", carModel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (ActivityCompat.checkSelfPermission(AutomobileEditActivity.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.create(AutomobileEditActivity.this).single().start();
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
            }
        } else {
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
                    editor.putString("CarPhoto", carPhoto);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}