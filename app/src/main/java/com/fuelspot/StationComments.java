package com.fuelspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
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
import android.view.Window;
import android.view.WindowManager;
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
import com.fuelspot.model.CommentItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.photo;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.StationDetails.choosenStationID;
import static com.fuelspot.StationDetails.commentList;
import static com.fuelspot.StationDetails.hasAlreadyCommented;
import static com.fuelspot.StationDetails.numOfComments;
import static com.fuelspot.StationDetails.stars;
import static com.fuelspot.StationDetails.stationScore;
import static com.fuelspot.StationDetails.sumOfPoints;
import static com.fuelspot.StationDetails.userComment;
import static com.fuelspot.StationDetails.userCommentID;
import static com.fuelspot.superuser.AdminMainActivity.superStationLogo;

public class StationComments extends AppCompatActivity {

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;
    SwipeRefreshLayout swipeContainer;
    RequestQueue requestQueue;
    PopupWindow mPopupWindow;
    Calendar calendar;
    Snackbar snackbar;
    Window window;
    Toolbar toolbar;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_comments);

        window = this.getWindow();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#0288D1"), Color.parseColor("#03A9F4"));

        // Get timestamp
        calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        //Comments
        requestQueue = Volley.newRequestQueue(StationComments.this);
        mRecyclerView = findViewById(R.id.commentView);

        swipeContainer = findViewById(R.id.swipeContainer);
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
        snackbar = Snackbar.make(swipeContainer, "Henüz hiç yorum yazılmamış.", Snackbar.LENGTH_LONG);

        // FABs
        materialDesignFAM = findViewById(R.id.fab_menu);

        floatingActionButton1 = findViewById(R.id.fab1);
        if (isSuperUser) {
            floatingActionButton1.setVisibility(View.GONE);
        } else {
            if (hasAlreadyCommented) {
                floatingActionButton1.setLabelText("Yorumu güncelle");
            } else {
                floatingActionButton1.setLabelText("Yorum yaz");
            }
            floatingActionButton1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    materialDesignFAM.close(true);
                    addUpdateCommentPopup(v);
                }
            });
        }

        loadComments();
    }

    public void loadComments() {
        if (commentList != null && commentList.size() > 0) {
            mAdapter = new SuperCommentsAdapter(StationComments.this, commentList);
            mLayoutManager = new GridLayoutManager(StationComments.this, 1);

            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
            swipeContainer.setRefreshing(false);
        } else {
            snackbar.show();
        }
    }

    void addUpdateCommentPopup(View view) {
        LayoutInflater inflater = (LayoutInflater) StationComments.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_comment, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        TextView titlePopup = customView.findViewById(R.id.campaignPhoto);
        if (hasAlreadyCommented) {
            titlePopup.setText("Yorumu güncelle");
        } else {
            titlePopup.setText("Yorum yaz");
        }

        Button sendAnswer = customView.findViewById(R.id.buttonSendComment);
        sendAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userComment != null && userComment.length() > 0) {
                    if (hasAlreadyCommented) {
                        updateComment();
                    } else {
                        addComment();
                    }
                } else {
                    Toast.makeText(StationComments.this, "Lütfen yorumunuzu yazınız", Toast.LENGTH_SHORT).show();
                }
            }
        });

        EditText getComment = customView.findViewById(R.id.editTextComment);
        if (userComment != null && userComment.length() > 0) {
            getComment.setText(userComment);
        }
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
                    userComment = s.toString();
                }
            }
        });

        final RatingBar ratingBar = customView.findViewById(R.id.ratingBar);
        ratingBar.setRating(stars);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                stars = (int) rating;
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

    public void fetchComments() {
        commentList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION_COMMENTS),
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
                                    item.setComment(obj.getString("comment"));
                                    item.setTime(obj.getString("time"));
                                    item.setProfile_pic(obj.getString("user_photo"));
                                    item.setUsername(obj.getString("username"));
                                    item.setRating(obj.getInt("stars"));
                                    item.setAnswer(obj.getString("answer"));
                                    item.setReplyTime(obj.getString("replyTime"));
                                    item.setLogo(obj.getString("logo"));
                                    commentList.add(item);

                                    sumOfPoints += obj.getInt("stars");
                                    numOfComments++;

                                    if (obj.getString("username").equals(username)) {
                                        hasAlreadyCommented = true;
                                        userCommentID = obj.getInt("id");
                                        userComment = obj.getString("comment");
                                        stars = obj.getInt("stars");
                                    }
                                }

                                stationScore = sumOfPoints / numOfComments;
                                mAdapter = new SuperCommentsAdapter(StationComments.this, commentList);
                                mLayoutManager = new GridLayoutManager(StationComments.this, 1);

                                mAdapter.notifyDataSetChanged();
                                mRecyclerView.setVisibility(View.VISIBLE);
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.setLayoutManager(mLayoutManager);
                                swipeContainer.setRefreshing(false);
                            } catch (JSONException e) {
                                swipeContainer.setRefreshing(false);
                                snackbar.show();
                            }
                        } else {
                            mRecyclerView.setVisibility(View.GONE);
                            swipeContainer.setRefreshing(false);
                            snackbar.show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mRecyclerView.setVisibility(View.GONE);
                        swipeContainer.setRefreshing(false);
                        snackbar.show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("id", String.valueOf(choosenStationID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void addComment() {
        final ProgressDialog loading = ProgressDialog.show(StationComments.this, "Adding comment...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Toast.makeText(StationComments.this, response, Toast.LENGTH_SHORT).show();
                        mPopupWindow.dismiss();
                        hasAlreadyCommented = true;
                        fetchComments();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        loading.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("comment", userComment);
                params.put("station_id", String.valueOf(choosenStationID));
                params.put("username", username);
                params.put("user_photo", photo);
                params.put("stars", String.valueOf(stars));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updateComment() {
        final ProgressDialog loading = ProgressDialog.show(StationComments.this, "Updating comment...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Toast.makeText(StationComments.this, response, Toast.LENGTH_SHORT).show();
                        mPopupWindow.dismiss();
                        fetchComments();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        loading.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("commentID", String.valueOf(userCommentID));
                params.put("comment", userComment);
                params.put("station_id", String.valueOf(choosenStationID));
                params.put("username", username);
                params.put("user_photo", photo);
                params.put("stars", String.valueOf(stars));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void deleteComment(final int id) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_DELETE_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    fetchComments();
                                    break;
                                case "Fail":
                                    Toast.makeText(StationComments.this, "Fail", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(StationComments.this, "Fail", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StationComments.this, "Fail", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("id", String.valueOf(id));

                //returning parameters
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void addAnswer(final int commentID, final String userAnswer) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, this.getString(R.string.API_ADD_ANSWER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    mPopupWindow.dismiss();
                                    fetchComments();
                                    break;
                                case "Fail":
                                    Toast.makeText(StationComments.this, "Fail", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(StationComments.this, "Fail", Toast.LENGTH_LONG).show();
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
                params.put("commentID", String.valueOf(commentID));
                params.put("answer", userAnswer);
                params.put("logo", superStationLogo);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void deleteAnswer(final int commentID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_DELETE_ANSWER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    fetchComments();
                                    break;
                                case "Fail":
                                    Toast.makeText(StationComments.this, "Fail", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(StationComments.this, "Fail", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("commentID", String.valueOf(commentID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void coloredBars(int color1, int color2) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color1);
            toolbar.setBackgroundColor(color2);
        } else {
            toolbar.setBackgroundColor(color2);
        }
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

    public class SuperCommentsAdapter extends RecyclerView.Adapter<StationComments.SuperCommentsAdapter.ViewHolder3> {

        private String userAnswer;
        private List<CommentItem> feedItemList;
        private Context mContext;
        String text;

        private View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                SuperCommentsAdapter.ViewHolder3 holder3 = (SuperCommentsAdapter.ViewHolder3) view.getTag();
                final int position = holder3.getAdapterPosition();
                final int commentID = feedItemList.get(position).getID();
                final String answer = feedItemList.get(position).getAnswer();
                final String commentUserName = feedItemList.get(position).getUsername();

                if (isSuperUser) {
                    if (answer != null && answer.length() > 0) {
                        // Delete answer
                        text = "Cevabı sil?";
                    } else {
                        // Add answer
                        text = "Cevapla";
                    }
                } else {
                    if (username.equals(commentUserName)) {
                        // Delete comment
                        text = "Yorumu sil";
                    }
                }

                if (text != null && text.length() > 0) {
                    Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                            .setAction("Evet", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (isSuperUser) {
                                        if (answer != null && answer.length() > 0) {
                                            deleteAnswer(commentID);
                                        } else {
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
                                                        addAnswer(commentID, userAnswer);
                                                    } else {
                                                        Toast.makeText(mContext, "Lütfen cevap giriniz.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                            EditText getComment = customView.findViewById(R.id.editTextAnswer);
                                            if (userAnswer != null && userAnswer.length() > 0) {
                                                getComment.setText(userAnswer);
                                            }
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
                                    } else {
                                        // deleteComment
                                        deleteComment(commentID);
                                    }
                                }
                            }).show();
                }
            }
        };

        SuperCommentsAdapter(Context context, List<CommentItem> feedItemList) {
            this.feedItemList = feedItemList;
            this.mContext = context;
        }

        @NonNull
        @Override
        public StationComments.SuperCommentsAdapter.ViewHolder3 onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_comment, viewGroup, false);
            return new StationComments.SuperCommentsAdapter.ViewHolder3(v);
        }

        @Override
        public void onBindViewHolder(@NonNull StationComments.SuperCommentsAdapter.ViewHolder3 viewHolder, int i) {
            CommentItem feedItem = feedItemList.get(i);
            String userName = feedItem.getUsername();

            viewHolder.username.setText(userName);

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = format.parse(feedItem.getTime());
                viewHolder.time.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            viewHolder.commentHolder.setText(feedItem.getComment());

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);
            Glide.with(mContext).load(feedItem.getProfile_pic()).apply(options).into(viewHolder.profilePic);

            viewHolder.rating.setRating(feedItem.getRating());


            if (feedItem.getAnswer() != null && feedItem.getAnswer().length() > 0) {
                viewHolder.answerView.setVisibility(View.VISIBLE);

                viewHolder.answerHolder.setText(feedItem.getAnswer());
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = format.parse(feedItem.getReplyTime());
                    viewHolder.replyTime.setReferenceTime(date.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //Station Icon
                Glide.with(mContext).load(feedItem.getLogo()).into(viewHolder.logo);
            }

            // Handle click event on image click
            viewHolder.card.setOnClickListener(clickListener);
            viewHolder.card.setTag(viewHolder);

            viewHolder.answerView.setOnClickListener(clickListener);
            viewHolder.answerView.setTag(viewHolder);
        }


        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        class ViewHolder3 extends RecyclerView.ViewHolder {

            RelativeLayout card, answerView;
            TextView commentHolder, answerHolder;
            TextView username;
            RelativeTimeTextView time, replyTime;
            ImageView profilePic, logo;
            RatingBar rating;

            ViewHolder3(View itemView) {
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
