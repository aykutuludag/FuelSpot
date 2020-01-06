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
import com.fuelspot.PurchaseDetails;
import com.fuelspot.R;
import com.fuelspot.model.PurchaseItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.yqritc.scalableimageview.ScalableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.USTimeFormat;
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
            intent.putExtra("PLATE_NO", feedItemList.get(position).getPlateNo());
            intent.putExtra("STATION_ID", feedItemList.get(position).getStationID());
            intent.putExtra("FUEL_TYPE_1", feedItemList.get(position).getFuelType());
            intent.putExtra("FUEL_PRICE_1", feedItemList.get(position).getFuelPrice());
            intent.putExtra("FUEL_LITER_1", feedItemList.get(position).getFuelLiter());
            intent.putExtra("FUEL_TAX_1", feedItemList.get(position).getFuelTax());
            intent.putExtra("SUB_TOTAL", feedItemList.get(position).getSubTotal());
            intent.putExtra("FUEL_TYPE_2", feedItemList.get(position).getFuelType2());
            intent.putExtra("FUEL_PRICE_2", feedItemList.get(position).getFuelPrice2());
            intent.putExtra("FUEL_LITER_2", feedItemList.get(position).getFuelLiter2());
            intent.putExtra("FUEL_TAX_2", feedItemList.get(position).getFuelTax2());
            intent.putExtra("SUB_TOTAL_2", feedItemList.get(position).getSubTotal2());
            intent.putExtra("TOTAL_PRICE", feedItemList.get(position).getTotalPrice());
            intent.putExtra("BONUS", feedItemList.get(position).getBonus());
            intent.putExtra("BILL_PHOTO", feedItemList.get(position).getBillPhoto());
            intent.putExtra("IS_PURCHASE_VERIFIED", feedItemList.get(position).getIsVerified());
            intent.putExtra("PURCHASE_TIME", feedItemList.get(position).getPurchaseTime());
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
                Glide.with(mContext).load(R.drawable.fuel_gasoline).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.gasoline));
                break;
            case 1:
                Glide.with(mContext).load(R.drawable.fuel_diesel).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.diesel));
                break;
            case 2:
                Glide.with(mContext).load(R.drawable.fuel_lpg).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.lpg));
                break;
            case 3:
                Glide.with(mContext).load(R.drawable.fuel_electricity).into(viewHolder.type1);
                viewHolder.type1Text.setText(mContext.getString(R.string.electricity));
                break;
        }

        // AMOUNT 1
        String kacLitre = feedItem.getFuelLiter() + " " + userUnit;
        viewHolder.amount1.setText(kacLitre);

        // PRICE 1
        String priceHolder = String.format(Locale.getDefault(), "%.2f", feedItem.getSubTotal()) + " " + currencySymbol;
        viewHolder.price1.setText(priceHolder);

        // If user didn't purchased second type of fuel just hide these textViews
        if (feedItem.getFuelType2() != -1) {
            // FUEL TYPE 2
            switch (feedItem.getFuelType2()) {
                case 0:
                    Glide.with(mContext).load(R.drawable.fuel_gasoline).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.gasoline));
                    break;
                case 1:
                    Glide.with(mContext).load(R.drawable.fuel_diesel).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.diesel));
                    break;
                case 2:
                    Glide.with(mContext).load(R.drawable.fuel_lpg).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.lpg));
                    break;
                case 3:
                    Glide.with(mContext).load(R.drawable.fuel_electricity).into(viewHolder.type2);
                    viewHolder.type2Text.setText(mContext.getString(R.string.electricity));
                    break;
            }

            // AMOUNT 2
            String kacLitre2 = String.format(Locale.getDefault(), "%.1f", feedItem.getFuelLiter2()) + " " + userUnit;
            viewHolder.amount2.setText(kacLitre2);

            // PRICE 2
            String priceHolder2 = String.format(Locale.getDefault(), "%.2f", feedItem.getSubTotal2()) + " " + currencySymbol;
            viewHolder.price2.setText(priceHolder2);
        } else {
            viewHolder.type2Layout.setVisibility(View.GONE);
            viewHolder.amount2.setVisibility(View.GONE);
            viewHolder.price2.setVisibility(View.GONE);
        }

        // Bonus
        String bonusHolder = mContext.getString(R.string.grand_bonus) + " " + String.format(Locale.getDefault(), "%.2f", feedItem.getBonus()) + " FP";
        viewHolder.bonusText.setText(bonusHolder);

        //TotalPrice
        String totalPriceHolder = mContext.getString(R.string.total) + " " + String.format(Locale.getDefault(), "%.2f", feedItem.getTotalPrice()) + " " + currencySymbol;
        viewHolder.totalPrice.setText(totalPriceHolder);

        // PurchaseTime
        try {
            SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
            Date date = format.parse(feedItem.getPurchaseTime());
            viewHolder.purchaseTime.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Station Icon
        if (feedItem.getStationIcon() != null && feedItem.getStationIcon().length() > 0) {
            Glide.with(mContext).load(feedItem.getStationIcon()).into(viewHolder.stationLogo);
        }

        // Station Name
        viewHolder.stationName.setText(feedItem.getStationName());

        // KM
        String mesafe = feedItem.getKilometer() + " KM";
        viewHolder.textviewKM.setText(mesafe);
        int kmDiff;
        if (feedItemList.size() >= 2) {
            if (i != 0) {
                kmDiff = feedItemList.get(i - 1).getKilometer() - feedItem.getKilometer();
            } else {
                kmDiff = 0;
            }
        } else {
            kmDiff = 0;
        }

        if (kmDiff != 0) {
            String trip = mContext.getString(R.string.road_taken) + " " + kmDiff + " KM";
            viewHolder.textViewTrip.setText(trip);
        } else {
            String noTrip = mContext.getString(R.string.road_taken) + " " + "- KM";
            viewHolder.textViewTrip.setText(noTrip);
        }

        if (feedItem.getBillPhoto() != null && feedItem.getBillPhoto().length() > 0) {
            viewHolder.imageViewBillPhoto.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(feedItem.getBillPhoto()).into(viewHolder.imageViewBillPhoto);
        } else {
            viewHolder.imageViewBillPhoto.setVisibility(View.GONE);
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

        RelativeLayout backgroundClick, type2Layout;
        TextView price1, stationName, amount1, price2, amount2, totalPrice, type1Text, type2Text, bonusText, textviewKM, textViewTrip;
        ImageView type1, type2;
        CircleImageView stationLogo;
        RelativeTimeTextView purchaseTime;
        ScalableImageView imageViewBillPhoto;

        ViewHolder(View itemView) {
            super(itemView);
            backgroundClick = itemView.findViewById(R.id.single_purchase);
            stationLogo = itemView.findViewById(R.id.imageViewStationLogo);
            stationName = itemView.findViewById(R.id.station_name);

            type1 = itemView.findViewById(R.id.type1);
            type1Text = itemView.findViewById(R.id.type1Text);
            amount1 = itemView.findViewById(R.id.amount1);
            price1 = itemView.findViewById(R.id.price1);

            type2Layout = itemView.findViewById(R.id.typeLayout2);
            type2 = itemView.findViewById(R.id.type2);
            type2Text = itemView.findViewById(R.id.type2Text);
            amount2 = itemView.findViewById(R.id.amount2);
            price2 = itemView.findViewById(R.id.price2);

            bonusText = itemView.findViewById(R.id.bonus);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            purchaseTime = itemView.findViewById(R.id.purchaseTime);

            textviewKM = itemView.findViewById(R.id.textViewKilometer);
            textViewTrip = itemView.findViewById(R.id.textViewkmChange);
            imageViewBillPhoto = itemView.findViewById(R.id.imageViewBillPic);
        }
    }
}
