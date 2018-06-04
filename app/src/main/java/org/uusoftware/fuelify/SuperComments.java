package org.uusoftware.fuelify;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.uusoftware.fuelify.model.CommentItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.uusoftware.fuelify.AdminMainActivity.superStationID;

public class SuperComments extends AppCompatActivity {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    List<CommentItem> feedsList;
    SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Comments
        feedsList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.commentView);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchSuperComments();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fetchSuperComments();
    }

    public void fetchSuperComments() {
        feedsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_SUPER_COMMENTS),
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
                            }

                            mAdapter = new SuperCommentsAdapter(SuperComments.this, feedsList);
                            mLayoutManager = new GridLayoutManager(SuperComments.this, 1);

                            mAdapter.notifyDataSetChanged();
                            mRecyclerView.setAdapter(mAdapter);
                            mRecyclerView.setLayoutManager(mLayoutManager);
                            swipeContainer.setRefreshing(false);
                        } catch (JSONException e) {
                            swipeContainer.setRefreshing(false);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        swipeContainer.setRefreshing(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("id", String.valueOf(superStationID));

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(SuperComments.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class SuperCommentsAdapter extends RecyclerView.Adapter<SuperComments.SuperCommentsAdapter.ViewHolder3> {

        PopupWindow mPopupWindow;
        Calendar calendar;
        private int commentID;
        private String userAnswer;
        private List<CommentItem> feedItemList;
        private Context mContext;
        private String userName;
        private View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                SuperCommentsAdapter.ViewHolder3 holder3 = (SuperCommentsAdapter.ViewHolder3) view.getTag();
                int position = holder3.getAdapterPosition();
                commentID = feedItemList.get(position).getID();
                userName = feedItemList.get(position).getUsername();

                // Get timestamp
                calendar = Calendar.getInstance();
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

                Snackbar.make(view, "Cevapla?", Snackbar.LENGTH_LONG)
                        .setAction("Evet", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                                View customView = inflater.inflate(R.layout.popup_answer, null);
                                mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                if (Build.VERSION.SDK_INT >= 21) {
                                    mPopupWindow.setElevation(5.0f);
                                }

                                Button sendAnswer = customView.findViewById(R.id.buttonAddAnswer);
                                sendAnswer.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (userAnswer != null && userAnswer.length() > 0) {
                                            addAnswer();
                                        } else {
                                            Toast.makeText(mContext, "Lütfen cevap giriniz.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                EditText getComment = customView.findViewById(R.id.editTextAnswer);
                                getComment.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        if (s != null && s.length() > 0) {
                                            userAnswer = s.toString();
                                        }
                                    }
                                });

                                ImageView closeButton = customView.findViewById(R.id.imageViewClose);
                                // Set a click listener for the popup window close button
                                closeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Dismiss the popup window
                                        mPopupWindow.dismiss();
                                    }
                                });
                                mPopupWindow.setFocusable(true);
                                mPopupWindow.update();
                                mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                            }
                        }).show();
            }
        };

        SuperCommentsAdapter(Context context, List<CommentItem> feedItemList) {
            this.feedItemList = feedItemList;
            this.mContext = context;
        }

        private void addAnswer() {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.API_ADD_ANSWER),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                            mPopupWindow.dismiss();
                            fetchSuperComments();
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
                    params.put("commentID", String.valueOf(commentID));
                    params.put("answer", userAnswer);
                    params.put("time", String.valueOf(calendar.getTimeInMillis()));

                    //returning parameters
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            //Adding request to the queue
            requestQueue.add(stringRequest);
        }

        @NonNull
        @Override
        public SuperComments.SuperCommentsAdapter.ViewHolder3 onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_comment, viewGroup, false);
            return new SuperComments.SuperCommentsAdapter.ViewHolder3(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SuperComments.SuperCommentsAdapter.ViewHolder3 viewHolder, int i) {
            CommentItem feedItem = feedItemList.get(i);
            commentID = feedItem.getID();
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

            // Handle click event on image click
            viewHolder.card.setOnClickListener(clickListener);
            viewHolder.card.setTag(viewHolder);
        }

        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        class ViewHolder3 extends RecyclerView.ViewHolder {

            RelativeLayout card;
            TextView commentHolder;
            TextView username;
            RelativeTimeTextView time;
            ImageView profilePic;
            RatingBar rating;

            ViewHolder3(View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.single_comment);
                commentHolder = itemView.findViewById(R.id.comment);
                username = itemView.findViewById(R.id.username);
                time = itemView.findViewById(R.id.time);
                profilePic = itemView.findViewById(R.id.other_profile_pic);
                rating = itemView.findViewById(R.id.ratingBar);
            }
        }
    }
}
