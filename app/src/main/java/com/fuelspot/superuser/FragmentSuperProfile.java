package com.fuelspot.superuser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.Application;
import com.fuelspot.R;
import com.fuelspot.UserFavorites;
import com.fuelspot.adapter.StationAdapter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.SuperMainActivity.listOfOwnedStations;

public class FragmentSuperProfile extends Fragment {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    TextView title;
    RequestOptions options;
    CircleImageView userProfileHolder;
    View headerView;
    RequestQueue requestQueue;
    SharedPreferences prefs;
    View rootView;

    public static FragmentSuperProfile newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Profile");

        FragmentSuperProfile fragment = new FragmentSuperProfile();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_super_profile, container, false);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Profile");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            headerView = rootView.findViewById(R.id.header_profile);

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            requestQueue = Volley.newRequestQueue(getActivity());

            ImageView updateUser = rootView.findViewById(R.id.updateUserInfo);
            updateUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SuperProfileEdit.class);
                    startActivity(intent);
                }
            });

            // Automobiles
            mRecyclerView = rootView.findViewById(R.id.stationViewAdmin);
            mRecyclerView.setNestedScrollingEnabled(false);

            // Comments
            title = rootView.findViewById(R.id.titleComment);
        }

        return rootView;
    }

    void loadProfile() {
        userProfileHolder = headerView.findViewById(R.id.profileImage);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        if (getActivity() != null && userProfileHolder != null) {
            Glide.with(getActivity()).load(photo).apply(options).into(userProfileHolder);
        }

        TextView userusername = headerView.findViewById(R.id.userUsername);
        userusername.setText(username);

        Button myWallet = headerView.findViewById(R.id.button_store);
        myWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SuperStoreActivity.class);
                startActivity(intent);
            }
        });

        Button openHelp = headerView.findViewById(R.id.button_help);
        openHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.enableUrlBarHiding();
                builder.setShowTitle(true);
                builder.setToolbarColor(Color.parseColor("#212121"));
                customTabsIntent.launchUrl(getActivity(), Uri.parse("https://fuel-spot.com/help-for-superuser"));
            }
        });

        Button openFavorites = headerView.findViewById(R.id.button_fav);
        openFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserFavorites.class);
                startActivity(intent);
            }
        });

        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mAdapter = new StationAdapter(getActivity(), listOfOwnedStations, "SUPERUSER_STATIONS");
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (headerView != null) {
            loadProfile();
        }
    }
}