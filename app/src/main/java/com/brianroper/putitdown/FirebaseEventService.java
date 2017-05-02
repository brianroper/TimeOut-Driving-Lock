package com.brianroper.putitdown;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import java.util.Map;

/**
 * Created by brianroper on 5/1/17.
 */

public class FirebaseEventService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map data = remoteMessage.getData();
        if (NeuraPushCommandFactory.getInstance().isNeuraEvent(data)) {
            NeuraEvent event = NeuraPushCommandFactory.getInstance().getEvent(data);
            Log.i(getClass().getSimpleName(), "received Neura event - " + event.toString());
        }
    }
}
