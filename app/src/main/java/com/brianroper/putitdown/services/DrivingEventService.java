package com.brianroper.putitdown.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.NeuraEventLog;
import com.brianroper.putitdown.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by brianroper on 5/1/17.
 */

public class DrivingEventService extends FirebaseMessagingService {

    private SharedPreferences mSharedPreferences;

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
            if(event.getEventName().equals("userStartedDriving")){
                if(mSharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false)){
                    startService(drivingService);
                    addNeuraEventLog(event);
                }
            }
            else if(event.getEventName().equals("userFinishedDriving")){
                if(!mSharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false)){
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
    }
}
