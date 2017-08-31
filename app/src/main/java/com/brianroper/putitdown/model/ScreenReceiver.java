package com.brianroper.putitdown.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.brianroper.putitdown.utils.Utils;

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

           /* Realm realm;
            Realm.init(context);
            RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            realm = Realm.getInstance(realmConfiguration);

            //if(realm.where(ScreenCounter.class).equalTo("id", Integer.parseInt(Utils.returnDateAsId())).findFirst())

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ScreenCounter counterData = realm.where(ScreenCounter.class).equalTo("id", 001).findFirst();
                    counterData.setCounter(counterData.getCounter() + 1);
                    realm.copyToRealmOrUpdate(counterData);
                }
            });
<<<<<<< HEAD
            realm.close(); */
=======
            realm.close();*/
>>>>>>> d85c2e02b3c92271cfab68917bfeb84028a4cca9
        }
        else{
            Log.d("ScreenStatus", "Inactive");
        }
    }
}

