package com.brianroper.putitdown.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

    private RemoteViews mRemoteViews;
    private Context mContext;
    public static final String ACTION_TEXT_CHANGED = "com.brianroper.putitdown.CounterWidgetProvider.TEXT_CHANGED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mContext = context;

        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

        mRemoteViews.setTextViewText(R.id.widget_dayofweek, Utils.returnDayOfWeek());
        mRemoteViews.setTextViewText(R.id.widget_date, Utils.returnFullDate());

        ComponentName counterWidget = new ComponentName(context, CounterWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(counterWidget, mRemoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_TEXT_CHANGED)) {
            Log.i("WidgetIntent: ", "Received");

            String extra = intent.getStringExtra("today");
            mRemoteViews.setTextViewText(R.id.widget_todays_check_count, extra);
        }
    }
}
