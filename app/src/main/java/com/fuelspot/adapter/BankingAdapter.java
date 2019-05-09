package com.fuelspot.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuelspot.R;
import com.fuelspot.model.BankingItem;

import java.util.List;

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
            case "purchase":
                viewHolder.textViewType.setText(mContext.getString(R.string.purchase));
                break;
            case "bonus":
                viewHolder.textViewType.setText(mContext.getString(R.string.bonus));
                break;
        }


        String amount = feedItem.getAmount() + " FS";
        viewHolder.textViewAmount.setText(amount);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        TextView textViewType, textViewAmount;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.single_report);
            textViewType = itemView.findViewById(R.id.banking_type);
            textViewAmount = itemView.findViewById(R.id.banking_amout);
        }
    }
}