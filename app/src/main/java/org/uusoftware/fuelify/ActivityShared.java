package org.uusoftware.fuelify;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.uusoftware.fuelify.adapter.NewsAdapter;
import org.uusoftware.fuelify.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class ActivityShared extends AppCompatActivity {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    List<NewsItem> feedsList = new ArrayList<>();
    ProgressBar pb;
    SQLiteDatabase mobiledatabase;
    Cursor cur;
    ArrayList<String> arraytitle = new ArrayList<>();
    ArrayList<String> arraythumbnail = new ArrayList<>();
    ArrayList<String> arraylink = new ArrayList<>();
    ArrayList<String> arraydate = new ArrayList<>();

    Window window;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //StatusBar
        window = this.getWindow();

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coloredBars(Color.parseColor("#000000"), Color.parseColor("#ffffff"));

        pb = findViewById(R.id.progressBar1);
        pb.getIndeterminateDrawable().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Paylaşılanlar");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        // ConnectivityManager
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        //getFavorites
        mobiledatabase = openOrCreateDatabase("fuelspot_local2", MODE_PRIVATE, null);
        pb.setVisibility(View.INVISIBLE);
        cur = mobiledatabase.rawQuery("Select * from fuelspot_local2", null);
        if (cur.getCount() != 0) {
            cur.moveToFirst();
            do {
                for (int i = 0; i < cur.getColumnCount(); i++) {
                    String row_values = cur.getString(i);
                    if (i % 4 == 0) {
                        // For Text
                        arraytitle.add(row_values);
                    } else if (i % 4 == 1) {
                        // For Image
                        arraythumbnail.add(row_values);
                    } else if (i % 4 == 2) {
                        // For Link
                        arraylink.add(row_values);
                    } else {
                        // For Date
                        arraydate.add(row_values);
                    }
                }
            } while (cur.moveToNext());
        }
        cur.close();
        for (int i = 0; i < arraytitle.size(); i++) {
            NewsItem item = new NewsItem();
            item.setTitle(arraytitle.get(i));
            item.setThumbnail(arraythumbnail.get(i));
            item.setLink(arraylink.get(i));
            item.setPublishDate(arraydate.get(i));
            feedsList.add(item);
        }

        //recyclerView
        mRecyclerView = findViewById(R.id.recycler_view);

        // Adapter
        mAdapter = new NewsAdapter(ActivityShared.this, feedsList);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);

        // The number of Columns
        mLayoutManager = new GridLayoutManager(ActivityShared.this, 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                if ((position % 3) == 0) {
                    return (2);
                } else {
                    return (1);
                }
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (!isConnected) {
            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
        mobiledatabase.close();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}