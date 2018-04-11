package org.uusoftware.fuelify;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static android.app.Activity.RESULT_OK;
import static org.uusoftware.fuelify.MainActivity.carBrand;
import static org.uusoftware.fuelify.MainActivity.carModel;
import static org.uusoftware.fuelify.MainActivity.carPhoto;
import static org.uusoftware.fuelify.MainActivity.fuelPri;
import static org.uusoftware.fuelify.MainActivity.fuelSec;
import static org.uusoftware.fuelify.MainActivity.kilometer;

public class FragmentVehicle extends Fragment {

    public static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
    ImageView carPhotoHolder;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicle, container, false);

        // Analytics
        Tracker t = ((AnalyticsApplication) getActivity().getApplication()).getDefaultTracker();
        t.setScreenName("Vehicle Profile");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = getActivity().getSharedPreferences("ProfileInformation", Context.MODE_PRIVATE);

        //SETTING HEADER VEHICLE VARIABLES
        View headerView = view.findViewById(R.id.header_vehicle);

        carPhotoHolder = headerView.findViewById(R.id.car_picture);
        Picasso.with(getActivity()).load(Uri.parse(carPhoto)).error(R.drawable.empty).placeholder(R.drawable.empty)
                .into(carPhotoHolder);
        carPhotoHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyStoragePermissions()) {
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(getActivity());
                } else {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
            }
        });

        //Marka-model
        TextView fullname = headerView.findViewById(R.id.carFullname);
        String fullad = carBrand + " " + carModel;
        fullname.setText(fullad);

        //Kilometre
        TextView kilometerText = headerView.findViewById(R.id.car_kilometer);
        kilometerText.setText(kilometer + " km");

        //Yakıt tipi başlangıç
        TextView fuelType = headerView.findViewById(R.id.car_fuelType);
        String fuelText;
        switch (fuelPri) {
            case 0:
                fuelText = "Gasoline";
                break;
            case 1:
                fuelText = "Diesel";
                break;
            case 2:
                fuelText = "LPG";
                break;
            case 3:
                fuelText = "Electric";
                break;
            default:
                fuelText = "-";
                break;
        }

        switch (fuelSec) {
            case 0:
                fuelText += ", Gasoline";
                break;
            case 1:
                fuelText = ", Diesel";
                break;
            case 2:
                fuelText = ", LPG";
                break;
            case 3:
                fuelText = ", Electric";
                break;
            default:
                break;
        }
        fuelType.setText(fuelText);
        //Yakıt tipi bitiş

        ImageView updateCar = headerView.findViewById(R.id.updateCarInfo);
        updateCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), VehicleSelection.class);
                startActivity(intent);
            }
        });

        return view;
    }

    public boolean verifyStoragePermissions() {
        boolean hasStorage;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int permission = ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            hasStorage = permission == PackageManager.PERMISSION_GRANTED;
        } else {
            hasStorage = true;
        }
        return hasStorage;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Settings saved...", Toast.LENGTH_SHORT).show();
                    FilePickerBuilder.getInstance().setMaxCount(1)
                            .setActivityTheme(R.style.AppTheme)
                            .pickPhoto(getActivity());
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> aq = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                String fileURI = aq.get(0);
                if (!fileURI.contains("file://")) {
                    fileURI = "file://" + fileURI;
                }
                carPhoto = fileURI;
                prefs.edit().putString("CarPhoto", carPhoto).apply();
                Picasso.with(getActivity()).load(Uri.parse(fileURI)).error(R.drawable.empty).placeholder(R.drawable.empty)
                        .into(carPhotoHolder);
            }
        }
    }
}
