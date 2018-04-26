package org.uusoftware.fuelify;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("User Profile");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //SETTING HEADER VEHICLE VARIABLES
        View headerView = findViewById(R.id.header_user);

        ImageView userProfileHolder = headerView.findViewById(R.id.user_picture);
        Picasso.with(this).load(Uri.parse(photo)).error(R.drawable.empty).placeholder(R.drawable.empty)
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
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

