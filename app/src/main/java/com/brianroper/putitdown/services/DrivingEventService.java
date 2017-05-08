package com.brianroper.putitdown.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.NeuraEventLog;
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
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map data = remoteMessage.getData();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (NeuraPushCommandFactory.getInstance().isNeuraEvent(data)) {
            NeuraEvent event = NeuraPushCommandFactory.getInstance().getEvent(data);
            Log.i(getClass().getSimpleName(), "received Neura event - " + event.toString());
            Intent drivingService = new Intent(this, DrivingService.class);
            //handles user started driving event
            if(event.getEventName().equals("userStartedDriving")){
                if(sharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false)){
                    startService(drivingService);
                    addNeuraEventLog(event);
                }
            }
            //handles user finished driving event
            else if(event.getEventName().equals("userFinishedDriving")){
                if(!sharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false)){
                    stopService(drivingService);
                    addNeuraEventLog(event);
                }
            }
        }
    }

    /**
     * creates a NeuraEventLog realm object for each Neura event
     */
    public void addNeuraEventLog(final NeuraEvent event){
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
}
