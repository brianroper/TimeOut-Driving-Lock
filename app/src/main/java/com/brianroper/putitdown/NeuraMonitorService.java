package com.brianroper.putitdown;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.EventDefinition;
import com.neura.sdk.object.Permission;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;

import java.util.ArrayList;

/**
 * Created by brianroper on 5/2/17.
 */

public class NeuraMonitorService extends Service {

    private NeuraApiClient mNeuraApiClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        monitorNeura();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void monitorNeura(){
        connectNeura();
    }

    public void connectNeura() {
        Builder builder = new Builder(getApplicationContext());
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(getString(R.string.app_uid));
        mNeuraApiClient.setAppSecret(getString(R.string.app_secret));
        mNeuraApiClient.connect();
        callNeura();
    }

    public void callNeura() {
        AuthenticationRequest request = new AuthenticationRequest(
                Permission.list(new String[]{Utils.FINISHED_DRIVING, Utils.STARTED_DRIVING}));

        mNeuraApiClient.authenticate(request, new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. " +
                        "NeuraUserId = " + authenticateData.getNeuraUserId() + " " +
                        "AccessToken = " + authenticateData.getAccessToken());

                ArrayList<EventDefinition> events = authenticateData.getEvents();
                //Subscribe to the events you wish Neura to alert you :
                for (int i = 0; i < events.size(); i++) {
                    mNeuraApiClient.subscribeToEvent(events.get(i).getName(),
                            "YourEventIdentifier_" + events.get(i).getName(),
                            new SubscriptionRequestCallbacks() {
                                @Override
                                public void onSuccess(String eventName, Bundle bundle, String s1) {
                                    Log.i(getClass().getSimpleName(), "Successfully subscribed to event " + eventName);
                                }

                                @Override
                                public void onFailure(String eventName, Bundle bundle, int i) {
                                    Log.e(getClass().getSimpleName(), "Failed to subscribe to event " + eventName);
                                }
                            });
                }
            }

            @Override
            public void onFailure(int i) {
                Log.e("Neura Authentication: ", "Failed");
            }
        });
    }
}
