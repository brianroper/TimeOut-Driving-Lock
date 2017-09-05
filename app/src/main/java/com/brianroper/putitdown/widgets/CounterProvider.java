package com.brianroper.putitdown.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
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

public class CounterProvider  extends AppWidgetProvider{

    private ScreenCounter mScreenCounter;
    private RealmResults<ScreenCounter> mRealmResults;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

        remoteViews.setTextViewText(R.id.widget_dayofweek, Utils.returnDayOfWeek());
        remoteViews.setTextViewText(R.id.widget_date, Utils.returnFullDate());

        try{
            remoteViews.setTextViewText(R.id.widget_todays_check_count, getTodayCounterDataFromRealm().getCounter() + "");
        }catch (Exception e){
            e.printStackTrace();
        }

        ComponentName counterWidget = new ComponentName(context, CounterProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(counterWidget, remoteViews);
    }

    /**
     * gets todays screen counter data from realm
     */
    public ScreenCounter getTodayCounterDataFromRealm(){
        Realm realm;
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);
        return realm
                .where(ScreenCounter.class)
                .equalTo("id", Integer.parseInt(Utils.returnDateAsId()))
                .findFirst();
    }
}
