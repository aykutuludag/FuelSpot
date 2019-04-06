package com.fuelspot.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.R;
import com.fuelspot.model.StationItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import de.hdodenhof.circleimageview.CircleImageView;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private Context mContext;
    private RequestOptions options;

    public MarkerAdapter(Context ctx) {
        mContext = ctx;
        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_station)
                .error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.popup_marker, null);
        final StationItem infoWindowData = (StationItem) marker.getTag();

        if (infoWindowData != null) {
            TextView sName = view.findViewById(R.id.station_name);
            CircleImageView sLogo = view.findViewById(R.id.station_logo);
            TextView priceOne = view.findViewById(R.id.priceGasoline);
            TextView priceTwo = view.findViewById(R.id.priceDiesel);
            TextView priceThree = view.findViewById(R.id.priceLPG);

            sName.setText(infoWindowData.getStationName());

            Glide.with(mContext).load(infoWindowData.getPhotoURL()).apply(options).into(sLogo);

            if (infoWindowData.getGasolinePrice() != 0) {
                priceOne.setText(String.valueOf(infoWindowData.getGasolinePrice()));
            } else {
                priceOne.setText("-");
            }

            if (infoWindowData.getDieselPrice() != 0) {
                priceTwo.setText(String.valueOf(infoWindowData.getDieselPrice()));
            } else {
                priceTwo.setText("-");
            }

            if (infoWindowData.getLpgPrice() != 0) {
                priceThree.setText(String.valueOf(infoWindowData.getLpgPrice()));
            } else {
                priceThree.setText("-");
            }
        }
        return view;
    }
}
