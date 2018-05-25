package org.uusoftware.fuelify;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class NewsContent extends AppCompatActivity {

    String title, thumbnail, link, date;
    int alpha = 0;
    int color = 255;
    Drawable back, favorite, share;

    SQLiteDatabase mobiledatabase, mobiledatabase2;
    ArrayList<String> links = new ArrayList<>();

    Window window;
    ActionBar actionBar;
    WebView webView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content);

        //StatusBar
        window = this.getWindow();

        //ActionBar
        actionBar = this.getSupportActionBar();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ProgressBar
        progressBar = findViewById(R.id.progressBar1);

        //Color statusbar and actionbar
        coloredBars(Color.argb(50, 0, 0, 0), Color.TRANSPARENT);

        // Get Intents
        Bundle extras = getIntent().getExtras();
        title = extras.getString("title");
        thumbnail = extras.getString("thumbnail");
        link = extras.getString("link");
        date = extras.getString("date");

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Haber: " + link);
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        if (!link.contains("/amp")) {
            link = link + "amp";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            back = ContextCompat.getDrawable(this, R.drawable.ic_menu_back);
            favorite = ContextCompat.getDrawable(this, R.drawable.ic_menu_favorite);
            share = ContextCompat.getDrawable(this, R.drawable.ic_menu_share);
        } else {
            back = getResources().getDrawable(R.drawable.ic_menu_back);
            favorite = getResources().getDrawable(R.drawable.ic_menu_favorite);
            share = getResources().getDrawable(R.drawable.ic_menu_share);
        }

        //check the news added favorites
        mobiledatabase = openOrCreateDatabase("fuelspot_local", MODE_PRIVATE, null);
        mobiledatabase2 = openOrCreateDatabase("fuelspot_local2", MODE_PRIVATE, null);
        getFavorites();
        if (links.contains(link)) {
            favorite.setColorFilter(Color.argb(255, 255, 127, 80), PorterDuff.Mode.SRC_IN);
        }

        webView = findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                //Calculate color based on scrollY
                if (webView.getScrollY() <= 450) {
                    alpha = (int) (webView.getScrollY() / 2.25);
                    color = 255 - (webView.getScrollY() / 6);
                } else {
                    alpha = 200;
                    color = 0;
                }

                //ActionBar Items color
                back.setColorFilter(Color.argb(255, color, color, color), PorterDuff.Mode.SRC_IN);
                share.setColorFilter(Color.argb(255, color, color, color), PorterDuff.Mode.SRC_IN);
                if (!links.contains(link)) {
                    favorite.setColorFilter(Color.argb(255, color, color, color), PorterDuff.Mode.SRC_IN);
                }

                //BarColors
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    window.setStatusBarColor(Color.argb(alpha / 2, 0, 0, 0));
                }
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(alpha, 255, 255, 255)));
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("fuel-spot.com")) {
                    view.loadUrl(url);
                    return false;
                } else {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.enableUrlBarHiding();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(Color.parseColor("#00801e"));
                    customTabsIntent.launchUrl(NewsContent.this, Uri.parse(url));
                    return true;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        webView.loadUrl(link);
    }

    public void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, webView.getTitle() + "\n" + webView.getUrl().replace("/amp", ""));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

        ContentValues values = new ContentValues();
        values.put("Title", title);
        values.put("Thumbnail", thumbnail);
        values.put("Link", link);
        values.put("Date", date);
        mobiledatabase2.insert("fuelspot_local2", null, values);
    }

    public void favorite() {
        if (links.contains(link)) {
            mobiledatabase.delete("fuelspot_local",
                    "Title =? AND Thumbnail =? AND Link =? AND Date =?",
                    new String[]{title, thumbnail, link, date});
            Toast.makeText(this, getString(R.string.unfavorited), Toast.LENGTH_SHORT).show();
            favorite.setColorFilter(Color.argb(255, color, color, color), PorterDuff.Mode.SRC_IN);
        } else {
            ContentValues values = new ContentValues();
            values.put("Title", title);
            values.put("Thumbnail", thumbnail);
            values.put("Link", link);
            values.put("Date", date);
            mobiledatabase.insert("fuelspot_local", null, values);
            Toast.makeText(this, getString(R.string.favorites_added), Toast.LENGTH_SHORT).show();
            favorite.setColorFilter(Color.argb(255, 255, 127, 80), PorterDuff.Mode.SRC_IN);
        }
        getFavorites();
    }

    //getallfavorites from databese
    public void getFavorites() {
        links.clear();
        Cursor cur = mobiledatabase.rawQuery("Select * from fuelspot_local", null);
        if (cur.getCount() != 0) {
            cur.moveToFirst();
            do {
                for (int i = 0; i < cur.getColumnCount(); i++) {
                    String row_values = cur.getString(i);
                    if (i % 3 == 2) {
                        links.add(row_values);
                    }
                }
            } while (cur.moveToNext());
        }
        cur.close();
    }

    public void coloredBars(int color1, int color2) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(color1);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color2));
        } else {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color2));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_news, menu);
        MenuItem mFav = menu.findItem(R.id.action_favorite);
        MenuItem mShare = menu.findItem(R.id.action_share);
        mFav.setIcon(favorite);
        mShare.setIcon(share);
        getSupportActionBar().setHomeAsUpIndicator(back);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back.setColorFilter(null);
                favorite.setColorFilter(null);
                share.setColorFilter(null);
                mobiledatabase.close();
                mobiledatabase2.close();
                finish();
                return true;
            case R.id.action_favorite:
                favorite();
                return true;
            case R.id.action_share:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            back.setColorFilter(null);
            favorite.setColorFilter(null);
            share.setColorFilter(null);
            mobiledatabase.close();
            mobiledatabase2.close();
            finish();
        }
    }
}