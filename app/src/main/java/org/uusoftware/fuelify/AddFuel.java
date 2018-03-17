package org.uusoftware.fuelify;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.username;

public class AddFuel extends AppCompatActivity {

    static final int REQUEST_EXTERNAL_STORAGE = 0;
    static String[] PERMISSIONS_STORAGE = {android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    String UPLOAD_URL = "http://granadagame.com/Sorbie/add_question.php";
    String choosenImagePath;
    String question;

    ImageView imageHolder;
    Button shareButton;
    Bitmap bitmap;
    EditText questionText;
    Window window;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfuel);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.sorbie);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

       /* imageHolder = findViewById(R.id.photoHolder);
        imageHolder.setImageBitmap(bitmap);
        imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyStoragePermissions()) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.MainTheme)
                            .pickPhoto(UploadActivity.this);
                } else {
                    ActivityCompat.requestPermissions(UploadActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

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

    public boolean verifyStoragePermissions() {
        boolean hasStorage;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int permission = ActivityCompat.checkSelfPermission(AddFuel.this, Manifest.permission.CAMERA);
            hasStorage = permission == PackageManager.PERMISSION_GRANTED;
        } else {
            hasStorage = true;
        }
        return hasStorage;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) AddFuel.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage() {
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
            protected Map<String, String> getParams() throws AuthFailureError {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("photo", getStringImage(bitmap));
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

    public void createFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Sorbie");
        folder.mkdirs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(AddFuel.this, "Settings saved...", Toast.LENGTH_SHORT).show();
                    createFolder();
                } else {
                    Toast.makeText(AddFuel.this, "Error 001: Permission request rejected by user...", Toast.LENGTH_LONG)
                            .show();
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
      /*  switch (requestCode) {
           case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    choosenImagePath = aq.get(0);

                    System.out.println("file://" + choosenImagePath);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/Sorbie");
                    folder.mkdirs();

                    CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
                    String fileName = now + ".jpg";

                    UCrop.of(Uri.parse("file://" + choosenImagePath), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(UploadActivity.this);
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        imageHolder.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                }
                break;
        }*/
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
