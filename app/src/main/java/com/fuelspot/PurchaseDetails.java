package com.fuelspot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.mapDefaultStationRange;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.username;

public class PurchaseDetails extends AppCompatActivity {

    private RequestOptions options;
    private MapView mMapView;
    private GoogleMap googleMap;
    private int purchaseID;
    private int isPurchaseVerified;
    private String stationName;
    private String stationLocation;
    private String plakaNo;

    private ImageView fatura;
    private RelativeTimeTextView tarih;
    private Bitmap bitmap;
    private Toolbar toolbar;
    private Window window;

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

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        purchaseID = getIntent().getIntExtra("PURCHASE_ID", 0);
        stationName = getIntent().getStringExtra("STATION_NAME");
        String iconURL = getIntent().getStringExtra("STATION_ICON");
        stationLocation = getIntent().getStringExtra("STATION_LOC");
        String purchaseTime = getIntent().getStringExtra("PURCHASE_TIME");
        int fuelType1 = getIntent().getIntExtra("FUEL_TYPE_1", -1);
        int fuelType2 = getIntent().getIntExtra("FUEL_TYPE_2", -1);
        String billPhoto = getIntent().getStringExtra("BILL_PHOTO");
        float fuelPrice1 = getIntent().getFloatExtra("FUEL_PRICE_1", 0);
        float fuelPrice2 = getIntent().getFloatExtra("FUEL_PRICE_2", 0);
        float fuelLiter1 = getIntent().getFloatExtra("FUEL_LITER_1", 0);
        float fuelLiter2 = getIntent().getFloatExtra("FUEL_LITER_2", 0);
        float fuelTax1 = getIntent().getFloatExtra("FUEL_TAX_1", 0);
        float fuelTax2 = getIntent().getFloatExtra("FUEL_TAX_2", 0);
        float totalPrice = getIntent().getFloatExtra("TOTAL_PRICE", 0);
        plakaNo = getIntent().getStringExtra("PLATE_NO");
        isPurchaseVerified = getIntent().getIntExtra("IS_PURCHASE_VERIFIED", 0);

        ImageView istasyonLogo = findViewById(R.id.imageViewStationLogo);
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
        tarih = findViewById(R.id.purchaseTime);

