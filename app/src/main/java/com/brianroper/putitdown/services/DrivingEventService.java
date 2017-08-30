package com.brianroper.putitdown.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.Constants;
import com.brianroper.putitdown.model.DrivingEventLog;
import com.brianroper.putitdown.model.DrivingMessage;
import com.brianroper.putitdown.model.NeuraEventLog;
import com.brianroper.putitdown.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import org.greenrobot.eventbus.EventBus;

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
    private EventBus mEventBus = EventBus.getDefault();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        getSharedPreferences();
        if(Utils.activeNetworkCheck(this)){
            handleNeuraEventMessage(remoteMessage);
            postEventMessage();
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
            mSharedPreferences.edit().putString("currentNeuraEventId", event.getNeuraId()).apply();
            Intent drivingService = new Intent(this, DrivingService.class);
            if(!mPassengerStatus){
                if(event.getEventName().equals("userStartedDriving")){
                    startService(drivingService);
                    addNeuraEventLog(event);
                }
                else if(event.getEventName().equals("userFinishedDriving")){
                    stopService(drivingService);
                    addNeuraEventLog(event);
                    addSuccessfulDrivingEvent(event, true);
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
               neuraEventLog.setTime(Utils.returnTime(calendar));
               neuraEventLog.setDate(Utils.returnDate(calendar));
               realm.copyToRealmOrUpdate(neuraEventLog);
            }
        });
        realm.close();
    }

    /**
     * adds the successful driving event data to the local storage
     */
    private void addSuccessfulDrivingEvent(final NeuraEvent event, final boolean isSuccessful){
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
                DrivingEventLog drivingEventLog = realm.createObject(DrivingEventLog.class, event.getNeuraId());
                drivingEventLog.setTime(Utils.returnTime(calendar));
                drivingEventLog.setDate(Utils.returnDate(calendar));
                drivingEventLog.setSuccessful(isSuccessful);
                realm.copyToRealmOrUpdate(drivingEventLog);
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

    /**
     * notifies DashBoardActivity that the service has finished
     */
    private void postEventMessage(){
        Constants constants = new Constants();
        mEventBus.postSticky(new DrivingMessage(constants.DRIVING_EVENT_FINISHED));
    }
}
