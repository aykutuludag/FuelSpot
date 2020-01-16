package com.fuelspot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.fuelspot.adapter.TabAdapter;
import com.google.android.material.tabs.TabLayout;

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

public class FragmentFilter extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_filter, container);

        ViewPager viewPager = view.findViewById(R.id.viewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        TabAdapter adapter = new TabAdapter(getChildFragmentManager());
        adapter.addFragment(new FragmentFilterFacilities(), "Tesisler");
        adapter.addFragment(new FragmentFilterDistributors(), "Markalar");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        // This means distributor filters has been selected. Open that page.
        boolean isDistributorFilterActive = filterStation1 || filterStation2 || filterStation3 || filterStation4 || filterStation5 || filterStation6 || filterStation7 || filterStation8 || filterStation9 || filterStation10
                || filterStation11 || filterStation12;
        if (isDistributorFilterActive) {
            viewPager.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(0);
        }

        return view;
    }
}
