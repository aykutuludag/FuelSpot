package com.fuelspot.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.AddFuel;
import com.fuelspot.R;
import com.fuelspot.model.VehicleItem;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.averageCons;
import static com.fuelspot.MainActivity.carBrand;
import static com.fuelspot.MainActivity.carModel;
import static com.fuelspot.MainActivity.carPhoto;
import static com.fuelspot.MainActivity.carbonEmission;
import static com.fuelspot.MainActivity.fuelPri;
import static com.fuelspot.MainActivity.fuelSec;
import static com.fuelspot.MainActivity.getVariables;
import static com.fuelspot.MainActivity.kilometer;
import static com.fuelspot.MainActivity.plateNo;
import static com.fuelspot.MainActivity.vehicleID;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.ViewHolder> {
    private List<VehicleItem> mItemList;
    private Context mContext;

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            VehicleAdapter.ViewHolder holder = (VehicleAdapter.ViewHolder) view.getTag();
            int position = holder.getAdapterPosition();

            changeVehicle(mItemList.get(position));
            Snackbar.make(view, mContext.getString(R.string.automobile_selected) + ": " + plateNo, Snackbar.LENGTH_SHORT).show();

            if (mContext.getClass().getSimpleName().equals("AddFuel")) {
                ((AddFuel) mContext).loadLayout();
            }
        }
    };

    public VehicleAdapter(Context context, List<VehicleItem> itemList) {
        this.mContext = context;
        this.mItemList = itemList;
    }

    @NonNull
    @Override
    public VehicleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_automobile, viewGroup, false);
        return new VehicleAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleAdapter.ViewHolder viewHolder, int position) {
        VehicleItem feedItem = mItemList.get(position);

        String carName;
        if (feedItem.getVehicleModel() != null && feedItem.getVehicleModel().length() > 0) {
            carName = feedItem.getVehicleBrand() + " " + feedItem.getVehicleModel();
        } else {
            // Add new car row
            carName = feedItem.getVehicleBrand();
        }

        viewHolder.carFullName.setText(carName);

        String plate = feedItem.getVehiclePlateNo();
        viewHolder.carPlateNo.setText(plate);

        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.default_automobile).error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(mContext).load(feedItem.getVehiclePhoto()).apply(options).into(viewHolder.carPhoto);

        if (vehicleID == 0) {
            changeVehicle(feedItem);
        }

        if (feedItem.getID() == vehicleID) {
            viewHolder.vehicleLayout.setBackgroundColor(Color.parseColor("#6000FF00"));
        } else {
            viewHolder.vehicleLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        // Handle click event on image click
        viewHolder.vehicleLayout.setOnClickListener(clickListener);
        viewHolder.vehicleLayout.setTag(viewHolder);
    }

    @Override
    public int getItemCount() {
        return (null != mItemList ? mItemList.size() : 0);
    }

    private void changeVehicle(VehicleItem item) {
        SharedPreferences prefs = mContext.getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

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

        notifyDataSetChanged();
        getVariables(prefs);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout vehicleLayout;
        TextView carFullName, carPlateNo;
        CircleImageView carPhoto;

        ViewHolder(View itemView) {
            super(itemView);
            vehicleLayout = itemView.findViewById(R.id.automobile_background);
            carFullName = itemView.findViewById(R.id.carFullname);
            carPlateNo = itemView.findViewById(R.id.carPlate);
            carPhoto = itemView.findViewById(R.id.carPic);
        }
    }
}
