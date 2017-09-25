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

            mExtra = intent.getStringExtra("today");

            String prev1Day = intent.getStringExtra("PrevDay1Day");
            String prev1Count = intent.getStringExtra("PrevDay1Count");

            String prev2Day = intent.getStringExtra("PrevDay2Day");
            String prev2Count = intent.getStringExtra("PrevDay2Count");

            String prev3Day = intent.getStringExtra("PrevDay3Day");
            String prev3Count = intent.getStringExtra("PrevDay3Count");

            String prev4Day = intent.getStringExtra("PrevDay4Day");
            String prev4Count = intent.getStringExtra("PrevDay4Count");

            String prev5Day = intent.getStringExtra("PrevDay5Day");
            String prev5Count = intent.getStringExtra("PrevDay5Count");

            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

            view.setTextViewText(R.id.widget_todays_check_count, mExtra);
            view.setImageViewResource(R.id.widget_todays_check_status_icon, calculateUnlockSeverity(mExtra));

            view.setTextViewText(R.id.slot_5_day, prev1Day);
            if(prev1Count != null){
                view.setTextViewText(R.id.slot_5_check_count, prev1Count);
                view.setImageViewResource(R.id.slot_5_status_icon, calculateUnlockSeverity(prev1Count));
            }
            else{
                view.setTextViewText(R.id.slot_5_check_count, "0");
                view.setImageViewResource(R.id.slot_5_status_icon, R.drawable.ic_device_lock);
            }

            view.setTextViewText(R.id.slot_4_day, prev2Day);
            if(prev2Count != null){
                view.setTextViewText(R.id.slot_4_check_count, prev2Count);
                view.setImageViewResource(R.id.slot_4_status_icon, calculateUnlockSeverity(prev2Count));
            }
            else{
                view.setTextViewText(R.id.slot_4_check_count, "0");
                view.setImageViewResource(R.id.slot_4_status_icon, R.drawable.ic_device_lock);
            }

            view.setTextViewText(R.id.slot_3_day, prev3Day);
            if(prev3Count != null){
                view.setTextViewText(R.id.slot_3_check_count, prev3Count);
                view.setImageViewResource(R.id.slot_3_status_icon, calculateUnlockSeverity(prev3Count));
            }
            else{
                view.setTextViewText(R.id.slot_3_check_count, "0");
                view.setImageViewResource(R.id.slot_3_status_icon, R.drawable.ic_device_lock);
            }

            view.setTextViewText(R.id.slot_2_day, prev4Day);
            if(prev4Count != null){
                view.setTextViewText(R.id.slot_2_check_count, prev4Count);
                view.setImageViewResource(R.id.slot_2_status_icon, calculateUnlockSeverity(prev4Count));
            }
            else{
                view.setTextViewText(R.id.slot_2_check_count, "0");
                view.setImageViewResource(R.id.slot_2_status_icon, R.drawable.ic_device_lock);
            }

            view.setTextViewText(R.id.slot_1_day, prev5Day);
            if(prev5Count != null){
                view.setTextViewText(R.id.slot_1_check_count, prev5Count);
                view.setImageViewResource(R.id.slot_1_status_icon, calculateUnlockSeverity(prev5Count));
            }
            else{
                view.setTextViewText(R.id.slot_1_check_count, "0");
                view.setImageViewResource(R.id.slot_1_status_icon, R.drawable.ic_device_lock);
            }

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
        else if(unlocks == 0){
            return R.drawable.ic_device_lock;
        }
        else{
            return R.drawable.ic_device_lock;
        }
    }
}
