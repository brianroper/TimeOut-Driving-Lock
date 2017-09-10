package com.brianroper.putitdown.services.neura;

import android.util.Log;

import com.brianroper.putitdown.model.events.TokenRefreshedMessage;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by brianroper on 9/8/17.
 */

public class NeuraTokenManager extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        postEvent();

        Log.i("Neura Token: ", "refreshed");
    }

    public void postEvent(){
        EventBus.getDefault().postSticky(new TokenRefreshedMessage("refreshed"));
    }
}
