package com.brianroper.putitdown.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.realmObjects.ScreenCounter;
import com.brianroper.putitdown.utils.Utils;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by brianroper on 8/10/17.
 */

public class CounterWidgetProvider extends AppWidgetProvider{

    private ScreenCounter mScreenCounter;
    private RealmResults<ScreenCounter> mRealmResults;
    private RemoteViews mRemoteViews;
    private Context mContext;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mContext = context;

        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

        mRemoteViews.setTextViewText(R.id.widget_dayofweek, Utils.returnDayOfWeek());
        mRemoteViews.setTextViewText(R.id.widget_date, Utils.returnFullDate());


        try {
            getTodayCounterDataFromRealm();
            populateAllViews(mRemoteViews);

        }catch (Exception e){
            e.printStackTrace();
        }

        ComponentName counterWidget = new ComponentName(context, CounterWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(counterWidget, mRemoteViews);
    }

    /**
     * gets todays screen counter data from realm
     */
    public ScreenCounter getTodayCounterDataFromRealm(){
        Realm realm;
        Realm.init(mContext);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);
        ScreenCounter screenCounter = realm
                .where(ScreenCounter.class)
                .equalTo("id", "001")
                .findFirst();
        Log.i("Counter: ", screenCounter.getCounter()+"");
        realm.close();
        return screenCounter;
    }

    public void populateAllViews(RemoteViews remoteViews){
        remoteViews.setTextViewText(R.id.widget_todays_check_count, mScreenCounter.getCounter() + "");
    }
}
