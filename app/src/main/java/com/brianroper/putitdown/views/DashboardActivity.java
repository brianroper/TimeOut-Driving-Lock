package com.brianroper.putitdown.views;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.DrivingLogEventAdapter;
import com.brianroper.putitdown.model.Constants;
import com.brianroper.putitdown.model.neura.NeuraManager;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;
import com.brianroper.putitdown.model.events.DrivingMessage;
import com.brianroper.putitdown.services.neura.NeuraConnectionService;
import com.brianroper.putitdown.services.screen.ScreenService;
import com.brianroper.putitdown.utils.Utils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.resources.authentication.AnonymousAuthenticateCallBack;
import com.neura.resources.authentication.AnonymousAuthenticateData;
import com.neura.resources.authentication.AnonymousAuthenticationStateListener;
import com.neura.resources.authentication.AuthenticationState;
import com.neura.sdk.object.AnonymousAuthenticationRequest;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.standalonesdk.util.SDKUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.realm.RealmResults;

public class DashboardActivity extends AppCompatActivity {

    //neura related variables
    List<String> mNeuraMoments = Arrays.asList("userStartedWalking",
            "userStartedDriving", "userFinishedDriving",
            "userIsAboutToGoToSleep", "userStartedRunning");

    public final static int REQUEST_CODE = 5463;
    private EventBus mEventBus = EventBus.getDefault();

    @BindView(R.id.passenger_switch)
    SwitchCompat mPassengerSwitch;
    @BindView(R.id.empty_view)
    RelativeLayout mEmptyView;
    @BindView(R.id.log_view)
    RelativeLayout mLogView;

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
    @BindView(R.id.goal_seek_bar)
    AppCompatSeekBar mGoalSeekbar;
    @BindView(R.id.goal_seek_bar_count)
    TextView mGoalCount;
    @BindView(R.id.dashboard_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * views for the recent trip log
     */
    @BindView(R.id.top_event_name)
    TextView mTripLogTopEventName;
    @BindView(R.id.top_event_date)
    TextView mTripLogTopEventDate;
    @BindView(R.id.top_event_time)
    TextView mTripLogTopEventTime;
    @BindView(R.id.bottom_event_date)
    TextView mTripBottomEventDate;
    @BindView(R.id.bottom_event_time)
    TextView mTripBottomEventTime;
    @BindView(R.id.bottom_event_name)
    TextView mTripBottomEventName;

    private DrivingLogEventAdapter mDrivingLogEventAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RealmResults<DrivingEventLog> mRealmResults;
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
        setPassengerSwitchPosition();

        monitorNeura();

        initializeAdapter();
        populateAllViews();

        setSwipeFreshListener();

        handleDoNotDisturbPermissions();

        initializeScreenService();

        handleSeekBar();
        handleStatusBarColor();
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
     * sets the color of the status bar
     */
    public void handleStatusBarColor(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    /**
     * populates all the views for this activity
     */
    public void populateAllViews(){
        setTripTextView();
        setTripDateTextView();
        setTripDayOfWeekTextView();
        setGoalCountTextView();
        setTripLogViews(mRealmResults);
    }

    /**
     * manages the calls to the Neura api
     */
    public void monitorNeura(){
        if(NeuraManager.getInstance().getClient()==null){
            if(Utils.activeNetworkCheck(this)){
                connectToNeura();
                callNeura();
            }
            else{
                Utils.noActiveNetworkToast(this);
            }
        }
        else{
            Log.i("NeuraClient: ", "Not Null");
        }
    }

    public void connectToNeura(){
        NeuraManager.getInstance().initNeuraConnection(getApplicationContext());
    }

    public void callNeura(){
        final String pushToken = FirebaseInstanceId.getInstance().getToken();

        AnonymousAuthenticationRequest request = new AnonymousAuthenticationRequest(pushToken);

        NeuraManager.getInstance().getClient().registerFirebaseToken(DashboardActivity.this, FirebaseInstanceId.getInstance().getToken());

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
                        subscribeToNeuraMoments(mNeuraMoments, "randomID");
                        NeuraManager.getInstance().getClient().unregisterAuthStateListener();
                        break;

                    case NotAuthenticated:
                        Log.i("NeuraAuth: ", "Not Authenticated");
                        break;

                    case FailedReceivingAccessToken:
                        //authentication failed indefinitely. Consider retrying authentication flow
                        Log.i("NeuraAuth: ", "Failed Access Token");
                        NeuraManager.getInstance().getClient().unregisterAuthStateListener();
                        connectToNeura();
                        break;

                    default:
                }
            }
        };

