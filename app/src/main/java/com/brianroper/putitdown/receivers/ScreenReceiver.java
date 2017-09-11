package com.brianroper.putitdown.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.brianroper.putitdown.model.realmObjects.ScreenCounter;
import com.brianroper.putitdown.utils.Utils;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by brianroper on 8/10/17.
 */

public class ScreenReceiver extends BroadcastReceiver {

    private Context mContext;

    /**
     * Listens for screen on and off activity
     *
     * When the device is off we do nothing.
     * When the device is powered on we update the counter total in Realm
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("ScreenStatus", "Screen Off");

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d("ScreenStatus", "Screen On");

            updateCounterInRealm();
        }
        else{
            Log.d("ScreenStatus", "Inactive");
        }
    }

    /**
     * creates a new ScreenCounter object in realm when one does not exist. If one does exisit
     * we update the existing value with the new one.
     *
     * to store in realm we are using the utils method return date as id which returns a string in the
     * mmyyyydd format for today's date
     */
    public void updateCounterInRealm(){
        Realm realm;
        Realm.init(mContext);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                try{
                    ScreenCounter screenCounter = realm.where(ScreenCounter.class).equalTo("id", Utils.returnDateAsId()).findFirst();

                    if(screenCounter != null){
                        ScreenCounter counterData = realm.where(ScreenCounter.class).equalTo("id", Utils.returnDateAsId()).findFirst();
                        counterData.setCounter(counterData.getCounter() + 1);
                        realm.copyToRealmOrUpdate(counterData);
                    }
                }
                catch (Exception e){

                    Log.i("ScreenCount: ", "null");

                    ScreenCounter screenCounter = realm.createObject(ScreenCounter.class, Utils.returnDateAsId());
                    screenCounter.setCounter(1);
                    realm.copyToRealmOrUpdate(screenCounter);
                }
            }
        });
        realm.close();
    }
}

