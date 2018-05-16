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

import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.uusoftware.fuelify.PurchaseDetails;
import org.uusoftware.fuelify.R;
import org.uusoftware.fuelify.model.PurchaseItem;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.uusoftware.fuelify.MainActivity.taxCalculator;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {
    private List<PurchaseItem> feedItemList;
    private Context mContext;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            Intent intent = new Intent(mContext, PurchaseDetails.class);
            intent.putExtra("ID", feedItemList.get(position).getID());
            intent.putExtra("STATION_NAME", feedItemList.get(position).getStationName());
            intent.putExtra("STATION_ICON", feedItemList.get(position).getStationIcon());
            intent.putExtra("STATION_LOC", feedItemList.get(position).getStationLocation());
            intent.putExtra("PURCHASE_TIME", feedItemList.get(position).getPurchaseTime());
            intent.putExtra("FUEL_TYPE_1", feedItemList.get(position).getFuelType());
            intent.putExtra("FUEL_PRICE_1", feedItemList.get(position).getFuelPrice());
            intent.putExtra("FUEL_LITER_1", feedItemList.get(position).getFuelLiter());
            intent.putExtra("FUEL_TYPE_2", feedItemList.get(position).getFuelType2());
            intent.putExtra("FUEL_PRICE_2", feedItemList.get(position).getFuelPrice2());
            intent.putExtra("FUEL_LITER_2", feedItemList.get(position).getFuelLiter2());
            intent.putExtra("TOTAL_PRICE", feedItemList.get(position).getTotalPrice());
            intent.putExtra("BILL_PHOTO", feedItemList.get(position).getBillPhoto());
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

        // UNIT 1
        String unitHolder = feedItem.getFuelPrice() + " TL";
        viewHolder.unitPrice1.setText(unitHolder);

        // PRICE 1
        float priceOne = (float) (feedItem.getFuelPrice() * feedItem.getFuelLiter());
        String priceHolder = String.format(Locale.getDefault(), "%.2f", priceOne) + " TL";
        viewHolder.price1.setText(priceHolder);

        // If user didn't purchased second type of fuel just hide these textViews
        if (feedItem.getFuelType2() != null && feedItem.getFuelType2().length() > 0) {
            // FUEL TYPE 2
            viewHolder.type2.setText(feedItem.getFuelType2());

            // AMOUNT 2
            viewHolder.amount2.setText(feedItem.getFuelLiter2() + " LT");

            // UNIT 2
            String unitHolder2 = feedItem.getFuelPrice2() + " TL";
            viewHolder.unitPrice2.setText(unitHolder2);

            // PRICE 2
            viewHolder.price2.setText(feedItem.getFuelPrice2() + " TL");
        } else {
            viewHolder.type2.setVisibility(View.GONE);
            viewHolder.amount2.setVisibility(View.GONE);
            viewHolder.unitPrice2.setVisibility(View.GONE);
            viewHolder.price2.setVisibility(View.GONE);
        }

        //TOTALTAX
        float tax1 = taxCalculator(feedItem.getFuelType(), (float) (feedItem.getFuelPrice() * feedItem.getFuelLiter()));
        float tax2 = taxCalculator(feedItem.getFuelType2(), (float) (feedItem.getFuelPrice2() * feedItem.getFuelLiter2()));
        String taxHolder = "VERGİ: " + String.format(Locale.getDefault(), "%.2f", tax1 + tax2);
        viewHolder.totalTax.setText(taxHolder);

        //TotalPrice
        String totalPriceHolder = "TOPLAM: " + feedItem.getTotalPrice() + " TL";
        viewHolder.totalPrice.setText(totalPriceHolder);

        // PurchaseTıme
        Date date = new Date(feedItem.getPurchaseTime());
        viewHolder.purchaseTime.setReferenceTime(date.getTime());

        //Station Icon
        Glide.with(mContext).load(feedItem.getStationIcon()).into(viewHolder.stationLogo);

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
        TextView stationName, type1, price1, unitPrice1, amount1, type2, price2, unitPrice2, amount2, totalTax, totalPrice;
        ImageView stationLogo;
        RelativeTimeTextView purchaseTime;

        ViewHolder(View itemView) {
            super(itemView);
            backgroundClick = itemView.findViewById(R.id.single_purchase);
            stationName = itemView.findViewById(R.id.stationName);
            stationLogo = itemView.findViewById(R.id.stationLogo);
            type1 = itemView.findViewById(R.id.type1);
            price1 = itemView.findViewById(R.id.price1);
            unitPrice1 = itemView.findViewById(R.id.unitPrice1);
            amount1 = itemView.findViewById(R.id.amount1);
            type2 = itemView.findViewById(R.id.type2);
            price2 = itemView.findViewById(R.id.price2);
            unitPrice2 = itemView.findViewById(R.id.unitPrice2);
            amount2 = itemView.findViewById(R.id.amount2);
            totalTax = itemView.findViewById(R.id.totalTax);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            purchaseTime = itemView.findViewById(R.id.purchaseTime);
        }
    }
}