        NeuraManager.getInstance().getClient().authenticate(request, new AnonymousAuthenticateCallBack() {
            @Override
            public void onSuccess(AnonymousAuthenticateData authenticationData) {
                Log.i(getClass().getSimpleName(), "Successfully requested authentication with neura. " +
                        "NeuraUserId = " + authenticationData.getNeuraUserId());

                NeuraManager.getInstance().getClient().registerAuthStateListener(authStateListener);
            }

            @Override
            public void onFailure(int errorCode) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. "
                        + "Reason : " + SDKUtils.errorCodeToString(errorCode));
                Log.i("NeuraError: ", errorCode+"");
            }
        });
    }

    public void subscribeToNeuraMoments(List<String> moments, String neuraId){

        Log.i("Moment: ", moments.get(0));

        if(NeuraManager.getInstance().getClient().isLoggedIn()){
            for (int i = 0; i < moments.size(); i++) {
                NeuraManager.getInstance().getClient().subscribeToEvent(moments.get(i),
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
        else{
            Log.i("NeuraClient: ", "User is not logged in");
        }
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
        mDrivingLogEventAdapter = new DrivingLogEventAdapter(getApplicationContext());
        mRealmResults = mDrivingLogEventAdapter.getDrivingEventLogFromRealm();
        handleEmptyView(mRealmResults);
        setTripLogViews(mRealmResults);
    }

    /**
     * changes empty view to visible when there is no data in adapter
     */
    private void handleEmptyView(RealmResults<DrivingEventLog> results){
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
        int successfulTrips = 0;
        int failedTrips = 0;
        for (int i = 0; i < mRealmResults.size(); i++) {
            if(mRealmResults.get(i).isSuccessful() == true){
                successfulTrips++;
            }
            else if(mRealmResults.get(i).isSuccessful() == false){
                failedTrips++;
            }
        }
        mTripSuccessCount.setText(successfulTrips + "");
        mTripFailedCount.setText(failedTrips + "");
    }

    private void setTripLogViews(RealmResults<DrivingEventLog> results){

        try{
            if(mRealmResults.size() != 0){
                int topLog = results.size() - 1;
                int bottomLog = results.size() - 2;

                mTripLogTopEventDate.setText(Utils.returnDateStringFromDate(results.get(topLog).getDate()));
                mTripBottomEventDate.setText(Utils.returnDateStringFromDate(results.get(bottomLog).getDate()));

                mTripBottomEventTime.setText(results.get(bottomLog).getTime());
                mTripLogTopEventTime.setText(results.get(topLog).getTime());

                if(results.get(topLog).isSuccessful()){
                    mTripLogTopEventName.setText("you had a safe trip");
                }
                else{
                    mTripLogTopEventName.setText("you used your device while driving");
                }

                if(results.get(bottomLog).isSuccessful()){
                    mTripBottomEventName.setText("you had a safe trip");
                }
                else{
                    mTripBottomEventName.setText("you used your device while driving");
                }
            }
        }
        catch (Exception e){

        }
    }

    /**
     * handle switch event for passenger switch
     */
    @OnCheckedChanged(R.id.passenger_switch)
    public void setPassengerSwitchListener(){
        //stopNeuraService();
        if(mPassengerSwitch.isChecked()){
            enablePassengerMode();
            //initializeNeuraService();
        }
        else if(!mPassengerSwitch.isChecked()){
            disablePassengerMode();
            //initializeNeuraService();
        }
    }

    /**
     * sets the on swipe listener for the refresh view
     */
    public void setSwipeFreshListener(){
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateAllViews();
                handleAdapterDataSet();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * begins a new NeuraConnectionService
     */
    private void initializeNeuraService(){
        Intent neuraService = new Intent(getApplicationContext(), NeuraConnectionService.class);
        startService(neuraService);
    }

    /**
     * stops a running NeuraConnectionService
     */
    private void stopNeuraService(){
        Intent neuraService = new Intent(getApplicationContext(), NeuraConnectionService.class);
        stopService(neuraService);
    }

    /**
     * returns shared preferences and sets default passenger value
     */
    private void getSharedPreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * updates passenger shared preferences value
     */
    private void enablePassengerMode(){
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.passenger_mode_key), true)
                .apply();

        Toast.makeText(getApplicationContext(), "Passenger mode enabled", Toast.LENGTH_LONG).show();
    }

    /**
     * updates passenger shared preferences value
     */
    private void disablePassengerMode(){
        mSharedPreferences
                .edit()
                .putBoolean(getString(R.string.passenger_mode_key), false)
                .apply();

        Toast.makeText(getApplicationContext(), "Passenger mode disabled", Toast.LENGTH_LONG).show();
    }

    /**
     * updates passenger switch ui
     */
    public void setPassengerSwitchPosition(){
        boolean passengerMode = mSharedPreferences.getBoolean(getString(R.string.passenger_mode_key), false);

        if(passengerMode){
            mPassengerSwitch.setChecked(true);
        }
        else if(!passengerMode){
            mPassengerSwitch.setChecked(false);
        }
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

    @OnClick(R.id.simulate_button)
    public void setTestButton(){
        if(Utils.activeNetworkCheck(getApplicationContext())){
            if(NeuraManager.getInstance().getClient().isLoggedIn()){
                NeuraManager.getInstance().getClient().simulateAnEvent();
            }
        }
        else{
            Utils.noActiveNetworkToast(getApplicationContext());
        }
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
     * listens for a DrivingMessage from the NeuraMomentMessageService when it completes
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
        //mDrivingLogEventAdapter.getDrivingEventLogFromRealm();
        mDrivingLogEventAdapter.notifyDataSetChanged();
        setTripTextView();
        handleEmptyView(mRealmResults);
    }

    /**
     * starts the service that monitors screen counts for the widget
     */
    public void initializeScreenService(){
        Intent screenService = new Intent(getApplicationContext(), ScreenService.class);
        startService(screenService);
    }

    /**
     * listener for the surface log
     */
    @OnClick(R.id.surface_log)
    public void setSurfaceLogListener(){
        Intent logIntent = new Intent(getApplicationContext(), DrivingLogActivity.class);
        startActivity(logIntent);
    }

    /**
     * listener for the trip surface
     */
    @OnClick(R.id.surface_trips)
    public void setSurfaceTripListener(){
        Intent logIntent = new Intent(getApplicationContext(), DrivingLogActivity.class);
        startActivity(logIntent);
    }

    /**
     * sets the text for the date textview
     */
    public void setTripDateTextView(){
        mTripDate.setText(Utils.returnFullDate());
    }

    /**
     * sets the text for the day of the week textview
     */
    public void setTripDayOfWeekTextView(){
        mTripDayOfWeek.setText(Utils.returnDayOfWeek());
    }

    /**
     * sets the stored value for the goal amount
     */
    public void setGoalCountTextView(){
        int goal = mSharedPreferences.getInt("goal", 25);
        mGoalCount.setText(goal + "");
        mGoalSeekbar.setProgress(goal);
    }

    /**
     * manages the seekbar
     */
    public void handleSeekBar(){
        mGoalSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGoalCount.setText(progress + "");
                Log.i("GoalProgress: ", progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setTitle("Set a goal")
                        .setMessage("Would you like to set " + seekBar.getProgress() +" as your personal goal?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storeGoal(seekBar.getProgress());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setGoalCountTextView();
                            }
                        })
                        .show();
            }
        });
    }

    /**
     * stores goal number in shared preferences
     */
    public void storeGoal(int progress){
        mSharedPreferences.edit().putInt("goal", progress).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NeuraManager.getInstance().getClient().disconnect();
    }

    @OnClick(R.id.test_list_button)
    public void setTestButtonListener(){
        Intent testIntent = new Intent(getApplicationContext(), MomentTestActivity.class);
        startActivity(testIntent);
    }

    @OnClick(R.id.authenticate_button)
    public void setAuthButton(){
        NeuraManager.getInstance().authenticateWithNeura();
    }
}