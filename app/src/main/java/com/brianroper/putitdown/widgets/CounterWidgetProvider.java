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
    public static final String ACTION_TEXT_CHANGED = "com.brianroper.putitdown.TEXT_CHANGED";

    private String mExtra = "0";
    private AppWidgetManager mAppWidgetManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mContext = context;

        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

        mRemoteViews.setTextViewText(R.id.widget_dayofweek, Utils.returnDayOfWeek());
        mRemoteViews.setTextViewText(R.id.widget_date, Utils.returnFullDate());

        mRemoteViews.setTextViewText(R.id.widget_todays_check_count, mExtra);

        ComponentName counterWidget = new ComponentName(context, CounterWidgetProvider.class);
        mAppWidgetManager = AppWidgetManager.getInstance(context);
        mAppWidgetManager.updateAppWidget(counterWidget, mRemoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_TEXT_CHANGED)) {
            Log.i("WidgetIntent: ", "Received");

            mExtra = intent.getStringExtra("today");

            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_counter);
            view.setTextViewText(R.id.widget_todays_check_count, mExtra);
            view.setImageViewResource(R.id.widget_todays_check_status_icon, calculateUnlockSeverity(mExtra));
            AppWidgetManager
                    .getInstance(context)
                    .updateAppWidget(new ComponentName(context, CounterWidgetProvider.class), view);
        }
    }

    /**
     * calculates and returns the id for the correct icon that relates to the current unlock number
     */
    public int calculateUnlockSeverity(String extra){

        int unlocks = Integer.parseInt(extra);

        if(unlocks < 30){
            return R.mipmap.ic_device_lock_g_stage_1;
        }
        else if(unlocks >= 30 && unlocks < 60){
            return R.mipmap.ic_device_lock_g_stage_2;
        }
        else if(unlocks >= 60 && unlocks < 90){
            return R.mipmap.ic_device_lock_g_stage_3;
        }
        else if(unlocks >= 90 && unlocks < 120){
            return R.mipmap.ic_device_lock_o_stage_1;
        }
        else if(unlocks >= 120 && unlocks < 150){
            return R.mipmap.ic_device_lock_o_stage_2;
        }
        else if(unlocks >= 150 && unlocks < 180){
            return R.mipmap.ic_device_lock_o_stage_3;
        }
        else if(unlocks >= 180 && unlocks < 200){
            return R.mipmap.ic_device_lock_r_stage_1;
        }
        else if(unlocks >= 200 && unlocks < 225){
            return R.mipmap.ic_device_lock_r_stage_2;
        }
        else if(unlocks >= 225){
            return R.mipmap.ic_device_lock_r_stage_3;
        }
        else{
            return R.drawable.ic_device_lock;
        }
    }
}
