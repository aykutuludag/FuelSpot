package com.fuelspot.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.R;
import com.fuelspot.model.NewsItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = sdf.parse(feedItem.getPublishDate());
                viewHolder.newsTime.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.photo_placeholder)
                .error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
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
        Uri newsUri = Uri.parse(feedItem.getURL());

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        builder.enableUrlBarHiding();
        builder.setShowTitle(true);
        builder.setToolbarColor(Color.parseColor("#FF7439"));
        customTabsIntent.launchUrl(mContext, newsUri);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView text;
        RelativeLayout layout;
        ImageView background;
        RelativeTimeTextView newsTime;

        ViewHolder(View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.newsBackground);
            layout = itemView.findViewById(R.id.top_layout);
            text = itemView.findViewById(R.id.txt_text);
            newsTime = itemView.findViewById(R.id.news_published);
        }
    }
}