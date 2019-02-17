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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.TAX_DIESEL;
import static com.fuelspot.MainActivity.TAX_ELECTRICITY;
import static com.fuelspot.MainActivity.TAX_GASOLINE;
import static com.fuelspot.MainActivity.TAX_LPG;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.vehicleID;

public class AddFuel extends AppCompatActivity {

    private Window window;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;
    private RequestOptions options;

    private int chosenStationID;
    private String stationName;
    private String stationAddress;
    private String stationLoc;
    private String stationLogo;
    private float gasolinePrice;
    private float dieselPrice;
    private float LPGPrice;
    private float electricityPrice;
    private float selectedUnitPrice;
    private float buyedLiter;
    private float entryPrice;
    private float selectedTaxRate;
    private float selectedUnitPrice2;
    private float buyedLiter2;
    private float entryPrice2;
    private float selectedTaxRate2;
    private float totalPrice;

    /* LAYOUT 1 ÖĞELER */
    private CircleImageView istasyonLogoHolder;
    private TextView istasyonNameHolder;
    private TextView istasyonIDHolder;
    private RelativeLayout expandableLayoutYakit;
    private RelativeLayout expandableLayoutYakit2;
    private Button expandableButton1;
    private Button expandableButton2;
    private String fuelType;
    private String fuelType2;
    private TextView fuelType1Text;
    private TextView fuelType2Text;
    private TextView fuelGrandTotal;
    private TextView textViewLitre;
    private ImageView fuelType1Icon;
    private ImageView fuelType2Icon;
    private EditText enterKilometer;
    private EditText textViewLitreFiyati;
    private EditText textViewTotalFiyat;
    private EditText textViewLitreFiyati2;
    private EditText textViewTotalFiyat2;
    private EditText textViewLitre2;
    private Bitmap bitmap;
    private ImageView photoHolder;
    private ScrollView scrollView;

    private int tempKM;

    private static float taxCalculator(int fuelType, float price) {
        float tax;
        switch (fuelType) {
            case 0:
                tax = price * TAX_GASOLINE;
                break;
            case 1:
                tax = price * TAX_DIESEL;
                break;
            case 2:
                tax = price * TAX_LPG;
                break;
            default:
                tax = price * TAX_ELECTRICITY;
                break;
        }
        return tax;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fuel);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        getVariables(prefs);

        // Analytics
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName("Yakıt ekle");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Creating a Request Queue
        requestQueue = Volley.newRequestQueue(AddFuel.this);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        chosenStationID = getIntent().getIntExtra("STATION_ID", 0);

        scrollView = findViewById(R.id.addfuel_layout1);

        istasyonIDHolder = findViewById(R.id.stationIDHolder);
        istasyonLogoHolder = findViewById(R.id.stationIconHolder);
        istasyonNameHolder = findViewById(R.id.stationNameHolder);

        expandableLayoutYakit = findViewById(R.id.division2);
        expandableLayoutYakit2 = findViewById(R.id.division3);
        expandableButton1 = findViewById(R.id.expandableButtonYakit1);
        expandableButton2 = findViewById(R.id.expandableButtonYakit2);

        enterKilometer = findViewById(R.id.editTextKilometer);

        fuelType1Icon = findViewById(R.id.fuelType1);
        fuelType1Text = findViewById(R.id.fuelType1Text);
        textViewLitreFiyati = findViewById(R.id.editTextPricePerLiter);
        textViewTotalFiyat = findViewById(R.id.editTextPrice);
        textViewLitre = findViewById(R.id.editTextLiter);

        fuelType2Icon = findViewById(R.id.fuelType2);
        fuelType2Text = findViewById(R.id.fuelType2Text);
        textViewLitreFiyati2 = findViewById(R.id.editTextPricePerLiter2);
        textViewLitre2 = findViewById(R.id.editTextLiter2);
        textViewTotalFiyat2 = findViewById(R.id.editTextPrice2);

        fuelGrandTotal = findViewById(R.id.textViewGrandTotal);

