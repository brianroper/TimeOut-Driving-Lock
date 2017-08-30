package com.brianroper.putitdown.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.brianroper.putitdown.utils.Utils;
import com.brianroper.putitdown.views.DrivingView;

/**
 * Created by brianroper on 5/2/17.
 */

public class DrivingService extends Service {

    private DrivingView mDrivingView;

    @Override
    public void onCreate() {
        super.onCreate();
        mDrivingView = new DrivingView(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //draw screen overlay
        mDrivingView.startDriving();
        Utils.silenceDevice(getApplicationContext());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //destroy screen overlay
        Utils.enableDeviceRinger(getApplicationContext());
        mDrivingView.stopDriving();
        super.onDestroy();
    }
}
