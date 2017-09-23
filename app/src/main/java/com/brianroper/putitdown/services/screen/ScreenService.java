package com.brianroper.putitdown.services.screen;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.brianroper.putitdown.R;
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
        Log.i("Screen_Service", "Started");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerScreenStatusReceiver();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            handleForegroundService();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScreenStatusReceiver();
        Log.i("Screen_Service", "Stopped");
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
