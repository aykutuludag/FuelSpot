package com.fuelspot;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.fuelspot.model.CompanyItem;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.TAX_DIESEL;
import static com.fuelspot.MainActivity.TAX_ELECTRICITY;
import static com.fuelspot.MainActivity.TAX_GASOLINE;
import static com.fuelspot.MainActivity.TAX_LPG;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.isGeofenceOpen;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;

public class FragmentSettings extends Fragment {


    public static List<CompanyItem> companyList = new ArrayList<>();
    public static List<String> companyNameList = new ArrayList<>();
    public static List<Integer> companyVerifiedNumberList = new ArrayList<>();
    public static List<Integer> companyStationNumberList = new ArrayList<>();
    TextView countryText, languageText, currencyText, unitSystemText, textViewGasolineTax, textViewDieselTax, textViewLPGTax, textViewElectricityTax;
    Button buttonTax, buttonEksikIstasyon, buttonBeta, buttonFeedback, buttonRate;
    SharedPreferences prefs;
    String feedbackMessage;
    Bitmap bitmap;
    ImageView getScreenshot;
    PopupWindow mPopupWindow;
    RequestOptions options;
    //Creating a Request Queue
    RequestQueue requestQueue;
    View rootView;
    ArrayList<PieEntry> entries = new ArrayList<>();
    PieChart chart3;
    TextView textViewTotalNumber;

    CheckBox geofenceCheckBox;

    int otherStations, totalVerified, totalStation;

