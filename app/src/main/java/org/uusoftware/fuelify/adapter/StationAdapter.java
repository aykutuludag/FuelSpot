package org.uusoftware.fuelify.adapter;

import android.app.Activity;
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

import org.uusoftware.fuelify.R;
import org.uusoftware.fuelify.StationDetails;
import org.uusoftware.fuelify.model.StationItem;

import java.util.Date;
import java.util.List;

import static org.uusoftware.fuelify.AddFuel.chosenStationID;
import static org.uusoftware.fuelify.AddFuel.chosenStationName;
import static org.uusoftware.fuelify.AddFuel.gasolinePrice;
import static org.uusoftware.fuelify.AddFuel.dieselPrice;
import static org.uusoftware.fuelify.AddFuel.LPGPrice;
import static org.uusoftware.fuelify.AddFuel.electricityPrice;
import static org.uusoftware.fuelify.ChooseStation.isAddingFuel;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    private List<StationItem> feedItemList;
    private Context mContext;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            if (isAddingFuel) {
                System.out.println("İSTASYON SEÇİMİ");
                chosenStationID = String.valueOf(feedItemList.get(position).getID());
                chosenStationName = feedItemList.get(position).getStationName();
                gasolinePrice = feedItemList.get(position).getGasolinePrice();
                dieselPrice = feedItemList.get(position).getDieselPrice();
                LPGPrice = feedItemList.get(position).getLpgPrice();
                electricityPrice = feedItemList.get(position).getElectricityPrice();
                ((Activity) mContext).finish();
            } else {
                System.out.println("İSTASYON DETAYI");
                Intent intent = new Intent(mContext, StationDetails.class);
                intent.putExtra("STATION_NAME", feedItemList.get(position).getStationName());
                intent.putExtra("STATION_VICINITY", feedItemList.get(position).getVicinity());
                intent.putExtra("STATION_LOCATION", feedItemList.get(position).getLocation());
                intent.putExtra("STATION_DISTANCE", feedItemList.get(position).getDistance());
                intent.putExtra("STATION_LASTUPDATED", feedItemList.get(position).getLastUpdated());
                intent.putExtra("STATION_GASOLINE", feedItemList.get(position).getGasolinePrice());
                intent.putExtra("STATION_DIESEL", feedItemList.get(position).getDieselPrice());
                intent.putExtra("STATION_LPG", feedItemList.get(position).getLpgPrice());
                intent.putExtra("STATION_ELECTRIC", feedItemList.get(position).getElectricityPrice());
                intent.putExtra("STATION_ID", feedItemList.get(position).getID());
                mContext.startActivity(intent);
            }
        }
    };

    public StationAdapter(Context context, List<StationItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_stations, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final StationItem feedItem = feedItemList.get(i);

        // Setting stationName
        viewHolder.stationName.setText(feedItem.getStationName());
        viewHolder.vicinity.setText(feedItem.getVicinity());

        // Setting prices
        viewHolder.gasolinePrice.setText(String.valueOf(feedItem.getGasolinePrice()));
        viewHolder.dieselPrice.setText(String.valueOf(feedItem.getDieselPrice()));
        viewHolder.lpgPrice.setText(String.valueOf(feedItem.getLpgPrice()));
        viewHolder.electricityPrice.setText(String.valueOf(feedItem.getElectricityPrice()));

        //Distance
        String distance = (int) feedItem.getDistance() + " m";
        viewHolder.distance.setText(distance);

        //Last updated
        Date date = new Date(feedItem.getLastUpdated());
        viewHolder.lastUpdated.setReferenceTime(date.getTime());

        //Station Icon
        Picasso.with(mContext).load(feedItem.getPhotoURL()).error(R.drawable.header_station).placeholder(R.drawable.header_station)
                .into(viewHolder.stationPic);

        // Handle click event on image click
        viewHolder.background.setOnClickListener(clickListener);
        viewHolder.background.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView stationName, vicinity, gasolinePrice, dieselPrice, lpgPrice, electricityPrice, distance;
        RelativeTimeTextView lastUpdated;
        ImageView stationPic;
        RelativeLayout background;

        ViewHolder(View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.station_name);
            vicinity = itemView.findViewById(R.id.station_vicinity);
            gasolinePrice = itemView.findViewById(R.id.gasoline_price);
            dieselPrice = itemView.findViewById(R.id.diesel_price);
            lpgPrice = itemView.findViewById(R.id.lpg_price);
            electricityPrice = itemView.findViewById(R.id.electricity_price);
            lastUpdated = itemView.findViewById(R.id.lastUpdated);
            stationPic = itemView.findViewById(R.id.station_photo);
            background = itemView.findViewById(R.id.single_station);
            distance = itemView.findViewById(R.id.distance_ofStation);
        }
    }
}