        // Check whether user is at station or not
        fetchSingleStation();
    }

    private void fetchSingleStation() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                stationName = obj.getString("name");
                                stationAddress = obj.getString("vicinity");
                                stationLoc = obj.getString("location");
                                stationLogo = obj.getString("logoURL");
                                gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                dieselPrice = (float) obj.getDouble("dieselPrice");
                                LPGPrice = (float) obj.getDouble("lpgPrice");
                                electricityPrice = (float) obj.getDouble("electricityPrice");

                                scrollView.setAlpha(1.0f);
                                loadLayout();
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
                params.put("stationID", String.valueOf(chosenStationID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void loadLayout() {
        istasyonIDHolder.setText("" + chosenStationID);
        istasyonNameHolder.setText(stationName);
        Glide.with(AddFuel.this).load(stationLogo).apply(options).into(istasyonLogoHolder);

        enterKilometer.setText(String.valueOf(kilometer));
        enterKilometer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    tempKM = Integer.parseInt(s.toString());
                }
            }
        });

        //1. YAKIT TİPİ
        switch (fuelPri) {
            case 0:
                selectedUnitPrice = gasolinePrice;
                selectedTaxRate = TAX_GASOLINE;
                fuelType = getString(R.string.gasoline);
                Glide.with(AddFuel.this).load(R.drawable.gasoline).apply(options).into(fuelType1Icon);
                break;
            case 1:
                selectedUnitPrice = dieselPrice;
                selectedTaxRate = TAX_DIESEL;
                fuelType = getString(R.string.diesel);
                Glide.with(AddFuel.this).load(R.drawable.diesel).apply(options).into(fuelType1Icon);
                break;
            case 2:
                selectedUnitPrice = LPGPrice;
                selectedTaxRate = TAX_LPG;
                fuelType = getString(R.string.lpg);
                Glide.with(AddFuel.this).load(R.drawable.lpg).apply(options).into(fuelType1Icon);
                break;
            case 3:
                selectedUnitPrice = electricityPrice;
                selectedTaxRate = TAX_ELECTRICITY;
                fuelType = getString(R.string.electricity);
                Glide.with(AddFuel.this).load(R.drawable.electricity).apply(options).into(fuelType1Icon);
                break;
            default:
                expandableLayoutYakit.setVisibility(View.GONE);
                expandableButton1.setVisibility(View.GONE);
                break;
        }

        fuelType1Text.setText(fuelType);
        textViewLitreFiyati.setText(String.valueOf(selectedUnitPrice));

        //2. YAKIT TİPİ
        switch (fuelSec) {
            case 0:
                selectedUnitPrice2 = gasolinePrice;
                selectedTaxRate2 = TAX_GASOLINE;
                fuelType2 = getString(R.string.gasoline);
                Glide.with(AddFuel.this).load(R.drawable.gasoline).apply(options).into(fuelType2Icon);
                break;
            case 1:
                selectedUnitPrice2 = dieselPrice;
                selectedTaxRate2 = TAX_DIESEL;
                fuelType2 = getString(R.string.diesel);
                Glide.with(AddFuel.this).load(R.drawable.diesel).apply(options).into(fuelType2Icon);
                break;
            case 2:
                selectedUnitPrice2 = LPGPrice;
                selectedTaxRate2 = TAX_LPG;
                fuelType2 = getString(R.string.lpg);
                Glide.with(AddFuel.this).load(R.drawable.lpg).apply(options).into(fuelType2Icon);
                break;
            case 3:
                selectedUnitPrice2 = electricityPrice;
                selectedTaxRate2 = TAX_ELECTRICITY;
                fuelType2 = getString(R.string.electricity);
                Glide.with(AddFuel.this).load(R.drawable.electricity).apply(options).into(fuelType2Icon);
                break;
            default:
                expandableLayoutYakit2.setVisibility(View.GONE);
                expandableButton2.setVisibility(View.GONE);
                break;
        }

        fuelType2Text.setText(fuelType2);
        textViewLitreFiyati2.setText(String.valueOf(selectedUnitPrice2));

        textViewLitreFiyati.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    selectedUnitPrice = Float.parseFloat(s.toString());
                    buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
                    String literText = String.format(Locale.getDefault(), "%.2f", buyedLiter);
                    textViewLitre.setText(literText);
                }
            }
        });

        textViewTotalFiyat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    entryPrice = Float.parseFloat(s.toString());
                    buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
                    String literText = String.format(Locale.getDefault(), "%.2f", buyedLiter);
                    textViewLitre.setText(literText);
                    totalPrice = entryPrice + entryPrice2;

                    updateTaxandGrandTotal();
                }
            }
        });

        textViewLitreFiyati2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    selectedUnitPrice2 = Float.parseFloat(s.toString());
                    buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
                    String literText2 = String.format(Locale.getDefault(), "%.2f", buyedLiter2);
                    textViewLitre.setText(literText2);
                }
            }
        });

        textViewTotalFiyat2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    entryPrice2 = Float.parseFloat(s.toString());
                    buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
                    String literText2 = String.format(Locale.getDefault(), "%.2f", buyedLiter2);
                    textViewLitre2.setText(literText2);
                    totalPrice = entryPrice + entryPrice2;
                    updateTaxandGrandTotal();
                }
            }
        });

        photoHolder = findViewById(R.id.photoHolder);
        photoHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyFilePickerPermission(AddFuel.this)) {
                    ImagePicker.cameraOnly().start(AddFuel.this);
                } else {
                    ActivityCompat.requestPermissions(AddFuel.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        Button sendVariables = findViewById(R.id.sendButton);
        sendVariables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected(AddFuel.this)) {
                    if (totalPrice > 0) {
                        if (kilometer != 0) {
                            if (tempKM > kilometer) {
                                addPurchase();
                            } else {
                                Toast.makeText(AddFuel.this, getString(R.string.low_kilometer), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AddFuel.this, getString(R.string.empty_kilometer), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AddFuel.this, getString(R.string.empty_total_price), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AddFuel.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private float howManyLiter(float priceForUnit, float totalPrice) {
        if (priceForUnit == 0) {
            return 0f;
        } else {
            return totalPrice / priceForUnit;
        }
    }

    private void addPurchase() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(AddFuel.this, getString(R.string.adding_fuel), getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_PURCHASE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                Toast.makeText(AddFuel.this, getString(R.string.fuel_add_success), Toast.LENGTH_LONG).show();
                                kilometer = tempKM;
                                prefs.edit().putInt("Kilometer", kilometer).apply();
                                finish();
                            } else {
                                Toast.makeText(AddFuel.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AddFuel.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }

                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        volleyError.printStackTrace();
                        Toast.makeText(AddFuel.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("vehicleID", String.valueOf(vehicleID));
                params.put("plateNO", plateNo);
                params.put("kilometer", String.valueOf(tempKM));
                params.put("stationID", String.valueOf(chosenStationID));
                params.put("stationNAME", stationName);
                params.put("stationLOC", stationLoc);
                params.put("stationICON", stationLogo);
                params.put("fuelType", String.valueOf(fuelPri));
                params.put("fuelPrice", String.valueOf(selectedUnitPrice));
                params.put("fuelLiter", String.valueOf(buyedLiter));
                params.put("fuelTax", String.valueOf(selectedTaxRate));
                params.put("fuelType2", String.valueOf(fuelSec));
                params.put("fuelPrice2", String.valueOf(selectedUnitPrice2));
                params.put("fuelLiter2", String.valueOf(buyedLiter2));
                params.put("fuelTax2", String.valueOf(selectedTaxRate2));
                params.put("totalPrice", String.valueOf(totalPrice));
                params.put("country", userCountry);
                params.put("unit", String.valueOf(userUnit));
                params.put("currency", String.valueOf(currencyCode));
                if (bitmap != null) {
                    params.put("billPhoto", getStringImage(bitmap));
                }
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));
                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);

    }

    private void updateTaxandGrandTotal() {
        float tax1 = taxCalculator(fuelPri, entryPrice);
        float tax2 = taxCalculator(fuelSec, entryPrice2);

        float taxTotal = tax1 + tax2;
        totalPrice = entryPrice + entryPrice2;
        String totalHolder = getString(R.string.total) + ": " + String.format(Locale.getDefault(), "%.2f", totalPrice) + " " + currencyCode;
        fuelGrandTotal.setText(totalHolder);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Remove variables
                bitmap = null;
                stationName = null;
                stationLoc = null;
                gasolinePrice = 0;
                dieselPrice = 0;
                electricityPrice = 0;
                LPGPrice = 0;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (ActivityCompat.checkSelfPermission(AddFuel.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(AddFuel.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                    ImagePicker.create(AddFuel.this).single().start();
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
                Glide.with(this).load(image.getPath()).apply(options).into(photoHolder);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Remove variables
        bitmap = null;
        stationName = null;
        tempKM = 0;
        stationLoc = null;
        stationLogo = null;
        gasolinePrice = 0;
        dieselPrice = 0;
        electricityPrice = 0;
        LPGPrice = 0;
        finish();
    }
}
