package com.fuelspot.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.MessagingActivity;
import com.fuelspot.R;
import com.fuelspot.model.MessageItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.MainActivity.USTimeFormat;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {
    private List<MessageItem> feedItemList;
    private Context mContext;
    private String whichScreen;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            InboxAdapter.ViewHolder holder = (InboxAdapter.ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            MessageItem yItem = feedItemList.get(position);

            if (!whichScreen.equals("MESSENGER")) {
                Intent intent = new Intent(mContext, MessagingActivity.class);
                intent.putExtra("CONVERSATION_ID", yItem.getConversationID());
                intent.putExtra("TOPIC", yItem.getTopic());
                mContext.startActivity(intent);
            }
        }
    };

    public InboxAdapter(Context context, List<MessageItem> feedItemList, String whichScreen) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.whichScreen = whichScreen;
    }

    @NonNull
    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_message, viewGroup, false);
        return new InboxAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxAdapter.ViewHolder viewHolder, int i) {
        MessageItem feedItem = feedItemList.get(i);

        viewHolder.textViewSenderName.setText(feedItem.getSender());

        SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
        Date date = new Date();
        try {
            date = format.parse(feedItem.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        viewHolder.textViewTime.setReferenceTime(date.getTime());

        if (whichScreen.equals("INBOX")) {
            viewHolder.textViewMessage.setText(feedItem.getTopic());

            if (feedItem.getIsOpen() == 0) {
                viewHolder.textViewTime.setVisibility(View.GONE);
            }
        } else {
            viewHolder.textViewMessage.setText(feedItem.getMessage());
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
        Glide.with(mContext.getApplicationContext()).load(feedItem.getSenderPhoto()).apply(options).into(viewHolder.senderImage);


        // Handle click event on image click
        viewHolder.card.setOnClickListener(clickListener);
        viewHolder.card.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        TextView textViewMessage;
        TextView textViewSenderName;
        RelativeTimeTextView textViewTime;
        ImageView senderImage;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_message);
            senderImage = itemView.findViewById(R.id.imageViewSender);
            textViewMessage = itemView.findViewById(R.id.message);
            textViewSenderName = itemView.findViewById(R.id.textViewSender);
            textViewTime = itemView.findViewById(R.id.time);
        }
    }
}