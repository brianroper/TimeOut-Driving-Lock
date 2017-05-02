package com.brianroper.putitdown;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

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
        Log.i("Driving Status: ", "Started");
        mDrivingView.startDriving();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mDrivingView.stopDriving();
        super.onDestroy();
    }
}
