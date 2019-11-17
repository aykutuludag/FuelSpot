package com.fuelspot.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.fuelspot.R;
import com.fuelspot.model.StationItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private DecimalFormat df = new DecimalFormat("#.##");
    private Context mContext;

    public MarkerAdapter(Context ctx) {
        mContext = ctx;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.popup_marker, null);
        final StationItem infoWindowData = (StationItem) marker.getTag();

        if (infoWindowData != null) {
            TextView sName = view.findViewById(R.id.station_name);
            final CircleImageView sLogo = view.findViewById(R.id.station_logo);
            TextView priceOne = view.findViewById(R.id.priceGasoline);
            TextView priceTwo = view.findViewById(R.id.priceDiesel);
            TextView priceThree = view.findViewById(R.id.priceLPG);
            TextView distanceText = view.findViewById(R.id.textDistance);
            TextView stationIDText = view.findViewById(R.id.textViewStationID);

            sName.setText(infoWindowData.getStationName());
            stationIDText.setText(String.valueOf(infoWindowData.getID()));

            if (infoWindowData.getStationLogoDrawable() != null) {
                sLogo.setImageDrawable(infoWindowData.getStationLogoDrawable());
            } else {
                sLogo.setBackgroundResource(R.drawable.default_station);
            }

            if (infoWindowData.getGasolinePrice() != 0) {
                priceOne.setText(String.valueOf(df.format(infoWindowData.getGasolinePrice())));
            } else {
                priceOne.setText("-");
            }

            if (infoWindowData.getDieselPrice() != 0) {
                priceTwo.setText(String.valueOf(df.format(infoWindowData.getDieselPrice())));
            } else {
                priceTwo.setText("-");
            }

            if (infoWindowData.getLpgPrice() != 0) {
                priceThree.setText(String.valueOf(df.format(infoWindowData.getLpgPrice())));
            } else {
                priceThree.setText("-");
            }

            String distance;
            if (infoWindowData.getDistance() >= 1500) {
                float km = infoWindowData.getDistance() / 1000f;
                distance = df.format(km) + " KM " + mContext.getString(R.string.away);
            } else {
                distance = infoWindowData.getDistance() + " m " + mContext.getString(R.string.away);
            }
            distanceText.setText(distance);

        }
        return view;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        return null;
    }
}
