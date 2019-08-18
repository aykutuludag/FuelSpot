package com.fuelspot.superuser;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.fuelspot.R;

import java.util.ArrayList;
import java.util.List;

import static com.fuelspot.MainActivity.hasDoubleRange;
import static com.fuelspot.MainActivity.mapDefaultRange;
import static com.fuelspot.MainActivity.mapDefaultZoom;
import static com.fuelspot.MainActivity.premium;

public class SuperStoreActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    public static SkuDetails premiumSuperSku, doubleSuperSku;
    private SharedPreferences prefs;
    private Window window;
    private Toolbar toolbar;
    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_store);

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

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

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
                            Toast.makeText(SuperStoreActivity.this, "Premium aboneliğini başlatabilmek için öncelikle Google Play uygulamasından 2x-Range aboneliğinizi iptal etmeniz gerekiyor.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

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
                            Toast.makeText(SuperStoreActivity.this, "2x-Range aboneliğini başlatabilmek için öncelikle Google Play uygulamasından Premium aboneliğinizi iptal etmeniz gerekiyor.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

        InAppBilling();
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
        Toast.makeText(SuperStoreActivity.this, getString(R.string.premium_version_desc), Toast.LENGTH_LONG).show();
        BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(premiumSuperSku).build();
        billingClient.launchBillingFlow(SuperStoreActivity.this, flowParams);
    }

    private void buyDoubleRange() {
        Toast.makeText(SuperStoreActivity.this, getString(R.string.double_range_desc), Toast.LENGTH_LONG).show();
        BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(doubleSuperSku).build();
        billingClient.launchBillingFlow(SuperStoreActivity.this, flowParams);
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

                Toast.makeText(SuperStoreActivity.this, getString(R.string.premium_successful), Toast.LENGTH_LONG).show();
            } else if (ownedSkus.contains("2x_range") || ownedSkus.contains("2x_range_super")) {
                hasDoubleRange = true;
                mapDefaultRange = 6000;
                mapDefaultZoom = 12f;

                Toast.makeText(SuperStoreActivity.this, getString(R.string.double_range_successful), Toast.LENGTH_LONG).show();
            }

            prefs.edit().putBoolean("hasDoubleRange", hasDoubleRange).apply();
            prefs.edit().putBoolean("hasPremium", premium).apply();
            prefs.edit().putInt("RANGE", mapDefaultRange).apply();
            prefs.edit().putFloat("ZOOM", mapDefaultZoom).apply();
        } else {
            Toast.makeText(SuperStoreActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
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
