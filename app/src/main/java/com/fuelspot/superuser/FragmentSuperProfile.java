package com.fuelspot.superuser;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
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

    private RecyclerView mRecyclerView;
    private View rootView;
    private SwipeRefreshLayout swipeContainer;

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

            // Keep screen off
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Profile");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

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

            Button addStationButton = rootView.findViewById(R.id.button_add_station);
            addStationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AddStation.class);
                    startActivity(intent);
                }
            });

            swipeContainer = rootView.findViewById(R.id.swipeContainer);
            // Setup refresh listener which triggers new data loading
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadProfile();
                }
            });
            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);

            loadProfile();
        }
        return rootView;
    }

    private void loadProfile() {
        CircleImageView userProfileHolder = rootView.findViewById(R.id.profileImage);
        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
        if (getActivity() != null && userProfileHolder != null) {
            Glide.with(getActivity()).load(photo).apply(options).into(userProfileHolder);
        }

        TextView userusername = rootView.findViewById(R.id.userUsername);
        userusername.setText(username);

        Button myWallet = rootView.findViewById(R.id.button_store);
        myWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SuperStoreActivity.class);
                startActivity(intent);
            }
        });

        Button openHelp = rootView.findViewById(R.id.button_help);
        openHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.enableUrlBarHiding();
                builder.setShowTitle(true);
                builder.setToolbarColor(Color.parseColor("#FF7439"));
                customTabsIntent.intent.setPackage("com.android.chrome");
                customTabsIntent.launchUrl(getActivity(), Uri.parse("https://fuelspot.com.tr/help-for-superuser"));
            }
        });

        Button openFavorites = rootView.findViewById(R.id.button_fav);
        openFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserFavorites.class);
                startActivity(intent);
            }
        });

        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        RecyclerView.Adapter mAdapter = new StationAdapter(getActivity(), listOfOwnedStations, "SUPERUSER_STATIONS");
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        swipeContainer.setRefreshing(false);
    }
}