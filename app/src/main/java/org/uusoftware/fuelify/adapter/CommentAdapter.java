package org.uusoftware.fuelify.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import org.uusoftware.fuelify.MainActivity;
import org.uusoftware.fuelify.R;
import org.uusoftware.fuelify.StationDetails;
import org.uusoftware.fuelify.model.CommentItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private int commentID;
    private List<CommentItem> feedItemList;
    private Context mContext;
    private String userName;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ViewHolder holder = (ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            commentID = feedItemList.get(position).getID();
            userName = feedItemList.get(position).getUsername();

            String localUser = MainActivity.username;
            if (localUser.equals(userName)) {
                Snackbar.make(view, "Yorumu?", Snackbar.LENGTH_LONG)
                        .setAction("Sil", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteComment(commentID);
                            }
                        })
                        .show();
            }
        }
    };

    public CommentAdapter(Context context, List<CommentItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    private void deleteComment(final int id) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.API_DELETE_COMMENT),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                        ((StationDetails) mContext).fetchComments();
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
                params.put("id", String.valueOf(id));

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_comment, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
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

    class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout card;
        TextView commentHolder;
        TextView username;
        RelativeTimeTextView time;
        ImageView profilePic;
        RatingBar rating;

        ViewHolder(View itemView) {
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