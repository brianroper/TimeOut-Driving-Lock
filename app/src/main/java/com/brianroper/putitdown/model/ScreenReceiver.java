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
        }
        else{
            Log.d("ScreenStatus", "Inactive");
        }
    }
}

