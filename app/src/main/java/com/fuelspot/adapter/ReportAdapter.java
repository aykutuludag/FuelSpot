package com.fuelspot.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuelspot.R;
import com.fuelspot.StationDetails;
import com.fuelspot.model.ReportItem;

import java.util.List;

import static com.fuelspot.MainActivity.currencySymbol;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private List<ReportItem> feedItemList;
    private Context mContext;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            ReportAdapter.ViewHolder holder = (ReportAdapter.ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            Intent intent = new Intent(mContext, StationDetails.class);
            intent.putExtra("STATION_ID", feedItemList.get(position).getStationID());
            mContext.startActivity(intent);
        }
    };

    public ReportAdapter(Context context, List<ReportItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_report, viewGroup, false);
        return new ReportAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ViewHolder viewHolder, int i) {
        ReportItem feedItem = feedItemList.get(i);

        String sIDDummy = mContext.getString(R.string.station_id) + " " + feedItem.getStationID();
        viewHolder.textViewStationID.setText(sIDDummy);

        viewHolder.textViewReportReason.setText(feedItem.getReportType());

        String dummyReward = feedItem.getReward() + " " + currencySymbol;
        viewHolder.textViewReward.setText(dummyReward);

        switch (feedItem.getIsReviewed()) {
            case 0:
                viewHolder.textViewStatus.setText(mContext.getString(R.string.report_close));
                break;
            case 1:
                viewHolder.textViewStatus.setText(mContext.getString(R.string.report_open));
                break;
        }

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
        TextView textViewStationID, textViewReportReason, textViewReward, textViewStatus;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.single_report);
            textViewStationID = itemView.findViewById(R.id.report_sID);
            textViewReportReason = itemView.findViewById(R.id.report_reason);
            textViewReward = itemView.findViewById(R.id.report_reward);
            textViewStatus = itemView.findViewById(R.id.report_status);
        }
    }
}