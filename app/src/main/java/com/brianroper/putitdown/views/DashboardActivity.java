package com.brianroper.putitdown.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.TextView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.NeuraEventAdapter;
import com.brianroper.putitdown.model.NeuraEventLog;
import com.brianroper.putitdown.services.NeuraMonitorService;
import com.brianroper.putitdown.utils.Utils;
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
import butterknife.OnCheckedChanged;
import io.realm.RealmResults;

public class DashboardActivity extends AppCompatActivity {

    private NeuraApiClient mNeuraApiClient;
    public final static int REQUEST_CODE = 5463;

    @BindView(R.id.log_recycler)
    RecyclerView mNeuraEventLogRecycler;
    @BindView(R.id.trip_count)
    TextView mTripCountTextView;
    @BindView(R.id.passenger_switch)
    SwitchCompat mPassengerSwitch;

    private NeuraEventAdapter mNeuraEventAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RealmResults<NeuraEventLog> mRealmResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ButterKnife.bind(this);

        //TODO: move overlay permission check to on boarding
        checkDrawOverlayPermission();

        monitorNeura();

        initializeAdapter();
        setTripTextView();
    }

    /**
     * manages the calls to the Neura api
     */
    public void monitorNeura(){
        if(mNeuraApiClient==null){
            if(Utils.activeNetworkCheck(this)){
                connectNeura();
                initializeNeuraService();
            }
            else{
                Utils.activeNetworkCheck(this);
            }
        }
    }

    /**
     * connects to the Neura Api using AppUid and AppSecret
     */
    public void connectNeura() {
        Builder builder = new Builder(getApplicationContext());
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(getString(R.string.app_uid));
        mNeuraApiClient.setAppSecret(getString(R.string.app_secret));
        mNeuraApiClient.connect();
        callNeura();
    }

    /**
     * calls the Neura Api and subscribes to the userStartedDriving and userFinishedDriving events
     */
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
     * checks for overlay permission
     */
    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    /**
     * initializes the adapter to the Neura Log Data recycler view
     */
    private void initializeAdapter(){
        mNeuraEventAdapter = new NeuraEventAdapter(getApplicationContext());
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        mNeuraEventLogRecycler.setLayoutManager(mLinearLayoutManager);
        mRealmResults = mNeuraEventAdapter.getNeuraEventLogDataFromRealm();
        mNeuraEventLogRecycler.setAdapter(mNeuraEventAdapter);
    }

    /**
     * sets the text using realm results for the trips text view
     */
    private void setTripTextView(){
        int trips = mRealmResults.size() / 2;
        mTripCountTextView.setText(trips + "");
    }

    /**
     * handle switch event for passenger switch
     */
    @OnCheckedChanged(R.id.passenger_switch)
    public void setPassengerSwitchListener(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(mPassengerSwitch.isChecked()){
            sharedPreferences
                    .edit()
                    .putBoolean(getString(R.string.passenger_mode_key), true)
                    .apply();
        }
        else if(!mPassengerSwitch.isChecked()){
            sharedPreferences
                    .edit()
                    .putBoolean(getString(R.string.passenger_mode_key), false)
                    .apply();
        }
    }

    /**
     * begins a new NeuraMonitorService
     */
    private void initializeNeuraService(){
        Intent neuraService = new Intent(getApplicationContext(), NeuraMonitorService.class);
        startService(neuraService);
    }
}
