package com.fuelspot;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.USTimeFormat;

public class NewsDetail extends AppCompatActivity {

    String coverPhoto, title, content, url, sourceURL, publishDate;
    Window window;
    ActionBar actionBar;
    ImageView imageViewCover;
    TextView textViewTitle, textViewBody, textViewSourceURL;
    RelativeTimeTextView textViewPublished;
    private RequestOptions options;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        //StatusBar
        window = this.getWindow();

        //ActionBar
        actionBar = this.getSupportActionBar();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        requestQueue = Volley.newRequestQueue(this);

        //Color statusbar and actionbar
        coloredBars(Color.argb(45, 0, 0, 0), Color.argb(45, 0, 0, 0));

        // Get Intents
        Bundle extras = getIntent().getExtras();
        coverPhoto = extras.getString("COVER");
        title = extras.getString("TITLE");
        content = extras.getString("CONTENT");
        url = extras.getString("URL");
        sourceURL = extras.getString("SOURCE_URL");
        publishDate = extras.getString("PUBLISH_DATE");

        // Analytics
        Tracker t = ((Application) this.getApplication()).getDefaultTracker();
        t.setScreenName(url);
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        imageViewCover = findViewById(R.id.newsCover);
        textViewTitle = findViewById(R.id.newsTitle);
        textViewBody = findViewById(R.id.newsContent);
        textViewPublished = findViewById(R.id.newsPublished);
        textViewSourceURL = findViewById(R.id.newsSource);

        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_news).error(R.drawable.default_news)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        if (title != null && title.length() > 0) {
            loadNews();
        } else {
            fetchSingleNews();
        }
    }

    void fetchSingleNews() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_NEWS_SINGLE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);
                                title = obj.getString("title");
                                content = obj.getString("content");
                                coverPhoto = obj.getString("photo");
                                url = obj.getString("url");
                                sourceURL = obj.getString("sourceURL");
                                publishDate = obj.getString("releaseDate");
                                loadNews();
                            } catch (JSONException e) {
                                Toast.makeText(NewsDetail.this, e.toString(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(NewsDetail.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(NewsDetail.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("url", url);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void loadNews() {
        Glide.with(this).load(coverPhoto).apply(options).into(imageViewCover);
        textViewTitle.setText(title);
        textViewBody.setText(content);

        if (publishDate != null && publishDate.length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
            try {
                Date date = sdf.parse(publishDate);
                textViewPublished.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        textViewSourceURL.setText(sourceURL);
        textViewSourceURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.enableUrlBarHiding();
                builder.setShowTitle(true);
                builder.setToolbarColor(Color.parseColor("#212121"));
                customTabsIntent.launchUrl(NewsDetail.this, Uri.parse(sourceURL));
            }
        });
    }

    public void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + url);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
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
        getMenuInflater().inflate(R.menu.menu_news_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
        super.onBackPressed();
        finish();
    }
}