        options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
        Glide.with(this).load(billPhoto).apply(options).into(fatura);
        fatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPurchaseVerified == 1) {
                    Toast.makeText(PurchaseDetails.this, "Onaylanmış siparişlerde değişiklik yapılamaz...", Toast.LENGTH_LONG).show();
                } else {
                    if (MainActivity.verifyFilePickerPermission(PurchaseDetails.this)) {
                        ImagePicker.cameraOnly().start(PurchaseDetails.this);
                    } else {
                        ActivityCompat.requestPermissions(PurchaseDetails.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                    }
                }
            }
        });

        if (isPurchaseVerified == 1) {
            circleImageViewStatus.setBackgroundResource(R.drawable.verified);
            textViewStatus.setText("Satın alma onaylandı! Bonus hesabınıza yansıtılmıştır.");
        } else {
            if (billPhoto != null && billPhoto.length() > 0) {
                circleImageViewStatus.setBackgroundResource(R.drawable.question);
                textViewStatus.setText("Satın alma incelemede! Onaylandığı takdirde bonus hesabınıza yansıtılacaktır.");
            } else {
                circleImageViewStatus.setBackgroundResource(R.drawable.money);
                textViewStatus.setText("Bonus kazanmak için fiş/fatura fotoğrafı ekle!");
            }
        }

        Glide.with(this).load(iconURL).into(istasyonLogo);
        switch (fuelType1) {
            case 0:
                Glide.with(this).load(R.drawable.gasoline).into(tur1);
                break;
            case 1:
                Glide.with(this).load(R.drawable.diesel).into(tur1);
                break;
            case 2:
                Glide.with(this).load(R.drawable.lpg).into(tur1);
                break;
            case 3:
                Glide.with(this).load(R.drawable.electricity).into(tur1);
                break;
        }
        birimFiyat1.setText(fuelPrice1 + " " + currencySymbol);
        litre1.setText(fuelLiter1 + " " + userUnit);
        int priceOne = (int) (fuelPrice1 * fuelLiter1);
        String priceHolder = priceOne + " " + currencySymbol;
        fiyat1.setText(priceHolder);

        if (fuelType2 != -1) {
            switch (fuelType2) {
                case 0:
                    Glide.with(this).load(R.drawable.gasoline).into(tur2);
                    break;
                case 1:
                    Glide.with(this).load(R.drawable.diesel).into(tur2);
                    break;
                case 2:
                    Glide.with(this).load(R.drawable.lpg).into(tur2);
                    break;
                case 3:
                    Glide.with(this).load(R.drawable.electricity).into(tur2);
                    break;
            }
            birimFiyat2.setText(fuelPrice2 + " " + currencySymbol);
            litre2.setText(fuelLiter2 + " " + userUnit);
            int priceTwo = (int) (fuelPrice2 * fuelLiter2);
            String priceHolder2 = priceTwo + " " + currencySymbol;
            fiyat2.setText(priceHolder2);
        } else {
            tur2.setVisibility(View.GONE);
            fiyat2.setVisibility(View.GONE);
            litre2.setVisibility(View.GONE);
        }

        float tax1 = fuelPrice1 * fuelLiter1 * fuelTax1;
        float tax2 = fuelPrice2 * fuelLiter2 * fuelTax2;
        String taxHolder = "VERGİ: " + String.format(Locale.getDefault(), "%.2f", tax1 + tax2) + " " + currencySymbol;
        vergi.setText(taxHolder);

        String totalHolder = "TOPLAM : " + String.format(Locale.getDefault(), "%.2f", totalPrice) + " " + currencySymbol;
        toplamfiyat.setText(totalHolder);

        SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
        try {
            Date date = format.parse(purchaseTime);
            tarih.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Satın alma silinecek", Snackbar.LENGTH_LONG)
                        .setAction("SİL", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isPurchaseVerified == 1) {
                                    Toast.makeText(PurchaseDetails.this, "Onaylanmış siparişlerde değişiklik yapılamaz...", Toast.LENGTH_LONG).show();
                                } else {
                                    deletePurchase();
                                }
                            }
                        }).show();
            }
        });

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
                LatLng mStationLoc = new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1]));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(mStationLoc).zoom(16f).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition
                        (cameraPosition));
                googleMap.addMarker(new MarkerOptions().position(mStationLoc).title(stationName).snippet(tarih.getText().toString()));
                //Draw a circle with radius of mapDefaultRange
                googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1])))
                        .radius(mapDefaultStationRange)
                        .fillColor(0x220000FF)
                        .strokeColor(Color.parseColor("#FF5635")));
            }
        });
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
                                    finish();
                                    break;
                                case "Fail":
                                    Toast.makeText(PurchaseDetails.this, "Bir hata oluştu. Lütfen tekrar deneyiniz.", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(PurchaseDetails.this, "Bir hata oluştu. Lütfen tekrar deneyiniz.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(PurchaseDetails.this, "Bir hata oluştu. Lütfen tekrar deneyiniz.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("purchaseID", String.valueOf(purchaseID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);

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
                                    Toast.makeText(PurchaseDetails.this, "Satın alma güncellendi...", Toast.LENGTH_LONG).show();
                                    finish();
                                    break;
                                case "Fail":
                                    Toast.makeText(PurchaseDetails.this, "An error occured. Try again later...", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(PurchaseDetails.this, "An error occured. Try again later...", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PurchaseDetails.this, "An error occured. Try again later...", Toast.LENGTH_LONG).show();
                    }
                }) {
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
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);

        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public Bitmap resizeAndRotate(Bitmap bmp, float degrees) {
        if (bmp.getWidth() > 1080 || bmp.getHeight() > 1920) {
            float aspectRatio = (float) bmp.getWidth() / bmp.getHeight();
            int width, height;

            if (aspectRatio < 1) {
                // Portrait
                width = (int) (aspectRatio * 1920);
                height = (int) (width * (1f / aspectRatio));
            } else {
                // Landscape
                width = (int) (aspectRatio * 1080);
                height = (int) (width * (1f / aspectRatio));
            }

            bmp = Bitmap.createScaledBitmap(bmp, width, height, true);
        }

        if (degrees != 0) {
            return rotate(bmp, degrees);
        } else {
            return bmp;
        }
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
