package com.brianroper.putitdown.services.neura;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.resources.authentication.AnonymousAuthenticationStateListener;
import com.neura.resources.authentication.AuthenticationState;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;

import com.neura.standalonesdk.util.SDKUtils;

import com.neura.resources.authentication.AnonymousAuthenticateCallBack;
import com.neura.resources.authentication.AnonymousAuthenticateData;
import com.neura.sdk.object.AnonymousAuthenticationRequest;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.object.EventDefinition;

import java.util.Arrays;
import java.util.List;

/**
 * Created by brianroper on 9/5/17.
 */

public class NeuraConnectionService extends Service {

    private NeuraApiClient mNeuraApiClient;

    //Define moments you would like to subscribe to.
    List<String> mNeuraMoments = Arrays.asList("userStartedWalking",
            "userStartedDriving", "userFinishedDriving",
            "userIsAboutToGoToSleep", "userStartedRunning");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeNeura();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initializeNeura(){
        if(Utils.activeNetworkCheck(this)){
            connectToNeura();
            callNeura();
        }
        else{
            Utils.noActiveNetworkNotification(this);
        }
    }

    public void connectToNeura(){
        Builder builder = new Builder(getApplicationContext());
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(getString(R.string.app_uid));
        mNeuraApiClient.setAppSecret(getString(R.string.app_secret));
        mNeuraApiClient.connect();
    }

    public void callNeura(){

        String pushToken = FirebaseInstanceId.getInstance().getToken();

        AnonymousAuthenticationRequest request = new AnonymousAuthenticationRequest(pushToken);

        final AnonymousAuthenticationStateListener authStateListener = new AnonymousAuthenticationStateListener() {
            @Override
            public void onStateChanged(AuthenticationState state) {
                switch (state){
                    case AuthenticatedAnonymously:
                        //successfully authenticated
                        Log.i("NeuraAuth: ", "Success");
                        mNeuraApiClient.unregisterAuthStateListener();
                        break;

                    case AccessTokenRequested:
                        break;

                    case NotAuthenticated:
                        break;

                    case FailedReceivingAccessToken:
                        //authentication failed indefinitely. Consider retrying authentication flow
                        Log.i("NeuraAuth: ", "Failed");
                        mNeuraApiClient.unregisterAuthStateListener();
                        connectToNeura();
                        break;

                    default:
                }
            }
        };

        mNeuraApiClient.authenticate(request, new AnonymousAuthenticateCallBack() {
            @Override
            public void onSuccess(AnonymousAuthenticateData authenticationData) {
                Log.i(getClass().getSimpleName(), "Successfully requested authentication with neura. " +
                        "NeuraUserId = " + authenticationData.getNeuraUserId());

                mNeuraApiClient.registerAuthStateListener(authStateListener);
                subscribeToNeuraMoments(mNeuraMoments, authenticationData.getNeuraUserId());
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. "
                        + "Reason : " + SDKUtils.errorCodeToString(errorCode));
            }
        });
    }

    public void subscribeToNeuraMoments(List<String> moments, String neuraId){

        Log.i("Moment: ", moments.get(0));

        for (int i = 0; i < moments.size(); i++) {
            mNeuraApiClient.subscribeToEvent(moments.get(i),
                    neuraId + moments.get(i),
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
}
