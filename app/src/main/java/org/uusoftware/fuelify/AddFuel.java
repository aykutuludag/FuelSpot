package org.uusoftware.fuelify;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;

import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.username;

public class AddFuel extends AppCompatActivity {

    public static String chosenStationName, chosenStationID;
    public static double gasolinePrice, dieselPrice, LPGPrice, electricityPrice;
    String UPLOAD_URL = "http://uusoftware.org/Fuelify/add-fuel.php";
    String question;
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
                        chooseTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        //1. YAKIT TİPİ
        chooseFuel = findViewById(R.id.radioGroup_fuel);
        expandableButton1 = findViewById(R.id.expandableButton1);
        switch (fuelPri) {
            case 0:
                chooseFuel.check(R.id.gasoline);
                break;
            case 1:
                chooseFuel.check(R.id.diesel);
                break;
            case 2:
                chooseFuel.check(R.id.lpg);
                break;
            case 3:
                chooseFuel.check(R.id.electricity);
                break;
            default:
                expandableLayoutYakit.setVisibility(View.GONE);
                expandableButton1.setVisibility(View.GONE);
                break;
        }

        //2. YAKIT TİPİ
        chooseFuel2 = findViewById(R.id.radioGroup_fuel2);
        expandableButton2 = findViewById(R.id.expandableButtonYakit2);
        switch (fuelSec) {
            case 0:
                chooseFuel2.check(R.id.gasoline2);
                break;
            case 1:
                chooseFuel2.check(R.id.diesel2);
                break;
            case 2:
                chooseFuel2.check(R.id.lpg2);
                break;
            case 3:
                chooseFuel2.check(R.id.electricity2);
                break;
            default:
                expandableLayoutYakit2.setVisibility(View.GONE);
                expandableButton2.setVisibility(View.GONE);
                break;
        }
    }

    public void getTime() {
        calendar = Calendar.getInstance();
        hour = calendar.getTime().getHours();
        minute = calendar.getTime().getMinutes();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) AddFuel.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    private void uploadData() {
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
                params.put("photo", bitmap.toString());
                params.put("question", question);
                params.put("username", username);
                params.put("user_photo", photo);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(AddFuel.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
    public void onResume() {
        super.onResume();
        if (chooseStation != null && chosenStationName != null) {
            chooseStation.setText(chosenStationName);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
