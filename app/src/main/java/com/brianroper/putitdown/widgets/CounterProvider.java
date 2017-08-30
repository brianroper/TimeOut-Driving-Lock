package com.brianroper.putitdown.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.utils.Utils;

/**
 * Created by brianroper on 8/10/17.
 */

public class CounterProvider  extends AppWidgetProvider{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_counter);

        remoteViews.setTextViewText(R.id.widget_dayofweek, Utils.returnDayOfWeek());
        remoteViews.setTextViewText(R.id.widget_date, Utils.returnFullDate());

        ComponentName counterWidget = new ComponentName(context, CounterProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(counterWidget, remoteViews);
    }
}
