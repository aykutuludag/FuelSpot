package com.fuelspot;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import static com.fuelspot.MainActivity.isGlobalNews;

public class FragmentSettings extends Fragment {

    Switch globalNewsSwitch;
    SharedPreferences prefs;

    public static FragmentSettings newInstance() {

        Bundle args = new Bundle();

        FragmentSettings fragment = new FragmentSettings();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("SETTINGS");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        globalNewsSwitch = rootView.findViewById(R.id.switch1);
        globalNewsSwitch.setChecked(isGlobalNews);
        globalNewsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isGlobalNews = isChecked;
                prefs.edit().putBoolean("isGlobalNews", isGlobalNews).apply();
            }
        });

        return rootView;
    }
}
