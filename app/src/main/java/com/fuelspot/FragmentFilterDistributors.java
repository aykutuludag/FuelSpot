package com.fuelspot;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import static com.fuelspot.FragmentFilterFacilities.filterByATM;
import static com.fuelspot.FragmentFilterFacilities.filterByCarWash;
import static com.fuelspot.FragmentFilterFacilities.filterByCoffeeShop;
import static com.fuelspot.FragmentFilterFacilities.filterByMarket;
import static com.fuelspot.FragmentFilterFacilities.filterByMechanic;
import static com.fuelspot.FragmentFilterFacilities.filterByMosque;
import static com.fuelspot.FragmentFilterFacilities.filterByMotel;
import static com.fuelspot.FragmentFilterFacilities.filterByParkSpot;
import static com.fuelspot.FragmentFilterFacilities.filterByRestaurant;
import static com.fuelspot.FragmentFilterFacilities.filterByTireStore;
import static com.fuelspot.FragmentFilterFacilities.filterByWC;
import static com.fuelspot.MainActivity.fragmentsUser;
import static com.fuelspot.MainActivity.fullStationList;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.superuser.SuperMainActivity.fragmentsSuperUser;

public class FragmentFilterDistributors extends Fragment {

    static boolean filterStation1, filterStation2, filterStation3, filterStation4, filterStation5, filterStation6, filterStation7, filterStation8, filterStation9, filterStation10, filterStation11, filterStation12;
    static List<String> markalar = new ArrayList<>();
    private static List<Drawable> logolar = new ArrayList<>();
    private FragmentFilter fragmentFilter;
    private FragmentStations fragmentStations;
    private List<CheckBox> checkBoxes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_filter_distributors, container, false);

        markalar.clear();
        logolar.clear();
        checkBoxes.clear();

        fragmentFilter = (FragmentFilter) getActivity().getSupportFragmentManager().findFragmentByTag("FragmentFilter");
        if (isSuperUser) {
            fragmentStations = (FragmentStations) fragmentsSuperUser.get(2);
        } else {
            fragmentStations = (FragmentStations) fragmentsUser.get(0);
        }

        CheckBox checkBox1 = view.findViewById(R.id.checkBox1);
        checkBox1.setChecked(filterStation1);
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation1 = isChecked;
            }
        });


        CheckBox checkBox2 = view.findViewById(R.id.checkBox2);
        checkBox2.setChecked(filterStation2);
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation2 = isChecked;
            }
        });


        CheckBox checkBox3 = view.findViewById(R.id.checkBox3);
        checkBox3.setChecked(filterStation3);
        checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation3 = isChecked;
            }
        });


        CheckBox checkBox4 = view.findViewById(R.id.checkBox4);
        checkBox4.setChecked(filterStation4);
        checkBox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation4 = isChecked;
            }
        });


        CheckBox checkBox5 = view.findViewById(R.id.checkBox5);
        checkBox5.setChecked(filterStation5);
        checkBox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation5 = isChecked;
            }
        });


        CheckBox checkBox6 = view.findViewById(R.id.checkBox6);
        checkBox6.setChecked(filterStation6);
        checkBox6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation6 = isChecked;
            }
        });


        CheckBox checkBox7 = view.findViewById(R.id.checkBox7);
        checkBox7.setChecked(filterStation7);
        checkBox7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation7 = isChecked;
            }
        });


        CheckBox checkBox8 = view.findViewById(R.id.checkBox8);
        checkBox8.setChecked(filterStation8);
        checkBox8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation8 = isChecked;
            }
        });

        CheckBox checkBox9 = view.findViewById(R.id.checkBox9);
        checkBox9.setChecked(filterStation9);
        checkBox9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation9 = isChecked;
            }
        });

        CheckBox checkBox10 = view.findViewById(R.id.checkBox10);
        checkBox10.setChecked(filterStation10);
        checkBox10.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation10 = isChecked;
            }
        });

        CheckBox checkBox11 = view.findViewById(R.id.checkBox11);
        checkBox11.setChecked(filterStation11);
        checkBox11.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation11 = isChecked;
            }
        });

        CheckBox checkBox12 = view.findViewById(R.id.checkBox12);
        checkBox12.setChecked(filterStation12);
        checkBox12.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterStation12 = isChecked;
            }
        });

        checkBoxes.add(checkBox1);
        checkBoxes.add(checkBox2);
        checkBoxes.add(checkBox3);
        checkBoxes.add(checkBox4);
        checkBoxes.add(checkBox5);
        checkBoxes.add(checkBox6);
        checkBoxes.add(checkBox7);
        checkBoxes.add(checkBox8);
        checkBoxes.add(checkBox9);
        checkBoxes.add(checkBox10);
        checkBoxes.add(checkBox11);
        checkBoxes.add(checkBox12);

        for (int i = 0; i < fullStationList.size(); i++) {
            if (!markalar.contains(fullStationList.get(i).getStationName())) {
                markalar.add(fullStationList.get(i).getStationName());
                logolar.add(fullStationList.get(i).getStationLogoDrawable());
            }
        }

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (i < markalar.size()) {
                checkBoxes.get(i).setVisibility(View.VISIBLE);
                Drawable dummy = checkBoxes.get(i).getCompoundDrawables()[3];
                checkBoxes.get(i).setCompoundDrawables(null, logolar.get(i), null, dummy);
                checkBoxes.get(i).setText(markalar.get(i));
            } else {
                checkBoxes.get(i).setVisibility(View.GONE);
            }
        }

        Button filterButton = view.findViewById(R.id.buttonFilterDistributors);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByWC = false;
                filterByMarket = false;
                filterByCarWash = false;
                filterByTireStore = false;
                filterByMechanic = false;
                filterByRestaurant = false;
                filterByParkSpot = false;
                filterByATM = false;
                filterByMotel = false;
                filterByCoffeeShop = false;
                filterByMosque = false;

                fragmentStations.filterStationsByDistributors();
                fragmentFilter.getDialog().dismiss();
            }
        });

        return view;
    }
}
