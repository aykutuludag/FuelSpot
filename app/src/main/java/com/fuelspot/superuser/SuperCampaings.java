package com.fuelspot.superuser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.universalTimeStamp;
import static com.fuelspot.superuser.AdminMainActivity.superStationID;

public class SuperCampaings extends AppCompatActivity {

    RequestQueue requestQueue;
    SimpleDateFormat sdf;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    List<CampaignItem> feedsList = new ArrayList<>();

    String campaignName, campaignDesc, campaignPhoto, sTime, eTime;

    Window window;
    Toolbar toolbar;

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

        coloredBars(Color.parseColor("#7B1FA2"), Color.parseColor("#9C27B0"));

        requestQueue = Volley.newRequestQueue(this);
        sdf = new SimpleDateFormat(universalTimeStamp, Locale.getDefault());
        // Comments
        mRecyclerView = findViewById(R.id.campaignView);

        FloatingActionButton myFab = findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addCampaign(v);
            }
        });

        fetchCampaigns();
    }

    public void fetchCampaigns() {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_CAMPAINGS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CampaignItem item = new CampaignItem();
                                    item.setCampaignName(obj.getString("campaignName"));
                                    item.setCampaignDesc(obj.getString("campaignDesc"));
                                    item.setCampaignPhoto(obj.getString("campaignPhoto"));
                                    item.setCampaignStart(obj.getString("campaignStart"));
                                    item.setCampaignEnd(obj.getString("campaignEnd"));
                                    feedsList.add(item);
                                }

                                mAdapter = new CampaignAdapter(SuperCampaings.this, feedsList);
                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(SuperCampaings.this, 2);
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
                        mRecyclerView.setVisibility(View.GONE);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();
                //Adding parameters
                params.put("id", String.valueOf(superStationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void addCampaign(View view) {
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

        final SlideDateTimeListener listener = new SlideDateTimeListener() {
            @Override
            public void onDateTimeSet(Date date) {
                sTime = sdf.format(date);
            }

            @Override
            public void onDateTimeCancel() {
                sTime = "";
            }
        };
        EditText startTimeText = customView.findViewById(R.id.editTextsTime);
        startTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });

        final SlideDateTimeListener listener2 = new SlideDateTimeListener() {
            @Override
            public void onDateTimeSet(Date date) {
                eTime = sdf.format(date);
            }

            @Override
            public void onDateTimeCancel() {
                eTime = "";
            }
        };
        EditText endTimeText = customView.findViewById(R.id.editTexteTime);
        endTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener2)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });

        ImageView imageViewCampaign = customView.findViewById(R.id.imageViewCampaignPhoto);
        imageViewCampaign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.create(SuperCampaings.this).single().start();
            }
        });


        Button sendValues = customView.findViewById(R.id.buttonAddCampaign);
        sendValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (campaignName != null && campaignName.length() > 0) {
                    if (campaignDesc != null && campaignDesc.length() > 0) {
                        if (sTime != null && sTime.length() > 0) {
                            if (eTime != null && eTime.length() > 0) {
                                sendCampaignToServer();
                            } else {
                                Toast.makeText(SuperCampaings.this, "Lütfen bitiş zamanını seçiniz.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(SuperCampaings.this, "Lütfen başlangıç zamanını seçiniz.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(SuperCampaings.this, "Lütfen kampanya açıklaması yazınız.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SuperCampaings.this, "Lütfen kampanya adını giriniz", Toast.LENGTH_LONG).show();
                }
            }

            private void sendCampaignToServer() {
                final ProgressDialog loading = ProgressDialog.show(SuperCampaings.this, "Sending report...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_CREATE_CAMPAING),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                switch (response) {
                                    case "Success":
                                        Toast.makeText(SuperCampaings.this, "Kampanya eklendi...", Toast.LENGTH_LONG).show();
                                        mPopupWindow.dismiss();
                                        fetchCampaigns();
                                        break;
                                    case "Fail":
                                        break;
                                }
                                loading.dismiss();
                                Toast.makeText(SuperCampaings.this, response, Toast.LENGTH_SHORT).show();
                                mPopupWindow.dismiss();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                //Showing toast
                                loading.dismiss();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        //Creating parameters
                        Map<String, String> params = new Hashtable<>();

                        //Adding parameters
                        params.put("stationID", String.valueOf(superStationID));
                        params.put("campaignName", campaignName);
                        params.put("campaignDesc", campaignDesc);
                        params.put("campaignPhoto", campaignPhoto);
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

    void updateCampaign(int campaignID) {

    }

    void deleteCampaign(int campaignID) {

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

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                Bitmap bmp = BitmapFactory.decodeFile(image.getPath());
                campaignPhoto = getStringImage(bmp);
            } else {
                campaignPhoto = "";
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
