package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import eu.amirs.JSON;

import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.TAX_DIESEL;
import static com.fuelspot.MainActivity.TAX_ELECTRICITY;
import static com.fuelspot.MainActivity.TAX_GASOLINE;
import static com.fuelspot.MainActivity.TAX_LPG;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.stationPhotoChooser;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.vehicleID;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;

public class AddFuel extends AppCompatActivity {

    Window window;
    Toolbar toolbar;
    SharedPreferences prefs;
    RequestQueue requestQueue;
    ProgressDialog pDialog;
    RequestOptions options;

    int chosenStationID;
    String chosenGoogleID, chosenStationName, chosenStationAddress, chosenStationLoc;
    float gasolinePrice, dieselPrice, LPGPrice, electricityPrice, selectedUnitPrice, buyedLiter, entryPrice, selectedTaxRate, selectedUnitPrice2, buyedLiter2, entryPrice2, selectedTaxRate2, tax1, tax2, taxTotal, totalPrice;

    /* LAYOUT 1 ÖĞELER */
    RelativeLayout expandableLayoutYakit, expandableLayoutYakit2;
    Button expandableButton1, expandableButton2;
    String fuelType, fuelType2, billPhoto;
    TextView fuelType1Text, fuelType2Text, fuelVergi, fuelGrandTotal;
    ImageView fuelType1Icon, fuelType2Icon;
    EditText enterKilometer, textViewLitreFiyati, textViewTotalFiyat, textViewLitre, textViewLitreFiyati2, textViewTotalFiyat2, textViewLitre2;
    Bitmap bitmap;
    ImageView photoHolder;
    ScrollView scrollView;

    public static float taxCalculator(int fuelType, float price) {
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
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        MainActivity.getVariables(prefs);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Yakıt ekle");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Creating a Request Queue
        requestQueue = Volley.newRequestQueue(AddFuel.this);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        pDialog = new ProgressDialog(AddFuel.this);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();

        scrollView = findViewById(R.id.addfuel_layout1);

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

        fuelVergi = findViewById(R.id.textViewVergi);
        fuelGrandTotal = findViewById(R.id.textViewGrandTotal);

        // Check whether user is at station or not
        checkIsAtStation();
    }

    public void checkIsAtStation() {
        //Search stations in a radius of 50m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=" + mapDefaultStationRange + "&type=gas_station&key=" + getString(R.string.g_api_key);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (json.key("results").count() > 0) {
                            // Yes! He is in station. Probably there is only one station in 100m so get the first value
                            chosenGoogleID = json.key("results").index(0).key("place_id").stringValue();
                            fetchStation(chosenGoogleID);
                        } else {
                            informUser();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                informUser();
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    void informUser() {
        pDialog.dismiss();
        chosenStationID = 0;
        chosenStationName = "";
        chosenStationLoc = "";
        chosenGoogleID = "";
        chosenStationAddress = "";

        new AlertDialog.Builder(this)
                .setTitle("Hata")
                .setMessage("Şu an bir istasyonda bulunmadığınızdan dolayı yakıt ekleyemezsiniz.")
                .setNeutralButton("TAMAM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void fetchStation(final String googleID) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {

                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                chosenStationID = obj.getInt("id");
                                chosenStationName = obj.getString("name");
                                chosenStationAddress = obj.getString("vicinity");
                                chosenStationLoc = obj.getString("location");
                                gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                dieselPrice = (float) obj.getDouble("dieselPrice");
                                LPGPrice = (float) obj.getDouble("lpgPrice");
                                electricityPrice = (float) obj.getDouble("electricityPrice");

                                scrollView.setAlpha(1.0f);
                                pDialog.dismiss();
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
                params.put("googleID", googleID);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void loadLayout() {
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
                    kilometer = Integer.parseInt(s.toString());
                }
            }
        });

