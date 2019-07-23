package com.fuelspot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.fuelspot.adapter.TabAdapter;
import com.google.android.material.tabs.TabLayout;

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

        return view;
    }
}
