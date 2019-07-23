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

public class FragmentFilterDistributors extends Fragment {

    static boolean filterStation1, filterStation2, filterStation3, filterStation4, filterStation5, filterStation6;
    static List<String> markalar = new ArrayList<>();
    private static List<Drawable> logolar = new ArrayList<>();
    private FragmentFilter fragmentFilter;
    private FragmentStations fragmentStations;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_filter_distributors, container, false);

        markalar.clear();
        logolar.clear();

        fragmentFilter = (FragmentFilter) getActivity().getSupportFragmentManager().findFragmentByTag("FragmentFilter");
        if (isSuperUser) {
            fragmentStations = (FragmentStations) fragmentsUser.get(2);
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

        for (int i = 0; i < fullStationList.size(); i++) {
            if (!markalar.contains(fullStationList.get(i).getStationName())) {
                markalar.add(fullStationList.get(i).getStationName());
                logolar.add(fullStationList.get(i).getStationLogoDrawable());
            }
        }

        if (markalar.size() >= 6) {
            checkBox1.setVisibility(View.VISIBLE);
            checkBox1.setCompoundDrawables(null, logolar.get(0), null, null);
            checkBox1.setText(markalar.get(0));

            checkBox2.setVisibility(View.VISIBLE);
            checkBox2.setCompoundDrawables(null, logolar.get(1), null, null);
            checkBox2.setText(markalar.get(1));

            checkBox3.setVisibility(View.VISIBLE);
            checkBox3.setCompoundDrawables(null, logolar.get(2), null, null);
            checkBox3.setText(markalar.get(2));

            checkBox4.setVisibility(View.VISIBLE);
            checkBox4.setCompoundDrawables(null, logolar.get(3), null, null);
            checkBox4.setText(markalar.get(3));

            checkBox5.setVisibility(View.VISIBLE);
            checkBox5.setCompoundDrawables(null, logolar.get(4), null, null);
            checkBox5.setText(markalar.get(4));

            checkBox6.setVisibility(View.VISIBLE);
            checkBox6.setCompoundDrawables(null, logolar.get(5), null, null);
            checkBox6.setText(markalar.get(5));
        } else if (markalar.size() == 5) {
            checkBox1.setVisibility(View.VISIBLE);
            checkBox1.setCompoundDrawables(null, logolar.get(0), null, null);
            checkBox1.setText(markalar.get(0));

            checkBox2.setVisibility(View.VISIBLE);
            checkBox2.setCompoundDrawables(null, logolar.get(1), null, null);
            checkBox2.setText(markalar.get(1));

            checkBox3.setVisibility(View.VISIBLE);
            checkBox3.setCompoundDrawables(null, logolar.get(2), null, null);
            checkBox3.setText(markalar.get(2));

            checkBox4.setVisibility(View.VISIBLE);
            checkBox4.setCompoundDrawables(null, logolar.get(3), null, null);
            checkBox4.setText(markalar.get(3));

            checkBox5.setVisibility(View.VISIBLE);
            checkBox5.setCompoundDrawables(null, logolar.get(4), null, null);
            checkBox5.setText(markalar.get(4));

            checkBox6.setVisibility(View.GONE);
        } else if (markalar.size() == 4) {
            checkBox1.setVisibility(View.VISIBLE);
            checkBox1.setCompoundDrawables(null, logolar.get(0), null, null);
            checkBox1.setText(markalar.get(0));

            checkBox2.setVisibility(View.VISIBLE);
            checkBox2.setCompoundDrawables(null, logolar.get(1), null, null);
            checkBox2.setText(markalar.get(1));

            checkBox3.setVisibility(View.VISIBLE);
            checkBox3.setCompoundDrawables(null, logolar.get(2), null, null);
            checkBox3.setText(markalar.get(2));

            checkBox4.setVisibility(View.VISIBLE);
            checkBox4.setCompoundDrawables(null, logolar.get(3), null, null);
            checkBox4.setText(markalar.get(3));

            checkBox5.setVisibility(View.GONE);
            checkBox6.setVisibility(View.GONE);
        } else if (markalar.size() == 3) {
            checkBox1.setVisibility(View.VISIBLE);
            checkBox1.setCompoundDrawables(null, logolar.get(0), null, null);
            checkBox1.setText(markalar.get(0));

            checkBox2.setVisibility(View.VISIBLE);
            checkBox2.setCompoundDrawables(null, logolar.get(1), null, null);
            checkBox2.setText(markalar.get(1));

            checkBox3.setVisibility(View.VISIBLE);
            checkBox3.setCompoundDrawables(null, logolar.get(2), null, null);
            checkBox3.setText(markalar.get(2));

            checkBox4.setVisibility(View.GONE);
            checkBox5.setVisibility(View.GONE);
            checkBox6.setVisibility(View.GONE);
        } else if (markalar.size() == 2) {
            checkBox1.setVisibility(View.VISIBLE);
            checkBox1.setCompoundDrawables(null, logolar.get(0), null, null);
            checkBox1.setText(markalar.get(0));

            checkBox2.setVisibility(View.VISIBLE);
            checkBox2.setCompoundDrawables(null, logolar.get(1), null, null);
            checkBox2.setText(markalar.get(1));

            checkBox3.setVisibility(View.GONE);
            checkBox4.setVisibility(View.GONE);
            checkBox5.setVisibility(View.GONE);
            checkBox6.setVisibility(View.GONE);
        } else if (markalar.size() == 1) {
            checkBox1.setVisibility(View.VISIBLE);
            checkBox1.setCompoundDrawables(null, logolar.get(0), null, null);
            checkBox1.setText(markalar.get(0));

            checkBox2.setVisibility(View.GONE);
            checkBox3.setVisibility(View.GONE);
            checkBox4.setVisibility(View.GONE);
            checkBox5.setVisibility(View.GONE);
            checkBox6.setVisibility(View.GONE);
        } else {
            checkBox1.setVisibility(View.GONE);
            checkBox2.setVisibility(View.GONE);
            checkBox3.setVisibility(View.GONE);
            checkBox4.setVisibility(View.GONE);
            checkBox5.setVisibility(View.GONE);
            checkBox6.setVisibility(View.GONE);
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
