package com.fuelspot.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.NewsDetail;
import com.fuelspot.R;
import com.fuelspot.model.NewsItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.adCount;
import static com.fuelspot.MainActivity.admobInterstitial;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> feedItemList;
    private Context mContext;

    public NewsAdapter(Context context, List<NewsItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_news, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        NewsItem feedItem = feedItemList.get(i);

        // Setting text view title
        if (feedItem.getTitle() != null && feedItem.getTitle().length() > 0) {
            viewHolder.text.setText(feedItem.getTitle());
        }

        // NewsTime
        if (feedItem.getPublishDate() != null && feedItem.getPublishDate().length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
            try {
                Date date = sdf.parse(feedItem.getPublishDate());
                viewHolder.newsTime.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_news)
                .error(R.drawable.default_news)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(mContext).load(feedItem.getPhoto()).apply(options).into(viewHolder.background);

        // Handle click event on both title and image click
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNews(viewHolder.getAdapterPosition());
            }
        });
    }

    private void openNews(int position) {
        NewsItem feedItem = feedItemList.get(position);

        Intent intent = new Intent(mContext, NewsDetail.class);
        intent.putExtra("COVER", feedItem.getPhoto());
        intent.putExtra("TITLE", feedItem.getTitle());
        intent.putExtra("CONTENT", feedItem.getContent());
        intent.putExtra("URL", feedItem.getURL());
        intent.putExtra("SOURCE_URL", feedItem.getSourceURL());
        intent.putExtra("PUBLISH_DATE", feedItem.getPublishDate());
        showAds(intent);
    }

    private void showAds(Intent intent) {
        if (admobInterstitial != null && admobInterstitial.isLoaded()) {
            //Facebook ads doesnt loaded he will see AdMob
            mContext.startActivity(intent);
            admobInterstitial.show();
            adCount++;
            admobInterstitial = null;
        } else {
            // Ads doesn't loaded.
            mContext.startActivity(intent);
        }

        if (adCount == 2) {
            Toast.makeText(mContext, mContext.getString(R.string.last_ads_info), Toast.LENGTH_SHORT).show();
            adCount++;
        }
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        RelativeLayout layout;
        ImageView background;
        RelativeTimeTextView newsTime;

        ViewHolder(View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.newsBackground);
            layout = itemView.findViewById(R.id.top_layout);
            text = itemView.findViewById(R.id.txt_text);
            newsTime = itemView.findViewById(R.id.newsPublished);
        }
    }
}