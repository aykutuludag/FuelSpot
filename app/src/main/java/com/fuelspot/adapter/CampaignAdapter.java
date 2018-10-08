package com.fuelspot.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.R;
import com.fuelspot.model.CampaignItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {
    private List<CampaignItem> mItemList;
    private Context mContext;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CampaignAdapter.ViewHolder holder = (CampaignAdapter.ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
            View customView = inflater.inflate(R.layout.popup_campaign, null);
            final PopupWindow mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (Build.VERSION.SDK_INT >= 21) {
                mPopupWindow.setElevation(5.0f);
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

            ImageView imgPopup = customView.findViewById(R.id.title);
            Glide.with(mContext).load(mItemList.get(position).getCampaignPhoto()).into(imgPopup);

            TextView titlePopup = customView.findViewById(R.id.campaignTitle);
            titlePopup.setText(mItemList.get(position).getCampaignName());

            TextView descPopup = customView.findViewById(R.id.campaignDesc);
            descPopup.setText(mItemList.get(position).getCampaignDesc());

            TextView startTime = customView.findViewById(R.id.startTime);
            try {
                Date date = format.parse(mItemList.get(position).getCampaignStart());
                startTime.setText(format2.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            TextView endTime = customView.findViewById(R.id.endTime);
            try {
                Date date = format.parse(mItemList.get(position).getCampaignEnd());
                endTime.setText(format2.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            ImageView closeButton = customView.findViewById(R.id.imageViewClose);
            // Set a click listener for the popup window close button
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Dismiss the popup window
                    mPopupWindow.dismiss();
                }
            });
            mPopupWindow.setFocusable(true);
            mPopupWindow.update();
            mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    };

    public CampaignAdapter(Context context, List<CampaignItem> itemList) {
        mContext = context;
        mItemList = itemList;
    }

    @NonNull
    @Override
    public CampaignAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_campaign, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        CampaignItem feedItem = mItemList.get(i);

        viewHolder.campaignTitle.setText(feedItem.getCampaignName());

        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.default_automobile).error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(mContext).load(feedItem.getCampaignPhoto()).apply(options).into(viewHolder.campaignPhoto);

        // Handle click event on image click
        viewHolder.card.setOnClickListener(clickListener);
        viewHolder.card.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != mItemList ? mItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        TextView campaignTitle;
        ImageView campaignPhoto;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.single_campaign);
            campaignTitle = itemView.findViewById(R.id.textViewTitle);
            campaignPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }
}
