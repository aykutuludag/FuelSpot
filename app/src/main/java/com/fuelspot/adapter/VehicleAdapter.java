package com.fuelspot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuelspot.R;
import com.fuelspot.model.VehicleItem;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.MainActivity.vehicleID;

public class VehicleAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<VehicleItem> mItemList;
    private Context mContext;

    public VehicleAdapter(Context context, List<VehicleItem> itemList) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mItemList = itemList;
    }

    @Override
    public int getCount() {
        return mItemList.size();
    }

    @Override
    public VehicleItem getItem(int i) {
        return mItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.card_automobile_mini, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String carName;
        if (getItem(position).getVehicleModel() != null && getItem(position).getVehicleModel().length() > 0) {
            carName = getItem(position).getVehicleBrand() + " " + getItem(position).getVehicleModel();
        } else {
            // Add new car row
            carName = getItem(position).getVehicleBrand();
        }

        holder.carFullName.setText(carName);

        String plate = getItem(position).getVehiclePlateNo();
        holder.carPlateNo.setText(plate);

        RequestOptions options = new RequestOptions().centerCrop().placeholder(R.drawable.default_automobile).error(R.drawable.default_automobile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(mContext).load(getItem(position).getVehiclePhoto()).apply(options).into(holder.carPhoto);

        if (getItem(position).getID() == vehicleID) {
            holder.isCarSelected.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView carFullName, carPlateNo;
        CircleImageView carPhoto, isCarSelected;

        ViewHolder(View view) {
            carFullName = view.findViewById(R.id.carFullname);
            carPlateNo = view.findViewById(R.id.carPlateNo);
            carPhoto = view.findViewById(R.id.carPicture);
            isCarSelected = view.findViewById(R.id.carIsSelected);
        }
    }
}
