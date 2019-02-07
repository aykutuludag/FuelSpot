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
    private RecyclerView.Adapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private TextView textViewCurrentBalance;
    private ServiceConnection mServiceConn;
    private IInAppBillingService mService;
    private SharedPreferences prefs;
    private Window window;
    private Toolbar toolbar;
    private Button buttonBuyPremium;
    private Button buttonBuyDoubleRange;
    private Button buttonAracKokusu;
    private Button buttonLastikSpreyi;
    private Button buttonBakimKiti;

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
        coloredBars(Color.parseColor("#000000"), Color.parseColor("#ffffff"));

        prefs = getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        textViewCurrentBalance = findViewById(R.id.textViewCurrentBalance);
        String holder = "MEVCUT BAKİYE: " + String.format(Locale.getDefault(), "%.2f", userFSMoney) + " " + currencySymbol;
        textViewCurrentBalance.setText(holder);

        mRecyclerView = findViewById(R.id.bankingView);
        buttonBuyPremium = findViewById(R.id.buttonPurchasePremium);
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

        buttonBuyDoubleRange = findViewById(R.id.buttonPurchaseRange);
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

        buttonAracKokusu = findViewById(R.id.button_item1);
        buttonAracKokusu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFSMoney >= 4.99f) {
                    Toast.makeText(StoreActivity.this, "I didn't wrote these code yet", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(StoreActivity.this, "Bakiyeniz yetersiz", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonLastikSpreyi = findViewById(R.id.button_item2);
        buttonLastikSpreyi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFSMoney >= 29.9f) {
                    Toast.makeText(StoreActivity.this, "I didn't wrote these code yet", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(StoreActivity.this, "Bakiyeniz yetersiz", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonBakimKiti = findViewById(R.id.button_item3);
        buttonBakimKiti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFSMoney >= 49.9f) {
                    Toast.makeText(StoreActivity.this, "I didn't wrote these code yet", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(StoreActivity.this, "Bakiyeniz yetersiz", Toast.LENGTH_LONG).show();
                }
            }
        });

        loadTransactions();
        InAppBilling();
    }

    private void loadTransactions() {
        if (userBankingList != null && userBankingList.size() > 0) {
            mAdapter = new BankingAdapter(StoreActivity.this, userBankingList);
            mLayoutManager = new GridLayoutManager(StoreActivity.this, 1);

            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }
    }

    private void InAppBilling() {
        mServiceConn = new ServiceConnection() {
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
        Toast.makeText(StoreActivity.this,
                "Premium sürüm reklamları kaldırır ve menzilinizi 2 katına çıkarır.", Toast.LENGTH_LONG)
                .show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "premium", "subs",
                "dfgfddfgdfgasd/sdfsffgdgfgjkjk/ajyUFbAyw93xVnDkeTZFdhdSdJ8M");
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        assert pendingIntent != null;
        startIntentSenderForResult(pendingIntent.getIntentSender(), PURCHASE_PREMIUM, new Intent(), 0,
                0, 0);
    }

    private void buyDoubleRange() throws RemoteException, IntentSender.SendIntentException {
        Toast.makeText(StoreActivity.this,
                "Menzilinizi 2 kat artırır. 5000 metreye çapınızdaki bütün istasyonları görebilirsiniz", Toast.LENGTH_LONG)
                .show();
        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "2x_range", "subs",
                "dfkjk/ajyUFbAyw93xVnDkeTZFdhdSdJ8M");
        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
        assert pendingIntent != null;
        startIntentSenderForResult(pendingIntent.getIntentSender(), PURCHASE_PREMIUM, new Intent(), 0,
                0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PURCHASE_PREMIUM:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(StoreActivity.this, "Satın alma başarılı. Premium sürüme geçiriliyorsunuz, teşekkürler!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(StoreActivity.this, "Satın alma başarısız. Lütfen daha sonra tekrar deneyiniz.", Toast.LENGTH_LONG).show();
                    prefs.edit().putBoolean("hasPremium", false).apply();
                }
                break;
            case PURCHASE_DOUBLE_RANGE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(StoreActivity.this, "Satın alma başarılı. Menziliniz artırılıyor, teşekkürler!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(StoreActivity.this, "Satın alma başarısız. Lütfen daha sonra tekrar deneyiniz.", Toast.LENGTH_LONG).show();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
