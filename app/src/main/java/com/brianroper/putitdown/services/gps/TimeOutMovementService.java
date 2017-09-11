package com.brianroper.putitdown.services.gps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.brianroper.putitdown.utils.Constants;
import com.brianroper.putitdown.model.events.DrivingMessage;
import com.brianroper.putitdown.model.gps.TimeOutGpsListener;
import com.brianroper.putitdown.model.gps.TimeOutLocation;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;
import com.brianroper.putitdown.services.driving.DrivingLockService;
import com.brianroper.putitdown.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by brianroper on 9/10/17.
 */

public class TimeOutMovementService extends Service implements TimeOutGpsListener {

    private Intent mDrivingService;
    private boolean mIsDriving = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initializeDrivingService();

        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){

        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.updateSpeed(null);

        return START_STICKY;
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

        if(mIsDriving){
            if(currentSpeed > 5){
                //if current speed is greater than 5 mph do something
                startService(mDrivingService);
            }
            else if(currentSpeed < 5){
                stopService(mDrivingService);
                addSuccessfulDrivingEvent(true);
            }
        }
        else if(!mIsDriving){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsDriving = true;
                }
            }, 5000);
        }
    }

    private boolean useMetricUnits(){
        return false;
    }

    /**
     * initializes the driving service
     */
    public void initializeDrivingService(){
        mDrivingService = new Intent(this, DrivingLockService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * listens for a DrivingMessage from when it completes
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDrivingMessageEvent(DrivingMessage drivingMessage){
        Constants constants = new Constants();
        if(drivingMessage.message == constants.DRIVING_STATUS_FALSE) {
            mIsDriving = false;
        }
        if (drivingMessage.message == constants.DRIVING_STATUS_TRUE){
            mIsDriving = true;
        }
    }

    /**
     * adds the successful driving event data to the local storage
     */
    private void addSuccessfulDrivingEvent(final boolean isSuccessful){
        final Calendar calendar = Calendar.getInstance();
        Realm realm;
        Realm.init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DrivingEventLog drivingEventLog = realm.createObject(DrivingEventLog.class, Utils.returnDateAsDate().getTime() + Utils.returnDateAsDate().getTime() + "");
                drivingEventLog.setTime(Utils.returnTime(calendar));
                drivingEventLog.setDate(Utils.convertTimeStampToDate(Utils.returnDateAsDate().getTime()));
                drivingEventLog.setSuccessful(isSuccessful);
                realm.copyToRealmOrUpdate(drivingEventLog);
            }
        });
        realm.close();
    }
}
