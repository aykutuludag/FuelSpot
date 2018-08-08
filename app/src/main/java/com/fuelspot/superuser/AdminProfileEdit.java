package com.fuelspot.superuser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.fuelspot.AnalyticsApplication;
import com.fuelspot.MainActivity;
import com.fuelspot.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.fuelspot.MainActivity.REQUEST_FILEPICKER;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;

public class AdminProfileEdit extends AppCompatActivity {

    Toolbar toolbar;
    Window window;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    int calendarYear, calendarMonth, calendarDay;
    Bitmap bitmap;
    RequestQueue requestQueue;
    RadioGroup editGender;
    RadioButton bMale, bFemale, bOther;
    EditText editTextEmail, editTextPhone, editTextBirthday;
    CircleImageView userPhoto;
    TextView textViewFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile_edit);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.brand_logo);


        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Profil düzenle");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        editor = prefs.edit();

        requestQueue = Volley.newRequestQueue(AdminProfileEdit.this);

        userPhoto = findViewById(R.id.userPhoto);
        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);
        Glide.with(this).load(MainActivity.photo).apply(options).into(userPhoto);
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(AdminProfileEdit.this)) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(AdminProfileEdit.this);
                } else {
                    ActivityCompat.requestPermissions(AdminProfileEdit.this, MainActivity.PERMISSIONS_FILEPICKER, MainActivity.REQUEST_FILEPICKER);
                }
            }
        });

        textViewFullName = findViewById(R.id.editFullName);
        textViewFullName.setText(MainActivity.name);

        editTextBirthday = findViewById(R.id.editTextBirthday);
        editTextBirthday.setText(MainActivity.birthday);
        if (MainActivity.birthday.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY", Locale.getDefault());
            try {
                Date birthDateasDate = sdf.parse(MainActivity.birthday);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(birthDateasDate);
                calendarYear = calendar.get(Calendar.YEAR);
                calendarMonth = calendar.get(Calendar.MONTH) + 1;
                calendarDay = calendar.get(Calendar.DATE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        editTextBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(AdminProfileEdit.this, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        MainActivity.birthday = pad(dayOfMonth) + "/" + pad(monthOfYear + 1) + "/" + year;
                        editTextBirthday.setText(MainActivity.birthday);
                    }
                }, calendarYear, calendarMonth, calendarDay);

                datePicker.setTitle("Bir tarih seçin");
                datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Set", datePicker);
                datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", datePicker);
                datePicker.show();
            }

            private String pad(int number) {
                String returnedValue;
                if (number < 10) {
                    returnedValue = "0" + number;
                } else {
                    returnedValue = String.valueOf(number);
                }
                return returnedValue;
            }
        });

        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPhone.setText(AdminMainActivity.userPhoneNumber);
        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    AdminMainActivity.userPhoneNumber = s.toString();
                }
            }
        });

        editTextEmail = findViewById(R.id.editTextMail);
        editTextEmail.setText(MainActivity.email);
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    MainActivity.email = s.toString();
                }
            }
        });

        //  Set gender and retrieve changes
        editGender = findViewById(R.id.radioGroupGender);
        bMale = findViewById(R.id.genderMale);
        bFemale = findViewById(R.id.genderFemale);
        bOther = findViewById(R.id.genderOther);
        switch (MainActivity.gender) {
            case "male":
                bMale.setChecked(true);
                break;
            case "female":
                bFemale.setChecked(true);
                break;
            default:
                bOther.setChecked(true);
                break;
        }
        editGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.genderMale) {
                    MainActivity.gender = "male";
                } else if (checkedId == R.id.genderFemale) {
                    MainActivity.gender = "female";
                } else {
                    MainActivity.gender = "transsexual";
                }
            }
        });
    }

    public void updateSuperUser() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_SUPERUSER_UPDATE_PROFILE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        // Register process ended redirect user to AdminMainActivity to wait verification process.
                        MainActivity.isSigned = true;
                        prefs.edit().putBoolean("isSigned", MainActivity.isSigned).apply();
                        MainActivity.isSuperUser = true;
                        prefs.edit().putBoolean("isSuperUser", MainActivity.isSuperUser).apply();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(AdminProfileEdit.this, AdminMainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }, 1500);
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
                params.put("username", MainActivity.username);
                params.put("email", MainActivity.email);
                params.put("gender", MainActivity.gender);
                params.put("birthday", MainActivity.birthday);
                params.put("phoneNumber", AdminMainActivity.userPhoneNumber);
                params.put("stationID", String.valueOf(AdminMainActivity.superStationID));
                params.put("googleID", AdminMainActivity.superGoogleID);
                params.put("stationName", AdminMainActivity.superStationName);
                params.put("stationLocation", AdminMainActivity.superStationLocation);
                params.put("stationAddress", AdminMainActivity.superStationAddress);
                params.put("stationLogo", AdminMainActivity.superStationLogo);
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                } else {
                    params.put("photo", "http://fuel-spot.com/FUELSPOTAPP/uploads/" + MainActivity.username + "-USERPHOTO.jpg");
                }

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
                finish();
                return true;
            case R.id.navigation_save:
                if (MainActivity.isNetworkConnected(this)) {
                    editor.putString("Email", MainActivity.email);
                    editor.putString("Gender", MainActivity.gender);
                    editor.putString("Birthday", MainActivity.birthday);
                    editor.putString("userPhoneNumber", AdminMainActivity.userPhoneNumber);
                    editor.apply();
                    updateSuperUser();
                } else {
                    Toast.makeText(AdminProfileEdit.this, "İnternet bağlantısında bir sorun var", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FILEPICKER: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(AdminProfileEdit.this, "Settings saved...", Toast.LENGTH_SHORT).show();
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(AdminProfileEdit.this);
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

        CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
        String fileName = now + ".jpg";

        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    photo = aq.get(0);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/UserPhotos");
                    folder.mkdirs();

                    UCrop.of(Uri.parse("file://" + photo), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(AdminProfileEdit.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);
                        Glide.with(this).load(bitmap).apply(options).into(userPhoto);
                        editor.putString("ProfilePhoto", "file://" + Environment.getExternalStorageDirectory() + "/FuelSpot/UserPhotos/" + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(AdminProfileEdit.this, cropError.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}