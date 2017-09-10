package com.brianroper.putitdown.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.brianroper.putitdown.model.TimeOutGpsListener;
import com.brianroper.putitdown.model.TimeOutLocation;
import com.brianroper.putitdown.services.driving.DrivingService;

/**
 * Created by brianroper on 9/10/17.
 */

public class TimeOutMovementService extends Service implements TimeOutGpsListener {

    private Intent mDrivingService;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){

        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            TimeOutLocation currentLocation = new TimeOutLocation(location, this.useMetricUnits());
            this.updateSpeed(currentLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Auto-generated method stub
    }

    @Override
    public void onGpsStatusChanged(int event) {
        //Auto-generated method stub
    }

    private void updateSpeed(TimeOutLocation location){
        float currentSpeed = 0;

        if(location != null){
            location.setUseMetricUnits(this.useMetricUnits());
            currentSpeed = location.getSpeed();
        }

        if(currentSpeed > 5){
            //if current speed is greater than 5 mph do something
            startService(mDrivingService);
        }
        else if(currentSpeed < 5){
            stopService(mDrivingService);
        }
    }

    private boolean useMetricUnits(){
        return false;
    }

    /**
     * initializes the driving service
     */
    public void initializeDrivingService(){
        mDrivingService = new Intent(this, DrivingService.class);
    }
}
