package org.uusoftware.fuelify.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.uusoftware.fuelify.SubFragmentCar;
import org.uusoftware.fuelify.SubFragmentUser;


public class ProfileAdapter extends FragmentPagerAdapter {

    private Fragment fragment = null;

    public ProfileAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                fragment = new SubFragmentUser();
                break;
            case 1:
                fragment = new SubFragmentCar();
                break;
            default:
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
