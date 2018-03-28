package org.uusoftware.fuelify.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

import org.uusoftware.fuelify.R;
import org.uusoftware.fuelify.model.StationItem;

import java.util.Date;
import java.util.List;


public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    SharedPreferences prefs;
    private List<StationItem> feedItemList;
    private Context mContext;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            int eventID = feedItemList.get(position).getID();

           /* Intent intent = new Intent(mContext, SingleEvent.class);
            intent.putExtra("EVENT_ID", eventID);
            mContext.startActivity(intent);*/
        }
    };

    public StationAdapter(Context context, List<StationItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_stations, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        StationItem feedItem = feedItemList.get(i);

        // Setting stationName
        viewHolder.stationName.setText(feedItem.getStationName());

        // Setting prices
        viewHolder.gasolinePrice.setText(String.valueOf(feedItem.getGasolinePrice()));
        viewHolder.dieselPrice.setText(String.valueOf(feedItem.getDieselPrice()));
        viewHolder.lpgPrice.setText(String.valueOf(feedItem.getLpgPrice()));
        viewHolder.electricityPrice.setText(String.valueOf(feedItem.getElectricityPrice()));

        //Last updated
        Date date = new Date(feedItem.getLastUpdated());
        viewHolder.lastUpdated.setReferenceTime(date.getTime());

        //Header pic
       /* Picasso.with(mContext).load(feedItem.getPhotoURL()).error(R.drawable.header_station).placeholder(R.drawable.header_station)
                .into(viewHolder.stationPic);*/

        // Handle click event on image click
        viewHolder.background.setOnClickListener(clickListener);
        viewHolder.background.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView stationName, gasolinePrice, dieselPrice, lpgPrice, electricityPrice;
        RelativeTimeTextView lastUpdated;
        ImageView stationPic;
        RelativeLayout background;

        ViewHolder(View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.station_name);
            gasolinePrice = itemView.findViewById(R.id.gasoline_price);
            dieselPrice = itemView.findViewById(R.id.diesel_price);
            lpgPrice = itemView.findViewById(R.id.lpg_price);
            electricityPrice = itemView.findViewById(R.id.electricity_price);
            lastUpdated = itemView.findViewById(R.id.lastUpdated);
            stationPic = itemView.findViewById(R.id.station_photo);
            background = itemView.findViewById(R.id.single_station);
        }
    }
}
