package com.fuelspot;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.fuelspot.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.MainActivity.REQUEST_LOCATION;
import static com.fuelspot.MainActivity.REQUEST_STORAGE;
import static com.fuelspot.MainActivity.TAX_DIESEL;
import static com.fuelspot.MainActivity.TAX_ELECTRICITY;
import static com.fuelspot.MainActivity.TAX_GASOLINE;
import static com.fuelspot.MainActivity.TAX_LPG;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isGeofenceOpen;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;

public class FragmentSettings extends Fragment {

    private TextView textViewGasolineTax;
    private TextView textViewDieselTax;
    private TextView textViewLPGTax;
    private TextView textViewElectricityTax;
    private SharedPreferences prefs;
    private String feedbackMessage;
    private Bitmap bitmap;
    private ImageView getScreenshot;
    private PopupWindow mPopupWindow;
    private RequestOptions options;
    //Creating a Request Queue
    private RequestQueue requestQueue;
    private View rootView;
    private CheckBox geofenceCheckBox;

    public static FragmentSettings newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Settings");

        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_settings, container, false);

            // Keep screen off
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Settings");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

            requestQueue = Volley.newRequestQueue(getActivity());
            options = new RequestOptions().centerCrop().placeholder(R.drawable.photo_placeholder).error(R.drawable.photo_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH);

            TextView countryText = rootView.findViewById(R.id.textViewCountryName);
            countryText.setText(userCountryName);

            TextView languageText = rootView.findViewById(R.id.textViewLanguage);
            languageText.setText(userDisplayLanguage);

            TextView currencyText = rootView.findViewById(R.id.textViewCurrency);
            currencyText.setText(currencyCode);

            TextView unitSystemText = rootView.findViewById(R.id.textViewUnitSystem);
            unitSystemText.setText(userUnit);

            TextView textViewVersion = rootView.findViewById(R.id.textViewVersion);
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                String version = "v";
                version += pInfo != null ? pInfo.versionName : null;
                String aq = getString(R.string.appVersion) + " " + version;
                textViewVersion.setText(aq);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            RelativeLayout geofenceLayout = rootView.findViewById(R.id.settings_geofence);
            if (isSuperUser) {
                geofenceLayout.setVisibility(View.GONE);
            } else {
                geofenceCheckBox = rootView.findViewById(R.id.checkBox);
                if (isGeofenceOpen) {
                    geofenceCheckBox.setChecked(true);
                    geofenceCheckBox.setText(getString(R.string.location_notification_on));
                } else {
                    geofenceCheckBox.setChecked(false);
                    geofenceCheckBox.setText(getString(R.string.location_notification_off));
                }
                geofenceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            isGeofenceOpen = true;
                            geofenceCheckBox.setChecked(true);
                            geofenceCheckBox.setText(getString(R.string.location_notification_on));
                        } else {
                            isGeofenceOpen = false;
                            geofenceCheckBox.setChecked(false);
                            geofenceCheckBox.setText(getString(R.string.location_notification_off));
                        }

                        prefs.edit().putBoolean("Geofence", isGeofenceOpen).apply();
                    }
                });
            }

            textViewGasolineTax = rootView.findViewById(R.id.priceGasoline);
            textViewGasolineTax.setText("% " + (int) (TAX_GASOLINE * 100f));

            textViewDieselTax = rootView.findViewById(R.id.priceDiesel);
            textViewDieselTax.setText("% " + (int) (TAX_DIESEL * 100f));

            textViewLPGTax = rootView.findViewById(R.id.priceLPG);
            textViewLPGTax.setText("% " + (int) (TAX_LPG * 100f));

            textViewElectricityTax = rootView.findViewById(R.id.priceElectricity);
            textViewElectricityTax.setText("% " + (int) (TAX_ELECTRICITY * 100f));

            Button buttonTax = rootView.findViewById(R.id.button_tax);
            buttonTax.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateTaxRates();
                }
            });

            Button buttonBeta = rootView.findViewById(R.id.button_beta);
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

            Button buttonMissingStation = rootView.findViewById(R.id.button_missing_station);
            buttonMissingStation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ReportMissingStation.class);
                    startActivity(intent);
                }
            });

            Button buttonFeedback = rootView.findViewById(R.id.button_feedback);
            buttonFeedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFeedBackPopup(v);
                }
            });

            Button buttonRate = rootView.findViewById(R.id.button_rate);
            buttonRate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.fuelspot"));
                    startActivity(intent);
                }
            });

            // BU İKİSİ POPUP İÇİNDE WEBVİEW OLACAK
            TextView openTerms = rootView.findViewById(R.id.textView34);
            openTerms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("URL", "https://fuelspot.com.tr/terms-and-conditions");
                    startActivity(intent);
                }
            });

            TextView openPrivacy = rootView.findViewById(R.id.textView35);
            openPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("URL", "https://fuelspot.com.tr/privacy");
                    startActivity(intent);
                }
            });
        }

        return rootView;
    }

    private void updateTaxRates() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_TAX),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                DecimalFormat df = new DecimalFormat("##.#");

                                TAX_GASOLINE = (float) obj.getDouble("gasolineTax");
                                prefs.edit().putFloat("taxGasoline", TAX_GASOLINE).apply();
                                String dummy0 = "% " + df.format(TAX_GASOLINE * 100f);
                                textViewGasolineTax.setText(dummy0);

                                TAX_DIESEL = (float) obj.getDouble("dieselTax");
                                prefs.edit().putFloat("taxDiesel", TAX_DIESEL).apply();
                                String dummy1 = "% " + df.format(TAX_DIESEL * 100f);
                                textViewDieselTax.setText(dummy1);

                                TAX_LPG = (float) obj.getDouble("LPGTax");
                                prefs.edit().putFloat("taxLPG", TAX_LPG).apply();
                                String dummy2 = "% " + df.format(TAX_LPG * 100f);
                                textViewLPGTax.setText(dummy2);

                                TAX_ELECTRICITY = (float) obj.getDouble("electricityTax");
                                prefs.edit().putFloat("taxElectricity", TAX_ELECTRICITY).apply();
                                String dummy3 = "% " + df.format(TAX_ELECTRICITY * 100f);
                                textViewElectricityTax.setText(dummy3);

                                getVariables(prefs);

                                if (isSuperUser) {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.pager), getString(R.string.update_tax_success), Snackbar.LENGTH_LONG);
                                    snackBar.show();
                                } else {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.update_tax_success), Snackbar.LENGTH_LONG);
                                    snackBar.show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getActivity(), getString(R.string.error), Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.error), Snackbar.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getActivity(), volleyError.toString(), Snackbar.LENGTH_LONG).show();
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

    private void openFeedBackPopup(View view) {
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
        Glide.with(getActivity()).load(bitmap).apply(options).into(getScreenshot);
        getScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(getActivity())) {
                    ImagePicker.create(getActivity()).single().start();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_STORAGE);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_LOCATION);
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
                final ProgressDialog loading = ProgressDialog.show(getActivity(), getString(R.string.feedback_sending), getString(R.string.please_wait), false, false);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FEEDBACK),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                loading.dismiss();
                                if (isSuperUser) {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.pager), getString(R.string.thanks_for_feedback), Snackbar.LENGTH_LONG);
                                    snackBar.show();
                                } else {
                                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.thanks_for_feedback), Snackbar.LENGTH_LONG);
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
                        params.put("username", username);
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

    private String getStringImage(Bitmap bmp) {
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
                    ImagePicker.create(this).single().start();
                } else {
                    Snackbar.make(getActivity().findViewById(R.id.mainContainer), getString(R.string.permission_denied), Snackbar.LENGTH_LONG).show();
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
