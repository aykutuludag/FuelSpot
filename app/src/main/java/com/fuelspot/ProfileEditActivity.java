package com.fuelspot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.fuelspot.MainActivity.GOOGLE_PLACE_AUTOCOMPLETE;
import static com.fuelspot.MainActivity.photo;

public class ProfileEditActivity extends AppCompatActivity {

    Toolbar toolbar;
    Window window;
    CircleImageView userPic;
    EditText editName, editMail, editLocation, editBirthday;
    RadioGroup editGender;
    RadioButton bMale, bFemale, bOther;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    int calendarYear, calendarMonth, calendarDay;
    Bitmap bitmap;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        requestQueue = Volley.newRequestQueue(ProfileEditActivity.this);

        editName = findViewById(R.id.editFullName);
        editMail = findViewById(R.id.editTextMail);
        editLocation = findViewById(R.id.editTextLocation);
        editBirthday = findViewById(R.id.editTextBirthday);
        editGender = findViewById(R.id.radioGroupGender);
        bMale = findViewById(R.id.genderMale);
        bFemale = findViewById(R.id.genderFemale);
        bOther = findViewById(R.id.genderOther);

        // Setting name
        editName.setText(MainActivity.name);

        // Setting email
        editMail.setText(MainActivity.email);
        editMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0 && s.toString().contains("@")) {
                    MainActivity.email = s.toString();
                }
            }
        });

        //UserPhoto
        userPic = findViewById(R.id.userPhoto);
        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);
        Glide.with(this).load(MainActivity.photo).apply(options).into(userPic);
        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyStoragePermissions(ProfileEditActivity.this)) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(ProfileEditActivity.this);
                } else {
                    ActivityCompat.requestPermissions(ProfileEditActivity.this, MainActivity.PERMISSIONS_STORAGE, MainActivity.REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

        //  Setting location and retrieving changes
        editLocation.setText(MainActivity.location);
        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AutocompleteFilter filter = new AutocompleteFilter.Builder().setCountry(MainActivity.userCountry).setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS).build();
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(filter).build(ProfileEditActivity.this);
                    startActivityForResult(intent, GOOGLE_PLACE_AUTOCOMPLETE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        //  Setting birthday and retrieving changes
        editBirthday.setText(MainActivity.birthday);
        if (MainActivity.birthday.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date birthDateasDate = sdf.parse(MainActivity.birthday);
                calendarYear = birthDateasDate.getYear() + 1900;
                calendarMonth = birthDateasDate.getMonth() + 1;
                calendarDay = birthDateasDate.getDate();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        editBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(ProfileEditActivity.this, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        MainActivity.birthday = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        editBirthday.setText(MainActivity.birthday);
                    }
                }, calendarYear, calendarMonth, calendarDay);

                datePicker.setTitle("Bir tarih seçin");
                datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Set", datePicker);
                datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", datePicker);
                datePicker.show();
            }
        });

        //  Set gender and retrieve changes
        if (MainActivity.gender.equals("male")) {
            bMale.setChecked(true);
        } else if (MainActivity.gender.equals("female")) {
            bFemale.setChecked(true);
        } else {
            bOther.setChecked(true);
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

    private void updateUserInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ProfileEditActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(ProfileEditActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
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
                params.put("location", MainActivity.location);
                params.put("country", MainActivity.userCountry);
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                } else {
                    params.put("photo", "http://fuel-spot.com/FUELSPOTAPP/uploads/userphotos/" + MainActivity.username + "-USERPHOTO.jpg");
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
                    editor.putString("Location", MainActivity.location);
                    editor.putString("Birthday", MainActivity.birthday);
                    editor.apply();
                    updateUserInfo();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "İnternet bağlantısında bir sorun var", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
        String fileName = now + ".jpg";

        switch (requestCode) {
            case GOOGLE_PLACE_AUTOCOMPLETE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    MainActivity.location = place.getAddress().toString();
                    editLocation.setText(MainActivity.location);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.i("Error", status.getStatusMessage());
                }
                break;
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    photo = aq.get(0);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/UserPhotos");
                    folder.mkdirs();

                    UCrop.of(Uri.parse("file://" + photo), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(ProfileEditActivity.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);
                        Glide.with(this).load(bitmap).apply(options).into(userPic);
                        editor.putString("ProfilePhoto", "http://fuel-spot.com/FUELSPOTAPP/uploads/userphotos/" + MainActivity.username + "-USERPHOTO.jpg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(ProfileEditActivity.this, cropError.toString(), Toast.LENGTH_LONG).show();
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