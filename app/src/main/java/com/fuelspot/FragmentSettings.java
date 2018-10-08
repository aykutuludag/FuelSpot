package com.fuelspot;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fuelspot.superuser.AdminMainActivity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.fuelspot.MainActivity.PERMISSIONS_FILEPICKER;
import static com.fuelspot.MainActivity.REQUEST_FILEPICKER;
import static com.fuelspot.MainActivity.TAX_DIESEL;
import static com.fuelspot.MainActivity.TAX_ELECTRICITY;
import static com.fuelspot.MainActivity.TAX_GASOLINE;
import static com.fuelspot.MainActivity.TAX_LPG;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.isGlobalNews;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.userCountryName;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userUnit;
import static com.fuelspot.MainActivity.verifyFilePickerPermission;

public class FragmentSettings extends Fragment {

    TextView countryText, languageText, currencyText, unitSystemText, textViewGasolineTax, textViewDieselTax, textViewLPGTax, textViewElectricityTax, superUserCount;
    Button buttonTax, buttonBeta, buttonFeedback, buttonRate;
    Switch globalNewsSwitch;
    SharedPreferences prefs;
    String feedbackMessage;
    Bitmap bitmap;
    ImageView getScreenshot;
    PopupWindow mPopupWindow;
    TextView userRange, userPremium;
    //Creating a Request Queue
    RequestQueue requestQueue;

    public static FragmentSettings newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Settings");

        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("SETTINGS");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        requestQueue = Volley.newRequestQueue(getActivity());

        countryText = rootView.findViewById(R.id.textViewCountryName);
        countryText.setText(userCountryName);

        languageText = rootView.findViewById(R.id.textViewLanguage);
        languageText.setText(userDisplayLanguage);

        currencyText = rootView.findViewById(R.id.textViewCurrency);
        currencyText.setText(currencyCode);

        unitSystemText = rootView.findViewById(R.id.textViewUnitSystem);
        unitSystemText.setText(userUnit);

        globalNewsSwitch = rootView.findViewById(R.id.switch1);
        globalNewsSwitch.setChecked(isGlobalNews);
        globalNewsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isGlobalNews = isChecked;
                prefs.edit().putBoolean("isGlobalNews", isGlobalNews).apply();
            }
        });

        textViewGasolineTax = rootView.findViewById(R.id.taxGasoline);
        textViewGasolineTax.setText("% " + (int) (TAX_GASOLINE * 100f));

        textViewDieselTax = rootView.findViewById(R.id.taxDiesel);
        textViewDieselTax.setText("% " + (int) (TAX_DIESEL * 100f));

        textViewLPGTax = rootView.findViewById(R.id.TaxLPG);
        textViewLPGTax.setText("% " + (int) (TAX_LPG * 100f));

        textViewElectricityTax = rootView.findViewById(R.id.TaxElectricity);
        textViewElectricityTax.setText("% " + (int) (TAX_ELECTRICITY * 100f));

        buttonTax = rootView.findViewById(R.id.button_tax);
        buttonTax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaxRates();
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

        userRange = rootView.findViewById(R.id.textViewMenzil);
        userRange.setText((mapDefaultRange / 1000) + " km");

        userPremium = rootView.findViewById(R.id.textViewPremium);
        userPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isSuperUser) {
                    try {
                        ((AdminMainActivity) getActivity()).buyAdminPremium();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        ((MainActivity) getActivity()).buyPremium();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        superUserCount = rootView.findViewById(R.id.textViewSuperUserCount);

        fetchStats();

        return rootView;
    }

    void fetchStats() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_STATS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);
                                superUserCount.setText("Anlık veri alınan istasyon sayısı: " + obj.getInt("id"));
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
                params.put("country", MainActivity.userCountry);

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
                params.put("country", MainActivity.userCountry);

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

        Button sendFeedback = customView.findViewById(R.id.sendFeedback);
        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

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

        getScreenshot = customView.findViewById(R.id.title);
        getScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyFilePickerPermission(getActivity())) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(getActivity());
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_FILEPICKER, REQUEST_FILEPICKER);
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FILEPICKER: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Settings saved...", Toast.LENGTH_SHORT).show();
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .enableCameraSupport(true)
                            .pickPhoto(getActivity());
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
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    String ss = aq.get(0);

                    System.out.println("file://" + ss);

                    File folder = new File(Environment.getExternalStorageDirectory() + "/FuelSpot/Feedback");
                    folder.mkdirs();

                    CharSequence now = android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", new Date());
                    String fileName = now + ".jpg";

                    UCrop.of(Uri.parse("file://" + ss), Uri.fromFile(new File(folder, fileName)))
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(1080, 1080)
                            .start(getActivity());
                }
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                        if (getScreenshot != null) {
                            getScreenshot.setImageBitmap(bitmap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Toast.makeText(getActivity(), cropError.toString(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}
