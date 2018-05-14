package org.uusoftware.fuelify.adapter;

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

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

import org.uusoftware.fuelify.PurchaseDetails;
import org.uusoftware.fuelify.R;
import org.uusoftware.fuelify.model.PurchaseItem;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {
    private List<PurchaseItem> feedItemList;
    private Context mContext;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            Intent intent = new Intent(mContext, PurchaseDetails.class);
           /* intent.putExtra("STATION_NAME", feedItemList.get(position).getStationName());
            intent.putExtra("STATION_VICINITY", feedItemList.get(position).getVicinity());
            intent.putExtra("STATION_LOCATION", feedItemList.get(position).getLocation());
            intent.putExtra("STATION_DISTANCE", feedItemList.get(position).getDistance());
            intent.putExtra("STATION_LASTUPDATED", feedItemList.get(position).getLastUpdated());
            intent.putExtra("STATION_GASOLINE", feedItemList.get(position).getGasolinePrice());
            intent.putExtra("STATION_DIESEL", feedItemList.get(position).getDieselPrice());
            intent.putExtra("STATION_LPG", feedItemList.get(position).getLpgPrice());
            intent.putExtra("STATION_ELECTRIC", feedItemList.get(position).getElectricityPrice());
            intent.putExtra("STATION_ICON", feedItemList.get(position).getPhotoURL());
            intent.putExtra("STATION_ID", feedItemList.get(position).getID());*/
            mContext.startActivity(intent);
        }
    };

    public PurchaseAdapter(Context context, List<PurchaseItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_purchases, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final PurchaseItem feedItem = feedItemList.get(i);

        // STATION NAME
        viewHolder.stationName.setText(feedItem.getStationName());

        // FUEL TYPE 1
        viewHolder.type1.setText(feedItem.getFuelType());

        // AMOUNT 1
        viewHolder.amount1.setText(feedItem.getFuelLiter() + " LT");

        // TAX 1
        double tax = feedItem.getFuelPrice() * 0.67f;
        viewHolder.tax1.setText(String.format(Locale.getDefault(), "%.2f", tax) + " TL (%67)");

        // PRICE 1
        viewHolder.price1.setText(feedItem.getFuelPrice() + " TL");

        // If user didn't purchased second type of fuel just hide these textViews
        if (feedItem.getFuelType2() != null && feedItem.getFuelType2().length() > 0) {
            // FUEL TYPE 2
            viewHolder.type2.setText(feedItem.getFuelType2());

            // AMOUNT 2
            viewHolder.amount2.setText(feedItem.getFuelLiter2() + " LT");

            // TAX 2
            double tax2 = feedItem.getFuelPrice() * 0.67f;
            viewHolder.tax2.setText(String.format(Locale.getDefault(),"%.2f", tax2) + " TL (%67)");

            // PRICE 2
            viewHolder.price2.setText(feedItem.getFuelPrice2() + " TL");
        } else {
            viewHolder.type2.setVisibility(View.GONE);
            viewHolder.amount2.setVisibility(View.GONE);
            viewHolder.tax2.setVisibility(View.GONE);
            viewHolder.price2.setVisibility(View.GONE);
        }

        //TotalPrice
        viewHolder.totalPrice.setText(feedItem.getTotalPrice() + " TL");

        // PurchaseTıme
        Date date = new Date(feedItem.getPurchaseTime());
        viewHolder.purchaseTime.setReferenceTime(date.getTime());

        //Station Icon
        Picasso.with(mContext).load(feedItem.getStationIcon()).error(R.drawable.unknown).placeholder(R.drawable.unknown)
                .into(viewHolder.stationLogo);

        // Handle click event on image click
        viewHolder.backgroundClick.setOnClickListener(clickListener);
        viewHolder.backgroundClick.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout backgroundClick;
        TextView stationName, type1, price1, tax1, amount1, type2, price2, tax2, amount2, totalPrice;
        ImageView stationLogo;
        RelativeTimeTextView purchaseTime;

        ViewHolder(View itemView) {
            super(itemView);
            backgroundClick = itemView.findViewById(R.id.single_purchase);
            stationName = itemView.findViewById(R.id.stationName);
            stationLogo = itemView.findViewById(R.id.stationLogo);
            type1 = itemView.findViewById(R.id.type1);
            price1 = itemView.findViewById(R.id.price1);
            tax1 = itemView.findViewById(R.id.tax1);
            amount1 = itemView.findViewById(R.id.amount1);
            type2 = itemView.findViewById(R.id.type2);
            price2 = itemView.findViewById(R.id.price2);
            tax2 = itemView.findViewById(R.id.tax2);
            amount2 = itemView.findViewById(R.id.amount2);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            purchaseTime = itemView.findViewById(R.id.purchaseTime);
        }
    }
}
