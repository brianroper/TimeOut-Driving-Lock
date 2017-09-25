package com.brianroper.putitdown.services.gps;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.events.PreferenceMessage;
import com.brianroper.putitdown.utils.Constants;
import com.brianroper.putitdown.model.events.DrivingMessage;
import com.brianroper.putitdown.model.gps.TimeOutGpsListener;
import com.brianroper.putitdown.model.gps.TimeOutLocation;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;
import com.brianroper.putitdown.services.driving.DrivingLockService;
import com.brianroper.putitdown.utils.Utils;
import com.brianroper.putitdown.views.DrivingLogActivity;

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

    private int DRIVING_LOCKOUT_RETRY_TIME = 5000;
    private int DRIVING_STOPPED_DOUBLE_CHECK_TIME = 30000;
    private int TARGET_LOCKOUT_SPEED = 5;
    private int TARGET_STOPPED_SPEED = 0;

    private Intent mDrivingService;
    private boolean mIsUnlocked = false;
    private boolean mIsDriving = false;

    private boolean mIsPassengerMode = false;

    private float mCurrentSpeed = 0;

    private int[] mDrivingModeSpeeds = {2, 4, 6};
    private int[] mLockOutTimes = {15000, 30000, 45000};

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("GPS_Service: ", "Started");

        returnSharedPreferences();
        initializeDrivingService();

        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        //permissions check is required before accessing locationManager.requestLocationUpdates()
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
        //auto generated service method
        return null;
    }

    /**
     * When the device location is changed we create a new Location object and update its speed
     */
    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            TimeOutLocation currentLocation = new TimeOutLocation(location, this.useMetricUnits());
            this.updateSpeed(currentLocation);
            //Log.i("CurrentSpeed: ", currentLocation + "");
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

    /**
     * updates the device speed according to gps sensor data
     *
     * if the users speed is above 5mph we begin the lockout,
     * when the users device drops below 5mph it unlocks,
     * if the user manually unlocks their device we wait 5000ms and then re start the driving lockout
     *
     * when speed hits 0mph we wait 30 seconds and do a second check to see if speed is still 0mph.
     * if this check passes we log a successful driving session. we are assuming here that the vehicle
     * is parked and is no longer driving.
     *
     * boolean mIsDriving is used to determine if the user has indeed stopped driving. It is set to
     * true when the user goes above 5mph and is set to false after the double 0mph check returns
     * successful. This prevents inaccurate log reporting due to the user consistently staying at
     * 0mph when not driving.
     *
     * boolean mIsPassengerMode is used to determine if passenger mode is enabled. If so we ignore the
     * speed activity until it is disabled
     */
    private void updateSpeed(TimeOutLocation location){
        mCurrentSpeed = 0;

        if(location != null){
            location.setUseMetricUnits(this.useMetricUnits());
            mCurrentSpeed = location.getSpeed();
        }

        if(!mIsPassengerMode){
            if(!mIsUnlocked){
                if(mCurrentSpeed >= TARGET_LOCKOUT_SPEED){
                    //if current speed is greater than 5 mph do something
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        handleAndroidOService(mDrivingService, true);
                    }
                    else{
                        startService(mDrivingService);
                    }
                    mIsDriving = true;
                    Log.i("Driving: ", "User has started driving above 5mph");
                }
                if(mCurrentSpeed < TARGET_LOCKOUT_SPEED && mCurrentSpeed != TARGET_STOPPED_SPEED){
                    if(mIsDriving){
                        stopService(mDrivingService);
                        Log.i("Driving: ", "User has started driving below 5mph");
                    }
                }
                if(mCurrentSpeed == TARGET_STOPPED_SPEED){
                    Log.i("Driving: ", "User has stopped driving");
                    if(mIsDriving){
                        //check after set seconds if speed is still 0. If so we log a successful driving session
                        mIsDriving = false;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handleStoppedDriving();
                            }
                        }, DRIVING_STOPPED_DOUBLE_CHECK_TIME);
                    }
                }
            }
        }
    }

    /**
     * checks to see if the user has actually stopped driving
     */
    public void handleStoppedDriving(){
        if (mCurrentSpeed == TARGET_STOPPED_SPEED){
            addSuccessfulDrivingEvent(true);
            sendSuccessNotification();
            Constants constants = new Constants();
            EventBus.getDefault().postSticky(new DrivingMessage(constants.DRIVING_LOG_EVENT_SUCCESS));
        }
        else{
            mIsDriving = true;
        }
    }

    public void handleRestartSession(){
        mIsUnlocked = true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsUnlocked = false;
                Constants constants = new Constants();
                EventBus.getDefault().postSticky(new DrivingMessage(constants.DRIVING_LOG_EVENT_FAILED));
            }
        }, DRIVING_LOCKOUT_RETRY_TIME);
    }

    /**
     * determines if we want to use metric units
     */
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            handleForegroundServiceStart();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Log.i("GPS_Service: ", "Destroyed");
    }

    /**
     * listens for a DrivingMessage from when it completes
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDrivingMessageEvent(DrivingMessage drivingMessage){
        Constants constants = new Constants();
        if(drivingMessage.message == constants.UNLOCK_STATUS_FALSE) {

        }
        if (drivingMessage.message == constants.UNLOCK_STATUS_TRUE){
            handleRestartSession();
        }
    }

    /**
     *
     * listens for a change to sharedPreferences
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDrivingMessageEvent(PreferenceMessage preferenceMessage){
        Constants constants = new Constants();
        if (preferenceMessage.equals("Changed"))
            returnSharedPreferences();
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
                DrivingEventLog drivingEventLog = realm.createObject(DrivingEventLog.class, Utils.returnDateAsDate().getTime() + "");
                drivingEventLog.setTime(Utils.returnTime(calendar));
                drivingEventLog.setDate(Utils.returnDateAsDate());
                drivingEventLog.setSuccessful(isSuccessful);
                realm.copyToRealmOrUpdate(drivingEventLog);
                EventBus.getDefault().postSticky(new DrivingMessage("newLog"));
            }
        });
        realm.close();
    }

    /**
     * sends a notification to the user when they complete a safe driving session
     */
    private void sendSuccessNotification(){

        //notification will open the DrivingLogActivity when clicked
        Intent logIntent = new Intent(getApplicationContext(), DrivingLogActivity.class);
        logIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, logIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_trip_success)
                        .setContentTitle(
                                getApplicationContext()
                                        .getResources()
                                        .getString(R.string.notification_success_title))
                        .setContentText(
                                getApplicationContext()
                                        .getResources()
                                        .getString(R.string.notification_success_content))
                        .addAction(R.drawable.redcar,
                                getApplicationContext().getString(R.string.notification_success_button),
                                pendingIntent);

        //shows notification text on the status bar when received
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);

        //sends the notification
        NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(002, builder.build());
    }

    /**
     * returns the current shared preferences
     */
    public void returnSharedPreferences(){
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mIsPassengerMode = sharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false);

        int drivingMode = sharedPreferences.getInt("driveModeOption", 0);
        TARGET_LOCKOUT_SPEED = mDrivingModeSpeeds[drivingMode];

        int lockOutTime = sharedPreferences.getInt("lockOutTime", 0);
        DRIVING_STOPPED_DOUBLE_CHECK_TIME = mLockOutTimes[lockOutTime];
    }

    /**
     * listens for changes to the passenger mode preference while service is running
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPreferenceMessageEvent(PreferenceMessage preferenceMessage){
        Constants constants = new Constants();
        if(preferenceMessage.message.equals(constants.PREFERENCE_TRUE)) {
            mIsPassengerMode = true;
        }
        else if(preferenceMessage.message.equals(constants.PREFERENCE_FALSE)){
            mIsPassengerMode = false;
        }
    }

    /**
     * ANDROID O
     */

    /**
     * handles persistent notification for Android O requirements,
     * having a "persistent" notification that is always visible to the user
     * allows the service to run since the app qualifies as in the foreground, since
     * a component of the app is visible.
     */
    public Notification handlePersistentServiceNotification(){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.redcar)
                        .setContentTitle("TimeOut")
                        .setContentText(
                                "TimeOut is monitoring your driving patterns");

        //shows notification text on the status bar when received
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager;

        return builder.build();
    }

    @TargetApi(26)
    public void handleForegroundServiceStart(){
        startForeground(101, handlePersistentServiceNotification() );
    }

    /**
     * handles the new service system for android O
     * specifies that this code is targeted for API 26
     */
    @TargetApi(26)
    public void handleAndroidOService(Intent service, boolean action){
        if(action){
            getApplicationContext().startForegroundService(service);
        }
        else if(!action){
            stopForeground(true);
        }
        Log.i("AndroidVersion: ", "Oreo");
    }

    /**
     * END OF ANDROID O
     */
}
