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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import static org.uusoftware.fuelify.MainActivity.birthday;
import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.email;
import static org.uusoftware.fuelify.MainActivity.gender;
import static org.uusoftware.fuelify.MainActivity.location;
import static org.uusoftware.fuelify.MainActivity.name;
import static org.uusoftware.fuelify.MainActivity.photo;
import static org.uusoftware.fuelify.MainActivity.username;

public class FragmentProfile extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("User Profile");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //SETTING HEADER VEHICLE VARIABLES
        View headerView = view.findViewById(R.id.header_user);

        ImageView userProfileHolder = headerView.findViewById(R.id.user_picture);
        Picasso.with(getActivity()).load(Uri.parse(photo)).error(R.drawable.empty).placeholder(R.drawable.empty)
                .into(userProfileHolder);

        TextView userName = headerView.findViewById(R.id.username);
        userName.setText(username);

        TextView userFullname = headerView.findViewById(R.id.userFullName);
        userFullname.setText(name);

        TextView eposta = headerView.findViewById(R.id.profile_mail);
        eposta.setText(email);

        TextView adres = headerView.findViewById(R.id.profile_loc);
        adres.setText(location);

        TextView dogumgunu = headerView.findViewById(R.id.userBirthday);
        dogumgunu.setText(birthday);

        TextView cinsiyet = headerView.findViewById(R.id.profile_gender);
        cinsiyet.setText(gender);

        TextView fullCarName = headerView.findViewById(R.id.profile_CarName);
        String fullad = carBrand + " " + carModel;
        fullCarName.setText(fullad);

        ImageView updateUser = headerView.findViewById(R.id.updateUserInfo);
        updateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}

