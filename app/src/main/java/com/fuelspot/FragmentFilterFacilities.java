package com.fuelspot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static com.fuelspot.FragmentFilterDistributors.filterStation1;
import static com.fuelspot.FragmentFilterDistributors.filterStation10;
import static com.fuelspot.FragmentFilterDistributors.filterStation11;
import static com.fuelspot.FragmentFilterDistributors.filterStation12;
import static com.fuelspot.FragmentFilterDistributors.filterStation2;
import static com.fuelspot.FragmentFilterDistributors.filterStation3;
import static com.fuelspot.FragmentFilterDistributors.filterStation4;
import static com.fuelspot.FragmentFilterDistributors.filterStation5;
import static com.fuelspot.FragmentFilterDistributors.filterStation6;
import static com.fuelspot.FragmentFilterDistributors.filterStation7;
import static com.fuelspot.FragmentFilterDistributors.filterStation8;
import static com.fuelspot.FragmentFilterDistributors.filterStation9;
import static com.fuelspot.MainActivity.fragmentsUser;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.superuser.SuperMainActivity.fragmentsSuperUser;

public class FragmentFilterFacilities extends Fragment {

    static boolean filterByWC, filterByMarket, filterByCarWash, filterByTireStore, filterByMechanic, filterByRestaurant, filterByParkSpot, filterByATM, filterByMotel, filterByCoffeeShop, filterByMosque;
    private FragmentFilter fragmentFilter;
    private FragmentStations fragmentStations;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_filter_facilities, container, false);

        fragmentFilter = (FragmentFilter) getActivity().getSupportFragmentManager().findFragmentByTag("FragmentFilter");
        if (isSuperUser) {
            fragmentStations = (FragmentStations) fragmentsSuperUser.get(2);
        } else {
            fragmentStations = (FragmentStations) fragmentsUser.get(0);
        }

        CheckBox checkBox1 = view.findViewById(R.id.checkBox2);
        checkBox1.setChecked(filterByWC);
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByWC = isChecked;
            }
        });

        CheckBox checkBox2 = view.findViewById(R.id.checkBox3);
        checkBox2.setChecked(filterByMarket);
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByMarket = isChecked;
            }
        });

        CheckBox checkBox3 = view.findViewById(R.id.checkBox4);
        checkBox3.setChecked(filterByCarWash);
        checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByCarWash = isChecked;
            }
        });

        CheckBox checkBox4 = view.findViewById(R.id.checkBox5);
        checkBox4.setChecked(filterByTireStore);
        checkBox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByTireStore = isChecked;
            }
        });

        CheckBox checkBox5 = view.findViewById(R.id.checkBox6);
        checkBox5.setChecked(filterByMechanic);
        checkBox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByMechanic = isChecked;
            }
        });

        CheckBox checkBox6 = view.findViewById(R.id.checkBox7);
        checkBox6.setChecked(filterByRestaurant);
        checkBox6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByRestaurant = isChecked;
            }
        });

        CheckBox checkBox7 = view.findViewById(R.id.checkBox8);
        checkBox7.setChecked(filterByParkSpot);
        checkBox7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByParkSpot = isChecked;
            }
        });

        CheckBox checkBox8 = view.findViewById(R.id.checkBox9);
        checkBox8.setChecked(filterByATM);
        checkBox8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByATM = isChecked;
            }
        });

        CheckBox checkBox9 = view.findViewById(R.id.checkBox10);
        checkBox9.setChecked(filterByMotel);
        checkBox9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByMotel = isChecked;
            }
        });

        CheckBox checkBox10 = view.findViewById(R.id.checkBox11);
        checkBox10.setChecked(filterByCoffeeShop);
        checkBox10.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByCoffeeShop = isChecked;
            }
        });

        CheckBox checkBox11 = view.findViewById(R.id.checkBox12);
        checkBox11.setChecked(filterByMosque);
        checkBox11.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterByMosque = isChecked;
            }
        });


        Button filterButton = view.findViewById(R.id.button8);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterStation1 = false;
                filterStation2 = false;
                filterStation3 = false;
                filterStation4 = false;
                filterStation5 = false;
                filterStation6 = false;
                filterStation7 = false;
                filterStation8 = false;
                filterStation9 = false;
                filterStation10 = false;
                filterStation11 = false;
                filterStation12 = false;

                fragmentStations.filterStationsByFacilities();
                fragmentFilter.getDialog().dismiss();
            }
        });

        return view;
    }
}
