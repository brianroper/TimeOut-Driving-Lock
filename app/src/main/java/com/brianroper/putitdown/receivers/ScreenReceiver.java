package com.brianroper.putitdown.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.brianroper.putitdown.model.Constants;
import com.brianroper.putitdown.model.events.ScreenMessage;
import com.brianroper.putitdown.model.realmObjects.ScreenCounter;
import com.brianroper.putitdown.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by brianroper on 8/10/17.
 */

public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("ScreenStatus", "Screen Off");

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d("ScreenStatus", "Screen On");

            Realm realm;
            Realm.init(context);
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            realm = Realm.getInstance(realmConfiguration);

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    try{
                        ScreenCounter screenCounter = realm.where(ScreenCounter.class).equalTo("id", "001").findFirst();

                        if(screenCounter != null){
                            ScreenCounter counterData = realm.where(ScreenCounter.class).equalTo("id", "001").findFirst();
                            counterData.setCounter(counterData.getCounter() + 1);
                            realm.copyToRealmOrUpdate(counterData);
                        }
                    }
                    catch (Exception e){

                        Log.i("ScreenCount: ", "null");

                        ScreenCounter screenCounter = realm.createObject(ScreenCounter.class, "001");
                        screenCounter.setCounter(1);
                        realm.copyToRealmOrUpdate(screenCounter);
                    }
                }
            });
            realm.close();
        }
        else{
            Log.d("ScreenStatus", "Inactive");
        }
    }
}

