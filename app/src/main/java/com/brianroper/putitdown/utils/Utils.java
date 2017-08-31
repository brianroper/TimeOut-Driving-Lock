package com.brianroper.putitdown.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.brianroper.putitdown.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.AUDIO_SERVICE;

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

    /**
     * error notification displayed to user when there is no available network
     */
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

    /**
     * formats and returns the current time in h:mm a format
     */
    static public String returnTime(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        String time = simpleDateFormat.format(calendar.getTime());
        return time;
    }

    /**
     * formats and returns the current date in MM/dd format
     */
    static public String returnDate(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }

    /**
     * convert the NeuraEventData timestamp into a date object
     */
    static public Date formatTimeStamp(long timestamp){
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy");
        simpleDateFormat.setTimeZone(timeZone);
        String localTime = simpleDateFormat.format(new Date(timestamp * 1000));
        Date date = new Date();
        try{
            date = simpleDateFormat.parse(localTime);
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        return date;
    }

    /**
     * set the device ringer to normal
     */
    static public void enableDeviceRinger(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    /**
     * silence the device audio and vibration
     */
    static public void silenceDevice(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    /**
     * returns the date in the yyyy M dd format
     */
    static public String returnFullDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        String formattedDate = dateFormat.format(calendar.getTime());
        return formattedDate;
    }

    /**
     * returns the day of the week
     */
    static public String returnDayOfWeek(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
        String formattedDate = dateFormat.format(calendar.getTime());
        return formattedDate;
    }

    /**
     * returns the date in an id format
     */
    static public String returnDateAsId(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        String formattedDate = dateFormat.format(calendar.getTime());
        return formattedDate;
    }
}
