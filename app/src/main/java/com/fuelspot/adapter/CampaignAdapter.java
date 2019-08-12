package com.fuelspot.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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
import com.fuelspot.model.CampaignItem;
import com.fuelspot.superuser.SuperCampaings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.fuelspot.MainActivity.USTimeFormat;
import static com.fuelspot.MainActivity.dimBehind;
import static com.fuelspot.MainActivity.shortTimeFormat;
import static com.fuelspot.MainActivity.token;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {
    private RequestOptions options;
    private List<CampaignItem> mItemList;
    private Context mContext;
    private String whichScreen;
    private RequestQueue requestQueue;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            CampaignAdapter.ViewHolder holder = (CampaignAdapter.ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            final CampaignItem cItem = mItemList.get(position);

            if (whichScreen.equals("SUPERUSER")) {
                if (cItem.getStationID() == -1) {
                    Toast.makeText(mContext, "Bu kampanya firma tarafından eklendiğinden düzenleyemezsiniz.", Toast.LENGTH_LONG).show();
                    showCampaignPopup(cItem, view);
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle(cItem.getCampaignName());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Gör",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    showCampaignPopup(cItem, view);
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Düzenle",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        ((SuperCampaings) mContext).addORupdateCampaign(view, cItem);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Sil",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                                    alertDialog.setTitle("Kampanyayı silmek istediğinizden emin misiniz?");
                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Kampanyayı sil",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    deleteCampaign(cItem.getID());
                                                }
                                            });
                                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "İptal",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();
                                }
                            });
                    alertDialog.show();
                }
            } else {
                // user at StationDetails
                showCampaignPopup(cItem, view);
            }
        }
    };

    public CampaignAdapter(Context context, List<CampaignItem> itemList, String whichPage) {
        this.mContext = context;
        this.mItemList = itemList;
        this.whichScreen = whichPage;
        this.requestQueue = Volley.newRequestQueue(mContext);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_campaign).error(R.drawable.default_campaign)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
    }

    private void showCampaignPopup(CampaignItem cItem, View view) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.popup_campaign, null);
        final PopupWindow mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        SimpleDateFormat dtformat = new SimpleDateFormat(USTimeFormat, Locale.getDefault());
        SimpleDateFormat shortFormat = new SimpleDateFormat(shortTimeFormat, Locale.getDefault());

        ImageView imgPopup = customView.findViewById(R.id.campaignPhoto);
        Glide.with(mContext).load(cItem.getCampaignPhoto()).into(imgPopup);

        TextView titlePopup = customView.findViewById(R.id.campaignTitle);
        titlePopup.setText(cItem.getCampaignName());

        TextView descPopup = customView.findViewById(R.id.campaignDesc);
        descPopup.setText(cItem.getCampaignDesc());

        TextView startTime = customView.findViewById(R.id.startTime);
        try {
            Date date = dtformat.parse(cItem.getCampaignStart());
            startTime.setText(shortFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TextView endTime = customView.findViewById(R.id.endTime);
        try {
            Date date = dtformat.parse(cItem.getCampaignEnd());
            endTime.setText(shortFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
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

    private void deleteCampaign(final int campaignID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.API_DELETE_CAMPAING),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            switch (response) {
                                case "Success":
                                    Toast.makeText(mContext, "Kampanya silindi", Toast.LENGTH_LONG).show();
                                    ((SuperCampaings) mContext).fetchCampaigns();
                                    break;
                                case "Fail":
                                    Toast.makeText(mContext, "Bir hata oluştu. Lütfen tekrar deneyiniz.", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        } else {
                            Toast.makeText(mContext, "Bir hata oluştu. Lütfen tekrar deneyiniz.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(mContext, "Bir hata oluştu. Lütfen tekrar deneyiniz.", Toast.LENGTH_LONG).show();
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
                params.put("campaignID", String.valueOf(campaignID));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @NonNull
    @Override
    public CampaignAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_campaign, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        CampaignItem feedItem = mItemList.get(i);

        viewHolder.campaignTitle.setText(feedItem.getCampaignName());

        Glide.with(mContext).load(feedItem.getCampaignPhoto()).apply(options).into(viewHolder.campaignPhoto);

        // Handle click event on image click
        viewHolder.cardView.setOnClickListener(clickListener);
        viewHolder.cardView.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != mItemList ? mItemList.size() : 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView campaignTitle;
        ImageView campaignPhoto;

        ViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.single_campaign_card);
            campaignTitle = itemView.findViewById(R.id.textViewTitle);
            campaignPhoto = itemView.findViewById(R.id.imageViewPhoto);

            if (whichScreen.equals("SUPERUSER")) {
                CardView.LayoutParams params = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, 300);
                cardView.setLayoutParams(params);
            }
        }
    }
}