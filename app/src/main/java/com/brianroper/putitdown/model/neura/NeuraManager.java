package com.brianroper.putitdown.model.neura;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.views.DashboardActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.resources.authentication.AnonymousAuthenticateCallBack;
import com.neura.resources.authentication.AnonymousAuthenticateData;
import com.neura.resources.authentication.AnonymousAuthenticationStateListener;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.resources.authentication.AuthenticationState;
import com.neura.sdk.callbacks.SubscriptionRequestCallbacks;
import com.neura.sdk.object.AnonymousAuthenticationRequest;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by brianroper on 9/6/17.
 */

public class NeuraManager {

    private static NeuraManager sInstance;
    private NeuraApiClient mNeuraApiClient;

    public static NeuraManager getInstance() {
        if (sInstance == null)
            sInstance = new NeuraManager();
        return sInstance;
    }

    private NeuraManager() {

    }

    public NeuraApiClient getClient() {
        return mNeuraApiClient;
    }

    public void initNeuraConnection(Context context) {
        Builder builder = new Builder(context);
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(context.getResources().getString(R.string.app_uid));
        mNeuraApiClient.setAppSecret(context.getResources().getString(R.string.app_secret));
        mNeuraApiClient.connect();
    }

    public void authenticateWithNeura(){
        AuthenticationRequest request = new AuthenticationRequest();

        mNeuraApiClient.authenticate(request, new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. NeuraUserId = "
                        + authenticateData.getNeuraUserId() + ". AccessToken = " + authenticateData.getAccessToken());

                List<String> moments = Arrays.asList("userStartedWalking",
                        "userStartedDriving", "userFinishedDriving",
                        "userIsAboutToGoToSleep", "userStartedRunning");

                for (int i = 0; i < moments.size(); i++) {
                    String momentIdentifier = authenticateData.getNeuraUserId() + "_" + moments.get(i);
                    mNeuraApiClient.subscribeToEvent(moments.get(i), momentIdentifier, mSubscriptionRequest);
                }
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. Reason : "
                        + SDKUtils.errorCodeToString(errorCode));
            }
        });
    }

    public void authenticateWithNeuraAnonymously(final Activity activity){
        final String pushToken = FirebaseInstanceId.getInstance().getToken();

        AnonymousAuthenticationRequest request = new AnonymousAuthenticationRequest(pushToken);

        final AnonymousAuthenticationStateListener authStateListener = new AnonymousAuthenticationStateListener() {
            @Override
            public void onStateChanged(AuthenticationState state) {
                switch (state){
                    case AccessTokenRequested:
                        Log.i("NeuraAuth: ", "Token Requested");
                        break;

                    case AuthenticatedAnonymously:
                        //successfully authenticated
                        Log.i("NeuraAuth: ", "Success");
                        NeuraManager.getInstance().getClient().unregisterAuthStateListener();
                        break;

                    case NotAuthenticated:
                        Log.i("NeuraAuth: ", "Not Authenticated");
                        break;

                    case FailedReceivingAccessToken:
                        //authentication failed indefinitely. Consider retrying authentication flow
                        Log.i("NeuraAuth: ", "Failed Access Token");
                        NeuraManager.getInstance().getClient().unregisterAuthStateListener();
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

                mNeuraApiClient.registerFirebaseToken(activity, FirebaseInstanceId.getInstance().getToken());

                List<String> moments = Arrays.asList("userStartedWalking",
                        "userStartedDriving", "userFinishedDriving",
                        "userIsAboutToGoToSleep", "userStartedRunning");

                for (int i = 0; i < moments.size(); i++) {
                    String momentIdentifier = authenticationData.getNeuraUserId() + "_" + moments.get(i);
                    mNeuraApiClient.subscribeToEvent(moments.get(i), momentIdentifier, mSubscriptionRequest);
                }

                mNeuraApiClient.registerAuthStateListener(authStateListener);
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. "
                        + "Reason : " + SDKUtils.errorCodeToString(errorCode));
                Log.i("NeuraError: ", errorCode+"");
            }
        });
    }

    private com.neura.sdk.service.SubscriptionRequestCallbacks mSubscriptionRequest =
            new com.neura.sdk.service.SubscriptionRequestCallbacks() {

        @Override
        public void onSuccess(String momentName, Bundle bundle, String s1) {
            Log.i(getClass().getSimpleName(), "Successfully subscribed to event : " + momentName);
        }

        @Override
        public void onFailure(String momentName, Bundle bundle, int i) {
            Log.w(getClass().getSimpleName(), "Failed to subscribe to event : " + momentName);
        }
    };
}
