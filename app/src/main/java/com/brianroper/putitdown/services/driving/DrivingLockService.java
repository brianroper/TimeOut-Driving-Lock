package com.brianroper.putitdown.services.driving;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.brianroper.putitdown.utils.Utils;
import com.brianroper.putitdown.model.driving.DrivingLockScreen;

/**
 * Created by brianroper on 5/2/17.
 */

public class DrivingLockService extends Service {

    private DrivingLockScreen mDrivingLockScreen;

    @Override
    public void onCreate() {
        super.onCreate();
        mDrivingLockScreen = new DrivingLockScreen(this);
    }

    /**
     * When the service starts it asks the DrivingLockService to begin drawing the lock screen overlay
     * then it puts the device into do not disturb mode
     *
     * note: START_STICKY allows for the service to be restarted when the Android OS destroys it
     * in order to conserve memory. This ensures the service will always be active to monitor driving
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDrivingLockScreen.startDriving();
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
}
