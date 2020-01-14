package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.adapter.BankingAdapter;
import com.fuelspot.model.BankingItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.FragmentProfile.userBankingList;
import static com.fuelspot.MainActivity.currencyCode;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.dimBehind;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.hasDoubleRange;
import static com.fuelspot.MainActivity.location;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userFSMoney;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.username;

public class StoreActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    int itemNo;
    public static SkuDetails premiumSku, doubleSku;
    float priceDoubleRange = 0.15f;
    float pricePremium = 0.25f;
    RequestOptions options;
    PopupWindow mPopupWindow;
    TextView textViewCurrentBalance;
    private RecyclerView mRecyclerView;
    private SharedPreferences prefs;
    private Window window;
    private Toolbar toolbar;
    private RequestQueue requestQueue;
    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        requestQueue = Volley.newRequestQueue(StoreActivity.this);

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        textViewCurrentBalance = findViewById(R.id.textViewCurrentBalance);
        String holder = String.format(Locale.getDefault(), "%.2f", userFSMoney) + " FP";
        textViewCurrentBalance.setText(holder);

        mRecyclerView = findViewById(R.id.bankingView);

        Button buttonBuyDoubleRange = findViewById(R.id.buttonPurchaseRange);
        if (hasDoubleRange) {
            buttonBuyDoubleRange.setText(getString(R.string.active));
        } else {
            buttonBuyDoubleRange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (billingClient != null) {
                        if (!premium) {
                            buyDoubleRange();
                        } else {
                            Toast.makeText(StoreActivity.this, getString(R.string.unsubscribe_2x_first), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(StoreActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        Button buttonDoubleRangeWithFP = findViewById(R.id.buttonPurchaseRangeWithFP);
        if (hasDoubleRange) {
            buttonDoubleRangeWithFP.setText(getString(R.string.active));
        } else {
            buttonDoubleRangeWithFP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userFSMoney >= priceDoubleRange) {
                        if (!premium) {
                            itemNo = 1;
                            buyRealItem(itemNo, v);
                        } else {
                            Toast.makeText(StoreActivity.this, getString(R.string.unsubscribe_2x_first), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(StoreActivity.this, getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


        Button buttonBuyPremium = findViewById(R.id.buttonPurchasePremium);
        if (premium) {
            buttonBuyPremium.setText(getString(R.string.active));
        } else {
            buttonBuyPremium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (billingClient != null) {
                        if (!hasDoubleRange) {
                            buyPremium();
                        } else {
                            Toast.makeText(StoreActivity.this, getString(R.string.unsubscribe_2x_first), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(StoreActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        Button buttonPremiumWithFP = findViewById(R.id.buttonPurchasePremiumWithFP);
        if (premium) {
            buttonPremiumWithFP.setText(getString(R.string.active));
        } else {
            buttonPremiumWithFP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userFSMoney >= pricePremium) {
                        if (!hasDoubleRange) {
                            itemNo = 2;
                            buyRealItem(itemNo, v);
                        } else {
                            Toast.makeText(StoreActivity.this, getString(R.string.unsubscribe_2x_first), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(StoreActivity.this, getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        loadTransactions();
        InAppBilling();
    }

    private void loadTransactions() {
        if (userBankingList != null && userBankingList.size() > 0) {
            RecyclerView.Adapter mAdapter = new BankingAdapter(StoreActivity.this, userBankingList);
            GridLayoutManager mLayoutManager = new GridLayoutManager(StoreActivity.this, 1);

            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
        } else {
            fetchBanking();
        }
    }

    private void InAppBilling() {
        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    billingClient = null;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                billingClient = null;
            }
        });
    }

    private void buyPremium() {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(premiumSku).build();
        billingClient.launchBillingFlow(StoreActivity.this, flowParams);
    }

    private void buyDoubleRange() {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(doubleSku).build();
        billingClient.launchBillingFlow(StoreActivity.this, flowParams);
    }

    private void buyRealItem(int itemNo, View view) {
        //POPUP
        //SCREEN 1 - ITEM
        //SCREEN 2 - RECEIVER
        //SCREEN 3 - PROMPT
        //SCREEN 4 - SUCCESS - FAIL

        final int[] whichPage = {1};
        final String[] productName = new String[1];
        final String[] productDesc = new String[1];
        final float[] productPrice = new float[1];
        int imageResourceID = R.drawable.default_campaign;

        switch (itemNo) {
            case 1:
                productName[0] = getString(R.string.double_range);
                productDesc[0] = getString(R.string.double_range_desc);
                productPrice[0] = priceDoubleRange;
                imageResourceID = R.drawable.popup_product1;
                break;
            case 2:
                productName[0] = getString(R.string.premium_version);
                productDesc[0] = getString(R.string.premium_version_desc);
                productPrice[0] = pricePremium;
                imageResourceID = R.drawable.popup_product1;
                break;
            default:
                Toast.makeText(StoreActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                break;
        }


        LayoutInflater inflater = (LayoutInflater) StoreActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_buy, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(8.0f);
        }

        final RelativeLayout layout1 = customView.findViewById(R.id.buyScreen1);
        final RelativeLayout layout2 = customView.findViewById(R.id.buyScreen2);
        final RelativeLayout layout3 = customView.findViewById(R.id.buyScreen3);

        // PAGE 1
        ImageView urunPhoto = customView.findViewById(R.id.productPhoto);
        Glide.with(StoreActivity.this).load(imageResourceID).apply(options).into(urunPhoto);

        TextView urunTitle1 = customView.findViewById(R.id.textViewProductTitle);
        urunTitle1.setText(productName[0]);

        TextView urunFiyat1 = customView.findViewById(R.id.urunfiyat);
        String dummy = productPrice[0] + " FP";
        urunFiyat1.setText(dummy);

        TextView urunAciklama = customView.findViewById(R.id.urunaciklama);
        urunAciklama.setText(productDesc[0]);

        // PAGE 2
        CircleImageView receiverPhoto = customView.findViewById(R.id.receiverPhoto);
        Glide.with(StoreActivity.this).load(photo).apply(options).into(receiverPhoto);

        TextView receiverName = customView.findViewById(R.id.receiverName);
        receiverName.setText(name);

        TextView receiverMail = customView.findViewById(R.id.receiverMail);
        receiverMail.setText(email);

        TextView receiverPhone = customView.findViewById(R.id.receiverPhone);
        receiverPhone.setText(userPhoneNumber);

        TextView receiverAddress = customView.findViewById(R.id.receiverAddress);
        receiverAddress.setText(location);

        // PAGE 3
        TextView urunTitle2 = customView.findViewById(R.id.textViewFinal);
        urunTitle2.setText(productName[0]);

        ImageView urunPhoto2 = customView.findViewById(R.id.productPhotoFinal);
        Glide.with(StoreActivity.this).load(imageResourceID).apply(options).into(urunPhoto2);

        TextView urunFiyat2 = customView.findViewById(R.id.urunfiyatFinal);
        String dummy2 = productPrice[0] + " FP";
        urunFiyat2.setText(dummy2);

        TextView finalPromptText = customView.findViewById(R.id.finalPrompt);
        float kalan = userFSMoney - productPrice[0];
        String dummy3 = getString(R.string.purchase_prompt_pre) + " " + String.format(Locale.getDefault(), "%.2f", kalan) + " " + getString(R.string.purchase_prompt_post);
        finalPromptText.setText(dummy3);

        Button buttonContinue = customView.findViewById(R.id.buttonProcessPurchase);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (whichPage[0]) {
                    case 1:
                        layout1.setVisibility(View.INVISIBLE);
                        layout2.setVisibility(View.VISIBLE);
                        whichPage[0] = 2;
                        break;
                    case 2:
                        if (name.length() > 0) {
                            if (email.length() > 0 && email.contains("@")) {
                                if (userPhoneNumber.length() > 0) {
                                    if (location.length() > 0) {
                                        layout2.setVisibility(View.INVISIBLE);
                                        layout3.setVisibility(View.VISIBLE);
                                        whichPage[0] = 3;
                                    } else {
                                        Toast.makeText(StoreActivity.this, getString(R.string.address_missing), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(StoreActivity.this, getString(R.string.phone_missing), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(StoreActivity.this, getString(R.string.email_missing), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(StoreActivity.this, getString(R.string.fullname_missing), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 3:
                        processPurchase(productName[0], productPrice[0]);
                        break;
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

        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBehind(mPopupWindow);
    }

    private void processPurchase(final String productName, final float productPrice) {
        final ProgressDialog loading = ProgressDialog.show(StoreActivity.this, getString(R.string.purchasing), getString(R.string.please_wait), false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ORDER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.equals("Success")) {
                            mPopupWindow.dismiss();
                            if (itemNo == 1) {
                                hasDoubleRange = true;
                                mapDefaultRange = 6000;
                                mapDefaultZoom = 12f;

                                Toast.makeText(StoreActivity.this, getString(R.string.double_range_successful), Toast.LENGTH_LONG).show();
                            } else if (itemNo == 2) {
                                premium = true;
                                mapDefaultRange = 6000;
                                mapDefaultZoom = 12f;

                                Toast.makeText(StoreActivity.this, getString(R.string.premium_successful), Toast.LENGTH_LONG).show();
                            }

                            prefs.edit().putBoolean("hasDoubleRange", hasDoubleRange).apply();
                            prefs.edit().putBoolean("hasPremium", premium).apply();
                            prefs.edit().putInt("RANGE", mapDefaultRange).apply();
                            prefs.edit().putFloat("ZOOM", mapDefaultZoom).apply();

                            fetchBanking();
                        } else {
                            Toast.makeText(StoreActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(StoreActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show();
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
                params.put("product", productName);
                params.put("price", String.valueOf(productPrice));
                params.put("name", name);
                params.put("address", location);
                params.put("phone", userPhoneNumber);
                params.put("email", email);
                params.put("currency", currencyCode);
                params.put("country", userCountry);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchBanking() {
        userBankingList.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_BANKING) + "?username=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    BankingItem item = new BankingItem();
                                    item.setID(obj.getInt("id"));
                                    item.setUsername(obj.getString("username"));
                                    item.setType(obj.getString("processType"));
                                    item.setCurrency(obj.getString("currency"));
                                    item.setCountry(obj.getString("country"));
                                    item.setAmount((float) obj.getDouble("amount"));
                                    item.setPreviousBalance((float) obj.getDouble("previous_balance"));
                                    item.setCurrentBalance((float) obj.getDouble("current_balance"));
                                    item.setTransactionTime(obj.getString("time"));
                                    item.setNotes(obj.getString("notes"));
                                    userBankingList.add(item);
                                }

                                userFSMoney = (float) res.getJSONObject(0).getDouble("current_balance");
                                String dummyMoneyText = String.format(Locale.getDefault(), "%.2f", userFSMoney) + " " + currencySymbol;
                                textViewCurrentBalance.setText(dummyMoneyText);

                                loadTransactions();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    /**
     * Implement this method to get notifications for purchases updates. Both purchases initiated by
     * your app and the ones initiated by Play Store will be reported here.
     *
     * @param billingResult BillingResult of the update.
     * @param purchases     List of updated purchases if present.
     */
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            ArrayList<String> ownedSkus = new ArrayList<>();
            for (int i = 0; i < purchases.size(); i++) {
                ownedSkus.add(purchases.get(i).getSku());
            }

            if (ownedSkus.contains("premium") || ownedSkus.contains("premium_super")) {
                premium = true;
                mapDefaultRange = 6000;
                mapDefaultZoom = 12f;

                Toast.makeText(StoreActivity.this, getString(R.string.premium_successful), Toast.LENGTH_LONG).show();
            } else if (ownedSkus.contains("2x_range") || ownedSkus.contains("2x_range_super")) {
                hasDoubleRange = true;
                mapDefaultRange = 6000;
                mapDefaultZoom = 12f;

                Toast.makeText(StoreActivity.this, getString(R.string.double_range_successful), Toast.LENGTH_LONG).show();
            }

            prefs.edit().putBoolean("hasDoubleRange", hasDoubleRange).apply();
            prefs.edit().putBoolean("hasPremium", premium).apply();
            prefs.edit().putInt("RANGE", mapDefaultRange).apply();
            prefs.edit().putFloat("ZOOM", mapDefaultZoom).apply();
        } else {
            Toast.makeText(StoreActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
