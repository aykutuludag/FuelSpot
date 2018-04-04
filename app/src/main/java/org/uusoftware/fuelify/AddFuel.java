package org.uusoftware.fuelify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import eu.amirs.JSON;

import static org.uusoftware.fuelify.MainActivity.mCurrentLocation;
import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.userlat;
import static org.uusoftware.fuelify.MainActivity.userlon;
import static org.uusoftware.fuelify.MainActivity.username;

public class AddFuel extends AppCompatActivity {

    String UPLOAD_URL = "http://uusoftware.org/Fuelify/add-fuel.php";
    String question;
    Button shareButton;
    Bitmap bitmap;
    EditText questionText;
    Window window;
    Toolbar toolbar;


    ExpandableRelativeLayout expandableLayout1, expandableLayout2, expandableLayout3;
    List<Float> distance = new ArrayList<>(99);
    String[] stationName = new String[99];
    String[] placeID = new String[99];
    String[] vicinity = new String[99];
    String[] location = new String[99];
    String[] photoURLs = new String[99];
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfuel);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        expandableLayout1 = findViewById(R.id.expandableLayout1);
        expandableLayout2 = findViewById(R.id.expandableLayout2);
        expandableLayout3 = findViewById(R.id.expandableLayout3);

        queue = Volley.newRequestQueue(AddFuel.this);

        if (isNetworkConnected()) {
            getLocation();
        } else {
            Toast.makeText(AddFuel.this, "İNTERNET BAĞLANTIIS YOK", Toast.LENGTH_LONG).show();
        }

       /*

        questionText = findViewById(R.id.questionHolder);
        questionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    question = editable.toString();
                }
            }
        });

        shareButton = findViewById(R.id.shareIt);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (question == null) {
                    Toast.makeText(UploadActivity.this, "Soru çok kısa. Bir şeyler daha yazmak ister misin?", Toast.LENGTH_SHORT).show();
                } else if (bitmap == null) {
                    Toast.makeText(UploadActivity.this, "Lütfen geçerli bir görsel seçiniz", Toast.LENGTH_SHORT).show();
                } else {
                    if (isNetworkConnected()) {
                        uploadImage();
                    } else {
                        Toast.makeText(UploadActivity.this, "İnternet bağlantınızda bir sorun var!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });*/
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void buttonClick1(View view) {
        expandableLayout1.toggle(); // toggle expand and collapse
        expandableLayout2.collapse();
        expandableLayout3.collapse();
    }

    public void buttonClick2(View view) {
        expandableLayout2.toggle(); // toggle expand and collapse
        expandableLayout1.collapse();
        expandableLayout3.collapse();
    }

    public void buttonClick3(View view) {
        expandableLayout3.toggle(); // toggle expand and collapse
        expandableLayout1.collapse();
        expandableLayout2.collapse();
    }

    public void getLocation() {
        if (ContextCompat.checkSelfPermission(AddFuel.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AddFuel.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(AddFuel.this)
                        .setTitle("Konum izni gerekiyor")
                        .setMessage("Size en yakın benzinlikleri ve fiyatlarını gösterebilmemiz için konum iznine ihtiyaç duyuyoruz")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(AddFuel.this, new String[]
                                        {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(AddFuel.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            //Request location updates:
            LocationManager locationManager = (LocationManager)
                    AddFuel.this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }

            findNearStations();
        }
    }

    public void findNearStations() {
        //Search stations in a radius of 3000m
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + mCurrentLocation.latitude + "," + mCurrentLocation.longitude + "&radius=3000&type=gas_station&opennow=true&key=AIzaSyAOE5dwDvW_IOVmw-Plp9y5FLD9_1qb4vc";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        for (int i = 0; i < json.key("results").count(); i++) {
                            stationName[i] = json.key("results").index(i).key("name").stringValue();
                            vicinity[i] = json.key("results").index(i).key("vicinity").stringValue();
                            placeID[i] = json.key("results").index(i).key("place_id").stringValue();

                            double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                            double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();
                            location[i] = lat + ";" + lon;

                            //DISTANCE START
                            Location loc1 = new Location("");
                            loc1.setLatitude(lat);
                            loc1.setLongitude(lon);

                            Location loc2 = new Location("");
                            loc2.setLatitude(userlat);
                            loc2.setLongitude(userlon);
                            float distanceInMeters = loc1.distanceTo(loc2);
                            distance.add(i, distanceInMeters);
                            //DISTANCE END

                            photoURLs[i] = "https://maps.gstatic.com/mapfiles/place_api/icons/gas_station-71.png";

                            registerStations(stationName[i], vicinity[i], location[i], placeID[i], photoURLs[i]);
                        }

                        float min = Collections.min(distance);

                        if (min <= 250) {
                            int index = distance.indexOf(min);

                            System.out.print("İSTASYON ADI: " + stationName[index]);
                            System.out.print("GOOGLE ID: " + placeID[index]);
                            System.out.println("MESAFE: " + min);
                            fetchPrices(placeID[index]);
                        } else {
                            Toast.makeText(AddFuel.this, "Konumunuz şu an hiçbir benzinlikte görünmüyor. Lütfen istasyon seçiniz", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void registerStations(final String name, final String vicinity, final String location, final String placeID, final String photoURL) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://uusoftware.org/Fuelify/add-station.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(AddFuel.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("name", name);
                params.put("vicinity", vicinity);
                params.put("location", location);
                params.put("googleID", placeID);
                params.put("photoURL", photoURL);
                params.put("timeStamp", String.valueOf(System.currentTimeMillis()));

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(AddFuel.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchPrices(final String placeID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://uusoftware.org/Fuelify/fetch-prices.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            //BURADA İSTASYON BİLGİLERİNİ ÇEKİP SET EDECEĞİZ.
                           /* item.setID(obj.getInt("id"));
                            item.setStationName(obj.getString("name"));
                            item.setGasolinePrice(obj.getDouble("gasolinePrice"));
                            item.setDieselPrice(obj.getDouble("dieselPrice"));
                            item.setLpgPrice(obj.getDouble("lpgPrice"));
                            item.setElectricityPrice(obj.getDouble("electricityPrice"));*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(AddFuel.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("placeID", placeID);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        queue.add(stringRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(AddFuel.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(AddFuel.this, "İZİN VERİLDİ", Toast.LENGTH_LONG).show();
                        //Request location updates:
                        LocationManager locationManager = (LocationManager)
                                AddFuel.this.getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();

                        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                        userlat = location.getLatitude();
                        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        findNearStations();
                    }
                } else {
                    Toast.makeText(AddFuel.this, "İZİN VERİLMEDİ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
