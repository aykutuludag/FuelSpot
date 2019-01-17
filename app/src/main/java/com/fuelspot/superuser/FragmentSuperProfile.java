package com.fuelspot.superuser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.Application;
import com.fuelspot.ProfileEditActivity;
import com.fuelspot.R;
import com.fuelspot.StoreActivity;
import com.fuelspot.UserFavorites;
import com.fuelspot.adapter.StationAdapter;
import com.fuelspot.model.StationItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.userlat;
import static com.fuelspot.MainActivity.userlon;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.AdminMainActivity.userStations;

public class FragmentSuperProfile extends Fragment {

    public static List<StationItem> listOfOwnedStations = new ArrayList<>();
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
                    Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                    startActivity(intent);
                }
            });

            // Automobiles
            mRecyclerView = rootView.findViewById(R.id.stationViewAdmin);
            mAdapter = new StationAdapter(getActivity(), listOfOwnedStations, "SUPERUSER_STATIONS");
            mLayoutManager = new GridLayoutManager(getActivity(), 1);

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setNestedScrollingEnabled(false);


            // Comments
            title = rootView.findViewById(R.id.titleComment);

            loadProfile();
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
                Intent intent = new Intent(getActivity(), StoreActivity.class);
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

        parseUserStations();
    }

    void parseUserStations() {
        listOfOwnedStations.clear();
        if (userStations != null && userStations.length() > 0) {
            String[] stationIDs = userStations.split(";");
            for (String stationID1 : stationIDs) {
                if (stationID1.length() > 0) {
                    fetchSingleStation(Integer.parseInt(stationID1));
                }
            }
        }
    }

    void fetchSingleStation(final int sID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                StationItem item = new StationItem();
                                item.setID(obj.getInt("id"));
                                item.setStationName(obj.getString("name"));
                                item.setVicinity(obj.getString("vicinity"));
                                item.setCountryCode(obj.getString("country"));
                                item.setLocation(obj.getString("location"));
                                item.setGoogleMapID(obj.getString("googleID"));
                                item.setLicenseNo(obj.getString("licenseNo"));
                                item.setOwner(obj.getString("owner"));
                                item.setPhotoURL(obj.getString("logoURL"));
                                item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                item.setIsVerified(obj.getInt("isVerified"));
                                item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
                                item.setIsActive(obj.getInt("isActive"));
                                item.setLastUpdated(obj.getString("lastUpdated"));

                                //DISTANCE START
                                Location locLastKnow = new Location("");
                                locLastKnow.setLatitude(Double.parseDouble(userlat));
                                locLastKnow.setLongitude(Double.parseDouble(userlon));

                                Location loc = new Location("");
                                String[] stationKonum = item.getLocation().split(";");
                                loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                float uzaklik = locLastKnow.distanceTo(loc);
                                item.setDistance((int) uzaklik);
                                //DISTANCE END

                                listOfOwnedStations.add(item);
                                mAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(sID));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
}