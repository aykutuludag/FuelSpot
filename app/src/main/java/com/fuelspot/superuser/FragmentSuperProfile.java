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
import android.widget.RelativeLayout;
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
import com.fuelspot.UserComments;
import com.fuelspot.UserFavorites;
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.model.CommentItem;
import com.fuelspot.model.VehicleItem;
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

import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.userFSMoney;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.AdminMainActivity.superStationID;

public class FragmentSuperProfile extends Fragment {

    public static List<VehicleItem> userAutomobileList = new ArrayList<>();
    public static List<CommentItem> userCommentList = new ArrayList<>();
    static List<String> vehicleIDs = new ArrayList<>();

    RecyclerView mRecyclerView, mRecyclerView2;
    RecyclerView.Adapter mAdapter, mAdapter2;
    TextView title;
    RequestOptions options;
    CircleImageView userProfileHolder;
    View headerView;
    RelativeLayout userNoCommentLayout;
    Button buttonSeeAllComments;
    TextView textViewFMoney;
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
            mRecyclerView = rootView.findViewById(R.id.automobileView);

            // Comments
            title = rootView.findViewById(R.id.titleComment);
            if (isSuperUser) {
                title.setText("Son cevaplarınız");
            }

            userNoCommentLayout = rootView.findViewById(R.id.noCommentLayout);
            mRecyclerView2 = rootView.findViewById(R.id.commentView);

            buttonSeeAllComments = rootView.findViewById(R.id.button_seeAllComments);
            buttonSeeAllComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserComments.class);
                    startActivity(intent);
                }
            });

            loadProfile();
            fetchComments();
        }
        return rootView;
    }

    void parseUserStations() {

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

        textViewFMoney = headerView.findViewById(R.id.textViewFMoney);
        String dummyMoneyText = userFSMoney + " " + currencySymbol;
        textViewFMoney.setText(dummyMoneyText);

        TextView userusername = headerView.findViewById(R.id.userUsername);
        userusername.setText(username);

        Button myWallet = headerView.findViewById(R.id.button_wallet);
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
                if (!isSuperUser) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.enableUrlBarHiding();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(Color.parseColor("#212121"));
                    customTabsIntent.launchUrl(getActivity(), Uri.parse("https://fuel-spot.com/help"));
                } else {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.enableUrlBarHiding();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(Color.parseColor("#212121"));
                    customTabsIntent.launchUrl(getActivity(), Uri.parse("https://fuel-spot.com/help-for-superuser"));
                }
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
    }

    public void fetchComments() {
        userCommentList.clear();

        final String whichApi, whichParamater, whichValue;
        if (isSuperUser) {
            whichApi = getString(R.string.API_FETCH_STATION_COMMENTS);
            whichParamater = "stationID";
            whichValue = String.valueOf(superStationID);
        } else {
            whichApi = getString(R.string.API_FETCH_COMMENTS);
            whichParamater = "username";
            whichValue = username;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, whichApi,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            List<CommentItem> dummyList = new ArrayList<>();
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CommentItem item = new CommentItem();
                                    item.setID(obj.getInt("id"));
                                    item.setComment(obj.getString("comment"));
                                    item.setTime(obj.getString("time"));
                                    item.setStationID(obj.getInt("station_id"));
                                    item.setUsername(obj.getString("username"));
                                    item.setProfile_pic(obj.getString("user_photo"));
                                    item.setRating(obj.getInt("stars"));
                                    item.setAnswer(obj.getString("answer"));
                                    item.setReplyTime(obj.getString("replyTime"));
                                    item.setLogo(obj.getString("logo"));
                                    userCommentList.add(item);

                                    if (i < 3) {
                                        dummyList.add(item);
                                    } else {
                                        buttonSeeAllComments.setVisibility(View.VISIBLE);
                                    }
                                }
                                mAdapter2 = new CommentAdapter(getActivity(), dummyList, "USER_COMMENTS");
                                GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                mAdapter2.notifyDataSetChanged();
                                mRecyclerView2.setAdapter(mAdapter2);
                                mRecyclerView2.setLayoutManager(mLayoutManager);
                            } catch (JSONException e) {
                                userNoCommentLayout.setVisibility(View.VISIBLE);
                            }
                        } else {
                            userNoCommentLayout.setVisibility(View.VISIBLE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        userNoCommentLayout.setVisibility(View.VISIBLE);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put(whichParamater, whichValue);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userAutomobileList.size() == 0) {
            parseUserStations();
        }
    }
}