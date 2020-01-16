package com.fuelspot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fuelspot.R;
import com.fuelspot.model.BankingItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.doubleRangeProductCode;
import static com.fuelspot.MainActivity.premiumProductCode;
import static com.fuelspot.MainActivity.shortTimeFormat;

public class BankingAdapter extends RecyclerView.Adapter<BankingAdapter.ViewHolder> {
    private List<BankingItem> feedItemList;
    private Context mContext;

    public BankingAdapter(Context context, List<BankingItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public BankingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_banking, viewGroup, false);
        return new BankingAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BankingAdapter.ViewHolder viewHolder, int i) {
        BankingItem feedItem = feedItemList.get(i);

        switch (feedItem.getType()) {
            case "reward":
                viewHolder.textViewType.setText(mContext.getString(R.string.reward));
                break;
            case "bonus":
                viewHolder.textViewType.setText(mContext.getString(R.string.bonus));
                break;
            case "purchase":
                if (feedItem.getNotes().equals(doubleRangeProductCode)) {
                    viewHolder.textViewType.setText(mContext.getString(R.string.double_range));
                } else if (feedItem.getNotes().equals(premiumProductCode)) {
                    viewHolder.textViewType.setText(mContext.getString(R.string.premium_version));
                } else {
                    viewHolder.textViewType.setText(mContext.getString(R.string.purchase));
                }
                break;
        }

        String amount = feedItem.getAmount() + " FP";
        viewHolder.textViewAmount.setText(amount);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
            Date date = sdf.parse(feedItem.getTransactionTime());

            SimpleDateFormat sdf2 = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());
            viewHolder.textViewDate.setText(sdf2.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewType, textViewAmount, textViewDate;

        ViewHolder(View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.banking_type);
            textViewAmount = itemView.findViewById(R.id.banking_amout);
            textViewDate = itemView.findViewById(R.id.banking_date);
        }
    }
}