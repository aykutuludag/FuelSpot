package org.uusoftware.fuelify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.username;

public class AddFuel extends AppCompatActivity {

    public static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
    String UPLOAD_URL = "http://uusoftware.org/Fuelify/add-fuel.php";
    public static String chosenStationName, chosenStationID;
    public static double gasolinePrice, dieselPrice, LPGPrice, electricityPrice;
    Bitmap bitmap;
    Window window;
    Toolbar toolbar;
    RelativeLayout expandableLayoutYakit, expandableLayoutYakit2;
    Button expandableButton1, expandableButton2;
    EditText chooseStation, chooseTime;
    SharedPreferences prefs;
    Calendar calendar;
    RadioGroup chooseFuel, chooseFuel2;
    int hour, minute;
    long purchaseTime = System.currentTimeMillis();
    String fuelType, fuelType2 = "-";
    double totalPrice;
    double selectedUnitPrice, buyedLiter, entryPrice, selectedUnitPrice2, buyedLiter2, entryPrice2;
    EditText textViewLitreFiyati, textViewTotalFiyat, textViewLitre, textViewLitreFiyati2, textViewTotalFiyat2, textViewLitre2;
    ImageView photoHolder;
    Button sendVariables;

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
        chooseTime.setText(hour + ":" + minute);
        chooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddFuel.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(selectedHour, selectedMinute, new Date().getDate());
                        purchaseTime = calendar.getTimeInMillis();
                        chooseTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
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
                if (s != null && s.length() > 0) {
                    selectedUnitPrice = Double.parseDouble(s.toString());
                    buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
                    String literText = String.format("%.2f", buyedLiter) + "litre";
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
                    String literText = String.format("%.2f", buyedLiter) + "litre";
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
                if (s != null && s.length() > 0) {
                    selectedUnitPrice2 = Double.parseDouble(s.toString());
                    buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
                    String literText2 = String.format("%.2f", buyedLiter2) + " litre";
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
                    String literText2 = String.format("%.2f", buyedLiter2) + " litre";
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
                if (isNetworkConnected()) {
                    //Showing the progress dialog
                    final ProgressDialog loading = ProgressDialog.show(AddFuel.this, "Uploading...", "Please wait...", false, false);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    //Disimissing the progress dialog
                                    loading.dismiss();
                                    //Showing toast message of the response
                                    Toast.makeText(AddFuel.this, s, Toast.LENGTH_LONG).show();
                                    finish();
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
                            params.put("fuelType", fuelType);
                            params.put("fuelPrice", String.valueOf(selectedUnitPrice));
                            params.put("fuelLiter", String.valueOf(buyedLiter));
                            params.put("fuelType2", fuelType2);
                            params.put("fuelPrice2", String.valueOf(selectedUnitPrice2));
                            params.put("fuelLiter2", String.valueOf(buyedLiter2));
                            params.put("totalPrice", String.valueOf(totalPrice));
                            params.put("billPhoto", getStringImage(bitmap));

                            //returning parameters
                            return params;
                        }
                    };

                    //Creating a Request Queue
                    RequestQueue requestQueue = Volley.newRequestQueue(AddFuel.this);

                    //Adding request to the queue
                    requestQueue.add(stringRequest);
                } else {
                    Toast.makeText(AddFuel.this, "İnternet bağlantınızda bir sorun var!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updatePrices() {
        //1. YAKIT TİPİ
        switch (fuelPri) {
            case 0:
                chooseFuel.check(R.id.gasoline);
                selectedUnitPrice = gasolinePrice;
                textViewLitreFiyati.setText("" + gasolinePrice);
                fuelType = "gasoline";
                break;
            case 1:
                chooseFuel.check(R.id.diesel);
                selectedUnitPrice = dieselPrice;
                textViewLitreFiyati.setText("" + dieselPrice);
                fuelType = "diesel";
                break;
            case 2:
                chooseFuel.check(R.id.lpg);
                selectedUnitPrice = LPGPrice;
                textViewLitreFiyati.setText("" + LPGPrice);
                fuelType = "lpg";
                break;
            case 3:
                chooseFuel.check(R.id.electricity);
                selectedUnitPrice = electricityPrice;
                textViewLitreFiyati.setText("" + electricityPrice);
                fuelType = "electric";
                break;
            default:
                expandableLayoutYakit.setVisibility(View.GONE);
                expandableButton1.setVisibility(View.GONE);
                break;
        }

        //2. YAKIT TİPİ
        switch (fuelSec) {
            case 0:
                chooseFuel2.check(R.id.gasoline2);
                selectedUnitPrice2 = gasolinePrice;
                textViewLitreFiyati2.setText("" + gasolinePrice);
                fuelType2 = "gasoline";
                break;
            case 1:
                chooseFuel2.check(R.id.diesel2);
                selectedUnitPrice2 = dieselPrice;
                textViewLitreFiyati2.setText("" + dieselPrice);
                fuelType2 = "diesel";
                break;
            case 2:
                chooseFuel2.check(R.id.lpg2);
                selectedUnitPrice2 = LPGPrice;
                textViewLitreFiyati2.setText("" + LPGPrice);
                fuelType2 = "lpg";
                break;
            case 3:
                chooseFuel2.check(R.id.electricity2);
                selectedUnitPrice2 = electricityPrice;
                textViewLitreFiyati2.setText("" + electricityPrice);
                fuelType2 = "electric";
                break;
            default:
                expandableLayoutYakit2.setVisibility(View.GONE);
                expandableButton2.setVisibility(View.GONE);
                break;
        }

        buyedLiter = howManyLiter(selectedUnitPrice, entryPrice);
        String literText = String.format("%.2f", buyedLiter) + " " + "litre";
        textViewLitre.setText(literText);
        textViewTotalFiyat.setText("" + entryPrice);


        buyedLiter2 = howManyLiter(selectedUnitPrice2, entryPrice2);
        String literText2 = String.format("%.2f", buyedLiter2) + " " + "litre";
        textViewLitre2.setText(literText2);
        textViewTotalFiyat2.setText("" + entryPrice2);
    }

    public double howManyLiter(double priceForUnit, double totalPrice) {
        return totalPrice / priceForUnit;
    }

    public void getTime() {
        calendar = Calendar.getInstance();
        hour = calendar.getTime().getHours();
        minute = calendar.getTime().getMinutes();
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

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) AddFuel.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
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
                    carPhoto = aq.get(0);

                    System.out.println("file://" + carPhoto);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/Fuelify");
                    folder.mkdirs();

                    CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
                    String fileName = now + ".jpg";

                    UCrop.of(Uri.parse("file://" + carPhoto), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(9, 16)
                            .withMaxResultSize(1080, 1920)
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
        finish();
    }
}