    public static FragmentSettings newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Settings");

        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_settings, container, false);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Settings");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

            requestQueue = Volley.newRequestQueue(getActivity());
            options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);

            countryText = rootView.findViewById(R.id.textViewCountryName);
            countryText.setText(userCountryName);

            languageText = rootView.findViewById(R.id.textViewLanguage);
            languageText.setText(userDisplayLanguage);

            currencyText = rootView.findViewById(R.id.textViewCurrency);
            currencyText.setText(currencyCode);

            unitSystemText = rootView.findViewById(R.id.textViewUnitSystem);
            unitSystemText.setText(userUnit);

            geofenceCheckBox = rootView.findViewById(R.id.checkBox);
            if (isGeofenceOpen) {
                geofenceCheckBox.setChecked(true);
                geofenceCheckBox.setText("Konum bildirimleri açık");
            } else {
                geofenceCheckBox.setChecked(false);
                geofenceCheckBox.setText("Konum bildirimleri kapalı");
            }
            geofenceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isGeofenceOpen = true;
                        geofenceCheckBox.setChecked(true);
                        geofenceCheckBox.setText("Konum bildirimleri açık");
                    } else {
                        isGeofenceOpen = false;
                        geofenceCheckBox.setChecked(false);
                        geofenceCheckBox.setText("Konum bildirimleri kapalı");
                    }

                    prefs.edit().putBoolean("Geofence", isGeofenceOpen).apply();
                }
            });

            textViewGasolineTax = rootView.findViewById(R.id.priceGasoline);
            textViewGasolineTax.setText("% " + (int) (TAX_GASOLINE * 100f));

            textViewDieselTax = rootView.findViewById(R.id.priceDiesel);
            textViewDieselTax.setText("% " + (int) (TAX_DIESEL * 100f));

            textViewLPGTax = rootView.findViewById(R.id.priceLPG);
            textViewLPGTax.setText("% " + (int) (TAX_LPG * 100f));

            textViewElectricityTax = rootView.findViewById(R.id.priceElectricity);
            textViewElectricityTax.setText("% " + (int) (TAX_ELECTRICITY * 100f));

            buttonTax = rootView.findViewById(R.id.button_tax);
            buttonTax.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateTaxRates();
                }
            });

            textViewTotalNumber = rootView.findViewById(R.id.textViewtoplamSayi);

            chart3 = rootView.findViewById(R.id.chart3);
            chart3.getDescription().setEnabled(false);
            chart3.setDragDecelerationFrictionCoef(0.95f);
            chart3.setDrawHoleEnabled(false);
            chart3.getLegend().setEnabled(false);
            chart3.setTransparentCircleColor(Color.BLACK);
            chart3.setTransparentCircleAlpha(110);
            chart3.setTransparentCircleRadius(61f);
            chart3.setUsePercentValues(false);
            chart3.setRotationEnabled(true);
            chart3.setHighlightPerTapEnabled(true);
            chart3.setEntryLabelColor(Color.BLACK);
            chart3.setEntryLabelTextSize(12f);

            fetchCompanyStats();

            buttonEksikIstasyon = rootView.findViewById(R.id.button_missingStation);
            buttonEksikIstasyon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMissingStationPopup(v);
                }
            });

            buttonBeta = rootView.findViewById(R.id.button_beta);
            buttonBeta.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.enableUrlBarHiding();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(Color.parseColor("#FF7439"));
                    customTabsIntent.launchUrl(getActivity(), Uri.parse("https://play.google.com/apps/testing/com.fuelspot"));
                }
            });

            buttonFeedback = rootView.findViewById(R.id.button_feedback);
            buttonFeedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFeedBackPopup(v);
                }
            });

            buttonRate = rootView.findViewById(R.id.button_rate);
            buttonRate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.fuelspot"));
                    startActivity(intent);
                }
            });

            TextView openTerms = rootView.findViewById(R.id.textView34);
            openTerms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.enableUrlBarHiding();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(Color.parseColor("#212121"));
                    customTabsIntent.launchUrl(getActivity(), Uri.parse("https://fuel-spot.com/terms-and-conditions"));
                }
            });

            TextView openPrivacy = rootView.findViewById(R.id.textView35);
            openPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.enableUrlBarHiding();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(Color.parseColor("#212121"));
                    customTabsIntent.launchUrl(getActivity(), Uri.parse("https://fuel-spot.com/privacy"));
                }
            });
        }

        return rootView;
    }

    void fetchCompanyStats() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_COMPANY),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CompanyItem item = new CompanyItem();
                                    item.setID(obj.getInt("id"));
                                    item.setName(obj.getString("companyName"));
                                    item.setLogo(obj.getString("companyLogo"));
                                    item.setWebsite(obj.getString("companyWebsite"));
                                    item.setPhone(obj.getString("companyPhone"));
                                    item.setAddress(obj.getString("companyAddress"));
                                    item.setNumOfVerifieds(obj.getInt("numOfVerifieds"));
                                    item.setNumOfStations(obj.getInt("numOfStations"));

                                    companyNameList.add(obj.getString("companyName"));
                                    companyVerifiedNumberList.add(obj.getInt("numOfVerifieds"));
                                    companyStationNumberList.add(obj.getInt("numOfStations"));
                                    companyList.add(item);

                                    totalVerified += obj.getInt("numOfVerifieds");
                                    totalStation += obj.getInt("numOfStations");

                                    if (companyStationNumberList.get(i) >= 225) {
                                        entries.add(new PieEntry((float) companyStationNumberList.get(i), obj.getString("companyName")));
                                    } else {
                                        otherStations += companyStationNumberList.get(i);
                                    }
                                }

                                textViewTotalNumber.setText("Kayıtlı istasyon sayısı: " + totalStation);

                                entries.add(new PieEntry((float) otherStations, "Diğer"));

                                PieDataSet dataSet = new PieDataSet(entries, "Akaryakıt dağıtım firmaları");
                                dataSet.setDrawIcons(false);

                                // add a lot of colors
                                ArrayList<Integer> colors = new ArrayList<>();

                                for (int c : ColorTemplate.VORDIPLOM_COLORS)
                                    colors.add(c);

                                for (int c : ColorTemplate.JOYFUL_COLORS)
                                    colors.add(c);

                                for (int c : ColorTemplate.COLORFUL_COLORS)
                                    colors.add(c);

                                for (int c : ColorTemplate.LIBERTY_COLORS)
                                    colors.add(c);

                                for (int c : ColorTemplate.PASTEL_COLORS)
                                    colors.add(c);

                                colors.add(ColorTemplate.getHoloBlue());

                                dataSet.setColors(colors);
                                //dataSet.setSelectionShift(0f);

                                PieData data = new PieData(dataSet);
                                data.setValueTextSize(11f);
                                data.setValueTextColor(Color.BLACK);
                                chart3.setData(data);
                                chart3.highlightValues(null);
                                chart3.invalidate();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
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
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void updateTaxRates() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_TAX),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                TAX_GASOLINE = (float) obj.getDouble("gasolineTax");
                                prefs.edit().putFloat("taxGasoline", TAX_GASOLINE).apply();
                                textViewGasolineTax.setText("% " + (int) (TAX_GASOLINE * 100f));

                                TAX_DIESEL = (float) obj.getDouble("dieselTax");
                                prefs.edit().putFloat("taxDiesel", TAX_DIESEL).apply();
                                textViewDieselTax.setText("% " + (int) (TAX_DIESEL * 100f));

                                TAX_LPG = (float) obj.getDouble("LPGTax");
                                prefs.edit().putFloat("taxLPG", TAX_LPG).apply();
                                textViewLPGTax.setText("% " + (int) (TAX_LPG * 100f));

                                TAX_ELECTRICITY = (float) obj.getDouble("electricityTax");
                                prefs.edit().putFloat("taxElectricity", TAX_ELECTRICITY).apply();
                                textViewElectricityTax.setText("% " + (int) (TAX_ELECTRICITY * 100f));

                                MainActivity.getVariables(prefs);

                                if (isSuperUser) {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.pager), "Vergi oranları güncellendi.", Snackbar.LENGTH_LONG);
                                    snackBar.show();
                                } else {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.mainContainer), "Vergi oranları güncellendi.", Snackbar.LENGTH_LONG);
                                    snackBar.show();
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
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("country", userCountry);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void openFeedBackPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_feedback, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        EditText getFeedback = customView.findViewById(R.id.editTextFeedback);
        getFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    feedbackMessage = s.toString();
                }
            }
        });

        getScreenshot = customView.findViewById(R.id.feedBackPhoto);
        Glide.with(this).load(bitmap).apply(options).into(getScreenshot);
        getScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(getActivity())) {
                    ImagePicker.create(getActivity()).single().start();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        Button sendFeedback = customView.findViewById(R.id.sendFeedback);
        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }

            private void sendFeedback() {
                //Showing the progress dialog
                final ProgressDialog loading = ProgressDialog.show(getActivity(), "Loading...", "Please wait...", false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FEEDBACK),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                loading.dismiss();
                                if (isSuperUser) {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.pager), "Geri bildiriminiz için teşekkür ederiz!", Snackbar.LENGTH_LONG);
                                    snackBar.show();
                                } else {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.mainContainer), "Geri bildiriminiz için teşekkür ederiz!", Snackbar.LENGTH_LONG);
                                    snackBar.show();
                                }
                                mPopupWindow.dismiss();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                //Dismissing the progress dialog
                                loading.dismiss();
                                Toast.makeText(getActivity(), volleyError.toString(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        //Creating parameters
                        Map<String, String> params = new Hashtable<>();

                        //Adding parameters
                        params.put("username", MainActivity.username);
                        params.put("message", feedbackMessage);
                        if (bitmap != null) {
                            params.put("screenshot", getStringImage(bitmap));
                        }
                        params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                        //returning parameters
                        return params;
                    }
                };

                //Adding request to the queue
                requestQueue.add(stringRequest);
            }
        });

        ImageView closeButton = customView.findViewById(R.id.imageViewClose);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    void openMissingStationPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_feedback, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        EditText getFeedback = customView.findViewById(R.id.editTextFeedback);
        getFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    feedbackMessage = s.toString();
                }
            }
        });

        getScreenshot = customView.findViewById(R.id.campaignPhoto);
        getScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(getActivity())) {
                    ImagePicker.create(getActivity()).single().start();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_STORAGE);
                }
            }
        });

        Button sendFeedback = customView.findViewById(R.id.sendFeedback);
        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReport();
            }

            private void sendReport() {
                
            }
        });

        ImageView closeButton = customView.findViewById(R.id.imageViewClose);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }



    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (ActivityCompat.checkSelfPermission(getActivity(), PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.create(getActivity()).single().start();
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
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

        // Imagepicker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                bitmap = BitmapFactory.decodeFile(image.getPath());
                Glide.with(this).load(image.getPath()).apply(options).into(getScreenshot);
            }
        }
    }
}
