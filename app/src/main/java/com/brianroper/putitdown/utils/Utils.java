package com.brianroper.putitdown.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.brianroper.putitdown.R;

/**
 * Created by brianroper on 5/1/17.
 */

public class Utils {
    public static final String STARTED_DRIVING = "userStartedDriving";
    public static final String FINISHED_DRIVING = "userFinishedDriving";

    /**
     * error toast displayed to user when there is no available network
     */
    static public void noActiveNetworkToast(Context context){
        Toast.makeText(context, context.getString(R.string.no_active_network), Toast.LENGTH_SHORT).show();
    }

    static public void noActiveNetworkNotification(Context context){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setContentTitle(context.getString(R.string.no_active_network_notification_title))
                .setContentText(context.getString(R.string.no_active_network_notification_content));

        int NOTIFICATION_ID = 1;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * check the device for an active network connection
     */
    static public boolean activeNetworkCheck(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
