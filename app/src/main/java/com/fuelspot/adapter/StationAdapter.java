package com.fuelspot.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.fuelspot.R;
import com.fuelspot.StationDetails;
import com.fuelspot.model.StationItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.superuser.SuperMainActivity.isStationVerified;
import static com.fuelspot.superuser.SuperMainActivity.ownedDieselPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedElectricityPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedGasolinePrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedLPGPrice;
import static com.fuelspot.superuser.SuperMainActivity.ownedOtherFuels;
import static com.fuelspot.superuser.SuperMainActivity.superFacilities;
import static com.fuelspot.superuser.SuperMainActivity.superLastUpdate;
import static com.fuelspot.superuser.SuperMainActivity.superLicenseNo;
import static com.fuelspot.superuser.SuperMainActivity.superStationAddress;
import static com.fuelspot.superuser.SuperMainActivity.superStationCountry;
import static com.fuelspot.superuser.SuperMainActivity.superStationID;
import static com.fuelspot.superuser.SuperMainActivity.superStationLocation;
import static com.fuelspot.superuser.SuperMainActivity.superStationLogo;
import static com.fuelspot.superuser.SuperMainActivity.superStationName;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {

    private List<StationItem> feedItemList;
    private String whichScreen;
    private Context mContext;
    private DecimalFormat df = new DecimalFormat("#.##");
    private SharedPreferences prefs;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            switch (whichScreen) {
                case "NEARBY_STATIONS":
                    Intent intent = new Intent(mContext, StationDetails.class);
                    intent.putExtra("STATION_ID", feedItemList.get(position).getID());
                    intent.putExtra("STATION_NAME", feedItemList.get(position).getStationName());
                    intent.putExtra("STATION_VICINITY", feedItemList.get(position).getVicinity());
                    intent.putExtra("STATION_LOCATION", feedItemList.get(position).getLocation());
                    intent.putExtra("STATION_DISTANCE", feedItemList.get(position).getDistance());
                    intent.putExtra("STATION_LASTUPDATED", feedItemList.get(position).getLastUpdated());
                    intent.putExtra("STATION_GASOLINE", feedItemList.get(position).getGasolinePrice());
                    intent.putExtra("STATION_DIESEL", feedItemList.get(position).getDieselPrice());
                    intent.putExtra("STATION_LPG", feedItemList.get(position).getLpgPrice());
                    intent.putExtra("STATION_ELECTRIC", feedItemList.get(position).getElectricityPrice());
                    intent.putExtra("STATION_OTHER_FUELS", feedItemList.get(position).getOtherFuels());
                    intent.putExtra("STATION_ICON", feedItemList.get(position).getPhotoURL());
                    intent.putExtra("IS_VERIFIED", feedItemList.get(position).getIsVerified());
                    intent.putExtra("STATION_FACILITIES", feedItemList.get(position).getFacilities());
                    showAds(mContext, intent);
                    break;
                case "SUPERUSER_STATIONS":
                    changeSuperStation(feedItemList.get(position));
                    break;
            }
        }
    };

    public StationAdapter(Context context, List<StationItem> feedItemList, String whichPage) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.whichScreen = whichPage;

        prefs = mContext.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
    }

    private void changeSuperStation(StationItem item) {
        superStationID = item.getID();
        prefs.edit().putInt("SuperStationID", superStationID).apply();

        superStationName = item.getStationName();
        prefs.edit().putString("SuperStationName", superStationName).apply();

        superStationAddress = item.getVicinity();
        prefs.edit().putString("SuperStationAddress", superStationAddress).apply();

        superStationCountry = item.getCountryCode();
        prefs.edit().putString("SuperStationCountry", superStationCountry).apply();

        superStationLocation = item.getLocation();
        prefs.edit().putString("SuperStationLocation", superStationLocation).apply();

        superFacilities = item.getFacilities();
        prefs.edit().putString("SuperStationFacilities", superFacilities).apply();

        superStationLogo = item.getPhotoURL();
        prefs.edit().putString("SuperStationLogo", superStationLogo).apply();

        ownedGasolinePrice = item.getGasolinePrice();
        prefs.edit().putFloat("superGasolinePrice", ownedGasolinePrice).apply();

        ownedDieselPrice = item.getDieselPrice();
        prefs.edit().putFloat("superDieselPrice", ownedDieselPrice).apply();

        ownedLPGPrice = item.getLpgPrice();
        prefs.edit().putFloat("superLPGPrice", ownedLPGPrice).apply();

        ownedElectricityPrice = item.getElectricityPrice();
        prefs.edit().putFloat("superElectricityPrice", ownedElectricityPrice).apply();

        ownedOtherFuels = item.getOtherFuels();
        prefs.edit().putString("superOtherFuels", ownedOtherFuels).apply();

        superLicenseNo = item.getLicenseNo();
        prefs.edit().putString("SuperLicenseNo", superLicenseNo).apply();

        isStationVerified = item.getIsVerified();
        prefs.edit().putInt("isStationVerified", isStationVerified).apply();

        superLastUpdate = item.getLastUpdated();
        prefs.edit().putString("SuperLastUpdate", superLastUpdate).apply();

        notifyDataSetChanged();
        Toast.makeText(mContext, "İSTASYON SEÇİLDİ: " + superStationName, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_station, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final StationItem feedItem = feedItemList.get(i);

        //Station Icon
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_station)
                .error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(mContext).load(feedItem.getPhotoURL()).apply(options).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                feedItem.setStationLogoDrawable(resource);
                return false;
            }
        }).into(viewHolder.stationPic);

        // Setting stationName
        viewHolder.stationName.setText(feedItem.getStationName());

        // Setting prices
        String gasolineHolder;
        if (feedItem.getGasolinePrice() == 0) {
            gasolineHolder = "-";
        } else {
            gasolineHolder = df.format(feedItem.getGasolinePrice()) + " " + currencySymbol;
        }
        viewHolder.gasolinePrice.setText(gasolineHolder);

        String dieselHolder;
        if (feedItem.getDieselPrice() == 0) {
            dieselHolder = "-";
        } else {
            dieselHolder = df.format(feedItem.getDieselPrice()) + " " + currencySymbol;
        }
        viewHolder.dieselPrice.setText(dieselHolder);

        String lpgHolder;
        if (feedItem.getLpgPrice() == 0) {
            lpgHolder = "-";
        } else {
            lpgHolder = df.format(feedItem.getLpgPrice()) + " " + currencySymbol;
        }
        viewHolder.lpgPrice.setText(lpgHolder);

        String elecHolder;
        if (feedItem.getElectricityPrice() == 0) {
            elecHolder = "-";
        } else {
            elecHolder = df.format(feedItem.getElectricityPrice()) + " " + currencySymbol;
        }
        viewHolder.electricityPrice.setText(elecHolder);

        //Distance
        String distance;
        if (feedItem.getDistance() >= 1500) {
            float km = feedItem.getDistance() / 1000f;
            distance = df.format(km) + " KM";
        } else {
            distance = feedItem.getDistance() + " m";
        }
        viewHolder.distance.setText(distance);

        //Last updated
        if (feedItem.getLastUpdated() != null) {
            SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
            try {
                Date date = format.parse(feedItem.getLastUpdated());
                viewHolder.lastUpdated.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (whichScreen.equals("SUPERUSER_STATIONS")) {
            if (feedItem.getID() == superStationID) {
                viewHolder.background.setBackgroundResource(R.drawable.white_box_stroke);
            } else {
                viewHolder.background.setBackground(null);
            }
        }

        // Handle click event on image click
        viewHolder.background.setOnClickListener(clickListener);
        viewHolder.background.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView stationName, gasolinePrice, dieselPrice, lpgPrice, electricityPrice, distance;
        RelativeTimeTextView lastUpdated;
        ImageView stationPic;
        RelativeLayout background;

        ViewHolder(View itemView) {
            super(itemView);
            background = itemView.findViewById(R.id.single_station);
            stationName = itemView.findViewById(R.id.station_name);
            gasolinePrice = itemView.findViewById(R.id.priceGasoline);
            dieselPrice = itemView.findViewById(R.id.priceDiesel);
            lpgPrice = itemView.findViewById(R.id.priceLPG);
            electricityPrice = itemView.findViewById(R.id.priceElectricity);
            lastUpdated = itemView.findViewById(R.id.stationLastUpdate);
            stationPic = itemView.findViewById(R.id.station_photo);
            distance = itemView.findViewById(R.id.distance_ofStation);
        }
    }
}
