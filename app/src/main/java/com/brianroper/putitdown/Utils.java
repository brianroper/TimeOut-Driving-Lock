package com.brianroper.putitdown;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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
