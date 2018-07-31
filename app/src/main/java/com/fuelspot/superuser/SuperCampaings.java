package com.fuelspot.superuser;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.fuelspot.R;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

public class SuperCampaings extends AppCompatActivity {

    RequestQueue requestQueue;
    ArrayList<String> campaignName = new ArrayList<>();
    ArrayList<String> campaignDesc = new ArrayList<>();
    ArrayList<String> campaignPhoto = new ArrayList<>();
    ArrayList<String> campaignStart = new ArrayList<>();
    ArrayList<String> campaignEnd = new ArrayList<>();

    ImageView imageViewCampaign, imageViewCampaign2, imageViewCampaign3, imageViewDelete1, imageViewDelete2, imageViewDelete3;
    EditText editTextCampaignName, editTextCampaignDetail, editTextCampaignName2, editTextCampaignDetail2, editTextCampaignName3, editTextCampaignDetail3;
    TextView startTimeText, endTimeText, startTimeText2, endTimeText2, startTimeText3, endTimeText3;

    SimpleDateFormat sdf;
    private SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            campaignStart.add(0, sdf.format(date));
            startTimeText.setText(sdf.format(date));
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };
    private SlideDateTimeListener listener2 = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            campaignEnd.add(0, sdf.format(date));
            endTimeText.setText(sdf.format(date));
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };
    private SlideDateTimeListener listener3 = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            campaignStart.add(1, sdf.format(date));
            startTimeText2.setText(sdf.format(date));
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };
    private SlideDateTimeListener listener4 = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            campaignEnd.add(1, sdf.format(date));
            endTimeText2.setText(sdf.format(date));
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };
    private SlideDateTimeListener listener5 = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            campaignStart.add(2, sdf.format(date));
            startTimeText3.setText(sdf.format(date));
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };
    private SlideDateTimeListener listener6 = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date) {
            campaignEnd.add(2, sdf.format(date));
            endTimeText3.setText(sdf.format(date));
        }

        @Override
        public void onDateTimeCancel() {
            // Overriding onDateTimeCancel() is optional.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_campaings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.brand_logo);

        requestQueue = Volley.newRequestQueue(this);
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

        // CAMPAIGN 1
        imageViewCampaign = findViewById(R.id.campaignPhoto);
        editTextCampaignName = findViewById(R.id.campaignTitle);
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
                    campaignName.add(0, s.toString());
                }
            }
        });
        editTextCampaignDetail = findViewById(R.id.campaignDesc);
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
                    campaignDesc.add(0, s.toString());
                }
            }
        });
        startTimeText = findViewById(R.id.startTime);
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
        endTimeText = findViewById(R.id.endTime);
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
        imageViewDelete1 = findViewById(R.id.deleteCampaignOne);
        imageViewDelete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCampaign(0);
            }
        });
        // CAMPAIGN 1

        // CAMPAIGN 2
        imageViewCampaign2 = findViewById(R.id.campaignPhoto2);
        editTextCampaignName2 = findViewById(R.id.campaignTitle2);
        editTextCampaignName2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    campaignName.add(1, s.toString());
                }
            }
        });
        editTextCampaignDetail2 = findViewById(R.id.campaignDesc2);
        editTextCampaignDetail2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    campaignDesc.add(1, s.toString());
                }
            }
        });
        startTimeText2 = findViewById(R.id.startTime2);
        startTimeText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener3)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });
        endTimeText2 = findViewById(R.id.endTime2);
        endTimeText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener4)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });
        // CAMPAIGN 2

        // CAMPAIGN 3
        imageViewCampaign3 = findViewById(R.id.campaignPhoto3);
        editTextCampaignName3 = findViewById(R.id.campaignTitle3);
        editTextCampaignName3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    campaignName.add(2, s.toString());
                }
            }
        });
        editTextCampaignDetail3 = findViewById(R.id.campaignDesc3);
        editTextCampaignDetail3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    campaignDesc.add(2, s.toString());
                }
            }
        });
        startTimeText3 = findViewById(R.id.startTime3);
        startTimeText3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener5)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });
        endTimeText3 = findViewById(R.id.endTime3);
        endTimeText3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener6)
                        .setIs24HourTime(true)
                        .setInitialDate(new Date())
                        .setMinDate(new Date())
                        .build()
                        .show();
            }
        });
        // CAMPAIGN 3

        fetchCampaigns();
    }

    public void fetchCampaigns() {
        campaignName.clear();
        campaignDesc.clear();
        campaignPhoto.clear();
        campaignStart.clear();
        campaignEnd.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_CAMPAINGS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("AMK:" + response);
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    campaignName.add(i, obj.getString("campaignName"));
                                    campaignDesc.add(i, obj.getString("campaignDesc"));
                                    campaignPhoto.add(i, obj.getString("campaignPhoto"));
                                    campaignStart.add(i, obj.getString("campaignStart"));
                                    campaignEnd.add(i, obj.getString("campaignEnd"));

                                    if (i == 0) {
                                        Glide.with(SuperCampaings.this).load(Uri.parse(campaignPhoto.get(0))).into(imageViewCampaign);
                                        editTextCampaignName.setText(campaignName.get(0));
                                        editTextCampaignDetail.setText(campaignDesc.get(0));
                                        startTimeText.setText(campaignStart.get(0));
                                        endTimeText.setText(campaignEnd.get(0));
                                    } else if (i == 1) {
                                        Glide.with(SuperCampaings.this).load(Uri.parse(campaignPhoto.get(1))).into(imageViewCampaign2);
                                        editTextCampaignName2.setText(campaignName.get(1));
                                        editTextCampaignDetail2.setText(campaignDesc.get(1));
                                        startTimeText2.setText(campaignStart.get(1));
                                        endTimeText2.setText(campaignEnd.get(1));
                                    } else {
                                        Glide.with(SuperCampaings.this).load(Uri.parse(campaignPhoto.get(2))).into(imageViewCampaign3);
                                        editTextCampaignName3.setText(campaignName.get(2));
                                        editTextCampaignDetail3.setText(campaignDesc.get(2));
                                        startTimeText3.setText(campaignStart.get(2));
                                        endTimeText3.setText(campaignEnd.get(2));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(AdminMainActivity.superStationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void deleteCampaign(int index) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
