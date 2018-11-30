package com.fuelspot;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
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
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.model.CommentItem;
import com.fuelspot.model.ReportItem;
import com.fuelspot.superuser.AdminMainActivity;
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

import static com.fuelspot.MainActivity.birthday;
import static com.fuelspot.MainActivity.currencySymbol;
import static com.fuelspot.MainActivity.email;
import static com.fuelspot.MainActivity.gender;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.location;
import static com.fuelspot.MainActivity.name;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.userCountry;
import static com.fuelspot.MainActivity.userDisplayLanguage;
import static com.fuelspot.MainActivity.userFuelSpotMoney;
import static com.fuelspot.MainActivity.userPhoneNumber;
import static com.fuelspot.MainActivity.userVehicles;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.superuser.AdminMainActivity.superStationID;

public class FragmentProfile extends Fragment {

    public static List<CommentItem> userCommentList = new ArrayList<>();
    public static List<ReportItem> userReportList = new ArrayList<>();
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    TextView title;
    RequestOptions options;
    CircleImageView userProfileHolder;
    View headerView;
    RelativeLayout userNoCommentLayout;
    Button buttonSeeAllRewards, buttonSeeAllComments;
    TextView textViewFMoney;
    RequestQueue requestQueue;
    SharedPreferences prefs;
    View rootView;

    public static FragmentProfile newInstance() {
        Bundle args = new Bundle();
        args.putString("FRAGMENT", "Profile");

        FragmentProfile fragment = new FragmentProfile();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_profile, container, false);

            // Analytics
            Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Profile");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

            headerView = rootView.findViewById(R.id.header_profile);

            title = rootView.findViewById(R.id.titleComment);
            if (isSuperUser) {
                title.setText("Son cevaplarınız");
            }

            prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);
            requestQueue = Volley.newRequestQueue(getActivity());
            userNoCommentLayout = rootView.findViewById(R.id.noCommentLayout);

            ImageView updateUser = rootView.findViewById(R.id.updateUserInfo);
            updateUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                    startActivity(intent);
                }
            });

            Button getPremium = rootView.findViewById(R.id.button_premium);
            getPremium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isSuperUser) {
                        try {
                            ((AdminMainActivity) getActivity()).buyAdminPremium();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            ((MainActivity) getActivity()).buyPremium();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });


            buttonSeeAllComments = rootView.findViewById(R.id.button_seeAllComments);
            buttonSeeAllComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserComments.class);
                    startActivity(intent);
                }
            });

            //Comments
            mRecyclerView = rootView.findViewById(R.id.commentView);

            fetchProfile();
            fetchComments();
        }
        return rootView;
    }

    void fetchProfile() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_USER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try {
                            JSONArray res = new JSONArray(response);
                            JSONObject obj = res.getJSONObject(0);

                            name = obj.getString("name");
                            prefs.edit().putString("Name", name).apply();

                            email = obj.getString("email");
                            prefs.edit().putString("Email", email).apply();

                            photo = obj.getString("photo");
                            prefs.edit().putString("ProfilePhoto", photo).apply();

                            gender = obj.getString("gender");
                            prefs.edit().putString("Gender", gender).apply();

                            birthday = obj.getString("birthday");
                            prefs.edit().putString("Birthday", birthday).apply();

                            userPhoneNumber = obj.getString("phoneNumber");
                            prefs.edit().putString("userPhoneNumber", userPhoneNumber).apply();

                            location = obj.getString("location");
                            prefs.edit().putString("Location", location).apply();

                            userCountry = obj.getString("country");
                            prefs.edit().putString("userCountry", userCountry).apply();

                            userDisplayLanguage = obj.getString("language");
                            prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                            userVehicles = obj.getString("vehicles");
                            prefs.edit().putString("userVehicles", userVehicles).apply();

                            getVariables(prefs);
                            loadProfile();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
        String dummyMoneyText = userFuelSpotMoney + " " + currencySymbol;
        textViewFMoney.setText(dummyMoneyText);

        TextView userusername = headerView.findViewById(R.id.userUsername);
        userusername.setText(username);

        Button myWallet = headerView.findViewById(R.id.button_wallet);
        myWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyWalletActivity.class);
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
                    customTabsIntent.launchUrl(getActivity(), Uri.parse("http://fuel-spot.com/help"));
                } else {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    builder.enableUrlBarHiding();
                    builder.setShowTitle(true);
                    builder.setToolbarColor(Color.parseColor("#212121"));
                    customTabsIntent.launchUrl(getActivity(), Uri.parse("http://fuel-spot.com/help-for-superuser"));
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


        Button openPrivacy = headerView.findViewById(R.id.button_privacy);
        openPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                builder.enableUrlBarHiding();
                builder.setShowTitle(true);
                builder.setToolbarColor(Color.parseColor("#212121"));
                customTabsIntent.launchUrl(getActivity(), Uri.parse("http://fuel-spot.com/privacy"));
            }
        });
    }

    public void fetchComments() {
        userCommentList.clear();

        final String whichApi, whichParamater, whichValue;
        if (isSuperUser) {
            whichApi = getString(R.string.API_FETCH_STATION_COMMENTS);
            whichParamater = "id";
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
                                mAdapter = new CommentAdapter(getActivity(), dummyList, "USER_COMMENTS");
                                GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
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

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
}