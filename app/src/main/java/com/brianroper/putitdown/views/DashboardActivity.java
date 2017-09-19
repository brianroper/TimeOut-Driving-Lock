package com.brianroper.putitdown.views;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.DrivingLogEventAdapter;
import com.brianroper.putitdown.model.events.PermissionsMessage;
import com.brianroper.putitdown.model.events.PreferenceMessage;
import com.brianroper.putitdown.utils.Constants;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;
import com.brianroper.putitdown.model.events.DrivingMessage;
import com.brianroper.putitdown.services.gps.TimeOutMovementService;
import com.brianroper.putitdown.services.screen.ScreenService;
import com.brianroper.putitdown.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class DashboardActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 5499;
    public final static int MY_PERMISSIONS_REQUEST_OVERLAY = 5463;

    private EventBus mEventBus = EventBus.getDefault();

    @BindView(R.id.passenger_switch)
    SwitchCompat mPassengerSwitch;
    @BindView(R.id.empty_view)
    RelativeLayout mEmptyView;
    @BindView(R.id.log_view)
    RelativeLayout mLogView;

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
    @BindView(R.id.goal_content)
    TextView mGoalContent;
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
    @BindView(R.id.dashboard_log_bottom)
    RelativeLayout mTripLogBottomLayout;

    private DrivingLogEventAdapter mDrivingLogEventAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RealmResults<DrivingEventLog> mRealmResults;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ButterKnife.bind(this);

        handleAppInto();

        checkPermissions();

        getSharedPreferences();
        setPassengerSwitchPosition();

        initializeAdapter();

        setSwipeFreshListener();

        handleUIUtilities();

        //onPermissionRetryIntent();
        populateAllViews();

        //initializeExternalActivityComponents();
    }

    /**
     * 1) UI UPDATING
     * 2) EXTERNAL ACTIVITY COMPONENTS
     * 3) USER PREFERENCES
     * 4) LIFE CYCLE METHODS
     * 5) EVENT BUS SUBSCRIPTIONS (WORMHOLES)
     * 6) PERMISSIONS
     * 7) VIEW LISTENERS
     * 8) VIEW UTILITY
     */

    /**
     * UI UPDATING
     */

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
        Date currentDate = Utils.returnDateAsDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentStringDate = sdf.format(currentDate);

        for (int i = 0; i < mRealmResults.size(); i++) {

            String logDateString = sdf.format(mRealmResults.get(i).getDate());

            if(logDateString.equals(currentStringDate)){
                if(mRealmResults.get(i).isSuccessful() == true){
                    successfulTrips++;
                }
                else if(mRealmResults.get(i).isSuccessful() == false){
                    failedTrips++;
                }
            }
        }
        mTripSuccessCount.setText(successfulTrips + "");
        mTripFailedCount.setText(failedTrips + "");
    }

    /**
     * updates the data in the trip log views using the realm results
     */
    private void setTripLogViews(RealmResults<DrivingEventLog> results){

        try{
            if(mRealmResults.size() != 0 && mRealmResults.size() != 1){
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
            else if(mRealmResults.size() == 1){
                int topLog = results.size() - 1;

                mTripLogTopEventDate.setText(Utils.returnDateStringFromDate(results.get(topLog).getDate()));
                mTripLogTopEventTime.setText(results.get(topLog).getTime());

                if(results.get(topLog).isSuccessful()){
                    mTripLogTopEventName.setText("you had a safe trip");
                }
                else{
                    mTripLogTopEventName.setText("you used your device while driving");
                }
                mTripLogBottomLayout.setVisibility(View.GONE);
            }
        }
        catch (Exception e){e.printStackTrace();}
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
        int goal = mSharedPreferences.getInt("goal", 10);
        boolean isGoalSet = mSharedPreferences.getBoolean("goalSet", false);
        int goalWeek = mSharedPreferences.getInt("goalWeek", 0);
        mGoalCount.setText(goal + "");
        mGoalSeekbar.setProgress(goal);
        setGoalSeekBarVisibility(isGoalSet);
        getGoalDate(goalWeek);
    }

    /**
     * sets the visibility of the seek bar depending on if a current goal is set
     */
    public void setGoalSeekBarVisibility(boolean isGoalSet){
        if(isGoalSet){
            mGoalSeekbar.setVisibility(View.GONE);
            mGoalContent.setText("You have set a goal for this week!");
        }
        else if(!isGoalSet){
            mGoalSeekbar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * compares the goal date to the current date and returns true if they match
     */
    public void getGoalDate(int goalDate){
        Calendar calendar = Calendar.getInstance();
        int currentDate = calendar.get(Calendar.WEEK_OF_YEAR);
        if (currentDate == goalDate){
            mGoalSeekbar.setVisibility(View.GONE);
        }
        else{
            mGoalSeekbar.setVisibility(View.VISIBLE);
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
     * END OF UI UPDATES
     */

    /**
     * EXTERNAL ACTIVITY COMPONENTS
     */

    /**
     * initializes all adapters and services
     */
    public void initializeExternalActivityComponents(){
        initializeMovementService();
        initializeScreenService();
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
     * starts the service that monitors screen counts for the widget
     */
    public void initializeScreenService(){
        Intent screenService = new Intent(getApplicationContext(), ScreenService.class);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(screenService);
            Log.i("AndroidVersion: ", "Oreo");
        }
        else{
            startService(screenService);
            Log.i("AndroidVersion: ", Build.VERSION.SDK_INT + "");
        }
    }

    /**
     * starts the service that monitors vehicle movement
     */
    public void initializeMovementService(){
        Intent locationIntent = new Intent(getApplicationContext(), TimeOutMovementService.class);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(locationIntent);
            Log.i("AndroidVersion: ", "Oreo");
        }
        else{
            startService(locationIntent);
            Log.i("AndroidVersion: ", Build.VERSION.SDK_INT + "");
        }
    }

    /**
     * END OF  EXTERNAL ACTIVITY COMPONENTS
     */

    /**
     * USER PREFERENCES
     */

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

        mEventBus.postSticky(new PreferenceMessage("true"));
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

        mEventBus.postSticky(new PreferenceMessage("false"));
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
     * stores goal number in shared preferences
     */
    public void storeGoal(int progress){
        mSharedPreferences.edit().putInt("goal", progress).apply();
        mSharedPreferences.edit().putBoolean("goalSet", true).apply();
        Calendar calender = Calendar.getInstance();
        mSharedPreferences.edit().putInt("goalWeek", calender.get(Calendar.WEEK_OF_YEAR)).apply();
    }

    /**
     * END OF USER PREFERENCES
     */

    /**
     * LIFE CYCLE METHODS
     * excludes OnCreate()
     */

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
     * END OF LIFE CYCLE METHODS
     */

    /**
     * EVENT BUS SUBSCRIPTIONS (WORMHOLES)
     */

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
     * listens for permission denied/retry message
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPermissionsMessageEvent(PermissionsMessage permissionsMessage){
        if(permissionsMessage.message.equals("denied")) {
            sendPermissionDeniedNotification();
        }
        else if(permissionsMessage.message.equals("retry")){
            checkPermissions();
        }
    }

    /**
     * listens for permission denied/retry message
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPreferenceMessageEvent(PreferenceMessage preferenceMessage){
        if(preferenceMessage.message.equals("true")) {
            mPassengerSwitch.setChecked(true);
        }
        else if(preferenceMessage.message.equals("false")){
            mPassengerSwitch.setChecked(false);
        }
    }

    /**
     * END OF EVENT BUS SUBSCRIPTIONS
     */

    /**
     * PERMISSIONS
     */

    /**
     * checks all permissions required
     */
    public void checkPermissions(){
        checkDrawOverlayPermission();
        checkDoNotDisturbPermissions();
        checkLocationPermission();
    }

    /**
     * checks for the do not disturb permission
     */
    public void checkDoNotDisturbPermissions(){
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

    /**
     * checks for overlay permission
     */
    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OVERLAY);
            }
        }
    }

    /**
     * checks for overlay permission
     */
    public void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    /**
     * handles the result of the permissions request
     * if the grantResults array size is greater than zero than the permission
     * was granted by the user and we can start the app services that depend on these
     * permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    //location permission was granted, time to start our services
                    initializeExternalActivityComponents();
                    Log.i("GPS_Service: ", "initialized");
                    populateAllViews();
                }
                else{
                    //location permission was denied and we need to notify the user that the app
                    //will not function properly without it
                    mEventBus.postSticky(new PermissionsMessage("denied"));
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * send a notification to the user informing them that the app will not function properly
     * without the permissions being granted. User will have option to gran permissions from
     * notification
     */
    public void sendPermissionDeniedNotification(){
        //notification will offer permissions when clicked
        Intent permissionIntent = new Intent(getApplicationContext(), DashboardActivity.class);
        permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        permissionIntent.putExtra("retryPermission", "retry");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, permissionIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.redcar)
                        .setContentTitle(
                                getApplicationContext()
                                        .getResources()
                                        .getString(R.string.notification_permission_denied_title))
                        .setContentText(
                                getApplicationContext()
                                        .getResources()
                                        .getString(R.string.notification_permission_denied_content))
                        .addAction(R.drawable.redcar,
                                getApplicationContext().getString(R.string.notification_permission_denied_button),
                                pendingIntent);

        //shows notification text on the status bar when received
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);

        //allows for the full content of longer facts to be displayed in the notification
        NotificationCompat.BigTextStyle bigTextStyle =
                new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.notification_permission_denied_title));
        bigTextStyle.bigText(getString(R.string.notification_permission_denied_content));
        builder.setStyle(bigTextStyle);

        //sends the notification
        NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(003, builder.build());
    }

    /**
     * watches for the incoming intent extra that indicates we want to retry the permission requests
     */
    public void onPermissionRetryIntent(){
        Intent incomingIntent = getIntent();
        boolean isRetryPermission = incomingIntent.getBooleanExtra("retryPermission", true);
        if(isRetryPermission){
            checkPermissions();
        }
    }

    /**
     * END OF PERMISSIONS
     */

    /**
     * VIEW LISTENERS
     */

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
     * handle switch event for passenger switch
     */
    @OnCheckedChanged(R.id.passenger_switch)
    public void setPassengerSwitchListener(){
        if(mPassengerSwitch.isChecked()){
            enablePassengerMode();
        }
        else if(!mPassengerSwitch.isChecked()){
            disablePassengerMode();
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

    @OnClick(R.id.settings_button)
    public void setSettingsButtonListener(){
        Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * END OF VIEW LISTENERS
     */

    /**
     * VIEW UTILITY
     */

    /**
     * handles all the ui updating methods
     */
    public void handleUIUtilities(){
        handleSeekBar();
        handleStatusBarColor();
        handleCardViewBackgroundColors();
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
                                setGoalCountTextView();
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
     * decides if the app is running for the first time or not
     */
    public void handleAppInto(){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                if(isFirstStart){
                    final Intent introIntent = new Intent(DashboardActivity.this, IntroActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(introIntent);
                        }
                    });

                    SharedPreferences.Editor editor = getPrefs.edit();
                    editor.putBoolean("firstStart", false);
                    editor.apply();
                }
            }
        });
        thread.start();
    }

    /**
     * END OF VIEW UTILITY
     */

    @OnClick(R.id.test_button)
    public void setTestListener(){

        final boolean isSuccessful = true;
        final Calendar calendar = Calendar.getInstance();
        Realm realm;
        Realm.init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DrivingEventLog drivingEventLog = realm.createObject(DrivingEventLog.class, Utils.returnDateAsDate().getTime() + "");
                drivingEventLog.setTime(Utils.returnTime(calendar));
                drivingEventLog.setDate(Utils.returnDateAsDate());
                drivingEventLog.setSuccessful(isSuccessful);
                realm.copyToRealmOrUpdate(drivingEventLog);
            }
        });
        realm.close();
    }
}