package org.uusoftware.fuelify;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.email;
import static org.uusoftware.fuelify.MainActivity.name;
import static org.uusoftware.fuelify.MainActivity.photo;

public class FragmentProfile extends Fragment {

    CircleImageView carPhotoHolder;

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

       /* TextView userName = headerView.findViewById(R.id.username);
        userName.setText(username);*/

        TextView userFullname = rootView.findViewById(R.id.userFullName);
        userFullname.setText(name);

        TextView eposta = rootView.findViewById(R.id.profile_mail);
        eposta.setText(email);

        /*TextView adres = headerView.findViewById(R.id.profile_loc);
        adres.setText(location);*/

       /* TextView dogumgunu = headerView.findViewById(R.id.userBirthday);
        dogumgunu.setText(birthday);*/

        /*TextView cinsiyet = headerView.findViewById(R.id.profile_gender);
        cinsiyet.setText(gender);*/

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

        return rootView;
    }
}