        //1. YAKIT TİPİ
        switch (fuelPri) {
            case 0:
                selectedUnitPrice = gasolinePrice;
                selectedTaxRate = TAX_GASOLINE;
                fuelType = "gasoline";
                Glide.with(AddFuel.this).load(R.drawable.gasoline).apply(options).into(fuelType1Icon);
                break;
            case 1:
                selectedUnitPrice = dieselPrice;
                selectedTaxRate = TAX_DIESEL;
                fuelType = "diesel";
                Glide.with(AddFuel.this).load(R.drawable.diesel).apply(options).into(fuelType1Icon);
                break;
            case 2:
                selectedUnitPrice = LPGPrice;
                selectedTaxRate = TAX_LPG;
                fuelType = "lpg";
                Glide.with(AddFuel.this).load(R.drawable.lpg).apply(options).into(fuelType1Icon);
                break;
            case 3:
                selectedUnitPrice = electricityPrice;
                selectedTaxRate = TAX_ELECTRICITY;
                fuelType = "electric";
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
                fuelType2 = "gasoline";
                Glide.with(AddFuel.this).load(R.drawable.gasoline).apply(options).into(fuelType2Icon);
                break;
            case 1:
                selectedUnitPrice2 = dieselPrice;
                selectedTaxRate2 = TAX_DIESEL;
                fuelType2 = "diesel";
                Glide.with(AddFuel.this).load(R.drawable.diesel).apply(options).into(fuelType2Icon);
                break;
            case 2:
                selectedUnitPrice2 = LPGPrice;
                selectedTaxRate2 = TAX_LPG;
                fuelType2 = "lpg";
                Glide.with(AddFuel.this).load(R.drawable.lpg).apply(options).into(fuelType2Icon);
                break;
            case 3:
                selectedUnitPrice2 = electricityPrice;
                selectedTaxRate2 = TAX_ELECTRICITY;
                fuelType2 = "electric";
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
                    String literText2 = String.format("%.2f", buyedLiter2);
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
                if (verifyFilePickerPermission(AddFuel.this)) {
               /*     FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(AddFuel.this);*/
                    Toast.makeText(AddFuel.this, "Geçici olarak deactive edildi.", Toast.LENGTH_LONG).show();
                } else {
                    ActivityCompat.requestPermissions(AddFuel.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        Button sendVariables = findViewById(R.id.sendButton);
        sendVariables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPurchase();
            }
        });
    }

    public float howManyLiter(float priceForUnit, float totalPrice) {
        if (priceForUnit == 0) {
            return 0f;
        } else {
            return totalPrice / priceForUnit;
        }
    }

    private void addPurchase() {
        if (chosenStationName != null && chosenStationName.length() > 0) {
            if (isNetworkConnected(AddFuel.this)) {
                if (totalPrice > 0) {
                    //Showing the progress dialog
                    final ProgressDialog loading = ProgressDialog.show(AddFuel.this, "Uploading...", "Please wait...", false, false);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_PURCHASE),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    //Disimissing the progress dialog
                                    loading.dismiss();
                                    Toast.makeText(AddFuel.this, s, Toast.LENGTH_LONG).show();
                                    // updateStation();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    //Dismissing the progress dialog
                                    loading.dismiss();
                                    //Showing toast
                                    Toast.makeText(AddFuel.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
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
                            params.put("stationID", String.valueOf(chosenStationID));
                            params.put("stationNAME", chosenStationName);
                            params.put("stationICON", stationPhotoChooser(chosenStationName));
                            params.put("stationLOC", chosenStationLoc);
                            params.put("fuelType", String.valueOf(fuelPri));
                            params.put("fuelPrice", String.valueOf(selectedUnitPrice));
                            params.put("fuelLiter", String.valueOf(buyedLiter));
                            params.put("fuelTax", String.valueOf(selectedTaxRate));
                            params.put("fuelType2", String.valueOf(fuelSec));
                            params.put("fuelPrice2", String.valueOf(selectedUnitPrice2));
                            params.put("fuelLiter2", String.valueOf(buyedLiter2));
                            params.put("fuelTax2", String.valueOf(selectedTaxRate2));
                            params.put("totalPrice", String.valueOf(totalPrice));
                            if (bitmap != null) {
                                params.put("billPhoto", getStringImage(bitmap));
                            }
                            params.put("kilometer", String.valueOf(kilometer));
                            params.put("unit", String.valueOf(userUnit));
                            params.put("currency", String.valueOf(currencyCode));

                            //returning parameters
                            return params;
                        }
                    };

                    //Adding request to the queue
                    requestQueue.add(stringRequest);
                } else {
                    Toast.makeText(AddFuel.this, "Lütfen ne kadar yakıt aldığınızı giriniz", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(AddFuel.this, "İnternet bağlantınızda bir sorun var!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(AddFuel.this, "Şu anda bir benzin istasyonunda değilsiniz. Yakıt aldığınız istasyonu seçiniz.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateStation() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        prefs.edit().putInt("Kilometer", kilometer).apply();
                        bitmap = null;
                        chosenStationName = null;
                        chosenGoogleID = null;
                        chosenStationLoc = null;
                        gasolinePrice = 0;
                        dieselPrice = 0;
                        electricityPrice = 0;
                        LPGPrice = 0;
                        billPhoto = null;
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(AddFuel.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(chosenStationID));
                params.put("stationName", chosenStationName);
                params.put("stationVicinity", chosenStationAddress);
                if (fuelType != null && fuelType.length() > 0 && selectedUnitPrice > 0) {
                    if (fuelType.contains("gasoline")) {
                        params.put("gasolinePrice", String.valueOf(selectedUnitPrice));
                    } else if (fuelType.contains("diesel")) {
                        params.put("dieselPrice", String.valueOf(selectedUnitPrice));
                    } else if (fuelType.contains("lpg")) {
                        params.put("lpgPrice", String.valueOf(selectedUnitPrice));
                    } else {
                        params.put("electricityPrice", String.valueOf(selectedUnitPrice));
                    }
                }
                if (fuelType2 != null && fuelType2.length() > 0 && selectedUnitPrice2 > 0) {
                    if (fuelType2.contains("gasoline")) {
                        params.put("gasolinePrice", String.valueOf(selectedUnitPrice2));
                    } else if (fuelType2.contains("diesel")) {
                        params.put("dieselPrice", String.valueOf(selectedUnitPrice2));
                    } else if (fuelType2.contains("lpg")) {
                        params.put("lpgPrice", String.valueOf(selectedUnitPrice2));
                    } else {
                        params.put("electricityPrice", String.valueOf(selectedUnitPrice2));
                    }
                }

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updateTaxandGrandTotal() {
        tax1 = taxCalculator(fuelPri, entryPrice);
        tax2 = taxCalculator(fuelSec, entryPrice2);

        taxTotal = tax1 + tax2;
        String taxHolder = "VERGİ: " + String.format(Locale.getDefault(), "%.2f", taxTotal) + " " + currencyCode;
        fuelVergi.setText(taxHolder);
        totalPrice = entryPrice + entryPrice2;
        String totalHolder = "TOPLAM: " + String.format(Locale.getDefault(), "%.2f", totalPrice) + " " + currencyCode;
        fuelGrandTotal.setText(totalHolder);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Remove variables
                bitmap = null;
                chosenStationName = null;
                chosenGoogleID = null;
                chosenStationLoc = null;
                gasolinePrice = 0;
                dieselPrice = 0;
                electricityPrice = 0;
                LPGPrice = 0;
                billPhoto = null;
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
                    Toast.makeText(AddFuel.this, "Settings saved...", Toast.LENGTH_SHORT).show();
                /*    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(AddFuel.this);*/
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
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
        switch (requestCode) {
           /* case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    billPhoto = aq.get(0);

                    System.out.println("file://" + billPhoto);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/Bills");
                    folder.mkdirs();

                    CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
                    String fileName = now + ".jpg";

                    UCrop.of(Uri.parse("file://" + billPhoto), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(AddFuel.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        photoHolder.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(AddFuel.this, cropError.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                break;*/
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Remove variables
        bitmap = null;
        chosenStationName = null;
        chosenGoogleID = null;
        chosenStationLoc = null;
        gasolinePrice = 0;
        dieselPrice = 0;
        electricityPrice = 0;
        LPGPrice = 0;
        billPhoto = null;
        finish();
    }
}
