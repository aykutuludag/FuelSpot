package com.fuelspot.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.R;
import com.fuelspot.StationComments;
import com.fuelspot.StationDetails;
import com.fuelspot.model.CommentItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.yqritc.scalableimageview.ScalableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.dimBehind;
import static com.fuelspot.MainActivity.isSuperUser;
import static com.fuelspot.MainActivity.showAds;
import static com.fuelspot.MainActivity.token;
import static com.fuelspot.MainActivity.username;
import static com.fuelspot.StationDetails.hasAlreadyCommented;
import static com.fuelspot.superuser.SuperMainActivity.listOfOwnedStations;
import static com.fuelspot.superuser.SuperMainActivity.superStationLogo;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<CommentItem> feedItemList;
    private Context mContext;
    private String whichScreen;
    private PopupWindow mPopupWindow;
    private SimpleDateFormat format = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
    private RequestQueue requestQueue;
    private RequestOptions options;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            CommentAdapter.ViewHolder holder = (CommentAdapter.ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            final CommentItem yItem = feedItemList.get(position);

            switch (whichScreen) {
                case "USER_COMMENTS":
                    Intent intent = new Intent(mContext, StationDetails.class);
                    intent.putExtra("STATION_ID", yItem.getStationID());
                    showAds(mContext, intent);
                    break;
                case "STATION_COMMENTS":
                    hasAlreadyCommented = yItem.getComment().length() > 0 && username.equals(yItem.getUsername());
                    if (yItem.getCommentPhoto().length() > 0) {
                        openBigComment(yItem, view);
                    } else {
                        if (isSuperUser) {
                            for (int i = 0; i < listOfOwnedStations.size(); i++) {
                                if ((listOfOwnedStations.get(i).getID() == yItem.getStationID()) && listOfOwnedStations.get(i).getIsVerified() == 1) {
                                    if (yItem.getAnswer().length() > 0) {
                                        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                                        alertDialog.setMessage(mContext.getString(R.string.remove_answer_prompt));
                                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.remove_answer),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        alertDialog.dismiss();
                                                        deleteAnswer(yItem.getID());
                                                    }
                                                });
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.cancel),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        alertDialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                    } else {
                                        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                                        alertDialog.setMessage(mContext.getString(R.string.add_answer_desc));
                                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.answer_it),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        alertDialog.dismiss();
                                                        answerPopup(view, yItem);
                                                    }
                                                });
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.cancel),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        alertDialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                    }
                                    break;
                                }
                            }
                        } else {
                            if (hasAlreadyCommented) {
                                final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                                alertDialog.setTitle(mContext.getString(R.string.your_comment));
                                alertDialog.setMessage(mContext.getString(R.string.snackbar_comment_desc));
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.remove_comment),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                alertDialog.dismiss();
                                                deleteComment(yItem.getID());
                                            }
                                        });
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.update_comment),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                alertDialog.dismiss();
                                                if (mContext instanceof StationComments) {
                                                    ((StationComments) mContext).addUpdateCommentPopup(view);
                                                } else if (mContext instanceof StationDetails) {
                                                    ((StationDetails) mContext).addUpdateCommentPopup(view);
                                                }
                                            }
                                        });
                                alertDialog.show();
                            }
                        }
                    }
                    break;
                default:
                    // Do nothing
                    break;
            }

        }
    };

    public CommentAdapter(Context context, List<CommentItem> feedItemList, String whichPage) {
        this.feedItemList = feedItemList;
        this.mContext = context;
        this.whichScreen = whichPage;
    }

    private void openBigComment(final CommentItem cItem, final View view) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_comment_big, null);
        mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        ImageView imageBig = customView.findViewById(R.id.imageViewCommentBig);
        Glide.with(mContext).load(cItem.getCommentPhoto()).apply(options).into(imageBig);

        RelativeTimeTextView relText = customView.findViewById(R.id.textViewDateFull);
        try {
            Date date = format.parse(cItem.getTime());
            relText.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CircleImageView userCircleImage = customView.findViewById(R.id.imageViewUserIcon);
        Glide.with(mContext).load(cItem.getProfile_pic()).apply(options).into(userCircleImage);

        TextView textViewYorum = customView.findViewById(R.id.textViewCommentFull);
        textViewYorum.setText(cItem.getComment());

        LinearLayout userActionLayout = customView.findViewById(R.id.userActionLayout);
        Button updateCommentButton = customView.findViewById(R.id.button_updateComment);
        updateCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                if (mContext instanceof StationComments) {
                    ((StationComments) mContext).addUpdateCommentPopup(v);
                } else if (mContext instanceof StationDetails) {
                    ((StationDetails) mContext).addUpdateCommentPopup(v);
                }
            }
        });
        Button removeCommentButton = customView.findViewById(R.id.button_removeComment);
        removeCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle(mContext.getString(R.string.remove_comment));
                alertDialog.setMessage(mContext.getString(R.string.remove_comment_prompt));
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                                mPopupWindow.dismiss();
                                deleteComment(cItem.getID());
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        LinearLayout superActionLayout = customView.findViewById(R.id.superUserActionLayout);
        Button addDeleteAnswer = customView.findViewById(R.id.button_addDeleteAnswer);

        if (isSuperUser) {
            for (int i = 0; i < listOfOwnedStations.size(); i++) {
                if ((listOfOwnedStations.get(i).getID() == cItem.getStationID()) && listOfOwnedStations.get(i).getIsVerified() == 1) {
                    // Enable SuperActionLayout
                    superActionLayout.setVisibility(View.VISIBLE);

                    if (cItem.getAnswer().length() > 0) {
                        // ButtonDeleteAnswer
                        addDeleteAnswer.setText(mContext.getString(R.string.remove_answer));
                        addDeleteAnswer.setCompoundDrawables(ContextCompat.getDrawable(mContext, R.drawable.delete), null, null, null);
                        addDeleteAnswer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                                alertDialog.setMessage(mContext.getString(R.string.remove_answer_prompt));
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.remove_answer),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                alertDialog.dismiss();
                                                mPopupWindow.dismiss();
                                                deleteAnswer(cItem.getID());
                                            }
                                        });
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getString(R.string.cancel),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                alertDialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        });
                    } else {
                        // ButtonAddAnswer
                        addDeleteAnswer.setText(mContext.getString(R.string.answer_it));
                        addDeleteAnswer.setCompoundDrawables(ContextCompat.getDrawable(mContext, R.drawable.edit), null, null, null);
                        addDeleteAnswer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mPopupWindow.dismiss();
                                answerPopup(v, cItem);
                            }
                        });
                    }
                    break;
                }
            }
        } else {
            if (hasAlreadyCommented) {
                // Enable UserActionLayout
                userActionLayout.setVisibility(View.VISIBLE);
            } else {
                // Disable/Hide UserActionLayout
                userActionLayout.setVisibility(View.GONE);
            }
        }

        ImageView closeButton = customView.findViewById(R.id.imageViewClose);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBehind(mPopupWindow);
    }


    private void deleteComment(final int id) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.API_DELETE_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                Toast.makeText(mContext, mContext.getString(R.string.comment_delete_success), Toast.LENGTH_LONG).show();
                                if (mContext instanceof StationComments) {
                                    ((StationComments) mContext).fetchStationComments();
                                } else if (mContext instanceof StationDetails) {
                                    ((StationDetails) mContext).fetchStationComments();
                                }
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("commentID", String.valueOf(id));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void answerPopup(View view, final CommentItem cItem) {
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
                if (cItem.getAnswer().length() > 0) {
                    cItem.setLogo(superStationLogo);
                    addAnswer(cItem);
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.empty_answer), Toast.LENGTH_SHORT).show();
                }
            }
        });

        EditText getanswer = customView.findViewById(R.id.editTextAnswer);
        if (cItem.getAnswer().length() > 0) {
            getanswer.setText(cItem.getAnswer());
        }
        getanswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    cItem.setAnswer(s.toString());
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

        mPopupWindow.update();
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBehind(mPopupWindow);
    }

    private void addAnswer(final CommentItem commentItem) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.API_ADD_ANSWER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            if ("Success".equals(response)) {
                                mPopupWindow.dismiss();
                                if (mContext instanceof StationComments) {
                                    ((StationComments) mContext).fetchStationComments();
                                } else if (mContext instanceof StationDetails) {
                                    ((StationDetails) mContext).fetchStationComments();
                                }
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("commentID", String.valueOf(commentItem.getID()));
                params.put("answer", commentItem.getAnswer());
                params.put("logo", commentItem.getLogo());

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void deleteAnswer(final int commentID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.API_DELETE_ANSWER),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            if ("Success".equals(response)) {
                                Toast.makeText(mContext, mContext.getString(R.string.answer_delete_success), Toast.LENGTH_LONG).show();
                                if (mContext instanceof StationComments) {
                                    ((StationComments) mContext).fetchStationComments();
                                } else if (mContext instanceof StationDetails) {
                                    ((StationDetails) mContext).fetchStationComments();
                                }
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(mContext, volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

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

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_comment, viewGroup, false);
        return new CommentAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder viewHolder, int i) {
        CommentItem feedItem = feedItemList.get(i);

        requestQueue = Volley.newRequestQueue(mContext);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.icon_upload).error(R.drawable.icon_upload).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        viewHolder.username.setText(feedItem.getUsername());

        Date date = new Date();
        try {
            date = format.parse(feedItem.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        viewHolder.time.setReferenceTime(date.getTime());

        viewHolder.commentHolder.setText(feedItem.getComment());

        Glide.with(mContext).load(feedItem.getProfile_pic()).apply(options).into(viewHolder.profilePic);

        viewHolder.rating.setRating(feedItem.getRating());
        LayerDrawable stars = (LayerDrawable) viewHolder.rating.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.parseColor("#2DE878"), PorterDuff.Mode.SRC_ATOP);

        if (feedItem.getCommentPhoto() != null && feedItem.getCommentPhoto().length() > 0) {
            viewHolder.commentPhoto.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(feedItem.getCommentPhoto()).into(viewHolder.commentPhoto);
        } else {
            viewHolder.commentPhoto.setVisibility(View.GONE);
        }

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

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout card, answerView;
        TextView commentHolder, answerHolder;
        TextView username;
        RelativeTimeTextView time, replyTime;
        ImageView profilePic, logo;
        RatingBar rating;
        ScalableImageView commentPhoto;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_comment);
            commentHolder = itemView.findViewById(R.id.comment);
            username = itemView.findViewById(R.id.textViewusername);
            time = itemView.findViewById(R.id.time);
            profilePic = itemView.findViewById(R.id.imageViewStationLogo);
            rating = itemView.findViewById(R.id.ratingBar);
            answerView = itemView.findViewById(R.id.answerView);
            answerHolder = itemView.findViewById(R.id.answer);
            replyTime = itemView.findViewById(R.id.textViewReplyTime);
            logo = itemView.findViewById(R.id.imageViewLogo);
            commentPhoto = itemView.findViewById(R.id.imageViewPicture);
        }
    }
}