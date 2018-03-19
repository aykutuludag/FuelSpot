package org.uusoftware.fuelify;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.uusoftware.fuelify.adapter.ProfileAdapter;

public class FragmentProfile extends Fragment {

    ProfileAdapter mSectionsPagerAdapter;
    PagerTitleStrip pagertabstrip;
    ViewPager mViewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mSectionsPagerAdapter = new ProfileAdapter(getActivity().getSupportFragmentManager());
        mViewPager = view.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        pagertabstrip = view.findViewById(R.id.pager_title_strip);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position){
                    case 0:
                        pagertabstrip.setBackgroundColor(Color.parseColor("#212121"));
                        break;
                    case 1:
                        pagertabstrip.setBackgroundColor(Color.parseColor("#F01B1B"));
                        break;
                }
            }

            public void onPageSelected(int position) {
                // Check if this is the page you want.
            }
        });

        return view;
    }
}

