package com.brianroper.putitdown.receivers;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import android.util.Log;

import com.brianroper.putitdown.model.realmObjects.ScreenCounter;
import com.brianroper.putitdown.utils.Utils;
import com.brianroper.putitdown.widgets.CounterWidgetProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by brianroper on 8/10/17.
 */

public class ScreenReceiver extends BroadcastReceiver {

    private Context mContext;
    private RealmResults<ScreenCounter> mRealmResults;

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
            getCounterInRealm();

            handleScreenCountData();
            handlePreviousScreenCounts();
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
    public void updateCounterInRealm() {
        Realm realm;
        Realm.init(mContext);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                try {
                    ScreenCounter screenCounter = realm.where(ScreenCounter.class).equalTo("id", Utils.returnDateAsId()).findFirst();

                    ScreenCounter counterData = realm.where(ScreenCounter.class).equalTo("id", Utils.returnDateAsId()).findFirst();
                    counterData.setCounter(counterData.getCounter() + 1);
                    screenCounter.setDate(Utils.returnDateAsDate());
                    realm.copyToRealmOrUpdate(counterData);
                    //sendCounterBroadcastToWidget(counterData);

                } catch (Exception e) {
                    ScreenCounter screenCounter = realm.createObject(ScreenCounter.class, Utils.returnDateAsId());
                    screenCounter.setCounter(1);
                    screenCounter.setDate(Utils.returnDateAsDate());
                    realm.copyToRealmOrUpdate(screenCounter);
                    //sendCounterBroadcastToWidget(screenCounter);
                }
            }
        });
        realm.close();
    }

    public void getCounterInRealm(){
        Realm realm;
        Realm.init(mContext);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mRealmResults = realm.where(ScreenCounter.class).findAll();
            }
        });
        realm.close();
    }

    public void handleScreenCountData(){
        handleTodayScreenCount();
    }

    public void handleTodayScreenCount(){
        for (int i = 0; i < mRealmResults.size(); i++) {
            Calendar calender = Calendar.getInstance();
            String todayStringDate = Utils.returnDateAsString(calender);
            String storedDate = Utils.returnDateStringFromDate(mRealmResults.get(i).getDate());

            Log.i("TodaysDate: ", todayStringDate);
            Log.i("StoredDate: ", storedDate);

            if(todayStringDate.equals(storedDate)){
                Log.i("TodaysCount: ", mRealmResults.get(i).getCounter() + "");
                sendDataChangedBroadcast("today", mRealmResults.get(i).getCounter());
            }
        }
    }

    /**
     * gets the previous 5 days worth of results
     */
    public void handlePreviousScreenCounts(){
        Calendar calendar = Calendar.getInstance();
        int todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        for (int i = 0; i < mRealmResults.size(); i++) {
            int previousDayOfYear = Utils.returnDayOfYearFromDate(mRealmResults.get(i).getDate());

            if(previousDayOfYear == todayDayOfYear - 1){
                sendPrevDataChangedBroadcast("PrevDay1Count", mRealmResults.get(i).getCounter());
            }
            else if(previousDayOfYear == todayDayOfYear - 2){
                sendPrevDataChangedBroadcast("PrevDay2Count", mRealmResults.get(i).getCounter());
            }
            else if(previousDayOfYear == todayDayOfYear - 3){
                sendPrevDataChangedBroadcast("PrevDay3Count", mRealmResults.get(i).getCounter());
            }
            else if(previousDayOfYear == todayDayOfYear - 4){
                sendPrevDataChangedBroadcast("PrevDay4Count", mRealmResults.get(i).getCounter());
            }
            else if(previousDayOfYear == todayDayOfYear - 5){
                sendPrevDataChangedBroadcast("PrevDay5Count", mRealmResults.get(i).getCounter());
            }
        }
    }

    public void sendDataChangedBroadcast(String key, int extra){
        Intent intent = new Intent(CounterWidgetProvider.ACTION_TEXT_CHANGED);
        intent.putExtra(key, extra);
        mContext.sendBroadcast(intent);
        Log.i("WidgetIntent: ", "Sent");
    }

    public void sendPrevDataChangedBroadcast(String key, int extra){
        Intent intent = new Intent(CounterWidgetProvider.ACTION_PREVIOUS_CHANGED);
        intent.putExtra(key, extra);
        mContext.sendBroadcast(intent);
        Log.i("WidgetIntent: ", "Sent");
    }
}

