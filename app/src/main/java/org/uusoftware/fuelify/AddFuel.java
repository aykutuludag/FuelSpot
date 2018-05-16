package org.uusoftware.fuelify;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static org.uusoftware.fuelify.ChooseStation.isAddingFuel;
import static org.uusoftware.fuelify.MainActivity.PERMISSIONS_STORAGE;
import static org.uusoftware.fuelify.MainActivity.REQUEST_EXTERNAL_STORAGE;
import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.isNetworkConnected;
import static org.uusoftware.fuelify.MainActivity.kilometer;
import static org.uusoftware.fuelify.MainActivity.pos;
import static org.uusoftware.fuelify.MainActivity.pos2;
import static org.uusoftware.fuelify.MainActivity.stationPhotoChooser;
import static org.uusoftware.fuelify.MainActivity.username;

public class AddFuel extends AppCompatActivity {

    public static String chosenStationName, chosenStationID, chosenStationLoc;
    public static double gasolinePrice, dieselPrice, LPGPrice, electricityPrice;
    Bitmap bitmap;
    Window window;
    Toolbar toolbar;
    RelativeLayout expandableLayoutYakit, expandableLayoutYakit2;
    Button expandableButton1, expandableButton2;
    EditText chooseStation, chooseTime, enterKilometer;
    SharedPreferences prefs;
    RadioGroup chooseFuel, chooseFuel2;
    long purchaseTime;
    String fuelType, fuelType2 = "";
    double totalPrice;
    double selectedUnitPrice, buyedLiter, entryPrice, selectedUnitPrice2, buyedLiter2, entryPrice2;
    EditText textViewLitreFiyati, textViewTotalFiyat, textViewLitre, textViewLitreFiyati2, textViewTotalFiyat2, textViewLitre2;
    ImageView photoHolder;
    Button sendVariables;
    String billPhoto;
    SimpleDateFormat mFormatter = new SimpleDateFormat("dd MMMM HH:mm", Locale.getDefault());

    //Listener for startTime
    SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            chooseTime.setText(mFormatter.format(date));
            purchaseTime = date.getTime();
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel() {
            // Do nothing
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfuel);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        //Variables
        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        MainActivity.getVariables(prefs);

        expandableLayoutYakit = findViewById(R.id.expandableLayoutYakit1);
        expandableLayoutYakit2 = findViewById(R.id.expandableLayoutYakit2);

