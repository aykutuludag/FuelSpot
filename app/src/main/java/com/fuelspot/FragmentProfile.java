package com.fuelspot;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
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
import com.fuelspot.model.CommentItem;
import com.fuelspot.superuser.AdminMainActivity;
import com.fuelspot.superuser.AdminProfileEdit;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentProfile extends Fragment {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<CommentItem> feedsList = new ArrayList<>();
    SwipeRefreshLayout swipeContainer;
    Snackbar snackBar;
    ImageView errorPhoto;
    TextView title;

    public static FragmentProfile newInstance() {

        Bundle args = new Bundle();

        FragmentProfile fragment = new FragmentProfile();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("Profil");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        String snackBarText = "Henüz hiç yorum yazmamışsınız.";
        if (MainActivity.isSuperUser) {
            snackBarText = "Henüz hiç cevap yazmamışsınız.";
        }
        if (MainActivity.isSuperUser) {
            snackBar = Snackbar.make(getActivity().findViewById(R.id.pager), snackBarText, Snackbar.LENGTH_LONG);
        } else {
            snackBar = Snackbar.make(getActivity().findViewById(R.id.mainContainer), snackBarText, Snackbar.LENGTH_LONG);
        }
        snackBar.setAction("Tamam", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });

        CircleImageView userProfileHolder = rootView.findViewById(R.id.user_picture);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.photo_placeholder)
                .error(R.drawable.photo_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(getActivity()).load(Uri.parse(MainActivity.photo)).apply(options).into(userProfileHolder);

        TextView userFullname = rootView.findViewById(R.id.userFullName);
        userFullname.setText(MainActivity.name);

        title = rootView.findViewById(R.id.viewTitle);
        if (MainActivity.isSuperUser) {
            title.setText("Son cevaplarınız");
        }

        errorPhoto = rootView.findViewById(R.id.errorPhoto);

        ImageView updateUser = rootView.findViewById(R.id.updateUserInfo);
        updateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isSuperUser) {
                    Intent intent = new Intent(getActivity(), AdminProfileEdit.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                    startActivity(intent);
                }
            }
        });

        Button getPremium = rootView.findViewById(R.id.button_premium);
        getPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isSuperUser) {
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

        Button openHelp = rootView.findViewById(R.id.button_help);
        openHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.isSuperUser) {
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

        Button openPrivacy = rootView.findViewById(R.id.button_privacy);
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

        //Comments
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

        return rootView;
    }

    public void fetchComments() {
        feedsList.clear();
        String whichApi = getString(R.string.API_FETCH_USER_COMMENTS);
        if (MainActivity.isSuperUser) {
            whichApi = getString(R.string.API_FETCH_STATION_COMMENTS);
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, whichApi,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CommentItem item = new CommentItem();
                                    item.setID(obj.getInt("id"));
                                    item.setComment(obj.getString("fab_comment"));
                                    item.setTime(obj.getString("time"));
                                    item.setStationID(obj.getInt("station_id"));
                                    item.setUsername(obj.getString("username"));
                                    item.setProfile_pic(obj.getString("user_photo"));
                                    item.setRating(obj.getInt("stars"));
                                    item.setAnswer(obj.getString("answer"));
                                    item.setReplyTime(obj.getString("replyTime"));
                                    item.setLogo(obj.getString("logo"));
                                    feedsList.add(item);
                                }
                                mAdapter = new CommentAdapterforProfile(getActivity(), feedsList);
                                mLayoutManager = new GridLayoutManager(getActivity(), 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                swipeContainer.setRefreshing(false);
                            } catch (JSONException e) {
                                errorPhoto.setVisibility(View.VISIBLE);
                                snackBar.show();
                                swipeContainer.setRefreshing(false);
                                e.printStackTrace();
                            }
                        } else {
                            errorPhoto.setVisibility(View.VISIBLE);
                            snackBar.show();
                            swipeContainer.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorPhoto.setVisibility(View.VISIBLE);
                        snackBar.show();
                        swipeContainer.setRefreshing(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", MainActivity.username);

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRecyclerView != null) {
            fetchComments();
        }
    }

    public class CommentAdapterforProfile extends RecyclerView.Adapter<CommentAdapterforProfile.ViewHolder2> {

        private List<CommentItem> feedItemList;
        private Context mContext;
        private String userName;

        private View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewHolder2 holder2 = (ViewHolder2) view.getTag();
                final int position = holder2.getAdapterPosition();
                userName = feedItemList.get(position).getUsername();

                String localUser = MainActivity.username;
                if (localUser.equals(userName)) {
                    Intent intent = new Intent(mContext, StationDetails.class);
                    intent.putExtra("STATION_ID", feedItemList.get(position).getStationID());
                    mContext.startActivity(intent);
                }
            }
        };

        CommentAdapterforProfile(Context context, List<CommentItem> feedItemList) {
            this.feedItemList = feedItemList;
            this.mContext = context;
        }

        @NonNull
        @Override
        public ViewHolder2 onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_comment, viewGroup, false);
            return new ViewHolder2(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder2 viewHolder, int i) {
            CommentItem feedItem = feedItemList.get(i);
            userName = feedItem.getUsername();

            viewHolder.username.setText(userName);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            try {
                date = format.parse(feedItem.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viewHolder.time.setReferenceTime(date.getTime());

            viewHolder.commentHolder.setText(feedItem.getComment());

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.photo_placeholder)
                    .error(R.drawable.photo_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            Glide.with(mContext).load(feedItem.getProfile_pic()).apply(options).into(viewHolder.profilePic);

            viewHolder.rating.setRating(feedItem.getRating());

            if (feedItem.getAnswer() != null && feedItem.getAnswer().length() > 0) {
                viewHolder.answerView.setVisibility(View.VISIBLE);

                viewHolder.answerHolder.setText(feedItem.getAnswer());
                try {
                    Date date2 = format.parse(feedItem.getReplyTime());
                    viewHolder.replyTime.setReferenceTime(date2.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //Station Icon
                Glide.with(mContext).load(feedItem.getLogo()).into(viewHolder.logo);
            }

            // Handle click event on image click
            viewHolder.card.setOnClickListener(clickListener);
            viewHolder.card.setTag(viewHolder);
        }

        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        class ViewHolder2 extends RecyclerView.ViewHolder {

            RelativeLayout card, answerView;
            TextView commentHolder, answerHolder;
            TextView username;
            RelativeTimeTextView time, replyTime;
            ImageView profilePic, logo;
            RatingBar rating;

            ViewHolder2(View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.single_comment);
                commentHolder = itemView.findViewById(R.id.comment);
                username = itemView.findViewById(R.id.username);
                time = itemView.findViewById(R.id.time);
                profilePic = itemView.findViewById(R.id.other_profile_pic);
                rating = itemView.findViewById(R.id.ratingBar);
                answerView = itemView.findViewById(R.id.answerView);
                answerHolder = itemView.findViewById(R.id.answer);
                replyTime = itemView.findViewById(R.id.textViewReplyTime);
                logo = itemView.findViewById(R.id.imageViewLogo);
            }
        }
    }
}
