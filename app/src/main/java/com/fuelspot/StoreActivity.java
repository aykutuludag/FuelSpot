package com.fuelspot;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.fuelspot.adapter.BankingAdapter;

import java.util.Locale;

import static com.fuelspot.FragmentProfile.userBankingList;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.hasDoubleRange;
import static com.fuelspot.MainActivity.premium;
import static com.fuelspot.MainActivity.userFSMoney;

public class StoreActivity extends AppCompatActivity {

    private static final int PURCHASE_PREMIUM = 13200;
    private static final int PURCHASE_DOUBLE_RANGE = 63000;
    private RecyclerView mRecyclerView;
    private IInAppBillingService mService;
    private SharedPreferences prefs;
    private Window window;
    private Toolbar toolbar;
    int itemNo;

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

        //Window
        window = this.getWindow();
        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        TextView textViewCurrentBalance = findViewById(R.id.textViewCurrentBalance);
        String holder = "MEVCUT BAKİYE: " + String.format(Locale.getDefault(), "%.2f", userFSMoney) + " " + currencySymbol;
        textViewCurrentBalance.setText(holder);

        mRecyclerView = findViewById(R.id.bankingView);
        Button buttonBuyPremium = findViewById(R.id.buttonPurchasePremium);
        if (premium) {
            buttonBuyPremium.setText("Aktif");
        } else {
            buttonBuyPremium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mService != null) {
                        try {
                            if (!hasDoubleRange) {
                                buyPremium();
                            } else {
                                Toast.makeText(StoreActivity.this, "Premium sürüm 2x menzil özelliğini de kapsamaktadır. Premium sürüme geçmeden önce lütfen 2x menzili iptal ediniz.", Toast.LENGTH_LONG).show();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        Button buttonBuyDoubleRange = findViewById(R.id.buttonPurchaseRange);
        if (hasDoubleRange) {
            buttonBuyDoubleRange.setText("Aktif");
        } else {
            buttonBuyDoubleRange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mService != null) {
                        try {
                            if (!premium) {
                                buyDoubleRange();
                            } else {
                                Toast.makeText(StoreActivity.this, "Premium sürüm 2x menzil özelliğini de kapsamaktadır. Ayrıca 2x menzil satın almanıza gerek yoktur.", Toast.LENGTH_LONG).show();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        Button buttonAracKokusu = findViewById(R.id.button_item1);
        buttonAracKokusu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFSMoney >= 4.99f) {
                    itemNo = 1;
                    buyRealItem(itemNo);
                } else {
                    Toast.makeText(StoreActivity.this, getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonLastikSpreyi = findViewById(R.id.button_item2);
        buttonLastikSpreyi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFSMoney >= 29.9f) {
                    itemNo = 2;
                    buyRealItem(itemNo);
                } else {
                    Toast.makeText(StoreActivity.this, getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonBakimKiti = findViewById(R.id.button_item3);
        buttonBakimKiti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFSMoney >= 49.9f) {
                    itemNo = 3;
                    buyRealItem(itemNo);
                } else {
                    Toast.makeText(StoreActivity.this, getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
                }
            }
        });

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
        }
    }

    private void InAppBilling() {
        ServiceConnection mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    private void buyPremium() throws RemoteException, IntentSender.SendIntentException {
        Toast.makeText(StoreActivity.this, getString(R.string.premium_version_desc), Toast.LENGTH_LONG).show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "premium", "subs",
                "dfgfddfgdfgasd/sdfsffgdgfgjkjk/ajyUFbAyw93xVnDkeTZFdhdSdJ8M");
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        assert pendingIntent != null;
        startIntentSenderForResult(pendingIntent.getIntentSender(), PURCHASE_PREMIUM, new Intent(), 0,
                0, 0);
    }

    private void buyDoubleRange() throws RemoteException, IntentSender.SendIntentException {
        Toast.makeText(StoreActivity.this, getString(R.string.double_range_desc), Toast.LENGTH_LONG).show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "2x_range", "subs",
                "dfkjk/ajyUFbAyw93xVnDkeTZFdhdSdJ8M");
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        assert pendingIntent != null;
        startIntentSenderForResult(pendingIntent.getIntentSender(), PURCHASE_PREMIUM, new Intent(), 0,
                0, 0);
    }

    private void buyRealItem(int id) {
        //POPUP
        //SCREEN 1 - ITEM
        //SCREEN 2 - RECEIVER
        //SCREEN 3 - PROMPT
        //SCREEN 4 - SUCCESS - FAIL
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PURCHASE_PREMIUM:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(StoreActivity.this, getString(R.string.premium_successful), Toast.LENGTH_LONG).show();
                    premium = true;
                    prefs.edit().putBoolean("hasPremium", premium).apply();
                    prefs.edit().putInt("RANGE", 5000).apply();
                    prefs.edit().putFloat("ZOOM", 12f).apply();
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    if (i != null) {
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                } else {
                    Toast.makeText(StoreActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    prefs.edit().putBoolean("hasPremium", false).apply();
                }
                break;
            case PURCHASE_DOUBLE_RANGE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(StoreActivity.this, getString(R.string.double_range_successful), Toast.LENGTH_LONG).show();
                    hasDoubleRange = true;
                    prefs.edit().putBoolean("hasDoubleRange", hasDoubleRange).apply();
                    prefs.edit().putInt("RANGE", 5000).apply();
                    prefs.edit().putFloat("ZOOM", 12f).apply();
                    Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                    if (i != null) {
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                } else {
                    Toast.makeText(StoreActivity.this, getString(R.string.purchase_failed), Toast.LENGTH_LONG).show();
                    prefs.edit().putBoolean("hasDoubleRange", false).apply();
                }
                break;
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
