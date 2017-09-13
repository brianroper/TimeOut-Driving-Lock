package com.brianroper.putitdown.services.screen;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.brianroper.putitdown.receivers.ScreenReceiver;

/**
 * Created by brianroper on 8/10/17.
 */

public class ScreenService extends Service {

    private ScreenReceiver mScreenReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //auto generated method
        return null;
    }

    /**
     * note: START_STICKY allows for the service to be restarted when the Android OS destroys it
     * in order to conserve memory. This ensures the service will always be active to monitor screen activity
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerScreenStatusReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScreenStatusReceiver();
    }

    /**
     * registers the ScreenStatusReceiver, broadcast receiver to this service
     */
    private void registerScreenStatusReceiver() {
        mScreenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mScreenReceiver, filter);
    }

    /**
     * un-registers the ScreenStatusReceiver, broadcast receiver from this service
     */
    private void unregisterScreenStatusReceiver() {
        try {
            if (mScreenReceiver != null) {
                unregisterReceiver(mScreenReceiver);
            }
        } catch (IllegalArgumentException e) {}
    }
}
