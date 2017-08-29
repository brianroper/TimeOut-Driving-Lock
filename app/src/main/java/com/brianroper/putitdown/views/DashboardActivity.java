package com.brianroper.putitdown.views;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.NeuraEventAdapter;
import com.brianroper.putitdown.model.Constants;
import com.brianroper.putitdown.model.DrivingMessage;
import com.brianroper.putitdown.model.NeuraEventLog;
import com.brianroper.putitdown.services.NeuraMonitorService;
import com.brianroper.putitdown.services.ScreenService;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.realm.RealmResults;

public class DashboardActivity extends AppCompatActivity {

    private NeuraApiClient mNeuraApiClient;
    public final static int REQUEST_CODE = 5463;
    private EventBus mEventBus = EventBus.getDefault();

    @BindView(R.id.log_recycler)
    RecyclerView mNeuraEventLogRecycler;
    @BindView(R.id.passenger_switch)
    SwitchCompat mPassengerSwitch;
    @BindView(R.id.empty_view)
    RelativeLayout mEmptyView;
    @BindView(R.id.log_view)
    RelativeLayout mLogView;
    @BindView(R.id.test_button)
    Button mButton;

    //Redesign Views
    @BindView(R.id.trip_success_count)
    TextView mTripSuccessCount;
    @BindView(R.id.trip_failed_count)
    TextView mTripFailedCount;
    @BindView(R.id.trip_day_of_week)
    TextView mTripDayOfWeek;
    @BindView(R.id.trip_date)
    TextView mTripDate;
    @BindView(R.id.surface_goal)
    CardView mSurfaceGoal;
    @BindView(R.id.surface_log)
    CardView mSurfaceLog;
    @BindView(R.id.surface_switch)
    CardView mSurfaceSwitch;
    @BindView(R.id.surface_trips)
    CardView mSurfaceTrips;


    private NeuraEventAdapter mNeuraEventAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RealmResults<NeuraEventLog> mRealmResults;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_redesign);

        ButterKnife.bind(this);

        handleCardViewBackgroundColors();

        //TODO: move overlay permission check to on boarding
        checkDrawOverlayPermission();

        getSharedPreferences();

        monitorNeura();

        initializeAdapter();
        setTripTextView();

        handleDoNotDisturbPermissions();

        initializeScreenService();
    }

    /**
     * handles cardview background colors
     */
    public void handleCardViewBackgroundColors(){
        mSurfaceGoal.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfaceLog.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfaceSwitch.setCardBackgroundColor(getResources().getColor(R.color.white));
        mSurfaceTrips.setCardBackgroundColor(getResources().getColor(R.color.white));
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
                Utils.noActiveNetworkToast(this);
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
        handleEmptyView(mRealmResults);
        mNeuraEventLogRecycler.setAdapter(mNeuraEventAdapter);
    }

    /**
     * changes empty view to visible when there is no data in adapter
     */
    private void handleEmptyView(RealmResults<NeuraEventLog> results){
        if(results.size()==0){
           mEmptyView.setVisibility(View.VISIBLE);
           mLogView.setVisibility(View.GONE);
        }
        else{
            mEmptyView.setVisibility(View.GONE);
            mLogView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * sets the text using realm results for the trips text view
     */
    private void setTripTextView(){
        int trips = mRealmResults.size() / 2;
        mTripSuccessCount.setText(trips + "");
    }

    /**
     * handle switch event for passenger switch
     */
    @OnCheckedChanged(R.id.passenger_switch)
    public void setPassengerSwitchListener(){
        stopNeuraService();
        if(mPassengerSwitch.isChecked()){
            mSharedPreferences
                    .edit()
                    .putBoolean(getString(R.string.passenger_mode_key), true)
                    .apply();
            initializeNeuraService();
        }
        else if(!mPassengerSwitch.isChecked()){
            mSharedPreferences
                    .edit()
                    .putBoolean(getString(R.string.passenger_mode_key), false)
                    .apply();
            initializeNeuraService();
        }
    }

    /**
     * begins a new NeuraMonitorService
     */
    private void initializeNeuraService(){
        Intent neuraService = new Intent(getApplicationContext(), NeuraMonitorService.class);
        startService(neuraService);
    }

    /**
     * stops a running NeuraMonitorService
     */
    private void stopNeuraService(){
        Intent neuraService = new Intent(getApplicationContext(), NeuraMonitorService.class);
        stopService(neuraService);
    }

    /**
     * returns shared preferences and sets default passenger value
     */
    private void getSharedPreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.passenger_mode_key), false)
                .apply();
    }

    /**
     * checks for the do not disturb permission
     */
    public void handleDoNotDisturbPermissions(){
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @OnClick(R.id.test_button)
    public void setTestButton(){
        mNeuraApiClient.simulateAnEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    /**
     * listens for a DrivingMessage from the DrivingEventService when it completes
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDrivingMessageEvent(DrivingMessage drivingMessage){
        Constants constants = new Constants();
        if(drivingMessage.message == constants.DRIVING_EVENT_FINISHED) {
            handleAdapterDataSet();
        }
    }

    /**
     * refreshes the data in the adapter
     */
    public void handleAdapterDataSet(){
        mNeuraEventAdapter.getNeuraEventLogDataFromRealm();
        mNeuraEventAdapter.notifyDataSetChanged();
        setTripTextView();
        handleEmptyView(mRealmResults);
    }

    public void initializeScreenService(){
        Intent screenService = new Intent(getApplicationContext(), ScreenService.class);
        startService(screenService);
    }
}
