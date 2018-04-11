package org.uusoftware.fuelify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class FragmentHome extends Fragment {

    ImageView vehicleCard, stationsCard, userCard, latestNews;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplicationContext()).getDefaultTracker();
        t.setScreenName("Home");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        vehicleCard = rootView.findViewById(R.id.vehicle_card);
        vehicleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_vehicle);
                Fragment fragment = new FragmentVehicle();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Vehicle").commit();
            }
        });

        stationsCard = rootView.findViewById(R.id.stations_card);
        stationsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_stations);
                Fragment fragment = new FragmentStations();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Stations").commit();
            }
        });

        userCard = rootView.findViewById(R.id.user_card);
        userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_profile);
                Fragment fragment = new FragmentProfile();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "Profile").commit();
            }
        });

        latestNews = rootView.findViewById(R.id.latest_news);
        latestNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_news);
                Fragment fragment = new FragmentNews();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment, "News").commit();
            }
        });

        return rootView;
    }
}