package com.brianroper.putitdown.services.driving;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.utils.Utils;
import com.brianroper.putitdown.model.driving.DrivingLockScreen;

import java.util.Date;

/**
 * Created by brianroper on 5/2/17.
 */

public class DrivingLockService extends Service {

    private DrivingLockScreen mDrivingLockScreen;
    private boolean mIsNight = false;
    private int mNightTime = 19; // If it is past 7:00PM then we say it is night
    private int mMorning = 6;
    @Override
    public void onCreate() {
        super.onCreate();
        mDrivingLockScreen = new DrivingLockScreen(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            handleForegroundService();
        }
    }

    /**
     * When the service starts it asks the DrivingLockService to begin drawing the lock screen overlay
     * then it puts the device into do not disturb mode
     *
     * note: START_STICKY allows for the service to be restarted when the Android OS destroys it
     * in order to conserve memory. This ensures the service will always be active to monitor driving
     *
     * We get today's date when the user starts driving, if the hours is greater than or equal to
     * mNightTime then we set mIsNight to true. We then pass mIsNight into the startDriving method.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Date today = Utils.returnDateAsDate(); // Get today's date
        if (today.getHours() >= mNightTime &&  today.getHours() <= mMorning)
            mIsNight = true;

        mDrivingLockScreen.startDriving(mIsNight);
        Utils.silenceDevice(getApplicationContext());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //auto generated method
        return null;
    }

    /**
     * when the service is destroyed it turns device sound back on and stops the DrivingLockScreen
     */
    @Override
    public void onDestroy() {
        Utils.enableDeviceRinger(getApplicationContext());
        mDrivingLockScreen.stopDriving();
        super.onDestroy();
    }

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
        return builder.build();
    }

    @TargetApi(26)
    public void handleForegroundService(){
        startForeground(101, handlePersistentServiceNotification() );
    }
}
