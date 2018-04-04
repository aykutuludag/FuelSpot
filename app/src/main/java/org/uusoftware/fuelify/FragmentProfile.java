package org.uusoftware.fuelify;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentProfile extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ProfileAdapter mSectionsPagerAdapter = new ProfileAdapter(getActivity().getSupportFragmentManager());
        ViewPager mViewPager = view.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        final PagerTitleStrip pagertabstrip = view.findViewById(R.id.pager_title_strip);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                switch (position) {
                    case 0:
                        pagertabstrip.setBackgroundColor(Color.parseColor("#212121"));
                        break;
                    case 1:
                        pagertabstrip.setBackgroundColor(Color.parseColor("#008000"));
                        break;
                }
            }

            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        pagertabstrip.setBackgroundColor(Color.parseColor("#212121"));
                        break;
                    case 1:
                        pagertabstrip.setBackgroundColor(Color.parseColor("#008000"));
                        break;
                    default:
                        break;
                }
            }
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.pager, new SubFragmentUser());
            transaction.commit();
        }

        return view;
    }


    class ProfileAdapter extends FragmentPagerAdapter {

        ProfileAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new SubFragmentUser();
                    break;
                case 1:
                    fragment = new SubFragmentCar();
                    break;
                default:
                    fragment = new SubFragmentUser();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "User profile";
                case 1:
                    return "Vehicle profile";
            }
            return null;
        }
    }
}

