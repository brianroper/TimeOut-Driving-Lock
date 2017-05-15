package com.brianroper.putitdown.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.NeuraEventLog;
import com.brianroper.putitdown.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by brianroper on 5/1/17.
 */

public class DrivingEventService extends FirebaseMessagingService {

    private SharedPreferences mSharedPreferences;
    private boolean mPassengerStatus;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        getSharedPreferences();
        if(Utils.activeNetworkCheck(this)){
            handleNeuraEventMessage(remoteMessage);
        }
        else{
            Utils.noActiveNetworkNotification(this);
        }
    }

    /**
     * handles the event message received from the Neura Api
     */
    private void handleNeuraEventMessage(RemoteMessage remoteMessage){
        Map data = remoteMessage.getData();
        if (NeuraPushCommandFactory.getInstance().isNeuraEvent(data)) {
            NeuraEvent event = NeuraPushCommandFactory.getInstance().getEvent(data);
            Intent drivingService = new Intent(this, DrivingService.class);
            if(!mPassengerStatus){
                if(event.getEventName().equals("userStartedDriving")){
                    startService(drivingService);
                    addNeuraEventLog(event);
                }
                else if(event.getEventName().equals("userFinishedDriving")){
                    stopService(drivingService);
                    addNeuraEventLog(event);
                }
            }
        }
    }

    /**
     * creates a NeuraEventLog realm object for each Neura event
     */
    private void addNeuraEventLog(final NeuraEvent event){
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
               NeuraEventLog neuraEventLog = realm.createObject(NeuraEventLog.class, event.getNeuraId());
               neuraEventLog.setEventName(event.getEventName());
               neuraEventLog.setTimestamp(event.getEventTimestamp());
               neuraEventLog.setTime(returnTime(calendar));
               neuraEventLog.setDate(returnDate(calendar));
               realm.copyToRealmOrUpdate(neuraEventLog);
            }
        });
        realm.close();
    }

    /**
     * returns default shared preferences
     */
    private void getSharedPreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPassengerStatus = mSharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false);
        Log.i("Passenger Status: ", mPassengerStatus + "");
    }

    private String returnTime(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        String time = simpleDateFormat.format(calendar.getTime());
        return time;
    }

    private String returnDate(Calendar calendar){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
        String date = simpleDateFormat.format(calendar.getTime());
        return date;
    }
}
