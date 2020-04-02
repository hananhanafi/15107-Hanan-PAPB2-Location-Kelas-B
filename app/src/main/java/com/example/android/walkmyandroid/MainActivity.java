/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.walkmyandroid;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted{

    private static final String TRACKING_LOCATION_KEY = "TRACKING_LOCATION_KEY" ;
    private static final int REQUEST_LOCATION_PERMISSION=1;

    private Button mLocationButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;

    private boolean mTrackingLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private AnimatorSet mRotateAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mLocationButton = findViewById(R.id.button_location);
        mLocationTextView = findViewById(R.id.textview_location);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mAndroidImageView = (ImageView) findViewById(R.id.imageview_android);

        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        if (savedInstanceState!=null){
            mTrackingLocation=savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTrackingLocation){
                    startTrackingLocation();
                }else{
                    stopTrackingLocation();
                }
            }
        });



        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(mTrackingLocation){
                    new FetchAddressTask(MainActivity.this,MainActivity.this).execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void startTrackingLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
        }else{
            mTrackingLocation=true;
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(),mLocationCallback,null/*looper*/);

//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
////                            mLocationTextView.setText(
////                                    "Latitude: " + location.getLatitude()
////                                    + "\nLongitude" + location.getLongitude()
////                                    + "\nTimestamp: " + location.getTime()
////                            );
//                            new FetchAddressTask(MainActivity.this,MainActivity.this).execute(location);
//                        }
//                    });
            mLocationTextView.setText("Loading");
            mLocationButton.setText("Stop Tracking Location");
            mRotateAnim.start();
        }



    }

    private void stopTrackingLocation(){
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mLocationButton.setText("Start Tracking Location");
            mLocationTextView.setText(R.string.textview_hint);
            mRotateAnim.end();
        }

    }

    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startTrackingLocation();
                }else{
                    Toast.makeText(this,"Permision denied!",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        if (mTrackingLocation){
            mLocationTextView.setText(result);
        }
    }

    @Override
    protected void onResume() {
        if(mTrackingLocation){
            startTrackingLocation();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mTrackingLocation){
            stopTrackingLocation();
            mTrackingLocation=true;
        }

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }
}
