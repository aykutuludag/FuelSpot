package org.uusoftware.fuelify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class AdminMainActivity extends AppCompatActivity {

    public static boolean isSuperVerified;
    public static int superStationID;
    public static String superStationName, superStationLocation, superStationLogo, superStationAddress, userPhoneNumber, contractPhoto;

    private TextView mTextMessage;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    public static void getSuperVariables(SharedPreferences prefs) {
        superStationID = prefs.getInt("StationID", 0);
        superStationName = prefs.getString("SuperStationName", "");
        superStationLocation = prefs.getString("SuperStationLocation", "");
        superStationAddress = prefs.getString("SuperStationAddress", "");
        superStationLogo = prefs.getString("SuperStationLogo", "");
        contractPhoto = prefs.getString("contractPhoto", "");
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
        isSuperVerified = prefs.getBoolean("Verified", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
