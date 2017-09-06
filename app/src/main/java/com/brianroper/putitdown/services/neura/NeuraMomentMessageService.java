package com.brianroper.putitdown.services.neura;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.Constants;
import com.brianroper.putitdown.model.events.DrivingMessage;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;
import com.brianroper.putitdown.services.driving.DrivingService;
import com.brianroper.putitdown.utils.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraEventCallBack;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by brianroper on 9/5/17.
 */

public class NeuraMomentMessageService extends FirebaseMessagingService {

    private SharedPreferences mSharedPreferences;
    private boolean mPassengerStatus;
    private EventBus mEventBus = EventBus.getDefault();
    private boolean mIsDriving = true;
    private Intent mDrivingService;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i("EventStatus: ", "Received");

        getSharedPreferences();

        if(Utils.activeNetworkCheck(this)){
            handleNeuraMoment(remoteMessage);
            postEventMessage();
        }
        else{
            Utils.noActiveNetworkNotification(this);
        }
    }

    /**
     * performs an action based on the incoming Neura moment message
     */
    public void handleNeuraMoment(RemoteMessage remoteMessage){

        initializeDrivingService();

        boolean isNeuraPush = NeuraPushCommandFactory.getInstance()
                .isNeuraPush(getApplicationContext(), remoteMessage.getData(), new NeuraEventCallBack() {
                    @Override
                    public void neuraEventDetected(NeuraEvent event) {
                        String eventText = event != null ? event.toString() : "couldn't parse data";
                        Log.i(getClass().getSimpleName(), "received Neura event - " + eventText);

                        mSharedPreferences.edit().putString("currentNeuraEventId", event.getNeuraId()).apply();

                        //all driving related moments, events are ignored if passenger mode is enabled
                        if(!mPassengerStatus){
                            if(event.getEventName().equals("userStartedDriving")){
                                startService(mDrivingService);
                                mIsDriving = true;
                            }
                            else if(event.getEventName().equals("userFinishedDriving")){
                                if(mIsDriving == true){
                                    stopService(mDrivingService);
                                    addSuccessfulDrivingEvent(event, true);
                                    mIsDriving = false;
                                }
                            }
                            else if(event.getEventName().equals("userStartedRunning")){
                                if(mIsDriving == true){
                                    stopService(mDrivingService);
                                    addSuccessfulDrivingEvent(event, true);
                                    mIsDriving = false;
                                }
                            }
                            else if(event.getEventName().equals("userStartedWalking")){
                                if(mIsDriving == true){
                                    stopService(mDrivingService);
                                    addSuccessfulDrivingEvent(event, true);
                                    mIsDriving = false;
                                }
                            }
                        }
                    }
                });

        if(!isNeuraPush) {
            //Handle non neura push here
        }
    }

    /**
     * initializes the driving service
     */
    public void initializeDrivingService(){
        mDrivingService = new Intent(this, DrivingService.class);
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
                DrivingEventLog drivingEventLog = realm.createObject(DrivingEventLog.class, event.getNeuraId() + event.getEventTimestamp());
                drivingEventLog.setTime(Utils.returnTime(calendar));
                drivingEventLog.setDate(Utils.convertTimeStampToDate(event.getEventTimestamp()));
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

    /**
     * listens for an interrupted driving event
     * when the user unlocks the device while driving this notifies this service
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDrivingMessageEvent(DrivingMessage drivingMessage){
        Constants constants = new Constants();
        if(drivingMessage.message == constants.DRIVING_EVENT_STOPPED) {
            mIsDriving = false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}