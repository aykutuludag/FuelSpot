package org.uusoftware.fuelify.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

import org.uusoftware.fuelify.NewsContent;
import org.uusoftware.fuelify.R;
import org.uusoftware.fuelify.model.NewsItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsItem> feedItemList;
    private Context mContext;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getPosition();
            NewsItem feedItem = feedItemList.get(position);
            Intent intent;

            intent = new Intent(mContext, NewsContent.class);
            intent.putExtra("title", feedItem.getTitle());
            intent.putExtra("content", feedItem.getContent());
            intent.putExtra("link", feedItem.getLink());
            mContext.startActivity(intent);
        }
    };

    public NewsAdapter(Context context, List<NewsItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_news, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        NewsItem feedItem = feedItemList.get(i);

        // Download image using picasso library
        String encodedUrl = feedItem.getThumbnail().replace("ç", "%C3%A7").replace("Ç", "%C3%87").replace("ğ", "%C4%9F")
                .replace("Ğ", "%C4%9E").replace("ı", "%C4%B1").replace("İ", "%C4%B0").replace("ö", "%C3%B6")
                .replace("Ö", "%C3%96").replace("ş", "%C5%9F").replace("Ş", "%C5%9E").replace("ü", "%C3%BC")
                .replace("Ü", "%C3%9C");

        Picasso.with(mContext).load(encodedUrl).error(R.drawable.empty).placeholder(R.drawable.empty)
                .into(viewHolder.image);

        // Setting text view title
        viewHolder.text.setText(feedItem.getTitle());

        // NewsTime
        System.out.println(feedItem.getPublishDate());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(feedItem.getPublishDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) {
            viewHolder.newsTime.setReferenceTime(date.getTime());
        }

        // Handle click event on both title and image click
        viewHolder.text.setOnClickListener(clickListener);
        viewHolder.image.setOnClickListener(clickListener);

        viewHolder.text.setTag(viewHolder);
        viewHolder.image.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView text;
        public RelativeTimeTextView newsTime;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_thumbnail);
            text = itemView.findViewById(R.id.txt_text);
            newsTime = itemView.findViewById(R.id.news_published);
        }
    }
}