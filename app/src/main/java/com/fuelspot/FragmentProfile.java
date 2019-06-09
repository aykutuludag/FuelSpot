package com.fuelspot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.fuelspot.adapter.CommentAdapter;
import com.fuelspot.adapter.VehicleAdapter;
import com.fuelspot.model.BankingItem;
import com.fuelspot.model.CommentItem;
import com.fuelspot.model.VehicleItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.userAutomobileList;
import static com.fuelspot.MainActivity.userFSMoney;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.MainActivity.vehicleID;

public class FragmentProfile extends Fragment {

    public static List<CommentItem> userCommentList = new ArrayList<>();
    public static List<BankingItem> userBankingList = new ArrayList<>();
    SharedPreferences prefs;
    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView2;
    private RecyclerView.Adapter mAdapter, mAdapter2;
    private RelativeLayout userNoCommentLayout;
    private Button buttonSeeAllComments;
    private TextView textViewFMoney;
    private RequestQueue requestQueue;
    private View rootView;
    private SwipeRefreshLayout swipeContainer;

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

            // Keep screen off
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Analytics
            Tracker t = ((Application) getActivity().getApplication()).getDefaultTracker();
            t.setScreenName("Profile");
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.ScreenViewBuilder().build());

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
            mRecyclerView.setNestedScrollingEnabled(true);

            Button addAutomobile = rootView.findViewById(R.id.button_add_vehicle);
            addAutomobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AddAutomobile.class);
                    startActivity(intent);
                }
            });

            // Comments
            userNoCommentLayout = rootView.findViewById(R.id.noCommentLayout);
            mRecyclerView2 = rootView.findViewById(R.id.commentView);
            mRecyclerView2.setNestedScrollingEnabled(true);

            buttonSeeAllComments = rootView.findViewById(R.id.button_seeAllComments);
            buttonSeeAllComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserComments.class);
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

        textViewFMoney = rootView.findViewById(R.id.textViewFMoney);

        TextView userusername = rootView.findViewById(R.id.userUsername);
        userusername.setText(username);

        Button myWallet = rootView.findViewById(R.id.button_store);
        myWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), StoreActivity.class);
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
                customTabsIntent.launchUrl(getActivity(), Uri.parse("https://fuelspot.com.tr/help"));
            }
        });

        swipeContainer.setRefreshing(false);
        fetchBanking();
        fetchComments();
        fetchAutomobiles();
    }

    private void fetchBanking() {
        userBankingList.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_BANKING) + "?username=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    BankingItem item = new BankingItem();
                                    item.setID(obj.getInt("id"));
                                    item.setUsername(obj.getString("username"));
                                    item.setType(obj.getString("processType"));
                                    item.setCurrency(obj.getString("currency"));
                                    item.setCountry(obj.getString("country"));
                                    item.setAmount((float) obj.getDouble("amount"));
                                    item.setPreviousBalance((float) obj.getDouble("previous_balance"));
                                    item.setCurrentBalance((float) obj.getDouble("current_balance"));
                                    item.setTransactionTime(obj.getString("time"));
                                    item.setNotes(obj.getString("notes"));
                                    userBankingList.add(item);
                                }

                                userFSMoney = (float) res.getJSONObject(0).getDouble("current_balance");
                                String dummyMoneyText = String.format(Locale.getDefault(), "%.2f", userFSMoney) + " FP";
                                textViewFMoney.setText(dummyMoneyText);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void fetchAutomobiles() {
        userAutomobileList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_USER_AUTOMOBILES) + "?username=" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    VehicleItem item = new VehicleItem();
                                    item.setID(obj.getInt("id"));
                                    item.setVehicleBrand(obj.getString("car_brand"));
                                    item.setVehicleModel(obj.getString("car_model"));
                                    item.setVehicleFuelPri(obj.getInt("fuelPri"));
                                    item.setVehicleFuelSec(obj.getInt("fuelSec"));
                                    item.setVehicleKilometer(obj.getInt("kilometer"));
                                    item.setVehiclePhoto(obj.getString("carPhoto"));
                                    item.setVehiclePlateNo(obj.getString("plateNo"));
                                    item.setVehicleConsumption((float) obj.getDouble("avgConsumption"));
                                    item.setVehicleEmission(obj.getInt("carbonEmission"));
                                    userAutomobileList.add(item);

                                    // If there is any selected auto, choose first one.
                                    if (vehicleID == 0) {
                                        chooseVehicle(item);
                                    }

                                    GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
                                    mAdapter = new VehicleAdapter(getActivity(), userAutomobileList);
                                    mAdapter.notifyDataSetChanged();
                                    mRecyclerView.setLayoutManager(mLayoutManager);
                                    mRecyclerView.setAdapter(mAdapter);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void chooseVehicle(VehicleItem item) {
        vehicleID = item.getID();
        prefs.edit().putInt("vehicleID", vehicleID).apply();

        carBrand = item.getVehicleBrand();
        prefs.edit().putString("carBrand", carBrand).apply();

        carModel = item.getVehicleModel();
        prefs.edit().putString("carModel", carModel).apply();

        fuelPri = item.getVehicleFuelPri();
        prefs.edit().putInt("FuelPrimary", fuelPri).apply();

        fuelSec = item.getVehicleFuelSec();
        prefs.edit().putInt("FuelSecondary", fuelSec).apply();

        kilometer = item.getVehicleKilometer();
        prefs.edit().putInt("Kilometer", kilometer).apply();

        carPhoto = item.getVehiclePhoto();
        prefs.edit().putString("CarPhoto", carPhoto).apply();

        plateNo = item.getVehiclePlateNo();
        prefs.edit().putString("plateNo", plateNo).apply();

        averageCons = item.getVehicleConsumption();
        prefs.edit().putFloat("averageConsumption", averageCons).apply();

        carbonEmission = item.getVehicleEmission();
        prefs.edit().putInt("carbonEmission", carbonEmission).apply();
    }

    private void fetchComments() {
        userCommentList.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_FETCH_USER_COMMENTS) + "?username=" + username,
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
                                userNoCommentLayout.setVisibility(View.GONE);
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (textViewFMoney != null) {
            String dummyMoneyText = String.format(Locale.getDefault(), "%.2f", userFSMoney) + " FP";
            textViewFMoney.setText(dummyMoneyText);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
}