package com.brianroper.putitdown;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.sdk.object.EventDefinition;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

public class DashboardActivity extends AppCompatActivity {

    private NeuraApiClient mNeuraApiClient;
    public final static int REQUEST_CODE = 5463;
    private boolean mOverlayPermission = false;
    @BindView(R.id.log_recycler)
    RecyclerView mNeuraEventLogRecycler;
    NeuraEventAdapter mNeuraEventAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RealmResults<NeuraEventLog> mRealmResults;
    @BindView(R.id.trip_count)
    TextView mTripCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ButterKnife.bind(this);

        checkDrawOverlayPermission();

        connectNeura();

        Intent neuraService = new Intent(getApplicationContext(), NeuraMonitorService.class);
        startService(neuraService);

        initializeAdapter();
        setTripTextView();
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

                mNeuraApiClient.registerFirebaseToken(
                        DashboardActivity.this, FirebaseInstanceId.getInstance().getToken());

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

    /**
     * check for overlay permission
     */
    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                permissionGranted();
            }
        } else
            permissionGranted();
    }

    public void permissionGranted() {
        mOverlayPermission = true;
    }

    private void initializeAdapter(){
        mNeuraEventAdapter = new NeuraEventAdapter(getApplicationContext());
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mNeuraEventLogRecycler.setLayoutManager(mLinearLayoutManager);
        mRealmResults = mNeuraEventAdapter.getNeuraEventLogDataFromRealm();
        mNeuraEventLogRecycler.setAdapter(mNeuraEventAdapter);
    }

    private void setTripTextView(){
        int trips = mRealmResults.size() / 2;
        mTripCountTextView.setText(trips + "");
    }
}
