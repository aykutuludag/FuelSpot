package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fuelspot.adapter.MarkerAdapter;
import com.fuelspot.model.PurchaseItem;
import com.fuelspot.model.StationItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.FragmentAutomobile.dummyPurchaseList;
import static com.fuelspot.FragmentAutomobile.vehiclePurchaseList;
import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.getStringImage;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.resizeAndRotate;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;

public class PurchaseDetails extends AppCompatActivity {

    Button addBillPhotoButton;
    RequestQueue requestQueue;
    FloatingActionButton fab;
    StationItem info = new StationItem();
    ImageView istasyonLogo;
    private RequestOptions options;
    private MapView mMapView;
    private GoogleMap googleMap;
    private int purchaseID;
    private int isPurchaseVerified;
    private String plakaNo;
    private ImageView fatura;
    private Bitmap bitmap;
    private Toolbar toolbar;
    private Window window;
    private int stationID;
    SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
    private int remainingHour, remainingMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_details);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        // Analytics
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName("Satın alma detay");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        requestQueue = Volley.newRequestQueue(this);
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        // Variables from Intent
        purchaseID = getIntent().getIntExtra("PURCHASE_ID", 0);
        plakaNo = getIntent().getStringExtra("PLATE_NO");
        stationID = getIntent().getIntExtra("STATION_ID", 0);
        int fuelType1 = getIntent().getIntExtra("FUEL_TYPE_1", -1);
        int fuelType2 = getIntent().getIntExtra("FUEL_TYPE_2", -1);
        String billPhoto = getIntent().getStringExtra("BILL_PHOTO");
        float fuelPrice1 = getIntent().getFloatExtra("FUEL_PRICE_1", 0);
        float fuelPrice2 = getIntent().getFloatExtra("FUEL_PRICE_2", 0);
        float fuelLiter1 = getIntent().getFloatExtra("FUEL_LITER_1", 0);
        float fuelLiter2 = getIntent().getFloatExtra("FUEL_LITER_2", 0);
        float fuelTax1 = getIntent().getFloatExtra("FUEL_TAX_1", 0);
        float fuelTax2 = getIntent().getFloatExtra("FUEL_TAX_2", 0);
        float subTotal = getIntent().getFloatExtra("SUB_TOTAL", 0);
        float subTotal2 = getIntent().getFloatExtra("SUB_TOTAL_2", 0);
        float totalPrice = getIntent().getFloatExtra("TOTAL_PRICE", 0);
        float bonus = getIntent().getFloatExtra("BONUS", 0);
        isPurchaseVerified = getIntent().getIntExtra("IS_PURCHASE_VERIFIED", 0);
        String purchaseTime = getIntent().getStringExtra("PURCHASE_TIME");
        // Variables from Intent

        // Remaining time for uploading bill photo
        try {
            Date date = format.parse(purchaseTime);
            Date dateNow = new Date(System.currentTimeMillis());

            int howManyHourPassed = (int) (dateNow.getTime() - date.getTime()) / (1000 * 60 * 60);
            int howManyMinPassed = (int) (dateNow.getTime() - date.getTime() - (remainingHour * 1000 * 60 * 60)) / (1000 * 60);

            if (howManyHourPassed <= 23 || howManyMinPassed <= 59) {
                remainingHour = howManyHourPassed;
                remainingMin = howManyMinPassed;
            } else {
                remainingHour = 0;
                remainingMin = 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        istasyonLogo = findViewById(R.id.imageViewStationLogo);
        CircleImageView circleImageViewStatus = findViewById(R.id.statusIcon);
        TextView textViewStatus = findViewById(R.id.statusText);
        fatura = findViewById(R.id.billPhoto);
        ImageView tur1 = findViewById(R.id.type1);
        ImageView tur2 = findViewById(R.id.type2);
        TextView fiyat1 = findViewById(R.id.price1);
        TextView birimFiyat1 = findViewById(R.id.unitPrice1);
        TextView birimFiyat2 = findViewById(R.id.unitPrice2);
        TextView litre1 = findViewById(R.id.amount1);
        TextView fiyat2 = findViewById(R.id.price2);
        TextView litre2 = findViewById(R.id.amount2);
        TextView vergi = findViewById(R.id.totalTax);
        TextView toplamfiyat = findViewById(R.id.totalPrice);
        RelativeTimeTextView tarih = findViewById(R.id.purchaseTime);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Satın alma silinecek", Snackbar.LENGTH_LONG)
                        .setAction("SİL", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isPurchaseVerified == 1) {
                                    Toast.makeText(PurchaseDetails.this, "Onaylanmış satın almalarda değişiklik yapılamaz...", Toast.LENGTH_LONG).show();
                                } else {
                                    deletePurchase();
                                }
                            }
                        }).show();
            }
        });

        options = new RequestOptions().centerCrop().placeholder(R.drawable.icon_upload).error(R.drawable.icon_upload).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
        if (billPhoto != null && billPhoto.length() > 0) {
            fatura.setVisibility(View.VISIBLE);
            Glide.with(this).load(billPhoto).apply(options).into(fatura);
        } else {
            fatura.setVisibility(View.GONE);
        }

        addBillPhotoButton = findViewById(R.id.button_add_bill);
        addBillPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPurchaseVerified == 1) {
                    Toast.makeText(PurchaseDetails.this, "Onaylanmış satın almalarda değişiklik yapılamaz...", Toast.LENGTH_LONG).show();
                } else {
                    if (remainingHour > 0 || remainingMin > 0) {
                        if (MainActivity.verifyFilePickerPermission(PurchaseDetails.this)) {
                            ImagePicker.cameraOnly().start(PurchaseDetails.this);
                        } else {
                            ActivityCompat.requestPermissions(PurchaseDetails.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                        }
                    } else {
                        Toast.makeText(PurchaseDetails.this, "Üzerinden 24 saatten fazla süre geçmiş satınalmalarda değişiklik yapılamaz...", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if (isPurchaseVerified == -1) {
            Glide.with(this).load(R.drawable.cancel).apply(options).into(circleImageViewStatus);

            textViewStatus.setText("Satınalma onaylanmadı. Detaylar için gelen kutusuna bakınız.");

            addBillPhotoButton.setVisibility(View.GONE);
        } else if (isPurchaseVerified == 1) {
            Glide.with(this).load(R.drawable.verified).apply(options).into(circleImageViewStatus);

            textViewStatus.setText("Satınalma onaylandı! " + String.format(Locale.getDefault(), "%.2f", bonus) + " FP bonus hesabınıza yansıtılmıştır.");
            fab.hide();

            addBillPhotoButton.setVisibility(View.GONE);
        } else {
            if (billPhoto != null && billPhoto.length() > 0) {
                Glide.with(this).load(R.drawable.ic_waiting).apply(options).into(circleImageViewStatus);

                textViewStatus.setText("Satınalma incelemede! Onaylandığı takdirde bonus hesabınıza yansıtılacaktır.");

                addBillPhotoButton.setVisibility(View.VISIBLE);
                addBillPhotoButton.setText("Fotoğrafı güncelle");
            } else {
                if (remainingHour > 0 || remainingMin > 0) {
                    Glide.with(this).load(R.drawable.money).apply(options).into(circleImageViewStatus);

                    textViewStatus.setText("Fiş/Fatura fotoğrafı ekle, " + String.format(Locale.getDefault(), "%.2f", totalPrice / 100f) + " FP bonus kazan! Fotoğraf eklemek için son " + remainingHour + " saat " + remainingMin + " dakika!");
                    addBillPhotoButton.setText("Fotoğrafı güncelle");
                    addBillPhotoButton.setText("Fotoğraf ekle");
                } else {
                    Glide.with(this).load(R.drawable.cancel).apply(options).into(circleImageViewStatus);
                    addBillPhotoButton.setVisibility(View.GONE);
                    textViewStatus.setText("Üzerinden 24 saatten fazla süre geçmiş satınalmalarda fiş/fatura eklenemez...");
                }
            }
        }

        switch (fuelType1) {
            case 0:
                Glide.with(this).load(R.drawable.fuel_gasoline).into(tur1);
                break;
            case 1:
                Glide.with(this).load(R.drawable.fuel_diesel).into(tur1);
                break;
            case 2:
                Glide.with(this).load(R.drawable.fuel_lpg).into(tur1);
                break;
            case 3:
                Glide.with(this).load(R.drawable.fuel_electricity).into(tur1);
                break;
        }
        birimFiyat1.setText(fuelPrice1 + " " + currencySymbol);
        litre1.setText(fuelLiter1 + " " + userUnit);
        String priceHolder = String.format(Locale.getDefault(), "%.2f", subTotal) + " " + currencySymbol;
        fiyat1.setText(priceHolder);

        if (fuelType2 != -1) {
            switch (fuelType2) {
                case 0:
                    Glide.with(this).load(R.drawable.fuel_gasoline).into(tur2);
                    break;
                case 1:
                    Glide.with(this).load(R.drawable.fuel_diesel).into(tur2);
                    break;
                case 2:
                    Glide.with(this).load(R.drawable.fuel_lpg).into(tur2);
                    break;
                case 3:
                    Glide.with(this).load(R.drawable.fuel_electricity).into(tur2);
                    break;
            }
            birimFiyat2.setText(fuelPrice2 + " " + currencySymbol);
            litre2.setText(fuelLiter2 + " " + userUnit);
            String priceHolder2 = String.format(Locale.getDefault(), "%.2f", subTotal2) + " " + currencySymbol;
            fiyat2.setText(priceHolder2);
        } else {
            tur2.setVisibility(View.GONE);
            fiyat2.setVisibility(View.GONE);
            litre2.setVisibility(View.GONE);
        }

        float totalVergiMoney = subTotal * fuelTax1 + subTotal2 * fuelTax2;
        String taxHolder = "VERGİ: " + String.format(Locale.getDefault(), "%.2f", totalVergiMoney) + " " + currencySymbol;
        vergi.setText(taxHolder);

        String totalHolder = "TOPLAM : " + String.format(Locale.getDefault(), "%.2f", totalPrice) + " " + currencySymbol;
        toplamfiyat.setText(totalHolder);

        try {
            Date date = format.parse(purchaseTime);
            tarih.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(PurchaseDetails.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PurchaseDetails.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PurchaseDetails.this, new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
        } else {
            loadMap();
        }
    }

    private void loadMap() {
        //Detect location and set on map
        MapsInitializer.initialize(this.getApplicationContext());
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);
                googleMap.setTrafficEnabled(true);

                MarkerAdapter customInfoWindow = new MarkerAdapter(PurchaseDetails.this);
                googleMap.setInfoWindowAdapter(customInfoWindow);
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        StationItem infoWindowData = (StationItem) marker.getTag();
                        openStation(infoWindowData);
                    }
                });

                fetchStation(stationID);
            }
        });
    }

    private void fetchStation(final int stationID) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_STATION) + "?stationID=" + stationID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            info.setID(obj.getInt("id"));
                            info.setStationName(obj.getString("name"));
                            info.setVicinity(obj.getString("vicinity"));
                            info.setCountryCode(obj.getString("country"));
                            info.setLocation(obj.getString("location"));
                            info.setGoogleMapID(obj.getString("googleID"));
                            info.setFacilities(obj.getString("facilities"));
                            info.setLicenseNo(obj.getString("licenseNo"));
                            info.setOwner(obj.getString("owner"));
                            info.setPhotoURL(obj.getString("logoURL"));
                            info.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                            info.setDieselPrice((float) obj.getDouble("dieselPrice"));
                            info.setLpgPrice((float) obj.getDouble("lpgPrice"));
                            info.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                            info.setOtherFuels(obj.getString("otherFuels"));
                            info.setIsVerified(obj.getInt("isVerified"));
                            info.setLastUpdated(obj.getString("lastUpdated"));

                            // DISTANCE
                            String[] locationHolder = info.getLocation().split(";");
                            LatLng sydney = new LatLng(Double.parseDouble(locationHolder[0]), Double.parseDouble(locationHolder[1]));
                            Location loc1 = new Location("");
                            loc1.setLatitude(Double.parseDouble(userlat));
                            loc1.setLongitude(Double.parseDouble(userlon));
                            Location loc2 = new Location("");
                            loc2.setLatitude(sydney.latitude);
                            loc2.setLongitude(sydney.longitude);
                            info.setDistance((int) loc1.distanceTo(loc2));
                            // DISTANCE

                            Glide.with(PurchaseDetails.this).load(info.getPhotoURL()).apply(options).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    info.setStationLogoDrawable(resource);
                                    return false;
                                }
                            }).into(istasyonLogo);

                            // We are waiting for loading logos
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    addMarker();
                                }
                            }, 1000);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(PurchaseDetails.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void addMarker() {
        Marker m;
        LatLng sydney = new LatLng(Double.parseDouble(info.getLocation().split(";")[0]), Double.parseDouble(info.getLocation().split(";")[1]));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(16f).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.addCircle(new CircleOptions()
                .center(sydney)
                .radius(mapDefaultStationRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));

        if (isStationVerified == 1) {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(info.getStationName()).snippet(info.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
            m = googleMap.addMarker(mOptions);
            m.setTag(info);
        } else {
            MarkerOptions mOptions = new MarkerOptions().position(sydney).title(info.getStationName()).snippet(info.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.distance));
            m = googleMap.addMarker(mOptions);
            m.setTag(info);
        }
        m.showInfoWindow();
    }

    private void openStation(StationItem feedItemList) {
        Intent intent = new Intent(PurchaseDetails.this, StationDetails.class);
        intent.putExtra("STATION_ID", feedItemList.getID());
        intent.putExtra("STATION_NAME", feedItemList.getStationName());
        intent.putExtra("STATION_VICINITY", feedItemList.getVicinity());
        intent.putExtra("STATION_LOCATION", feedItemList.getLocation());
        intent.putExtra("STATION_DISTANCE", feedItemList.getDistance());
        intent.putExtra("STATION_LASTUPDATED", feedItemList.getLastUpdated());
        intent.putExtra("STATION_GASOLINE", feedItemList.getGasolinePrice());
        intent.putExtra("STATION_DIESEL", feedItemList.getDieselPrice());
        intent.putExtra("STATION_LPG", feedItemList.getLpgPrice());
        intent.putExtra("STATION_ELECTRIC", feedItemList.getElectricityPrice());
        intent.putExtra("STATION_OTHER_FUELS", feedItemList.getOtherFuels());
        intent.putExtra("STATION_ICON", feedItemList.getPhotoURL());
        intent.putExtra("IS_VERIFIED", feedItemList.getIsVerified());
        intent.putExtra("STATION_FACILITIES", feedItemList.getFacilities());
        showAds(PurchaseDetails.this, intent);
    }

    private void deletePurchase() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.getString(R.string.API_DELETE_PURCHASE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    Toast.makeText(PurchaseDetails.this, "Satın alma silindi", Toast.LENGTH_LONG).show();
                                    fetchVehiclePurchases();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            finish();
                                        }
                                    }, 1000);
                                    break;
                                case "Fail":
                                    Toast.makeText(PurchaseDetails.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(PurchaseDetails.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(PurchaseDetails.this, getString(R.string.error), Toast.LENGTH_LONG).show();
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
                params.put("purchaseID", String.valueOf(purchaseID));

                //returning parameters
                return params;
            }
        };


        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updatePurchase() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.getString(R.string.API_UPDATE_PURCHASE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    Toast.makeText(PurchaseDetails.this, "Satınalma güncellendi...", Toast.LENGTH_LONG).show();
                                    fetchVehiclePurchases();
                                    break;
                                case "Fail":
                                    Toast.makeText(PurchaseDetails.this, "Bir hata oluştu. Lütfen daha sonra tekrar deneyiniz...", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(PurchaseDetails.this, "Bir hata oluştu. Lütfen daha sonra tekrar deneyiniz...", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(PurchaseDetails.this, "Bir hata oluştu. Lütfen daha sonra tekrar deneyiniz....", Toast.LENGTH_LONG).show();
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
                params.put("purchaseID", String.valueOf(purchaseID));
                params.put("username", username);
                params.put("plateNO", plakaNo);
                if (bitmap != null) {
                    params.put("billPhoto", getStringImage(bitmap));
                } else {
                    params.put("billPhoto", "");
                }

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchVehiclePurchases() {
        dummyPurchaseList.clear();
        vehiclePurchaseList.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_AUTOMOBILE_PURCHASES) + "?plateNo=" + plateNo,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    PurchaseItem item = new PurchaseItem();
                                    item.setID(obj.getInt("id"));
                                    item.setPurchaseTime(obj.getString("time"));
                                    item.setStationID(obj.getInt("stationID"));
                                    item.setStationName(obj.getString("stationName"));
                                    item.setStationIcon(obj.getString("stationIcon"));
                                    item.setStationLocation(obj.getString("stationLocation"));
                                    item.setPlateNo(obj.getString("plateNo"));
                                    item.setFuelType(obj.getInt("fuelType"));
                                    item.setFuelPrice((float) obj.getDouble("fuelPrice"));
                                    item.setFuelLiter((float) obj.getDouble("fuelLiter"));
                                    item.setFuelTax((float) obj.getDouble("fuelTax"));
                                    item.setSubTotal((float) obj.getDouble("subTotal"));
                                    item.setFuelType2(obj.getInt("fuelType2"));
                                    item.setFuelPrice2((float) obj.getDouble("fuelPrice2"));
                                    item.setFuelLiter2((float) obj.getDouble("fuelLiter2"));
                                    item.setFuelTax2((float) obj.getDouble("fuelTax2"));
                                    item.setSubTotal2((float) obj.getDouble("subTotal2"));
                                    item.setBonus((float) obj.getDouble("bonus"));
                                    item.setTotalPrice((float) obj.getDouble("totalPrice"));
                                    item.setBillPhoto(obj.getString("billPhoto"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setKilometer(obj.getInt("kilometer"));
                                    vehiclePurchaseList.add(item);

                                    if (i < 3) {
                                        dummyPurchaseList.add(item);
                                    }
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
                        //Showing toast
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (ActivityCompat.checkSelfPermission(PurchaseDetails.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.create(PurchaseDetails.this).single().start();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
                }
            }
            case REQUEST_LOCATION: {
                if (ActivityCompat.checkSelfPermission(PurchaseDetails.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        loadMap();
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
                }
            }
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
                    Glide.with(this).load(bitmap).apply(options).into(fatura);
                    fatura.setVisibility(View.VISIBLE);
                    updatePurchase();
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
