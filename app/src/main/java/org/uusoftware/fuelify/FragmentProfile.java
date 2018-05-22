package org.uusoftware.fuelify;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uusoftware.fuelify.adapter.CommentAdapter;
import org.uusoftware.fuelify.model.CommentItem;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.email;
import static org.uusoftware.fuelify.MainActivity.name;
import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.username;

public class FragmentProfile extends Fragment {

    CircleImageView carPhotoHolder;
    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<CommentItem> feedsList;
    SwipeRefreshLayout swipeContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("User Profile");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        ImageView userProfileHolder = rootView.findViewById(R.id.user_picture);
        Glide.with(getActivity()).load(Uri.parse(photo)).into(userProfileHolder);

        TextView userFullname = rootView.findViewById(R.id.userFullName);
        userFullname.setText(name);

        TextView eposta = rootView.findViewById(R.id.profile_mail);
        eposta.setText(email);

        TextView fullCarName = rootView.findViewById(R.id.profile_CarName);
        String fullad = carBrand + " " + carModel;
        fullCarName.setText(fullad);

        //CarPhoto
        carPhotoHolder = rootView.findViewById(R.id.car_picture);
        Glide.with(getActivity()).load(Uri.parse(carPhoto)).into(carPhotoHolder);

        ImageView updateUser = rootView.findViewById(R.id.updateUserInfo);
        updateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                startActivity(intent);
            }
        });

        //Comments
        feedsList = new ArrayList<>();
        mRecyclerView = rootView.findViewById(R.id.commentView);

        swipeContainer = rootView.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchComments();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fetchComments();

        return rootView;
    }

    public void fetchComments() {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_USER_COMMENTS),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray res = new JSONArray(response);
                            for (int i = 0; i < res.length(); i++) {
                                JSONObject obj = res.getJSONObject(i);

                                CommentItem item = new CommentItem();
                                item.setID(obj.getInt("id"));
                                item.setComment(obj.getString("comment"));
                                item.setTime(obj.getString("time"));
                                item.setProfile_pic(obj.getString("user_photo"));
                                item.setUsername(obj.getString("username"));
                                item.setRating(obj.getInt("stars"));
                                feedsList.add(item);

                                mAdapter = new CommentAdapter(getActivity(), feedsList);
                                mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
}
