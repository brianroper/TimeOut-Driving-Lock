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

    private int mExtra = 0;
    private AppWidgetManager mAppWidgetManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mContext = context;

        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

        String dayOfWeek = Utils.returnDayOfWeek();

        mRemoteViews.setTextViewText(R.id.widget_dayofweek, Utils.returnDayOfWeek());
        mRemoteViews.setTextViewText(R.id.widget_date, Utils.returnFullDate());

        setPreviousDays(dayOfWeek);

        mRemoteViews.setTextViewText(R.id.widget_todays_check_count, mExtra + "");

        ComponentName counterWidget = new ComponentName(context, CounterWidgetProvider.class);
        mAppWidgetManager = AppWidgetManager.getInstance(context);
        mAppWidgetManager.updateAppWidget(counterWidget, mRemoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_TEXT_CHANGED)) {

            mExtra = intent.getIntExtra("today", 0);

            int prev1Count = intent.getIntExtra("PrevDay1Count", 0);
            int prev2Count = intent.getIntExtra("PrevDay2Count", 0);
            int prev3Count = intent.getIntExtra("PrevDay3Count", 0);
            int prev4Count = intent.getIntExtra("PrevDay4Count", 0);
            int prev5Count = intent.getIntExtra("PrevDay5Count", 0);

            RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

            view.setTextViewText(R.id.widget_todays_check_count, mExtra + "");
            view.setImageViewResource(R.id.widget_todays_check_status_icon, calculateUnlockSeverity(mExtra));

            if(prev1Count != 0){
                view.setTextViewText(R.id.slot_5_check_count, prev1Count + "");
                view.setImageViewResource(R.id.slot_5_status_icon, calculateUnlockSeverity(prev1Count));
            }
            else{
                view.setTextViewText(R.id.slot_5_check_count, "0");
                view.setImageViewResource(R.id.slot_5_status_icon, R.drawable.ic_device_lock);
            }

            if(prev2Count != 0){
                view.setTextViewText(R.id.slot_4_check_count, prev2Count + "");
                view.setImageViewResource(R.id.slot_4_status_icon, calculateUnlockSeverity(prev2Count));
            }
            else{
                view.setTextViewText(R.id.slot_4_check_count, "0");
                view.setImageViewResource(R.id.slot_4_status_icon, R.drawable.ic_device_lock);
            }

            if(prev3Count != 0){
                view.setTextViewText(R.id.slot_3_check_count, prev3Count + "");
                view.setImageViewResource(R.id.slot_3_status_icon, calculateUnlockSeverity(prev3Count));
            }
            else{
                view.setTextViewText(R.id.slot_3_check_count, "0");
                view.setImageViewResource(R.id.slot_3_status_icon, R.drawable.ic_device_lock);
            }

            if(prev4Count != 0){
                view.setTextViewText(R.id.slot_2_check_count, prev4Count + "");
                view.setImageViewResource(R.id.slot_2_status_icon, calculateUnlockSeverity(prev4Count));
            }
            else{
                view.setTextViewText(R.id.slot_2_check_count, "0");
                view.setImageViewResource(R.id.slot_2_status_icon, R.drawable.ic_device_lock);
            }

            if(prev5Count != 0){
                view.setTextViewText(R.id.slot_1_check_count, prev5Count + "");
                view.setImageViewResource(R.id.slot_1_status_icon, calculateUnlockSeverity(prev1Count));
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
    public int calculateUnlockSeverity(int extra){

        if(extra < 30){
            return R.mipmap.ic_device_lock_g_stage_1;
        }
        else if(extra >= 30 && extra < 60){
            return R.mipmap.ic_device_lock_g_stage_2;
        }
        else if(extra >= 60 && extra < 90){
            return R.mipmap.ic_device_lock_g_stage_3;
        }
        else if(extra >= 90 && extra < 120){
            return R.mipmap.ic_device_lock_o_stage_1;
        }
        else if(extra >= 120 && extra < 150){
            return R.mipmap.ic_device_lock_o_stage_2;
        }
        else if(extra >= 150 && extra < 180){
            return R.mipmap.ic_device_lock_o_stage_3;
        }
        else if(extra >= 180 && extra < 200){
            return R.mipmap.ic_device_lock_r_stage_1;
        }
        else if(extra >= 200 && extra < 225){
            return R.mipmap.ic_device_lock_r_stage_2;
        }
        else if(extra >= 225){
            return R.mipmap.ic_device_lock_r_stage_3;
        }
        else if(extra == 0){
            return R.drawable.ic_device_lock;
        }
        else{
            return R.drawable.ic_device_lock;
        }
    }

    public void setPreviousDays(String currentDay){
        if(currentDay.equals("Sunday")){
            mRemoteViews.setTextViewText(R.id.slot_5_day, "Sat");
            mRemoteViews.setTextViewText(R.id.slot_4_day, "Fri");
            mRemoteViews.setTextViewText(R.id.slot_3_day, "Thu");
            mRemoteViews.setTextViewText(R.id.slot_2_day, "Wed");
            mRemoteViews.setTextViewText(R.id.slot_1_day, "Tue");
        }
        else if(currentDay.equals("Monday")){
            mRemoteViews.setTextViewText(R.id.slot_5_day, "Sun");
            mRemoteViews.setTextViewText(R.id.slot_4_day, "Sat");
            mRemoteViews.setTextViewText(R.id.slot_3_day, "Fri");
            mRemoteViews.setTextViewText(R.id.slot_2_day, "Thu");
            mRemoteViews.setTextViewText(R.id.slot_1_day, "Wed");
        }
        else if(currentDay.equals("Tuesday")){
            mRemoteViews.setTextViewText(R.id.slot_5_day, "Mon");
            mRemoteViews.setTextViewText(R.id.slot_4_day, "Sun");
            mRemoteViews.setTextViewText(R.id.slot_3_day, "Sat");
            mRemoteViews.setTextViewText(R.id.slot_2_day, "Fri");
            mRemoteViews.setTextViewText(R.id.slot_1_day, "Thu");
        }
        else if(currentDay.equals("Wednesday")){
            mRemoteViews.setTextViewText(R.id.slot_5_day, "Tue");
            mRemoteViews.setTextViewText(R.id.slot_4_day, "Mon");
            mRemoteViews.setTextViewText(R.id.slot_3_day, "Sun");
            mRemoteViews.setTextViewText(R.id.slot_2_day, "Sat");
            mRemoteViews.setTextViewText(R.id.slot_1_day, "Fri");
        }
        else if(currentDay.equals("Thursday")){
            mRemoteViews.setTextViewText(R.id.slot_5_day, "Wed");
            mRemoteViews.setTextViewText(R.id.slot_4_day, "Tue");
            mRemoteViews.setTextViewText(R.id.slot_3_day, "Mon");
            mRemoteViews.setTextViewText(R.id.slot_2_day, "Sun");
            mRemoteViews.setTextViewText(R.id.slot_1_day, "Sat");
        }
        else if(currentDay.equals("Friday")){
            mRemoteViews.setTextViewText(R.id.slot_5_day, "Thu");
            mRemoteViews.setTextViewText(R.id.slot_4_day, "Wed");
            mRemoteViews.setTextViewText(R.id.slot_3_day, "Tue");
            mRemoteViews.setTextViewText(R.id.slot_2_day, "Mon");
            mRemoteViews.setTextViewText(R.id.slot_1_day, "Sun");
        }
        else if(currentDay.equals("Saturday")){
            mRemoteViews.setTextViewText(R.id.slot_5_day, "Fri");
            mRemoteViews.setTextViewText(R.id.slot_4_day, "Thu");
            mRemoteViews.setTextViewText(R.id.slot_3_day, "Wed");
            mRemoteViews.setTextViewText(R.id.slot_2_day, "Tue");
            mRemoteViews.setTextViewText(R.id.slot_1_day, "Mon");
        }
    }
}