        //İSTASYON SEÇİMİ
        chooseStation = findViewById(R.id.editTextStation);
        if (chosenStationName != null) {
            chooseStation.setText(chosenStationName);
        }
        chooseStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(AddFuel.this, ChooseStation.class);
                startActivity(intent);
            }
        });

        //SAAT SEÇİMİ
        getTime();
        chooseTime = findViewById(R.id.editTextTime);
        chooseTime.setText(mFormatter.format(new Date(purchaseTime)));
        chooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date(purchaseTime))
                        .setMaxDate(new Date(purchaseTime))
                        .setIs24HourTime(true)
                        .build()
                        .show();
            }
        });

        enterKilometer = findViewById(R.id.editTextKM);
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
                if (s != null && s.length() > 1) {
                    kilometer = Integer.parseInt(s.toString());
                }
            }
        });

        chooseFuel = findViewById(R.id.radioGroup_fuel);
        expandableButton1 = findViewById(R.id.expandableButton1);
        textViewLitreFiyati = findViewById(R.id.editTextPricePerLiter);
        textViewTotalFiyat = findViewById(R.id.editTextPrice);
        textViewLitre = findViewById(R.id.editTextLiter);

        chooseFuel2 = findViewById(R.id.radioGroup_fuel2);
        expandableButton2 = findViewById(R.id.expandableButtonYakit2);
        textViewLitreFiyati2 = findViewById(R.id.editTextPricePerLiter2);
        textViewTotalFiyat2 = findViewById(R.id.editTextPrice2);
        textViewLitre2 = findViewById(R.id.editTextLiter2);

        textViewLitreFiyati.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 3) {
                    selectedUnitPrice = Double.parseDouble(s.toString());
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
                if (s.length() > 0) {
                    entryPrice = Double.parseDouble(s.toString());
                    buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
                    String literText = String.format(Locale.getDefault(), "%.2f", buyedLiter);
                    textViewLitre.setText(literText);
                    totalPrice = entryPrice + entryPrice2;
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
                if (s != null && s.length() > 3) {
                    selectedUnitPrice2 = Double.parseDouble(s.toString());
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
                if (s.length() > 0) {
                    entryPrice2 = Double.parseDouble(s.toString());
                    buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
                    String literText2 = String.format("%.2f", buyedLiter2);
                    textViewLitre2.setText(literText2);
                    totalPrice = entryPrice + entryPrice2;
                }
            }
        });

        photoHolder = findViewById(R.id.photoHolder);
        photoHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyStoragePermissions()) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(AddFuel.this);
                } else {
                    ActivityCompat.requestPermissions(AddFuel.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

        sendVariables = findViewById(R.id.sendToServer);
        sendVariables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosenStationName != null && chosenStationName.length() >= 3) {
                    if (isNetworkConnected(AddFuel.this)) {
                        if (totalPrice > 0) {
                            //Showing the progress dialog
                            final ProgressDialog loading = ProgressDialog.show(AddFuel.this, "Uploading...", "Please wait...", false, false);
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_FUEL),
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String s) {
                                            //Disimissing the progress dialog
                                            loading.dismiss();
                                            Toast.makeText(AddFuel.this, s, Toast.LENGTH_LONG).show();
                                            updateStationPrices();
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
                                    params.put("purchaseTime", String.valueOf(purchaseTime));
                                    params.put("username", username);
                                    params.put("stationID", chosenStationID);
                                    params.put("stationNAME", chosenStationName);
                                    params.put("stationICON", stationPhotoChooser(chosenStationName));
                                    params.put("stationLOC", chosenStationLoc);
                                    params.put("fuelType", fuelType);
                                    params.put("fuelPrice", String.valueOf(selectedUnitPrice));
                                    params.put("fuelLiter", String.valueOf(buyedLiter));
                                    params.put("fuelType2", fuelType2);
                                    params.put("fuelPrice2", String.valueOf(selectedUnitPrice2));
                                    params.put("fuelLiter2", String.valueOf(buyedLiter2));
                                    params.put("totalPrice", String.valueOf(totalPrice));
                                    params.put("kilometer", String.valueOf(kilometer));
                                    if (bitmap != null) {
                                        params.put("billPhoto", getStringImage(bitmap));
                                    }

                                    //returning parameters
                                    return params;
                                }
                            };

                            //Creating a Request Queue
                            RequestQueue requestQueue = Volley.newRequestQueue(AddFuel.this);

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
        });

        if (savedInstanceState == null) {
            updatePrices();
        }
    }

    private void updateStationPrices() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(AddFuel.this, s, Toast.LENGTH_LONG).show();
                        updateCarInfo();
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
                params.put("stationID", chosenStationID);
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
                params.put("lastUpdated", String.valueOf(purchaseTime));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(AddFuel.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updateCarInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_CAR),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Toast.makeText(AddFuel.this, s, Toast.LENGTH_LONG).show();
                        prefs.edit().putInt("Kilometer", kilometer).apply();
                        bitmap = null;
                        chosenStationName = null;
                        chosenStationID = null;
                        chosenStationLoc = null;
                        gasolinePrice = 0;
                        dieselPrice = 0;
                        electricityPrice = 0;
                        LPGPrice = 0;
                        billPhoto = null;
                        isAddingFuel = false;
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
                params.put("username", username);
                params.put("carBrand", carBrand);
                params.put("carModel", carModel);
                params.put("fuelPri", String.valueOf(fuelPri));
                params.put("fuelSec", String.valueOf(fuelSec));
                params.put("km", String.valueOf(kilometer));
                if (carPhoto != null) {
                    params.put("carPhoto", carPhoto);
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
        RequestQueue requestQueue = Volley.newRequestQueue(AddFuel.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void updatePrices() {
        //1. YAKIT TİPİ
        switch (fuelPri) {
            case 0:
                chooseFuel.check(R.id.gasoline);
                selectedUnitPrice = gasolinePrice;
                fuelType = "gasoline";
                break;
            case 1:
                chooseFuel.check(R.id.diesel);
                selectedUnitPrice = dieselPrice;
                fuelType = "diesel";
                break;
            case 2:
                chooseFuel.check(R.id.lpg);
                selectedUnitPrice = LPGPrice;
                fuelType = "lpg";
                break;
            case 3:
                chooseFuel.check(R.id.electricity);
                selectedUnitPrice = electricityPrice;
                fuelType = "electric";
                break;
            default:
                expandableLayoutYakit.setVisibility(View.GONE);
                expandableButton1.setVisibility(View.GONE);
                break;
        }

        textViewLitreFiyati.setText(String.valueOf(selectedUnitPrice));
        buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
        String literText = String.format(Locale.getDefault(), "%.2f", buyedLiter);
        textViewLitre.setText(literText);
        textViewTotalFiyat.setText(String.valueOf(entryPrice));


        //2. YAKIT TİPİ
        switch (fuelSec) {
            case 0:
                chooseFuel2.check(R.id.gasoline2);
                selectedUnitPrice2 = gasolinePrice;
                fuelType2 = "gasoline";
                break;
            case 1:
                chooseFuel2.check(R.id.diesel2);
                selectedUnitPrice2 = dieselPrice;
                fuelType2 = "diesel";
                break;
            case 2:
                chooseFuel2.check(R.id.lpg2);
                selectedUnitPrice2 = LPGPrice;
                fuelType2 = "lpg";
                break;
            case 3:
                chooseFuel2.check(R.id.electricity2);
                selectedUnitPrice2 = electricityPrice;
                fuelType2 = "electric";
                break;
            default:
                expandableLayoutYakit2.setVisibility(View.GONE);
                expandableButton2.setVisibility(View.GONE);
                break;
        }

        textViewLitreFiyati2.setText(String.valueOf(selectedUnitPrice2));
        buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
        String literText2 = String.format(Locale.getDefault(), "%.2f", buyedLiter2);
        textViewLitre2.setText(literText2);
        textViewTotalFiyat2.setText(String.valueOf(entryPrice2));
    }

    public double howManyLiter(double priceForUnit, double totalPrice) {
        if (priceForUnit == 0) {
            return 0.00;
        } else {
            return totalPrice / priceForUnit;
        }
    }

    public void getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        purchaseTime = calendar.getTimeInMillis();
    }

    public boolean verifyStoragePermissions() {
        boolean hasStorage;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int permission = ActivityCompat.checkSelfPermission(AddFuel.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            hasStorage = permission == PackageManager.PERMISSION_GRANTED;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Remove variables
                bitmap = null;
                chosenStationName = null;
                chosenStationID = null;
                chosenStationLoc = null;
                gasolinePrice = 0;
                dieselPrice = 0;
                electricityPrice = 0;
                LPGPrice = 0;
                billPhoto = null;
                isAddingFuel = false;
                finish();
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
                    Toast.makeText(AddFuel.this, "Settings saved...", Toast.LENGTH_SHORT).show();
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(AddFuel.this);
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
            case FilePickerConst.REQUEST_CODE_PHOTO:
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
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chooseStation != null && chosenStationName != null) {
            chooseStation.setText(chosenStationName);
            updatePrices();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Remove variables
        bitmap = null;
        chosenStationName = null;
        chosenStationID = null;
        chosenStationLoc = null;
        gasolinePrice = 0;
        dieselPrice = 0;
        electricityPrice = 0;
        LPGPrice = 0;
        billPhoto = null;
        isAddingFuel = false;
        finish();
    }
}
