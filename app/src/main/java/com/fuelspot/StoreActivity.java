package com.fuelspot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fuelspot.adapter.BankingAdapter;

import java.util.Locale;

import static com.fuelspot.FragmentProfile.userBankingList;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.userFSMoney;

public class StoreActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    GridLayoutManager mLayoutManager;
    TextView textViewCurrentBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewCurrentBalance = findViewById(R.id.textViewCurrentBalance);
        String holder = "MEVCUT BAKİYE: " + String.format(Locale.getDefault(), "%.2f", userFSMoney) + " " + currencySymbol;
        textViewCurrentBalance.setText(holder);

        mRecyclerView = findViewById(R.id.bankingView);

        loadTransactions();
    }

    void loadTransactions() {
        if (userBankingList != null && userBankingList.size() > 0) {
            mAdapter = new BankingAdapter(StoreActivity.this, userBankingList);
            mLayoutManager = new GridLayoutManager(StoreActivity.this, 1);

            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
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
