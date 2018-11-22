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

import com.bumptech.glide.Glide;
import com.fuelspot.PurchaseDetails;
import com.fuelspot.R;
import com.fuelspot.model.PurchaseItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.userUnit;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {
    private List<PurchaseItem> feedItemList;
    private Context mContext;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            Intent intent = new Intent(mContext, PurchaseDetails.class);
            intent.putExtra("PURCHASE_ID", feedItemList.get(position).getID());
            intent.putExtra("STATION_NAME", feedItemList.get(position).getStationName());
            intent.putExtra("STATION_ICON", feedItemList.get(position).getStationIcon());
            intent.putExtra("STATION_LOC", feedItemList.get(position).getStationLocation());
            intent.putExtra("PURCHASE_TIME", feedItemList.get(position).getPurchaseTime());
            intent.putExtra("FUEL_TYPE_1", feedItemList.get(position).getFuelType());
            intent.putExtra("FUEL_PRICE_1", feedItemList.get(position).getFuelPrice());
            intent.putExtra("FUEL_LITER_1", feedItemList.get(position).getFuelLiter());
            intent.putExtra("FUEL_TAX_1", feedItemList.get(position).getFuelTax());
            intent.putExtra("FUEL_TYPE_2", feedItemList.get(position).getFuelType2());
            intent.putExtra("FUEL_PRICE_2", feedItemList.get(position).getFuelPrice2());
            intent.putExtra("FUEL_LITER_2", feedItemList.get(position).getFuelLiter2());
            intent.putExtra("FUEL_TAX_2", feedItemList.get(position).getFuelTax2());
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
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_purchase, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final PurchaseItem feedItem = feedItemList.get(i);

        // FUEL TYPE 1
        switch (feedItem.getFuelType()) {
            case 0:
                Glide.with(mContext).load(R.drawable.gasoline).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.gasoline));
                break;
            case 1:
                Glide.with(mContext).load(R.drawable.diesel).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.diesel));
                break;
            case 2:
                Glide.with(mContext).load(R.drawable.lpg).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.lpg));
                break;
            case 3:
                Glide.with(mContext).load(R.drawable.electricity).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.electricity));
                break;
        }

        // AMOUNT 1
        viewHolder.amount1.setText(feedItem.getFuelLiter() + " " + userUnit);

        // UNIT 1
        String unitHolder = feedItem.getFuelPrice() + " " + currencySymbol;
        viewHolder.unitPrice1.setText(unitHolder);

        // PRICE 1
        int priceOne = (int) (feedItem.getFuelPrice() * feedItem.getFuelLiter());
        String priceHolder = priceOne + " " + currencySymbol;
        viewHolder.price1.setText(priceHolder);

        // If user didn't purchased second type of fuel just hide these textViews
        if (feedItem.getFuelType2() != -1) {
            // FUEL TYPE 2
            switch (feedItem.getFuelType2()) {
                case 0:
                    Glide.with(mContext).load(R.drawable.gasoline).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.gasoline));
                    break;
                case 1:
                    Glide.with(mContext).load(R.drawable.diesel).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.diesel));
                    break;
                case 2:
                    Glide.with(mContext).load(R.drawable.lpg).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.lpg));
                    break;
                case 3:
                    Glide.with(mContext).load(R.drawable.electricity).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.electricity));
                    break;
            }

            // AMOUNT 2
            viewHolder.amount2.setText(feedItem.getFuelLiter2() + " " + userUnit);

            // UNIT 2
            String unitHolder2 = feedItem.getFuelPrice2() + " " + currencySymbol;
            viewHolder.unitPrice2.setText(unitHolder2);

            // PRICE 2
            int priceTwo = (int) (feedItem.getFuelPrice2() * feedItem.getFuelLiter2());
            String priceHolder2 = priceTwo + " " + currencySymbol;
            viewHolder.price2.setText(priceHolder2);
        } else {
            viewHolder.type2Layout.setVisibility(View.GONE);
            viewHolder.amount2.setVisibility(View.GONE);
            viewHolder.unitPrice2.setVisibility(View.GONE);
            viewHolder.price2.setVisibility(View.GONE);
        }

        //TOTALTAX
        float tax1 = feedItem.getFuelPrice() * feedItem.getFuelLiter() * feedItem.getFuelTax();
        float tax2 = feedItem.getFuelPrice2() * feedItem.getFuelLiter2() * feedItem.getFuelTax2();
        String taxHolder = "VERGİ: " + String.format(Locale.getDefault(), "%.2f", tax1 + tax2) + " " + currencySymbol;
        viewHolder.totalTax.setText(taxHolder);

        //TotalPrice
        String totalPriceHolder = "TOPLAM: " + (int) feedItem.getTotalPrice() + " " + currencySymbol;
        viewHolder.totalPrice.setText(totalPriceHolder);

        // PurchaseTime
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = format.parse(feedItem.getPurchaseTime());
            viewHolder.purchaseTime.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Station Icon
        if (feedItem.getStationIcon() != null && feedItem.getStationIcon().length() > 0) {
            Glide.with(mContext).load(feedItem.getStationIcon()).into(viewHolder.stationLogo);
        }

        // Handle click event on image click
        viewHolder.backgroundClick.setOnClickListener(clickListener);
        viewHolder.backgroundClick.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout backgroundClick, type1Layout, type2Layout;
        TextView price1, unitPrice1, amount1, price2, unitPrice2, amount2, totalTax, totalPrice, type1Text, type2Text;
        ImageView stationLogo, type1, type2;
        RelativeTimeTextView purchaseTime;

        ViewHolder(View itemView) {
            super(itemView);
            backgroundClick = itemView.findViewById(R.id.single_purchase);
            stationLogo = itemView.findViewById(R.id.imageViewStationLogo);
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
            type1Text = itemView.findViewById(R.id.type1Text);
            type2Text = itemView.findViewById(R.id.fuelType2Text);
            type1Layout = itemView.findViewById(R.id.typeLayout);
            type2Layout = itemView.findViewById(R.id.typeLayout2);
        }
    }
}
