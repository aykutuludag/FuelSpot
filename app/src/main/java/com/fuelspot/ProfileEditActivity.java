package com.fuelspot;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;

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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.location;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.username;

public class ProfileEditActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Window window;
    private CircleImageView userPic;
    private EditText editBirthday;
    private SharedPreferences.Editor editor;
    private int calendarYear;
    private int calendarMonth;
    private int calendarDay;
    private Bitmap bitmap;
    private RequestQueue requestQueue;
    private RequestOptions options;

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

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
        t.setScreenName("Profil düzenle");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        SharedPreferences prefs = this.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
        editor = prefs.edit();

        requestQueue = Volley.newRequestQueue(ProfileEditActivity.this);

        TextView textViewUsername = findViewById(R.id.userUsername);
        textViewUsername.setText(username);
        EditText editName = findViewById(R.id.editFullName);
        EditText editMail = findViewById(R.id.editTextMail);
        EditText editLocation = findViewById(R.id.editTextLocation);
        editBirthday = findViewById(R.id.editTextBirthday);
        RadioGroup editGender = findViewById(R.id.radioGroupGender);
        RadioButton bMale = findViewById(R.id.genderMale);
        RadioButton bFemale = findViewById(R.id.genderFemale);
        RadioButton bOther = findViewById(R.id.genderOther);

        // Setting name
        editName.setText(name);
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    name = s.toString();
                    editor.putString("Name", name);
                }
            }
        });

        // Setting email
        editMail.setText(email);
        editMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    if (s.toString().contains("@")) {
                        email = s.toString();
                        editor.putString("Email", email);
                    }
                }
            }
        });

        //UserPhoto
        userPic = findViewById(R.id.receiverPhoto);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
        Glide.with(this).load(photo).apply(options).into(userPic);
        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyFilePickerPermission(ProfileEditActivity.this)) {
                    ImagePicker.create(ProfileEditActivity.this).single().start();
                } else {
                    ActivityCompat.requestPermissions(ProfileEditActivity.this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        //  Setting location and retrieving changes
        editLocation.setText(location);
        editLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    location = s.toString();
                    editor.putString("Location", location);
                }
            }
        });

        EditText editTextPhone = findViewById(R.id.editTextPhone);
        editTextPhone.setText(userPhoneNumber);
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
                    userPhoneNumber = s.toString();
                    editor.putString("userPhoneNumber", userPhoneNumber);
                }
            }
        });

        //  Setting birthday and retrieving changes
        editBirthday.setText(birthday);
        if (birthday.length() > 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date birthDateasDate = sdf.parse(birthday);
                Calendar cal = Calendar.getInstance();
                cal.setTime(birthDateasDate);
                calendarYear = cal.get(Calendar.YEAR);
                calendarMonth = cal.get(Calendar.MONTH);
                calendarDay = cal.get(Calendar.DAY_OF_WEEK);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Calendar cal = Calendar.getInstance();
            calendarYear = cal.get(Calendar.YEAR);
            calendarMonth = cal.get(Calendar.MONTH);
            calendarDay = cal.get(Calendar.DAY_OF_WEEK);
        }
        editBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(ProfileEditActivity.this, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        birthday = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        editBirthday.setText(birthday);
                        editor.putString("Birthday", birthday);
                    }
                }, calendarYear, calendarMonth, calendarDay);

                datePicker.setTitle("Bir tarih seçin");
                datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Set", datePicker);
                datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", datePicker);
                datePicker.show();
            }
        });

        //  Set gender and retrieve changes
        switch (gender) {
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
                    gender = "male";
                } else if (checkedId == R.id.genderFemale) {
                    gender = "female";
                } else {
                    gender = "transsexual";
                }
                editor.putString("Gender", gender);
            }
        });

        Button logOutFromAccount = findViewById(R.id.button5);
        logOutFromAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(ProfileEditActivity.this).create();
                alertDialog.setTitle("ÇIKIŞ YAP");
                alertDialog.setMessage("Hesaptan çıkılacak ve bu cihaza kaydedilmiş veriler silinecek?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                @SuppressLint("SdCardPath")
                                File sharedPreferenceFile = new File("/data/data/" + getPackageName() + "/shared_prefs/");
                                File[] listFiles = sharedPreferenceFile.listFiles();
                                for (File file : listFiles) {
                                    file.delete();
                                }

                                PackageManager packageManager = ProfileEditActivity.this.getPackageManager();
                                Intent intent = packageManager.getLaunchIntentForPackage(ProfileEditActivity.this.getPackageName());
                                ComponentName componentName = intent.getComponent();
                                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                                ProfileEditActivity.this.startActivity(mainIntent);
                                Runtime.getRuntime().exit(0);
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void updateUserInfo() {
        final ProgressDialog loading = ProgressDialog.show(ProfileEditActivity.this, "Updating profile...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                editor.apply();
                                Toast.makeText(ProfileEditActivity.this, response, Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(ProfileEditActivity.this, "The request failed. Please check the form and try again...", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(ProfileEditActivity.this, "The request failed. Please check the form and try again...", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        loading.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Something went wrong with our servers. Please try again later.", Toast.LENGTH_LONG).show();
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
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                params.put("phoneNumber", userPhoneNumber);
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                } else {
                    params.put("photo", "");
                }
                params.put("gender", gender);
                params.put("birthday", birthday);
                params.put("location", location);
                params.put("country", userCountry);
                params.put("language", userDisplayLanguage);
                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
                if (isNetworkConnected(this)) {
                    if (email.contains("@")) {
                        updateUserInfo();
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Lütfen geçerli bir e-posta adresi giriniz", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ProfileEditActivity.this, "İnternet bağlantısında bir sorun var", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_STORAGE) {
            if (ActivityCompat.checkSelfPermission(ProfileEditActivity.this, PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.create(ProfileEditActivity.this).single().start();
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
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
                    Glide.with(this).load(bitmap).apply(options).into(userPic);
                    photo = "https://fuelspot.com.tr/uploads/users/" + username + ".jpg";
                    editor.putString("ProfilePhoto", photo);
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