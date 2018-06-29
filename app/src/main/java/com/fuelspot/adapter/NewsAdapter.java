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

        // Download image using picasso library
        if (feedItem.getThumbnail() != null && feedItem.getThumbnail().length() > 0) {
            String encodedUrl = feedItem.getThumbnail().replace("ç", "%C3%A7").replace("Ç", "%C3%87").replace("ğ", "%C4%9F")
                    .replace("Ğ", "%C4%9E").replace("ı", "%C4%B1").replace("İ", "%C4%B0").replace("ö", "%C3%B6")
                    .replace("Ö", "%C3%96").replace("ş", "%C5%9F").replace("Ş", "%C5%9E").replace("ü", "%C3%BC")
                    .replace("Ü", "%C3%9C");

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.photo_placeholder)
                    .error(R.drawable.photo_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            Glide.with(mContext).load(Uri.parse(encodedUrl)).apply(options).into(viewHolder.image);
        }


        // Setting text view title
        if (feedItem.getTitle() != null && feedItem.getTitle().length() > 0) {
            viewHolder.text.setText(feedItem.getTitle());
        }

        // NewsTime
        if (feedItem.getPublishDate() != null && feedItem.getPublishDate().length() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            try {
                Date date = sdf.parse(feedItem.getPublishDate());
                viewHolder.newsTime.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Handle click event on both title and image click
        viewHolder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNews(viewHolder.getAdapterPosition());
            }
        });
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNews(viewHolder.getAdapterPosition());
            }
        });
    }

    private void openNews(int position) {
        NewsItem feedItem = feedItemList.get(position);
        Uri newsUri = Uri.parse(feedItem.getLink());

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

        public ImageView image;
        public TextView text;
        RelativeTimeTextView newsTime;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_thumbnail);
            text = itemView.findViewById(R.id.txt_text);
            newsTime = itemView.findViewById(R.id.news_published);
        }
    }
}