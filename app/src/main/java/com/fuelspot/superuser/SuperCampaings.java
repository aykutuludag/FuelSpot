package com.fuelspot.superuser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.fuelspot.R;
import com.fuelspot.adapter.CampaignAdapter;
import com.fuelspot.model.CampaignItem;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.isNetworkConnected;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;

public class SuperCampaings extends AppCompatActivity {

    private RequestQueue requestQueue;
    ImageView imageViewCampaign;
    private SimpleDateFormat sdf = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
    private RecyclerView mRecyclerView, mRecyclerView2;
    private RecyclerView.Adapter mAdapter;
    private List<CampaignItem> feedsList = new ArrayList<>();
    private List<CampaignItem> feedsList2 = new ArrayList<>();
    private String campaignName;
    private String campaignDesc;
    private String sTime;
    private String eTime;
    private Bitmap bmp;
    private Window window;
    private Toolbar toolbar;
    private SimpleDateFormat sdf2 = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
    private SwipeRefreshLayout swipeContainer;
    private RequestOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_campaings);

        window = this.getWindow();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_campaign).error(R.drawable.default_campaign)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        requestQueue = Volley.newRequestQueue(this);

        // Comments
        mRecyclerView = findViewById(R.id.campaignView);
        mRecyclerView2 = findViewById(R.id.campaignOldView);

        Button buttonAdd = findViewById(R.id.button_add_campaign);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addORupdateCampaign(v, null);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchCampaigns();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fetchCampaigns();
        fetchOldCampaigns();
    }

    public void fetchCampaigns() {
        final ProgressDialog loading = ProgressDialog.show(SuperCampaings.this, getString(R.string.loading_campaigns), getString(R.string.please_wait), false, false);
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_CAMPAINGS) + "?stationID=" + superStationID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        swipeContainer.setRefreshing(false);
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CampaignItem item = new CampaignItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationID(obj.getInt("stationID"));
                                    item.setCampaignName(obj.getString("campaignName"));
                                    item.setCampaignDesc(obj.getString("campaignDesc"));
                                    item.setCampaignPhoto(obj.getString("campaignPhoto"));
                                    item.setCampaignStart(obj.getString("campaignStart"));
                                    item.setCampaignEnd(obj.getString("campaignEnd"));
                                    item.setIsGlobal(obj.getInt("isGlobal"));
                                    feedsList.add(item);
                                }

                                mAdapter = new CampaignAdapter(SuperCampaings.this, feedsList, "SUPERUSER");
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(SuperCampaings.this, 1);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                mRecyclerView.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                mRecyclerView.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(SuperCampaings.this, "Henüz hiç kampanya eklememişsiniz.", Toast.LENGTH_LONG).show();
                            mRecyclerView.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        swipeContainer.setRefreshing(false);
                        mRecyclerView.setVisibility(View.GONE);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("token", token);
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void fetchOldCampaigns() {
        feedsList2.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_OLD_CAMPAINGS) + "?stationID=" + superStationID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CampaignItem item = new CampaignItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationID(obj.getInt("stationID"));
                                    item.setCampaignName(obj.getString("campaignName"));
                                    item.setCampaignDesc(obj.getString("campaignDesc"));
                                    item.setCampaignPhoto(obj.getString("campaignPhoto"));
                                    item.setCampaignStart(obj.getString("campaignStart"));
                                    item.setCampaignEnd(obj.getString("campaignEnd"));
                                    item.setIsGlobal(obj.getInt("isGlobal"));
                                    feedsList2.add(item);
                                }

                                mAdapter = new CampaignAdapter(SuperCampaings.this, feedsList2, "SUPERUSER");
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView2.setAdapter(mAdapter);
                                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(SuperCampaings.this, 1);
                                mRecyclerView2.setLayoutManager(mLayoutManager);
                                mRecyclerView2.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                mRecyclerView2.setVisibility(View.GONE);
                            }
                        } else {
                            mRecyclerView2.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRecyclerView2.setVisibility(View.GONE);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("token", token);
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void addORupdateCampaign(View view, final CampaignItem item) throws ParseException {
        final String apiURL;
        final String dialogText;
        final String buttonText;

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_add_campaign, null);
        final PopupWindow mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        EditText editTextCampaignName = customView.findViewById(R.id.editTextCampaignName);
        editTextCampaignName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    campaignName = s.toString();
                }
            }
        });

        EditText editTextCampaignDetail = customView.findViewById(R.id.editTextCampaignDesc);
        editTextCampaignDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    campaignDesc = s.toString();
                }
            }
        });

        final EditText startTimeText = customView.findViewById(R.id.editTextsTime);
        startTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlideDateTimeListener listener = new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {
                        sTime = sdf.format(date);
                        startTimeText.setText(sdf2.format(date));
                    }

                    @Override
                    public void onDateTimeCancel() {
                        sTime = "";
                        startTimeText.setText(sTime);
                    }
                };

                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });


        final EditText endTimeText = customView.findViewById(R.id.editTexteTime);
        endTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlideDateTimeListener listener2 = new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {
                        eTime = sdf.format(date);
                        endTimeText.setText(sdf2.format(date));
                    }

                    @Override
                    public void onDateTimeCancel() {
                        eTime = "";
                        endTimeText.setText(eTime);
                    }
                };

                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener2)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });

        imageViewCampaign = customView.findViewById(R.id.imageViewCampaignPhoto);
        imageViewCampaign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.create(SuperCampaings.this).single().start();
            }
        });

        if (item != null) {
            //UPDATE CAMPAIGN
            apiURL = getString(R.string.API_UPDATE_CAMPAING);
            dialogText = getString(R.string.campaign_updating);
            buttonText = getString(R.string.update_campaign);

            campaignName = item.getCampaignName();
            campaignDesc = item.getCampaignDesc();
            sTime = item.getCampaignStart();
            eTime = item.getCampaignEnd();

            editTextCampaignName.setText(campaignName);
            editTextCampaignDetail.setText(campaignDesc);
            Date a = sdf.parse(sTime);
            startTimeText.setText(sdf2.format(a));
            Date b = sdf.parse(eTime);
            endTimeText.setText(sdf2.format(b));
            Glide.with(this).load(item.getCampaignPhoto()).apply(options).into(imageViewCampaign);
        } else {
            // ADD CAMPAIGN
            apiURL = getString(R.string.API_CREATE_CAMPAING);
            dialogText = getString(R.string.campaign_adding);
            buttonText = getString(R.string.add_campaign);
        }

        Button sendValues = customView.findViewById(R.id.buttonAddCampaign);
        sendValues.setText(buttonText);
        sendValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campaignName != null && campaignName.length() > 0) {
                    if (campaignDesc != null && campaignDesc.length() > 0) {
                        if (sTime != null && sTime.length() > 0) {
                            if (eTime != null && eTime.length() > 0) {
                                if (isNetworkConnected(SuperCampaings.this)) {
                                    sendCampaignToServer();
                                } else {
                                    Toast.makeText(SuperCampaings.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(SuperCampaings.this, getString(R.string.empty_campaign_end), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SuperCampaings.this, getString(R.string.empty_campaign_start), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(SuperCampaings.this, getString(R.string.empty_campaign_desc), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SuperCampaings.this, getString(R.string.empty_campaign_name), Toast.LENGTH_LONG).show();
                }
            }

            private void sendCampaignToServer() {
                final ProgressDialog loading = ProgressDialog.show(SuperCampaings.this, dialogText, getString(R.string.please_wait), false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, apiURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println(response);
                                loading.dismiss();
                                if (response != null && response.length() > 0) {
                                    switch (response) {
                                        case "Success":
                                            if (item != null) {
                                                Toast.makeText(SuperCampaings.this, getString(R.string.campaign_updated), Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(SuperCampaings.this, getString(R.string.campaign_added), Toast.LENGTH_LONG).show();
                                            }
                                            mPopupWindow.dismiss();
                                            fetchCampaigns();
                                            break;
                                        case "Fail":
                                            Toast.makeText(SuperCampaings.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                } else {
                                    Toast.makeText(SuperCampaings.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                loading.dismiss();
                                Toast.makeText(SuperCampaings.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        //Creating parameters
                        Map<String, String> params = new Hashtable<>();

                        // Adding parameters
                        if (item != null) {
                            // UPDATE CAMPAIGN
                            params.put("campaignID", String.valueOf(item.getID()));
                        }
                        params.put("stationID", String.valueOf(superStationID));
                        params.put("campaignName", campaignName);
                        params.put("campaignDesc", campaignDesc);
                        if (bmp != null) {
                            params.put("campaignPhoto", getStringImage(bmp));
                        }
                        params.put("campaignStart", sTime);
                        params.put("campaignEnd", eTime);

                        //returning parameters
                        return params;
                    }
                };

                //Adding request to the queue
                requestQueue.add(stringRequest);
            }
        });

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
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

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Imagepicker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                bmp = BitmapFactory.decodeFile(image.getPath());
                try {
                    bmp = BitmapFactory.decodeFile(image.getPath());
                    ExifInterface ei = new ExifInterface(image.getPath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_NORMAL:
                            bmp = resizeAndRotate(bmp, 0);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bmp = resizeAndRotate(bmp, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bmp = resizeAndRotate(bmp, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bmp = resizeAndRotate(bmp, 270);
                            break;
                    }
                    Glide.with(this).load(bmp).apply(options).into(imageViewCampaign);
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
